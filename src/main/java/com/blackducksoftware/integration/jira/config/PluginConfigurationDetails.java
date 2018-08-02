/**
 * Hub JIRA Plugin
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.jira.config;

import org.apache.commons.lang3.math.NumberUtils;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.blackducksoftware.integration.hub.configuration.HubServerConfigBuilder;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraConfigKeys;

public class PluginConfigurationDetails {
    private final String hubUrl;
    private final String hubUsername;
    private final String hubPasswordEncrypted;
    private final String hubPasswordLength;
    private final String hubTimeoutString;
    private final boolean hubTrustCert;
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
    private final String jiraAdminUserName;
    private final String jiraIssueCreatorUserName;
    private final String fieldCopyMappingJson;
    private final boolean createVulnerabilityIssues;

    private final PluginSettings settings;

    public PluginConfigurationDetails(final PluginSettings settings) {
        this.settings = settings;
        hubUrl = getStringValue(settings, HubConfigKeys.CONFIG_HUB_URL);
        hubUsername = getStringValue(settings, HubConfigKeys.CONFIG_HUB_USER);
        hubPasswordEncrypted = getStringValue(settings, HubConfigKeys.CONFIG_HUB_PASS);
        hubPasswordLength = getStringValue(settings, HubConfigKeys.CONFIG_HUB_PASS_LENGTH);
        hubTimeoutString = getStringValue(settings, HubConfigKeys.CONFIG_HUB_TIMEOUT);
        hubTrustCert = getBooleanValue(settings, HubConfigKeys.CONFIG_HUB_TRUST_CERT);

        hubProxyHost = getStringValue(settings, HubConfigKeys.CONFIG_PROXY_HOST);
        hubProxyPort = getStringValue(settings, HubConfigKeys.CONFIG_PROXY_PORT);
        hubProxyNoHost = getStringValue(settings, HubConfigKeys.CONFIG_PROXY_NO_HOST);
        hubProxyUser = getStringValue(settings, HubConfigKeys.CONFIG_PROXY_USER);
        hubProxyPassEncrypted = getStringValue(settings, HubConfigKeys.CONFIG_PROXY_PASS);
        hubProxyPassLength = getStringValue(settings, HubConfigKeys.CONFIG_PROXY_PASS_LENGTH);

        intervalString = getStringValue(settings, BlackDuckJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS);
        projectMappingJson = getStringValue(settings, BlackDuckJiraConfigKeys.HUB_CONFIG_JIRA_PROJECT_MAPPINGS_JSON);
        policyRulesJson = getStringValue(settings, BlackDuckJiraConfigKeys.HUB_CONFIG_JIRA_POLICY_RULES_JSON);
        installDateString = getStringValue(settings, BlackDuckJiraConfigKeys.HUB_CONFIG_JIRA_FIRST_SAVE_TIME);
        lastRunDateString = getStringValue(settings, BlackDuckJiraConfigKeys.HUB_CONFIG_LAST_RUN_DATE);

        jiraIssueCreatorUserName = getStringValue(settings, BlackDuckJiraConfigKeys.HUB_CONFIG_JIRA_ISSUE_CREATOR_USER);
        jiraAdminUserName = getStringValue(settings, BlackDuckJiraConfigKeys.HUB_CONFIG_JIRA_ADMIN_USER);

        fieldCopyMappingJson = getStringValue(settings, BlackDuckJiraConfigKeys.HUB_CONFIG_FIELD_COPY_MAPPINGS_JSON);
        createVulnerabilityIssues = getBooleanValue(settings, BlackDuckJiraConfigKeys.HUB_CONFIG_CREATE_VULN_ISSUES_CHOICE);
    }

    public PluginSettings getSettings() {
        return settings;
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

    public int getIntervalMinutes() {
        return NumberUtils.toInt(intervalString);
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

    public String getJiraAdminUserName() {
        return jiraAdminUserName;
    }

    public String getJiraIssueCreatorUserName() {
        return jiraIssueCreatorUserName;
    }

    public String getFieldCopyMappingJson() {
        return fieldCopyMappingJson;
    }

    public boolean isCreateVulnerabilityIssues() {
        return createVulnerabilityIssues;
    }

    public HubServerConfigBuilder createHubServerConfigBuilder() {
        final HubServerConfigBuilder hubConfigBuilder = new HubServerConfigBuilder();
        hubConfigBuilder.setUrl(hubUrl);
        hubConfigBuilder.setUsername(hubUsername);
        hubConfigBuilder.setPassword(hubPasswordEncrypted);
        hubConfigBuilder.setPasswordLength(NumberUtils.toInt(hubPasswordLength));
        hubConfigBuilder.setTimeout(hubTimeoutString);
        hubConfigBuilder.setTrustCert(hubTrustCert);

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

    private boolean getBooleanValue(final PluginSettings settings, final String key) {
        final String valueString = (String) getValue(settings, key);
        if ("true".equalsIgnoreCase(valueString)) {
            return true;
        }
        return false;
    }
}
