package com.blackducksoftware.integration.jira.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.mocks.AssignableWorkflowSchemeBuilderMock;
import com.blackducksoftware.integration.jira.mocks.AssignableWorkflowSchemeMock;
import com.blackducksoftware.integration.jira.mocks.IssueTypeMock;
import com.blackducksoftware.integration.jira.mocks.JiraServicesMock;
import com.blackducksoftware.integration.jira.mocks.JiraWorkflowMock;
import com.blackducksoftware.integration.jira.mocks.PluginSettingsMock;
import com.blackducksoftware.integration.jira.mocks.ProjectMock;
import com.blackducksoftware.integration.jira.mocks.UserMock;
import com.blackducksoftware.integration.jira.mocks.UserUtilMock;
import com.blackducksoftware.integration.jira.mocks.WorkflowManagerMock;
import com.blackducksoftware.integration.jira.mocks.WorkflowSchemeManagerMock;

public class HubWorkflowSetupTest {

	@Test
	public void testAddHubWorkflowToJiraNoUser() {
		final PluginSettingsMock settingsMock = new PluginSettingsMock();
		final JiraSettingsService settingService = new JiraSettingsService(settingsMock);

		final WorkflowManagerMock workflowManager = new WorkflowManagerMock();

		final WorkflowSchemeManagerMock workflowSchemeManager = new WorkflowSchemeManagerMock();

		final UserUtilMock userUtil = new UserUtilMock();

		final JiraServicesMock services = new JiraServicesMock();
		services.setWorkflowManager(workflowManager);
		services.setWorkflowSchemeManager(workflowSchemeManager);
		services.setUserUtil(userUtil);

		final HubWorkflowSetup workflowSetup = new HubWorkflowSetup(settingService, services);

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

		final UserUtilMock userUtil = new UserUtilMock();
		final UserMock user = new UserMock();
		user.setName(jiraUser);
		userUtil.setUser(user);

		final JiraServicesMock services = new JiraServicesMock();
		services.setWorkflowManager(workflowManager);
		services.setWorkflowSchemeManager(workflowSchemeManager);
		services.setUserUtil(userUtil);

		final HubWorkflowSetup workflowSetup = new HubWorkflowSetup(settingService, services);

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

		final UserUtilMock userUtil = new UserUtilMock();
		final UserMock user = new UserMock();
		user.setName(jiraUser);
		userUtil.setUser(user);

		final JiraServicesMock services = new JiraServicesMock();
		services.setWorkflowManager(workflowManager);
		services.setWorkflowSchemeManager(workflowSchemeManager);
		services.setUserUtil(userUtil);

		final HubWorkflowSetup workflowSetup = new HubWorkflowSetup(settingService, services);

		final JiraWorkflow workflow = workflowSetup.addHubWorkflowToJira();

		assertNotNull(workflow);
		assertTrue(workflowManager.getAttemptedCreateWorkflow());
	}

	@Test
	public void testAddWorkflowToProjectsWorkflowSchemeNoWorkflow() {
		final PluginSettingsMock settingsMock = new PluginSettingsMock();
		final JiraSettingsService settingService = new JiraSettingsService(settingsMock);

		final String workflowName = "TestWorkflow";

		final WorkflowManagerMock workflowManager = new WorkflowManagerMock();

		final WorkflowSchemeManagerMock workflowSchemeManager = new WorkflowSchemeManagerMock();

		final String jiraUser = "FakeUser";

		final UserUtilMock userUtil = new UserUtilMock();
		final UserMock user = new UserMock();
		user.setName(jiraUser);
		userUtil.setUser(user);

		final JiraServicesMock services = new JiraServicesMock();
		services.setWorkflowManager(workflowManager);
		services.setWorkflowSchemeManager(workflowSchemeManager);
		services.setUserUtil(userUtil);

		final HubWorkflowSetup workflowSetup = new HubWorkflowSetup(settingService, services);

		final JiraWorkflowMock workflow = new JiraWorkflowMock();
		workflow.setName(workflowName);

		final ProjectMock project = new ProjectMock();
		project.setName("TestProject");

		workflowSetup.addWorkflowToProjectsWorkflowScheme(workflow, project, null);

		assertTrue(!workflowSchemeManager.getAttemptedWorkflowUpdate());
	}

