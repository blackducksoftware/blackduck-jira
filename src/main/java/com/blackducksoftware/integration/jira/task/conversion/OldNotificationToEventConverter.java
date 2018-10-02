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
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

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
import com.synopsys.integration.blackduck.api.UriSingleResponse;
import com.synopsys.integration.blackduck.api.core.LinkSingleResponse;
import com.synopsys.integration.blackduck.api.generated.component.RiskCountView;
import com.synopsys.integration.blackduck.api.generated.component.VersionBomLicenseView;
import com.synopsys.integration.blackduck.api.generated.enumeration.MatchedFileUsagesType;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.generated.enumeration.RiskCountType;
import com.synopsys.integration.blackduck.api.generated.response.VersionRiskProfileView;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ComponentView;
import com.synopsys.integration.blackduck.api.generated.view.LicenseView;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleViewV2;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomComponentView;
import com.synopsys.integration.blackduck.exception.HubIntegrationException;
import com.synopsys.integration.blackduck.notification.NotificationDetailResult;
import com.synopsys.integration.blackduck.notification.content.NotificationContent;
import com.synopsys.integration.blackduck.notification.content.VulnerabilityNotificationContent;
import com.synopsys.integration.blackduck.notification.content.detail.NotificationContentDetail;
import com.synopsys.integration.blackduck.service.HubService;
import com.synopsys.integration.blackduck.service.bucket.HubBucket;
import com.synopsys.integration.exception.IntegrationException;

public class OldNotificationToEventConverter {
    private final BlackDuckJiraLogger logger;
    private final JiraServices jiraServices;
    private final JiraUserContext jiraUserContext;
    private final JiraSettingsService jiraSettingsService;
    private final BlackDuckProjectMappings blackDuckProjectMappings;
    private final BlackDuckJiraFieldCopyConfigSerializable fieldCopyConfig;
    private final EventDataFormatHelper dataFormatHelper;
    private final List<String> linksOfRulesToMonitor;
    private final HubService blackDuckService;

    public OldNotificationToEventConverter(final JiraServices jiraServices, final JiraUserContext jiraUserContext, final JiraSettingsService jiraSettingsService, final BlackDuckProjectMappings blackDuckProjectMappings,
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
    }

    public Collection<EventData> createEventDataForNotificationDetailResult(final NotificationDetailResult detailResult, final HubBucket blackDuckBucket, final Date batchStartDate) throws HubIntegrationException {
        logger.debug("Using Old Notification Converter");
        final NotificationType notificationType = detailResult.getType();
        logger.debug(String.format("%s Notification: %s", notificationType, detailResult.getNotificationContent()));

        final Set<EventData> allEvents = new HashSet<>();
        for (final NotificationContentDetail detail : detailResult.getNotificationContentDetails()) {
            if (shouldHandle(detail) && detail.getProjectName().isPresent()) {
                final String projectName = detail.getProjectName().get();
                final List<EventData> projectEvents = createEventDataForBlackDuckProjectMappings(projectName, notificationType, detail, detailResult.getNotificationContent(), blackDuckBucket, batchStartDate);
                allEvents.addAll(projectEvents);
            } else {
                logger.debug(String.format("Ignoring the following notification detail: %s", detail));
            }
        }
        return allEvents;
    }

    public List<EventData> createEventDataForBlackDuckProjectMappings(final String blackDuckProjectName, final NotificationType notificationType, final NotificationContentDetail detail, final NotificationContent notificationContent,
        final HubBucket blackDuckBucket, final Date batchStartDate) {
        logger.debug("Getting JIRA project(s) mapped to Black Duck project: " + blackDuckProjectName);
        final List<JiraProject> mappingJiraProjects = blackDuckProjectMappings.getJiraProjects(blackDuckProjectName);
        logger.debug("There are " + mappingJiraProjects.size() + " JIRA projects mapped to this Black Duck project : " + blackDuckProjectName);

        final List<EventData> eventDataList = new ArrayList<>();
        for (final JiraProject jiraProject : mappingJiraProjects) {
            logger.debug("JIRA Project: " + jiraProject);
            try {
                final Optional<EventData> jiraProjectEventData = createEventDataForJiraProject(notificationType, detail, notificationContent, jiraProject, blackDuckBucket, batchStartDate);
                if (jiraProjectEventData.isPresent()) {
                    eventDataList.add(jiraProjectEventData.get());
                }
            } catch (final Exception e) {
                logger.error(e);
                jiraSettingsService.addBlackDuckError(e, blackDuckProjectName, detail.getProjectVersionName().orElse("?"), jiraProject.getProjectName(), jiraUserContext.getJiraAdminUser().getName(),
                    jiraUserContext.getJiraIssueCreatorUser().getName(), "transitionIssue");
            }
        }
        return eventDataList;
    }

