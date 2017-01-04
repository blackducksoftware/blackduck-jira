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
package com.blackducksoftware.integration.jira.task.conversion.output;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.blackducksoftware.integration.jira.config.ProjectFieldCopyMapping;
import com.blackducksoftware.integration.jira.task.conversion.EventDataSetKeys;

public class JiraEventInfo {
    private HubEventAction action;

    private String jiraUserName;

    private String jiraUserKey;

    private String jiraIssueAssigneeUserId;

    private String jiraIssueTypeId;

    private String jiraProjectName;

    private Long jiraProjectId;

    private Set<ProjectFieldCopyMapping> jiraFieldCopyMappings;

    private String hubProjectName;

    private String hubProjectVersion;

    private String hubComponentName;

    private String hubComponentVersion;

    private String jiraIssueSummary;

    private String jiraIssueDescription;

    private String jiraIssueComment;

    private String jiraIssueReOpenComment;

    private String jiraIssueCommentForExistingIssue;

    private String jiraIssueResolveComment;

    private String jiraIssueCommentInLieuOfStateChange;

    private IssuePropertiesGenerator jiraIssuePropertiesGenerator;

    private String hubRuleName;

    public JiraEventInfo() {

    }

    @SuppressWarnings("unchecked")
    public JiraEventInfo(final Map<String, Object> dataSet) {
        action = (HubEventAction) dataSet.get(EventDataSetKeys.ACTION);
        jiraUserName = (String) dataSet.get(EventDataSetKeys.JIRA_USER_NAME);
        jiraUserKey = (String) dataSet.get(EventDataSetKeys.JIRA_USER_KEY);
        jiraIssueAssigneeUserId = (String) dataSet.get(EventDataSetKeys.JIRA_ISSUE_ASSIGNEE_USER_ID);
        jiraIssueTypeId = (String) dataSet.get(EventDataSetKeys.JIRA_ISSUE_TYPE_ID);

        jiraProjectName = (String) dataSet.get(EventDataSetKeys.JIRA_PROJECT_NAME);
        jiraProjectId = (Long) dataSet.get(EventDataSetKeys.JIRA_PROJECT_ID);
        jiraFieldCopyMappings = (Set<ProjectFieldCopyMapping>) dataSet.get(EventDataSetKeys.JIRA_FIELD_COPY_MAPPINGS);

        hubProjectName = (String) dataSet.get(EventDataSetKeys.HUB_PROJECT_NAME);
        hubProjectVersion = (String) dataSet.get(EventDataSetKeys.HUB_PROJECT_VERSION);
        hubComponentName = (String) dataSet.get(EventDataSetKeys.HUB_COMPONENT_NAME);
        hubComponentVersion = (String) dataSet.get(EventDataSetKeys.HUB_COMPONENT_VERSION);

        jiraIssueSummary = (String) dataSet.get(EventDataSetKeys.JIRA_ISSUE_SUMMARY);
        jiraIssueDescription = (String) dataSet.get(EventDataSetKeys.JIRA_ISSUE_DESCRIPTION);

        jiraIssueComment = (String) dataSet.get(EventDataSetKeys.JIRA_ISSUE_COMMENT);
        jiraIssueReOpenComment = (String) dataSet.get(EventDataSetKeys.JIRA_ISSUE_REOPEN_COMMENT);
        jiraIssueCommentForExistingIssue = (String) dataSet.get(EventDataSetKeys.JIRA_ISSUE_COMMENT_FOR_EXISTING_ISSUE);
        jiraIssueResolveComment = (String) dataSet.get(EventDataSetKeys.JIRA_ISSUE_RESOLVE_COMMENT);
        jiraIssueCommentInLieuOfStateChange = (String) dataSet.get(EventDataSetKeys.JIRA_ISSUE_COMMENT_IN_LIEU_OF_STATE_CHANGE);
        jiraIssuePropertiesGenerator = (IssuePropertiesGenerator) dataSet.get(EventDataSetKeys.JIRA_ISSUE_PROPERTIES_GENERATOR);
        hubRuleName = (String) dataSet.get(EventDataSetKeys.HUB_RULE_NAME);
    }

