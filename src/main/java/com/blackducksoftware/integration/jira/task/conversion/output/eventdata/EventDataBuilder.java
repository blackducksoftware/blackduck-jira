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
import com.blackducksoftware.integration.hub.notification.content.detail.NotificationContentDetail;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.common.JiraUserContext;
import com.blackducksoftware.integration.jira.common.exception.EventDataBuilderException;
import com.blackducksoftware.integration.jira.common.model.JiraProject;
import com.blackducksoftware.integration.jira.config.model.ProjectFieldCopyMapping;
import com.blackducksoftware.integration.jira.task.conversion.output.HubEventAction;
import com.blackducksoftware.integration.jira.task.conversion.output.IssuePropertiesGenerator;
import com.blackducksoftware.integration.util.Stringable;

public class EventDataBuilder extends Stringable {
    private final EventCategory eventCategory;

    private HubEventAction action;
    private Date lastBatchStartDate;
    private String jiraAdminUserName;
    private String jiraIssueCreatorUserName;
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

    public EventDataBuilder(final EventCategory eventCategory) {
        this.eventCategory = eventCategory;
    }

    public EventCategory getEventCategory() {
        return eventCategory;
    }

    public EventDataBuilder setPropertiesFromJiraContext(final JiraUserContext jiraContext) {
        setJiraAdminUserName(jiraContext.getJiraAdminUser().getName());
        setJiraAdminUserKey(jiraContext.getJiraAdminUser().getKey());
        setJiraIssueCreatorUserName(jiraContext.getJiraIssueCreatorUser().getName());
        setJiraIssueCreatorUserKey(jiraContext.getJiraIssueCreatorUser().getKey());
        return this;
    }

    public EventDataBuilder setPropertiesFromJiraProject(final JiraProject jiraProject) {
        setJiraIssueAssigneeUserId(jiraProject.getAssigneeUserId());
        setJiraProjectName(jiraProject.getProjectName());
        setJiraProjectId(jiraProject.getProjectId());
        return this;
    }

    public EventDataBuilder setPropertiesFromNotificationContentDetail(final NotificationContentDetail detail) {
        if (detail.getProjectName().isPresent()) {
            setHubProjectName(detail.getProjectName().get());
        }
        if (detail.getProjectVersionName().isPresent()) {
            setHubProjectVersion(detail.getProjectVersionName().get());
        }
        if (detail.getProjectVersion().isPresent()) {
            setHubProjectVersionUrl(detail.getProjectVersion().get().uri);
        }
        if (detail.getComponentName().isPresent()) {
            setHubComponentName(detail.getComponentName().get());
        }
        if (detail.getComponent().isPresent()) {
            setHubComponentUrl(detail.getComponent().get().uri);
        }
        if (detail.getComponentVersionName().isPresent()) {
            setHubComponentVersion(detail.getComponentVersionName().get());
        }
        if (detail.getComponentVersion().isPresent()) {
            setHubComponentVersionUrl(detail.getComponentVersion().get().uri);
        }
        if (detail.getComponentIssue().isPresent()) {
            setComponentIssueUrl(detail.getComponentIssue().get().uri);
        }
        if (detail.getComponentVersionOriginName().isPresent()) {
            setHubComponentOrigin(detail.getComponentVersionOriginName().get());
        }
        if (detail.getComponentVersionOriginId().isPresent()) {
            setHubComponentOriginId(detail.getComponentVersionOriginId().get());
        }
        return this;
    }

