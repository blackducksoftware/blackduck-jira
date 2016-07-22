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
package com.blackducksoftware.integration.jira.hub;

import java.util.UUID;

import com.blackducksoftware.integration.hub.policy.api.PolicyRule;
import com.blackducksoftware.integration.jira.issue.EventType;

public class FilteredNotificationResult {

	private final String hubProjectName;
	private final String hubProjectVersion;
	private final String hubComponentName;
	private final String hubComponentVersion;
	private final PolicyRule rule;

	private final UUID hubProjectVersionId;
	private final UUID hubComponentId;
	private final UUID hubComponentVersionId;
	private final UUID ruleId;

	private final String jiraUserName;
	private final String jiraIssueTypeId;
	private final Long jiraProjectId;
	private final String jiraProjectName;

	private final EventType eventType;

	public FilteredNotificationResult(final String hubProjectName, final String hubProjectVersion,
			final String hubComponentName, final String hubComponentVersion, final PolicyRule rule,
			final UUID hubProjectVersionId, final UUID hubComponentId,
			final UUID hubComponentVersionId,
			final UUID ruleId, final String jiraUserName,
			final String jiraIssueTypeId, final Long jiraProjectId, final String jiraProjectName,
 final EventType eventType) {
		this.hubProjectName = hubProjectName;
		this.hubProjectVersion = hubProjectVersion;
		this.hubComponentName = hubComponentName;
		this.hubComponentVersion = hubComponentVersion;
		this.rule = rule;
		this.hubProjectVersionId = hubProjectVersionId;
		this.hubComponentId = hubComponentId;
		this.hubComponentVersionId = hubComponentVersionId;
		this.ruleId = ruleId;
		this.jiraUserName = jiraUserName;
		this.jiraIssueTypeId = jiraIssueTypeId;
		this.jiraProjectId = jiraProjectId;
		this.jiraProjectName = jiraProjectName;
		this.eventType = eventType;
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

	public PolicyRule getRule() {
		return rule;
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

	public UUID getRuleId() {
		return ruleId;
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

	public EventType getEventType() {
		return eventType;
	}

	public String getUniquePropertyKey() {
		final StringBuilder keyBuilder = new StringBuilder();
		keyBuilder.append(getJiraProjectId().toString());
		keyBuilder.append(".");
		keyBuilder.append(getHubProjectVersionId().toString());
		keyBuilder.append(".");
		keyBuilder.append(getHubComponentId().toString());
		keyBuilder.append(".");
		if (getHubComponentVersionId() != null) {
			keyBuilder.append(getHubComponentVersionId().toString());
			keyBuilder.append(".");
		}
		keyBuilder.append(getRuleId().toString());
		return keyBuilder.toString();
	}

}
