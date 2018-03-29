/**
 * Hub JIRA Plugin
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.jira.common.jiraversion;

import org.apache.log4j.Logger;

import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.BuildUtilsInfoImpl;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.common.exception.ConfigurationException;

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
public class JiraVersionCheck {
    private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final JiraVersion minJiraVersion = new JiraVersion("7.1", 7, 1);

    private final JiraVersion maxJiraVersion = new JiraVersion("7.7", 7, 7);

    private final JiraVersion currentJiraVersion;

    private final boolean supported;

    public JiraVersionCheck() throws ConfigurationException {
        this(new BuildUtilsInfoImpl());
    }

    public boolean isSupported() {
        return supported;
    }

    public String getMostRecentJiraVersionSupportedString() {
        return maxJiraVersion.getName();
    }

    public JiraVersionCheck(final BuildUtilsInfo currentJiraVersionInfo) throws ConfigurationException {
        final int[] jiraVersionNumberComponents = currentJiraVersionInfo.getVersionNumbers();
        final int jiraVersionMajor = getVersionComponent(jiraVersionNumberComponents, 0);
        final int jiraVersionMinor = getVersionComponent(jiraVersionNumberComponents, 1);
        currentJiraVersion = new JiraVersion(currentJiraVersionInfo.getVersion(),
                jiraVersionMajor, jiraVersionMinor);

        final JiraVersionComparator comparator = new JiraVersionComparator();
        if (comparator.compare(currentJiraVersion, minJiraVersion) >= 0) {
            if (comparator.compare(currentJiraVersion, maxJiraVersion) > 0) {
                logger.warn("This version of JIRA (" + currentJiraVersion.getName()
                        + ") is not supported. Attempting to proceed as if it were JIRA version "
                        + maxJiraVersion.getName());
                supported = false;
            } else {
                logger.debug(String.format("This version of JIRA (%s) is supported.", currentJiraVersion.getName()));
                supported = true;
            }
        } else {
            final String msg = "This version of JIRA (" + currentJiraVersion.getName() + ") is not supported.";
            logger.error(msg);
            throw new ConfigurationException(msg);
        }
    }

    private int getVersionComponent(final int[] versionComponents, final int componentIndex) {
        if (versionComponents.length < componentIndex) {
            return 0;
        }
        return versionComponents[componentIndex];
    }
}
