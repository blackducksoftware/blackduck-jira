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
import java.util.Map;

import com.blackducksoftware.integration.hub.dataservices.notification.items.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservices.notification.items.PolicyOverrideContentItem;
import com.blackducksoftware.integration.hub.dataservices.notification.items.PolicyViolationContentItem;
import com.blackducksoftware.integration.hub.dataservices.notification.items.VulnerabilityContentItem;
import com.blackducksoftware.integration.hub.exception.NotificationServiceException;
import com.blackducksoftware.integration.jira.common.HubProjectMappings;
import com.blackducksoftware.integration.jira.common.JiraContext;
import com.blackducksoftware.integration.jira.task.JiraSettingsService;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;

public class ConverterLookupTable {
	private final Map<Class<? extends NotificationContentItem>, NotificationToEventConverter> lookupTable = new HashMap<>();

	public ConverterLookupTable(final HubProjectMappings mappings, final JiraServices jiraServices,
			final JiraContext jiraContext, final JiraSettingsService jiraSettingsService) {

		final NotificationToEventConverter vulnerabilityNotificationConverter = new VulnerabilityNotificationConverter(
				mappings, jiraServices, jiraContext, jiraSettingsService);
		final NotificationToEventConverter policyViolationNotificationConverter = new PolicyViolationNotificationConverter(
				mappings, jiraServices, jiraContext, jiraSettingsService);
		final NotificationToEventConverter policyOverrideNotificationConverter = new PolicyOverrideNotificationConverter(
				mappings, jiraServices, jiraContext, jiraSettingsService);

		lookupTable.put(PolicyViolationContentItem.class, policyViolationNotificationConverter);
		lookupTable.put(PolicyOverrideContentItem.class, policyOverrideNotificationConverter);
		lookupTable.put(VulnerabilityContentItem.class, vulnerabilityNotificationConverter);
	}

	public NotificationToEventConverter getConverter(final NotificationContentItem notif)
			throws NotificationServiceException {
		final Class<? extends NotificationContentItem> c = notif.getClass();
		final NotificationToEventConverter converter = lookupTable.get(c);
		if (converter == null) {
			throw new NotificationServiceException("Notification type unknown for notification: " + notif);
		}
		return converter;
	}
}
