package com.blackducksoftware.integration.jira.task.conversion.output;

public enum HubEventType {
	POLICY_VIOLATION("Policy Violation"),
	POLICY_OVERRIDE("Policy Override"),
 VULNERABILITY_STATUS_CHANGE(
			"Vulnerability Status Change");

	private final String displayName;

	private HubEventType(final String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
