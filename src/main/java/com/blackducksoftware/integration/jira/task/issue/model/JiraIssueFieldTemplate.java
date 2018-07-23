/*
 * Copyright (C) 2018 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.jira.task.issue.model;

import com.blackducksoftware.integration.util.Stringable;

public class JiraIssueFieldTemplate extends Stringable {
    private final Long jiraProjectId;
    private final String jiraIssueTypeId;
    private final String summary;
    private final String issueCreatorUsername;
    private final String issueDescription;
    private final String assigneeId;

    private boolean retainExistingFieldValues = true;
    private boolean useDefaults = true;

    // @formatter:off
    public JiraIssueFieldTemplate(
             final Long jiraProjectId
            ,final String jiraIssueTypeId
            ,final String summary
            ,final String issueCreatorUsername
            ,final String issueDescription
            ,final String assigneeId
            ) {
        this.jiraProjectId = jiraProjectId;
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

    public boolean shouldRetainExistingFieldValues() {
        return retainExistingFieldValues;
    }

    public void setRetainExistingFieldValues(final boolean retainExistingFieldValues) {
        this.retainExistingFieldValues = retainExistingFieldValues;
    }

    public boolean shouldUseDefaults() {
        return useDefaults;
    }

    public void setUseDefaults(final boolean useDefaults) {
        this.useDefaults = useDefaults;
    }

}
