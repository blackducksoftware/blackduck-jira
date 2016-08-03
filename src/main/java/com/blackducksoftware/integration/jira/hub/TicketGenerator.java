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

import org.apache.log4j.Logger;

import com.blackducksoftware.integration.hub.notification.NotificationDateRange;
import com.blackducksoftware.integration.hub.notification.NotificationService;
import com.blackducksoftware.integration.hub.notification.NotificationServiceException;
import com.blackducksoftware.integration.hub.notification.api.NotificationItem;
import com.blackducksoftware.integration.jira.HubJiraLogger;
import com.blackducksoftware.integration.jira.config.HubProjectMappings;
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
	private final NotificationService notificationService;
	private final TicketGeneratorInfo ticketGenInfo;


	public TicketGenerator(final NotificationService notificationService,
			final TicketGeneratorInfo ticketGenInfo) {
		this.notificationService = notificationService;
		this.ticketGenInfo = ticketGenInfo;
	}

	public int generateTicketsForRecentNotifications(final HubProjectMappings hubProjectMappings,
			final List<String> linksOfRulesToMonitor,
			final NotificationDateRange notificationDateRange) throws NotificationServiceException {

		final List<NotificationItem> notifs = notificationService.fetchNotifications(notificationDateRange);
		final JiraNotificationProcessor processor = new JiraNotificationProcessor(notificationService,
				hubProjectMappings, linksOfRulesToMonitor, ticketGenInfo);

		final HubEvents notificationResults = processor.generateEvents(notifs);

		final JiraIssueHandler issueHandler = new JiraIssueHandler(ticketGenInfo);

		// TODO Shouldn't need all these different types
		// event should just tell JiraIssueHandler what to do
		int ticketCount = 0;
		for (final HubEvent notificationResult : notificationResults.getPolicyViolationEvents()) {
			issueHandler.handleEvent(notificationResult);
			ticketCount++;
		}
		for (final HubEvent notificationResult : notificationResults
				.getPolicyViolationOverrideEvents()) {
			issueHandler.handleEvent(notificationResult);
		}

		// TODO can this be combined with the rule issue create loop above
		for (final HubEvent event : notificationResults.getVulnerabilityEvents()) {
			issueHandler.handleEvent(event);
			ticketCount++;
		}
		return ticketCount;
	}
}