    public Optional<EventData> createEventDataForJiraProject(final NotificationType notificationType, final NotificationContentDetail detail, final NotificationContent notificationContent, final JiraProject jiraProject,
        final HubBucket blackDuckBucket, final Date batchStartDate) throws EventDataBuilderException, IntegrationException, ConfigurationException {
        BlackDuckEventAction action = BlackDuckEventAction.OPEN;
        final EventCategory eventCategory = EventCategory.fromNotificationType(notificationType);
        final EventDataBuilder eventDataBuilder = new EventDataBuilder(eventCategory);
        eventDataBuilder.setLastBatchStartDate(batchStartDate);

        final VersionBomComponentView versionBomComponent = getBomComponent(detail, blackDuckBucket);
        if (versionBomComponent != null) {
            eventDataBuilder.setBlackDuckBomComponentUri(blackDuckService.getHref(versionBomComponent));
        }

        Optional<PolicyRuleViewV2> optionalPolicyRule = Optional.empty();
        if (detail.isPolicy()) {
            final UriSingleResponse<PolicyRuleViewV2> policyRuleLink = detail.getPolicy().get();
            optionalPolicyRule = Optional.ofNullable(blackDuckBucket.get(policyRuleLink));
            if (optionalPolicyRule.isPresent()) {
                eventDataBuilder.setBlackDuckRuleName(optionalPolicyRule.get().name);
                eventDataBuilder.setBlackDuckRuleOverridable(optionalPolicyRule.get().overridable);
                eventDataBuilder.setBlackDuckRuleDescription(optionalPolicyRule.get().description);
                eventDataBuilder.setBlackDuckRuleUrl(policyRuleLink.uri);
            }
            action = BlackDuckEventAction.fromNotificationType(notificationType);
        } else if (detail.isVulnerability()) {
            final VulnerabilityNotificationContent vulnerabilityContent = (VulnerabilityNotificationContent) notificationContent;
            final String comment = dataFormatHelper.generateVulnerabilitiesComment(vulnerabilityContent);
            eventDataBuilder.setVulnerabilityIssueCommentProperties(comment);

            action = BlackDuckEventAction.ADD_COMMENT;
            if (!doesComponentVersionHaveVulnerabilities(vulnerabilityContent, versionBomComponent)) {
                action = BlackDuckEventAction.RESOLVE;
            } else if (doesNotificationOnlyHaveDeletes(vulnerabilityContent)) {
                action = BlackDuckEventAction.ADD_COMMENT_IF_EXISTS;
            }
        }

        eventDataBuilder.setPropertiesFromJiraUserContext(jiraUserContext);
        eventDataBuilder.setPropertiesFromJiraProject(jiraProject);
        eventDataBuilder.setPropertiesFromNotificationContentDetail(detail);
        eventDataBuilder.setBlackDuckProjectVersionNickname(getProjectVersionNickname(detail.getProjectVersion(), blackDuckBucket));
        eventDataBuilder.setJiraFieldCopyMappings(fieldCopyConfig.getProjectFieldCopyMappings());

        final String licenseText = getLicenseText(detail, versionBomComponent, blackDuckBucket);
        eventDataBuilder.setBlackDuckLicenseNames(licenseText);
        eventDataBuilder.setBlackDuckLicenseUrl(getLicenseTextLink(versionBomComponent, licenseText));
        eventDataBuilder.setBlackDuckComponentUsages(getComponentUsage(versionBomComponent));
        eventDataBuilder.setBlackDuckBaseUrl(blackDuckService.getHubBaseUrl().toString());
        eventDataBuilder.setBlackDuckProjectVersionLastUpdated(getBomLastUpdated(detail, blackDuckBucket));
        eventDataBuilder.setBlackDuckProjectOwner(getJiraProjectOwner(jiraServices.getUserSearchService(), detail.getProjectVersion(), blackDuckBucket));

        eventDataBuilder.setAction(action);
        eventDataBuilder.setNotificationType(notificationType);

        eventDataBuilder.setJiraIssueTypeId(getIssueTypeId(eventCategory));
        eventDataBuilder
            .setJiraIssueDescription(dataFormatHelper.getIssueDescription(eventDataBuilder.getEventCategory(), eventDataBuilder.getBlackDuckProjectVersionUrl(), eventDataBuilder.getBlackDuckComponentVersionUrl(), blackDuckBucket));

        final EventData eventData = eventDataBuilder.build();
        logger.debug("Event key: " + eventData.getEventKey());
        return Optional.of(eventData);
    }

    private boolean shouldHandle(final NotificationContentDetail detail) {
        if (detail.isPolicy() && detail.getPolicy().isPresent()) {
            final String linkOfRule = detail.getPolicy().get().uri;
            return linksOfRulesToMonitor.contains(linkOfRule);
        }
        // TicketGenerator has already determined that vulnerability notifications are "on".
        return detail.isVulnerability();
    }

