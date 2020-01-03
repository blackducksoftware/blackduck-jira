/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2020 Synopsys, Inc.
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
package com.blackducksoftware.integration.jira.task;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.util.BuildUtilsInfoImpl;
import com.blackducksoftware.integration.jira.blackduck.BlackDuckConnectionHelper;
import com.blackducksoftware.integration.jira.common.BlackDuckPluginDateFormatter;
import com.blackducksoftware.integration.jira.common.JiraUserContext;
import com.blackducksoftware.integration.jira.common.model.PluginBlackDuckServerConfigModel;
import com.blackducksoftware.integration.jira.common.model.PluginField;
import com.blackducksoftware.integration.jira.data.accessor.GlobalConfigurationAccessor;
import com.blackducksoftware.integration.jira.data.accessor.PluginConfigurationAccessor;
import com.blackducksoftware.integration.jira.data.accessor.PluginErrorAccessor;
import com.blackducksoftware.integration.jira.issue.conversion.TicketGenerator;
import com.blackducksoftware.integration.jira.issue.handler.BlackDuckProjectMappings;
import com.blackducksoftware.integration.jira.issue.model.PluginIssueCreationConfigModel;
import com.blackducksoftware.integration.jira.issue.model.PluginIssueFieldConfigModel;
import com.blackducksoftware.integration.jira.web.JiraConfigDeserializer;
import com.blackducksoftware.integration.jira.web.JiraServices;
import com.blackducksoftware.integration.jira.web.model.BlackDuckJiraConfigSerializable;
import com.blackducksoftware.integration.jira.web.model.BlackDuckJiraFieldCopyConfigSerializable;
import com.blackducksoftware.integration.jira.web.model.PolicyRuleSerializable;
import com.blackducksoftware.integration.jira.workflow.notification.CommonNotificationService;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.phonehome.BlackDuckPhoneHomeHelper;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.NotificationService;
import com.synopsys.integration.exception.IntegrationException;

public class BlackDuckJiraTask {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final GlobalConfigurationAccessor globalConfigurationAccessor;
    private final PluginConfigurationAccessor pluginConfigurationAccessor;
    private final JiraUserContext jiraUserContext;
    private final Date runDate;
    private final JiraServices jiraServices = new JiraServices();
    private final PluginErrorAccessor pluginErrorAccessor;
    private final Map<PluginField, CustomField> ticketInfoFromSetup;
    private final JiraConfigDeserializer configDeserializer;
    private final BlackDuckPluginDateFormatter pluginDateFormatter;
    private final BlackDuckConnectionHelper blackDuckConnectionHelper;

    public BlackDuckJiraTask(final GlobalConfigurationAccessor globalConfigurationAccessor, final PluginConfigurationAccessor pluginConfigurationAccessor, final PluginErrorAccessor pluginErrorAccessor,
        final JiraUserContext jiraUserContext, final Map<PluginField, CustomField> ticketInfoFromSetup) {
        this.globalConfigurationAccessor = globalConfigurationAccessor;
        this.pluginConfigurationAccessor = pluginConfigurationAccessor;
        this.jiraUserContext = jiraUserContext;

        this.runDate = new Date();
        logger.info("Install date: " + pluginConfigurationAccessor.getFirstTimeSave());
        logger.info("Last run date: " + pluginConfigurationAccessor.getLastRunDate());

        this.pluginErrorAccessor = pluginErrorAccessor;
        this.ticketInfoFromSetup = ticketInfoFromSetup;
        this.configDeserializer = new JiraConfigDeserializer();
        this.pluginDateFormatter = new BlackDuckPluginDateFormatter();
        this.blackDuckConnectionHelper = new BlackDuckConnectionHelper();
    }

