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

    private final JiraSettingsService jiraSettingsService;

    public HubIssueTrackerHandler(final JiraSettingsService jiraSettingsService) {
        this.jiraSettingsService = jiraSettingsService;
    }

    public void createHubIssue(final EventData eventData, final Issue jiraIssue) {
        if (StringUtils.isEmpty(eventData.getHubComponentVersionUrl())) {
            createIssueForComponentVersion(eventData, jiraIssue);
        } else if (StringUtils.isEmpty(eventData.getHubComponentUrl())) {
            createIssueForComponent(eventData, jiraIssue);
        } else {
            final String message = "Error creating hub issue; no component or component version found.";
            logger.error(message);
            jiraSettingsService.addHubError(message, "createHubIssue");
        }
    }

    public void updateHubIssue(final EventData eventData, final Issue jiraIssue) {
        if (StringUtils.isEmpty(eventData.getHubComponentVersionUrl())) {
            updateIssueForComponentVersion(eventData, jiraIssue);
        } else if (StringUtils.isEmpty(eventData.getHubComponentUrl())) {
            updateIssueForComponent(eventData, jiraIssue);
        } else {
            final String message = "Error updating hub issue; no component or component version found.";
            logger.error(message);
            jiraSettingsService.addHubError(message, "updateHubIssue");
        }
    }

    public void deleteHubIssue(final EventData eventData, final Issue jiraIssue) {
        if (StringUtils.isEmpty(eventData.getHubComponentVersionUrl())) {
            deleteIssueForComponentVersion(eventData, jiraIssue);
        } else if (StringUtils.isEmpty(eventData.getHubComponentUrl())) {
            deleteIssueForComponent(eventData, jiraIssue);
        } else {
            final String message = "Error deleting hub issue; no component or component version found.";
            logger.error(message);
            jiraSettingsService.addHubError(message, "deleteHubIssue");
        }
    }

    private void createIssueForComponent(final EventData eventData, final Issue jiraIssue) {
        logger.error("##### CREATING HUB ISSUE FOR COMPONENT");
        final Map<String, String> hubIssue = convertJiraIssueToHubIssue(eventData, jiraIssue);
    }

    private void updateIssueForComponent(final EventData eventData, final Issue jiraIssue) {
        logger.error("##### UPDATING HUB ISSUE FOR COMPONENT");
        final Map<String, String> hubIssue = convertJiraIssueToHubIssue(eventData, jiraIssue);
    }

    private void deleteIssueForComponent(final EventData eventData, final Issue jiraIssue) {
        logger.error("##### DELETING HUB ISSUE FOR COMPONENT");
        final Map<String, String> hubIssue = convertJiraIssueToHubIssue(eventData, jiraIssue);
    }

    private void createIssueForComponentVersion(final EventData eventData, final Issue jiraIssue) {
        logger.error("##### CREATING HUB ISSUE FOR COMPONENT VERSION");
        final Map<String, String> hubIssue = convertJiraIssueToHubIssue(eventData, jiraIssue);
    }

    private void updateIssueForComponentVersion(final EventData eventData, final Issue jiraIssue) {
        logger.error("##### UPDATING HUB ISSUE FOR COMPONENT VERSION");
        final Map<String, String> hubIssue = convertJiraIssueToHubIssue(eventData, jiraIssue);
    }

    private void deleteIssueForComponentVersion(final EventData eventData, final Issue jiraIssue) {
        logger.error("##### DELETING HUB ISSUE FOR COMPONENT VERSION");
        final Map<String, String> hubIssue = convertJiraIssueToHubIssue(eventData, jiraIssue);
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
