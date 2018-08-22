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
package com.blackducksoftware.integration.jira.task.issue;

import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.exception.JiraIssueException;
import com.blackducksoftware.integration.jira.common.model.BlackDuckProjectMapping;
import com.blackducksoftware.integration.jira.common.model.PolicyRuleSerializable;
import com.blackducksoftware.integration.jira.config.JiraSettingsService;
import com.blackducksoftware.integration.jira.config.PluginConfigurationDetails;
import com.blackducksoftware.integration.jira.config.model.BlackDuckJiraConfigSerializable;
import com.blackducksoftware.integration.jira.task.conversion.output.BlackDuckIssueTrackerProperties;
import com.blackducksoftware.integration.jira.task.issue.handler.BlackDuckIssueTrackerHandler;
import com.blackducksoftware.integration.jira.task.issue.handler.JiraIssuePropertyWrapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.synopsys.integration.blackduck.configuration.HubServerConfig;
import com.synopsys.integration.blackduck.configuration.HubServerConfigBuilder;
import com.synopsys.integration.blackduck.rest.BlackduckRestConnection;
import com.synopsys.integration.blackduck.service.HubServicesFactory;
import com.synopsys.integration.exception.EncryptionException;
import com.synopsys.integration.exception.IntegrationException;

public class IssueTrackerTask implements Callable<Boolean> {
    private final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final Issue jiraIssue;
    private final JiraIssuePropertyWrapper issueProperyWrapper;
    private final Long eventTypeID;
    private final PluginSettings settings;
    private final String propertyKey;
    private final EntityProperty property;

    public IssueTrackerTask(final Issue jiraIssue, final JiraIssuePropertyWrapper issueProperyWrapper, final Long eventTypeID, final PluginSettings settings, final String propertyKey, final EntityProperty property) {
        this.jiraIssue = jiraIssue;
        this.issueProperyWrapper = issueProperyWrapper;
        this.eventTypeID = eventTypeID;
        this.settings = settings;
        this.propertyKey = propertyKey;
        this.property = property;
    }

    @Override
    public Boolean call() throws Exception {
        try {
            logger.debug(String.format("ISSUE TRACKER TASK STARTED: Event Type ID: %s, Issue: %s", eventTypeID, jiraIssue));
            final PluginConfigurationDetails configDetails = new PluginConfigurationDetails(settings);
            final JiraSettingsService jiraSettingsService = new JiraSettingsService(settings);

            // only execute if hub 3.7 or higher with the issue tracker capability
            final HubServerConfig blackDuckServerConfig = createBlackDuckServerConfig(configDetails);
            if (blackDuckServerConfig == null) {
                logger.error("Black Duck Server Configuration is invalid.  Cannot update Black Duck issue tracking data.");
            } else {
                final HubServicesFactory servicesFactory = createBlackDuckServicesFactory(blackDuckServerConfig);
                final BlackDuckJiraConfigSerializable config = createJiraConfig(configDetails);
                if (config.getHubProjectMappings() == null || config.getHubProjectMappings().isEmpty()) {
                    logger.debug("Black Duck JIRA configuration is incomplete");
                    return Boolean.FALSE;
                }

                final BlackDuckIssueTrackerHandler blackDuckIssueHandler = new BlackDuckIssueTrackerHandler(jiraSettingsService, servicesFactory.createIssueService());
                handleIssue(eventTypeID, jiraIssue, blackDuckIssueHandler, property, propertyKey);
            }
        } catch (final Throwable throwable) {
            logger.error(String.format("Error occurred processing issue %s, caused by %s", jiraIssue, throwable));
            return Boolean.FALSE;
        } finally {
            logger.debug(String.format("ISSUE TRACKER TASK FINISHED: Event Type ID: %s, Issue: %s", eventTypeID, jiraIssue));
        }

        return Boolean.TRUE;
    }

