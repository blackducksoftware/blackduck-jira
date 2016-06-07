package com.blackducksoftware.integration.jira.hub.model.notification;

public class RuleViolationNotificationItem extends NotificationItem {
	public RuleViolationNotificationContent content;

	public RuleViolationNotificationContent getContent() {
		return content;
	}

	@Override
	public String toString() {
		return "RuleViolationNotificationItem [content=" + content + ", contentType=" + contentType + ", type=" + type
				+ ", createdAt=" + createdAt + ", Meta=" + getMeta() + "]";
	}

}
