/**
 * Hub JIRA Plugin
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
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
import com.blackducksoftware.integration.hub.api.bom.BomRequestService;
import com.blackducksoftware.integration.hub.api.component.version.ComplexLicenseItem;
import com.blackducksoftware.integration.hub.api.component.version.ComplexLicenseType;
import com.blackducksoftware.integration.hub.api.component.version.ComponentVersion;
import com.blackducksoftware.integration.hub.api.view.UsageEnum;
import com.blackducksoftware.integration.hub.api.view.VersionBomComponentView;
import com.blackducksoftware.integration.hub.dataservice.model.ProjectVersion;
import com.blackducksoftware.integration.hub.dataservice.notification.model.NotificationContentItem;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.notification.processor.NotificationSubProcessor;
import com.blackducksoftware.integration.hub.notification.processor.SubProcessorCache;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.common.HubProjectMappings;
import com.blackducksoftware.integration.jira.common.JiraContext;
import com.blackducksoftware.integration.jira.common.JiraProject;
import com.blackducksoftware.integration.jira.common.exception.ConfigurationException;
import com.blackducksoftware.integration.jira.config.HubJiraFieldCopyConfigSerializable;
import com.blackducksoftware.integration.jira.task.JiraSettingsService;
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

    private final BomRequestService bomRequestService;

    public NotificationToEventConverter(final SubProcessorCache cache, final JiraServices jiraServices, final JiraContext jiraContext,
            final JiraSettingsService jiraSettingsService,
            final HubProjectMappings mappings,
            final String issueTypeName,
            final HubJiraFieldCopyConfigSerializable fieldCopyConfig,
            final HubServicesFactory hubServicesFactory,
            final HubJiraLogger logger) throws ConfigurationException {
        super(cache, hubServicesFactory.createMetaService(logger));
        this.jiraServices = jiraServices;
        this.jiraContext = jiraContext;
        this.jiraSettingsService = jiraSettingsService;
        this.mappings = mappings;
        this.issueTypeId = lookUpIssueTypeId(issueTypeName);
        this.fieldCopyConfig = fieldCopyConfig;
        this.hubServicesFactory = hubServicesFactory;
        this.bomRequestService = hubServicesFactory.createBomRequestService();
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

    protected String getComponentLicensesStringPlainText(final NotificationContentItem notification) throws HubIntegrationException {
        return getComponentLicensesString(notification, false);
    }

    protected String getComponentLicensesStringWithLinksAtlassianFormat(final NotificationContentItem notification) throws HubIntegrationException {
        return getComponentLicensesString(notification, true);
    }

    protected String getComponentUsage(final NotificationContentItem notification) throws HubIntegrationException {
        final VersionBomComponentView bomComp = getBomComponent(notification.getProjectVersion(),
                notification.getComponentName(), notification.getComponentVersion());
        if (bomComp == null) {
            logger.info(String.format("Unable to find component %s in BOM, so cannot get usage information",
                    notification.getComponentName()));
            return "<unknown>";
        }
        final StringBuilder usagesText = new StringBuilder();
        int usagesIndex = 0;
        for (final UsageEnum usage : bomComp.getUsages()) {
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

    private VersionBomComponentView getBomComponent(final ProjectVersion projectVersion,
            final String componentName, final ComponentVersion componentVersion) throws HubIntegrationException {
        final String componentVersionUrl = getMetaService().getHref(componentVersion);
        // TODO pass additional arg componentUrl into this method.
        // TODO if componentVersion is null, don't try to get its URL
        // TODO but get/use (look in bom for) componentUrl instead
        final String bomUrl = projectVersion.getComponentsLink();
        if (bomUrl == null) {
            logger.debug(String.format("The BOM url for project %s / %s is null, indicating that the BOM is now empty",
                    projectVersion.getProjectName(), projectVersion.getProjectVersionName()));
            return null;
        }
        List<VersionBomComponentView> bomComps;
        try {
            bomComps = bomRequestService.getBom(bomUrl);
        } catch (final Exception e) {
            logger.debug(String.format("Error getting BOM for project %s / %s; Perhaps the BOM is now empty",
                    projectVersion.getProjectName(), projectVersion.getProjectVersionName()));
            return null;
        }
        for (final VersionBomComponentView bomComp : bomComps) {
            if (componentVersionUrl.equals(bomComp.getComponentVersion())) {
                return bomComp;
            }
        }
        logger.debug(String.format("Component %s / %s not found in the BOM for project %s / %s",
                componentName, componentVersion.getVersionName(),
                projectVersion.getProjectName(), projectVersion.getProjectVersionName()));
        return null;
    }

    protected String getProjectVersionNickname(final NotificationContentItem notification) throws HubIntegrationException {
        return notification.getProjectVersion().getNickname();
    }

    private String getComponentLicensesString(final NotificationContentItem notification, final boolean includeLinks) throws HubIntegrationException {
        final ComponentVersion componentVersion = notification.getComponentVersion();
        String licensesString = "";
        if ((componentVersion != null) && (componentVersion.getLicense() != null) && (componentVersion.getLicense().getLicenses() != null)) {
            final ComplexLicenseType type = componentVersion.getLicense().getType();
            final String licenseJoinString = (type == ComplexLicenseType.CONJUNCTIVE) ? HubJiraConstants.LICENSE_NAME_JOINER_AND
                    : HubJiraConstants.LICENSE_NAME_JOINER_OR;
            int licenseIndex = 0;
            final StringBuilder sb = new StringBuilder();
            for (final ComplexLicenseItem license : componentVersion.getLicense().getLicenses()) {
                final String licenseTextUrl = getLicenseTextUrl(license);
                logger.debug("Link to licence text: " + licenseTextUrl);
                if (licenseIndex++ > 0) {
                    sb.append(licenseJoinString);
                }
                if (includeLinks) {
                    sb.append("[");
                }
                sb.append(license.getName());
                if (includeLinks) {
                    sb.append("|");
                    sb.append(licenseTextUrl);
                    sb.append("]");
                }
            }
            licensesString = sb.toString();
        }
        return licensesString;
    }

    private String getLicenseTextUrl(final ComplexLicenseItem license) throws HubIntegrationException {
        final String licenseUrl = license.getLicense();
        final ComplexLicenseItem fullLicense = getHubServicesFactory().createHubRequestService().getItem(
                licenseUrl, ComplexLicenseItem.class);
        final String licenseTextUrl = getMetaService().getFirstLink(fullLicense, "text");
        return licenseTextUrl;
    }

}
