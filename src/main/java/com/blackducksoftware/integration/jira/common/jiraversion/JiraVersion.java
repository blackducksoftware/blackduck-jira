package com.blackducksoftware.integration.jira.common.jiraversion;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.BuildUtilsInfoImpl;
import com.blackducksoftware.integration.hub.logging.IntLogger;
import com.blackducksoftware.integration.jira.common.exception.ConfigurationException;

public class JiraVersion {
	private static final String JIRA_6_4_VERSION_STRING = "6.4";
	private final int jiraVersionMajor;
	private final int jiraVersionMinor;
	private final int jiraVersionPatch;

	private final String jiraVersionString;

	private final Map<String, CapabilitySet> capabilityMatrix = new HashMap<>();
	private final CapabilitySet currentCapabilities;
	private String mostRecentJiraVersionString;

	JiraVersion(final IntLogger logger) throws ConfigurationException {
		this(logger, null, 0, 0, 0);
	}


	public JiraVersion(final IntLogger logger, final String versionString, final int major, final int minor,
			final int patch) throws ConfigurationException {
		addVersion(JIRA_6_4_VERSION_STRING, new CapabilitySet(true, false));
		addVersion("7.0", new CapabilitySet(false, true));

		if (versionString != null) {
			jiraVersionString = versionString;
			jiraVersionMajor = major;
			jiraVersionMinor = minor;
			jiraVersionPatch = patch;
		} else {
			final BuildUtilsInfo jiraVersionInfo = new BuildUtilsInfoImpl();
			final int[] jiraVersionNumberComponents = jiraVersionInfo.getVersionNumbers();
			for (final int jiraVersionNumberComponent : jiraVersionNumberComponents) {
				System.out.println("JIRA Version number component: " + jiraVersionNumberComponent);
			}
			jiraVersionString = jiraVersionInfo.getVersion();
			jiraVersionMajor = getVersionComponent(jiraVersionNumberComponents, 0);
			jiraVersionMinor = getVersionComponent(jiraVersionNumberComponents, 1);
			jiraVersionPatch = getVersionComponent(jiraVersionNumberComponents, 2);
		}

		if (!capabilityMatrix.containsKey(this.jiraVersionString)) {
			if (jiraVersionMajor >= 7) {
				logger.warn("This this version of JIRA (" + this.jiraVersionString
						+ ") is not supported. Attempting to proceed as if it were JIRA version "
						+ mostRecentJiraVersionString);
				currentCapabilities = capabilityMatrix.get(this.mostRecentJiraVersionString);
			} else if ((jiraVersionMajor >= 6) && (jiraVersionMinor >= 4)) {
				logger.warn("This this version of JIRA (" + this.jiraVersionString
						+ ") is not supported. Attempting to proceed as if it were JIRA version "
						+ JIRA_6_4_VERSION_STRING);
				currentCapabilities = capabilityMatrix.get(JIRA_6_4_VERSION_STRING);
			} else {
				final String msg = "This this version of JIRA (" + this.jiraVersionString + ") is not supported.";
				logger.error(msg);
				throw new ConfigurationException(msg);
			}
		} else {
			final String majorDotMinor = String.format("%d.%d", jiraVersionMajor, jiraVersionMinor);
			currentCapabilities = capabilityMatrix.get(majorDotMinor);
		}
	}

	private void addVersion(final String jiraVersionString, final CapabilitySet capabilitySet) {
		capabilityMatrix.put(jiraVersionString, capabilitySet);
		mostRecentJiraVersionString = jiraVersionString;
	}

	private int getVersionComponent(final int[] versionComponents, final int componentIndex) {
		if (versionComponents.length < componentIndex) {
			return 0;
		}
		return versionComponents[componentIndex];
	}

	public int getMajor() {
		return jiraVersionMajor;
	}

	public int getMinor() {
		return jiraVersionMinor;
	}

	public int getPatch() {
		return jiraVersionPatch;
	}

	@Override
	public String toString() {
		return jiraVersionString;
	}

	public boolean hasCapability(final JiraCapability capability) {
		switch (capability) {
		case GET_SYSTEM_ADMINS_AS_USERS:
			return currentCapabilities.isHasGetSystemAdminsAsUsers();
		case GET_SYSTEM_ADMINS_AS_APPLICATIONUSERS:
			return currentCapabilities.isHasGetSystemAdminsAsApplicationUsers();
		}
		throw new UnsupportedOperationException("Invalid capability: " + capability);
	}

	private class CapabilitySet {
		private final boolean hasGetSystemAdminsAsUsers;
		private final boolean hasGetSystemAdminsAsApplicationUsers;

		private CapabilitySet(final boolean hasGetSystemAdminsAsUsers,
				final boolean hasGetSystemAdminsAsApplicationUsers) {
			super();
			this.hasGetSystemAdminsAsUsers = hasGetSystemAdminsAsUsers;
			this.hasGetSystemAdminsAsApplicationUsers = hasGetSystemAdminsAsApplicationUsers;
		}

		private boolean isHasGetSystemAdminsAsUsers() {
			return hasGetSystemAdminsAsUsers;
		}

		private boolean isHasGetSystemAdminsAsApplicationUsers() {
			return hasGetSystemAdminsAsApplicationUsers;
		}


	}
}
