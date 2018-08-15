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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventData;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventDataBuilder;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventDataFormatHelper;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.SpecialEventData;
import com.synopsys.integration.blackduck.api.UriSingleResponse;
import com.synopsys.integration.blackduck.api.core.HubResponse;
import com.synopsys.integration.blackduck.api.generated.component.RiskCountView;
import com.synopsys.integration.blackduck.api.generated.component.VersionBomLicenseView;
import com.synopsys.integration.blackduck.api.generated.component.VersionBomOriginView;
import com.synopsys.integration.blackduck.api.generated.enumeration.MatchedFileUsagesType;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.generated.enumeration.RiskCountType;
import com.synopsys.integration.blackduck.api.generated.response.VersionRiskProfileView;
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
    private final EventDataFormatHelper dataFormatHelper;
    private final List<String> linksOfRulesToMonitor;
    private final HubService blackDuckService;

    private final NotificationToEventConverter oldConverter;

    public BomNotificationToEventConverter(final JiraServices jiraServices, final JiraUserContext jiraUserContext, final JiraSettingsService jiraSettingsService, final BlackDuckProjectMappings blackDuckProjectMappings,
            final BlackDuckJiraFieldCopyConfigSerializable fieldCopyConfig, final EventDataFormatHelper dataFormatHelper, final List<String> linksOfRulesToMonitor, final HubService blackDuckSerivce, final BlackDuckJiraLogger logger)
            throws ConfigurationException {
        this.jiraServices = jiraServices;
        this.jiraUserContext = jiraUserContext;
        this.jiraSettingsService = jiraSettingsService;
        this.blackDuckProjectMappings = blackDuckProjectMappings;
        this.fieldCopyConfig = fieldCopyConfig;
        this.dataFormatHelper = dataFormatHelper;
        this.linksOfRulesToMonitor = linksOfRulesToMonitor;
        this.blackDuckService = blackDuckSerivce;
        this.logger = logger;

        this.oldConverter = new NotificationToEventConverter(jiraServices, jiraUserContext, jiraSettingsService, blackDuckProjectMappings, fieldCopyConfig, dataFormatHelper, linksOfRulesToMonitor, blackDuckSerivce, logger);
    }

    public Collection<EventData> convertToEventData(final NotificationDetailResult detailResult, final HubBucket blackDuckBucket, final Date batchStartDate) {
        final NotificationType notificationType = detailResult.getType();
        logger.debug(String.format("%s Notification: %s", notificationType, detailResult.getNotificationContent()));

        final Set<EventData> allEvents = new HashSet<>();
        for (final NotificationContentDetail detail : detailResult.getNotificationContentDetails()) {
            try {
                final Collection<EventData> eventsFromDetail = createEventDataFromContentDetail(notificationType, detail, detailResult.getNotificationContent(), blackDuckBucket, batchStartDate);
                allEvents.addAll(eventsFromDetail);
            } catch (final Exception e) {
                logger.error(e);
                jiraSettingsService.addBlackDuckError(e.getMessage(), "convertToEventData");
            }
        }
        return allEvents;
    }

    private Collection<EventData> createEventDataFromContentDetail(final NotificationType notificationType, final NotificationContentDetail detail, final NotificationContent notificationContent,
            final HubBucket blackDuckBucket, final Date batchStartDate) throws IntegrationException {
        final List<EventData> eventDataList = new ArrayList<>();

        final ProjectVersionWrapper projectVersionWrapper = getProjectVersionWrapper(detail, blackDuckBucket);
        final String blackDuckProjectName = projectVersionWrapper.getProjectView().name;
        final List<JiraProject> jiraProjects = blackDuckProjectMappings.getJiraProjects(blackDuckProjectName);
        logger.debug(String.format("There are %d jira projects configured", jiraProjects));
        for (final JiraProject jiraProject : jiraProjects) {
            try {
                final Collection<EventData> createdEventData = createEventDataFromContentDetail(jiraProject, projectVersionWrapper, notificationType, detail, notificationContent, blackDuckBucket, batchStartDate);
                eventDataList.addAll(createdEventData);
            } catch (final Exception e) {
                logger.error(e);
                jiraSettingsService.addBlackDuckError(e, blackDuckProjectName, detail.getProjectVersionName().orElse("?"), jiraProject.getProjectName(), jiraUserContext.getJiraAdminUser().getName(),
                        jiraUserContext.getJiraIssueCreatorUser().getName(), "convertToEventData");
            }
        }
        return eventDataList;
    }

    private Collection<EventData> createEventDataFromContentDetail(final JiraProject jiraProject, final ProjectVersionWrapper projectVersionWrapper, final NotificationType notificationType, final NotificationContentDetail detail,
            final NotificationContent notificationContent, final HubBucket blackDuckBucket, final Date batchStartDate) throws IntegrationException, EventDataBuilderException, ConfigurationException {

        if (detail.getBomComponent().isPresent()) {
            final UriSingleResponse<VersionBomComponentView> bomComponentUriSingleResponse = detail.getBomComponent().get();
            VersionBomComponentView versionBomComponent;
            try {
                versionBomComponent = getBomComponent(bomComponentUriSingleResponse, blackDuckBucket);
            } catch (final IntegrationRestException restException) {
                if (restException.getHttpStatusCode() == 404) {
                    return create404EventData(jiraProject, notificationType, detail, bomComponentUriSingleResponse.uri, batchStartDate);
                }
                throw restException;
            }

            if (detail.isPolicy()) {
                final EventData eventData = createEventDataForPolicy(jiraProject, detail.getPolicy().get(), projectVersionWrapper, versionBomComponent, notificationType, blackDuckBucket, batchStartDate);
                if (eventData != null) {
                    return Arrays.asList(eventData);
                }
            } else if (detail.isVulnerability()) {
                final VulnerabilityNotificationContent vulnerabilityContent = (VulnerabilityNotificationContent) notificationContent;
                final EventData eventData = createEventDataForVulnerability(jiraProject, projectVersionWrapper, versionBomComponent,
                        vulnerabilityContent.newVulnerabilityIds, vulnerabilityContent.updatedVulnerabilityIds, vulnerabilityContent.deletedVulnerabilityIds, blackDuckBucket, batchStartDate);
                if (eventData != null) {
                    return Arrays.asList(eventData);
                }
            } else if (NotificationType.BOM_EDIT.equals(notificationType)) {
                return createEventDataForBomEdit(jiraProject, projectVersionWrapper, versionBomComponent, blackDuckBucket, batchStartDate);
            }
        } else {
            logger.warn("No bom component information provided by the notification: " + detail);
            logger.warn("Falling back to old converter...");
            final Optional<EventData> optionalEventData = oldConverter.createEventDataForJiraProject(notificationType, detail, notificationContent, jiraProject, blackDuckBucket, batchStartDate);
            if (optionalEventData.isPresent()) {
                return Arrays.asList(optionalEventData.get());
            }
        }
        logger.debug("Ignoring the following notification detail: " + detail);
        return Collections.emptyList();
    }

    // ===============================
    // NOTIFICATION EVENT DATA METHODS
    // ===============================

    private EventData createEventDataForPolicy(final JiraProject jiraProject, final UriSingleResponse<PolicyRuleViewV2> policyRuleUriSingleResponse, final ProjectVersionWrapper projectVersionWrapper,
            final VersionBomComponentView versionBomComponent, final NotificationType notificationType, final HubBucket blackDuckBucket, final Date batchStartDate)
            throws IntegrationException, EventDataBuilderException, ConfigurationException {

        if (!linksOfRulesToMonitor.contains(policyRuleUriSingleResponse.uri)) {
            return null;
        }
        final PolicyRuleViewV2 policyRule = getBlackDuckResponse(policyRuleUriSingleResponse, blackDuckBucket);

        final EventDataBuilder eventDataBuilder = createCommonEventDataBuilder(jiraProject, EventCategory.POLICY, batchStartDate);
        addCommonIssuePanelFields(eventDataBuilder, projectVersionWrapper, versionBomComponent, blackDuckBucket);
        eventDataBuilder.setNotificationType(notificationType);
        return createEventDataForPolicy(eventDataBuilder, policyRule, blackDuckBucket);
    }

    private EventData createEventDataForPolicy(final EventDataBuilder eventDataBuilder, final PolicyRuleViewV2 policyRule, final HubBucket blackDuckBucket)
            throws IntegrationException, EventDataBuilderException {
        eventDataBuilder.setBlackDuckRuleUrl(blackDuckService.getHref(policyRule));
        eventDataBuilder.setBlackDuckRuleName(policyRule.name);
        eventDataBuilder.setBlackDuckRuleDescription(policyRule.description);
        eventDataBuilder.setBlackDuckRuleOverridable(policyRule.overridable);
        // TODO eventDataBuilder.setBlackDuckRuleSeverity(policyRule.severity);

        final BlackDuckEventAction action = BlackDuckEventAction.fromPolicyNotificationType(eventDataBuilder.getNotificationType());
        eventDataBuilder.setAction(action);

        return addRemainingFieldsToEventDataAndBuild(eventDataBuilder, blackDuckBucket);
    }

    private EventData createEventDataForVulnerability(final JiraProject jiraProject, final ProjectVersionWrapper projectVersionWrapper, final VersionBomComponentView versionBomComponent,
            final List<VulnerabilitySourceQualifiedId> addedIds, final List<VulnerabilitySourceQualifiedId> updatedIds, final List<VulnerabilitySourceQualifiedId> deletedIds,
            final HubBucket blackDuckBucket, final Date batchStartDate) throws IntegrationException, EventDataBuilderException, ConfigurationException {

        final EventDataBuilder eventDataBuilder = createCommonEventDataBuilder(jiraProject, EventCategory.VULNERABILITY, batchStartDate);
        addCommonIssuePanelFields(eventDataBuilder, projectVersionWrapper, versionBomComponent, blackDuckBucket);

        final String comment = dataFormatHelper.generateVulnerabilitiesComment(addedIds, updatedIds, deletedIds);
        eventDataBuilder.setVulnerabilityIssueCommentProperties(comment);

        BlackDuckEventAction action = BlackDuckEventAction.ADD_COMMENT;
        if (!doesComponentVersionHaveVulnerabilities(versionBomComponent.securityRiskProfile)) {
            action = BlackDuckEventAction.RESOLVE;
        } else if (doesNotificationOnlyHaveDeletes(addedIds, updatedIds, deletedIds)) {
            action = BlackDuckEventAction.ADD_COMMENT_IF_EXISTS;
        }
        eventDataBuilder.setAction(action);

        return addRemainingFieldsToEventDataAndBuild(eventDataBuilder, blackDuckBucket);
    }

    // TODO add tests for this
    private Collection<EventData> createEventDataForBomEdit(final JiraProject jiraProject, final ProjectVersionWrapper projectVersionWrapper, final VersionBomComponentView versionBomComponent, final HubBucket blackDuckBucket,
            final Date batchStartDate) throws IntegrationException, EventDataBuilderException, ConfigurationException {

        final List<EventData> editEvents = new ArrayList<>();
        if (doesComponentVersionHaveVulnerabilities(versionBomComponent.securityRiskProfile)) {
            final EventData vulnerabilityEventData = createEventDataForVulnerability(
                    jiraProject, projectVersionWrapper, versionBomComponent, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), blackDuckBucket, batchStartDate);
            editEvents.add(vulnerabilityEventData);
        }

        final List<PolicyRuleViewV2> policyRules = blackDuckService.getAllResponses(versionBomComponent, VersionBomComponentView.POLICY_RULES_LINK_RESPONSE);
        if (!policyRules.isEmpty()) {
            for (final PolicyRuleViewV2 rule : policyRules) {
                final EventDataBuilder eventDataBuilder = createCommonEventDataBuilder(jiraProject, EventCategory.POLICY, batchStartDate);
                addCommonIssuePanelFields(eventDataBuilder, projectVersionWrapper, versionBomComponent, blackDuckBucket);
                eventDataBuilder.setNotificationType(NotificationType.BOM_EDIT);
                final EventData policyEventData = createEventDataForPolicy(eventDataBuilder, rule, blackDuckBucket);
                editEvents.add(policyEventData);
            }
        }
        return editEvents;
    }

    // TODO implement this
    private Collection<EventData> create404EventData(final JiraProject jiraProject, final NotificationType notificationType, final NotificationContentDetail detail, final String notFoundUri, final Date batchStartDate) {
        final SpecialEventData specialEventData = SpecialEventData.create404EventData(jiraProject, notFoundUri, batchStartDate);
        return Arrays.asList(specialEventData);
    }

    // ==========================
    // EVENT DATA BUILDER METHODS
    // ==========================

    private EventDataBuilder createCommonEventDataBuilder(final JiraProject jiraProject, final EventCategory eventCategory, final Date batchStartDate) throws IntegrationException, ConfigurationException {
        return new EventDataBuilder(eventCategory, batchStartDate, jiraProject, jiraUserContext, getIssueTypeId(eventCategory), blackDuckService.getHubBaseUrl(), fieldCopyConfig.getProjectFieldCopyMappings());
    }

    private void addCommonIssuePanelFields(final EventDataBuilder builder, final ProjectVersionWrapper projectVersionWrapper, final VersionBomComponentView versionBomComponent, final HubBucket blackDuckBucket) throws IntegrationException {
        addProjectSectionData(builder, projectVersionWrapper, blackDuckBucket);
        addComponentSectionData(builder, versionBomComponent);
    }

    private EventData addRemainingFieldsToEventDataAndBuild(final EventDataBuilder eventDataBuilder, final HubBucket blackDuckBucket) throws EventDataBuilderException {
        // TODO
        eventDataBuilder.setJiraIssueDescription(dataFormatHelper.getIssueDescription(eventDataBuilder, blackDuckBucket));

        return eventDataBuilder.build();
    }

    // ==============================
    // ISSUE PANEL EVENT DATA METHODS
    // ==============================

    private void addProjectSectionData(final EventDataBuilder eventDataBuilder, final ProjectVersionWrapper projectVersionWrapper, final HubBucket blackDuckBucket) throws IntegrationException {
        final ProjectView project = projectVersionWrapper.getProjectView();
        final ProjectVersionView projectVersion = projectVersionWrapper.getProjectVersionView();

        eventDataBuilder.setBlackDuckProjectName(project.name);
        eventDataBuilder.setBlackDuckProjectVersionName(projectVersion.versionName);
        eventDataBuilder.setBlackDuckProjectVersionLastUpdated(getBomLastUpdated(projectVersion, project.name));
        eventDataBuilder.setBlackDuckProjectVersionNickname(projectVersion.nickname);
        eventDataBuilder.setBlackDuckProjectOwner(getJiraProjectOwner(project.projectOwner, blackDuckBucket));
    }

    private void addComponentSectionData(final EventDataBuilder eventDataBuilder, final VersionBomComponentView versionBomComponent) {
        eventDataBuilder.setBlackDuckComponentName(versionBomComponent.componentName);
        eventDataBuilder.setBlackDuckComponentUrl(versionBomComponent.component);
        eventDataBuilder.setBlackDuckComponentVersionName(versionBomComponent.componentName);
        eventDataBuilder.setBlackDuckComponentVersionUrl(versionBomComponent.componentVersion);

        addComponentVersionOriginData(eventDataBuilder, versionBomComponent.origins);
        addLicenseData(eventDataBuilder, versionBomComponent.licenses);
        addUsageData(eventDataBuilder, versionBomComponent.usages);
    }

    private void addComponentVersionOriginData(final EventDataBuilder eventDataBuilder, final List<VersionBomOriginView> origins) {
        final String originsString = createCommaSeparatedString(origins, origin -> origin.name);
        eventDataBuilder.setBlackDuckComponentOrigins(originsString);

        final String originIdsString = createCommaSeparatedString(origins, origin -> origin.externalId);
        eventDataBuilder.setBlackDuckComponentOriginId(originIdsString);
    }

    private void addLicenseData(final EventDataBuilder eventDataBuilder, final List<VersionBomLicenseView> licenses) {
        final String licenseText = dataFormatHelper.getComponentLicensesStringPlainText(licenses);
        eventDataBuilder.setBlackDuckLicenseNames(licenseText);

        final String licenseTextUrl = dataFormatHelper.getLicenseTextLink(licenses, licenseText);
        eventDataBuilder.setBlackDuckLicenseUrl(licenseTextUrl);
    }

    private void addUsageData(final EventDataBuilder eventDataBuilder, final List<MatchedFileUsagesType> usages) {
        final String usagesString = createCommaSeparatedString(usages, usage -> usage.prettyPrint());
        eventDataBuilder.setBlackDuckComponentUsages(usagesString);
    }

    private <T> String createCommaSeparatedString(final List<T> list, final Function<T, String> reductionFunction) {
        if (list != null && !list.isEmpty()) {
            return list.stream().map(reductionFunction).collect(Collectors.joining(", "));
        }
        return null;
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
        // TODO Fix this when Black Duck supports going back to the project-version
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

    private boolean doesComponentVersionHaveVulnerabilities(final RiskProfileView securityRiskProfile) {
        logger.debug("Checking if the component still has vulnerabilities...");
        final int vulnerablitiesCount = getSumOfCounts(securityRiskProfile.counts);
        logger.debug("Number of vulnerabilities found: " + vulnerablitiesCount);
        if (vulnerablitiesCount > 0) {
            logger.debug("This component still has vulnerabilities");
            return true;
        }
        return false;
    }

    private int getSumOfCounts(final List<RiskCountView> vulnerabilityCounts) {
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

    private String getBomLastUpdated(final ProjectVersionView projectVersion, final String projectName) {
        try {
            final VersionRiskProfileView riskProfile = blackDuckService.getResponse(projectVersion, ProjectVersionView.RISKPROFILE_LINK_RESPONSE);
            final SimpleDateFormat dateFormat = new SimpleDateFormat();
            return dateFormat.format(riskProfile.bomLastUpdatedAt);
        } catch (final IntegrationException intException) {
            logger.debug(String.format("Could not find the risk profile: %s", intException.getMessage()));
        } catch (final NullPointerException npe) {
            logger.debug(String.format("The risk profile for %s / %s was null.", projectName, projectVersion.versionName));
            logger.trace("Caught NPE in getBomLastUpdated()", npe);
        }
        return "";
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
