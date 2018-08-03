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
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.jira.config.model.ProjectFieldCopyMapping;
import com.blackducksoftware.integration.jira.task.conversion.output.BlackDuckEventAction;
import com.blackducksoftware.integration.jira.task.conversion.output.IssuePropertiesGenerator;
import com.blackducksoftware.integration.util.Stringable;

public class EventData extends Stringable {
    private BlackDuckEventAction action;
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
    private String jiraIssueSummary;
    private String jiraIssueDescription;
    private String jiraIssueComment;
    private String jiraIssueReOpenComment;
    private String jiraIssueCommentForExistingIssue;
    private String jiraIssueResolveComment;
    private String jiraIssueCommentInLieuOfStateChange;
    private IssuePropertiesGenerator jiraIssuePropertiesGenerator;
    private String blackDuckRuleName;
    private String blackDuckRuleOverridable;
    private String blackDuckRuleDescription;
    private String blackDuckRuleUrl;
    private String componentIssueUrl;
    private ApplicationUser blackDuckProjectOwner;
    private String blackDuckProjectVersionLastUpdated;
    private NotificationType notificationType;
    private String eventKey;

    // The constructor and setters are only for EventDataBuilder
    EventData() {
    }

    EventData setAction(final BlackDuckEventAction action) {
        this.action = action;
        return this;
    }

    public boolean isPolicy() {
        return NotificationType.POLICY_OVERRIDE.equals(notificationType) || NotificationType.RULE_VIOLATION.equals(notificationType) || NotificationType.RULE_VIOLATION_CLEARED.equals(notificationType);
    }

    EventData setLastBatchStartDate(final Date lastBatchStartDate) {
        this.lastBatchStartDate = lastBatchStartDate;
        return this;
    }

    EventData setJiraAdminUsername(final String jiraAdminUsername) {
        this.jiraAdminUsername = jiraAdminUsername;
        return this;
    }

    EventData setJiraIssueCreatorUsername(final String jiraIssueCreatorUsername) {
        this.jiraIssueCreatorUsername = jiraIssueCreatorUsername;
        return this;
    }

    EventData setJiraAdminUserKey(final String jiraAdminUserKey) {
        this.jiraAdminUserKey = jiraAdminUserKey;
        return this;
    }

    EventData setJiraIssueCreatorUserKey(final String jiraIssueCreatorUserKey) {
        this.jiraIssueCreatorUserKey = jiraIssueCreatorUserKey;
        return this;
    }

    EventData setJiraIssueAssigneeUserId(final String jiraIssueAssigneeUserId) {
        this.jiraIssueAssigneeUserId = jiraIssueAssigneeUserId;
        return this;
    }

    EventData setJiraIssueTypeId(final String jiraIssueTypeId) {
        this.jiraIssueTypeId = jiraIssueTypeId;
        return this;
    }

    EventData setJiraProjectName(final String jiraProjectName) {
        this.jiraProjectName = jiraProjectName;
        return this;
    }

    EventData setJiraProjectId(final Long jiraProjectId) {
        this.jiraProjectId = jiraProjectId;
        return this;
    }

    EventData setJiraFieldCopyMappings(final Set<ProjectFieldCopyMapping> jiraFieldCopyMappings) {
        this.jiraFieldCopyMappings = jiraFieldCopyMappings;
        return this;
    }

    EventData setBlackDuckBaseUrl(final String blackDuckBaseUrl) {
        this.blackDuckBaseUrl = blackDuckBaseUrl;
        return this;
    }

    EventData setBlackDuckProjectName(final String blackDuckProjectName) {
        this.blackDuckProjectName = blackDuckProjectName;
        return this;
    }

    EventData setBlackDuckProjectVersion(final String blackDuckProjectVersion) {
        this.blackDuckProjectVersion = blackDuckProjectVersion;
        return this;
    }

    EventData setBlackDuckProjectVersionUrl(final String blackDuckProjectVersionUrl) {
        this.blackDuckProjectVersionUrl = blackDuckProjectVersionUrl;
        return this;
    }

    EventData setBlackDuckComponentName(final String blackDuckComponentName) {
        this.blackDuckComponentName = blackDuckComponentName;
        return this;
    }

    EventData setBlackDuckComponentUrl(final String blackDuckComponentUrl) {
        this.blackDuckComponentUrl = blackDuckComponentUrl;
        return this;
    }

    EventData setBlackDuckComponentVersion(final String blackDuckComponentVersion) {
        this.blackDuckComponentVersion = blackDuckComponentVersion;
        return this;
    }

    EventData setBlackDuckComponentVersionUrl(final String blackDuckComponentVersionUrl) {
        this.blackDuckComponentVersionUrl = blackDuckComponentVersionUrl;
        return this;
    }

    EventData setJiraIssueSummary(final String jiraIssueSummary) {
        this.jiraIssueSummary = jiraIssueSummary;
        return this;
    }

    EventData setJiraIssueDescription(final String jiraIssueDescription) {
        this.jiraIssueDescription = jiraIssueDescription;
        return this;
    }

    EventData setJiraIssueComment(final String jiraIssueComment) {
        this.jiraIssueComment = jiraIssueComment;
        return this;
    }

    EventData setJiraIssueReOpenComment(final String jiraIssueReOpenComment) {
        this.jiraIssueReOpenComment = jiraIssueReOpenComment;
        return this;
    }

    EventData setJiraIssueCommentForExistingIssue(final String jiraIssueCommentForExistingIssue) {
        this.jiraIssueCommentForExistingIssue = jiraIssueCommentForExistingIssue;
        return this;
    }

    EventData setJiraIssueResolveComment(final String jiraIssueResolveComment) {
        this.jiraIssueResolveComment = jiraIssueResolveComment;
        return this;
    }

