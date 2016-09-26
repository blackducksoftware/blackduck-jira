package com.blackducksoftware.integration.jira.common.jiraversion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.integration.hub.logging.IntLogger;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.common.exception.ConfigurationException;

public class JiraVersionTest {
	private static Properties testProperties;
	private static IntLogger logger;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		logger = new HubJiraLogger(Logger.getLogger(JiraVersionTest.class.getName()));
		testProperties = new Properties();
		final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		final InputStream is = classLoader.getResourceAsStream("test.properties");
		try {
			testProperties.load(is);
		} catch (final IOException e) {
			System.err.println("reading test.properties failed!");
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testActualJiraVersion() throws ConfigurationException {
		final JiraVersion jiraVersion = new JiraVersion(logger);
		assertEquals(testProperties.getProperty("TEST_JIRA_VERSION_MAJOR"), String.valueOf(jiraVersion.getMajor()));
		assertEquals(testProperties.getProperty("TEST_JIRA_VERSION_MINOR"), String.valueOf(jiraVersion.getMinor()));
		assertEquals(testProperties.getProperty("TEST_JIRA_VERSION_PATCH"), String.valueOf(jiraVersion.getPatch()));
		assertEquals(testProperties.getProperty("TEST_JIRA_VERSION_STRING"), String.valueOf(jiraVersion.toString()));
	}

	@Test
	public void testJira6_4_0() throws ConfigurationException {
		final JiraVersion jiraVersion = new JiraVersion(logger, "6.4", 6, 4, 0);
		assertEquals(6, jiraVersion.getMajor());
		assertEquals(4, jiraVersion.getMinor());
		assertEquals(0, jiraVersion.getPatch());
		assertEquals("6.4", String.valueOf(jiraVersion.toString()));

		assertTrue(jiraVersion.hasCapability(JiraCapability.GET_SYSTEM_ADMINS_AS_USERS));
		assertFalse(jiraVersion.hasCapability(JiraCapability.GET_SYSTEM_ADMINS_AS_APPLICATIONUSERS));
	}

	@Test
	public void testJira6_4_1() throws ConfigurationException {
		// Should proceed as if it were 6.4.0 (closest we support)
		final JiraVersion jiraVersion = new JiraVersion(logger, "6.4", 6, 4, 0);
		assertEquals(6, jiraVersion.getMajor());
		assertEquals(4, jiraVersion.getMinor());
		assertEquals(0, jiraVersion.getPatch());
		assertEquals("6.4", String.valueOf(jiraVersion.toString()));

		assertTrue(jiraVersion.hasCapability(JiraCapability.GET_SYSTEM_ADMINS_AS_USERS));
		assertFalse(jiraVersion.hasCapability(JiraCapability.GET_SYSTEM_ADMINS_AS_APPLICATIONUSERS));
	}

	@Test
	public void testJira6_5() throws ConfigurationException {
		// Should proceed as if it were 6.4.0 (closest we support)
		final JiraVersion jiraVersion = new JiraVersion(logger, "6.5", 6, 5, 0);
		assertEquals(6, jiraVersion.getMajor());
		assertEquals(5, jiraVersion.getMinor());
		assertEquals(0, jiraVersion.getPatch());
		assertEquals("6.5", String.valueOf(jiraVersion.toString()));

		assertTrue(jiraVersion.hasCapability(JiraCapability.GET_SYSTEM_ADMINS_AS_USERS));
		assertFalse(jiraVersion.hasCapability(JiraCapability.GET_SYSTEM_ADMINS_AS_APPLICATIONUSERS));
	}

	@Test
	public void testJira7_0() throws ConfigurationException {
		final JiraVersion jiraVersion = new JiraVersion(logger, "7.0", 7, 0, 0);
		assertEquals("7", String.valueOf(jiraVersion.getMajor()));
		assertEquals("0", String.valueOf(jiraVersion.getMinor()));
		assertEquals("0", String.valueOf(jiraVersion.getPatch()));
		assertEquals("7.0", String.valueOf(jiraVersion.toString()));

		assertFalse(jiraVersion.hasCapability(JiraCapability.GET_SYSTEM_ADMINS_AS_USERS));
		assertTrue(jiraVersion.hasCapability(JiraCapability.GET_SYSTEM_ADMINS_AS_APPLICATIONUSERS));
	}

	@Test
	public void testJira7_0_11() throws ConfigurationException {
		final JiraVersion jiraVersion = new JiraVersion(logger, "7.0.11", 7, 0, 11);
		assertEquals("7", String.valueOf(jiraVersion.getMajor()));
		assertEquals("0", String.valueOf(jiraVersion.getMinor()));
		assertEquals("11", String.valueOf(jiraVersion.getPatch()));
		assertEquals("7.0.11", String.valueOf(jiraVersion.toString()));

		assertFalse(jiraVersion.hasCapability(JiraCapability.GET_SYSTEM_ADMINS_AS_USERS));
		assertTrue(jiraVersion.hasCapability(JiraCapability.GET_SYSTEM_ADMINS_AS_APPLICATIONUSERS));
	}

	@Test
	public void testUnsupportedOldVersion() {
		try {
			new JiraVersion(logger, "6.3", 6, 3, 0);
			fail("Expected exception");
		} catch (final ConfigurationException e) {
		}

	}

	@Test
	public void testUnsupportedNewVersion() throws ConfigurationException {
		new JiraVersion(logger, "99.99.99", 99, 99, 99);
		// Should log a warning and proceed as if it were most recent supported
		// version
	}
}
