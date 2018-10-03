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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.atlassian.jira.bc.user.search.UserSearchService;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.user.ApplicationUser;
import com.blackducksoftware.integration.jira.common.BlackDuckDataHelper;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.BlackDuckProjectMappings;
import com.blackducksoftware.integration.jira.common.JiraUserContext;
import com.blackducksoftware.integration.jira.common.exception.ConfigurationException;
import com.blackducksoftware.integration.jira.common.model.JiraProject;
import com.blackducksoftware.integration.jira.config.JiraServices;
import com.blackducksoftware.integration.jira.config.JiraSettingsService;
import com.blackducksoftware.integration.jira.config.model.BlackDuckJiraFieldCopyConfigSerializable;
import com.blackducksoftware.integration.jira.task.conversion.output.BlackDuckEventAction;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.DataFormatHelper;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventCategory;
import com.blackducksoftware.integration.jira.task.issue.model.BlackDuckIssueBuilder;
import com.blackducksoftware.integration.jira.task.issue.model.BlackDuckIssueModel;
import com.synopsys.integration.blackduck.api.UriSingleResponse;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.generated.enumeration.PolicySummaryStatusType;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleViewV2;
import com.synopsys.integration.blackduck.api.generated.view.RiskProfileView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomComponentView;
import com.synopsys.integration.blackduck.notification.NotificationDetailResult;
import com.synopsys.integration.blackduck.notification.content.NotificationContent;
import com.synopsys.integration.blackduck.notification.content.VulnerabilityNotificationContent;
import com.synopsys.integration.blackduck.notification.content.VulnerabilitySourceQualifiedId;
import com.synopsys.integration.blackduck.notification.content.detail.NotificationContentDetail;
import com.synopsys.integration.blackduck.service.HubService;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.exception.IntegrationRestException;

public class BomNotificationToIssueModelConverter {
    private final BlackDuckJiraLogger logger;
    private final JiraServices jiraServices;
    private final JiraUserContext jiraUserContext;
    private final JiraSettingsService jiraSettingsService;
    private final BlackDuckProjectMappings blackDuckProjectMappings;
    private final BlackDuckJiraFieldCopyConfigSerializable fieldCopyConfig;
    private final BlackDuckDataHelper blackDuckDataHelper;
    private final DataFormatHelper dataFormatHelper;
    private final List<String> linksOfRulesToMonitor;

    public BomNotificationToIssueModelConverter(final JiraServices jiraServices, final JiraUserContext jiraUserContext, final JiraSettingsService jiraSettingsService, final BlackDuckProjectMappings blackDuckProjectMappings,
        final BlackDuckJiraFieldCopyConfigSerializable fieldCopyConfig, final DataFormatHelper dataFormatHelper, final List<String> linksOfRulesToMonitor, final BlackDuckDataHelper blackDuckDataHelper, final HubService blackDuckService,
        final BlackDuckJiraLogger logger) {
        this.jiraServices = jiraServices;
        this.jiraUserContext = jiraUserContext;
        this.jiraSettingsService = jiraSettingsService;
        this.blackDuckProjectMappings = blackDuckProjectMappings;
        this.fieldCopyConfig = fieldCopyConfig;
        this.dataFormatHelper = dataFormatHelper;
        this.linksOfRulesToMonitor = linksOfRulesToMonitor;
        this.blackDuckDataHelper = blackDuckDataHelper;
        this.logger = logger;
    }

    public Collection<BlackDuckIssueModel> convertToModel(final NotificationDetailResult detailResult, final Date batchStartDate) {
        logger.debug("Using BOM Notification Converter");
        final NotificationType notificationType = detailResult.getType();
        logger.debug(String.format("%s Notification: %s", notificationType, detailResult.getNotificationContent()));

        final Set<BlackDuckIssueModel> issueWrappers = new HashSet<>();
        for (final NotificationContentDetail detail : detailResult.getNotificationContentDetails()) {
            try {
                try {
                    final Collection<BlackDuckIssueModel> issueWrappersFromDetail = populateModelFromContentDetail(notificationType, detail, detailResult.getNotificationContent(), batchStartDate);
                    issueWrappers.addAll(issueWrappersFromDetail);
                } catch (final IntegrationRestException restException) {
                    logger.warn(String.format("The Black Duck resource requested was not found. It was probably deleted: %s. Caused by: %s", restException.getMessage(), restException.getCause()));
                    issueWrappers.addAll(create404Wrapper(restException, detail, batchStartDate));
                }
            } catch (final Exception e) {
                logger.error(e);
                jiraSettingsService.addBlackDuckError(e.getMessage(), "convertToModel");
            }
        }
        return issueWrappers;
    }

