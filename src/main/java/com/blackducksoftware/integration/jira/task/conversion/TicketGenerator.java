/**
 * Black Duck JIRA Plugin
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
package com.blackducksoftware.integration.jira.task.conversion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;

import com.atlassian.jira.issue.fields.CustomField;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.BlackDuckProjectMappings;
import com.blackducksoftware.integration.jira.common.JiraUserContext;
import com.blackducksoftware.integration.jira.common.model.PluginField;
import com.blackducksoftware.integration.jira.config.JiraServices;
import com.blackducksoftware.integration.jira.config.JiraSettingsService;
import com.blackducksoftware.integration.jira.config.model.BlackDuckJiraFieldCopyConfigSerializable;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.DataFormatHelper;
import com.blackducksoftware.integration.jira.task.issue.handler.BlackDuckIssueTrackerHandler;
import com.blackducksoftware.integration.jira.task.issue.handler.JiraIssueHandler;
import com.blackducksoftware.integration.jira.task.issue.handler.JiraIssueServiceWrapper;
import com.blackducksoftware.integration.jira.task.issue.model.BlackDuckIssueWrapper;
import com.google.gson.GsonBuilder;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.generated.view.NotificationUserView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.exception.HubIntegrationException;
import com.synopsys.integration.blackduck.exception.HubItemTransformException;
import com.synopsys.integration.blackduck.notification.CommonNotificationView;
import com.synopsys.integration.blackduck.notification.NotificationDetailResult;
import com.synopsys.integration.blackduck.notification.NotificationDetailResults;
import com.synopsys.integration.blackduck.service.CommonNotificationService;
import com.synopsys.integration.blackduck.service.HubService;
import com.synopsys.integration.blackduck.service.IssueService;
import com.synopsys.integration.blackduck.service.NotificationService;
import com.synopsys.integration.blackduck.service.bucket.HubBucket;
import com.synopsys.integration.blackduck.service.bucket.HubBucketService;
import com.synopsys.integration.rest.exception.IntegrationRestException;

/**
 * Collects recent notifications from Black Duck, and generates JIRA tickets for them.
 */
public class TicketGenerator {
    private final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final HubService blackDuckService;
    private final HubBucketService blackDuckBucketService;
    private final NotificationService notificationService;
    private final CommonNotificationService commonNotificationService;
    private final JiraUserContext jiraUserContext;
    private final JiraServices jiraServices;
    private final JiraSettingsService jiraSettingsService;
    private final Map<PluginField, CustomField> customFields;
    private final BlackDuckIssueTrackerHandler blackDuckIssueTrackerHandler;
    private final boolean shouldCreateVulnerabilityIssues;
    private final List<String> linksOfRulesToMonitor;
    private final BlackDuckJiraFieldCopyConfigSerializable fieldCopyConfig;

    public TicketGenerator(final HubService blackDuckService, final HubBucketService blackDuckBucketService, final NotificationService notificationService, final CommonNotificationService commonNotificationService,
        final IssueService blackDuckIssueService, final JiraServices jiraServices, final JiraUserContext jiraUserContext, final JiraSettingsService jiraSettingsService, final Map<PluginField, CustomField> customFields,
        final boolean shouldCreateVulnerabilityIssues, final List<String> listOfRulesToMonitor, final BlackDuckJiraFieldCopyConfigSerializable fieldCopyConfig) {
        this.blackDuckService = blackDuckService;
        this.blackDuckBucketService = blackDuckBucketService;
        this.notificationService = notificationService;
        this.commonNotificationService = commonNotificationService;
        this.jiraServices = jiraServices;
        this.jiraUserContext = jiraUserContext;
        this.jiraSettingsService = jiraSettingsService;
        this.customFields = customFields;
        this.blackDuckIssueTrackerHandler = new BlackDuckIssueTrackerHandler(jiraSettingsService, blackDuckIssueService);
        this.shouldCreateVulnerabilityIssues = shouldCreateVulnerabilityIssues;
        this.linksOfRulesToMonitor = listOfRulesToMonitor;
        this.fieldCopyConfig = fieldCopyConfig;
    }