    public EventDataBuilder setPolicyIssueCommentPropertiesFromNotificationType(final NotificationType notificationType) {
        if (NotificationType.POLICY_OVERRIDE.equals(notificationType)) {
            setJiraIssueReOpenComment(HubJiraConstants.HUB_POLICY_VIOLATION_REOPEN);
            setJiraIssueCommentForExistingIssue(HubJiraConstants.HUB_POLICY_VIOLATION_OVERRIDDEN_COMMENT);
            setJiraIssueResolveComment(HubJiraConstants.HUB_POLICY_VIOLATION_RESOLVE);
            setJiraIssueCommentInLieuOfStateChange(HubJiraConstants.HUB_POLICY_VIOLATION_OVERRIDDEN_COMMENT);
        } else if (NotificationType.RULE_VIOLATION.equals(notificationType)) {
            setJiraIssueReOpenComment(HubJiraConstants.HUB_POLICY_VIOLATION_REOPEN);
            setJiraIssueCommentForExistingIssue(HubJiraConstants.HUB_POLICY_VIOLATION_DETECTED_AGAIN_COMMENT);
            setJiraIssueResolveComment(HubJiraConstants.HUB_POLICY_VIOLATION_RESOLVE);
            setJiraIssueCommentInLieuOfStateChange(HubJiraConstants.HUB_POLICY_VIOLATION_DETECTED_AGAIN_COMMENT);
        } else if (NotificationType.RULE_VIOLATION_CLEARED.equals(notificationType)) {
            setJiraIssueReOpenComment(HubJiraConstants.HUB_POLICY_VIOLATION_REOPEN);
            setJiraIssueCommentForExistingIssue(HubJiraConstants.HUB_POLICY_VIOLATION_CLEARED_COMMENT);
            setJiraIssueResolveComment(HubJiraConstants.HUB_POLICY_VIOLATION_CLEARED_RESOLVE);
            setJiraIssueCommentInLieuOfStateChange(HubJiraConstants.HUB_POLICY_VIOLATION_CLEARED_COMMENT);
        } else {

        }
        return this;
    }

    public EventDataBuilder setVulnerabilityIssueCommentProperties(final String comment) {
        setJiraIssueComment(comment);
        setJiraIssueCommentForExistingIssue(comment);
        setJiraIssueReOpenComment(HubJiraConstants.HUB_VULNERABILITY_REOPEN);
        setJiraIssueResolveComment(HubJiraConstants.HUB_VULNERABILITY_RESOLVE);
        setJiraIssueCommentInLieuOfStateChange(comment);
        return this;
    }

    public EventDataBuilder setAction(final HubEventAction action) {
        this.action = action;
        return this;
    }

    public EventDataBuilder setLastBatchStartDate(final Date lastBatchStartDate) {
        this.lastBatchStartDate = lastBatchStartDate;
        return this;
    }

    public EventDataBuilder setJiraAdminUserName(final String jiraAdminUserName) {
        this.jiraAdminUserName = jiraAdminUserName;
        return this;
    }

    public EventDataBuilder setJiraIssueCreatorUserName(final String jiraIssueCreatorUserName) {
        this.jiraIssueCreatorUserName = jiraIssueCreatorUserName;
        return this;
    }

    public EventDataBuilder setJiraAdminUserKey(final String jiraAdminUserKey) {
        this.jiraAdminUserKey = jiraAdminUserKey;
        return this;
    }

    public EventDataBuilder setJiraIssueCreatorUserKey(final String jiraIssueCreatorUserKey) {
        this.jiraIssueCreatorUserKey = jiraIssueCreatorUserKey;
        return this;
    }

    public EventDataBuilder setJiraIssueAssigneeUserId(final String jiraIssueAssigneeUserId) {
        this.jiraIssueAssigneeUserId = jiraIssueAssigneeUserId;
        return this;
    }

    public EventDataBuilder setJiraIssueTypeId(final String jiraIssueTypeId) {
        this.jiraIssueTypeId = jiraIssueTypeId;
        return this;
    }

    public EventDataBuilder setJiraProjectName(final String jiraProjectName) {
        this.jiraProjectName = jiraProjectName;
        return this;
    }

    public EventDataBuilder setJiraProjectId(final Long jiraProjectId) {
        this.jiraProjectId = jiraProjectId;
        return this;
    }

