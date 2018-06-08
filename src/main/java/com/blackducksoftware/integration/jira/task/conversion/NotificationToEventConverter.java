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
package com.blackducksoftware.integration.jira.task.conversion;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import com.atlassian.jira.issue.issuetype.IssueType;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.UriSingleResponse;
import com.blackducksoftware.integration.hub.api.generated.component.RiskCountView;
import com.blackducksoftware.integration.hub.api.generated.enumeration.MatchedFileUsagesType;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.hub.api.generated.enumeration.RiskCountType;
import com.blackducksoftware.integration.hub.api.generated.response.VersionRiskProfileView;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentView;
import com.blackducksoftware.integration.hub.api.generated.view.PolicyRuleViewV2;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectView;
import com.blackducksoftware.integration.hub.api.generated.view.VersionBomComponentView;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.notification.NotificationDetailResult;
import com.blackducksoftware.integration.hub.notification.content.NotificationContent;
import com.blackducksoftware.integration.hub.notification.content.VulnerabilityNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.detail.NotificationContentDetail;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.hub.service.bucket.HubBucket;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.common.HubProjectMappings;
import com.blackducksoftware.integration.jira.common.HubUrlParser;
import com.blackducksoftware.integration.jira.common.JiraContext;
import com.blackducksoftware.integration.jira.common.JiraProject;
import com.blackducksoftware.integration.jira.common.exception.ConfigurationException;
import com.blackducksoftware.integration.jira.common.exception.EventDataBuilderException;
import com.blackducksoftware.integration.jira.config.HubJiraFieldCopyConfigSerializable;
import com.blackducksoftware.integration.jira.task.JiraSettingsService;
import com.blackducksoftware.integration.jira.task.conversion.output.HubEventAction;
import com.blackducksoftware.integration.jira.task.conversion.output.IssuePropertiesGenerator;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventCategory;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventData;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventDataBuilder;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventDataFormatHelper;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;

public class NotificationToEventConverter {
    private final HubJiraLogger logger;
    private final JiraServices jiraServices;
    private final JiraContext jiraContext;
    private final JiraSettingsService jiraSettingsService;
    private final HubProjectMappings hubProjectMappings;
    private final HubJiraFieldCopyConfigSerializable fieldCopyConfig;
    private final EventDataFormatHelper dataFormatHelper;
    private final List<String> linksOfRulesToMonitor;
    private final HubService hubService;

    public NotificationToEventConverter(final JiraServices jiraServices, final JiraContext jiraContext, final JiraSettingsService jiraSettingsService, final HubProjectMappings mappings,
            final HubJiraFieldCopyConfigSerializable fieldCopyConfig, final EventDataFormatHelper dataFormatHelper, final List<String> linksOfRulesToMonitor, final HubService hubSerivce, final HubJiraLogger logger)
            throws ConfigurationException {
        this.jiraServices = jiraServices;
        this.jiraContext = jiraContext;
        this.jiraSettingsService = jiraSettingsService;
        this.hubProjectMappings = mappings;
        this.fieldCopyConfig = fieldCopyConfig;
        this.dataFormatHelper = dataFormatHelper;
        this.linksOfRulesToMonitor = linksOfRulesToMonitor;
        this.hubService = hubSerivce;
        this.logger = logger;
    }

    public Collection<EventData> createEventDataForNotificationDetailResult(final NotificationDetailResult detailResult, final HubBucket hubBucket) throws HubIntegrationException {
        final NotificationType notificationType = detailResult.getType();
        logger.debug(String.format("%s Notification: %s", notificationType, detailResult));

        final Set<EventData> allEvents = new HashSet<>();
        for (final NotificationContentDetail detail : detailResult.getNotificationContentDetails()) {
            if (shouldHandle(detail) && detail.getProjectName().isPresent()) {
                final String projectName = detail.getProjectName().get();
                final List<EventData> projectEvents = createEventDataForHubProjectMappings(projectName, notificationType, detail, detailResult.getNotificationContent(), hubBucket);
                allEvents.addAll(projectEvents);
            } else {
                logger.debug(String.format("Ignoring the following notification detail: %s", detail));
            }
        }
        return allEvents;
    }