	@Test
	public void testAddWorkflowToProjectsWorkflowSchemeNoIssueTypes() {
		final PluginSettingsMock settingsMock = new PluginSettingsMock();
		final JiraSettingsService settingService = new JiraSettingsService(settingsMock);

		final WorkflowManagerMock workflowManager = new WorkflowManagerMock();

		final String workflowName = "TestWorkflow";

		final AssignableWorkflowSchemeMock hubWorkflow = new AssignableWorkflowSchemeMock();
		hubWorkflow.setName(workflowName);

		final AssignableWorkflowSchemeBuilderMock builder = new AssignableWorkflowSchemeBuilderMock();
		builder.setWorkflowScheme(hubWorkflow);

		hubWorkflow.setBuilder(builder);

		final WorkflowSchemeManagerMock workflowSchemeManager = new WorkflowSchemeManagerMock();
		workflowSchemeManager.setAssignableWorkflowScheme(hubWorkflow);

		final String jiraUser = "FakeUser";

		final UserUtilMock userUtil = new UserUtilMock();
		final UserMock user = new UserMock();
		user.setName(jiraUser);
		userUtil.setUser(user);

		final JiraServicesMock services = new JiraServicesMock();
		services.setWorkflowManager(workflowManager);
		services.setWorkflowSchemeManager(workflowSchemeManager);
		services.setUserUtil(userUtil);

		final HubWorkflowSetup workflowSetup = new HubWorkflowSetup(settingService, services);

		final JiraWorkflowMock workflow = new JiraWorkflowMock();
		workflow.setName(workflowName);

		final ProjectMock project = new ProjectMock();
		project.setName("TestProject");

		workflowSetup.addWorkflowToProjectsWorkflowScheme(workflow, project, null);

		assertTrue(!workflowSchemeManager.getAttemptedWorkflowUpdate());
	}

	@Test
	public void testAddWorkflowToProjectsWorkflowSchemeIssueTypesNotInScheme() {
		final PluginSettingsMock settingsMock = new PluginSettingsMock();
		final JiraSettingsService settingService = new JiraSettingsService(settingsMock);

		final String workflowName = "TestWorkflow";

		final WorkflowManagerMock workflowManager = new WorkflowManagerMock();

		final String issueTypeName = "CustomIssueType";
		final String issueTypeId = "CustomIssueType";

		final List<IssueType> issueTypes = new ArrayList<>();
		final IssueTypeMock issueType = new IssueTypeMock();
		issueType.setName(issueTypeName);
		issueType.setId(issueTypeId);
		issueTypes.add(issueType);

		final AssignableWorkflowSchemeMock hubWorkflow = new AssignableWorkflowSchemeMock();
		hubWorkflow.setName(workflowName);

		final AssignableWorkflowSchemeBuilderMock builder = new AssignableWorkflowSchemeBuilderMock();
		builder.setWorkflowScheme(hubWorkflow);

		hubWorkflow.setBuilder(builder);

		final WorkflowSchemeManagerMock workflowSchemeManager = new WorkflowSchemeManagerMock();
		workflowSchemeManager.setAssignableWorkflowScheme(hubWorkflow);

		final String jiraUser = "FakeUser";

		final UserUtilMock userUtil = new UserUtilMock();
		final UserMock user = new UserMock();
		user.setName(jiraUser);
		userUtil.setUser(user);

		final JiraServicesMock services = new JiraServicesMock();
		services.setWorkflowManager(workflowManager);
		services.setWorkflowSchemeManager(workflowSchemeManager);
		services.setUserUtil(userUtil);

		final HubWorkflowSetup workflowSetup = new HubWorkflowSetup(settingService, services);

		final JiraWorkflowMock workflow = new JiraWorkflowMock();
		workflow.setName(workflowName);

		final ProjectMock project = new ProjectMock();
		project.setName("TestProject");

		workflowSetup.addWorkflowToProjectsWorkflowScheme(workflow, project, issueTypes);

		assertTrue(workflowSchemeManager.getAttemptedWorkflowUpdate());
	}