    EventData setJiraIssueCommentInLieuOfStateChange(final String jiraIssueCommentInLieuOfStateChange) {
        this.jiraIssueCommentInLieuOfStateChange = jiraIssueCommentInLieuOfStateChange;
        return this;
    }

    EventData setJiraIssuePropertiesGenerator(final IssuePropertiesGenerator jiraIssuePropertiesGenerator) {
        this.jiraIssuePropertiesGenerator = jiraIssuePropertiesGenerator;
        return this;
    }

    EventData setBlackDuckRuleName(final String blackDuckRuleName) {
        this.blackDuckRuleName = blackDuckRuleName;
        return this;
    }

    EventData setBlackDuckRuleOverridable(final String blackDuckRuleOverridable) {
        this.blackDuckRuleOverridable = blackDuckRuleOverridable;
        return this;
    }

    EventData setBlackDuckRuleDescription(final String blackDuckRuleDescription) {
        this.blackDuckRuleDescription = blackDuckRuleDescription;
        return this;
    }

    EventData setBlackDuckRuleUrl(final String blackDuckRuleUrl) {
        this.blackDuckRuleUrl = blackDuckRuleUrl;
        return this;
    }

    EventData setBlackDuckLicenseNames(final String blackDuckLicenseNames) {
        this.blackDuckLicenseNames = blackDuckLicenseNames;
        return this;
    }

    EventData setBlackDuckLicenseUrl(final String blackDuckLicenseUrl) {
        this.blackDuckLicenseUrl = blackDuckLicenseUrl;
        return this;
    }

    EventData setBlackDuckComponentUsage(final String blackDuckComponentUsage) {
        this.blackDuckComponentUsage = blackDuckComponentUsage;
        return this;
    }

    EventData setBlackDuckComponentOrigin(final String blackDuckComponentOrigin) {
        this.blackDuckComponentOrigin = blackDuckComponentOrigin;
        return this;
    }

    EventData setBlackDuckComponentOriginId(final String blackDuckComponentOriginId) {
        this.blackDuckComponentOriginId = blackDuckComponentOriginId;
        return this;
    }

    EventData setBlackDuckProjectVersionNickname(final String blackDuckProjectVersionNickname) {
        this.blackDuckProjectVersionNickname = blackDuckProjectVersionNickname;
        return this;
    }

    EventData setComponentIssueUrl(final String componentIssueUrl) {
        this.componentIssueUrl = componentIssueUrl;
        return this;
    }

    EventData setBlackDuckProjectOwner(final ApplicationUser blackDuckProjectOwner) {
        this.blackDuckProjectOwner = blackDuckProjectOwner;
        return this;
    }

    EventData setBlackDuckProjectVersionLastUpdated(final String blackDuckProjectVersionLastUpdated) {
        this.blackDuckProjectVersionLastUpdated = blackDuckProjectVersionLastUpdated;
        return this;
    }

    public EventData setNotificationType(final NotificationType notificationType) {
        this.notificationType = notificationType;
        return this;
    }

    public EventData setEventKey(final String eventKey) {
        this.eventKey = eventKey;
        return this;
    }

    public BlackDuckEventAction getAction() {
        return action;
    }

    public Date getLastBatchStartDate() {
        return lastBatchStartDate;
    }

    public String getJiraAdminUsername() {
        return jiraAdminUsername;
    }

    public String getJiraIssueCreatorUsername() {
        return jiraIssueCreatorUsername;
    }

    public String getJiraAdminUserKey() {
        return jiraAdminUserKey;
    }

    public String getJiraIssueCreatorUserKey() {
        return jiraIssueCreatorUserKey;
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

    public String getBlackDuckBaseUrl() {
        return blackDuckBaseUrl;
    }

    public String getBlackDuckProjectName() {
        return blackDuckProjectName;
    }

    public String getBlackDuckProjectVersion() {
        return blackDuckProjectVersion;
    }

    public String getBlackDuckProjectVersionUrl() {
        return blackDuckProjectVersionUrl;
    }

    public String getBlackDuckComponentName() {
        return blackDuckComponentName;
    }

    public String getBlackDuckComponentUrl() {
        return blackDuckComponentUrl;
    }

    public String getBlackDuckComponentVersion() {
        return blackDuckComponentVersion;
    }

    public String getBlackDuckComponentVersionUrl() {
        return blackDuckComponentVersionUrl;
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

    public String getBlackDuckRuleName() {
        return blackDuckRuleName;
    }

    public String getBlackDuckRuleOverridable() {
        return blackDuckRuleOverridable;
    }

    public String getBlackDuckRuleDescription() {
        return blackDuckRuleDescription;
    }

    public String getBlackDuckRuleUrl() {
        return blackDuckRuleUrl;
    }

    public String getBlackDuckLicenseNames() {
        return blackDuckLicenseNames;
    }

    public String getBlackDuckLicenseUrl() {
        return blackDuckLicenseUrl;
    }

    public String getBlackDuckComponentUsage() {
        return blackDuckComponentUsage;
    }

    public String getBlackDuckComponentOrigin() {
        return blackDuckComponentOrigin;
    }

    public String getBlackDuckComponentOriginId() {
        return blackDuckComponentOriginId;
    }

    public String getBlackDuckProjectVersionNickname() {
        return blackDuckProjectVersionNickname;
    }

    public String getComponentIssueUrl() {
        return componentIssueUrl;
    }

    public ApplicationUser getBlackDuckProjectOwner() {
        return blackDuckProjectOwner;
    }

    public String getBlackDuckProjectVersionLastUpdated() {
        return blackDuckProjectVersionLastUpdated;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public String getEventKey() {
        return eventKey;
    }

}
