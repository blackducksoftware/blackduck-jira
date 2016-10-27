package com.blackducksoftware.integration.jira.common.jiraversion;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.atlassian.jira.util.BuildUtilsInfoImpl;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.common.exception.ConfigurationException;

/**
 * Provides insight into the capabilities of the current JIRA version.
 *
 * @author sbillings
 *
 */
public class JiraVersion {
    private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final List<JiraCapabilityEnum> capabilities = new ArrayList<>();

    private final String mostRecentJiraVersionSupportedString = "7.1.0";

    public JiraVersion() throws ConfigurationException {
        this(new BuildUtilsInfoImpl());
    }

    public JiraVersion(final BuildUtilsInfoImpl serverInfoUtils) throws ConfigurationException {
        final int[] versionNumbers = serverInfoUtils.getVersionNumbers();

        if ((versionNumbers[0] > 7) || ((versionNumbers[0] == 7) && (versionNumbers[1] > 1))) {
            logger.warn("This version of JIRA (" + serverInfoUtils.getVersion()
                    + ") is not supported. Attempting to proceed as if it were JIRA version "
                    + mostRecentJiraVersionSupportedString);
            capabilities.add(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_APPLICATIONUSERS);
        } else if ((versionNumbers[0] == 7) && (versionNumbers[1] == 1)) {
            logger.debug("This version of JIRA (" + serverInfoUtils.getVersion() + ") is supported.");
            capabilities.add(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_APPLICATIONUSERS);
        } else {
            final String msg = "This version of JIRA (" + serverInfoUtils.getVersion() + ") is not supported.";
            logger.error(msg);
            throw new ConfigurationException(msg);
        }
    }

    public boolean hasCapability(final JiraCapabilityEnum capability) {
        return capabilities.contains(capability);
    }
}
