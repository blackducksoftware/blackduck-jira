package com.blackducksoftware.integration.jira.hub;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.ResourceDoesNotExistException;
import com.blackducksoftware.integration.hub.item.HubItemsService;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.jira.hub.model.notification.NotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.RuleViolationNotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.VulnerabilityNotificationItem;
import com.google.gson.reflect.TypeToken;

import static org.mockito.Mockito.*;

public class HubNotificationServiceTest {

	private static final String END_DATE_STRING = "2016-05-10T00:00:00.000Z";
	private static final String START_DATE_STRING = "2016-05-01T00:00:00.000Z";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void test() throws HubNotificationServiceException, URISyntaxException, BDRestException, ParseException,
			IOException, ResourceDoesNotExistException {

		SimpleDateFormat dateFormatter = new SimpleDateFormat(RestConnection.JSON_DATE_FORMAT);

		Date startDate = dateFormatter.parse(START_DATE_STRING);
		System.out.println("startDate: " + startDate.toString());

		Date endDate = dateFormatter.parse(END_DATE_STRING);
		System.out.println("endDate: " + endDate.toString());

		int limit = 10;

		RestConnection mockRestConnection = mock(RestConnection.class);

		HubIntRestService mockHubIntRestService = mock(HubIntRestService.class);
		HubItemsService<NotificationItem> mockHubItemsService = mock(HubItemsService.class);
		HubNotificationService hubNotificationService = new HubNotificationService(mockRestConnection,
				mockHubIntRestService, mockHubItemsService);

		List<NotificationItem> notifs = hubNotificationService.getNotifications(startDate, endDate, limit);

		// Verify
		List<String> expectedUrlSegments = new ArrayList<>();
		expectedUrlSegments.add("api");
		expectedUrlSegments.add("notifications");

		Set<SimpleEntry<String, String>> expectedQueryParameters = new HashSet<>();
		expectedQueryParameters.add(new AbstractMap.SimpleEntry<String, String>("startDate", START_DATE_STRING));
		expectedQueryParameters.add(new AbstractMap.SimpleEntry<String, String>("endDate", END_DATE_STRING));
		expectedQueryParameters.add(new AbstractMap.SimpleEntry<String, String>("limit", String.valueOf(limit)));

		verify(mockHubItemsService).httpGetItemList(expectedUrlSegments, expectedQueryParameters);

		for (NotificationItem notif : notifs) {
			System.out.println(notif);
		}
	}
}