    private Collection<BlackDuckIssueModel> populateModelFromContentDetail(final NotificationType notificationType, final NotificationContentDetail detail, final NotificationContent notificationContent, final Date batchStartDate)
        throws IntegrationException {
        final List<BlackDuckIssueModel> issueWrapperList = new ArrayList<>();

        final ProjectVersionWrapper projectVersionWrapper = blackDuckDataHelper.getProjectVersionWrapper(detail);
        final String blackDuckProjectName = projectVersionWrapper.getProjectView().name;
        final List<JiraProject> jiraProjects = blackDuckProjectMappings.getJiraProjects(blackDuckProjectName);
        logger.debug(String.format("There are %d jira projects configured", jiraProjects.size()));
        for (final JiraProject jiraProject : jiraProjects) {
            try {
                final Collection<BlackDuckIssueModel> createdIssueWrappers = populateModelFromContentDetail(jiraProject, projectVersionWrapper, notificationType, detail, notificationContent, batchStartDate);
                issueWrapperList.addAll(createdIssueWrappers);
            } catch (final Exception e) {
                logger.error(e);
                jiraSettingsService.addBlackDuckError(e, blackDuckProjectName, detail.getProjectVersionName().orElse("?"), jiraProject.getProjectName(), jiraUserContext.getJiraAdminUser().getName(),
                    jiraUserContext.getJiraIssueCreatorUser().getName(), "createEventDataFromContentDetail");
            }
        }
        return issueWrapperList;
    }

    private Collection<BlackDuckIssueModel> populateModelFromContentDetail(final JiraProject jiraProject, final ProjectVersionWrapper projectVersionWrapper, final NotificationType notificationType,
        final NotificationContentDetail detail, final NotificationContent notificationContent, final Date batchStartDate) throws IntegrationException, ConfigurationException {
        if (detail.getBomComponent().isPresent()) {
            final UriSingleResponse<VersionBomComponentView> bomComponentUriSingleResponse = detail.getBomComponent().get();
            logger.debug("BOM Component was present: " + bomComponentUriSingleResponse.uri);
            VersionBomComponentView versionBomComponent;
            try {
                versionBomComponent = blackDuckDataHelper.getBomComponent(bomComponentUriSingleResponse);
            } catch (final IntegrationRestException restException) {
                return create404Wrapper(restException, detail, batchStartDate);
            }

            final BlackDuckIssueBuilder blackDuckIssueBuilder = createCommonBlackDuckIssueBuilder(jiraProject, notificationType, batchStartDate, projectVersionWrapper, versionBomComponent);
            if (detail.isBomEdit()) {
                return populateWrapperForBomEdit(blackDuckIssueBuilder, versionBomComponent, notificationType);
            } else {
                if (detail.isPolicy()) {
                    populateWrapperForPolicy(blackDuckIssueBuilder, detail.getPolicy().get(), notificationType);
                } else if (detail.isVulnerability()) {
                    final VulnerabilityNotificationContent vulnerabilityContent = (VulnerabilityNotificationContent) notificationContent;
                    populateWrapperForVulnerability(blackDuckIssueBuilder, versionBomComponent.securityRiskProfile,
                        vulnerabilityContent.newVulnerabilityIds, vulnerabilityContent.updatedVulnerabilityIds, vulnerabilityContent.deletedVulnerabilityIds);
                }

                final EventCategory eventCategory = EventCategory.fromNotificationType(notificationType);
                blackDuckIssueBuilder.setEventCategory(eventCategory);
                blackDuckIssueBuilder.setJiraIssueTypeId(getIssueTypeId(eventCategory));
                final BlackDuckIssueModel issueWrapper = blackDuckIssueBuilder.build();
                if (issueWrapper != null) {
                    return Arrays.asList(issueWrapper);
                }
            }
        } else {
            logger.warn("This notification comes from an old version of Black Duck that is not supported with this version of the plugin.");
        }
        logger.debug("Ignoring the following notification detail: " + detail);
        return Collections.emptyList();
    }

