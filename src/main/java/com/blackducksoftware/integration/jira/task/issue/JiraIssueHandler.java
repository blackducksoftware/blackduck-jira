/**
 * Hub JIRA Plugin
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.jira.task.issue;

import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.atlassian.jira.bc.issue.IssueService.AssignValidationResult;
import com.atlassian.jira.bc.issue.IssueService.CreateValidationResult;
import com.atlassian.jira.bc.issue.IssueService.IssueResult;
import com.atlassian.jira.bc.issue.IssueService.TransitionValidationResult;
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
import com.blackducksoftware.integration.hub.notification.processor.event.NotificationEvent;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.common.JiraContext;
import com.blackducksoftware.integration.jira.common.TicketInfoFromSetup;
import com.blackducksoftware.integration.jira.task.JiraSettingsService;
import com.blackducksoftware.integration.jira.task.conversion.output.IssueProperties;
import com.blackducksoftware.integration.jira.task.conversion.output.IssuePropertiesGenerator;
import com.blackducksoftware.integration.jira.task.conversion.output.JiraEventInfo;
import com.blackducksoftware.integration.jira.task.conversion.output.PolicyViolationIssueProperties;
import com.blackducksoftware.integration.jira.task.conversion.output.VulnerabilityIssueProperties;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opensymphony.workflow.loader.ActionDescriptor;

public class JiraIssueHandler {
    private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final JiraContext jiraContext;

    private final JiraServices jiraServices;

    private final JiraSettingsService jiraSettingsService;

    private final TicketInfoFromSetup ticketInfoFromSetup;

    private final IssueFieldHandler issueFieldHandler;

    public JiraIssueHandler(final JiraServices jiraServices, final JiraContext jiraContext,
            final JiraSettingsService jiraSettingsService, final TicketInfoFromSetup ticketInfoFromSetup) {
        this.jiraServices = jiraServices;
        this.jiraContext = jiraContext;
        this.jiraSettingsService = jiraSettingsService;
        this.ticketInfoFromSetup = ticketInfoFromSetup;
        this.issueFieldHandler = new IssueFieldHandler(jiraServices, jiraSettingsService, jiraContext, ticketInfoFromSetup);
    }

    private void addIssueProperty(final NotificationEvent notificationEvent, final JiraEventInfo eventData,
            final Long issueId, final String key,
            final IssueProperties value) {

        final Gson gson = new GsonBuilder().create();

        final String jsonValue = gson.toJson(value);
        addIssuePropertyJson(notificationEvent, eventData, issueId, key, jsonValue);
    }

    private void handleErrorCollection(final String methodAttempt, final NotificationEvent notificationEvent,
            final JiraEventInfo eventData,
            final ErrorCollection errors) {
        if (errors.hasAnyErrors()) {
            logger.error("Error on: " + methodAttempt + " for notificationEvent: " + notificationEvent);
            for (final Entry<String, String> error : errors.getErrors().entrySet()) {
                final String errorMessage = error.getKey() + " / " + error.getValue();
                logger.error(errorMessage);
                jiraSettingsService.addHubError(errorMessage,
                        eventData.getHubProjectName(),
                        eventData.getHubProjectVersion(),
                        eventData.getJiraProjectName(),
                        eventData.getJiraUserName(),
                        methodAttempt);
            }
            for (final String errorMessage : errors.getErrorMessages()) {
                logger.error(errorMessage);
                jiraSettingsService.addHubError(errorMessage,
                        eventData.getHubProjectName(),
                        eventData.getHubProjectVersion(),
                        eventData.getJiraProjectName(),
                        eventData.getJiraUserName(),
                        methodAttempt);
            }
        }
    }

    private void addIssuePropertyJson(final NotificationEvent notificationEvent,
            final JiraEventInfo eventData,
            final Long issueId, final String key,
            final String jsonValue) {
        logger.debug("addIssuePropertyJson(): issueId: " + issueId + "; key: " + key + "; json: " + jsonValue);
        final EntityPropertyService.PropertyInput propertyInput = new EntityPropertyService.PropertyInput(jsonValue,
                key);

        final SetPropertyValidationResult validationResult = jiraServices.getPropertyService()
                .validateSetProperty(jiraContext.getJiraUser(), issueId, propertyInput);

        if (!validationResult.isValid()) {
            handleErrorCollection("addIssueProperty", notificationEvent, eventData, validationResult.getErrorCollection());
        } else {
            final PropertyResult result = jiraServices.getPropertyService().setProperty(jiraContext.getJiraUser(),
                    validationResult);
            handleErrorCollection("addIssueProperty", notificationEvent, eventData, result.getErrorCollection());
        }
    }

    private String getNotificationUniqueKey(final NotificationEvent notificationEvent) {
        String notificationUniqueKey = null;
        notificationUniqueKey = notificationEvent.getEventKey();
        return notificationUniqueKey;
    }

    private Issue findIssue(final NotificationEvent notificationEvent,
            final JiraEventInfo eventData) {
        logger.debug("findIssue(): notificationEvent: " + notificationEvent);

        final String notificationUniqueKey = getNotificationUniqueKey(notificationEvent);

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
            final IssueProperties propertyValue = createIssuePropertiesFromJson(notificationEvent, property.getValue());
            logger.debug("findIssue(): propertyValue (converted from JSON): " + propertyValue);
            final IssueResult result = jiraServices.getIssueService().getIssue(jiraContext.getJiraUser(),
                    propertyValue.getJiraIssueId());

            if (!result.isValid()) {
                handleErrorCollection("findIssue", notificationEvent, eventData, result.getErrorCollection());
            } else {
                return result.getIssue();
            }
        }
        return null;
    }

    private IssueProperties createIssuePropertiesFromJson(final NotificationEvent notificationEvent, final String json) {
        final Gson gson = new GsonBuilder().create();
        if (notificationEvent.isPolicyEvent()) {
            return gson.fromJson(json, PolicyViolationIssueProperties.class);
        }

        return gson.fromJson(json, VulnerabilityIssueProperties.class);
    }

    private Issue createIssue(final NotificationEvent notificationEvent, final JiraEventInfo eventData) {

        IssueInputParameters issueInputParameters = jiraServices.getIssueService().newIssueInputParameters();
        issueInputParameters.setProjectId(eventData.getJiraProjectId())
                .setIssueTypeId(eventData.getJiraIssueTypeId())
                .setSummary(eventData.getJiraIssueSummary())
                .setReporterId(eventData.getJiraUserName())
                .setDescription(eventData.getJiraIssueDescription());

        issueInputParameters.setRetainExistingValuesWhenParameterNotProvided(true);
        issueInputParameters.setApplyDefaultValuesWhenParameterNotProvided(true);

        final String assigneeId = eventData.getJiraIssueAssigneeUserId();
        if (assigneeId != null) {
            logger.debug("notificaitonEvent: issueAssigneeId: " + assigneeId);
            issueInputParameters = issueInputParameters.setAssigneeId(assigneeId);
        } else {
            logger.debug(
                    "notificationEvent: issueAssigneeId is not set, which will result in an unassigned Issue (assuming JIRA is configured to allow unassigned issues)");
        }
        logger.debug("issueInputParameters.getAssigneeId(): " + issueInputParameters.getAssigneeId());
        logger.debug("issueInputParameters.applyDefaultValuesWhenParameterNotProvided(): "
                + issueInputParameters.applyDefaultValuesWhenParameterNotProvided());
        logger.debug("issueInputParameters.retainExistingValuesWhenParameterNotProvided(): "
                + issueInputParameters.retainExistingValuesWhenParameterNotProvided());

        issueFieldHandler.setPluginFieldValues(notificationEvent, eventData, issueInputParameters);
        final List<String> labels = issueFieldHandler.setOtherFieldValues(notificationEvent, eventData, issueInputParameters);

        final CreateValidationResult validationResult = jiraServices.getIssueService()
                .validateCreate(jiraContext.getJiraUser(), issueInputParameters);
        logger.debug("createIssue(): Project: " + eventData.getJiraProjectName() + ": "
                + eventData.getJiraIssueSummary());
        if (!validationResult.isValid()) {
            handleErrorCollection("createIssue", notificationEvent, eventData, validationResult.getErrorCollection());
        } else {
            final IssueResult result = jiraServices.getIssueService().create(jiraContext.getJiraUser(),
                    validationResult);
            final ErrorCollection errors = result.getErrorCollection();
            if (errors.hasAnyErrors()) {
                handleErrorCollection("createIssue", notificationEvent, eventData, errors);
            } else {
                fixIssueAssignment(notificationEvent, eventData, result);
                issueFieldHandler.addLabels(result.getIssue(), labels);
                return result.getIssue();
            }
        }
        return null;
    }

    private void fixIssueAssignment(final NotificationEvent notificationEvent,
            final JiraEventInfo eventData, final IssueResult result) {
        final MutableIssue issue = result.getIssue();
        if (issue.getAssignee() == null) {
            logger.debug("Created issue " + issue.getKey() + "; Assignee: null");
        } else {
            logger.debug("Created issue " + issue.getKey() + "; Assignee: " + issue.getAssignee().getName());
        }
        final String assigneeId = eventData.getJiraIssueAssigneeUserId();
        if ((assigneeId == null) && (issue.getAssigneeId() != null)) {
            logger.debug("Issue needs to be UNassigned");
            assignIssue(issue, notificationEvent, eventData);
        } else if ((assigneeId != null)
                && (issue.getAssigneeId() != assigneeId)) {
            final String errorMessage = "Issue assignment failed";
            logger.error(errorMessage);
            jiraSettingsService.addHubError(errorMessage,
                    eventData.getHubProjectName(),
                    eventData.getHubProjectVersion(),
                    eventData.getJiraProjectName(),
                    eventData.getJiraUserName(),
                    "fixIssueAssignment");
        } else {
            logger.debug("Issue assignment is correct");
        }
    }

    private void assignIssue(final MutableIssue issue, final NotificationEvent notificationEvent,
            final JiraEventInfo eventData) {
        final ApplicationUser user = jiraContext.getJiraUser();
        final String assigneeId = eventData.getJiraIssueAssigneeUserId();
        final AssignValidationResult assignValidationResult = jiraServices.getIssueService().validateAssign(user,
                issue.getId(), assigneeId);
        final ErrorCollection errors = assignValidationResult.getErrorCollection();
        if (assignValidationResult.isValid() && !errors.hasAnyErrors()) {
            logger.debug("Assigning issue to user ID: " + assigneeId);
            jiraServices.getIssueService().assign(user, assignValidationResult);
            updateIssue(issue, assignValidationResult, user, assigneeId);
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
            jiraSettingsService.addHubError(errorMessage,
                    eventData.getHubProjectName(),
                    eventData.getHubProjectVersion(),
                    eventData.getJiraProjectName(),
                    eventData.getJiraUserName(),
                    "assignIssue");
        }
    }

    private Issue updateIssue(final MutableIssue issueToUpdate, final AssignValidationResult assignValidationResult,
            final ApplicationUser userMakingChange, final String assigneeId) {
        issueToUpdate.setAssigneeId(assigneeId);
        final UpdateIssueRequest issueUpdate = UpdateIssueRequest.builder()
                .eventDispatchOption(EventDispatchOption.ISSUE_UPDATED).sendMail(false).build();
        logger.debug("Updating issue with assigned user ID: " + assigneeId);
        final Issue updatedIssue = jiraServices.getIssueManager().updateIssue(userMakingChange, issueToUpdate,
                issueUpdate);
        return updatedIssue;
    }

    private Issue transitionIssue(final NotificationEvent notificationEvent,
            final JiraEventInfo eventData,
            final Issue issueToTransition,
            final String stepName, final String newExpectedStatus, final ApplicationUser user) {
        final Status currentStatus = issueToTransition.getStatus();
        logger.debug("Current status : " + currentStatus.getName());

        if (currentStatus.equals(newExpectedStatus)) {
            logger.debug("Will not tranisition issue, since it is already in the expected state.");
            return issueToTransition;
        }

        final JiraWorkflow workflow = jiraServices.getWorkflowManager().getWorkflow(issueToTransition);

        ActionDescriptor transitionAction = null;
        // https://answers.atlassian.com/questions/6985/how-do-i-change-status-of-issue
        final List<ActionDescriptor> actions = workflow.getLinkedStep(currentStatus).getActions();
        logger.debug("Found this many actions : " + actions.size());
        if (actions.size() == 0) {
            final String errorMessage = "Can not transition this issue : " + issueToTransition.getKey()
                    + ", from status : " + currentStatus.getName()
                    + ". There are no steps from this status to any other status.";
            logger.error(errorMessage);
            jiraSettingsService.addHubError(errorMessage,
                    eventData.getHubProjectName(),
                    eventData.getHubProjectVersion(),
                    eventData.getJiraProjectName(),
                    eventData.getJiraUserName(),
                    "transitionIssue");
        }
        for (final ActionDescriptor descriptor : actions) {
            if (descriptor.getName() != null && descriptor.getName().equals(stepName)) {
                logger.debug("Found Step descriptor : " + descriptor.getName());
                transitionAction = descriptor;
                break;
            }
        }
        if (transitionAction == null) {
            final String errorMessage = "Can not transition this issue : " + issueToTransition.getKey()
                    + ", from status : " + currentStatus.getName() + ". We could not find the step : " + stepName;
            logger.error(errorMessage);
            jiraSettingsService.addHubError(errorMessage,
                    eventData.getHubProjectName(),
                    eventData.getHubProjectVersion(),
                    eventData.getJiraProjectName(),
                    eventData.getJiraUserName(), "transitionIssue");
        }
        if (transitionAction != null) {
            final IssueInputParameters parameters = jiraServices.getIssueService().newIssueInputParameters();
            parameters.setRetainExistingValuesWhenParameterNotProvided(true);
            final TransitionValidationResult validationResult = jiraServices.getIssueService().validateTransition(
                    jiraContext.getJiraUser(), issueToTransition.getId(), transitionAction.getId(), parameters);

            if (!validationResult.isValid()) {
                handleErrorCollection("transitionIssue", notificationEvent, eventData, validationResult.getErrorCollection());
            } else {
                final IssueResult result = jiraServices.getIssueService().transition(jiraContext.getJiraUser(),
                        validationResult);
                final ErrorCollection errors = result.getErrorCollection();
                if (errors.hasAnyErrors()) {
                    handleErrorCollection("transitionIssue", notificationEvent, eventData, errors);
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
                    final UpdateIssueRequest issueUpdate = UpdateIssueRequest.builder()
                            .eventDispatchOption(EventDispatchOption.ISSUE_UPDATED).sendMail(false).build();

                    final Issue updatedIssue = jiraServices.getIssueManager().updateIssue(user, issueToUpdate,
                            issueUpdate);
                    return updatedIssue;
                }
            }
        } else {
            final String errorMessage = "Could not find the action : " + stepName + " to transition this issue: "
                    + issueToTransition.getKey();
            logger.error(errorMessage);
            jiraSettingsService.addHubError(errorMessage,
                    eventData.getHubProjectName(),
                    eventData.getHubProjectVersion(),
                    eventData.getJiraProjectName(),
                    eventData.getJiraUserName(), "transitionIssue");
        }
        return null;
    }

    public void handleEvent(final NotificationEvent notificationEvent) {
        final JiraEventInfo eventData = new JiraEventInfo(notificationEvent.getDataSet());
        logger.debug("Licences: " + eventData.getHubLicenseNames());

        switch (eventData.getAction()) {
        case OPEN:
            final ExistenceAwareIssue openedIssue = openIssue(notificationEvent, eventData);
            if (openedIssue != null) {
                if (openedIssue.issueStateChangeBlocked) {
                    addComment(eventData.getJiraIssueCommentInLieuOfStateChange(),
                            openedIssue.getIssue());
                }
            }
            break;
        case RESOLVE:
            final ExistenceAwareIssue resolvedIssue = closeIssue(notificationEvent, eventData);
            if (resolvedIssue != null) {
                if (resolvedIssue.issueStateChangeBlocked) {
                    addComment(eventData.getJiraIssueCommentInLieuOfStateChange(),
                            resolvedIssue.getIssue());
                }
            }
            break;
        case ADD_COMMENT:
            final ExistenceAwareIssue issueToCommentOn = openIssue(notificationEvent, eventData);
            if (issueToCommentOn != null) {
                if (!issueToCommentOn.isExisted()) {
                    addComment(eventData.getJiraIssueComment(), issueToCommentOn.getIssue());
                } else if (issueToCommentOn.isIssueStateChangeBlocked()) {
                    addComment(eventData.getJiraIssueCommentInLieuOfStateChange(),
                            issueToCommentOn.getIssue());
                } else {
                    addComment(eventData.getJiraIssueCommentForExistingIssue(),
                            issueToCommentOn.getIssue());
                }
            }
            break;
        case ADD_COMMENT_IF_EXISTS:
            final Issue existingIssue = findIssue(notificationEvent, eventData);
            if (existingIssue != null) {
                addComment(eventData.getJiraIssueCommentInLieuOfStateChange(), existingIssue);
            }
            break;
        }

    }

    private void addComment(final String comment, final Issue issue) {
        if (comment == null) {
            return;
        }
        final CommentManager commentManager = jiraServices.getCommentManager();
        commentManager.create(issue, jiraContext.getJiraUser(), comment, true);
    }

    private ExistenceAwareIssue openIssue(final NotificationEvent notificationEvent, final JiraEventInfo eventData) {
        logger.debug("Setting logged in User : " + jiraContext.getJiraUser().getDisplayName());
        jiraServices.getAuthContext().setLoggedInUser(jiraContext.getJiraUser());
        logger.debug("notificationEvent: " + notificationEvent);

        final String notificationUniqueKey = getNotificationUniqueKey(notificationEvent);
        if (notificationUniqueKey != null) {
            final Issue oldIssue = findIssue(notificationEvent, eventData);

            if (oldIssue == null) {
                // Issue does not yet exist
                final Issue issue = createIssue(notificationEvent, eventData);
                if (issue != null) {
                    logger.info("Created new Issue.");
                    printIssueInfo(issue);

                    final IssuePropertiesGenerator issuePropertiesGenerator = eventData.getJiraIssuePropertiesGenerator();
                    final IssueProperties properties = issuePropertiesGenerator.createIssueProperties(issue.getId());
                    logger.debug("Adding properties to created issue: " + properties);
                    addIssueProperty(notificationEvent, eventData, issue.getId(), notificationUniqueKey, properties);
                }
                return new ExistenceAwareIssue(issue, false, false);
            } else {
                // Issue already exists
                if (!issueUsesBdsWorkflow(oldIssue)) {
                    logger.debug("This is not the BDS workflow; plugin will not change issue's state");
                    return new ExistenceAwareIssue(oldIssue, true, true);
                }

                if (oldIssue.getStatus().getName().equals(HubJiraConstants.HUB_WORKFLOW_STATUS_RESOLVED)) {
                    final Issue transitionedIssue = transitionIssue(notificationEvent, eventData, oldIssue,
                            HubJiraConstants.HUB_WORKFLOW_TRANSITION_READD_OR_OVERRIDE_REMOVED,
                            HubJiraConstants.HUB_WORKFLOW_STATUS_OPEN,
                            jiraContext.getJiraUser());
                    if (transitionedIssue != null) {
                        logger.info("Re-opened the already exisiting issue.");
                        addComment(eventData.getJiraIssueReOpenComment(), oldIssue);
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
        boolean isBdsWorkflow;
        if (HubJiraConstants.HUB_JIRA_WORKFLOW.equals(issueWorkflow.getName())) {
            isBdsWorkflow = true;
        } else {
            isBdsWorkflow = false;
        }
        return isBdsWorkflow;
    }

    private ExistenceAwareIssue closeIssue(final NotificationEvent event,
            final JiraEventInfo eventData) {
        final Issue oldIssue = findIssue(event, eventData);
        if (oldIssue != null) {
            if (!issueUsesBdsWorkflow(oldIssue)) {
                logger.debug("This is not the BDS workflow; plugin will not change issue's state");
                return new ExistenceAwareIssue(oldIssue, true, true);
            }
            final Issue updatedIssue = transitionIssue(event, eventData, oldIssue,
                    HubJiraConstants.HUB_WORKFLOW_TRANSITION_REMOVE_OR_OVERRIDE,
                    HubJiraConstants.HUB_WORKFLOW_STATUS_RESOLVED, jiraContext.getJiraUser());
            if (updatedIssue != null) {
                addComment(eventData.getJiraIssueResolveComment(), updatedIssue);
                logger.info("Resolved the issue based on an override.");
                printIssueInfo(updatedIssue);
            }
            return new ExistenceAwareIssue(oldIssue, true, false);
        } else {
            logger.info("Could not find an existing issue to close for this event.");
            logger.debug("Hub Project Name : " + eventData.getHubProjectName());
            logger.debug("Hub Project Version : " + eventData.getHubProjectVersion());
            logger.debug("Hub Component Name : " + eventData.getHubComponentName());
            logger.debug("Hub Component Version : " + eventData.getHubComponentVersion());
            if (event.isPolicyEvent()) {
                logger.debug("Hub Rule Name : " + eventData.getHubRuleName());
            }
            return null;
        }
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

    private class ExistenceAwareIssue {
        private final Issue issue;

        private final boolean existed;

        private final boolean issueStateChangeBlocked;

        public ExistenceAwareIssue(final Issue issue, final boolean existed, final boolean issueStateChangeBlocked) {
            super();
            this.issue = issue;
            this.existed = existed;
            this.issueStateChangeBlocked = issueStateChangeBlocked;
        }

        private Issue getIssue() {
            return issue;
        }

        private boolean isExisted() {
            return existed;
        }

        public boolean isIssueStateChangeBlocked() {
            return issueStateChangeBlocked;
        }
    }

}
