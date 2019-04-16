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
package com.blackducksoftware.integration.jira.task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.blackducksoftware.integration.jira.JiraVersionCheck;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.JiraUserContext;
import com.blackducksoftware.integration.jira.common.TicketInfoFromSetup;
import com.blackducksoftware.integration.jira.common.exception.ConfigurationException;
import com.blackducksoftware.integration.jira.common.exception.JiraException;
import com.blackducksoftware.integration.jira.common.model.BlackDuckProjectMapping;
import com.blackducksoftware.integration.jira.common.settings.GlobalConfigurationAccessor;
import com.blackducksoftware.integration.jira.common.settings.JiraSettingsAccessor;
import com.blackducksoftware.integration.jira.common.settings.PluginConfigurationAccessor;
import com.blackducksoftware.integration.jira.common.settings.PluginErrorAccessor;
import com.blackducksoftware.integration.jira.common.settings.model.GeneralIssueCreationConfigModel;
import com.blackducksoftware.integration.jira.common.settings.model.PluginGroupsConfigModel;
import com.blackducksoftware.integration.jira.common.settings.model.PluginIssueCreationConfigModel;
import com.blackducksoftware.integration.jira.common.settings.model.ProjectMappingConfigModel;
import com.blackducksoftware.integration.jira.config.JiraServices;
import com.blackducksoftware.integration.jira.config.model.BlackDuckJiraConfigSerializable;
import com.blackducksoftware.integration.jira.task.setup.BlackDuckFieldConfigurationSetup;
import com.blackducksoftware.integration.jira.task.setup.BlackDuckFieldScreenSchemeSetup;
import com.blackducksoftware.integration.jira.task.setup.BlackDuckIssueTypeSetup;
import com.blackducksoftware.integration.jira.task.setup.BlackDuckWorkflowSetup;

public class JiraTaskTimed implements Callable<String> {
    private final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final JiraSettingsAccessor jiraSettingsAccessor;
    private final JiraServices jiraServices;
    private final Integer configuredTaskInterval;

    public JiraTaskTimed(final JiraSettingsAccessor jiraSettingsAccessor, final JiraServices jiraServices, final Integer configuredTaskInterval) {
        this.jiraSettingsAccessor = jiraSettingsAccessor;
        this.jiraServices = jiraServices;
        this.configuredTaskInterval = configuredTaskInterval;
    }

