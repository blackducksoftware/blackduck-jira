/**
 * Black Duck JIRA Plugin
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
package com.blackducksoftware.integration.jira.task.issue.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.IssueService.AssignValidationResult;
import com.atlassian.jira.bc.issue.IssueService.CreateValidationResult;
import com.atlassian.jira.bc.issue.IssueService.IssueResult;
import com.atlassian.jira.bc.issue.IssueService.TransitionValidationResult;
import com.atlassian.jira.bc.issue.IssueService.UpdateValidationResult;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.UpdateIssueRequest;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.JiraUserContext;
import com.blackducksoftware.integration.jira.common.exception.JiraIssueException;
import com.blackducksoftware.integration.jira.common.model.PluginField;
import com.blackducksoftware.integration.jira.config.JiraServices;
import com.blackducksoftware.integration.jira.task.conversion.output.IssueProperties;
import com.blackducksoftware.integration.jira.task.issue.model.BlackDuckIssueFieldTemplate;
import com.blackducksoftware.integration.jira.task.issue.model.BlackDuckIssueModel;
import com.blackducksoftware.integration.jira.task.issue.model.JiraIssueFieldTemplate;
import com.google.gson.Gson;

public class JiraIssueServiceWrapper {
    private final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final IssueService jiraIssueService;
    private final IssueManager jiraIssueManager;
    private final CommentManager commentManager;
    private final WorkflowManager workflowManager;
    private final JiraIssuePropertyWrapper issuePropertyWrapper;
    private final IssueFieldCopyMappingHandler issueFieldCopyHandler;
    private final JiraUserContext jiraUserContext;
    private final Gson gson;
    private final Map<PluginField, CustomField> customFieldsMap;

    public JiraIssueServiceWrapper(final IssueService jiraIssueService, final IssueManager jiraIssueManager, final CommentManager commentManager, final WorkflowManager workflowManager, final JiraIssuePropertyWrapper issuePropertyWrapper,
        final IssueFieldCopyMappingHandler issueFieldCopyHandler, final JiraUserContext jiraUserContext, final Map<PluginField, CustomField> customFieldsMap, final Gson gson) {
        this.jiraIssueService = jiraIssueService;
        this.jiraIssueManager = jiraIssueManager;
        this.commentManager = commentManager;
        this.workflowManager = workflowManager;
        this.issuePropertyWrapper = issuePropertyWrapper;
        this.issueFieldCopyHandler = issueFieldCopyHandler;
        this.jiraUserContext = jiraUserContext;
        this.gson = gson;
        this.customFieldsMap = customFieldsMap;
    }

    // @formatter:off
    public static JiraIssueServiceWrapper createIssueServiceWrapperFromJiraServices(final JiraServices jiraServices, final JiraUserContext jiraUserContext, final Gson gson, final Map<PluginField, CustomField> customFieldsMap) {
        return new JiraIssueServiceWrapper(
                 jiraServices.getIssueService()
                ,jiraServices.getIssueManager()
                ,jiraServices.getCommentManager()
                ,jiraServices.getWorkflowManager()
                ,jiraServices.createIssuePropertyWrapper()
                ,new IssueFieldCopyMappingHandler(jiraServices, jiraUserContext, customFieldsMap)
                ,jiraUserContext
                ,customFieldsMap
                ,gson
                );
    }
    // @formatter:on

    public Issue getIssue(final Long issueId) throws JiraIssueException {
        final IssueResult result = jiraIssueService.getIssue(jiraUserContext.getJiraIssueCreatorUser(), issueId);
        if (result.isValid()) {
            return result.getIssue();
        }
        throw new JiraIssueException("getIssue", result.getErrorCollection());
    }

    public Issue findIssueByContentKey(final String notificationUniqueKey) throws JiraIssueException {
        logger.debug("Find issue: " + notificationUniqueKey);
        final EntityProperty property = issuePropertyWrapper.findProperty(notificationUniqueKey);
        if (property != null) {
            final IssueProperties propertyValue = createIssuePropertiesFromJson(property.getValue());
            logger.debug("findIssueByContentKey(): propertyValue (converted from JSON): " + propertyValue);
            return getIssue(propertyValue.getJiraIssueId());
        }
        return null;
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
            jiraIssueFieldTemplate.getJiraProjectName(), jiraIssueFieldTemplate.getJiraProjectId());

        final CreateValidationResult validationResult = jiraIssueService.validateCreate(jiraUserContext.getJiraIssueCreatorUser(), issueInputParameters);
        if (validationResult.isValid()) {
            final IssueResult result = jiraIssueService.create(jiraUserContext.getJiraIssueCreatorUser(), validationResult);
            final ErrorCollection errors = result.getErrorCollection();
            if (!errors.hasAnyErrors()) {
                final MutableIssue jiraIssue = result.getIssue();
                issueFieldCopyHandler.addLabels(jiraIssue.getId(), labels);
                // TODO Fixing the issue assignment should be separate from creating the issue (if an exception is thrown, the issue will be missing pieces).
                fixIssueAssignment(jiraIssue, blackDuckIssueModel.getJiraIssueFieldTemplate().getAssigneeId());
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
            jiraIssueFieldTemplate.getJiraProjectName(), jiraIssueFieldTemplate.getJiraProjectId());
        final UpdateValidationResult validationResult = jiraIssueService.validateUpdate(jiraUserContext.getJiraIssueCreatorUser(), jiraIssueId, issueInputParameters);
        if (validationResult.isValid()) {
            final boolean sendMail = false;
            final IssueResult result = jiraIssueService.update(jiraUserContext.getJiraIssueCreatorUser(), validationResult, EventDispatchOption.ISSUE_UPDATED, sendMail);
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

    public Issue transitionIssue(final Issue existingIssue, final int transitionActionId) throws JiraIssueException {
        logger.debug("Transition issue (" + existingIssue.getKey() + "): " + transitionActionId);
        final IssueInputParameters issueInputParameters = jiraIssueService.newIssueInputParameters();
        issueInputParameters.setRetainExistingValuesWhenParameterNotProvided(true);

        logger.debug("Previous issue status: " + existingIssue.getStatus().getName());
        final TransitionValidationResult validationResult = jiraIssueService.validateTransition(jiraUserContext.getJiraIssueCreatorUser(), existingIssue.getId(), transitionActionId, issueInputParameters);
        if (validationResult.isValid()) {
            final IssueResult result = jiraIssueService.transition(jiraUserContext.getJiraIssueCreatorUser(), validationResult);
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
        commentManager.create(issue, jiraUserContext.getJiraIssueCreatorUser(), comment, dispatchCommentEvent);
    }

    public String getIssueProperty(final Long issueId, final String propertyName) {
        return issuePropertyWrapper.getIssueProperty(issueId, jiraUserContext.getJiraIssueCreatorUser(), propertyName);
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
        issuePropertyWrapper.addIssuePropertyJson(issueId, jiraUserContext.getJiraIssueCreatorUser(), key, jsonValue);
    }

    public void addProjectProperty(final Long issueId, final String key, final Object value) throws JiraIssueException {
        String jsonValue = "";
        if (null != value) {
            jsonValue = gson.toJson(value);
        }
        issuePropertyWrapper.addProjectPropertyJson(issueId, jiraUserContext.getJiraIssueCreatorUser(), key, jsonValue);
    }

    public JiraWorkflow getWorkflow(final Long issueId) {
        final Issue issue;
        try {
            issue = getIssue(issueId);
            return getWorkflow(issue);
        } catch (JiraIssueException e) {
            return null;
        }
    }

    public JiraWorkflow getWorkflow(final Issue issue) {
        return workflowManager.getWorkflow(issue);
    }

    private void fixIssueAssignment(final MutableIssue mutableIssue, final String assigneeId) throws JiraIssueException {
        if (mutableIssue.getAssignee() == null) {
            logger.debug("Created issue " + mutableIssue.getKey() + "; Assignee: null");
        } else {
            logger.debug("Created issue " + mutableIssue.getKey() + "; Assignee: " + mutableIssue.getAssignee().getName());
        }
        if (assigneeId == null && mutableIssue.getAssigneeId() != null) {
            logger.debug("Issue needs to be Unassigned");
            assignIssue(mutableIssue, assigneeId);
        } else if (assigneeId != null && !mutableIssue.getAssigneeId().equals(assigneeId)) {
            throw new JiraIssueException("Issue assignment failed", "fixIssueAssignment");
        } else {
            logger.debug("Issue assignment is correct");
        }
    }

    private void assignIssue(final MutableIssue issue, final String assigneeId) throws JiraIssueException {
        final ApplicationUser issueCreator = jiraUserContext.getJiraIssueCreatorUser();
        final AssignValidationResult assignValidationResult = jiraIssueService.validateAssign(jiraUserContext.getJiraIssueCreatorUser(), issue.getId(), assigneeId);
        final ErrorCollection errors = assignValidationResult.getErrorCollection();
        if (assignValidationResult.isValid() && !errors.hasAnyErrors()) {
            logger.debug("Assigning issue to user ID: " + assigneeId);
            jiraIssueService.assign(issueCreator, assignValidationResult);

            // Dispatch event to sync the new assignee with Black Duck server
            issue.setAssigneeId(assigneeId);
            dispatchEvent(issue, EventDispatchOption.ISSUE_UPDATED, false);
        } else {
            final StringBuilder errorMessageBuilder = new StringBuilder("Unable to assign issue ");
            errorMessageBuilder.append(issue.getKey());
            errorMessageBuilder.append(": ");
            for (final String errorMsg : errors.getErrorMessages()) {
                errorMessageBuilder.append(errorMsg);
                errorMessageBuilder.append("; ");
            }
            throw new JiraIssueException(errorMessageBuilder.toString(), "assignIssue");
        }
    }

    private void dispatchEvent(final MutableIssue modifiedIssue, final EventDispatchOption option, final boolean sendMail) {
        final UpdateIssueRequest issueUpdate = UpdateIssueRequest.builder().eventDispatchOption(option).sendMail(sendMail).build();
        jiraIssueManager.updateIssue(jiraUserContext.getJiraIssueCreatorUser(), modifiedIssue, issueUpdate);
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
        issueInputParameters.setProjectId(jiraIssueFieldTemplate.getJiraProjectId()).setIssueTypeId(jiraIssueFieldTemplate.getJiraIssueTypeId());
        if (jiraIssueFieldTemplate.getSummary() != null) {
            issueInputParameters.setSummary(jiraIssueFieldTemplate.getSummary());
        }
        if (jiraIssueFieldTemplate.getIssueCreatorUsername() != null) {
            issueInputParameters.setReporterId(jiraIssueFieldTemplate.getIssueCreatorUsername());
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
