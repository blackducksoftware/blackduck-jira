package com.blackducksoftware.integration.jira.hub;

import com.blackducksoftware.integration.jira.hub.model.notification.NotificationItem;

public abstract class NotificationFilter {

	public abstract FilteredNotificationResults handleNotification(NotificationItem notif);
}
