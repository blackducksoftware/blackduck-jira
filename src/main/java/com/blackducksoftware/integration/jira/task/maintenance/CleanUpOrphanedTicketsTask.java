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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.jql.parser.DefaultJqlQueryParser;
import com.atlassian.jira.jql.parser.JqlParseException;
import com.atlassian.jira.jql.parser.JqlQueryParser;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.query.Query;
import com.blackducksoftware.integration.jira.blackduck.BlackDuckConnectionHelper;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.JiraUserContext;
import com.blackducksoftware.integration.jira.common.WorkflowHelper;
import com.blackducksoftware.integration.jira.common.exception.JiraIssueException;
import com.blackducksoftware.integration.jira.common.model.PluginBlackDuckServerConfigModel;
import com.blackducksoftware.integration.jira.data.accessor.GlobalConfigurationAccessor;
import com.blackducksoftware.integration.jira.data.accessor.JiraSettingsAccessor;
import com.blackducksoftware.integration.jira.data.accessor.PluginConfigurationAccessor;
import com.blackducksoftware.integration.jira.issue.handler.JiraIssueServiceWrapper;
import com.blackducksoftware.integration.jira.issue.model.GeneralIssueCreationConfigModel;
import com.blackducksoftware.integration.jira.web.JiraServices;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.synopsys.integration.blackduck.exception.BlackDuckApiException;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.exception.IntegrationRestException;
import com.synopsys.integration.rest.request.Response;

public class CleanUpOrphanedTicketsTask implements Callable<String> {
    private static final String JIRA_QUERY_PARAM_NAME_ISSUE_STATUS = "status";
    private static final String JIRA_QUERY_PARAM_NAME_ISSUE_TYPE = "issuetype";
    private static final String JIRA_QUERY_CONJUNCTION = " AND ";
    private static final String JIRA_QUERY_DISJUNCTION = " OR ";

    // TODO determine reasonable numbers here
    private static final Integer MAX_BATCH_SIZE = 100;
    private static final Integer MAX_BATCHES_PER_RUN = 100;

    private static final String DEFAULT_STATUS_MESSAGE = "COMPLETED";
    private static final String NOT_CONFIGURED_STATUS_MESSAGE = "NOT CONFIGURED";
    private static final String ERROR_STATUS_MESSAGE = "ERROR";

