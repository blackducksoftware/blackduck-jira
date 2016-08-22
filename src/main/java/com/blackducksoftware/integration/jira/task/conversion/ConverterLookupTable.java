/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package com.blackducksoftware.integration.jira.task.conversion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blackducksoftware.integration.hub.api.notification.NotificationItem;
import com.blackducksoftware.integration.hub.api.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.hub.api.notification.RuleViolationNotificationItem;
import com.blackducksoftware.integration.hub.api.notification.VulnerabilityNotificationItem;
import com.blackducksoftware.integration.hub.exception.NotificationServiceException;
import com.blackducksoftware.integration.hub.notification.NotificationService;
import com.blackducksoftware.integration.jira.common.HubProjectMappings;
import com.blackducksoftware.integration.jira.common.JiraContext;
import com.blackducksoftware.integration.jira.task.JiraSettingsService;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;

public class ConverterLookupTable {
	private final Map<Class<? extends NotificationItem>, NotificationToEventConverter> lookupTable = new HashMap<>();

	public ConverterLookupTable(final HubProjectMappings mappings, final JiraServices jiraServices,
			final JiraContext jiraContext,
			final List<String> linksOfRulesToMonitor, final NotificationService hubNotificationService,
			final JiraSettingsService jiraSettingsService) {

		final NotificationToEventConverter vulnerabilityNotificationConverter = new VulnerabilityNotificationConverter(mappings,
				jiraServices, jiraContext, hubNotificationService, jiraSettingsService);
		final NotificationToEventConverter policyViolationNotificationConverter = new PolicyViolationNotificationConverter(mappings,
				jiraServices, jiraContext, linksOfRulesToMonitor, hubNotificationService, jiraSettingsService);
		final NotificationToEventConverter policyOverrideNotificationConverter = new PolicyOverrideNotificationConverter(
				mappings, jiraServices, jiraContext, linksOfRulesToMonitor, hubNotificationService,
				jiraSettingsService);

		lookupTable.put(RuleViolationNotificationItem.class, policyViolationNotificationConverter);
		lookupTable.put(PolicyOverrideNotificationItem.class, policyOverrideNotificationConverter);
		lookupTable.put(VulnerabilityNotificationItem.class, vulnerabilityNotificationConverter);
	}

	public NotificationToEventConverter getConverter(final NotificationItem notif)
			throws NotificationServiceException {
		final Class<? extends NotificationItem> c = notif.getClass();
		final NotificationToEventConverter converter = lookupTable.get(c);
		if (converter == null) {
			throw new NotificationServiceException("Notification type unknown for notification: " + notif);
		}
		return converter;
	}
}
