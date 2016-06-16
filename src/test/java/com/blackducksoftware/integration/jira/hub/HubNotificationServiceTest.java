package com.blackducksoftware.integration.jira.hub;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.ResourceDoesNotExistException;
import com.blackducksoftware.integration.hub.item.HubItemsService;
import com.blackducksoftware.integration.hub.meta.MetaInformation;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.version.api.ReleaseItem;
import com.blackducksoftware.integration.jira.hub.model.NameVersion;
import com.blackducksoftware.integration.jira.hub.model.component.ComponentVersion;
import com.blackducksoftware.integration.jira.hub.model.component.ComponentVersionStatus;
import com.blackducksoftware.integration.jira.hub.model.notification.NotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.NotificationType;
import com.blackducksoftware.integration.jira.hub.model.notification.RuleViolationNotificationContent;
import com.blackducksoftware.integration.jira.hub.model.notification.RuleViolationNotificationItem;

public class HubNotificationServiceTest {

	private static final String TEST_COMPONENT_VERSION_NAME = "testComponentVersionName";
	private static final String TEST_COMPONENT_VERSION_LINK = "testComponentVersionLink";
	private static final String TEST_COMPONENT_NAME = "testComponentName";
	private static final String END_DATE_STRING = "2016-05-10T00:00:00.000Z";
	private static final String START_DATE_STRING = "2016-05-01T00:00:00.000Z";

	private static HubNotificationService hubNotificationService;
	private static HubItemsService<NotificationItem> mockHubItemsService;
	private static RestConnection mockRestConnection;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		mockRestConnection = mock(RestConnection.class);

		final HubIntRestService mockHubIntRestService = mock(HubIntRestService.class);
		mockHubItemsService = mock(HubItemsService.class);
		final ReleaseItem mockProjectVersion = mock(ReleaseItem.class);
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

		final SimpleDateFormat dateFormatter = new SimpleDateFormat(RestConnection.JSON_DATE_FORMAT);
		dateFormatter.setTimeZone(java.util.TimeZone.getTimeZone("Zulu"));

		final Date startDate = dateFormatter.parse(START_DATE_STRING);
		System.out.println("startDate: " + startDate.toString());

		final Date endDate = dateFormatter.parse(END_DATE_STRING);
		System.out.println("endDate: " + endDate.toString());

		final NotificationDateRange dateRange = new NotificationDateRange(startDate, endDate);
		final List<NotificationItem> notifs = hubNotificationService.fetchNotifications(dateRange);

		// Verify
		final List<String> expectedUrlSegments = new ArrayList<>();
		expectedUrlSegments.add("api");
		expectedUrlSegments.add("notifications");

		final Set<SimpleEntry<String, String>> expectedQueryParameters = new HashSet<>();
		expectedQueryParameters.add(new AbstractMap.SimpleEntry<String, String>("startDate", START_DATE_STRING));
		expectedQueryParameters.add(new AbstractMap.SimpleEntry<String, String>("endDate", END_DATE_STRING));
		// TODO this will need to change
		expectedQueryParameters.add(new AbstractMap.SimpleEntry<String, String>("limit", String.valueOf(1000)));

		verify(mockHubItemsService).httpGetItemList(expectedUrlSegments, expectedQueryParameters);
	}

	@Test
	public void testGetProjectUrlFromProjectReleaseUrl() throws HubNotificationServiceException {
		final String versionUrl = "http://test.projectVersion.url";

		final String projectUrl = hubNotificationService.getProjectUrlFromProjectReleaseUrl(versionUrl);

		assertEquals("http://test.project.url", projectUrl);
	}

	@Test
	public void testGetComponents() throws HubNotificationServiceException, ResourceDoesNotExistException,
	URISyntaxException, IOException, BDRestException {
		final MetaInformation meta = null;
		final RuleViolationNotificationItem notif = new RuleViolationNotificationItem(meta);
		final RuleViolationNotificationContent content = new RuleViolationNotificationContent();
		final List<ComponentVersionStatus> componentVersionStatuses = new ArrayList<>();

		for (int i = 0; i < 2; i++) {
			final ComponentVersionStatus compVerStatus = new ComponentVersionStatus();
			compVerStatus.setComponentName(TEST_COMPONENT_NAME + i);
			compVerStatus.setComponentVersionLink(TEST_COMPONENT_VERSION_LINK + i);
			componentVersionStatuses.add(compVerStatus);
		}

		content.setComponentVersionStatuses(componentVersionStatuses);
		notif.setContent(content);
		notif.setContentType("RULE_VIOLATION");
		notif.setCreatedAt(new Date());
		final NotificationType type = NotificationType.RULE_VIOLATION;
		notif.setType(type);

		for (int i = 0; i < 2; i++) {
			final ComponentVersion testComponentVersion = new ComponentVersion(meta);
			testComponentVersion.setVersionName(TEST_COMPONENT_VERSION_NAME + i);

			when(mockRestConnection.httpGetFromAbsoluteUrl(ComponentVersion.class, TEST_COMPONENT_VERSION_LINK + i))
			.thenReturn(testComponentVersion);
		}

		final List<NameVersion> componentVersions = hubNotificationService.getComponents(notif);

		assertEquals(TEST_COMPONENT_NAME + 0, componentVersions.get(0).getName());
		assertEquals(TEST_COMPONENT_VERSION_NAME + 0, componentVersions.get(0).getVersion());

		assertEquals(TEST_COMPONENT_NAME + 1, componentVersions.get(1).getName());
		assertEquals(TEST_COMPONENT_VERSION_NAME + 1, componentVersions.get(1).getVersion());
	}
}