    private final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));
    private final JiraSettingsAccessor jiraSettingsAccessor;
    private final JiraServices jiraServices;
    private final HashSet<String> badProjectVersionUrlCache;

    public CleanUpOrphanedTicketsTask(final JiraSettingsAccessor jiraSettingsAccessor) {
        this.jiraSettingsAccessor = jiraSettingsAccessor;
        this.jiraServices = new JiraServices();
        this.badProjectVersionUrlCache = new HashSet<>();
    }

    @Override
    public String call() {
        final CustomFieldManager customFieldManager = jiraServices.getCustomFieldManager();
        final Optional<CustomField> optionalProjectVersionUrlField = customFieldManager.getCustomFieldObjectsByName(BlackDuckJiraConstants.BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION_URL).stream().findFirst();
        final Optional<CustomField> optionalProjectVersionLastUpdatedField = customFieldManager.getCustomFieldObjectsByName(BlackDuckJiraConstants.BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION_LAST_UPDATED).stream().findFirst();

        final CustomField projectVersionUrlField;
        final CustomField projectVersionLastUpdatedField;
        if (optionalProjectVersionUrlField.isPresent() && optionalProjectVersionLastUpdatedField.isPresent()) {
            projectVersionUrlField = optionalProjectVersionUrlField.get();
            projectVersionLastUpdatedField = optionalProjectVersionLastUpdatedField.get();
        } else {
            logger.warn(String.format("Cannot find the custom field(s) necessary for this task: %s, %s",
                BlackDuckJiraConstants.BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION_URL, BlackDuckJiraConstants.BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION_LAST_UPDATED));
            return NOT_CONFIGURED_STATUS_MESSAGE;
        }

        final PluginConfigurationAccessor pluginConfigurationAccessor = jiraSettingsAccessor.createPluginConfigurationAccessor();
        final GlobalConfigurationAccessor globalConfigurationAccessor = jiraSettingsAccessor.createGlobalConfigurationAccessor();
        final GeneralIssueCreationConfigModel generalIssueConfig = globalConfigurationAccessor.getIssueCreationConfig().getGeneral();
        final Optional<JiraUserContext> optionalJiraUserContext = JiraUserContext.create(logger, pluginConfigurationAccessor.getJiraAdminUser(), generalIssueConfig.getDefaultIssueCreator(), jiraServices.getUserManager());

        final JiraUserContext jiraUserContext;
        if (optionalJiraUserContext.isPresent()) {
            jiraUserContext = optionalJiraUserContext.get();
        } else {
            logger.warn("No (valid) user in configuration data; The plugin has likely not yet been configured; The task cannot run (yet)");
            return NOT_CONFIGURED_STATUS_MESSAGE;
        }

        final Optional<Query> optionalOrphanedTicketQuery = createOrphanedTicketQuery();
        final Query searchQuery;
        if (optionalOrphanedTicketQuery.isPresent()) {
            searchQuery = optionalOrphanedTicketQuery.get();
        } else {
            return ERROR_STATUS_MESSAGE;
        }

        final BlackDuckService blackDuckService;
        try {
            final PluginBlackDuckServerConfigModel blackDuckServerConfig = globalConfigurationAccessor.getBlackDuckServerConfig();
            final BlackDuckConnectionHelper blackDuckConnectionHelper = new BlackDuckConnectionHelper();
            final BlackDuckServicesFactory blackDuckServicesFactory;
            blackDuckServicesFactory = blackDuckConnectionHelper.createBlackDuckServicesFactory(logger, blackDuckServerConfig.createBlackDuckServerConfigBuilder());
            blackDuckService = blackDuckServicesFactory.createBlackDuckService();

            final Response connectionAttemptResponse = blackDuckService.get(blackDuckServerConfig.getUrl());
            connectionAttemptResponse.throwExceptionForError();
        } catch (final IntegrationException e) {
            logger.warn("Could not establish a connection to the Black Duck server.");
            return NOT_CONFIGURED_STATUS_MESSAGE;
        }

        try {
            final JiraIssueServiceWrapper issueServiceWrapper = JiraIssueServiceWrapper.createIssueServiceWrapperFromJiraServices(jiraServices, jiraUserContext, new Gson(), ImmutableMap.of());
            findAndUpdateIssuesInBatches(issueServiceWrapper, jiraUserContext.getJiraAdminUser(), searchQuery, projectVersionUrlField, projectVersionLastUpdatedField, blackDuckService);
        } catch (final Exception e) {
            logger.warn("There was a problem while attempting to clean up orphan tickets: " + e.getMessage());
            return ERROR_STATUS_MESSAGE;
        }
        return DEFAULT_STATUS_MESSAGE;
    }

    private void findAndUpdateIssuesInBatches(final JiraIssueServiceWrapper issueServiceWrapper, final ApplicationUser user, final Query query, final CustomField projectVersionUrlField, final CustomField projectVersionLastUpdatedField,
        final BlackDuckService blackDuckService)
        throws JiraIssueException, IntegrationException {
        int batchCount = 0;
        int offset = 0;
        final int limit = MAX_BATCH_SIZE;
        List<Issue> foundIssues;
        do {
            foundIssues = issueServiceWrapper.queryForIssues(user, query, offset, limit);
            offset += limit;
            processBatch(issueServiceWrapper, user, foundIssues, projectVersionUrlField, projectVersionLastUpdatedField, blackDuckService);
            batchCount++;
        } while (foundIssues.size() == limit || batchCount <= MAX_BATCHES_PER_RUN);
    }

    private void processBatch(final JiraIssueServiceWrapper issueServiceWrapper, final ApplicationUser admin, final List<Issue> issuesToProcess, final CustomField projectVersionUrlField, final CustomField projectVersionLastUpdatedField,
        final BlackDuckService blackDuckService) throws IntegrationException, JiraIssueException {
        for (final Issue issue : issuesToProcess) {
            final String projectVersionUrl = (String) issue.getCustomFieldValue(projectVersionUrlField);
            if (StringUtils.isNotBlank(projectVersionUrl)) {
                if (isProjectVersionInvalid(projectVersionUrl, blackDuckService)) {
                    logger.debug("The Black Duck project version for " + issue.getKey() + " was not found on the current Black Duck server.");
                    resolveIssue(issueServiceWrapper, issue);
                } else {
                    // Update this issue so it is not processed the next time we query for the least-recently updated issues.
                    updateIssueMetadata(issueServiceWrapper, issue, admin, projectVersionLastUpdatedField);
                }
            }
        }
    }

    private boolean isProjectVersionInvalid(final String projectVersionUrl, final BlackDuckService blackDuckService) throws IntegrationException {
        if (badProjectVersionUrlCache.contains(projectVersionUrl)) {
            return true;
        }
        try {
            blackDuckService.get(projectVersionUrl);
        } catch (final BlackDuckApiException apiException) {
            final IntegrationRestException restException = apiException.getOriginalIntegrationRestException();
            final int statusCode = restException.getHttpStatusCode();
            if (404 == statusCode) {
                badProjectVersionUrlCache.add(projectVersionUrl);
                return true;
            }
        }
        return false;
    }

    private void resolveIssue(final JiraIssueServiceWrapper issueServiceWrapper, final Issue issue) throws JiraIssueException {
        final JiraWorkflow issueWorkflow = issueServiceWrapper.getWorkflow(issue);
        if (issueWorkflow == null) {
            logger.debug("Unknown workflow. No action will be taken.");
            return;
        }

        if (!WorkflowHelper.matchesBlackDuckWorkflowName(issueWorkflow.getName())) {
            logger.debug("This issue does not use the Black Duck plugin workflow. No action will be taken.");
            return;
        }

        logger.debug("Resolving issue: " + issue.getKey());
        final String workflowStepResolveName = BlackDuckJiraConstants.BLACKDUCK_WORKFLOW_TRANSITION_REMOVE_OR_OVERRIDE;
        final List<ActionDescriptor> actions = issueWorkflow.getLinkedStep(issue.getStatus()).getActions();
        final Optional<Integer> optionalResolveActionId = actions
                                                              .stream()
                                                              .filter(descriptor -> StringUtils.isNotBlank(descriptor.getName()) && descriptor.getName().equals(workflowStepResolveName))
                                                              .map(ActionDescriptor::getId)
                                                              .findFirst();
        if (optionalResolveActionId.isPresent()) {
            final Issue updatedIssue = issueServiceWrapper.transitionIssue(issue, optionalResolveActionId.get());
            if (updatedIssue != null) {
                issueServiceWrapper.addComment(issue, "This issue was auto-resolved by the Black Duck Plugin because the Project Version no longer exists on the Black Duck server.");
            }
        }
    }

    private void updateIssueMetadata(final JiraIssueServiceWrapper issueServiceWrapper, final Issue issue, final ApplicationUser admin, final CustomField lastUpdatedByBlackDuckField) throws JiraIssueException {
        final String lastUpdatedCurrentValue = (String) issue.getCustomFieldValue(lastUpdatedByBlackDuckField);
        final String lastUpdatedNewValue;
        if (StringUtils.endsWith(lastUpdatedCurrentValue, StringUtils.SPACE)) {
            lastUpdatedNewValue = StringUtils.trimToEmpty(lastUpdatedCurrentValue);
        } else {
            lastUpdatedNewValue = lastUpdatedCurrentValue + StringUtils.SPACE;
        }

        issueServiceWrapper.updateCustomField(issue, admin, lastUpdatedByBlackDuckField, lastUpdatedNewValue);
    }

    private Optional<Query> createOrphanedTicketQuery() {
        final StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("(");
        appendEqualityCheck(queryBuilder, JIRA_QUERY_PARAM_NAME_ISSUE_TYPE, BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_ISSUE);
        queryBuilder.append(JIRA_QUERY_DISJUNCTION);
        appendEqualityCheck(queryBuilder, JIRA_QUERY_PARAM_NAME_ISSUE_TYPE, BlackDuckJiraConstants.BLACKDUCK_SECURITY_POLICY_VIOLATION_ISSUE);
        queryBuilder.append(JIRA_QUERY_DISJUNCTION);
        appendEqualityCheck(queryBuilder, JIRA_QUERY_PARAM_NAME_ISSUE_TYPE, BlackDuckJiraConstants.BLACKDUCK_VULNERABILITY_ISSUE);
        queryBuilder.append(") ");
        queryBuilder.append(JIRA_QUERY_CONJUNCTION);
        appendEqualityCheck(queryBuilder, JIRA_QUERY_PARAM_NAME_ISSUE_STATUS, "Open");
        // Query from least recently updated to most
        queryBuilder.append(" ORDER BY updated ASC");

        final String queryString = queryBuilder.toString();
        try {
            final JqlQueryParser queryParser = new DefaultJqlQueryParser();
            final Query orphanQuery = queryParser.parseQuery(queryString);
            return Optional.of(orphanQuery);
        } catch (final JqlParseException e) {
            logger.warn("The query generated to search for orphan issues was invalid: " + queryString);
        }
        return Optional.empty();
    }

    private void appendEqualityCheck(final StringBuilder queryBuilder, final String key, final String value) {
        queryBuilder.append(key);
        queryBuilder.append(" = ");
        queryBuilder.append(enquote(value));
    }

    private String enquote(final String text) {
        return "\"" + text + "\"";
    }

}
