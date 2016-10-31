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
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.common.JiraContext;
import com.blackducksoftware.integration.jira.common.TicketInfoFromSetup;
import com.blackducksoftware.integration.jira.task.JiraSettingsService;
import com.blackducksoftware.integration.jira.task.conversion.output.HubEvent;
import com.blackducksoftware.integration.jira.task.conversion.output.IssueProperties;
import com.blackducksoftware.integration.jira.task.conversion.output.PolicyEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opensymphony.workflow.loader.ActionDescriptor;

public class JiraIssueHandler {
    private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final JiraContext jiraContext;

    private final JiraServices jiraServices;

    private final JiraSettingsService jiraSettingsService;

    private final TicketInfoFromSetup ticketInfoFromSetup;

    public JiraIssueHandler(final JiraServices jiraServices, final JiraContext jiraContext,
            final JiraSettingsService jiraSettingsService, final TicketInfoFromSetup ticketInfoFromSetup) {
        this.jiraServices = jiraServices;
        this.jiraContext = jiraContext;
        this.jiraSettingsService = jiraSettingsService;
        this.ticketInfoFromSetup = ticketInfoFromSetup;
    }

    private void addIssueProperty(final HubEvent notificationEvent, final Long issueId, final String key,
            final IssueProperties value) {

        final Gson gson = new GsonBuilder().create();

        final String jsonValue = gson.toJson(value);
        addIssuePropertyJson(notificationEvent, issueId, key, jsonValue);
    }

    private void handleErrorCollection(final String methodAttempt, final HubEvent notificationEvent,
            final ErrorCollection errors) {
        if (errors.hasAnyErrors()) {
            logger.error("Error on: " + methodAttempt + " for notificationEvent: " + notificationEvent);
            for (final Entry<String, String> error : errors.getErrors().entrySet()) {
                final String errorMessage = error.getKey() + " / " + error.getValue();
                logger.error(errorMessage);
                jiraSettingsService.addHubError(errorMessage,
                        notificationEvent.getNotif().getProjectVersion().getProjectName(),
                        notificationEvent.getNotif().getProjectVersion().getProjectVersionName(),
                        notificationEvent.getJiraProjectName(), notificationEvent.getJiraUserName(), methodAttempt);
            }
            for (final String errorMessage : errors.getErrorMessages()) {
                logger.error(errorMessage);
                jiraSettingsService.addHubError(errorMessage,
                        notificationEvent.getNotif().getProjectVersion().getProjectName(),
                        notificationEvent.getNotif().getProjectVersion().getProjectVersionName(),
                        notificationEvent.getJiraProjectName(), notificationEvent.getJiraUserName(), methodAttempt);
            }
        }
    }

    private void addIssuePropertyJson(final HubEvent notificationEvent, final Long issueId, final String key,
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

    private String getNotificationUniqueKey(final HubEvent notificationEvent) {
        String notificationUniqueKey = null;
        try {
            notificationUniqueKey = notificationEvent.getUniquePropertyKey();
        } catch (final URISyntaxException e) {
            logger.error(e);
            jiraSettingsService.addHubError(e, notificationEvent.getNotif().getProjectVersion().getProjectName(),
                    notificationEvent.getNotif().getProjectVersion().getProjectVersionName(),
                    notificationEvent.getJiraProjectName(), notificationEvent.getJiraUserName(),
                    "getNotificationUniqueKey");
        }
        return notificationUniqueKey;
    }

    private Issue findIssue(final HubEvent notificationEvent) {
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

    private Issue createIssue(final HubEvent notificationEvent) {

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

        if (ticketInfoFromSetup != null && ticketInfoFromSetup.getCustomFields() != null
                && !ticketInfoFromSetup.getCustomFields().isEmpty()) {
            final Long projectFieldId = ticketInfoFromSetup.getCustomFields()
                    .get(HubJiraConstants.HUB_CUSTOM_FIELD_PROJECT).getIdAsLong();
            issueInputParameters.addCustomFieldValue(projectFieldId,
                    notificationEvent.getNotif().getProjectVersion().getProjectName());

            final Long projectVersionFieldId = ticketInfoFromSetup.getCustomFields()
                    .get(HubJiraConstants.HUB_CUSTOM_FIELD_PROJECT_VERSION).getIdAsLong();
            issueInputParameters.addCustomFieldValue(projectVersionFieldId,
                    notificationEvent.getNotif().getProjectVersion().getProjectVersionName());

            final Long componentFieldId = ticketInfoFromSetup.getCustomFields()
                    .get(HubJiraConstants.HUB_CUSTOM_FIELD_COMPONENT).getIdAsLong();
            issueInputParameters.addCustomFieldValue(componentFieldId, notificationEvent.getNotif().getComponentName());

            final Long componentVersionFieldId = ticketInfoFromSetup.getCustomFields()
                    .get(HubJiraConstants.HUB_CUSTOM_FIELD_COMPONENT_VERSION).getIdAsLong();
            issueInputParameters.addCustomFieldValue(componentVersionFieldId,
                    notificationEvent.getNotif().getComponentVersion());

            if (notificationEvent.getClass().equals(PolicyEvent.class)) {
                final PolicyEvent policyNotif = (PolicyEvent) notificationEvent;
                final Long policyRuleFieldId = ticketInfoFromSetup.getCustomFields()
                        .get(HubJiraConstants.HUB_CUSTOM_FIELD_POLICY_RULE).getIdAsLong();
                issueInputParameters.addCustomFieldValue(policyRuleFieldId, policyNotif.getPolicyRule().getName());
            }
        }

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
                return result.getIssue();
            }
        }
        return null;
    }

