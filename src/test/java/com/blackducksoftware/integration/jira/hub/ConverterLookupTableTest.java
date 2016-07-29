package com.blackducksoftware.integration.jira.hub;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.integration.hub.meta.MetaInformation;
import com.blackducksoftware.integration.jira.hub.model.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.RuleViolationNotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.VulnerabilityNotificationItem;

public class ConverterLookupTableTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void test() {
		final ConverterLookupTable table = new ConverterLookupTable(null, null, null, null);

		assertEquals(null, table.getConverter(null));

		NotificationToEventConverter converter = table.getConverter(new VulnerabilityNotificationItem(new MetaInformation(null,
				null, null)));
		assertEquals("com.blackducksoftware.integration.jira.hub.vulnerability.VulnerabilityNotificationConverter",
				converter.getClass().getName());

		converter = table.getConverter(new RuleViolationNotificationItem(new MetaInformation(null, null, null)));
		assertEquals("com.blackducksoftware.integration.jira.hub.policy.PolicyViolationNotificationConverter",
				converter.getClass().getName());

		converter = table.getConverter(new PolicyOverrideNotificationItem(new MetaInformation(null, null, null)));
		assertEquals("com.blackducksoftware.integration.jira.hub.policy.PolicyOverrideNotificationConverter",
				converter.getClass().getName());
	}

}
