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
import java.util.Set;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.PolicyRuleViewV2;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.api.view.CommonNotificationState;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.notification.content.NotificationContentDetail;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.hub.service.bucket.HubBucket;
import com.blackducksoftware.integration.hub.throwaway.NotificationEvent;
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
    private final HubService hubService;

    public AbstractPolicyNotificationConverter(final Set<NotificationEvent> cache, final HubProjectMappings mappings, final JiraServices jiraServices, final JiraContext jiraContext, final JiraSettingsService jiraSettingsService,
            final String issueTypeName, final HubJiraFieldCopyConfigSerializable fieldCopyConfig, final HubService hubService, final HubJiraLogger logger)
            throws ConfigurationException {
        super(cache, jiraServices, jiraContext, jiraSettingsService, mappings, issueTypeName, fieldCopyConfig, hubService, logger);
        this.logger = logger;
        this.hubService = hubService;
    }

    @Override
    public void process(final CommonNotificationState commonNotificationState) throws HubIntegrationException {
        logger.debug("policyNotif: " + commonNotificationState);

        // FIXME get the hub project info
        final String hubProjectName = "";
        final String hubProjectVersionName = "";

        logger.debug("Getting JIRA project(s) mapped to Hub project: " + hubProjectName);
        final List<JiraProject> mappingJiraProjects = getMappings().getJiraProjects(hubProjectName);
        logger.debug("There are " + mappingJiraProjects.size() + " JIRA projects mapped to this Hub project : " + hubProjectVersionName);

        if (!mappingJiraProjects.isEmpty()) {
            for (final JiraProject jiraProject : mappingJiraProjects) {
                logger.debug("JIRA Project: " + jiraProject);
                try {
                    // FIXME pass in the correct data
                    final List<NotificationEvent> projectEvents = handleNotificationPerJiraProject(commonNotificationState, jiraProject);
                    if (projectEvents != null) {
                        for (final NotificationEvent event : projectEvents) {
                            getCache().add(event);
                        }
                    }
                } catch (final Exception e) {
                    logger.error(e);
                    getJiraSettingsService().addHubError(e, hubProjectName, hubProjectVersionName, jiraProject.getProjectName(),
                            getJiraContext().getJiraAdminUser().getName(), getJiraContext().getJiraIssueCreatorUser().getName(), "transitionIssue");
                }

            }
        }
    }

    protected abstract List<NotificationEvent> handleNotificationPerJiraProject(final CommonNotificationState commonNotificationState, final JiraProject jiraProject) throws EventDataBuilderException, IntegrationException;

    protected String getIssueDescription(final NotificationContentDetail detail, final PolicyRuleViewV2 rule, final HubBucket hubBucket) {
        final StringBuilder issueDescription = new StringBuilder();

        String componentsLink = null;
        if (detail.getProjectVersion().isPresent()) {
            final ProjectVersionView projectVersion = hubBucket.get(detail.getProjectVersion().get());
            componentsLink = hubService.getFirstLinkSafely(projectVersion, ProjectVersionView.COMPONENTS_LINK);
        }
        issueDescription.append("The Black Duck Hub has detected a policy violation on Hub project ");
        if (componentsLink == null) {
            issueDescription.append("'");
            issueDescription.append(detail.getProjectName());
            issueDescription.append("' / '");
            issueDescription.append(detail.getProjectVersionName());
            issueDescription.append("'");
        } else {
            issueDescription.append("['");
            issueDescription.append(detail.getProjectName());
            issueDescription.append("' / '");
            issueDescription.append(detail.getProjectVersionName());
            issueDescription.append("'|");
            issueDescription.append(componentsLink);
            issueDescription.append("]");
        }
        if (detail.getComponentName().isPresent()) {
            issueDescription.append(", component '");
            issueDescription.append(detail.getComponentName());
            if (detail.getComponentVersionName().isPresent()) {
                issueDescription.append("' / '");
                issueDescription.append(detail.getComponentVersionName().get());
            }
        }
        issueDescription.append("'.");
        issueDescription.append(" The rule violated is: '");
        issueDescription.append(rule.name);
        issueDescription.append("'. Rule overridable: ");
        issueDescription.append(rule.overridable);

        if (detail.getComponentVersion().isPresent()) {
            try {
                final ComponentVersionView componentVersion = hubBucket.get(detail.getComponentVersion().get());
                final String licenseText = getComponentLicensesStringWithLinksAtlassianFormat(componentVersion);
                issueDescription.append("\nComponent license(s): ");
                issueDescription.append(licenseText);
            } catch (final IntegrationException e) {
                // omit license text
            }
        }
        return issueDescription.toString();
    }

    protected String getIssueSummary(final NotificationContentDetail detail, final PolicyRuleViewV2 rule) {
        if (detail.getComponentName().isPresent()) {
            String componentString = detail.getComponentName().get();
            if (detail.getComponentVersionName().isPresent()) {
                componentString += "' / '" + detail.getComponentVersionName().get();
            }
            final String issueSummaryTemplate = "Black Duck policy violation detected on Hub project '%s' / '%s', component '%s' [Rule: '%s']";
            return String.format(issueSummaryTemplate, detail.getProjectName(), detail.getProjectVersionName(), componentString, rule.name);
        }
        return "";
    }

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

    public String hashString(final String text) {
        // FIXME hash the string
        return text;
    }
}
