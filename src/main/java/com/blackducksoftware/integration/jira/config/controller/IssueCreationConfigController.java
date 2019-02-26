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
package com.blackducksoftware.integration.jira.config.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
import com.blackducksoftware.integration.jira.common.BlackDuckPluginDateFormatter;
import com.blackducksoftware.integration.jira.common.model.BlackDuckProjectMapping;
import com.blackducksoftware.integration.jira.common.model.JiraProject;
import com.blackducksoftware.integration.jira.config.JiraConfigErrorStrings;
import com.blackducksoftware.integration.jira.config.JiraServices;
import com.blackducksoftware.integration.jira.config.PluginConfigKeys;
import com.blackducksoftware.integration.jira.config.model.BlackDuckJiraConfigSerializable;
import com.blackducksoftware.integration.jira.task.BlackDuckMonitor;

@Path("/config/issue/creator")
public class IssueCreationConfigController extends ConfigController {

    final ProjectManager projectManager;
    private final BlackDuckMonitor blackDuckMonitor;

    public IssueCreationConfigController(final PluginSettingsFactory pluginSettingsFactory, final TransactionTemplate transactionTemplate, final UserManager userManager, final ProjectManager projectManager,
        final BlackDuckMonitor blackDuckMonitor) {
        super(pluginSettingsFactory, transactionTemplate, userManager);
        this.projectManager = projectManager;
        this.blackDuckMonitor = blackDuckMonitor;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCreator(@Context final HttpServletRequest request) {
        final Object config;
        try {
            final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
            final Response response = checkUserPermissions(request, settings);
            if (response != null) {
                return response;
            }
            config = getTransactionTemplate().execute(new TransactionCallback() {
                @Override
                public Object doInTransaction() {
                    final BlackDuckJiraConfigSerializable txConfig = new BlackDuckJiraConfigSerializable();
                    final String creator = getStringValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_ISSUE_CREATOR_USER);
                    txConfig.setCreator(creator);
                    validateCreator(txConfig);
                    return txConfig;
                }
            });
        } catch (final Exception e) {
            final BlackDuckJiraConfigSerializable errorConfig = new BlackDuckJiraConfigSerializable();
            final String msg = "Error getting creator config: " + e.getMessage();
            logger.error(msg, e);
            errorConfig.setGeneralSettingsError(msg);
            return Response.ok(errorConfig).build();
        }
        return Response.ok(config).build();
    }

    @Path("/candidates")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCreatorCandidates(@Context final HttpServletRequest request) {
        logger.debug("getCreatorCandidates()");
        final Object projectsConfig;
        try {
            final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
            final Response response = checkUserPermissions(request, settings);
            if (response != null) {
                return response;
            }
            projectsConfig = getTransactionTemplate().execute(new TransactionCallback() {
                @Override
                public Object doInTransaction() {
                    final BlackDuckJiraConfigSerializable config = new BlackDuckJiraConfigSerializable();
                    config.setCreatorCandidates(new TreeSet<String>());

                    final SortedSet<String> creatorCandidates = getIssueCreatorCandidates(settings);
                    config.setCreatorCandidates(creatorCandidates);

                    if (creatorCandidates.size() == 0) {
                        config.setGeneralSettingsError(JiraConfigErrorStrings.NO_CREATOR_CANDIDATES_FOUND);
                    }
                    return config;
                }
            });
        } catch (final Exception e) {
            final BlackDuckJiraConfigSerializable errorConfig = new BlackDuckJiraConfigSerializable();
            final String msg = "Error getting issue creator candidates config: " + e.getMessage();
            logger.error(msg, e);
            errorConfig.setGeneralSettingsError(msg);
            return Response.ok(errorConfig).build();
        }
        return Response.ok(projectsConfig).build();
    }

