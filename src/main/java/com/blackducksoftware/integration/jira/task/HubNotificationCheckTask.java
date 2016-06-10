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
import com.blackducksoftware.integration.jira.service.JiraService;
import com.blackducksoftware.integration.jira.service.JiraServiceException;
import com.google.gson.reflect.TypeToken;

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

		// TODO The code below is temporary / overly hard-coded.

		SimpleDateFormat dateFormatter = new SimpleDateFormat(RestConnection.JSON_DATE_FORMAT);

		final String END_DATE_STRING = "2016-05-10T00:00:00.000Z";
		final String START_DATE_STRING = "2016-05-01T00:00:00.000Z";
		Date startDate;
		Date endDate;
		try {
			startDate = dateFormatter.parse(START_DATE_STRING);
			endDate = dateFormatter.parse(END_DATE_STRING);
		} catch (ParseException e) {
			throw new IllegalArgumentException(e);
		}

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
}
