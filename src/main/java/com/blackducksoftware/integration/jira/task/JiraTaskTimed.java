/**
 * Black Duck JIRA Plugin
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
package com.blackducksoftware.integration.jira.task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.blackducksoftware.integration.jira.JiraVersionCheck;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.JiraUserContext;
import com.blackducksoftware.integration.jira.common.TicketInfoFromSetup;
import com.blackducksoftware.integration.jira.common.exception.ConfigurationException;
import com.blackducksoftware.integration.jira.common.exception.JiraException;
import com.blackducksoftware.integration.jira.common.model.BlackDuckProjectMapping;
import com.blackducksoftware.integration.jira.config.JiraServices;
import com.blackducksoftware.integration.jira.config.JiraSettingsService;
import com.blackducksoftware.integration.jira.config.PluginConfigKeys;
import com.blackducksoftware.integration.jira.config.PluginConfigurationDetails;
import com.blackducksoftware.integration.jira.config.model.BlackDuckJiraConfigSerializable;
import com.blackducksoftware.integration.jira.task.setup.BlackDuckFieldConfigurationSetup;
import com.blackducksoftware.integration.jira.task.setup.BlackDuckFieldScreenSchemeSetup;
import com.blackducksoftware.integration.jira.task.setup.BlackDuckIssueTypeSetup;
import com.blackducksoftware.integration.jira.task.setup.BlackDuckWorkflowSetup;

public class JiraTaskTimed implements Callable<String> {
    private final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final PluginSettings settings;
    private final JiraServices jiraServices;
    private final JiraSettingsService jiraSettingsService;

    public JiraTaskTimed(final PluginSettings settings, final JiraSettingsService jiraSettingsService, final JiraServices jiraServices) {
        this.settings = settings;
        this.jiraSettingsService = jiraSettingsService;
        this.jiraServices = jiraServices;
    }

    @Override
    public String call() throws Exception {
        logger.info("Running the Black Duck JIRA periodic timed task.");

        // This plugin configuration needs to be read during execution because the task could have been queued for an arbitrarily long time
        final PluginConfigurationDetails configDetails = new PluginConfigurationDetails(settings);
        final JiraUserContext jiraContext = initJiraContext(configDetails.getJiraAdminUserName(), configDetails.getJiraIssueCreatorUserName());
        if (jiraContext == null) {
            logger.error("No (valid) user in configuration data; The plugin has likely not yet been configured; The task cannot run (yet)");
            return "error";
        }
        final LocalDateTime beforeSetup = LocalDateTime.now();
        final TicketInfoFromSetup ticketInfoFromSetup = new TicketInfoFromSetup();
        try {
            jiraSetup(jiraServices, jiraSettingsService, configDetails.getProjectMappingJson(), ticketInfoFromSetup, jiraContext);
        } catch (final Exception e) {
            logger.error("Error during JIRA setup: " + e.getMessage() + "; The task cannot run", e);
            return "error";
        }
        final LocalDateTime afterSetup = LocalDateTime.now();
        final Duration diff = Duration.between(beforeSetup, afterSetup);
        logger.info("Black Duck JIRA setup took " + diff.toMinutes() + "m," + (diff.getSeconds() % 60L) + "s," + (diff.toMillis() % 1000l) + "ms.");
        final BlackDuckJiraTask processor = new BlackDuckJiraTask(configDetails, jiraContext, jiraSettingsService, ticketInfoFromSetup);
        final String runResult = runHubJiraTaskAndSetLastRunDate(processor, configDetails);
        logger.info("blackduck-jira periodic timed task has completed");
        return runResult;
    }

    public void jiraSetup(final JiraServices jiraServices, final JiraSettingsService jiraSettingsService, final String projectMappingJson, final TicketInfoFromSetup ticketInfoFromSetup, final JiraUserContext jiraContext)
            throws ConfigurationException, JiraException {
        // Make sure current JIRA version is supported
        getJiraVersionCheck(); // throws exception if not

        // Create Issue Types, workflow, etc.
        BlackDuckIssueTypeSetup issueTypeSetup;
        try {
            issueTypeSetup = getBlackDuckIssueTypeSetup(jiraSettingsService, jiraServices, jiraContext.getJiraAdminUser().getName());
        } catch (final ConfigurationException e) {
            logger.error("Unable to create IssueTypes; Perhaps configuration is not ready; Will try again next time");
            return;
        }
        final List<IssueType> issueTypes = issueTypeSetup.addBdsIssueTypesToJira();
        if (issueTypes == null || issueTypes.isEmpty()) {
            logger.error("No Black Duck Issue Types found or created");
            return;
        }
        logger.debug("Number of Black Duck issue types found or created: " + issueTypes.size());
        // FIXME remove: issueTypeSetup.replaceOldIssueTypes(issueTypes);

        final BlackDuckFieldScreenSchemeSetup fieldConfigurationSetup = getBlackDuckFieldScreenSchemeSetup(jiraSettingsService, jiraServices);

        final Map<IssueType, FieldScreenScheme> screenSchemesByIssueType = fieldConfigurationSetup.addBlackDuckFieldConfigurationToJira(issueTypes);
        if (screenSchemesByIssueType.isEmpty()) {
            logger.error("No Black Duck Screen Schemes found or created");
        }
        ticketInfoFromSetup.setCustomFields(fieldConfigurationSetup.getCustomFields());

        logger.debug("Number of Black Duck Screen Schemes found or created: " + screenSchemesByIssueType.size());

        final BlackDuckFieldConfigurationSetup blackDuckFieldConfigurationSetup = getBlackDuckFieldConfigurationSetup(jiraSettingsService, jiraServices);
        final EditableFieldLayout fieldConfiguration = blackDuckFieldConfigurationSetup.addBlackDuckFieldConfigurationToJira();
        final FieldLayoutScheme fieldConfigurationScheme = blackDuckFieldConfigurationSetup.createFieldConfigurationScheme(issueTypes, fieldConfiguration);

        final BlackDuckWorkflowSetup workflowSetup = getBlackDuckWorkflowSetup(jiraSettingsService, jiraServices);
        final JiraWorkflow workflow = workflowSetup.addBlackDuckWorkflowToJira();
        logger.debug("Black Duck workflow Name: " + workflow.getName());

        // Associate these config objects with mapped projects
        adjustProjectsConfig(jiraServices, projectMappingJson, issueTypeSetup, issueTypes, screenSchemesByIssueType, fieldConfiguration,
                fieldConfigurationScheme, workflowSetup, workflow);
    }

    public JiraVersionCheck getJiraVersionCheck() throws ConfigurationException {
        return new JiraVersionCheck();
    }

    // Set the last run date immediately so that if the task is rescheduled on a different thread before this one completes, data will not be duplicated.
    private String runHubJiraTaskAndSetLastRunDate(final BlackDuckJiraTask processor, final PluginConfigurationDetails configDetails) {
        String runStatus = "error";
        final String previousRunDateString = configDetails.getLastRunDateString();
        final String currentRunDateString = processor.getRunDateString();
        if (previousRunDateString != null && currentRunDateString != null) {
            settings.put(PluginConfigKeys.BLACKDUCK_CONFIG_LAST_RUN_DATE, currentRunDateString);
        }
        final String newRunDateString = processor.execute(previousRunDateString);
        if (newRunDateString != null) {
            settings.put(PluginConfigKeys.BLACKDUCK_CONFIG_LAST_RUN_DATE, newRunDateString);
            runStatus = newRunDateString.equals(previousRunDateString) ? runStatus : "success";
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

    private BlackDuckIssueTypeSetup getBlackDuckIssueTypeSetup(final JiraSettingsService jiraSettingsService, final JiraServices jiraServices, final String jiraUserName) throws ConfigurationException {
        return new BlackDuckIssueTypeSetup(jiraServices, jiraSettingsService, jiraServices.getIssueTypes(), jiraUserName);
    }

    public BlackDuckFieldScreenSchemeSetup getBlackDuckFieldScreenSchemeSetup(final JiraSettingsService jiraSettingsService, final JiraServices jiraServices) {
        return new BlackDuckFieldScreenSchemeSetup(jiraSettingsService, jiraServices);
    }

    private BlackDuckFieldConfigurationSetup getBlackDuckFieldConfigurationSetup(final JiraSettingsService jiraSettingsService, final JiraServices jiraServices) {
        return new BlackDuckFieldConfigurationSetup(jiraSettingsService, jiraServices);
    }

    private BlackDuckWorkflowSetup getBlackDuckWorkflowSetup(final JiraSettingsService jiraSettingsService, final JiraServices jiraServices) {
        return new BlackDuckWorkflowSetup(jiraSettingsService, jiraServices);
    }

    private JiraUserContext initJiraContext(final String jiraAdminUsername, String jiraIssueCreatorUsername) {
        logger.debug(String.format("Checking JIRA users: Admin: %s; Issue creator: %s", jiraAdminUsername, jiraIssueCreatorUsername));
        if (jiraIssueCreatorUsername == null) {
            logger.warn(String.format("The JIRA Issue Creator user has not been configured, using the admin user (%s) to create issues. This can be changed via the Issue Creation configuration", jiraAdminUsername));
            jiraIssueCreatorUsername = jiraAdminUsername;
        }
        final ApplicationUser jiraAdminUser = getJiraUser(jiraAdminUsername);
        final ApplicationUser jiraIssueCreatorUser = getJiraUser(jiraIssueCreatorUsername);
        if ((jiraAdminUser == null) || (jiraIssueCreatorUser == null)) {
            return null;
        }
        final JiraUserContext jiraContext = new JiraUserContext(jiraAdminUser, jiraIssueCreatorUser);
        return jiraContext;
    }

    private ApplicationUser getJiraUser(final String jiraUsername) {
        final UserManager jiraUserManager = jiraServices.getUserManager();
        final ApplicationUser jiraUser = jiraUserManager.getUserByName(jiraUsername);
        if (jiraUser == null) {
            logger.error(String.format("Could not find the JIRA user %s", jiraUsername));
            return null;
        }
        return jiraUser;
    }

}