    private void populateWrapperForPolicy(final BlackDuckIssueBuilder blackDuckIssueBuilder, final UriSingleResponse<PolicyRuleViewV2> policyRuleUriSingleResponse, final NotificationType notificationType)
        throws IntegrationException {
        if (!linksOfRulesToMonitor.contains(policyRuleUriSingleResponse.uri)) {
            return;
        }
        final PolicyRuleViewV2 policyRule = blackDuckDataHelper.getResponse(policyRuleUriSingleResponse);
        logger.debug("Collecting data for policy: " + policyRule.name);
        blackDuckIssueBuilder.setPolicyFields(policyRule);
        blackDuckIssueBuilder.setPolicyComments(notificationType);
    }

    private void populateWrapperForVulnerability(final BlackDuckIssueBuilder blackDuckIssueBuilder, final RiskProfileView securityRiskProfile,
        final List<VulnerabilitySourceQualifiedId> addedIds, final List<VulnerabilitySourceQualifiedId> updatedIds, final List<VulnerabilitySourceQualifiedId> deletedIds) {
        logger.debug("Populating event data for vulnerability");

        final String comment = dataFormatHelper.generateVulnerabilitiesComment(addedIds, updatedIds, deletedIds);
        blackDuckIssueBuilder.setVulnerabilityComments(comment);

        BlackDuckEventAction action = BlackDuckEventAction.ADD_COMMENT;
        if (!blackDuckDataHelper.doesSecurityRiskProfileHaveVulnerabilities(securityRiskProfile)) {
            action = BlackDuckEventAction.RESOLVE;
        } else if (blackDuckDataHelper.doesNotificationOnlyHaveDeletes(addedIds, updatedIds, deletedIds)) {
            action = BlackDuckEventAction.ADD_COMMENT_IF_EXISTS;
        }
        blackDuckIssueBuilder.setAction(action);
    }

    // TODO abstract this into the "populate" methods
    private Collection<BlackDuckIssueModel> populateWrapperForBomEdit(final BlackDuckIssueBuilder blackDuckIssueBuilder, final VersionBomComponentView versionBomComponent, final NotificationType notificationType)
        throws IntegrationException {
        logger.debug("Populating event data for BOM Component");
        final List<BlackDuckIssueModel> issueWrappersForEdits = new ArrayList<>();
        if (blackDuckDataHelper.doesSecurityRiskProfileHaveVulnerabilities(versionBomComponent.securityRiskProfile)) {
            logger.debug("This component has vulnerabilities.");
            try {
                final EventCategory eventCategory = EventCategory.VULNERABILITY;
                blackDuckIssueBuilder.setEventCategory(eventCategory);
                blackDuckIssueBuilder.setJiraIssueTypeId(getIssueTypeId(eventCategory));
                final BlackDuckIssueModel vulnerabilityWrapper = blackDuckIssueBuilder.build();
                issueWrappersForEdits.add(vulnerabilityWrapper);
            } catch (final Exception e) {
                logger.error("Unable to create vulnerability template for BOM component.", e);
            }
        }

        if (PolicySummaryStatusType.IN_VIOLATION.equals(versionBomComponent.policyStatus)) {
            logger.debug("This component is in violation of at least one policy.");
            final List<PolicyRuleViewV2> policyRules = blackDuckDataHelper.getAllResponses(versionBomComponent, VersionBomComponentView.POLICY_RULES_LINK_RESPONSE);
            for (final PolicyRuleViewV2 rule : policyRules) {
                try {
                    final EventCategory eventCategory = EventCategory.POLICY;
                    blackDuckIssueBuilder.setEventCategory(eventCategory);
                    blackDuckIssueBuilder.setJiraIssueTypeId(getIssueTypeId(eventCategory));
                    final BlackDuckIssueBuilder policyWrapperBuilder = blackDuckIssueBuilder.copy();
                    policyWrapperBuilder.setPolicyFields(rule);
                    policyWrapperBuilder.setPolicyComments(notificationType);
                    final BlackDuckIssueModel createdWrapper = policyWrapperBuilder.build();
                    issueWrappersForEdits.add(createdWrapper);
                } catch (final Exception e) {
                    logger.error("Unable to create policy template for BOM component.", e);
                }
            }
        }
        return issueWrappersForEdits;
    }

