package com.blackducksoftware.integration.jira.hub;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.integration.jira.hub.model.notification.NotificationItem;
import static org.mockito.Mockito.*;

public class RecentNotificationsTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * TODO : This test will likely need to be expanded to test retry logic in
	 * class under test. RIght now this test is pretty meaningless.
	 * 
	 * @throws HubNotificationServiceException
	 */
	@Test
	public void test() throws HubNotificationServiceException {
		HubNotificationService mockNotificationService = mock(HubNotificationService.class);

		NotificationDateRange notificationDateRange = new NotificationDateRange();
		RecentNotifications recentNotifs = new RecentNotifications(mockNotificationService, notificationDateRange);

		recentNotifs.fetchNotifications();

		// verify
		verify(mockNotificationService).getNotifications(notificationDateRange, 100);
	}
}