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
import java.util.Map;

import com.atlassian.jira.issue.issuetype.IssueType;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.enumeration.ComplexLicenseType;
import com.blackducksoftware.integration.hub.api.generated.enumeration.MatchedFileUsagesType;
import com.blackducksoftware.integration.hub.api.generated.view.ComplexLicenseView;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.UserView;
import com.blackducksoftware.integration.hub.api.generated.view.VersionBomComponentView;
import com.blackducksoftware.integration.hub.api.view.MetaHandler;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.notification.NotificationContentItem;
import com.blackducksoftware.integration.hub.notification.NotificationSubProcessor;
import com.blackducksoftware.integration.hub.notification.ProjectVersionModel;
import com.blackducksoftware.integration.hub.notification.SubProcessorCache;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.common.HubProjectMappings;
import com.blackducksoftware.integration.jira.common.JiraContext;
import com.blackducksoftware.integration.jira.common.JiraProject;
import com.blackducksoftware.integration.jira.common.exception.ConfigurationException;
import com.blackducksoftware.integration.jira.config.HubJiraFieldCopyConfigSerializable;
import com.blackducksoftware.integration.jira.hub.ProjectResponse;
import com.blackducksoftware.integration.jira.hub.VersionRiskProfileResponse;
import com.blackducksoftware.integration.jira.task.JiraSettingsService;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventDataBuilder;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;

public abstract class NotificationToEventConverter extends NotificationSubProcessor {
    private final HubJiraLogger logger;
    private final JiraServices jiraServices;
    private final JiraContext jiraContext;
    private final JiraSettingsService jiraSettingsService;
    private final HubProjectMappings mappings;
    private final String issueTypeId;
    private final HubJiraFieldCopyConfigSerializable fieldCopyConfig;
    private final HubServicesFactory hubServicesFactory;

