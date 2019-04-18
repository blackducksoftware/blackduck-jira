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
package com.blackducksoftware.integration.jira.config.controller.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.BlackDuckPluginDateFormatter;
import com.blackducksoftware.integration.jira.common.BlackDuckWorkflowStatus;
import com.blackducksoftware.integration.jira.common.WorkflowHelper;
import com.blackducksoftware.integration.jira.common.model.JiraProject;
import com.blackducksoftware.integration.jira.common.settings.GlobalConfigurationAccessor;
import com.blackducksoftware.integration.jira.common.settings.JiraSettingsAccessor;
import com.blackducksoftware.integration.jira.common.settings.PluginConfigurationAccessor;
import com.blackducksoftware.integration.jira.common.settings.model.GeneralIssueCreationConfigModel;
import com.blackducksoftware.integration.jira.common.settings.model.PluginGroupsConfigModel;
import com.blackducksoftware.integration.jira.common.settings.model.PluginIssueCreationConfigModel;
import com.blackducksoftware.integration.jira.common.settings.model.ProjectMappingConfigModel;
import com.blackducksoftware.integration.jira.common.settings.model.TicketCriteriaConfigModel;
import com.blackducksoftware.integration.jira.config.JiraConfigErrorStrings;
import com.blackducksoftware.integration.jira.config.JiraServices;
import com.blackducksoftware.integration.jira.config.controller.AuthorizationChecker;
import com.blackducksoftware.integration.jira.config.model.BlackDuckJiraConfigSerializable;
import com.blackducksoftware.integration.jira.task.BlackDuckMonitor;

