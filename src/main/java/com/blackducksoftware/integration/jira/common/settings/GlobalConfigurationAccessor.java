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
package com.blackducksoftware.integration.jira.common.settings;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.jira.common.settings.model.GeneralIssueCreationConfigModel;
import com.blackducksoftware.integration.jira.common.settings.model.PluginBlackDuckServerConfigModel;
import com.blackducksoftware.integration.jira.common.settings.model.PluginGroupsConfigModel;
import com.blackducksoftware.integration.jira.common.settings.model.PluginIssueCreationConfigModel;
import com.blackducksoftware.integration.jira.common.settings.model.PluginIssueFieldConfigModel;
import com.blackducksoftware.integration.jira.common.settings.model.ProjectMappingConfigModel;
import com.blackducksoftware.integration.jira.common.settings.model.TicketCriteriaConfigModel;

public class GlobalConfigurationAccessor {
    private JiraSettingsAccessor jiraSettingsAccessor;

    public GlobalConfigurationAccessor(final JiraSettingsAccessor jiraSettingsAccessor) {
        this.jiraSettingsAccessor = jiraSettingsAccessor;
    }

    public PluginGroupsConfigModel getGroupsConfig() {
        final String blackDuckConfigGroupsString = jiraSettingsAccessor.getStringValue(PluginConfigKeys.BLACKDUCK_CONFIG_GROUPS);
        if (StringUtils.isNotBlank(blackDuckConfigGroupsString)) {
            return PluginGroupsConfigModel.fromDelimitedString(blackDuckConfigGroupsString);
        }
        return PluginGroupsConfigModel.none();
    }

    public void setGroupsConfig(final PluginGroupsConfigModel groupsModel) {
        final String groupsString = groupsModel.getGroupsStringDelimited();
        jiraSettingsAccessor.setValue(PluginConfigKeys.BLACKDUCK_CONFIG_GROUPS, groupsString);
    }

    public PluginBlackDuckServerConfigModel getBlackDuckServerConfig() {
        final String url = jiraSettingsAccessor.getStringValue(BlackDuckConfigKeys.CONFIG_BLACKDUCK_URL);
        final String apiToken = jiraSettingsAccessor.getStringValue(BlackDuckConfigKeys.CONFIG_BLACKDUCK_API_TOKEN);
        final Integer timeout = jiraSettingsAccessor.getIntegerValue(BlackDuckConfigKeys.CONFIG_BLACKDUCK_TIMEOUT).orElse(null);
        final Boolean trustCert = jiraSettingsAccessor.getBooleanValue(BlackDuckConfigKeys.CONFIG_BLACKDUCK_TRUST_CERT);

        final String proxyHost = jiraSettingsAccessor.getStringValue(BlackDuckConfigKeys.CONFIG_PROXY_HOST);
        final Integer proxyPort = jiraSettingsAccessor.getIntegerValue(BlackDuckConfigKeys.CONFIG_PROXY_PORT).orElse(null);
        final String poxyUser = jiraSettingsAccessor.getStringValue(BlackDuckConfigKeys.CONFIG_PROXY_USER);
        final String proxyPassword = getBlackDuckProxyPassword();

        return new PluginBlackDuckServerConfigModel(url, apiToken, timeout, trustCert, proxyHost, proxyPort, poxyUser, proxyPassword);
    }

    public void setBlackDuckServerConfig(final PluginBlackDuckServerConfigModel blackDuckServerConfigModel) {
        jiraSettingsAccessor.setValue(BlackDuckConfigKeys.CONFIG_BLACKDUCK_URL, blackDuckServerConfigModel.getUrl());
        jiraSettingsAccessor.setValue(BlackDuckConfigKeys.CONFIG_BLACKDUCK_API_TOKEN, blackDuckServerConfigModel.getApiToken());

        blackDuckServerConfigModel.getTimeoutInSeconds().ifPresent(timeout -> jiraSettingsAccessor.setValue(BlackDuckConfigKeys.CONFIG_BLACKDUCK_TIMEOUT, timeout));
        jiraSettingsAccessor.setValue(BlackDuckConfigKeys.CONFIG_BLACKDUCK_TRUST_CERT, blackDuckServerConfigModel.getTrustCert());

        jiraSettingsAccessor.setValue(BlackDuckConfigKeys.CONFIG_PROXY_HOST, blackDuckServerConfigModel.getProxyHost());
        blackDuckServerConfigModel.getProxyPort().ifPresent(port -> jiraSettingsAccessor.setValue(BlackDuckConfigKeys.CONFIG_PROXY_PORT, port));
        jiraSettingsAccessor.setValue(BlackDuckConfigKeys.CONFIG_PROXY_USER, blackDuckServerConfigModel.getProxyUsername());
        setBlackDuckProxyPassword(blackDuckServerConfigModel.getProxyPassword());
    }

