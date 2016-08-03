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
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package com.blackducksoftware.integration.jira.hub;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.ResourceDoesNotExistException;
import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.hub.item.HubItemsService;
import com.blackducksoftware.integration.hub.notification.NotificationDateRange;
import com.blackducksoftware.integration.hub.notification.NotificationService;
import com.blackducksoftware.integration.hub.notification.NotificationServiceException;
import com.blackducksoftware.integration.hub.notification.api.NotificationItem;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.version.api.ReleaseItem;
import com.blackducksoftware.integration.jira.HubJiraLogger;

public class HubNotificationServiceTest {
	private static final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(HubNotificationServiceTest.class
			.getName()));
	private static final String TEST_COMPONENT_VERSION_NAME = "testComponentVersionName";
	private static final String TEST_COMPONENT_VERSION_LINK = "testComponentVersionLink";
	private static final String TEST_COMPONENT_NAME = "testComponentName";
	private static final String END_DATE_STRING = "2016-05-10T00:00:00.000Z";
	private static final String START_DATE_STRING = "2016-05-01T00:00:00.000Z";

	private static NotificationService hubNotificationService;
	private static HubItemsService<NotificationItem> mockHubItemsService;
	private static RestConnection mockRestConnection;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		mockRestConnection = mock(RestConnection.class);

		final HubIntRestService mockHubIntRestService = mock(HubIntRestService.class);
		mockHubItemsService = mock(HubItemsService.class);
		final ReleaseItem mockProjectVersion = mock(ReleaseItem.class);
		final List<String> links = new ArrayList<>();
		links.add("http://test.project.url");
		when(mockProjectVersion.getLinks("project")).thenReturn(links);
		when(mockHubIntRestService.getProjectVersion("http://test.projectVersion.url")).thenReturn(mockProjectVersion);
		hubNotificationService = new NotificationService(mockRestConnection, mockHubIntRestService,
				mockHubItemsService, logger);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testFetchNotifications() throws NotificationServiceException, URISyntaxException, BDRestException,
	ParseException, IOException, ResourceDoesNotExistException {

		final SimpleDateFormat dateFormatter = new SimpleDateFormat(RestConnection.JSON_DATE_FORMAT);
		dateFormatter.setTimeZone(java.util.TimeZone.getTimeZone("Zulu"));

		final Date startDate = dateFormatter.parse(START_DATE_STRING);
		System.out.println("startDate: " + startDate.toString());

		final Date endDate = dateFormatter.parse(END_DATE_STRING);
		System.out.println("endDate: " + endDate.toString());

		final NotificationDateRange dateRange = new NotificationDateRange(startDate, endDate);
		final List<NotificationItem> notifs = hubNotificationService.fetchNotifications(dateRange);

		// Verify
		final List<String> expectedUrlSegments = new ArrayList<>();
		expectedUrlSegments.add("api");
		expectedUrlSegments.add("notifications");

		final Set<SimpleEntry<String, String>> expectedQueryParameters = new HashSet<>();
		expectedQueryParameters.add(new AbstractMap.SimpleEntry<String, String>("startDate", START_DATE_STRING));
		expectedQueryParameters.add(new AbstractMap.SimpleEntry<String, String>("endDate", END_DATE_STRING));
		// TODO this will need to change
		expectedQueryParameters.add(new AbstractMap.SimpleEntry<String, String>("limit", String.valueOf(1000)));

		verify(mockHubItemsService).httpGetItemList(expectedUrlSegments, expectedQueryParameters);
	}

	@Test
	public void testGetProjectUrlFromProjectReleaseUrl() throws NotificationServiceException,
	UnexpectedHubResponseException {
		final String versionUrl = "http://test.projectVersion.url";

		final ReleaseItem releaseItem = hubNotificationService.getProjectReleaseItemFromProjectReleaseUrl(versionUrl);
		assertEquals("http://test.project.url", releaseItem.getLinks("project").get(0));
	}
}
