package com.blackducksoftware.integration.jira.hub;

import com.atlassian.jira.issue.issuetype.IssueType;
import com.blackducksoftware.integration.hub.notification.NotificationService;
import com.blackducksoftware.integration.hub.notification.NotificationServiceException;
import com.blackducksoftware.integration.hub.notification.api.NotificationItem;
import com.blackducksoftware.integration.jira.config.JiraProject;
import com.blackducksoftware.integration.jira.issue.JiraServices;

public abstract class NotificationToEventConverter {
	private final NotificationService hubNotificationService;
	private final JiraServices jiraServices;
	private final JiraContext jiraContext;

	public NotificationToEventConverter(final NotificationService hubNotificationService,
			final JiraServices jiraServices,
			final JiraContext jiraContext) {
		this.hubNotificationService = hubNotificationService;
		this.jiraServices = jiraServices;
		this.jiraContext = jiraContext;
	}
	public abstract HubEvents generateEvents(NotificationItem notif);

	protected NotificationService getHubNotificationService() {
		return hubNotificationService;
	}

	protected JiraProject getJiraProject(final long jiraProjectId) throws NotificationServiceException {
		final com.atlassian.jira.project.Project atlassianJiraProject = jiraServices.getJiraProjectManager()
				.getProjectObj(jiraProjectId);
		if (atlassianJiraProject == null) {
			throw new NotificationServiceException("Error: JIRA Project with ID " + jiraProjectId + " not found");
		}
		final String jiraProjectKey = atlassianJiraProject.getKey();
		final String jiraProjectName = atlassianJiraProject.getName();
		final JiraProject bdsJiraProject = new JiraProject();
		bdsJiraProject.setProjectId(jiraProjectId);
		bdsJiraProject.setProjectKey(jiraProjectKey);
		bdsJiraProject.setProjectName(jiraProjectName);

		if (atlassianJiraProject.getIssueTypes() == null || atlassianJiraProject.getIssueTypes().isEmpty()) {
			bdsJiraProject.setProjectError("The Jira project : " + bdsJiraProject.getProjectName()
					+ " does not have any issue types, we will not be able to create tickets for this project.");
		} else {
			boolean projectHasIssueType = false;
			if (atlassianJiraProject.getIssueTypes() != null && !atlassianJiraProject.getIssueTypes().isEmpty()) {
				for (final IssueType issueType : atlassianJiraProject.getIssueTypes()) {
					if (issueType.getName().equals(jiraContext.getJiraIssueTypeName())) {
						bdsJiraProject.setIssueTypeId(issueType.getId());
						projectHasIssueType = true;
					}
				}
			}
			if (!projectHasIssueType) {
				bdsJiraProject.setProjectError("The Jira project is missing the "
						+ jiraContext.getJiraIssueTypeName() + " issue type.");
			}
		}
		return bdsJiraProject;
	}

	protected JiraContext getJiraContext() {
		return jiraContext;
	}

}
