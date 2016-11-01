/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
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
 *******************************************************************************/
package com.blackducksoftware.integration.jira.task;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Period;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.Clause;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.scheduling.PluginJob;
import com.blackducksoftware.integration.atlassian.utils.HubConfigKeys;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.jira.common.HubJiraConfigKeys;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.common.HubProjectMapping;
import com.blackducksoftware.integration.jira.common.JiraContext;
import com.blackducksoftware.integration.jira.common.TicketInfoFromSetup;
import com.blackducksoftware.integration.jira.common.exception.ConfigurationException;
import com.blackducksoftware.integration.jira.common.exception.JiraException;
import com.blackducksoftware.integration.jira.common.jiraversion.JiraVersion;
import com.blackducksoftware.integration.jira.config.HubJiraConfigSerializable;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;
import com.blackducksoftware.integration.jira.task.setup.HubWorkflowSetup;
import com.blackducksoftware.integration.jira.task.setup.HubFieldConfigurationSetup;
import com.blackducksoftware.integration.jira.task.setup.HubFieldScreenSchemeSetup;
import com.blackducksoftware.integration.jira.task.setup.HubIssueTypeSetup;
import com.blackducksoftware.integration.jira.task.setup.HubWorkflowSetup;

/**
 * A scheduled JIRA task that collects recent notifications from the Hub, and
 * generates JIRA tickets for them.
 *
 * @author sbillings
 *
 */