    @Path("/jira/projects")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJiraProjects(@Context final HttpServletRequest request) {
        final Object projectsConfig;
        try {
            final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
            final Response response = checkUserPermissions(request, settings);
            if (response != null) {
                return response;
            }
            projectsConfig = getTransactionTemplate().execute(new TransactionCallback() {
                @Override
                public Object doInTransaction() {
                    final List<JiraProject> jiraProjects = getJiraProjects(projectManager.getProjectObjects());

                    final BlackDuckJiraConfigSerializable txProjectsConfig = new BlackDuckJiraConfigSerializable();
                    txProjectsConfig.setJiraProjects(jiraProjects);

                    if (jiraProjects.size() == 0) {
                        txProjectsConfig.setJiraProjectsError(JiraConfigErrorStrings.NO_JIRA_PROJECTS_FOUND);
                    }
                    return txProjectsConfig;
                }
            });
        } catch (final Exception e) {
            final BlackDuckJiraConfigSerializable errorConfig = new BlackDuckJiraConfigSerializable();
            final String msg = "Error getting JIRA projects config: " + e.getMessage();
            logger.error(msg, e);
            errorConfig.setJiraProjectsError(msg);
            return Response.ok(errorConfig).build();
        }
        return Response.ok(projectsConfig).build();
    }

