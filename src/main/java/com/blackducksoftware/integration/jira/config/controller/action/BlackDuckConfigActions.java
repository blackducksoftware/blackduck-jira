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
package com.blackducksoftware.integration.jira.config.controller.action;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.BlackDuckProjectMappings;
import com.blackducksoftware.integration.jira.common.exception.ConfigurationException;
import com.blackducksoftware.integration.jira.common.model.PolicyRuleSerializable;
import com.blackducksoftware.integration.jira.common.settings.GlobalConfigurationAccessor;
import com.blackducksoftware.integration.jira.common.settings.JiraSettingsAccessor;
import com.blackducksoftware.integration.jira.common.settings.model.PluginBlackDuckServerConfigModel;
import com.blackducksoftware.integration.jira.common.settings.model.PluginIssueCreationConfigModel;
import com.blackducksoftware.integration.jira.config.JiraConfigErrorStrings;
import com.blackducksoftware.integration.jira.config.model.BlackDuckJiraConfigSerializable;
import com.blackducksoftware.integration.jira.config.model.BlackDuckServerConfigSerializable;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.rest.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.ProjectService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.exception.IntegrationRestException;
import com.synopsys.integration.util.IntEnvironmentVariables;

public class BlackDuckConfigActions {
    private final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final GlobalConfigurationAccessor globalConfigurationAccessor;

    public BlackDuckConfigActions(final JiraSettingsAccessor jiraSettingsAccessor) {
        this.globalConfigurationAccessor = jiraSettingsAccessor.createGlobalConfigurationAccessor();
    }

    public BlackDuckServerConfigSerializable getStoredBlackDuckConfig() {
        final BlackDuckServerConfigSerializable config = new BlackDuckServerConfigSerializable();
        final PluginBlackDuckServerConfigModel blackDuckServerConfig = globalConfigurationAccessor.getBlackDuckServerConfig();

        final String blackDuckUrl = blackDuckServerConfig.getUrl();
        logger.debug(String.format("Returning Black Duck details for %s", blackDuckUrl));

        config.setHubUrl(blackDuckUrl);
        if (StringUtils.isNotBlank(blackDuckServerConfig.getApiToken())) {
            config.setApiTokenLength(blackDuckServerConfig.getApiToken().length());
            config.setApiToken(config.getMaskedApiToken());
        }

        config.setTrustCert(blackDuckServerConfig.getTrustCert());
        config.setHubProxyHost(blackDuckServerConfig.getProxyHost());
        blackDuckServerConfig.getProxyPort().ifPresent(value -> {
            config.setHubProxyPort(String.valueOf(value));
        });
        config.setHubProxyUser(blackDuckServerConfig.getProxyUsername());
        if (StringUtils.isNotBlank(blackDuckServerConfig.getProxyPassword())) {
            config.setHubProxyPasswordLength(blackDuckServerConfig.getProxyPassword().length());
            config.setHubProxyPassword(config.getMaskedProxyPassword());
        }
        validateAndUpdateErrorsOnConfig(config);
        return config;
    }

    public BlackDuckServerConfigSerializable updateBlackDuckConfig(final BlackDuckServerConfigSerializable config) {
        final BlackDuckServerConfigSerializable newConfig = new BlackDuckServerConfigSerializable(config);

        final Integer intTimeout = null != newConfig.getTimeout() ? Integer.parseInt(newConfig.getTimeout()) : null;
        final Integer proxyPort = null != newConfig.getHubProxyPort() ? Integer.parseInt(newConfig.getHubProxyPort()) : null;
        final Boolean trustCert = Boolean.parseBoolean(newConfig.getTrustCert());
        final PluginBlackDuckServerConfigModel pluginBlackDuckServerConfigModel =
            new PluginBlackDuckServerConfigModel(newConfig.getHubUrl(), newConfig.getApiToken(), intTimeout, trustCert, newConfig.getHubProxyHost(), proxyPort, newConfig.getHubProxyUser(), newConfig.getHubProxyPassword());

        logger.debug(String.format("Saving connection to %s...", newConfig.getHubUrl()));
        globalConfigurationAccessor.setBlackDuckServerConfig(pluginBlackDuckServerConfigModel);

        validateAndUpdateErrorsOnConfig(newConfig);
        return newConfig;
    }