public class IssueCreationConfigActions {
    private final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));
    private final PluginConfigurationAccessor pluginConfigurationAccessor;
    private final GlobalConfigurationAccessor globalConfigurationAccessor;
    private final AuthorizationChecker authorizationChecker;
    private final ProjectManager projectManager;
    private final WorkflowHelper workflowHelper;
    private final BlackDuckMonitor blackDuckMonitor;
    private final ProjectMappingConfigActions projectMappingConfigActions;

    public IssueCreationConfigActions(final JiraSettingsAccessor jiraSettingsAccessor, final AuthorizationChecker authorizationChecker, final ProjectManager projectManager,
        final WorkflowHelper workflowHelper, final BlackDuckMonitor blackDuckMonitor) {
        this.pluginConfigurationAccessor = jiraSettingsAccessor.createPluginConfigurationAccessor();
        this.globalConfigurationAccessor = jiraSettingsAccessor.createGlobalConfigurationAccessor();
        this.authorizationChecker = authorizationChecker;
        this.projectManager = projectManager;
        this.workflowHelper = workflowHelper;
        this.blackDuckMonitor = blackDuckMonitor;
        this.projectMappingConfigActions = new ProjectMappingConfigActions(jiraSettingsAccessor, workflowHelper);
    }

    public BlackDuckJiraConfigSerializable getCreator() {
        final BlackDuckJiraConfigSerializable txConfig = new BlackDuckJiraConfigSerializable();

        final PluginIssueCreationConfigModel issueCreationConfig = globalConfigurationAccessor.getIssueCreationConfig();
        final String creator = issueCreationConfig.getGeneral().getDefaultIssueCreator();
        txConfig.setCreator(creator);
        validateCreator(txConfig);
        return txConfig;
    }

    public BlackDuckJiraConfigSerializable getCreatorCandidates() {
        final BlackDuckJiraConfigSerializable config = new BlackDuckJiraConfigSerializable();
        config.setCreatorCandidates(new TreeSet());

        final SortedSet<String> creatorCandidates = getIssueCreatorCandidates();
        config.setCreatorCandidates(creatorCandidates);

        if (creatorCandidates.isEmpty()) {
            config.setGeneralSettingsError(JiraConfigErrorStrings.NO_CREATOR_CANDIDATES_FOUND);
        }
        return config;
    }

    public BlackDuckJiraConfigSerializable getJiraProjects() {
        final List<JiraProject> jiraProjects = getJiraProjects(projectManager.getProjectObjects());

        final BlackDuckJiraConfigSerializable txProjectsConfig = new BlackDuckJiraConfigSerializable();
        txProjectsConfig.setJiraProjects(jiraProjects);

        if (jiraProjects.isEmpty()) {
            txProjectsConfig.setJiraProjectsError(JiraConfigErrorStrings.NO_JIRA_PROJECTS_FOUND);
        }
        return txProjectsConfig;
    }

    public BlackDuckJiraConfigSerializable getCommentOnIssueUpdates() {
        logger.debug("GET /comment/updatechoice transaction");
        final BlackDuckJiraConfigSerializable txConfig = new BlackDuckJiraConfigSerializable();
        final PluginIssueCreationConfigModel issueCreationConfig = globalConfigurationAccessor.getIssueCreationConfig();
        final Boolean choice = issueCreationConfig.getTicketCriteria().getCommentOnIssueUpdates();
        logger.debug("choice: " + choice);
        txConfig.setCommentOnIssueUpdatesChoice(choice);
        return txConfig;
    }

    public BlackDuckJiraConfigSerializable getProjectReviewerNotificationsChoice() {
        logger.debug("GET /project/reviewerChoice transaction");
        final BlackDuckJiraConfigSerializable txConfig = new BlackDuckJiraConfigSerializable();
        final PluginIssueCreationConfigModel issueCreationConfig = globalConfigurationAccessor.getIssueCreationConfig();
        final Boolean choice = issueCreationConfig.getTicketCriteria().getAddComponentReviewerToTickets();
        logger.debug("choice: " + choice);
        txConfig.setProjectReviewerNotificationsChoice(choice);
        return txConfig;
    }

    public BlackDuckJiraConfigSerializable getInterval() {
        final BlackDuckJiraConfigSerializable txConfig = new BlackDuckJiraConfigSerializable();
        final PluginIssueCreationConfigModel issueCreationConfig = globalConfigurationAccessor.getIssueCreationConfig();

        final Optional<Integer> intervalBetweenChecks = issueCreationConfig.getGeneral().getInterval();
        intervalBetweenChecks.map(String::valueOf).ifPresent(txConfig::setIntervalBetweenChecks);

        validateInterval(txConfig);
        return txConfig;
    }

    public BlackDuckJiraConfigSerializable updateConfig(final BlackDuckJiraConfigSerializable config, final HttpServletRequest request) {
        final List<JiraProject> jiraProjects = getJiraProjects(projectManager.getProjectObjects());
        config.setJiraProjects(jiraProjects);
        validateInterval(config);
        validateCreator(config);
        projectMappingConfigActions.validateMapping(config);

        final String username = authorizationChecker.getUsername(request).orElse(null);
        pluginConfigurationAccessor.setJiraAdminUser(username);

        final String firstTimeSave = pluginConfigurationAccessor.getFirstTimeSave();
        if (firstTimeSave == null) {
            final BlackDuckPluginDateFormatter pluginDateFormatter = new BlackDuckPluginDateFormatter();
            pluginConfigurationAccessor.setFirstTimeSave(pluginDateFormatter.format(new Date()));
        }

        final PluginIssueCreationConfigModel previousIssueCreationConfig = globalConfigurationAccessor.getIssueCreationConfig();

        final String issueCreatorJiraUser = config.getCreator();
        final Optional<Integer> previousInterval = previousIssueCreationConfig.getGeneral().getInterval();
        final Integer intervalBetweenChecks = Integer.parseInt(config.getIntervalBetweenChecks());
        final GeneralIssueCreationConfigModel general = new GeneralIssueCreationConfigModel(intervalBetweenChecks, issueCreatorJiraUser);

        final ProjectMappingConfigModel projectMapping = new ProjectMappingConfigModel(config.getHubProjectMappingsJson());

        final Boolean commentOnIssueUpdatesChoice = config.getCommentOnIssueUpdatesChoice();
        final Boolean projectReviewerNotificationsChoice = config.getProjectReviewerNotificationsChoice();
        final TicketCriteriaConfigModel ticketCriteria = new TicketCriteriaConfigModel(config.getPolicyRulesJson(), commentOnIssueUpdatesChoice, projectReviewerNotificationsChoice);

        final PluginIssueCreationConfigModel issueCreationConfig = new PluginIssueCreationConfigModel(general, projectMapping, ticketCriteria);

        logger.debug("Setting issue creator jira user to: " + issueCreatorJiraUser);
        logger.debug("Setting commentOnIssueUpdatesChoice to " + commentOnIssueUpdatesChoice.toString());
        logger.debug("Setting projectReviewerNotificationsChoice to " + projectReviewerNotificationsChoice.toString());

        globalConfigurationAccessor.setIssueCreationConfig(issueCreationConfig);
        updatePluginTaskInterval(previousInterval.orElse(0), intervalBetweenChecks);

        return config;
    }

    private void validateCreator(final BlackDuckJiraConfigSerializable config) {
        if (StringUtils.isBlank(config.getCreator())) {
            config.setGeneralSettingsError(JiraConfigErrorStrings.NO_CREATOR_SPECIFIED_ERROR);
        }

        final PluginGroupsConfigModel groupsConfig = globalConfigurationAccessor.getGroupsConfig();
        if (authorizationChecker.isValidAuthorization(config.getCreator(), groupsConfig.getGroups())) {
            return;
        } else {
            config.setGeneralSettingsError(JiraConfigErrorStrings.UNAUTHORIZED_CREATOR_ERROR);
        }
    }

    private SortedSet<String> getIssueCreatorCandidates() {
        final SortedSet<String> jiraUsernames = new TreeSet<>();
        final PluginGroupsConfigModel groupsConfig = globalConfigurationAccessor.getGroupsConfig();
        final Collection<String> groupNames = groupsConfig.getGroups();
        for (final String groupName : groupNames) {
            jiraUsernames.addAll(new JiraServices().getGroupManager().getUserNamesInGroup(groupName));
        }
        logger.debug("getJiraUsernames(): returning: " + jiraUsernames);
        return jiraUsernames;
    }

    private List<JiraProject> getJiraProjects(final List<Project> jiraProjects) {
        final List<JiraProject> newJiraProjects = new ArrayList<>();
        if (jiraProjects != null && !jiraProjects.isEmpty()) {
            for (final Project oldProject : jiraProjects) {
                final JiraProject newProject = new JiraProject();
                newProject.setProjectName(oldProject.getName());
                newProject.setProjectId(oldProject.getId());

                final BlackDuckWorkflowStatus projectWorkflowStatus = workflowHelper.getBlackDuckWorkflowStatus(oldProject);
                newProject.setWorkflowStatus(projectWorkflowStatus.getPrettyPrintName());

                newJiraProjects.add(newProject);
            }
        }
        return newJiraProjects;
    }

    private void validateInterval(final BlackDuckJiraConfigSerializable config) {
        final String intervalBetweenChecks = config.getIntervalBetweenChecks();
        if (StringUtils.isBlank(intervalBetweenChecks)) {
            config.setGeneralSettingsError(JiraConfigErrorStrings.NO_INTERVAL_FOUND_ERROR);
        } else {
            try {
                final Integer interval = Integer.valueOf(intervalBetweenChecks);
                if (interval <= 0) {
                    config.setGeneralSettingsError(JiraConfigErrorStrings.INVALID_INTERVAL_FOUND_ERROR);
                }
            } catch (final NumberFormatException e) {
                config.setGeneralSettingsError(e.getMessage());
            }
        }
    }

    private void updatePluginTaskInterval(final Integer previousInterval, final Integer newInterval) {
        if (newInterval == null) {
            logger.error("The specified interval is not an integer.");
        }
        if (newInterval > 0 && newInterval != previousInterval) {
            blackDuckMonitor.changeInterval();
        }
    }

}
