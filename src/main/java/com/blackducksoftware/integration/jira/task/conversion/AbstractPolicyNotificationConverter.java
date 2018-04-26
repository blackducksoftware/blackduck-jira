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

import java.util.List;
import java.util.Map;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.PolicyRuleView;
import com.blackducksoftware.integration.hub.api.generated.view.VersionBomComponentView;
import com.blackducksoftware.integration.hub.api.view.MetaHandler;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.throwaway.NotificationContentItem;
import com.blackducksoftware.integration.hub.throwaway.NotificationEvent;
import com.blackducksoftware.integration.hub.throwaway.PolicyContentItem;
import com.blackducksoftware.integration.hub.throwaway.ProjectVersionModel;
import com.blackducksoftware.integration.hub.throwaway.SubProcessorCache;
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
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventData;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;

public abstract class AbstractPolicyNotificationConverter extends NotificationToEventConverter {
    private final HubJiraLogger logger;

    public AbstractPolicyNotificationConverter(final SubProcessorCache cache, final HubProjectMappings mappings, final JiraServices jiraServices, final JiraContext jiraContext, final JiraSettingsService jiraSettingsService,
            final String issueTypeName, final HubJiraFieldCopyConfigSerializable fieldCopyConfig, final HubServicesFactory hubServicesFactory, final MetaHandler metaHandler, final HubJiraLogger logger)
            throws ConfigurationException {
        super(cache, jiraServices, jiraContext, jiraSettingsService, mappings, issueTypeName, fieldCopyConfig, hubServicesFactory, metaHandler, logger);
        this.logger = logger;
    }

    @Override
    public void process(final NotificationContentItem notification) throws HubIntegrationException {
        logger.debug("policyNotif: " + notification);
        final ProjectVersionModel projectVersion = notification.getProjectVersion();
        logger.debug("Getting JIRA project(s) mapped to Hub project: " + projectVersion.getProjectName());
        final List<JiraProject> mappingJiraProjects = getMappings()
                .getJiraProjects(projectVersion.getProjectName());
        logger.debug("There are " + mappingJiraProjects.size() + " JIRA projects mapped to this Hub project : "
                + projectVersion.getProjectName());

        if (!mappingJiraProjects.isEmpty()) {

            for (final JiraProject jiraProject : mappingJiraProjects) {
                logger.debug("JIRA Project: " + jiraProject);
                try {
                    final List<NotificationEvent> projectEvents = handleNotificationPerJiraProject(notification, jiraProject);
                    if (projectEvents != null) {
                        for (final NotificationEvent event : projectEvents) {
                            getCache().addEvent(event);
                        }
                    }
                } catch (final Exception e) {
                    logger.error(e);
                    getJiraSettingsService().addHubError(e, projectVersion.getProjectName(),
                            projectVersion.getProjectVersionName(), jiraProject.getProjectName(),
                            getJiraContext().getJiraAdminUser().getName(), getJiraContext().getJiraIssueCreatorUser().getName(), "transitionIssue");
                }

            }
        }
    }

    @Override
    protected VersionBomComponentView getBomComponent(final NotificationContentItem notification) throws HubIntegrationException {
        final PolicyContentItem policyNotif = (PolicyContentItem) notification;
        final VersionBomComponentView bomComp = getBomComponent(notification.getProjectVersion(),
                notification.getComponentName(), policyNotif.getComponentUrl(), notification.getComponentVersion());
        return bomComp;
    }

    protected abstract List<NotificationEvent> handleNotificationPerJiraProject(final NotificationContentItem notif,
            final JiraProject jiraProject) throws EventDataBuilderException, IntegrationException;

    protected String getIssueDescription(final NotificationContentItem notif, final PolicyRuleView rule) {
        final StringBuilder issueDescription = new StringBuilder();
        final String componentsLink = notif.getProjectVersion().getComponentsLink();
        issueDescription.append("The Black Duck Hub has detected a policy violation on Hub project ");
        if (componentsLink == null) {
            issueDescription.append("'");
            issueDescription.append(notif.getProjectVersion().getProjectName());
            issueDescription.append("' / '");
            issueDescription.append(notif.getProjectVersion().getProjectVersionName());
            issueDescription.append("'");
        } else {
            issueDescription.append("['");
            issueDescription.append(notif.getProjectVersion().getProjectName());
            issueDescription.append("' / '");
            issueDescription.append(notif.getProjectVersion().getProjectVersionName());
            issueDescription.append("'|");
            issueDescription.append(componentsLink);
            issueDescription.append("]");
        }
        issueDescription.append(", component '");
        issueDescription.append(notif.getComponentName());
        final ComponentVersionView compVer = notif.getComponentVersion();
        if (compVer != null) {
            issueDescription.append("' / '");
            issueDescription.append(compVer.versionName);
        }
        issueDescription.append("'.");
        issueDescription.append(" The rule violated is: '");
        issueDescription.append(rule.name);
        issueDescription.append("'. Rule overridable: ");
        issueDescription.append(rule.overridable);

        if (compVer != null) {
            final String licenseText;
            try {
                licenseText = getComponentLicensesStringWithLinksAtlassianFormat(notif);
                issueDescription.append("\nComponent license(s): ");
                issueDescription.append(licenseText);
            } catch (final IntegrationException e) {
                // omit license text
            }
        }

        return issueDescription.toString();
    }

    protected String getIssueSummary(final NotificationContentItem notif, final PolicyRuleView rule) {
        String componentString = notif.getComponentName();
        if (notif.getComponentVersion() != null) {
            componentString += "' / '" + notif.getComponentVersion().versionName;
        }
        final String issueSummaryTemplate = "Black Duck policy violation detected on Hub project '%s' / '%s', component '%s' [Rule: '%s']";
        final Object[] replacements = new String[] { notif.getProjectVersion().getProjectName(), notif.getProjectVersion().getProjectVersionName(), componentString, rule.name };

        final String issueSummary = String.format(issueSummaryTemplate, replacements);
        return issueSummary.toString();
    }

    @Override
    public String generateEventKey(final Map<String, Object> inputData) throws HubIntegrationException {
        final EventData eventData = (EventData) inputData.get(HubJiraConstants.EVENT_DATA_SET_KEY_JIRA_EVENT_DATA);

        final Long jiraProjectId = eventData.getJiraProjectId();
        final String hubProjectVersionUrl = eventData.getHubProjectVersionUrl();
        final String hubComponentVersionUrl = eventData.getHubComponentVersionUrl();
        final String hubComponentUrl = eventData.getHubComponentUrl();
        final String policyRuleUrl = eventData.getHubRuleUrl();
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
}