    public Object getBlackDuckProjects() {
        try {
            final PluginBlackDuckServerConfigModel blackDuckServerConfig = globalConfigurationAccessor.getBlackDuckServerConfig();
            final BlackDuckServicesFactory blackDuckServicesFactory = createBlackDuckServicesFactory(blackDuckServerConfig.createBlackDuckServerConfigBuilder());
            final List<String> blackDuckProjects = getBlackDuckProjects(blackDuckServicesFactory);

            if (blackDuckProjects.size() == 0) {
                return JiraConfigErrorStrings.NO_BLACKDUCK_PROJECTS_FOUND;
            }
            return blackDuckProjects;
        } catch (final ConfigurationException e) {
            return e.getMessage();
        }
    }

    public BlackDuckServerConfigSerializable testConnection(final BlackDuckServerConfigSerializable config) {
        BlackDuckServerConfigSerializable newConfig = new BlackDuckServerConfigSerializable(config);
        validateAndUpdateErrorsOnConfig(newConfig);

        if (newConfig.hasErrors()) {
            return newConfig;
        } else {
            newConfig = getUnMaskedConfig(newConfig);
            final BlackDuckServerConfigBuilder serverConfigBuilder = new BlackDuckServerConfigBuilder();
            serverConfigBuilder.setLogger(logger);
            serverConfigBuilder.setUrl(newConfig.getHubUrl());
            serverConfigBuilder.setTimeoutInSeconds(newConfig.getTimeout());
            serverConfigBuilder.setApiToken(newConfig.getApiToken());
            serverConfigBuilder.setTrustCert(newConfig.getTrustCert());

            serverConfigBuilder.setProxyHost(newConfig.getHubProxyHost());
            serverConfigBuilder.setProxyPort(newConfig.getHubProxyPort());
            serverConfigBuilder.setProxyUsername(newConfig.getHubProxyUser());
            serverConfigBuilder.setProxyPassword(newConfig.getHubProxyPassword());

            try {
                serverConfigBuilder.build();
            } catch (final IllegalStateException e) {
                newConfig.setTestConnectionError(e.getMessage());
            }

            return newConfig;
        }
    }

    public BlackDuckJiraConfigSerializable getBlackDuckPolicies() {
        final PluginIssueCreationConfigModel issueCreationConfig = globalConfigurationAccessor.getIssueCreationConfig();
        final String policyRulesJson = issueCreationConfig.getTicketCriteria().getPolicyRulesJson();
        final BlackDuckJiraConfigSerializable txConfig = new BlackDuckJiraConfigSerializable();

        if (StringUtils.isNotBlank(policyRulesJson)) {
            txConfig.setPolicyRulesJson(policyRulesJson);
        } else {
            txConfig.setPolicyRules(new ArrayList<>(0));
        }

        final BlackDuckServicesFactory blackDuckServicesFactory;
        try {
            final PluginBlackDuckServerConfigModel blackDuckServerConfig = globalConfigurationAccessor.getBlackDuckServerConfig();
            blackDuckServicesFactory = createBlackDuckServicesFactory(blackDuckServerConfig.createBlackDuckServerConfigBuilder());
            setBlackDuckPolicyRules(blackDuckServicesFactory, txConfig);
        } catch (final ConfigurationException e) {
            txConfig.setErrorMessage(e.getMessage());
        }
        return txConfig;
    }

