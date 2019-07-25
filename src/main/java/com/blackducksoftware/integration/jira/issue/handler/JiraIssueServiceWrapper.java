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
package com.blackducksoftware.integration.jira.issue.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.IssueService.CreateValidationResult;
import com.atlassian.jira.bc.issue.IssueService.IssueResult;
import com.atlassian.jira.bc.issue.IssueService.TransitionValidationResult;
import com.atlassian.jira.bc.issue.IssueService.UpdateValidationResult;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.entity.property.EntityProperty;
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
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.query.Query;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.JiraUserContext;
import com.blackducksoftware.integration.jira.common.exception.JiraIssueException;
import com.blackducksoftware.integration.jira.common.model.PluginField;
import com.blackducksoftware.integration.jira.issue.conversion.output.IssueProperties;
import com.blackducksoftware.integration.jira.issue.model.BlackDuckIssueFieldTemplate;
import com.blackducksoftware.integration.jira.issue.model.BlackDuckIssueModel;
import com.blackducksoftware.integration.jira.issue.model.JiraIssueFieldTemplate;
import com.blackducksoftware.integration.jira.web.JiraServices;
import com.google.gson.Gson;

public class JiraIssueServiceWrapper {
    private final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final IssueService jiraIssueService;
    private final IssueManager jiraIssueManager;
    private final CommentManager commentManager;
    private final WorkflowManager workflowManager;
    private final WatcherManager watcherManager;
    private final SearchService jiraSearchService;
    private final JiraIssuePropertyWrapper issuePropertyWrapper;
    private final IssueFieldCopyMappingHandler issueFieldCopyHandler;
    private final ApplicationUser jiraAdminUser;
    private final Map<PluginField, CustomField> customFieldsMap;
    private final Gson gson;

    public JiraIssueServiceWrapper(final IssueService jiraIssueService, final IssueManager jiraIssueManager, final CommentManager commentManager, final WorkflowManager workflowManager,
        final SearchService searchService, final JiraIssuePropertyWrapper issuePropertyWrapper,
        final IssueFieldCopyMappingHandler issueFieldCopyHandler, final ApplicationUser jiraAdminUser, final Map<PluginField, CustomField> customFieldsMap, final Gson gson, final WatcherManager watcherManager) {
        this.jiraIssueService = jiraIssueService;
        this.jiraIssueManager = jiraIssueManager;
        this.commentManager = commentManager;
        this.workflowManager = workflowManager;
        this.jiraSearchService = searchService;
        this.issuePropertyWrapper = issuePropertyWrapper;
        this.issueFieldCopyHandler = issueFieldCopyHandler;
        this.jiraAdminUser = jiraAdminUser;
        this.customFieldsMap = customFieldsMap;
        this.gson = gson;
        this.watcherManager = watcherManager;
    }

    // @formatter:off
    public static JiraIssueServiceWrapper createIssueServiceWrapperFromJiraServices(final JiraServices jiraServices, final JiraUserContext jiraUserContext, final Gson gson, final Map<PluginField, CustomField> customFieldsMap) {
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
                ,gson
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

        final Map<Long, String> blackDuckFieldMappings = blackDuckIssueModel.getBlackDuckIssueTemplate().createBlackDuckFieldMappings(customFieldsMap);
        final JiraIssueFieldTemplate jiraIssueFieldTemplate = blackDuckIssueModel.getJiraIssueFieldTemplate();
        final List<String> labels = issueFieldCopyHandler.setFieldCopyMappings(issueInputParameters, blackDuckIssueModel.getProjectFieldCopyMappings(), blackDuckFieldMappings,
            jiraIssueFieldTemplate.getProjectName(), jiraIssueFieldTemplate.getProjectId());

        final CreateValidationResult validationResult = jiraIssueService.validateCreate(jiraIssueFieldTemplate.getIssueCreator(), issueInputParameters);

        if (validationResult.isValid()) {
            final IssueResult result = jiraIssueService.create(jiraIssueFieldTemplate.getIssueCreator(), validationResult);
            final ErrorCollection errors = result.getErrorCollection();
            if (!errors.hasAnyErrors()) {
                final MutableIssue jiraIssue = result.getIssue();
                issueFieldCopyHandler.addLabels(jiraIssue.getId(), labels);
                return jiraIssue;
            }
            throw new JiraIssueException("createIssue", errors);
        }
        throw new JiraIssueException("createIssue", validationResult.getErrorCollection());
    }

