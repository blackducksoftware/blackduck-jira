package com.blackducksoftware.integration.jira.hub;

import java.util.List;
import java.util.Set;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.item.HubItemsService;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.jira.config.HubProjectMapping;
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

	public TicketGenerator(final RestConnection restConnection, final HubIntRestService hub,
			final HubItemsService<NotificationItem> hubItemsService, final JiraService jiraService) {
		notificationService = new HubNotificationService(restConnection, hub, hubItemsService);
		this.jiraService = jiraService;
	}

	public int generateTicketsForRecentNotifications(final Set<HubProjectMapping> hubProjectMappings,
			final List<String> linksOfRulesToMonitor,
			final NotificationDateRange notificationDateRange) throws HubNotificationServiceException, JiraServiceException {

		final List<NotificationItem> notifs = notificationService.fetchNotifications(notificationDateRange);
		final JiraNotificationFilter filter = new JiraNotificationFilter(notificationService, jiraService,
				hubProjectMappings, linksOfRulesToMonitor);
		final List<JiraReadyNotification> jiraReadyNotifs = filter.extractJiraReadyNotifications(notifs);
		return jiraService.generateTickets(jiraReadyNotifs);
	}
}
