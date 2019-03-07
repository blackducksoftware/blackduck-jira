/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.JiraUserContext;
import com.blackducksoftware.integration.jira.common.exception.JiraIssueException;
import com.blackducksoftware.integration.jira.config.JiraSettingsService;
import com.blackducksoftware.integration.jira.config.PluginConfigurationDetails;
import com.blackducksoftware.integration.jira.task.conversion.output.BlackDuckIssueAction;
import com.blackducksoftware.integration.jira.task.conversion.output.BlackDuckIssueTrackerProperties;
import com.blackducksoftware.integration.jira.task.conversion.output.IssueProperties;
import com.blackducksoftware.integration.jira.task.issue.model.BlackDuckIssueFieldTemplate;
import com.blackducksoftware.integration.jira.task.issue.model.BlackDuckIssueModel;
import com.blackducksoftware.integration.jira.task.issue.model.IssueCategory;
import com.opensymphony.workflow.loader.ActionDescriptor;

public class JiraIssueHandler {
    private final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final JiraIssueServiceWrapper issueServiceWrapper;
    private final JiraSettingsService jiraSettingsService;
    private final JiraAuthenticationContext authContext;
    private final JiraUserContext jiraUserContext;
    private final BlackDuckIssueTrackerHandler blackDuckIssueTrackerHandler;
    private final BlackDuckIssueTrackerPropertyHandler blackDuckIssueTrackerPropertyHandler;
    private final PluginConfigurationDetails pluginConfigurationDetails;
    private final Date instanceUniqueDate;

    public JiraIssueHandler(final JiraIssueServiceWrapper issueServiceWrapper, final JiraSettingsService jiraSettingsService, final BlackDuckIssueTrackerHandler blackDuckIssueTrackerHandler,
        final JiraAuthenticationContext authContext, final JiraUserContext jiraContext, final PluginConfigurationDetails pluginConfigurationDetails) {
        this.issueServiceWrapper = issueServiceWrapper;
        this.jiraSettingsService = jiraSettingsService;
        this.authContext = authContext;
        this.jiraUserContext = jiraContext;
        this.blackDuckIssueTrackerHandler = blackDuckIssueTrackerHandler;
        this.blackDuckIssueTrackerPropertyHandler = new BlackDuckIssueTrackerPropertyHandler();
        this.pluginConfigurationDetails = pluginConfigurationDetails;
        this.instanceUniqueDate = new Date();
    }