    public Issue updateIssue(final BlackDuckIssueModel blackDuckIssueModel) throws JiraIssueException {
        final Long jiraIssueId = blackDuckIssueModel.getJiraIssueId();
        logger.debug("Update issue (id: " + jiraIssueId + "): " + blackDuckIssueModel);
        final IssueInputParameters issueInputParameters = createPopulatedIssueInputParameters(blackDuckIssueModel);

        final Map<Long, String> blackDuckFieldMappings = blackDuckIssueModel.getBlackDuckIssueTemplate().createBlackDuckFieldMappings(customFieldsMap);
        final JiraIssueFieldTemplate jiraIssueFieldTemplate = blackDuckIssueModel.getJiraIssueFieldTemplate();
        final List<String> labels = issueFieldCopyHandler.setFieldCopyMappings(issueInputParameters, blackDuckIssueModel.getProjectFieldCopyMappings(), blackDuckFieldMappings,
            jiraIssueFieldTemplate.getProjectName(), jiraIssueFieldTemplate.getProjectId());
        final UpdateValidationResult validationResult = jiraIssueService.validateUpdate(jiraIssueFieldTemplate.getIssueCreator(), jiraIssueId, issueInputParameters);
        if (validationResult.isValid()) {
            final boolean sendMail = false;
            final IssueResult result = jiraIssueService.update(jiraIssueFieldTemplate.getIssueCreator(), validationResult, EventDispatchOption.ISSUE_UPDATED, sendMail);
            final ErrorCollection errors = result.getErrorCollection();
            if (!errors.hasAnyErrors()) {
                final MutableIssue jiraIssue = result.getIssue();
                issueFieldCopyHandler.addLabels(jiraIssue.getId(), labels);
                return jiraIssue;
            }
            throw new JiraIssueException("updateIssue", errors);
        }
        throw new JiraIssueException("updateIssue", validationResult.getErrorCollection());
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

        logger.debug("Previous issue status: " + existingIssue.getStatus().getName());
        final TransitionValidationResult validationResult = jiraIssueService.validateTransition(existingIssue.getCreator(), existingIssue.getId(), transitionActionId, issueInputParameters);
        if (validationResult.isValid()) {
            final IssueResult result = jiraIssueService.transition(existingIssue.getCreator(), validationResult);
            final ErrorCollection errors = result.getErrorCollection();
            if (!errors.hasAnyErrors()) {
                final Issue jiraIssue = result.getIssue();
                logger.debug("New issue status: " + jiraIssue.getStatus().getName());
                return jiraIssue;
            }
            throw new JiraIssueException("transitionIssue", errors);
        }
        throw new JiraIssueException("transitionIssue", validationResult.getErrorCollection());
    }

    public void addComment(final Issue issue, final String comment) {
        final boolean dispatchCommentEvent = false;
        commentManager.create(issue, issue.getCreator(), comment, dispatchCommentEvent);
    }

    public String getIssueProperty(final Long issueId, final String propertyName) {
        return issuePropertyWrapper.getIssueProperty(issueId, jiraAdminUser, propertyName);
    }

    public List<IssueProperties> findIssuePropertiesByBomComponentUri(final String bomComponentUri) throws JiraIssueException {
        logger.debug("Find issue by Bom Component URI: " + bomComponentUri);
        final List<IssueProperties> foundProperties = new ArrayList<>();

        final List<EntityProperty> properties = issuePropertyWrapper.findProperties(bomComponentUri);
        for (final EntityProperty property : properties) {
            final IssueProperties issueProperties = createIssuePropertiesFromJson(property.getValue());
            logger.debug("findIssuesByBomComponentUri(): propertyValue (converted from JSON): " + issueProperties);
            foundProperties.add(issueProperties);
        }
        return foundProperties;
    }

    public void addIssueProperties(final Long issueId, final String key, final IssueProperties propertiesObject) throws JiraIssueException {
        String jsonValue = "";
        if (null != propertiesObject) {
            jsonValue = gson.toJson(propertiesObject);
        }
        addIssuePropertyJson(issueId, key, jsonValue);
    }

    public void addIssuePropertyJson(final Long issueId, final String key, final String jsonValue) throws JiraIssueException {
        issuePropertyWrapper.addIssuePropertyJson(issueId, jiraAdminUser, key, jsonValue);
    }

    public void addProjectProperty(final Long issueId, final String key, final Object value) throws JiraIssueException {
        String jsonValue = "";
        if (null != value) {
            jsonValue = gson.toJson(value);
        }
        issuePropertyWrapper.addProjectPropertyJson(issueId, jiraAdminUser, key, jsonValue);
    }

    public JiraWorkflow getWorkflow(final Issue issue) {
        return workflowManager.getWorkflow(issue);
    }

    public void addWatcher(final Issue issue, final ApplicationUser watcher) {
        if (!watcherManager.isWatching(watcher, issue)) {
            watcherManager.startWatching(watcher, issue);
        }
    }

    private IssueProperties createIssuePropertiesFromJson(final String json) throws JiraIssueException {
        try {
            return gson.fromJson(json, IssueProperties.class);
        } catch (final Exception e) {
            throw new JiraIssueException("Could not deserialize issue properties.", "createIssuePropertiesFromJson");
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
