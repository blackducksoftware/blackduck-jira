package com.blackducksoftware.integration.jira.hub;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.item.HubItemsService;
import com.blackducksoftware.integration.hub.policy.api.PolicyRule;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.jira.hub.model.notification.NotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.RuleViolationNotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.VulnerabilityNotificationItem;
import com.blackducksoftware.integration.jira.service.JiraService;
import com.blackducksoftware.integration.jira.service.JiraServiceException;
import com.google.gson.reflect.TypeToken;

/**
 * DELETE THIS WHOLE CLASS; I'm just using them temporarily during development.
 *
 * @author sbillings
 *
 */
public class TicketGeneratorIT {
	private static final String END_DATE_STRING = "2016-05-02T00:00:00.000Z";
	private static final String START_DATE_STRING = "2016-05-01T00:00:00.000Z";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testGetRule() throws URISyntaxException, BDRestException, HubNotificationServiceException {
		final RestConnection restConnection = new RestConnection("http://eng-hub-valid03.dc1.lan/");
		restConnection.setCookies("sysadmin", "blackduck");
		final HubIntRestService hub = new HubIntRestService(restConnection);

		final TypeToken<NotificationItem> typeToken = new TypeToken<NotificationItem>() {
		};
		final Map<String, Class<? extends NotificationItem>> typeToSubclassMap = new HashMap<>();
		typeToSubclassMap.put("VULNERABILITY", VulnerabilityNotificationItem.class);
		typeToSubclassMap.put("RULE_VIOLATION", RuleViolationNotificationItem.class);
		typeToSubclassMap.put("POLICY_OVERRIDE", PolicyOverrideNotificationItem.class);

		final HubItemsService<NotificationItem> hubItemsService = new HubItemsService<>(restConnection,
				NotificationItem.class, typeToken, typeToSubclassMap);

		final HubNotificationService svc = new HubNotificationService(restConnection, hub, hubItemsService);

		final PolicyRule rule = svc
				.getPolicyRule("http://eng-hub-valid03.dc1.lan/api/policy-rules/b3438c66-77d8-4a10-83b0-40f474e02b06");
		System.out.println(rule);
	}

	@Test
	public void test() throws URISyntaxException, ParseException, HubNotificationServiceException, BDRestException,
	JiraServiceException {
		final RestConnection restConnection = new RestConnection("http://eng-hub-valid03.dc1.lan/");
		restConnection.setCookies("sysadmin", "blackduck");
		final HubIntRestService hub = new HubIntRestService(restConnection);

		final TypeToken<NotificationItem> typeToken = new TypeToken<NotificationItem>() {
		};
		final Map<String, Class<? extends NotificationItem>> typeToSubclassMap = new HashMap<>();
		typeToSubclassMap.put("VULNERABILITY", VulnerabilityNotificationItem.class);
		typeToSubclassMap.put("RULE_VIOLATION", RuleViolationNotificationItem.class);
		typeToSubclassMap.put("POLICY_OVERRIDE", PolicyOverrideNotificationItem.class);

		final HubItemsService<NotificationItem> hubItemsService = new HubItemsService<>(restConnection,
				NotificationItem.class, typeToken, typeToSubclassMap);

		final JiraService jiraService = new JiraService(null, "http://bds00829:2990/jira", "Bug"); // TODO
		// this
		// won't
		// work
		final TicketGenerator ticketGenerator = new TicketGenerator(restConnection, hub, hubItemsService, jiraService);

		final SimpleDateFormat dateFormatter = new SimpleDateFormat(RestConnection.JSON_DATE_FORMAT);

		final Date startDate = dateFormatter.parse(START_DATE_STRING);
		System.out.println("startDate: " + startDate.toString());

		final Date endDate = dateFormatter.parse(END_DATE_STRING);
		System.out.println("endDate: " + endDate.toString());

		final NotificationDateRange notificationDateRange = new NotificationDateRange(startDate, endDate);

		// TODO have to pass in the mappings
		final int ticketCount = ticketGenerator
				.generateTicketsForRecentNotifications(null, null, notificationDateRange);

		assertEquals(100, ticketCount);
	}

}
