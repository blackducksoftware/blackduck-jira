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
package com.blackducksoftware.integration.jira.task;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.atlassian.jira.util.BuildUtilsInfoImpl;
import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.discovery.ApiDiscovery;
import com.blackducksoftware.integration.hub.api.generated.view.UserView;
import com.blackducksoftware.integration.hub.configuration.HubServerConfig;
import com.blackducksoftware.integration.hub.configuration.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.service.IssueService;
import com.blackducksoftware.integration.hub.service.NotificationService;
import com.blackducksoftware.integration.hub.service.PhoneHomeService;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.BlackDuckProjectMappings;
import com.blackducksoftware.integration.jira.common.JiraUserContext;
import com.blackducksoftware.integration.jira.common.TicketInfoFromSetup;
import com.blackducksoftware.integration.jira.common.model.BlackDuckProjectMapping;
import com.blackducksoftware.integration.jira.common.model.PolicyRuleSerializable;
import com.blackducksoftware.integration.jira.config.JiraServices;
import com.blackducksoftware.integration.jira.config.JiraSettingsService;
import com.blackducksoftware.integration.jira.config.PluginConfigurationDetails;
import com.blackducksoftware.integration.jira.config.model.BlackDuckJiraConfigSerializable;
import com.blackducksoftware.integration.jira.config.model.BlackDuckJiraFieldCopyConfigSerializable;
import com.blackducksoftware.integration.jira.task.conversion.TicketGenerator;
import com.blackducksoftware.integration.phonehome.PhoneHomeRequestBody;
import com.blackducksoftware.integration.rest.RestConstants;
import com.blackducksoftware.integration.rest.connection.RestConnection;

public class BlackDuckJiraTask {
    private final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final PluginConfigurationDetails pluginConfigDetails;
    private final JiraUserContext jiraContext;
    private final Date runDate;
    private final SimpleDateFormat dateFormatter;
    private final JiraServices jiraServices = new JiraServices();
    private final JiraSettingsService jiraSettingsService;
    private final TicketInfoFromSetup ticketInfoFromSetup;
    private final String fieldCopyMappingJson;

    public BlackDuckJiraTask(final PluginConfigurationDetails configDetails, final JiraUserContext jiraContext, final JiraSettingsService jiraSettingsService, final TicketInfoFromSetup ticketInfoFromSetup) {
        this.pluginConfigDetails = configDetails;
        this.jiraContext = jiraContext;

        this.runDate = new Date();
        dateFormatter = new SimpleDateFormat(RestConstants.JSON_DATE_FORMAT);
        dateFormatter.setTimeZone(java.util.TimeZone.getTimeZone("Zulu"));
        logger.info("Install date: " + configDetails.getInstallDateString());
        logger.info("Last run date: " + configDetails.getLastRunDateString());

        this.jiraSettingsService = jiraSettingsService;
        this.ticketInfoFromSetup = ticketInfoFromSetup;
        this.fieldCopyMappingJson = configDetails.getFieldCopyMappingJson();

        logger.debug("createVulnerabilityIssues: " + configDetails.isCreateVulnerabilityIssues());
    }

