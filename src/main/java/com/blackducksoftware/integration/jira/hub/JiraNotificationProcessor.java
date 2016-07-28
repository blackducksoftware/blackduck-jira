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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.hub.version.api.ReleaseItem;
import com.blackducksoftware.integration.jira.HubJiraLogger;
import com.blackducksoftware.integration.jira.config.HubProjectMapping;
import com.blackducksoftware.integration.jira.config.HubProjectMappings;
import com.blackducksoftware.integration.jira.hub.model.component.ComponentVersionStatus;
import com.blackducksoftware.integration.jira.hub.model.notification.NotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.RuleViolationNotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.VulnerabilityNotificationContent;
import com.blackducksoftware.integration.jira.hub.model.notification.VulnerabilityNotificationItem;
import com.blackducksoftware.integration.jira.hub.model.project.ProjectVersion;
import com.blackducksoftware.integration.jira.hub.policy.PolicyNotificationFilter;
import com.blackducksoftware.integration.jira.hub.vulnerability.VulnerabilityNotificationFilter;
import com.blackducksoftware.integration.jira.issue.EventType;

public class JiraNotificationProcessor {
	private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));
	public static final String PROJECT_LINK = "project";
	private final HubNotificationService hubNotificationService;
	// TODO replace with HubProjectMappings
	private final Set<HubProjectMapping> underlyingMappings;
	private final List<String> linksOfRulesToMonitor;
	private final TicketGeneratorInfo ticketGenInfo;

	public JiraNotificationProcessor(final HubNotificationService hubNotificationService,
			final Set<HubProjectMapping> mappings, final List<String> linksOfRulesToMonitor,
			final TicketGeneratorInfo ticketGenInfo) {
		this.hubNotificationService = hubNotificationService;
		this.underlyingMappings = mappings;
		this.linksOfRulesToMonitor = linksOfRulesToMonitor;
		this.ticketGenInfo = ticketGenInfo;
	}

	public FilteredNotificationResults extractJiraReadyNotifications(final List<NotificationItem> notifications)
			throws HubNotificationServiceException {
		final FilteredNotificationResults allResults = new FilteredNotificationResults();

		logger.debug("JiraNotificationFilter.extractJiraReadyNotifications(): Sifting through " + notifications.size()
				+ " notifications");
		for (final NotificationItem notif : notifications) {
			logger.debug("Notification: " + notif);

			FilteredNotificationResults notifResults;
			try {
				notifResults = convertNotificationToIssues(notif);
			} catch (final UnexpectedHubResponseException e) {
				throw new HubNotificationServiceException("Error converting notifications to issues", e);
			}
			if (notifResults != null) {
				allResults.addAllResults(notifResults);
			}
		}
		return allResults;
	}

	private FilteredNotificationResults convertNotificationToIssues(final NotificationItem notif)
			throws HubNotificationServiceException, UnexpectedHubResponseException {
		EventType eventType;
		String projectName;
		String projectVersionName;
		List<ComponentVersionStatus> compVerStatuses;
		final ReleaseItem notifHubProjectReleaseItem;

		if (notif instanceof RuleViolationNotificationItem) {
			try {
				final RuleViolationNotificationItem ruleViolationNotif = (RuleViolationNotificationItem) notif;
				eventType = EventType.POLICY_VIOLATION;
				compVerStatuses = ruleViolationNotif.getContent().getComponentVersionStatuses();
				projectName = ruleViolationNotif.getContent().getProjectName();
				notifHubProjectReleaseItem = hubNotificationService
						.getProjectReleaseItemFromProjectReleaseUrl(ruleViolationNotif.getContent().getProjectVersionLink());
				projectVersionName = notifHubProjectReleaseItem.getVersionName();
			} catch (final HubNotificationServiceException e) {
				logger.error(e);
				return null;
			}
			final PolicyNotificationFilter filter = new PolicyNotificationFilter(underlyingMappings,
					ticketGenInfo, linksOfRulesToMonitor, hubNotificationService);
			return filter.handleNotification(eventType, projectName, projectVersionName, compVerStatuses,
					notifHubProjectReleaseItem);
		} else if (notif instanceof PolicyOverrideNotificationItem) {
			try {
				final PolicyOverrideNotificationItem ruleViolationNotif = (PolicyOverrideNotificationItem) notif;
				eventType = EventType.POLICY_OVERRIDE;

				compVerStatuses = new ArrayList<>();
				final ComponentVersionStatus componentStatus = new ComponentVersionStatus();
				componentStatus.setBomComponentVersionPolicyStatusLink(
						ruleViolationNotif.getContent().getBomComponentVersionPolicyStatusLink());
				componentStatus.setComponentName(ruleViolationNotif.getContent().getComponentName());
				componentStatus.setComponentVersionLink(ruleViolationNotif.getContent().getComponentVersionLink());

				compVerStatuses.add(componentStatus);

				projectName = ruleViolationNotif.getContent().getProjectName();

				notifHubProjectReleaseItem = hubNotificationService.getProjectReleaseItemFromProjectReleaseUrl(
						ruleViolationNotif.getContent().getProjectVersionLink());
				projectVersionName = notifHubProjectReleaseItem.getVersionName();
			} catch (final HubNotificationServiceException e) {
				logger.error(e);
				return null;
			}
			final PolicyNotificationFilter filter = new PolicyNotificationFilter(underlyingMappings,
					ticketGenInfo, linksOfRulesToMonitor, hubNotificationService);
			return filter.handleNotification(eventType, projectName, projectVersionName, compVerStatuses,
					notifHubProjectReleaseItem);
		} else if (notif instanceof VulnerabilityNotificationItem) {
			final VulnerabilityNotificationItem vulnerabilityNotif = (VulnerabilityNotificationItem) notif;
			logger.debug("vulnerabilityNotif: " + vulnerabilityNotif);
			logger.info("This vulnerability notification affects "
					+ vulnerabilityNotif.getContent().getAffectedProjectVersions().size() + " project versions");
			final VulnerabilityNotificationContent vulnerabilityNotificationContent = vulnerabilityNotif.getContent();

			final HubProjectMappings mapping = new HubProjectMappings(ticketGenInfo, underlyingMappings);
			final VulnerabilityNotificationFilter filter = new VulnerabilityNotificationFilter(mapping, ticketGenInfo,
					linksOfRulesToMonitor, hubNotificationService);

			final String componentName = vulnerabilityNotif.getContent().getComponentName();
			final String componentVersionName = vulnerabilityNotif.getContent().getVersionName();

			for (final ProjectVersion projectVersion : vulnerabilityNotif.getContent().getAffectedProjectVersions()) {
				projectName = projectVersion.getProjectName();
				projectVersionName = projectVersion.getProjectVersionName();
				final String projectVersionLink = projectVersion.getProjectVersionLink();

				notifHubProjectReleaseItem = hubNotificationService
						.getProjectReleaseItemFromProjectReleaseUrl(projectVersionLink);

				return filter.handleNotification(projectName, projectVersionName, projectVersionLink, componentName,
						componentVersionName, vulnerabilityNotificationContent, notifHubProjectReleaseItem);
			}


			return null; // TODO
		} else {
			throw new HubNotificationServiceException("Notification type unknown for notification: " + notif);
		}
	}

}
