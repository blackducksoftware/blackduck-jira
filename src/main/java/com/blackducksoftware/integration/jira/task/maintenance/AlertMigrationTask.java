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
package com.blackducksoftware.integration.jira.task.maintenance;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.bc.issue.properties.IssuePropertyService;
import com.atlassian.jira.entity.property.EntityPropertyService;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.jql.parser.DefaultJqlQueryParser;
import com.atlassian.jira.jql.parser.JqlParseException;
import com.atlassian.jira.jql.parser.JqlQueryParser;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.query.Query;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;
import com.blackducksoftware.integration.jira.common.JiraUserContext;
import com.blackducksoftware.integration.jira.common.exception.JiraIssueException;
import com.blackducksoftware.integration.jira.common.model.PluginBlackDuckServerConfigModel;
import com.blackducksoftware.integration.jira.data.accessor.GlobalConfigurationAccessor;
import com.blackducksoftware.integration.jira.data.accessor.JiraSettingsAccessor;
import com.blackducksoftware.integration.jira.data.accessor.MigrationAccessor;
import com.blackducksoftware.integration.jira.data.accessor.PluginConfigurationAccessor;
import com.blackducksoftware.integration.jira.issue.conversion.output.AlertIssueSearchProperties;
import com.blackducksoftware.integration.jira.issue.handler.JiraIssuePropertyWrapper;
import com.blackducksoftware.integration.jira.issue.handler.JiraIssueServiceWrapper;
import com.blackducksoftware.integration.jira.issue.model.GeneralIssueCreationConfigModel;
import com.blackducksoftware.integration.jira.issue.model.ProjectMappingConfigModel;
import com.blackducksoftware.integration.jira.web.JiraServices;
import com.blackducksoftware.integration.jira.web.model.BlackDuckJiraConfigSerializable;
import com.blackducksoftware.integration.jira.web.model.JiraProject;
import com.google.common.collect.ImmutableMap;

public class AlertMigrationTask implements Callable<String> {
    private static final String JIRA_QUERY_PARAM_NAME_ISSUE_TYPE = "issuetype";
    private static final String JIRA_QUERY_PARAM_NAME_PROEJCT = "project";
    private static final String JIRA_QUERY_CONJUNCTION = " AND ";
    private static final String JIRA_QUERY_DISJUNCTION = " OR ";

    private static final Integer MAX_BATCH_SIZE = 100;
    private static final String COMPLETE_STATUS_MESSAGE = "Complete";
    private static final String NOT_CONFIGURED_STATUS_MESSAGE = "NOT CONFIGURED";
    private static final String ERROR_STATUS_MESSAGE = "ERROR";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final JiraSettingsAccessor jiraSettingsAccessor;
    private final JiraServices jiraServices;
    private final IssuePropertyService issuePropertyService;
    private final MigrationAccessor migrationAccessor;

    public AlertMigrationTask(JiraSettingsAccessor jiraSettingsAccessor, IssuePropertyService issuePropertyService) {
        this.jiraSettingsAccessor = jiraSettingsAccessor;
        this.jiraServices = new JiraServices();
        this.issuePropertyService = issuePropertyService;
        this.migrationAccessor = new MigrationAccessor(jiraSettingsAccessor);
    }