    public NotificationToEventConverter(final SubProcessorCache cache, final JiraServices jiraServices, final JiraContext jiraContext, final JiraSettingsService jiraSettingsService, final HubProjectMappings mappings,
            final String issueTypeName, final HubJiraFieldCopyConfigSerializable fieldCopyConfig, final HubServicesFactory hubServicesFactory, final HubJiraLogger logger) throws ConfigurationException {
        super(cache, new MetaHandler(logger));
        this.jiraServices = jiraServices;
        this.jiraContext = jiraContext;
        this.jiraSettingsService = jiraSettingsService;
        this.mappings = mappings;
        this.issueTypeId = lookUpIssueTypeId(issueTypeName);
        this.fieldCopyConfig = fieldCopyConfig;
        this.hubServicesFactory = hubServicesFactory;
        this.logger = logger;
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

    @Deprecated
    @Override
    public Map<String, Object> generateDataSet(final Map<String, Object> inputData) {
        throw new UnsupportedOperationException("generateDataSet() method is not supported");
    }

    protected HubJiraFieldCopyConfigSerializable getFieldCopyConfig() {
        return fieldCopyConfig;
    }

    protected HubServicesFactory getHubServicesFactory() {
        return hubServicesFactory;
    }

    protected String getComponentLicensesStringPlainText(final NotificationContentItem notification) throws IntegrationException {
        return getComponentLicensesString(notification, false);
    }

    protected String getComponentLicensesStringWithLinksAtlassianFormat(final NotificationContentItem notification) throws IntegrationException {
        return getComponentLicensesString(notification, true);
    }

    protected abstract VersionBomComponentView getBomComponent(final NotificationContentItem notification) throws HubIntegrationException;

    protected String getComponentUsage(final NotificationContentItem notification, final VersionBomComponentView bomComp) throws HubIntegrationException {
        if (bomComp == null) {
            logger.info(String.format("Unable to find component %s in BOM, so cannot get usage information", notification.getComponentName()));
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

    protected String getComponentOrigin(final NotificationContentItem notification) throws HubIntegrationException {
        // Planned for hub-jira 3.3
        return "";
    }

    protected String getComponentOriginId(final NotificationContentItem notification) throws HubIntegrationException {
        // Planned for hub-jira 3.3
        return "";
    }

    protected VersionBomComponentView getBomComponent(final ProjectVersionModel projectVersion, final String componentName, final String componentUrl, final ComponentVersionView componentVersion) throws HubIntegrationException {
        String componentVersionUrl = null;
        if (componentVersion != null) {
            componentVersionUrl = getMetaHandler().getHref(componentVersion);
        }
        final String bomUrl = projectVersion.getComponentsLink();
        if (bomUrl == null) {
            logger.debug(String.format("The BOM url for project %s / %s is null, indicating that the BOM is now empty", projectVersion.getProjectName(), projectVersion.getProjectVersionName()));
            return null;
        }
        List<VersionBomComponentView> bomComps;
        try {
            bomComps = hubServicesFactory.createHubService().getAllResponses(bomUrl, VersionBomComponentView.class);
        } catch (final Exception e) {
            logger.debug(String.format("Error getting BOM for project %s / %s; Perhaps the BOM is now empty", projectVersion.getProjectName(), projectVersion.getProjectVersionName()));
            return null;
        }

        final VersionBomComponentView targetBomComp = findCompInBom(bomComps, componentUrl, componentVersionUrl);
        if (targetBomComp == null) {
            logger.info(String.format("Component %s not found in BOM", componentName));
            String componentVersionName = "<none>";
            if (componentVersion != null) {
                componentVersionName = componentVersion.versionName;
            }
            logger.debug(String.format("Component %s / %s not found in the BOM for project %s / %s", componentName, componentVersionName, projectVersion.getProjectName(), projectVersion.getProjectVersionName()));
        }
        return targetBomComp;
    }

    VersionBomComponentView findCompInBom(final List<VersionBomComponentView> bomComps, final String componentUrl, final String componentVersionUrl) {
        String urlSought;
        if (componentVersionUrl != null) {
            urlSought = componentVersionUrl;
        } else {
            urlSought = componentUrl;
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

    protected String getProjectVersionNickname(final NotificationContentItem notification) throws HubIntegrationException {
        return notification.getProjectVersion().getNickname();
    }

    protected void populateEventDataBuilder(final EventDataBuilder eventDataBuilder, final NotificationContentItem notificationContentItem) {
        final ProjectVersionModel projectVersionModel = notificationContentItem.getProjectVersion();
        final String riskProfileUri = projectVersionModel.getRiskProfileLink();
        final String projectUri = notificationContentItem.getProjectVersion().getProjectLink();
        try {
            final VersionRiskProfileResponse riskProfile = hubServicesFactory.createHubService().getResponse(riskProfileUri, VersionRiskProfileResponse.class);
            eventDataBuilder.setHubProjectVersionLastUpdated(riskProfile.bomLastUpdatedAt);
        } catch (final IntegrationException e) {
            logger.error(String.format("Could not find the risk profile for %s: %s", riskProfileUri, e.getMessage()));
        }
        try {
            final ProjectResponse projectResponse = hubServicesFactory.createHubService().getResponse(projectUri, ProjectResponse.class);
            final String userUri = projectResponse.projectOwner;
            final UserView userView = hubServicesFactory.createHubService().getResponse(userUri, UserView.class);
            eventDataBuilder.setHubProjectOwner(userView.firstName + " " + userView.lastName);
        } catch (final IntegrationException e) {
            logger.error(String.format("Could not find the project for %s: %s", projectUri, e.getMessage()));
        }
    }

    private String getComponentLicensesString(final NotificationContentItem notification, final boolean includeLinks) throws IntegrationException {
        final ComponentVersionView componentVersion = notification.getComponentVersion();
        String licensesString = "";
        if ((componentVersion != null) && (componentVersion.license != null) && (componentVersion.license.licenses != null)) {
            final ComplexLicenseType type = componentVersion.license.type;
            final StringBuilder sb = new StringBuilder();

            if (type != null) {

                final String licenseJoinString = (type == ComplexLicenseType.CONJUNCTIVE) ? HubJiraConstants.LICENSE_NAME_JOINER_AND
                        : HubJiraConstants.LICENSE_NAME_JOINER_OR;
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
        final ComplexLicenseView fullLicense = hubServicesFactory.createHubService().getResponse(licenseUrl, ComplexLicenseView.class);
        final String licenseTextUrl = getMetaHandler().getFirstLink(fullLicense, "text");
        return licenseTextUrl;
    }

}
