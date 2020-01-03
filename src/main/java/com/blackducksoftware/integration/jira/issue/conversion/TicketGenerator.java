/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2020 Synopsys, Inc.
 * https://www.synopsys.com/
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
package com.blackducksoftware.integration.jira.issue.conversion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.issue.fields.CustomField;
import com.blackducksoftware.integration.jira.blackduck.BlackDuckDataHelper;
import com.blackducksoftware.integration.jira.common.JiraUserContext;
import com.blackducksoftware.integration.jira.common.model.PluginField;
import com.blackducksoftware.integration.jira.data.accessor.PluginErrorAccessor;
import com.blackducksoftware.integration.jira.issue.handler.BlackDuckProjectMappings;
import com.blackducksoftware.integration.jira.issue.handler.DataFormatHelper;
import com.blackducksoftware.integration.jira.issue.handler.JiraIssueHandler;
import com.blackducksoftware.integration.jira.issue.handler.JiraIssueServiceWrapper;
import com.blackducksoftware.integration.jira.issue.model.BlackDuckIssueModel;
import com.blackducksoftware.integration.jira.issue.model.TicketCriteriaConfigModel;
import com.blackducksoftware.integration.jira.issue.tracker.IssueTrackerHandler;
import com.blackducksoftware.integration.jira.web.JiraServices;
import com.blackducksoftware.integration.jira.web.model.BlackDuckJiraFieldCopyConfigSerializable;
import com.blackducksoftware.integration.jira.workflow.notification.CommonNotificationService;
import com.blackducksoftware.integration.jira.workflow.notification.CommonNotificationView;
import com.blackducksoftware.integration.jira.workflow.notification.NotificationDetailResult;
import com.blackducksoftware.integration.jira.workflow.notification.NotificationDetailResults;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.api.manual.view.NotificationUserView;
import com.synopsys.integration.blackduck.exception.BlackDuckApiException;
import com.synopsys.integration.blackduck.exception.BlackDuckItemTransformException;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.NotificationService;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucket;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucketService;
import com.synopsys.integration.rest.exception.IntegrationRestException;

/**
 * Collects recent notifications from Black Duck, and generates JIRA tickets for them.
 */
public class TicketGenerator {
    private final Logger logger = LoggerFactory.getLogger(TicketGenerator.class);

    private final BlackDuckService blackDuckService;
    private final BlackDuckBucketService blackDuckBucketService;
    private final NotificationService notificationService;
    private final CommonNotificationService commonNotificationService;
    private final JiraUserContext jiraUserContext;
    private final JiraServices jiraServices;
    private final PluginErrorAccessor pluginErrorAccessor;
    private final Map<PluginField, CustomField> customFields;
    private final IssueTrackerHandler issueTrackerHandler;
    private final List<String> linksOfRulesToMonitor;
    private final BlackDuckJiraFieldCopyConfigSerializable fieldCopyConfig;

    public TicketGenerator(BlackDuckService blackDuckService, BlackDuckBucketService blackDuckBucketService, NotificationService notificationService, CommonNotificationService commonNotificationService,
        JiraServices jiraServices, JiraUserContext jiraUserContext, PluginErrorAccessor pluginErrorAccessor, Map<PluginField, CustomField> customFields,
        List<String> listOfRulesToMonitor, BlackDuckJiraFieldCopyConfigSerializable fieldCopyConfig) {
        this.blackDuckService = blackDuckService;
        this.blackDuckBucketService = blackDuckBucketService;
        this.notificationService = notificationService;
        this.commonNotificationService = commonNotificationService;
        this.jiraServices = jiraServices;
        this.jiraUserContext = jiraUserContext;
        this.pluginErrorAccessor = pluginErrorAccessor;
        this.customFields = customFields;
        this.issueTrackerHandler = new IssueTrackerHandler(pluginErrorAccessor, blackDuckService);
        this.linksOfRulesToMonitor = listOfRulesToMonitor;
        this.fieldCopyConfig = fieldCopyConfig;
    }

