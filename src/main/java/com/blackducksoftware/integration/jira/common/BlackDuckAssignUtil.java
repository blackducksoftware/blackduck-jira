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
package com.blackducksoftware.integration.jira.common;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.blackducksoftware.integration.jira.common.blackduck.BlackDuckConnectionHelper;
import com.blackducksoftware.integration.jira.common.model.BlackDuckProjectMapping;
import com.blackducksoftware.integration.jira.common.settings.GlobalConfigurationAccessor;
import com.blackducksoftware.integration.jira.common.settings.PluginErrorAccessor;
import com.blackducksoftware.integration.jira.common.settings.model.PluginBlackDuckServerConfigModel;
import com.blackducksoftware.integration.jira.config.model.BlackDuckJiraConfigSerializable;
import com.synopsys.integration.blackduck.api.generated.component.AssignedUserRequest;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;

//TODO remove this when Super users in Black Duck no longer need to be assigned to a project in order to receive notifications for that project. Remove when all customers update to the fixed version of BD
public class BlackDuckAssignUtil {
    private final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));

    public void assignUserToBlackDuckProject(final PluginErrorAccessor pluginErrorAccessor, final GlobalConfigurationAccessor globalConfigurationAccessor) {
        try {
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

            final AssignedUserRequest assignedUserRequest = new AssignedUserRequest();
            assignedUserRequest.setUser(currentUser.getHref().orElseThrow(() -> new IntegrationException(String.format("The current user, %s, does not have an href.", currentUser.getUserName()))));
            for (final ProjectView projectView : nonAssignedProjects) {
                final Optional<String> projectUsersLinkOptional = projectView.getFirstLink(ProjectView.USERS_LINK);
                if (projectUsersLinkOptional.isPresent()) {
                    blackDuckService.post(projectUsersLinkOptional.get(), assignedUserRequest);
                } else {
                    final String errorMessage = String.format("Could not assign the user, %s, to the project %s because there is no users link.", currentUser.getUserName(), projectView.getName());
                    logger.error(errorMessage);
                    pluginErrorAccessor.addBlackDuckError(errorMessage, "assignUserToBlackDuckProject");
                }
            }

        } catch (final IntegrationException e) {
            logger.error("Could not assign the Black Duck user to the configured Black Duck projects. " + e.getMessage(), e);
            pluginErrorAccessor.addBlackDuckError(e, "assignUserToBlackDuckProject");
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
