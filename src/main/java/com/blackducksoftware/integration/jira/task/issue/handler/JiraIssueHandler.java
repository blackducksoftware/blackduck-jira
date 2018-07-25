/**
 * Hub JIRA Plugin
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

import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.atlassian.jira.bc.issue.IssueService.AssignValidationResult;
import com.atlassian.jira.bc.issue.IssueService.CreateValidationResult;
import com.atlassian.jira.bc.issue.IssueService.IssueResult;
import com.atlassian.jira.bc.issue.IssueService.TransitionValidationResult;
import com.atlassian.jira.bc.issue.IssueService.UpdateValidationResult;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.entity.property.EntityPropertyQuery;
import com.atlassian.jira.entity.property.EntityPropertyService;
import com.atlassian.jira.entity.property.EntityPropertyService.PropertyResult;
import com.atlassian.jira.entity.property.EntityPropertyService.SetPropertyValidationResult;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueImpl;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.UpdateIssueRequest;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.common.JiraUserContext;
import com.blackducksoftware.integration.jira.common.TicketInfoFromSetup;
import com.blackducksoftware.integration.jira.config.JiraServices;
import com.blackducksoftware.integration.jira.config.JiraSettingsService;
import com.blackducksoftware.integration.jira.task.conversion.output.HubEventAction;
import com.blackducksoftware.integration.jira.task.conversion.output.HubIssueTrackerProperties;
import com.blackducksoftware.integration.jira.task.conversion.output.IssueProperties;
import com.blackducksoftware.integration.jira.task.conversion.output.IssuePropertiesGenerator;
import com.blackducksoftware.integration.jira.task.conversion.output.PolicyViolationIssueProperties;
import com.blackducksoftware.integration.jira.task.conversion.output.VulnerabilityIssueProperties;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventCategory;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opensymphony.workflow.loader.ActionDescriptor;

public class JiraIssueHandler {
    private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final JiraUserContext jiraContext;
    private final JiraServices jiraServices;
    private final JiraSettingsService jiraSettingsService;
    private final IssueFieldHandler issueFieldHandler;
    private final HubIssueTrackerHandler hubIssueTrackerHandler;
    private final HubIssueTrackerPropertyHandler hubIssueTrackerPropertyHandler;
    private final Date instanceUniqueDate;

    public JiraIssueHandler(final JiraServices jiraServices, final JiraUserContext jiraContext, final JiraSettingsService jiraSettingsService, final TicketInfoFromSetup ticketInfoFromSetup, final HubIssueTrackerHandler hubIssueTrackerHandler) {
        this.jiraServices = jiraServices;
        this.jiraContext = jiraContext;
        this.jiraSettingsService = jiraSettingsService;
        this.issueFieldHandler = new IssueFieldHandler(jiraServices, jiraSettingsService, jiraContext, ticketInfoFromSetup);
        this.hubIssueTrackerHandler = hubIssueTrackerHandler;
        this.hubIssueTrackerPropertyHandler = new HubIssueTrackerPropertyHandler();
        this.instanceUniqueDate = new Date();
    }

    private void addIssueProperty(final EventData eventData, final Long issueId, final String key, final IssueProperties value) {
        final Gson gson = new GsonBuilder().create();

        final String jsonValue = gson.toJson(value);
        addIssuePropertyJson(eventData, issueId, key, jsonValue);
    }

    private void handleErrorCollection(final String methodAttempt, final EventData eventData, final ErrorCollection errors) {
        if (errors.hasAnyErrors()) {
            logger.error("Error on: " + methodAttempt + " for eventData: " + eventData);
            for (final Entry<String, String> error : errors.getErrors().entrySet()) {
                final String errorMessage = error.getKey() + " / " + error.getValue();
                logger.error(errorMessage);
                jiraSettingsService.addHubError(errorMessage, eventData.getHubProjectName(), eventData.getHubProjectVersion(), eventData.getJiraProjectName(), eventData.getJiraAdminUsername(), eventData.getJiraIssueCreatorUsername(),
                        methodAttempt);
            }
            for (final String errorMessage : errors.getErrorMessages()) {
                logger.error(errorMessage);
                jiraSettingsService.addHubError(errorMessage, eventData.getHubProjectName(), eventData.getHubProjectVersion(), eventData.getJiraProjectName(), eventData.getJiraAdminUsername(), eventData.getJiraIssueCreatorUsername(),
                        methodAttempt);
            }
        }
    }

    private void addIssuePropertyJson(final EventData eventData, final Long issueId, final String key, final String jsonValue) {
        logger.debug("addIssuePropertyJson(): issueId: " + issueId + "; key: " + key + "; json: " + jsonValue);
        final EntityPropertyService.PropertyInput propertyInput = new EntityPropertyService.PropertyInput(jsonValue, key);

        final SetPropertyValidationResult validationResult = jiraServices.getPropertyService().validateSetProperty(jiraContext.getJiraIssueCreatorUser(), issueId, propertyInput);

        if (!validationResult.isValid()) {
            handleErrorCollection("addIssueProperty", eventData, validationResult.getErrorCollection());
        } else {
            final PropertyResult result = jiraServices.getPropertyService().setProperty(jiraContext.getJiraIssueCreatorUser(), validationResult);
            handleErrorCollection("addIssueProperty", eventData, result.getErrorCollection());
        }
    }

    private void addProjectPropertyJson(final EventData eventData, final Long issueId, final String key, final String jsonValue) {
        logger.debug("addIssuePropertyJson(): issueId: " + issueId + "; key: " + key + "; json: " + jsonValue);
        final EntityPropertyService.PropertyInput propertyInput = new EntityPropertyService.PropertyInput(jsonValue, key);

        final SetPropertyValidationResult validationResult = jiraServices.getProjectPropertyService().validateSetProperty(jiraContext.getJiraIssueCreatorUser(), issueId, propertyInput);

        if (!validationResult.isValid()) {
            handleErrorCollection("addIssueProperty", eventData, validationResult.getErrorCollection());
        } else {
            final PropertyResult result = jiraServices.getProjectPropertyService().setProperty(jiraContext.getJiraIssueCreatorUser(), validationResult);
            handleErrorCollection("addIssueProperty", eventData, result.getErrorCollection());
        }
    }

    private void addHubIssueUrlIssueProperty(final EventData eventData, final HubIssueTrackerProperties value, final Issue issue) {
        final Gson gson = new GsonBuilder().create();
        final String jsonValue = gson.toJson(value);
        final String key = hubIssueTrackerPropertyHandler.createEntityPropertyKey(issue);

        addProjectPropertyJson(eventData, issue.getProjectId(), key, jsonValue);
    }

    private String getNotificationUniqueKey(final EventData eventData) {
        final String notificationUniqueKey = eventData.getEventKey();
        return notificationUniqueKey;
    }

    private Issue findIssue(final EventData eventData) {
        logger.debug("findIssue(): eventData: " + eventData);

        final String notificationUniqueKey = getNotificationUniqueKey(eventData);

        if (notificationUniqueKey != null) {
            logger.debug("findIssue(): key: " + notificationUniqueKey);
            final EntityPropertyQuery<?> query = jiraServices.getJsonEntityPropertyManager().query();
            final EntityPropertyQuery.ExecutableQuery executableQuery = query.key(notificationUniqueKey);
            final List<EntityProperty> props = executableQuery.maxResults(1).find();
            if (props.size() == 0) {
                logger.debug("No property found with that key");
                return null;
            }
            final EntityProperty property = props.get(0);
            final IssueProperties propertyValue = createIssuePropertiesFromJson(eventData, property.getValue());
            logger.debug("findIssue(): propertyValue (converted from JSON): " + propertyValue);
            final IssueResult result = jiraServices.getIssueService().getIssue(jiraContext.getJiraIssueCreatorUser(), propertyValue.getJiraIssueId());

            if (!result.isValid()) {
                handleErrorCollection("findIssue", eventData, result.getErrorCollection());
            } else {
                return result.getIssue();
            }
        }
        return null;
    }

    private IssueProperties createIssuePropertiesFromJson(final EventData eventData, final String json) {
        final Gson gson = new GsonBuilder().create();

        final EventCategory eventDataCategory = EventCategory.fromNotificationType(eventData.getNotificationType());
        if (EventCategory.POLICY.equals(eventDataCategory)) {
            return gson.fromJson(json, PolicyViolationIssueProperties.class);
        }

        return gson.fromJson(json, VulnerabilityIssueProperties.class);
    }

    private Issue createIssue(final EventData eventData) {
        IssueInputParameters issueInputParameters = jiraServices.getIssueService().newIssueInputParameters();
        issueInputParameters.setProjectId(eventData.getJiraProjectId()).setIssueTypeId(eventData.getJiraIssueTypeId()).setSummary(eventData.getJiraIssueSummary()).setReporterId(eventData.getJiraIssueCreatorUsername())
                .setDescription(eventData.getJiraIssueDescription());

        issueInputParameters.setRetainExistingValuesWhenParameterNotProvided(true);
        issueInputParameters.setApplyDefaultValuesWhenParameterNotProvided(true);

        final String assigneeId = eventData.getJiraIssueAssigneeUserId();
        if (assigneeId != null) {
            logger.debug("notificaitonEvent: issueAssigneeId: " + assigneeId);
            issueInputParameters = issueInputParameters.setAssigneeId(assigneeId);
        } else {
            logger.debug("notificationEvent: issueAssigneeId is not set, which will result in an unassigned Issue (assuming JIRA is configured to allow unassigned issues)");
        }
        logger.debug("issueInputParameters.getAssigneeId(): " + issueInputParameters.getAssigneeId());
        logger.debug("issueInputParameters.applyDefaultValuesWhenParameterNotProvided(): " + issueInputParameters.applyDefaultValuesWhenParameterNotProvided());
        logger.debug("issueInputParameters.retainExistingValuesWhenParameterNotProvided(): " + issueInputParameters.retainExistingValuesWhenParameterNotProvided());

        issueFieldHandler.setPluginFieldValues(eventData, issueInputParameters);
        final List<String> labels = issueFieldHandler.setOtherFieldValues(eventData, issueInputParameters);

        final CreateValidationResult validationResult = jiraServices.getIssueService().validateCreate(jiraContext.getJiraIssueCreatorUser(), issueInputParameters);
        logger.debug("createIssue(): Project: " + eventData.getJiraProjectName() + ": " + eventData.getJiraIssueSummary());
        if (!validationResult.isValid()) {
            handleErrorCollection("createIssue", eventData, validationResult.getErrorCollection());
        } else {
            final IssueResult result = jiraServices.getIssueService().create(jiraContext.getJiraIssueCreatorUser(), validationResult);
            final ErrorCollection errors = result.getErrorCollection();
            if (errors.hasAnyErrors()) {
                handleErrorCollection("createIssue", eventData, errors);
            } else {
                fixIssueAssignment(eventData, result);
                issueFieldHandler.addLabels(result.getIssue(), labels);
                final Issue jiraIssue = result.getIssue();
                return jiraIssue;
            }
        }
        return null;
    }

    private Issue updateHubFieldsAndDescription(final Issue existingIssue, final EventData eventData) {
        final IssueInputParameters issueInputParameters = jiraServices.getIssueService().newIssueInputParameters();
        issueInputParameters.setDescription(eventData.getJiraIssueDescription()).setRetainExistingValuesWhenParameterNotProvided(true);

        issueFieldHandler.setPluginFieldValues(eventData, issueInputParameters);

        final UpdateValidationResult validationResult = jiraServices.getIssueService().validateUpdate(existingIssue.getCreator(), existingIssue.getId(), issueInputParameters);
        logger.debug("updateHubFieldsAndDescription(): Issue: " + existingIssue.getKey());
        if (!validationResult.isValid()) {
            handleErrorCollection("updateHubFieldsAndDescription", eventData, validationResult.getErrorCollection());
        } else {
            final IssueResult result = jiraServices.getIssueService().update(jiraContext.getJiraIssueCreatorUser(), validationResult);
            final ErrorCollection errors = result.getErrorCollection();
            if (errors.hasAnyErrors()) {
                handleErrorCollection("updateHubFieldsAndDescription", eventData, errors);
            } else {
                final Issue jiraIssue = result.getIssue();
                logger.debug("Updated Black Duck fields for issue: " + jiraIssue.getKey());
                return jiraIssue;
            }
        }
        return existingIssue;
    }

    private void fixIssueAssignment(final EventData eventData, final IssueResult result) {
        final MutableIssue issue = result.getIssue();
        if (issue.getAssignee() == null) {
            logger.debug("Created issue " + issue.getKey() + "; Assignee: null");
        } else {
            logger.debug("Created issue " + issue.getKey() + "; Assignee: " + issue.getAssignee().getName());
        }
        final String assigneeId = eventData.getJiraIssueAssigneeUserId();
        if ((assigneeId == null) && (issue.getAssigneeId() != null)) {
            logger.debug("Issue needs to be UNassigned");
            assignIssue(issue, eventData);
        } else if ((assigneeId != null) && (!issue.getAssigneeId().equals(assigneeId))) {
            final String errorMessage = "Issue assignment failed";
            logger.error(errorMessage);
            jiraSettingsService.addHubError(errorMessage, eventData.getHubProjectName(), eventData.getHubProjectVersion(), eventData.getJiraProjectName(), eventData.getJiraAdminUsername(), eventData.getJiraIssueCreatorUsername(),
                    "fixIssueAssignment");
        } else {
            logger.debug("Issue assignment is correct");
        }
    }

    private void assignIssue(final MutableIssue issue, final EventData eventData) {
        final ApplicationUser user = jiraContext.getJiraIssueCreatorUser();
        final String assigneeId = eventData.getJiraIssueAssigneeUserId();
        final AssignValidationResult assignValidationResult = jiraServices.getIssueService().validateAssign(user, issue.getId(), assigneeId);
        final ErrorCollection errors = assignValidationResult.getErrorCollection();
        if (assignValidationResult.isValid() && !errors.hasAnyErrors()) {
            logger.debug("Assigning issue to user ID: " + assigneeId);
            jiraServices.getIssueService().assign(user, assignValidationResult);
            updateIssue(issue, user, assigneeId);
        } else {
            final StringBuilder sb = new StringBuilder("Unable to assign issue ");
            sb.append(issue.getKey());
            sb.append(": ");
            for (final String errorMsg : errors.getErrorMessages()) {
                sb.append(errorMsg);
                sb.append("; ");
            }
            final String errorMessage = sb.toString();
            logger.error(errorMessage);
            jiraSettingsService.addHubError(errorMessage, eventData.getHubProjectName(), eventData.getHubProjectVersion(), eventData.getJiraProjectName(), eventData.getJiraAdminUsername(), eventData.getJiraIssueCreatorUsername(),
                    "assignIssue");
        }
    }

    private Issue updateIssue(final MutableIssue issueToUpdate, final ApplicationUser userMakingChange, final String assigneeId) {
        issueToUpdate.setAssigneeId(assigneeId);
        final UpdateIssueRequest issueUpdate = UpdateIssueRequest.builder().eventDispatchOption(EventDispatchOption.ISSUE_UPDATED).sendMail(false).build();
        logger.debug("Updating issue with assigned user ID: " + assigneeId);
        final Issue updatedIssue = jiraServices.getIssueManager().updateIssue(userMakingChange, issueToUpdate, issueUpdate);
        return updatedIssue;
    }

    private Issue transitionIssue(final EventData eventData, final Issue issueToTransition, final String stepName, final String newExpectedStatus, final ApplicationUser user) {
        final Status currentStatus = issueToTransition.getStatus();
        logger.debug("Current status : " + currentStatus.getName());

        if (currentStatus.getName().equals(newExpectedStatus)) {
            logger.debug("Will not tranisition issue, since it is already in the expected state.");
            return issueToTransition;
        }

        final JiraWorkflow workflow = jiraServices.getWorkflowManager().getWorkflow(issueToTransition);

        ActionDescriptor transitionAction = null;
        // https://answers.atlassian.com/questions/6985/how-do-i-change-status-of-issue
        final List<ActionDescriptor> actions = workflow.getLinkedStep(currentStatus).getActions();
        logger.debug("Found this many actions : " + actions.size());
        if (actions.size() == 0) {
            final String errorMessage = "Can not transition this issue : " + issueToTransition.getKey() + ", from status : " + currentStatus.getName() + ". There are no steps from this status to any other status.";
            logger.error(errorMessage);
            jiraSettingsService.addHubError(errorMessage, eventData.getHubProjectName(), eventData.getHubProjectVersion(), eventData.getJiraProjectName(), eventData.getJiraAdminUsername(), eventData.getJiraIssueCreatorUsername(),
                    "transitionIssue");
        }
        for (final ActionDescriptor descriptor : actions) {
            if (descriptor.getName() != null && descriptor.getName().equals(stepName)) {
                logger.debug("Found Step descriptor : " + descriptor.getName());
                transitionAction = descriptor;
                break;
            }
        }
        if (transitionAction != null) {
            final IssueInputParameters parameters = jiraServices.getIssueService().newIssueInputParameters();
            parameters.setRetainExistingValuesWhenParameterNotProvided(true);
            final TransitionValidationResult validationResult = jiraServices.getIssueService().validateTransition(jiraContext.getJiraIssueCreatorUser(), issueToTransition.getId(), transitionAction.getId(), parameters);

            if (!validationResult.isValid()) {
                handleErrorCollection("transitionIssue", eventData, validationResult.getErrorCollection());
            } else {
                final IssueResult result = jiraServices.getIssueService().transition(jiraContext.getJiraIssueCreatorUser(), validationResult);
                final ErrorCollection errors = result.getErrorCollection();
                if (errors.hasAnyErrors()) {
                    handleErrorCollection("transitionIssue", eventData, errors);
                } else {
                    final IssueImpl issueToUpdate = (IssueImpl) issueToTransition;
                    issueToUpdate.setStatusObject(result.getIssue().getStatus());
                    issueToUpdate.setResolutionObject(result.getIssue().getResolution());
                    logger.debug("NEW ISSUE STATUS: " + issueToUpdate.getStatus().getName());

                    if (issueToUpdate.getResolutionObject() == null) {
                        logger.debug("NEW ISSUE RESOLUTION OBJECT IS NULL");
                    } else {
                        logger.debug("NEW ISSUE RESOLUTION: " + issueToUpdate.getResolutionObject().getName());
                    }
                    final UpdateIssueRequest issueUpdate = UpdateIssueRequest.builder().eventDispatchOption(EventDispatchOption.ISSUE_UPDATED).sendMail(false).build();

                    final Issue updatedIssue = jiraServices.getIssueManager().updateIssue(user, issueToUpdate, issueUpdate);
                    return updatedIssue;
                }
            }
        } else {
            final String errorMessage = "Could not find the action : " + stepName + " to transition this issue: " + issueToTransition.getKey();
            logger.error(errorMessage);
            jiraSettingsService.addHubError(errorMessage, eventData.getHubProjectName(), eventData.getHubProjectVersion(), eventData.getJiraProjectName(), eventData.getJiraAdminUsername(), eventData.getJiraIssueCreatorUsername(),
                    "transitionIssue");
        }
        return null;
    }

    public void handleEvent(final EventData eventData) {
        logger.info("Handling event: " + eventData.getEventKey());

        final HubEventAction actionToTake = eventData.getAction();
        if (HubEventAction.OPEN.equals(actionToTake)) {
            final ExistenceAwareIssue openedIssue = openIssue(eventData);
            if (openedIssue != null) {
                if (openedIssue.isIssueStateChangeBlocked()) {
                    addComment(eventData, eventData.getJiraIssueCommentInLieuOfStateChange(), openedIssue.getIssue());
                }
            }
        } else if (HubEventAction.RESOLVE.equals(actionToTake)) {
            final ExistenceAwareIssue resolvedIssue = closeIssue(eventData);
            if (resolvedIssue != null) {
                if (resolvedIssue.isIssueStateChangeBlocked()) {
                    addComment(eventData, eventData.getJiraIssueCommentInLieuOfStateChange(), resolvedIssue.getIssue());
                }
            }
        } else if (HubEventAction.ADD_COMMENT.equals(actionToTake)) {
            final ExistenceAwareIssue issueToCommentOn = openIssue(eventData);
            if (issueToCommentOn != null && issueToCommentOn.getIssue() != null) {
                if (!issueToCommentOn.isExisted()) {
                    addComment(eventData, eventData.getJiraIssueComment(), issueToCommentOn.getIssue());
                } else if (issueToCommentOn.isIssueStateChangeBlocked()) {
                    addComment(eventData, eventData.getJiraIssueCommentInLieuOfStateChange(), issueToCommentOn.getIssue());
                } else {
                    addComment(eventData, eventData.getJiraIssueCommentForExistingIssue(), issueToCommentOn.getIssue());
                }
            }
        } else if (HubEventAction.ADD_COMMENT_IF_EXISTS.equals(actionToTake)) {
            final Issue existingIssue = findIssue(eventData);
            if (existingIssue != null) {
                addComment(eventData, eventData.getJiraIssueCommentInLieuOfStateChange(), existingIssue);
            }
        }
    }

    private void addComment(final EventData eventData, final String comment, final Issue issue) {
        logger.debug(String.format("Attempting to add comment to %s: %s", issue.getKey(), comment));
        if (comment != null && !checkIfAlreadyProcessedAndUpdateLastBatch(issue.getId(), eventData)) {
            final String lastCommentKey = String.valueOf(comment.hashCode());
            final PropertyResult propResult = jiraServices.getPropertyService().getProperty(jiraContext.getJiraIssueCreatorUser(), issue.getId(), HubJiraConstants.HUB_JIRA_ISSUE_LAST_COMMENT_KEY);
            if (propResult.isValid() && propResult.getEntityProperty().isDefined() && lastCommentKey.equals(propResult.getEntityProperty().get().getValue())) {
                // This comment would be a duplicate of the previous one, so there is no need to add it.
                return;
            }
            final CommentManager commentManager = jiraServices.getCommentManager();
            commentManager.create(issue, jiraContext.getJiraIssueCreatorUser(), comment, true);
            addIssuePropertyJson(eventData, issue.getId(), HubJiraConstants.HUB_JIRA_ISSUE_LAST_COMMENT_KEY, lastCommentKey);
        }
    }

    private ExistenceAwareIssue openIssue(final EventData eventData) {
        logger.debug("Setting logged in User : " + jiraContext.getJiraIssueCreatorUser().getDisplayName());
        jiraServices.getAuthContext().setLoggedInUser(jiraContext.getJiraIssueCreatorUser());
        logger.debug("eventData: " + eventData);

        final String notificationUniqueKey = getNotificationUniqueKey(eventData);
        if (notificationUniqueKey != null) {
            final Issue oldIssue = findIssue(eventData);

            if (oldIssue == null) {
                // Issue does not yet exist
                final Issue issue = createIssue(eventData);
                if (issue != null) {
                    logger.info("Created new Issue.");
                    printIssueInfo(issue);
                    final String hubIssueUrl = hubIssueTrackerHandler.createHubIssue(eventData.getComponentIssueUrl(), issue);

                    if (StringUtils.isNotBlank(hubIssueUrl)) {
                        final HubIssueTrackerProperties issueTrackerProperties = new HubIssueTrackerProperties(hubIssueUrl, issue.getId());
                        addHubIssueUrlIssueProperty(eventData, issueTrackerProperties, issue);
                    }

                    final IssuePropertiesGenerator issuePropertiesGenerator = eventData.getJiraIssuePropertiesGenerator();
                    final IssueProperties properties = issuePropertiesGenerator.createIssueProperties(issue.getId());
                    logger.debug("Adding properties to created issue: " + properties);
                    addLastBatchStartKeyToIssue(issue.getId(), eventData);
                    addIssueProperty(eventData, issue.getId(), notificationUniqueKey, properties);
                }
                return new ExistenceAwareIssue(issue, false, false);
            } else {
                // Issue already exists
                if (checkIfAlreadyProcessedAndUpdateLastBatch(oldIssue.getId(), eventData)) {
                    logger.debug("This issue has already been updated; plugin will not change issue's state");
                    return new ExistenceAwareIssue(oldIssue, true, true);
                }

                updateHubFieldsAndDescription(oldIssue, eventData);

                if (!issueUsesBdsWorkflow(oldIssue)) {
                    logger.debug("This is not the BDS workflow; plugin will not change issue's state");
                    return new ExistenceAwareIssue(oldIssue, true, true);
                }

                if (oldIssue.getStatus().getName().equals(HubJiraConstants.HUB_WORKFLOW_STATUS_RESOLVED)) {
                    final Issue transitionedIssue = transitionIssue(eventData, oldIssue, HubJiraConstants.HUB_WORKFLOW_TRANSITION_READD_OR_OVERRIDE_REMOVED, HubJiraConstants.HUB_WORKFLOW_STATUS_OPEN,
                            jiraContext.getJiraIssueCreatorUser());
                    if (transitionedIssue != null) {
                        logger.info("Re-opened the already exisiting issue.");
                        addComment(eventData, eventData.getJiraIssueReOpenComment(), oldIssue);
                        printIssueInfo(oldIssue);
                    }
                } else {
                    logger.info("This issue already exists and is not resolved.");
                    printIssueInfo(oldIssue);
                }
                return new ExistenceAwareIssue(oldIssue, true, false);
            }
        }
        return null;
    }

    private boolean issueUsesBdsWorkflow(final Issue oldIssue) {
        final JiraWorkflow issueWorkflow = jiraServices.getWorkflowManager().getWorkflow(oldIssue);
        logger.debug("Issue " + oldIssue.getKey() + " uses workflow " + issueWorkflow.getName());
        return HubJiraConstants.HUB_JIRA_WORKFLOW.equals(issueWorkflow.getName());
    }

    private ExistenceAwareIssue closeIssue(final EventData eventData) {
        final Issue oldIssue = findIssue(eventData);
        if (oldIssue != null) {
            boolean issueStateChangeBlocked = true;
            if (checkIfAlreadyProcessedAndUpdateLastBatch(oldIssue.getId(), eventData)) {
                logger.debug("This issue has already been updated; plugin will not change issue's state");
            } else if (!issueUsesBdsWorkflow(oldIssue)) {
                logger.debug("This is not the BDS workflow; plugin will not change issue's state");
            } else if (oldIssue.getStatus().getName().equals(HubJiraConstants.HUB_WORKFLOW_STATUS_CLOSED)) {
                logger.debug("This issue has been closed; plugin will not change issue's state");
            } else if (oldIssue.getStatus().getName().equals(HubJiraConstants.HUB_WORKFLOW_STATUS_RESOLVED)) {
                logger.debug("This issue is already Resolved; plugin will not change issue's state");
            } else {
                issueStateChangeBlocked = false;
                updateHubFieldsAndDescription(oldIssue, eventData);
                final Issue updatedIssue = transitionIssue(eventData, oldIssue, HubJiraConstants.HUB_WORKFLOW_TRANSITION_REMOVE_OR_OVERRIDE, HubJiraConstants.HUB_WORKFLOW_STATUS_RESOLVED, jiraContext.getJiraIssueCreatorUser());
                if (updatedIssue != null) {
                    addComment(eventData, eventData.getJiraIssueResolveComment(), updatedIssue);
                    logger.info("Resolved the issue based on an override.");
                    printIssueInfo(updatedIssue);
                }
            }
            return new ExistenceAwareIssue(oldIssue, true, issueStateChangeBlocked);
        } else {
            logger.info("Could not find an existing issue to close for this event.");
            logger.debug("Hub Project Name : " + eventData.getHubProjectName());
            logger.debug("Hub Project Version : " + eventData.getHubProjectVersion());
            logger.debug("Hub Component Name : " + eventData.getHubComponentName());
            logger.debug("Hub Component Version : " + eventData.getHubComponentVersion());
            if (eventData.isPolicy()) {
                logger.debug("Hub Rule Name : " + eventData.getHubRuleName());
            }
            return null;
        }
    }

    private boolean checkIfAlreadyProcessedAndUpdateLastBatch(final Long issueId, final EventData eventData) {
        final Date eventBatchStartDate = eventData.getLastBatchStartDate();
        if (eventBatchStartDate != null) {
            final PropertyResult propResult = jiraServices.getPropertyService().getProperty(jiraContext.getJiraIssueCreatorUser(), issueId, HubJiraConstants.HUB_JIRA_ISSUE_LAST_BATCH_START_KEY);
            if (propResult.isValid() && propResult.getEntityProperty().isDefined()) {
                final String lastBatchStartKey = propResult.getEntityProperty().get().getValue();
                if (isAlreadyProcessed(lastBatchStartKey, eventBatchStartDate)) {
                    // This issue has already been updated by a notification within the same startDate range, but outside of this batch (i.e. we
                    // already processed this notification at some point with a different instance of this class, perhaps on a different thread).
                    logger.debug("Ignoring a notification that has already been processed: eventKey=" + eventData.getEventKey());
                    return true;
                }
            }
            addLastBatchStartKeyToIssue(issueId, eventData);
        }
        return false;
    }

    private boolean isAlreadyProcessed(final String lastBatchStartKey, final Date eventBatchStartDate) {
        final String instanceUniqueDateString = getTimeString(instanceUniqueDate);
        final String currentBatchStartDateString = getTimeString(eventBatchStartDate);
        if (!lastBatchStartKey.endsWith(instanceUniqueDateString) && lastBatchStartKey.length() >= currentBatchStartDateString.length()) {
            final String lastBatchStartDateString = lastBatchStartKey.substring(0, currentBatchStartDateString.length());
            final Date lastBatchStartDate = new Date(Long.parseLong(lastBatchStartDateString));
            logger.debug("Determined that this notification is from a new batch. Last batch time key: " + lastBatchStartDateString + ". Current batch time key: " + currentBatchStartDateString + ".");
            if (lastBatchStartDate.compareTo(eventBatchStartDate) > 0) {
                return true;
            }
        }
        return false;
    }

    private void addLastBatchStartKeyToIssue(final Long issueId, final EventData eventData) {
        final Date eventBatchStartDate = eventData.getLastBatchStartDate();
        if (eventBatchStartDate != null) {
            final String newBatchStartKey = getTimeString(eventBatchStartDate) + getTimeString(instanceUniqueDate);
            addIssuePropertyJson(eventData, issueId, HubJiraConstants.HUB_JIRA_ISSUE_LAST_BATCH_START_KEY, newBatchStartKey);
        }
    }

    private String getTimeString(final Date date) {
        return Long.toString(date.getTime());
    }

    private void printIssueInfo(final Issue issue) {
        logger.debug("Issue Key : " + issue.getKey());
        logger.debug("Issue ID : " + issue.getId());
        logger.debug("Summary : " + issue.getSummary());
        logger.debug("Description : " + issue.getDescription());
        logger.debug("Issue Type : " + issue.getIssueType().getName());
        logger.debug("Status : " + issue.getStatus().getName());
        logger.debug("For Project : " + issue.getProjectObject().getName());
        logger.debug("For Project Id : " + issue.getProjectObject().getId());
    }

    protected class ExistenceAwareIssue {
        private final Issue issue;
        private final boolean existed;
        private final boolean issueStateChangeBlocked;

        // The constructor must be "package protected" to avoid synthetic access
        ExistenceAwareIssue(final Issue issue, final boolean existed, final boolean issueStateChangeBlocked) {
            super();
            this.issue = issue;
            this.existed = existed;
            this.issueStateChangeBlocked = issueStateChangeBlocked;
        }

        public Issue getIssue() {
            return issue;
        }

        public boolean isExisted() {
            return existed;
        }

        public boolean isIssueStateChangeBlocked() {
            return issueStateChangeBlocked;
        }
    }

}
