package com.blackducksoftware.integration.jira.task;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.Date;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.integration.jira.hub.HubNotificationServiceException;
import com.blackducksoftware.integration.jira.hub.NotificationDateRange;

public class NotificationDateRangeTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testWithGivenStartTime() throws ParseException, InterruptedException {
		Date startDate = new Date();
		Thread.sleep(2L);
		NotificationDateRange r = new NotificationDateRange(startDate);
		assertEquals(startDate, r.getStartDate());
		assertTrue(r.getEndDate().getTime() > r.getStartDate().getTime());
	}

	@Test
	public void testWithoutGivenStartTime() throws HubNotificationServiceException {
		NotificationDateRange r = new NotificationDateRange();
		assertTrue(r.getEndDate().getTime() > r.getStartDate().getTime());
	}
}
