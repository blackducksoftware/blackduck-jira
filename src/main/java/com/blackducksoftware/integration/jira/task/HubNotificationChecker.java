package com.blackducksoftware.integration.jira.task;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.project.ProjectManager;
import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.encryption.PasswordDecrypter;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.EncryptionException;
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
import com.blackducksoftware.integration.jira.service.JiraService;
import com.blackducksoftware.integration.jira.service.JiraServiceException;
import com.google.gson.reflect.TypeToken;

public class HubNotificationChecker {
	private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));
	private final String hubUrl;
	private final String hubUsername;
	private final String hubPasswordEncrypted;
	private final String hubTimeoutString;
	private final String intervalString;
	private final String lastRunDateString;
	private final String configJson;
	private final ProjectManager jiraProjectManager;

	private final Date runDate;
	private final String runDateString;
	private final SimpleDateFormat dateFormatter;

	public HubNotificationChecker(final String hubUrl, final String hubUsername, final String hubPasswordEncrypted,
			final String hubTimeoutString, final String intervalString, final String lastRunDateString,
			final String configJson,
			final ProjectManager jiraProjectManager) {
		this.hubUrl = hubUrl;
		this.hubUsername = hubUsername;
		this.hubPasswordEncrypted = hubPasswordEncrypted;
		this.hubTimeoutString = hubTimeoutString;
		this.intervalString = intervalString;
		this.lastRunDateString = lastRunDateString;
		this.configJson = configJson;
		this.jiraProjectManager = jiraProjectManager;
		this.runDate = new Date();

		dateFormatter = new SimpleDateFormat(RestConnection.JSON_DATE_FORMAT);
		dateFormatter.setTimeZone(java.util.TimeZone.getTimeZone("Zulu"));
		this.runDateString = dateFormatter.format(runDate);
	}

	/**
	 *
	 * @return this execution's run date/time string on success, null otherwise
	 */
	public String check() {

		final JiraService jiraService = new JiraService(jiraProjectManager);

		logger.debug("Hub url / username: " + hubUrl + " / " + hubUsername);

		if (hubUrl == null || hubUsername == null || hubPasswordEncrypted == null) {
			logger.debug("The Hub connection details have not been configured; Exiting");
			return null;
		}

		String hubPassword;
		try {
			hubPassword = PasswordDecrypter.decrypt(hubPasswordEncrypted);
		} catch (IllegalArgumentException | EncryptionException e2) {
			logger.error("Error decrypting Hub password", e2);
			return null;
		}

		final RestConnection restConnection = new RestConnection(hubUrl);
		try {
			restConnection.setCookies(hubUsername, hubPassword);

		} catch (final URISyntaxException e) {
			throw new IllegalArgumentException(e);
		} catch (final BDRestException e) {
			throw new IllegalArgumentException(e);
		}

		if (hubTimeoutString != null) {
			final int hubTimeout = Integer.parseInt(hubTimeoutString);
			logger.debug("Setting Hub timeout to: " + hubTimeout);
			restConnection.setTimeout(hubTimeout);
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
		final TicketGenerator ticketGenerator = new TicketGenerator(restConnection, hub, hubItemsService, jiraService);

		logger.debug("Interval: " + intervalString);

		logger.debug("Last run date: " + lastRunDateString);
		if (lastRunDateString == null) {
			logger.info("No lastRunDate provided; Not doing anything this time, will collect notifications next time");
			return runDateString;
		}


		if (configJson == null) {
			logger.info("HubNotificationCheckTask: Project Mappings not configured. Nothing to do.");
			return null;
		}
		final HubJiraConfigSerializable config = new HubJiraConfigSerializable();
		config.setHubProjectMappingsJson(configJson);
		logger.debug("Mappings:");
		for (final HubProjectMapping mapping : config.getHubProjectMappings()) {
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
		logger.info("Getting Hub notifications from " + lastRunDate + " to " + runDate);

		NotificationDateRange notificationDateRange;
		try {
			notificationDateRange = new NotificationDateRange(lastRunDate, runDate);
		} catch (final ParseException e) {
			throw new IllegalArgumentException(e);
		}
		try {
			ticketGenerator
			.generateTicketsForRecentNotifications(config.getHubProjectMappings(), notificationDateRange);
		} catch (HubNotificationServiceException | JiraServiceException e) {
			throw new IllegalArgumentException(e);
		}
		return runDateString;
	}

}
