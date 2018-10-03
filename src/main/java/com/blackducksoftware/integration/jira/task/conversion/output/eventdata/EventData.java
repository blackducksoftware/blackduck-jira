/**
 * Black Duck JIRA Plugin
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
package com.blackducksoftware.integration.jira.task.conversion.output.eventdata;

import java.util.Date;
import java.util.Set;

import com.atlassian.jira.user.ApplicationUser;
import com.blackducksoftware.integration.jira.config.model.ProjectFieldCopyMapping;
import com.blackducksoftware.integration.jira.task.conversion.output.BlackDuckEventAction;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.util.Stringable;

public class EventData extends Stringable {
    private BlackDuckEventAction action;
    private IssueCategory category;
    private Date lastBatchStartDate;
    private String jiraAdminUsername;
    private String jiraIssueCreatorUsername;
    private String jiraAdminUserKey;
    private String jiraIssueCreatorUserKey;
    private String jiraIssueAssigneeUserId;
    private String jiraIssueTypeId;
    private String jiraProjectName;
    private Long jiraProjectId;
    private Set<ProjectFieldCopyMapping> jiraFieldCopyMappings;

    private String blackDuckBaseUrl;
    private String blackDuckProjectName;
    private String blackDuckProjectVersion;
    private String blackDuckProjectVersionUrl;
    private String blackDuckComponentName;
    private String blackDuckComponentUrl;
    private String blackDuckComponentVersion;
    private String blackDuckComponentVersionUrl;
    private String blackDuckLicenseNames;
    private String blackDuckLicenseUrl;
    private String blackDuckComponentUsage;
    private String blackDuckComponentOrigin;
    private String blackDuckComponentOriginId;
    private String blackDuckProjectVersionNickname;
    private String blackDuckBomComponentUri;

    private String blackDuckRuleName;
    private String blackDuckRuleOverridable;
    private String blackDuckRuleDescription;
    private String blackDuckRuleUrl;
    private String componentIssueUrl;
    private ApplicationUser blackDuckProjectOwner;

    private String jiraIssueSummary;
    private String jiraIssueDescription;
    private String jiraIssueComment;
    private String jiraIssueReOpenComment;
    private String jiraIssueCommentForExistingIssue;
    private String jiraIssueResolveComment;
    private String jiraIssueCommentInLieuOfStateChange;
    private String blackDuckProjectVersionLastUpdated;
    private NotificationType notificationType;
    private String eventKey;

    // The constructor and setters are only for EventDataBuilder
    EventData() {
    }

    public void overrideAction(final BlackDuckEventAction action) {
        this.action = action;
    }

    public IssueCategory getCategory() {
        return category;
    }

    EventData setCategory(final IssueCategory category) {
        this.category = category;
        return this;
    }

    public boolean isPolicy() {
        return IssueCategory.POLICY.equals(category);
    }

    public boolean isVulnerability() {
        return IssueCategory.VULNERABILITY.equals(category);
    }

    EventData setBlackDuckProjectVersion(final String blackDuckProjectVersion) {
        this.blackDuckProjectVersion = blackDuckProjectVersion;
        return this;
    }

    EventData setBlackDuckComponentVersion(final String blackDuckComponentVersion) {
        this.blackDuckComponentVersion = blackDuckComponentVersion;
        return this;
    }

    public BlackDuckEventAction getAction() {
        return action;
    }

    EventData setAction(final BlackDuckEventAction action) {
        this.action = action;
        return this;
    }

    public Date getLastBatchStartDate() {
        return lastBatchStartDate;
    }

    EventData setLastBatchStartDate(final Date lastBatchStartDate) {
        this.lastBatchStartDate = lastBatchStartDate;
        return this;
    }

    public String getJiraAdminUsername() {
        return jiraAdminUsername;
    }

    EventData setJiraAdminUsername(final String jiraAdminUsername) {
        this.jiraAdminUsername = jiraAdminUsername;
        return this;
    }

    public String getJiraIssueCreatorUsername() {
        return jiraIssueCreatorUsername;
    }

    EventData setJiraIssueCreatorUsername(final String jiraIssueCreatorUsername) {
        this.jiraIssueCreatorUsername = jiraIssueCreatorUsername;
        return this;
    }

    public String getJiraAdminUserKey() {
        return jiraAdminUserKey;
    }

    EventData setJiraAdminUserKey(final String jiraAdminUserKey) {
        this.jiraAdminUserKey = jiraAdminUserKey;
        return this;
    }

    public String getJiraIssueCreatorUserKey() {
        return jiraIssueCreatorUserKey;
    }

    EventData setJiraIssueCreatorUserKey(final String jiraIssueCreatorUserKey) {
        this.jiraIssueCreatorUserKey = jiraIssueCreatorUserKey;
        return this;
    }

    public String getJiraIssueAssigneeUserId() {
        return jiraIssueAssigneeUserId;
    }

    EventData setJiraIssueAssigneeUserId(final String jiraIssueAssigneeUserId) {
        this.jiraIssueAssigneeUserId = jiraIssueAssigneeUserId;
        return this;
    }

    public String getJiraIssueTypeId() {
        return jiraIssueTypeId;
    }

    EventData setJiraIssueTypeId(final String jiraIssueTypeId) {
        this.jiraIssueTypeId = jiraIssueTypeId;
        return this;
    }

    public String getJiraProjectName() {
        return jiraProjectName;
    }

    EventData setJiraProjectName(final String jiraProjectName) {
        this.jiraProjectName = jiraProjectName;
        return this;
    }

    public Long getJiraProjectId() {
        return jiraProjectId;
    }

    EventData setJiraProjectId(final Long jiraProjectId) {
        this.jiraProjectId = jiraProjectId;
        return this;
    }

    public Set<ProjectFieldCopyMapping> getJiraFieldCopyMappings() {
        return jiraFieldCopyMappings;
    }

    EventData setJiraFieldCopyMappings(final Set<ProjectFieldCopyMapping> jiraFieldCopyMappings) {
        this.jiraFieldCopyMappings = jiraFieldCopyMappings;
        return this;
    }

    public String getBlackDuckBaseUrl() {
        return blackDuckBaseUrl;
    }

    EventData setBlackDuckBaseUrl(final String blackDuckBaseUrl) {
        this.blackDuckBaseUrl = blackDuckBaseUrl;
        return this;
    }

    public String getBlackDuckProjectName() {
        return blackDuckProjectName;
    }

    EventData setBlackDuckProjectName(final String blackDuckProjectName) {
        this.blackDuckProjectName = blackDuckProjectName;
        return this;
    }

    public String getBlackDuckProjectVersionName() {
        return blackDuckProjectVersion;
    }

    public String getBlackDuckProjectVersionUrl() {
        return blackDuckProjectVersionUrl;
    }

    EventData setBlackDuckProjectVersionUrl(final String blackDuckProjectVersionUrl) {
        this.blackDuckProjectVersionUrl = blackDuckProjectVersionUrl;
        return this;
    }

    public String getBlackDuckComponentName() {
        return blackDuckComponentName;
    }

    EventData setBlackDuckComponentName(final String blackDuckComponentName) {
        this.blackDuckComponentName = blackDuckComponentName;
        return this;
    }

    public String getBlackDuckComponentUrl() {
        return blackDuckComponentUrl;
    }

    EventData setBlackDuckComponentUrl(final String blackDuckComponentUrl) {
        this.blackDuckComponentUrl = blackDuckComponentUrl;
        return this;
    }

    public String getBlackDuckComponentVersionName() {
        return blackDuckComponentVersion;
    }

    public String getBlackDuckComponentVersionUrl() {
        return blackDuckComponentVersionUrl;
    }

    EventData setBlackDuckComponentVersionUrl(final String blackDuckComponentVersionUrl) {
        this.blackDuckComponentVersionUrl = blackDuckComponentVersionUrl;
        return this;
    }

    public String getJiraIssueSummary() {
        return jiraIssueSummary;
    }

    EventData setJiraIssueSummary(final String jiraIssueSummary) {
        this.jiraIssueSummary = jiraIssueSummary;
        return this;
    }

    public String getJiraIssueDescription() {
        return jiraIssueDescription;
    }

    EventData setJiraIssueDescription(final String jiraIssueDescription) {
        this.jiraIssueDescription = jiraIssueDescription;
        return this;
    }

    public String getJiraIssueComment() {
        return jiraIssueComment;
    }

    EventData setJiraIssueComment(final String jiraIssueComment) {
        this.jiraIssueComment = jiraIssueComment;
        return this;
    }

    public String getJiraIssueReOpenComment() {
        return jiraIssueReOpenComment;
    }

    EventData setJiraIssueReOpenComment(final String jiraIssueReOpenComment) {
        this.jiraIssueReOpenComment = jiraIssueReOpenComment;
        return this;
    }

    public String getJiraIssueCommentForExistingIssue() {
        return jiraIssueCommentForExistingIssue;
    }

    EventData setJiraIssueCommentForExistingIssue(final String jiraIssueCommentForExistingIssue) {
        this.jiraIssueCommentForExistingIssue = jiraIssueCommentForExistingIssue;
        return this;
    }

    public String getJiraIssueResolveComment() {
        return jiraIssueResolveComment;
    }

    EventData setJiraIssueResolveComment(final String jiraIssueResolveComment) {
        this.jiraIssueResolveComment = jiraIssueResolveComment;
        return this;
    }

    public String getJiraIssueCommentInLieuOfStateChange() {
        return jiraIssueCommentInLieuOfStateChange;
    }

    EventData setJiraIssueCommentInLieuOfStateChange(final String jiraIssueCommentInLieuOfStateChange) {
        this.jiraIssueCommentInLieuOfStateChange = jiraIssueCommentInLieuOfStateChange;
        return this;
    }

    public String getBlackDuckRuleName() {
        return blackDuckRuleName;
    }

    EventData setBlackDuckRuleName(final String blackDuckRuleName) {
        this.blackDuckRuleName = blackDuckRuleName;
        return this;
    }

    public String getBlackDuckRuleOverridable() {
        return blackDuckRuleOverridable;
    }

    EventData setBlackDuckRuleOverridable(final String blackDuckRuleOverridable) {
        this.blackDuckRuleOverridable = blackDuckRuleOverridable;
        return this;
    }

    public String getBlackDuckRuleDescription() {
        return blackDuckRuleDescription;
    }

    EventData setBlackDuckRuleDescription(final String blackDuckRuleDescription) {
        this.blackDuckRuleDescription = blackDuckRuleDescription;
        return this;
    }

    public String getBlackDuckRuleUrl() {
        return blackDuckRuleUrl;
    }

    EventData setBlackDuckRuleUrl(final String blackDuckRuleUrl) {
        this.blackDuckRuleUrl = blackDuckRuleUrl;
        return this;
    }

    public String getBlackDuckLicenseNames() {
        return blackDuckLicenseNames;
    }

    EventData setBlackDuckLicenseNames(final String blackDuckLicenseNames) {
        this.blackDuckLicenseNames = blackDuckLicenseNames;
        return this;
    }

    public String getBlackDuckLicenseUrl() {
        return blackDuckLicenseUrl;
    }

    EventData setBlackDuckLicenseUrl(final String blackDuckLicenseUrl) {
        this.blackDuckLicenseUrl = blackDuckLicenseUrl;
        return this;
    }

    public String getBlackDuckComponentUsage() {
        return blackDuckComponentUsage;
    }

    EventData setBlackDuckComponentUsage(final String blackDuckComponentUsage) {
        this.blackDuckComponentUsage = blackDuckComponentUsage;
        return this;
    }

    public String getBlackDuckComponentOrigin() {
        return blackDuckComponentOrigin;
    }

    EventData setBlackDuckComponentOrigin(final String blackDuckComponentOrigin) {
        this.blackDuckComponentOrigin = blackDuckComponentOrigin;
        return this;
    }

    public String getBlackDuckComponentOriginId() {
        return blackDuckComponentOriginId;
    }

    EventData setBlackDuckComponentOriginId(final String blackDuckComponentOriginId) {
        this.blackDuckComponentOriginId = blackDuckComponentOriginId;
        return this;
    }

    public String getBlackDuckProjectVersionNickname() {
        return blackDuckProjectVersionNickname;
    }

    EventData setBlackDuckProjectVersionNickname(final String blackDuckProjectVersionNickname) {
        this.blackDuckProjectVersionNickname = blackDuckProjectVersionNickname;
        return this;
    }

    public String getBlackDuckBomComponentUri() {
        return blackDuckBomComponentUri;
    }

    EventData setBlackDuckBomComponentUri(final String blackDuckBomComponentUri) {
        this.blackDuckBomComponentUri = blackDuckBomComponentUri;
        return this;
    }

    public String getComponentIssueUrl() {
        return componentIssueUrl;
    }

    EventData setComponentIssueUrl(final String componentIssueUrl) {
        this.componentIssueUrl = componentIssueUrl;
        return this;
    }

    public ApplicationUser getBlackDuckProjectOwner() {
        return blackDuckProjectOwner;
    }

    EventData setBlackDuckProjectOwner(final ApplicationUser blackDuckProjectOwner) {
        this.blackDuckProjectOwner = blackDuckProjectOwner;
        return this;
    }

    public String getBlackDuckProjectVersionLastUpdated() {
        return blackDuckProjectVersionLastUpdated;
    }

    EventData setBlackDuckProjectVersionLastUpdated(final String blackDuckProjectVersionLastUpdated) {
        this.blackDuckProjectVersionLastUpdated = blackDuckProjectVersionLastUpdated;
        return this;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public EventData setNotificationType(final NotificationType notificationType) {
        this.notificationType = notificationType;
        return this;
    }

    public String getEventKey() {
        return eventKey;
    }

    public EventData setEventKey(final String eventKey) {
        this.eventKey = eventKey;
        return this;
    }

}
