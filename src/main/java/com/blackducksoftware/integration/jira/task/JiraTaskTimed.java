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
package com.blackducksoftware.integration.jira.task;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Period;

import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.blackducksoftware.integration.jira.common.HubJiraConfigKeys;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.common.HubProjectMapping;
import com.blackducksoftware.integration.jira.common.JiraContext;
import com.blackducksoftware.integration.jira.common.TicketInfoFromSetup;
import com.blackducksoftware.integration.jira.common.exception.ConfigurationException;
import com.blackducksoftware.integration.jira.common.exception.JiraException;
import com.blackducksoftware.integration.jira.common.jiraversion.JiraVersionCheck;
import com.blackducksoftware.integration.jira.config.HubJiraConfigSerializable;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;
import com.blackducksoftware.integration.jira.task.setup.HubFieldConfigurationSetup;
import com.blackducksoftware.integration.jira.task.setup.HubFieldScreenSchemeSetup;
import com.blackducksoftware.integration.jira.task.setup.HubIssueTypeSetup;
import com.blackducksoftware.integration.jira.task.setup.HubWorkflowSetup;

public class JiraTaskTimed implements Callable<String> {
    private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final PluginSettings settings;
    private final JiraServices jiraServices;
    private final JiraSettingsService jiraSettingsService;
    private final PluginConfigurationDetails configDetails;

    public JiraTaskTimed(final PluginSettings settings, final JiraSettingsService jiraSettingsService, final JiraServices jiraServices, final PluginConfigurationDetails configDetails) {
        this.settings = settings;
        this.jiraSettingsService = jiraSettingsService;
        this.configDetails = configDetails;
        this.jiraServices = jiraServices;
    }

    @Override
    public String call() throws Exception {
        logger.info("Running the Hub JIRA periodic timed task.");

        final JiraContext jiraContext = initJiraContext(configDetails.getJiraAdminUserName(), configDetails.getJiraIssueCreatorUserName());
        if (jiraContext == null) {
            logger.error("No (valid) user in configuration data; The plugin has likely not yet been configured; The task cannot run (yet)");
            return "error";
        }
        final DateTime beforeSetup = new DateTime();
        final TicketInfoFromSetup ticketInfoFromSetup = new TicketInfoFromSetup();
        try {
            jiraSetup(jiraServices, jiraSettingsService, configDetails.getProjectMappingJson(), ticketInfoFromSetup, jiraContext);
        } catch (final Exception e) {
            logger.error("Error during JIRA setup: " + e.getMessage() + "; The task cannot run", e);
            return "error";
        }
        final DateTime afterSetup = new DateTime();
        final Period diff = new Period(beforeSetup, afterSetup);
        logger.info("Hub JIRA setup took " + diff.getMinutes() + "m," + diff.getSeconds() + "s," + diff.getMillis() + "ms.");
        final HubJiraTask processor = new HubJiraTask(configDetails, jiraContext, jiraSettingsService, ticketInfoFromSetup);
        runHubJiraTaskAndSetLastRunDate(processor);
        logger.info("hub-jira periodic timed task has completed");
        return "success";
    }

    public void jiraSetup(final JiraServices jiraServices, final JiraSettingsService jiraSettingsService,
            final String projectMappingJson, final TicketInfoFromSetup ticketInfoFromSetup,
            final JiraContext jiraContext)
            throws ConfigurationException, JiraException {

        // Make sure current JIRA version is supported
        getJiraVersionCheck(); // throws exception if not

        // Create Issue Types, workflow, etc.
        HubIssueTypeSetup issueTypeSetup;
        try {
            issueTypeSetup = getHubIssueTypeSetup(jiraSettingsService, jiraServices, jiraContext.getJiraAdminUser().getName());
        } catch (final ConfigurationException e) {
            logger.error("Unable to create IssueTypes; Perhaps configuration is not ready; Will try again next time");
            return;
        }
        final List<IssueType> issueTypes = issueTypeSetup.addIssueTypesToJira();
        if (issueTypes == null || issueTypes.isEmpty()) {
            logger.error("No Black Duck Issue Types found or created");
            return;
        }
        logger.debug("Number of Black Duck issue types found or created: " + issueTypes.size());

        final HubFieldScreenSchemeSetup fieldConfigurationSetup = getHubFieldScreenSchemeSetup(
                jiraSettingsService,
                jiraServices);

        final Map<IssueType, FieldScreenScheme> screenSchemesByIssueType = fieldConfigurationSetup.addHubFieldConfigurationToJira(issueTypes);
        if (screenSchemesByIssueType.isEmpty()) {
            logger.error("No Black Duck Screen Schemes found or created");
        }
        ticketInfoFromSetup.setCustomFields(fieldConfigurationSetup.getCustomFields());

        logger.debug("Number of Black Duck Screen Schemes found or created: " + screenSchemesByIssueType.size());

        final HubFieldConfigurationSetup hubFieldConfigurationSetup = getHubFieldConfigurationSetup(jiraSettingsService, jiraServices);
        final EditableFieldLayout fieldConfiguration = hubFieldConfigurationSetup.addHubFieldConfigurationToJira();
        final FieldLayoutScheme fieldConfigurationScheme = hubFieldConfigurationSetup.createFieldConfigurationScheme(issueTypes, fieldConfiguration);

        final HubWorkflowSetup workflowSetup = getHubWorkflowSetup(jiraSettingsService, jiraServices);
        final JiraWorkflow workflow = workflowSetup.addHubWorkflowToJira();
        logger.debug("Black Duck workflow Name: " + workflow.getName());

        // Associate these config objects with mapped projects
        adjustProjectsConfig(jiraServices, projectMappingJson, issueTypeSetup, issueTypes, screenSchemesByIssueType, fieldConfiguration,
                fieldConfigurationScheme, workflowSetup, workflow);
    }

