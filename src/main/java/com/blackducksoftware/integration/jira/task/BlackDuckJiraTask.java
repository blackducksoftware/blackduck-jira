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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.BlackDuckPluginDateFormatter;
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
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.configuration.HubServerConfig;
import com.synopsys.integration.blackduck.configuration.HubServerConfigBuilder;
import com.synopsys.integration.blackduck.notification.content.detail.NotificationContentDetailFactory;
import com.synopsys.integration.blackduck.rest.BlackduckRestConnection;
import com.synopsys.integration.blackduck.service.CommonNotificationService;
import com.synopsys.integration.blackduck.service.HubService;
import com.synopsys.integration.blackduck.service.HubServicesFactory;
import com.synopsys.integration.exception.EncryptionException;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.phonehome.PhoneHomeCallable;
import com.synopsys.integration.phonehome.PhoneHomeService;
import com.synopsys.integration.rest.connection.RestConnection;

public class BlackDuckJiraTask {
    private final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final PluginConfigurationDetails pluginConfigDetails;
    private final JiraUserContext jiraContext;
    private final Date runDate;
    private final JiraServices jiraServices = new JiraServices();
    private final JiraSettingsService jiraSettingsService;
    private final TicketInfoFromSetup ticketInfoFromSetup;
    private final String fieldCopyMappingJson;

    public BlackDuckJiraTask(final PluginConfigurationDetails configDetails, final JiraUserContext jiraContext, final JiraSettingsService jiraSettingsService, final TicketInfoFromSetup ticketInfoFromSetup) {
        this.pluginConfigDetails = configDetails;
        this.jiraContext = jiraContext;

        this.runDate = new Date();
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
            final TicketGenerator ticketGenerator = initTicketGenerator(jiraContext, blackDuckServicesFactory, getOldestNotificationsFirst, ticketInfoFromSetup, getRuleUrls(config), fieldCopyConfig);

            // Phone-Home
            final LocalDate lastPhoneHome = jiraSettingsService.getLastPhoneHome();
            if (LocalDate.now().isAfter(lastPhoneHome)) {
                final PhoneHomeCallable phCallable = blackDuckServicesFactory.createBlackDuckPhoneHomeCallable(blackDuckServicesFactory.createHubService().getHubBaseUrl(), "blackduck-jira", jiraServices.getPluginVersion());
                bdPhoneHome(phCallable);
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
            logger.debug("Finished running ticket generator. Last notification date: " + BlackDuckPluginDateFormatter.format(lastNotificationDate));
            final Date nextRunDate = new Date(lastNotificationDate.getTime() + 1l);
            return BlackDuckPluginDateFormatter.format(nextRunDate);
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
        return BlackDuckPluginDateFormatter.format(runDate);
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
        final BlackduckRestConnection restConnection = blackDuckServerConfig.createRestConnection(logger);
        final HubServicesFactory blackDuckServicesFactory = new HubServicesFactory(HubServicesFactory.createDefaultGson(), HubServicesFactory.createDefaultJsonParser(), restConnection, logger);
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

    private TicketGenerator initTicketGenerator(final JiraUserContext jiraUserContext, final HubServicesFactory hubServicesFactory, final boolean notificationsOldestFirst,
            final TicketInfoFromSetup ticketInfoFromSetup, final List<String> linksOfRulesToMonitor, final BlackDuckJiraFieldCopyConfigSerializable fieldCopyConfig) throws URISyntaxException {
        logger.debug("JIRA user: " + this.jiraContext.getJiraAdminUser().getName());

        final NotificationContentDetailFactory contentDetailFactory = new NotificationContentDetailFactory(hubServicesFactory.getGson(), HubServicesFactory.createDefaultJsonParser());
        final CommonNotificationService commonNotificationService = hubServicesFactory.createCommonNotificationService(contentDetailFactory, notificationsOldestFirst);

        final TicketGenerator ticketGenerator = new TicketGenerator(hubServicesFactory.createHubService(), hubServicesFactory.createHubBucketService(), hubServicesFactory.createNotificationService(), commonNotificationService,
                hubServicesFactory.createIssueService(), jiraServices, jiraUserContext, jiraSettingsService, ticketInfoFromSetup.getCustomFields(), pluginConfigDetails.isCreateVulnerabilityIssues(), linksOfRulesToMonitor, fieldCopyConfig);
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
            startDate = BlackDuckPluginDateFormatter.parse(installDateString);
        } else {
            startDate = BlackDuckPluginDateFormatter.parse(lastRunDateString);
        }
        return startDate;
    }

    public void bdPhoneHome(final PhoneHomeCallable phCallable) {
        final PhoneHomeService phService = new PhoneHomeService(logger, null);
        try {
            // FIXME find a way to pass meta data and environment variables into the body
            // Map<String, String> environmentVariables;
            // try {
            // final Map<String, String> systemEnv = System.getenv();
            // environmentVariables = new HashMap<>();
            // environmentVariables.putAll(systemEnv);
            // } catch (final Exception e) {
            // environmentVariables = Collections.emptyMap();
            // }
            // phBodyBuilder.addToMetaData("jira.version", new BuildUtilsInfoImpl().getVersion());
            // final PhoneHomeRequestBody phBody = phBodyBuilder.build();
            // phClient.postPhoneHomeRequest(phBody, environmentVariables);

            phService.phoneHome(phCallable);
            jiraSettingsService.setLastPhoneHome(LocalDate.now());
        } catch (final Exception phException) {
            logger.debug("Unable to phone home: " + phException.getMessage());
        }
    }

}
