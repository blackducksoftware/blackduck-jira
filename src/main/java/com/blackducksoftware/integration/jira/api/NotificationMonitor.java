package com.blackducksoftware.integration.jira.api;

public interface NotificationMonitor {
	public void reschedule(String serverName, long interval);
}
