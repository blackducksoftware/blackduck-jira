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
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package com.blackducksoftware.integration.jira.task.conversion;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.blackducksoftware.integration.hub.api.component.ComponentVersionStatus;
import com.blackducksoftware.integration.hub.api.notification.NotificationItem;
import com.blackducksoftware.integration.hub.api.notification.RuleViolationNotificationItem;
import com.blackducksoftware.integration.hub.api.version.ReleaseItem;
import com.blackducksoftware.integration.hub.exception.NotificationServiceException;
import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.hub.notification.NotificationService;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.common.HubProjectMappings;
import com.blackducksoftware.integration.jira.common.JiraContext;
import com.blackducksoftware.integration.jira.common.JiraProject;
import com.blackducksoftware.integration.jira.task.conversion.output.HubEvent;
import com.blackducksoftware.integration.jira.task.conversion.output.HubEventType;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;

public class PolicyViolationNotificationConverter extends PolicyNotificationConverter {
	private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));
	private final HubProjectMappings mappings;

	public PolicyViolationNotificationConverter(final HubProjectMappings mappings, final JiraServices jiraServices,
			final JiraContext jiraContext,
			final List<String> linksOfRulesToMonitor, final NotificationService hubNotificationService) {
		super(jiraServices, jiraContext, linksOfRulesToMonitor, hubNotificationService);
		this.mappings = mappings;
	}

	@Override
	public List<HubEvent> generateEvents(final NotificationItem notif) {
		List<HubEvent> events = new LinkedList<HubEvent>();

		if (!isRulesToMonitor()) {
			logger.warn("No rules-to-monitor provided, skipping policy notifications.");
			return null;
		}

		HubEventType eventType;
		String projectName;
		String projectVersionName;
		List<ComponentVersionStatus> compVerStatuses;
		final ReleaseItem notifHubProjectReleaseItem;
		eventType = HubEventType.POLICY_VIOLATION;
		final RuleViolationNotificationItem ruleViolationNotif = (RuleViolationNotificationItem) notif;

		try {
			compVerStatuses = ruleViolationNotif.getContent().getComponentVersionStatuses();
			projectName = ruleViolationNotif.getContent().getProjectName();

			logger.debug("Getting JIRA project(s) mapped to Hub project: " + projectName);
			final List<JiraProject> mappingJiraProjects = mappings.getJiraProjects(projectName);
			logger.debug("There are " + mappingJiraProjects.size() + " JIRA projects mapped to this Hub project : "
					+ projectName);
			// get the mapped projects by name before making any Hub calls to
			// prevent forbidden errors
			if (!mappingJiraProjects.isEmpty()) {

				notifHubProjectReleaseItem = getHubNotificationService()
						.getProjectReleaseItemFromProjectReleaseUrl(ruleViolationNotif.getContent().getProjectVersionLink());
				projectVersionName = notifHubProjectReleaseItem.getVersionName();
				events = handleNotification(eventType, projectName, projectVersionName, compVerStatuses,
						notifHubProjectReleaseItem, mappingJiraProjects);
			}
		} catch (UnexpectedHubResponseException | NotificationServiceException e) {
			logger.error(e);
			return null;
		}
		return events;
	}
}
