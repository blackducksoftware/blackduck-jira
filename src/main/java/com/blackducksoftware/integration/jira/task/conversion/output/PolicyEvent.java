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
package com.blackducksoftware.integration.jira.task.conversion.output;

import org.apache.log4j.Logger;

import com.atlassian.jira.issue.Issue;
import com.blackducksoftware.integration.hub.api.policy.PolicyRule;
import com.blackducksoftware.integration.hub.dataservices.notification.items.NotificationContentItem;
import com.blackducksoftware.integration.hub.exception.MissingUUIDException;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class PolicyEvent extends HubEvent<NotificationContentItem> {
	private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));
	private final NotificationContentItem notificationContentItem;
	private final PolicyRule policyRule;
	private final String resolveComment;

	public PolicyEvent(final HubEventAction action, final String jiraUserName, final String jiraUserId,

			final String issueAssigneeId,
			final String jiraIssueTypeId,
			final Long jiraProjectId, final String jiraProjectName,
			final NotificationContentItem notificationContentItem,
			final PolicyRule policyRule, final String resolveComment) {

		super(action, jiraUserName, jiraUserId, issueAssigneeId, jiraIssueTypeId, jiraProjectId, jiraProjectName,
				notificationContentItem);
		this.notificationContentItem = notificationContentItem;
		this.policyRule = policyRule;
		this.resolveComment = resolveComment;
	}

	public NotificationContentItem getNotificationContentItem() {
		return notificationContentItem;
	}

	public PolicyRule getPolicyRule() {
		return policyRule;
	}

	@Override
	public String getUniquePropertyKey() throws MissingUUIDException {
		final StringBuilder keyBuilder = new StringBuilder();
		keyBuilder.append(getJiraProjectId().toString());
		keyBuilder.append(".");
		keyBuilder.append(getNotificationContentItem().getProjectVersion().getVersionId().toString());
		keyBuilder.append(".");
		keyBuilder.append(getNotificationContentItem().getComponentId().toString());
		keyBuilder.append(".");
		if (getNotificationContentItem().getComponentVersionId() != null) {
			keyBuilder.append(getNotificationContentItem().getComponentVersionId().toString());
			keyBuilder.append(".");
		}
		keyBuilder.append(getPolicyRule().getPolicyRuleId().toString());
		final String key = keyBuilder.toString();
		logger.debug("property key: " + key);
		return key;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("PolicyEvent [notificationContentItem=");
		builder.append(notificationContentItem);
		builder.append(", policyRule=");
		builder.append(policyRule);
		builder.append(", getJiraUserName()=");
		builder.append(getJiraUserName());
		builder.append(", getJiraIssueTypeId()=");
		builder.append(getJiraIssueTypeId());
		builder.append(", getJiraProjectId()=");
		builder.append(getJiraProjectId());
		builder.append(", getJiraProjectName()=");
		builder.append(getJiraProjectName());
		builder.append("]");
		return builder.toString();
	}

	@Override
	public String getIssueSummary() {
		final StringBuilder issueSummary = new StringBuilder();
		issueSummary.append("Black Duck Policy Violation detected on Hub Project '");
		issueSummary.append(getNotificationContentItem().getProjectVersion().getProjectName());
		issueSummary.append("' / '");
		issueSummary.append(getNotificationContentItem().getProjectVersion().getProjectVersionName());
		issueSummary.append("', component '");
		issueSummary.append(getNotificationContentItem().getComponentName());
		issueSummary.append("' / '");
		issueSummary.append(getNotificationContentItem().getComponentVersion());
		issueSummary.append("'");
		issueSummary.append(" [Rule: '");
		issueSummary.append(getPolicyRule().getName());
		issueSummary.append("']");
		return issueSummary.toString();
	}

	@Override
	public String getIssueDescription() {
		final StringBuilder issueDescription = new StringBuilder();
		issueDescription.append("The Black Duck Hub has detected a Policy Violation on Hub Project '");
		issueDescription.append(getNotificationContentItem().getProjectVersion().getProjectName());
		issueDescription.append("' / '");
		issueDescription.append(getNotificationContentItem().getProjectVersion().getProjectVersionName());
		issueDescription.append("', component '");
		issueDescription.append(getNotificationContentItem().getComponentName());
		issueDescription.append("' / '");
		issueDescription.append(getNotificationContentItem().getComponentVersion());
		issueDescription.append("'.");
		issueDescription.append(" The rule violated is: '");
		issueDescription.append(getPolicyRule().getName());
		issueDescription.append("'. Rule overridable : ");
		issueDescription.append(getPolicyRule().getOverridable());
		return issueDescription.toString();
	}

	@Override
	public PolicyViolationIssueProperties createIssuePropertiesFromJson(final String json) {
		final Gson gson = new GsonBuilder().create();
		return gson.fromJson(json, PolicyViolationIssueProperties.class);
	}

	@Override
	public IssueProperties createIssueProperties(final Issue issue) {
		final IssueProperties properties = new PolicyViolationIssueProperties(
				getNotificationContentItem().getProjectVersion().getProjectName(),
				getNotificationContentItem().getProjectVersion().getProjectVersionName(),
				getNotificationContentItem().getComponentName(), getNotificationContentItem().getComponentVersion(),
				issue.getId(), getPolicyRule().getName());
		return properties;
	}

	@Override
	public String getReopenComment() {
		return HubJiraConstants.HUB_POLICY_VIOLATION_REOPEN;
	}

	@Override
	public String getResolveComment() {
		return resolveComment;
	}
}
