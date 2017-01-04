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
package com.blackducksoftware.integration.jira.task;

import java.util.Date;
import java.util.List;
import java.util.SortedSet;

import org.apache.log4j.Logger;

import com.blackducksoftware.integration.hub.dataservice.notification.NotificationDataService;
import com.blackducksoftware.integration.hub.dataservice.notification.item.NotificationContentItem;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.notification.processor.event.NotificationEvent;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.common.HubProjectMappings;
import com.blackducksoftware.integration.jira.common.JiraContext;
import com.blackducksoftware.integration.jira.common.TicketInfoFromSetup;
import com.blackducksoftware.integration.jira.config.HubJiraFieldCopyConfigSerializable;
import com.blackducksoftware.integration.jira.task.conversion.JiraNotificationProcessor;
import com.blackducksoftware.integration.jira.task.issue.JiraIssueHandler;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;

/**
 * Collects recent notifications from the Hub, and generates JIRA tickets for
 * them.
 *
 * @author sbillings
 *
 */
public class TicketGenerator {
    private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final HubServicesFactory hubServicesFactory;

    private final NotificationDataService notificationDataService;

    private final JiraContext jiraContext;

    private final JiraServices jiraServices;

    private final JiraSettingsService jiraSettingsService;

    private final TicketInfoFromSetup ticketInfoFromSetup;

    private final HubJiraFieldCopyConfigSerializable fieldCopyConfig;

    public TicketGenerator(final HubServicesFactory hubServicesFactory,
            final JiraServices jiraServices,
            final JiraContext jiraContext, final JiraSettingsService jiraSettingsService,
            final TicketInfoFromSetup ticketInfoFromSetup,
            final HubJiraFieldCopyConfigSerializable fieldCopyConfig) {
        this.hubServicesFactory = hubServicesFactory;
        this.notificationDataService = hubServicesFactory.createNotificationDataService(logger);
        this.jiraServices = jiraServices;
        this.jiraContext = jiraContext;
        this.jiraSettingsService = jiraSettingsService;
        this.ticketInfoFromSetup = ticketInfoFromSetup;
        this.fieldCopyConfig = fieldCopyConfig;
    }

    public void generateTicketsForRecentNotifications(final HubProjectMappings hubProjectMappings, final Date startDate,
            final Date endDate) throws HubIntegrationException {

        if ((hubProjectMappings == null) || (hubProjectMappings.size() == 0)) {
            logger.debug("The configuration does not specify any Hub projects to monitor");
            return;
        }
        try {

            final SortedSet<NotificationContentItem> notifs = notificationDataService.getAllNotifications(startDate,
                    endDate);
            // final List<NotificationItem> notifs =
            // notificationService.fetchNotifications(notificationDateRange);
            if ((notifs == null) || (notifs.size() == 0)) {
                logger.info("There are no notifications to handle");
                return;
            }
            final JiraNotificationProcessor processor = new JiraNotificationProcessor(hubProjectMappings, fieldCopyConfig, jiraServices,
                    jiraContext, jiraSettingsService, hubServicesFactory);

            final List<NotificationEvent> events = processor.process(notifs);
            if ((events == null) || (events.size() == 0)) {
                logger.info("There are no events to handle");
                return;
            }

            final JiraIssueHandler issueHandler = new JiraIssueHandler(jiraServices, jiraContext, jiraSettingsService,
                    ticketInfoFromSetup);

            for (final NotificationEvent event : events) {
                try {
                    issueHandler.handleEvent(event);
                } catch (final Exception e) {
                    logger.error(e);
                    jiraSettingsService.addHubError(e, "issueHandler.handleEvent(event)");
                }
            }
        } catch (final Exception e) {
            logger.error(e);
            jiraSettingsService.addHubError(e, "generateTicketsForRecentNotifications");
        }

    }
}
