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
import com.blackducksoftware.integration.jira.hub.model.component.ComponentVersionStatus;
import com.blackducksoftware.integration.jira.hub.model.notification.NotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.NotificationType;
import com.blackducksoftware.integration.jira.hub.model.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.RuleViolationNotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.VulnerabilityNotificationItem;

public class JiraNotificationProcessor {
	private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));
	public static final String PROJECT_LINK = "project";
	private final HubNotificationService hubNotificationService;
	private final Set<HubProjectMapping> mappings;
	private final List<String> linksOfRulesToMonitor;
	private final TicketGeneratorInfo ticketGenInfo;

	public JiraNotificationProcessor(final HubNotificationService hubNotificationService,
			final Set<HubProjectMapping> mappings, final List<String> linksOfRulesToMonitor,
			final TicketGeneratorInfo ticketGenInfo) {
		this.hubNotificationService = hubNotificationService;
		this.mappings = mappings;
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
		NotificationType notificationType;
		String projectName;
		String projectVersionName;
		List<ComponentVersionStatus> compVerStatuses;
		final ReleaseItem notifHubProjectReleaseItem;

		NotificationFilter filter;

		if (notif instanceof RuleViolationNotificationItem) {
			try {
				final RuleViolationNotificationItem ruleViolationNotif = (RuleViolationNotificationItem) notif;
				notificationType = NotificationType.POLICY_VIOLATION;
				compVerStatuses = ruleViolationNotif.getContent().getComponentVersionStatuses();
				projectName = ruleViolationNotif.getContent().getProjectName();
				notifHubProjectReleaseItem = hubNotificationService
						.getProjectReleaseItemFromProjectReleaseUrl(ruleViolationNotif.getContent().getProjectVersionLink());
				projectVersionName = notifHubProjectReleaseItem.getVersionName();
			} catch (final HubNotificationServiceException e) {
				logger.error(e);
				return null;
			}
			filter = new PolicyNotificationFilter(mappings,
					ticketGenInfo, linksOfRulesToMonitor, hubNotificationService);
		} else if (notif instanceof PolicyOverrideNotificationItem) {
			try {
				final PolicyOverrideNotificationItem ruleViolationNotif = (PolicyOverrideNotificationItem) notif;
				notificationType = NotificationType.POLICY_OVERRIDE;

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
			filter = new PolicyNotificationFilter(mappings,
					ticketGenInfo, linksOfRulesToMonitor, hubNotificationService);
		} else if (notif instanceof VulnerabilityNotificationItem) {
			notificationType = NotificationType.VULNERABILITY;
			return null; // TODO
		} else {
			throw new HubNotificationServiceException("Notification type unknown for notification: " + notif);
		}
		return filter.handleNotification(notificationType, projectName, projectVersionName, compVerStatuses,
				notifHubProjectReleaseItem);
	}

}
