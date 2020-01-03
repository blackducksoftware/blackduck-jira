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

    public static String getInstallDateString(PluginSettings pluginSettings) {
        JiraSettingsAccessor jiraSettingsAccessor = new JiraSettingsAccessor(pluginSettings);
        PluginConfigurationAccessor pluginConfigurationAccessor = jiraSettingsAccessor.createPluginConfigurationAccessor();
        String firstTimeSave = pluginConfigurationAccessor.getFirstTimeSave();
        if (StringUtils.isNotBlank(firstTimeSave)) {
            return firstTimeSave;
        }

        BlackDuckPluginDateFormatter pluginDateFormatter = new BlackDuckPluginDateFormatter();
        return pluginDateFormatter.format(new Date());
    }

    public UpgradeSteps(PluginSettings pluginSettings) {
        this.jiraSettingsAccessor = new JiraSettingsAccessor(pluginSettings);
    }

    // For every upgrade
    public void updateInstallDate(Date installDate) {
        BlackDuckPluginDateFormatter pluginDateFormatter = new BlackDuckPluginDateFormatter();
        String installDateString = pluginDateFormatter.format(installDate);

        PluginConfigurationAccessor pluginConfigurationAccessor = jiraSettingsAccessor.createPluginConfigurationAccessor();

        logger.debug("Updating install date...");
        String previousFirstTimeSave = pluginConfigurationAccessor.getFirstTimeSave();
        pluginConfigurationAccessor.setFirstTimeSave(installDateString);
        logger.debug("The previous install date was: " + previousFirstTimeSave);

        logger.debug("The new install date is: " + pluginConfigurationAccessor.getFirstTimeSave());
    }

    // Delete in V8
    public void upgradeToV6FromAny() {
        GlobalConfigurationAccessor globalConfigurationAccessor = new GlobalConfigurationAccessor(jiraSettingsAccessor);
        boolean vulnerabilityTicketsEnabled = jiraSettingsAccessor.getBooleanValue(PluginConfigKeys.BLACKDUCK_CONFIG_CREATE_VULN_ISSUES_CHOICE, true);

        PluginIssueCreationConfigModel issueCreationConfig = globalConfigurationAccessor.getIssueCreationConfig();
        ProjectMappingConfigModel projectMappingModel = issueCreationConfig.getProjectMapping();

        BlackDuckJiraConfigSerializable config = new BlackDuckJiraConfigSerializable();
        config.setHubProjectMappingsJson(projectMappingModel.getMappingsJson());

        Set<BlackDuckProjectMapping> blackDuckProjectMappings = config.getHubProjectMappings();
        if (null != blackDuckProjectMappings && !blackDuckProjectMappings.isEmpty()) {
            for (BlackDuckProjectMapping mapping : config.getHubProjectMappings()) {
                JiraProject jiraProject = mapping.getJiraProject();
                if (null == jiraProject.isConfiguredForVulnerabilities()) {
                    jiraProject.setConfiguredForVulnerabilities(vulnerabilityTicketsEnabled);
                }
            }
        }

        ProjectMappingConfigModel mappingConfig = new ProjectMappingConfigModel(config.getHubProjectMappingsJson());
        PluginIssueCreationConfigModel newIssueCreationConfig = new PluginIssueCreationConfigModel(issueCreationConfig.getGeneral(), mappingConfig, issueCreationConfig.getTicketCriteria());
        globalConfigurationAccessor.setIssueCreationConfig(newIssueCreationConfig);
    }

    // Delete when customers all upgrade to 4.2.0+
    public void updateOldMappingsIfNeeded() {
        GlobalConfigurationAccessor globalConfigurationAccessor = jiraSettingsAccessor.createGlobalConfigurationAccessor();
        PluginIssueCreationConfigModel issueCreationConfig = globalConfigurationAccessor.getIssueCreationConfig();
        String projectMappingJson = issueCreationConfig.getProjectMapping().getMappingsJson();

        BlackDuckJiraConfigSerializable config = new BlackDuckJiraConfigSerializable();
        if (StringUtils.isBlank(projectMappingJson)) {
            return;
        }

        config.setHubProjectMappingsJson(projectMappingJson);
        if (config.getHubProjectMappings().isEmpty()) {
            return;
        }

        Optional<BlackDuckProjectMapping> blackDuckProjectMappingOptional = config.getHubProjectMappings().stream().findFirst();
        if (blackDuckProjectMappingOptional.isPresent()) {
            BlackDuckProjectMapping blackDuckProjectMapping = blackDuckProjectMappingOptional.get();
            if (null != blackDuckProjectMapping.getJiraProject() && null != blackDuckProjectMapping.getHubProject()) {
                logger.debug("Updating the old project mappings.");
                Set<BlackDuckProjectMapping> newProjectMappings = new HashSet<>();
                for (BlackDuckProjectMapping mapping : config.getHubProjectMappings()) {
                    BlackDuckProjectMapping newMapping = new BlackDuckProjectMapping();
                    newMapping.setJiraProject(mapping.getJiraProject());
                    newMapping.setBlackDuckProjectName(mapping.getHubProject().getProjectName());
                    newProjectMappings.add(newMapping);
                }
                config.setHubProjectMappings(newProjectMappings);

                ProjectMappingConfigModel newProjectMapping = new ProjectMappingConfigModel(config.getHubProjectMappingsJson());
                PluginIssueCreationConfigModel newIssueCreationConfig = new PluginIssueCreationConfigModel(issueCreationConfig.getGeneral(), newProjectMapping, issueCreationConfig.getTicketCriteria());
                globalConfigurationAccessor.setIssueCreationConfig(newIssueCreationConfig);
            }
        }
    }

    public void assignUserToBlackDuckProject() {
        BlackDuckAssignUtil blackDuckAssignUtil = new BlackDuckAssignUtil();
        blackDuckAssignUtil.assignUserToBlackDuckProject(jiraSettingsAccessor.createPluginErrorAccessor(), jiraSettingsAccessor.createGlobalConfigurationAccessor());
    }

}
