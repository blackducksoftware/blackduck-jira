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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.task.issue.HubIssueTrackerPropertyHandler;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;

public class IssueEventListener implements InitializingBean, DisposableBean {
    private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));
    private final EventPublisher eventPublisher;
    private final PluginSettingsFactory pluginSettingsFactory;
    private final JiraServices jiraServices;
    private final HubIssueTrackerPropertyHandler hubIssueTrackerPropertyHandler;

    private final ExecutorService executorService;

    public IssueEventListener(final EventPublisher eventPublisher, final PluginSettingsFactory pluginSettingsFactory) {
        this(eventPublisher, pluginSettingsFactory, new JiraServices());
    }

    public IssueEventListener(final EventPublisher eventPublisher, final PluginSettingsFactory pluginSettingsFactory, final JiraServices jiraServices) {
        this.eventPublisher = eventPublisher;
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.jiraServices = jiraServices;
        this.hubIssueTrackerPropertyHandler = new HubIssueTrackerPropertyHandler();
        this.executorService = createExecutorService();
    }

    public ExecutorService createExecutorService() {
        return Executors.newSingleThreadExecutor();
    }

    @Override
    public void destroy() throws Exception {
        executorService.shutdown();
        eventPublisher.unregister(this);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        eventPublisher.register(this);
    }

    @EventListener
    public void onIssueEvent(final IssueEvent issueEvent) {
        try {
            final Long eventTypeID = issueEvent.getEventTypeId();
            final Issue issue = issueEvent.getIssue();

            if (!eventTypeID.equals(EventType.ISSUE_CREATED_ID)) {
                logger.debug("=== ISSUE EVENT ===");
                logger.debug(String.format("Event Type ID:    %s", eventTypeID));
                logger.debug(String.format("Issue:            %s", issue));

                final String propertyKey = hubIssueTrackerPropertyHandler.createEntityPropertyKey(issue);
                final EntityProperty hubIssueUrlProperty = getHubIssueTrackerUrlProperty(propertyKey, issue);

                if (hubIssueUrlProperty == null) {
                    logger.debug(String.format("Hub Issue Tracker URL not present. No further processing for issue: %s", issue));
                } else {
                    final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
                    executorService.submit(createTask(issue, eventTypeID, jiraServices, settings, propertyKey, hubIssueUrlProperty));
                }
            }
        } catch (final Exception ex) {
            logger.error("An unexpected error occurred processing issue event ", ex);
        }
    }

    private EntityProperty getHubIssueTrackerUrlProperty(final String propertyKey, final Issue issue) {

        logger.debug(String.format("Entitykey: %s", propertyKey));
        final EntityPropertyQuery<?> query = jiraServices.getJsonEntityPropertyManager().query();
        final EntityPropertyQuery.ExecutableQuery executableQuery = query.key(propertyKey);
        final List<EntityProperty> entityProperties = executableQuery.find();
        if (entityProperties.isEmpty()) {
            return null;
        } else {
            return entityProperties.get(0);
        }
    }

    public IssueTrackerTask createTask(final Issue issue, final Long eventTypeID, final JiraServices jiraServices, final PluginSettings settings, final String propertyKey, final EntityProperty property) {
        return new IssueTrackerTask(issue, eventTypeID, jiraServices, settings, propertyKey, property);
    }
}
