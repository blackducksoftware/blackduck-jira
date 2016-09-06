package com.blackducksoftware.integration.jira.task;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.mocks.GroupManagerMock;
import com.blackducksoftware.integration.jira.mocks.PluginSettingsMock;
import com.blackducksoftware.integration.jira.task.setup.HubGroupSetup;

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
	}

	@Test
	public void testAddGroup() {
		final GroupManagerMock groupManager = new GroupManagerMock();

		final PluginSettingsMock settingsMock = new PluginSettingsMock();
		final JiraSettingsService settingService = new JiraSettingsService(settingsMock);

		final HubGroupSetup groupSetup = new HubGroupSetup(settingService, groupManager);
		groupSetup.addHubJiraGroupToJira();
		assertTrue(groupManager.getGroupCreateAttempted());
	}

}
