/**
 * Hub JIRA Plugin
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
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

    private final String mostRecentJiraVersionSupportedString = "7.3.x";

    private final boolean supported;

    public String getMostRecentJiraVersionSupportedString() {
        return mostRecentJiraVersionSupportedString;
    }

    public JiraVersion() throws ConfigurationException {
        this(new BuildUtilsInfoImpl());
    }

    public JiraVersion(final BuildUtilsInfoImpl serverInfoUtils) throws ConfigurationException {
        final int[] versionNumbers = serverInfoUtils.getVersionNumbers();

        if ((versionNumbers[0] > 7) || ((versionNumbers[0] == 7) && (versionNumbers[1] > 3))) {
            logger.warn("This version of JIRA (" + serverInfoUtils.getVersion()
                    + ") is not supported. Attempting to proceed as if it were JIRA version "
                    + mostRecentJiraVersionSupportedString);
            capabilities.add(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_APPLICATIONUSERS);
            supported = false;
        } else if ((versionNumbers[0] == 7) && ((versionNumbers[1] >= 1) && (versionNumbers[1] <= 3))) {
            logger.debug("This version of JIRA (" + serverInfoUtils.getVersion() + ") is supported.");
            capabilities.add(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_APPLICATIONUSERS);
            supported = true;
        } else {
            final String msg = "This version of JIRA (" + serverInfoUtils.getVersion() + ") is not supported.";
            logger.error(msg);
            throw new ConfigurationException(msg);
        }
    }

    public boolean hasCapability(final JiraCapabilityEnum capability) {
        return capabilities.contains(capability);
    }

    public boolean isSupported() {
        return supported;
    }

}
