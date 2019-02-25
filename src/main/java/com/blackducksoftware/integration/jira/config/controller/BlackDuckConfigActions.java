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
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.config.BlackDuckConfigKeys;
import com.blackducksoftware.integration.jira.config.model.BlackDuckServerConfigSerializable;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;

public class BlackDuckConfigActions {
    final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));

    public BlackDuckServerConfigSerializable getStoredBlackDuckConfig(final PluginSettings settings) {
        final String blackDuckUrl = getValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_URL);
        logger.debug(String.format("Returning Black Duck details for %s", blackDuckUrl));
        final String apiToken = getValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_API_TOKEN);
        final String timeout = getValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_TIMEOUT);
        final String trustCert = getValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_TRUST_CERT);
        final String proxyHost = getValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_HOST);
        final String proxyPort = getValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_PORT);
        final String noProxyHosts = getValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_NO_HOST);
        final String proxyUser = getValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_USER);
        final String proxyPassword = getValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_PASS);
        final String proxyPasswordLength = getValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_PASS_LENGTH);

        final BlackDuckServerConfigSerializable config = new BlackDuckServerConfigSerializable();
        config.setHubUrl(blackDuckUrl);
        if (StringUtils.isNotBlank(apiToken)) {
            config.setApiTokenLength(apiToken.length());
            config.setApiToken(config.getMaskedApiToken());
        }
        config.setTimeout(timeout);
        config.setTrustCert(trustCert);
        config.setHubProxyHost(proxyHost);
        config.setHubProxyPort(proxyPort);
        config.setHubNoProxyHosts(noProxyHosts);
        config.setHubProxyUser(proxyUser);
        if (StringUtils.isNotBlank(proxyPassword)) {
            final int blackDuckProxyPasswordLength = getIntFromObject(proxyPasswordLength);
            if (blackDuckProxyPasswordLength > 0) {
                config.setHubProxyPasswordLength(blackDuckProxyPasswordLength);
                config.setHubProxyPassword(config.getMaskedProxyPassword());
            }
        }
        validateAndUpdateErrorsOnConfig(config);
        return config;
    }

    public BlackDuckServerConfigSerializable updateBlackDuckConfig(final BlackDuckServerConfigSerializable config, final PluginSettings settings) {
        final BlackDuckServerConfigSerializable newConfig = new BlackDuckServerConfigSerializable(config);

        logger.debug(String.format("Saving connection to %s...", newConfig.getHubUrl()));
        setValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_URL, newConfig.getHubUrl());
        setValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_API_TOKEN, newConfig.getApiToken());
        setValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_TIMEOUT, newConfig.getTimeout());
        setValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_TRUST_CERT, newConfig.getTrustCert());
        setValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_HOST, newConfig.getHubProxyHost());
        setValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_PORT, newConfig.getHubProxyPort());
        setValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_NO_HOST, newConfig.getHubNoProxyHosts());
        setValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_USER, newConfig.getHubProxyUser());

        final String proxyPassword = newConfig.getHubProxyPassword();
        if (StringUtils.isBlank(proxyPassword)) {
            setValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_PASS, null);
            setValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_PASS_LENGTH, null);
        } else if (!newConfig.isProxyPasswordMasked()) {
            // only update the stored password if it is not the masked password used for display
            setValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_PASS, proxyPassword);
        }
        validateAndUpdateErrorsOnConfig(newConfig);
        return newConfig;
    }

    public BlackDuckServerConfigSerializable testConnection(final BlackDuckServerConfigSerializable config, final PluginSettings settings) {
        BlackDuckServerConfigSerializable newConfig = new BlackDuckServerConfigSerializable(config);
        validateAndUpdateErrorsOnConfig(newConfig);

        if (newConfig.hasErrors()) {
            return newConfig;
        } else {
            newConfig = getUnMaskedConfig(newConfig, settings);
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

    BlackDuckServerConfigSerializable getUnMaskedConfig(final BlackDuckServerConfigSerializable currentConfig, final PluginSettings settings) {
        final BlackDuckServerConfigSerializable newConfig = new BlackDuckServerConfigSerializable(currentConfig);

        if (StringUtils.isNotBlank(newConfig.getApiToken()) && newConfig.isApiTokenMasked()) {
            newConfig.setApiToken(getValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_API_TOKEN));
        }
        if (StringUtils.isNotBlank(newConfig.getHubProxyPassword()) && newConfig.isProxyPasswordMasked()) {
            newConfig.setHubProxyPassword(getValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_PASS));
            newConfig.setHubProxyPasswordLength(NumberUtils.toInt(getValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_PASS_LENGTH)));
        }

        return newConfig;
    }

    // This method must be "package protected" to avoid synthetic access
    void validateAndUpdateErrorsOnConfig(final BlackDuckServerConfigSerializable config) {
        validateBlackDuckUrl(config);
        validateBlackDuckTimeout(config);
        validateBlackDuckCredentials(config);
        validateProxyHostAndPort(config);
        validateProxyCredentials(config);
        validateIgnoreHosts(config);
    }

    private void validateBlackDuckUrl(final BlackDuckServerConfigSerializable config) {
        if (StringUtils.isBlank(config.getHubUrl())) {
            config.setHubUrlError("No Hub Url was found.");
        } else {
            try {
                final URL hubURL = new URL(config.getHubUrl());
                hubURL.toURI();
            } catch (final MalformedURLException | URISyntaxException e) {
                config.setHubUrlError("The Hub Url is not a valid URL.");
                return;
            }
        }
    }

    private void validateBlackDuckTimeout(final BlackDuckServerConfigSerializable config) {
        if (StringUtils.isBlank(config.getTimeout())) {
            config.setTimeoutError("No Hub Timeout was found.");
        } else {
            try {
                final Integer timeout = Integer.valueOf(config.getTimeout());
                if (timeout <= 0) {
                    config.setTimeoutError("Timeout must be greater than 0.");
                }
            } catch (final NumberFormatException e) {
                config.setTimeoutError(String.format("The String : %s, is not an Integer.", config.getTimeout()));
            }
        }
    }

    private void validateBlackDuckCredentials(final BlackDuckServerConfigSerializable config) {
        if (StringUtils.isBlank(config.getApiToken())) {
            config.setApiTokenError("No api token was found.");
        }
    }

    private void validateProxyHostAndPort(final BlackDuckServerConfigSerializable config) {

        if (StringUtils.isBlank(config.getHubProxyHost()) && StringUtils.isNotBlank(config.getHubProxyPort())) {
            config.setHubProxyHostError("Proxy host not specified.");
        }
        if (StringUtils.isNotBlank(config.getHubProxyHost()) && StringUtils.isBlank(config.getHubProxyPort())) {
            config.setHubProxyPortError("Proxy port not specified.");
        } else if (StringUtils.isNotBlank(config.getHubProxyHost()) && StringUtils.isNotBlank(config.getHubProxyPort())) {
            try {
                final Integer timeout = Integer.valueOf(config.getHubProxyPort());
                if (timeout <= 0) {
                    config.setHubProxyPortError("Proxy port must be greater than 0.");
                }
            } catch (final NumberFormatException e) {
                config.setHubProxyPortError(String.format("The String : %s, is not an Integer.", config.getHubProxyPort()));
            }
        }
    }

    public void validateProxyCredentials(final BlackDuckServerConfigSerializable config) {
        if (StringUtils.isNotBlank(config.getHubProxyUser()) && StringUtils.isNotBlank(config.getHubProxyPassword()) && StringUtils.isBlank(config.getHubProxyHost())) {
            config.setHubProxyHostError("Proxy host not specified.");
        }
        if (StringUtils.isNotBlank(config.getHubProxyUser()) && StringUtils.isBlank(config.getHubProxyPassword())) {
            config.setHubProxyPasswordError("Proxy password not specified.");
        } else if (StringUtils.isBlank(config.getHubProxyUser()) && StringUtils.isNotBlank(config.getHubProxyPassword())) {
            config.setHubProxyUserError("Proxy user not specified.");
        }

    }

    public void validateIgnoreHosts(final BlackDuckServerConfigSerializable config) {
        final String ignoredProxyHosts = config.getHubNoProxyHosts();
        if (StringUtils.isNotBlank(ignoredProxyHosts)) {
            if (StringUtils.isBlank(config.getHubProxyHost())) {
                config.setHubProxyHostError("Proxy host not specified.");
            }
            try {
                if (ignoredProxyHosts.contains(",")) {
                    String[] ignoreHosts = null;
                    ignoreHosts = ignoredProxyHosts.split(",");
                    for (final String ignoreHost : ignoreHosts) {
                        Pattern.compile(ignoreHost.trim());
                    }
                } else {
                    Pattern.compile(ignoredProxyHosts);
                }
            } catch (final PatternSyntaxException ex) {
                config.setHubNoProxyHostsError("Proxy ignore hosts does not compile to a valid regular expression.");
            }
        }
    }

    // This method must be "package protected" to avoid synthetic access
    int getIntFromObject(final String value) {
        if (StringUtils.isNotBlank(value)) {
            return NumberUtils.toInt(value);
        }
        return 0;
    }

    // This method must be "package protected" to avoid synthetic access
    String getValue(final PluginSettings settings, final String key) {
        return (String) settings.get(key);
    }

    // This method must be "package protected" to avoid synthetic access
    void setValue(final PluginSettings settings, final String key, final Object value) {
        if (value == null) {
            settings.remove(key);
        } else {
            settings.put(key, String.valueOf(value));
        }
    }
}
