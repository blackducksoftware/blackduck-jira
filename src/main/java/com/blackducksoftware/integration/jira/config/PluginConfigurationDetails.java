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
///**
// * Black Duck JIRA Plugin
// *
// * Copyright (C) 2019 Black Duck Software, Inc.
// * http://www.blackducksoftware.com/
// *
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements. See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership. The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License. You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied. See the License for the
// * specific language governing permissions and limitations
// * under the License.
// */
//package com.blackducksoftware.integration.jira.config;
//
//import org.apache.commons.lang3.math.NumberUtils;
//
//import com.atlassian.sal.api.pluginsettings.PluginSettings;
//import com.blackducksoftware.integration.jira.common.settings.PluginSettingsWrapper;
//import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
//import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
//
//public class PluginConfigurationDetails {
//    private final String blackDuckUrl;
//    private final String blackDuckApiToken;
//    private final String blackDuckTimeoutString;
//    private final boolean blackDuckTrustCert;
//    private final String blackDuckProxyHost;
//    private final String blackDuckProxyPort;
//    private final String blackDuckProxyUser;
//    private final String blackDuckProxyPass;
//    private final String blackDuckProxyPassLength;
//    private final String intervalString;
//    private final String projectMappingJson;
//    private final String policyRulesJson;
//    private final String installDateString;
//    private final String lastRunDateString;
//    private final String jiraAdminUserName;
//    private final String defaultJiraIssueCreatorUserName;
//    private final String fieldCopyMappingJson;
//    private final boolean createVulnerabilityIssues;
//    private final boolean commentOnIssueUpdates;
//    private final boolean projectReviewerEnabled;
//
//    private final PluginSettings settings;
//
//    public PluginConfigurationDetails(final PluginSettings settings) {
//        this.settings = settings;
//        final PluginSettingsWrapper pluginSettingsWrapper = new PluginSettingsWrapper(settings);
//        blackDuckUrl = pluginSettingsWrapper.getBlackDuckUrl();
//        blackDuckApiToken = pluginSettingsWrapper.getBlackDuckApiToken();
//        blackDuckTimeoutString = pluginSettingsWrapper.getBlackDuckTimeout().map(String::valueOf).orElse(null);
//        blackDuckTrustCert = pluginSettingsWrapper.getBlackDuckAlwaysTrust();
//
//        blackDuckProxyHost = pluginSettingsWrapper.getBlackDuckProxyHost();
//        blackDuckProxyPort = pluginSettingsWrapper.getBlackDuckProxyPort().map(String::valueOf).orElse(null);
//        blackDuckProxyUser = pluginSettingsWrapper.getBlackDuckProxyUser();
//        blackDuckProxyPass = pluginSettingsWrapper.getBlackDuckProxyPassword();
//        blackDuckProxyPassLength = pluginSettingsWrapper.getBlackDuckProxyPasswordLength().map(String::valueOf).orElse(null);
//
//        intervalString = pluginSettingsWrapper.getIntervalBetweenChecks().map(String::valueOf).orElse(null);
//        projectMappingJson = pluginSettingsWrapper.getProjectMappingsJson();
//        policyRulesJson = pluginSettingsWrapper.getPolicyRulesJson();
//        installDateString = pluginSettingsWrapper.getFirstTimeSave();
//        lastRunDateString = pluginSettingsWrapper.getLastRunDate();
//
//        defaultJiraIssueCreatorUserName = pluginSettingsWrapper.getIssueCreatorUser();
//        jiraAdminUserName = pluginSettingsWrapper.getJiraAdminUser();
//
//        fieldCopyMappingJson = pluginSettingsWrapper.getFieldMappingsCopyJson();
//        createVulnerabilityIssues = pluginSettingsWrapper.getVulnerabilityIssuesChoice();
//        commentOnIssueUpdates = pluginSettingsWrapper.getCommentOnIssuesUpdatesChoice();
//        projectReviewerEnabled = pluginSettingsWrapper.getProjectReviewerNotificationsChoice();
//    }
//
//    public PluginSettings getSettings() {
//        return settings;
//    }
//
//    public String getBlackDuckUrl() {
//        return blackDuckUrl;
//    }
//
//    public String getBlackDuckTimeoutString() {
//        return blackDuckTimeoutString;
//    }
//
//    public String getBlackDuckProxyHost() {
//        return blackDuckProxyHost;
//    }
//
//    public String getBlackDuckProxyPort() {
//        return blackDuckProxyPort;
//    }
//
//    public String getBlackDuckProxyUser() {
//        return blackDuckProxyUser;
//    }
//
//    public String getBlackDuckProxyPass() {
//        return blackDuckProxyPass;
//    }
//
//    public String getBlackDuckProxyPassLength() {
//        return blackDuckProxyPassLength;
//    }
//
//    public String getIntervalString() {
//        return intervalString;
//    }
//
//    public int getIntervalMinutes() {
//        return NumberUtils.toInt(intervalString);
//    }
//
//    public String getProjectMappingJson() {
//        return projectMappingJson;
//    }
//
//    public String getPolicyRulesJson() {
//        return policyRulesJson;
//    }
//
//    public String getInstallDateString() {
//        return installDateString;
//    }
//
//    public String getLastRunDateString() {
//        return lastRunDateString;
//    }
//
//    public String getJiraAdminUserName() {
//        return jiraAdminUserName;
//    }
//
//    public String getDefaultJiraIssueCreatorUserName() {
//        return defaultJiraIssueCreatorUserName;
//    }
//
//    public String getFieldCopyMappingJson() {
//        return fieldCopyMappingJson;
//    }
//
//    public boolean isCreateVulnerabilityIssues() {
//        return createVulnerabilityIssues;
//    }
//
//    public boolean isCommentOnIssueUpdates() {
//        return commentOnIssueUpdates;
//    }
//
//    public boolean isProjectReviewerEnabled() {
//        return projectReviewerEnabled;
//    }
//
//    public BlackDuckServerConfigBuilder createServerConfigBuilder() {
//        final BlackDuckServerConfigBuilder serverConfigBuilder = BlackDuckServerConfig.newBuilder();
//        serverConfigBuilder.setUrl(blackDuckUrl);
//        serverConfigBuilder.setApiToken(blackDuckApiToken);
//        serverConfigBuilder.setTimeoutInSeconds(blackDuckTimeoutString);
//        serverConfigBuilder.setTrustCert(blackDuckTrustCert);
//        serverConfigBuilder.setProxyHost(blackDuckProxyHost);
//        serverConfigBuilder.setProxyPort(blackDuckProxyPort);
//        serverConfigBuilder.setProxyUsername(blackDuckProxyUser);
//        serverConfigBuilder.setProxyPassword(blackDuckProxyPass);
//
//        return serverConfigBuilder;
//    }
//
//    private Object getValue(final PluginSettings settings, final String key) {
//        return settings.get(key);
//    }
//
//    private String getStringValue(final PluginSettings settings, final String key) {
//        return (String) getValue(settings, key);
//    }
//
//    private boolean getBooleanValue(final PluginSettings settings, final String key) {
//        final String valueString = (String) getValue(settings, key);
//        return Boolean.parseBoolean(valueString);
//    }
//}
