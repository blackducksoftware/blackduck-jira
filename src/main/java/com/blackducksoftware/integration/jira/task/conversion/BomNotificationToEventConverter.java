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
import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.BlackDuckProjectMappings;
import com.blackducksoftware.integration.jira.common.JiraUserContext;
import com.blackducksoftware.integration.jira.common.exception.ConfigurationException;
import com.blackducksoftware.integration.jira.common.exception.EventDataBuilderException;
import com.blackducksoftware.integration.jira.common.model.JiraProject;
import com.blackducksoftware.integration.jira.config.JiraServices;
import com.blackducksoftware.integration.jira.config.JiraSettingsService;
import com.blackducksoftware.integration.jira.config.model.BlackDuckJiraFieldCopyConfigSerializable;
import com.blackducksoftware.integration.jira.task.conversion.output.BlackDuckEventAction;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventCategory;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.DataFormatHelper;
import com.blackducksoftware.integration.jira.task.issue.model.BlackDuckIssueBuilder;
import com.blackducksoftware.integration.jira.task.issue.model.BlackDuckIssueWrapper;
import com.synopsys.integration.blackduck.api.UriSingleResponse;
import com.synopsys.integration.blackduck.api.core.HubResponse;
import com.synopsys.integration.blackduck.api.generated.component.RiskCountView;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.generated.enumeration.PolicySummaryStatusType;
import com.synopsys.integration.blackduck.api.generated.enumeration.RiskCountType;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleViewV2;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.RiskProfileView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomComponentView;
import com.synopsys.integration.blackduck.notification.NotificationDetailResult;
import com.synopsys.integration.blackduck.notification.content.NotificationContent;
import com.synopsys.integration.blackduck.notification.content.VulnerabilityNotificationContent;
import com.synopsys.integration.blackduck.notification.content.VulnerabilitySourceQualifiedId;
import com.synopsys.integration.blackduck.notification.content.detail.NotificationContentDetail;
import com.synopsys.integration.blackduck.service.HubService;
import com.synopsys.integration.blackduck.service.bucket.HubBucket;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.exception.IntegrationRestException;

public class BomNotificationToEventConverter {
    private final BlackDuckJiraLogger logger;
    private final JiraServices jiraServices;
    private final JiraUserContext jiraUserContext;
    private final JiraSettingsService jiraSettingsService;
    private final BlackDuckProjectMappings blackDuckProjectMappings;
    private final BlackDuckJiraFieldCopyConfigSerializable fieldCopyConfig;
    private final DataFormatHelper dataFormatHelper;
    private final List<String> linksOfRulesToMonitor;
    private final HubService blackDuckService;

    public BomNotificationToEventConverter(final JiraServices jiraServices, final JiraUserContext jiraUserContext, final JiraSettingsService jiraSettingsService, final BlackDuckProjectMappings blackDuckProjectMappings,
        final BlackDuckJiraFieldCopyConfigSerializable fieldCopyConfig, final DataFormatHelper dataFormatHelper, final List<String> linksOfRulesToMonitor, final HubService blackDuckSerivce, final BlackDuckJiraLogger logger) {
        this.jiraServices = jiraServices;
        this.jiraUserContext = jiraUserContext;
        this.jiraSettingsService = jiraSettingsService;
        this.blackDuckProjectMappings = blackDuckProjectMappings;
        this.fieldCopyConfig = fieldCopyConfig;
        this.dataFormatHelper = dataFormatHelper;
        this.linksOfRulesToMonitor = linksOfRulesToMonitor;
        this.blackDuckService = blackDuckSerivce;
        this.logger = logger;
    }

    public Collection<BlackDuckIssueWrapper> convertToEventData(final NotificationDetailResult detailResult, final HubBucket blackDuckBucket, final Date batchStartDate) {
        logger.debug("Using BOM Notification Converter");
        final NotificationType notificationType = detailResult.getType();
        logger.debug(String.format("%s Notification: %s", notificationType, detailResult.getNotificationContent()));

        final Set<BlackDuckIssueWrapper> issueWrappers = new HashSet<>();
        for (final NotificationContentDetail detail : detailResult.getNotificationContentDetails()) {
            try {
                try {
                    final Collection<BlackDuckIssueWrapper> issueWrappersFromDetail = populateEventDataFromContentDetail(notificationType, detail, detailResult.getNotificationContent(), blackDuckBucket, batchStartDate);
                    issueWrappers.addAll(issueWrappersFromDetail);
                } catch (final IntegrationRestException restException) {
                    logger.warn(String.format("The Black Duck resource requested was not found. It was probably deleted: %s. Caused by: %s", restException.getMessage(), restException.getCause()));
                    issueWrappers.addAll(create404Wrapper(restException, detail, batchStartDate, blackDuckBucket));
                }
            } catch (final Exception e) {
                logger.error(e);
                jiraSettingsService.addBlackDuckError(e.getMessage(), "convertToEventData");
            }
        }
        return issueWrappers;
    }