public class JiraTask implements PluginJob {
    private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));
    private final JiraServices jiraServices;
    
    public JiraTask() {
        jiraServices = new JiraServices();
    }

    @Override
    public void execute(final Map<String, Object> jobDataMap) {
        logger.info("Running the Hub Jira task.");

        final PluginSettings settings = (PluginSettings) jobDataMap.get(HubMonitor.KEY_SETTINGS);
        final String hubUrl = getStringValue(settings, HubConfigKeys.CONFIG_HUB_URL);
        final String hubUsername = getStringValue(settings, HubConfigKeys.CONFIG_HUB_USER);
        final String hubPasswordEncrypted = getStringValue(settings, HubConfigKeys.CONFIG_HUB_PASS);
        final String hubPasswordLength = getStringValue(settings, HubConfigKeys.CONFIG_HUB_PASS_LENGTH);
        final String hubTimeoutString = getStringValue(settings, HubConfigKeys.CONFIG_HUB_TIMEOUT);

        final String hubProxyHost = getStringValue(settings, HubConfigKeys.CONFIG_PROXY_HOST);
        final String hubProxyPort = getStringValue(settings, HubConfigKeys.CONFIG_PROXY_PORT);
        final String hubProxyNoHost = getStringValue(settings, HubConfigKeys.CONFIG_PROXY_NO_HOST);
        final String hubProxyUser = getStringValue(settings, HubConfigKeys.CONFIG_PROXY_USER);
        final String hubProxyPassEncrypted = getStringValue(settings, HubConfigKeys.CONFIG_PROXY_PASS);
        final String hubProxyPassLength = getStringValue(settings, HubConfigKeys.CONFIG_PROXY_PASS_LENGTH);

        final String intervalString = getStringValue(settings,
                HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS);
        final String projectMappingJson = getStringValue(settings,
                HubJiraConfigKeys.HUB_CONFIG_JIRA_PROJECT_MAPPINGS_JSON);
        final String policyRulesJson = getStringValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_POLICY_RULES_JSON);
        final String installDateString = getStringValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_FIRST_SAVE_TIME);
        final String lastRunDateString = getStringValue(settings, HubJiraConfigKeys.HUB_CONFIG_LAST_RUN_DATE);

        final String jiraUserName = getStringValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_USER);
        
        final boolean changeIssueStateEnabled = HubJiraConfigKeys.getBooleanValue(settings, HubJiraConfigKeys.HUB_CONFIG_CHANGE_ISSUE_STATE_IF_EXISTS, true);
        logger.debug("changeIssueStateIfExists: " + changeIssueStateEnabled);
        final JiraSettingsService jiraSettingsService = new JiraSettingsService(settings);
        
        JiraContext jiraContext = initJiraContext(jiraUserName);

        final DateTime beforeSetup = new DateTime();
        final TicketInfoFromSetup ticketInfoFromSetup = new TicketInfoFromSetup();
        try {
            jiraSetup(jiraServices, jiraSettingsService, projectMappingJson, ticketInfoFromSetup, jiraContext,
                    changeIssueStateEnabled);
        } catch (final Exception e) {
            logger.error("Error during JIRA setup: " + e.getMessage() + "; The task cannot run", e);
            return;
        }
        final DateTime afterSetup = new DateTime();

        final Period diff = new Period(beforeSetup, afterSetup);
        logger.info("Hub Jira setup took " + diff.getMinutes() + "m," + diff.getSeconds() + "s," + diff.getMillis()
                + "ms.");

        final HubServerConfigBuilder hubConfigBuilder = new HubServerConfigBuilder();
        hubConfigBuilder.setHubUrl(hubUrl);
        hubConfigBuilder.setUsername(hubUsername);
        hubConfigBuilder.setPassword(hubPasswordEncrypted);
        hubConfigBuilder.setPasswordLength(NumberUtils.toInt(hubPasswordLength));
        hubConfigBuilder.setTimeout(hubTimeoutString);

        hubConfigBuilder.setProxyHost(hubProxyHost);
        hubConfigBuilder.setProxyPort(hubProxyPort);
        hubConfigBuilder.setIgnoredProxyHosts(hubProxyNoHost);
        hubConfigBuilder.setProxyUsername(hubProxyUser);
        hubConfigBuilder.setProxyPassword(hubProxyPassEncrypted);
        hubConfigBuilder.setProxyPasswordLength(NumberUtils.toInt(hubProxyPassLength));

        
        
        HubServerConfig serverConfig = null;
        try {
            logger.debug("Building Hub configuration");
            serverConfig = hubConfigBuilder.build();
            logger.debug("Finished building Hub configuration");
        } catch (final IllegalStateException e) {
            logger.error("Unable to connect to the Hub. This could mean the Hub is currently unreachable, or that at least one of the Black Duck plugins (either the Hub Admin plugin or the Hub Jira plugin) is not (yet) configured correctly: "
                    + e.getMessage());
            return;
        }

        if (hubConfigBuilder.buildResults().hasErrors()) {
            logger.error(
                    "At least one of the Black Duck plugins (either the Hub Admin plugin or the Hub Jira plugin) is not (yet) configured correctly.");
            return;
        }

        final HubJiraTask processor = new HubJiraTask(serverConfig, intervalString, installDateString,
                lastRunDateString, projectMappingJson, policyRulesJson, jiraContext, jiraSettingsService,
                ticketInfoFromSetup, changeIssueStateEnabled);
        final String runDateString = processor.execute();
        if (runDateString != null) {
            settings.put(HubJiraConfigKeys.HUB_CONFIG_LAST_RUN_DATE, runDateString);
        }
    }

    public void jiraSetup(final JiraServices jiraServices, final JiraSettingsService jiraSettingsService,
            final String projectMappingJson, final TicketInfoFromSetup ticketInfoFromSetup,
            final JiraContext jiraContext,
            final boolean changeIssueStateEnabled)
            throws ConfigurationException, JiraException {
        
        //////////////////////// Create Issue Types, workflow, etc ////////////
        final JiraVersion jiraVersion = getJiraVersion();
        HubIssueTypeSetup issueTypeSetup;
        try {
            issueTypeSetup = getHubIssueTypeSetup(jiraSettingsService, jiraServices, jiraContext.getJiraUser().getName());
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
                jiraServices, jiraVersion);

        final Map<IssueType, FieldScreenScheme> screenSchemesByIssueType = fieldConfigurationSetup
                .addHubFieldConfigurationToJira(issueTypes);
        if (screenSchemesByIssueType == null || screenSchemesByIssueType.isEmpty()) {
            logger.error("No Black Duck Screen Schemes found or created");
        }
        ticketInfoFromSetup.setCustomFields(fieldConfigurationSetup.getCustomFields());

        logger.debug("Number of Black Duck Screen Schemes found or created: " + screenSchemesByIssueType.size());

        final HubFieldConfigurationSetup hubFieldConfigurationSetup = getHubFieldConfigurationSetup(jiraSettingsService,
                jiraServices);
        final EditableFieldLayout fieldConfiguration = hubFieldConfigurationSetup.addHubFieldConfigurationToJira();
        final FieldLayoutScheme fieldConfigurationScheme = hubFieldConfigurationSetup
                .createFieldConfigurationScheme(issueTypes, fieldConfiguration);

        final HubWorkflowSetup workflowSetup = getHubWorkflowSetup(jiraSettingsService, jiraServices,
                jiraVersion, jiraContext, changeIssueStateEnabled);
        final JiraWorkflow workflow = workflowSetup.addHubWorkflowToJira();
        logger.debug("Black Duck workflow Name: " + workflow.getName() + "; ID: " + workflow.getDescriptor().getId()); 
        ////////////////////////////////////////////////////////////////////////

        /////////////////////// Associate with projects ///////////////////////
        if (projectMappingJson != null && issueTypes != null && !issueTypes.isEmpty()) {
            final HubJiraConfigSerializable config = new HubJiraConfigSerializable();
            // Converts Json to list of mappings
            config.setHubProjectMappingsJson(projectMappingJson);
            if (!config.getHubProjectMappings().isEmpty()) {
                for (final HubProjectMapping projectMapping : config.getHubProjectMappings()) {
                    if (projectMapping.getJiraProject() != null
                            && projectMapping.getJiraProject().getProjectId() != null) {
                        // Get jira Project object by Id
                        // from the JiraProject in the mapping
                        final Project jiraProject = jiraServices.getJiraProjectManager()
                                .getProjectObj(projectMapping.getJiraProject().getProjectId());
                        // add issuetypes to this project
                        issueTypeSetup.addIssueTypesToProjectIssueTypeScheme(jiraProject, issueTypes);
                        issueTypeSetup.addIssueTypesToProjectIssueTypeScreenSchemes(jiraProject,
                                screenSchemesByIssueType);
                        boolean wasAlreadySetUp = issueTypeSetup.associateIssueTypesWithFieldConfigurationsOnProjectFieldConfigurationScheme(
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
        /////////////////////////////////////////////////////////////////////////
    }

    public JiraVersion getJiraVersion() throws ConfigurationException {
        return new JiraVersion();
    }

    private HubIssueTypeSetup getHubIssueTypeSetup(final JiraSettingsService jiraSettingsService,
            final JiraServices jiraServices, final String jiraUserName) throws ConfigurationException {
        return new HubIssueTypeSetup(jiraServices, jiraSettingsService, jiraServices.getIssueTypes(), jiraUserName);
    }

    public HubFieldScreenSchemeSetup getHubFieldScreenSchemeSetup(
            final JiraSettingsService jiraSettingsService,
            final JiraServices jiraServices, final JiraVersion jiraVersion) {
        return new HubFieldScreenSchemeSetup(jiraSettingsService, jiraServices);
    }

    private HubFieldConfigurationSetup getHubFieldConfigurationSetup(final JiraSettingsService jiraSettingsService,
            final JiraServices jiraServices) {
        return new HubFieldConfigurationSetup(jiraSettingsService, jiraServices);
    }

    private HubWorkflowSetup getHubWorkflowSetup(final JiraSettingsService jiraSettingsService,
            final JiraServices jiraServices, final JiraVersion jiraVersion, final JiraContext jiraContext,
            final boolean changeIssueStateEnabled) {
        return new HubWorkflowSetup(jiraSettingsService, jiraServices, jiraContext, changeIssueStateEnabled);
    }

    private Object getValue(final PluginSettings settings, final String key) {
        return settings.get(key);
    }

    private String getStringValue(final PluginSettings settings, final String key) {
        return (String) getValue(settings, key);
    }
    
    private JiraContext initJiraContext(final String jiraUser) {
        final UserManager jiraUserManager = jiraServices.getUserManager();
        final ApplicationUser jiraSysAdmin = jiraUserManager.getUserByName(jiraUser);
        if (jiraSysAdmin == null) {
            logger.error("Could not find the Jira System admin that saved the Hub Jira config.");
            return null;
        }

        final JiraContext jiraContext = new JiraContext(jiraSysAdmin);
        return jiraContext;
    }
}