    public Date generateTicketsForNotificationsInDateRange(final UserView blackDuckUser, final BlackDuckProjectMappings blackDuckProjectMappings, final Date startDate, final Date endDate) throws HubIntegrationException {
        if ((blackDuckProjectMappings == null) || (blackDuckProjectMappings.size() == 0)) {
            logger.debug("The configuration does not specify any Black Duck projects to monitor");
            return startDate;
        }
        try {
            final List<NotificationUserView> userNotifications = notificationService.getFilteredUserNotifications(blackDuckUser, startDate, endDate, getAllNotificationTypes());
            final List<CommonNotificationView> commonNotifications = commonNotificationService.getCommonUserNotifications(userNotifications);
            final NotificationDetailResults results = commonNotificationService.getNotificationDetailResults(commonNotifications);
            final List<NotificationDetailResult> notificationDetailResults = results.getResults();

            final HubBucket blackDuckBucket = new HubBucket();
            commonNotificationService.populateHubBucket(blackDuckBucketService, blackDuckBucket, results);
            reportAnyErrors(blackDuckBucket);

            logger.info(String.format("There are %d notifications to handle", notificationDetailResults.size()));
            if (!notificationDetailResults.isEmpty()) {
                final JiraIssueServiceWrapper issueServiceWrapper = JiraIssueServiceWrapper.createIssueServiceWrapperFromJiraServices(jiraServices, jiraUserContext, new GsonBuilder().create(), customFields);
                final JiraIssueHandler issueHandler = new JiraIssueHandler(issueServiceWrapper, jiraSettingsService, blackDuckIssueTrackerHandler, jiraServices.getAuthContext(), jiraUserContext);

                final BomNotificationToEventConverter notificationConverter = new BomNotificationToEventConverter(jiraServices, jiraUserContext, jiraSettingsService, blackDuckProjectMappings, fieldCopyConfig,
                    new DataFormatHelper(blackDuckService), linksOfRulesToMonitor, blackDuckService, logger);
                handleEachIssue(notificationConverter, notificationDetailResults, issueHandler, blackDuckBucket, startDate);
            }
            if (results.getLatestNotificationCreatedAtDate().isPresent()) {
                return results.getLatestNotificationCreatedAtDate().get();
            }
        } catch (final Exception e) {
            logger.error(e);
            jiraSettingsService.addBlackDuckError(e, "generateTicketsForRecentNotifications");
        }
        return startDate;
    }

    private List<String> getAllNotificationTypes() {
        final List<String> types = new ArrayList<>();
        for (final NotificationType notificationType : NotificationType.values()) {
            types.add(notificationType.name());
        }
        return types;
    }

    private void handleEachIssue(final BomNotificationToEventConverter converter, final List<NotificationDetailResult> notificationDetailResults, final JiraIssueHandler issueHandler, final HubBucket blackDuckBucket,
        final Date batchStartDate)
        throws HubIntegrationException {
        for (final NotificationDetailResult detailResult : notificationDetailResults) {
            if (shouldCreateVulnerabilityIssues || !NotificationType.VULNERABILITY.equals(detailResult.getType())) {
                final Collection<BlackDuckIssueWrapper> issueWrappers = converter.convertToEventData(detailResult, blackDuckBucket, batchStartDate);
                for (final BlackDuckIssueWrapper wrapper : issueWrappers) {
                    try {
                        issueHandler.handleBlackDuckIssue(wrapper);
                    } catch (final Exception e) {
                        logger.error(e);
                        jiraSettingsService.addBlackDuckError(e, "issueHandler.handleEvent(event)");
                    }
                }
            }
        }
    }

    private void reportAnyErrors(final HubBucket blackDuckBucket) {
        blackDuckBucket.getAvailableUris().parallelStream().forEach(uri -> {
            final Optional<Exception> uriError = blackDuckBucket.getError(uri);
            if (uriError.isPresent()) {
                final Exception e = uriError.get();
                if ((e instanceof ExecutionException) && (e.getCause() != null) && (e.getCause() instanceof HubItemTransformException)) {
                    final String msg = String.format(
                        "WARNING: An error occurred while collecting supporting information from Black Duck for a notification: %s; This can be caused by deletion of Black Duck data (project version, component, etc.) relevant to the notification soon after the notification was generated",
                        e.getMessage());
                    logger.warn(msg);
                    jiraSettingsService.addBlackDuckError(msg, "getAllNotifications");
                } else if (e instanceof IntegrationRestException && ((IntegrationRestException) e).getHttpStatusCode() == 404) {
                    logger.debug(String.format("The Black Duck resource located at %s no longer exists. All tickets associated with that resource will be updated to reflect this.", uri));
                } else {
                    logger.error("Error retrieving notifications: " + e.getMessage(), e);
                    jiraSettingsService.addBlackDuckError(e, "getAllNotifications");
                }
            }
        });
    }

}
