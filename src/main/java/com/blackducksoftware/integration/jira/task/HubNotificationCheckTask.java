package com.blackducksoftware.integration.jira.task;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.sal.api.scheduling.PluginJob;
import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.config.HubConfigKeys;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.item.HubItemsService;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.jira.hub.HubNotificationService;
import com.blackducksoftware.integration.jira.hub.HubNotificationServiceException;
import com.blackducksoftware.integration.jira.hub.NotificationDateRange;
import com.blackducksoftware.integration.jira.hub.TicketGenerator;
import com.blackducksoftware.integration.jira.hub.model.notification.NotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.RuleViolationNotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.VulnerabilityNotificationItem;
import com.blackducksoftware.integration.jira.impl.HubMonitor;
import com.blackducksoftware.integration.jira.service.JiraService;
import com.blackducksoftware.integration.jira.service.JiraServiceException;
import com.blackducksoftware.integration.jira.utils.HubJiraConfigKeys;
import com.google.gson.reflect.TypeToken;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

/**
 * A scheduled JIRA task that collects recent notifications from the Hub, and
 * generates JIRA tickets for them.
 * 
 * @author sbillings
 * 
 */
public class HubNotificationCheckTask implements PluginJob {

	private final Logger logger = Logger.getLogger(HubNotificationCheckTask.class);
	private final TicketGenerator ticketGenerator;

	public HubNotificationCheckTask() {
		// TODO The code below is temporary / overly hard-coded.
		RestConnection restConnection = new RestConnection("http://eng-hub-valid03.dc1.lan/");
		try {
			restConnection.setCookies("sysadmin", "blackduck");
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		} catch (BDRestException e) {
			throw new IllegalArgumentException(e);
		}
		HubIntRestService hub;
		try {
			hub = new HubIntRestService(restConnection);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}

		final TypeToken<NotificationItem> typeToken = new TypeToken<NotificationItem>() {
		};
		final Map<String, Class<? extends NotificationItem>> typeToSubclassMap = new HashMap<>();
		typeToSubclassMap.put("VULNERABILITY", VulnerabilityNotificationItem.class);
		typeToSubclassMap.put("RULE_VIOLATION", RuleViolationNotificationItem.class);
		typeToSubclassMap.put("POLICY_OVERRIDE", PolicyOverrideNotificationItem.class);

		HubItemsService<NotificationItem> hubItemsService = new HubItemsService<>(restConnection,
				NotificationItem.class, typeToken, typeToSubclassMap);

		JiraService jiraService = new JiraService();
		ticketGenerator = new TicketGenerator(restConnection, hub, hubItemsService, jiraService);

	}

	@Override
	public void execute(Map<String, Object> jobDataMap) {

		SimpleDateFormat dateFormatter = new SimpleDateFormat(RestConnection.JSON_DATE_FORMAT);
		dateFormatter.setTimeZone(java.util.TimeZone.getTimeZone("Zulu"));

		// TODO The code below is temporary / overly hard-coded.
		PluginSettings settings = (PluginSettings) jobDataMap.get(HubMonitor.KEY_SETTINGS);
		System.out.println("Interval: "
				+ getStringValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS));

		String lastRunDateString = getStringValue(settings, HubJiraConfigKeys.LAST_RUN_DATE);
		System.out.println("Last run date: " + lastRunDateString);
		if (lastRunDateString == null) {
			System.out
					.println("No lastRunDate provided; Not doing anything this time, we'll collect notifications next time");
			settings.put(HubJiraConfigKeys.LAST_RUN_DATE, dateFormatter.format(new Date()));
			return;
		}

		Date lastRunDate;
		try {
			lastRunDate = dateFormatter.parse(lastRunDateString);
		} catch (ParseException e1) {
			throw new IllegalArgumentException("Error parsing lastRunDate read from settings: '" + lastRunDateString
					+ "': " + e1.getMessage(), e1);
		}
		System.out.println("Last run date: " + lastRunDate);

		Date startDate = lastRunDate;
		Date endDate = new Date();

		settings.put(HubJiraConfigKeys.LAST_RUN_DATE, dateFormatter.format(endDate));

		System.out.println("Getting notifications from " + startDate + " to " + endDate);

		NotificationDateRange notificationDateRange;
		try {
			notificationDateRange = new NotificationDateRange(startDate, endDate);
		} catch (ParseException e) {
			throw new IllegalArgumentException(e);
		}
		try {
			ticketGenerator.generateTicketsForRecentNotifications(notificationDateRange);
		} catch (HubNotificationServiceException | JiraServiceException e) {
			throw new IllegalArgumentException(e);
		}

	}

	private Object getValue(final PluginSettings settings, final String key) {
		return settings.get(key);
	}

	private String getStringValue(final PluginSettings settings, final String key) {
		return (String) getValue(settings, key);
	}
}
