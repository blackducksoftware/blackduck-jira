package com.blackducksoftware.integration.jira.task.setup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
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
import com.blackducksoftware.integration.jira.mocks.GroupManagerMock;
import com.blackducksoftware.integration.jira.mocks.JiraServicesMock;
import com.blackducksoftware.integration.jira.mocks.PluginSettingsMock;
import com.blackducksoftware.integration.jira.mocks.ProjectManagerMock;
import com.blackducksoftware.integration.jira.mocks.UserManagerMock;
import com.blackducksoftware.integration.jira.mocks.UserMock;
import com.blackducksoftware.integration.jira.mocks.UserUtilMock;
import com.blackducksoftware.integration.jira.mocks.field.CustomFieldManagerMock;
import com.blackducksoftware.integration.jira.mocks.field.EditableFieldLayoutMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldConfigSchemeMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldConfigurationSchemeMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldLayoutItemMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldLayoutManagerMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldLayoutSchemeMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldManagerMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldScreenManagerMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldScreenMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldScreenSchemeItemMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldScreenSchemeManagerMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldScreenSchemeMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldScreenTabMock;
import com.blackducksoftware.integration.jira.mocks.field.OrderableFieldMock;
import com.blackducksoftware.integration.jira.mocks.issue.IssueTypeMock;
import com.blackducksoftware.integration.jira.mocks.issue.IssueTypeSchemeManagerMock;
import com.blackducksoftware.integration.jira.mocks.issue.IssueTypeScreenSchemeManagerMock;
import com.blackducksoftware.integration.jira.mocks.issue.IssueTypeScreenSchemeMock;
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

	// @Test TODO
	public void testServerSetupIssueTypesAlreadyCreated() throws JiraException {
		JiraTask jiraTask = new JiraTask();

		final GroupManagerMock groupManager = getGroupManagerMock(false);
		final WorkflowManagerMock workflowManager = getWorkflowManagerMock();
		final WorkflowSchemeManagerMock workflowSchemeManager = getWorkflowSchemeManagerMock(false);
		final UserManagerMock userManager = getUserManagerMockManagerMock();
		final ProjectManagerMock projectManager = getProjectManagerMock(true);
		final AvatarManagerMock avatarManager = getAvatarManagerMock();
		final ConstantsManagerMock constantsManager = getConstantsManagerMock();
		final IssueTypeSchemeManagerMock issueTypeSchemeManager = getIssueTypeSchemeManagerMock(constantsManager);
		final FieldConfigScheme fieldConfigScheme = new FieldConfigSchemeMock();
		issueTypeSchemeManager.setConfigScheme(fieldConfigScheme);
		final FieldLayoutManagerMock fieldLayoutManager = getFieldLayoutManagerMock();
		final IssueTypeScreenSchemeManagerMock issueTypeScreenSchemeManager = new IssueTypeScreenSchemeManagerMock();
		final IssueTypeScreenSchemeMock issueTypeScreenScheme = new IssueTypeScreenSchemeMock();
		issueTypeScreenSchemeManager.setIssueTypeScreenScheme(issueTypeScreenScheme);
		final FieldLayoutSchemeMock fieldLayoutScheme = new FieldLayoutSchemeMock();
		fieldLayoutScheme.setName("Field Layout Scheme");
		fieldLayoutManager.setFieldLayoutScheme(fieldLayoutScheme);

		final FieldConfigurationSchemeMock projectFieldConfigScheme = new FieldConfigurationSchemeMock();
		projectFieldConfigScheme.setName("Project Field Config Scheme");
		projectFieldConfigScheme.setId(356l);
		fieldLayoutManager.setProjectFieldConfigScheme(projectFieldConfigScheme);

		final EditableFieldLayoutMock fieldLayout = new EditableFieldLayoutMock();
		fieldLayout.setName("Hub Field Configuration");
		fieldLayout.setDescription("mock");
		final List<FieldLayoutItem> fields = new ArrayList<>();
		final FieldLayoutItemMock field = new FieldLayoutItemMock();
		field.setIsRequired(false);
		final OrderableFieldMock orderableField = new OrderableFieldMock();
		orderableField.setId("1");
		orderableField.setName("Policy Rule");
		field.setOrderableField(orderableField);
		fields.add(field);
		fieldLayout.setFieldLayoutItems(fields);
		fieldLayoutManager.addEditableFieldLayout(fieldLayout);

		final Collection<IssueType> issueTypes = getIssueTypes(true);
		issueTypeSchemeManager.setIssueTypes(issueTypes);

		final UserUtil userUtil = getUserUtil(true);

		final CustomFieldManagerMock customFieldManager = new CustomFieldManagerMock();
		final FieldManagerMock fieldManager = new FieldManagerMock();
		final FieldScreenManagerMock fieldScreenManager = new FieldScreenManagerMock();
		final FieldScreenSchemeManagerMock fieldScreenSchemeManager = new FieldScreenSchemeManagerMock();

		fieldScreenManager.setDefaultFieldScreen(getDefaultFieldScreen());

		final PluginSettingsMock settingsMock = new PluginSettingsMock();

		final JiraSettingsService settingService = new JiraSettingsService(settingsMock);

		final JiraServices jiraServices = getJiraServices(groupManager, workflowManager, workflowSchemeManager,
				userManager, projectManager, avatarManager, constantsManager, issueTypeSchemeManager,
				fieldLayoutManager, issueTypeScreenSchemeManager, issueTypes,
				userUtil, customFieldManager, fieldManager, fieldScreenManager, fieldScreenSchemeManager);

		HubFieldScreenSchemeSetup fieldScreenSchemeSetup = new HubFieldScreenSchemeSetup(settingService, jiraServices);
		fieldScreenSchemeSetup = Mockito.spy(fieldScreenSchemeSetup);

		jiraTask = Mockito.spy(jiraTask);

		mockCreationMethods(jiraTask, fieldScreenSchemeSetup);

		final String mappingJson = getProjectMappingJson(true, JIRA_PROJECT_NAME, JIRA_PROJECT_ID);

		jiraTask.jiraSetup(jiraServices, settingService, mappingJson);

		assertTrue(groupManager.getGroupCreateAttempted());
		assertTrue(workflowManager.getAttemptedCreateWorkflow());
		assertTrue(workflowSchemeManager.getAttemptedWorkflowUpdate());
		assertEquals(0, constantsManager.getIssueTypesCreatedCount());

		assertTrue(customFieldManager.getCustomFieldObjects().size() == 5);
		for (final FieldScreen fieldScreen : fieldScreenManager.getUpdatedScreens()) {
			final FieldScreenMock fieldScreenMock = (FieldScreenMock) fieldScreen;
			assertTrue(fieldScreenMock.getAttemptedScreenStore());
		}
		assertTrue(fieldScreenManager.getUpdatedTabs().size() == 2);

		for (final FieldScreenTab tab : fieldScreenManager.getUpdatedTabs()) {
			final String screenName = tab.getFieldScreen().getName();
			if (screenName.equals(HubFieldScreenSchemeSetup.HUB_POLICY_SCREEN_NAME)) {
				assertTrue(tab.getFieldScreenLayoutItems().size() == 9);
			} else if (screenName.equals(HubFieldScreenSchemeSetup.HUB_POLICY_SCREEN_NAME)) {
				assertTrue(tab.getFieldScreenLayoutItems().size() == 8);
			}
		}
		assertTrue(fieldScreenManager.getUpdatedScreens().size() == 2);
		for (final FieldScreenScheme fieldScreenScheme : fieldScreenSchemeManager.getUpdatedSchemes()) {
			final FieldScreenSchemeMock fieldScreenSchemeMock = (FieldScreenSchemeMock) fieldScreenScheme;
			assertTrue(fieldScreenSchemeMock.getAttemptedScreenSchemeStore());

			for (final FieldScreenSchemeItem currentSchemeItem : fieldScreenScheme.getFieldScreenSchemeItems()) {
				assertTrue(currentSchemeItem.getFieldScreen().getName()
						.equals(HubFieldScreenSchemeSetup.HUB_POLICY_SCREEN_NAME)
						|| currentSchemeItem.getFieldScreen().getName()
						.equals(HubFieldScreenSchemeSetup.HUB_SECURITY_SCREEN_NAME));
			}
		}
		assertTrue(fieldScreenSchemeManager.getUpdatedSchemes().size() == 2);
		assertTrue(fieldScreenSchemeManager.getUpdatedSchemeItems().size() == 6);
		assertNull(settingsMock.get(HubJiraConstants.HUB_JIRA_ERROR));

		// TODO: verify: Adds Issue Types to Project's Issue Type Scheme,
		// creates BDS Field Configuration Scheme
	}

	// @Test TODO
	public void testServerSetupIssueTypesNotAlreadyCreated() throws JiraException {
		JiraTask jiraTask = new JiraTask();

		final GroupManagerMock groupManager = getGroupManagerMock(false);
		final WorkflowManagerMock workflowManager = getWorkflowManagerMock();
		final WorkflowSchemeManagerMock workflowSchemeManager = getWorkflowSchemeManagerMock(false);
		final UserManagerMock userManager = getUserManagerMockManagerMock();
		final ProjectManagerMock projectManager = getProjectManagerMock(true);
		final AvatarManagerMock avatarManager = getAvatarManagerMock();
		final ConstantsManagerMock constantsManager = getConstantsManagerMock();
		final IssueTypeSchemeManagerMock issueTypeSchemeManager = getIssueTypeSchemeManagerMock(constantsManager);
		final FieldConfigScheme fieldConfigScheme = new FieldConfigSchemeMock();
		issueTypeSchemeManager.setConfigScheme(fieldConfigScheme);
		final FieldLayoutManagerMock fieldLayoutManager = getFieldLayoutManagerMock();
		final IssueTypeScreenSchemeManagerMock issueTypeScreenSchemeManager = new IssueTypeScreenSchemeManagerMock();
		final IssueTypeScreenSchemeMock issueTypeScreenScheme = new IssueTypeScreenSchemeMock();
		issueTypeScreenSchemeManager.setIssueTypeScreenScheme(issueTypeScreenScheme);
		final FieldLayoutSchemeMock fieldLayoutScheme = new FieldLayoutSchemeMock();
		fieldLayoutScheme.setName("Field Layout Scheme");
		fieldLayoutManager.setFieldLayoutScheme(fieldLayoutScheme);

		final FieldConfigurationSchemeMock projectFieldConfigScheme = new FieldConfigurationSchemeMock();
		projectFieldConfigScheme.setName("Project Field Config Scheme");
		projectFieldConfigScheme.setId(356l);
		fieldLayoutManager.setProjectFieldConfigScheme(projectFieldConfigScheme);

		final EditableFieldLayoutMock fieldLayout = new EditableFieldLayoutMock();
		fieldLayout.setName("Hub Field Configuration");
		fieldLayout.setDescription("mock");
		final List<FieldLayoutItem> fields = new ArrayList<>();
		final FieldLayoutItemMock field = new FieldLayoutItemMock();
		field.setIsRequired(false);
		final OrderableFieldMock orderableField = new OrderableFieldMock();
		orderableField.setId("1");
		orderableField.setName("Policy Rule");
		field.setOrderableField(orderableField);
		fields.add(field);
		fieldLayout.setFieldLayoutItems(fields);
		fieldLayoutManager.addEditableFieldLayout(fieldLayout);

		final Collection<IssueType> issueTypes = getIssueTypes(false);
		issueTypeSchemeManager.setIssueTypes(issueTypes);

		final UserUtil userUtil = getUserUtil(true);

		final CustomFieldManagerMock customFieldManager = new CustomFieldManagerMock();
		final FieldManagerMock fieldManager = new FieldManagerMock();
		final FieldScreenManagerMock fieldScreenManager = new FieldScreenManagerMock();
		final FieldScreenSchemeManagerMock fieldScreenSchemeManager = new FieldScreenSchemeManagerMock();

		fieldScreenManager.setDefaultFieldScreen(getDefaultFieldScreen());

		final PluginSettingsMock settingsMock = new PluginSettingsMock();

		final JiraSettingsService settingService = new JiraSettingsService(settingsMock);

		final JiraServices jiraServices = getJiraServices(groupManager, workflowManager, workflowSchemeManager,
				userManager, projectManager, avatarManager, constantsManager, issueTypeSchemeManager,
				fieldLayoutManager, issueTypeScreenSchemeManager, issueTypes, userUtil, customFieldManager,
				fieldManager, fieldScreenManager, fieldScreenSchemeManager);

		HubFieldScreenSchemeSetup fieldScreenSchemeSetup = new HubFieldScreenSchemeSetup(settingService, jiraServices);
		fieldScreenSchemeSetup = Mockito.spy(fieldScreenSchemeSetup);

		jiraTask = Mockito.spy(jiraTask);

		mockCreationMethods(jiraTask, fieldScreenSchemeSetup);

		final String mappingJson = getProjectMappingJson(true, JIRA_PROJECT_NAME, JIRA_PROJECT_ID);

		jiraTask.jiraSetup(jiraServices, settingService, mappingJson);

		assertTrue(groupManager.getGroupCreateAttempted());
		assertTrue(workflowManager.getAttemptedCreateWorkflow());
		assertTrue(workflowSchemeManager.getAttemptedWorkflowUpdate());
		assertEquals(2, constantsManager.getIssueTypesCreatedCount());
		System.out.println(customFieldManager.getCustomFieldObjects().size());
		assertTrue(customFieldManager.getCustomFieldObjects().size() == 5);
		for (final FieldScreen fieldScreen : fieldScreenManager.getUpdatedScreens()) {
			final FieldScreenMock fieldScreenMock = (FieldScreenMock) fieldScreen;
			assertTrue(fieldScreenMock.getAttemptedScreenStore());
		}
		assertTrue(fieldScreenManager.getUpdatedTabs().size() == 2);

		for (final FieldScreenTab tab : fieldScreenManager.getUpdatedTabs()) {
			final String screenName = tab.getFieldScreen().getName();
			if (screenName.equals(HubFieldScreenSchemeSetup.HUB_POLICY_SCREEN_NAME)) {
				assertTrue(tab.getFieldScreenLayoutItems().size() == 9);
			} else if (screenName.equals(HubFieldScreenSchemeSetup.HUB_POLICY_SCREEN_NAME)) {
				assertTrue(tab.getFieldScreenLayoutItems().size() == 8);
			}
		}
		assertTrue(fieldScreenManager.getUpdatedScreens().size() == 2);
		for (final FieldScreenScheme fieldScreenScheme : fieldScreenSchemeManager.getUpdatedSchemes()) {
			final FieldScreenSchemeMock fieldScreenSchemeMock = (FieldScreenSchemeMock) fieldScreenScheme;
			assertTrue(fieldScreenSchemeMock.getAttemptedScreenSchemeStore());

			for (final FieldScreenSchemeItem currentSchemeItem : fieldScreenScheme.getFieldScreenSchemeItems()) {
				assertTrue(currentSchemeItem.getFieldScreen().getName()
						.equals(HubFieldScreenSchemeSetup.HUB_POLICY_SCREEN_NAME)
						|| currentSchemeItem.getFieldScreen().getName()
						.equals(HubFieldScreenSchemeSetup.HUB_SECURITY_SCREEN_NAME));
			}
		}
		assertTrue(fieldScreenSchemeManager.getUpdatedSchemes().size() == 2);
		assertTrue(fieldScreenSchemeManager.getUpdatedSchemeItems().size() == 6);
		assertNull(settingsMock.get(HubJiraConstants.HUB_JIRA_ERROR));

		// TODO: verify: Adds Issue Types to Project's Issue Type Scheme,
		// creates BDS Field Configuration Scheme (only if it doesn't exist)
	}

	private FieldScreen getDefaultFieldScreen() {
		final FieldScreenMock fieldScreen = new FieldScreenMock();
		final FieldScreenTabMock defaultTab1 = new FieldScreenTabMock();
		defaultTab1.setFieldScreen(fieldScreen);
		defaultTab1.addFieldScreenLayoutItem("Default Field 1");
		defaultTab1.addFieldScreenLayoutItem("Default Field 2");
		defaultTab1.addFieldScreenLayoutItem("Default Field 3");
		final FieldScreenTabMock defaultTab2 = new FieldScreenTabMock();
		defaultTab2.setFieldScreen(fieldScreen);
		defaultTab2.addFieldScreenLayoutItem("Default Field 1");
		defaultTab2.addFieldScreenLayoutItem("Default Field 2");
		defaultTab2.addFieldScreenLayoutItem("Default Field 3");
		defaultTab2.addFieldScreenLayoutItem("Default Field 4");

		fieldScreen.addTab(defaultTab1);
		fieldScreen.addTab(defaultTab2);

		fieldScreen.setName("Default Screen");
		return fieldScreen;
	}

	private void mockCreationMethods(final JiraTask jiraTask,final HubFieldScreenSchemeSetup fieldConfigSetup) {
		Mockito.when(fieldConfigSetup.createNewScreenImpl(Mockito.any(FieldScreenManager.class)))
		.thenAnswer(new Answer<FieldScreen>() {
			@Override
			public FieldScreen answer(final InvocationOnMock invocation) throws Throwable {
				return new FieldScreenMock();
			}
		});

		Mockito.when(fieldConfigSetup.createNewScreenSchemeImpl(Mockito.any(FieldScreenSchemeManager.class)))
		.thenAnswer(new Answer<FieldScreenScheme>() {
			@Override
			public FieldScreenScheme answer(final InvocationOnMock invocation) throws Throwable {
				return new FieldScreenSchemeMock();
			}
		});

		Mockito.when(fieldConfigSetup.createNewFieldScreenSchemeItemImpl(Mockito.any(FieldScreenSchemeManager.class),
				Mockito.any(FieldScreenManager.class))).thenAnswer(new Answer<FieldScreenSchemeItem>() {
					@Override
					public FieldScreenSchemeItem answer(final InvocationOnMock invocation) throws Throwable {
						return new FieldScreenSchemeItemMock();
					}
				});

		Mockito.when(jiraTask.getHubFieldScreenSchemeSetup(Mockito.any(JiraSettingsService.class),
				Mockito.any(JiraServices.class))).thenReturn(fieldConfigSetup);
	}

	private FieldLayoutManagerMock getFieldLayoutManagerMock() {
		return new FieldLayoutManagerMock();
	}

	private IssueTypeSchemeManagerMock getIssueTypeSchemeManagerMock(final ConstantsManagerMock constantsManager) {
		return new IssueTypeSchemeManagerMock(constantsManager);
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
		issueType.setValue(Mockito.mock(GenericValue.class));
		issueTypes.add(issueType);
		if (bdIssueTypesAlreadyAdded) {
			final IssueTypeMock policyViolationIssue = new IssueTypeMock();
			policyViolationIssue.setName(HubJiraConstants.HUB_POLICY_VIOLATION_ISSUE);
			policyViolationIssue.setId(HubJiraConstants.HUB_POLICY_VIOLATION_ISSUE);
			policyViolationIssue.setValue(Mockito.mock(GenericValue.class));
			issueTypes.add(policyViolationIssue);
			final IssueTypeMock securityIssue = new IssueTypeMock();
			securityIssue.setName(HubJiraConstants.HUB_VULNERABILITY_ISSUE);
			securityIssue.setId(HubJiraConstants.HUB_VULNERABILITY_ISSUE);
			securityIssue.setValue(Mockito.mock(GenericValue.class));
			issueTypes.add(securityIssue);
		}
		return issueTypes;
	}

	private JiraServices getJiraServices(final GroupManagerMock groupManager, final WorkflowManager workflowManager,
			final WorkflowSchemeManager workflowSchemeManager, final UserManager userManager,
			final ProjectManager projectManager, final AvatarManager avatarManager,
			final ConstantsManager constantsManager, final IssueTypeSchemeManager issueTypeSchemeManager,
			final FieldLayoutManager fieldLayoutManager,
			final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager,
			final Collection<IssueType> issueTypes, final UserUtil userUtil,
			final CustomFieldManagerMock customFieldManager, final FieldManagerMock fieldManager,
			final FieldScreenManagerMock fieldScreenManager,
			final FieldScreenSchemeManagerMock fieldScreenSchemeManager) {

		final JiraServicesMock jiraServices = new JiraServicesMock();
		jiraServices.setGroupManager(groupManager);
		jiraServices.setWorkflowManager(workflowManager);
		jiraServices.setWorkflowSchemeManager(workflowSchemeManager);
		jiraServices.setUserManager(userManager);
		jiraServices.setProjectManager(projectManager);
		jiraServices.setAvatarManager(avatarManager);
		jiraServices.setConstantsManager(constantsManager);
		jiraServices.setIssueTypeSchemeManager(issueTypeSchemeManager);
		jiraServices.setFieldLayoutManager(fieldLayoutManager);
		jiraServices.setIssueTypes(issueTypes);
		jiraServices.setUserUtil(userUtil);
		jiraServices.setIssueTypeScreenSchemeManager(issueTypeScreenSchemeManager);
		jiraServices.setCustomFieldManager(customFieldManager);
		jiraServices.setFieldManager(fieldManager);
		jiraServices.setFieldScreenManager(fieldScreenManager);
		jiraServices.setFieldScreenSchemeManager(fieldScreenSchemeManager);
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
