package com.blackducksoftware.integration.jira.hub;

import java.util.UUID;

import com.blackducksoftware.integration.hub.policy.api.PolicyRule;
import com.blackducksoftware.integration.jira.hub.property.PolicyViolationIssueProperties;
import com.blackducksoftware.integration.jira.issue.EventType;

public class FilteredNotificationResultRule extends FilteredNotificationResult {
	private final PolicyRule rule;
	private final UUID ruleId;

	public FilteredNotificationResultRule(final String hubProjectName, final String hubProjectVersion,
			final String hubComponentName, final String hubComponentVersion,
			final UUID hubProjectVersionId, final UUID hubComponentId,
			final UUID hubComponentVersionId, final String jiraUserName,
			final String jiraIssueTypeId, final Long jiraProjectId, final String jiraProjectName,
			final EventType eventType, final PolicyRule rule, final UUID ruleId) {

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

	@Override
	public String toString() {
		return "FilteredNotificationResultRule [rule=" + rule + ", ruleId=" + ruleId + ", getHubProjectName()="
				+ getHubProjectName() + ", getHubProjectVersion()=" + getHubProjectVersion()
				+ ", getHubComponentName()=" + getHubComponentName() + ", getHubComponentVersion()="
				+ getHubComponentVersion() + ", getHubProjectVersionId()=" + getHubProjectVersionId()
				+ ", getHubComponentId()=" + getHubComponentId() + ", getHubComponentVersionId()="
				+ getHubComponentVersionId() + ", getJiraUserName()=" + getJiraUserName() + ", getJiraIssueTypeId()="
				+ getJiraIssueTypeId() + ", getJiraProjectId()=" + getJiraProjectId() + ", getJiraProjectName()="
				+ getJiraProjectName() + ", getEventType()=" + getEventType() + "]";
	}

	@Override
	public String getIssueSummary() {
		final StringBuilder issueSummary = new StringBuilder();
		issueSummary.append("Black Duck ");
		issueSummary.append(getEventType().getDisplayName());
		issueSummary.append(" detected on Hub Project '");
		issueSummary.append(getHubProjectName());
		issueSummary.append("' / '");
		issueSummary.append(getHubProjectVersion());
		issueSummary.append("', component '");
		issueSummary.append(getHubComponentName());
		issueSummary.append("' / '");
		issueSummary.append(getHubComponentVersion());
		issueSummary.append("'");
		issueSummary.append(" [Rule: '");
		issueSummary.append(getRule().getName());
		issueSummary.append("']");
		return issueSummary.toString();
	}

	@Override
	public String getIssueDescription() {
		final StringBuilder issueDescription = new StringBuilder();
		issueDescription.append("The Black Duck Hub has detected a ");
		issueDescription.append(getEventType().getDisplayName());
		issueDescription.append(" on Hub Project '");
		issueDescription.append(getHubProjectName());
		issueDescription.append("', component '");
		issueDescription.append(getHubComponentName());
		issueDescription.append("' / '");
		issueDescription.append(getHubComponentVersion());
		issueDescription.append("'.");
		issueDescription.append(" The rule violated is: '");
		issueDescription.append(getRule().getName());
		issueDescription.append("'. Rule overridable : ");
		issueDescription.append(getRule().getOverridable());
		return issueDescription.toString();
	}

	@Override
	public PolicyViolationIssueProperties createIssuePropertiesFromJson(final String json) {
		return gson.fromJson(json, PolicyViolationIssueProperties.class);
	}
}
