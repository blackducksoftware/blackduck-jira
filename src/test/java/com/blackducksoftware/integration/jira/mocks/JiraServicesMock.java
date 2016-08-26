package com.blackducksoftware.integration.jira.mocks;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.properties.IssuePropertyService;
import com.atlassian.jira.entity.property.JsonEntityPropertyManager;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;

public class JiraServicesMock {

	private ProjectManager projectManager;
	private IssueService issueService;
	private JiraAuthenticationContext jiraAuthenticationContext;
	private IssuePropertyService issuePropertyService;
	private WorkflowManager workflowManager;
	private WorkflowSchemeManager workflowSchemeManager;
	private JsonEntityPropertyManager jsonEntityPropertyManager;
	private CommentManager commentManager;
	private GroupManager groupManager;
	private UserManager userManager;

	public void setProjectManager(final ProjectManager projectManager) {
		this.projectManager = projectManager;
	}

	public void setIssueService(final IssueService issueService) {
		this.issueService = issueService;
	}

	public void setJiraAuthenticationContext(final JiraAuthenticationContext jiraAuthenticationContext) {
		this.jiraAuthenticationContext = jiraAuthenticationContext;
	}

	public void setIssuePropertyService(final IssuePropertyService issuePropertyService) {
		this.issuePropertyService = issuePropertyService;
	}

	public void setWorkflowManager(final WorkflowManager workflowManager) {
		this.workflowManager = workflowManager;
	}

	public void setWorkflowSchemeManager(final WorkflowSchemeManager workflowSchemeManager) {
		this.workflowSchemeManager = workflowSchemeManager;
	}

	public void setJsonEntityPropertyManager(final JsonEntityPropertyManager jsonEntityPropertyManager) {
		this.jsonEntityPropertyManager = jsonEntityPropertyManager;
	}

	public void setCommentManager(final CommentManager commentManager) {
		this.commentManager = commentManager;
	}

	public void setGroupManager(final GroupManager groupManager) {
		this.groupManager = groupManager;
	}

	public void setUserManager(final UserManager userManager) {
		this.userManager = userManager;
	}

	public ProjectManager getJiraProjectManager() {
		return projectManager;
	}

	public IssueService getIssueService() {
		return issueService;
	}

	public JiraAuthenticationContext getAuthContext() {
		return jiraAuthenticationContext;
	}

	public IssuePropertyService getPropertyService() {
		return issuePropertyService;
	}

	public WorkflowManager getWorkflowManager() {
		return workflowManager;
	}

	public WorkflowSchemeManager getWorkflowSchemeManager() {
		return workflowSchemeManager;
	}

	public JsonEntityPropertyManager getJsonEntityPropertyManager() {
		return jsonEntityPropertyManager;
	}

	public CommentManager getCommentManager() {
		return commentManager;
	}

	public GroupManager getGroupManager() {
		return groupManager;
	}

	public UserManager getUserManager() {
		return userManager;
	}

}
