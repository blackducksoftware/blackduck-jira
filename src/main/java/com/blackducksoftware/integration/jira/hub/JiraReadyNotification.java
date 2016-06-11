package com.blackducksoftware.integration.jira.hub;

import com.blackducksoftware.integration.jira.hub.model.notification.NotificationItem;

public class JiraReadyNotification {
	private final String jiraProjectName;
	private final String jiraProjectKey;
	private final String hubProjectName;
	private final NotificationItem notificationItem;

	public JiraReadyNotification(String jiraProjectKey, String jiraProjectName, String hubProjectName,
			NotificationItem notificationItem) {
		this.jiraProjectKey = jiraProjectKey;
		this.jiraProjectName = jiraProjectName;
		this.hubProjectName = hubProjectName;
		this.notificationItem = notificationItem;
	}

	public String getJiraProjectKey() {
		return jiraProjectKey;
	}

	public String getJiraProjectName() {
		return jiraProjectName;
	}

	public String getHubProjectName() {
		return hubProjectName;
	}

	public NotificationItem getNotificationItem() {
		return notificationItem;
	}

	@Override
	public String toString() {
		return "JiraReadyNotification [jiraProjectName=" + jiraProjectName + ", hubProjectName=" + hubProjectName
				+ ", notificationItem=" + notificationItem + "]";
	}

}
