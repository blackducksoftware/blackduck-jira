package com.blackducksoftware.integration.jira.hub.model.notification;

import java.util.Date;

import com.blackducksoftware.integration.hub.item.HubItem;
import com.blackducksoftware.integration.hub.meta.MetaInformation;

public class NotificationItem extends HubItem {
	public NotificationItem(final MetaInformation meta) {
		super(meta);
	}

	public String contentType;
	public NotificationType type;
	public Date createdAt;

	public String getContentType() {
		return contentType;
	}

	public NotificationType getType() {
		return type;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setContentType(final String contentType) {
		this.contentType = contentType;
	}

	public void setType(final NotificationType type) {
		this.type = type;
	}

	public void setCreatedAt(final Date createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public String toString() {
		return "NotificationItem [contentType=" + contentType + ", type=" + type + ", createdAt=" + createdAt
				+ ", Meta=" + getMeta() + "]";
	}

}