    public EventDataBuilder setJiraFieldCopyMappings(final Set<ProjectFieldCopyMapping> jiraFieldCopyMappings) {
        this.jiraFieldCopyMappings = jiraFieldCopyMappings;
        return this;
    }

    public EventDataBuilder setHubBaseUrl(final String hubBaseUrl) {
        this.hubBaseUrl = hubBaseUrl;
        return this;
    }

    public EventDataBuilder setHubProjectName(final String hubProjectName) {
        this.hubProjectName = hubProjectName;
        return this;
    }

    public EventDataBuilder setHubProjectVersion(final String hubProjectVersion) {
        this.hubProjectVersion = hubProjectVersion;
        return this;
    }

    public EventDataBuilder setHubProjectVersionUrl(final String hubProjectVersionUrl) {
        this.hubProjectVersionUrl = hubProjectVersionUrl;
        return this;
    }

    public EventDataBuilder setHubComponentName(final String hubComponentName) {
        this.hubComponentName = hubComponentName;
        return this;
    }

    public EventDataBuilder setHubComponentUrl(final String hubComponentUrl) {
        this.hubComponentUrl = hubComponentUrl;
        return this;
    }

    public EventDataBuilder setHubComponentVersion(final String hubComponentVersion) {
        this.hubComponentVersion = hubComponentVersion;
        return this;
    }

    public EventDataBuilder setHubComponentVersionUrl(final String hubComponentVersionUrl) {
        this.hubComponentVersionUrl = hubComponentVersionUrl;
        return this;
    }

    public EventDataBuilder setJiraIssueSummary(final String jiraIssueSummary) {
        this.jiraIssueSummary = jiraIssueSummary;
        return this;
    }

    public EventDataBuilder setJiraIssueDescription(final String jiraIssueDescription) {
        this.jiraIssueDescription = jiraIssueDescription;
        return this;
    }

    public EventDataBuilder setJiraIssueComment(final String jiraIssueComment) {
        this.jiraIssueComment = jiraIssueComment;
        return this;
    }

    public EventDataBuilder setJiraIssueReOpenComment(final String jiraIssueReOpenComment) {
        this.jiraIssueReOpenComment = jiraIssueReOpenComment;
        return this;
    }

    public EventDataBuilder setJiraIssueCommentForExistingIssue(final String jiraIssueCommentForExistingIssue) {
        this.jiraIssueCommentForExistingIssue = jiraIssueCommentForExistingIssue;
        return this;
    }

    public EventDataBuilder setJiraIssueResolveComment(final String jiraIssueResolveComment) {
        this.jiraIssueResolveComment = jiraIssueResolveComment;
        return this;
    }

    public EventDataBuilder setJiraIssueCommentInLieuOfStateChange(final String jiraIssueCommentInLieuOfStateChange) {
        this.jiraIssueCommentInLieuOfStateChange = jiraIssueCommentInLieuOfStateChange;
        return this;
    }

    public EventDataBuilder setJiraIssuePropertiesGenerator(final IssuePropertiesGenerator jiraIssuePropertiesGenerator) {
        this.jiraIssuePropertiesGenerator = jiraIssuePropertiesGenerator;
        return this;
    }

    public EventDataBuilder setHubRuleOverridable(final Boolean hubRuleOverridable) {
        this.hubRuleOverridable = hubRuleOverridable != null ? hubRuleOverridable.toString() : "unknown";
        return this;
    }

    public EventDataBuilder setHubRuleName(final String hubRuleName) {
        this.hubRuleName = hubRuleName;
        return this;
    }

    public EventDataBuilder setHubRuleDescription(final String hubRuleDescription) {
        this.hubRuleDescription = hubRuleDescription != null ? hubRuleDescription : "No description.";
        return this;
    }

    public EventDataBuilder setHubRuleUrl(final String hubRuleUrl) {
        this.hubRuleUrl = hubRuleUrl;
        return this;
    }

