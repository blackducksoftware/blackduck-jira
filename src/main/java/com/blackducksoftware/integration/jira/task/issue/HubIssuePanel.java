/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
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
 *******************************************************************************/
package com.blackducksoftware.integration.jira.task.issue;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.plugin.webfragment.contextproviders.AbstractJiraContextProvider;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;

public class HubIssuePanel extends AbstractJiraContextProvider {

	private final CustomFieldManager customFieldManager;

	public HubIssuePanel(final CustomFieldManager customFieldManager) {
		this.customFieldManager = customFieldManager;
	}

	@Override
	public Map<String, String> getContextMap(final ApplicationUser user, final JiraHelper jiraHelper) {
		final Map<String, String> contextMap = new HashMap<>();
		final Issue currentIssue = (Issue) jiraHelper.getContextParams().get("issue");
		if (currentIssue != null) {
			final String hubProject = getCustomFieldValue(currentIssue, customFieldManager,
					HubJiraConstants.HUB_CUSTOM_FIELD_PROJECT);
			if (hubProject != null) {
				contextMap.put("bdsHubProject", hubProject);
			}
			final String hubProjectVersion = getCustomFieldValue(currentIssue, customFieldManager,
					HubJiraConstants.HUB_CUSTOM_FIELD_PROJECT_VERSION);
			if (hubProjectVersion != null) {
				contextMap.put("bdsHubProjectVersion", hubProjectVersion);
			}
			final String hubComponent = getCustomFieldValue(currentIssue, customFieldManager,
					HubJiraConstants.HUB_CUSTOM_FIELD_COMPONENT);
			if (hubComponent != null) {
				contextMap.put("bdsHubComponent", hubComponent);
			}
			final String hubComponentVersion = getCustomFieldValue(currentIssue, customFieldManager,
					HubJiraConstants.HUB_CUSTOM_FIELD_COMPONENT_VERSION);
			if (hubComponentVersion != null) {
				contextMap.put("bdsHubComponentVersion", hubComponentVersion);
			}
			final String hubPolicyRule = getCustomFieldValue(currentIssue, customFieldManager,
					HubJiraConstants.HUB_CUSTOM_FIELD_POLICY_RULE);
			if (hubPolicyRule != null) {
				contextMap.put("bdsHubPolicyRule", hubPolicyRule);
			}
		}
		return contextMap;
	}

	private String getCustomFieldValue(final Issue currentIssue, final CustomFieldManager customFieldManager,
			final String fieldName) {
		final CustomField hubCustomField = customFieldManager.getCustomFieldObjectByName(fieldName);
		if (hubCustomField != null) {
			final String hubFieldValue = (String) currentIssue.getCustomFieldValue(hubCustomField);
			if (StringUtils.isNotBlank(hubFieldValue)) {
				return hubFieldValue;
			}
		}
		return null;
	}


}
