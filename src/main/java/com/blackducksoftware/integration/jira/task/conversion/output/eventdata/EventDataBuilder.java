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

import java.net.URL;
import java.util.Date;
import java.util.Set;

import com.atlassian.jira.user.ApplicationUser;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;
import com.blackducksoftware.integration.jira.common.JiraUserContext;
import com.blackducksoftware.integration.jira.common.UrlParser;
import com.blackducksoftware.integration.jira.common.exception.EventDataBuilderException;
import com.blackducksoftware.integration.jira.common.model.JiraProject;
import com.blackducksoftware.integration.jira.config.model.ProjectFieldCopyMapping;
import com.blackducksoftware.integration.jira.task.conversion.output.BlackDuckEventAction;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.exception.HubIntegrationException;
import com.synopsys.integration.blackduck.notification.content.detail.NotificationContentDetail;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.util.Stringable;

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
    private String blackDuckProjectVersionName;
    private String blackDuckProjectVersionUrl;
    private String blackDuckComponentName;
    private String blackDuckComponentUrl;
    private String blackDuckComponentVersionName;
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

    private String jiraIssueDescription;
    private String jiraIssueComment;
    private String jiraIssueReOpenComment;
    private String jiraIssueCommentForExistingIssue;
    private String jiraIssueResolveComment;
    private String jiraIssueCommentInLieuOfStateChange;

    private String blackDuckProjectVersionLastUpdated;
    private NotificationType notificationType;

    public EventDataBuilder(final EventCategory eventCategory) {
        this.eventCategory = eventCategory;
    }

    public EventDataBuilder(final EventCategory eventCategory, final Date lastBatchStartDate, final JiraProject jiraProject, final JiraUserContext jiraUserContext, final String issueTypeId, final URL blackDuckBaseUrl,
            final Set<ProjectFieldCopyMapping> jiraFieldCopyMappings) {
        this.eventCategory = eventCategory;
        setLastBatchStartDate(lastBatchStartDate);
        setPropertiesFromJiraProject(jiraProject);
        setPropertiesFromJiraUserContext(jiraUserContext);
        setJiraIssueTypeId(issueTypeId);
        setBlackDuckBaseUrl(blackDuckBaseUrl.toString());
        setJiraFieldCopyMappings(jiraFieldCopyMappings);
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
            setBlackDuckProjectVersionName(detail.getProjectVersionName().get());
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
            setBlackDuckComponentVersionName(detail.getComponentVersionName().get());
        }
        if (detail.getComponentVersion().isPresent()) {
            setBlackDuckComponentVersionUrl(detail.getComponentVersion().get().uri);
        }
        if (detail.getComponentIssue().isPresent()) {
            setComponentIssueUrl(detail.getComponentIssue().get().uri);
        }
        if (detail.getComponentVersionOriginName().isPresent()) {
            setBlackDuckComponentOrigins(detail.getComponentVersionOriginName().get());
        }
        if (detail.getComponentVersionOriginId().isPresent()) {
            setBlackDuckComponentOriginId(detail.getComponentVersionOriginId().get());
        }
        return this;
    }

    public EventDataBuilder setVulnerabilityIssueCommentProperties(final String comment) {
        setJiraIssueComment(comment);
        setJiraIssueCommentForExistingIssue(comment);
        setJiraIssueReOpenComment(BlackDuckJiraConstants.BLACKDUCK_VULNERABILITY_REOPEN);
        setJiraIssueResolveComment(BlackDuckJiraConstants.BLACKDUCK_VULNERABILITY_RESOLVE);
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

    public String getBlackDuckProjectName() {
        return blackDuckProjectName;
    }

    public EventDataBuilder setBlackDuckProjectVersionName(final String blackDuckProjectVersionName) {
        this.blackDuckProjectVersionName = blackDuckProjectVersionName;
        return this;
    }

    public String getBlackDuckProjectVersionName() {
        return blackDuckProjectVersionName;
    }

    public EventDataBuilder setBlackDuckProjectVersionUrl(final String blackDuckProjectVersionUrl) {
        this.blackDuckProjectVersionUrl = blackDuckProjectVersionUrl;
        return this;
    }

    public EventDataBuilder setBlackDuckComponentName(final String blackDuckComponentName) {
        this.blackDuckComponentName = blackDuckComponentName;
        return this;
    }

    public String getBlackDuckComponentName() {
        return blackDuckComponentName;
    }

    public EventDataBuilder setBlackDuckComponentUrl(final String blackDuckComponentUrl) {
        this.blackDuckComponentUrl = blackDuckComponentUrl;
        return this;
    }

    public EventDataBuilder setBlackDuckComponentVersionName(final String blackDuckComponentVersionName) {
        this.blackDuckComponentVersionName = blackDuckComponentVersionName;
        return this;
    }

    public String getBlackDuckComponentVersionName() {
        return blackDuckComponentVersionName;
    }

    public EventDataBuilder setBlackDuckComponentVersionUrl(final String blackDuckComponentVersionUrl) {
        this.blackDuckComponentVersionUrl = blackDuckComponentVersionUrl;
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

    public EventDataBuilder setBlackDuckRuleOverridable(final Boolean blackDuckRuleOverridable) {
        this.blackDuckRuleOverridable = blackDuckRuleOverridable != null ? blackDuckRuleOverridable.toString() : "unknown";
        return this;
    }

    public EventDataBuilder setBlackDuckRuleName(final String blackDuckRuleName) {
        this.blackDuckRuleName = blackDuckRuleName;
        return this;
    }

    public String getBlackDuckRuleName() {
        return blackDuckRuleName;
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

    public EventDataBuilder setBlackDuckComponentUsages(final String blackDuckComponentUsage) {
        this.blackDuckComponentUsage = blackDuckComponentUsage;
        return this;
    }

    public EventDataBuilder setBlackDuckComponentOrigins(final String blackDuckComponentOrigin) {
        this.blackDuckComponentOrigin = blackDuckComponentOrigin;
        return this;
    }

    public EventDataBuilder setBlackDuckComponentOriginId(final String blackDuckComponentOriginId) {
        this.blackDuckComponentOriginId = blackDuckComponentOriginId;
        return this;
    }

    public EventDataBuilder setBlackDuckProjectVersionNickname(final String blackDuckProjectVersionNickname) {
        this.blackDuckProjectVersionNickname = blackDuckProjectVersionNickname;
        return this;
    }

    public EventDataBuilder setBlackDuckBomComponentUri(final String blackDuckBomComponentUri) {
        this.blackDuckBomComponentUri = blackDuckBomComponentUri;
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

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public Long getJiraProjectId() {
        return jiraProjectId;
    }

    public String getBlackDuckProjectVersionUrl() {
        return blackDuckProjectVersionUrl;
    }

    public String getBlackDuckComponentUrl() {
        return blackDuckComponentUrl;
    }

    public String getBlackDuckComponentVersionUrl() {
        return blackDuckComponentVersionUrl;
    }

    public String getBomComponentUri() {
        return blackDuckBomComponentUri;
    }

    public String getBlackDuckRuleUrl() {
        return blackDuckRuleUrl;
    }

    public EventData build() throws EventDataBuilderException {
        // Use available data to complete
        final EventData eventData = new EventData();
        eventData.setJiraIssueSummary(getIssueSummary());
        setPolicyIssueCommentPropertiesFromNotificationType();

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

        if (blackDuckProjectVersionName == null) {
            throw new EventDataBuilderException("blackDuckProjectVersion not set");
        }

        if (blackDuckProjectVersionUrl == null) {
            throw new EventDataBuilderException("blackDuckProjectVersionUrl not set");
        }

        if (blackDuckComponentName == null) {
            throw new EventDataBuilderException("blackDuckComponentName not set");
        }

        if (eventData.getJiraIssueSummary().length() > 255) {
            // a jira summary can be at most 255 characters
            eventData.setJiraIssueSummary(eventData.getJiraIssueSummary().substring(0, 252) + "...");
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

        if (this.eventCategory == EventCategory.POLICY) {
            if (blackDuckRuleName == null) {
                throw new EventDataBuilderException("blackDuckRuleName not set");
            }
            if (blackDuckRuleUrl == null) {
                throw new EventDataBuilderException("blackDuckRuleUrl not set");
            }
        }
        return build(eventData);
    }

    private EventData build(final EventData eventData) throws EventDataBuilderException {
        eventData.setAction(action)
                .setCategory(eventCategory)
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
                .setBlackDuckProjectVersion(blackDuckProjectVersionName)
                .setBlackDuckProjectVersionUrl(blackDuckProjectVersionUrl)
                .setBlackDuckComponentName(blackDuckComponentName)
                .setBlackDuckComponentUrl(blackDuckComponentUrl)
                .setBlackDuckComponentVersion(blackDuckComponentVersionName)
                .setBlackDuckComponentVersionUrl(blackDuckComponentVersionUrl)
                .setBlackDuckLicenseNames(blackDuckLicenseNames)
                .setBlackDuckLicenseUrl(blackDuckLicenseUrl)
                .setBlackDuckComponentUsage(blackDuckComponentUsage)
                .setBlackDuckComponentOrigin(blackDuckComponentOrigin)
                .setBlackDuckComponentOriginId(blackDuckComponentOriginId)
                .setBlackDuckProjectVersionNickname(blackDuckProjectVersionNickname)
                .setBlackDuckBomComponentUri(blackDuckBomComponentUri)
                .setJiraIssueDescription(jiraIssueDescription)
                .setJiraIssueComment(jiraIssueComment)
                .setJiraIssueReOpenComment(jiraIssueReOpenComment)
                .setJiraIssueCommentForExistingIssue(jiraIssueCommentForExistingIssue)
                .setJiraIssueResolveComment(jiraIssueResolveComment)
                .setJiraIssueCommentInLieuOfStateChange(jiraIssueCommentInLieuOfStateChange)
                .setBlackDuckRuleName(blackDuckRuleName)
                .setBlackDuckRuleOverridable(blackDuckRuleOverridable)
                .setBlackDuckRuleDescription(blackDuckRuleDescription)
                .setBlackDuckRuleUrl(blackDuckRuleUrl)
                .setComponentIssueUrl(componentIssueUrl)
                .setBlackDuckProjectOwner(blackDuckProjectOwner)
                .setBlackDuckProjectVersionLastUpdated(blackDuckProjectVersionLastUpdated)
                .setNotificationType(notificationType);

        try {
            eventData.setEventKey(generateEventKey());
        } catch (final IntegrationException e) {
            throw new EventDataBuilderException("Could not create event key.", e);
        }
        return eventData;
    }

    public EventData buildSpecialEventData(final JiraProject jiraProject, final NotificationContentDetail detail, final Date batchStartDate) throws EventDataBuilderException {
        setAction(BlackDuckEventAction.RESOLVE_ALL);

        setJiraIssueAssigneeUserId(jiraProject.getAssigneeUserId());
        setJiraProjectName(jiraProject.getProjectName());
        setJiraProjectId(jiraProject.getProjectId());
        setPropertiesFromNotificationContentDetail(detail);

        if (detail.getBomComponent().isPresent()) {
            setBlackDuckBomComponentUri(detail.getBomComponent().get().uri);
        }

        setLastBatchStartDate(batchStartDate);
        final EventData specialEventData = new EventData();

        return build(specialEventData);
    }

    // This must remain consistent among non-major versions
    private final String generateEventKey() throws IntegrationException {
        final Long jiraProjectId = this.getJiraProjectId();
        final String blackDuckProjectVersionUrl = this.getBlackDuckProjectVersionUrl();
        final String blackDuckComponentVersionUrl = this.getBlackDuckComponentVersionUrl();
        final String blackDuckComponentUrl = this.getBlackDuckComponentUrl();
        final StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(BlackDuckJiraConstants.ISSUE_PROPERTY_KEY_ISSUE_TYPE_NAME);
        keyBuilder.append(BlackDuckJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_SEPARATOR);
        if (EventCategory.POLICY.equals(this.getEventCategory())) {
            keyBuilder.append(BlackDuckJiraConstants.ISSUE_PROPERTY_KEY_ISSUE_TYPE_VALUE_POLICY);
        } else {
            keyBuilder.append(BlackDuckJiraConstants.ISSUE_PROPERTY_KEY_ISSUE_TYPE_VALUE_VULNERABILITY);
        }
        keyBuilder.append(BlackDuckJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_PAIR_SEPARATOR);

        keyBuilder.append(BlackDuckJiraConstants.ISSUE_PROPERTY_KEY_JIRA_PROJECT_ID_NAME);
        keyBuilder.append(BlackDuckJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_SEPARATOR);
        keyBuilder.append(jiraProjectId.toString());
        keyBuilder.append(BlackDuckJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_PAIR_SEPARATOR);

        keyBuilder.append(BlackDuckJiraConstants.ISSUE_PROPERTY_KEY_BLACKDUCK_PROJECT_VERSION_REL_URL_HASHED_NAME);
        keyBuilder.append(BlackDuckJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_SEPARATOR);
        keyBuilder.append(hashString(UrlParser.getRelativeUrl(blackDuckProjectVersionUrl)));
        keyBuilder.append(BlackDuckJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_PAIR_SEPARATOR);

        keyBuilder.append(BlackDuckJiraConstants.ISSUE_PROPERTY_KEY_BLACKDUCK_COMPONENT_REL_URL_HASHED_NAME);
        keyBuilder.append(BlackDuckJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_SEPARATOR);
        if (EventCategory.POLICY.equals(this.getEventCategory())) {
            keyBuilder.append(hashString(UrlParser.getRelativeUrl(blackDuckComponentUrl)));
        } else {
            // Vulnerabilities do not have a component URL
            keyBuilder.append("");
        }
        keyBuilder.append(BlackDuckJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_PAIR_SEPARATOR);

        keyBuilder.append(BlackDuckJiraConstants.ISSUE_PROPERTY_KEY_BLACKDUCK_COMPONENT_VERSION_REL_URL_HASHED_NAME);
        keyBuilder.append(BlackDuckJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_SEPARATOR);
        keyBuilder.append(hashString(UrlParser.getRelativeUrl(blackDuckComponentVersionUrl)));

        if (EventCategory.POLICY.equals(this.getEventCategory())) {
            final String policyRuleUrl = this.getBlackDuckRuleUrl();
            if (policyRuleUrl == null) {
                throw new HubIntegrationException("Policy Rule URL is null");
            }
            keyBuilder.append(BlackDuckJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_PAIR_SEPARATOR);
            keyBuilder.append(BlackDuckJiraConstants.ISSUE_PROPERTY_KEY_BLACKDUCK_POLICY_RULE_REL_URL_HASHED_NAME);
            keyBuilder.append(BlackDuckJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_SEPARATOR);
            keyBuilder.append(hashString(UrlParser.getRelativeUrl(policyRuleUrl)));
        }
        // TODO before a MAJOR release, discuss how we should differentiate tickets based on origin

        final String key = keyBuilder.toString();
        return key;
    }

    public final String hashString(final String origString) {
        String hashString;
        if (origString == null) {
            hashString = "";
        } else {
            hashString = String.valueOf(origString.hashCode());
        }
        return hashString;
    }

    private EventDataBuilder setPolicyIssueCommentPropertiesFromNotificationType() {
        if (NotificationType.POLICY_OVERRIDE.equals(notificationType)) {
            setJiraIssueReOpenComment(BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_REOPEN);
            setJiraIssueCommentForExistingIssue(BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_OVERRIDDEN_COMMENT);
            setJiraIssueResolveComment(BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_RESOLVE);
            setJiraIssueCommentInLieuOfStateChange(BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_OVERRIDDEN_COMMENT);
        } else if (NotificationType.RULE_VIOLATION.equals(notificationType)) {
            setJiraIssueReOpenComment(BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_REOPEN);
            setJiraIssueCommentForExistingIssue(BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_DETECTED_AGAIN_COMMENT);
            setJiraIssueResolveComment(BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_RESOLVE);
            setJiraIssueCommentInLieuOfStateChange(BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_DETECTED_AGAIN_COMMENT);
        } else if (NotificationType.RULE_VIOLATION_CLEARED.equals(notificationType)) {
            setJiraIssueReOpenComment(BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_REOPEN);
            setJiraIssueCommentForExistingIssue(BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_CLEARED_COMMENT);
            setJiraIssueResolveComment(BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_CLEARED_RESOLVE);
            setJiraIssueCommentInLieuOfStateChange(BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_CLEARED_COMMENT);
        } else if (NotificationType.BOM_EDIT.equals(notificationType)) {
            final String noComment = "";
            setJiraIssueReOpenComment(noComment);
            setJiraIssueCommentForExistingIssue(noComment);
            setJiraIssueResolveComment(noComment);
            setJiraIssueCommentInLieuOfStateChange(noComment);
        }
        return this;
    }

    private String getIssueSummary() throws EventDataBuilderException {
        final String projectName = blackDuckProjectName;
        final String projectVersionName = blackDuckProjectVersionName;
        if (EventCategory.POLICY.equals(eventCategory)) {
            final String issueSummaryTemplate = "%s: Project '%s' / '%s', Component '%s' [Rule: '%s']";
            return String.format(issueSummaryTemplate, BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_ISSUE, projectName, projectVersionName, getComponentString(), blackDuckRuleName);
        } else if (EventCategory.VULNERABILITY.equals(eventCategory)) {
            final StringBuilder issueSummary = new StringBuilder();
            issueSummary.append(BlackDuckJiraConstants.BLACKDUCK_VULNERABILITY_ISSUE);
            issueSummary.append(": Project '");
            issueSummary.append(projectName);
            issueSummary.append("' / '");
            issueSummary.append(projectVersionName);
            issueSummary.append("', Component '");
            issueSummary.append(blackDuckComponentName);
            issueSummary.append("' / '");
            issueSummary.append(blackDuckComponentVersionName != null ? blackDuckComponentVersionName : "?");
            issueSummary.append("'");
            return issueSummary.toString();
        } else if (EventCategory.SPECIAL.equals(eventCategory)) {
            return null;
        } else {
            throw new EventDataBuilderException("Invalid event category: " + eventCategory);
        }
    }

    private String getComponentString() {
        String componentString = "?";
        if (blackDuckComponentName != null) {
            componentString = blackDuckComponentName;
            if (blackDuckComponentVersionName != null) {
                componentString += "' / '" + blackDuckComponentVersionName;
            }
        }
        return componentString;
    }

}
