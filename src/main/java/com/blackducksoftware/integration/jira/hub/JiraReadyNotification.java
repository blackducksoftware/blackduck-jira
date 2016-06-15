package com.blackducksoftware.integration.jira.hub;

import java.util.List;

import com.blackducksoftware.integration.jira.hub.model.notification.NotificationItem;

public class JiraReadyNotification {
	private final String jiraProjectName;
	private final String jiraProjectKey;
	private final String hubProjectName;
	private final NotificationItem notificationItem;
	private final List<String> ruleUrls;

	public JiraReadyNotification(final String jiraProjectKey, final String jiraProjectName, final String hubProjectName,
 final NotificationItem notificationItem, final List<String> ruleUrls) {
		this.jiraProjectKey = jiraProjectKey;
		this.jiraProjectName = jiraProjectName;
		this.hubProjectName = hubProjectName;
		this.notificationItem = notificationItem;
		this.ruleUrls = ruleUrls;
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

	public List<String> getRuleUrls() {
		return ruleUrls;
	}

	@Override
	public String toString() {
		return "JiraReadyNotification [jiraProjectName=" + jiraProjectName + ", hubProjectName=" + hubProjectName
				+ ", notificationItem=" + notificationItem + "]";
	}

}