    @Override
    public String call() {
        CustomFieldManager customFieldManager = jiraServices.getCustomFieldManager();
        Optional<CustomField> optionalProjectField = customFieldManager.getCustomFieldObjectsByName(BlackDuckJiraConstants.BLACKDUCK_CUSTOM_FIELD_PROJECT).stream().findFirst();
        Optional<CustomField> optionalProjectVersionField = customFieldManager.getCustomFieldObjectsByName(BlackDuckJiraConstants.BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION).stream().findFirst();
        Optional<CustomField> optionalComponentField = customFieldManager.getCustomFieldObjectsByName(BlackDuckJiraConstants.BLACKDUCK_CUSTOM_FIELD_COMPONENT).stream().findFirst();
        Optional<CustomField> optionalComponentVersionField = customFieldManager.getCustomFieldObjectsByName(BlackDuckJiraConstants.BLACKDUCK_CUSTOM_FIELD_COMPONENT_VERSION).stream().findFirst();

        CustomField projectField;
        CustomField projectVersionField;
        CustomField componentField;
        CustomField componentVersionField;
        if (optionalProjectField.isPresent() && optionalProjectVersionField.isPresent() && optionalComponentField.isPresent() && optionalComponentVersionField.isPresent()) {
            projectField = optionalProjectField.get();
            projectVersionField = optionalProjectVersionField.get();
            componentField = optionalComponentField.get();
            componentVersionField = optionalComponentVersionField.get();
        } else {
            logger.warn(String.format("Cannot find the custom field(s) necessary for this task: %s, %s, %s, %s",
                BlackDuckJiraConstants.BLACKDUCK_CUSTOM_FIELD_PROJECT, BlackDuckJiraConstants.BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION, BlackDuckJiraConstants.BLACKDUCK_CUSTOM_FIELD_COMPONENT,
                BlackDuckJiraConstants.BLACKDUCK_CUSTOM_FIELD_COMPONENT_VERSION));
            return NOT_CONFIGURED_STATUS_MESSAGE;
        }

        PluginConfigurationAccessor pluginConfigurationAccessor = jiraSettingsAccessor.createPluginConfigurationAccessor();
        GlobalConfigurationAccessor globalConfigurationAccessor = jiraSettingsAccessor.createGlobalConfigurationAccessor();

        GeneralIssueCreationConfigModel generalIssueConfig = globalConfigurationAccessor.getIssueCreationConfig().getGeneral();
        Optional<JiraUserContext> optionalJiraUserContext = JiraUserContext.create(pluginConfigurationAccessor.getJiraAdminUser(), generalIssueConfig.getDefaultIssueCreator(), jiraServices.getUserManager());

        JiraUserContext jiraUserContext;
        if (optionalJiraUserContext.isPresent()) {
            jiraUserContext = optionalJiraUserContext.get();
        } else {
            logger.warn("No (valid) user in configuration data; The plugin has likely not yet been configured; The task cannot run (yet)");
            return NOT_CONFIGURED_STATUS_MESSAGE;
        }

        PluginBlackDuckServerConfigModel blackDuckServerConfig = globalConfigurationAccessor.getBlackDuckServerConfig();
        String blackDuckUrl = blackDuckServerConfig.getUrl();

        ProjectMappingConfigModel projectMapping = globalConfigurationAccessor.getIssueCreationConfig().getProjectMapping();
        BlackDuckJiraConfigSerializable config = new BlackDuckJiraConfigSerializable();
        config.setHubProjectMappingsJson(projectMapping.getMappingsJson());

        List<JiraProject> jiraProjects = config.getJiraProjects();
        List<String> migratedProjects = migrationAccessor.getMigratedProjects();

        List<JiraProject> projectsToMigrate = jiraProjects.stream()
                                                  .filter(jiraProject -> !migratedProjects.contains(jiraProject.getProjectName()))
                                                  .collect(Collectors.toList());
        for (JiraProject jiraProject : projectsToMigrate) {
            logger.info(String.format("Checking Jira Project %s for Black Duck issues to migrate.", jiraProject.getProjectName()));
            Optional<Query> optionalBlackDuckIssueQuery = createBlackDuckIssueQuery(jiraProject.getProjectName());
            Query searchQuery;
            if (optionalBlackDuckIssueQuery.isPresent()) {
                searchQuery = optionalBlackDuckIssueQuery.get();
            } else {
                return ERROR_STATUS_MESSAGE;
            }

            try {
                JiraIssueServiceWrapper issueServiceWrapper = JiraIssueServiceWrapper.createIssueServiceWrapperFromJiraServices(jiraServices, jiraUserContext, ImmutableMap.of());
                findAndUpdateIssuesInBatches(issueServiceWrapper, jiraUserContext.getJiraAdminUser(), jiraProject.getProjectName(), searchQuery, blackDuckUrl, projectField, projectVersionField, componentField, componentVersionField);
                migratedProjects.add(jiraProject.getProjectName());
                migrationAccessor.updateMigratedProjects(migratedProjects);
            } catch (Exception e) {
                logger.warn("There was a problem while attempting to migrate the existing Black Duck issues: " + e.getMessage());
                return ERROR_STATUS_MESSAGE;
            }
        }
        return COMPLETE_STATUS_MESSAGE;
    }

