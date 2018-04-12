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
package com.blackducksoftware.integration.jira.task.issue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.atlassian.jira.issue.Issue;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.bom.BomComponentIssueRequestService;
import com.blackducksoftware.integration.hub.model.view.IssueView;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.task.JiraSettingsService;

public class HubIssueTrackerHandler {
    private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));

    public final static String USER_NOT_ASSIGNED = "Not Assigned";

    private final JiraServices jiraServices;

    private final JiraSettingsService jiraSettingsService;

    private final BomComponentIssueRequestService issueRequestService;

    private final DateFormat dateFormatter;

    public HubIssueTrackerHandler(final JiraServices jiraServices, final JiraSettingsService jiraSettingsService, final BomComponentIssueRequestService issueRequestService) {
        this.jiraServices = jiraServices;
        this.jiraSettingsService = jiraSettingsService;
        this.issueRequestService = issueRequestService;

        dateFormatter = new SimpleDateFormat(RestConnection.JSON_DATE_FORMAT);
        dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public String createHubIssue(final String hubIssueUrl, final Issue jiraIssue) {
        String url = "";
        try {
            if (StringUtils.isNotBlank(hubIssueUrl)) {
                url = issueRequestService.createIssue(createHubIssueView(jiraIssue), hubIssueUrl);
            } else {
                final String message = "Error creating hub issue; no component or component version found.";
                logger.error(message);
                jiraSettingsService.addHubError(message, "createHubIssue");
            }
        } catch (final IntegrationException ex) {
            logger.error("Error creating Hub Issue", ex);
            jiraSettingsService.addHubError(ex, "createHubIssue");
        }

        return url;
    }

    public void updateHubIssue(final String hubIssueUrl, final Issue jiraIssue) {
        try {
            if (StringUtils.isNotBlank(hubIssueUrl)) {
                logger.debug(String.format("Updating issue %s from hub for jira issue %s", hubIssueUrl, jiraIssue.getKey()));
                issueRequestService.updateIssue(createHubIssueView(jiraIssue), hubIssueUrl);
            } else {
                final String message = "Error updating hub issue; no component or component version found.";
                logger.error(message);
                jiraSettingsService.addHubError(message, "updateHubIssue");
            }
        } catch (final IntegrationException ex) {
            logger.error("Error updating Hub Issue", ex);
            jiraSettingsService.addHubError(ex, "updateHubIssue");
        }
    }

    public void deleteHubIssue(final String hubIssueUrl, final Issue jiraIssue) {
        try {
            if (StringUtils.isNotBlank(hubIssueUrl)) {
                logger.debug(String.format("Deleting issue %s from hub for jira issue %s", hubIssueUrl, jiraIssue.getKey()));
                issueRequestService.deleteIssue(hubIssueUrl);
            } else {
                final String message = "Error deleting hub issue; no component or component version found.";
                logger.error(message);
                jiraSettingsService.addHubError(message, "deleteHubIssue");
            }
        } catch (final IntegrationException ex) {
            logger.error("Error updating Hub Issue", ex);
            jiraSettingsService.addHubError(ex, "deleteHubIssue");
        }
    }

    private IssueView createHubIssueView(final Issue jiraIssue) {
        final IssueView hubIssue = new IssueView();
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

        status = jiraIssue.getStatus().getName();
        final String createdAt = dateFormatter.format(jiraIssue.getCreated());
        final String updatedAt = dateFormatter.format(jiraIssue.getUpdated());
        hubIssue.issueId = issueId;
        hubIssue.issueAssignee = assignee;
        hubIssue.issueStatus = status;
        hubIssue.issueCreatedAt = createdAt;
        hubIssue.issueUpdatedAt = updatedAt;
        hubIssue.issueDescription = jiraIssue.getSummary();
        hubIssue.issueLink = String.format("%s/browse/%s", jiraServices.getJiraBaseUrl(), jiraIssue.getKey());
        return hubIssue;
    }
}
