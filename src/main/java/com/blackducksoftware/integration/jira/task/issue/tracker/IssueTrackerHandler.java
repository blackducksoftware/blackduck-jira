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
package com.blackducksoftware.integration.jira.task.issue.tracker;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.Issue;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.config.JiraSettingsService;
import com.synopsys.integration.blackduck.api.generated.view.IssueView;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.exception.IntegrationRestException;

public class IssueTrackerHandler {
    public final static String JIRA_ISSUE_PROPERTY_BLACKDUCK_ISSUE_URL = "bdsHubIssueURL";
    public final static String USER_NOT_ASSIGNED = "Not Assigned";

    private final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final JiraSettingsService jiraSettingsService;
    private final BlackDuckService blackDuckService;

    public IssueTrackerHandler(final JiraSettingsService jiraSettingsService, final BlackDuckService blackDuckService) {
        this.jiraSettingsService = jiraSettingsService;
        this.blackDuckService = blackDuckService;
    }

    public static final String createEntityPropertyKey(final Long jiraIssueId) {
        return String.format("%s_%s", IssueTrackerHandler.JIRA_ISSUE_PROPERTY_BLACKDUCK_ISSUE_URL, jiraIssueId);
    }

    public String createBlackDuckIssue(final String blackDuckIssueUrl, final Issue jiraIssue) {
        String url = "";
        try {
            if (StringUtils.isNotBlank(blackDuckIssueUrl)) {
                url = blackDuckService.post(blackDuckIssueUrl, updateBlackDuckIssueView(jiraIssue, new IssueView()));
            } else {
                final String message = "Error creating Black Duck issue; no component or component version found.";
                logger.error(message);
                jiraSettingsService.addBlackDuckError(message, "createBlackDuckIssue");
            }
        } catch (final IntegrationException ex) {
            logger.error("Error creating Black Duck Issue", ex);
            jiraSettingsService.addBlackDuckError(ex, "createBlackDuckIssue");
        }

        return url;
    }

    public void updateBlackDuckIssue(final String blackDuckIssueUrl, final Issue jiraIssue) {
        try {
            try {
                if (StringUtils.isNotBlank(blackDuckIssueUrl)) {
                    logger.debug(String.format("Updating issue %s from Black Duck for jira issue %s", blackDuckIssueUrl, jiraIssue.getKey()));
                    final Optional<IssueView> foundBlackduckIssue = findComponentIssue(jiraIssue, blackDuckIssueUrl);
                    if (foundBlackduckIssue.isPresent()) {
                        final IssueView issueView = foundBlackduckIssue.get();
                        // FIXME: need to remove this code when Black Duck fixes the error where Black Duck sets the href field to null for component issues.
                        issueView.getMeta().setHref(blackDuckIssueUrl);
                        blackDuckService.put(updateBlackDuckIssueView(jiraIssue, foundBlackduckIssue.get()));
                    } else {
                        logger.debug(String.format("Black Duck Issue not found; cannot update Black Duck for jira issue %s", jiraIssue.getKey()));
                    }
                } else {
                    final String message = "Error updating Black Duck issue; no component or component version found.";
                    logger.error(message);
                    jiraSettingsService.addBlackDuckError(message, "updateBlackDuckIssue");
                }
            } catch (final IntegrationRestException restException) {
                if (restException.getHttpStatusCode() == 404) {
                    logger.debug("The Black Duck issue tracker was not found. The project/version it was associated with was probably deleted.");
                    return;
                }
                throw restException;
            }
        } catch (final IntegrationException intException) {
            logger.error("Error updating Black Duck Issue", intException);
            jiraSettingsService.addBlackDuckError(intException, "updateBlackDuckIssue");
        }
    }

    public void deleteBlackDuckIssue(final String blackDuckIssueUrl, final Issue jiraIssue) {
        try {
            if (StringUtils.isNotBlank(blackDuckIssueUrl)) {
                logger.debug(String.format("Deleting issue %s from Black Duck for jira issue %s", blackDuckIssueUrl, jiraIssue.getKey()));
                blackDuckService.delete(blackDuckIssueUrl);
            } else {
                final String message = "Error deleting Black Duck issue; no component or component version found.";
                logger.error(message);
                jiraSettingsService.addBlackDuckError(message, "deleteBlackDuckIssue");
            }
        } catch (final IntegrationException ex) {
            logger.error("Error deleting Black Duck Issue", ex);
            jiraSettingsService.addBlackDuckError(ex, "deleteBlackDuckIssue");
        }
    }

    private IssueView updateBlackDuckIssueView(final Issue jiraIssue, final IssueView issueToUpdate) {
        final String issueId = jiraIssue.getKey();

        final String assignee;
        if (jiraIssue.getAssignee() != null) {
            assignee = jiraIssue.getAssignee().getDisplayName();
        } else {
            assignee = USER_NOT_ASSIGNED;
        }

        String status = "";

        if (jiraIssue.getStatus() != null) {
            status = jiraIssue.getStatus().getName();
        }

        issueToUpdate.setIssueId(issueId);
        issueToUpdate.setIssueAssignee(assignee);
        issueToUpdate.setIssueStatus(status);
        issueToUpdate.setIssueCreatedAt(jiraIssue.getCreated());
        issueToUpdate.setIssueUpdatedAt(jiraIssue.getUpdated());
        issueToUpdate.setIssueDescription(jiraIssue.getSummary());
        issueToUpdate.setIssueLink(String.format("%s/browse/%s", getJiraBaseUrl(), jiraIssue.getKey()));
        return issueToUpdate;
    }

    private Optional<IssueView> findComponentIssue(final Issue jiraIssue, final String blackDuckIssueUrl) throws IntegrationException {
        final String issueId = jiraIssue.getKey();
        final Optional<String> projectIssuesUrl = createUrl(blackDuckIssueUrl);
        if (!projectIssuesUrl.isPresent()) {
            return Optional.empty();
        }
        final List<IssueView> issues = getCurrentIssues(projectIssuesUrl.get());
        return issues.stream().filter(issue -> issueId.equals(issue.getIssueId())).findFirst();
    }

    private Optional<String> createUrl(final String blackDuckIssueUrl) {
        final String issuesSuffix = "/components";
        final int indexOfComponents = blackDuckIssueUrl.lastIndexOf(issuesSuffix);
        if (indexOfComponents <= 0) {
            return Optional.empty();
        }
        return Optional.of(String.format("%s/issues", blackDuckIssueUrl.substring(0, indexOfComponents)));
    }

    private List<IssueView> getCurrentIssues(final String projectIssueUrl) throws IntegrationException {
        return blackDuckService.getAllResponses(projectIssueUrl, IssueView.class);
    }

    // TODO find a better way to do this
    private String getJiraBaseUrl() {
        try {
            return ComponentAccessor.getApplicationProperties().getString(APKeys.JIRA_BASEURL);
        } catch (final Exception e) {
            logger.error("Could not get the base url for JIRA", e);
            return "";
        }
    }
}
