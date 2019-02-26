package com.blackducksoftware.integration.jira.common;

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

    public Integer getBlackDuckTimeout() {
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

    public Integer getBlackDuckProxyPasswordLength() {
        return getIntegerValue(BlackDuckConfigKeys.CONFIG_PROXY_PASS_LENGTH);
    }

    public void setBlackDuckProxyPasswordLength(final Integer length) {
        setValue(BlackDuckConfigKeys.CONFIG_PROXY_PASS_LENGTH, length);
    }

    public Integer getBlackDuckProxyPort() {
        return getIntegerValue(BlackDuckConfigKeys.CONFIG_PROXY_PORT);
    }

    public void setBlackDuckProxyPort(final Integer port) {
        setValue(BlackDuckConfigKeys.CONFIG_PROXY_PORT, port);
    }

    public String getStringValue(final String key) {
        return (String) pluginSettings.get(key);
    }

    public Integer getIntegerValue(final String key) {
        if (NumberUtils.isParsable(key)) {
            return Integer.parseInt(getStringValue(key));
        }

        return null;
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