    public JiraVersionCheck getJiraVersionCheck() throws ConfigurationException {
        return new JiraVersionCheck();
    }

    // Set the last run date immediately so that if the task is rescheduled on a different thread before this one completes, data will not be duplicated.
    private void runHubJiraTaskAndSetLastRunDate(final HubJiraTask processor) {
        final String previousRunDateString = configDetails.getLastRunDateString();
        if (previousRunDateString != null) {
            settings.put(HubJiraConfigKeys.HUB_CONFIG_LAST_RUN_DATE, processor.getRunDateString());
        }
        final String newRunDateString = processor.execute();
        if (newRunDateString != null) {
            settings.put(HubJiraConfigKeys.HUB_CONFIG_LAST_RUN_DATE, newRunDateString);
        } else {
            settings.put(HubJiraConfigKeys.HUB_CONFIG_LAST_RUN_DATE, previousRunDateString);
        }
    }

    private void adjustProjectsConfig(final JiraServices jiraServices, final String projectMappingJson, final HubIssueTypeSetup issueTypeSetup,
            final List<IssueType> issueTypes, final Map<IssueType, FieldScreenScheme> screenSchemesByIssueType, final EditableFieldLayout fieldConfiguration,
            final FieldLayoutScheme fieldConfigurationScheme, final HubWorkflowSetup workflowSetup, final JiraWorkflow workflow) {
        if (projectMappingJson != null && issueTypes != null && !issueTypes.isEmpty()) {
            final HubJiraConfigSerializable config = new HubJiraConfigSerializable();
            // Converts Json to list of mappings
            config.setHubProjectMappingsJson(projectMappingJson);
            if (!config.getHubProjectMappings().isEmpty()) {
                for (final HubProjectMapping projectMapping : config.getHubProjectMappings()) {
                    if (projectMapping.getJiraProject() != null
                            && projectMapping.getJiraProject().getProjectId() != null) {
                        // Get jira Project object by Id from the JiraProject in the mapping
                        final Project jiraProject = jiraServices.getJiraProjectManager()
                                .getProjectObj(projectMapping.getJiraProject().getProjectId());
                        if (jiraProject != null) {
                            // add issuetypes to this project
                            issueTypeSetup.addIssueTypesToProjectIssueTypeScheme(jiraProject, issueTypes);
                            issueTypeSetup.addIssueTypesToProjectIssueTypeScreenSchemes(jiraProject, screenSchemesByIssueType);
                            final boolean wasAlreadySetUp = issueTypeSetup.associateIssueTypesWithFieldConfigurationsOnProjectFieldConfigurationScheme(
                                    jiraProject, fieldConfigurationScheme, issueTypes, fieldConfiguration);
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

    private HubIssueTypeSetup getHubIssueTypeSetup(final JiraSettingsService jiraSettingsService,
            final JiraServices jiraServices, final String jiraUserName) throws ConfigurationException {
        return new HubIssueTypeSetup(jiraServices, jiraSettingsService, jiraServices.getIssueTypes(), jiraUserName);
    }

    public HubFieldScreenSchemeSetup getHubFieldScreenSchemeSetup(final JiraSettingsService jiraSettingsService, final JiraServices jiraServices) {
        return new HubFieldScreenSchemeSetup(jiraSettingsService, jiraServices);
    }

    private HubFieldConfigurationSetup getHubFieldConfigurationSetup(final JiraSettingsService jiraSettingsService,
            final JiraServices jiraServices) {
        return new HubFieldConfigurationSetup(jiraSettingsService, jiraServices);
    }

    private HubWorkflowSetup getHubWorkflowSetup(final JiraSettingsService jiraSettingsService,
            final JiraServices jiraServices) {
        return new HubWorkflowSetup(jiraSettingsService, jiraServices);
    }

    private JiraContext initJiraContext(final String jiraAdminUsername, String jiraIssueCreatorUsername) {
        logger.debug(String.format("Checking JIRA users: Admin: %s; Issue creator: %s", jiraAdminUsername, jiraIssueCreatorUsername));
        if (jiraIssueCreatorUsername == null) {
            logger.warn(String.format(
                    "The JIRA Issue Creator user has not been configured, using the admin user (%s) to create issues. This can be changed via the Issue Creation configuration",
                    jiraAdminUsername));
            jiraIssueCreatorUsername = jiraAdminUsername;
        }
        final ApplicationUser jiraAdminUser = getJiraUser(jiraAdminUsername);
        final ApplicationUser jiraIssueCreatorUser = getJiraUser(jiraIssueCreatorUsername);
        if ((jiraAdminUser == null) || (jiraIssueCreatorUser == null)) {
            return null;
        }
        final JiraContext jiraContext = new JiraContext(jiraAdminUser, jiraIssueCreatorUser);
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
