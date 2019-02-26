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
package com.blackducksoftware.integration.jira.config.controller;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.PluginSettingsWrapper;
import com.blackducksoftware.integration.jira.config.model.BlackDuckServerConfigSerializable;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;

public class BlackDuckConfigActions {
    final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));

    public BlackDuckServerConfigSerializable getStoredBlackDuckConfig(final PluginSettingsWrapper pluginSettingsWrapper) {
        final String blackDuckUrl = pluginSettingsWrapper.getBlackDuckUrl();
        logger.debug(String.format("Returning Black Duck details for %s", blackDuckUrl));
        final String apiToken = pluginSettingsWrapper.getBlackDuckApiToken();
        final Optional<Integer> timeout = pluginSettingsWrapper.getBlackDuckTimeout();
        final Boolean trustCert = pluginSettingsWrapper.getBlackDuckAlwaysTrust();
        final String proxyHost = pluginSettingsWrapper.getBlackDuckProxyHost();
        final Optional<Integer> proxyPort = pluginSettingsWrapper.getBlackDuckProxyPort();
        final String proxyUser = pluginSettingsWrapper.getBlackDuckProxyUser();
        final String proxyPassword = pluginSettingsWrapper.getBlackDuckProxyPassword();
        final Optional<Integer> proxyPasswordLength = pluginSettingsWrapper.getBlackDuckProxyPasswordLength();

        final BlackDuckServerConfigSerializable config = new BlackDuckServerConfigSerializable();
        config.setHubUrl(blackDuckUrl);
        if (StringUtils.isNotBlank(apiToken)) {
            config.setApiTokenLength(apiToken.length());
            config.setApiToken(config.getMaskedApiToken());
        }
        timeout.ifPresent(value -> {
            config.setTimeout(String.valueOf(value));
        });

        config.setTrustCert(trustCert);
        config.setHubProxyHost(proxyHost);
        proxyPort.ifPresent(value -> {
            config.setHubProxyPort(String.valueOf(value));
        });
        config.setHubProxyUser(proxyUser);
        if (StringUtils.isNotBlank(proxyPassword)) {
            proxyPasswordLength
                .filter(value -> value > 0)
                .ifPresent(value -> {
                    config.setHubProxyPasswordLength(value);
                    config.setHubProxyPassword(config.getMaskedProxyPassword());
                });
        }
        validateAndUpdateErrorsOnConfig(config);
        return config;
    }

    public BlackDuckServerConfigSerializable updateBlackDuckConfig(final BlackDuckServerConfigSerializable config, final PluginSettingsWrapper pluginSettingsWrapper) {
        final BlackDuckServerConfigSerializable newConfig = new BlackDuckServerConfigSerializable(config);

        logger.debug(String.format("Saving connection to %s...", newConfig.getHubUrl()));
        pluginSettingsWrapper.setBlackDuckUrl(newConfig.getHubUrl());
        if (StringUtils.isBlank(newConfig.getApiToken())) {
            pluginSettingsWrapper.setBlackDuckApiToken(null);
        } else if (!newConfig.isApiTokenMasked()) {
            pluginSettingsWrapper.setBlackDuckApiToken(newConfig.getApiToken());
        }
        pluginSettingsWrapper.setBlackDuckTimeout(Integer.parseInt(newConfig.getTimeout()));
        pluginSettingsWrapper.setBlackDuckAlwaysTrust(Boolean.parseBoolean(newConfig.getTrustCert()));
        pluginSettingsWrapper.setBlackDuckProxyHost(newConfig.getHubProxyHost());
        Integer proxyPortValue = null;
        if (StringUtils.isNotBlank(newConfig.getHubProxyPort())) {
            proxyPortValue = Integer.parseInt(newConfig.getHubProxyPort());
        }
        pluginSettingsWrapper.setBlackDuckProxyPort(proxyPortValue);
        pluginSettingsWrapper.setBlackDuckProxyUser(newConfig.getHubProxyUser());

        final String proxyPassword = newConfig.getHubProxyPassword();
        if (StringUtils.isBlank(proxyPassword)) {
            pluginSettingsWrapper.setBlackDuckProxyPassword(null);
            pluginSettingsWrapper.setBlackDuckProxyPasswordLength(null);
        } else if (!newConfig.isProxyPasswordMasked()) {
            // only update the stored password if it is not the masked password used for display
            pluginSettingsWrapper.setBlackDuckProxyPassword(proxyPassword);
        }
        validateAndUpdateErrorsOnConfig(newConfig);
        return newConfig;
    }

    public BlackDuckServerConfigSerializable testConnection(final BlackDuckServerConfigSerializable config, final PluginSettingsWrapper pluginSettingsWrapper) {
        BlackDuckServerConfigSerializable newConfig = new BlackDuckServerConfigSerializable(config);
        validateAndUpdateErrorsOnConfig(newConfig);

        if (newConfig.hasErrors()) {
            return newConfig;
        } else {
            newConfig = getUnMaskedConfig(newConfig, pluginSettingsWrapper);
            final BlackDuckServerConfigBuilder serverConfigBuilder = new BlackDuckServerConfigBuilder();
            serverConfigBuilder.setLogger(logger);
            serverConfigBuilder.setUrl(newConfig.getHubUrl());
            serverConfigBuilder.setTimeout(newConfig.getTimeout());
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

    BlackDuckServerConfigSerializable getUnMaskedConfig(final BlackDuckServerConfigSerializable currentConfig, final PluginSettingsWrapper pluginSettingsWrapper) {
        final BlackDuckServerConfigSerializable newConfig = new BlackDuckServerConfigSerializable(currentConfig);

        if (StringUtils.isNotBlank(newConfig.getApiToken()) && newConfig.isApiTokenMasked()) {
            newConfig.setApiToken(pluginSettingsWrapper.getBlackDuckApiToken());
        }
        if (StringUtils.isNotBlank(newConfig.getHubProxyPassword()) && newConfig.isProxyPasswordMasked()) {
            newConfig.setHubProxyPassword(pluginSettingsWrapper.getBlackDuckProxyPassword());
            pluginSettingsWrapper.getBlackDuckProxyPasswordLength().ifPresent(newConfig::setHubProxyPasswordLength);
        }

        return newConfig;
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
