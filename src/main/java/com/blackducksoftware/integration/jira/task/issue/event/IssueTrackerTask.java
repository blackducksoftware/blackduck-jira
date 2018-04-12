/**
 * Hub JIRA Plugin
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
package com.blackducksoftware.integration.jira.task.issue.event;

import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.atlassian.jira.bc.project.property.ProjectPropertyService;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.entity.property.EntityPropertyService.DeletePropertyValidationResult;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.HubSupportHelper;
import com.blackducksoftware.integration.hub.api.nonpublic.HubVersionRequestService;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.capability.HubCapabilitiesEnum;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.common.HubProjectMapping;
import com.blackducksoftware.integration.jira.common.JiraContext;
import com.blackducksoftware.integration.jira.common.PolicyRuleSerializable;
import com.blackducksoftware.integration.jira.config.HubJiraConfigSerializable;
import com.blackducksoftware.integration.jira.task.JiraSettingsService;
import com.blackducksoftware.integration.jira.task.PluginConfigurationDetails;
import com.blackducksoftware.integration.jira.task.conversion.output.HubIssueTrackerProperties;
import com.blackducksoftware.integration.jira.task.issue.HubIssueTrackerHandler;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class IssueTrackerTask implements Callable<Boolean> {
    private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));
    private final Long eventTypeID;
    private final Issue jiraIssue;
    private final JiraServices jiraServices;
    private final PluginSettings settings;
    private final String propertyKey;
    private final EntityProperty property;

    public IssueTrackerTask(final Issue jiraIssue, final Long eventTypeID, final JiraServices jiraServices, final PluginSettings settings, final String propertyKey, final EntityProperty property) {
        this.eventTypeID = eventTypeID;
        this.jiraIssue = jiraIssue;
        this.jiraServices = jiraServices;
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

            // only execute if hub 3.7 or higher with the issue tracker
            // capability
            final HubServerConfig hubServerConfig = createHubServerConfig(configDetails);
            if (hubServerConfig == null) {
                logger.error("Hub Server Configuration is invalid.  Cannot update Hub issue tracking data.");
            } else {
                final HubServicesFactory servicesFactory = createHubServicesFactory(hubServerConfig);

                final HubSupportHelper hubSupportHelper = new HubSupportHelper();
                final HubVersionRequestService hubVersionRequestService = servicesFactory.createHubVersionRequestService();
                hubSupportHelper.checkHubSupport(hubVersionRequestService, null);

                if (hubSupportHelper.hasCapability(HubCapabilitiesEnum.ISSUE_TRACKER)) {
                    final JiraContext jiraContext = initJiraContext(configDetails.getJiraAdminUserName(), configDetails.getJiraIssueCreatorUserName());
                    final HubJiraConfigSerializable config = createJiraConfig(configDetails);
                    if (config == null) {
                        logger.debug("No Hub Jira configuration set");
                        return Boolean.FALSE;
                    }

                    final HubIssueTrackerHandler hubIssueHandler = new HubIssueTrackerHandler(jiraServices, jiraSettingsService, servicesFactory.createBomComponentIssueRequestService());
                    handleIssue(jiraContext, eventTypeID, jiraIssue, hubIssueHandler, property, propertyKey);
                }
            }
        } catch (final Throwable throwable) {
            logger.error(String.format("Error occurred processing issue %s, caused by %s", jiraIssue, throwable));
            return Boolean.FALSE;
        } finally {
            logger.debug(String.format("ISSUE TRACKER TASK FINISHED: Event Type ID: %s, Issue: %s", eventTypeID, jiraIssue));
        }

        return Boolean.TRUE;
    }

    public HubServerConfig createHubServerConfig(final PluginConfigurationDetails configDetails) {
        final HubServerConfigBuilder hubConfigBuilder = configDetails.createHubServerConfigBuilder();
        HubServerConfig hubServerConfig = null;
        if (configDetails.getProjectMappingJson() == null) {
            logger.debug("HubNotificationCheckTask: Project Mappings not configured, therefore there is nothing to do.");
            return null;
        }

        if (configDetails.getPolicyRulesJson() == null) {
            logger.debug("HubNotificationCheckTask: Policy Rules not configured, therefore there is nothing to do.");
            return null;
        }

        try {
            logger.debug("Building Hub configuration");
            hubServerConfig = hubConfigBuilder.build();
            logger.debug("Finished building Hub configuration");
        } catch (final IllegalStateException e) {
            logger.error(
                    "Unable to connect to the Hub. This could mean the Hub is currently unreachable, or that at least one of the Black Duck plugins (either the Hub Admin plugin or the Hub JIRA plugin) is not (yet) configured correctly: "
                            + e.getMessage());
            return null;
        }

        logger.debug("Last run date: " + configDetails.getLastRunDateString());
        logger.debug("Hub url / username: " + hubServerConfig.getHubUrl().toString() + " / " + hubServerConfig.getGlobalCredentials().getUsername());
        logger.debug("Interval: " + configDetails.getIntervalString());

        return hubServerConfig;
    }

    public HubServicesFactory createHubServicesFactory(final HubServerConfig config) throws EncryptionException {
        final RestConnection restConnection = new CredentialsRestConnection(logger, config.getHubUrl(), config.getGlobalCredentials().getUsername(), config.getGlobalCredentials().getDecryptedPassword(), config.getTimeout());
        return new HubServicesFactory(restConnection);
    }

    private HubJiraConfigSerializable createJiraConfig(final PluginConfigurationDetails pluginConfigDetails) {
        return deSerializeConfig(pluginConfigDetails);
    }

    private HubJiraConfigSerializable deSerializeConfig(final PluginConfigurationDetails pluginConfigDetails) {
        final HubJiraConfigSerializable config = new HubJiraConfigSerializable();
        config.setHubProjectMappingsJson(pluginConfigDetails.getProjectMappingJson());
        config.setPolicyRulesJson(pluginConfigDetails.getPolicyRulesJson());
        if (config.getHubProjectMappings() != null) {
            logger.debug("Mappings:");
            for (final HubProjectMapping mapping : config.getHubProjectMappings()) {
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

    private void handleIssue(final JiraContext jiraContext, final Long eventTypeID, final Issue issue, final HubIssueTrackerHandler hubIssueHandler, final EntityProperty property, final String propertyKey) throws IntegrationException {
        // final EntityProperty property = props.get(0);
        final HubIssueTrackerProperties properties = createIssueTrackerPropertiesFromJson(property.getValue());
        if (eventTypeID.equals(EventType.ISSUE_DELETED_ID)) {
            // || eventTypeID.equals(EventType.ISSUE_MOVED_ID))) { // move
            // may be treated as delete in the future
            hubIssueHandler.deleteHubIssue(properties.getHubIssueUrl(), issue);
            // the issue has been
            final ProjectPropertyService projectPropertyService = jiraServices.getProjectPropertyService();
            final ApplicationUser issueCreatorUser = jiraContext.getJiraIssueCreatorUser();
            final DeletePropertyValidationResult validationResult = projectPropertyService.validateDeleteProperty(issueCreatorUser, property.getEntityId(), propertyKey);
            jiraServices.getProjectPropertyService().deleteProperty(issueCreatorUser, validationResult);
        } else {
            // issue updated.
            hubIssueHandler.updateHubIssue(properties.getHubIssueUrl(), issue);
        }
    }

    private HubIssueTrackerProperties createIssueTrackerPropertiesFromJson(final String json) {
        final Gson gson = new GsonBuilder().create();
        return gson.fromJson(json, HubIssueTrackerProperties.class);
    }

    private JiraContext initJiraContext(final String jiraAdminUsername, String jiraIssueCreatorUsername) {
        logger.debug(String.format("Checking JIRA users: Admin: %s; Issue creator: %s", jiraAdminUsername, jiraIssueCreatorUsername));
        if (jiraIssueCreatorUsername == null) {
            logger.warn(String.format("The JIRA Issue Creator user has not been configured, using the admin user (%s) to create issues. This can be changed via the Issue Creation configuration", jiraAdminUsername));
            jiraIssueCreatorUsername = jiraAdminUsername;
        }
        final ApplicationUser jiraAdminUser = getJiraUser(jiraAdminUsername);
        final ApplicationUser jiraIssueCreatorUser = getJiraUser(jiraIssueCreatorUsername);
        if ((jiraAdminUser == null) || (jiraIssueCreatorUser == null)) {
            return null;
        }
        final JiraContext jiraContext = new JiraContext(jiraAdminUser, jiraIssueCreatorUser);
        return jiraContext;
    }

    private ApplicationUser getJiraUser(final String jiraUsername) {
        final UserManager jiraUserManager = jiraServices.getUserManager();
        final ApplicationUser jiraUser = jiraUserManager.getUserByName(jiraUsername);
        if (jiraUser == null) {
            logger.error(String.format("Could not find the JIRA user %s", jiraUsername));
            return null;
        }
        return jiraUser;
    }
}
