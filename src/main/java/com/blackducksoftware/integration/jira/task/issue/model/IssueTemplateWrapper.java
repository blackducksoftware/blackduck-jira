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

import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.IssueInputParametersImpl;

public class IssueTemplateWrapper {
    private final BlackDuckIssueFieldTemplate blackDuckIssueTemplate;
    private final IssueInputParameters issueInputParameters;

    public IssueTemplateWrapper(final BlackDuckIssueFieldTemplate blackDuckIssueTemplate, final IssueInputParameters issueInputParameters) {
        this.blackDuckIssueTemplate = blackDuckIssueTemplate;
        this.issueInputParameters = issueInputParameters;

        setDefaultValues(issueInputParameters);
    }

    public IssueTemplateWrapper(final BlackDuckIssueFieldTemplate blackDuckIssueTemplate, final Long jiraProjectId, final String jiraIssueTypeId, final String summary, final String issueCreatorUsername, final String issueDescription,
            final String assigneeId) {
        this.blackDuckIssueTemplate = blackDuckIssueTemplate;
        this.issueInputParameters = new IssueInputParametersImpl(); // TODO jiraServices.getIssueService().newIssueInputParameters();
        issueInputParameters
                .setProjectId(jiraProjectId)
                .setIssueTypeId(jiraIssueTypeId)
                .setSummary(summary)
                .setReporterId(issueCreatorUsername)
                .setDescription(issueDescription)
                .setAssigneeId(assigneeId);
    }

    public BlackDuckIssueFieldTemplate getBlackDuckIssueTemplate() {
        return blackDuckIssueTemplate;
    }

    public IssueInputParameters getIssueInputParameters() {
        return issueInputParameters;
    }

    private void setDefaultValues(final IssueInputParameters issueInputParameters) {
        issueInputParameters.setRetainExistingValuesWhenParameterNotProvided(true);
        issueInputParameters.setApplyDefaultValuesWhenParameterNotProvided(true);
    }

}
