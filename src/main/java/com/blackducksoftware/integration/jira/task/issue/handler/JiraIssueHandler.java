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

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.JiraUserContext;
import com.blackducksoftware.integration.jira.common.exception.JiraIssueException;
import com.blackducksoftware.integration.jira.config.JiraSettingsService;
import com.blackducksoftware.integration.jira.task.conversion.output.BlackDuckEventAction;
import com.blackducksoftware.integration.jira.task.conversion.output.BlackDuckIssueTrackerProperties;
import com.blackducksoftware.integration.jira.task.conversion.output.IssueProperties;
import com.blackducksoftware.integration.jira.task.conversion.output.IssuePropertiesGenerator;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventCategory;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventData;
import com.blackducksoftware.integration.jira.task.issue.model.BlackDuckIssueFieldTemplate;
import com.blackducksoftware.integration.jira.task.issue.model.JiraIssueFieldTemplate;
import com.blackducksoftware.integration.jira.task.issue.model.JiraIssueWrapper;
import com.blackducksoftware.integration.jira.task.issue.model.PolicyIssueFieldTempate;
import com.blackducksoftware.integration.jira.task.issue.model.VulnerabilityIssueFieldTemplate;
import com.opensymphony.workflow.loader.ActionDescriptor;

public class JiraIssueHandler {
    private final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final JiraIssueServiceWrapper issueServiceWrapper;
    private final JiraSettingsService jiraSettingsService;
    private final JiraAuthenticationContext authContext;
    private final JiraUserContext jiraUserContext;
    private final BlackDuckIssueTrackerHandler hubIssueTrackerHandler;
    private final BlackDuckIssueTrackerPropertyHandler hubIssueTrackerPropertyHandler;
    private final Date instanceUniqueDate;

    public JiraIssueHandler(final JiraIssueServiceWrapper issueServiceWrapper, final JiraSettingsService jiraSettingsService, final BlackDuckIssueTrackerHandler hubIssueTrackerHandler,
            final JiraAuthenticationContext authContext, final JiraUserContext jiraContext) {
        this.issueServiceWrapper = issueServiceWrapper;
        this.jiraSettingsService = jiraSettingsService;
        this.authContext = authContext;
        this.jiraUserContext = jiraContext;
        this.hubIssueTrackerHandler = hubIssueTrackerHandler;
        this.hubIssueTrackerPropertyHandler = new BlackDuckIssueTrackerPropertyHandler();
        this.instanceUniqueDate = new Date();
    }

    public void handleEvent(final EventData eventData) {
        logger.info("Handling event: " + eventData.getEventKey());

        final BlackDuckEventAction actionToTake = eventData.getAction();
        if (BlackDuckEventAction.OPEN.equals(actionToTake)) {
            final ExistenceAwareIssue openedIssue = openIssue(eventData);
            if (openedIssue != null) {
                if (openedIssue.isIssueStateChangeBlocked()) {
                    addComment(eventData, eventData.getJiraIssueCommentInLieuOfStateChange(), openedIssue.getIssue());
                }
            }
        } else if (BlackDuckEventAction.RESOLVE.equals(actionToTake)) {
            final ExistenceAwareIssue resolvedIssue = closeIssue(eventData);
            if (resolvedIssue != null) {
                if (resolvedIssue.isIssueStateChangeBlocked()) {
                    addComment(eventData, eventData.getJiraIssueCommentInLieuOfStateChange(), resolvedIssue.getIssue());
                }
            }
        } else if (BlackDuckEventAction.ADD_COMMENT.equals(actionToTake)) {
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
        } else if (BlackDuckEventAction.ADD_COMMENT_IF_EXISTS.equals(actionToTake)) {
            final Issue existingIssue = findIssue(eventData);
            if (existingIssue != null) {
                addComment(eventData, eventData.getJiraIssueCommentInLieuOfStateChange(), existingIssue);
            }
        }
    }

