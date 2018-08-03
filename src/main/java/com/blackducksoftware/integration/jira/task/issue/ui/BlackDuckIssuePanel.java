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
package com.blackducksoftware.integration.jira.task.issue.ui;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.plugin.webfragment.contextproviders.AbstractJiraContextProvider;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;

public class BlackDuckIssuePanel extends AbstractJiraContextProvider {
    private final CustomFieldManager customFieldManager;

    public BlackDuckIssuePanel(final CustomFieldManager customFieldManager) {
        this.customFieldManager = customFieldManager;
    }

    @Override
    public Map<String, Object> getContextMap(final ApplicationUser user, final JiraHelper jiraHelper) {
        final Map<String, Object> contextMap = new HashMap<>();
        final Issue currentIssue = (Issue) jiraHelper.getContextParams().get("issue");
        if (currentIssue != null) {
            populateContextMap(contextMap, currentIssue, BlackDuckJiraConstants.BLACK_DUCK_CUSTOM_FIELD_PROJECT, "bdsHubProject");
            populateContextMap(contextMap, currentIssue, BlackDuckJiraConstants.BLACK_DUCK_CUSTOM_FIELD_PROJECT_VERSION, "bdsHubProjectVersion");
            populateContextMap(contextMap, currentIssue, BlackDuckJiraConstants.BLACK_DUCK_CUSTOM_FIELD_PROJECT_VERSION_URL, "bdsHubProjectVersionUrl");
            populateContextMap(contextMap, currentIssue, BlackDuckJiraConstants.BLACK_DUCK_CUSTOM_FIELD_PROJECT_OWNER, "bdsHubProjectOwner");
            populateContextMap(contextMap, currentIssue, BlackDuckJiraConstants.BLACK_DUCK_CUSTOM_FIELD_PROJECT_VERSION_NICKNAME, "bdsHubProjectVersionNickname");

            populateContextMap(contextMap, currentIssue, BlackDuckJiraConstants.BLACK_DUCK_CUSTOM_FIELD_COMPONENT, "bdsHubComponent");
            populateContextMap(contextMap, currentIssue, BlackDuckJiraConstants.BLACK_DUCK_CUSTOM_FIELD_COMPONENT_URL, "bdsHubComponentUrl");
            populateContextMap(contextMap, currentIssue, BlackDuckJiraConstants.BLACK_DUCK_CUSTOM_FIELD_COMPONENT_VERSION, "bdsHubComponentVersion");
            populateContextMap(contextMap, currentIssue, BlackDuckJiraConstants.BLACK_DUCK_CUSTOM_FIELD_COMPONENT_VERSION_URL, "bdsHubComponentVersionUrl");
            populateContextMap(contextMap, currentIssue, BlackDuckJiraConstants.BLACK_DUCK_CUSTOM_FIELD_COMPONENT_ORIGIN, "bdsHubComponentOrigin");
            populateContextMap(contextMap, currentIssue, BlackDuckJiraConstants.BLACK_DUCK_CUSTOM_FIELD_COMPONENT_ORIGIN_ID, "bdsHubComponentOriginId");
            populateContextMap(contextMap, currentIssue, BlackDuckJiraConstants.BLACK_DUCK_CUSTOM_FIELD_COMPONENT_USAGE, "bdsHubComponentUsage");

            populateContextMap(contextMap, currentIssue, BlackDuckJiraConstants.BLACK_DUCK_CUSTOM_FIELD_LICENSE_NAMES, "bdsHubLicenses");
            populateContextMap(contextMap, currentIssue, BlackDuckJiraConstants.BLACK_DUCK_CUSTOM_FIELD_LICENSE_URL, "bdsHubLicenseUrl");
            populateContextMap(contextMap, currentIssue, BlackDuckJiraConstants.BLACK_DUCK_CUSTOM_FIELD_PROJECT_VERSION_LAST_UPDATED, "bdsHubProjectVersionLastUpdated");
            populateContextMap(contextMap, currentIssue, BlackDuckJiraConstants.BLACK_DUCK_CUSTOM_FIELD_POLICY_RULE, "bdsHubPolicyRule");
            populateContextMap(contextMap, currentIssue, BlackDuckJiraConstants.BLACK_DUCK_CUSTOM_FIELD_POLICY_RULE_OVERRIDABLE, "bdsHubPolicyRuleOverridable");
            populateContextMap(contextMap, currentIssue, BlackDuckJiraConstants.BLACK_DUCK_CUSTOM_FIELD_POLICY_RULE_DESCRIPTION, "bdsHubPolicyRuleDescription");
            populateContextMap(contextMap, currentIssue, BlackDuckJiraConstants.BLACK_DUCK_CUSTOM_FIELD_POLICY_RULE_URL, "bdsHubPolicyRuleUrl");
        }

        return contextMap;
    }

    private void populateContextMap(final Map<String, Object> contextMap, final Issue currentIssue, final String fieldConstant, final String fieldKey) {
        final Object valueForMap = getCustomFieldValue(currentIssue, customFieldManager, fieldConstant);
        if (valueForMap != null) {
            contextMap.put(fieldKey, valueForMap);
        }
    }

    private Object getCustomFieldValue(final Issue currentIssue, final CustomFieldManager customFieldManager, final String fieldName) {
        final Collection<CustomField> hubCustomFields = customFieldManager.getCustomFieldObjectsByName(fieldName);
        if (hubCustomFields != null) {
            for (final CustomField hubField : hubCustomFields) {
                final Object hubFieldValue = getCustomFieldValue(currentIssue, hubField);
                if (hubFieldValue != null) {
                    return hubFieldValue;
                }
            }
        }
        return null;
    }

    private Object getCustomFieldValue(final Issue currentIssue, final CustomField hubField) {
        final Object fieldValue = currentIssue.getCustomFieldValue(hubField);
        if (fieldValue instanceof String && StringUtils.isBlank((String) fieldValue)) {
            return null;
        } else if (fieldValue instanceof ApplicationUser) {
            return ((ApplicationUser) fieldValue).getUsername();
        }
        return fieldValue;
    }

}
