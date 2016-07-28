package com.blackducksoftware.integration.jira.hub;

import com.blackducksoftware.integration.jira.hub.model.notification.NotificationItem;

public abstract class NotificationFilter {
	private final HubNotificationService hubNotificationService;

	public NotificationFilter(final HubNotificationService hubNotificationService) {
		this.hubNotificationService = hubNotificationService;
	}
	public abstract HubEvents generateEvents(NotificationItem notif);

	protected HubNotificationService getHubNotificationService() {
		return hubNotificationService;
	}

}
