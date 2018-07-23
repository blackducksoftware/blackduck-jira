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

public class JiraIssueWrapper {
    private final JiraIssueFieldTemplate jiraIssueFieldTemplate;
    private final BlackDuckIssueFieldTemplate blackDuckIssueFieldTemplate;

    public JiraIssueWrapper(final JiraIssueFieldTemplate jiraIssueFieldTemplate, final BlackDuckIssueFieldTemplate blackDuckIssueTemplate) {
        this.jiraIssueFieldTemplate = jiraIssueFieldTemplate;
        this.blackDuckIssueFieldTemplate = blackDuckIssueTemplate;
    }

    public JiraIssueFieldTemplate getJiraIssueFieldTemplate() {
        return jiraIssueFieldTemplate;
    }

    public BlackDuckIssueFieldTemplate getBlackDuckIssueTemplate() {
        return blackDuckIssueFieldTemplate;
    }

    @Override
    public String toString() {
        return "[" + jiraIssueFieldTemplate + "|" + blackDuckIssueFieldTemplate + "]";
    }

}
