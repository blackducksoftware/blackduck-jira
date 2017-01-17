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
    public static final String ACTION = "action";

    public static final String JIRA_ISSUE_SUMMARY = "jiraIssueSummary";

    public static final String JIRA_ISSUE_DESCRIPTION = "jiraIssueDescription";

    public static final String JIRA_ISSUE_RESOLVE_COMMENT = "jiraIssueResolveComment";

    public static final String JIRA_ISSUE_REOPEN_COMMENT = "jiraIssueReOpenComment";

    public static final String JIRA_ISSUE_COMMENT_FOR_EXISTING_ISSUE = "jiraIssueCommentForExistingIssue";

    public static final String JIRA_ISSUE_COMMENT_IN_LIEU_OF_STATE_CHANGE = "jiraIssueCommentInLieuOfStateChange";

    public static final String JIRA_ISSUE_COMMENT = "jiraIssueComment";

    public static final String JIRA_FIELD_COPY_MAPPINGS = "jiraFieldCopyMappings";

    public static final String JIRA_PROJECT_NAME = "jiraProjectName";

    public static final String JIRA_PROJECT_ID = "jiraProjectId";

    public static final String JIRA_ISSUE_TYPE_ID = "jiraIssueTypeId";

    public static final String JIRA_ISSUE_ASSIGNEE_USER_ID = "jiraIssueAssigneeUserId";

    public static final String JIRA_USER_KEY = "jiraUserKey";

    public static final String JIRA_USER_NAME = "jiraUserName";

    public static final String JIRA_ISSUE_PROPERTIES_GENERATOR = "jiraIssuePropertiesGenerator";

    // Jira objects

    public static final String JIRA_CONTEXT = "jiraContext";

    public static final String JIRA_PROJECT = "jiraProject";

    // Hub

    public static final String HUB_PROJECT_NAME = "hubProjectName";

    public static final String HUB_PROJECT_VERSION = "hubProjectVersion";

    public static final String HUB_PROJECT_VERSION_URL = "hubProjectVersionUrl";

    public static final String HUB_COMPONENT_NAME = "hubComponentName";

    public static final String HUB_COMPONENT_VERSION = "hubComponentVersion";

    public static final String HUB_COMPONENT_VERSION_URL = "hubComponentVersionUrl";

    public static final String HUB_RULE_NAME = "hubRuleName";

    public static final String HUB_RULE_URL = "hubRuleUrl";
}
