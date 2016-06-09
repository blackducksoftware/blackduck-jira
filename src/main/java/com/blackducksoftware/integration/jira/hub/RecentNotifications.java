package com.blackducksoftware.integration.jira.hub;

import java.util.List;

import com.blackducksoftware.integration.jira.hub.model.notification.NotificationItem;

/**
 * This class is used to access a set of notifications in a reliable way,
 * defined by a date/time range. It knows how to fetch them from the hub,
 * managing chunking (a strategy for avoiding erros) and retrying after certain
 * failures (failures that result from asking for too many at once).
 * 
 * @author sbillings
 * 
 */
public class RecentNotifications {
	private final NotificationDateRange notificationDateRange;
	private final HubNotificationService notificationService;

	public RecentNotifications(HubNotificationService notificationService, NotificationDateRange notificationDateRange) {
		this.notificationDateRange = notificationDateRange;
		this.notificationService = notificationService;
	}

	public List<NotificationItem> fetchNotifications() throws HubNotificationServiceException {
		int limit = 100; // TODO might actually need to get in chunks
		return notificationService.getNotifications(notificationDateRange, limit);
	}
}
