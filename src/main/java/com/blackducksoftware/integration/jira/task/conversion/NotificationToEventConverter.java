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
package com.blackducksoftware.integration.jira.task.conversion;

import java.util.List;

import com.atlassian.jira.issue.issuetype.IssueType;
import com.blackducksoftware.integration.hub.dataservices.notification.items.NotificationContentItem;
import com.blackducksoftware.integration.hub.exception.NotificationServiceException;
import com.blackducksoftware.integration.jira.common.HubProjectMappings;
import com.blackducksoftware.integration.jira.common.JiraContext;
import com.blackducksoftware.integration.jira.common.JiraProject;
import com.blackducksoftware.integration.jira.task.JiraSettingsService;
import com.blackducksoftware.integration.jira.task.conversion.output.HubEvent;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;

public abstract class NotificationToEventConverter {
	private final JiraServices jiraServices;
	private final JiraContext jiraContext;
	private final JiraSettingsService jiraSettingsService;
	private final HubProjectMappings mappings;

	public NotificationToEventConverter(final JiraServices jiraServices, final JiraContext jiraContext,
			final JiraSettingsService jiraSettingsService, final HubProjectMappings mappings) {
		this.jiraServices = jiraServices;
		this.jiraContext = jiraContext;
		this.jiraSettingsService = jiraSettingsService;
		this.mappings = mappings;
	}

	public abstract List<HubEvent> generateEvents(NotificationContentItem notif);

	public JiraSettingsService getJiraSettingsService() {
		return jiraSettingsService;
	}

	public HubProjectMappings getMappings() {
		return mappings;
	}

	protected JiraProject getJiraProject(final long jiraProjectId) throws NotificationServiceException {
		final com.atlassian.jira.project.Project atlassianJiraProject = jiraServices.getJiraProjectManager()
				.getProjectObj(jiraProjectId);
		if (atlassianJiraProject == null) {
			throw new NotificationServiceException("Error: JIRA Project with ID " + jiraProjectId + " not found");
		}
		final String jiraProjectKey = atlassianJiraProject.getKey();
		final String jiraProjectName = atlassianJiraProject.getName();
		final JiraProject bdsJiraProject = new JiraProject();
		bdsJiraProject.setProjectId(jiraProjectId);
		bdsJiraProject.setProjectKey(jiraProjectKey);
		bdsJiraProject.setProjectName(jiraProjectName);

		if (atlassianJiraProject.getIssueTypes() == null || atlassianJiraProject.getIssueTypes().isEmpty()) {
			bdsJiraProject.setProjectError("The Jira project : " + bdsJiraProject.getProjectName()
					+ " does not have any issue types, we will not be able to create tickets for this project.");
		} else {
			boolean projectHasIssueType = false;
			if (atlassianJiraProject.getIssueTypes() != null && !atlassianJiraProject.getIssueTypes().isEmpty()) {
				for (final IssueType issueType : atlassianJiraProject.getIssueTypes()) {
					if (issueType.getName().equals(jiraContext.getJiraIssueTypeName())) {
						bdsJiraProject.setIssueTypeId(issueType.getId());
						projectHasIssueType = true;
					}
				}
			}
			if (!projectHasIssueType) {
				bdsJiraProject.setProjectError(
						"The Jira project is missing the " + jiraContext.getJiraIssueTypeName() + " issue type.");
			}
		}
		return bdsJiraProject;
	}

	protected JiraContext getJiraContext() {
		return jiraContext;
	}

}
