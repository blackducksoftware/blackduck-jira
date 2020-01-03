/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2020 Synopsys, Inc.
 * https://www.synopsys.com/
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
package com.blackducksoftware.integration.jira.workflow.setup;

import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.blackducksoftware.integration.jira.blackduck.BlackDuckAssignUtil;
import com.blackducksoftware.integration.jira.common.BlackDuckPluginDateFormatter;
import com.blackducksoftware.integration.jira.data.PluginConfigKeys;
import com.blackducksoftware.integration.jira.data.accessor.GlobalConfigurationAccessor;
import com.blackducksoftware.integration.jira.data.accessor.JiraSettingsAccessor;
import com.blackducksoftware.integration.jira.data.accessor.PluginConfigurationAccessor;
import com.blackducksoftware.integration.jira.issue.model.PluginIssueCreationConfigModel;
import com.blackducksoftware.integration.jira.issue.model.ProjectMappingConfigModel;
import com.blackducksoftware.integration.jira.web.model.BlackDuckJiraConfigSerializable;
import com.blackducksoftware.integration.jira.web.model.BlackDuckProjectMapping;
import com.blackducksoftware.integration.jira.web.model.JiraProject;

public class UpgradeSteps {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final JiraSettingsAccessor jiraSettingsAccessor;

    public static String getInstallDateString(final PluginSettings pluginSettings) {
        final JiraSettingsAccessor jiraSettingsAccessor = new JiraSettingsAccessor(pluginSettings);
        final PluginConfigurationAccessor pluginConfigurationAccessor = jiraSettingsAccessor.createPluginConfigurationAccessor();
        final String firstTimeSave = pluginConfigurationAccessor.getFirstTimeSave();
        if (StringUtils.isNotBlank(firstTimeSave)) {
            return firstTimeSave;
        }

        final BlackDuckPluginDateFormatter pluginDateFormatter = new BlackDuckPluginDateFormatter();
        return pluginDateFormatter.format(new Date());
    }

    public UpgradeSteps(final PluginSettings pluginSettings) {
        this.jiraSettingsAccessor = new JiraSettingsAccessor(pluginSettings);
    }

    // For every upgrade
    public void updateInstallDate(final Date installDate) {
        final BlackDuckPluginDateFormatter pluginDateFormatter = new BlackDuckPluginDateFormatter();
        final String installDateString = pluginDateFormatter.format(installDate);

        final PluginConfigurationAccessor pluginConfigurationAccessor = jiraSettingsAccessor.createPluginConfigurationAccessor();

        logger.debug("Updating install date...");
        final String previousFirstTimeSave = pluginConfigurationAccessor.getFirstTimeSave();
        pluginConfigurationAccessor.setFirstTimeSave(installDateString);
        logger.debug("The previous install date was: " + previousFirstTimeSave);

        logger.debug("The new install date is: " + pluginConfigurationAccessor.getFirstTimeSave());
    }

    // Delete in V8
    public void upgradeToV6FromAny() {
        final GlobalConfigurationAccessor globalConfigurationAccessor = new GlobalConfigurationAccessor(jiraSettingsAccessor);
        final boolean vulnerabilityTicketsEnabled = jiraSettingsAccessor.getBooleanValue(PluginConfigKeys.BLACKDUCK_CONFIG_CREATE_VULN_ISSUES_CHOICE, true);

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
        final GlobalConfigurationAccessor globalConfigurationAccessor = jiraSettingsAccessor.createGlobalConfigurationAccessor();
        final PluginIssueCreationConfigModel issueCreationConfig = globalConfigurationAccessor.getIssueCreationConfig();
        final String projectMappingJson = issueCreationConfig.getProjectMapping().getMappingsJson();

        final BlackDuckJiraConfigSerializable config = new BlackDuckJiraConfigSerializable();
        if (StringUtils.isBlank(projectMappingJson)) {
            return;
        }

        config.setHubProjectMappingsJson(projectMappingJson);
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

                final ProjectMappingConfigModel newProjectMapping = new ProjectMappingConfigModel(config.getHubProjectMappingsJson());
                final PluginIssueCreationConfigModel newIssueCreationConfig = new PluginIssueCreationConfigModel(issueCreationConfig.getGeneral(), newProjectMapping, issueCreationConfig.getTicketCriteria());
                globalConfigurationAccessor.setIssueCreationConfig(newIssueCreationConfig);
            }
        }
    }

    public void assignUserToBlackDuckProject() {
        final BlackDuckAssignUtil blackDuckAssignUtil = new BlackDuckAssignUtil();
        blackDuckAssignUtil.assignUserToBlackDuckProject(jiraSettingsAccessor.createPluginErrorAccessor(), jiraSettingsAccessor.createGlobalConfigurationAccessor());
    }

}
