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

public class HubJiraTask {
	private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));
	private static final String JIRA_ISSUE_TYPE_NAME_DEFAULT = "Bug";
	private final String hubUrl;
	private final String hubUsername;
	private final String hubPasswordEncrypted;
	private final String hubTimeoutString;
	private final String intervalString;
	private final String jiraIssueTypeName;
	private final String lastRunDateString;
	private final String configJson;
	private final ProjectManager jiraProjectManager;

	private final Date runDate;
	private final String runDateString;
	private final SimpleDateFormat dateFormatter;

	public HubJiraTask(final String hubUrl, final String hubUsername, final String hubPasswordEncrypted,
			final String hubTimeoutString, final String intervalString, final String jiraIssueTypeName,
			final String lastRunDateString,
			final String configJson,
			final ProjectManager jiraProjectManager) {
		this.hubUrl = hubUrl;
		this.hubUsername = hubUsername;
		this.hubPasswordEncrypted = hubPasswordEncrypted;
		this.hubTimeoutString = hubTimeoutString;
		this.intervalString = intervalString;
		if (jiraIssueTypeName != null) {
			this.jiraIssueTypeName = jiraIssueTypeName;
		} else {
			this.jiraIssueTypeName = JIRA_ISSUE_TYPE_NAME_DEFAULT;
		}
		this.lastRunDateString = lastRunDateString;
		this.configJson = configJson;
		this.jiraProjectManager = jiraProjectManager;
		this.runDate = new Date();

		dateFormatter = new SimpleDateFormat(RestConnection.JSON_DATE_FORMAT);
		dateFormatter.setTimeZone(java.util.TimeZone.getTimeZone("Zulu"));
		this.runDateString = dateFormatter.format(runDate);

	}

	/**
	 * Setup, then generate JIRA tickets based on recent notifications
	 *
	 * @return this execution's run date/time string on success, null otherwise
	 */
	public String execute() {

		// Validate input

		if (hubUrl == null || hubUsername == null || hubPasswordEncrypted == null) {
			logger.debug("The Hub connection details have not been configured, therefore there is nothing to do.");
			return null;
		}

		if (configJson == null) {
			logger.debug("HubNotificationCheckTask: Project Mappings not configured, therefore there is nothing to do.");
			return null;
		}

		logger.debug("Last run date: " + lastRunDateString);
		if (lastRunDateString == null) {
			logger.info("No lastRunDate provided; Not doing anything this time, will collect notifications next time");
			return runDateString;
		}

		logger.debug("Hub url / username: " + hubUrl + " / " + hubUsername);
		logger.debug("Interval: " + intervalString);

		final HubJiraConfigSerializable config = new HubJiraConfigSerializable();
		config.setHubProjectMappingsJson(configJson);
		logger.debug("Mappings:");
		for (final HubProjectMapping mapping : config.getHubProjectMappings()) {
			logger.debug(mapping.toString());
		}

		try {

			// Connect to Hub and Jira

			final JiraService jiraService = new JiraService(jiraProjectManager, jiraIssueTypeName);
			final String hubPassword = PasswordDecrypter.decrypt(hubPasswordEncrypted);

			final RestConnection restConnection = new RestConnection(hubUrl);
			restConnection.setCookies(hubUsername, hubPassword);

			if (hubTimeoutString != null) {
				final int hubTimeout = Integer.parseInt(hubTimeoutString);
				logger.debug("Setting Hub timeout to: " + hubTimeout);
				restConnection.setTimeout(hubTimeout);
			}

			final HubIntRestService hub = new HubIntRestService(restConnection);

			final TypeToken<NotificationItem> typeToken = new TypeToken<NotificationItem>() {
			};
			final Map<String, Class<? extends NotificationItem>> typeToSubclassMap = new HashMap<>();
			typeToSubclassMap.put("VULNERABILITY", VulnerabilityNotificationItem.class);
			typeToSubclassMap.put("RULE_VIOLATION", RuleViolationNotificationItem.class);
			typeToSubclassMap.put("POLICY_OVERRIDE", PolicyOverrideNotificationItem.class);
			final HubItemsService<NotificationItem> hubItemsService = new HubItemsService<>(restConnection,
					NotificationItem.class, typeToken, typeToSubclassMap);
			final TicketGenerator ticketGenerator = new TicketGenerator(restConnection, hub, hubItemsService, jiraService);

			final Date lastRunDate = dateFormatter.parse(lastRunDateString);

			logger.debug("Last run date: " + lastRunDate);
			logger.info("Getting Hub notifications from " + lastRunDate + " to " + runDate);

			final NotificationDateRange notificationDateRange = new NotificationDateRange(lastRunDate,
					runDate);

			// Generate Jira Issues based on recent notifications

			ticketGenerator
			.generateTicketsForRecentNotifications(config.getHubProjectMappings(), notificationDateRange);
		} catch (final BDRestException | IllegalArgumentException | EncryptionException | ParseException
				| HubNotificationServiceException | JiraServiceException | URISyntaxException e) {
			logger.error("Error processing Hub notifications or generating JIRA issues: " + e.getMessage(), e);
			return null;
		}
		return runDateString;
	}

}
