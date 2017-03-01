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
package com.blackducksoftware.integration.jira.task.conversion;

public class EventDataSetKeys {
    public static final String JIRA_EVENT_INFO = "jiraEventInfo";

    private static final String ACTION = "action";

    private static final String JIRA_ISSUE_SUMMARY = "jiraIssueSummary";

    private static final String JIRA_ISSUE_DESCRIPTION = "jiraIssueDescription";

    private static final String JIRA_ISSUE_RESOLVE_COMMENT = "jiraIssueResolveComment";

    private static final String JIRA_ISSUE_REOPEN_COMMENT = "jiraIssueReOpenComment";

    private static final String JIRA_ISSUE_COMMENT_FOR_EXISTING_ISSUE = "jiraIssueCommentForExistingIssue";

    private static final String JIRA_ISSUE_COMMENT_IN_LIEU_OF_STATE_CHANGE = "jiraIssueCommentInLieuOfStateChange";

    private static final String JIRA_ISSUE_COMMENT = "jiraIssueComment";

    private static final String JIRA_FIELD_COPY_MAPPINGS = "jiraFieldCopyMappings";

    private static final String JIRA_PROJECT_NAME = "jiraProjectName";

    private static final String JIRA_PROJECT_ID = "jiraProjectId";

    private static final String JIRA_ISSUE_TYPE_ID = "jiraIssueTypeId";

    private static final String JIRA_ISSUE_ASSIGNEE_USER_ID = "jiraIssueAssigneeUserId";

    private static final String JIRA_USER_KEY = "jiraUserKey";

    private static final String JIRA_USER_NAME = "jiraUserName";

    private static final String JIRA_ISSUE_PROPERTIES_GENERATOR = "jiraIssuePropertiesGenerator";

    // Jira objects

    public static final String JIRA_CONTEXT = "jiraContext";

    public static final String JIRA_PROJECT = "jiraProject";

    // Hub

    public static final String HUB_PROJECT_NAME = "hubProjectName";

    public static final String HUB_PROJECT_VERSION = "hubProjectVersion";

    public static final String HUB_PROJECT_VERSION_URL = "hubProjectVersionUrl";

    public static final String HUB_COMPONENT_NAME = "hubComponentName";

    public static final String HUB_COMPONENT_URL = "hubComponentUrl";

    public static final String HUB_COMPONENT_VERSION = "hubComponentVersion";

    public static final String HUB_COMPONENT_VERSION_URL = "hubComponentVersionUrl";

    public static final String HUB_RULE_NAME = "hubRuleName";

    public static final String HUB_RULE_URL = "hubRuleUrl";

    public static final String HUB_LICENSE_NAMES = "hubLicenseNames";

    public static final String HUB_COMPONENT_USAGE = "hubComponentUsage";

    public static final String HUB_COMPONENT_ORIGIN = "hubComponentOrigin";

    public static final String HUB_COMPONENT_ORIGIN_ID = "hubComponentOriginId";

    public static final String HUB_PROJECT_VERSION_NICKNAME = "hubProjectVersionNickname";
}
