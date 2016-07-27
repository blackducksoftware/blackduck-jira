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
package com.blackducksoftware.integration.jira.hub;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.blackducksoftware.integration.jira.HubJiraLogger;
import com.blackducksoftware.integration.jira.config.HubProjectMapping;
import com.blackducksoftware.integration.jira.hub.model.notification.NotificationItem;
import com.blackducksoftware.integration.jira.hub.property.IssueProperties;
import com.blackducksoftware.integration.jira.issue.JiraIssueHandler;

/**
 * Collects recent notifications from the Hub, and generates JIRA tickets for
 * them.
 *
 * @author sbillings
 *
 */
public class TicketGenerator {
	public static final String DONE_STATUS = "Done";
	public static final String REOPEN_STATUS = "Reopen";

	private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));
	private final HubNotificationService notificationService;
	private final TicketGeneratorInfo ticketGenInfo;


	public TicketGenerator(final HubNotificationService notificationService,
			final TicketGeneratorInfo ticketGenInfo) {
		this.notificationService = notificationService;
		this.ticketGenInfo = ticketGenInfo;
	}

	public int generateTicketsForRecentNotifications(final Set<HubProjectMapping> hubProjectMappings,
			final List<String> linksOfRulesToMonitor,
			final NotificationDateRange notificationDateRange) throws HubNotificationServiceException {

		final List<NotificationItem> notifs = notificationService.fetchNotifications(notificationDateRange);
		for (final NotificationItem notification : notifs) {
			logger.debug(notification.toString());
		}
		final JiraNotificationProcessor processor = new JiraNotificationProcessor(notificationService,
				hubProjectMappings, linksOfRulesToMonitor, ticketGenInfo);

		final FilteredNotificationResults notificationResults = processor.extractJiraReadyNotifications(notifs);

		final JiraIssueHandler issueHandler = new JiraIssueHandler(ticketGenInfo);

		int ticketCount = 0;
		for (final FilteredNotificationResult notificationResult : notificationResults.getPolicyViolationResults()) {
			createIssue(issueHandler, notificationResult);
			ticketCount++;
		}
		for (final FilteredNotificationResult notificationResult : notificationResults
				.getPolicyViolationOverrideResults()) {
			closeIssue(issueHandler, notificationResult);
		}

		// TODO can this be combined with the rule issue create loop above
		for (final FilteredNotificationResult vulnerabilityResult : notificationResults.getVulnerabilityResults()) {
			createIssue(issueHandler, vulnerabilityResult);
			ticketCount++;
		}
		return ticketCount;
	}

	private void createIssue(final JiraIssueHandler issueHandler,
			final FilteredNotificationResult notificationResult) {
		logger.debug("Setting logged in User : " + ticketGenInfo.getJiraUser().getDisplayName());
		logger.debug("User active : " + ticketGenInfo.getJiraUser().isActive());

		ticketGenInfo.getAuthContext().setLoggedInUser(ticketGenInfo.getJiraUser());

		final IssueInputParameters issueInputParameters =
				ticketGenInfo.getIssueService()
				.newIssueInputParameters();
		issueInputParameters.setProjectId(notificationResult.getJiraProjectId())
		.setIssueTypeId(notificationResult.getJiraIssueTypeId())
		.setSummary(notificationResult.getIssueSummary())
		.setReporterId(notificationResult.getJiraUserName())
		.setDescription(notificationResult.getIssueDescription());

		final Issue oldIssue = issueHandler.findIssue(notificationResult);
		if (oldIssue == null) {
			final Issue issue = issueHandler.createIssue(issueInputParameters);
			if (issue != null) {
				logger.info("Created new Issue.");
				issueHandler.printIssueInfo(issue);

				final IssueProperties properties = notificationResult.createIssueProperties(issue);
				logger.debug("Adding properties to created issue: " + properties);
				issueHandler.addIssueProperty(issue.getId(), notificationResult.getUniquePropertyKey(), properties);
			}
		} else {
			if (oldIssue.getStatusObject().getName().equals(DONE_STATUS)) {
				issueHandler.transitionIssue(oldIssue, REOPEN_STATUS);
				logger.info("Re-opened the already exisiting issue.");
				issueHandler.printIssueInfo(oldIssue);
			} else {
				logger.info("This issue already exists.");
				issueHandler.printIssueInfo(oldIssue);
			}
		}
	}

	private void closeIssue(final JiraIssueHandler issueHandler,
			final FilteredNotificationResult notificationResult) {
		final Issue oldIssue = issueHandler.findIssue(notificationResult);
		if (oldIssue != null) {
			final Issue updatedIssue = issueHandler.transitionIssue(oldIssue, DONE_STATUS);
			if (updatedIssue != null) {
				logger.info("Closed the issue based on an override.");
				issueHandler.printIssueInfo(updatedIssue);
			}
		} else {
			logger.info("Could not find an existing issue to close for this override.");
			logger.debug("Hub Project Name : " + notificationResult.getHubProjectName());
			logger.debug("Hub Project Version : " + notificationResult.getHubProjectVersion());
			logger.debug("Hub Component Name : " + notificationResult.getHubComponentName());
			logger.debug("Hub Component Version : " + notificationResult.getHubComponentVersion());
			if (notificationResult instanceof FilteredNotificationResultRule) {
				final FilteredNotificationResultRule notificationResultRule = (FilteredNotificationResultRule) notificationResult;
				logger.debug("Hub Rule Name : " + notificationResultRule.getRule().getName());
			}
		}
	}


}
