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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.blackducksoftware.integration.hub.dataservice.notification.model.PolicyOverrideContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.model.PolicyViolationClearedContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.model.PolicyViolationContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.model.VulnerabilityContentItem;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.notification.processor.ListProcessorCache;
import com.blackducksoftware.integration.hub.notification.processor.NotificationProcessor;
import com.blackducksoftware.integration.hub.notification.processor.event.NotificationEvent;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.common.HubProjectMappings;
import com.blackducksoftware.integration.jira.common.JiraContext;
import com.blackducksoftware.integration.jira.common.exception.ConfigurationException;
import com.blackducksoftware.integration.jira.config.HubJiraFieldCopyConfigSerializable;
import com.blackducksoftware.integration.jira.task.JiraSettingsService;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;

public class JiraNotificationProcessor extends NotificationProcessor<List<NotificationEvent>> {
    private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));

    public JiraNotificationProcessor(final HubProjectMappings mapping,
            final HubJiraFieldCopyConfigSerializable fieldCopyConfig,
            final JiraServices jiraServices,
            final JiraContext jiraContext, final JiraSettingsService jiraSettingsService,
            final HubServicesFactory hubServicesFactory,
            final boolean createVulnerabilityIssues)
            throws ConfigurationException {
        final ListProcessorCache cache = new ListProcessorCache();
        getCacheList().add(cache);

        final NotificationToEventConverter policyViolationNotificationConverter = new PolicyViolationNotificationConverter(cache,
                mapping, fieldCopyConfig, jiraServices, jiraContext, jiraSettingsService,
                hubServicesFactory);

        final NotificationToEventConverter policyViolationClearedNotificationConverter = new PolicyViolationClearedNotificationConverter(cache,
                mapping, fieldCopyConfig, jiraServices, jiraContext, jiraSettingsService, hubServicesFactory);
        final NotificationToEventConverter policyOverrideNotificationConverter = new PolicyOverrideNotificationConverter(cache,
                mapping, fieldCopyConfig, jiraServices, jiraContext, jiraSettingsService, hubServicesFactory);

        getProcessorMap().put(PolicyViolationContentItem.class, policyViolationNotificationConverter);
        getProcessorMap().put(PolicyViolationClearedContentItem.class, policyViolationClearedNotificationConverter);
        getProcessorMap().put(PolicyOverrideContentItem.class, policyOverrideNotificationConverter);
        if (createVulnerabilityIssues) {
            logger.info("Creation of vulnerability issues has been enabled.");
            final NotificationToEventConverter vulnerabilityNotificationConverter = new VulnerabilityNotificationConverter(cache,
                    mapping, fieldCopyConfig,
                    jiraServices, jiraContext, jiraSettingsService,
                    hubServicesFactory);
            getProcessorMap().put(VulnerabilityContentItem.class, vulnerabilityNotificationConverter);
        } else {
            logger.info("Creation of vulnerability issues has been disabled. No vulnerability issues will be created.");
        }
    }

    @Override
    public List<NotificationEvent> processEvents(final Collection<NotificationEvent> eventCollection) throws HubIntegrationException {
        final LinkedList<NotificationEvent> list = new LinkedList<>(eventCollection);
        return list;
    }
}
