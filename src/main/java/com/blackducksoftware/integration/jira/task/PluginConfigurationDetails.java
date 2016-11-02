/*
 * Copyright (C) 2016 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.jira.task;

import org.apache.commons.lang3.math.NumberUtils;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.blackducksoftware.integration.atlassian.utils.HubConfigKeys;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.jira.common.HubJiraConfigKeys;

public class PluginConfigurationDetails {
    private final String hubUrl;
    private final String hubUsername;
    private final String hubPasswordEncrypted;
    private final String hubPasswordLength;
    private final String hubTimeoutString;

    private final String hubProxyHost;
    private final String hubProxyPort;
    private final String hubProxyNoHost;
    private final String hubProxyUser;
    private final String hubProxyPassEncrypted;
    private final String hubProxyPassLength;

    private final String intervalString;
    private final String projectMappingJson;
    private final String policyRulesJson;
    private final String installDateString;
    private final String lastRunDateString;

    private final String jiraUserName;
    
    public PluginConfigurationDetails(final PluginSettings settings) {
        hubUrl = getStringValue(settings, HubConfigKeys.CONFIG_HUB_URL);
        hubUsername = getStringValue(settings, HubConfigKeys.CONFIG_HUB_USER);
        hubPasswordEncrypted = getStringValue(settings, HubConfigKeys.CONFIG_HUB_PASS);
        hubPasswordLength = getStringValue(settings, HubConfigKeys.CONFIG_HUB_PASS_LENGTH);
        hubTimeoutString = getStringValue(settings, HubConfigKeys.CONFIG_HUB_TIMEOUT);

        hubProxyHost = getStringValue(settings, HubConfigKeys.CONFIG_PROXY_HOST);
        hubProxyPort = getStringValue(settings, HubConfigKeys.CONFIG_PROXY_PORT);
        hubProxyNoHost = getStringValue(settings, HubConfigKeys.CONFIG_PROXY_NO_HOST);
        hubProxyUser = getStringValue(settings, HubConfigKeys.CONFIG_PROXY_USER);
        hubProxyPassEncrypted = getStringValue(settings, HubConfigKeys.CONFIG_PROXY_PASS);
        hubProxyPassLength = getStringValue(settings, HubConfigKeys.CONFIG_PROXY_PASS_LENGTH);

        intervalString = getStringValue(settings,
                HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS);
        projectMappingJson = getStringValue(settings,
                HubJiraConfigKeys.HUB_CONFIG_JIRA_PROJECT_MAPPINGS_JSON);
        policyRulesJson = getStringValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_POLICY_RULES_JSON);
        installDateString = getStringValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_FIRST_SAVE_TIME);
        lastRunDateString = getStringValue(settings, HubJiraConfigKeys.HUB_CONFIG_LAST_RUN_DATE);

        jiraUserName = getStringValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_USER);
    }
    
    
    
    public String getHubUrl() {
        return hubUrl;
    }



    public String getHubUsername() {
        return hubUsername;
    }



    public String getHubPasswordEncrypted() {
        return hubPasswordEncrypted;
    }



    public String getHubPasswordLength() {
        return hubPasswordLength;
    }



    public String getHubTimeoutString() {
        return hubTimeoutString;
    }



    public String getHubProxyHost() {
        return hubProxyHost;
    }



    public String getHubProxyPort() {
        return hubProxyPort;
    }



    public String getHubProxyNoHost() {
        return hubProxyNoHost;
    }



    public String getHubProxyUser() {
        return hubProxyUser;
    }



    public String getHubProxyPassEncrypted() {
        return hubProxyPassEncrypted;
    }



    public String getHubProxyPassLength() {
        return hubProxyPassLength;
    }



    public String getIntervalString() {
        return intervalString;
    }



    public String getProjectMappingJson() {
        return projectMappingJson;
    }



    public String getPolicyRulesJson() {
        return policyRulesJson;
    }



    public String getInstallDateString() {
        return installDateString;
    }



    public String getLastRunDateString() {
        return lastRunDateString;
    }



    public String getJiraUserName() {
        return jiraUserName;
    }
    
    public HubServerConfigBuilder createHubServerConfigBuilder() {
        final HubServerConfigBuilder hubConfigBuilder = new HubServerConfigBuilder();
        hubConfigBuilder.setHubUrl(hubUrl);
        hubConfigBuilder.setUsername(hubUsername);
        hubConfigBuilder.setPassword(hubPasswordEncrypted);
        hubConfigBuilder.setPasswordLength(NumberUtils.toInt(hubPasswordLength));
        hubConfigBuilder.setTimeout(hubTimeoutString);

        hubConfigBuilder.setProxyHost(hubProxyHost);
        hubConfigBuilder.setProxyPort(hubProxyPort);
        hubConfigBuilder.setIgnoredProxyHosts(hubProxyNoHost);
        hubConfigBuilder.setProxyUsername(hubProxyUser);
        hubConfigBuilder.setProxyPassword(hubProxyPassEncrypted);
        hubConfigBuilder.setProxyPasswordLength(NumberUtils.toInt(hubProxyPassLength));
        
        return hubConfigBuilder;
    }



    private Object getValue(final PluginSettings settings, final String key) {
        return settings.get(key);
    }

    private String getStringValue(final PluginSettings settings, final String key) {
        return (String) getValue(settings, key);
    }
}