    private boolean doesComponentVersionHaveVulnerabilities(final VulnerabilityNotificationContent vulnerabilityContent, final VersionBomComponentView versionBomComponent) {
        logger.debug("Checking if the component still has vulnerabilities...");
        if (CollectionUtils.isEmpty(vulnerabilityContent.deletedVulnerabilityIds) && CollectionUtils.isEmpty(vulnerabilityContent.updatedVulnerabilityIds)) {
            logger.debug("Since no vulnerabilities were deleted or changed, the component must still have vulnerabilities");
            return true;
        }

        if (versionBomComponent != null) {
            final int vulnerablitiesCount = getSumOfCounts(versionBomComponent.securityRiskProfile.counts);
            logger.debug("Number of vulnerabilities found: " + vulnerablitiesCount);
            if (vulnerablitiesCount > 0) {
                logger.debug("This component still has vulnerabilities");
                return true;
            }
        }
        logger.debug("This component either no longer has vulnerabilities, or is no longer in the BOM");
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

    private boolean doesNotificationOnlyHaveDeletes(final VulnerabilityNotificationContent vulnerabilityContent) {
        return vulnerabilityContent.deletedVulnerabilityCount > 0 && vulnerabilityContent.newVulnerabilityCount == 0 && vulnerabilityContent.updatedVulnerabilityCount == 0;
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
            if (issueType == null) {
                continue;
            }
            if (issueType.getName().equals(targetIssueTypeName)) {
                return issueType.getId();
            }
        }
        throw new ConfigurationException("IssueType " + targetIssueTypeName + " not found");
    }

    private String getComponentUsage(final VersionBomComponentView bomComp) throws HubIntegrationException {
        final StringBuilder usagesText = new StringBuilder();
        if (bomComp != null) {
            int usagesIndex = 0;
            for (final MatchedFileUsagesType usage : bomComp.usages) {
                if (usagesIndex > 0) {
                    usagesText.append(", ");
                }
                usagesText.append(usage.toString());
                usagesIndex++;
            }
        }
        return usagesText.toString();
    }

    private String getProjectVersionNickname(final Optional<UriSingleResponse<ProjectVersionView>> optionalProjectVersion, final HubBucket blackDuckBucket) throws HubIntegrationException {
        if (optionalProjectVersion.isPresent()) {
            try {
                final ProjectVersionView projectVersion = blackDuckBucket.get(optionalProjectVersion.get());
                return projectVersion.nickname;
            } catch (final NullPointerException npe) {
                logger.debug("Caught NPE in getProjectVersionNickname()", npe);
            }
        }
        return "";
    }

    private ApplicationUser getJiraProjectOwner(final UserSearchService userSearchService, final Optional<UriSingleResponse<ProjectVersionView>> projectVersionOptional, final HubBucket blackDuckBucket) {
        if (projectVersionOptional.isPresent()) {
            try {
                final ProjectVersionView projectVersion = blackDuckBucket.get(projectVersionOptional.get());
                final ProjectView project = blackDuckService.getResponse(projectVersion, ProjectVersionView.PROJECT_LINK_RESPONSE);
                if (project.projectOwner != null) {
                    final UserView projectOwner = blackDuckService.getResponse(project.projectOwner, UserView.class);
                    if (projectOwner != null) {
                        for (final ApplicationUser jiraUser : userSearchService.findUsersByEmail(projectOwner.email)) {
                            // We will assume that if users are configured correctly, they will have unique email addresses.
                            return jiraUser;
                        }
                    }
                }
            } catch (final Exception e) {
                logger.warn("Unable to get the project owner for this notification: " + e.getMessage());
            }
        }
        return null;
    }

    private String getBomLastUpdated(final NotificationContentDetail detail, final HubBucket blackDuckBucket) {
        if (detail.getProjectVersion().isPresent()) {
            final ProjectVersionView projectVersion = blackDuckBucket.get(detail.getProjectVersion().get());
            try {
                final VersionRiskProfileView riskProfile = blackDuckService.getResponse(projectVersion, ProjectVersionView.RISKPROFILE_LINK_RESPONSE);
                final SimpleDateFormat dateFormat = new SimpleDateFormat();
                return dateFormat.format(riskProfile.bomLastUpdatedAt);
            } catch (final IntegrationException intException) {
                logger.debug(String.format("Could not find the risk profile: %s", intException.getMessage()));
            } catch (final NullPointerException npe) {
                logger.debug(String.format("The risk profile for %s / %s was null.", detail.getProjectName().orElse("?"), detail.getProjectVersionName().orElse("?")));
                logger.trace("Caught NPE in getBomLastUpdated()", npe);
            }
        }
        return "";
    }

