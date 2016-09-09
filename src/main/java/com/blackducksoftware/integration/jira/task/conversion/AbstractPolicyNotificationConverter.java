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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.blackducksoftware.integration.hub.dataservices.notification.items.NotificationContentItem;
import com.blackducksoftware.integration.hub.exception.NotificationServiceException;
import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.common.HubProjectMappings;
import com.blackducksoftware.integration.jira.common.JiraContext;
import com.blackducksoftware.integration.jira.common.JiraProject;
import com.blackducksoftware.integration.jira.exception.ConfigurationException;
import com.blackducksoftware.integration.jira.task.JiraSettingsService;
import com.blackducksoftware.integration.jira.task.conversion.output.HubEvent;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;

public abstract class AbstractPolicyNotificationConverter extends NotificationToEventConverter {
	private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));
	public static final String PROJECT_LINK = "project";

	public AbstractPolicyNotificationConverter(final HubProjectMappings mappings, final JiraServices jiraServices,
			final JiraContext jiraContext, final JiraSettingsService jiraSettingsService, final String issueTypeName)
			throws ConfigurationException {
		super(jiraServices, jiraContext, jiraSettingsService, mappings, issueTypeName);
	}

	@Override
	public List<HubEvent> generateEvents(final NotificationContentItem notif) {
		final List<HubEvent> notifEvents = new ArrayList<>();

		logger.debug("policyNotif: " + notif);
		logger.debug("Getting JIRA project(s) mapped to Hub project: " + notif.getProjectVersion().getProjectName());
		final List<JiraProject> mappingJiraProjects = getMappings()
				.getJiraProjects(notif.getProjectVersion().getProjectName());
		logger.debug("There are " + mappingJiraProjects.size() + " JIRA projects mapped to this Hub project : "
				+ notif.getProjectVersion().getProjectName());

		if (!mappingJiraProjects.isEmpty()) {

			for (final JiraProject jiraProject : mappingJiraProjects) {
				logger.debug("JIRA Project: " + jiraProject);
				try {
					final List<HubEvent> projectEvents = handleNotificationPerJiraProject(notif, jiraProject);
					if (projectEvents != null) {
						notifEvents.addAll(projectEvents);
					}
				} catch (final Exception e) {
					logger.error(e);
					getJiraSettingsService().addHubError(e, notif.getProjectVersion().getProjectName(),
							notif.getProjectVersion().getProjectVersionName(), jiraProject.getProjectName(),
							getJiraContext().getJiraUser().getName(), "transitionIssue");
					return null;
				}

			}
		}
		return notifEvents;
	}

	protected abstract List<HubEvent> handleNotificationPerJiraProject(final NotificationContentItem notif,
			final JiraProject jiraProject) throws UnexpectedHubResponseException, NotificationServiceException;
}