    /**
     * Setup, then generate JIRA tickets based on recent notifications
     * @return this execution's run date/time string on success, or previous start date/time on failure
     */
    public Optional<String> execute(final String previousStartDate) {
        logger.debug("Previous start date: " + previousStartDate);

        final PluginBlackDuckServerConfigModel blackDuckServerConfig = globalConfigurationAccessor.getBlackDuckServerConfig();
        final Optional<BlackDuckServicesFactory> optionalHubServicesFactory = blackDuckConnectionHelper.createBlackDuckServicesFactorySafely(logger, blackDuckServerConfig.createBlackDuckServerConfigBuilder());
        if (!optionalHubServicesFactory.isPresent()) {
            return Optional.ofNullable(previousStartDate);
        }
        final BlackDuckServicesFactory blackDuckServicesFactory = optionalHubServicesFactory.get();
        phoneHome(blackDuckServicesFactory);

        final NotificationService notificationService = blackDuckServicesFactory.createNotificationService();

        final PluginIssueCreationConfigModel issueCreationConfig = globalConfigurationAccessor.getIssueCreationConfig();
        final PluginIssueFieldConfigModel fieldMappingConfig = globalConfigurationAccessor.getFieldMappingConfig();

        final BlackDuckJiraConfigSerializable config = configDeserializer.deserializeConfig(issueCreationConfig);
        // FIXME the policy check here is unnecessary
        if (!config.hasProjectMappings() && !config.hasPolicyRules()) {
            return Optional.ofNullable(previousStartDate);
        }

        logger.debug("Attempting to get the Black Duck user...");
        final Optional<UserView> optionalUserView = getBlackDuckUser(blackDuckServicesFactory.createBlackDuckService());
        if (!optionalUserView.isPresent()) {
            logger.warn("Will not request notifications from Black Duck because of an invalid user configuration");
            return Optional.ofNullable(previousStartDate);
        }
        final UserView blackDuckUser = optionalUserView.get();

        final Date startDate;
        try {
            logger.debug("Determining what to use as the start date...");
            startDate = deriveStartDate(notificationService, blackDuckUser, previousStartDate);
            logger.debug("Derived start date: " + startDate);
        } catch (final ParseException parseException) {
            logger.info("This is the first run, but the plugin install date cannot be parsed; Not doing anything this time, will record collection start time and start collecting notifications next time");
            return Optional.of(getRunDateString());
        } catch (final IntegrationException integrationException) {
            logger.error("Could not determine the last notification date from Black Duck. Please ensure that a connection can be established.");
            return Optional.ofNullable(previousStartDate);
        }
        final String fallbackDate = pluginDateFormatter.format(startDate);

        try {
            final BlackDuckJiraFieldCopyConfigSerializable fieldCopyConfig = configDeserializer.deserializeFieldCopyConfig(fieldMappingConfig.getFieldMappingJson());
            final boolean getOldestNotificationsFirst = true;
            final TicketGenerator ticketGenerator = initTicketGenerator(jiraUserContext, blackDuckServicesFactory, notificationService, getOldestNotificationsFirst, ticketInfoFromSetup, getRuleUrls(config), fieldCopyConfig);

            final BlackDuckProjectMappings blackDuckProjectMappings = new BlackDuckProjectMappings(jiraServices, config.getHubProjectMappings());

            // Generate JIRA Issues based on recent notifications
            logger.info("Getting Black Duck notifications from " + startDate + " to " + runDate);
            final Date lastNotificationDate = ticketGenerator.generateTicketsForNotificationsInDateRange(blackDuckUser, blackDuckProjectMappings, issueCreationConfig.getTicketCriteria(), startDate, runDate);
            logger.debug("Finished running ticket generator. Last notification date: " + pluginDateFormatter.format(lastNotificationDate));
            final Date nextRunDate = new Date(lastNotificationDate.getTime() + 1l);
            return Optional.of(pluginDateFormatter.format(nextRunDate));
        } catch (final Exception e) {
            logger.error("Error processing Black Duck notifications or generating JIRA issues: " + e.getMessage(), e);
            pluginErrorAccessor.addBlackDuckError(e, "executeBlackDuckJiraTask");
            return Optional.of(fallbackDate);
        }
    }

