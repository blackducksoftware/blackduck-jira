/**
 * Black Duck JIRA Plugin
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
import com.synopsys.integration.blackduck.configuration.HubServerConfigBuilder;

public class PluginConfigurationDetails {
    private final String blackDuckUrl;
    private final String blackDuckApiToken;
    private final String blackDuckUsername;
    private final String blackDuckPasswordEncrypted;
    private final String blackDuckPasswordLength;
    private final String blackDuckTimeoutString;
    private final boolean blackDuckTrustCert;
    private final String blackDuckProxyHost;
    private final String blackDuckProxyPort;
    private final String blackDuckProxyNoHost;
    private final String blackDuckProxyUser;
    private final String blackDuckProxyPassEncrypted;
    private final String blackDuckProxyPassLength;
    private final String intervalString;
    private final String projectMappingJson;
    private final String policyRulesJson;
    private final String installDateString;
    private final String lastRunDateString;
    private final String jiraAdminUserName;
    private final String defaultJiraIssueCreatorUserName;
    private final String fieldCopyMappingJson;
    private final boolean createVulnerabilityIssues;

    private final PluginSettings settings;

    public PluginConfigurationDetails(final PluginSettings settings) {
        this.settings = settings;
        blackDuckUrl = getStringValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_URL);
        blackDuckApiToken = getStringValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_API_TOKEN);
        blackDuckUsername = getStringValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_USER);
        blackDuckPasswordEncrypted = getStringValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_PASS);
        blackDuckPasswordLength = getStringValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_PASS_LENGTH);
        blackDuckTimeoutString = getStringValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_TIMEOUT);
        blackDuckTrustCert = getBooleanValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_TRUST_CERT);

        blackDuckProxyHost = getStringValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_HOST);
        blackDuckProxyPort = getStringValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_PORT);
        blackDuckProxyNoHost = getStringValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_NO_HOST);
        blackDuckProxyUser = getStringValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_USER);
        blackDuckProxyPassEncrypted = getStringValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_PASS);
        blackDuckProxyPassLength = getStringValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_PASS_LENGTH);

        intervalString = getStringValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS);
        projectMappingJson = getStringValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_PROJECT_MAPPINGS_JSON);
        policyRulesJson = getStringValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_POLICY_RULES_JSON);
        installDateString = getStringValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_FIRST_SAVE_TIME);
        lastRunDateString = getStringValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_LAST_RUN_DATE);

        defaultJiraIssueCreatorUserName = getStringValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_ISSUE_CREATOR_USER);
        jiraAdminUserName = getStringValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_ADMIN_USER);

        fieldCopyMappingJson = getStringValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_FIELD_COPY_MAPPINGS_JSON);
        createVulnerabilityIssues = getBooleanValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_CREATE_VULN_ISSUES_CHOICE);
    }

    public PluginSettings getSettings() {
        return settings;
    }

    public String getBlackDuckUrl() {
        return blackDuckUrl;
    }

    public String getBlackDuckUsername() {
        return blackDuckUsername;
    }

    public String getBlackDuckPasswordEncrypted() {
        return blackDuckPasswordEncrypted;
    }

    public String getBlackDuckPasswordLength() {
        return blackDuckPasswordLength;
    }

    public String getBlackDuckTimeoutString() {
        return blackDuckTimeoutString;
    }

    public String getBlackDuckProxyHost() {
        return blackDuckProxyHost;
    }

    public String getBlackDuckProxyPort() {
        return blackDuckProxyPort;
    }

    public String getBlackDuckProxyNoHost() {
        return blackDuckProxyNoHost;
    }

    public String getBlackDuckProxyUser() {
        return blackDuckProxyUser;
    }

    public String getBlackDuckProxyPassEncrypted() {
        return blackDuckProxyPassEncrypted;
    }

    public String getBlackDuckProxyPassLength() {
        return blackDuckProxyPassLength;
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

    public String getDefaultJiraIssueCreatorUserName() {
        return defaultJiraIssueCreatorUserName;
    }

    public String getFieldCopyMappingJson() {
        return fieldCopyMappingJson;
    }

    public boolean isCreateVulnerabilityIssues() {
        return createVulnerabilityIssues;
    }

    public HubServerConfigBuilder createServerConfigBuilder() {
        final HubServerConfigBuilder serverConfigBuilder = new HubServerConfigBuilder();
        serverConfigBuilder.setUrl(blackDuckUrl);
        serverConfigBuilder.setApiToken(blackDuckApiToken);
        serverConfigBuilder.setUsername(blackDuckUsername);
        serverConfigBuilder.setPassword(blackDuckPasswordEncrypted);
        serverConfigBuilder.setPasswordLength(NumberUtils.toInt(blackDuckPasswordLength));
        serverConfigBuilder.setTimeout(blackDuckTimeoutString);
        serverConfigBuilder.setTrustCert(blackDuckTrustCert);

        serverConfigBuilder.setProxyHost(blackDuckProxyHost);
        serverConfigBuilder.setProxyPort(blackDuckProxyPort);
        serverConfigBuilder.setProxyIgnoredHosts(blackDuckProxyNoHost);
        serverConfigBuilder.setProxyUsername(blackDuckProxyUser);
        serverConfigBuilder.setProxyPassword(blackDuckProxyPassEncrypted);
        serverConfigBuilder.setProxyPasswordLength(NumberUtils.toInt(blackDuckProxyPassLength));

        return serverConfigBuilder;
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