    @Override
    public String call() throws Exception {
        logger.info("Running the Black Duck JIRA periodic timed task.");

        // These need to be created during execution because the task could have been queued for an arbitrarily long time
        logger.debug("Retrieving plugin settings for run...");
        final PluginConfigurationAccessor pluginConfigurationAccessor = jiraSettingsAccessor.createPluginConfigurationAccessor();
        final PluginErrorAccessor pluginErrorAccessor = jiraSettingsAccessor.createPluginErrorAccessor();
        final GlobalConfigurationAccessor globalConfigurationAccessor = jiraSettingsAccessor.createGlobalConfigurationAccessor();
        final PluginGroupsConfigModel groupsConfig = globalConfigurationAccessor.getGroupsConfig();
        final PluginIssueCreationConfigModel issueCreationConfig = globalConfigurationAccessor.getIssueCreationConfig();
        final GeneralIssueCreationConfigModel generalIssueConfig = issueCreationConfig.getGeneral();
        logger.debug("Retrieved plugin settings");
        logger.debug("Last run date based on SAL: " + pluginConfigurationAccessor.getFirstTimeSave());

        final Optional<JiraUserContext> optionalJiraUserContext = initJiraContext(pluginConfigurationAccessor.getJiraAdminUser(), generalIssueConfig.getDefaultIssueCreator(), jiraServices.getUserManager());
        if (!optionalJiraUserContext.isPresent()) {
            logger.error("No (valid) user in configuration data; The plugin has likely not yet been configured; The task cannot run (yet)");
            return "error";
        }
        final JiraUserContext jiraUserContext = optionalJiraUserContext.get();
        final String jiraPluginGroupsString = groupsConfig.getGroupsStringDelimited();
        if (!checkUserInPluginGroups(jiraPluginGroupsString, jiraServices.getGroupManager(), jiraUserContext.getDefaultJiraIssueCreatorUser())) {
            logger.error(String.format("User '%s' is no longer in the groups '%s'. The task cannot run.", jiraUserContext.getDefaultJiraIssueCreatorUser().getUsername(), jiraPluginGroupsString));
            return "error";
        }
        final LocalDateTime beforeSetup = LocalDateTime.now();
        final TicketInfoFromSetup ticketInfoFromSetup = new TicketInfoFromSetup();
        try {
            jiraSetup(jiraServices, pluginErrorAccessor, issueCreationConfig.getProjectMapping(), ticketInfoFromSetup, jiraUserContext);
        } catch (final Exception e) {
            logger.error("Error during JIRA setup: " + e.getMessage() + "; The task cannot run", e);
            return "error";
        }
        final LocalDateTime afterSetup = LocalDateTime.now();
        final Duration diff = Duration.between(beforeSetup, afterSetup);
        logger.info("Black Duck JIRA setup took " + diff.toMinutes() + "m," + (diff.getSeconds() % 60L) + "s," + (diff.toMillis() % 1000l) + "ms.");
        final BlackDuckJiraTask processor = new BlackDuckJiraTask(globalConfigurationAccessor, pluginConfigurationAccessor, pluginErrorAccessor, jiraUserContext, ticketInfoFromSetup);
        final String runResult = runBlackDuckJiraTaskAndSetLastRunDate(processor, pluginConfigurationAccessor);
        logger.info("blackduck-jira periodic timed task has completed");
        return runResult;
    }

    public void jiraSetup(final JiraServices jiraServices, final PluginErrorAccessor pluginErrorAccessor, final ProjectMappingConfigModel projectMappingConfig, final TicketInfoFromSetup ticketInfoFromSetup,
        final JiraUserContext jiraContext)
        throws ConfigurationException, JiraException {
        // Make sure current JIRA version is supported throws exception if not
        getJiraVersionCheck();

        // Create Issue Types, workflow, etc.
        final BlackDuckIssueTypeSetup issueTypeSetup;
        try {
            issueTypeSetup = createBlackDuckIssueTypeSetup(pluginErrorAccessor, jiraServices, jiraContext.getJiraAdminUser().getName());
        } catch (final ConfigurationException e) {
            throw new JiraException("Unable to create IssueTypes; Perhaps configuration is not ready; Will try again next time");
        }
        final List<IssueType> issueTypes = issueTypeSetup.addBdsIssueTypesToJira();
        if (issueTypes == null || issueTypes.isEmpty()) {
            throw new JiraException("No Black Duck Issue Types found or created");
        }
        logger.debug("Number of Black Duck issue types found or created: " + issueTypes.size());

        final BlackDuckFieldScreenSchemeSetup fieldConfigurationSetup = createBlackDuckFieldScreenSchemeSetup(pluginErrorAccessor, jiraServices);

        final Map<IssueType, FieldScreenScheme> screenSchemesByIssueType = fieldConfigurationSetup.addBlackDuckFieldConfigurationToJira(issueTypes);
        if (screenSchemesByIssueType.isEmpty()) {
            throw new JiraException("No Black Duck Screen Schemes found or created");
        }
        ticketInfoFromSetup.setCustomFields(fieldConfigurationSetup.getCustomFields());

        logger.debug("Number of Black Duck Screen Schemes found or created: " + screenSchemesByIssueType.size());

        final BlackDuckFieldConfigurationSetup blackDuckFieldConfigurationSetup = createBlackDuckFieldConfigurationSetup(pluginErrorAccessor, jiraServices);
        final EditableFieldLayout fieldConfiguration = blackDuckFieldConfigurationSetup.addBlackDuckFieldConfigurationToJira();
        final FieldLayoutScheme fieldConfigurationScheme = blackDuckFieldConfigurationSetup.createFieldConfigurationScheme(issueTypes, fieldConfiguration);

        final BlackDuckWorkflowSetup workflowSetup = createBlackDuckWorkflowSetup(pluginErrorAccessor, jiraServices);
        final JiraWorkflow workflow = workflowSetup.addBlackDuckWorkflowToJira().orElseThrow(() -> new JiraException("Unable to add Black Duck workflow to JIRA."));
        logger.debug("Black Duck workflow Name: " + workflow.getName());

        // Associate these config objects with mapped projects
        adjustProjectsConfig(jiraServices, projectMappingConfig.getMappingsJson(), issueTypeSetup, issueTypes, screenSchemesByIssueType, fieldConfiguration, fieldConfigurationScheme, workflowSetup, workflow);
    }

