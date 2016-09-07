package com.blackducksoftware.integration.jira.task.setup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.common.HubProject;
import com.blackducksoftware.integration.jira.common.HubProjectMapping;
import com.blackducksoftware.integration.jira.common.JiraProject;
import com.blackducksoftware.integration.jira.config.HubJiraConfigSerializable;
import com.blackducksoftware.integration.jira.exception.JiraException;
import com.blackducksoftware.integration.jira.mocks.ApplicationUserMock;
import com.blackducksoftware.integration.jira.mocks.AvatarManagerMock;
import com.blackducksoftware.integration.jira.mocks.ConstantsManagerMock;
import com.blackducksoftware.integration.jira.mocks.FieldConfigSchemeMock;
import com.blackducksoftware.integration.jira.mocks.GroupManagerMock;
import com.blackducksoftware.integration.jira.mocks.IssueTypeSchemeManagerMock;
import com.blackducksoftware.integration.jira.mocks.JiraServicesMock;
import com.blackducksoftware.integration.jira.mocks.PluginSettingsMock;
import com.blackducksoftware.integration.jira.mocks.ProjectManagerMock;
import com.blackducksoftware.integration.jira.mocks.UserManagerMock;
import com.blackducksoftware.integration.jira.mocks.UserMock;
import com.blackducksoftware.integration.jira.mocks.UserUtilMock;
import com.blackducksoftware.integration.jira.mocks.issue.IssueTypeMock;
import com.blackducksoftware.integration.jira.mocks.workflow.AssignableWorkflowSchemeBuilderMock;
import com.blackducksoftware.integration.jira.mocks.workflow.AssignableWorkflowSchemeMock;
import com.blackducksoftware.integration.jira.mocks.workflow.WorkflowManagerMock;
import com.blackducksoftware.integration.jira.mocks.workflow.WorkflowSchemeManagerMock;
import com.blackducksoftware.integration.jira.task.JiraSettingsService;
import com.blackducksoftware.integration.jira.task.JiraTask;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;

public class JiraTaskSetupTest {

	private static final String HUB_PROJECT_NAME = "Test Hub Project";
	private static final String JIRA_PROJECT_NAME = ProjectManagerMock.JIRA_PROJECT_PREFIX;
	private static final long JIRA_PROJECT_ID = ProjectManagerMock.JIRA_PROJECT_ID_BASE;
	private static final String JIRA_USER = "Jira User";
	private static final String HUB_WORKFLOW_NAME = "Hub Workflow";

	@Test
	public void testServerSetupIssueTypesAlreadyCreated() throws JiraException {
		final JiraTask jiraTask = new JiraTask();

		final GroupManagerMock groupManager = getGroupManagerMock(false);
		final WorkflowManagerMock workflowManager = getWorkflowManagerMock();
		final WorkflowSchemeManagerMock workflowSchemeManager = getWorkflowSchemeManagerMock(false);
		final UserManagerMock userManager = getUserManagerMockManagerMock();
		final ProjectManagerMock projectManager = getProjectManagerMock(true);
		final AvatarManagerMock avatarManager = getAvatarManagerMock();
		final ConstantsManagerMock constantsManager = getConstantsManagerMock();
		final IssueTypeSchemeManagerMock issueTypeSchemeManager = getIssueTypeSchemeManagerMock();
		final FieldConfigScheme fieldConfigScheme = new FieldConfigSchemeMock();
		issueTypeSchemeManager.setConfigScheme(fieldConfigScheme);

		final Collection<IssueType> issueTypes = getIssueTypes(true);
		issueTypeSchemeManager.setIssueTypes(issueTypes);

		final UserUtil userUtil = getUserUtil(true);
		final JiraServices jiraServices = getJiraServices(groupManager, workflowManager, workflowSchemeManager,
				userManager, projectManager, avatarManager, constantsManager, issueTypeSchemeManager, issueTypes,
				userUtil);
		final PluginSettingsMock settingsMock = new PluginSettingsMock();

		final JiraSettingsService settingService = new JiraSettingsService(settingsMock);

		final String mappingJson = getProjectMappingJson(true, JIRA_PROJECT_NAME, JIRA_PROJECT_ID);

		jiraTask.jiraSetup(jiraServices, settingService, mappingJson);

		assertTrue(groupManager.getGroupCreateAttempted());
		assertTrue(workflowManager.getAttemptedCreateWorkflow());
		assertTrue(workflowSchemeManager.getAttemptedWorkflowUpdate());
		assertEquals(0, constantsManager.getIssueTypesCreatedCount());
	}

