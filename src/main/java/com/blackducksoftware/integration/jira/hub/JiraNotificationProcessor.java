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

import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.jira.HubJiraLogger;
import com.blackducksoftware.integration.jira.config.HubProjectMappings;
import com.blackducksoftware.integration.jira.hub.model.notification.NotificationItem;

public class JiraNotificationProcessor {
	private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));
	public static final String PROJECT_LINK = "project";
	private final ConverterLookupTable converterTable;

	public JiraNotificationProcessor(final HubNotificationService hubNotificationService,
			final HubProjectMappings mapping, final List<String> linksOfRulesToMonitor,
			final TicketGeneratorInfo ticketGenInfo) {
		converterTable = new ConverterLookupTable(mapping, ticketGenInfo, linksOfRulesToMonitor, hubNotificationService);
	}

	public HubEvents generateEvents(final List<NotificationItem> notifications)
			throws HubNotificationServiceException {
		final HubEvents allResults = new HubEvents();

		logger.debug("JiraNotificationFilter.extractJiraReadyNotifications(): Sifting through " + notifications.size()
				+ " notifications");
		for (final NotificationItem notif : notifications) {
			logger.debug("Notification: " + notif);

			HubEvents notifResults;
			try {
				notifResults = generateEvents(notif);
			} catch (final UnexpectedHubResponseException e) {
				throw new HubNotificationServiceException("Error converting notifications to issues", e);
			}
			if (notifResults != null) {
				allResults.addAllEvents(notifResults);
			}
		}
		return allResults;
	}

	private HubEvents generateEvents(final NotificationItem notif) throws UnexpectedHubResponseException,
	HubNotificationServiceException {
		final NotificationToEventConverter converter = converterTable.getConverter(notif);
		final HubEvents events = converter.generateEvents(notif);
		return events;
	}

}