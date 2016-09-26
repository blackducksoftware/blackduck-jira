package com.blackducksoftware.integration.jira.common.jiraversion;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.atlassian.jira.util.BuildUtilsInfoImpl;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;

/**
 * Provides insight into the capabilities of the current JIRA version.
 *
 * To add a new JIRA version, add a new addVersion() call to buildTable().
 *
 * To add a new capability, add it to the following (which are all in this
 * class): 1. JiraCapability, 2. CapabilitySet, 3. hasCapability().
 *
 * @author sbillings
 *
 */
public class JiraVersion {
	private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));

	private final List<JiraCapabilityEnum> capabilities = new ArrayList<>();

	private final String mostRecentJiraVersionSupportedString = "7.0.0";

	private final String oldestJiraVersionSupportedString = "6.4.0";

	public JiraVersion() {
		this(new BuildUtilsInfoImpl());
	}

	public JiraVersion(final BuildUtilsInfoImpl serverInfoUtils) {
		final int[] versionNumbers = serverInfoUtils.getVersionNumbers();

		if ((versionNumbers[0] >= 7) && (versionNumbers[1] >= 1)) {
			logger.warn("This version of JIRA (" + serverInfoUtils.getVersion()
			+ ") is not supported. Attempting to proceed as if it were JIRA version "
			+ mostRecentJiraVersionSupportedString);
			capabilities.add(JiraCapabilityEnum.CUSTOM_FIELDS_REQUIRE_ISSUE_TYPES);
			capabilities.add(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_APPLICATIONUSERS);
		} else if (versionNumbers[0] >= 7) {
			logger.debug("This version of JIRA (" + serverInfoUtils.getVersion() + ") is supported.");
			capabilities.add(JiraCapabilityEnum.CUSTOM_FIELDS_REQUIRE_ISSUE_TYPES);
			capabilities.add(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_APPLICATIONUSERS);
		} else if ((versionNumbers[0] <= 6) && (versionNumbers[1] < 4)) {
			logger.warn("This version of JIRA (" + serverInfoUtils.getVersion()
			+ ") is not supported. Attempting to proceed as if it were JIRA version "
			+ oldestJiraVersionSupportedString);
			capabilities.add(JiraCapabilityEnum.CUSTOM_FIELDS_REQUIRE_GENERIC_VALUES);
			capabilities.add(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_USERS);
		} else if ((versionNumbers[0] >= 6) && (versionNumbers[1] >= 4)) {
			logger.debug("This version of JIRA (" + serverInfoUtils.getVersion() + ") is supported.");
			capabilities.add(JiraCapabilityEnum.CUSTOM_FIELDS_REQUIRE_GENERIC_VALUES);
			capabilities.add(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_USERS);
		}
	}

	public boolean hasCapability(final JiraCapabilityEnum capability) {
		return capabilities.contains(capability);
	}
}