	@Test
	public void testServerSetupIssueTypesNotAlreadyCreated() throws JiraException {
		final JiraTask jiraTask = new JiraTask();

		final GroupManagerMock groupManager = getGroupManagerMock(false);
		final WorkflowManagerMock workflowManager = getWorkflowManagerMock();
		final WorkflowSchemeManagerMock workflowSchemeManager = getWorkflowSchemeManagerMock(false);
		final UserManagerMock userManager = getUserManagerMockManagerMock();
		final ProjectManagerMock projectManager = getProjectManagerMock(true);
		final AvatarManagerMock avatarManager = getAvatarManagerMock();
		final ConstantsManagerMock constantsManager = getConstantsManagerMock();
		final IssueTypeSchemeManagerMock issueTypeSchemeManager = getIssueTypeSchemeManagerMock();
		final FieldConfigScheme fieldConfigScheme = new FieldConfigSchemeMock();
		issueTypeSchemeManager.setConfigScheme(fieldConfigScheme);

		final Collection<IssueType> issueTypes = getIssueTypes(false);
		issueTypeSchemeManager.setIssueTypes(issueTypes);

		final UserUtil userUtil = getUserUtil(true);
		final JiraServices jiraServices = getJiraServices(groupManager, workflowManager, workflowSchemeManager,
				userManager, projectManager, avatarManager, constantsManager, issueTypeSchemeManager, issueTypes,
				userUtil);
		final PluginSettingsMock settingsMock = new PluginSettingsMock();

		final JiraSettingsService settingService = new JiraSettingsService(settingsMock);

		final String mappingJson = getProjectMappingJson(true, JIRA_PROJECT_NAME, JIRA_PROJECT_ID);

		jiraTask.jiraSetup(jiraServices, settingService, mappingJson);

		assertTrue(groupManager.getGroupCreateAttempted());
		assertTrue(workflowManager.getAttemptedCreateWorkflow());
		assertTrue(workflowSchemeManager.getAttemptedWorkflowUpdate());
		assertEquals(2, constantsManager.getIssueTypesCreatedCount());
	}

	private IssueTypeSchemeManagerMock getIssueTypeSchemeManagerMock() {
		return new IssueTypeSchemeManagerMock();
	}
	private ConstantsManagerMock getConstantsManagerMock() {
		return new ConstantsManagerMock();
	}
	private AvatarManagerMock getAvatarManagerMock() {
		return new AvatarManagerMock();
	}

	private UserUtil getUserUtil(final boolean hasSystemAdmin) {
		final UserUtilMock userUtil = new UserUtilMock();
		if (hasSystemAdmin) {
			final UserMock user = new UserMock();
			user.setName(JIRA_USER);
			userUtil.setUser(user);
		}
		return userUtil;
	}

	private WorkflowManagerMock getWorkflowManagerMock() {
		final WorkflowManagerMock workflowManagerMock = new WorkflowManagerMock();

		return workflowManagerMock;
	}

	private WorkflowSchemeManagerMock getWorkflowSchemeManagerMock(final boolean workflowMappedToOurIssueTypes) {
		final WorkflowSchemeManagerMock workflowSchemeManagerMock = new WorkflowSchemeManagerMock();


		final AssignableWorkflowSchemeMock hubWorkflow = new AssignableWorkflowSchemeMock();
		hubWorkflow.setName(HUB_WORKFLOW_NAME);
		if (workflowMappedToOurIssueTypes) {
			hubWorkflow.addMappingIssueToWorkflow(HubJiraConstants.HUB_POLICY_VIOLATION_ISSUE, HUB_WORKFLOW_NAME);
			hubWorkflow.addMappingIssueToWorkflow(HubJiraConstants.HUB_VULNERABILITY_ISSUE, HUB_WORKFLOW_NAME);
		} else {
			hubWorkflow.addMappingIssueToWorkflow(HubJiraConstants.HUB_POLICY_VIOLATION_ISSUE, "Fake Workflow");
			hubWorkflow.addMappingIssueToWorkflow(HubJiraConstants.HUB_VULNERABILITY_ISSUE, "Fake Workflow");
		}
		final AssignableWorkflowSchemeBuilderMock builder = new AssignableWorkflowSchemeBuilderMock();
		builder.setWorkflowScheme(hubWorkflow);

		hubWorkflow.setBuilder(builder);

		workflowSchemeManagerMock.setAssignableWorkflowScheme(hubWorkflow);

		return workflowSchemeManagerMock;
	}