    private BlackDuckServerConfigSerializable getUnMaskedConfig(final BlackDuckServerConfigSerializable currentConfig) {
        final PluginBlackDuckServerConfigModel blackDuckServerConfig = globalConfigurationAccessor.getBlackDuckServerConfig();
        final BlackDuckServerConfigSerializable newConfig = new BlackDuckServerConfigSerializable(currentConfig);

        if (StringUtils.isNotBlank(newConfig.getApiToken()) && newConfig.isApiTokenMasked()) {
            newConfig.setApiToken(blackDuckServerConfig.getApiToken());
        }
        if (StringUtils.isNotBlank(newConfig.getHubProxyPassword()) && newConfig.isProxyPasswordMasked()) {
            final String proxyPassword = blackDuckServerConfig.getProxyPassword();
            if (null != proxyPassword) {
                newConfig.setHubProxyPassword(proxyPassword);
                newConfig.setHubProxyPasswordLength(proxyPassword.length());
            }
        }

        return newConfig;
    }

    private BlackDuckServicesFactory createBlackDuckServicesFactory(final BlackDuckServerConfigBuilder blackDuckServerConfigBuilder) throws ConfigurationException {
        final BlackDuckHttpClient restConnection = createRestConnection(blackDuckServerConfigBuilder);
        return new BlackDuckServicesFactory(new IntEnvironmentVariables(), BlackDuckServicesFactory.createDefaultGson(), BlackDuckServicesFactory.createDefaultObjectMapper(), null, restConnection, logger);
    }

    private BlackDuckHttpClient createRestConnection(final BlackDuckServerConfigBuilder blackDuckServerConfigBuilder) throws ConfigurationException {
        if (StringUtils.isBlank(blackDuckServerConfigBuilder.getApiToken())) {
            throw new ConfigurationException(JiraConfigErrorStrings.BLACKDUCK_SERVER_MISCONFIGURATION + " " + JiraConfigErrorStrings.CHECK_BLACKDUCK_SERVER_CONFIGURATION);
        }

        final BlackDuckServerConfig serverConfig;
        try {
            serverConfig = blackDuckServerConfigBuilder.build();
        } catch (final IllegalStateException e) {
            logger.error("Error in Black Duck server configuration: " + e.getMessage());
            throw new ConfigurationException(JiraConfigErrorStrings.CHECK_BLACKDUCK_SERVER_CONFIGURATION);
        }

        final BlackDuckHttpClient restConnection;
        try {
            restConnection = serverConfig.createBlackDuckHttpClient(logger);
        } catch (final IllegalArgumentException e) {
            throw new ConfigurationException(JiraConfigErrorStrings.CHECK_BLACKDUCK_SERVER_CONFIGURATION + " :: " + e.getMessage());
        }
        return restConnection;
    }

