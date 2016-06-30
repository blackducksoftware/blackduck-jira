package com.blackducksoftware.integration.jira.hub.model.notification;

public enum NotificationType {

	POLICY_VIOLATION("Policy Violation"), POLICY_OVERRIDE("Policy Override"), VULNERABILITY("Vulnerability");

	private final String displayName;

	private NotificationType(final String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
