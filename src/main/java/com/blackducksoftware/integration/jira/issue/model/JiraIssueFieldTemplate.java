/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2019 Synopsys, Inc.
 * https://www.synopsys.com/
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
package com.blackducksoftware.integration.jira.issue.model;

import com.atlassian.jira.user.ApplicationUser;
import com.synopsys.integration.util.Stringable;

public class JiraIssueFieldTemplate extends Stringable {
    private final Long projectId;
    private final String projectName;
    private final String issueTypeId;
    private final String summary;
    private final ApplicationUser issueCreator;
    private final String issueDescription;
    private final String assigneeId;

    private boolean retainExistingValuesWhenParameterNotProvided = true;
    private boolean applyDefaultValuesWhenParameterNotProvided = true;

    // @formatter:off
    public JiraIssueFieldTemplate(
             final Long projectId
            ,final String projectName
            ,final String issueTypeId
            ,final String summary
            ,final ApplicationUser issueCreator
            ,final String issueDescription
            ,final String assigneeId
            ) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.issueTypeId = issueTypeId;
        this.summary = summary;
        this.issueCreator = issueCreator;
        this.issueDescription = issueDescription;
        this.assigneeId = assigneeId;
    }
    // @formatter:on

    public Long getProjectId() {
        return projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getIssueTypeId() {
        return issueTypeId;
    }

    public String getSummary() {
        return summary;
    }

    public ApplicationUser getIssueCreator() {
        return issueCreator;
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
