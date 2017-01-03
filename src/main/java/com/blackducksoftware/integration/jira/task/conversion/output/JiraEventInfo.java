/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
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
