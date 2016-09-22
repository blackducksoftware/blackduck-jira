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
package com.blackducksoftware.integration.jira.task.setup;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.mocks.GroupManagerMock;
import com.blackducksoftware.integration.jira.mocks.PluginSettingsMock;
import com.blackducksoftware.integration.jira.task.JiraSettingsService;

public class HubGroupSetupTest {

	@Test
	public void testAddGroupAlreadyAdded() {
		final GroupManagerMock groupManager = new GroupManagerMock();
		groupManager.addGroupByName(HubJiraConstants.HUB_JIRA_GROUP);

		final PluginSettingsMock settingsMock = new PluginSettingsMock();
		final JiraSettingsService settingService = new JiraSettingsService(settingsMock);

		final HubGroupSetup groupSetup = new HubGroupSetup(settingService, groupManager);
		groupSetup.addHubJiraGroupToJira();
		assertTrue(!groupManager.getGroupCreateAttempted());
		assertNull(settingsMock.get(HubJiraConstants.HUB_JIRA_ERROR));
	}

	@Test
	public void testAddGroup() {
		final GroupManagerMock groupManager = new GroupManagerMock();

		final PluginSettingsMock settingsMock = new PluginSettingsMock();
		final JiraSettingsService settingService = new JiraSettingsService(settingsMock);

		final HubGroupSetup groupSetup = new HubGroupSetup(settingService, groupManager);
		groupSetup.addHubJiraGroupToJira();
		assertTrue(groupManager.getGroupCreateAttempted());
		assertNull(settingsMock.get(HubJiraConstants.HUB_JIRA_ERROR));
	}

}
