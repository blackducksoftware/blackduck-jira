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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.blackducksoftware.integration.hub.api.component.BomComponentVersionPolicyStatus;
import com.blackducksoftware.integration.hub.api.component.ComponentVersionStatus;
import com.blackducksoftware.integration.hub.api.policy.PolicyRule;
import com.blackducksoftware.integration.hub.api.version.ReleaseItem;
import com.blackducksoftware.integration.hub.dataservices.items.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservices.items.PolicyViolationContentItem;
import com.blackducksoftware.integration.hub.exception.MissingUUIDException;
import com.blackducksoftware.integration.hub.exception.NotificationServiceException;
import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.common.JiraContext;
import com.blackducksoftware.integration.jira.common.JiraProject;
import com.blackducksoftware.integration.jira.task.JiraSettingsService;
import com.blackducksoftware.integration.jira.task.conversion.output.HubEvent;
import com.blackducksoftware.integration.jira.task.conversion.output.HubEventAction;
import com.blackducksoftware.integration.jira.task.conversion.output.PolicyEvent;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;

public class PolicyNotificationConverter extends NotificationToEventConverter {
	private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));
	public static final String PROJECT_LINK = "project";


	public PolicyNotificationConverter(final JiraServices jiraServices,
			final JiraContext jiraContext,
			final JiraSettingsService jiraSettingsService) {
		super(jiraServices, jiraContext, jiraSettingsService);
	}

	@Override
	public List<HubEvent> generateEvents(final NotificationContentItem notif) {
		List<HubEvent> events = new LinkedList<HubEvent>();

		String projectName;
		String projectVersionName;
		final ReleaseItem notifHubProjectReleaseItem;
		final PolicyViolationContentItem ruleViolationNotif = (PolicyViolationContentItem) notif;
		final List<ComponentVersionStatus> compVerStatuses = new ArrayList<>();
		final ComponentVersionStatus componentStatus = new ComponentVersionStatus();

		try {
			componentStatus.setBomComponentVersionPolicyStatusLink(
					ruleViolationNotif.getContent().getBomComponentVersionPolicyStatusLink());
			componentStatus.setComponentName(ruleViolationNotif.getContent().getComponentName());
			componentStatus.setComponentVersionLink(ruleViolationNotif.getContent().getComponentVersionLink());
			compVerStatuses.add(componentStatus);
			projectName = ruleViolationNotif.getContent().getProjectName();

			logger.debug("Getting JIRA project(s) mapped to Hub project: " + projectName);
			final List<JiraProject> mappingJiraProjects = mappings.getJiraProjects(projectName);
			logger.debug("There are " + mappingJiraProjects.size() + " JIRA projects mapped to this Hub project : "
					+ projectName);
			// get the mapped projects by name before making any Hub calls to
			// prevent forbidden errors
			if (!mappingJiraProjects.isEmpty()) {
				notifHubProjectReleaseItem = getHubNotificationService().getProjectReleaseItemFromProjectReleaseUrl(
						ruleViolationNotif.getContent().getProjectVersionLink());
				projectVersionName = notifHubProjectReleaseItem.getVersionName();
				events = handleNotification(eventType, projectName, projectVersionName, compVerStatuses,
						notifHubProjectReleaseItem, mappingJiraProjects);
			}
		} catch (UnexpectedHubResponseException | NotificationServiceException e) {
			logger.error(e);
			getJiraSettingsService().addHubError(e);
			return null;
		}
		return events;

		final List<HubEvent> notifEvents = new ArrayList<>();

		for (final JiraProject mappingJiraProject : jiraProjects) {
			final JiraProject jiraProject;
			try {
				jiraProject = getJiraProject(mappingJiraProject.getProjectId());
			} catch (final NotificationServiceException e) {
				logger.warn("Mapped project '" + mappingJiraProject.getProjectName() + "' with ID "
						+ mappingJiraProject.getProjectId() + " not found in JIRA; skipping this notification");
				continue;
			}
			if (StringUtils.isNotBlank(jiraProject.getProjectError())) {
				logger.error(jiraProject.getProjectError());
				continue;
			}

			logger.debug("JIRA Project: " + jiraProject);

			final List<HubEvent> projectEvents = handleNotificationPerJiraProject(eventType, projectName,
					projectVersionName, compVerStatuses, notifHubProjectReleaseItem, jiraProject);
			if (projectEvents != null) {
				notifEvents.addAll(projectEvents);
			}
		}
		return notifEvents;

	}

	protected List<HubEvent> handleNotification(final HubEventType eventType,
			final String projectName, final String projectVersionName,
			final List<ComponentVersionStatus> compVerStatuses, final ReleaseItem notifHubProjectReleaseItem,
			final List<JiraProject> jiraProjects)
					throws UnexpectedHubResponseException, NotificationServiceException {
		final List<HubEvent> notifEvents = new ArrayList<>();

		for (final JiraProject mappingJiraProject : jiraProjects) {
			final JiraProject jiraProject;
			try {
				jiraProject = getJiraProject(mappingJiraProject.getProjectId());
			} catch (final NotificationServiceException e) {
				logger.warn("Mapped project '" + mappingJiraProject.getProjectName() + "' with ID "
						+ mappingJiraProject.getProjectId() + " not found in JIRA; skipping this notification");
				continue;
			}
			if (StringUtils.isNotBlank(jiraProject.getProjectError())) {
				logger.error(jiraProject.getProjectError());
				continue;
			}

			logger.debug("JIRA Project: " + jiraProject);

			final List<HubEvent> projectEvents = handleNotificationPerJiraProject(eventType,
					projectName, projectVersionName, compVerStatuses, notifHubProjectReleaseItem, jiraProject);
			if (projectEvents != null) {
				notifEvents.addAll(projectEvents);
			}
		}
		return notifEvents;
	}

	private List<HubEvent> handleNotificationPerJiraProject(final HubEventType eventType,
			final String projectName, final String projectVersionName,
			final List<ComponentVersionStatus> compVerStatuses, final ReleaseItem notifHubProjectReleaseItem,
			final JiraProject jiraProject)
					throws UnexpectedHubResponseException, NotificationServiceException {
		final List<HubEvent> events = new ArrayList<>();

		for (final ComponentVersionStatus compVerStatus : compVerStatuses) {
			if (eventType == HubEventType.POLICY_VIOLATION
					&& compVerStatus.getComponentVersionLink() == null) {
				// FIXME see HUB-7571
				logger.error(
						"Cannot create tickets for component level violations at this time. This will be fixed in future releases.");
				continue;
			}
			final String componentVersionName = getHubNotificationService()
					.getComponentVersion(
							compVerStatus.getComponentVersionLink()).getVersionName();

			final String policyStatusUrl = compVerStatus.getBomComponentVersionPolicyStatusLink();

			final BomComponentVersionPolicyStatus bomComponentVersionPolicyStatus = getHubNotificationService()
					.getPolicyStatus(policyStatusUrl);

			logger.debug("BomComponentVersionPolicyStatus: " + bomComponentVersionPolicyStatus);
			final List<String> monitoredUrls = getMonitoredRules(bomComponentVersionPolicyStatus
					.getLinks(BomComponentVersionPolicyStatus.POLICY_RULE_URL));
			if(monitoredUrls == null || monitoredUrls.isEmpty()){
				logger.warn(
						"No configured policy violations matching this notification found; skipping this notification");
				continue;
			}

			for (final String ruleUrl : monitoredUrls) {
				final PolicyRule rule = getHubNotificationService().getPolicyRule(ruleUrl);
				logger.debug("Rule : " + rule);

				if (rule.getExpression() != null && rule.getExpression().hasOnlyProjectLevelConditions()) {
					logger.warn("Skipping this Violation since it is a Project only violation.");
					continue;
				}

				UUID versionId;
				UUID componentId;
				UUID componentVersionId;
				UUID ruleId;
				try {
					versionId = notifHubProjectReleaseItem.getVersionId();

					componentId = compVerStatus.getComponentId();

					componentVersionId = compVerStatus.getComponentVersionId();

					ruleId = rule.getPolicyRuleId();
				} catch (final MissingUUIDException e) {
					logger.error(e);
					continue;
				}

				HubEventAction action;
				if (eventType == HubEventType.POLICY_VIOLATION) {
					action = HubEventAction.OPEN;
				} else {
					action = HubEventAction.CLOSE;
				}
				final HubEvent event = new PolicyEvent(action, projectName,
						projectVersionName, compVerStatus.getComponentName(), componentVersionName, versionId,
						componentId, componentVersionId,
						getJiraContext().getJiraUser().getName(),
						jiraProject.getIssueTypeId(),
						jiraProject.getProjectId(), jiraProject.getProjectName(),
						eventType, rule, ruleId);

				if (event.getEventType() == HubEventType.POLICY_VIOLATION) {
					events.add(event);
				} else if (event.getEventType() == HubEventType.POLICY_OVERRIDE) {
					events.add(event);
				}
			}
		}
		return events;
	}

}
