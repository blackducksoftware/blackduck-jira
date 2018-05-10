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
package com.blackducksoftware.integration.jira.task.conversion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.apache.log4j.Logger;

import com.blackducksoftware.integration.hub.api.view.CommonNotificationState;
import com.blackducksoftware.integration.hub.api.view.MetaHandler;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.throwaway.NotificationEvent;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.common.HubProjectMappings;
import com.blackducksoftware.integration.jira.common.JiraContext;
import com.blackducksoftware.integration.jira.common.exception.ConfigurationException;
import com.blackducksoftware.integration.jira.config.HubJiraFieldCopyConfigSerializable;
import com.blackducksoftware.integration.jira.task.JiraSettingsService;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;

public class JiraNotificationProcessor {
    private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));

    // TODO NotificationProcessor<List<NotificationEvent>> leftovers
    private final Map<Class<?>, NotificationToEventConverter> processorMap = new HashMap<>();

    private final Set<NotificationEvent> cache = new HashSet<>();

    private final List<NotificationToEventConverter> converters = new ArrayList<>();

    public JiraNotificationProcessor(final HubProjectMappings mapping, final HubJiraFieldCopyConfigSerializable fieldCopyConfig, final JiraServices jiraServices, final JiraContext jiraContext, final JiraSettingsService jiraSettingsService,
            final HubServicesFactory hubServicesFactory, final MetaHandler metaHandler, final boolean createVulnerabilityIssues) throws ConfigurationException {
        // FIXME add NotificationToEventConverters to the converters list

        // final ListProcessorCache cache = new ListProcessorCache();
        // getCacheList().add(cache);
        //
        // final NotificationToEventConverter policyViolationNotificationConverter = new PolicyViolationNotificationConverter(cache,
        // mapping, fieldCopyConfig, jiraServices, jiraContext, jiraSettingsService, hubServicesFactory, metaHandler);
        //
        // final NotificationToEventConverter policyViolationClearedNotificationConverter = new PolicyViolationClearedNotificationConverter(cache,
        // mapping, fieldCopyConfig, jiraServices, jiraContext, jiraSettingsService, hubServicesFactory, metaHandler);
        // final NotificationToEventConverter policyOverrideNotificationConverter = new PolicyOverrideNotificationConverter(cache,
        // mapping, fieldCopyConfig, jiraServices, jiraContext, jiraSettingsService, hubServicesFactory, metaHandler);
        //
        // getProcessorMap().put(PolicyViolationContentItem.class, policyViolationNotificationConverter);
        // getProcessorMap().put(PolicyViolationClearedContentItem.class, policyViolationClearedNotificationConverter);
        // getProcessorMap().put(PolicyOverrideContentItem.class, policyOverrideNotificationConverter);
        // if (createVulnerabilityIssues) {
        // logger.info("Creation of vulnerability issues has been enabled.");
        // final NotificationToEventConverter vulnerabilityNotificationConverter = new VulnerabilityNotificationConverter(cache,
        // mapping, fieldCopyConfig, jiraServices, jiraContext, jiraSettingsService, hubServicesFactory, metaHandler);
        // getProcessorMap().put(VulnerabilityContentItem.class, vulnerabilityNotificationConverter);
        // } else {
        // logger.info("Creation of vulnerability issues has been disabled. No vulnerability issues will be created.");
        // }
    }

    public List<NotificationEvent> processEvents(final Collection<NotificationEvent> eventCollection) throws HubIntegrationException {
        final LinkedList<NotificationEvent> list = new LinkedList<>(eventCollection);
        return list;
    }

    // TODO NotificationProcessor<List<NotificationEvent>> leftovers
    public List<NotificationEvent> process(final SortedSet<CommonNotificationState> notifications) throws HubIntegrationException {
        createEvents(notifications);
        final Collection<NotificationEvent> events = collectEvents();
        return processEvents(events);
    }

    private void createEvents(final SortedSet<CommonNotificationState> notifications) throws HubIntegrationException {
        // TODO this will likely change
        for (final CommonNotificationState item : notifications) {
            final Class<?> key = item.getClass();
            if (processorMap.containsKey(key)) {
                final NotificationToEventConverter processor = processorMap.get(key);
                processor.convert(item);
            }
        }
    }

    private Collection<NotificationEvent> collectEvents() throws HubIntegrationException {
        final Collection<NotificationEvent> eventList = new LinkedList<>();
        for (final NotificationToEventConverter processor : converters) {
            eventList.addAll(processor.getCache());
        }
        return eventList;
    }

    public Map<Class<?>, NotificationToEventConverter> getProcessorMap() {
        return processorMap;
    }

    public List<NotificationToEventConverter> getCacheList() {
        return converters;
    }

}