	@Test
	public void testAddWorkflowToProjectsWorkflowSchemeIssueTypesNeedUpdate() {
		final PluginSettingsMock settingsMock = new PluginSettingsMock();
		final JiraSettingsService settingService = new JiraSettingsService(settingsMock);

		final String workflowName = "TestWorkflow";

		final WorkflowManagerMock workflowManager = new WorkflowManagerMock();

		final String issueTypeName = "CustomIssueType";
		final String issueTypeId = "CustomIssueType";

		final List<IssueType> issueTypes = new ArrayList<>();
		final IssueTypeMock issueType = new IssueTypeMock();
		issueType.setName(issueTypeName);
		issueType.setId(issueTypeId);
		issueTypes.add(issueType);

		final AssignableWorkflowSchemeMock hubWorkflow = new AssignableWorkflowSchemeMock();
		hubWorkflow.setName(workflowName);

		hubWorkflow.addMappingIssueToWorkflow(issueTypeName, "FakeWorkflow");

		final AssignableWorkflowSchemeBuilderMock builder = new AssignableWorkflowSchemeBuilderMock();
		builder.setWorkflowScheme(hubWorkflow);

		hubWorkflow.setBuilder(builder);

		final WorkflowSchemeManagerMock workflowSchemeManager = new WorkflowSchemeManagerMock();
		workflowSchemeManager.setAssignableWorkflowScheme(hubWorkflow);

		final String jiraUser = "FakeUser";

		final UserUtilMock userUtil = new UserUtilMock();
		final UserMock user = new UserMock();
		user.setName(jiraUser);
		userUtil.setUser(user);

		final JiraServicesMock services = new JiraServicesMock();
		services.setWorkflowManager(workflowManager);
		services.setWorkflowSchemeManager(workflowSchemeManager);
		services.setUserUtil(userUtil);

		final HubWorkflowSetup workflowSetup = new HubWorkflowSetup(settingService, services);

		final JiraWorkflowMock workflow = new JiraWorkflowMock();
		workflow.setName(workflowName);

		final ProjectMock project = new ProjectMock();
		project.setName("TestProject");

		workflowSetup.addWorkflowToProjectsWorkflowScheme(workflow, project, issueTypes);

		assertTrue(workflowSchemeManager.getAttemptedWorkflowUpdate());

		final Map<String, String> mappings = hubWorkflow.getMappings();

		final String workflowNameMapped = mappings.get(issueTypeName);
		assertEquals(workflowName, workflowNameMapped);
	}

	@Test
	public void testAddWorkflowToProjectsWorkflowSchemeIssueTypesNoUpdate() {
		final PluginSettingsMock settingsMock = new PluginSettingsMock();
		final JiraSettingsService settingService = new JiraSettingsService(settingsMock);

		final String workflowName = "TestWorkflow";

		final WorkflowManagerMock workflowManager = new WorkflowManagerMock();

		final String issueTypeName = "CustomIssueType";
		final String issueTypeId = "CustomIssueType";

		final List<IssueType> issueTypes = new ArrayList<>();
		final IssueTypeMock issueType = new IssueTypeMock();
		issueType.setName(issueTypeName);
		issueType.setId(issueTypeId);
		issueTypes.add(issueType);

		final AssignableWorkflowSchemeMock hubWorkflow = new AssignableWorkflowSchemeMock();
		hubWorkflow.setName(workflowName);

		hubWorkflow.addMappingIssueToWorkflow(issueTypeName, workflowName);

		final AssignableWorkflowSchemeBuilderMock builder = new AssignableWorkflowSchemeBuilderMock();
		builder.setWorkflowScheme(hubWorkflow);

		hubWorkflow.setBuilder(builder);

		final WorkflowSchemeManagerMock workflowSchemeManager = new WorkflowSchemeManagerMock();
		workflowSchemeManager.setAssignableWorkflowScheme(hubWorkflow);

		final String jiraUser = "FakeUser";

		final UserUtilMock userUtil = new UserUtilMock();
		final UserMock user = new UserMock();
		user.setName(jiraUser);
		userUtil.setUser(user);

		final JiraServicesMock services = new JiraServicesMock();
		services.setWorkflowManager(workflowManager);
		services.setWorkflowSchemeManager(workflowSchemeManager);
		services.setUserUtil(userUtil);

		final HubWorkflowSetup workflowSetup = new HubWorkflowSetup(settingService, services);

		final JiraWorkflowMock workflow = new JiraWorkflowMock();
		workflow.setName(workflowName);

		final ProjectMock project = new ProjectMock();
		project.setName("TestProject");

		workflowSetup.addWorkflowToProjectsWorkflowScheme(workflow, project, issueTypes);

		assertTrue(!workflowSchemeManager.getAttemptedWorkflowUpdate());

		final Map<String, String> mappings = hubWorkflow.getMappings();

		final String workflowNameMapped = mappings.get(issueTypeName);
		assertEquals(workflowName, workflowNameMapped);
	}

}
