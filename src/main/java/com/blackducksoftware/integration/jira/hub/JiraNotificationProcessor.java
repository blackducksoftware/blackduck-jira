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

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.jira.HubJiraLogger;
import com.blackducksoftware.integration.jira.config.HubProjectMapping;
import com.blackducksoftware.integration.jira.config.HubProjectMappings;
import com.blackducksoftware.integration.jira.hub.model.notification.NotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.RuleViolationNotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.VulnerabilityNotificationItem;
import com.blackducksoftware.integration.jira.hub.policy.PolicyOverrideNotificationFilter;
import com.blackducksoftware.integration.jira.hub.policy.PolicyViolationNotificationFilter;
import com.blackducksoftware.integration.jira.hub.vulnerability.VulnerabilityNotificationFilter;

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

	public HubEvents extractJiraReadyNotifications(final List<NotificationItem> notifications)
			throws HubNotificationServiceException {
		final HubEvents allResults = new HubEvents();

		logger.debug("JiraNotificationFilter.extractJiraReadyNotifications(): Sifting through " + notifications.size()
				+ " notifications");
		for (final NotificationItem notif : notifications) {
			logger.debug("Notification: " + notif);

			HubEvents notifResults;
			try {
				notifResults = convertNotificationToEvents(notif);
			} catch (final UnexpectedHubResponseException e) {
				throw new HubNotificationServiceException("Error converting notifications to issues", e);
			}
			if (notifResults != null) {
				allResults.addAllEvents(notifResults);
			}
		}
		return allResults;
	}

	private HubEvents convertNotificationToEvents(final NotificationItem notif) throws UnexpectedHubResponseException,
	HubNotificationServiceException {
		final HubProjectMappings mapping = new HubProjectMappings(ticketGenInfo, underlyingMappings);

		if (notif instanceof RuleViolationNotificationItem) {
			// TODO: We should not create a new filter every time; create one of
			// each once,
			// or make each a singleton
			final PolicyViolationNotificationFilter filter = new PolicyViolationNotificationFilter(mapping,
					ticketGenInfo, linksOfRulesToMonitor, hubNotificationService);
			return filter.generateEvents(notif);
		} else if (notif instanceof PolicyOverrideNotificationItem) {
			// TODO: We should not create a new filter every time; create one of
			// each once,
			// or make each a singleton
			final PolicyOverrideNotificationFilter filter = new PolicyOverrideNotificationFilter(mapping,
					ticketGenInfo, linksOfRulesToMonitor, hubNotificationService);
			return filter.generateEvents(notif);
		} else if (notif instanceof VulnerabilityNotificationItem) {

			//
			// // TODO: We should not create a new filter every time; create one
			// of
			// // each once,
			// // or make each a singleton
			final VulnerabilityNotificationFilter filter = new VulnerabilityNotificationFilter(mapping, ticketGenInfo,
					linksOfRulesToMonitor, hubNotificationService);

			return filter.generateEvents(notif);
		} else {
			throw new HubNotificationServiceException("Notification type unknown for notification: " + notif);
		}
	}

}