    private VersionBomComponentView getBomComponent(final NotificationContentDetail detail, final HubBucket blackDuckBucket) throws HubIntegrationException {
        VersionBomComponentView targetBomComp = null;
        if (!detail.getProjectVersion().isPresent()) {
            logger.debug("No project version uri was available from the current notification detail.");
            return null;
        }
        List<VersionBomComponentView> bomComps;
        final String projectName = detail.getProjectName().orElse("?");
        final String projectVersionName = detail.getProjectVersionName().orElse("?");
        final String componentName = detail.getComponentName().orElse("?");
        final String componentVersionName = detail.getComponentVersionName().orElse("?");
        try {
            final ProjectVersionView projectVersion = blackDuckBucket.get(detail.getProjectVersion().get());
            bomComps = blackDuckService.getAllResponses(projectVersion, ProjectVersionView.COMPONENTS_LINK_RESPONSE);
        } catch (final IntegrationException intException) {
            logger.debug(String.format("Error getting BOM for project %s / %s; perhaps the BOM is now empty.", projectName, projectVersionName));
            return null;
        } catch (final NullPointerException npe) {
            logger.debug(String.format("The Black Duck resource (%s / %s, %s / %s) sought could not be found; perhaps it was deleted.", projectName, projectVersionName, componentName, componentVersionName));
            logger.trace("Caught NPE in getBomComponent()", npe);
            return null;
        }
        ComponentView notificationComponent = null;
        ComponentVersionView notificationComponentVersion = null;
        if (detail.getComponent().isPresent()) {
            notificationComponent = blackDuckBucket.get(detail.getComponent().get());
        }
        if (detail.getComponentVersion().isPresent()) {
            notificationComponentVersion = blackDuckBucket.get(detail.getComponentVersion().get());
        }
        targetBomComp = findCompInBom(bomComps, notificationComponent, notificationComponentVersion);
        if (targetBomComp == null) {
            logger.info(String.format("Component %s not found in BOM", componentName));
            logger.debug(String.format("Component %s / %s not found in the BOM for project %s / %s", componentName, componentVersionName, projectName, projectVersionName));
        }
        return targetBomComp;
    }

    protected final VersionBomComponentView findCompInBom(final List<VersionBomComponentView> bomComps, final ComponentView actualComp, final ComponentVersionView actualCompVer) {
        String urlSought;
        try {
            if (actualCompVer != null) {
                urlSought = blackDuckService.getHref(actualCompVer);
            } else {
                urlSought = blackDuckService.getHref(actualComp);
            }
            for (final VersionBomComponentView bomComp : bomComps) {
                String urlToTest;
                if (bomComp.componentVersion != null) {
                    urlToTest = bomComp.componentVersion;
                } else {
                    urlToTest = bomComp.component;
                }
                if (urlSought.equals(urlToTest)) {
                    return bomComp;
                }
            }
        } catch (final HubIntegrationException e) {
            logger.error(e);
        }
        return null;
    }

    private String getLicenseText(final NotificationContentDetail detail, final VersionBomComponentView versionBomComponent, final HubBucket blackDuckBucket) throws IntegrationException {
        String licensesString = "";
        if (versionBomComponent != null) {
            licensesString = dataFormatHelper.getComponentLicensesStringPlainText(versionBomComponent.licenses);
            logger.debug("Component " + versionBomComponent.componentName + " (version: " + versionBomComponent.componentVersionName + "): License: " + licensesString);
        } else if (detail.getComponentVersion().isPresent()) {
            final ComponentVersionView componentVersion = blackDuckBucket.get(detail.getComponentVersion().get());
            licensesString = dataFormatHelper.getComponentLicensesStringPlainText(componentVersion);
            logger.debug("Component " + detail.getComponentName().orElse("?") + " (version: " + detail.getComponentVersionName().orElse("?") + "): License: " + licensesString);
        }
        return licensesString;
    }

    private String getLicenseTextLink(final VersionBomComponentView versionBomComponent, final String licenseName) {
        if (versionBomComponent != null && CollectionUtils.isNotEmpty(versionBomComponent.licenses)) {
            VersionBomLicenseView versionBomLicense = null;
            for (final VersionBomLicenseView license : versionBomComponent.licenses) {
                if (licenseName.equals(license.licenseDisplay)) {
                    versionBomLicense = license;
                }
            }
            if (versionBomLicense == null) {
                versionBomLicense = versionBomComponent.licenses.get(0);
            }
            try {
                final LicenseView genericLicense = blackDuckService.getResponse(versionBomLicense.license, LicenseView.class);
                final LicenseView kbLicense = blackDuckService.getResponse(genericLicense, new LinkSingleResponse<>("license", LicenseView.class));
                return blackDuckService.getFirstLink(kbLicense, LicenseView.TEXT_LINK);
            } catch (final Exception e) {
                logger.debug("Unable to get the BOM component license text.");
            }
        }
        return "";
    }

}