	private UserManagerMock getUserManagerMockManagerMock() {
		final UserManagerMock userManager = new UserManagerMock();
		final ApplicationUserMock applicationUser = new ApplicationUserMock();
		applicationUser.setName(JIRA_USER);
		userManager.setMockApplicationUser(applicationUser);
		return userManager;
	}

	private ProjectManagerMock getProjectManagerMock(final boolean hasJiraProjects) {
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();
		if (hasJiraProjects) {
			projectManagerMock.setProjectObjects(ProjectManagerMock.getTestProjectObjectsWithTaskIssueType());
		}
		return projectManagerMock;
	}

	private GroupManagerMock getGroupManagerMock(final boolean groupAlreadyExists) {
		final GroupManagerMock groupManager = new GroupManagerMock();
		if (groupAlreadyExists) {
			groupManager.addGroupByName(HubJiraConstants.HUB_JIRA_GROUP);
		}
		return groupManager;
	}

	private Collection<IssueType> getIssueTypes(final boolean bdIssueTypesAlreadyAdded) {
		final Collection<IssueType> issueTypes = new ArrayList<IssueType>();
		final IssueTypeMock issueType = new IssueTypeMock();
		issueType.setName("Task");
		issueType.setId("Task");
		issueTypes.add(issueType);
		if (bdIssueTypesAlreadyAdded) {
			final IssueTypeMock policyViolationIssue = new IssueTypeMock();
			policyViolationIssue.setName(HubJiraConstants.HUB_POLICY_VIOLATION_ISSUE);
			policyViolationIssue.setId(HubJiraConstants.HUB_POLICY_VIOLATION_ISSUE);
			issueTypes.add(policyViolationIssue);
			final IssueTypeMock securityIssue = new IssueTypeMock();
			securityIssue.setName(HubJiraConstants.HUB_VULNERABILITY_ISSUE);
			securityIssue.setId(HubJiraConstants.HUB_VULNERABILITY_ISSUE);
			issueTypes.add(securityIssue);
		}
		return issueTypes;
	}

	private JiraServices getJiraServices(final GroupManagerMock groupManager, final WorkflowManager workflowManager,
			final WorkflowSchemeManager workflowSchemeManager, final UserManager userManager,
			final ProjectManager projectManager, final AvatarManager avatarManager,
			final ConstantsManager constantsManager, final IssueTypeSchemeManager issueTypeSchemeManager,
			final Collection<IssueType> issueTypes, final UserUtil userUtil) {
		final JiraServicesMock jiraServices = new JiraServicesMock();
		jiraServices.setGroupManager(groupManager);
		jiraServices.setWorkflowManager(workflowManager);
		jiraServices.setWorkflowSchemeManager(workflowSchemeManager);
		jiraServices.setUserManager(userManager);
		jiraServices.setProjectManager(projectManager);
		jiraServices.setAvatarManager(avatarManager);
		jiraServices.setConstantsManager(constantsManager);
		jiraServices.setIssueTypeSchemeManager(issueTypeSchemeManager);
		jiraServices.setIssueTypes(issueTypes);
		jiraServices.setUserUtil(userUtil);
		return jiraServices;
	}

	private String getProjectMappingJson(final boolean hasProjectMapping, final String jiraProjectName,
			final long jiraProjectId) {
		final Set<HubProjectMapping> mappings = new HashSet<>();
		if(hasProjectMapping){
			final HubProjectMapping mapping = new HubProjectMapping();
			final JiraProject jiraProject = new JiraProject();
			jiraProject.setProjectName(jiraProjectName);
			jiraProject.setProjectId(jiraProjectId);
			mapping.setJiraProject(jiraProject);
			final HubProject hubProject = new HubProject();
			hubProject.setProjectName(HUB_PROJECT_NAME);
			mapping.setHubProject(hubProject);
			mappings.add(mapping);
		}
		final HubJiraConfigSerializable config = new HubJiraConfigSerializable();
		config.setHubProjectMappings(mappings);

		return config.getHubProjectMappingsJson();
	}
}