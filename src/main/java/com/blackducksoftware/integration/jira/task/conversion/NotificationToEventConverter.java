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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.atlassian.jira.issue.issuetype.IssueType;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.enumeration.ComplexLicenseType;
import com.blackducksoftware.integration.hub.api.generated.enumeration.MatchedFileUsagesType;
import com.blackducksoftware.integration.hub.api.generated.response.VersionRiskProfileView;
import com.blackducksoftware.integration.hub.api.generated.view.ComplexLicenseView;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentView;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectView;
import com.blackducksoftware.integration.hub.api.generated.view.VersionBomComponentView;
import com.blackducksoftware.integration.hub.api.view.CommonNotificationState;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.notification.content.NotificationContentDetail;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.hub.service.bucket.HubBucket;
import com.blackducksoftware.integration.hub.throwaway.NotificationEvent;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.common.HubProjectMappings;
import com.blackducksoftware.integration.jira.common.JiraContext;
import com.blackducksoftware.integration.jira.common.JiraProject;
import com.blackducksoftware.integration.jira.common.exception.ConfigurationException;
import com.blackducksoftware.integration.jira.config.HubJiraFieldCopyConfigSerializable;
import com.blackducksoftware.integration.jira.task.JiraSettingsService;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventDataBuilder;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;

public abstract class NotificationToEventConverter {
    private final Set<NotificationEvent> cache;
    private final HubJiraLogger logger;
    private final JiraServices jiraServices;
    private final JiraContext jiraContext;
    private final JiraSettingsService jiraSettingsService;
    private final HubProjectMappings mappings;
    private final String issueTypeId;
    private final HubJiraFieldCopyConfigSerializable fieldCopyConfig;
    private final HubService hubService;

    public NotificationToEventConverter(final Set<NotificationEvent> cache, final JiraServices jiraServices, final JiraContext jiraContext, final JiraSettingsService jiraSettingsService, final HubProjectMappings mappings,
            final String issueTypeName, final HubJiraFieldCopyConfigSerializable fieldCopyConfig, final HubService hubSerivce, final HubJiraLogger logger) throws ConfigurationException {
        this.cache = cache;
        this.jiraServices = jiraServices;
        this.jiraContext = jiraContext;
        this.jiraSettingsService = jiraSettingsService;
        this.mappings = mappings;
        this.issueTypeId = lookUpIssueTypeId(issueTypeName);
        this.fieldCopyConfig = fieldCopyConfig;
        this.hubService = hubSerivce;
        this.logger = logger;
    }

    public abstract void process(final CommonNotificationState commonNotificationState) throws HubIntegrationException;

    public Set<NotificationEvent> getCache() {
        return cache;
    }

    public JiraSettingsService getJiraSettingsService() {
        return jiraSettingsService;
    }

    public HubProjectMappings getMappings() {
        return mappings;
    }

    protected JiraProject getJiraProject(final long jiraProjectId) throws HubIntegrationException {
        return jiraServices.getJiraProject(jiraProjectId);
    }

    protected JiraContext getJiraContext() {
        return jiraContext;
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

    protected String getIssueTypeId() {
        return issueTypeId;
    }

    protected HubJiraFieldCopyConfigSerializable getFieldCopyConfig() {
        return fieldCopyConfig;
    }

    protected String getComponentLicensesStringPlainText(final ComponentVersionView componentVersion) throws IntegrationException {
        return getComponentLicensesString(componentVersion, false);
    }

    protected String getComponentLicensesStringWithLinksAtlassianFormat(final ComponentVersionView componentVersion) throws IntegrationException {
        return getComponentLicensesString(componentVersion, true);
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
        } catch (final HubIntegrationException e) {
            logger.error(e);
            return null;
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
        return null;
    }

    protected String getProjectVersionNickname(final NotificationContentDetail detail, final HubBucket hubBucket) throws HubIntegrationException {
        if (detail.getProjectVersion().isPresent()) {
            final ProjectVersionView projectVersion = hubBucket.get(detail.getProjectVersion().get());
            return projectVersion.nickname;
        }
        return "";
    }

    protected void populateEventDataBuilder(final EventDataBuilder eventDataBuilder, final NotificationContentDetail detail, final HubBucket hubBucket) {
        if (detail.getProjectVersion().isPresent()) {
            final ProjectVersionView projectVersion = hubBucket.get(detail.getProjectVersion().get());
            try {
                final VersionRiskProfileView riskProfile = hubService.getResponse(projectVersion, ProjectVersionView.RISKPROFILE_LINK_RESPONSE);
                eventDataBuilder.setHubProjectVersionLastUpdated(riskProfile.bomLastUpdatedAt);
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

    private String getComponentLicensesString(final ComponentVersionView componentVersion, final boolean includeLinks) throws IntegrationException {
        String licensesString = "";
        if ((componentVersion != null) && (componentVersion.license != null) && (componentVersion.license.licenses != null)) {
            final ComplexLicenseType type = componentVersion.license.type;
            final StringBuilder sb = new StringBuilder();

            if (type != null) {
                final String licenseJoinString = (type == ComplexLicenseType.CONJUNCTIVE) ? HubJiraConstants.LICENSE_NAME_JOINER_AND : HubJiraConstants.LICENSE_NAME_JOINER_OR;
                int licenseIndex = 0;
                for (final ComplexLicenseView license : componentVersion.license.licenses) {
                    if (licenseIndex++ > 0) {
                        sb.append(licenseJoinString);
                    }
                    createLicenseString(sb, license, includeLinks);
                }

            } else {
                createLicenseString(sb, componentVersion.license, includeLinks);
            }
            licensesString = sb.toString();
        }
        return licensesString;
    }

    private void createLicenseString(final StringBuilder sb, final ComplexLicenseView license, final boolean includeLinks) throws IntegrationException {
        final String licenseTextUrl = getLicenseTextUrl(license);
        logger.debug("Link to licence text: " + licenseTextUrl);

        if (includeLinks) {
            sb.append("[");
        }
        sb.append(license.name);
        if (includeLinks) {
            sb.append("|");
            sb.append(licenseTextUrl);
            sb.append("]");
        }
    }

    private String getLicenseTextUrl(final ComplexLicenseView license) throws IntegrationException {
        final String licenseUrl = license.license;
        final ComplexLicenseView fullLicense = hubService.getResponse(licenseUrl, ComplexLicenseView.class);
        final String licenseTextUrl = hubService.getFirstLink(fullLicense, "text");
        return licenseTextUrl;
    }

}