    private Collection<BlackDuckIssueWrapper> populateEventDataFromContentDetail(final NotificationType notificationType, final NotificationContentDetail detail, final NotificationContent notificationContent,
        final HubBucket blackDuckBucket, final Date batchStartDate) throws IntegrationException {
        final List<BlackDuckIssueWrapper> issueWrapperList = new ArrayList<>();

        final ProjectVersionWrapper projectVersionWrapper = getProjectVersionWrapper(detail, blackDuckBucket);
        final String blackDuckProjectName = projectVersionWrapper.getProjectView().name;
        final List<JiraProject> jiraProjects = blackDuckProjectMappings.getJiraProjects(blackDuckProjectName);
        logger.debug(String.format("There are %d jira projects configured", jiraProjects.size()));
        for (final JiraProject jiraProject : jiraProjects) {
            try {
                final Collection<BlackDuckIssueWrapper> createdIssueWrappers = populateEventDataFromContentDetail(jiraProject, projectVersionWrapper, notificationType, detail, notificationContent, blackDuckBucket, batchStartDate);
                issueWrapperList.addAll(createdIssueWrappers);
            } catch (final Exception e) {
                logger.error(e);
                jiraSettingsService.addBlackDuckError(e, blackDuckProjectName, detail.getProjectVersionName().orElse("?"), jiraProject.getProjectName(), jiraUserContext.getJiraAdminUser().getName(),
                    jiraUserContext.getJiraIssueCreatorUser().getName(), "createEventDataFromContentDetail");
            }
        }
        return issueWrapperList;
    }

