/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
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
 *******************************************************************************/
package com.blackducksoftware.integration.jira.task.issue;

import java.net.URISyntaxException;
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
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.common.JiraContext;
import com.blackducksoftware.integration.jira.common.TicketInfoFromSetup;
import com.blackducksoftware.integration.jira.task.JiraSettingsService;
import com.blackducksoftware.integration.jira.task.conversion.output.IssueProperties;
import com.blackducksoftware.integration.jira.task.conversion.output.JiraEvent;
import com.blackducksoftware.integration.jira.task.conversion.output.JiraPolicyEvent;
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
        this.issueFieldHandler = new IssueFieldHandler(jiraServices, jiraContext, ticketInfoFromSetup);
    }

    private void addIssueProperty(final JiraEvent notificationEvent, final Long issueId, final String key,
            final IssueProperties value) {

        final Gson gson = new GsonBuilder().create();

        final String jsonValue = gson.toJson(value);
        addIssuePropertyJson(notificationEvent, issueId, key, jsonValue);
    }

    private void handleErrorCollection(final String methodAttempt, final JiraEvent notificationEvent,
            final ErrorCollection errors) {
        if (errors.hasAnyErrors()) {
            logger.error("Error on: " + methodAttempt + " for notificationEvent: " + notificationEvent);
            for (final Entry<String, String> error : errors.getErrors().entrySet()) {
                final String errorMessage = error.getKey() + " / " + error.getValue();
                logger.error(errorMessage);
                jiraSettingsService.addHubError(errorMessage,
                        notificationEvent.getNotificationContent().getProjectVersion().getProjectName(),
                        notificationEvent.getNotificationContent().getProjectVersion().getProjectVersionName(),
                        notificationEvent.getJiraProjectName(), notificationEvent.getJiraUserName(), methodAttempt);
            }
            for (final String errorMessage : errors.getErrorMessages()) {
                logger.error(errorMessage);
                jiraSettingsService.addHubError(errorMessage,
                        notificationEvent.getNotificationContent().getProjectVersion().getProjectName(),
                        notificationEvent.getNotificationContent().getProjectVersion().getProjectVersionName(),
                        notificationEvent.getJiraProjectName(), notificationEvent.getJiraUserName(), methodAttempt);
            }
        }
    }

    private void addIssuePropertyJson(final JiraEvent notificationEvent, final Long issueId, final String key,
            final String jsonValue) {
        logger.debug("addIssuePropertyJson(): issueId: " + issueId + "; key: " + key + "; json: " + jsonValue);
        final EntityPropertyService.PropertyInput propertyInput = new EntityPropertyService.PropertyInput(jsonValue,
                key);

        final SetPropertyValidationResult validationResult = jiraServices.getPropertyService()
                .validateSetProperty(jiraContext.getJiraUser(), issueId, propertyInput);

        if (!validationResult.isValid()) {
            handleErrorCollection("addIssueProperty", notificationEvent, validationResult.getErrorCollection());
        } else {
            final PropertyResult result = jiraServices.getPropertyService().setProperty(jiraContext.getJiraUser(),
                    validationResult);
            handleErrorCollection("addIssueProperty", notificationEvent, result.getErrorCollection());
        }
    }

    private String getNotificationUniqueKey(final JiraEvent notificationEvent) {
        String notificationUniqueKey = null;
        try {
            notificationUniqueKey = notificationEvent.getUniquePropertyKey();
        } catch (final URISyntaxException | HubIntegrationException e) {
            logger.error(e);
            jiraSettingsService.addHubError(e, notificationEvent.getNotificationContent().getProjectVersion().getProjectName(),
                    notificationEvent.getNotificationContent().getProjectVersion().getProjectVersionName(),
                    notificationEvent.getJiraProjectName(), notificationEvent.getJiraUserName(),
                    "getNotificationUniqueKey");
        }
        return notificationUniqueKey;
    }

    private Issue findIssue(final JiraEvent notificationEvent) {
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
            final IssueProperties propertyValue = notificationEvent.createIssuePropertiesFromJson(property.getValue());
            logger.debug("findIssue(): propertyValue (converted from JSON): " + propertyValue);
            final IssueResult result = jiraServices.getIssueService().getIssue(jiraContext.getJiraUser(),
                    propertyValue.getJiraIssueId());

            if (!result.isValid()) {
                handleErrorCollection("findIssue", notificationEvent, result.getErrorCollection());
            } else {
                return result.getIssue();
            }
        }
        return null;
    }

    private Issue createIssue(final JiraEvent notificationEvent) {

        IssueInputParameters issueInputParameters = jiraServices.getIssueService().newIssueInputParameters();
        issueInputParameters.setProjectId(notificationEvent.getJiraProjectId())
                .setIssueTypeId(notificationEvent.getJiraIssueTypeId()).setSummary(notificationEvent.getIssueSummary())
                .setReporterId(notificationEvent.getJiraUserName())
                .setDescription(notificationEvent.getIssueDescription());

        issueInputParameters.setRetainExistingValuesWhenParameterNotProvided(true);
        issueInputParameters.setApplyDefaultValuesWhenParameterNotProvided(true);

        if (notificationEvent.getIssueAssigneeId() != null) {
            logger.debug("notificaitonEvent: issueAssigneeId: " + notificationEvent.getIssueAssigneeId());
            issueInputParameters = issueInputParameters.setAssigneeId(notificationEvent.getIssueAssigneeId());
        } else {
            logger.debug(
                    "notificationEvent: issueAssigneeId is not set, which will result in an unassigned Issue (assuming JIRA is configured to allow unassigned issues)");
        }
        logger.debug("issueInputParameters.getAssigneeId(): " + issueInputParameters.getAssigneeId());
        logger.debug("issueInputParameters.applyDefaultValuesWhenParameterNotProvided(): "
                + issueInputParameters.applyDefaultValuesWhenParameterNotProvided());
        logger.debug("issueInputParameters.retainExistingValuesWhenParameterNotProvided(): "
                + issueInputParameters.retainExistingValuesWhenParameterNotProvided());

        issueFieldHandler.setPluginFieldValues(notificationEvent, issueInputParameters);
        final List<String> labels = issueFieldHandler.setOtherFieldValues(notificationEvent, issueInputParameters);

        final CreateValidationResult validationResult = jiraServices.getIssueService()
                .validateCreate(jiraContext.getJiraUser(), issueInputParameters);
        logger.debug("createIssue(): Project: " + notificationEvent.getJiraProjectName() + ": "
                + notificationEvent.getIssueSummary());
        if (!validationResult.isValid()) {
            handleErrorCollection("createIssue", notificationEvent, validationResult.getErrorCollection());
        } else {
            final IssueResult result = jiraServices.getIssueService().create(jiraContext.getJiraUser(),
                    validationResult);
            final ErrorCollection errors = result.getErrorCollection();
            if (errors.hasAnyErrors()) {
                handleErrorCollection("createIssue", notificationEvent, errors);
            } else {
                fixIssueAssignment(notificationEvent, result);
                issueFieldHandler.addLabels(result.getIssue(), labels);
                // JiraFieldUtils.printFields(logger, jiraServices.getFieldManager(), jiraContext.getJiraUser(),
                // result.getIssue());
                return result.getIssue();
            }
        }
        return null;
    }

    private void fixIssueAssignment(final JiraEvent notificationEvent, final IssueResult result) {
        final MutableIssue issue = result.getIssue();
        if (issue.getAssignee() == null) {
            logger.debug("Created issue " + issue.getKey() + "; Assignee: null");
        } else {
            logger.debug("Created issue " + issue.getKey() + "; Assignee: " + issue.getAssignee().getName());
        }
        if ((notificationEvent.getIssueAssigneeId() == null) && (issue.getAssigneeId() != null)) {
            logger.debug("Issue needs to be UNassigned");
            assignIssue(issue, notificationEvent);
        } else if ((notificationEvent.getIssueAssigneeId() != null)
                && (issue.getAssigneeId() != notificationEvent.getIssueAssigneeId())) {
            logger.error("Issue assignment is incorrect");
        } else {
            logger.debug("Issue assignment is correct");
        }
    }

    private void assignIssue(final MutableIssue issue, final JiraEvent notificationEvent) {
        final ApplicationUser user = jiraContext.getJiraUser();
        final AssignValidationResult assignValidationResult = jiraServices.getIssueService().validateAssign(user,
                issue.getId(), notificationEvent.getIssueAssigneeId());
        final ErrorCollection errors = assignValidationResult.getErrorCollection();
        if (assignValidationResult.isValid() && !errors.hasAnyErrors()) {
            logger.debug("Assigning issue to user ID: " + notificationEvent.getIssueAssigneeId());
            jiraServices.getIssueService().assign(user, assignValidationResult);
            updateIssue(issue, assignValidationResult, user, notificationEvent.getIssueAssigneeId());
        } else {
            final StringBuilder sb = new StringBuilder("Unable to assign issue ");
            sb.append(issue.getKey());
            sb.append(": ");
            for (final String errorMsg : errors.getErrorMessages()) {
                sb.append(errorMsg);
                sb.append("; ");
            }
            logger.error(sb.toString());
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

    private Issue transitionIssue(final JiraEvent notificationEvent, final Issue issueToTransition,
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
                    notificationEvent.getNotificationContent().getProjectVersion().getProjectName(),
                    notificationEvent.getNotificationContent().getProjectVersion().getProjectVersionName(),
                    notificationEvent.getJiraProjectName(), notificationEvent.getJiraUserName(), "transitionIssue");
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
                    notificationEvent.getNotificationContent().getProjectVersion().getProjectName(),
                    notificationEvent.getNotificationContent().getProjectVersion().getProjectVersionName(),
                    notificationEvent.getJiraProjectName(), notificationEvent.getJiraUserName(), "transitionIssue");
        }
        if (transitionAction != null) {
            final IssueInputParameters parameters = jiraServices.getIssueService().newIssueInputParameters();
            parameters.setRetainExistingValuesWhenParameterNotProvided(true);
            final TransitionValidationResult validationResult = jiraServices.getIssueService().validateTransition(
                    jiraContext.getJiraUser(), issueToTransition.getId(), transitionAction.getId(), parameters);

            if (!validationResult.isValid()) {
                handleErrorCollection("transitionIssue", notificationEvent, validationResult.getErrorCollection());
            } else {
                final IssueResult result = jiraServices.getIssueService().transition(jiraContext.getJiraUser(),
                        validationResult);
                final ErrorCollection errors = result.getErrorCollection();
                if (errors.hasAnyErrors()) {
                    handleErrorCollection("transitionIssue", notificationEvent, errors);
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
                    notificationEvent.getNotificationContent().getProjectVersion().getProjectName(),
                    notificationEvent.getNotificationContent().getProjectVersion().getProjectVersionName(),
                    notificationEvent.getJiraProjectName(), notificationEvent.getJiraUserName(), "transitionIssue");
        }
        return null;
    }

    public void handleEvent(final JiraEvent notificationEvent) {
        switch (notificationEvent.getAction()) {
        case OPEN:
            final ExistenceAwareIssue openedIssue = openIssue(notificationEvent);
            if (openedIssue != null) {
                if (openedIssue.issueStateChangeBlocked) {
                    addComment(notificationEvent.getCommentInLieuOfStateChange(), openedIssue.getIssue());
                }
            }
            break;
        case RESOLVE:
            final ExistenceAwareIssue resolvedIssue = closeIssue(notificationEvent);
            if (resolvedIssue != null) {
                if (resolvedIssue.issueStateChangeBlocked) {
                    addComment(notificationEvent.getCommentInLieuOfStateChange(), resolvedIssue.getIssue());
                }
            }
            break;
        case ADD_COMMENT:
            final ExistenceAwareIssue issueToCommentOn = openIssue(notificationEvent);
            if (issueToCommentOn != null) {
                if (!issueToCommentOn.isExisted()) {
                    addComment(notificationEvent.getComment(), issueToCommentOn.getIssue());
                } else if (issueToCommentOn.isIssueStateChangeBlocked()) {
                    addComment(notificationEvent.getCommentInLieuOfStateChange(), issueToCommentOn.getIssue());
                } else {
                    addComment(notificationEvent.getCommentIfExists(), issueToCommentOn.getIssue());
                }
            }
            break;
        case ADD_COMMENT_IF_EXISTS:
            final Issue existingIssue = findIssue(notificationEvent);
            if (existingIssue != null) {
                addComment(notificationEvent.getCommentInLieuOfStateChange(), existingIssue);
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

    private ExistenceAwareIssue openIssue(final JiraEvent notificationEvent) {
        logger.debug("Setting logged in User : " + jiraContext.getJiraUser().getDisplayName());
        jiraServices.getAuthContext().setLoggedInUser(jiraContext.getJiraUser());
        logger.debug("notificationEvent: " + notificationEvent);

        final String notificationUniqueKey = getNotificationUniqueKey(notificationEvent);
        if (notificationUniqueKey != null) {
            final Issue oldIssue = findIssue(notificationEvent);

            if (oldIssue == null) {
                // Issue does not yet exist
                final Issue issue = createIssue(notificationEvent);
                if (issue != null) {
                    logger.info("Created new Issue.");
                    printIssueInfo(issue);

                    final IssueProperties properties = notificationEvent.createIssueProperties(issue);
                    logger.debug("Adding properties to created issue: " + properties);
                    addIssueProperty(notificationEvent, issue.getId(), notificationUniqueKey, properties);
                }
                return new ExistenceAwareIssue(issue, false, false);
            } else {
                // Issue already exists
                if (!issueUsesBdsWorkflow(oldIssue)) {
                    logger.debug("This is not the BDS workflow; plugin will not change issue's state");
                    return new ExistenceAwareIssue(oldIssue, true, true);
                }

                if (oldIssue.getStatus().getName().equals(HubJiraConstants.HUB_WORKFLOW_STATUS_RESOLVED)) {
                    final Issue transitionedIssue = transitionIssue(notificationEvent, oldIssue,
                            HubJiraConstants.HUB_WORKFLOW_TRANSITION_READD_OR_OVERRIDE_REMOVED,
                            HubJiraConstants.HUB_WORKFLOW_STATUS_OPEN,
                            jiraContext.getJiraUser());
                    if (transitionedIssue != null) {
                        logger.info("Re-opened the already exisiting issue.");
                        addComment(notificationEvent.getReopenComment(), oldIssue);
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

    private ExistenceAwareIssue closeIssue(final JiraEvent event) {
        final Issue oldIssue = findIssue(event);
        if (oldIssue != null) {
            if (!issueUsesBdsWorkflow(oldIssue)) {
                logger.debug("This is not the BDS workflow; plugin will not change issue's state");
                return new ExistenceAwareIssue(oldIssue, true, true);
            }
            final Issue updatedIssue = transitionIssue(event, oldIssue,
                    HubJiraConstants.HUB_WORKFLOW_TRANSITION_REMOVE_OR_OVERRIDE,
                    HubJiraConstants.HUB_WORKFLOW_STATUS_RESOLVED, jiraContext.getJiraUser());
            if (updatedIssue != null) {
                addComment(event.getResolveComment(), updatedIssue);
                logger.info("Closed the issue based on an override.");
                printIssueInfo(updatedIssue);
            }
            return new ExistenceAwareIssue(oldIssue, true, false);
        } else {
            logger.info("Could not find an existing issue to close for this event.");
            logger.debug("Hub Project Name : " + event.getNotificationContent().getProjectVersion().getProjectName());
            logger.debug("Hub Project Version : " + event.getNotificationContent().getProjectVersion().getProjectVersionName());
            logger.debug("Hub Component Name : " + event.getNotificationContent().getComponentName());
            logger.debug("Hub Component Version : " + event.getNotificationContent().getComponentVersion());
            if (event instanceof JiraPolicyEvent) {
                final JiraPolicyEvent notificationResultRule = (JiraPolicyEvent) event;
                logger.debug("Hub Rule Name : " + notificationResultRule.getPolicyRule().getName());
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

        public ExistenceAwareIssue(Issue issue, boolean existed, boolean issueStateChangeBlocked) {
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