    public JiraEventInfo setAction(final HubEventAction action) {
        this.action = action;
        return this;
    }

    public JiraEventInfo setJiraUserName(final String jiraUserName) {
        this.jiraUserName = jiraUserName;
        return this;
    }

    public JiraEventInfo setJiraUserKey(final String jiraUserKey) {
        this.jiraUserKey = jiraUserKey;
        return this;
    }

    public JiraEventInfo setJiraIssueAssigneeUserId(final String jiraIssueAssigneeUserId) {
        this.jiraIssueAssigneeUserId = jiraIssueAssigneeUserId;
        return this;
    }

    public JiraEventInfo setJiraIssueTypeId(final String jiraIssueTypeId) {
        this.jiraIssueTypeId = jiraIssueTypeId;
        return this;
    }

    public JiraEventInfo setJiraProjectName(final String jiraProjectName) {
        this.jiraProjectName = jiraProjectName;
        return this;
    }

    public JiraEventInfo setJiraProjectId(final Long jiraProjectId) {
        this.jiraProjectId = jiraProjectId;
        return this;
    }

    public JiraEventInfo setJiraFieldCopyMappings(final Set<ProjectFieldCopyMapping> jiraFieldCopyMappings) {
        this.jiraFieldCopyMappings = jiraFieldCopyMappings;
        return this;
    }

    public JiraEventInfo setHubProjectName(final String hubProjectName) {
        this.hubProjectName = hubProjectName;
        return this;
    }

    public JiraEventInfo setHubProjectVersion(final String hubProjectVersion) {
        this.hubProjectVersion = hubProjectVersion;
        return this;
    }

    public JiraEventInfo setHubComponentName(final String hubComponentName) {
        this.hubComponentName = hubComponentName;
        return this;
    }

    public JiraEventInfo setHubComponentVersion(final String hubComponentVersion) {
        this.hubComponentVersion = hubComponentVersion;
        return this;
    }

    public JiraEventInfo setJiraIssueSummary(final String jiraIssueSummary) {
        this.jiraIssueSummary = jiraIssueSummary;
        return this;
    }

    public JiraEventInfo setJiraIssueDescription(final String jiraIssueDescription) {
        this.jiraIssueDescription = jiraIssueDescription;
        return this;
    }

    public JiraEventInfo setJiraIssueComment(final String jiraIssueComment) {
        this.jiraIssueComment = jiraIssueComment;
        return this;
    }

    public JiraEventInfo setJiraIssueReOpenComment(final String jiraIssueReOpenComment) {
        this.jiraIssueReOpenComment = jiraIssueReOpenComment;
        return this;
    }

    public JiraEventInfo setJiraIssueCommentForExistingIssue(final String jiraIssueCommentForExistingIssue) {
        this.jiraIssueCommentForExistingIssue = jiraIssueCommentForExistingIssue;
        return this;
    }

    public JiraEventInfo setJiraIssueResolveComment(final String jiraIssueResolveComment) {
        this.jiraIssueResolveComment = jiraIssueResolveComment;
        return this;
    }

    public JiraEventInfo setJiraIssueCommentInLieuOfStateChange(final String jiraIssueCommentInLieuOfStateChange) {
        this.jiraIssueCommentInLieuOfStateChange = jiraIssueCommentInLieuOfStateChange;
        return this;
    }

    public JiraEventInfo setJiraIssuePropertiesGenerator(final IssuePropertiesGenerator jiraIssuePropertiesGenerator) {
        this.jiraIssuePropertiesGenerator = jiraIssuePropertiesGenerator;
        return this;
    }

    public JiraEventInfo setHubRuleName(final String hubRuleName) {
        this.hubRuleName = hubRuleName;
        return this;
    }

    public HubEventAction getAction() {
        return action;
    }

    public String getJiraUserName() {
        return jiraUserName;
    }

    public String getJiraUserKey() {
        return jiraUserKey;
    }

    public String getJiraIssueAssigneeUserId() {
        return jiraIssueAssigneeUserId;
    }

    public String getJiraIssueTypeId() {
        return jiraIssueTypeId;
    }

