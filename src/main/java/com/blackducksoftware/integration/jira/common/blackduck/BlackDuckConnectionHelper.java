/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.jira.common.blackduck;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.notification.CommonNotificationService;
import com.blackducksoftware.integration.jira.common.notification.NotificationContentDetailFactory;
import com.blackducksoftware.integration.jira.config.JiraConfigErrorStrings;
import com.blackducksoftware.integration.jira.config.model.BlackDuckServerConfigSerializable;
import com.google.gson.JsonParser;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.synopsys.integration.blackduck.rest.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.ProjectService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.util.IntEnvironmentVariables;

public class BlackDuckConnectionHelper {

    public BlackDuckConnectionHelper() {
    }

    public Optional<BlackDuckServicesFactory> createBlackDuckServicesFactorySafely(final BlackDuckJiraLogger logger, final BlackDuckServerConfigBuilder blackDuckServerConfigBuilder) {
        try {
            final BlackDuckServicesFactory blackDuckServicesFactory = createBlackDuckServicesFactory(logger, blackDuckServerConfigBuilder);
            return Optional.ofNullable(blackDuckServicesFactory);
        } catch (final IntegrationException e) {
            logger.error("Unable to connect to Black Duck. This could mean Black Duck is currently unreachable, or that the Black Duck JIRA plugin is not (yet) configured correctly: " + e.getMessage());
        }

        return Optional.empty();
    }

    public BlackDuckServicesFactory createBlackDuckServicesFactory(final BlackDuckJiraLogger logger, final BlackDuckServerConfigBuilder blackDuckServerConfigBuilder) throws IntegrationException {
        final BlackDuckHttpClient restConnection = createRestConnection(logger, blackDuckServerConfigBuilder);
        return new BlackDuckServicesFactory(new IntEnvironmentVariables(), BlackDuckServicesFactory.createDefaultGson(), BlackDuckServicesFactory.createDefaultObjectMapper(), null, restConnection, logger);
    }

    public BlackDuckHttpClient createRestConnection(final BlackDuckJiraLogger logger, final BlackDuckServerConfigBuilder blackDuckServerConfigBuilder) throws IntegrationException {
        if (StringUtils.isBlank(blackDuckServerConfigBuilder.getApiToken())) {
            throw new IntegrationException(JiraConfigErrorStrings.BLACKDUCK_SERVER_MISCONFIGURATION + " " + JiraConfigErrorStrings.CHECK_BLACKDUCK_SERVER_CONFIGURATION);
        }

        final BlackDuckServerConfig serverConfig;
        try {
            logger.debug("Building Black Duck configuration");
            serverConfig = blackDuckServerConfigBuilder.build();
            logger.debug("Finished building Black Duck configuration for " + serverConfig.getBlackDuckUrl());
        } catch (final IllegalStateException e) {
            logger.error("Error in Black Duck server configuration: " + e.getMessage());
            throw new IntegrationException(JiraConfigErrorStrings.CHECK_BLACKDUCK_SERVER_CONFIGURATION);
        }

        final BlackDuckHttpClient restConnection;
        try {
            restConnection = serverConfig.createBlackDuckHttpClient(logger);
        } catch (final IllegalArgumentException e) {
            throw new IntegrationException(JiraConfigErrorStrings.CHECK_BLACKDUCK_SERVER_CONFIGURATION + " :: " + e.getMessage());
        }
        return restConnection;
    }

    public CommonNotificationService createCommonNotificationService(final BlackDuckServicesFactory blackDuckServicesFactory, final boolean notificationsOldestFirst) {
        final NotificationContentDetailFactory contentDetailFactory = new NotificationContentDetailFactory(blackDuckServicesFactory.getGson(), new JsonParser());
        return new CommonNotificationService(contentDetailFactory, notificationsOldestFirst);
    }

    public List<String> getBlackDuckProjects(final BlackDuckServicesFactory blackDuckServicesFactory) throws IntegrationException {
        final List<String> blackDuckProjects = new ArrayList<>();

        final ProjectService projectRequestService = blackDuckServicesFactory.createProjectService();
        final List<ProjectView> blackDuckProjectItems = projectRequestService.getAllProjectMatches(null);

        if (blackDuckProjectItems != null && !blackDuckProjectItems.isEmpty()) {
            for (final ProjectView project : blackDuckProjectItems) {
                final List<String> allowedMethods = project.getAllowedMethods();
                if (allowedMethods != null && !allowedMethods.isEmpty() && allowedMethods.contains("GET") && allowedMethods.contains("PUT")) {
                    blackDuckProjects.add(project.getName());
                }
            }
        }
        return blackDuckProjects;
    }

    public Optional<String> validateBlackDuckUrl(final String url) {
        if (StringUtils.isNotBlank(url)) {
            try {
                final URL hubURL = new URL(url);
                hubURL.toURI();
                return Optional.empty();
            } catch (final MalformedURLException | URISyntaxException e) {
                return Optional.of("The Hub Url is not a valid URL.");
            }
        }
        return Optional.of("No Hub Url was found.");
    }

    public Optional<String> validateBlackDuckTimeout(final String timeout) {
        if (StringUtils.isNotBlank(timeout)) {
            try {
                final Integer intTimeout = Integer.valueOf(timeout);
                if (intTimeout <= 0) {
                    return Optional.of("Timeout must be greater than 0.");
                }
                return Optional.empty();
            } catch (final NumberFormatException e) {
                return Optional.of(String.format("The String : %s, is not an Integer.", timeout));
            }
        }
        return Optional.of("No Hub Timeout was found.");
    }

    public Optional<String> validateBlackDuckApiToken(final String apiToken) {
        if (StringUtils.isBlank(apiToken)) {
            return Optional.of("No api token was found.");
        }
        return Optional.empty();
    }

    public void validateProxy(final BlackDuckServerConfigSerializable config) {
        final String proxyHost = config.getHubProxyHost();
        final String proxyPort = config.getHubProxyPort();
        final String proxyUser = config.getHubProxyUser();
        final String proxyPassword = config.getHubProxyPassword();

        if (StringUtils.isBlank(proxyHost) && StringUtils.isNotBlank(proxyPort)) {
            config.setHubProxyHostError("Proxy host not specified.");
        }
        if (StringUtils.isNotBlank(proxyHost) && StringUtils.isBlank(proxyPort)) {
            config.setHubProxyPortError("Proxy port not specified.");
        } else if (StringUtils.isNotBlank(proxyHost) && StringUtils.isNotBlank(proxyPort)) {
            try {
                final Integer port = Integer.valueOf(proxyPort);
                if (port <= 0) {
                    config.setHubProxyPortError("Proxy port must be greater than 0.");
                }
            } catch (final NumberFormatException e) {
                config.setHubProxyPortError(String.format("The String : %s, is not an Integer.", proxyPort));
            }
        }

        if (StringUtils.isNotBlank(proxyUser) && StringUtils.isNotBlank(proxyPassword) && StringUtils.isBlank(proxyHost)) {
            config.setHubProxyHostError("Proxy host not specified.");
        }
        if (StringUtils.isNotBlank(proxyUser) && StringUtils.isBlank(proxyPassword)) {
            config.setHubProxyPasswordError("Proxy password not specified.");
        } else if (StringUtils.isBlank(proxyUser) && StringUtils.isNotBlank(proxyPassword)) {
            config.setHubProxyUserError("Proxy user not specified.");
        }
    }

}
