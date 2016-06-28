package com.blackducksoftware.integration.jira.hub;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.properties.IssuePropertyService;
import com.atlassian.jira.entity.property.JsonEntityPropertyManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.WorkflowManager;

public class TicketGeneratorInfo {

	private final ProjectManager jiraProjectManager;
	private final IssueService issueService;
	private final ApplicationUser jiraUser;
	private final String jiraIssueTypeName;
	private final JiraAuthenticationContext authContext;
	private final IssuePropertyService propertyService;

	private final WorkflowManager workflowManager;

	private final JsonEntityPropertyManager jsonEntityPropertyManager;

	public TicketGeneratorInfo(final ProjectManager jiraProjectManager, final IssueService issueService,
			final ApplicationUser jiraUser, final String jiraIssueTypeName, final JiraAuthenticationContext authContext,
			final IssuePropertyService propertyService, 
			final WorkflowManager workflowManager, final JsonEntityPropertyManager jsonEntityPropertyManager) {
		this.jiraProjectManager = jiraProjectManager;
		this.issueService = issueService;
		this.jiraUser = jiraUser;
		this.jiraIssueTypeName = jiraIssueTypeName;
		this.authContext = authContext;
		this.propertyService = propertyService;
		this.workflowManager = workflowManager;
		this.jsonEntityPropertyManager = jsonEntityPropertyManager;
	}

	public ProjectManager getJiraProjectManager() {
		return jiraProjectManager;
	}
	public IssueService getIssueService() {
		return issueService;
	}
	public ApplicationUser getJiraUser() {
		return jiraUser;
	}
	public String getJiraIssueTypeName() {
		return jiraIssueTypeName;
	}
	public JiraAuthenticationContext getAuthContext() {
		return authContext;
	}
	public IssuePropertyService getPropertyService() {
		return propertyService;
	}

	public WorkflowManager getWorkflowManager() {
		return workflowManager;
	}

	public JsonEntityPropertyManager getJsonEntityPropertyManager() {
		return jsonEntityPropertyManager;
	}

}
