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

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.Issue;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.config.JiraSettingsService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.hub.api.generated.view.IssueView;
import com.synopsys.integration.hub.service.IssueService;

public class BlackDuckIssueTrackerHandler {
    public final static String USER_NOT_ASSIGNED = "Not Assigned";

    private final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final JiraSettingsService jiraSettingsService;
    private final IssueService blackDuckIssueService;

    public BlackDuckIssueTrackerHandler(final JiraSettingsService jiraSettingsService, final IssueService blackDuckIssueService) {
        this.jiraSettingsService = jiraSettingsService;
        this.blackDuckIssueService = blackDuckIssueService;
    }

    public String createBlackDuckIssue(final String blackDuckIssueUrl, final Issue jiraIssue) {
        String url = "";
        try {
            if (StringUtils.isNotBlank(blackDuckIssueUrl)) {
                url = blackDuckIssueService.createIssue(createBlackDuckIssueView(jiraIssue), blackDuckIssueUrl);
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
            if (StringUtils.isNotBlank(blackDuckIssueUrl)) {
                logger.debug(String.format("Updating issue %s from Black Duck for jira issue %s", blackDuckIssueUrl, jiraIssue.getKey()));
                blackDuckIssueService.updateIssue(createBlackDuckIssueView(jiraIssue), blackDuckIssueUrl);
            } else {
                final String message = "Error updating Black Duck issue; no component or component version found.";
                logger.error(message);
                jiraSettingsService.addBlackDuckError(message, "updateBlackDuckIssue");
            }
        } catch (final IntegrationException ex) {
            logger.error("Error updating Black Duck Issue", ex);
            jiraSettingsService.addBlackDuckError(ex, "updateBlackDuckIssue");
        }
    }

    public void deleteBlackDuckIssue(final String blackDuckIssueUrl, final Issue jiraIssue) {
        try {
            if (StringUtils.isNotBlank(blackDuckIssueUrl)) {
                logger.debug(String.format("Deleting issue %s from Black Duck for jira issue %s", blackDuckIssueUrl, jiraIssue.getKey()));
                blackDuckIssueService.deleteIssue(blackDuckIssueUrl);
            } else {
                final String message = "Error deleting Black Duck issue; no component or component version found.";
                logger.error(message);
                jiraSettingsService.addBlackDuckError(message, "deleteBlackDuckIssue");
            }
        } catch (final IntegrationException ex) {
            logger.error("Error updating Black Duck Issue", ex);
            jiraSettingsService.addBlackDuckError(ex, "deleteBlackDuckIssue");
        }
    }

    private IssueView createBlackDuckIssueView(final Issue jiraIssue) {
        final IssueView blackDuckIssue = new IssueView();
        final String issueId = jiraIssue.getKey();
        String assignee = "";

        if (jiraIssue.getAssignee() != null) {
            assignee = jiraIssue.getAssignee().getDisplayName();
        } else {
            assignee = USER_NOT_ASSIGNED;
        }

        String status = "";

        if (jiraIssue.getStatus() != null) {
            status = jiraIssue.getStatus().getName();
        }

        blackDuckIssue.issueId = issueId;
        blackDuckIssue.issueAssignee = assignee;
        blackDuckIssue.issueStatus = status;
        blackDuckIssue.issueCreatedAt = jiraIssue.getCreated();
        blackDuckIssue.issueUpdatedAt = jiraIssue.getUpdated();
        blackDuckIssue.issueDescription = jiraIssue.getSummary();
        blackDuckIssue.issueLink = String.format("%s/browse/%s", getJiraBaseUrl(), jiraIssue.getKey());
        return blackDuckIssue;
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
