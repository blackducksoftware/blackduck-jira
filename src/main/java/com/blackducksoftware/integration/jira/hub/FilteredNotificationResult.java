package com.blackducksoftware.integration.jira.hub;

import java.util.UUID;

import com.atlassian.jira.user.ApplicationUser;
import com.blackducksoftware.integration.hub.policy.api.PolicyRule;
import com.blackducksoftware.integration.jira.hub.model.notification.NotificationType;

public class FilteredNotificationResult {

	private final String hubProjectName;
	private final String hubProjectVersion;
	private final String hubComponentName;
	private final String hubComponentVersion;
	private final PolicyRule rule;

	private final UUID hubProjectVersionId;
	private final UUID hubComponentId;
	private final UUID hubComponentVersionId;
	private final UUID ruleId;

	private final ApplicationUser jiraUser;
	private final String jiraIssueTypeId;
	private final Long jiraProjectId;
	private final String jiraProjectName;

	private final NotificationType notificationType;

	public FilteredNotificationResult(final String hubProjectName, final String hubProjectVersion,
			final String hubComponentName, final String hubComponentVersion, final PolicyRule rule,
			final UUID hubProjectVersionId, final UUID hubComponentId,
			final UUID hubComponentVersionId, final UUID ruleId, final ApplicationUser jiraUser,
			final String jiraIssueTypeId, final Long jiraProjectId, final String jiraProjectName,
			final NotificationType notificationType) {
		this.hubProjectName = hubProjectName;
		this.hubProjectVersion = hubProjectVersion;
		this.hubComponentName = hubComponentName;
		this.hubComponentVersion = hubComponentVersion;
		this.rule = rule;
		this.hubProjectVersionId = hubProjectVersionId;
		this.hubComponentId = hubComponentId;
		this.hubComponentVersionId = hubComponentVersionId;
		this.ruleId = ruleId;
		this.jiraUser = jiraUser;
		this.jiraIssueTypeId = jiraIssueTypeId;
		this.jiraProjectId = jiraProjectId;
		this.jiraProjectName = jiraProjectName;
		this.notificationType = notificationType;
	}

	public String getHubProjectName() {
		return hubProjectName;
	}

	public String getHubProjectVersion() {
		return hubProjectVersion;
	}

	public String getHubComponentName() {
		return hubComponentName;
	}

	public String getHubComponentVersion() {
		return hubComponentVersion;
	}

	public PolicyRule getRule() {
		return rule;
	}

	public UUID getHubProjectVersionId() {
		return hubProjectVersionId;
	}

	public UUID getHubComponentId() {
		return hubComponentId;
	}

	public UUID getHubComponentVersionId() {
		return hubComponentVersionId;
	}

	public UUID getRuleId() {
		return ruleId;
	}

	public ApplicationUser getJiraUser() {
		return jiraUser;
	}

	public String getJiraIssueTypeId() {
		return jiraIssueTypeId;
	}

	public Long getJiraProjectId() {
		return jiraProjectId;
	}

	public String getJiraProjectName() {
		return jiraProjectName;
	}

	public NotificationType getNotificationType() {
		return notificationType;
	}

	public String getUniquePropertyKey() {
		final StringBuilder keyBuilder = new StringBuilder();
		keyBuilder.append(getJiraProjectId().toString());
		keyBuilder.append(".");
		keyBuilder.append(getHubProjectVersionId().toString());
		keyBuilder.append(".");
		keyBuilder.append(getHubComponentId().toString());
		keyBuilder.append(".");
		if (getHubComponentVersionId() != null) {
			keyBuilder.append(getHubComponentVersionId().toString());
			keyBuilder.append(".");
		}
		keyBuilder.append(getRuleId().toString());
		return keyBuilder.toString();
	}

}
