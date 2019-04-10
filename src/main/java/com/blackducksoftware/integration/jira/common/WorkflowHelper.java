package com.blackducksoftware.integration.jira.common;

import java.util.Collection;

import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;

public class WorkflowHelper {
    private WorkflowManager workflowManager;

    public WorkflowHelper(final WorkflowManager workflowManager) {
        this.workflowManager = workflowManager;
    }

    public BlackDuckWorkflowStatus getBlackDuckWorkflowStatus(final Project jiraProject) {
        final Long jiraProjectId = jiraProject.getId();
        final Collection<IssueType> issueTypes = jiraProject.getIssueTypes();

        boolean policyUsesBlackDuckWorkflow = false;
        boolean vulnUsesBlackDuckWorkflow = false;

        for (final IssueType issueType : issueTypes) {
            final String issueTypeName = issueType.getName();
            final String issueTypeId = issueType.getId();
            if (BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_ISSUE.equals(issueTypeName)) {
                policyUsesBlackDuckWorkflow = usesBlackduckWorkflow(jiraProjectId, issueTypeId);
            } else if (BlackDuckJiraConstants.BLACKDUCK_VULNERABILITY_ISSUE.equals(issueTypeName)) {
                vulnUsesBlackDuckWorkflow = usesBlackduckWorkflow(jiraProjectId, issueTypeId);
            }
        }

        if (policyUsesBlackDuckWorkflow && vulnUsesBlackDuckWorkflow) {
            return BlackDuckWorkflowStatus.ENABLED;
        } else if (policyUsesBlackDuckWorkflow || vulnUsesBlackDuckWorkflow) {
            return BlackDuckWorkflowStatus.PARTIAL;
        }
        return BlackDuckWorkflowStatus.DISABLED;
    }

    public boolean usesBlackduckWorkflow(final Long projectId, final String issueTypeId) {
        final JiraWorkflow workflow = workflowManager.getWorkflow(projectId, issueTypeId);
        return BlackDuckJiraConstants.BLACKDUCK_JIRA_WORKFLOW.equals(workflow.getName());
    }

}
