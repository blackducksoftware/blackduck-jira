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
package com.blackducksoftware.integration.jira.task.conversion.output.eventdata;

import java.util.Date;
import java.util.Set;

import com.atlassian.jira.user.ApplicationUser;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.jira.config.model.ProjectFieldCopyMapping;
import com.blackducksoftware.integration.jira.task.conversion.output.HubEventAction;
import com.blackducksoftware.integration.jira.task.conversion.output.IssuePropertiesGenerator;
import com.blackducksoftware.integration.util.Stringable;

public class EventData extends Stringable {
    private HubEventAction action;
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
    private String hubBaseUrl;
    private String hubProjectName;
    private String hubProjectVersion;
    private String hubProjectVersionUrl;
    private String hubComponentName;
    private String hubComponentUrl;
    private String hubComponentVersion;
    private String hubComponentVersionUrl;
    private String hubLicenseNames;
    private String hubLicenseUrl;
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
    private String hubRuleOverridable;
    private String hubRuleDescription;
    private String hubRuleUrl;
    private String componentIssueUrl;
    private ApplicationUser hubProjectOwner;
    private String hubProjectVersionLastUpdated;
    private NotificationType notificationType;
    private String eventKey;

    // The constructor and setters are only for EventDataBuilder
    EventData() {
    }

    EventData setAction(final HubEventAction action) {
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

    EventData setHubBaseUrl(final String hubBaseUrl) {
        this.hubBaseUrl = hubBaseUrl;
        return this;
    }

    EventData setHubProjectName(final String hubProjectName) {
        this.hubProjectName = hubProjectName;
        return this;
    }

    EventData setHubProjectVersion(final String hubProjectVersion) {
        this.hubProjectVersion = hubProjectVersion;
        return this;
    }

    EventData setHubProjectVersionUrl(final String hubProjectVersionUrl) {
        this.hubProjectVersionUrl = hubProjectVersionUrl;
        return this;
    }

    EventData setHubComponentName(final String hubComponentName) {
        this.hubComponentName = hubComponentName;
        return this;
    }

    EventData setHubComponentUrl(final String hubComponentUrl) {
        this.hubComponentUrl = hubComponentUrl;
        return this;
    }

    EventData setHubComponentVersion(final String hubComponentVersion) {
        this.hubComponentVersion = hubComponentVersion;
        return this;
    }

    EventData setHubComponentVersionUrl(final String hubComponentVersionUrl) {
        this.hubComponentVersionUrl = hubComponentVersionUrl;
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

    EventData setHubRuleName(final String hubRuleName) {
        this.hubRuleName = hubRuleName;
        return this;
    }

    EventData setHubRuleOverridable(final String hubRuleOverridable) {
        this.hubRuleOverridable = hubRuleOverridable;
        return this;
    }

    EventData setHubRuleDescription(final String hubRuleDescription) {
        this.hubRuleDescription = hubRuleDescription;
        return this;
    }

    EventData setHubRuleUrl(final String hubRuleUrl) {
        this.hubRuleUrl = hubRuleUrl;
        return this;
    }

    EventData setHubLicenseNames(final String hubLicenseNames) {
        this.hubLicenseNames = hubLicenseNames;
        return this;
    }

    EventData setHubLicenseUrl(final String hubLicenseUrl) {
        this.hubLicenseUrl = hubLicenseUrl;
        return this;
    }

    EventData setHubComponentUsage(final String hubComponentUsage) {
        this.hubComponentUsage = hubComponentUsage;
        return this;
    }

    EventData setHubComponentOrigin(final String hubComponentOrigin) {
        this.hubComponentOrigin = hubComponentOrigin;
        return this;
    }

    EventData setHubComponentOriginId(final String hubComponentOriginId) {
        this.hubComponentOriginId = hubComponentOriginId;
        return this;
    }

    EventData setHubProjectVersionNickname(final String hubProjectVersionNickname) {
        this.hubProjectVersionNickname = hubProjectVersionNickname;
        return this;
    }

    EventData setComponentIssueUrl(final String componentIssueUrl) {
        this.componentIssueUrl = componentIssueUrl;
        return this;
    }

    EventData setHubProjectOwner(final ApplicationUser hubProjectOwner) {
        this.hubProjectOwner = hubProjectOwner;
        return this;
    }

    EventData setHubProjectVersionLastUpdated(final String hubProjectVersionLastUpdated) {
        this.hubProjectVersionLastUpdated = hubProjectVersionLastUpdated;
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

    public HubEventAction getAction() {
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

    public String getHubBaseUrl() {
        return hubBaseUrl;
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

    public String getHubRuleOverridable() {
        return hubRuleOverridable;
    }

    public String getHubRuleDescription() {
        return hubRuleDescription;
    }

    public String getHubRuleUrl() {
        return hubRuleUrl;
    }

    public String getHubLicenseNames() {
        return hubLicenseNames;
    }

    public String getHubLicenseUrl() {
        return hubLicenseUrl;
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

    public String getComponentIssueUrl() {
        return componentIssueUrl;
    }

    public ApplicationUser getHubProjectOwner() {
        return hubProjectOwner;
    }

    public String getHubProjectVersionLastUpdated() {
        return hubProjectVersionLastUpdated;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public String getEventKey() {
        return eventKey;
    }

}
