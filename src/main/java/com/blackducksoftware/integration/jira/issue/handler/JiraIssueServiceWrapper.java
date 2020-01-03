/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2020 Synopsys, Inc.
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
package com.blackducksoftware.integration.jira.issue.handler;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.IssueService.IssueResult;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.issue.watchers.WatcherManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.query.Query;
import com.blackducksoftware.integration.jira.common.JiraUserContext;
import com.blackducksoftware.integration.jira.common.exception.JiraIssueException;
import com.blackducksoftware.integration.jira.common.model.PluginField;
import com.blackducksoftware.integration.jira.issue.model.BlackDuckIssueFieldTemplate;
import com.blackducksoftware.integration.jira.issue.model.BlackDuckIssueModel;
import com.blackducksoftware.integration.jira.issue.model.JiraIssueFieldTemplate;
import com.blackducksoftware.integration.jira.web.JiraServices;

public class JiraIssueServiceWrapper {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final IssueService jiraIssueService;
    private final IssueManager jiraIssueManager;
    private final CommentManager commentManager;
    private final WorkflowManager workflowManager;
    private final WatcherManager watcherManager;
    private final SearchService jiraSearchService;
    private final JiraIssuePropertyWrapper issuePropertyWrapper;
    private final ApplicationUser jiraAdminUser;
    private final Map<PluginField, CustomField> customFieldsMap;
    private IssueEditor issueEditor;

    public JiraIssueServiceWrapper(final IssueService jiraIssueService, final IssueManager jiraIssueManager, final CommentManager commentManager, final WorkflowManager workflowManager,
        final SearchService searchService, final JiraIssuePropertyWrapper issuePropertyWrapper, final IssueFieldCopyMappingHandler issueFieldCopyHandler, final ApplicationUser jiraAdminUser,
        final Map<PluginField, CustomField> customFieldsMap, final WatcherManager watcherManager) {
        this.jiraIssueService = jiraIssueService;
        this.jiraIssueManager = jiraIssueManager;
        this.commentManager = commentManager;
        this.workflowManager = workflowManager;
        this.jiraSearchService = searchService;
        this.issuePropertyWrapper = issuePropertyWrapper;
        this.jiraAdminUser = jiraAdminUser;
        this.customFieldsMap = customFieldsMap;
        this.watcherManager = watcherManager;
        this.issueEditor = new IssueEditor(jiraIssueService, issueFieldCopyHandler, commentManager);
    }

    // @formatter:off
    public static JiraIssueServiceWrapper createIssueServiceWrapperFromJiraServices(final JiraServices jiraServices, final JiraUserContext jiraUserContext, final Map<PluginField, CustomField> customFieldsMap) {
        return new JiraIssueServiceWrapper(
                 jiraServices.getIssueService()
                ,jiraServices.getIssueManager()
                ,jiraServices.getCommentManager()
                ,jiraServices.getWorkflowManager()
                ,jiraServices.getSearchService()
                ,jiraServices.createIssuePropertyWrapper()
                ,new IssueFieldCopyMappingHandler(jiraServices, jiraUserContext, customFieldsMap)
                ,jiraUserContext.getJiraAdminUser()
                ,customFieldsMap
                ,jiraServices.getWatcherManager());
    }
    // @formatter:on

    public List<Issue> queryForIssues(final ApplicationUser searchUser, final Query jqlQuery, final int startingOffset, final int resultLimit) throws JiraIssueException {
        final PagerFilter queryPageLimiter = PagerFilter.newPageAlignedFilter(startingOffset, resultLimit);
        try {
            final SearchResults searchResults = jiraSearchService.search(searchUser, jqlQuery, queryPageLimiter);
            return searchResults.getIssues();
        } catch (final SearchException e) {
            throw new JiraIssueException("Error executing query: " + jqlQuery.getQueryString() + " | Error Message: " + e.getMessage(), "queryForIssues");
        }
    }

    public Issue getIssue(final Long issueId) throws JiraIssueException {
        final IssueResult result = jiraIssueService.getIssue(jiraAdminUser, issueId);
        if (result.isValid()) {
            return result.getIssue();
        }
        throw new JiraIssueException("getIssue", result.getErrorCollection());
    }

    public Issue createIssue(final BlackDuckIssueModel blackDuckIssueModel) throws JiraIssueException {
        logger.debug("Create issue: " + blackDuckIssueModel);
        final IssueInputParameters issueInputParameters = createPopulatedIssueInputParameters(blackDuckIssueModel);

        logger.debug("issueInputParameters.getAssigneeId(): " + issueInputParameters.getAssigneeId());
        logger.debug("issueInputParameters.applyDefaultValuesWhenParameterNotProvided(): " + issueInputParameters.applyDefaultValuesWhenParameterNotProvided());
        logger.debug("issueInputParameters.retainExistingValuesWhenParameterNotProvided(): " + issueInputParameters.retainExistingValuesWhenParameterNotProvided());

        final JiraIssueFieldTemplate jiraIssueFieldTemplate = blackDuckIssueModel.getJiraIssueFieldTemplate();
        final ApplicationUser issueCreator = jiraIssueFieldTemplate.getIssueCreator();
        final Issue issue = issueEditor.createIssue(issueCreator, issueInputParameters);
        final Map<Long, String> blackDuckFieldMappings = blackDuckIssueModel.getBlackDuckIssueTemplate().createBlackDuckFieldMappings(customFieldsMap);
        issueEditor.addLabel(issue, blackDuckIssueModel.getProjectFieldCopyMappings(), issueInputParameters, blackDuckFieldMappings);

        return issue;
    }