    public Date generateTicketsForNotificationsInDateRange(UserView blackDuckUser, BlackDuckProjectMappings blackDuckProjectMappings, TicketCriteriaConfigModel ticketCriteria, Date startDate, Date endDate) {
        if ((blackDuckProjectMappings == null) || (blackDuckProjectMappings.size() == 0)) {
            logger.debug("The configuration does not specify any Black Duck projects to monitor");
            return startDate;
        }
        try {
            List<NotificationUserView> userNotifications = notificationService.getFilteredUserNotifications(blackDuckUser, startDate, endDate, getAllNotificationTypes());
            List<CommonNotificationView> commonNotifications = commonNotificationService.getCommonUserNotifications(userNotifications);
            NotificationDetailResults results = commonNotificationService.getNotificationDetailResults(commonNotifications);
            List<NotificationDetailResult> notificationDetailResults = results.getResults();

            BlackDuckBucket blackDuckBucket = new BlackDuckBucket();
            commonNotificationService.populateHubBucket(blackDuckBucketService, blackDuckBucket, results);
            reportAnyErrors(blackDuckBucket);

            logger.info(String.format("There are %d notifications to handle", notificationDetailResults.size()));

            if (!notificationDetailResults.isEmpty()) {
                JiraIssueServiceWrapper issueServiceWrapper = JiraIssueServiceWrapper.createIssueServiceWrapperFromJiraServices(jiraServices, jiraUserContext, customFields);
                JiraIssueHandler issueHandler = new JiraIssueHandler(issueServiceWrapper, pluginErrorAccessor, issueTrackerHandler, jiraServices.getAuthContext(), jiraUserContext, ticketCriteria);

                BlackDuckDataHelper blackDuckDataHelper = new BlackDuckDataHelper(logger, blackDuckService, blackDuckBucket);
                DataFormatHelper dataFormatHelper = new DataFormatHelper(blackDuckDataHelper);

                BomNotificationToIssueModelConverter notificationConverter = new BomNotificationToIssueModelConverter(jiraServices, jiraUserContext, pluginErrorAccessor, blackDuckProjectMappings, fieldCopyConfig,
                    dataFormatHelper, linksOfRulesToMonitor, blackDuckDataHelper, ticketCriteria);

                handleEachIssue(notificationConverter, notificationDetailResults, issueHandler, startDate);
            }

            Optional<Date> optionalCreatedAtDate = results.getLatestNotificationCreatedAtDate();
            if (optionalCreatedAtDate.isPresent()) {
                return optionalCreatedAtDate.get();
            }
        } catch (Exception e) {
            logger.error("Error occurred when generating issues.", e);
            pluginErrorAccessor.addBlackDuckError(e, "generateTicketsForNotificationsInDateRange");
        }
        return startDate;
    }

    private List<String> getAllNotificationTypes() {
        List<String> types = new ArrayList<>();
        for (NotificationType notificationType : NotificationType.values()) {
            types.add(notificationType.name());
        }
        return types;
    }

    private void handleEachIssue(BomNotificationToIssueModelConverter converter, List<NotificationDetailResult> notificationDetailResults, JiraIssueHandler issueHandler, Date batchStartDate) {
        for (NotificationDetailResult detailResult : notificationDetailResults) {
            Collection<BlackDuckIssueModel> issueModels = converter.convertToModel(detailResult, batchStartDate);
            for (BlackDuckIssueModel model : issueModels) {
                try {
                    issueHandler.handleBlackDuckIssue(model, blackDuckService.getBlackDuckBaseUrl());
                } catch (Exception e) {
                    logger.error("Error occurred while handling Black Duck issue.", e);
                    pluginErrorAccessor.addBlackDuckError(e, "issueHandler.handleBlackDuckIssue(model)");
                }
            }
        }
    }

    private void reportAnyErrors(BlackDuckBucket blackDuckBucket) {
        blackDuckBucket.getAvailableUris().parallelStream().forEach(uri -> {
            Optional<Exception> uriError = blackDuckBucket.getError(uri);
            if (uriError.isPresent()) {
                Exception e = uriError.get();
                if ((e instanceof ExecutionException) && (e.getCause() != null) && (e.getCause() instanceof BlackDuckItemTransformException)) {
                    String msg = String.format(
                        "WARNING: An error occurred while collecting supporting information from Black Duck for a notification: %s; "
                            + "This can be caused by deletion of Black Duck data (project version, component, etc.) relevant to the notification soon after the notification was generated",
                        e.getMessage());
                    logger.warn(msg);
                    pluginErrorAccessor.addBlackDuckError(msg, "generateTicketsForNotificationsInDateRange");
                } else if (e instanceof BlackDuckApiException && ((BlackDuckApiException) e).getOriginalIntegrationRestException().getHttpStatusCode() == 404) {
                    logger.debug(String.format("The Black Duck resource located at %s no longer exists. All tickets associated with that resource will be updated to reflect this.", uri));
                } else if (e instanceof IntegrationRestException && ((IntegrationRestException) e).getHttpStatusCode() == 404) {
                    logger.debug(String.format("The Black Duck resource located at %s no longer exists. All tickets associated with that resource will be updated to reflect this.", uri));
                } else {
                    logger.error("Error retrieving notifications: " + e.getMessage(), e);
                    pluginErrorAccessor.addBlackDuckError(e, "generateTicketsForNotificationsInDateRange");
                }
            }
        });
    }

}