    public EventDataBuilder setHubLicenseNames(final String hubLicenseNames) {
        this.hubLicenseNames = hubLicenseNames;
        return this;
    }

    public EventDataBuilder setHubLicenseUrl(final String hubLicenseUrl) {
        this.hubLicenseUrl = hubLicenseUrl;
        return this;
    }

    public EventDataBuilder setHubComponentUsage(final String hubComponentUsage) {
        this.hubComponentUsage = hubComponentUsage;
        return this;
    }

    public EventDataBuilder setHubComponentOrigin(final String hubComponentOrigin) {
        this.hubComponentOrigin = hubComponentOrigin;
        return this;
    }

    public EventDataBuilder setHubComponentOriginId(final String hubComponentOriginId) {
        this.hubComponentOriginId = hubComponentOriginId;
        return this;
    }

    public EventDataBuilder setHubProjectVersionNickname(final String hubProjectVersionNickname) {
        this.hubProjectVersionNickname = hubProjectVersionNickname;
        return this;
    }

    public EventDataBuilder setComponentIssueUrl(final String componentIssueUrl) {
        this.componentIssueUrl = componentIssueUrl;
        return this;
    }

    public EventDataBuilder setHubProjectOwner(final ApplicationUser hubProjectOwner) {
        this.hubProjectOwner = hubProjectOwner;
        return this;
    }

    public EventDataBuilder setHubProjectVersionLastUpdated(final String hubProjectVersionLastUpdated) {
        this.hubProjectVersionLastUpdated = hubProjectVersionLastUpdated;
        return this;
    }

    public EventDataBuilder setNotificationType(final NotificationType notificationType) {
        this.notificationType = notificationType;
        return this;
    }

    public EventDataBuilder setEventKey(final String eventKey) {
        this.eventKey = eventKey;
        return this;
    }

    public Long getJiraProjectId() {
        return jiraProjectId;
    }

    public String getHubProjectVersionUrl() {
        return hubProjectVersionUrl;
    }

    public String getHubComponentVersionUrl() {
        return hubComponentVersionUrl;
    }

    public String getHubComponentUrl() {
        return hubComponentUrl;
    }

    public String getHubRuleUrl() {
        return hubRuleUrl;
    }

