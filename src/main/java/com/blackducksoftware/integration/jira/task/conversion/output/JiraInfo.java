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

import java.util.Set;

import com.blackducksoftware.integration.jira.config.ProjectFieldCopyMapping;

public class JiraInfo {

    private final String jiraUserName;

    private final String jiraUserId;

    // if issueAssigneeId is null: leave it unassigned
    private final String issueAssigneeId;

    private final String jiraIssueTypeId;

    private final Long jiraProjectId;

    private final String jiraProjectName;

    private final Set<ProjectFieldCopyMapping> projectFieldCopyMappings;

    public JiraInfo(final String jiraUserName, final String jiraUserId,
            final String issueAssigneeId, final String jiraIssueTypeId, final Long jiraProjectId,
            final String jiraProjectName, final Set<ProjectFieldCopyMapping> projectFieldCopyMappings) {
        this.jiraUserName = jiraUserName;
        this.jiraUserId = jiraUserId;
        this.issueAssigneeId = issueAssigneeId;
        this.jiraIssueTypeId = jiraIssueTypeId;
        this.jiraProjectId = jiraProjectId;
        this.jiraProjectName = jiraProjectName;
        this.projectFieldCopyMappings = projectFieldCopyMappings;
    }

    public String getJiraUserName() {
        return jiraUserName;
    }

    public String getJiraUserId() {
        return jiraUserId;
    }

    public String getIssueAssigneeId() {
        return issueAssigneeId;
    }

    public String getJiraIssueTypeId() {
        return jiraIssueTypeId;
    }

    public Long getJiraProjectId() {
        return jiraProjectId;
    }

    public String getJiraProjectName() {
        return jiraProjectName;
    }

    public Set<ProjectFieldCopyMapping> getProjectFieldCopyMappings() {
        return projectFieldCopyMappings;
    }
}