    public HubServerConfig createBlackDuckServerConfig(final PluginConfigurationDetails configDetails) {
        final HubServerConfigBuilder blackDuckConfigBuilder = configDetails.createServerConfigBuilder();
        HubServerConfig blackDuckServerConfig = null;
        if (configDetails.getProjectMappingJson() == null) {
            logger.debug("BlackDuckNotificationCheckTask: Project Mappings not configured, therefore there is nothing to do.");
            return null;
        }

        if (configDetails.getPolicyRulesJson() == null) {
            logger.debug("BlackDuckNotificationCheckTask: Policy Rules not configured, therefore there is nothing to do.");
            return null;
        }

        try {
            logger.debug("Building Black Duck configuration");
            blackDuckServerConfig = blackDuckConfigBuilder.build();
            logger.debug("Finished building Black Duck configuration");
        } catch (final IllegalStateException e) {
            logger.error("Unable to connect to Black Duck. This could mean Black Duck is currently unreachable, or that the Black Duck plugin is not (yet) configured correctly: " + e.getMessage());
            return null;
        }

        logger.debug("Last run date: " + configDetails.getLastRunDateString());
        logger.debug("Black Duck url: " + blackDuckServerConfig.getHubUrl().toString());
        logger.debug("Interval: " + configDetails.getIntervalString());

        return blackDuckServerConfig;
    }

    public HubServicesFactory createBlackDuckServicesFactory(final HubServerConfig config) throws EncryptionException {
        final BlackduckRestConnection restConnection = config.createRestConnection(logger);
        return new HubServicesFactory(HubServicesFactory.createDefaultGson(), HubServicesFactory.createDefaultJsonParser(), restConnection, logger);
    }

    private BlackDuckJiraConfigSerializable createJiraConfig(final PluginConfigurationDetails pluginConfigDetails) {
        return deSerializeConfig(pluginConfigDetails);
    }

    private BlackDuckJiraConfigSerializable deSerializeConfig(final PluginConfigurationDetails pluginConfigDetails) {
        final BlackDuckJiraConfigSerializable config = new BlackDuckJiraConfigSerializable();
        config.setHubProjectMappingsJson(pluginConfigDetails.getProjectMappingJson());
        config.setPolicyRulesJson(pluginConfigDetails.getPolicyRulesJson());
        if (config.getHubProjectMappings() != null) {
            logger.debug("Mappings:");
            for (final BlackDuckProjectMapping mapping : config.getHubProjectMappings()) {
                if (mapping != null) {
                    logger.debug(mapping.toString());
                }
            }
        }

        if (config.getPolicyRules() != null) {
            logger.debug("Policy Rules:");
            for (final PolicyRuleSerializable rule : config.getPolicyRules()) {
                if (rule != null) {
                    logger.debug(rule.toString());
                }
            }
        }
        return config;
    }

    private void handleIssue(final Long eventTypeID, final Issue issue, final BlackDuckIssueTrackerHandler blackDuckIssueHandler, final EntityProperty property, final String propertyKey) throws IntegrationException {
        // final EntityProperty property = props.get(0);
        final BlackDuckIssueTrackerProperties properties = createIssueTrackerPropertiesFromJson(property.getValue());
        if (eventTypeID.equals(EventType.ISSUE_DELETED_ID)) {
            // || eventTypeID.equals(EventType.ISSUE_MOVED_ID))) { // move may be treated as delete in the future
            blackDuckIssueHandler.deleteBlackDuckIssue(properties.getBlackDuckIssueUrl(), issue);
            try {
                issueProperyWrapper.deleteIssueProperty(property.getEntityId(), jiraIssue.getCreator(), propertyKey);
            } catch (final JiraIssueException e) {
                logger.error("Problem deleting issue tracker property", e);
            }
        } else {
            blackDuckIssueHandler.updateBlackDuckIssue(properties.getBlackDuckIssueUrl(), issue);
        }
    }

    private BlackDuckIssueTrackerProperties createIssueTrackerPropertiesFromJson(final String json) {
        final Gson gson = new GsonBuilder().create();
        return gson.fromJson(json, BlackDuckIssueTrackerProperties.class);
    }

}