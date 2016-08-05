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
package com.blackducksoftware.integration.jira.task;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.EncryptionException;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.item.HubItemsService;
import com.blackducksoftware.integration.hub.notification.NotificationDateRange;
import com.blackducksoftware.integration.hub.notification.NotificationService;
import com.blackducksoftware.integration.hub.notification.NotificationServiceException;
import com.blackducksoftware.integration.hub.notification.api.NotificationItem;
import com.blackducksoftware.integration.hub.notification.api.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.hub.notification.api.RuleViolationNotificationItem;
import com.blackducksoftware.integration.hub.notification.api.VulnerabilityNotificationItem;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.common.HubProjectMapping;
import com.blackducksoftware.integration.jira.common.HubProjectMappings;
import com.blackducksoftware.integration.jira.common.JiraContext;
import com.blackducksoftware.integration.jira.common.PolicyRuleSerializable;
import com.blackducksoftware.integration.jira.config.HubJiraConfigSerializable;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;
import com.google.gson.reflect.TypeToken;

public class HubJiraTask {
	private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));
	private static final String JIRA_ISSUE_TYPE_NAME_DEFAULT = "Task";
	private final HubServerConfig serverConfig;
	private final String intervalString;
	private final String jiraIssueTypeName;
	private final String installDateString;
	private final String lastRunDateString;
	private final String projectMappingJson;
	private final String policyRulesJson;
	private final String jiraUser;
	private final Date runDate;
	private final String runDateString;
	private final SimpleDateFormat dateFormatter;
	private final JiraServices jiraServices = new JiraServices();

	public HubJiraTask(final HubServerConfig serverConfig, final String intervalString, final String jiraIssueTypeName,
			final String installDateString,
			final String lastRunDateString,
			final String projectMappingJson,
			final String policyRulesJson, final String jiraUser) {

		this.serverConfig = serverConfig;
		this.intervalString = intervalString;
		if (jiraIssueTypeName != null) {
			this.jiraIssueTypeName = jiraIssueTypeName;
		} else {
			this.jiraIssueTypeName = JIRA_ISSUE_TYPE_NAME_DEFAULT;
		}
		this.installDateString = installDateString;
		this.lastRunDateString = lastRunDateString;
		this.projectMappingJson = projectMappingJson;
		this.policyRulesJson = policyRulesJson;
		this.jiraUser = jiraUser;
		this.runDate = new Date();

		dateFormatter = new SimpleDateFormat(RestConnection.JSON_DATE_FORMAT);
		dateFormatter.setTimeZone(java.util.TimeZone.getTimeZone("Zulu"));
		this.runDateString = dateFormatter.format(runDate);

		logger.debug("Install date: " + installDateString);
		logger.debug("Last run date: " + lastRunDateString);
	}

	/**
	 * Setup, then generate JIRA tickets based on recent notifications
	 *
	 * @return this execution's run date/time string on success, null otherwise
	 */
	public String execute() {

		final HubJiraConfigSerializable config = validateInput();
		if (config == null) {
			return null;
		}

		final Date startDate;
		try {
			startDate = deriveStartDate(installDateString, lastRunDateString);
		} catch (final ParseException e) {
			logger.info("This is the first run, but the plugin install date cannot be parsed; Not doing anything this time, will record collection start time and start collecting notifications next time");
			return runDateString;
		}

		try {
			final RestConnection restConnection = initRestConnection();
			final HubIntRestService hub = initHubRestService(restConnection);
			final HubItemsService<NotificationItem> hubItemsService = initHubItemsService(restConnection);

			final JiraContext jiraContext = initJiraContext(jiraUser, jiraIssueTypeName);

			if (jiraContext == null) {
				logger.info("Missing information to generate tickets.");

				return null;
			}

			final TicketGenerator ticketGenerator = initTicketGenerator(jiraContext,
					restConnection,
					hub,
					hubItemsService);

			logger.info("Getting Hub notifications from " + startDate + " to " + runDate);

			final NotificationDateRange notificationDateRange = new NotificationDateRange(startDate,
					runDate);

			final List<String> linksOfRulesToMonitor = getRuleUrls(config);
			final HubProjectMappings hubProjectMappings = new HubProjectMappings(jiraServices, jiraContext,
					config.getHubProjectMappings());

			// Generate Jira Issues based on recent notifications
			ticketGenerator.generateTicketsForRecentNotifications(hubProjectMappings,
					linksOfRulesToMonitor, notificationDateRange);
		} catch (final BDRestException e) {
			logger.error("Error processing Hub notifications or generating JIRA issues: " + e.getMessage(), e);
			return null;
		} catch (final IllegalArgumentException e) {
			logger.error("Error processing Hub notifications or generating JIRA issues: " + e.getMessage(), e);
			return null;
		} catch (final EncryptionException e) {
			logger.error("Error processing Hub notifications or generating JIRA issues: " + e.getMessage(), e);
			return null;
		} catch (final ParseException e) {
			logger.error("Error processing Hub notifications or generating JIRA issues: " + e.getMessage(), e);
			return null;
		} catch (final NotificationServiceException e) {
			logger.error("Error processing Hub notifications or generating JIRA issues: " + e.getMessage(), e);
			return null;
		} catch (final URISyntaxException e) {
			logger.error("Error processing Hub notifications or generating JIRA issues: " + e.getMessage(), e);
			return null;
		}
		return runDateString;
	}

	private List<String> getRuleUrls(final HubJiraConfigSerializable config) {
		final List<String> ruleUrls = new ArrayList<>();
		final List<PolicyRuleSerializable> rules = config.getPolicyRules();
		for (final PolicyRuleSerializable rule : rules) {
			final String ruleUrl = rule.getPolicyUrl();
			logger.debug("getRuleUrls(): rule name: " + rule.getName() + "; ruleUrl: " + ruleUrl + "; checked: "
					+ rule.isChecked());
			if ((rule.isChecked()) && (!ruleUrl.equals("undefined"))) {
				ruleUrls.add(ruleUrl);
			}
		}
		if (ruleUrls.size() > 0) {
			return ruleUrls;
		} else {
			logger.error("No valid rule URLs found in configuration");
			return null;
		}
	}

	private JiraContext initJiraContext(final String jiraUser, final String issueTypeName) {
		final UserManager jiraUserManager = ComponentAccessor.getUserManager();
		final ApplicationUser jiraSysAdmin = jiraUserManager.getUserByName(jiraUser);
		if (jiraSysAdmin == null) {
			logger.error("Could not find the Jira System admin that saved the Hub Jira config.");
			return null;
		}

		final JiraContext jiraContext = new JiraContext(jiraSysAdmin, issueTypeName);
		return jiraContext;
	}

	private TicketGenerator initTicketGenerator(final JiraContext jiraContext,
			final RestConnection restConnection,
			final HubIntRestService hub, final HubItemsService<NotificationItem> hubItemsService) {
		logger.debug("Jira user: " + this.jiraUser);

		final NotificationService notificationService;
		if (!"mock".equals(jiraUser)) {
			logger.debug("Creating HubNotificationService");
			notificationService = new NotificationService(restConnection, hub, hubItemsService, logger);
		} else {
			logger.debug("Creating HubNotificationServiceMock");
			notificationService = new HubNotificationServiceMock(restConnection, hub, hubItemsService, logger);
		}

		final TicketGenerator ticketGenerator = new TicketGenerator(notificationService,
 jiraServices, jiraContext);
		return ticketGenerator;
	}

	private HubItemsService<NotificationItem> initHubItemsService(final RestConnection restConnection) {
		final TypeToken<NotificationItem> typeToken = new TypeToken<NotificationItem>() {
		};
		final Map<String, Class<? extends NotificationItem>> typeToSubclassMap = new HashMap<>();
		typeToSubclassMap.put("VULNERABILITY", VulnerabilityNotificationItem.class);
		typeToSubclassMap.put("RULE_VIOLATION", RuleViolationNotificationItem.class);
		typeToSubclassMap.put("POLICY_OVERRIDE", PolicyOverrideNotificationItem.class);
		final HubItemsService<NotificationItem> hubItemsService = new HubItemsService<>(restConnection,
				NotificationItem.class, typeToken, typeToSubclassMap);
		return hubItemsService;
	}

	private HubIntRestService initHubRestService(final RestConnection restConnection) throws URISyntaxException {
		final HubIntRestService hub = new HubIntRestService(restConnection);
		return hub;
	}


	private RestConnection initRestConnection() throws EncryptionException, URISyntaxException, BDRestException {

		final RestConnection restConnection = new RestConnection(serverConfig.getHubUrl().toString());

		restConnection.setCookies(serverConfig.getGlobalCredentials().getUsername(),
				serverConfig.getGlobalCredentials().getDecryptedPassword());
		restConnection.setProxyProperties(serverConfig.getProxyInfo());

		logger.debug("Setting Hub timeout to: " + serverConfig.getTimeout());
		restConnection.setTimeout(serverConfig.getTimeout());
		return restConnection;
	}

	private HubJiraConfigSerializable validateInput() {
		if (projectMappingJson == null) {
			logger.debug("HubNotificationCheckTask: Project Mappings not configured, therefore there is nothing to do.");
			return null;
		}

		if (policyRulesJson == null) {
			logger.debug("HubNotificationCheckTask: Policy Rules not configured, therefore there is nothing to do.");
			return null;
		}

		logger.debug("Last run date: " + lastRunDateString);
		logger.debug("Hub url / username: " + serverConfig.getHubUrl().toString() + " / "
				+ serverConfig.getGlobalCredentials().getUsername());
		logger.debug("Interval: " + intervalString);

		final HubJiraConfigSerializable config = new HubJiraConfigSerializable();
		config.setHubProjectMappingsJson(projectMappingJson);
		config.setPolicyRulesJson(policyRulesJson);
		logger.debug("Mappings:");
		for (final HubProjectMapping mapping : config.getHubProjectMappings()) {
			logger.debug(mapping.toString());
		}
		logger.debug("Policy Rules:");
		for (final PolicyRuleSerializable rule : config.getPolicyRules()) {
			logger.debug(rule.toString());
		}
		return config;
	}

	private Date deriveStartDate(final String installDateString, final String lastRunDateString) throws ParseException {
		final Date startDate;
		if (lastRunDateString == null) {
			logger.info("No lastRunDate set, so this is the first run; Will collect notifications since the plugin install time: "
					+ installDateString);

			startDate = dateFormatter.parse(installDateString);

		} else {
			startDate = dateFormatter.parse(lastRunDateString);
		}
		return startDate;
	}
}