    private Collection<BlackDuckIssueWrapper> populateEventDataFromContentDetail(final JiraProject jiraProject, final ProjectVersionWrapper projectVersionWrapper, final NotificationType notificationType,
        final NotificationContentDetail detail, final NotificationContent notificationContent, final HubBucket blackDuckBucket, final Date batchStartDate) throws IntegrationException, EventDataBuilderException, ConfigurationException {
        if (detail.getBomComponent().isPresent()) {
            final UriSingleResponse<VersionBomComponentView> bomComponentUriSingleResponse = detail.getBomComponent().get();
            logger.debug("BOM Component was present: " + bomComponentUriSingleResponse.uri);
            VersionBomComponentView versionBomComponent;
            try {
                versionBomComponent = getBomComponent(bomComponentUriSingleResponse, blackDuckBucket);
            } catch (final IntegrationRestException restException) {
                return create404Wrapper(restException, detail, batchStartDate, blackDuckBucket);
            }

            final BlackDuckIssueBuilder blackDuckIssueBuilder = createCommonBlackDuckIssueBuilder(jiraProject, notificationType, batchStartDate, projectVersionWrapper, versionBomComponent, blackDuckBucket);
            if (detail.isBomEdit()) {
                return populateWrapperForBomEdit(blackDuckIssueBuilder, versionBomComponent, notificationType);
            } else {
                if (detail.isPolicy()) {
                    populateWrapperForPolicy(blackDuckIssueBuilder, detail.getPolicy().get(), notificationType, blackDuckBucket);
                } else if (detail.isVulnerability()) {
                    final VulnerabilityNotificationContent vulnerabilityContent = (VulnerabilityNotificationContent) notificationContent;
                    populateWrapperForVulnerability(blackDuckIssueBuilder, versionBomComponent.securityRiskProfile,
                        vulnerabilityContent.newVulnerabilityIds, vulnerabilityContent.updatedVulnerabilityIds, vulnerabilityContent.deletedVulnerabilityIds);
                }

                final EventCategory eventCategory = EventCategory.fromNotificationType(notificationType);
                blackDuckIssueBuilder.setEventCategory(eventCategory);
                blackDuckIssueBuilder.setJiraIssueTypeId(getIssueTypeId(eventCategory));
                final BlackDuckIssueWrapper issueWrapper = blackDuckIssueBuilder.build();
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

    private void populateWrapperForPolicy(final BlackDuckIssueBuilder blackDuckIssueBuilder, final UriSingleResponse<PolicyRuleViewV2> policyRuleUriSingleResponse, final NotificationType notificationType, final HubBucket blackDuckBucket)
        throws IntegrationException {
        if (!linksOfRulesToMonitor.contains(policyRuleUriSingleResponse.uri)) {
            return;
        }
        final PolicyRuleViewV2 policyRule = getBlackDuckResponse(policyRuleUriSingleResponse, blackDuckBucket);
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
        if (!doesSecurityRiskProfileHaveVulnerabilities(securityRiskProfile)) {
            action = BlackDuckEventAction.RESOLVE;
        } else if (doesNotificationOnlyHaveDeletes(addedIds, updatedIds, deletedIds)) {
            action = BlackDuckEventAction.ADD_COMMENT_IF_EXISTS;
        }
        blackDuckIssueBuilder.setAction(action);
    }

    // TODO abstract this into the "populate" methods
    private Collection<BlackDuckIssueWrapper> populateWrapperForBomEdit(final BlackDuckIssueBuilder blackDuckIssueBuilder, final VersionBomComponentView versionBomComponent, final NotificationType notificationType)
        throws IntegrationException {
        logger.debug("Populating event data for BOM Component");
        final List<BlackDuckIssueWrapper> issueWrappersForEdits = new ArrayList<>();
        if (doesSecurityRiskProfileHaveVulnerabilities(versionBomComponent.securityRiskProfile)) {
            logger.debug("This component has vulnerabilities.");
            try {
                final EventCategory eventCategory = EventCategory.VULNERABILITY;
                blackDuckIssueBuilder.setEventCategory(eventCategory);
                blackDuckIssueBuilder.setJiraIssueTypeId(getIssueTypeId(eventCategory));
                final BlackDuckIssueWrapper vulnerabilityWrapper = blackDuckIssueBuilder.build();
                issueWrappersForEdits.add(vulnerabilityWrapper);
            } catch (final Exception e) {
                logger.error("Unable to create vulnerability template for BOM component.", e);
            }
        }

        if (PolicySummaryStatusType.IN_VIOLATION.equals(versionBomComponent.policyStatus)) {
            logger.debug("This component is in violation of at least one policy.");
            final List<PolicyRuleViewV2> policyRules = blackDuckService.getAllResponses(versionBomComponent, VersionBomComponentView.POLICY_RULES_LINK_RESPONSE);
            for (final PolicyRuleViewV2 rule : policyRules) {
                try {
                    final EventCategory eventCategory = EventCategory.POLICY;
                    blackDuckIssueBuilder.setEventCategory(eventCategory);
                    blackDuckIssueBuilder.setJiraIssueTypeId(getIssueTypeId(eventCategory));
                    final BlackDuckIssueBuilder policyWrapperBuilder = blackDuckIssueBuilder.copy();
                    policyWrapperBuilder.setPolicyFields(rule);
                    policyWrapperBuilder.setPolicyComments(notificationType);
                    final BlackDuckIssueWrapper createdWrapper = policyWrapperBuilder.build();
                    issueWrappersForEdits.add(createdWrapper);
                } catch (final Exception e) {
                    logger.error("Unable to create policy template for BOM component.", e);
                }
            }
        }
        return issueWrappersForEdits;
    }

    private Collection<BlackDuckIssueWrapper> create404Wrapper(final IntegrationRestException restException, final NotificationContentDetail detail, final Date batchStartDate, final HubBucket blackDuckBucket)
        throws IntegrationException {
        logger.debug("HTTP Status Code 404: Creating event for notification with missing resources on Black Duck server.");
        if (restException.getHttpStatusCode() == 404) {
            final BlackDuckIssueBuilder builder = new BlackDuckIssueBuilder(blackDuckService, blackDuckBucket, dataFormatHelper);
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
        final ProjectVersionWrapper projectVersionWrapper, final VersionBomComponentView versionBomComponent, final HubBucket blackDuckBucket) throws IntegrationException {
        final BlackDuckIssueBuilder builder = new BlackDuckIssueBuilder(blackDuckService, blackDuckBucket, dataFormatHelper);
        builder.setJiraProject(jiraProject);
        builder.setAction(BlackDuckEventAction.fromNotificationType(notificationType));
        builder.setLastBatchStartDate(batchStartDate);
        builder.setBlackDuckFields(getJiraProjectOwner(projectVersionWrapper.getProjectView().projectOwner, blackDuckBucket), projectVersionWrapper, versionBomComponent);
        builder.setProjectFieldCopyMappings(fieldCopyConfig.getProjectFieldCopyMappings());
        builder.setIssueCreatorUsername(jiraUserContext.getJiraIssueCreatorUser().getUsername());
        return builder;
    }

    // ==============================
    // BLACK DUCK API UTILITY METHODS
    // ==============================

    private VersionBomComponentView getBomComponent(final UriSingleResponse<VersionBomComponentView> bomComponentLocation, final HubBucket blackDuckBucket) throws IntegrationException {
        IntegrationRestException restException = null;
        try {
            final VersionBomComponentView versionBomComponentView = getBlackDuckResponse(bomComponentLocation, blackDuckBucket);
            return versionBomComponentView;
        } catch (final IntegrationRestException caughtRestException) {
            restException = caughtRestException;
        } catch (final Exception genericException) {
            logger.error(genericException);
            throw genericException;
        }
        throw restException;
    }

    private ProjectVersionWrapper getProjectVersionWrapper(final NotificationContentDetail detail, final HubBucket blackDuckBucket) throws IntegrationException {
        final ProjectVersionWrapper projectVersionWrapper;
        if (detail.getProjectVersion().isPresent()) {
            final UriSingleResponse<ProjectVersionView> projectVersionResponse = detail.getProjectVersion().get();
            projectVersionWrapper = getProjectVersionWrapper(projectVersionResponse.uri, blackDuckBucket);
        } else if (detail.getBomComponent().isPresent()) {
            final VersionBomComponentView versionBomComponent = getBlackDuckResponse(detail.getBomComponent().get(), blackDuckBucket);
            projectVersionWrapper = getProjectVersionWrapper(versionBomComponent, blackDuckBucket);
        } else {
            throw new IntegrationException("No Black Duck project data available from the notification.");
        }
        return projectVersionWrapper;
    }

    private ProjectVersionWrapper getProjectVersionWrapper(final VersionBomComponentView versionBomComponent, final HubBucket blackDuckBucket) throws IntegrationException {
        // TODO Stop using this when Black Duck supports going back to the project-version
        final String versionBomComponentHref = blackDuckService.getHref(versionBomComponent);
        final int componentsIndex = versionBomComponentHref.indexOf(ProjectVersionView.COMPONENTS_LINK);
        final String projectVersionUri = versionBomComponentHref.substring(0, componentsIndex - 1);

        return getProjectVersionWrapper(projectVersionUri, blackDuckBucket);
    }

    private ProjectVersionWrapper getProjectVersionWrapper(final String projectVersionUri, final HubBucket blackDuckBucket) throws IntegrationException {
        final ProjectVersionView projectVersion = getBlackDuckResponse(projectVersionUri, ProjectVersionView.class, blackDuckBucket);
        final ProjectView project = blackDuckService.getResponse(projectVersion, ProjectVersionView.PROJECT_LINK_RESPONSE);

        final ProjectVersionWrapper wrapper = new ProjectVersionWrapper();
        wrapper.setProjectVersionView(projectVersion);
        wrapper.setProjectView(project);
        return wrapper;
    }

    private boolean doesSecurityRiskProfileHaveVulnerabilities(final RiskProfileView securityRiskProfile) {
        logger.debug("Checking if the component still has vulnerabilities...");
        final int vulnerablitiesCount = getSumOfRiskCounts(securityRiskProfile.counts);
        logger.debug("Number of vulnerabilities found: " + vulnerablitiesCount);
        if (vulnerablitiesCount > 0) {
            logger.debug("This component still has vulnerabilities");
            return true;
        }
        return false;
    }

    private int getSumOfRiskCounts(final List<RiskCountView> vulnerabilityCounts) {
        int count = 0;
        for (final RiskCountView riskCount : vulnerabilityCounts) {
            if (!RiskCountType.OK.equals(riskCount.countType)) {
                count += riskCount.count.intValue();
            }
        }
        return count;
    }

    private boolean doesNotificationOnlyHaveDeletes(final List<VulnerabilitySourceQualifiedId> addedIds, final List<VulnerabilitySourceQualifiedId> updatedIds, final List<VulnerabilitySourceQualifiedId> deletedIds) {
        return deletedIds.size() > 0 && addedIds.size() == 0 && updatedIds.size() == 0;
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

    // TODO can this be moved?
    private ApplicationUser getJiraProjectOwner(final String blackDuckProjectOwner, final HubBucket blackDuckBucket) {
        try {
            if (blackDuckProjectOwner != null) {
                final UserView projectOwner = getBlackDuckResponse(blackDuckProjectOwner, UserView.class, blackDuckBucket);
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

    private <T extends HubResponse> T getBlackDuckResponse(final String uri, final Class<T> clazz, final HubBucket blackDuckBucket) throws IntegrationException {
        return getBlackDuckResponse(new UriSingleResponse<>(uri, clazz), blackDuckBucket);
    }

    private <T extends HubResponse> T getBlackDuckResponse(final UriSingleResponse<T> uriSingleResponse, final HubBucket blackDuckBucket) throws IntegrationException {
        T response = blackDuckBucket.get(uriSingleResponse);
        if (response == null) {
            response = blackDuckService.getResponse(uriSingleResponse);
        }
        return response;
    }
}