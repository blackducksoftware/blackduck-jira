package com.blackducksoftware.integration.jira.hub.model.notification;

public class PolicyOverrideNotificationItem extends NotificationItem {
	private PolicyOverrideNotificationContent content;

	public PolicyOverrideNotificationContent getContent() {
		return content;
	}

	@Override
	public String toString() {
		return "PolicyOverrideNotificationItem [content=" + content + ", contentType=" + contentType + ", type=" + type
				+ ", createdAt=" + createdAt + ", Meta=" + getMeta() + "]";
	}

}
