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

import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.config.ProjectFieldCopyMapping;

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

    private String hubProjectVersionUrl;

    private String hubComponentName;

    private String hubComponentUrl;

    private String hubComponentVersion;

    private String hubComponentVersionUrl;

    private String hubLicenseNames;

    private String hubComponentUsage;

    private String hubComponentOrigin;

    private String hubComponentOriginId;

    private String hubProjectVersionNickname;

    private String jiraIssueSummary;

    private String jiraIssueDescription;

    private String jiraIssueComment;

    private String jiraIssueReOpenComment;

    private String jiraIssueCommentForExistingIssue;

    private String jiraIssueResolveComment;

    private String jiraIssueCommentInLieuOfStateChange;

    private IssuePropertiesGenerator jiraIssuePropertiesGenerator;

    private String hubRuleName;

    private String hubRuleUrl;

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

    public JiraEventInfo setHubProjectVersionUrl(final String hubProjectVersionUrl) {
        this.hubProjectVersionUrl = hubProjectVersionUrl;
        return this;
    }

    public JiraEventInfo setHubComponentName(final String hubComponentName) {
        this.hubComponentName = hubComponentName;
        return this;
    }

    public JiraEventInfo setHubComponentUrl(final String hubComponentUrl) {
        this.hubComponentUrl = hubComponentUrl;
        return this;
    }

    public JiraEventInfo setHubComponentVersion(final String hubComponentVersion) {
        this.hubComponentVersion = hubComponentVersion;
        return this;
    }

    public JiraEventInfo setHubComponentVersionUrl(final String hubComponentVersionUrl) {
        this.hubComponentVersionUrl = hubComponentVersionUrl;
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

    public JiraEventInfo setHubRuleUrl(final String hubRuleUrl) {
        this.hubRuleUrl = hubRuleUrl;
        return this;
    }

    public JiraEventInfo setHubLicenseNames(final String hubLicenseNames) {
        this.hubLicenseNames = hubLicenseNames;
        return this;
    }

    public JiraEventInfo setHubComponentUsage(final String hubComponentUsage) {
        this.hubComponentUsage = hubComponentUsage;
        return this;
    }

    public JiraEventInfo setHubComponentOrigin(final String hubComponentOrigin) {
        this.hubComponentOrigin = hubComponentOrigin;
        return this;
    }

    public JiraEventInfo setHubComponentOriginId(final String hubComponentOriginId) {
        this.hubComponentOriginId = hubComponentOriginId;
        return this;
    }

    public JiraEventInfo setHubProjectVersionNickname(final String hubProjectVersionNickname) {
        this.hubProjectVersionNickname = hubProjectVersionNickname;
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

    public String getHubProjectVersionUrl() {
        return hubProjectVersionUrl;
    }

    public String getHubComponentName() {
        return hubComponentName;
    }

    public String getHubComponentUrl() {
        return hubComponentUrl;
    }

    public String getHubComponentVersion() {
        return hubComponentVersion;
    }

    public String getHubComponentVersionUrl() {
        return hubComponentVersionUrl;
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

    public String getHubRuleUrl() {
        return hubRuleUrl;
    }

    public String getHubLicenseNames() {
        return hubLicenseNames;
    }

    public String getHubComponentUsage() {
        return hubComponentUsage;
    }

    public String getHubComponentOrigin() {
        return hubComponentOrigin;
    }

    public String getHubComponentOriginId() {
        return hubComponentOriginId;
    }

    public String getHubProjectVersionNickname() {
        return hubProjectVersionNickname;
    }

    public Map<String, Object> getDataSet() {
        final Map<String, Object> dataSet = new HashMap<>();
        dataSet.put(HubJiraConstants.EVENT_DATA_SET_KEY_JIRA_EVENT_INFO, this);
        return dataSet;
    }
}
