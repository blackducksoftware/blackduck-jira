/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
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
 *******************************************************************************/
package com.blackducksoftware.integration.jira.task.conversion;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.dataservice.notification.item.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.item.PolicyContentItem;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.notification.processor.event.NotificationEvent;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.common.HubProjectMappings;
import com.blackducksoftware.integration.jira.common.HubUrlParser;
import com.blackducksoftware.integration.jira.common.JiraContext;
import com.blackducksoftware.integration.jira.common.JiraProject;
import com.blackducksoftware.integration.jira.common.exception.ConfigurationException;
import com.blackducksoftware.integration.jira.task.JiraSettingsService;
import com.blackducksoftware.integration.jira.task.conversion.output.JiraInfo;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;

public abstract class AbstractPolicyNotificationConverter extends NotificationToEventConverter {
    private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));

    public AbstractPolicyNotificationConverter(final HubProjectMappings mappings, final JiraServices jiraServices,
            final JiraContext jiraContext, final JiraSettingsService jiraSettingsService,
            final String issueTypeName, final MetaService metaService)
            throws ConfigurationException {
        super(jiraServices, jiraContext, jiraSettingsService, mappings, issueTypeName, metaService);
    }

    @Override
    public List<NotificationEvent> generateEvents(final NotificationContentItem notif) {
        final List<NotificationEvent> notifEvents = new ArrayList<>();

        logger.debug("policyNotif: " + notif);
        logger.debug("Getting JIRA project(s) mapped to Hub project: " + notif.getProjectVersion().getProjectName());
        final List<JiraProject> mappingJiraProjects = getMappings()
                .getJiraProjects(notif.getProjectVersion().getProjectName());
        logger.debug("There are " + mappingJiraProjects.size() + " JIRA projects mapped to this Hub project : "
                + notif.getProjectVersion().getProjectName());

        if (!mappingJiraProjects.isEmpty()) {

            for (final JiraProject jiraProject : mappingJiraProjects) {
                logger.debug("JIRA Project: " + jiraProject);
                try {
                    final List<NotificationEvent> projectEvents = handleNotificationPerJiraProject(notif, jiraProject);
                    if (projectEvents != null) {
                        notifEvents.addAll(projectEvents);
                    }
                } catch (final Exception e) {
                    logger.error(e);
                    getJiraSettingsService().addHubError(e, notif.getProjectVersion().getProjectName(),
                            notif.getProjectVersion().getProjectVersionName(), jiraProject.getProjectName(),
                            getJiraContext().getJiraUser().getName(), "transitionIssue");
                    return null;
                }

            }
        }
        return notifEvents;
    }

    protected abstract List<NotificationEvent> handleNotificationPerJiraProject(final NotificationContentItem notif,
            final JiraProject jiraProject) throws HubIntegrationException;

    public String getUniquePropertyKey(final JiraInfo jiraInfo, PolicyContentItem notificationContentItem,
            String policyRuleURL)
            throws HubIntegrationException, URISyntaxException {
        final StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_ISSUE_TYPE_NAME);
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_SEPARATOR);
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_ISSUE_TYPE_VALUE_POLICY);
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_PAIR_SEPARATOR);

        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_JIRA_PROJECT_ID_NAME);
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_SEPARATOR);
        keyBuilder.append(jiraInfo.getJiraProjectId().toString());
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_PAIR_SEPARATOR);

        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_HUB_PROJECT_VERSION_REL_URL_HASHED_NAME);
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_SEPARATOR);

        keyBuilder.append(hashString(HubUrlParser.getRelativeUrl(notificationContentItem.getProjectVersion().getUrl())));
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_PAIR_SEPARATOR);

        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_HUB_COMPONENT_REL_URL_HASHED_NAME);
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_SEPARATOR);
        keyBuilder.append(hashString(HubUrlParser.getRelativeUrl(notificationContentItem.getComponentUrl())));
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_PAIR_SEPARATOR);

        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_HUB_COMPONENT_VERSION_REL_URL_HASHED_NAME);
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_SEPARATOR);
        keyBuilder.append(hashString(HubUrlParser.getRelativeUrl(notificationContentItem.getComponentVersionUrl())));
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_PAIR_SEPARATOR);

        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_HUB_POLICY_RULE_REL_URL_HASHED_NAME);
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_SEPARATOR);
        keyBuilder.append(hashString(HubUrlParser.getRelativeUrl(policyRuleURL)));

        final String key = keyBuilder.toString();
        logger.debug("property key: " + key);
        return key;
    }

    private String hashString(final String origString) {
        String hashString;
        if (origString == null) {
            hashString = "";
        } else {
            hashString = String.valueOf(origString.hashCode());
        }
        logger.debug("Hash string for '" + origString + "': " + hashString);
        return hashString;
    }
}
