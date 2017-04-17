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
package com.blackducksoftware.integration.jira.task.issue.event;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.entity.property.EntityPropertyQuery;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.jira.common.HubProject;
import com.blackducksoftware.integration.jira.common.HubProjectMapping;
import com.blackducksoftware.integration.jira.common.JiraProjectMappings;
import com.blackducksoftware.integration.jira.common.PolicyRuleSerializable;
import com.blackducksoftware.integration.jira.config.HubJiraConfigSerializable;
import com.blackducksoftware.integration.jira.task.JiraSettingsService;
import com.blackducksoftware.integration.jira.task.PluginConfigurationDetails;
import com.blackducksoftware.integration.jira.task.conversion.output.HubIssueTrackerProperties;
import com.blackducksoftware.integration.jira.task.issue.HubIssueTrackerHandler;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class IssueEventListener implements InitializingBean, DisposableBean {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private final EventPublisher eventPublisher;

    private final PluginSettingsFactory pluginSettingsFactory;

    private final JiraServices jiraServices = new JiraServices();

    public IssueEventListener(final EventPublisher eventPublisher, final PluginSettingsFactory pluginSettingsFactory) {
        this.eventPublisher = eventPublisher;
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    @Override
    public void destroy() throws Exception {
        eventPublisher.unregister(this);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        eventPublisher.register(this);
    }

    @EventListener
    public void onIssueEvent(final IssueEvent issueEvent) {
        final Long eventTypeID = issueEvent.getEventTypeId();
        final Issue issue = issueEvent.getIssue();

        final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        final PluginConfigurationDetails configDetails = new PluginConfigurationDetails(settings);
        final JiraSettingsService jiraSettingsService = new JiraSettingsService(settings);

        final HubServerConfigBuilder hubConfigBuilder = configDetails.createHubServerConfigBuilder();
        HubServerConfig hubServerConfig = null;
        try {
            logger.debug("Building Hub configuration");
            hubServerConfig = hubConfigBuilder.build();
            logger.debug("Finished building Hub configuration");
        } catch (final IllegalStateException e) {
            logger.error(
                    "Unable to connect to the Hub. This could mean the Hub is currently unreachable, or that at least one of the Black Duck plugins (either the Hub Admin plugin or the Hub JIRA plugin) is not (yet) configured correctly: "
                            + e.getMessage());
            return;
        }

        // only execute if hub 3.7 or higher with the issue tracker capability

        final HubJiraConfigSerializable config = deSerializeConfig(configDetails, hubServerConfig);
        if (config == null) {
            logger.debug("No Hub Jira configuration set");
            return;
        }

        final JiraProjectMappings jiraProjectMappings = new JiraProjectMappings(config.getHubProjectMappings());
        final List<HubProject> hubProjectList = jiraProjectMappings.getHubProjects(issue.getProjectId());
        // limit to only mapped issues
        if (!hubProjectList.isEmpty()) {
            final HubIssueTrackerHandler hubIssueHandler = new HubIssueTrackerHandler(jiraSettingsService);
            for (final HubProject hubProject : hubProjectList) {

                logger.debug("=== ISSUE EVENT ===");
                logger.debug(String.format("Event Type ID:    %s", eventTypeID));
                logger.debug(String.format("Issue:            %s", issue));
                logger.debug(String.format("Hub Project Name: %s", hubProject.getProjectName()));

                final EntityPropertyQuery<?> query = jiraServices.getJsonEntityPropertyManager().query();
                final EntityPropertyQuery.ExecutableQuery executableQuery = query.key(HubIssueTrackerHandler.JIRA_ISSUE_PROPERTY_HUB_ISSUE_URL);
                final List<EntityProperty> props = executableQuery.maxResults(1).find();
                if (props.size() == 1) {
                    final EntityProperty property = props.get(0);
                    final HubIssueTrackerProperties properties = createIssueTrackerPropertiesFromJson(property.getValue());
                    if (eventTypeID.equals(EventType.ISSUE_CREATED_ID)) {
                        // Do nothing because the properties haven't been set on the project yet.
                    } else if (eventTypeID.equals(EventType.ISSUE_DELETED_ID)) {
                        hubIssueHandler.deleteHubIssue(properties.getHubIssueUrl(), issue);
                    } else {
                        // issue updated.
                        hubIssueHandler.updateHubIssue(properties.getHubIssueUrl(), issue);
                    }
                }
            }
        }
    }

    private HubJiraConfigSerializable deSerializeConfig(final PluginConfigurationDetails pluginConfigDetails, final HubServerConfig hubServerConfig) {
        if (pluginConfigDetails.getProjectMappingJson() == null) {
            logger.debug(
                    "HubNotificationCheckTask: Project Mappings not configured, therefore there is nothing to do.");
            return null;
        }

        if (pluginConfigDetails.getPolicyRulesJson() == null) {
            logger.debug("HubNotificationCheckTask: Policy Rules not configured, therefore there is nothing to do.");
            return null;
        }

        logger.debug("Last run date: " + pluginConfigDetails.getLastRunDateString());
        logger.debug("Hub url / username: " + hubServerConfig.getHubUrl().toString() + " / "
                + hubServerConfig.getGlobalCredentials().getUsername());
        logger.debug("Interval: " + pluginConfigDetails.getIntervalString());

        final HubJiraConfigSerializable config = new HubJiraConfigSerializable();
        config.setHubProjectMappingsJson(pluginConfigDetails.getProjectMappingJson());
        config.setPolicyRulesJson(pluginConfigDetails.getPolicyRulesJson());
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

    private HubIssueTrackerProperties createIssueTrackerPropertiesFromJson(final String json) {
        final Gson gson = new GsonBuilder().create();
        return gson.fromJson(json, HubIssueTrackerProperties.class);
    }
}