    private List<String> getBlackDuckProjects(final BlackDuckServicesFactory blackDuckServicesFactory) throws ConfigurationException {
        final List<String> blackDuckProjects = new ArrayList<>();
        blackDuckProjects.add(BlackDuckProjectMappings.MAP_ALL_PROJECTS);

        final ProjectService projectRequestService = blackDuckServicesFactory.createProjectService();
        final List<ProjectView> blackDuckProjectItems;
        try {
            blackDuckProjectItems = projectRequestService.getAllProjectMatches(null);
        } catch (final IntegrationException e) {
            throw new ConfigurationException(e.getMessage());
        }

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

    // TODO create a BlackDuck class that handles most BlackDuck functionality and clean this method up
    private void setBlackDuckPolicyRules(final BlackDuckServicesFactory blackDuckServicesFactory, final BlackDuckJiraConfigSerializable config) {
        final List<PolicyRuleSerializable> newPolicyRules = new ArrayList<>();
        if (blackDuckServicesFactory != null) {
            final BlackDuckService blackDuckService = blackDuckServicesFactory.createBlackDuckService();
            try {
                List<PolicyRuleView> policyRules = null;
                try {
                    policyRules = blackDuckService.getAllResponses(ApiDiscovery.POLICY_RULES_LINK_RESPONSE);
                } catch (final BlackDuckIntegrationException e) {
                    config.setPolicyRulesError(e.getMessage());
                } catch (final IntegrationRestException ire) {
                    if (ire.getHttpStatusCode() == 402) {
                        config.setPolicyRulesError(JiraConfigErrorStrings.NO_POLICY_LICENSE_FOUND);
                    } else {
                        config.setPolicyRulesError(ire.getMessage());
                    }
                }

                if (policyRules != null && !policyRules.isEmpty()) {
                    for (final PolicyRuleView rule : policyRules) {
                        final PolicyRuleSerializable newRule = new PolicyRuleSerializable();
                        String description = rule.getDescription();
                        if (description == null) {
                            description = "";
                        }
                        newRule.setDescription(cleanDescription(description));
                        newRule.setName(rule.getName().trim());

                        final Optional<String> ruleHref = rule.getHref();
                        if (ruleHref.isPresent()) {
                            newRule.setPolicyUrl(ruleHref.get());
                        } else {
                            logger.error("URL for policy rule" + rule.getName() + " does not exist.");
                            config.setPolicyRulesError(JiraConfigErrorStrings.POLICY_RULE_URL_ERROR);
                            continue;
                        }

                        newRule.setEnabled(rule.getEnabled());
                        newPolicyRules.add(newRule);
                    }
                }
                if (config.getPolicyRules() != null) {
                    for (final PolicyRuleSerializable oldRule : config.getPolicyRules()) {
                        for (final PolicyRuleSerializable newRule : newPolicyRules) {
                            if (oldRule.getPolicyUrl().equals(newRule.getPolicyUrl())) {
                                newRule.setChecked(oldRule.getChecked());
                                break;
                            }
                        }
                    }
                }
            } catch (final Exception e) {
                config.setPolicyRulesError(e.getMessage());
            }
        }
        config.setPolicyRules(newPolicyRules);
        if (config.getPolicyRules().isEmpty()) {
            config.setPolicyRulesError(org.apache.commons.lang3.StringUtils.joinWith(" : ", config.getPolicyRulesError(), JiraConfigErrorStrings.NO_POLICY_RULES_FOUND_ERROR));
        }
    }

    private String cleanDescription(final String origString) {
        return removeCharsFromString(origString.trim(), "\n\r\t");
    }

    private String removeCharsFromString(final String origString, final String charsToRemoveString) {
        String cleanerString = origString;
        final char[] charsToRemove = charsToRemoveString.toCharArray();
        for (final char c : charsToRemove) {
            cleanerString = cleanerString.replace(c, ' ');
        }
        return cleanerString;

    }

    // This method must be "package protected" to avoid synthetic access
    void validateAndUpdateErrorsOnConfig(final BlackDuckServerConfigSerializable config) {
        validateBlackDuckUrl(config.getHubUrl()).ifPresent(config::setHubUrlError);
        validateBlackDuckTimeout(config.getTimeout()).ifPresent(config::setTimeoutError);
        validateBlackDuckApiToken(config.getApiToken()).ifPresent(config::setApiTokenError);
        validateProxy(config);
    }

    private Optional<String> validateBlackDuckUrl(final String url) {
        if (StringUtils.isBlank(url)) {
            return Optional.of("No Hub Url was found.");
        } else {
            try {
                final URL hubURL = new URL(url);
                hubURL.toURI();
            } catch (final MalformedURLException | URISyntaxException e) {
                return Optional.of("The Hub Url is not a valid URL.");
            }
        }
        return Optional.empty();
    }

    private Optional<String> validateBlackDuckTimeout(final String timeout) {
        if (StringUtils.isBlank(timeout)) {
            return Optional.of("No Hub Timeout was found.");
        } else {
            try {
                final Integer intTimeout = Integer.valueOf(timeout);
                if (intTimeout <= 0) {
                    return Optional.of("Timeout must be greater than 0.");
                }
            } catch (final NumberFormatException e) {
                return Optional.of(String.format("The String : %s, is not an Integer.", timeout));
            }
        }
        return Optional.empty();
    }

    private Optional<String> validateBlackDuckApiToken(final String apiToken) {
        if (StringUtils.isBlank(apiToken)) {
            return Optional.of("No api token was found.");
        }
        return Optional.empty();
    }

    private void validateProxy(final BlackDuckServerConfigSerializable config) {
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