    public void handleBlackDuckIssue(final BlackDuckIssueModel blackDuckIssueModel) {
        logger.info(String.format("Performing action '%s' on BOM Component: %s", blackDuckIssueModel.getIssueAction(), blackDuckIssueModel.getBomComponentUri()));
        logger.debug("Handling event. Old key: " + blackDuckIssueModel.getEventKey());
        final BlackDuckIssueAction actionToTake = blackDuckIssueModel.getIssueAction();
        Issue jiraIssue = null;
        //TODO: clean up code to find or create a jira issue and then add a comment if possible.
        if (BlackDuckIssueAction.OPEN.equals(actionToTake)) {
            final ExistenceAwareIssue openedIssue = openIssue(blackDuckIssueModel);
            if (openedIssue != null) {
                jiraIssue = openedIssue.getIssue();
                if (openedIssue.isIssueStateChangeBlocked()) {
                    addComment(blackDuckIssueModel, blackDuckIssueModel.getJiraIssueCommentInLieuOfStateChange(), jiraIssue);
                }
            }
        } else if (BlackDuckIssueAction.RESOLVE.equals(actionToTake)) {
            final ExistenceAwareIssue resolvedIssue = closeIssue(blackDuckIssueModel);
            if (resolvedIssue != null) {
                jiraIssue = resolvedIssue.getIssue();
                if (resolvedIssue.isIssueStateChangeBlocked()) {
                    addComment(blackDuckIssueModel, blackDuckIssueModel.getJiraIssueCommentInLieuOfStateChange(), jiraIssue);
                }
            }
        } else if (BlackDuckIssueAction.ADD_COMMENT.equals(actionToTake)) {
            final ExistenceAwareIssue issueToCommentOn = openIssue(blackDuckIssueModel);
            if (issueToCommentOn != null && issueToCommentOn.getIssue() != null) {
                jiraIssue = issueToCommentOn.getIssue();
                if (!issueToCommentOn.isExisted()) {
                    addComment(blackDuckIssueModel, blackDuckIssueModel.getJiraIssueComment(), jiraIssue);
                } else if (issueToCommentOn.isIssueStateChangeBlocked()) {
                    addComment(blackDuckIssueModel, blackDuckIssueModel.getJiraIssueCommentInLieuOfStateChange(), jiraIssue);
                } else {
                    addComment(blackDuckIssueModel, blackDuckIssueModel.getJiraIssueCommentForExistingIssue(), jiraIssue);
                }
            }
        } else if (BlackDuckIssueAction.ADD_COMMENT_IF_EXISTS.equals(actionToTake)) {
            final Issue existingIssue = findIssueAndUpdateModel(blackDuckIssueModel);
            if (existingIssue != null) {
                jiraIssue = existingIssue;
                addComment(blackDuckIssueModel, blackDuckIssueModel.getJiraIssueCommentInLieuOfStateChange(), existingIssue);
            }
        } else if (BlackDuckIssueAction.UPDATE_IF_EXISTS.equals(actionToTake)) {
            final ExistenceAwareIssue openedIssue = updateIssueIfExists(blackDuckIssueModel);
            if (openedIssue != null && !openedIssue.isExisted()) {
                jiraIssue = openedIssue.getIssue();
                addComment(blackDuckIssueModel, blackDuckIssueModel.getJiraIssueCommentInLieuOfStateChange(), jiraIssue);
            }
        } else if (BlackDuckIssueAction.RESOLVE_ALL.equals(actionToTake)) {
            resolveAllRelatedIssues(blackDuckIssueModel);
        } else {
            logger.warn("No action to take for event data: " + blackDuckIssueModel);
        }
        // add component reviewer for single issues.  resolve all handles
        addComponentReviewerAsWatcher(jiraIssue, blackDuckIssueModel);
    }

    private ExistenceAwareIssue openIssue(final BlackDuckIssueModel blackDuckIssueModel) {
        final ApplicationUser issueCreator = blackDuckIssueModel.getJiraIssueFieldTemplate().getIssueCreator();
        logger.debug("Setting logged in User : " + issueCreator);
        authContext.setLoggedInUser(issueCreator);
        logger.debug("issue template: " + blackDuckIssueModel);

        final Issue oldIssue = findIssueAndUpdateModel(blackDuckIssueModel);
        if (oldIssue == null) {
            // Issue does not yet exist
            final Issue issue = createIssue(blackDuckIssueModel);
            if (issue != null) {
                blackDuckIssueModel.setJiraIssueId(issue.getId());
                logger.info("Created new Issue.");
                printIssueInfo(issue);
                updateIssueTrackerProperties(blackDuckIssueModel, issue);
                updateDefaultIssueProperties(blackDuckIssueModel);
                addLastBatchStartKeyToIssue(blackDuckIssueModel);
            }
            return new ExistenceAwareIssue(issue, false, false);
        } else {
            // Issue already exists
            if (checkIfAlreadyProcessedAndUpdateLastBatch(blackDuckIssueModel)) {
                logger.debug("This issue has already been updated; plugin will not change issue's state");
                return new ExistenceAwareIssue(oldIssue, true, true);
            }

            updateBlackDuckFieldsAndDescription(blackDuckIssueModel);

            if (!issueUsesBdsWorkflow(oldIssue)) {
                logger.debug("This is not the BDS workflow; plugin will not change issue's state");
                return new ExistenceAwareIssue(oldIssue, true, true);
            }

            if (BlackDuckJiraConstants.BLACKDUCK_WORKFLOW_STATUS_RESOLVED.equals(oldIssue.getStatus().getName())) {
                final Issue transitionedIssue = transitionIssue(blackDuckIssueModel, oldIssue, BlackDuckJiraConstants.BLACKDUCK_WORKFLOW_TRANSITION_READ_OR_OVERRIDE_REMOVED, BlackDuckJiraConstants.BLACKDUCK_WORKFLOW_STATUS_OPEN);
                if (transitionedIssue != null) {
                    logger.info("Re-opened the already existing issue.");
                    addComment(blackDuckIssueModel, blackDuckIssueModel.getJiraIssueReOpenComment(), oldIssue);
                    printIssueInfo(oldIssue);
                }
            } else {
                logger.info("This issue already exists and is not resolved.");
                printIssueInfo(oldIssue);
            }
            return new ExistenceAwareIssue(oldIssue, true, false);
        }
    }

