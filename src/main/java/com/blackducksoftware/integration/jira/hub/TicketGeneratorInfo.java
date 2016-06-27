package com.blackducksoftware.integration.jira.hub;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;

public class TicketGeneratorInfo {

	private final ProjectManager jiraProjectManager;
	private final IssueService issueService;
	private final ApplicationUser jiraUser;
	private final String jiraIssueTypeName;
	private final JiraAuthenticationContext authContext;

	public TicketGeneratorInfo(final ProjectManager jiraProjectManager, final IssueService issueService,
			final ApplicationUser jiraUser, final String jiraIssueTypeName,
			final JiraAuthenticationContext authContext) {
		this.jiraProjectManager = jiraProjectManager;
		this.issueService = issueService;
		this.jiraUser = jiraUser;
		this.jiraIssueTypeName = jiraIssueTypeName;
		this.authContext = authContext;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((authContext == null) ? 0 : authContext.hashCode());
		result = prime * result + ((issueService == null) ? 0 : issueService.hashCode());
		result = prime * result + ((jiraIssueTypeName == null) ? 0 : jiraIssueTypeName.hashCode());
		result = prime * result + ((jiraProjectManager == null) ? 0 : jiraProjectManager.hashCode());
		result = prime * result + ((jiraUser == null) ? 0 : jiraUser.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TicketGeneratorInfo)) {
			return false;
		}
		final TicketGeneratorInfo other = (TicketGeneratorInfo) obj;
		if (authContext == null) {
			if (other.authContext != null) {
				return false;
			}
		} else if (!authContext.equals(other.authContext)) {
			return false;
		}
		if (issueService == null) {
			if (other.issueService != null) {
				return false;
			}
		} else if (!issueService.equals(other.issueService)) {
			return false;
		}
		if (jiraIssueTypeName == null) {
			if (other.jiraIssueTypeName != null) {
				return false;
			}
		} else if (!jiraIssueTypeName.equals(other.jiraIssueTypeName)) {
			return false;
		}
		if (jiraProjectManager == null) {
			if (other.jiraProjectManager != null) {
				return false;
			}
		} else if (!jiraProjectManager.equals(other.jiraProjectManager)) {
			return false;
		}
		if (jiraUser == null) {
			if (other.jiraUser != null) {
				return false;
			}
		} else if (!jiraUser.equals(other.jiraUser)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("TicketGeneratorInfo [jiraProjectManager=");
		builder.append(jiraProjectManager);
		builder.append(", issueService=");
		builder.append(issueService);
		builder.append(", jiraUser=");
		builder.append(jiraUser);
		builder.append(", jiraIssueTypeName=");
		builder.append(jiraIssueTypeName);
		builder.append(", authContext=");
		builder.append(authContext);
		builder.append("]");
		return builder.toString();
	}

}
