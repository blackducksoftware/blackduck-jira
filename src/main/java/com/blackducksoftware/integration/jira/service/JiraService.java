package com.blackducksoftware.integration.jira.service;

import java.util.List;

import com.blackducksoftware.integration.jira.hub.model.notification.NotificationItem;

public class JiraService {
	public int generateTickets(List<NotificationItem> notifs) {
		System.out.println("Generating tickets for " + notifs.size() + " notifications");
		int ticketCount = 0;
		for (NotificationItem notif : notifs) {
			System.out.println("Generating ticket for: " + notif);
			ticketCount++;
		}
		return ticketCount;
	}
}
