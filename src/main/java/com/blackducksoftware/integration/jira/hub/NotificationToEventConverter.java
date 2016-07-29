package com.blackducksoftware.integration.jira.hub;

import com.atlassian.jira.issue.issuetype.IssueType;
import com.blackducksoftware.integration.jira.config.JiraProject;
import com.blackducksoftware.integration.jira.hub.model.notification.NotificationItem;

public abstract class NotificationToEventConverter {
	private final HubNotificationService hubNotificationService;
	private final TicketGeneratorInfo ticketGenInfo;

	public NotificationToEventConverter(final HubNotificationService hubNotificationService,
			final TicketGeneratorInfo ticketGenInfo) {
		this.hubNotificationService = hubNotificationService;
		this.ticketGenInfo = ticketGenInfo;
	}
	public abstract HubEvents generateEvents(NotificationItem notif);

	protected HubNotificationService getHubNotificationService() {
		return hubNotificationService;
	}

	protected JiraProject getJiraProject(final long jiraProjectId) throws HubNotificationServiceException {
		if (ticketGenInfo.getJiraProjectManager() == null) {
			throw new HubNotificationServiceException("The JIRA projectManager has not been set");
		}
		final com.atlassian.jira.project.Project atlassianJiraProject = ticketGenInfo.getJiraProjectManager()
				.getProjectObj(jiraProjectId);
		if (atlassianJiraProject == null) {
			throw new HubNotificationServiceException("Error: JIRA Project with ID " + jiraProjectId + " not found");
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
					if (issueType.getName().equals(ticketGenInfo.getJiraIssueTypeName())) {
						bdsJiraProject.setIssueTypeId(issueType.getId());
						projectHasIssueType = true;
					}
				}
			}
			if (!projectHasIssueType) {
				bdsJiraProject.setProjectError("The Jira project is missing the "
						+ ticketGenInfo.getJiraIssueTypeName() + " issue type.");
			}
		}
		return bdsJiraProject;
	}

	protected TicketGeneratorInfo getTicketGenInfo() {
		return ticketGenInfo;
	}

}
