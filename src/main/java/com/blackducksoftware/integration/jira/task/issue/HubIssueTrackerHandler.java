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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.atlassian.jira.issue.Issue;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.task.JiraSettingsService;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventData;

public class HubIssueTrackerHandler {
    private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));

    public final static String JIRA_ISSUE_PROPERTY_HUB_ISSUE_URL = "bdsHubIssueURL";

    private final JiraSettingsService jiraSettingsService;

    public HubIssueTrackerHandler(final JiraSettingsService jiraSettingsService) {
        this.jiraSettingsService = jiraSettingsService;
    }

    public String createHubIssue(final EventData eventData, final Issue jiraIssue) {
        String url = "";
        if (StringUtils.isEmpty(eventData.getHubComponentVersionUrl())) {
            url = createIssueForComponentVersion(eventData, jiraIssue);
        } else if (StringUtils.isEmpty(eventData.getHubComponentUrl())) {
            url = createIssueForComponent(eventData, jiraIssue);
        } else {
            final String message = "Error creating hub issue; no component or component version found.";
            logger.error(message);
            jiraSettingsService.addHubError(message, "createHubIssue");
        }

        return url;
    }

    public void updateHubIssue(final String hubIssueUrl, final Issue jiraIssue) {
        if (StringUtils.isNotBlank(hubIssueUrl)) {
            logger.debug(String.format("Updating issue %s from hub for jira issue %s-%s", hubIssueUrl, jiraIssue.getProjectObject().getName(),
                    jiraIssue.getNumber()));
        } else {
            final String message = "Error updating hub issue; no component or component version found.";
            logger.error(message);
            jiraSettingsService.addHubError(message, "updateHubIssue");
        }
    }

    public void deleteHubIssue(final String hubIssueUrl, final Issue jiraIssue) {
        if (StringUtils.isNotBlank(hubIssueUrl)) {
            logger.debug(String.format("Deleting issue %s from hub for jira issue %s-%s", hubIssueUrl, jiraIssue.getProjectObject().getName(),
                    jiraIssue.getNumber()));
        } else {
            final String message = "Error deleting hub issue; no component or component version found.";
            logger.error(message);
            jiraSettingsService.addHubError(message, "deleteHubIssue");
        }
    }

    private String createIssueForComponent(final EventData eventData, final Issue jiraIssue) {
        logger.error("##### CREATING HUB ISSUE FOR COMPONENT");
        final Map<String, String> hubIssue = convertJiraIssueToHubIssue(eventData, jiraIssue);
        return "Test hub issue component URL";
    }

    private String createIssueForComponentVersion(final EventData eventData, final Issue jiraIssue) {
        logger.error("##### CREATING HUB ISSUE FOR COMPONENT VERSION");
        final Map<String, String> hubIssue = convertJiraIssueToHubIssue(eventData, jiraIssue);
        return "Test hub issue component version URL";
    }

    private Map<String, String> convertJiraIssueToHubIssue(final EventData eventData, final Issue jiraIssue) {
        final Map<String, String> dataMap = new HashMap<>();
        logHubIssueData(dataMap);
        return dataMap;
    }

    private void logHubIssueData(final Map<String, String> hubIssue) {
        logger.error("Begin Hub Issue data___________");
        for (final Map.Entry<String, String> entry : hubIssue.entrySet()) {
            logger.error(String.format("Key: %s, Value: %s", entry.getKey(), entry.getValue()));
        }
        logger.error("End Hub Issue data___________");
    }
}
