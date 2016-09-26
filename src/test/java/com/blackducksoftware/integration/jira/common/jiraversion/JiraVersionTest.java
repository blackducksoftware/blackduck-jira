package com.blackducksoftware.integration.jira.common.jiraversion;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.blackducksoftware.integration.jira.mocks.MockBuildUtilsInfoImpl;

public class JiraVersionTest {

	private JiraVersion getJiraVersion(final String version, final int[] versionNumbers) {
		final MockBuildUtilsInfoImpl mockBuildInfo = new MockBuildUtilsInfoImpl();
		mockBuildInfo.setVersion(version);
		mockBuildInfo.setVersionNumbers(versionNumbers);

		final JiraVersion jiraVersion = new JiraVersion(mockBuildInfo);
		return jiraVersion;
	}

	@Test
	public void testJira6_4_0() {
		final int[] versionNumbers = { 6, 4, 0 };
		final JiraVersion jiraVersion = getJiraVersion("6.4.0", versionNumbers);

		assertTrue(jiraVersion.hasCapability(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_USERS));
		assertFalse(jiraVersion.hasCapability(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_APPLICATIONUSERS));
		assertTrue(jiraVersion.hasCapability(JiraCapabilityEnum.CUSTOM_FIELDS_REQUIRE_GENERIC_VALUES));
		assertFalse(jiraVersion.hasCapability(JiraCapabilityEnum.CUSTOM_FIELDS_REQUIRE_ISSUE_TYPES));
	}

	@Test
	public void testJira6_4_1() {
		// Should proceed as if it were 6.4.0 (closest we support)
		final int[] versionNumbers = { 6, 4, 1 };
		final JiraVersion jiraVersion = getJiraVersion("6.4.1", versionNumbers);

		assertTrue(jiraVersion.hasCapability(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_USERS));
		assertFalse(jiraVersion.hasCapability(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_APPLICATIONUSERS));
		assertTrue(jiraVersion.hasCapability(JiraCapabilityEnum.CUSTOM_FIELDS_REQUIRE_GENERIC_VALUES));
		assertFalse(jiraVersion.hasCapability(JiraCapabilityEnum.CUSTOM_FIELDS_REQUIRE_ISSUE_TYPES));
	}

	@Test
	public void testJira6_5() {
		// Should proceed as if it were 6.4.0 (closest we support)
		final int[] versionNumbers = { 6, 5, 0 };
		final JiraVersion jiraVersion = getJiraVersion("6.5.0", versionNumbers);

		assertTrue(jiraVersion.hasCapability(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_USERS));
		assertFalse(jiraVersion.hasCapability(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_APPLICATIONUSERS));
		assertTrue(jiraVersion.hasCapability(JiraCapabilityEnum.CUSTOM_FIELDS_REQUIRE_GENERIC_VALUES));
		assertFalse(jiraVersion.hasCapability(JiraCapabilityEnum.CUSTOM_FIELDS_REQUIRE_ISSUE_TYPES));
	}

	@Test
	public void testJira7_0() {
		final int[] versionNumbers = { 7, 0, 0 };
		final JiraVersion jiraVersion = getJiraVersion("7.0.0", versionNumbers);

		assertFalse(jiraVersion.hasCapability(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_USERS));
		assertTrue(jiraVersion.hasCapability(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_APPLICATIONUSERS));
		assertFalse(jiraVersion.hasCapability(JiraCapabilityEnum.CUSTOM_FIELDS_REQUIRE_GENERIC_VALUES));
		assertTrue(jiraVersion.hasCapability(JiraCapabilityEnum.CUSTOM_FIELDS_REQUIRE_ISSUE_TYPES));
	}

	@Test
	public void testJira7_0_11() {
		final int[] versionNumbers = { 7, 0, 11 };
		final JiraVersion jiraVersion = getJiraVersion("7.0.11", versionNumbers);

		assertFalse(jiraVersion.hasCapability(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_USERS));
		assertTrue(jiraVersion.hasCapability(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_APPLICATIONUSERS));
		assertFalse(jiraVersion.hasCapability(JiraCapabilityEnum.CUSTOM_FIELDS_REQUIRE_GENERIC_VALUES));
		assertTrue(jiraVersion.hasCapability(JiraCapabilityEnum.CUSTOM_FIELDS_REQUIRE_ISSUE_TYPES));
	}

	@Test
	public void testUnsupportedOldVersion() {
		final int[] versionNumbers = { 6, 3, 0 };
		final JiraVersion jiraVersion = getJiraVersion("6.3.0", versionNumbers);

		assertTrue(jiraVersion.hasCapability(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_USERS));
		assertFalse(jiraVersion.hasCapability(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_APPLICATIONUSERS));
		assertTrue(jiraVersion.hasCapability(JiraCapabilityEnum.CUSTOM_FIELDS_REQUIRE_GENERIC_VALUES));
		assertFalse(jiraVersion.hasCapability(JiraCapabilityEnum.CUSTOM_FIELDS_REQUIRE_ISSUE_TYPES));
	}

	@Test
	public void testUnsupportedNewVersion() {
		final int[] versionNumbers = { 8, 0, 0 };
		final JiraVersion jiraVersion = getJiraVersion("8.0.0", versionNumbers);

		assertFalse(jiraVersion.hasCapability(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_USERS));
		assertTrue(jiraVersion.hasCapability(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_APPLICATIONUSERS));
		assertFalse(jiraVersion.hasCapability(JiraCapabilityEnum.CUSTOM_FIELDS_REQUIRE_GENERIC_VALUES));
		assertTrue(jiraVersion.hasCapability(JiraCapabilityEnum.CUSTOM_FIELDS_REQUIRE_ISSUE_TYPES));
	}
}
