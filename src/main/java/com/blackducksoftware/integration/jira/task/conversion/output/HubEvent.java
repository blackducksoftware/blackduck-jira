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

import com.atlassian.jira.issue.Issue;
import com.blackducksoftware.integration.hub.dataservices.notification.items.NotificationContentItem;
import com.blackducksoftware.integration.hub.exception.MissingUUIDException;

/**
 * An event is one of the following: Policy violation by a specific component on
 * a specific project, policy override (on a ...), vulnerability added to a
 * specific component on a specific project, vulnerability removed (from a ...),
 * vulnerability updated (on a ...).
 *
 * @author sbillings
 *
 */
public abstract class HubEvent<T extends NotificationContentItem> {

	private final HubEventAction action;
	private final String jiraUserName;
	private final String jiraUserId;
	// if issueAssigneeId is null: leave it unassigned
	private final String issueAssigneeId;
	private final String jiraIssueTypeId;
	private final Long jiraProjectId;
	private final String jiraProjectName;
	private final T notif;

	public HubEvent(final HubEventAction action, final String jiraUserName, final String jiraUserId,
			final String issueAssigneeId, final String jiraIssueTypeId, final Long jiraProjectId,
			final String jiraProjectName, final T notif) {
		this.action = action;
		this.jiraUserName = jiraUserName;
		this.jiraUserId = jiraUserId;
		this.issueAssigneeId = issueAssigneeId;
		this.jiraIssueTypeId = jiraIssueTypeId;
		this.jiraProjectId = jiraProjectId;
		this.jiraProjectName = jiraProjectName;
		this.notif = notif;
	}

	public HubEventAction getAction() {
		return action;
	}

	public T getNotif() {
		return notif;
	}

	public String getJiraUserName() {
		return jiraUserName;
	}

	public String getJiraUserId() {
		return jiraUserId;
	}

	public String getIssueAssigneeId() {
		return issueAssigneeId;
	}

	public String getJiraIssueTypeId() {
		return jiraIssueTypeId;
	}

	public Long getJiraProjectId() {
		return jiraProjectId;
	}

	public String getJiraProjectName() {
		return jiraProjectName;
	}

	// Override these to generate comments
	public String getReopenComment() {
		return null;
	}

	public String getComment() {
		return null; // most event types don't produce comments
	}

	public String getResolveComment() {
		return null;
	}

	public abstract String getUniquePropertyKey() throws MissingUUIDException;

	public abstract String getIssueSummary();

	public abstract String getIssueDescription();

	public abstract IssueProperties createIssuePropertiesFromJson(final String json);

	public abstract IssueProperties createIssueProperties(final Issue issue);
}