    @Path("/vulnerability/ticketchoice")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCreateVulnerabilityTicketsChoice(@Context final HttpServletRequest request) {
        logger.debug("GET createVulnerabilityTicketsChoice");
        final Object config;
        try {
            final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
            final Response response = checkUserPermissions(request, settings);
            if (response != null) {
                return response;
            }
            config = getTransactionTemplate().execute(new TransactionCallback() {
                @Override
                public Object doInTransaction() {
                    logger.debug("GET createVulnerabilityTicketsChoice transaction");
                    final BlackDuckJiraConfigSerializable txConfig = new BlackDuckJiraConfigSerializable();
                    final String createVulnIssuesChoiceString = getStringValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_CREATE_VULN_ISSUES_CHOICE);
                    logger.debug("createVulnIssuesChoiceString: " + createVulnIssuesChoiceString);
                    boolean choice = true;
                    if ("false".equalsIgnoreCase(createVulnIssuesChoiceString)) {
                        choice = false;
                    }
                    logger.debug("choice: " + choice);
                    txConfig.setCreateVulnerabilityIssues(choice);
                    return txConfig;
                }
            });
        } catch (final Exception e) {
            final BlackDuckJiraConfigSerializable errorConfig = new BlackDuckJiraConfigSerializable();
            final String msg = "Error getting 'create vulnerability issues' choice: " + e.getMessage();
            logger.error(msg, e);
            errorConfig.setCreateVulnerabilityIssuesError(msg);
            return Response.ok(errorConfig).build();
        }
        return Response.ok(config).build();
    }

    @Path("/comment/updatechoice")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCommentOnIssueUpdatesChoice(@Context final HttpServletRequest request) {
        logger.debug("GET commentOnIssueUpdatesChoice");
        final Object config;
        try {
            final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
            final Response response = checkUserPermissions(request, settings);
            if (response != null) {
                return response;
            }
            config = getTransactionTemplate().execute(new TransactionCallback() {
                @Override
                public Object doInTransaction() {
                    logger.debug("GET commentOnIssueUpdatesChoice transaction");
                    final BlackDuckJiraConfigSerializable txConfig = new BlackDuckJiraConfigSerializable();
                    final String commentOnIssueUpdatesChoiceString = getStringValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_COMMENT_ON_ISSUE_UPDATES_CHOICE);
                    logger.debug("commentOnIssueUpdatesChoiceString: " + commentOnIssueUpdatesChoiceString);
                    boolean choice = true;
                    if ("false".equalsIgnoreCase(commentOnIssueUpdatesChoiceString)) {
                        choice = false;
                    }
                    logger.debug("choice: " + choice);
                    txConfig.setCommentOnIssueUpdatesChoice(choice);
                    return txConfig;
                }
            });
        } catch (final Exception e) {
            final BlackDuckJiraConfigSerializable errorConfig = new BlackDuckJiraConfigSerializable();
            final String msg = "Error getting 'comment on issue updates' choice: " + e.getMessage();
            logger.error(msg, e);
            errorConfig.setCommentOnIssueUpdatesChoiceError(msg);
            return Response.ok(errorConfig).build();
        }
        return Response.ok(config).build();
    }

    @Path("/interval")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInterval(@Context final HttpServletRequest request) {
        final Object config;
        try {
            final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
            final Response response = checkUserPermissions(request, settings);
            if (response != null) {
                return response;
            }
            config = getTransactionTemplate().execute(new TransactionCallback() {
                @Override
                public Object doInTransaction() {
                    final BlackDuckJiraConfigSerializable txConfig = new BlackDuckJiraConfigSerializable();

                    final String intervalBetweenChecks = getStringValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS);

                    txConfig.setIntervalBetweenChecks(intervalBetweenChecks);

                    validateInterval(txConfig);
                    return txConfig;
                }
            });
        } catch (final Exception e) {
            final BlackDuckJiraConfigSerializable errorConfig = new BlackDuckJiraConfigSerializable();
            final String msg = "Error getting interval config: " + e.getMessage();
            logger.error(msg, e);
            errorConfig.setGeneralSettingsError(msg);
            return Response.ok(errorConfig).build();
        }
        return Response.ok(config).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response put(final BlackDuckJiraConfigSerializable config, @Context final HttpServletRequest request) {
        try {
            final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
            final String username = getUserManager().getRemoteUsername(request);
            final Response response = checkUserPermissions(request, settings);
            if (response != null) {
                return response;
            }
            getTransactionTemplate().execute(new TransactionCallback() {
                @Override
                public Object doInTransaction() {
                    final List<JiraProject> jiraProjects = getJiraProjects(projectManager.getProjectObjects());
                    config.setJiraProjects(jiraProjects);
                    validateInterval(config);
                    validateCreator(config, settings);
                    validateMapping(config);
                    if (getValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_FIRST_SAVE_TIME) == null) {
                        setValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_FIRST_SAVE_TIME, BlackDuckPluginDateFormatter.format(new Date()));
                    }
                    final String previousInterval = getStringValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS);
                    setValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS, config.getIntervalBetweenChecks());
                    final String issueCreatorJiraUser = config.getCreator();
                    logger.debug("Setting issue creator jira user to: " + issueCreatorJiraUser);
                    setValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_ISSUE_CREATOR_USER, issueCreatorJiraUser);
                    setValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_POLICY_RULES_JSON, config.getPolicyRulesJson());
                    setValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_PROJECT_MAPPINGS_JSON, config.getHubProjectMappingsJson());
                    setValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_ADMIN_USER, username);
                    updatePluginTaskInterval(previousInterval, config.getIntervalBetweenChecks());
                    logger.debug("User input: createVulnerabilityIssues: " + config.isCreateVulnerabilityIssues());
                    final Boolean createVulnerabilityIssuesChoice = config.isCreateVulnerabilityIssues();
                    logger.debug("Setting createVulnerabilityIssuesChoice to " + createVulnerabilityIssuesChoice.toString());
                    setValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_CREATE_VULN_ISSUES_CHOICE, createVulnerabilityIssuesChoice.toString());
                    final Boolean commentOnIssueUpdatesChoice = config.getCommentOnIssueUpdatesChoice();
                    logger.debug("Setting commentOnIssueUpdatesChoice to " + commentOnIssueUpdatesChoice.toString());
                    setValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_COMMENT_ON_ISSUE_UPDATES_CHOICE, commentOnIssueUpdatesChoice.toString());

                    return null;
                }
            });
        } catch (final Exception e) {
            final String msg = "Exception during save: " + e.getMessage();
            logger.error(msg, e);
            config.setErrorMessage(msg);
        }
        if (config.hasErrors()) {
            logger.error("There are one or more errors in the configuration: " + config.getConsolidatedErrorMessage());
            config.enhanceMappingErrorMessage();
            return Response.ok(config).status(Response.Status.BAD_REQUEST).build();
        }
        return Response.noContent().build();
    }

    // This must be "package protected" to avoid synthetic access
    SortedSet<String> getIssueCreatorCandidates(final PluginSettings settings) {
        final SortedSet<String> jiraUsernames = new TreeSet<>();
        final String groupList = getStringValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_GROUPS);
        if (!StringUtils.isBlank(groupList)) {
            final String[] groupNames = groupList.split(",");
            for (final String groupName : groupNames) {
                jiraUsernames.addAll(new JiraServices().getGroupManager().getUserNamesInGroup(groupName));
            }
        }
        logger.debug("getJiraUsernames(): returning: " + jiraUsernames);
        return jiraUsernames;
    }

    // This must be "package protected" to avoid synthetic access
    List<JiraProject> getJiraProjects(final List<Project> jiraProjects) {
        final List<JiraProject> newJiraProjects = new ArrayList<>();
        if (jiraProjects != null && !jiraProjects.isEmpty()) {
            for (final Project oldProject : jiraProjects) {
                final JiraProject newProject = new JiraProject();
                newProject.setProjectName(oldProject.getName());
                newProject.setProjectId(oldProject.getId());

                newJiraProjects.add(newProject);
            }
        }
        return newJiraProjects;
    }

    // This must be "package protected" to avoid synthetic access
    void validateCreator(final BlackDuckJiraConfigSerializable config) {
        if (StringUtils.isBlank(config.getCreator())) {
            config.setGeneralSettingsError(JiraConfigErrorStrings.NO_CREATOR_SPECIFIED_ERROR);
        }
    }

    // This must be "package protected" to avoid synthetic access
    void validateCreator(final BlackDuckJiraConfigSerializable config, final PluginSettings settings) {
        if (StringUtils.isBlank(config.getCreator())) {
            config.setGeneralSettingsError(JiraConfigErrorStrings.NO_CREATOR_SPECIFIED_ERROR);
        }
        if (isUserAuthorizedForPlugin(settings, config.getCreator())) {
            return;
        } else {
            config.setGeneralSettingsError(JiraConfigErrorStrings.UNAUTHORIZED_CREATOR_ERROR);
        }
    }

    // This must be "package protected" to avoid synthetic access
    void validateInterval(final BlackDuckJiraConfigSerializable config) {
        if (StringUtils.isBlank(config.getIntervalBetweenChecks())) {
            config.setGeneralSettingsError(JiraConfigErrorStrings.NO_INTERVAL_FOUND_ERROR);
        } else {
            try {
                final int interval = stringToInteger(config.getIntervalBetweenChecks());
                if (interval <= 0) {
                    config.setGeneralSettingsError(JiraConfigErrorStrings.INVALID_INTERVAL_FOUND_ERROR);
                }
            } catch (final IllegalArgumentException e) {
                config.setGeneralSettingsError(e.getMessage());
            }
        }
    }

    // This must be "package protected" to avoid synthetic access
    void updatePluginTaskInterval(final String previousIntervalString, final String newIntervalString) {
        final int previousInterval = NumberUtils.toInt(previousIntervalString);
        final int newInterval;
        try {
            newInterval = stringToInteger(newIntervalString);
            if (newInterval > 0 && newInterval != previousInterval) {
                blackDuckMonitor.changeInterval();
            }
        } catch (final IllegalArgumentException e) {
            logger.error("The specified interval is not an integer.");
        }
    }

    // This must be "package protected" to avoid synthetic access
    void validateMapping(final BlackDuckJiraConfigSerializable config) {
        if (config.getHubProjectMappings() != null && !config.getHubProjectMappings().isEmpty()) {
            boolean hasEmptyMapping = false;
            for (final BlackDuckProjectMapping mapping : config.getHubProjectMappings()) {
                boolean jiraProjectBlank = true;
                boolean blackDuckProjectBlank = true;
                if (mapping.getJiraProject() != null) {
                    if (mapping.getJiraProject().getProjectId() != null) {
                        jiraProjectBlank = false;
                    }
                }
                if (StringUtils.isNotBlank(mapping.getBlackDuckProjectName())) {
                    blackDuckProjectBlank = false;
                }
                if (jiraProjectBlank || blackDuckProjectBlank) {
                    hasEmptyMapping = true;
                }
            }
            if (hasEmptyMapping) {
                config.setHubProjectMappingError(concatErrorMessage(config.getHubProjectMappingError(), JiraConfigErrorStrings.MAPPING_HAS_EMPTY_ERROR));
            }
        }
    }
}
