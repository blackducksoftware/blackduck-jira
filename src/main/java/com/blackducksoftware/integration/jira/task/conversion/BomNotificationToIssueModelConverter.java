/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.atlassian.jira.bc.user.search.UserSearchService;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.blackducksoftware.integration.jira.common.BlackDuckDataHelper;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.BlackDuckProjectMappings;
import com.blackducksoftware.integration.jira.common.JiraUserContext;
import com.blackducksoftware.integration.jira.common.exception.ConfigurationException;
import com.blackducksoftware.integration.jira.common.model.JiraProject;
import com.blackducksoftware.integration.jira.common.model.NotificationVulnerability;
import com.blackducksoftware.integration.jira.common.notification.NotificationContent;
import com.blackducksoftware.integration.jira.common.notification.NotificationContentDetail;
import com.blackducksoftware.integration.jira.common.notification.NotificationDetailResult;
import com.blackducksoftware.integration.jira.common.notification.VulnerabilityNotificationContent;
import com.blackducksoftware.integration.jira.config.JiraServices;
import com.blackducksoftware.integration.jira.config.JiraSettingsService;
import com.blackducksoftware.integration.jira.config.model.BlackDuckJiraFieldCopyConfigSerializable;
import com.blackducksoftware.integration.jira.task.conversion.output.BlackDuckIssueAction;
import com.blackducksoftware.integration.jira.task.issue.handler.DataFormatHelper;
import com.blackducksoftware.integration.jira.task.issue.model.BlackDuckIssueModel;
import com.blackducksoftware.integration.jira.task.issue.model.BlackDuckIssueModelBuilder;
import com.blackducksoftware.integration.jira.task.issue.model.IssueCategory;
import com.synopsys.integration.blackduck.api.UriSingleResponse;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionSetView;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionView;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.generated.enumeration.PolicySummaryStatusType;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.RiskProfileView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomComponentView;
import com.synopsys.integration.blackduck.api.generated.view.VulnerableComponentView;
import com.synopsys.integration.blackduck.api.manual.component.VulnerabilitySourceQualifiedId;
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
        final BlackDuckJiraFieldCopyConfigSerializable fieldCopyConfig, final DataFormatHelper dataFormatHelper, final List<String> linksOfRulesToMonitor, final BlackDuckDataHelper blackDuckDataHelper, final BlackDuckJiraLogger logger) {
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
                    issueWrappers.addAll(createModelsFor404(restException, detail, batchStartDate));
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
        final String blackDuckProjectName = projectVersionWrapper.getProjectView().getName();
        final List<JiraProject> jiraProjects = blackDuckProjectMappings.getJiraProjects(blackDuckProjectName);
        logger.debug(String.format("There are %d jira projects configured", jiraProjects.size()));
        for (final JiraProject jiraProject : jiraProjects) {
            try {
                final Collection<BlackDuckIssueModel> createdIssueWrappers = populateModelFromContentDetail(jiraProject, projectVersionWrapper, notificationType, detail, notificationContent, batchStartDate);
                issueWrapperList.addAll(createdIssueWrappers);
            } catch (final Exception e) {
                logger.error(e);
                jiraSettingsService.addBlackDuckError(e, blackDuckProjectName, detail.getProjectVersionName().orElse("?"), jiraProject.getProjectName(), jiraUserContext.getJiraAdminUser().getName(),
                    jiraUserContext.getDefaultJiraIssueCreatorUser().getName(), "createEventDataFromContentDetail");
            }
        }
        return issueWrapperList;
    }

    private Collection<BlackDuckIssueModel> populateModelFromContentDetail(final JiraProject jiraProject, final ProjectVersionWrapper projectVersionWrapper, final NotificationType notificationType,
        final NotificationContentDetail detail, final NotificationContent notificationContent, final Date batchStartDate) throws IntegrationException, ConfigurationException {
        if (detail.getBomComponent().isPresent()) {
            final UriSingleResponse<VersionBomComponentView> bomComponentUriSingleResponse = detail.getBomComponent().get();
            logger.debug("BOM Component was present: " + bomComponentUriSingleResponse.getUri());
            final VersionBomComponentView versionBomComponent;
            try {
                versionBomComponent = blackDuckDataHelper.getBomComponent(bomComponentUriSingleResponse);
            } catch (final IntegrationRestException restException) {
                return createModelsFor404(restException, detail, batchStartDate);
            }

            final BlackDuckIssueModelBuilder blackDuckIssueModelBuilder = createCommonBlackDuckIssueBuilder(jiraProject, notificationType, batchStartDate, projectVersionWrapper, versionBomComponent);
            final ProjectVersionView projectVersionView = projectVersionWrapper.getProjectVersionView();
            if (detail.isBomEdit()) {
                return createModelsForBomEdit(blackDuckIssueModelBuilder, notificationType, versionBomComponent, projectVersionView);
            } else {
                Optional<BlackDuckIssueModel> issueModel = Optional.empty();
                if (detail.isPolicy()) {
                    final UriSingleResponse<PolicyRuleView> uriSingleResponse = detail.getPolicy().get();
                    final Optional<UriSingleResponse<VersionBomComponentView>> bomComponent = detail.getBomComponent();

                    final PolicyRuleView policyRule = blackDuckDataHelper.getResponse(uriSingleResponse);
                    issueModel = populateModelForPolicy(blackDuckIssueModelBuilder, notificationType, policyRule, projectVersionView);
                } else if (detail.isVulnerability()) {
                    final VulnerabilityNotificationContent vulnerabilityContent = (VulnerabilityNotificationContent) notificationContent;
                    issueModel = createModelForVulnerability(blackDuckIssueModelBuilder, notificationType, versionBomComponent.getSecurityRiskProfile(),
                        vulnerabilityContent.getNewVulnerabilityIds(), vulnerabilityContent.getUpdatedVulnerabilityIds(), vulnerabilityContent.getDeletedVulnerabilityIds());
                }
                if (issueModel.isPresent()) {
                    return Arrays.asList(issueModel.get());
                }
            }
        } else {
            logger.warn("This notification comes from an old version of Black Duck that is not supported with this version of the plugin.");
        }
        logger.debug("Ignoring the following notification detail: " + detail);
        return Collections.emptyList();
    }

    private Optional<BlackDuckIssueModel> populateModelForPolicy(final BlackDuckIssueModelBuilder blackDuckIssueModelBuilder, final NotificationType notificationType, final PolicyRuleView policyRule,
        final ProjectVersionView projectVersionView)
        throws IntegrationException, ConfigurationException {
        final String policyRuleUrl = policyRule.getHref().orElse(null);
        if (!linksOfRulesToMonitor.contains(policyRuleUrl)) {
            return Optional.empty();
        }
        logger.debug("Creating model for policy: " + policyRule.getName());
        blackDuckIssueModelBuilder.setPolicyFields(policyRule);
        blackDuckIssueModelBuilder.setPolicyComments(notificationType);

        final IssueCategory issueCategory = IssueCategory.POLICY;
        blackDuckIssueModelBuilder.setIssueCategory(issueCategory);
        blackDuckIssueModelBuilder.setJiraIssueTypeId(getIssueTypeId(issueCategory));

        if (hasVulnerabilityRule(policyRule)) {
            final List<VulnerableComponentView> vulnerableComponentViews = blackDuckDataHelper.getAllResponses(projectVersionView, ProjectVersionView.VULNERABLE_COMPONENTS_LINK_RESPONSE);
            addVulnerabilityInfo(blackDuckIssueModelBuilder, notificationType, vulnerableComponentViews);
        }

        return Optional.of(blackDuckIssueModelBuilder.build());
    }

    private void addVulnerabilityInfo(final BlackDuckIssueModelBuilder blackDuckIssueModelBuilder, final NotificationType notificationType, final List<VulnerableComponentView> allVulnerabilities) {
        final List<NotificationVulnerability> notificationVulnerabilities = allVulnerabilities.stream()
                                                                                .map(VulnerableComponentView::getVulnerabilityWithRemediation)
                                                                                .map(vulnerabilityView -> new NotificationVulnerability(vulnerabilityView.getSource().name(), vulnerabilityView.getVulnerabilityName()))
                                                                                .collect(Collectors.toList());
        //        if (NotificationType.RULE_VIOLATION.equals(notificationType)) {
        //            setVulnerabilityComment(blackDuckIssueModelBuilder, notificationVulnerabilities, Arrays.asList(), Arrays.asList());
        //        } else if (NotificationType.RULE_VIOLATION_CLEARED.equals(notificationType)) {
        //            setVulnerabilityComment(blackDuckIssueModelBuilder, Arrays.asList(), Arrays.asList(), notificationVulnerabilities);
        //        } else if (NotificationType.POLICY_OVERRIDE.equals(notificationType)) {
        //            setVulnerabilityComment(blackDuckIssueModelBuilder, Arrays.asList(), notificationVulnerabilities, Arrays.asList());
        //        }
        setVulnerabilityComment(blackDuckIssueModelBuilder, notificationVulnerabilities, notificationVulnerabilities, notificationVulnerabilities);
    }

    private Boolean hasVulnerabilityRule(final PolicyRuleView policyRuleView) {
        final String vulnerabilityCheck = "vuln";
        final PolicyRuleExpressionSetView expression = policyRuleView.getExpression();
        final List<PolicyRuleExpressionView> expressions = expression.getExpressions();
        for (final PolicyRuleExpressionView expressionView : expressions) {
            if (expressionView.getName().contains(vulnerabilityCheck)) {
                return true;
            }
        }
        return false;
    }

    private Optional<BlackDuckIssueModel> createModelForVulnerability(final BlackDuckIssueModelBuilder blackDuckIssueModelBuilder, final NotificationType notificationType, final RiskProfileView securityRiskProfile,
        final List<VulnerabilitySourceQualifiedId> addedIds, final List<VulnerabilitySourceQualifiedId> updatedIds, final List<VulnerabilitySourceQualifiedId> deletedIds) throws IntegrationException, ConfigurationException {
        logger.debug("Creating model for vulnerability");

        setVulnerabilityComment(blackDuckIssueModelBuilder, convertToNotificationVulnerabilities(addedIds), convertToNotificationVulnerabilities(updatedIds), convertToNotificationVulnerabilities(deletedIds));

        if (!NotificationType.BOM_EDIT.equals(notificationType)) {
            BlackDuckIssueAction action = BlackDuckIssueAction.ADD_COMMENT;
            if (!blackDuckDataHelper.doesSecurityRiskProfileHaveVulnerabilities(securityRiskProfile)) {
                action = BlackDuckIssueAction.RESOLVE;
            } else if (blackDuckDataHelper.doesNotificationOnlyHaveDeletes(addedIds, updatedIds, deletedIds)) {
                action = BlackDuckIssueAction.ADD_COMMENT_IF_EXISTS;
            }
            blackDuckIssueModelBuilder.setAction(action);
        }

        final IssueCategory issueCategory = IssueCategory.VULNERABILITY;
        blackDuckIssueModelBuilder.setIssueCategory(issueCategory);
        blackDuckIssueModelBuilder.setJiraIssueTypeId(getIssueTypeId(issueCategory));

        return Optional.of(blackDuckIssueModelBuilder.build());
    }

    private List<NotificationVulnerability> convertToNotificationVulnerabilities(final List<VulnerabilitySourceQualifiedId> ids) {
        if (ids == null) {
            return Arrays.asList();
        }
        return ids.stream()
                   .map(id -> new NotificationVulnerability(id.getSource(), id.getVulnerabilityId()))
                   .collect(Collectors.toList());
    }

    private void setVulnerabilityComment(final BlackDuckIssueModelBuilder blackDuckIssueModelBuilder, final List<NotificationVulnerability> addedIds, final List<NotificationVulnerability> updatedIds,
        final List<NotificationVulnerability> deletedIds) {
        final String comment = dataFormatHelper.generateVulnerabilitiesComment(addedIds, updatedIds, deletedIds);
        blackDuckIssueModelBuilder.setVulnerabilityComments(comment);
    }

    private Collection<BlackDuckIssueModel> createModelsForBomEdit(final BlackDuckIssueModelBuilder blackDuckIssueModelBuilder, final NotificationType notificationType, final VersionBomComponentView versionBomComponent,
        final ProjectVersionView projectVersionView)
        throws IntegrationException {
        logger.debug("Populating event data for BOM Component");
        final List<BlackDuckIssueModel> issueWrappersForEdits = new ArrayList<>();

        // Vulnerability
        try {
            final RiskProfileView securityRiskProfile = versionBomComponent.getSecurityRiskProfile();
            if (blackDuckDataHelper.doesSecurityRiskProfileHaveVulnerabilities(securityRiskProfile)) {
                final Optional<BlackDuckIssueModel> vulnModel = createModelForVulnerability(blackDuckIssueModelBuilder, notificationType, securityRiskProfile, null, null, null);
                vulnModel.ifPresent(model -> issueWrappersForEdits.add(model));
            }
        } catch (final Exception e) {
            logger.error("Unable to create vulnerability template for BOM component.", e);
        }

        // Policy
        if (PolicySummaryStatusType.IN_VIOLATION.equals(versionBomComponent.getPolicyStatus())) {
            logger.debug("This component is in violation of at least one policy.");
            final List<PolicyRuleView> policyRules = blackDuckDataHelper.getAllResponses(versionBomComponent, VersionBomComponentView.POLICY_RULES_LINK_RESPONSE);
            for (final PolicyRuleView rule : policyRules) {
                if (linksOfRulesToMonitor.contains(blackDuckDataHelper.getHrefNullable(rule))) {
                    try {
                        final BlackDuckIssueModelBuilder policyWrapperBuilder = blackDuckIssueModelBuilder.copy();
                        final Optional<BlackDuckIssueModel> policyModel = populateModelForPolicy(policyWrapperBuilder, notificationType, rule, projectVersionView);
                        policyModel.ifPresent(model -> issueWrappersForEdits.add(model));
                    } catch (final Exception e) {
                        logger.error("Unable to create policy template for BOM component.", e);
                    }
                }
            }
        }
        return issueWrappersForEdits;
    }

    private Collection<BlackDuckIssueModel> createModelsFor404(final IntegrationRestException restException, final NotificationContentDetail detail, final Date batchStartDate)
        throws IntegrationException {
        logger.debug("HTTP Status Code 404: Creating event for notification with missing resources on Black Duck server.");
        if (restException.getHttpStatusCode() == 404) {
            final BlackDuckIssueModelBuilder builder = new BlackDuckIssueModelBuilder(blackDuckDataHelper, dataFormatHelper);
            builder.setAction(BlackDuckIssueAction.RESOLVE_ALL);
            builder.setIssueCategory(IssueCategory.SPECIAL);
            builder.setLastBatchStartDate(batchStartDate);
            builder.setAllIssueComments(BlackDuckJiraConstants.BLACKDUCK_COMPONENT_DELETED);
            builder.setIssueCreator(jiraUserContext.getDefaultJiraIssueCreatorUser());
            if (detail.getBomComponent().isPresent()) {
                builder.setBomComponentUri(detail.getBomComponent().get().getUri());
            }
            return Arrays.asList(builder.build());
        }
        throw restException;
    }

    private BlackDuckIssueModelBuilder createCommonBlackDuckIssueBuilder(final JiraProject jiraProject, final NotificationType notificationType, final Date batchStartDate,
        final ProjectVersionWrapper projectVersionWrapper, final VersionBomComponentView versionBomComponent) {
        final BlackDuckIssueModelBuilder builder = new BlackDuckIssueModelBuilder(blackDuckDataHelper, dataFormatHelper);
        builder.setJiraProject(jiraProject);
        builder.setAction(BlackDuckIssueAction.fromNotificationType(notificationType));
        builder.setLastBatchStartDate(batchStartDate);
        builder.setBlackDuckFields(getJiraProjectOwner(projectVersionWrapper.getProjectView().getProjectOwner()), projectVersionWrapper, versionBomComponent);
        builder.setProjectFieldCopyMappings(fieldCopyConfig.getProjectFieldCopyMappings());
        builder.setIssueCreator(lookupIssueCreator(jiraProject.getIssueCreator(), jiraUserContext));
        return builder;
    }

    private String getIssueTypeId(final IssueCategory category) throws ConfigurationException {
        String issueType = BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_ISSUE;
        if (IssueCategory.VULNERABILITY.equals(category)) {
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
                    final UserSearchService userSearchService = jiraServices.createUserSearchService();
                    for (final ApplicationUser jiraUser : userSearchService.findUsersByEmail(projectOwner.getEmail())) {
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

    private ApplicationUser lookupIssueCreator(final String issueCreatorUsername, final JiraUserContext jiraUserContext) {
        final UserManager userManager = jiraServices.getUserManager();
        ApplicationUser issueCreator = userManager.getUserByName(issueCreatorUsername);
        if (issueCreator == null) {
            issueCreator = jiraUserContext.getDefaultJiraIssueCreatorUser();
        }
        return issueCreator;
    }
}