    private ExistenceAwareIssue openIssue(final EventData eventData) {
        logger.debug("Setting logged in User : " + eventData.getJiraIssueCreatorUsername());
        authContext.setLoggedInUser(jiraUserContext.getJiraIssueCreatorUser());
        logger.debug("eventData: " + eventData);

        final String notificationUniqueKey = eventData.getEventKey();
        if (notificationUniqueKey != null) {
            final Issue oldIssue = findIssue(eventData);
            if (oldIssue == null) {
                // Issue does not yet exist
                final Issue issue = createIssue(eventData);
                if (issue != null) {
                    logger.info("Created new Issue.");
                    printIssueInfo(issue);
                    final String hubIssueUrl = hubIssueTrackerHandler.createBlackDuckIssue(eventData.getComponentIssueUrl(), issue);

                    if (StringUtils.isNotBlank(hubIssueUrl)) {
                        final BlackDuckIssueTrackerProperties issueTrackerProperties = new BlackDuckIssueTrackerProperties(hubIssueUrl, issue.getId());
                        try {
                            final String key = hubIssueTrackerPropertyHandler.createEntityPropertyKey(issue.getId());
                            issueServiceWrapper.addProjectProperty(issue.getProjectId(), key, issueTrackerProperties);
                        } catch (final JiraIssueException e) {
                            handleJiraIssueException(e, eventData);
                        }
                    }
                    addDefaultIssueProperties(issue.getId(), eventData);
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

                if (oldIssue.getStatus().getName().equals(BlackDuckJiraConstants.HUB_WORKFLOW_STATUS_RESOLVED)) {
                    final Issue transitionedIssue = transitionIssue(eventData, oldIssue, BlackDuckJiraConstants.HUB_WORKFLOW_TRANSITION_READD_OR_OVERRIDE_REMOVED, BlackDuckJiraConstants.HUB_WORKFLOW_STATUS_OPEN);
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

    private ExistenceAwareIssue closeIssue(final EventData eventData) {
        final Issue oldIssue = findIssue(eventData);
        if (oldIssue != null) {
            boolean issueStateChangeBlocked = true;
            if (checkIfAlreadyProcessedAndUpdateLastBatch(oldIssue.getId(), eventData)) {
                logger.debug("This issue has already been updated; plugin will not change issue's state");
            } else if (!issueUsesBdsWorkflow(oldIssue)) {
                logger.debug("This is not the BDS workflow; plugin will not change issue's state");
            } else if (oldIssue.getStatus().getName().equals(BlackDuckJiraConstants.HUB_WORKFLOW_STATUS_CLOSED)) {
                logger.debug("This issue has been closed; plugin will not change issue's state");
            } else if (oldIssue.getStatus().getName().equals(BlackDuckJiraConstants.HUB_WORKFLOW_STATUS_RESOLVED)) {
                logger.debug("This issue is already Resolved; plugin will not change issue's state");
            } else {
                issueStateChangeBlocked = false;
                updateHubFieldsAndDescription(oldIssue, eventData);
                final Issue updatedIssue = transitionIssue(eventData, oldIssue, BlackDuckJiraConstants.HUB_WORKFLOW_TRANSITION_REMOVE_OR_OVERRIDE, BlackDuckJiraConstants.HUB_WORKFLOW_STATUS_RESOLVED);
                if (updatedIssue != null) {
                    addComment(eventData, eventData.getJiraIssueResolveComment(), updatedIssue);
                    logger.info("Resolved the issue based on an override.");
                    printIssueInfo(updatedIssue);
                }
            }
            return new ExistenceAwareIssue(oldIssue, true, issueStateChangeBlocked);
        } else {
            logger.info("Could not find an existing issue to close for this event.");
            logger.debug("Black Duck Project Name : " + eventData.getHubProjectName());
            logger.debug("Black Duck Project Version : " + eventData.getHubProjectVersion());
            logger.debug("Black Duck Component Name : " + eventData.getHubComponentName());
            logger.debug("Black Duck Component Version : " + eventData.getHubComponentVersion());
            if (eventData.isPolicy()) {
                logger.debug("Black Duck Rule Name : " + eventData.getHubRuleName());
            }
            return null;
        }
    }

    private void addComment(final EventData eventData, final String comment, final Issue issue) {
        logger.debug(String.format("Attempting to add comment to %s: %s", issue.getKey(), comment));
        if (comment != null && !checkIfAlreadyProcessedAndUpdateLastBatch(issue.getId(), eventData)) {
            final String newCommentKey = String.valueOf(comment.hashCode());
            final String storedLastCommentKey = issueServiceWrapper.getIssueProperty(issue.getId(), BlackDuckJiraConstants.HUB_JIRA_ISSUE_LAST_COMMENT_KEY);
            if (storedLastCommentKey != null && newCommentKey.equals(storedLastCommentKey)) {
                // This comment would be a duplicate of the previous one, so there is no need to add it.
                logger.debug("Ignoring a comment that would be an exact duplicate of the previous comment.");
                return;
            }
            issueServiceWrapper.addComment(issue, comment);
            try {
                issueServiceWrapper.addIssuePropertyJson(issue.getId(), BlackDuckJiraConstants.HUB_JIRA_ISSUE_LAST_COMMENT_KEY, newCommentKey);
            } catch (final JiraIssueException e) {
                handleJiraIssueException(e, eventData);
            }
        }
    }

    private void addDefaultIssueProperties(final Long issueId, final EventData eventData) {
        final IssuePropertiesGenerator issuePropertiesGenerator = eventData.getJiraIssuePropertiesGenerator();
        final IssueProperties properties = issuePropertiesGenerator.createIssueProperties(issueId);
        logger.debug("Adding properties to created issue: " + properties);
        addLastBatchStartKeyToIssue(issueId, eventData);
        try {
            issueServiceWrapper.addIssueProperty(issueId, eventData.getEventKey(), properties);
        } catch (final JiraIssueException e) {
            handleJiraIssueException(e, eventData);
        }
    }

    private Issue findIssue(final EventData eventData) {
        try {
            final EventCategory eventCategory = EventCategory.fromNotificationType(eventData.getNotificationType());
            final String notificationUniqueKey = eventData.getEventKey();
            return issueServiceWrapper.findIssue(eventCategory, notificationUniqueKey);
        } catch (final JiraIssueException e) {
            handleJiraIssueException(e, eventData);
        }
        return null;
    }

    private Issue createIssue(final EventData eventData) {
        try {
            final JiraIssueWrapper jiraIssueWrapper = createJiraIssueWrapperFromEventData(eventData.getJiraProjectId(), eventData, true, true);
            return issueServiceWrapper.createIssue(jiraIssueWrapper);
        } catch (final JiraIssueException e) {
            handleJiraIssueException(e, eventData);
        }
        return null;
    }

    private Issue updateHubFieldsAndDescription(final Issue existingIssue, final EventData eventData) {
        try {
            final JiraIssueWrapper jiraIssueWrapper = createJiraIssueWrapperFromEventData(existingIssue.getId(), eventData, false, true);
            return issueServiceWrapper.updateIssue(existingIssue.getId(), jiraIssueWrapper);
        } catch (final JiraIssueException e) {
            handleJiraIssueException(e, eventData);
        }
        return null;
    }

    private Issue transitionIssue(final EventData eventData, final Issue issueToTransition, final String stepName, final String newExpectedStatus) {
        final Status currentStatus = issueToTransition.getStatus();
        logger.debug("Current status : " + currentStatus.getName());

        if (currentStatus.getName().equals(newExpectedStatus)) {
            logger.debug("Will not tranisition issue, since it is already in the expected state.");
            return issueToTransition;
        }

        final JiraWorkflow workflow = issueServiceWrapper.getWorkflow(issueToTransition);

        ActionDescriptor transitionAction = null;
        // https://answers.atlassian.com/questions/6985/how-do-i-change-status-of-issue
        final List<ActionDescriptor> actions = workflow.getLinkedStep(currentStatus).getActions();
        logger.debug("Found this many actions : " + actions.size());
        if (actions.size() == 0) {
            final String errorMessage = "Can not transition this issue : " + issueToTransition.getKey() + ", from status : " + currentStatus.getName() + ". There are no steps from this status to any other status.";
            logger.error(errorMessage);
            jiraSettingsService.addBlackDuckError(errorMessage, eventData.getHubProjectName(), eventData.getHubProjectVersion(), eventData.getJiraProjectName(), eventData.getJiraAdminUsername(), eventData.getJiraIssueCreatorUsername(),
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
            try {
                return issueServiceWrapper.transitionIssue(issueToTransition, transitionAction.getId());
            } catch (final JiraIssueException e) {
                handleJiraIssueException(e, eventData);
            }
        } else {
            final String errorMessage = "Could not find the action : " + stepName + " to transition this issue: " + issueToTransition.getKey();
            logger.error(errorMessage);
            jiraSettingsService.addBlackDuckError(errorMessage, eventData.getHubProjectName(), eventData.getHubProjectVersion(), eventData.getJiraProjectName(), eventData.getJiraAdminUsername(), eventData.getJiraIssueCreatorUsername(),
                    "transitionIssue");
        }
        return null;
    }

    private boolean issueUsesBdsWorkflow(final Issue oldIssue) {
        final JiraWorkflow issueWorkflow = issueServiceWrapper.getWorkflow(oldIssue);
        logger.debug("Issue " + oldIssue.getKey() + " uses workflow " + issueWorkflow.getName());
        return BlackDuckJiraConstants.HUB_JIRA_WORKFLOW.equals(issueWorkflow.getName());
    }

    private boolean checkIfAlreadyProcessedAndUpdateLastBatch(final Long issueId, final EventData eventData) {
        final Date eventBatchStartDate = eventData.getLastBatchStartDate();
        if (eventBatchStartDate != null) {
            final String lastBatchStartKey = issueServiceWrapper.getIssueProperty(issueId, BlackDuckJiraConstants.HUB_JIRA_ISSUE_LAST_BATCH_START_KEY);
            if (lastBatchStartKey != null && isAlreadyProcessed(lastBatchStartKey, eventBatchStartDate)) {
                // This issue has already been updated by a notification within the same startDate range, but outside of this batch (i.e. we
                // already processed this notification at some point with a different instance of this class, perhaps on a different thread).
                logger.debug("Ignoring a notification that has already been processed: eventKey=" + eventData.getEventKey());
                return true;
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
            try {
                issueServiceWrapper.addIssuePropertyJson(issueId, BlackDuckJiraConstants.HUB_JIRA_ISSUE_LAST_BATCH_START_KEY, newBatchStartKey);
            } catch (final JiraIssueException e) {
                handleJiraIssueException(e, eventData);
            }
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

    // TODO eventually get rid of event data in favor of issue field templates
    private JiraIssueWrapper createJiraIssueWrapperFromEventData(final Long jiraProjectId, final EventData eventData, final boolean applyDefaults, final boolean retainExisting) throws JiraIssueException {
        final JiraIssueFieldTemplate jiraIssueFieldTemplate = new JiraIssueFieldTemplate(jiraProjectId, eventData.getJiraProjectName(), eventData.getJiraIssueTypeId(), eventData.getJiraIssueSummary(),
                eventData.getJiraIssueCreatorUsername(), eventData.getJiraIssueDescription(), eventData.getJiraIssueAssigneeUserId());
        jiraIssueFieldTemplate.setApplyDefaultValuesWhenParameterNotProvided(applyDefaults);
        jiraIssueFieldTemplate.setRetainExistingValuesWhenParameterNotProvided(retainExisting);

        BlackDuckIssueFieldTemplate blackDuckIssueTemplate;

        final NotificationType type = eventData.getNotificationType();
        final EventCategory eventCategory = EventCategory.fromNotificationType(type);
        if (EventCategory.POLICY.equals(eventCategory)) {
            blackDuckIssueTemplate = new PolicyIssueFieldTempate(eventData.getHubProjectOwner(), eventData.getHubProjectName(), eventData.getHubProjectVersion(), eventData.getHubProjectVersionUrl(), eventData.getHubProjectVersionNickname(),
                    eventData.getHubComponentName(), eventData.getHubComponentUrl(), eventData.getHubComponentVersion(), eventData.getHubComponentVersionUrl(), eventData.getHubLicenseNames(), eventData.getHubLicenseUrl(),
                    eventData.getHubComponentUsage(), eventData.getHubProjectVersionLastUpdated(), eventData.getHubRuleName(), eventData.getHubRuleUrl(), eventData.getHubRuleOverridable(), eventData.getHubRuleDescription());
        } else if (EventCategory.VULNERABILITY.equals(eventCategory)) {
            blackDuckIssueTemplate = new VulnerabilityIssueFieldTemplate(eventData.getHubProjectOwner(), eventData.getHubProjectName(), eventData.getHubProjectVersion(), eventData.getHubProjectVersionUrl(),
                    eventData.getHubProjectVersionNickname(),
                    eventData.getHubComponentName(), eventData.getHubComponentVersion(), eventData.getHubComponentVersionUrl(), eventData.getHubComponentOrigin(), eventData.getHubComponentOriginId(),
                    eventData.getHubLicenseNames(), eventData.getHubLicenseUrl(), eventData.getHubComponentUsage(), eventData.getHubProjectVersionLastUpdated());
        } else {
            throw new JiraIssueException("Could not correctly wrap event data: " + eventData, "createJiraIssueWrapperFromEventData");
        }

        return new JiraIssueWrapper(jiraIssueFieldTemplate, blackDuckIssueTemplate, eventData.getJiraFieldCopyMappings());
    }

    private void handleJiraIssueException(final JiraIssueException issueException, final EventData eventData) {
        handleJiraIssueException(issueException, eventData.getHubProjectName(), eventData.getHubProjectVersion(), eventData.getJiraProjectName(), eventData.getJiraAdminUsername(), eventData.getJiraIssueCreatorUsername());
    }

    private void handleJiraIssueException(final JiraIssueException issueException, final String hubProjectName, final String hubProjectVersionName, final String jiraProjectName, final String jiraAdminUsername,
            final String jiraIssueCreatorUsername) {
        final String exceptionMessage = issueException.getMessage();
        final String methodAttempt = issueException.getMethodAttempt();
        final ErrorCollection errorCollection = issueException.getErrorCollection();
        if (errorCollection.hasAnyErrors()) {
            logger.error("Error on: " + methodAttempt);
            for (final Entry<String, String> error : errorCollection.getErrors().entrySet()) {
                final String errorMessage = error.getKey() + " / " + error.getValue();
                logger.error(errorMessage);
                jiraSettingsService.addBlackDuckError(errorMessage, hubProjectName, hubProjectVersionName, jiraProjectName, jiraAdminUsername, jiraIssueCreatorUsername, methodAttempt);
            }
            for (final String errorMessage : errorCollection.getErrorMessages()) {
                logger.error(errorMessage);
                jiraSettingsService.addBlackDuckError(errorMessage, hubProjectName, hubProjectVersionName, jiraProjectName, jiraAdminUsername, jiraIssueCreatorUsername, methodAttempt);
            }
        } else if (exceptionMessage != null) {
            jiraSettingsService.addBlackDuckError(exceptionMessage, hubProjectName, hubProjectVersionName, jiraProjectName, jiraAdminUsername, jiraIssueCreatorUsername, methodAttempt);
        } else {
            jiraSettingsService.addBlackDuckError(issueException, hubProjectName, hubProjectVersionName, jiraProjectName, jiraAdminUsername, jiraIssueCreatorUsername, methodAttempt);
        }
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
