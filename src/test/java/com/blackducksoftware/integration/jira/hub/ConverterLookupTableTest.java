package com.blackducksoftware.integration.jira.hub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.integration.hub.meta.MetaInformation;
import com.blackducksoftware.integration.hub.notification.NotificationServiceException;
import com.blackducksoftware.integration.hub.notification.api.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.hub.notification.api.RuleViolationNotificationItem;
import com.blackducksoftware.integration.hub.notification.api.VulnerabilityNotificationItem;
import com.blackducksoftware.integration.jira.task.conversion.ConverterLookupTable;
import com.blackducksoftware.integration.jira.task.conversion.NotificationToEventConverter;

public class ConverterLookupTableTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void test() throws NotificationServiceException {
		final ConverterLookupTable table = new ConverterLookupTable(null, null, null, null, null);

		try {
			assertEquals(null, table.getConverter(null));
			fail("Expected null pointer exception");
		} catch (final NullPointerException e) {
			// expected
		}

		NotificationToEventConverter converter = table.getConverter(new VulnerabilityNotificationItem(new MetaInformation(null,
				null, null)));
		assertEquals("com.blackducksoftware.integration.jira.task.conversion.VulnerabilityNotificationConverter",
				converter.getClass().getName());

		converter = table.getConverter(new RuleViolationNotificationItem(new MetaInformation(null, null, null)));
		assertEquals("com.blackducksoftware.integration.jira.task.conversion.PolicyViolationNotificationConverter",
				converter.getClass().getName());

		converter = table.getConverter(new PolicyOverrideNotificationItem(new MetaInformation(null, null, null)));
		assertEquals("com.blackducksoftware.integration.jira.task.conversion.PolicyOverrideNotificationConverter",
				converter.getClass().getName());
	}

}
