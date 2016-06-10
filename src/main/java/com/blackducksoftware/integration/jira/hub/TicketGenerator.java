package com.blackducksoftware.integration.jira.hub;

import java.util.List;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.item.HubItemsService;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.jira.hub.model.notification.NotificationItem;
import com.blackducksoftware.integration.jira.service.JiraService;
import com.blackducksoftware.integration.jira.service.JiraServiceException;

/**
 * Collects recent notifications from the Hub, and generates JIRA tickets for
 * them.
 * 
 * @author sbillings
 * 
 */
public class TicketGenerator {
	private final HubNotificationService notificationService;
	private final JiraService jiraService;

	public TicketGenerator(RestConnection restConnection, HubIntRestService hub,
			HubItemsService<NotificationItem> hubItemsService, JiraService jiraService) {
		notificationService = new HubNotificationService(restConnection, hub, hubItemsService);
		this.jiraService = jiraService;
	}

	public int generateTicketsForRecentNotifications(NotificationDateRange notificationDateRange)
			throws HubNotificationServiceException, JiraServiceException {
		List<NotificationItem> notifs = notificationService.fetchNotifications(notificationDateRange);
		return jiraService.generateTickets(notifs);
	}
}
