/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
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
 *******************************************************************************/
package com.blackducksoftware.integration.jira.task.conversion.output;

import java.net.URISyntaxException;
import java.util.Set;

import org.apache.log4j.Logger;

import com.atlassian.jira.issue.Issue;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.policy.PolicyRule;
import com.blackducksoftware.integration.hub.dataservice.notification.item.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.item.PolicyContentItem;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.common.HubUrlParser;
import com.blackducksoftware.integration.jira.config.ProjectFieldCopyMapping;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class PolicyEvent extends HubEvent<NotificationContentItem> {
    private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final PolicyContentItem notificationContentItem;

    private final PolicyRule policyRule;

    private final String comment;

    private final String commentForExistingIssue;

    private final String resolveComment;

    public PolicyEvent(final HubEventAction action, final String jiraUserName, final String jiraUserId,

            final String issueAssigneeId,
            final String jiraIssueTypeId,
            final Long jiraProjectId, final String jiraProjectName,
            final PolicyContentItem notificationContentItem,
            final PolicyRule policyRule, final String comment, final String commentForExistingIssue,
            final String resolveComment,
            final Set<ProjectFieldCopyMapping> projectFieldCopyMappings, final MetaService metaService) {

        super(action, jiraUserName, jiraUserId, issueAssigneeId, jiraIssueTypeId, jiraProjectId, jiraProjectName,
                projectFieldCopyMappings, notificationContentItem, metaService);
        this.notificationContentItem = notificationContentItem;
        this.policyRule = policyRule;
        this.comment = comment;
        this.commentForExistingIssue = commentForExistingIssue;
        this.resolveComment = resolveComment;
    }

    public PolicyContentItem getNotificationContentItem() {
        return notificationContentItem;
    }

    public PolicyRule getPolicyRule() {
        return policyRule;
    }

    @Override
    public String getUniquePropertyKey() throws HubIntegrationException, URISyntaxException {
        final StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_ISSUE_TYPE_NAME);
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_SEPARATOR);
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_ISSUE_TYPE_VALUE_POLICY);
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_PAIR_SEPARATOR);

        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_JIRA_PROJECT_ID_NAME);
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_SEPARATOR);
        keyBuilder.append(getJiraProjectId().toString());
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_PAIR_SEPARATOR);

        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_HUB_PROJECT_VERSION_REL_URL_HASHED_NAME);
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_SEPARATOR);

        keyBuilder.append(hashString(HubUrlParser.getRelativeUrl(getNotificationContentItem().getProjectVersion().getUrl())));
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_PAIR_SEPARATOR);

        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_HUB_COMPONENT_REL_URL_HASHED_NAME);
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_SEPARATOR);
        keyBuilder.append(hashString(HubUrlParser.getRelativeUrl(getNotificationContentItem().getComponentUrl())));
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_PAIR_SEPARATOR);

        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_HUB_COMPONENT_VERSION_REL_URL_HASHED_NAME);
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_SEPARATOR);
        keyBuilder.append(hashString(HubUrlParser.getRelativeUrl(getNotificationContentItem().getComponentVersionUrl())));
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_PAIR_SEPARATOR);

        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_HUB_POLICY_RULE_REL_URL_HASHED_NAME);
        keyBuilder.append(HubJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_SEPARATOR);
        keyBuilder.append(hashString(HubUrlParser.getRelativeUrl(getMetaService().getHref(getPolicyRule()))));

        final String key = keyBuilder.toString();
        logger.debug("property key: " + key);
        return key;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("PolicyEvent [notificationContentItem=");
        builder.append(notificationContentItem);
        builder.append(", policyRule=");
        builder.append(policyRule);
        builder.append(", getJiraUserName()=");
        builder.append(getJiraUserName());
        builder.append(", getJiraIssueTypeId()=");
        builder.append(getJiraIssueTypeId());
        builder.append(", getJiraProjectId()=");
        builder.append(getJiraProjectId());
        builder.append(", getJiraProjectName()=");
        builder.append(getJiraProjectName());
        builder.append("]");
        return builder.toString();
    }

    @Override
    public String getIssueSummary() {
        final StringBuilder issueSummary = new StringBuilder();
        issueSummary.append("Black Duck policy violation detected on Hub project '");
        issueSummary.append(getNotificationContentItem().getProjectVersion().getProjectName());
        issueSummary.append("' / '");
        issueSummary.append(getNotificationContentItem().getProjectVersion().getProjectVersionName());
        issueSummary.append("', component '");
        issueSummary.append(getNotificationContentItem().getComponentName());
        issueSummary.append("' / '");
        issueSummary.append(getNotificationContentItem().getComponentVersion());
        issueSummary.append("'");
        issueSummary.append(" [Rule: '");
        issueSummary.append(getPolicyRule().getName());
        issueSummary.append("']");
        return issueSummary.toString();
    }

    @Override
    public String getIssueDescription() {
        final StringBuilder issueDescription = new StringBuilder();
        issueDescription.append("The Black Duck Hub has detected a policy violation on Hub project '");
        issueDescription.append(getNotificationContentItem().getProjectVersion().getProjectName());
        issueDescription.append("' / '");
        issueDescription.append(getNotificationContentItem().getProjectVersion().getProjectVersionName());
        issueDescription.append("', component '");
        issueDescription.append(getNotificationContentItem().getComponentName());
        issueDescription.append("' / '");
        issueDescription.append(getNotificationContentItem().getComponentVersion());
        issueDescription.append("'.");
        issueDescription.append(" The rule violated is: '");
        issueDescription.append(getPolicyRule().getName());
        issueDescription.append("'. Rule overridable : ");
        issueDescription.append(getPolicyRule().getOverridable());
        return issueDescription.toString();
    }

    @Override
    public PolicyViolationIssueProperties createIssuePropertiesFromJson(final String json) {
        final Gson gson = new GsonBuilder().create();
        return gson.fromJson(json, PolicyViolationIssueProperties.class);
    }

    // TODO: This class shouldn't know about JIRA issues; just pass in the ID, not the whole Issue
    @Override
    public IssueProperties createIssueProperties(final Issue issue) {
        final IssueProperties properties = new PolicyViolationIssueProperties(
                getNotificationContentItem().getProjectVersion().getProjectName(),
                getNotificationContentItem().getProjectVersion().getProjectVersionName(),
                getNotificationContentItem().getComponentName(), getNotificationContentItem().getComponentVersion(),
                issue.getId(), getPolicyRule().getName());
        return properties;
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public String getCommentIfExists() {
        return commentForExistingIssue;
    }

    public String getCommentInLieuOfStateChange() {
        return getCommentIfExists();
    }

    @Override
    public String getReopenComment() {
        return HubJiraConstants.HUB_POLICY_VIOLATION_REOPEN;
    }

    @Override
    public String getResolveComment() {
        return resolveComment;
    }
}
