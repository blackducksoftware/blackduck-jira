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
import com.blackducksoftware.integration.hub.project.api.ProjectItem;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.version.api.ReleaseItem;
import com.blackducksoftware.integration.jira.hub.model.notification.NotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.RuleViolationNotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.VulnerabilityNotificationItem;
import com.blackducksoftware.integration.jira.hub.model.project.ProjectVersion;
import com.google.gson.reflect.TypeToken;

import static org.mockito.Mockito.*;

public class HubNotificationServiceTest {

	private static final String END_DATE_STRING = "2016-05-10T00:00:00.000Z";
	private static final String START_DATE_STRING = "2016-05-01T00:00:00.000Z";

	private static HubNotificationService hubNotificationService;
	private static HubItemsService<NotificationItem> mockHubItemsService;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		RestConnection mockRestConnection = mock(RestConnection.class);

		HubIntRestService mockHubIntRestService = mock(HubIntRestService.class);
		mockHubItemsService = mock(HubItemsService.class);
		ReleaseItem mockProjectVersion = mock(ReleaseItem.class);
		when(mockProjectVersion.getLink("project")).thenReturn("http://test.project.url");
		when(mockHubIntRestService.getProjectVersion("http://test.projectVersion.url")).thenReturn(mockProjectVersion);
		hubNotificationService = new HubNotificationService(mockRestConnection, mockHubIntRestService,
				mockHubItemsService);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testFetchNotifications() throws HubNotificationServiceException, URISyntaxException, BDRestException,
			ParseException, IOException, ResourceDoesNotExistException {

		SimpleDateFormat dateFormatter = new SimpleDateFormat(RestConnection.JSON_DATE_FORMAT);
		dateFormatter.setTimeZone(java.util.TimeZone.getTimeZone("Zulu"));

		Date startDate = dateFormatter.parse(START_DATE_STRING);
		System.out.println("startDate: " + startDate.toString());

		Date endDate = dateFormatter.parse(END_DATE_STRING);
		System.out.println("endDate: " + endDate.toString());

		NotificationDateRange dateRange = new NotificationDateRange(startDate, endDate);
		List<NotificationItem> notifs = hubNotificationService.fetchNotifications(dateRange);

		// Verify
		List<String> expectedUrlSegments = new ArrayList<>();
		expectedUrlSegments.add("api");
		expectedUrlSegments.add("notifications");

		Set<SimpleEntry<String, String>> expectedQueryParameters = new HashSet<>();
		expectedQueryParameters.add(new AbstractMap.SimpleEntry<String, String>("startDate", START_DATE_STRING));
		expectedQueryParameters.add(new AbstractMap.SimpleEntry<String, String>("endDate", END_DATE_STRING));
		// TODO this will need to change
		expectedQueryParameters.add(new AbstractMap.SimpleEntry<String, String>("limit", String.valueOf(1000)));

		verify(mockHubItemsService).httpGetItemList(expectedUrlSegments, expectedQueryParameters);
	}

	@Test
	public void testGetProjectUrlFromProjectReleaseUrl() throws HubNotificationServiceException {
		String versionUrl = "http://test.projectVersion.url";

		String projectUrl = hubNotificationService.getProjectUrlFromProjectReleaseUrl(versionUrl);

		assertEquals("http://test.project.url", projectUrl);
	}
}
