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
import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;
import com.blackducksoftware.integration.jira.common.JiraUserContext;
import com.blackducksoftware.integration.jira.common.exception.EventDataBuilderException;
import com.blackducksoftware.integration.jira.common.model.JiraProject;
import com.blackducksoftware.integration.jira.config.model.ProjectFieldCopyMapping;
import com.blackducksoftware.integration.jira.task.conversion.output.BlackDuckEventAction;
import com.blackducksoftware.integration.jira.task.conversion.output.IssuePropertiesGenerator;
import com.blackducksoftware.integration.util.Stringable;

public class EventDataBuilder extends Stringable {
    private final EventCategory eventCategory;

    private BlackDuckEventAction action;
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

    public EventDataBuilder(final EventCategory eventCategory) {
        this.eventCategory = eventCategory;
    }

    public EventCategory getEventCategory() {
        return eventCategory;
    }

    public EventDataBuilder setPropertiesFromJiraUserContext(final JiraUserContext jiraUserContext) {
        setJiraAdminUserName(jiraUserContext.getJiraAdminUser().getName());
        setJiraAdminUserKey(jiraUserContext.getJiraAdminUser().getKey());
        setJiraIssueCreatorUserName(jiraUserContext.getJiraIssueCreatorUser().getName());
        setJiraIssueCreatorUserKey(jiraUserContext.getJiraIssueCreatorUser().getKey());
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
            setBlackDuckProjectName(detail.getProjectName().get());
        }
        if (detail.getProjectVersionName().isPresent()) {
            setBlackDuckProjectVersion(detail.getProjectVersionName().get());
        }
        if (detail.getProjectVersion().isPresent()) {
            setBlackDuckProjectVersionUrl(detail.getProjectVersion().get().uri);
        }
        if (detail.getComponentName().isPresent()) {
            setBlackDuckComponentName(detail.getComponentName().get());
        }
        if (detail.getComponent().isPresent()) {
            setBlackDuckComponentUrl(detail.getComponent().get().uri);
        }
        if (detail.getComponentVersionName().isPresent()) {
            setBlackDuckComponentVersion(detail.getComponentVersionName().get());
        }
        if (detail.getComponentVersion().isPresent()) {
            setBlackDuckComponentVersionUrl(detail.getComponentVersion().get().uri);
        }
        if (detail.getComponentIssue().isPresent()) {
            setComponentIssueUrl(detail.getComponentIssue().get().uri);
        }
        if (detail.getComponentVersionOriginName().isPresent()) {
            setBlackDuckComponentOrigin(detail.getComponentVersionOriginName().get());
        }
        if (detail.getComponentVersionOriginId().isPresent()) {
            setBlackDuckComponentOriginId(detail.getComponentVersionOriginId().get());
        }
        return this;
    }

    public EventDataBuilder setPolicyIssueCommentPropertiesFromNotificationType(final NotificationType notificationType) {
        if (NotificationType.POLICY_OVERRIDE.equals(notificationType)) {
            setJiraIssueReOpenComment(BlackDuckJiraConstants.BLACK_DUCK_POLICY_VIOLATION_REOPEN);
            setJiraIssueCommentForExistingIssue(BlackDuckJiraConstants.BLACK_DUCK_POLICY_VIOLATION_OVERRIDDEN_COMMENT);
            setJiraIssueResolveComment(BlackDuckJiraConstants.BLACK_DUCK_POLICY_VIOLATION_RESOLVE);
            setJiraIssueCommentInLieuOfStateChange(BlackDuckJiraConstants.BLACK_DUCK_POLICY_VIOLATION_OVERRIDDEN_COMMENT);
        } else if (NotificationType.RULE_VIOLATION.equals(notificationType)) {
            setJiraIssueReOpenComment(BlackDuckJiraConstants.BLACK_DUCK_POLICY_VIOLATION_REOPEN);
            setJiraIssueCommentForExistingIssue(BlackDuckJiraConstants.BLACK_DUCK_POLICY_VIOLATION_DETECTED_AGAIN_COMMENT);
            setJiraIssueResolveComment(BlackDuckJiraConstants.BLACK_DUCK_POLICY_VIOLATION_RESOLVE);
            setJiraIssueCommentInLieuOfStateChange(BlackDuckJiraConstants.BLACK_DUCK_POLICY_VIOLATION_DETECTED_AGAIN_COMMENT);
        } else if (NotificationType.RULE_VIOLATION_CLEARED.equals(notificationType)) {
            setJiraIssueReOpenComment(BlackDuckJiraConstants.BLACK_DUCK_POLICY_VIOLATION_REOPEN);
            setJiraIssueCommentForExistingIssue(BlackDuckJiraConstants.BLACK_DUCK_POLICY_VIOLATION_CLEARED_COMMENT);
            setJiraIssueResolveComment(BlackDuckJiraConstants.BLACK_DUCK_POLICY_VIOLATION_CLEARED_RESOLVE);
            setJiraIssueCommentInLieuOfStateChange(BlackDuckJiraConstants.BLACK_DUCK_POLICY_VIOLATION_CLEARED_COMMENT);
        } else {

        }
        return this;
    }

    public EventDataBuilder setVulnerabilityIssueCommentProperties(final String comment) {
        setJiraIssueComment(comment);
        setJiraIssueCommentForExistingIssue(comment);
        setJiraIssueReOpenComment(BlackDuckJiraConstants.BLACK_DUCK_VULNERABILITY_REOPEN);
        setJiraIssueResolveComment(BlackDuckJiraConstants.BLACK_DUCK_VULNERABILITY_RESOLVE);
        setJiraIssueCommentInLieuOfStateChange(comment);
        return this;
    }

    public EventDataBuilder setAction(final BlackDuckEventAction action) {
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

    public EventDataBuilder setBlackDuckBaseUrl(final String blackDuckBaseUrl) {
        this.blackDuckBaseUrl = blackDuckBaseUrl;
        return this;
    }

    public EventDataBuilder setBlackDuckProjectName(final String blackDuckProjectName) {
        this.blackDuckProjectName = blackDuckProjectName;
        return this;
    }

    public EventDataBuilder setBlackDuckProjectVersion(final String blackDuckProjectVersion) {
        this.blackDuckProjectVersion = blackDuckProjectVersion;
        return this;
    }

    public EventDataBuilder setBlackDuckProjectVersionUrl(final String blackDuckProjectVersionUrl) {
        this.blackDuckProjectVersionUrl = blackDuckProjectVersionUrl;
        return this;
    }

    public EventDataBuilder setBlackDuckComponentName(final String blackDuckComponentName) {
        this.blackDuckComponentName = blackDuckComponentName;
        return this;
    }

    public EventDataBuilder setBlackDuckComponentUrl(final String blackDuckComponentUrl) {
        this.blackDuckComponentUrl = blackDuckComponentUrl;
        return this;
    }

    public EventDataBuilder setBlackDuckComponentVersion(final String blackDuckComponentVersion) {
        this.blackDuckComponentVersion = blackDuckComponentVersion;
        return this;
    }

    public EventDataBuilder setBlackDuckComponentVersionUrl(final String blackDuckComponentVersionUrl) {
        this.blackDuckComponentVersionUrl = blackDuckComponentVersionUrl;
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

    public EventDataBuilder setBlackDuckRuleOverridable(final Boolean blackDuckRuleOverridable) {
        this.blackDuckRuleOverridable = blackDuckRuleOverridable != null ? blackDuckRuleOverridable.toString() : "unknown";
        return this;
    }

    public EventDataBuilder setBlackDuckRuleName(final String hubblackDuckRuleName) {
        this.blackDuckRuleName = hubblackDuckRuleName;
        return this;
    }

    public EventDataBuilder setBlackDuckRuleDescription(final String blackDuckRuleDescription) {
        this.blackDuckRuleDescription = blackDuckRuleDescription != null ? blackDuckRuleDescription : "No description.";
        return this;
    }

    public EventDataBuilder setBlackDuckRuleUrl(final String blackDuckRuleUrl) {
        this.blackDuckRuleUrl = blackDuckRuleUrl;
        return this;
    }

    public EventDataBuilder setBlackDuckLicenseNames(final String blackDuckLicenseNames) {
        this.blackDuckLicenseNames = blackDuckLicenseNames;
        return this;
    }

    public EventDataBuilder setBlackDuckLicenseUrl(final String blackDuckLicenseUrl) {
        this.blackDuckLicenseUrl = blackDuckLicenseUrl;
        return this;
    }

    public EventDataBuilder setBlackDuckComponentUsage(final String blackDuckComponentUsage) {
        this.blackDuckComponentUsage = blackDuckComponentUsage;
        return this;
    }

    public EventDataBuilder setBlackDuckComponentOrigin(final String blackDuckComponentOrigin) {
        this.blackDuckComponentOrigin = blackDuckComponentOrigin;
        return this;
    }

    public EventDataBuilder setBlackDuckComponentOriginId(final String blackDuckComponentOriginId) {
        this.blackDuckComponentOriginId = blackDuckComponentOriginId;
        return this;
    }

    public EventDataBuilder setHubProjectVersionNickname(final String blackDuckProjectVersionNickname) {
        this.blackDuckProjectVersionNickname = blackDuckProjectVersionNickname;
        return this;
    }

    public EventDataBuilder setComponentIssueUrl(final String componentIssueUrl) {
        this.componentIssueUrl = componentIssueUrl;
        return this;
    }

    public EventDataBuilder setBlackDuckProjectOwner(final ApplicationUser blackDuckProjectOwner) {
        this.blackDuckProjectOwner = blackDuckProjectOwner;
        return this;
    }

    public EventDataBuilder setBlackDuckProjectVersionLastUpdated(final String blackDuckProjectVersionLastUpdated) {
        this.blackDuckProjectVersionLastUpdated = blackDuckProjectVersionLastUpdated;
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

    public String getBlackDuckProjectVersionUrl() {
        return blackDuckProjectVersionUrl;
    }

    public String getBlackDuckComponentVersionUrl() {
        return blackDuckComponentVersionUrl;
    }

    public String getBlackDuckComponentUrl() {
        return blackDuckComponentUrl;
    }

    public String getBlackDuckRuleUrl() {
        return blackDuckRuleUrl;
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

        if (blackDuckProjectName == null) {
            throw new EventDataBuilderException("blackDuckProjectName not set");
        }

        if (blackDuckProjectVersion == null) {
            throw new EventDataBuilderException("blackDuckProjectVersion not set");
        }

        if (blackDuckProjectVersionUrl == null) {
            throw new EventDataBuilderException("blackDuckProjectVersionUrl not set");
        }

        if (blackDuckComponentName == null) {
            throw new EventDataBuilderException("blackDuckComponentName not set");
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
            if (blackDuckRuleName == null) {
                throw new EventDataBuilderException("blackDuckRuleName not set");
            }
            if (blackDuckRuleUrl == null) {
                throw new EventDataBuilderException("blackDuckRuleUrl not set");
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
                .setBlackDuckBaseUrl(blackDuckBaseUrl)
                .setBlackDuckProjectName(blackDuckProjectName)
                .setBlackDuckProjectVersion(blackDuckProjectVersion)
                .setBlackDuckProjectVersionUrl(blackDuckProjectVersionUrl)
                .setBlackDuckComponentName(blackDuckComponentName)
                .setBlackDuckComponentUrl(blackDuckComponentUrl)
                .setBlackDuckComponentVersion(blackDuckComponentVersion)
                .setBlackDuckComponentVersionUrl(blackDuckComponentVersionUrl)
                .setBlackDuckLicenseNames(blackDuckLicenseNames)
                .setBlackDuckLicenseUrl(blackDuckLicenseUrl)
                .setBlackDuckComponentUsage(blackDuckComponentUsage)
                .setBlackDuckComponentOrigin(blackDuckComponentOrigin)
                .setBlackDuckComponentOriginId(blackDuckComponentOriginId)
                .setBlackDuckProjectVersionNickname(blackDuckProjectVersionNickname)
                .setJiraIssueSummary(jiraIssueSummary)
                .setJiraIssueDescription(jiraIssueDescription)
                .setJiraIssueComment(jiraIssueComment)
                .setJiraIssueReOpenComment(jiraIssueReOpenComment)
                .setJiraIssueCommentForExistingIssue(jiraIssueCommentForExistingIssue)
                .setJiraIssueResolveComment(jiraIssueResolveComment)
                .setJiraIssueCommentInLieuOfStateChange(jiraIssueCommentInLieuOfStateChange)
                .setJiraIssuePropertiesGenerator(jiraIssuePropertiesGenerator)
                .setBlackDuckRuleName(blackDuckRuleName)
                .setBlackDuckRuleOverridable(blackDuckRuleOverridable)
                .setBlackDuckRuleDescription(blackDuckRuleDescription)
                .setBlackDuckRuleUrl(blackDuckRuleUrl)
                .setComponentIssueUrl(componentIssueUrl)
                .setBlackDuckProjectOwner(blackDuckProjectOwner)
                .setBlackDuckProjectVersionLastUpdated(blackDuckProjectVersionLastUpdated)
                .setNotificationType(notificationType)
                .setEventKey(eventKey);
        return eventData;
    }
}