    private void fixIssueAssignment(final HubEvent notificationEvent, final IssueResult result) {
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

    private void assignIssue(final MutableIssue issue, final HubEvent notificationEvent) {
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

    private Issue transitionIssue(final HubEvent notificationEvent, final Issue issueToTransition,
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
                    notificationEvent.getNotif().getProjectVersion().getProjectName(),
                    notificationEvent.getNotif().getProjectVersion().getProjectVersionName(),
                    notificationEvent.getJiraProjectName(), notificationEvent.getJiraUserName(), "transitionIssue");
        }
        for (final ActionDescriptor descriptor : actions) {
            if (descriptor.getName() != null && descriptor.getName().equals(stepName)) {
                logger.info("Found Step descriptor : " + descriptor.getName());
                transitionAction = descriptor;
                break;
            }
        }
        if (transitionAction == null) {
            final String errorMessage = "Can not transition this issue : " + issueToTransition.getKey()
                    + ", from status : " + currentStatus.getName() + ". We could not find the step : " + stepName;
            logger.error(errorMessage);
            jiraSettingsService.addHubError(errorMessage,
                    notificationEvent.getNotif().getProjectVersion().getProjectName(),
                    notificationEvent.getNotif().getProjectVersion().getProjectVersionName(),
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
                    notificationEvent.getNotif().getProjectVersion().getProjectName(),
                    notificationEvent.getNotif().getProjectVersion().getProjectVersionName(),
                    notificationEvent.getJiraProjectName(), notificationEvent.getJiraUserName(), "transitionIssue");
        }
        return null;
    }

    public void handleEvent(final HubEvent notificationEvent) {
        logger.debug("changeIssueStateIfExists: " + notificationEvent.isChangeIssueStateIfExists());
        switch (notificationEvent.getAction()) {
        case OPEN:
            openIssue(notificationEvent);
            break;
        case RESOLVE:
            closeIssue(notificationEvent);
            break;
        case ADD_COMMENT:
            final ExistenceAwareIssue issue = openIssue(notificationEvent);
            if (issue != null) {
                if (!issue.isExisted()) {
                    addComment(notificationEvent.getComment(), issue.getIssue());
                } else {
                    addComment(notificationEvent.getCommentForExistingIssue(), issue.getIssue());
                }
            }
            break;
        case ADD_COMMENT_IF_EXISTS:
            final Issue existingIssue = findIssue(notificationEvent);
            if (existingIssue != null) {
                addComment(notificationEvent.getCommentForExistingIssue(), existingIssue);
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

    private ExistenceAwareIssue openIssue(final HubEvent notificationEvent) {
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
                return new ExistenceAwareIssue(issue, false);
            } else {
                // Issue already exists
                if (notificationEvent.isChangeIssueStateIfExists() &&
                        oldIssue.getStatus().getName().equals(HubJiraConstants.HUB_WORKFLOW_STATUS_RESOLVED)) {
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
                    logger.info("This issue already exists.");
                    printIssueInfo(oldIssue);
                }
                return new ExistenceAwareIssue(oldIssue, true);
            }
        }
        return null;
    }

    private void closeIssue(final HubEvent event) {
        if (!event.isChangeIssueStateIfExists()) {
            return;
        }
        final Issue oldIssue = findIssue(event);
        if (oldIssue != null) {
            final Issue updatedIssue = transitionIssue(event, oldIssue,
                    HubJiraConstants.HUB_WORKFLOW_TRANSITION_REMOVE_OR_OVERRIDE,
                    HubJiraConstants.HUB_WORKFLOW_STATUS_RESOLVED, jiraContext.getJiraUser());
            if (updatedIssue != null) {
                addComment(event.getResolveComment(), updatedIssue);
                logger.info("Closed the issue based on an override.");
                printIssueInfo(updatedIssue);
            }
        } else {
            logger.info("Could not find an existing issue to close for this event.");
            logger.debug("Hub Project Name : " + event.getNotif().getProjectVersion().getProjectName());
            logger.debug("Hub Project Version : " + event.getNotif().getProjectVersion().getProjectVersionName());
            logger.debug("Hub Component Name : " + event.getNotif().getComponentName());
            logger.debug("Hub Component Version : " + event.getNotif().getComponentVersion());
            if (event instanceof PolicyEvent) {
                final PolicyEvent notificationResultRule = (PolicyEvent) event;
                logger.debug("Hub Rule Name : " + notificationResultRule.getPolicyRule().getName());
            }
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
        public ExistenceAwareIssue(Issue issue, boolean existed) {
            super();
            this.issue = issue;
            this.existed = existed;
        }
        private Issue getIssue() {
            return issue;
        }
        private boolean isExisted() {
            return existed;
        }
    }

}
