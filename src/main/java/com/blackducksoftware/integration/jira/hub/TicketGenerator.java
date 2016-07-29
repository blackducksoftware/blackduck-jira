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

import com.blackducksoftware.integration.jira.HubJiraLogger;
import com.blackducksoftware.integration.jira.config.HubProjectMapping;
import com.blackducksoftware.integration.jira.hub.model.notification.NotificationItem;
import com.blackducksoftware.integration.jira.issue.JiraIssueHandler;

/**
 * Collects recent notifications from the Hub, and generates JIRA tickets for
 * them.
 *
 * @author sbillings
 *
 */
public class TicketGenerator {
	private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));
	private final HubNotificationService notificationService;
	private final TicketGeneratorInfo ticketGenInfo;


	public TicketGenerator(final HubNotificationService notificationService,
			final TicketGeneratorInfo ticketGenInfo) {
		this.notificationService = notificationService;
		this.ticketGenInfo = ticketGenInfo;
	}

	public int generateTicketsForRecentNotifications(final Set<HubProjectMapping> hubProjectMappings,
			final List<String> linksOfRulesToMonitor,
			final NotificationDateRange notificationDateRange) throws HubNotificationServiceException {

		final List<NotificationItem> notifs = notificationService.fetchNotifications(notificationDateRange);
		for (final NotificationItem notification : notifs) {
			logger.debug(notification.toString());
		}
		final JiraNotificationProcessor processor = new JiraNotificationProcessor(notificationService,
				hubProjectMappings, linksOfRulesToMonitor, ticketGenInfo);

		final HubEvents notificationResults = processor.generateEvents(notifs);

		final JiraIssueHandler issueHandler = new JiraIssueHandler(ticketGenInfo);

		int ticketCount = 0;
		for (final HubEvent notificationResult : notificationResults.getPolicyViolationEvents()) {
			issueHandler.createOrReOpenIssue(notificationResult);
			ticketCount++;
		}
		for (final HubEvent notificationResult : notificationResults
				.getPolicyViolationOverrideEvents()) {
			issueHandler.closeIssue(notificationResult);
		}

		// TODO can this be combined with the rule issue create loop above
		for (final HubEvent vulnerabilityResult : notificationResults.getVulnerabilityEvents()) {
			issueHandler.createOrReOpenIssue(vulnerabilityResult);
			ticketCount++;
		}
		return ticketCount;
	}
}
