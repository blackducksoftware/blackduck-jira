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

public class IssueTemplateWrapper {
    private final String issueSummary;
    private final String description;
    private final JiraIssueFieldTemplate jiraIssueTemplate;
    private final BlackDuckIssueFieldTemplate blackDuckIssueTemplate;

    public IssueTemplateWrapper(final String issueSummary, final String description, final JiraIssueFieldTemplate jiraIssueTemplate, final BlackDuckIssueFieldTemplate blackDuckIssueTemplate) {
        this.issueSummary = issueSummary;
        this.description = description;
        this.jiraIssueTemplate = jiraIssueTemplate;
        this.blackDuckIssueTemplate = blackDuckIssueTemplate;
    }

    public String getIssueSummary() {
        return issueSummary;
    }

    public String getDescription() {
        return description;
    }

    public JiraIssueFieldTemplate getJiraIssueTemplate() {
        return jiraIssueTemplate;
    }

    public BlackDuckIssueFieldTemplate getBlackDuckIssueTemplate() {
        return blackDuckIssueTemplate;
    }

}