    private Collection<BlackDuckIssueModel> create404Wrapper(final IntegrationRestException restException, final NotificationContentDetail detail, final Date batchStartDate)
        throws IntegrationException {
        logger.debug("HTTP Status Code 404: Creating event for notification with missing resources on Black Duck server.");
        if (restException.getHttpStatusCode() == 404) {
            final BlackDuckIssueBuilder builder = new BlackDuckIssueBuilder(blackDuckDataHelper, dataFormatHelper);
            builder.setAction(BlackDuckEventAction.RESOLVE_ALL);
            builder.setEventCategory(EventCategory.SPECIAL);
            builder.setLastBatchStartDate(batchStartDate);
            builder.setAllIssueComments(BlackDuckJiraConstants.BLACKDUCK_COMPONENT_DELETED);
            if (detail.getBomComponent().isPresent()) {
                builder.setBomComponentUri(detail.getBomComponent().get().uri);
            }
            return Arrays.asList(builder.build());
        }
        throw restException;
    }

    private BlackDuckIssueBuilder createCommonBlackDuckIssueBuilder(final JiraProject jiraProject, final NotificationType notificationType, final Date batchStartDate,
        final ProjectVersionWrapper projectVersionWrapper, final VersionBomComponentView versionBomComponent) {
        final BlackDuckIssueBuilder builder = new BlackDuckIssueBuilder(blackDuckDataHelper, dataFormatHelper);
        builder.setJiraProject(jiraProject);
        builder.setAction(BlackDuckEventAction.fromNotificationType(notificationType));
        builder.setLastBatchStartDate(batchStartDate);
        builder.setBlackDuckFields(getJiraProjectOwner(projectVersionWrapper.getProjectView().projectOwner), projectVersionWrapper, versionBomComponent);
        builder.setProjectFieldCopyMappings(fieldCopyConfig.getProjectFieldCopyMappings());
        builder.setIssueCreatorUsername(jiraUserContext.getJiraIssueCreatorUser().getUsername());
        return builder;
    }

    private final String getIssueTypeId(final EventCategory category) throws ConfigurationException {
        String issueType = BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_ISSUE;
        if (EventCategory.VULNERABILITY.equals(category)) {
            issueType = BlackDuckJiraConstants.BLACKDUCK_VULNERABILITY_ISSUE;
        }
        return lookUpIssueTypeId(issueType);
    }

    private String lookUpIssueTypeId(final String targetIssueTypeName) throws ConfigurationException {
        final Collection<IssueType> issueTypes = jiraServices.getConstantsManager().getAllIssueTypeObjects();
        for (final IssueType issueType : issueTypes) {
            if (targetIssueTypeName.equals(issueType.getName())) {
                return issueType.getId();
            }
        }
        throw new ConfigurationException("IssueType " + targetIssueTypeName + " not found");
    }

    private ApplicationUser getJiraProjectOwner(final String blackDuckProjectOwner) {
        try {
            if (blackDuckProjectOwner != null) {
                final UserView projectOwner = blackDuckDataHelper.getResponse(blackDuckProjectOwner, UserView.class);
                if (projectOwner != null) {
                    final UserSearchService userSearchService = jiraServices.getUserSearchService();
                    for (final ApplicationUser jiraUser : userSearchService.findUsersByEmail(projectOwner.email)) {
                        // We will assume that if users are configured correctly, they will have unique email addresses.
                        return jiraUser;
                    }
                }
            }
        } catch (final Exception e) {
            logger.warn("Unable to get the project owner for this notification: " + e.getMessage());
        }
        return null;
    }
}