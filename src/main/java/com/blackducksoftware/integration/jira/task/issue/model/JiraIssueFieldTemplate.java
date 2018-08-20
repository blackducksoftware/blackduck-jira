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
package com.blackducksoftware.integration.jira.task.issue.model;

import com.synopsys.integration.util.Stringable;

public class JiraIssueFieldTemplate extends Stringable {
    private final Long jiraProjectId;
    private final String jiraProjectName;
    private final String jiraIssueTypeId;
    private final String summary;
    private final String issueCreatorUsername;
    private final String issueDescription;
    private final String assigneeId;

    private boolean retainExistingValuesWhenParameterNotProvided = true;
    private boolean applyDefaultValuesWhenParameterNotProvided = true;

    // @formatter:off
    public JiraIssueFieldTemplate(
             final Long jiraProjectId
            ,final String jiraProjectName
            ,final String jiraIssueTypeId
            ,final String summary
            ,final String issueCreatorUsername
            ,final String issueDescription
            ,final String assigneeId
            ) {
        this.jiraProjectId = jiraProjectId;
        this.jiraProjectName = jiraProjectName;
        this.jiraIssueTypeId = jiraIssueTypeId;
        this.summary = summary;
        this.issueCreatorUsername = issueCreatorUsername;
        this.issueDescription = issueDescription;
        this.assigneeId = assigneeId;
    }
    // @formatter:on

    public Long getJiraProjectId() {
        return jiraProjectId;
    }

    public String getJiraProjectName() {
        return jiraProjectName;
    }

    public String getJiraIssueTypeId() {
        return jiraIssueTypeId;
    }

    public String getSummary() {
        return summary;
    }

    public String getIssueCreatorUsername() {
        return issueCreatorUsername;
    }

    public String getIssueDescription() {
        return issueDescription;
    }

    public String getAssigneeId() {
        return assigneeId;
    }

    public boolean shouldRetainExistingValuesWhenParameterNotProvided() {
        return retainExistingValuesWhenParameterNotProvided;
    }

    public void setRetainExistingValuesWhenParameterNotProvided(final boolean retainExistingValuesWhenParameterNotProvided) {
        this.retainExistingValuesWhenParameterNotProvided = retainExistingValuesWhenParameterNotProvided;
    }

    public boolean shouldApplyDefaultValuesWhenParameterNotProvided() {
        return applyDefaultValuesWhenParameterNotProvided;
    }

    public void setApplyDefaultValuesWhenParameterNotProvided(final boolean applyDefaultValuesWhenParameterNotProvided) {
        this.applyDefaultValuesWhenParameterNotProvided = applyDefaultValuesWhenParameterNotProvided;
    }

}
