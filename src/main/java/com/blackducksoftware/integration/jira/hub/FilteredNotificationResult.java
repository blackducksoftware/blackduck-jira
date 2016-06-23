package com.blackducksoftware.integration.jira.hub;

import com.blackducksoftware.integration.jira.hub.model.notification.NotificationType;

public class FilteredNotificationResult {

	private String hubProjectName;
	private String hubProjectVersion;
	private String hubComponentName;
	private String hubComponentVersion;
	private String ruleName;
	private String jiraUserName;
	private String jiraIssueTypeId;
	private Long jiraProjectId;

	private NotificationType notificationType;

	public String getHubProjectName() {
		return hubProjectName;
	}

	public void setHubProjectName(final String hubProjectName) {
		this.hubProjectName = hubProjectName;
	}

	public String getHubProjectVersion() {
		return hubProjectVersion;
	}

	public void setHubProjectVersion(final String hubProjectVersion) {
		this.hubProjectVersion = hubProjectVersion;
	}

	public String getHubComponentName() {
		return hubComponentName;
	}

	public void setHubComponentName(final String hubComponentName) {
		this.hubComponentName = hubComponentName;
	}

	public String getHubComponentVersion() {
		return hubComponentVersion;
	}

	public void setHubComponentVersion(final String hubComponentVersion) {
		this.hubComponentVersion = hubComponentVersion;
	}

	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(final String ruleName) {
		this.ruleName = ruleName;
	}

	public String getJiraUserName() {
		return jiraUserName;
	}

	public void setJiraUserName(final String jiraUserName) {
		this.jiraUserName = jiraUserName;
	}

	public String getJiraIssueTypeId() {
		return jiraIssueTypeId;
	}

	public void setJiraIssueTypeId(final String jiraIssueTypeId) {
		this.jiraIssueTypeId = jiraIssueTypeId;
	}

	public Long getJiraProjectId() {
		return jiraProjectId;
	}

	public void setJiraProjectId(final Long jiraProjectId) {
		this.jiraProjectId = jiraProjectId;
	}

	public NotificationType getNotificationType() {
		return notificationType;
	}

	public void setNotificationType(final NotificationType notificationType) {
		this.notificationType = notificationType;
	}

}