    public String getJiraProjectName() {
        return jiraProjectName;
    }

    public Long getJiraProjectId() {
        return jiraProjectId;
    }

    public Set<ProjectFieldCopyMapping> getJiraFieldCopyMappings() {
        return jiraFieldCopyMappings;
    }

    public String getHubProjectName() {
        return hubProjectName;
    }

    public String getHubProjectVersion() {
        return hubProjectVersion;
    }

    public String getHubComponentName() {
        return hubComponentName;
    }

    public String getHubComponentVersion() {
        return hubComponentVersion;
    }

    public String getJiraIssueSummary() {
        return jiraIssueSummary;
    }

    public String getJiraIssueDescription() {
        return jiraIssueDescription;
    }

    public String getJiraIssueComment() {
        return jiraIssueComment;
    }

    public String getJiraIssueReOpenComment() {
        return jiraIssueReOpenComment;
    }

    public String getJiraIssueCommentForExistingIssue() {
        return jiraIssueCommentForExistingIssue;
    }

    public String getJiraIssueResolveComment() {
        return jiraIssueResolveComment;
    }

    public String getJiraIssueCommentInLieuOfStateChange() {
        return jiraIssueCommentInLieuOfStateChange;
    }

    public IssuePropertiesGenerator getJiraIssuePropertiesGenerator() {
        return jiraIssuePropertiesGenerator;
    }

    public String getHubRuleName() {
        return hubRuleName;
    }

    public Map<String, Object> getDataSet() {
        final Map<String, Object> dataSet = new HashMap<>();
        dataSet.put(EventDataSetKeys.ACTION, action);
        dataSet.put(EventDataSetKeys.JIRA_USER_NAME, jiraUserName);
        dataSet.put(EventDataSetKeys.JIRA_USER_KEY, jiraUserKey);
        dataSet.put(EventDataSetKeys.JIRA_ISSUE_ASSIGNEE_USER_ID, jiraIssueAssigneeUserId);
        dataSet.put(EventDataSetKeys.JIRA_ISSUE_TYPE_ID, jiraIssueTypeId);

        dataSet.put(EventDataSetKeys.JIRA_PROJECT_NAME, jiraProjectName);
        dataSet.put(EventDataSetKeys.JIRA_PROJECT_ID, jiraProjectId);
        dataSet.put(EventDataSetKeys.JIRA_FIELD_COPY_MAPPINGS, jiraFieldCopyMappings);

        dataSet.put(EventDataSetKeys.HUB_PROJECT_NAME, hubProjectName);
        dataSet.put(EventDataSetKeys.HUB_PROJECT_VERSION, hubProjectVersion);
        dataSet.put(EventDataSetKeys.HUB_COMPONENT_NAME, hubComponentName);
        dataSet.put(EventDataSetKeys.HUB_COMPONENT_VERSION, hubComponentVersion);

        dataSet.put(EventDataSetKeys.JIRA_ISSUE_SUMMARY, jiraIssueSummary);
        dataSet.put(EventDataSetKeys.JIRA_ISSUE_DESCRIPTION, jiraIssueDescription);

        dataSet.put(EventDataSetKeys.JIRA_ISSUE_COMMENT, jiraIssueComment);
        dataSet.put(EventDataSetKeys.JIRA_ISSUE_REOPEN_COMMENT, jiraIssueReOpenComment);
        dataSet.put(EventDataSetKeys.JIRA_ISSUE_COMMENT_FOR_EXISTING_ISSUE, jiraIssueCommentForExistingIssue);
        dataSet.put(EventDataSetKeys.JIRA_ISSUE_RESOLVE_COMMENT, jiraIssueResolveComment);
        dataSet.put(EventDataSetKeys.JIRA_ISSUE_COMMENT_IN_LIEU_OF_STATE_CHANGE, jiraIssueCommentInLieuOfStateChange);
        dataSet.put(EventDataSetKeys.JIRA_ISSUE_PROPERTIES_GENERATOR, jiraIssuePropertiesGenerator);
        dataSet.put(EventDataSetKeys.HUB_RULE_NAME, hubRuleName);

        return dataSet;
    }
}
