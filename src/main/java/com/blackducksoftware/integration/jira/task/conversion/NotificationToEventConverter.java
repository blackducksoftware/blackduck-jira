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
import java.util.Map;

import com.atlassian.jira.issue.issuetype.IssueType;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.dataservice.notification.model.NotificationContentItem;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.notification.processor.NotificationSubProcessor;
import com.blackducksoftware.integration.hub.notification.processor.SubProcessorCache;
import com.blackducksoftware.integration.hub.notification.processor.event.NotificationEvent;
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

public abstract class NotificationToEventConverter extends NotificationSubProcessor {

    private final JiraServices jiraServices;

    private final JiraContext jiraContext;

    private final JiraSettingsService jiraSettingsService;

    private final HubProjectMappings mappings;

    private final String issueTypeId;

    private final HubJiraFieldCopyConfigSerializable fieldCopyConfig;

    public NotificationToEventConverter(final SubProcessorCache cache, final JiraServices jiraServices, final JiraContext jiraContext,
            final JiraSettingsService jiraSettingsService,
            final HubProjectMappings mappings,
            final String issueTypeName, final MetaService metaService,
            final HubJiraFieldCopyConfigSerializable fieldCopyConfig) throws ConfigurationException {
        super(cache, metaService);
        this.jiraServices = jiraServices;
        this.jiraContext = jiraContext;
        this.jiraSettingsService = jiraSettingsService;
        this.mappings = mappings;
        this.issueTypeId = lookUpIssueTypeId(issueTypeName);
        this.fieldCopyConfig = fieldCopyConfig;
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

    @Override
    public Map<String, Object> generateDataSet(final Map<String, Object> inputData) {
        final NotificationContentItem notif = (NotificationContentItem) inputData.get(NotificationEvent.DATA_SET_KEY_NOTIFICATION_CONTENT);
        final HubEventAction action = (HubEventAction) inputData.get(EventDataSetKeys.ACTION);
        final JiraContext jiraContext = (JiraContext) inputData.get(EventDataSetKeys.JIRA_CONTEXT);
        final JiraProject jiraProject = (JiraProject) inputData.get(EventDataSetKeys.JIRA_PROJECT);
        final String issueSummary = (String) inputData.get(EventDataSetKeys.JIRA_ISSUE_SUMMARY);
        final String issueDescription = (String) inputData.get(EventDataSetKeys.JIRA_ISSUE_DESCRIPTION);
        final String issueComment = (String) inputData.get(EventDataSetKeys.JIRA_ISSUE_COMMENT);
        final String issueReOpenComment = (String) inputData.get(EventDataSetKeys.JIRA_ISSUE_REOPEN_COMMENT);
        final String issueCommentForExistingIssue = (String) inputData.get(EventDataSetKeys.JIRA_ISSUE_COMMENT_FOR_EXISTING_ISSUE);
        final String issueResolveComment = (String) inputData.get(EventDataSetKeys.JIRA_ISSUE_RESOLVE_COMMENT);
        final String issueCommentInLieuOfStateChange = (String) inputData.get(EventDataSetKeys.JIRA_ISSUE_COMMENT_IN_LIEU_OF_STATE_CHANGE);
        final IssuePropertiesGenerator issuePropertiesGenerator = (IssuePropertiesGenerator) inputData.get(EventDataSetKeys.JIRA_ISSUE_PROPERTIES_GENERATOR);
        final String hubRuleName = (String) inputData.get(EventDataSetKeys.HUB_RULE_NAME);

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
                .setHubComponentVersion(notif.getComponentVersion().getVersionName())
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
}
