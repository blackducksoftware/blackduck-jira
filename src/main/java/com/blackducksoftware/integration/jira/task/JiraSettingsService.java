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
package com.blackducksoftware.integration.jira.task;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.config.TicketCreationError;

public class JiraSettingsService {

    private final PluginSettings settings;

    public JiraSettingsService(final PluginSettings settings) {
        this.settings = settings;
    };

    public void addHubError(final Throwable throwable, final String methodAttempt) {
        addHubError(throwable, null, null, null, null, methodAttempt);
    }

    public void addHubError(final String errorMessage, final String methodAttempt) {
        addHubError(errorMessage, null, null, null, null, methodAttempt);
    }

    public void addHubError(final Throwable throwable, final String hubProject, final String hubProjectVersion,
            final String jiraProject, final String jiraUser, final String methodAttempt) {
        final StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        addHubError(sw.toString(), hubProject, hubProjectVersion, jiraProject, jiraUser, methodAttempt);
    }

    public void addHubError(final String errorMessage, final String hubProject, final String hubProjectVersion,
            final String jiraProject, final String jiraUser, final String methodAttempt) {
        List<TicketCreationError> ticketErrors = expireOldErrors(settings);
        if (ticketErrors == null) {
            ticketErrors = new ArrayList<>();
        }

        final StringBuilder suffixBuilder = new StringBuilder();
        if (StringUtils.isNotBlank(hubProject)) {
            suffixBuilder.append("Hub Project : ");
            suffixBuilder.append(hubProject);
            suffixBuilder.append(" / ");
        }
        if (StringUtils.isNotBlank(hubProjectVersion)) {
            suffixBuilder.append("Version : ");
            suffixBuilder.append(hubProjectVersion);
            suffixBuilder.append(" / ");
        }
        if (StringUtils.isNotBlank(jiraProject)) {
            suffixBuilder.append("JIRA Project : ");
            suffixBuilder.append(jiraProject);
            suffixBuilder.append(" / ");
        }
        if (StringUtils.isNotBlank(jiraUser)) {
            suffixBuilder.append("JIRA User : ");
            suffixBuilder.append(jiraUser);
            suffixBuilder.append(" / ");
        }
        suffixBuilder.append("Method : ");
        suffixBuilder.append(methodAttempt);

        final StringBuilder finalErrorBuilder = new StringBuilder();
        finalErrorBuilder.append(errorMessage.trim());
        finalErrorBuilder.append("\n");
        finalErrorBuilder.append(suffixBuilder.toString());

        TicketCreationError error = new TicketCreationError();
        error.setStackTrace(finalErrorBuilder.toString());
        error.setTimeStamp(DateTime.now().toString(TicketCreationError.ERROR_TIME_FORMAT));

        ticketErrors.add(error);

        final int maxErrorSize = 20;
        if (ticketErrors.size() > maxErrorSize) {
            Collections.sort(ticketErrors);
            ticketErrors.subList(maxErrorSize, ticketErrors.size()).clear();
        }
        settings.put(HubJiraConstants.HUB_JIRA_ERROR, TicketCreationError.toJson(ticketErrors));
    }

    public static List<TicketCreationError> expireOldErrors(final PluginSettings pluginSettings) {
        final Object errorObject = pluginSettings.get(HubJiraConstants.HUB_JIRA_ERROR);
        if (errorObject != null) {
            List<TicketCreationError> ticketErrors = null;
            String ticketErrorsString = (String) errorObject;
            try {
                ticketErrors = TicketCreationError.fromJson(ticketErrorsString);
            } catch (Exception e) {
                ticketErrors = new ArrayList<>();
            }
            if (ticketErrors != null && !ticketErrors.isEmpty()) {
                Collections.sort(ticketErrors);
                final DateTime currentTime = DateTime.now();
                final Iterator<TicketCreationError> expirationIterator = ticketErrors.iterator();
                while (expirationIterator.hasNext()) {
                    final TicketCreationError ticketError = expirationIterator.next();
                    DateTime errorTime = ticketError.getTimeStampDateTime();
                    if (Days.daysBetween(errorTime, currentTime).isGreaterThan(Days.days(30))) {
                        expirationIterator.remove();
                    }
                }
                pluginSettings.put(HubJiraConstants.HUB_JIRA_ERROR, TicketCreationError.toJson(ticketErrors));
                return ticketErrors;
            }
        }
        return null;
    }

}