    private ExistenceAwareIssue closeIssue(final BlackDuckIssueModel blackDuckIssueModel) {
        final Issue oldIssue = findIssueAndUpdateModel(blackDuckIssueModel);
        return closeIssue(blackDuckIssueModel, oldIssue);
    }

    private void resolveAllRelatedIssues(final BlackDuckIssueModel blackDuckIssueModel) {
        try {
            final String bomComponentUri = blackDuckIssueModel.getBomComponentUri();
            logger.debug("Resolving all issues associated with the missing component: " + bomComponentUri);
            final List<IssueProperties> foundProperties = findIssuePropertiesByBomComponentUri(bomComponentUri);
            for (final IssueProperties properties : foundProperties) {
                blackDuckIssueModel.setJiraIssueId(properties.getJiraIssueId());
                final Issue matchingIssue = issueServiceWrapper.getIssue(properties.getJiraIssueId());
                final ExistenceAwareIssue closedIssue = closeIssue(blackDuckIssueModel, matchingIssue);
                if (closedIssue != null && closedIssue.isIssueStateChangeBlocked()) {
                    Issue jiraIssue = closedIssue.getIssue();
                    addComment(blackDuckIssueModel, blackDuckIssueModel.getJiraIssueCommentInLieuOfStateChange(), jiraIssue);
                    addComponentReviewerAsWatcher(jiraIssue, blackDuckIssueModel);
                }
            }
        } catch (final JiraIssueException e) {
            handleJiraIssueException(e, blackDuckIssueModel);
        }
    }

    private ExistenceAwareIssue closeIssue(final BlackDuckIssueModel blackDuckIssueModel, final Issue oldIssue) {
        if (oldIssue != null) {
            final boolean issueStateChangeBlocked = blockStateChange(oldIssue, blackDuckIssueModel);
            if (!issueStateChangeBlocked) {
                updateBlackDuckFieldsAndDescription(blackDuckIssueModel);
                final Issue updatedIssue = transitionIssue(blackDuckIssueModel, oldIssue, BlackDuckJiraConstants.BLACKDUCK_WORKFLOW_TRANSITION_REMOVE_OR_OVERRIDE, BlackDuckJiraConstants.BLACKDUCK_WORKFLOW_STATUS_RESOLVED);
                if (updatedIssue != null) {
                    addComment(blackDuckIssueModel, blackDuckIssueModel.getJiraIssueResolveComment(), updatedIssue);
                    logger.info("Resolved the issue based on an override.");
                    printIssueInfo(updatedIssue);
                }
            }
            return new ExistenceAwareIssue(oldIssue, true, issueStateChangeBlocked);
        } else {
            final BlackDuckIssueFieldTemplate blackDuckIssueFieldTemplate = blackDuckIssueModel.getBlackDuckIssueTemplate();
            logger.info("Could not find an existing issue to close for this event.");
            logger.debug("Black Duck Project Name : " + blackDuckIssueFieldTemplate.getProjectName());
            logger.debug("Black Duck Project Version : " + blackDuckIssueFieldTemplate.getProjectVersionName());
            logger.debug("Black Duck Component Name : " + blackDuckIssueFieldTemplate.getComponentName());
            logger.debug("Black Duck Component Version : " + blackDuckIssueFieldTemplate.getComponentVersionName());
            if (blackDuckIssueModel.isPolicy()) {
                logger.debug("Black Duck Rule Name : " + blackDuckIssueFieldTemplate.getPolicyRuleName());
            }
            return null;
        }
    }

