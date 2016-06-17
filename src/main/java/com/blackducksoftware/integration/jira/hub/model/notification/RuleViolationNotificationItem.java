package com.blackducksoftware.integration.jira.hub.model.notification;

import com.blackducksoftware.integration.hub.meta.MetaInformation;

public class RuleViolationNotificationItem extends NotificationItem {
	public RuleViolationNotificationContent content;

	public RuleViolationNotificationItem(final MetaInformation meta) {
		super(meta);
	}

	public RuleViolationNotificationContent getContent() {
		return content;
	}

	public void setContent(final RuleViolationNotificationContent content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return "RuleViolationNotificationItem [content=" + content + ", contentType=" + contentType + ", type=" + type
				+ ", createdAt=" + createdAt + ", Meta=" + getMeta() + "]";
	}

}
