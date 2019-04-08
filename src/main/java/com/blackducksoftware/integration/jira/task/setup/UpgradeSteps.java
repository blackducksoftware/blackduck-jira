/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.jira.task.setup;

import java.util.Date;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.BlackDuckPluginDateFormatter;
import com.blackducksoftware.integration.jira.common.PluginSettingsWrapper;
import com.blackducksoftware.integration.jira.common.model.BlackDuckProjectMapping;
import com.blackducksoftware.integration.jira.common.model.JiraProject;
import com.blackducksoftware.integration.jira.config.JiraConfigDeserializer;
import com.blackducksoftware.integration.jira.config.PluginConfigKeys;
import com.blackducksoftware.integration.jira.config.PluginConfigurationDetails;
import com.blackducksoftware.integration.jira.config.model.BlackDuckJiraConfigSerializable;

public class UpgradeSteps {
    private final BlackDuckJiraLogger logger;

    public UpgradeSteps(final BlackDuckJiraLogger logger) {
        this.logger = logger;
    }

    public void updateInstallDate(final PluginSettings pluginSettings, final Date installDate) {
        final String installDateString = BlackDuckPluginDateFormatter.format(installDate);

        logger.debug("Updating install date...");
        final String oldInstallDate = (String) pluginSettings.put(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_FIRST_SAVE_TIME, installDateString);
        logger.debug("The previous install date was: " + oldInstallDate);

        final String newInstallDate = getInstallDateString(pluginSettings);
        logger.debug("The new install date is: " + newInstallDate);
    }

    public void upgradeToV6FromAny(final PluginSettings pluginSettings) {
        final PluginConfigurationDetails pluginConfigDetails = new PluginConfigurationDetails(pluginSettings);
        final PluginSettingsWrapper settingsWrapper = new PluginSettingsWrapper(pluginSettings);
        final JiraConfigDeserializer configDeserializer = new JiraConfigDeserializer();
        final BlackDuckJiraConfigSerializable config = configDeserializer.deserializeConfig(pluginConfigDetails);
        final boolean vulnerabilityTicketsEnabled = config.isCreateVulnerabilityIssues();

        for (final BlackDuckProjectMapping mapping : config.getHubProjectMappings()) {
            final JiraProject jiraProject = mapping.getJiraProject();
            if (null == jiraProject.isConfiguredForVulnerabilities()) {
                jiraProject.setConfiguredForVulnerabilities(vulnerabilityTicketsEnabled);
            }
        }
        settingsWrapper.setProjectMappingsJson(config.getHubProjectMappingsJson());
    }

    public static String getInstallDateString(final PluginSettings pluginSettings) {
        return (String) pluginSettings.get(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_FIRST_SAVE_TIME);
    }

}
