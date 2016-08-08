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
package com.blackducksoftware.integration.jira.task;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.api.item.HubItemsService;
import com.blackducksoftware.integration.hub.api.notification.NotificationItem;
import com.blackducksoftware.integration.hub.api.notification.VulnerabilityNotificationContent;
import com.blackducksoftware.integration.hub.api.notification.VulnerabilityNotificationItem;
import com.blackducksoftware.integration.hub.exception.NotificationServiceException;
import com.blackducksoftware.integration.hub.logging.IntLogger;
import com.blackducksoftware.integration.hub.meta.MetaInformation;
import com.blackducksoftware.integration.hub.notification.NotificationDateRange;
import com.blackducksoftware.integration.hub.notification.NotificationService;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class HubNotificationServiceMock extends NotificationService {
	private final IntLogger logger;

	public HubNotificationServiceMock(final RestConnection restConnection, final HubIntRestService hub,
			final HubItemsService<NotificationItem> hubItemsService, final IntLogger logger) {
		// final RestConnection restConnection, final HubIntRestService hub,
		// final HubItemsService<NotificationItem> hubItemsService, final
		// IntLogger logger
		super(restConnection, hub, hubItemsService, logger);
		this.logger = logger;
	}

	@Override
	public List<NotificationItem> fetchNotifications(final NotificationDateRange dateRange)
			throws NotificationServiceException {
		logger.debug("fetchNotifications(");
		final String filePath = "/tmp/json/VulnerabilityNotificationContent_current.json";
		List<NotificationItem> notificationItems;
		try {
			notificationItems = mockNewVulnerabilityNotificationItems(filePath, false);
		} catch (final IOException e) {
			logger.error("Error mocking notifications from file: " + filePath + ": " + e.getMessage());
			return new ArrayList<>(0);
		}

		return notificationItems;
	}

	private List<NotificationItem> mockNewVulnerabilityNotificationItems(final String filePath,
			final boolean createDuplicate) throws IOException {
		final List<NotificationItem> notificationItems = new ArrayList<>();
		final MetaInformation meta = new MetaInformation(null, null, null);
		final VulnerabilityNotificationItem notificationItem = new VulnerabilityNotificationItem(meta);
		final String jsonString = readFile(filePath);
		final VulnerabilityNotificationContent content = createVulnerabilityNotificationContent(jsonString);
		notificationItem.setContent(content);

		notificationItems.add(notificationItem);
		if (createDuplicate) {
			notificationItems.add(notificationItem);
		}
		return notificationItems;
	}

	private VulnerabilityNotificationContent createVulnerabilityNotificationContent(final String jsonString) {
		final Gson gson = new GsonBuilder().create();
		final VulnerabilityNotificationContent vulnContent = gson.fromJson(jsonString,
				VulnerabilityNotificationContent.class);
		return vulnContent;
	}

	private String readFile(final String path) throws IOException {
		final byte[] jsonBytes = Files.readAllBytes(Paths.get(path));
		final String jsonString = new String(jsonBytes, Charset.forName("UTF-8"));
		return jsonString;
	}
}