    private void findAndUpdateIssuesInBatches(JiraIssueServiceWrapper issueServiceWrapper, ApplicationUser user, String jiraProject, Query query, String blackDuckUrl, CustomField projectField, CustomField projectVersionField,
        CustomField componentField,
        CustomField componentVersionField) throws JiraIssueException {
        int offset = 0;
        final int limit = MAX_BATCH_SIZE;
        List<Issue> foundIssues;
        do {
            foundIssues = issueServiceWrapper.queryForIssues(user, query, offset, limit);
            offset += limit;
            logger.info(String.format("Processing %s issues for project %s between %s and %s.", foundIssues.size(), jiraProject, offset, limit));
            processBatch(issueServiceWrapper, user, foundIssues, blackDuckUrl, projectField, projectVersionField, componentField, componentVersionField);
        } while (foundIssues.size() == limit);
    }

    private void processBatch(JiraIssueServiceWrapper issueServiceWrapper, ApplicationUser admin, List<Issue> issuesToProcess, String blackDuckUrl, CustomField projectField, CustomField projectVersionField, CustomField componentField,
        CustomField componentVersionField) {
        for (Issue issue : issuesToProcess) {
            EntityPropertyService.PropertyKeys<Issue> propertiesKeys = issuePropertyService.getPropertiesKeys(admin, issue.getId());
            boolean containsAlertKey = propertiesKeys.getKeys().contains(JiraIssuePropertyWrapper.ALERT_PROPERTY_KEY);
            if (containsAlertKey) {
                continue;
            }
            String issueTypeName = issue.getIssueType().getName();

            String alertCategory = "Policy";
            if (BlackDuckJiraConstants.BLACKDUCK_VULNERABILITY_ISSUE.equals(issueTypeName)) {
                alertCategory = "Vulnerability";
            }

            String projectName = (String) issue.getCustomFieldValue(projectField);
            String projectVersionName = (String) issue.getCustomFieldValue(projectVersionField);
            String componentName = (String) issue.getCustomFieldValue(componentField);
            String componentVersionName = (String) issue.getCustomFieldValue(componentVersionField);

            AlertIssueSearchProperties alertIssueSearchProperties = new AlertIssueSearchProperties("Black Duck", blackDuckUrl, "Project", projectName, "Project Version",
                projectVersionName, alertCategory, "Component", componentName, "Component Version", componentVersionName, "");
            try {
                issueServiceWrapper.getIssuePropertyWrapper().addAlertIssueProperties(issue.getId(), admin, alertIssueSearchProperties);
                logger.trace(String.format("Added the Alert issue properties to issue %s.", issue.getKey()));
            } catch (JiraIssueException e) {
                logger.error(String.format("Error adding issue properties to issue %s : %s", issue.getKey(), e.getMessage()), e);
            }
        }
    }

    private Optional<Query> createBlackDuckIssueQuery(String projectName) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("(");
        appendEqualityCheck(queryBuilder, JIRA_QUERY_PARAM_NAME_ISSUE_TYPE, BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_ISSUE);
        queryBuilder.append(JIRA_QUERY_DISJUNCTION);
        appendEqualityCheck(queryBuilder, JIRA_QUERY_PARAM_NAME_ISSUE_TYPE, BlackDuckJiraConstants.BLACKDUCK_SECURITY_POLICY_VIOLATION_ISSUE);
        queryBuilder.append(JIRA_QUERY_DISJUNCTION);
        appendEqualityCheck(queryBuilder, JIRA_QUERY_PARAM_NAME_ISSUE_TYPE, BlackDuckJiraConstants.BLACKDUCK_VULNERABILITY_ISSUE);
        queryBuilder.append(") ");
        queryBuilder.append(JIRA_QUERY_CONJUNCTION);
        appendEqualityCheck(queryBuilder, JIRA_QUERY_PARAM_NAME_PROEJCT, projectName);
        // Query from least recently updated to most
        queryBuilder.append(" ORDER BY updated ASC");

        String queryString = queryBuilder.toString();
        try {
            JqlQueryParser queryParser = new DefaultJqlQueryParser();
            Query orphanQuery = queryParser.parseQuery(queryString);
            return Optional.of(orphanQuery);
        } catch (JqlParseException e) {
            logger.warn("The query generated to search for orphan issues was invalid: " + queryString);
        }
        return Optional.empty();
    }

    private void appendEqualityCheck(StringBuilder queryBuilder, String key, String value) {
        queryBuilder.append(key);
        queryBuilder.append(" = ");
        queryBuilder.append(enquote(value));
    }

    private String enquote(String text) {
        return "\"" + text + "\"";
    }

}
