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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.blackducksoftware.integration.jira.config.BlackDuckConfigKeys;
import com.blackducksoftware.integration.jira.config.PluginConfigKeys;

// TODO change these methods to all return optionals.
public class PluginSettingsWrapper {
    public static final String BLACK_DUCK_GROUPS_LIST_DELIMETER = ",";
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
        final String stringValue = getStringValue(BlackDuckConfigKeys.CONFIG_PROXY_PASS);
        if (StringUtils.isBlank(stringValue)) {
            return stringValue;
        }
        final Base64.Decoder decoder = Base64.getDecoder();
        final byte[] decode = decoder.decode(stringValue);
        return new String(decode, StandardCharsets.UTF_8);
    }

    public void setBlackDuckProxyPassword(final String password) {
        if (StringUtils.isBlank(password)) {
            setValue(BlackDuckConfigKeys.CONFIG_PROXY_PASS, password);
            return;
        }
        final Base64.Encoder encoder = Base64.getEncoder();
        final String encodedPassword = encoder.encodeToString(password.getBytes());
        setValue(BlackDuckConfigKeys.CONFIG_PROXY_PASS, encodedPassword);
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

    public String getBlackDuckConfigGroups() {
        return getStringValue(PluginConfigKeys.BLACKDUCK_CONFIG_GROUPS);
    }

    public void setBlackDuckConfigGroups(final String groups) {
        setValue(PluginConfigKeys.BLACKDUCK_CONFIG_GROUPS, groups);
    }

    public String[] getParsedBlackDuckConfigGroups() {
        final String blackDuckConfigGroupsString = getBlackDuckConfigGroups();
        if (StringUtils.isNotBlank(blackDuckConfigGroupsString)) {
            return blackDuckConfigGroupsString.split(BLACK_DUCK_GROUPS_LIST_DELIMETER);
        }
        return new String[0];
    }

    public String getIssueCreatorUser() {
        return getStringValue(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_ISSUE_CREATOR_USER);
    }

    public void setIssueCreatorUser(final String user) {
        setValue(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_ISSUE_CREATOR_USER, user);
    }

    public Optional<Integer> getIntervalBetweenChecks() {
        return getIntegerValue(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS);
    }

    public void setIntervalBetweenChecks(final Integer interval) {
        setValue(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS, interval);
    }

    public String getProjectMappingsJson() {
        return getStringValue(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_PROJECT_MAPPINGS_JSON);
    }

    public void setProjectMappingsJson(final String json) {
        setValue(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_PROJECT_MAPPINGS_JSON, json);
    }

    public String getFieldMappingsCopyJson() {
        return getStringValue(PluginConfigKeys.BLACKDUCK_CONFIG_FIELD_COPY_MAPPINGS_JSON);
    }

    public void setFieldMappingsCopyJson(final String json) {
        setValue(PluginConfigKeys.BLACKDUCK_CONFIG_FIELD_COPY_MAPPINGS_JSON, json);
    }

    public String getFirstTimeSave() {
        return getStringValue(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_FIRST_SAVE_TIME);
    }

    public void setFirstTimeSave(final String firstTimeSave) {
        setValue(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_FIRST_SAVE_TIME, firstTimeSave);
    }

    public String getPolicyRulesJson() {
        return getStringValue(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_POLICY_RULES_JSON);
    }

    public void setPolicyRulesJson(final String json) {
        setValue(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_POLICY_RULES_JSON, json);
    }

    public Boolean getVulnerabilityIssuesChoice() {
        return getBooleanValue(PluginConfigKeys.BLACKDUCK_CONFIG_CREATE_VULN_ISSUES_CHOICE, true);
    }

    public void setVulnerabilityIssuesChoice(final Boolean choice) {
        setValue(PluginConfigKeys.BLACKDUCK_CONFIG_CREATE_VULN_ISSUES_CHOICE, choice);
    }

    public Boolean getCommentOnIssuesUpdatesChoice() {
        return getBooleanValue(PluginConfigKeys.BLACKDUCK_CONFIG_COMMENT_ON_ISSUE_UPDATES_CHOICE, false);
    }

    public void setCommentOnIssuesUpdatesChoice(final Boolean choice) {
        setValue(PluginConfigKeys.BLACKDUCK_CONFIG_COMMENT_ON_ISSUE_UPDATES_CHOICE, choice);
    }

    public Boolean getProjectReviewerNotificationsChoice() {
        return getBooleanValue(PluginConfigKeys.BLACKDUCK_CONFIG_PROJECT_REVIEWER_NOTIFICATIONS_CHOICE);
    }

    public void setProjectReviewerNotificationsChoice(final Boolean choice) {
        setValue(PluginConfigKeys.BLACKDUCK_CONFIG_PROJECT_REVIEWER_NOTIFICATIONS_CHOICE, choice);
    }

    public String getLastRunDate() {
        return getStringValue(PluginConfigKeys.BLACKDUCK_CONFIG_LAST_RUN_DATE);
    }

    public void setLastRunDate(final String lastRunDate) {
        setValue(PluginConfigKeys.BLACKDUCK_CONFIG_LAST_RUN_DATE, lastRunDate);
    }

    public String getJiraAdminUser() {
        return getStringValue(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_ADMIN_USER);
    }

    public void setJiraAdminUser(final String adminUser) {
        setValue(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_ADMIN_USER, adminUser);
    }

    public String getStringValue(final String key) {
        final Object foundObject = pluginSettings.get(key);
        if (foundObject == null) {
            return null;
        }
        return String.valueOf(foundObject);
    }

    public Optional<Integer> getIntegerValue(final String key) {
        final String value = getStringValue(key);
        if (NumberUtils.isParsable(value)) {
            return Optional.of(Integer.parseInt(value));
        }
        return Optional.empty();
    }

    public Boolean getBooleanValue(final String key) {
        return getBooleanValue(key, false);
    }

    public Boolean getBooleanValue(final String key, final Boolean defaultValue) {
        final String stringValue = getStringValue(key);
        if (StringUtils.isBlank(stringValue)) {
            return defaultValue;
        }
        return Boolean.parseBoolean(stringValue);
    }

    public void setValue(final String key, final Object value) {
        if (value == null) {
            pluginSettings.remove(key);
        } else {
            pluginSettings.put(key, String.valueOf(value));
        }
    }
}