    /**
     * Setup, then generate JIRA tickets based on recent notifications
     *
     * @return this execution's run date/time string on success, or previous start date/time on failure
     */
    public String execute(final String previousStartDate) {
        final HubServerConfigBuilder blackDuckServerConfigBuilder = pluginConfigDetails.createServerConfigBuilder();
        HubServerConfig blackDuckServerConfig = null;
        try {
            logger.debug("Building Black Duck configuration");
            blackDuckServerConfig = blackDuckServerConfigBuilder.build();
            logger.debug("Finished building Black Duck configuration");
        } catch (final IllegalStateException e) {
            logger.error("Unable to connect to Black Duck. This could mean Black Duck is currently unreachable, or that the Black Duck JIRA plugin is not (yet) configured correctly: " + e.getMessage());
            return previousStartDate;
        }

        final BlackDuckJiraConfigSerializable config = deSerializeConfig(blackDuckServerConfig);
        if (config == null) {
            return previousStartDate;
        }
        final BlackDuckJiraFieldCopyConfigSerializable fieldCopyConfig = deSerializeFieldCopyConfig();

        Date startDate;
        try {
            startDate = deriveStartDate(pluginConfigDetails.getInstallDateString(), previousStartDate);
        } catch (final ParseException e) {
            logger.info("This is the first run, but the plugin install date cannot be parsed; Not doing anything this time, will record collection start time and start collecting notifications next time");
            return getRunDateString();
        }

        HubServicesFactory blackDuckServicesFactory = null;
        try {
            try {
                blackDuckServicesFactory = createBlackDuckServicesFactory(blackDuckServerConfig);
            } catch (final EncryptionException e) {
                logger.warn("Error handling password: " + e.getMessage());
                return previousStartDate;
            }

            final boolean getOldestNotificationsFirst = true;
            final TicketGenerator ticketGenerator = initTicketGenerator(jiraContext, blackDuckServicesFactory.createHubService(), blackDuckServicesFactory.createNotificationService(getOldestNotificationsFirst),
                    blackDuckServicesFactory.createIssueService(),
                    ticketInfoFromSetup, getRuleUrls(config), fieldCopyConfig);

            // Phone-Home
            final LocalDate lastPhoneHome = jiraSettingsService.getLastPhoneHome();
            if (LocalDate.now().isAfter(lastPhoneHome)) {
                bdPhoneHome(blackDuckServicesFactory.createPhoneHomeService());
            }

            final BlackDuckProjectMappings blackDuckProjectMappings = new BlackDuckProjectMappings(jiraServices, config.getHubProjectMappings());

            final String blackDuckUsername = blackDuckServerConfig.getGlobalCredentials().getUsername();
            logger.debug("Getting user item for user: " + blackDuckUsername);
            final UserView blackDuckUserItem = getBlackDuckUserItem(blackDuckServicesFactory, blackDuckUsername);
            if (blackDuckUserItem == null) {
                logger.warn("Will not request notifications from Black Duck because of an invalid user configuration");
                return previousStartDate;
            }
            // Generate JIRA Issues based on recent notifications
            logger.info("Getting Black Duck notifications from " + startDate + " to " + runDate);
            final Date lastNotificationDate = ticketGenerator.generateTicketsForNotificationsInDateRange(blackDuckUserItem, blackDuckProjectMappings, startDate, runDate);
            logger.debug("Finished running ticket generator. Last notification date: " + dateFormatter.format(lastNotificationDate));
            final Date nextRunDate = new Date(lastNotificationDate.getTime() + 1l);
            return dateFormatter.format(nextRunDate);
        } catch (final Exception e) {
            logger.error("Error processing Black Duck notifications or generating JIRA issues: " + e.getMessage(), e);
            jiraSettingsService.addBlackDuckError(e, "executeBlackDuckJiraTask");
            return previousStartDate;
        } finally {
            if (blackDuckServicesFactory != null) {
                closeRestConnection(blackDuckServicesFactory.getRestConnection());
            }
        }
    }

    public String getRunDateString() {
        return dateFormatter.format(runDate);
    }

    private UserView getBlackDuckUserItem(final HubServicesFactory blackDuckServicesFactory, final String currentUsername) {
        if (currentUsername == null) {
            final String msg = "Current username is null";
            logger.error(msg);
            jiraSettingsService.addBlackDuckError(msg, "getCurrentUser");
            return null;
        }
        final HubService blackDuckService = blackDuckServicesFactory.createHubService();
        List<UserView> users;
        try {
            users = blackDuckService.getAllResponses(ApiDiscovery.USERS_LINK_RESPONSE);
        } catch (final IntegrationException e) {
            final String msg = "Error getting user item for current user: " + currentUsername + ": " + e.getMessage();
            logger.error(msg);
            jiraSettingsService.addBlackDuckError(msg, "getCurrentUser");
            return null;
        }
        for (final UserView user : users) {
            if (currentUsername.equalsIgnoreCase(user.userName)) {
                return user;
            }
        }
        final String msg = "Current user: " + currentUsername + " not found in list of all users";
        logger.error(msg);
        jiraSettingsService.addBlackDuckError(msg, "getCurrentUser");
        return null;
    }

    private HubServicesFactory createBlackDuckServicesFactory(final HubServerConfig blackDuckServerConfig) throws EncryptionException {
        final RestConnection restConnection = blackDuckServerConfig.createRestConnection(logger);
        final HubServicesFactory blackDuckServicesFactory = new HubServicesFactory(restConnection);
        return blackDuckServicesFactory;
    }

    void closeRestConnection(final RestConnection restConnection) {
        try {
            restConnection.close();
        } catch (final IOException e) {
            logger.error("There was a problem trying to close the connection to the Black Duck server.", e);
        }
    }