    private List<EventData> createEventDataForHubProjectMappings(final String hubProjectName, final NotificationType notificationType, final NotificationContentDetail detail, final NotificationContent notificationContent,
            final HubBucket hubBucket) {
        logger.debug("Getting JIRA project(s) mapped to Hub project: " + hubProjectName);
        final List<JiraProject> mappingJiraProjects = hubProjectMappings.getJiraProjects(hubProjectName);
        logger.debug("There are " + mappingJiraProjects.size() + " JIRA projects mapped to this Hub project : " + hubProjectName);

        final List<EventData> eventDataList = new ArrayList<>();
        for (final JiraProject jiraProject : mappingJiraProjects) {
            logger.debug("JIRA Project: " + jiraProject);
            try {
                final Optional<EventData> jiraProjectEventData = createEventDataForJiraProject(notificationType, detail, notificationContent, jiraProject, hubBucket);
                if (jiraProjectEventData.isPresent()) {
                    eventDataList.add(jiraProjectEventData.get());
                }
            } catch (final Exception e) {
                logger.error(e);
                jiraSettingsService.addHubError(e, hubProjectName, detail.getProjectVersionName().orElse("?"), jiraProject.getProjectName(), jiraContext.getJiraAdminUser().getName(),
                        jiraContext.getJiraIssueCreatorUser().getName(), "transitionIssue");
            }
        }
        return eventDataList;
    }

    private Optional<EventData> createEventDataForJiraProject(final NotificationType notificationType, final NotificationContentDetail detail, final NotificationContent notificationContent, final JiraProject jiraProject,
            final HubBucket hubBucket)
            throws EventDataBuilderException, IntegrationException, ConfigurationException {
        HubEventAction action = HubEventAction.OPEN;
        final EventCategory eventCategory = EventCategory.fromNotificationType(notificationType);
        final EventDataBuilder eventDataBuilder = new EventDataBuilder(eventCategory);
        final VersionBomComponentView versionBomComponent = getBomComponent(detail, hubBucket);

        Optional<PolicyRuleViewV2> optionalPolicyRule = Optional.empty();
        if (detail.isPolicy()) {
            final UriSingleResponse<PolicyRuleViewV2> policyRuleLink = detail.getPolicy().get();
            optionalPolicyRule = Optional.ofNullable(hubBucket.get(policyRuleLink));
            if (optionalPolicyRule.isPresent()) {
                eventDataBuilder.setHubRuleName(optionalPolicyRule.get().name);
                eventDataBuilder.setHubRuleUrl(policyRuleLink.uri);
            }
            eventDataBuilder.setPolicyIssueCommentPropertiesFromNotificationType(notificationType);

            action = HubEventAction.fromPolicyNotificationType(notificationType);
        } else {
            final VulnerabilityNotificationContent vulnerabilityContent = (VulnerabilityNotificationContent) notificationContent;
            final String comment = dataFormatHelper.generateVulnerabilitiesComment(vulnerabilityContent);
            eventDataBuilder.setVulnerabilityIssueCommentProperties(comment);

            action = HubEventAction.ADD_COMMENT;
            if (!doesComponentVersionHaveVulnerabilities(vulnerabilityContent, versionBomComponent)) {
                action = HubEventAction.RESOLVE;
            } else if (doesNotificationOnlyHaveDeletes(vulnerabilityContent)) {
                action = HubEventAction.ADD_COMMENT_IF_EXISTS;
            }
        }

        eventDataBuilder.setPropertiesFromJiraContext(jiraContext);
        eventDataBuilder.setPropertiesFromJiraProject(jiraProject);
        eventDataBuilder.setPropertiesFromNotificationContentDetail(detail);
        eventDataBuilder.setHubProjectVersionNickname(getProjectVersionNickname(detail, hubBucket));
        eventDataBuilder.setJiraFieldCopyMappings(fieldCopyConfig.getProjectFieldCopyMappings());

        eventDataBuilder.setJiraIssuePropertiesGenerator(new IssuePropertiesGenerator(detail, optionalPolicyRule));
        eventDataBuilder.setJiraIssueSummary(dataFormatHelper.getIssueSummary(detail, optionalPolicyRule));
        eventDataBuilder.setJiraIssueDescription(dataFormatHelper.getIssueDescription(detail, optionalPolicyRule, hubBucket));
        eventDataBuilder.setJiraIssueTypeId(getIssueTypeId(eventCategory));

        eventDataBuilder.setHubLicenseNames(getLicenseText(detail, versionBomComponent, hubBucket));
        eventDataBuilder.setHubComponentUsage(getComponentUsage(versionBomComponent));

        eventDataBuilder.setAction(action);
        eventDataBuilder.setNotificationType(notificationType);
        eventDataBuilder.setEventKey(generateEventKey(eventDataBuilder));

        addExtraneousHubInfoToEventDataBuilder(eventDataBuilder, detail, hubBucket);

        return Optional.of(eventDataBuilder.build());
    }

