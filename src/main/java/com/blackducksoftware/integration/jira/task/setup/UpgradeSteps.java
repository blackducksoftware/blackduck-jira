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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.BlackDuckPluginDateFormatter;
import com.blackducksoftware.integration.jira.common.blackduck.BlackDuckConnectionHelper;
import com.blackducksoftware.integration.jira.common.model.BlackDuckProjectMapping;
import com.blackducksoftware.integration.jira.common.model.JiraProject;
import com.blackducksoftware.integration.jira.common.settings.GlobalConfigurationAccessor;
import com.blackducksoftware.integration.jira.common.settings.JiraSettingsAccessor;
import com.blackducksoftware.integration.jira.common.settings.PluginConfigKeys;
import com.blackducksoftware.integration.jira.common.settings.PluginConfigurationAccessor;
import com.blackducksoftware.integration.jira.common.settings.model.PluginBlackDuckServerConfigModel;
import com.blackducksoftware.integration.jira.common.settings.model.PluginIssueCreationConfigModel;
import com.blackducksoftware.integration.jira.common.settings.model.ProjectMappingConfigModel;
import com.blackducksoftware.integration.jira.config.model.BlackDuckJiraConfigSerializable;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;

public class UpgradeSteps {
    private final BlackDuckJiraLogger logger;
    private final JiraSettingsAccessor jiraSettingsAccessor;

    public static String getInstallDateString(final PluginSettings pluginSettings) {
        final JiraSettingsAccessor jiraSettingsAccessor = new JiraSettingsAccessor(pluginSettings);
        final PluginConfigurationAccessor pluginConfigurationAccessor = jiraSettingsAccessor.createPluginConfigurationAccessor();
        return pluginConfigurationAccessor.getFirstTimeSave();
    }

    public UpgradeSteps(final BlackDuckJiraLogger logger, final PluginSettings pluginSettings) {
        this.logger = logger;
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

    //TODO remove this when Super users in Black Duck no longer need to be assigned to a project in order to receive notifications for that project. Remove when all customers update to the fixed version of BD
    public void assignUserToBlackDuckProject() {
        try {
            final GlobalConfigurationAccessor globalConfigurationAccessor = jiraSettingsAccessor.createGlobalConfigurationAccessor();
            if (null == globalConfigurationAccessor.getIssueCreationConfig() && null == globalConfigurationAccessor.getIssueCreationConfig().getProjectMapping()) {
                logger.debug("There is no issue creation configuration or project mappings. Skipping assigning the user to the BD Project.");
                return;
            }
            final String projectMappingJson = globalConfigurationAccessor.getIssueCreationConfig().getProjectMapping().getMappingsJson();
            if (StringUtils.isBlank(projectMappingJson)) {
                logger.debug("There are no project mappings. Skipping assigning the user to the BD Project.");
                return;
            }
            final BlackDuckJiraConfigSerializable config = new BlackDuckJiraConfigSerializable();
            config.setHubProjectMappingsJson(projectMappingJson);
            if (config.getHubProjectMappings().isEmpty()) {
                logger.debug("There are no project mappings in the mapping json. Skipping assigning the user to the BD Project.");
                return;
            }

            final BlackDuckConnectionHelper blackDuckConnectionHelper = new BlackDuckConnectionHelper();

            final PluginBlackDuckServerConfigModel blackDuckServerConfig = globalConfigurationAccessor.getBlackDuckServerConfig();
            final BlackDuckServicesFactory blackDuckServicesFactory = blackDuckConnectionHelper.createBlackDuckServicesFactory(logger, blackDuckServerConfig.createBlackDuckServerConfigBuilder());

            final BlackDuckService blackDuckService = blackDuckServicesFactory.createBlackDuckService();
            final Set<ProjectView> matchingProjects = getMatchingBDProjects(config.getHubProjectMappings(), blackDuckService);

            if (matchingProjects.isEmpty()) {
                logger.debug("There are no BD projects that map the projects configured in the project mappings. Skipping assigning the user to the BD Project.");
                return;
            }
            final UserView currentUser = blackDuckService.getResponse(ApiDiscovery.CURRENT_USER_LINK_RESPONSE);
            final Set<String> assignedProjects = blackDuckService.getAllResponses(currentUser, UserView.PROJECTS_LINK_RESPONSE)
                                                     .stream()
                                                     .map(assignedProject -> assignedProject.getName())
                                                     .collect(Collectors.toSet());
            final Set<ProjectView> nonAssignedProjects = matchingProjects.stream()
                                                             .filter(project -> !assignedProjects.contains(project.getName()))
                                                             .collect(Collectors.toSet());

            if (nonAssignedProjects.isEmpty()) {
                logger.debug("There are no BD projects that need to have this User assigned to them. Skipping assigning the user to the BD Project.");
                return;
            }

            for (final ProjectView projectView : nonAssignedProjects) {
                // TODO how do we assign users?
            }

        } catch (final IntegrationException e) {
            logger.error("Could not assign the Black Duck user to the configured Black Duck projects. " + e.getMessage(), e);
        }
    }

    private Set<ProjectView> getMatchingBDProjects(final Set<BlackDuckProjectMapping> projectMappings, final BlackDuckService blackDuckService) throws IntegrationException {
        final List<ProjectView> projects = blackDuckService.getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE);
        final Map<String, ProjectView> projectMap = projects.stream().collect(Collectors.toMap(project -> project.getName(), Function.identity()));

        final Set<ProjectView> matchingProjects = new HashSet<>();
        for (final BlackDuckProjectMapping blackDuckProjectMapping : projectMappings) {
            final String blackDuckProjectName = blackDuckProjectMapping.getBlackDuckProjectName();
            if (blackDuckProjectMapping.isProjectPattern()) {
                matchingProjects.addAll(projectMap.entrySet().stream()
                                            .filter(entry -> entry.getKey().matches(blackDuckProjectName))
                                            .map(Map.Entry::getValue)
                                            .collect(Collectors.toSet()));
            } else {
                final ProjectView projectView = projectMap.get(blackDuckProjectName);
                matchingProjects.add(projectView);
            }
        }
        return matchingProjects;
    }

}
