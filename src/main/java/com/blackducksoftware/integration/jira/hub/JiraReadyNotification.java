package com.blackducksoftware.integration.jira.hub;

import java.util.List;

import com.blackducksoftware.integration.jira.hub.model.notification.NotificationItem;
import com.blackducksoftware.integration.jira.issue.Issue;

public class JiraReadyNotification {
	private final String jiraProjectName;
	private final String jiraProjectKey;
	private final String hubProjectName;
	private final NotificationItem notificationItem;
	private final List<Issue> issues;

	public JiraReadyNotification(final String jiraProjectName, final String jiraProjectKey,
			final String hubProjectName, final NotificationItem notificationItem, final List<Issue> issues) {
		super();
		this.jiraProjectName = jiraProjectName;
		this.jiraProjectKey = jiraProjectKey;
		this.hubProjectName = hubProjectName;
		this.notificationItem = notificationItem;
		this.issues = issues;
	}

	public String getJiraProjectName() {
		return jiraProjectName;
	}
	public String getJiraProjectKey() {
		return jiraProjectKey;
	}
	public String getHubProjectName() {
		return hubProjectName;
	}
	public NotificationItem getNotificationItem() {
		return notificationItem;
	}

	public List<Issue> getIssues() {
		return issues;
	}
	@Override
	public String toString() {
		return "JiraReadyNotification [jiraProjectName=" + jiraProjectName + ", jiraProjectKey=" + jiraProjectKey
				+ ", hubProjectName=" + hubProjectName + ", notificationItem=" + notificationItem + ", issues="
				+ issues + "]";
	}
}
