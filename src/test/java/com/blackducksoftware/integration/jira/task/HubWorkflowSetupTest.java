package com.blackducksoftware.integration.jira.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.atlassian.jira.workflow.JiraWorkflow;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.mocks.ApplicationUserMock;
import com.blackducksoftware.integration.jira.mocks.JiraWorkflowMock;
import com.blackducksoftware.integration.jira.mocks.PluginSettingsMock;
import com.blackducksoftware.integration.jira.mocks.UserManagerMock;
import com.blackducksoftware.integration.jira.mocks.WorkflowManagerMock;
import com.blackducksoftware.integration.jira.mocks.WorkflowSchemeManagerMock;

public class HubWorkflowSetupTest {

	@Test
	public void testAddHubWorkflowToJiraNoUser() {
		final PluginSettingsMock settingsMock = new PluginSettingsMock();
		final JiraSettingsService settingService = new JiraSettingsService(settingsMock);

		final WorkflowManagerMock workflowManager = new WorkflowManagerMock();

		final WorkflowSchemeManagerMock workflowSchemeManager = new WorkflowSchemeManagerMock();

		final String jiraUser = "FakeUser";

		final UserManagerMock userManager = new UserManagerMock();

		final HubWorkflowSetup workflowSetup = new HubWorkflowSetup(settingService, workflowManager,
				workflowSchemeManager, userManager, jiraUser);

		assertNull(workflowSetup.addHubWorkflowToJira());
		assertTrue(!workflowManager.getAttemptedCreateWorkflow());

	}

	@Test
	public void testAddHubWorkflowToJiraAlreadyExisting() {
		final PluginSettingsMock settingsMock = new PluginSettingsMock();
		final JiraSettingsService settingService = new JiraSettingsService(settingsMock);

		final JiraWorkflowMock workflowExisitng = new JiraWorkflowMock();
		workflowExisitng.setName(HubJiraConstants.HUB_JIRA_WORKFLOW);

		final WorkflowManagerMock workflowManager = new WorkflowManagerMock();

		workflowManager.addWorkflow(workflowExisitng);

		final WorkflowSchemeManagerMock workflowSchemeManager = new WorkflowSchemeManagerMock();

		final String jiraUser = "FakeUser";

		final UserManagerMock userManager = new UserManagerMock();
		final ApplicationUserMock user = new ApplicationUserMock();
		user.setName(jiraUser);
		userManager.setMockApplicationUser(user);

		final HubWorkflowSetup workflowSetup = new HubWorkflowSetup(settingService, workflowManager,
				workflowSchemeManager, userManager, jiraUser);

		assertEquals(workflowExisitng, workflowSetup.addHubWorkflowToJira());
		assertTrue(!workflowManager.getAttemptedCreateWorkflow());
	}

	@Test
	public void testAddHubWorkflowToJira() {
		final PluginSettingsMock settingsMock = new PluginSettingsMock();
		final JiraSettingsService settingService = new JiraSettingsService(settingsMock);


		final WorkflowManagerMock workflowManager = new WorkflowManagerMock();

		final WorkflowSchemeManagerMock workflowSchemeManager = new WorkflowSchemeManagerMock();

		final String jiraUser = "FakeUser";

		final UserManagerMock userManager = new UserManagerMock();
		final ApplicationUserMock user = new ApplicationUserMock();
		user.setName(jiraUser);
		userManager.setMockApplicationUser(user);

		final HubWorkflowSetup workflowSetup = new HubWorkflowSetup(settingService, workflowManager,
				workflowSchemeManager, userManager, jiraUser);

		final JiraWorkflow workflow = workflowSetup.addHubWorkflowToJira();

		assertNotNull(workflow);
		assertTrue(workflowManager.getAttemptedCreateWorkflow());
	}

	// TODO test addWorkflowToProjectsWorkflowScheme
}
