package com.blackducksoftware.integration.jira.hub.model.notification;

import com.blackducksoftware.integration.hub.meta.MetaInformation;

public class PolicyOverrideNotificationItem extends NotificationItem {
	public PolicyOverrideNotificationItem(final MetaInformation meta) {
		super(meta);
	}

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