    public String getRunDateString() {
        return pluginDateFormatter.format(runDate);
    }

    private void phoneHome(final BlackDuckServicesFactory blackDuckServicesFactory) {
        final LocalDate lastPhoneHome = pluginConfigurationAccessor.getLastPhoneHome();
        if (LocalDate.now().isAfter(lastPhoneHome)) {
            final Map<String, String> phoneHomeMetaData = new HashMap<>();
            final ClusterManager clusterManager = jiraServices.getClusterManager();
            JiraDeploymentType deploymentType = JiraDeploymentType.SERVER;
            if (clusterManager.isClusterLicensed()) {
                deploymentType = JiraDeploymentType.DATA_CENTER;
            }

            phoneHomeMetaData.put("jira.version", new BuildUtilsInfoImpl().getVersion());
            phoneHomeMetaData.put("jira.deployment", deploymentType.name());

            final ExecutorService executorService = Executors.newSingleThreadExecutor();
            try {
                final BlackDuckPhoneHomeHelper blackDuckPhoneHomeHelper = BlackDuckPhoneHomeHelper.createAsynchronousPhoneHomeHelper(blackDuckServicesFactory, executorService);
                blackDuckPhoneHomeHelper.handlePhoneHome("blackduck-jira", jiraServices.getPluginVersion(), phoneHomeMetaData);
                pluginConfigurationAccessor.setLastPhoneHome(LocalDate.now());
            } finally {
                executorService.shutdown();
            }
        }
    }

    private Optional<UserView> getBlackDuckUser(final BlackDuckService blackDuckService) {
        UserView userView = null;
        try {
            userView = blackDuckService.getResponse(ApiDiscovery.CURRENT_USER_LINK_RESPONSE);
        } catch (final IntegrationException e) {
            final String message = "Could not get the logged in user for Black Duck: " + e.getMessage();
            logger.error(message, e);
            pluginErrorAccessor.addBlackDuckError(message, "getCurrentUser");
        }
        return Optional.ofNullable(userView);
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

    private TicketGenerator initTicketGenerator(final JiraUserContext jiraUserContext, final BlackDuckServicesFactory blackDuckServicesFactory, final NotificationService notificationService, final boolean notificationsOldestFirst,
        final Map<PluginField, CustomField> ticketInfoFromSetup, final List<String> linksOfRulesToMonitor, final BlackDuckJiraFieldCopyConfigSerializable fieldCopyConfig) {
        logger.debug("JIRA user: " + this.jiraUserContext.getJiraAdminUser().getName());

        final CommonNotificationService commonNotificationService = blackDuckConnectionHelper.createCommonNotificationService(blackDuckServicesFactory, notificationsOldestFirst);
        return new TicketGenerator(blackDuckServicesFactory.createBlackDuckService(), blackDuckServicesFactory.createBlackDuckBucketService(), notificationService, commonNotificationService, jiraServices, jiraUserContext,
            pluginErrorAccessor, ticketInfoFromSetup, linksOfRulesToMonitor, fieldCopyConfig);
    }

    private Date deriveStartDate(final NotificationService notificationService, final UserView user, final String lastRunDateString) throws ParseException, IntegrationException {
        if (lastRunDateString == null) {
            logger.info("No lastRunDate set, using the last notification date from Black Duck to determine the start date");
            final Date lastBlackDuckNotificationDate = notificationService.getLatestUserNotificationDate(user);
            final LocalDateTime lastBlackDuckNotificationDateLocal = pluginDateFormatter.toLocalDateTime(lastBlackDuckNotificationDate);
            return pluginDateFormatter.fromLocalDateTime(lastBlackDuckNotificationDateLocal.plus(1L, ChronoUnit.MILLIS));
        }
        return pluginDateFormatter.parse(lastRunDateString);
    }

}