    private List<String> getRuleUrls(final BlackDuckJiraConfigSerializable config) {
        final List<String> ruleUrls = new ArrayList<>();
        final List<PolicyRuleSerializable> rules = config.getPolicyRules();
        for (final PolicyRuleSerializable rule : rules) {
            final String ruleUrl = rule.getPolicyUrl();
            logger.debug("getRuleUrls(): rule name: " + rule.getName() + "; ruleUrl: " + ruleUrl + "; checked: " + rule.getChecked());
            if ((rule.getChecked()) && (!ruleUrl.equals("undefined"))) {
                ruleUrls.add(ruleUrl);
            }
        }
        return ruleUrls;
    }

    private TicketGenerator initTicketGenerator(final JiraUserContext jiraUserContext, final HubService blackDuckService, final NotificationService notificationService, final IssueService issueService,
            final TicketInfoFromSetup ticketInfoFromSetup, final List<String> linksOfRulesToMonitor, final BlackDuckJiraFieldCopyConfigSerializable fieldCopyConfig) throws URISyntaxException {
        logger.debug("JIRA user: " + this.jiraContext.getJiraAdminUser().getName());

        final TicketGenerator ticketGenerator = new TicketGenerator(blackDuckService, notificationService, issueService, jiraServices, jiraUserContext, jiraSettingsService, ticketInfoFromSetup.getCustomFields(),
                pluginConfigDetails.isCreateVulnerabilityIssues(), linksOfRulesToMonitor, fieldCopyConfig);
        return ticketGenerator;
    }

    private BlackDuckJiraConfigSerializable deSerializeConfig(final HubServerConfig blackDuckServerConfig) {
        if (pluginConfigDetails.getProjectMappingJson() == null) {
            logger.debug("BlackDuckNotificationCheckTask: Project Mappings not configured, therefore there is nothing to do.");
            return null;
        }

        if (pluginConfigDetails.getPolicyRulesJson() == null) {
            logger.debug("BlackDuckNotificationCheckTask: Policy Rules not configured, therefore there is nothing to do.");
            return null;
        }

        logger.debug("Black Duck url / username: " + blackDuckServerConfig.getHubUrl().toString() + " / " + blackDuckServerConfig.getGlobalCredentials().getUsername());
        final BlackDuckJiraConfigSerializable config = new BlackDuckJiraConfigSerializable();
        config.setHubProjectMappingsJson(pluginConfigDetails.getProjectMappingJson());
        config.setPolicyRulesJson(pluginConfigDetails.getPolicyRulesJson());
        logger.debug("Mappings:");
        for (final BlackDuckProjectMapping mapping : config.getHubProjectMappings()) {
            logger.debug(mapping.toString());
        }
        logger.debug("Policy Rules:");
        for (final PolicyRuleSerializable rule : config.getPolicyRules()) {
            logger.debug(rule.toString());
        }
        return config;
    }

    private BlackDuckJiraFieldCopyConfigSerializable deSerializeFieldCopyConfig() {
        final BlackDuckJiraFieldCopyConfigSerializable fieldCopyConfig = new BlackDuckJiraFieldCopyConfigSerializable();
        fieldCopyConfig.setJson(fieldCopyMappingJson);
        return fieldCopyConfig;
    }

    private Date deriveStartDate(final String installDateString, final String lastRunDateString) throws ParseException {
        final Date startDate;
        if (lastRunDateString == null) {
            logger.info("No lastRunDate set, so this is the first run; Will collect notifications since the plugin install time: " + installDateString);
            startDate = dateFormatter.parse(installDateString);
        } else {
            startDate = dateFormatter.parse(lastRunDateString);
        }
        return startDate;
    }

    public void bdPhoneHome(final PhoneHomeService phService) {
        try {
            final PhoneHomeRequestBody.Builder phBodyBuilder = phService.createInitialPhoneHomeRequestBodyBuilder("blackduck-jira", jiraServices.getPluginVersion());
            phBodyBuilder.addToMetaData("jira.version", new BuildUtilsInfoImpl().getVersion());
            final PhoneHomeRequestBody phBody = phBodyBuilder.build();
            phService.phoneHome(phBody);
            jiraSettingsService.setLastPhoneHome(LocalDate.now());
        } catch (final Exception phException) {
            logger.debug("Unable to phone home: " + phException.getMessage());
        }
    }

}
