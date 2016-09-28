package com.blackducksoftware.integration.jira.common.jiraversion;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.blackducksoftware.integration.jira.common.exception.ConfigurationException;
import com.blackducksoftware.integration.jira.mocks.MockBuildUtilsInfoImpl;


public class JiraVersionTest {

	private JiraVersion getJiraVersion(final String version, final int[] versionNumbers) throws ConfigurationException {
		final MockBuildUtilsInfoImpl mockBuildInfo = new MockBuildUtilsInfoImpl();
		mockBuildInfo.setVersion(version);
		mockBuildInfo.setVersionNumbers(versionNumbers);

		final JiraVersion jiraVersion = new JiraVersion(mockBuildInfo);
		return jiraVersion;
	}

	@Test
	public void testJira6_4_0() {
		final int[] versionNumbers = { 6, 4, 0 };
		JiraVersion jiraVersion;
		try {
			jiraVersion = getJiraVersion("6.4.0", versionNumbers);
			fail("Expected configuration exception");
		} catch (final ConfigurationException e) {

		}

	}

	@Test
	public void testJira7_0() throws ConfigurationException {
		final int[] versionNumbers = { 7, 0, 0 };
		JiraVersion jiraVersion;
		try {
			jiraVersion = getJiraVersion("6.4.0", versionNumbers);
			fail("Expected configuration exception");
		} catch (final ConfigurationException e) {

		}
	}

	@Test
	public void testJira7_1_0() throws ConfigurationException {
		final int[] versionNumbers = { 7, 1, 0 };
		final JiraVersion jiraVersion = getJiraVersion("7.1.0", versionNumbers);

		assertFalse(jiraVersion.hasCapability(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_USERS));
		assertTrue(jiraVersion.hasCapability(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_APPLICATIONUSERS));
	}

	@Test
	public void testJira7_1_99() throws ConfigurationException {
		final int[] versionNumbers = { 7, 1, 99 };
		final JiraVersion jiraVersion = getJiraVersion("7.1.99", versionNumbers);

		assertFalse(jiraVersion.hasCapability(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_USERS));
		assertTrue(jiraVersion.hasCapability(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_APPLICATIONUSERS));
	}

	@Test
	public void testJira7_2() throws ConfigurationException {
		final int[] versionNumbers = { 7, 2, 0 };
		final JiraVersion jiraVersion = getJiraVersion("7.2", versionNumbers);

		assertFalse(jiraVersion.hasCapability(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_USERS));
		assertTrue(jiraVersion.hasCapability(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_APPLICATIONUSERS));
	}

	@Test
	public void testJira8_0() throws ConfigurationException {
		final int[] versionNumbers = { 8, 0, 0 };
		final JiraVersion jiraVersion = getJiraVersion("8.0", versionNumbers);

		assertFalse(jiraVersion.hasCapability(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_USERS));
		assertTrue(jiraVersion.hasCapability(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_APPLICATIONUSERS));
	}
}
