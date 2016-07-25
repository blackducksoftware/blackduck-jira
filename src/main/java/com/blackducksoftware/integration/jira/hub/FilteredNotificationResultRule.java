package com.blackducksoftware.integration.jira.hub;

import java.util.UUID;

import com.blackducksoftware.integration.hub.policy.api.PolicyRule;
import com.blackducksoftware.integration.jira.issue.EventType;

public class FilteredNotificationResultRule extends FilteredNotificationResult {
	private final PolicyRule rule;
	private final UUID ruleId;

	public FilteredNotificationResultRule(final String hubProjectName, final String hubProjectVersion,
			final String hubComponentName, final String hubComponentVersion, final PolicyRule rule,
			final UUID hubProjectVersionId, final UUID hubComponentId,
			final UUID hubComponentVersionId,
			final UUID ruleId, final String jiraUserName,
			final String jiraIssueTypeId, final Long jiraProjectId, final String jiraProjectName,
			final EventType eventType) {

		super(hubProjectName, hubProjectVersion, hubComponentName, hubComponentVersion, hubProjectVersionId,
				hubComponentId, hubComponentVersionId, jiraUserName, jiraIssueTypeId, jiraProjectId, jiraProjectName,
				eventType);
		this.rule = rule;
		this.ruleId = ruleId;
	}

	public PolicyRule getRule() {
		return rule;
	}

	public UUID getRuleId() {
		return ruleId;
	}

	@Override
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