    private ExistenceAwareIssue updateIssueIfExists(final BlackDuckIssueModel blackDuckIssueModel) {
        final Issue existingIssue = findIssueAndUpdateModel(blackDuckIssueModel);
        if (existingIssue == null) {
            // TODO do this this when Black Duck supports per-policy status information on bom components
            // return openIssue(blackDuckIssueModel);
            return null;
        }
        if (!blockStateChange(existingIssue, blackDuckIssueModel)) {
            updateBlackDuckFieldsAndDescription(blackDuckIssueModel);
        }
        return null;
    }

    private boolean blockStateChange(final Issue issue, final BlackDuckIssueModel blackDuckIssueModel) {
        final String issueStatusName = issue.getStatus().getName();
        // TODO this happens even if the ticket is closed
        if (checkIfAlreadyProcessedAndUpdateLastBatch(blackDuckIssueModel)) {
            logger.debug("This issue has already been updated; plugin will not change issue's state");
        } else if (!issueUsesBdsWorkflow(issue)) {
            logger.debug("This is not the BDS workflow; plugin will not change issue's state");
        } else if (BlackDuckJiraConstants.BLACKDUCK_WORKFLOW_STATUS_CLOSED.equals(issueStatusName)) {
            logger.debug("This issue has been closed; plugin will not change issue's state");
        } else if (BlackDuckJiraConstants.BLACKDUCK_WORKFLOW_STATUS_RESOLVED.equals(issueStatusName)) {
            logger.debug("This issue is already Resolved; plugin will not change issue's state");
        } else {
            return false;
        }
        return true;
    }

    private void addComment(final BlackDuckIssueModel blackDuckIssueModel, final String comment, final Issue issue) {
        final String issueKey = issue.getKey();
        if (!pluginConfigurationDetails.isCommentOnIssueUpdates()) {
            logger.debug(String.format("Will not add a comment to issue %s because the plugin has been configured to not comment on issue updates", issueKey));
            return;
        }
        logger.debug(String.format("Attempting to add comment to %s: %s", issueKey, comment));
        if (StringUtils.isNotBlank(comment) && !checkIfAlreadyProcessedAndUpdateLastBatch(blackDuckIssueModel)) {
            final String newCommentKey = String.valueOf(comment.hashCode());
            final String storedLastCommentKey = issueServiceWrapper.getIssueProperty(issue.getId(), BlackDuckJiraConstants.BLACKDUCK_JIRA_ISSUE_LAST_COMMENT_KEY);
            if (storedLastCommentKey != null && newCommentKey.equals(storedLastCommentKey)) {
                // This comment would be a duplicate of the previous one, so there is no need to add it.
                logger.debug("Ignoring a comment that would be an exact duplicate of the previous comment.");
                return;
            }
            issueServiceWrapper.addComment(issue, comment);
            try {
                issueServiceWrapper.addIssuePropertyJson(blackDuckIssueModel.getJiraIssueId(), BlackDuckJiraConstants.BLACKDUCK_JIRA_ISSUE_LAST_COMMENT_KEY, newCommentKey);
            } catch (final JiraIssueException e) {
                handleJiraIssueException(e, blackDuckIssueModel);
            }
        }
    }