    public PluginIssueCreationConfigModel getIssueCreationConfig() {
        final Integer interval = jiraSettingsAccessor.getIntegerValue(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS).orElse(null);
        final String defaultIssueCreator = jiraSettingsAccessor.getStringValue(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_ISSUE_CREATOR_USER);
        final GeneralIssueCreationConfigModel general = new GeneralIssueCreationConfigModel(interval, defaultIssueCreator);

        final String mappingsJson = jiraSettingsAccessor.getStringValue(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_PROJECT_MAPPINGS_JSON);
        final ProjectMappingConfigModel projectMapping = new ProjectMappingConfigModel(mappingsJson);

        final String policyRulesJson = jiraSettingsAccessor.getStringValue(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_POLICY_RULES_JSON);
        final Boolean commentOnIssueUpdates = jiraSettingsAccessor.getBooleanValue(PluginConfigKeys.BLACKDUCK_CONFIG_COMMENT_ON_ISSUE_UPDATES_CHOICE, false);
        final Boolean addComponentReviewerToTickets = jiraSettingsAccessor.getBooleanValue(PluginConfigKeys.BLACKDUCK_CONFIG_PROJECT_REVIEWER_NOTIFICATIONS_CHOICE, false);
        final TicketCriteriaConfigModel ticketCriteriaConfigModel = new TicketCriteriaConfigModel(policyRulesJson, commentOnIssueUpdates, addComponentReviewerToTickets);

        return new PluginIssueCreationConfigModel(general, projectMapping, ticketCriteriaConfigModel);
    }

    public void setIssueCreationConfig(final PluginIssueCreationConfigModel pluginIssueCreationConfigModel) {
        final GeneralIssueCreationConfigModel general = pluginIssueCreationConfigModel.getGeneral();
        jiraSettingsAccessor.setValue(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS, general.getInterval().orElse(null));
        jiraSettingsAccessor.setValue(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_ISSUE_CREATOR_USER, general.getDefaultIssueCreator());

        final ProjectMappingConfigModel projectMapping = pluginIssueCreationConfigModel.getProjectMapping();
        jiraSettingsAccessor.setValue(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_PROJECT_MAPPINGS_JSON, projectMapping.getMappingsJson());

        final TicketCriteriaConfigModel ticketCriteriaConfigModel = pluginIssueCreationConfigModel.getTicketCriteria();
        jiraSettingsAccessor.setValue(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_POLICY_RULES_JSON, ticketCriteriaConfigModel.getPolicyRulesJson());
        jiraSettingsAccessor.setValue(PluginConfigKeys.BLACKDUCK_CONFIG_COMMENT_ON_ISSUE_UPDATES_CHOICE, ticketCriteriaConfigModel.getCommentOnIssueUpdates());
        jiraSettingsAccessor.setValue(PluginConfigKeys.BLACKDUCK_CONFIG_PROJECT_REVIEWER_NOTIFICATIONS_CHOICE, ticketCriteriaConfigModel.getAddComponentReviewerToTickets());
    }

    public PluginIssueFieldConfigModel getFieldMappingConfig() {
        final String fieldMappings = jiraSettingsAccessor.getStringValue(PluginConfigKeys.BLACKDUCK_CONFIG_FIELD_COPY_MAPPINGS_JSON);
        return new PluginIssueFieldConfigModel(fieldMappings);
    }

    public void setFieldMappingConfig(final PluginIssueFieldConfigModel fieldMappingConfig) {
        jiraSettingsAccessor.setValue(PluginConfigKeys.BLACKDUCK_CONFIG_FIELD_COPY_MAPPINGS_JSON, fieldMappingConfig.getFieldMappingJson());
    }

    @Deprecated
    public Boolean getVulnerabilityIssuesChoice() {
        return jiraSettingsAccessor.getBooleanValue(PluginConfigKeys.BLACKDUCK_CONFIG_CREATE_VULN_ISSUES_CHOICE, true);
    }

    private String getBlackDuckProxyPassword() {
        final String stringValue = jiraSettingsAccessor.getStringValue(BlackDuckConfigKeys.CONFIG_PROXY_PASS);
        if (StringUtils.isBlank(stringValue)) {
            return stringValue;
        }
        final Base64.Decoder decoder = Base64.getDecoder();
        final byte[] decode = decoder.decode(stringValue);
        return new String(decode, StandardCharsets.UTF_8);
    }

    private void setBlackDuckProxyPassword(final String password) {
        if (StringUtils.isBlank(password)) {
            jiraSettingsAccessor.setValue(BlackDuckConfigKeys.CONFIG_PROXY_PASS, null);
            return;
        }
        final Base64.Encoder encoder = Base64.getEncoder();
        final String encodedPassword = encoder.encodeToString(password.getBytes());
        jiraSettingsAccessor.setValue(BlackDuckConfigKeys.CONFIG_PROXY_PASS, encodedPassword);
    }

}
