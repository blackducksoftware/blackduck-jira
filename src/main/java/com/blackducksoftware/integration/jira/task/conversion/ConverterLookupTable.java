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

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.notification.NotificationContentItem;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.common.HubProjectMappings;
import com.blackducksoftware.integration.jira.common.JiraContext;
import com.blackducksoftware.integration.jira.common.exception.ConfigurationException;
import com.blackducksoftware.integration.jira.config.HubJiraFieldCopyConfigSerializable;
import com.blackducksoftware.integration.jira.task.JiraSettingsService;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;

public class ConverterLookupTable {
    private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final Map<Class<? extends NotificationContentItem>, NotificationToEventConverter> lookupTable = new HashMap<>();

    public ConverterLookupTable(final HubProjectMappings mappings,
            final HubJiraFieldCopyConfigSerializable fieldCopyConfig,
            final JiraServices jiraServices,
            final JiraContext jiraContext, final JiraSettingsService jiraSettingsService,
            final HubServicesFactory hubServicesFactory)
            throws ConfigurationException {

        // final NotificationToEventConverter vulnerabilityNotificationConverter = new
        // VulnerabilityNotificationConverter(
        // mappings, fieldCopyConfig, jiraServices, jiraContext, jiraSettingsService,
        // hubServicesFactory, hubServicesFactory.createMetaService(logger));
        // final NotificationToEventConverter policyViolationNotificationConverter = new
        // PolicyViolationNotificationConverter(
        // mappings, fieldCopyConfig, jiraServices, jiraContext, jiraSettingsService,
        // hubServicesFactory.createMetaService(logger));
        //
        // final NotificationToEventConverter policyViolationClearedNotificationConverter = new
        // PolicyViolationClearedNotificationConverter(
        // mappings, fieldCopyConfig, jiraServices, jiraContext, jiraSettingsService,
        // hubServicesFactory.createMetaService(logger));
        // final NotificationToEventConverter policyOverrideNotificationConverter = new
        // PolicyOverrideNotificationConverter(
        // mappings, fieldCopyConfig, jiraServices, jiraContext, jiraSettingsService,
        // hubServicesFactory.createMetaService(logger));
        //
        // lookupTable.put(PolicyViolationContentItem.class, policyViolationNotificationConverter);
        // lookupTable.put(PolicyViolationClearedContentItem.class, policyViolationClearedNotificationConverter);
        // lookupTable.put(PolicyOverrideContentItem.class, policyOverrideNotificationConverter);
        // lookupTable.put(VulnerabilityContentItem.class, vulnerabilityNotificationConverter);
    }

    public NotificationToEventConverter getConverter(final NotificationContentItem notif)
            throws HubIntegrationException {
        final Class<? extends NotificationContentItem> c = notif.getClass();
        final NotificationToEventConverter converter = lookupTable.get(c);
        if (converter == null) {
            logger.info("No converter configured for the Notification type of this notification: " + notif);
        }
        return converter;
    }
}