    public JiraVersionCheck getJiraVersionCheck() throws ConfigurationException {
        return new JiraVersionCheck();
    }

    // Set the last run date immediately so that if the task is rescheduled on a different thread before this one completes, data will not be duplicated.
    private String runBlackDuckJiraTaskAndSetLastRunDate(final BlackDuckJiraTask processor, final PluginConfigurationAccessor pluginConfigurationAccessor) {
        String runStatus = "error";
        final String previousRunDateString = pluginConfigurationAccessor.getLastRunDate();
        final String currentRunDateString = processor.getRunDateString();
        if (previousRunDateString != null && currentRunDateString != null) {
            logger.debug("Before processing, going to set the last run date to the current date: " + currentRunDateString);
            pluginConfigurationAccessor.setLastRunDate(currentRunDateString);
        } else {
            logger.warn(String.format("Before processing, did not update the last run date. Previous run date: %s   Current run date: %s", previousRunDateString, currentRunDateString));
        }
        final Optional<String> newRunDateOptional = processor.execute(previousRunDateString);
        if (newRunDateOptional.isPresent()) {
            final String newRunDate = newRunDateOptional.get();
            logger.debug("After processing, going to set the last run date to the new date: " + newRunDate);
            pluginConfigurationAccessor.setLastRunDate(newRunDate);
            runStatus = newRunDate.equals(previousRunDateString) ? runStatus : "success";
        } else {
            logger.warn("After processing, the new run date was null.");
        }
        // TODO determine if an else case is needed to revert to old last run date
        return runStatus;
    }

    private void adjustProjectsConfig(final JiraServices jiraServices, final String projectMappingJson, final BlackDuckIssueTypeSetup issueTypeSetup,
        final List<IssueType> issueTypes, final Map<IssueType, FieldScreenScheme> screenSchemesByIssueType, final EditableFieldLayout fieldConfiguration,
        final FieldLayoutScheme fieldConfigurationScheme, final BlackDuckWorkflowSetup workflowSetup, final JiraWorkflow workflow) {
        if (projectMappingJson != null && issueTypes != null && !issueTypes.isEmpty()) {
            final BlackDuckJiraConfigSerializable config = new BlackDuckJiraConfigSerializable();
            // Converts Json to list of mappings
            config.setHubProjectMappingsJson(projectMappingJson);
            if (!config.getHubProjectMappings().isEmpty()) {
                for (final BlackDuckProjectMapping projectMapping : config.getHubProjectMappings()) {
                    if (projectMapping.getJiraProject() != null
                            && projectMapping.getJiraProject().getProjectId() != null) {
                        // Get jira Project object by Id from the JiraProject in the mapping
                        final Project jiraProject = jiraServices.getJiraProjectManager()
                                                        .getProjectObj(projectMapping.getJiraProject().getProjectId());
                        if (jiraProject != null) {
                            // add issuetypes to this project
                            issueTypeSetup.addIssueTypesToProjectIssueTypeScheme(jiraProject, issueTypes);
                            issueTypeSetup.addIssueTypesToProjectIssueTypeScreenSchemes(jiraProject, screenSchemesByIssueType);
                            final boolean wasAlreadySetUp = issueTypeSetup.associateIssueTypesWithFieldConfigurationsOnProjectFieldConfigurationScheme(jiraProject, fieldConfigurationScheme, issueTypes, fieldConfiguration);
                            if (wasAlreadySetUp) {
                                logger.debug("It appears the project's WorkflowScheme has already been configured; leaving it unchanged");
                            } else {
                                workflowSetup.addWorkflowToProjectsWorkflowScheme(workflow, jiraProject, issueTypes);
                            }
                        }
                    }
                }
            }
        }
    }