    private void updateIssueTrackerProperties(final BlackDuckIssueModel blackDuckIssueModel, final Issue issue) {
        final String blackDuckIssueUrl = blackDuckIssueTrackerHandler.createBlackDuckIssue(blackDuckIssueModel.getComponentIssueUrl(), issue);
        if (StringUtils.isNotBlank(blackDuckIssueUrl)) {
            final BlackDuckIssueTrackerProperties issueTrackerProperties = new BlackDuckIssueTrackerProperties(blackDuckIssueUrl, blackDuckIssueModel.getJiraIssueId());
            try {
                final String key = blackDuckIssueTrackerPropertyHandler.createEntityPropertyKey(blackDuckIssueModel.getJiraIssueId());
                issueServiceWrapper.addProjectProperty(blackDuckIssueModel.getJiraIssueFieldTemplate().getProjectId(), key, issueTrackerProperties);
            } catch (final JiraIssueException e) {
                handleJiraIssueException(e, blackDuckIssueModel);
            }
        }
    }

    private void updateDefaultIssueProperties(final BlackDuckIssueModel blackDuckIssueModel) {
        final BlackDuckIssueFieldTemplate blackDuckIssueFieldTemplate = blackDuckIssueModel.getBlackDuckIssueFieldTemplate();
        final IssueCategory category = blackDuckIssueFieldTemplate.getIssueCategory();
        if (!IssueCategory.SPECIAL.equals(category)) {
            final IssueProperties properties = new IssueProperties(category, blackDuckIssueModel.getBomComponentUri(), blackDuckIssueFieldTemplate.getPolicyRuleName(), blackDuckIssueModel.getJiraIssueId());
            logger.debug("Setting default properties on issue: " + properties);
            try {
                issueServiceWrapper.addIssueProperties(blackDuckIssueModel.getJiraIssueId(), blackDuckIssueModel.getBomComponentUri(), properties);
            } catch (final JiraIssueException e) {
                handleJiraIssueException(e, blackDuckIssueModel);
            }
        }
    }

    private Issue findIssueAndUpdateModel(final BlackDuckIssueModel blackDuckIssueModel) {
        try {
            Issue foundIssue = findIssueByBomComponentUri(blackDuckIssueModel);
            if (foundIssue != null) {
                blackDuckIssueModel.setJiraIssueId(foundIssue.getId());
            } else {
                foundIssue = issueServiceWrapper.findIssueByContentKey(blackDuckIssueModel.getEventKey());
                if (foundIssue != null) {
                    blackDuckIssueModel.setJiraIssueId(foundIssue.getId());
                    updateDefaultIssueProperties(blackDuckIssueModel);
                }
            }
            return foundIssue;
        } catch (final JiraIssueException e) {
            handleJiraIssueException(e, blackDuckIssueModel);
        }
        return null;
    }

    private Issue findIssueByBomComponentUri(final BlackDuckIssueModel blackDuckIssueModel) throws JiraIssueException {
        final List<IssueProperties> propertyCandidates = findIssuePropertiesByBomComponentUri(blackDuckIssueModel.getBomComponentUri());
        for (final IssueProperties candidate : propertyCandidates) {
            final Long candidateIssueId = candidate.getJiraIssueId();
            final IssueCategory candidateIssueType = candidate.getType();

            final BlackDuckIssueFieldTemplate blackDuckIssueFieldTemplate = blackDuckIssueModel.getBlackDuckIssueTemplate();
            if (candidateIssueType.equals(blackDuckIssueFieldTemplate.getIssueCategory())) {
                if (IssueCategory.VULNERABILITY.equals(candidateIssueType)) {
                    // There should only be one vulnerability issue per bomComponent
                    return issueServiceWrapper.getIssue(candidateIssueId);
                } else if (IssueCategory.POLICY.equals(candidateIssueType)) {
                    final Optional<String> optionalRuleName = candidate.getRuleName();
                    if (optionalRuleName.isPresent() && optionalRuleName.get().equals(blackDuckIssueFieldTemplate.getPolicyRuleName())) {
                        return issueServiceWrapper.getIssue(candidateIssueId);
                    }
                }
            }
        }
        return null;
    }

    private List<IssueProperties> findIssuePropertiesByBomComponentUri(final String bomComponentUri) throws JiraIssueException {
        if (bomComponentUri != null) {
            return issueServiceWrapper.findIssuePropertiesByBomComponentUri(bomComponentUri);
        }
        return Collections.emptyList();
    }

