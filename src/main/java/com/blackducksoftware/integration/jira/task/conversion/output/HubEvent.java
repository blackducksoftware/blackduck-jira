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

import java.util.UUID;

import com.atlassian.jira.issue.Issue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * An event is one of the following: Policy violation by a specific component on
 * a specific project, policy override (on a ...), vulnerability added to a
 * specific component on a specific project, vulnerability removed (from a ...),
 * vulnerability updated (on a ...).
 *
 * @author sbillings
 *
 */
public abstract class HubEvent {
	protected final Gson gson = new GsonBuilder().create();
	private final HubEventAction action;
	private final String hubProjectName;
	private final String hubProjectVersion;
	private final String hubComponentName;
	private final String hubComponentVersion;

	private final UUID hubProjectVersionId;
	private final UUID hubComponentId;
	private final UUID hubComponentVersionId;

	private final String jiraUserName;
	private final String jiraIssueTypeId;
	private final Long jiraProjectId;
	private final String jiraProjectName;

	private final HubEventType eventType;

	public HubEvent(final HubEventAction action, final String hubProjectName,
			final String hubProjectVersion,
			final String hubComponentName, final String hubComponentVersion,
			final UUID hubProjectVersionId, final UUID hubComponentId,
			final UUID hubComponentVersionId,
			final String jiraUserName,
			final String jiraIssueTypeId, final Long jiraProjectId, final String jiraProjectName,
			final HubEventType eventType) {
		this.action = action;
		this.hubProjectName = hubProjectName;
		this.hubProjectVersion = hubProjectVersion;
		this.hubComponentName = hubComponentName;
		this.hubComponentVersion = hubComponentVersion;

		this.hubProjectVersionId = hubProjectVersionId;
		this.hubComponentId = hubComponentId;
		this.hubComponentVersionId = hubComponentVersionId;

		this.jiraUserName = jiraUserName;
		this.jiraIssueTypeId = jiraIssueTypeId;
		this.jiraProjectId = jiraProjectId;
		this.jiraProjectName = jiraProjectName;
		this.eventType = eventType;
	}

	public HubEventAction getAction() {
		return action;
	}

	public String getHubProjectName() {
		return hubProjectName;
	}

	public String getHubProjectVersion() {
		return hubProjectVersion;
	}

	public String getHubComponentName() {
		return hubComponentName;
	}

	public String getHubComponentVersion() {
		return hubComponentVersion;
	}

	public UUID getHubProjectVersionId() {
		return hubProjectVersionId;
	}

	public UUID getHubComponentId() {
		return hubComponentId;
	}

	public UUID getHubComponentVersionId() {
		return hubComponentVersionId;
	}

	public String getJiraUserName() {
		return jiraUserName;
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

	public HubEventType getEventType() {
		return eventType;
	}

	// Subtypes that use comments must override this
	public String getComment() {
		return null; // most event types don't produce comments
	}

	public abstract String getUniquePropertyKey();

	public abstract String getIssueSummary();

	public abstract String getIssueDescription();

	public abstract IssueProperties createIssuePropertiesFromJson(final String json);

	public abstract IssueProperties createIssueProperties(final Issue issue);
}
