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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.issue.issuetype.IssueType;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.dataservice.notification.item.NotificationContentItem;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.notification.processor.event.NotificationEvent;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.common.HubProjectMappings;
import com.blackducksoftware.integration.jira.common.JiraContext;
import com.blackducksoftware.integration.jira.common.JiraProject;
import com.blackducksoftware.integration.jira.common.exception.ConfigurationException;
import com.blackducksoftware.integration.jira.config.HubJiraFieldCopyConfigSerializable;
import com.blackducksoftware.integration.jira.task.JiraSettingsService;
import com.blackducksoftware.integration.jira.task.conversion.output.HubEventAction;
import com.blackducksoftware.integration.jira.task.conversion.output.IssuePropertiesGenerator;
import com.blackducksoftware.integration.jira.task.conversion.output.JiraEventInfo;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;

public abstract class NotificationToEventConverter {
    private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final JiraServices jiraServices;

    private final JiraContext jiraContext;

    private final JiraSettingsService jiraSettingsService;

    private final HubProjectMappings mappings;

    private final String issueTypeId;

    private final MetaService metaService;

    private final HubJiraFieldCopyConfigSerializable fieldCopyConfig;

    public NotificationToEventConverter(final JiraServices jiraServices, final JiraContext jiraContext,
            final JiraSettingsService jiraSettingsService,
            final HubProjectMappings mappings,
            final String issueTypeName, final MetaService metaService,
            final HubJiraFieldCopyConfigSerializable fieldCopyConfig) throws ConfigurationException {
        this.jiraServices = jiraServices;
        this.jiraContext = jiraContext;
        this.jiraSettingsService = jiraSettingsService;
        this.mappings = mappings;
        this.issueTypeId = lookUpIssueTypeId(issueTypeName);
        this.metaService = metaService;
        this.fieldCopyConfig = fieldCopyConfig;
    }

    public abstract List<NotificationEvent> generateEvents(NotificationContentItem notif);

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

    protected MetaService getMetaService() {
        return metaService;
    }

    protected Map<String, Object> createDataSet(final NotificationContentItem notif,
            final HubEventAction action,
            final JiraContext jiraContext, final JiraProject jiraProject,
            final String issueSummary,
            final String issueDescription,
            final String issueComment,
            final String issueReOpenComment,
            final String issueCommentForExistingIssue,
            final String issueResolveComment,
            final String issueCommentInLieuOfStateChange,
            final IssuePropertiesGenerator issuePropertiesGenerator,
            final String hubRuleName) {

        final JiraEventInfo jiraEventInfo = new JiraEventInfo();
        jiraEventInfo.setAction(action)
                .setJiraUserName(jiraContext.getJiraUser().getName())
                .setJiraUserKey(jiraContext.getJiraUser().getKey())
                .setJiraIssueAssigneeUserId(jiraProject.getAssigneeUserId())
                .setJiraIssueTypeId(getIssueTypeId())
                .setJiraProjectName(jiraProject.getProjectName())
                .setJiraProjectId(jiraProject.getProjectId())
                .setJiraFieldCopyMappings(getFieldCopyConfig().getProjectFieldCopyMappings())
                .setHubProjectName(notif.getProjectVersion().getProjectName())
                .setHubProjectVersion(notif.getProjectVersion().getProjectVersionName())
                .setHubComponentName(notif.getComponentName())
                .setHubComponentVersion(notif.getComponentVersion())
                .setJiraIssueSummary(issueSummary)
                .setJiraIssueDescription(issueDescription)
                .setJiraIssueComment(issueComment)
                .setJiraIssueReOpenComment(issueReOpenComment)
                .setJiraIssueCommentForExistingIssue(issueCommentForExistingIssue)
                .setJiraIssueResolveComment(issueResolveComment)
                .setJiraIssueCommentInLieuOfStateChange(issueCommentInLieuOfStateChange)
                .setJiraIssuePropertiesGenerator(issuePropertiesGenerator)
                .setHubRuleName(hubRuleName);

        return jiraEventInfo.getDataSet();
    }

    protected HubJiraFieldCopyConfigSerializable getFieldCopyConfig() {
        return fieldCopyConfig;
    }

    protected String hashString(final String origString) {
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