    private Issue createIssue(final BlackDuckIssueModel blackDuckIssueModel) {
        try {
            blackDuckIssueModel.getJiraIssueFieldTemplate().setApplyDefaultValuesWhenParameterNotProvided(true);
            blackDuckIssueModel.getJiraIssueFieldTemplate().setRetainExistingValuesWhenParameterNotProvided(true);
            return issueServiceWrapper.createIssue(blackDuckIssueModel);
        } catch (final JiraIssueException e) {
            handleJiraIssueException(e, blackDuckIssueModel);
        }
        return null;
    }

    private Issue updateBlackDuckFieldsAndDescription(final BlackDuckIssueModel blackDuckIssueModel) {
        try {
            blackDuckIssueModel.getJiraIssueFieldTemplate().setApplyDefaultValuesWhenParameterNotProvided(false);
            blackDuckIssueModel.getJiraIssueFieldTemplate().setRetainExistingValuesWhenParameterNotProvided(true);
            return issueServiceWrapper.updateIssue(blackDuckIssueModel);
        } catch (final JiraIssueException e) {
            handleJiraIssueException(e, blackDuckIssueModel);
        }
        return null;
    }

    private Issue transitionIssue(final BlackDuckIssueModel blackDuckIssueModel, final Issue issueToTransition, final String stepName, final String newExpectedStatus) {
        final BlackDuckIssueFieldTemplate blackDuckIssueFieldTemplate = blackDuckIssueModel.getBlackDuckIssueFieldTemplate();

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
            updateJiraSettings(issueToTransition, blackDuckIssueFieldTemplate, errorMessage);
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
                handleJiraIssueException(e, blackDuckIssueModel);
            }
        } else {
            final String errorMessage = "Could not find the action : " + stepName + " to transition this issue: " + issueToTransition.getKey();
            updateJiraSettings(issueToTransition, blackDuckIssueFieldTemplate, errorMessage);
        }
        return null;
    }

    private void updateJiraSettings(final Issue issueToTransition, final BlackDuckIssueFieldTemplate blackDuckIssueFieldTemplate, final String errorMessage) {
        logger.error(errorMessage);
        final Project jiraProject = issueToTransition.getProjectObject();
        jiraSettingsService.addBlackDuckError(errorMessage, blackDuckIssueFieldTemplate.getProjectName(), blackDuckIssueFieldTemplate.getProjectVersionName(), jiraProject.getName(), jiraUserContext.getJiraAdminUser().getUsername(),
            jiraUserContext.getDefaultJiraIssueCreatorUser().getUsername(), "transitionIssue");
    }

    private boolean issueUsesBdsWorkflow(final Issue issue) {
        final JiraWorkflow issueWorkflow = issueServiceWrapper.getWorkflow(issue);
        if (issueWorkflow != null) {
            logger.debug("Issue " + issue.getKey() + " uses workflow " + issueWorkflow.getName());
            return issueWorkflow.getName().contains(BlackDuckJiraConstants.BLACKDUCK_JIRA_WORKFLOW);
        }
        return false;
    }

    private boolean checkIfAlreadyProcessedAndUpdateLastBatch(final BlackDuckIssueModel blackDuckIssueModel) {
        final Date eventBatchStartDate = blackDuckIssueModel.getLastBatchStartDate();
        if (eventBatchStartDate != null) {
            final String lastBatchStartKey = issueServiceWrapper.getIssueProperty(blackDuckIssueModel.getJiraIssueId(), BlackDuckJiraConstants.BLACKDUCK_JIRA_ISSUE_LAST_BATCH_START_KEY);
            if (lastBatchStartKey != null && isAlreadyProcessed(lastBatchStartKey, eventBatchStartDate)) {
                // This issue has already been updated by a notification within the same startDate range, but outside of this batch (i.e. we
                // already processed this notification at some point with a different instance of this class, perhaps on a different thread).
                logger.debug("Ignoring a notification that has already been processed: eventKey=" + blackDuckIssueModel.getEventKey());
                return true;
            }
            addLastBatchStartKeyToIssue(blackDuckIssueModel);
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

    private void addLastBatchStartKeyToIssue(final BlackDuckIssueModel blackDuckIssueModel) {
        final Date eventBatchStartDate = blackDuckIssueModel.getLastBatchStartDate();
        if (eventBatchStartDate != null) {
            final String newBatchStartKey = getTimeString(eventBatchStartDate) + getTimeString(instanceUniqueDate);
            try {
                issueServiceWrapper.addIssuePropertyJson(blackDuckIssueModel.getJiraIssueId(), BlackDuckJiraConstants.BLACKDUCK_JIRA_ISSUE_LAST_BATCH_START_KEY, newBatchStartKey);
            } catch (final JiraIssueException e) {
                handleJiraIssueException(e, blackDuckIssueModel);
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

    private void handleJiraIssueException(final JiraIssueException issueException, final BlackDuckIssueModel blackDuckIssueModel) {
        final ApplicationUser issueCreator = blackDuckIssueModel.getJiraIssueFieldTemplate().getIssueCreator();
        final BlackDuckIssueFieldTemplate blackDuckIssueFieldTemplate = blackDuckIssueModel.getBlackDuckIssueTemplate();
        handleJiraIssueException(issueException, blackDuckIssueFieldTemplate.getProjectName(), blackDuckIssueFieldTemplate.getProjectVersionName(), blackDuckIssueModel.getJiraIssueFieldTemplate().getProjectName(),
            jiraUserContext.getJiraAdminUser().getUsername(), issueCreator.getUsername());
    }

    private void handleJiraIssueException(final JiraIssueException issueException, final String blackDuckProjectName, final String blackDuckProjectVersionName, final String jiraProjectName, final String jiraAdminUsername,
        final String jiraIssueCreatorUsername) {
        final String exceptionMessage = issueException.getMessage();
        final String methodAttempt = issueException.getMethodAttempt();
        final ErrorCollection errorCollection = issueException.getErrorCollection();
        if (errorCollection.hasAnyErrors()) {
            logger.error("Error on: " + methodAttempt);
            for (final Entry<String, String> error : errorCollection.getErrors().entrySet()) {
                final String errorMessage = error.getKey() + " / " + error.getValue();
                logger.error(errorMessage);
                jiraSettingsService.addBlackDuckError(errorMessage, blackDuckProjectName, blackDuckProjectVersionName, jiraProjectName, jiraAdminUsername, jiraIssueCreatorUsername, methodAttempt);
            }
            for (final String errorMessage : errorCollection.getErrorMessages()) {
                logger.error(errorMessage);
                jiraSettingsService.addBlackDuckError(errorMessage, blackDuckProjectName, blackDuckProjectVersionName, jiraProjectName, jiraAdminUsername, jiraIssueCreatorUsername, methodAttempt);
            }
        } else if (exceptionMessage != null) {
            jiraSettingsService.addBlackDuckError(exceptionMessage, blackDuckProjectName, blackDuckProjectVersionName, jiraProjectName, jiraAdminUsername, jiraIssueCreatorUsername, methodAttempt);
        } else {
            jiraSettingsService.addBlackDuckError(issueException, blackDuckProjectName, blackDuckProjectVersionName, jiraProjectName, jiraAdminUsername, jiraIssueCreatorUsername, methodAttempt);
        }
    }

    private void addComponentReviewerAsWatcher(final Issue issue, final BlackDuckIssueModel model) {
        if (null != issue && pluginConfigurationDetails.isProjectReviewerEnabled()) {
            ApplicationUser componentReviewer = model.getBlackDuckIssueTemplate().getComponentReviewer();
            if (null != componentReviewer) {
                issueServiceWrapper.addWatcher(issue, componentReviewer);
            }
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
