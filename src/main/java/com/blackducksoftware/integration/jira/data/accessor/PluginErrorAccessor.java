/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2019 Synopsys, Inc.
 * https://www.synopsys.com/
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
package com.blackducksoftware.integration.jira.data.accessor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;
import com.blackducksoftware.integration.jira.web.TicketCreationError;

public class PluginErrorAccessor {
    private static final Logger logger = LoggerFactory.getLogger(PluginErrorAccessor.class);

    private final JiraSettingsAccessor jiraSettingsAccessor;

    public PluginErrorAccessor(final JiraSettingsAccessor jiraSettingsAccessor) {
        this.jiraSettingsAccessor = jiraSettingsAccessor;
    }

    public void addBlackDuckError(final Throwable throwable, final String methodAttempt) {
        addBlackDuckError(throwable, null, null, null, null, null, methodAttempt);
    }

    public void addBlackDuckError(final String errorMessage, final String methodAttempt) {
        addBlackDuckError(errorMessage, null, null, null, null, null, methodAttempt);
    }

    public void addBlackDuckError(final Throwable throwable, final String blackDuckProjectName, final String blackDuckProjectVersionName, final String jiraProject, final String jiraAdminUsername, final String jiraIssueCreatorUsername,
        final String methodAttempt) {
        final StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        addBlackDuckError(sw.toString(), blackDuckProjectName, blackDuckProjectVersionName, jiraProject, jiraAdminUsername, jiraIssueCreatorUsername, methodAttempt);
    }

    public void addBlackDuckError(final String errorMessage, final String blackDuckProjectName, final String blackDuckProjectVersionName, final String jiraProject, final String jiraAdminUsername, final String jiraIssueCreatorUsername,
        final String methodAttempt) {
        logger.debug("Sending error to UI");
        List<TicketCreationError> ticketErrors = expireOldErrors(jiraSettingsAccessor);
        if (ticketErrors == null) {
            ticketErrors = new ArrayList<>();
        }

        final StringBuilder suffixBuilder = new StringBuilder();
        if (StringUtils.isNotBlank(blackDuckProjectName)) {
            suffixBuilder.append("Black Duck Project : ");
            suffixBuilder.append(blackDuckProjectName);
            suffixBuilder.append(" / ");
        }
        if (StringUtils.isNotBlank(blackDuckProjectVersionName)) {
            suffixBuilder.append("Version : ");
            suffixBuilder.append(blackDuckProjectVersionName);
            suffixBuilder.append(" / ");
        }
        if (StringUtils.isNotBlank(jiraProject)) {
            suffixBuilder.append("JIRA Project : ");
            suffixBuilder.append(jiraProject);
            suffixBuilder.append(" / ");
        }
        if (StringUtils.isNotBlank(jiraAdminUsername)) {
            suffixBuilder.append("JIRA Admin User : ");
            suffixBuilder.append(jiraAdminUsername);
            suffixBuilder.append(" / ");
        }
        if (StringUtils.isNotBlank(jiraIssueCreatorUsername)) {
            suffixBuilder.append("JIRA Issue Creator User : ");
            suffixBuilder.append(jiraIssueCreatorUsername);
            suffixBuilder.append(" / ");
        }
        suffixBuilder.append("Method : ");
        suffixBuilder.append(methodAttempt);

        final StringBuilder finalErrorBuilder = new StringBuilder();
        finalErrorBuilder.append(StringUtils.trimToEmpty(errorMessage));
        finalErrorBuilder.append("\n");
        finalErrorBuilder.append(suffixBuilder.toString());

        final TicketCreationError error = new TicketCreationError();
        error.setStackTrace(finalErrorBuilder.toString());
        error.setTimeStamp(LocalDateTime.now().format(TicketCreationError.ERROR_TIME_FORMAT));
        ticketErrors.add(error);

        final int maxErrorSize = 20;
        if (ticketErrors.size() > maxErrorSize) {
            Collections.sort(ticketErrors);
            ticketErrors.subList(maxErrorSize, ticketErrors.size()).clear();
        }
        logger.info("Saving " + ticketErrors.size() + " error messages to settings");
        jiraSettingsAccessor.setValue(BlackDuckJiraConstants.BLACKDUCK_JIRA_ERROR, TicketCreationError.toJson(ticketErrors));
    }

    public static List<TicketCreationError> expireOldErrors(final JiraSettingsAccessor jiraSettingsAccessor) {
        logger.debug("Pulling error messages from settings");
        final Object errorObject = jiraSettingsAccessor.getObjectValue(BlackDuckJiraConstants.BLACKDUCK_JIRA_ERROR);
        if (errorObject == null) {
            logger.debug("No error messages found in settings");
            return null;
        }
        if (!(errorObject instanceof String)) {
            logger.warn("The error object in settings is invalid (probably stored by an older version of the plugin); discarding it");
            jiraSettingsAccessor.setValue(BlackDuckJiraConstants.BLACKDUCK_JIRA_ERROR, null);
            return null;
        }

        List<TicketCreationError> ticketErrors = null;
        final String ticketErrorsString = (String) errorObject;
        try {
            ticketErrors = TicketCreationError.fromJson(ticketErrorsString);
        } catch (final Exception e) {
            logger.warn("Error deserializing JSON string pulled from settings: " + e.getMessage() + "; resettting error message list");
            jiraSettingsAccessor.setValue(BlackDuckJiraConstants.BLACKDUCK_JIRA_ERROR, null);
            return null;
        }
        if ((ticketErrors == null) || ticketErrors.isEmpty()) {
            logger.debug("No error messages found in settings");
            return null;
        }
        logger.debug("# error messages pulled from settings: " + ticketErrors.size());
        Collections.sort(ticketErrors);
        final LocalDateTime currentTime = LocalDateTime.now();
        final Iterator<TicketCreationError> expirationIterator = ticketErrors.iterator();
        while (expirationIterator.hasNext()) {
            final TicketCreationError ticketError = expirationIterator.next();
            final LocalDateTime errorTime = ticketError.getTimeStampDateTime();
            if (Duration.between(errorTime, currentTime).toDays() > 30L) {
                logger.debug("Removing old error message with timestamp: " + ticketError.getTimeStamp());
                expirationIterator.remove();
            }
        }
        logger.debug("Saving " + ticketErrors.size() + " non-expired error messages in settings");
        jiraSettingsAccessor.setValue(BlackDuckJiraConstants.BLACKDUCK_JIRA_ERROR, TicketCreationError.toJson(ticketErrors));
        return ticketErrors;
    }

}
