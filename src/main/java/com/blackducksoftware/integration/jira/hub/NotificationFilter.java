package com.blackducksoftware.integration.jira.hub;

import com.blackducksoftware.integration.jira.hub.model.notification.NotificationItem;

public abstract class NotificationFilter {

	// TODO: Better name for this? What does handle mean??
	public abstract FilteredNotificationResults handleNotification(NotificationItem notif);
}
