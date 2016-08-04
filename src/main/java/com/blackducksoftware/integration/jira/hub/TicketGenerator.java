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
import com.blackducksoftware.integration.jira.issue.JiraServices;

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
	private final JiraContext jiraContext;
	private final JiraServices jiraServices;

	public TicketGenerator(final NotificationService notificationService,
			final JiraServices jiraServices,
			final JiraContext jiraContext) {
		this.notificationService = notificationService;
		this.jiraServices = jiraServices;
		this.jiraContext = jiraContext;
	}

	public void generateTicketsForRecentNotifications(final HubProjectMappings hubProjectMappings,
			final List<String> linksOfRulesToMonitor,
			final NotificationDateRange notificationDateRange) throws NotificationServiceException {

		final List<NotificationItem> notifs = notificationService.fetchNotifications(notificationDateRange);
		final JiraNotificationProcessor processor = new JiraNotificationProcessor(notificationService,
				hubProjectMappings, linksOfRulesToMonitor, jiraServices, jiraContext);

		final HubEvents events = processor.generateEvents(notifs);

		final JiraIssueHandler issueHandler = new JiraIssueHandler(jiraServices, jiraContext);

		// TODO Shouldn't need all these different types
		// event should just tell JiraIssueHandler what to do
		for (final HubEvent notificationResult : events.getPolicyViolationEvents()) {
			issueHandler.handleEvent(notificationResult);
		}
		for (final HubEvent notificationResult : events
				.getPolicyViolationOverrideEvents()) {
			issueHandler.handleEvent(notificationResult);
		}

		// TODO can this be combined with the rule issue create loop above
		for (final HubEvent event : events.getVulnerabilityEvents()) {
			issueHandler.handleEvent(event);
		}
	}
}