    public Issue updateIssue(final BlackDuckIssueModel blackDuckIssueModel) throws JiraIssueException {
        final Long jiraIssueId = blackDuckIssueModel.getJiraIssueId();
        logger.debug("Update issue (id: " + jiraIssueId + "): " + blackDuckIssueModel);
        final IssueInputParameters issueInputParameters = createPopulatedIssueInputParameters(blackDuckIssueModel);
        final Issue issue = issueEditor.editIssue(jiraIssueId, blackDuckIssueModel.getJiraIssueFieldTemplate().getIssueCreator(), issueInputParameters);
        final Map<Long, String> blackDuckFieldMappings = blackDuckIssueModel.getBlackDuckIssueTemplate().createBlackDuckFieldMappings(customFieldsMap);
        issueEditor.addLabel(issue, blackDuckIssueModel.getProjectFieldCopyMappings(), issueInputParameters, blackDuckFieldMappings);
        return issue;
    }

    public Issue updateCustomField(final Issue issue, final ApplicationUser updater, final CustomField customField, final Object newFieldValue) throws JiraIssueException {
        try {
            final MutableIssue mutableIssue = jiraIssueManager.getIssueByCurrentKey(issue.getKey());
            mutableIssue.setCustomFieldValue(customField, newFieldValue);

            return jiraIssueManager.updateIssue(updater, mutableIssue, EventDispatchOption.DO_NOT_DISPATCH, false);
        } catch (final Exception e) {
            throw new JiraIssueException("Problem updating issue: " + e.getMessage(), "updateCustomField");
        }
    }

    public Issue transitionIssue(final Issue existingIssue, final int transitionActionId) throws JiraIssueException {
        logger.debug("Transition issue (" + existingIssue.getKey() + "): " + transitionActionId);
        final IssueInputParameters issueInputParameters = jiraIssueService.newIssueInputParameters();
        issueInputParameters.setRetainExistingValuesWhenParameterNotProvided(true);
        return issueEditor.transitionIssue(existingIssue.getId(), existingIssue.getCreator(), transitionActionId, issueInputParameters);
    }

    public void addComment(final Issue issue, final String comment) {
        final boolean dispatchCommentEvent = false;
        commentManager.create(issue, issue.getCreator(), comment, dispatchCommentEvent);
    }

    public JiraIssuePropertyWrapper getIssuePropertyWrapper() {
        return issuePropertyWrapper;
    }

    public ApplicationUser getJiraAdminUser() {
        return jiraAdminUser;
    }

    public JiraWorkflow getWorkflow(final Issue issue) {
        return workflowManager.getWorkflow(issue);
    }

    public void addWatcher(final Issue issue, final ApplicationUser watcher) {
        if (!watcherManager.isWatching(watcher, issue)) {
            watcherManager.startWatching(watcher, issue);
        }
    }

    private IssueInputParameters createPopulatedIssueInputParameters(final BlackDuckIssueModel blackDuckIssueModel) {
        final IssueInputParameters issueInputParameters = jiraIssueService.newIssueInputParameters();
        populateIssueInputParameters(issueInputParameters, blackDuckIssueModel.getJiraIssueFieldTemplate());
        populateIssueInputParameters(issueInputParameters, blackDuckIssueModel.getBlackDuckIssueTemplate());

        return issueInputParameters;
    }

    private void populateIssueInputParameters(final IssueInputParameters issueInputParameters, final JiraIssueFieldTemplate jiraIssueFieldTemplate) {
        issueInputParameters.setProjectId(jiraIssueFieldTemplate.getProjectId()).setIssueTypeId(jiraIssueFieldTemplate.getIssueTypeId());
        if (jiraIssueFieldTemplate.getSummary() != null) {
            issueInputParameters.setSummary(jiraIssueFieldTemplate.getSummary());
        }
        if (jiraIssueFieldTemplate.getIssueCreator() != null) {
            final String reporterId = jiraIssueFieldTemplate.getIssueCreator().getUsername();
            issueInputParameters.setReporterId(reporterId);
        }
        if (jiraIssueFieldTemplate.getIssueDescription() != null) {
            issueInputParameters.setDescription(jiraIssueFieldTemplate.getIssueDescription());
        }
        if (jiraIssueFieldTemplate.getAssigneeId() != null) {
            issueInputParameters.setAssigneeId(jiraIssueFieldTemplate.getAssigneeId());
        }

        issueInputParameters.setRetainExistingValuesWhenParameterNotProvided(jiraIssueFieldTemplate.shouldRetainExistingValuesWhenParameterNotProvided());
        issueInputParameters.setApplyDefaultValuesWhenParameterNotProvided(jiraIssueFieldTemplate.shouldApplyDefaultValuesWhenParameterNotProvided());
    }

    private void populateIssueInputParameters(final IssueInputParameters issueInputParameters, final BlackDuckIssueFieldTemplate blackDuckIssueFieldTemplate) {
        final Map<Long, String> blackDuckFieldMap = blackDuckIssueFieldTemplate.createBlackDuckFieldMappings(customFieldsMap);
        for (final Entry<Long, String> blackDuckFieldEntry : blackDuckFieldMap.entrySet()) {
            final String fieldValue = blackDuckFieldEntry.getValue();
            if (fieldValue != null) {
                issueInputParameters.addCustomFieldValue(blackDuckFieldEntry.getKey(), blackDuckFieldEntry.getValue());
            }
        }
    }

}
