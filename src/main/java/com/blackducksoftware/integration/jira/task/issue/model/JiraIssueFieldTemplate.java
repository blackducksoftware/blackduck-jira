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

import com.atlassian.jira.issue.status.Status;

public class JiraIssueFieldTemplate {
    private final String issueTypeId;
    private final Status issueStatus;

    // TODO how should we track fields here

    public JiraIssueFieldTemplate(final String issueTypeId, final Status issueStatus) {
        this.issueTypeId = issueTypeId;
        this.issueStatus = issueStatus;

    }

    public String getIssueTypeId() {
        return issueTypeId;
    }

    public Status getIssueStatus() {
        return issueStatus;
    }

}
