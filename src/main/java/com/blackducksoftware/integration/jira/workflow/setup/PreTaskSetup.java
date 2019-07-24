/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2019 Synopsys, Inc.
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

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.JiraUserContext;
import com.blackducksoftware.integration.jira.common.TicketInfoFromSetup;
import com.blackducksoftware.integration.jira.common.exception.ConfigurationException;
import com.blackducksoftware.integration.jira.common.exception.JiraException;
import com.blackducksoftware.integration.jira.common.model.BlackDuckProjectMapping;
import com.blackducksoftware.integration.jira.dal.PluginErrorAccessor;
import com.blackducksoftware.integration.jira.dal.model.ProjectMappingConfigModel;
import com.blackducksoftware.integration.jira.web.JiraServices;
import com.blackducksoftware.integration.jira.web.model.BlackDuckJiraConfigSerializable;
import com.blackducksoftware.integration.jira.workflow.JiraVersionCheck;

public class PreTaskSetup {
    private final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));

    public void runPluginSetup(final JiraServices jiraServices, final PluginErrorAccessor pluginErrorAccessor, final ProjectMappingConfigModel projectMappingConfig, final TicketInfoFromSetup ticketInfoFromSetup,
        final JiraUserContext jiraContext) throws ConfigurationException, JiraException {
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

    private void adjustProjectsConfig(final JiraServices jiraServices, final String projectMappingJson, final BlackDuckIssueTypeSetup issueTypeSetup, final List<IssueType> issueTypes,
        final Map<IssueType, FieldScreenScheme> screenSchemesByIssueType, final EditableFieldLayout fieldConfiguration, final FieldLayoutScheme fieldConfigurationScheme,
        final BlackDuckWorkflowSetup workflowSetup, final JiraWorkflow workflow) {
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

}
