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
package com.blackducksoftware.integration.jira.common;

import java.util.Optional;

import org.apache.commons.lang3.math.NumberUtils;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.blackducksoftware.integration.jira.config.BlackDuckConfigKeys;

public class PluginSettingsWrapper {
    private final PluginSettings pluginSettings;

    public PluginSettingsWrapper(final PluginSettings pluginSettings) {
        this.pluginSettings = pluginSettings;
    }

    public String getBlackDuckUrl() {
        return getStringValue(BlackDuckConfigKeys.CONFIG_BLACKDUCK_URL);
    }

    public void setBlackDuckUrl(final String url) {
        setValue(BlackDuckConfigKeys.CONFIG_BLACKDUCK_URL, url);
    }

    public String getBlackDuckApiToken() {
        return getStringValue(BlackDuckConfigKeys.CONFIG_BLACKDUCK_API_TOKEN);
    }

    public void setBlackDuckApiToken(final String apiToken) {
        setValue(BlackDuckConfigKeys.CONFIG_BLACKDUCK_API_TOKEN, apiToken);
    }

    public Optional<Integer> getBlackDuckTimeout() {
        return getIntegerValue(BlackDuckConfigKeys.CONFIG_BLACKDUCK_TIMEOUT);
    }

    public void setBlackDuckTimeout(final Integer timeout) {
        setValue(BlackDuckConfigKeys.CONFIG_BLACKDUCK_TIMEOUT, timeout);
    }

    public Boolean getBlackDuckAlwaysTrust() {
        return getBooleanValue(BlackDuckConfigKeys.CONFIG_BLACKDUCK_TRUST_CERT);
    }

    public void setBlackDuckAlwaysTrust(final Boolean alwaysTrust) {
        setValue(BlackDuckConfigKeys.CONFIG_BLACKDUCK_TRUST_CERT, alwaysTrust);
    }

    public String getBlackDuckProxyHost() {
        return getStringValue(BlackDuckConfigKeys.CONFIG_PROXY_HOST);
    }

    public void setBlackDuckProxyHost(final String host) {
        setValue(BlackDuckConfigKeys.CONFIG_PROXY_HOST, host);
    }

    public String getBlackDuckProxyUser() {
        return getStringValue(BlackDuckConfigKeys.CONFIG_PROXY_USER);
    }

    public void setBlackDuckProxyUser(final String user) {
        setValue(BlackDuckConfigKeys.CONFIG_PROXY_USER, user);
    }

    public String getBlackDuckProxyPassword() {
        return getStringValue(BlackDuckConfigKeys.CONFIG_PROXY_PASS);
    }

    public void setBlackDuckProxyPassword(final String password) {
        setValue(BlackDuckConfigKeys.CONFIG_PROXY_PASS, password);
    }

    public Optional<Integer> getBlackDuckProxyPasswordLength() {
        return getIntegerValue(BlackDuckConfigKeys.CONFIG_PROXY_PASS_LENGTH);
    }

    public void setBlackDuckProxyPasswordLength(final Integer length) {
        setValue(BlackDuckConfigKeys.CONFIG_PROXY_PASS_LENGTH, length);
    }

    public Optional<Integer> getBlackDuckProxyPort() {
        return getIntegerValue(BlackDuckConfigKeys.CONFIG_PROXY_PORT);
    }

    public void setBlackDuckProxyPort(final Integer port) {
        setValue(BlackDuckConfigKeys.CONFIG_PROXY_PORT, port);
    }

    public String getStringValue(final String key) {
        return (String) pluginSettings.get(key);
    }

    public Optional<Integer> getIntegerValue(final String key) {
        String value = getStringValue(key);
        if (NumberUtils.isParsable(value)) {
            return Optional.of(Integer.parseInt(value));
        }
        return Optional.empty();
    }

    public Boolean getBooleanValue(final String key) {
        return Boolean.parseBoolean(getStringValue(key));
    }

    public void setValue(final String key, final Object value) {
        if (value == null) {
            pluginSettings.remove(key);
        } else {
            pluginSettings.put(key, String.valueOf(value));
        }
    }
}
