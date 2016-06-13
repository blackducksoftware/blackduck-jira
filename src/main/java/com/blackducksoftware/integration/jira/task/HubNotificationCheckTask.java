package com.blackducksoftware.integration.jira.task;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.project.ProjectManager;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.scheduling.PluginJob;
import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.item.HubItemsService;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.jira.HubJiraLogger;
import com.blackducksoftware.integration.jira.config.HubJiraConfigSerializable;
import com.blackducksoftware.integration.jira.config.HubProjectMapping;
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

/**
 * A scheduled JIRA task that collects recent notifications from the Hub, and
 * generates JIRA tickets for them.
 * 
 * @author sbillings
 * 
 */
public class HubNotificationCheckTask implements PluginJob {

	private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));
	private final TicketGenerator ticketGenerator;
	private final JiraService jiraService;

	public HubNotificationCheckTask() {
		// TODO The code below is temporary / overly hard-coded.
		final RestConnection restConnection = new RestConnection("http://eng-hub-valid03.dc1.lan/");
		try {
			restConnection.setCookies("sysadmin", "blackduck");
		} catch (final URISyntaxException e) {
			throw new IllegalArgumentException(e);
		} catch (final BDRestException e) {
			throw new IllegalArgumentException(e);
		}
		HubIntRestService hub;
		try {
			hub = new HubIntRestService(restConnection);
		} catch (final URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}

		final TypeToken<NotificationItem> typeToken = new TypeToken<NotificationItem>() {
		};
		final Map<String, Class<? extends NotificationItem>> typeToSubclassMap = new HashMap<>();
		typeToSubclassMap.put("VULNERABILITY", VulnerabilityNotificationItem.class);
		typeToSubclassMap.put("RULE_VIOLATION", RuleViolationNotificationItem.class);
		typeToSubclassMap.put("POLICY_OVERRIDE", PolicyOverrideNotificationItem.class);

		final HubItemsService<NotificationItem> hubItemsService = new HubItemsService<>(restConnection,
				NotificationItem.class, typeToken, typeToSubclassMap);

		jiraService = new JiraService();
		ticketGenerator = new TicketGenerator(restConnection, hub, hubItemsService, jiraService);

	}

	@Override
	public void execute(final Map<String, Object> jobDataMap) {

		final SimpleDateFormat dateFormatter = new SimpleDateFormat(RestConnection.JSON_DATE_FORMAT);
		dateFormatter.setTimeZone(java.util.TimeZone.getTimeZone("Zulu"));

		final ProjectManager jiraProjectManager = (ProjectManager) jobDataMap.get(HubMonitor.KEY_PROJECT_MANAGER);
		jiraService.setJiraProjectManager(jiraProjectManager);

		// TODO The code below is temporary / overly hard-coded.
		final PluginSettings settings = (PluginSettings) jobDataMap.get(HubMonitor.KEY_SETTINGS);
		logger.debug("Interval: " + getStringValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS));

		final String lastRunDateString = getStringValue(settings, HubJiraConfigKeys.LAST_RUN_DATE);
		logger.debug("Last run date: " + lastRunDateString);
		if (lastRunDateString == null) {
			logger.info("No lastRunDate provided; Not doing anything this time, will collect notifications next time");
			settings.put(HubJiraConfigKeys.LAST_RUN_DATE, dateFormatter.format(new Date()));
			return;
		}

		String configJson = getStringValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_PROJECT_MAPPINGS_JSON);
		if (configJson == null) {
			logger.info("HubNotificationCheckTask: Project Mappings not configured. Nothing to do.");
			return;
		}
		HubJiraConfigSerializable config = new HubJiraConfigSerializable();
		config.setHubProjectMappingsJson(configJson);
		logger.debug("Mappings:");
		for (HubProjectMapping mapping : config.getHubProjectMappings()) {
			logger.debug(mapping.toString());
		}

		Date lastRunDate;
		try {
			lastRunDate = dateFormatter.parse(lastRunDateString);
		} catch (final ParseException e1) {
			throw new IllegalArgumentException("Error parsing lastRunDate read from settings: '" + lastRunDateString
					+ "': " + e1.getMessage(), e1);
		}
		logger.debug("Last run date: " + lastRunDate);

		final Date startDate = lastRunDate;
		final Date endDate = new Date();

		settings.put(HubJiraConfigKeys.LAST_RUN_DATE, dateFormatter.format(endDate));

		logger.info("Getting Hub notifications from " + startDate + " to " + endDate);

		NotificationDateRange notificationDateRange;
		try {
			notificationDateRange = new NotificationDateRange(startDate, endDate);
		} catch (final ParseException e) {
			throw new IllegalArgumentException(e);
		}
		try {
			ticketGenerator
					.generateTicketsForRecentNotifications(config.getHubProjectMappings(), notificationDateRange);
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