    // "We can't mess with this" -ekerwin
    public String generateEventKey(final EventDataBuilder eventDataBuilder) throws HubIntegrationException {
        final Long jiraProjectId = eventDataBuilder.getJiraProjectId();
        final String hubProjectVersionUrl = eventDataBuilder.getHubProjectVersionUrl();
        final String hubComponentVersionUrl = eventDataBuilder.getHubComponentVersionUrl();
        final String hubComponentUrl = eventDataBuilder.getHubComponentUrl();
        final StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_ISSUE_TYPE_NAME);
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_SEPARATOR);
        if (EventCategory.POLICY.equals(eventDataBuilder.getEventCategory())) {
            keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_ISSUE_TYPE_VALUE_POLICY);
        } else {
            keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_ISSUE_TYPE_VALUE_VULNERABILITY);
        }
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_PAIR_SEPARATOR);

        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_JIRA_PROJECT_ID_NAME);
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_SEPARATOR);
        keyBuilder.append(jiraProjectId.toString());
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_PAIR_SEPARATOR);

        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_HUB_PROJECT_VERSION_REL_URL_HASHED_NAME);
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_SEPARATOR);
        keyBuilder.append(hashString(HubUrlParser.getRelativeUrl(hubProjectVersionUrl)));
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_PAIR_SEPARATOR);

        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_HUB_COMPONENT_REL_URL_HASHED_NAME);
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_SEPARATOR);
        if (EventCategory.POLICY.equals(eventDataBuilder.getEventCategory())) {
            keyBuilder.append(hashString(HubUrlParser.getRelativeUrl(hubComponentUrl)));
        } else {
            keyBuilder.append(""); // Vulnerabilities do not have a component URL
        }
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_PAIR_SEPARATOR);

        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_HUB_COMPONENT_VERSION_REL_URL_HASHED_NAME);
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_SEPARATOR);
        keyBuilder.append(hashString(HubUrlParser.getRelativeUrl(hubComponentVersionUrl)));

        if (EventCategory.POLICY.equals(eventDataBuilder.getEventCategory())) {
            final String policyRuleUrl = eventDataBuilder.getHubRuleUrl();
            if (policyRuleUrl == null) {
                throw new HubIntegrationException("Policy Rule URL is null");
            }
            keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_PAIR_SEPARATOR);
            keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_HUB_POLICY_RULE_REL_URL_HASHED_NAME);
            keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_SEPARATOR);
            keyBuilder.append(hashString(HubUrlParser.getRelativeUrl(policyRuleUrl)));
        }

        final String key = keyBuilder.toString();
        logger.debug("property key: " + key);
        return key;
    }

    public String hashString(final String origString) {
        String hashString;
        if (origString == null) {
            hashString = "";
        } else {
            hashString = String.valueOf(origString.hashCode());
        }
        return hashString;
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
        if (CollectionUtils.isEmpty(vulnerabilityContent.deletedVulnerabilityIds) && CollectionUtils.isEmpty(vulnerabilityContent.updatedVulnerabilityIds)) {
            logger.debug("Since no vulnerabilities were deleted or changed, the component must still have vulnerabilities");
            return true;
        }

        int vulnerablitiesCount = 0;
        if (versionBomComponent != null) {
            vulnerablitiesCount = getSumOfCounts(versionBomComponent.securityRiskProfile.counts);
        }

        logger.debug("Number of vulnerabilities found: " + vulnerablitiesCount);
        if (vulnerablitiesCount > 0) {
            logger.debug("This component still has vulnerabilities");
            return true;
        } else {
            logger.debug("This component either no longer has vulnerabilities, or is no longer in the BOM");
            return false;
        }
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
        String issueType = HubJiraConstants.HUB_POLICY_VIOLATION_ISSUE;
        if (EventCategory.VULNERABILITY.equals(category)) {
            issueType = HubJiraConstants.HUB_VULNERABILITY_ISSUE;
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

    private String getProjectVersionNickname(final NotificationContentDetail detail, final HubBucket hubBucket) throws HubIntegrationException {
        if (detail.getProjectVersion().isPresent()) {
            final ProjectVersionView projectVersion = hubBucket.get(detail.getProjectVersion().get());
            return projectVersion.nickname;
        }
        return "";
    }

    private void addExtraneousHubInfoToEventDataBuilder(final EventDataBuilder eventDataBuilder, final NotificationContentDetail detail, final HubBucket hubBucket) {
        if (detail.getProjectVersion().isPresent()) {
            final ProjectVersionView projectVersion = hubBucket.get(detail.getProjectVersion().get());
            try {
                final VersionRiskProfileView riskProfile = hubService.getResponse(projectVersion, ProjectVersionView.RISKPROFILE_LINK_RESPONSE);
                final SimpleDateFormat dateFormat = new SimpleDateFormat();
                eventDataBuilder.setHubProjectVersionLastUpdated(dateFormat.format(riskProfile.bomLastUpdatedAt));
            } catch (final IntegrationException e) {
                logger.error(String.format("Could not find the risk profile for %s: %s", ProjectVersionView.RISKPROFILE_LINK_RESPONSE, e.getMessage()));
            }
            try {
                final ProjectView project = hubService.getResponse(projectVersion, ProjectVersionView.PROJECT_LINK_RESPONSE);
                eventDataBuilder.setHubProjectOwner(project.projectOwner);
            } catch (final IntegrationException e) {
                logger.error(String.format("Could not find the project for %s: %s", ProjectVersionView.PROJECT_LINK_RESPONSE, e.getMessage()));
            }
        }
    }

    private VersionBomComponentView getBomComponent(final NotificationContentDetail detail, final HubBucket hubBucket) throws HubIntegrationException {
        VersionBomComponentView targetBomComp = null;
        if (detail.getProjectVersion().isPresent()) {
            List<VersionBomComponentView> bomComps;
            final ProjectVersionView projectVersion = hubBucket.get(detail.getProjectVersion().get());
            try {
                bomComps = hubService.getAllResponses(projectVersion, ProjectVersionView.COMPONENTS_LINK_RESPONSE);
            } catch (final IntegrationException e) {
                logger.debug(String.format("Error getting BOM for project %s / %s; Perhaps the BOM is now empty", detail.getProjectName(), detail.getProjectVersionName()));
                return null;
            }
            ComponentView notificationComponent = null;
            ComponentVersionView notificationComponentVersion = null;
            if (detail.getComponent().isPresent()) {
                notificationComponent = hubBucket.get(detail.getComponent().get());
            }
            if (detail.getComponentVersion().isPresent()) {
                notificationComponentVersion = hubBucket.get(detail.getComponentVersion().get());
            }
            targetBomComp = findCompInBom(bomComps, notificationComponent, notificationComponentVersion);
            if (targetBomComp == null) {
                final String componentName = detail.getComponentName().orElse("<unknown component>");
                final String componentVersionName = detail.getComponentVersionName().orElse("<unknown component version>");
                logger.info(String.format("Component %s not found in BOM", componentName));
                logger.debug(String.format("Component %s / %s not found in the BOM for project %s / %s", componentName, componentVersionName, detail.getProjectName().orElse("?"), detail.getProjectVersionName().orElse("?")));
            }
        }
        return targetBomComp;
    }

    protected final VersionBomComponentView findCompInBom(final List<VersionBomComponentView> bomComps, final ComponentView actualComp, final ComponentVersionView actualCompVer) {
        String urlSought;
        try {
            if (actualCompVer != null) {
                urlSought = hubService.getHref(actualCompVer);
            } else {
                urlSought = hubService.getHref(actualComp);
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

    private String getLicenseText(final NotificationContentDetail detail, final VersionBomComponentView versionBomComponent, final HubBucket hubBucket) throws IntegrationException {
        String licensesString = "";
        if (versionBomComponent != null) {
            licensesString = dataFormatHelper.getComponentLicensesStringPlainText(versionBomComponent);
            logger.debug("Component " + versionBomComponent.componentName + " (version: " + versionBomComponent.componentVersionName + "): License: " + licensesString);
        } else if (detail.getComponentVersion().isPresent()) {
            final ComponentVersionView componentVersion = hubBucket.get(detail.getComponentVersion().get());
            licensesString = dataFormatHelper.getComponentLicensesStringPlainText(componentVersion);
            logger.debug("Component " + detail.getComponentName().orElse("?") + " (version: " + detail.getComponentVersionName().orElse("?") + "): License: " + licensesString);
        }
        return licensesString;
    }

}
