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

import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.hub.notification.NotificationService;
import com.blackducksoftware.integration.hub.notification.NotificationServiceException;
import com.blackducksoftware.integration.hub.notification.api.NotificationItem;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.common.HubProjectMappings;
import com.blackducksoftware.integration.jira.common.JiraContext;
import com.blackducksoftware.integration.jira.task.conversion.output.HubEvent;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;

public class JiraNotificationProcessor {
	private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));
	public static final String PROJECT_LINK = "project";
	private final ConverterLookupTable converterTable;

	public JiraNotificationProcessor(final NotificationService hubNotificationService,
			final HubProjectMappings mapping, final List<String> linksOfRulesToMonitor,
			final JiraServices jiraServices,
			final JiraContext jiraContext) {
		converterTable = new ConverterLookupTable(mapping, jiraServices, jiraContext, linksOfRulesToMonitor,
				hubNotificationService);
	}

	public List<HubEvent> generateEvents(final List<NotificationItem> notifications)
			throws NotificationServiceException {
		final List<HubEvent> allResults = new ArrayList<>();

		logger.debug("JiraNotificationFilter.extractJiraReadyNotifications(): Sifting through " + notifications.size()
				+ " notifications");
		for (final NotificationItem notif : notifications) {
			logger.debug("Notification: " + notif);

			List<HubEvent> notifResults;
			try {
				notifResults = generateEvents(notif);
			} catch (final UnexpectedHubResponseException e) {
				throw new NotificationServiceException("Error converting notifications to issues", e);
			}
			if (notifResults != null) {
				allResults.addAll(notifResults);
			}
		}
		return allResults;
	}

	private List<HubEvent> generateEvents(final NotificationItem notif) throws UnexpectedHubResponseException,
	NotificationServiceException {
		final NotificationToEventConverter converter = converterTable.getConverter(notif);
		final List<HubEvent> events = converter.generateEvents(notif);
		return events;
	}

}