    public BlackDuckFieldScreenSchemeSetup createBlackDuckFieldScreenSchemeSetup(final PluginErrorAccessor pluginErrorAccessor, final JiraServices jiraServices) {
        return new BlackDuckFieldScreenSchemeSetup(pluginErrorAccessor, jiraServices);
    }

    private BlackDuckIssueTypeSetup createBlackDuckIssueTypeSetup(final PluginErrorAccessor pluginErrorAccessor, final JiraServices jiraServices, final String jiraUserName) throws ConfigurationException {
        return new BlackDuckIssueTypeSetup(jiraServices, pluginErrorAccessor, jiraServices.getIssueTypes(), jiraUserName);
    }

    private BlackDuckFieldConfigurationSetup createBlackDuckFieldConfigurationSetup(final PluginErrorAccessor pluginErrorAccessor, final JiraServices jiraServices) {
        return new BlackDuckFieldConfigurationSetup(pluginErrorAccessor, jiraServices);
    }

    private BlackDuckWorkflowSetup createBlackDuckWorkflowSetup(final PluginErrorAccessor pluginErrorAccessor, final JiraServices jiraServices) {
        return new BlackDuckWorkflowSetup(pluginErrorAccessor, jiraServices);
    }

    private Optional<JiraUserContext> initJiraContext(final String jiraAdminUsername, String jiraIssueCreatorUsername, final UserManager userManager) {
        logger.debug(String.format("Checking JIRA users: Admin: %s; Issue creator: %s", jiraAdminUsername, jiraIssueCreatorUsername));
        if (jiraIssueCreatorUsername == null) {
            logger.warn(String.format("The JIRA Issue Creator user has not been configured, using the admin user (%s) to create issues. This can be changed via the Issue Creation configuration", jiraAdminUsername));
            jiraIssueCreatorUsername = jiraAdminUsername;
        }
        final Optional<ApplicationUser> jiraAdminUser = getJiraUser(jiraAdminUsername, userManager);
        final Optional<ApplicationUser> jiraIssueCreatorUser = getJiraUser(jiraIssueCreatorUsername, userManager);
        if (!jiraAdminUser.isPresent() || !jiraIssueCreatorUser.isPresent()) {
            return Optional.empty();
        }
        final JiraUserContext jiraContext = new JiraUserContext(jiraAdminUser.get(), jiraIssueCreatorUser.get());
        return Optional.of(jiraContext);
    }

    private Optional<ApplicationUser> getJiraUser(final String jiraUsername, final UserManager userManager) {
        final ApplicationUser jiraUser = userManager.getUserByName(jiraUsername);
        if (jiraUser == null) {
            logger.error(String.format("Could not find the JIRA user %s", jiraUsername));
        }
        return Optional.ofNullable(jiraUser);
    }

    private boolean checkUserInPluginGroups(final String jiraPluginGroupsString, final GroupManager groupManager, final ApplicationUser issueCreator) {
        if (StringUtils.isNotBlank(jiraPluginGroupsString)) {
            final String[] jiraPluginGroups = jiraPluginGroupsString.split(",");
            for (final String blackDuckJiraGroup : jiraPluginGroups) {
                if (groupManager.isUserInGroup(issueCreator, blackDuckJiraGroup.trim())) {
                    return true;
                }
            }
        }
        return false;
    }

}
