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
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.BlackDuckPluginDateFormatter;
import com.blackducksoftware.integration.jira.common.model.BlackDuckProjectMapping;
import com.blackducksoftware.integration.jira.common.model.JiraProject;
import com.blackducksoftware.integration.jira.common.settings.GlobalConfigurationAccessor;
import com.blackducksoftware.integration.jira.common.settings.JiraSettingsAccessor;
import com.blackducksoftware.integration.jira.common.settings.PluginConfigKeys;
import com.blackducksoftware.integration.jira.common.settings.model.PluginIssueCreationConfigModel;
import com.blackducksoftware.integration.jira.common.settings.model.ProjectMappingConfigModel;
import com.blackducksoftware.integration.jira.config.PluginConfigurationDetails;
import com.blackducksoftware.integration.jira.config.model.BlackDuckJiraConfigSerializable;

public class UpgradeSteps {
    private final BlackDuckJiraLogger logger;
    private final PluginSettings pluginSettings;

    public static String getInstallDateString(final PluginSettings pluginSettings) {
        return (String) pluginSettings.get(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_FIRST_SAVE_TIME);
    }

    public UpgradeSteps(final BlackDuckJiraLogger logger, final PluginSettings pluginSettings) {
        this.logger = logger;
        this.pluginSettings = pluginSettings;
    }

    // For every upgrade
    public void updateInstallDate(final Date installDate) {
        final String installDateString = BlackDuckPluginDateFormatter.format(installDate);

        logger.debug("Updating install date...");
        final String oldInstallDate = (String) pluginSettings.put(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_FIRST_SAVE_TIME, installDateString);
        logger.debug("The previous install date was: " + oldInstallDate);

        final String newInstallDate = getInstallDateString(pluginSettings);
        logger.debug("The new install date is: " + newInstallDate);
    }

    // Delete in V8
    public void upgradeToV6FromAny() {
        final JiraSettingsAccessor jiraSettingsAccessor = new JiraSettingsAccessor(pluginSettings);
        final GlobalConfigurationAccessor globalConfigurationAccessor = new GlobalConfigurationAccessor(jiraSettingsAccessor);

        final boolean vulnerabilityTicketsEnabled = globalConfigurationAccessor.getVulnerabilityIssuesChoice();

        final PluginIssueCreationConfigModel issueCreationConfig = globalConfigurationAccessor.getIssueCreationConfig();
        final ProjectMappingConfigModel projectMappingModel = issueCreationConfig.getProjectMapping();

        final BlackDuckJiraConfigSerializable config = new BlackDuckJiraConfigSerializable();
        config.setHubProjectMappingsJson(projectMappingModel.getMappingsJson());

        for (final BlackDuckProjectMapping mapping : config.getHubProjectMappings()) {
            final JiraProject jiraProject = mapping.getJiraProject();
            if (null == jiraProject.isConfiguredForVulnerabilities()) {
                jiraProject.setConfiguredForVulnerabilities(vulnerabilityTicketsEnabled);
            }
        }

        final ProjectMappingConfigModel mappingConfig = new ProjectMappingConfigModel(config.getHubProjectMappingsJson());
        final PluginIssueCreationConfigModel newIssueCreationConfig = new PluginIssueCreationConfigModel(issueCreationConfig.getGeneral(), mappingConfig, issueCreationConfig.getTicketCriteria());
        globalConfigurationAccessor.setIssueCreationConfig(newIssueCreationConfig);
    }

    // Delete when customers all upgrade to 4.2.0+
    public void updateOldMappingsIfNeeded() {
        final PluginConfigurationDetails pluginConfigDetails = new PluginConfigurationDetails(pluginSettings);
        final BlackDuckJiraConfigSerializable config = new BlackDuckJiraConfigSerializable();
        if (StringUtils.isBlank(pluginConfigDetails.getProjectMappingJson())) {
            return;
        }

        config.setHubProjectMappingsJson(pluginConfigDetails.getProjectMappingJson());
        if (config.getHubProjectMappings().isEmpty()) {
            return;
        }

        final Optional<BlackDuckProjectMapping> blackDuckProjectMappingOptional = config.getHubProjectMappings().stream().findFirst();
        if (blackDuckProjectMappingOptional.isPresent()) {
            final BlackDuckProjectMapping blackDuckProjectMapping = blackDuckProjectMappingOptional.get();
            if (null != blackDuckProjectMapping.getJiraProject() && null != blackDuckProjectMapping.getHubProject()) {
                logger.debug("Updating the old project mappings.");
                final Set<BlackDuckProjectMapping> newProjectMappings = new HashSet<>();
                for (final BlackDuckProjectMapping mapping : config.getHubProjectMappings()) {
                    final BlackDuckProjectMapping newMapping = new BlackDuckProjectMapping();
                    newMapping.setJiraProject(mapping.getJiraProject());
                    newMapping.setBlackDuckProjectName(mapping.getHubProject().getProjectName());
                    newProjectMappings.add(newMapping);
                }
                config.setHubProjectMappings(newProjectMappings);
                pluginSettings.put(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_PROJECT_MAPPINGS_JSON, config.getHubProjectMappingsJson());
            }
        }
    }

}
