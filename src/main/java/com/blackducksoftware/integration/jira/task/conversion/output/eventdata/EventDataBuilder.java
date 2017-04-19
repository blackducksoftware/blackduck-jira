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
package com.blackducksoftware.integration.jira.task.conversion.output.eventdata;

import java.util.Set;

import com.blackducksoftware.integration.jira.common.exception.EventDataBuilderException;
import com.blackducksoftware.integration.jira.config.ProjectFieldCopyMapping;
import com.blackducksoftware.integration.jira.task.conversion.output.HubEventAction;
import com.blackducksoftware.integration.jira.task.conversion.output.IssuePropertiesGenerator;

public class EventDataBuilder {
    private final EventCategory eventCategory;

    private HubEventAction action;

    private String jiraAdminUserName;

    private String jiraIssueCreatorUserName;

    private String jiraAdminUserKey;

    private String jiraIssueCreatorUserKey;

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

    private String componentIssueUrl;

    public EventDataBuilder(final EventCategory eventCategory) {
        this.eventCategory = eventCategory;
    }

    public EventDataBuilder setAction(final HubEventAction action) {
        this.action = action;
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

    public EventDataBuilder setHubRuleName(final String hubRuleName) {
        this.hubRuleName = hubRuleName;
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
                .setJiraAdminUsername(jiraAdminUserName)
                .setJiraAdminUserKey(jiraAdminUserKey)
                .setJiraIssueCreatorUsername(jiraIssueCreatorUserName)
                .setJiraIssueCreatorUserKey(jiraIssueCreatorUserKey)
                .setJiraIssueAssigneeUserId(jiraIssueAssigneeUserId)
                .setJiraIssueTypeId(jiraIssueTypeId)
                .setJiraProjectName(jiraProjectName)
                .setJiraProjectId(jiraProjectId)
                .setJiraFieldCopyMappings(jiraFieldCopyMappings)
                .setHubProjectName(hubProjectName)
                .setHubProjectVersion(hubProjectVersion)
                .setHubProjectVersionUrl(hubProjectVersionUrl)
                .setHubComponentName(hubComponentName)
                .setHubComponentUrl(hubComponentUrl)
                .setHubComponentVersion(hubComponentVersion)
                .setHubComponentVersionUrl(hubComponentVersionUrl)
                .setHubLicenseNames(hubLicenseNames)
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
                .setHubRuleUrl(hubRuleUrl)
                .setComponentIssueUrl(componentIssueUrl);
        return eventData;
    }
}
