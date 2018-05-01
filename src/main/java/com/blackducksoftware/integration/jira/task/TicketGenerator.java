/**
 * Hub JIRA Plugin
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
 */
package com.blackducksoftware.integration.jira.task;

import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;

import com.blackducksoftware.integration.hub.api.generated.view.NotificationUserView;
import com.blackducksoftware.integration.hub.api.generated.view.UserView;
import com.blackducksoftware.integration.hub.api.view.MetaHandler;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.HubItemTransformException;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.service.IssueService;
import com.blackducksoftware.integration.hub.service.NotificationService;
import com.blackducksoftware.integration.hub.throwaway.NotificationContentItem;
import com.blackducksoftware.integration.hub.throwaway.NotificationEvent;
import com.blackducksoftware.integration.hub.throwaway.OldNotificationResults;
import com.blackducksoftware.integration.hub.throwaway.OldNotificationService;
import com.blackducksoftware.integration.hub.throwaway.PolicyNotificationFilter;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.common.HubProjectMappings;
import com.blackducksoftware.integration.jira.common.JiraContext;
import com.blackducksoftware.integration.jira.common.TicketInfoFromSetup;
import com.blackducksoftware.integration.jira.config.HubJiraFieldCopyConfigSerializable;
import com.blackducksoftware.integration.jira.task.conversion.JiraNotificationProcessor;
import com.blackducksoftware.integration.jira.task.issue.HubIssueTrackerHandler;
import com.blackducksoftware.integration.jira.task.issue.JiraIssueHandler;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;

/**
 * Collects recent notifications from the Hub, and generates JIRA tickets for them.
 */
public class TicketGenerator {
    private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final HubServicesFactory hubServicesFactory;
    private final OldNotificationService oldNotificationService;
    private final NotificationService notificationService;
    private final JiraContext jiraContext;
    private final JiraServices jiraServices;
    private final JiraSettingsService jiraSettingsService;
    private final TicketInfoFromSetup ticketInfoFromSetup;
    private final HubJiraFieldCopyConfigSerializable fieldCopyConfig;
    private final boolean createVulnerabilityIssues;
    private final HubIssueTrackerHandler hubIssueTrackerHandler;

    public TicketGenerator(final HubServicesFactory hubServicesFactory, final JiraServices jiraServices, final JiraContext jiraContext, final JiraSettingsService jiraSettingsService, final TicketInfoFromSetup ticketInfoFromSetup,
            final HubJiraFieldCopyConfigSerializable fieldCopyConfig, final boolean createVulnerabilityIssues, final List<String> linksOfRulesToInclude) {
        this.hubServicesFactory = hubServicesFactory;
        final PolicyNotificationFilter policyNotificationFilter = new PolicyNotificationFilter(linksOfRulesToInclude);
        final HubService hubService = hubServicesFactory.createHubService();
        this.oldNotificationService = new OldNotificationService(hubService, policyNotificationFilter);
        this.notificationService = hubServicesFactory.createNotificationService();
        this.jiraServices = jiraServices;
        this.jiraContext = jiraContext;
        this.jiraSettingsService = jiraSettingsService;
        this.ticketInfoFromSetup = ticketInfoFromSetup;
        this.fieldCopyConfig = fieldCopyConfig;
        this.createVulnerabilityIssues = createVulnerabilityIssues;
        final IssueService issueService = hubServicesFactory.createIssueService();
        this.hubIssueTrackerHandler = new HubIssueTrackerHandler(jiraServices, jiraSettingsService, issueService);
    }

    public void generateTicketsForRecentNotifications(final UserView hubUser, final HubProjectMappings hubProjectMappings, final Date startDate, final Date endDate) throws HubIntegrationException {
        if ((hubProjectMappings == null) || (hubProjectMappings.size() == 0)) {
            logger.debug("The configuration does not specify any Hub projects to monitor");
            return;
        }
        try {
            final List<NotificationUserView> notificationUserViews = notificationService.getAllUserNotifications(hubUser, startDate, endDate);
            final OldNotificationResults results = oldNotificationService.getAllUserNotificationResults(notificationUserViews);
            reportAnyErrors(results);
            final SortedSet<NotificationContentItem> notifs = results.getNotificationContentItems();
            if ((notifs == null) || (notifs.size() == 0)) {
                logger.info("There are no notifications to handle");
                return;
            }
            final JiraNotificationProcessor processor = new JiraNotificationProcessor(hubProjectMappings, fieldCopyConfig, jiraServices, jiraContext, jiraSettingsService, hubServicesFactory, new MetaHandler(logger),
                    createVulnerabilityIssues);

            final List<NotificationEvent> events = processor.process(notifs);
            if ((events == null) || (events.size() == 0)) {
                logger.info("There are no events to handle");
                return;
            }

            final JiraIssueHandler issueHandler = new JiraIssueHandler(jiraServices, jiraContext, jiraSettingsService, ticketInfoFromSetup, hubIssueTrackerHandler);

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

    private void reportAnyErrors(final OldNotificationResults results) {
        if (results.isError()) {
            for (final Exception e : results.getExceptions()) {
                if ((e instanceof ExecutionException) && (e.getCause() != null) && (e.getCause() instanceof HubItemTransformException)) {
                    final String msg = String.format(
                            "WARNING: An error occurred while collecting supporting information from the Hub for a notification: %s; This can be caused by deletion of Hub data (project version, component, etc.) relevant to the notification soon after the notification was generated",
                            e.getMessage());
                    logger.warn(msg);
                    jiraSettingsService.addHubError(msg, "getAllNotifications");
                } else {
                    logger.error("Error retrieving notifications: " + e.getMessage(), e);
                    jiraSettingsService.addHubError(e, "getAllNotifications");
                }
            }
        }
    }

}
