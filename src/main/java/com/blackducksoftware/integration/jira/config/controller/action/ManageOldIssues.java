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
package com.blackducksoftware.integration.jira.config.controller.action;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.properties.IssuePropertyService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.jql.parser.DefaultJqlQueryParser;
import com.atlassian.jira.jql.parser.JqlParseException;
import com.atlassian.jira.jql.parser.JqlQueryParser;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.query.Query;
import com.atlassian.sal.api.user.UserKey;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.exception.JiraIssueException;
import com.blackducksoftware.integration.jira.config.JiraServices;
import com.opensymphony.workflow.loader.ActionDescriptor;

public class ManageOldIssues {
    private static final String JIRA_QUERY_PARAM_NAME_ISSUE_STATUS = "status";
    private static final String JIRA_QUERY_PARAM_NAME_ISSUE_TYPE = "issuetype";
    private static final String JIRA_QUERY_CONJUNCTION = " AND ";
    private static final String JIRA_QUERY_DISJUNCTION = " OR ";
    private final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));
    private final JiraServices jiraServices;
    private final UserManager userManager;

    public ManageOldIssues(final JiraServices jiraServices, final UserManager userManager) {
        this.jiraServices = jiraServices;
        this.userManager = userManager;
    }

    public void closeAllIssues(final UserKey userKey, final String oldUrl) throws JiraIssueException {
        int offset = 0;
        final int resultLimit = 1000;

        final ApplicationUser adminUser = userManager.getUserByKey(userKey.getStringValue());
        List<Issue> issues = retrievePagedOldIssues(adminUser, offset, resultLimit);

        while (issues.size() > 0) {
            for (final Issue issue : issues) {
                if (shouldIssueBeTransitioned(issue.getId(), adminUser, oldUrl)) {
                    transitionIssue(issue);
                }
            }

            offset += resultLimit;
            issues = retrievePagedOldIssues(adminUser, offset, resultLimit);
        }
    }

    private List<Issue> retrievePagedOldIssues(final ApplicationUser adminUser, final int startingOffset, final int resultLimit) throws JiraIssueException {
        final PagerFilter queryPageLimiter = PagerFilter.newPageAlignedFilter(startingOffset, resultLimit);
        final Query jqlQuery = createIssueQuery().orElseThrow(() -> new JiraIssueException("The generated Issues search query was invalid.", "retrievePagedOldIssues"));

        try {
            final SearchService jiraSearchService = jiraServices.getSearchService();
            final SearchResults searchResults = jiraSearchService.search(adminUser, jqlQuery, queryPageLimiter);
            return searchResults.getIssues();
        } catch (final SearchException e) {
            throw new JiraIssueException("Error executing query: " + jqlQuery.getQueryString() + " | Error Message: " + e.getMessage(), "retrievePagedOldIssues");
        }
    }

    private Optional<Query> createIssueQuery() {
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
        queryBuilder.append(createComparisonCheck(key, value, "="));
    }

    private String createComparisonCheck(final String key, final String value, final String comparison) {
        final StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append(key);
        queryBuilder.append(" ");
        queryBuilder.append(comparison);
        queryBuilder.append(" \"");
        queryBuilder.append(value);
        queryBuilder.append("\"");

        return queryBuilder.toString();
    }

    private boolean shouldIssueBeTransitioned(final Long issueId, final ApplicationUser user, final String oldUrl) {
        final Map<String, String> issueProperties = getIssueProperties(issueId, user);
        return issueProperties
                   .keySet()
                   .stream()
                   .anyMatch(key -> key.startsWith(oldUrl));
    }

    private void transitionIssue(final Issue issue) throws JiraIssueException {
        final IssueService issueService = jiraServices.getIssueService();
        final IssueInputParameters issueInputParameters = issueService.newIssueInputParameters();
        issueInputParameters.setRetainExistingValuesWhenParameterNotProvided(true);

        final IssueService.TransitionValidationResult validationResult = issueService.validateTransition(issue.getCreator(), issue.getId(), getTransitionId(issue), issueInputParameters);
        if (validationResult.isValid()) {
            final IssueService.IssueResult result = issueService.transition(issue.getCreator(), validationResult);
            final ErrorCollection errors = result.getErrorCollection();
            if (errors.hasAnyErrors()) {
                throw new JiraIssueException("transitionIssue", errors);
            }
        } else {
            throw new JiraIssueException("transitionIssue", validationResult.getErrorCollection());
        }
    }

    private int getTransitionId(final Issue issue) throws JiraIssueException {
        final WorkflowManager workflowManager = jiraServices.getWorkflowManager();
        final JiraWorkflow workflow = workflowManager.getWorkflow(issue);
        final Status status = issue.getStatus();
        final List<ActionDescriptor> actions = workflow.getLinkedStep(status).getActions();

        for (final ActionDescriptor descriptor : actions) {
            if (descriptor.getName() != null && descriptor.getName().equals(BlackDuckJiraConstants.BLACKDUCK_WORKFLOW_TRANSITION_REMOVE_OR_OVERRIDE)) {
                return descriptor.getId();
            }
        }
        throw new JiraIssueException("Unable to find the expected transition for workflow.", "getTransitionId");
    }

    public Map<String, String> getIssueProperties(final Long issueId, final ApplicationUser user) {
        final IssuePropertyService issuePropertyService = jiraServices.getPropertyService();
        return issuePropertyService.getProperties(user, issueId)
                   .stream()
                   .collect(Collectors.toMap(EntityProperty::getKey, EntityProperty::getValue));
    }
}