    public EventData build() throws EventDataBuilderException {
        if (jiraAdminUserName == null) {
            throw new EventDataBuilderException("jiraAdminUserName not set");
        }

        if (jiraAdminUserKey == null) {
            throw new EventDataBuilderException("jiraAdminUserKey not set");
        }

        if (jiraIssueCreatorUserName == null) {
            throw new EventDataBuilderException("jiraIssueCreatorUserName not set");
        }

        if (jiraIssueCreatorUserKey == null) {
            throw new EventDataBuilderException("jiraIssueCreatorUserKey not set");
        }

        if (jiraIssueTypeId == null) {
            throw new EventDataBuilderException("jiraIssueTypeId not set");
        }

        if (jiraProjectName == null) {
            throw new EventDataBuilderException("jiraProjectName not set");
        }

        if (jiraProjectId == null) {
            throw new EventDataBuilderException("jiraProjectId not set");
        }

        if (jiraFieldCopyMappings == null) {
            throw new EventDataBuilderException("jiraFieldCopyMappings not set");
        }

        if (hubProjectName == null) {
            throw new EventDataBuilderException("hubProjectName not set");
        }

        if (hubProjectVersion == null) {
            throw new EventDataBuilderException("hubProjectVersion not set");
        }

        if (hubProjectVersionUrl == null) {
            throw new EventDataBuilderException("hubProjectVersionUrl not set");
        }

        if (hubComponentName == null) {
            throw new EventDataBuilderException("hubComponentName not set");
        }

        if (jiraIssueSummary == null) {
            throw new EventDataBuilderException("jiraIssueSummary not set");
        } else if (jiraIssueSummary.length() > 255) {
            // a jira summary can be at most 255 characters
            jiraIssueSummary = jiraIssueSummary.substring(0, 252) + "...";
        }

        if (jiraIssueDescription == null) {
            throw new EventDataBuilderException("jiraIssueDescription not set");
        }

        if (jiraIssueReOpenComment == null) {
            throw new EventDataBuilderException("jiraIssueReOpenComment not set");
        }

        if (jiraIssueCommentForExistingIssue == null) {
            throw new EventDataBuilderException("jiraIssueCommentForExistingIssue not set");
        }

        if (jiraIssueResolveComment == null) {
            throw new EventDataBuilderException("jiraIssueResolveComment not set");
        }

        if (jiraIssueCommentInLieuOfStateChange == null) {
            throw new EventDataBuilderException("jiraIssueCommentInLieuOfStateChange not set");
        }

        if (jiraIssuePropertiesGenerator == null) {
            throw new EventDataBuilderException("jiraIssuePropertiesGenerator not set");
        }

        if (this.eventCategory == EventCategory.POLICY) {
            if (hubRuleName == null) {
                throw new EventDataBuilderException("hubRuleName not set");
            }
            if (hubRuleUrl == null) {
                throw new EventDataBuilderException("hubRuleUrl not set");
            }
        }

        final EventData eventData = new EventData();
        eventData.setAction(action)
                .setLastBatchStartDate(lastBatchStartDate)
                .setJiraAdminUsername(jiraAdminUserName)
                .setJiraAdminUserKey(jiraAdminUserKey)
                .setJiraIssueCreatorUsername(jiraIssueCreatorUserName)
                .setJiraIssueCreatorUserKey(jiraIssueCreatorUserKey)
                .setJiraIssueAssigneeUserId(jiraIssueAssigneeUserId)
                .setJiraIssueTypeId(jiraIssueTypeId)
                .setJiraProjectName(jiraProjectName)
                .setJiraProjectId(jiraProjectId)
                .setJiraFieldCopyMappings(jiraFieldCopyMappings)
                .setHubBaseUrl(hubBaseUrl)
                .setHubProjectName(hubProjectName)
                .setHubProjectVersion(hubProjectVersion)
                .setHubProjectVersionUrl(hubProjectVersionUrl)
                .setHubComponentName(hubComponentName)
                .setHubComponentUrl(hubComponentUrl)
                .setHubComponentVersion(hubComponentVersion)
                .setHubComponentVersionUrl(hubComponentVersionUrl)
                .setHubLicenseNames(hubLicenseNames)
                .setHubLicenseUrl(hubLicenseUrl)
                .setHubComponentUsage(hubComponentUsage)
                .setHubComponentOrigin(hubComponentOrigin)
                .setHubComponentOriginId(hubComponentOriginId)
                .setHubProjectVersionNickname(hubProjectVersionNickname)
                .setJiraIssueSummary(jiraIssueSummary)
                .setJiraIssueDescription(jiraIssueDescription)
                .setJiraIssueComment(jiraIssueComment)
                .setJiraIssueReOpenComment(jiraIssueReOpenComment)
                .setJiraIssueCommentForExistingIssue(jiraIssueCommentForExistingIssue)
                .setJiraIssueResolveComment(jiraIssueResolveComment)
                .setJiraIssueCommentInLieuOfStateChange(jiraIssueCommentInLieuOfStateChange)
                .setJiraIssuePropertiesGenerator(jiraIssuePropertiesGenerator)
                .setHubRuleName(hubRuleName)
                .setHubRuleOverridable(hubRuleOverridable)
                .setHubRuleDescription(hubRuleDescription)
                .setHubRuleUrl(hubRuleUrl)
                .setComponentIssueUrl(componentIssueUrl)
                .setHubProjectOwner(hubProjectOwner)
                .setHubProjectVersionLastUpdated(hubProjectVersionLastUpdated)
                .setNotificationType(notificationType)
                .setEventKey(eventKey);
        return eventData;
    }
}
