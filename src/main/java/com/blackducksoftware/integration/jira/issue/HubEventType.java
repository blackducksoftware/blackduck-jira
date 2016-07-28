package com.blackducksoftware.integration.jira.issue;

public enum HubEventType {
	POLICY_VIOLATION("Policy Violation"),
	POLICY_OVERRIDE("Policy Override"),
	VULNERABILITY_ADD("Vulnerability Add"),
	VULNERABILITY_UPDATE("Vulnerability Update"),
	VULNERABILITY_DELETE("Vulnerability Delete");
	private final String displayName;

	private HubEventType(final String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
