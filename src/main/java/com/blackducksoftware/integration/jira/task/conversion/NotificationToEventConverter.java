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
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.atlassian.jira.issue.issuetype.IssueType;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.UriSingleResponse;
import com.blackducksoftware.integration.hub.api.generated.enumeration.MatchedFileUsagesType;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.hub.api.generated.response.VersionRiskProfileView;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentView;
import com.blackducksoftware.integration.hub.api.generated.view.PolicyRuleViewV2;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectView;
import com.blackducksoftware.integration.hub.api.generated.view.VersionBomComponentView;
import com.blackducksoftware.integration.hub.api.generated.view.VulnerableComponentView;
import com.blackducksoftware.integration.hub.api.view.CommonNotificationState;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.notification.content.NotificationContent;
import com.blackducksoftware.integration.hub.notification.content.NotificationContentDetail;
import com.blackducksoftware.integration.hub.notification.content.VulnerabilityNotificationContent;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.hub.service.bucket.HubBucket;
import com.blackducksoftware.integration.hub.service.model.ProjectVersionDescription;
import com.blackducksoftware.integration.hub.service.model.RequestFactory;
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
import com.blackducksoftware.integration.jira.task.conversion.output.PolicyIssuePropertiesGenerator;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventCategory;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventData;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventDataBuilder;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventDataFormatHelper;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;
import com.blackducksoftware.integration.rest.RestConstants;
import com.blackducksoftware.integration.rest.request.Request;

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

    public Collection<EventData> convert(final CommonNotificationState commonNotificationState, final HubBucket hubBucket) throws HubIntegrationException {
        final NotificationType notificationType = commonNotificationState.getType();
        final NotificationContent notificationContent = commonNotificationState.getContent();
        logger.debug(String.format("%s Notification: %s", notificationType, notificationContent));

        final Set<EventData> allEvents = new HashSet<>();
        for (final ProjectVersionDescription projectVersionDescription : notificationContent.getAffectedProjectVersionDescriptions()) {
            final String projectName = projectVersionDescription.getProjectName();
            logger.debug("Getting JIRA project(s) mapped to Hub project: " + projectName);
            final List<JiraProject> mappingJiraProjects = hubProjectMappings.getJiraProjects(projectName);
            logger.debug("There are " + mappingJiraProjects.size() + " JIRA projects mapped to this Hub project : " + projectName);

            if (!mappingJiraProjects.isEmpty()) {
                for (final JiraProject jiraProject : mappingJiraProjects) {
                    logger.debug("JIRA Project: " + jiraProject);
                    try {
                        final List<EventData> projectEvents = handleNotificationPerJiraProject(notificationType, notificationContent, jiraProject, hubBucket);
                        allEvents.addAll(projectEvents);
                    } catch (final Exception e) {
                        logger.error(e);
                        jiraSettingsService.addHubError(e, projectName, projectVersionDescription.getProjectVersionName(), jiraProject.getProjectName(), jiraContext.getJiraAdminUser().getName(),
                                jiraContext.getJiraIssueCreatorUser().getName(), "transitionIssue");
                    }

                }
            }
        }
        return allEvents;
    }

    private List<EventData> handleNotificationPerJiraProject(final NotificationType notificationType, final NotificationContent notificationContent, final JiraProject jiraProject, final HubBucket hubBucket)
            throws EventDataBuilderException, IntegrationException, ConfigurationException {
        final List<EventData> events = new ArrayList<>();
        for (final NotificationContentDetail detail : notificationContent.getNotificationContentDetails()) {
            if (doWeCareAboutThisNotification(detail)) {
                HubEventAction action = HubEventAction.OPEN;
                final EventCategory eventCategory = EventCategory.fromNotificationType(notificationType);
                final EventDataBuilder eventDataBuilder = new EventDataBuilder(eventCategory);

                if (detail.isPolicy()) {
                    final UriSingleResponse<PolicyRuleViewV2> policyRuleLink = detail.getPolicy().get();
                    final PolicyRuleViewV2 rule = hubBucket.get(policyRuleLink);
                    eventDataBuilder.setHubRuleName(rule.name);
                    eventDataBuilder.setHubRuleUrl(policyRuleLink.uri);

                    final IssuePropertiesGenerator issuePropertiesGenerator = new PolicyIssuePropertiesGenerator(detail, rule.name);
                    eventDataBuilder.setJiraIssuePropertiesGenerator(issuePropertiesGenerator);
                    eventDataBuilder.setJiraIssueSummary(dataFormatHelper.getIssueSummary(detail, rule));
                    eventDataBuilder.setJiraIssueDescription(dataFormatHelper.getIssueDescription(detail, rule, hubBucket));
                    eventDataBuilder.setPolicyIssueCommentPropertiesFromNotificationType(notificationType);

                    action = HubEventAction.fromPolicyNotificationType(notificationType);
                } else {
                    final VulnerabilityNotificationContent vulnerabilityContent = (VulnerabilityNotificationContent) notificationContent;
                    final String comment = dataFormatHelper.generateVulnerabilitiesComment(vulnerabilityContent);
                    eventDataBuilder.setVulnerabilityIssueCommentProperties(comment);

                    action = HubEventAction.ADD_COMMENT;
                    if (!doesComponentVersionHaveVulnerabilities(vulnerabilityContent, detail, hubBucket)) {
                        action = HubEventAction.RESOLVE;
                    } else if (doesNotificationOnlyHaveDeletes(vulnerabilityContent)) {
                        action = HubEventAction.ADD_COMMENT_IF_EXISTS;
                    }
                }

                String licensesString;
                ComponentVersionView componentVersion = null;
                if (detail.getComponentVersion().isPresent()) {
                    componentVersion = hubBucket.get(detail.getComponentVersion().get());
                    licensesString = dataFormatHelper.getComponentLicensesStringPlainText(componentVersion);
                    // if the component is not null, we shouldn't have to worry about these
                    final String componentName = detail.getComponentName().orElse("");
                    final String componentVersionName = detail.getComponentVersionName().orElse("");
                    logger.debug("Component " + componentName + " (version: " + componentVersionName + "): License: " + licensesString);
                } else {
                    licensesString = "";
                }
                eventDataBuilder.setPropertiesFromJiraContext(jiraContext);
                eventDataBuilder.setPropertiesFromJiraProject(jiraProject);
                eventDataBuilder.setPropertiesFromNotificationContentDetail(detail);

                eventDataBuilder.setHubLicenseNames(licensesString);
                eventDataBuilder.setHubComponentUsage(getComponentUsage(detail, hubBucket));
                eventDataBuilder.setHubProjectVersionNickname(getProjectVersionNickname(detail, hubBucket));

                eventDataBuilder.setJiraIssueTypeId(getIssueTypeId(eventCategory));
                eventDataBuilder.setJiraFieldCopyMappings(fieldCopyConfig.getProjectFieldCopyMappings());

                addExtraneousHubInfoToEventDataBuilder(eventDataBuilder, detail, hubBucket);

                eventDataBuilder.setAction(action);
                eventDataBuilder.setNotificationType(notificationType);
                eventDataBuilder.setEventKey(generateEventKey(eventDataBuilder));

                events.add(eventDataBuilder.build());
            } else {
                logger.debug(String.format("Ignoring the following notification detail: %s", detail));
            }
        }
        return events;
    }

    // "We can't mess with this" -ekerwin
    public String generateEventKey(final EventDataBuilder eventDataBuilder) throws HubIntegrationException {
        final Long jiraProjectId = eventDataBuilder.getJiraProjectId();
        final String hubProjectVersionUrl = eventDataBuilder.getHubProjectVersionUrl();
        final String hubComponentVersionUrl = eventDataBuilder.getHubComponentVersionUrl();
        final String hubComponentUrl = eventDataBuilder.getHubComponentUrl();
        final String policyRuleUrl = eventDataBuilder.getHubRuleUrl();
        if (policyRuleUrl == null) {
            throw new HubIntegrationException("Policy Rule URL is null");
        }

        final StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_ISSUE_TYPE_NAME);
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_SEPARATOR);
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_ISSUE_TYPE_VALUE_POLICY);
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
        keyBuilder.append(hashString(HubUrlParser.getRelativeUrl(hubComponentUrl)));
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_PAIR_SEPARATOR);

        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_HUB_COMPONENT_VERSION_REL_URL_HASHED_NAME);
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_SEPARATOR);
        keyBuilder.append(hashString(HubUrlParser.getRelativeUrl(hubComponentVersionUrl)));
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_PAIR_SEPARATOR);

        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_HUB_POLICY_RULE_REL_URL_HASHED_NAME);
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_SEPARATOR);
        keyBuilder.append(hashString(HubUrlParser.getRelativeUrl(policyRuleUrl)));

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

    private boolean doWeCareAboutThisNotification(final NotificationContentDetail detail) {
        if (detail.isPolicy() && detail.getPolicy().isPresent()) {
            final String linkOfRule = detail.getPolicy().get().uri;
            return linksOfRulesToMonitor.contains(linkOfRule);
        }
        return false;
    }

    private boolean doesComponentVersionHaveVulnerabilities(final VulnerabilityNotificationContent vulnerabilityContent, final NotificationContentDetail detail, final HubBucket hubBucket) {
        if (CollectionUtils.isEmpty(vulnerabilityContent.deletedVulnerabilityIds)) {
            logger.debug("Since no vulnerabilities were deleted, the component must still have vulnerabilities");
            return true;
        }

        List<VulnerableComponentView> vulnerableBomComponentItems;
        try {
            final ProjectVersionView projectVersion = hubBucket.get(detail.getProjectVersion().orElse(null));
            final Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder();
            requestBuilder.addQueryParameter("q", detail.getComponentName().orElse(""));

            vulnerableBomComponentItems = hubService.getAllResponses(projectVersion, ProjectVersionView.VULNERABLE_COMPONENTS_LINK_RESPONSE, requestBuilder);
        } catch (final IntegrationException intException) {
            final String msg = String.format("Error getting vulnerable components. Unable to determine if this component still has vulnerabilities. The error was: %s", intException.getMessage());
            logger.error(msg);
            jiraSettingsService.addHubError(msg, "getVulnerableComponentsMatchingComponentName");
            return true;
        }

        logger.debug("vulnerableBomComponentItems.size(): " + vulnerableBomComponentItems.size());
        if (hasVersion(vulnerableBomComponentItems, detail.getComponentVersionName().orElse(""))) {
            logger.debug("This component still has vulnerabilities");
            return true;
        } else {
            logger.debug("This component either no longer has vulnerabilities, or is no longer in the BOM");
            return false;
        }
    }

    private boolean hasVersion(final List<VulnerableComponentView> vulnerableBomComponentItems, final String targetVersionName) {
        for (final VulnerableComponentView vulnerableBomComponentItem : vulnerableBomComponentItems) {
            final String currentVersionName = vulnerableBomComponentItem.componentVersionName;
            if (!StringUtils.isEmpty(currentVersionName) && currentVersionName.equals(targetVersionName)) {
                return true;
            }
        }
        return false;
    }

    private boolean doesNotificationOnlyHaveDeletes(final VulnerabilityNotificationContent vulnerabilityContent) {
        return vulnerabilityContent.deletedVulnerabilityCount > 0 && vulnerabilityContent.newVulnerabilityCount == 0 && vulnerabilityContent.updatedVulnerabilityCount == 0;
    }

    protected JiraProject getJiraProject(final long jiraProjectId) throws HubIntegrationException {
        return jiraServices.getJiraProject(jiraProjectId);
    }

    protected String getIssueTypeId(final EventCategory category) throws ConfigurationException {
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

    protected String getComponentUsage(final NotificationContentDetail detail, final HubBucket hubBucket) throws HubIntegrationException {
        final VersionBomComponentView bomComp = getBomComponent(detail, hubBucket);
        if (bomComp == null) {
            logger.info(String.format("Unable to find component %s in BOM, so cannot get usage information", detail.getComponentName()));
            return "";
        }
        final StringBuilder usagesText = new StringBuilder();
        int usagesIndex = 0;
        for (final MatchedFileUsagesType usage : bomComp.usages) {
            if (usagesIndex > 0) {
                usagesText.append(", ");
            }
            usagesText.append(usage.toString());
            usagesIndex++;
        }
        return usagesText.toString();
    }

    VersionBomComponentView findCompInBom(final List<VersionBomComponentView> bomComps, final ComponentView actualComp, final ComponentVersionView actualCompVer) {
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

    protected String getProjectVersionNickname(final NotificationContentDetail detail, final HubBucket hubBucket) throws HubIntegrationException {
        if (detail.getProjectVersion().isPresent()) {
            final ProjectVersionView projectVersion = hubBucket.get(detail.getProjectVersion().get());
            return projectVersion.nickname;
        }
        return "";
    }

    protected void addExtraneousHubInfoToEventDataBuilder(final EventDataBuilder eventDataBuilder, final NotificationContentDetail detail, final HubBucket hubBucket) {
        if (detail.getProjectVersion().isPresent()) {
            final ProjectVersionView projectVersion = hubBucket.get(detail.getProjectVersion().get());
            try {
                final VersionRiskProfileView riskProfile = hubService.getResponse(projectVersion, ProjectVersionView.RISKPROFILE_LINK_RESPONSE);
                final SimpleDateFormat dateFormat = new SimpleDateFormat(RestConstants.JSON_DATE_FORMAT);
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
        if (detail.getProjectVersion().isPresent() && detail.getComponent().isPresent()) {
            List<VersionBomComponentView> bomComps;
            final ProjectVersionView projectVersion = hubBucket.get(detail.getProjectVersion().get());
            try {
                bomComps = hubService.getAllResponses(projectVersion, ProjectVersionView.COMPONENTS_LINK_RESPONSE);
            } catch (final IntegrationException e) {
                logger.debug(String.format("Error getting BOM for project %s / %s; Perhaps the BOM is now empty", detail.getProjectName(), detail.getProjectVersionName()));
                return null;
            }
            final ComponentView notificationComponent = hubBucket.get(detail.getComponent().get());
            ComponentVersionView notificationComponentVersion = null;
            if (detail.getComponentVersion().isPresent()) {
                notificationComponentVersion = hubBucket.get(detail.getComponentVersion().get());
            }
            targetBomComp = findCompInBom(bomComps, notificationComponent, notificationComponentVersion);
            if (targetBomComp == null) {
                logger.info(String.format("Component %s not found in BOM", notificationComponent.name));
                final String componentVersionName = detail.getComponentVersionName().orElse("<unknown component version>");
                logger.debug(String.format("Component %s / %s not found in the BOM for project %s / %s", notificationComponent.name, componentVersionName, detail.getProjectName(), detail.getProjectVersionName()));
            }
        }
        return targetBomComp;
    }

}
