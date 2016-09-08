package com.blackducksoftware.integration.jira.task.setup;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.mocks.JiraServicesMock;
import com.blackducksoftware.integration.jira.mocks.PluginSettingsMock;
import com.blackducksoftware.integration.jira.mocks.field.CustomFieldManagerMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldManagerMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldScreenManagerMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldScreenMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldScreenSchemeItemMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldScreenSchemeManagerMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldScreenSchemeMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldScreenTabMock;
import com.blackducksoftware.integration.jira.mocks.issue.IssueTypeMock;
import com.blackducksoftware.integration.jira.task.JiraSettingsService;

public class HubFieldScreenSchemeSetupTest {

	@Test
	public void testAddHubFieldConfigurationToJiraFirstTimeCreateNullIssueTypes() {
		final PluginSettingsMock settingsMock = new PluginSettingsMock();
		final JiraSettingsService settingService = new JiraSettingsService(settingsMock);

		final JiraServicesMock jiraServices = new JiraServicesMock();

		final HubFieldScreenSchemeSetup fieldConfigSetup = new HubFieldScreenSchemeSetup(settingService, jiraServices);
		fieldConfigSetup.addHubFieldConfigurationToJira(null);

		assertNull(settingsMock.get(HubJiraConstants.HUB_JIRA_ERROR));
	}

	@Test
	public void testAddHubFieldConfigurationToJiraFirstTimeCreateNoIssueTypes() {
		final PluginSettingsMock settingsMock = new PluginSettingsMock();
		final JiraSettingsService settingService = new JiraSettingsService(settingsMock);

		final JiraServicesMock jiraServices = new JiraServicesMock();

		final HubFieldScreenSchemeSetup fieldConfigSetup = new HubFieldScreenSchemeSetup(settingService, jiraServices);
		fieldConfigSetup.addHubFieldConfigurationToJira(new ArrayList<IssueType>());

		assertNull(settingsMock.get(HubJiraConstants.HUB_JIRA_ERROR));
	}

	@Test
	public void testAddHubFieldConfigurationToJiraFirstTimeCreate() {
		final PluginSettingsMock settingsMock = new PluginSettingsMock();
		final JiraSettingsService settingService = new JiraSettingsService(settingsMock);

		final CustomFieldManagerMock customFieldManager = new CustomFieldManagerMock();
		final FieldManagerMock fieldManager = new FieldManagerMock();
		final FieldScreenManagerMock fieldScreenManager = new FieldScreenManagerMock();
		final FieldScreenSchemeManagerMock fieldScreenSchemeManager = new FieldScreenSchemeManagerMock();

		final JiraServicesMock jiraServices = new JiraServicesMock();
		jiraServices.setCustomFieldManager(customFieldManager);
		jiraServices.setFieldManager(fieldManager);
		jiraServices.setFieldScreenManager(fieldScreenManager);
		jiraServices.setFieldScreenSchemeManager(fieldScreenSchemeManager);

		final List<IssueType> issueTypes = getHubIssueTypes();

		HubFieldScreenSchemeSetup fieldConfigSetup = new HubFieldScreenSchemeSetup(settingService, jiraServices);
		fieldConfigSetup = Mockito.spy(fieldConfigSetup);

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

		fieldConfigSetup.addHubFieldConfigurationToJira(issueTypes);

		assertTrue(customFieldManager.getCustomFieldObjects().size() == 5);
		for (final FieldScreen fieldScreen : fieldScreenManager.getUpdatedScreens()) {
			final FieldScreenMock fieldScreenMock = (FieldScreenMock) fieldScreen;
			assertTrue(fieldScreenMock.getAttemptedScreenStore());
		}
		assertTrue(fieldScreenManager.getUpdatedTabs().size() == 2);

		for (final FieldScreenTab tab : fieldScreenManager.getUpdatedTabs()) {
			final String screenName = tab.getFieldScreen().getName();
			if (screenName.equals(HubFieldScreenSchemeSetup.HUB_POLICY_SCREEN_NAME)) {
				assertTrue(tab.getFieldScreenLayoutItems().size() == 5);
			} else if (screenName.equals(HubFieldScreenSchemeSetup.HUB_POLICY_SCREEN_NAME)) {
				assertTrue(tab.getFieldScreenLayoutItems().size() == 4);
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
	}

	@Test
	public void testAddHubFieldConfigurationToJiraFirstTimeCreateWithDefaultTabsAndFields() {
		final PluginSettingsMock settingsMock = new PluginSettingsMock();
		final JiraSettingsService settingService = new JiraSettingsService(settingsMock);

		final CustomFieldManagerMock customFieldManager = new CustomFieldManagerMock();
		final FieldManagerMock fieldManager = new FieldManagerMock();
		final FieldScreenManagerMock fieldScreenManager = new FieldScreenManagerMock();
		final FieldScreenSchemeManagerMock fieldScreenSchemeManager = new FieldScreenSchemeManagerMock();

		fieldScreenManager.setDefaultFieldScreen(getDefaultFieldScreen());

		final JiraServicesMock jiraServices = new JiraServicesMock();
		jiraServices.setCustomFieldManager(customFieldManager);
		jiraServices.setFieldManager(fieldManager);
		jiraServices.setFieldScreenManager(fieldScreenManager);
		jiraServices.setFieldScreenSchemeManager(fieldScreenSchemeManager);

		final List<IssueType> issueTypes = getHubIssueTypes();

		HubFieldScreenSchemeSetup fieldConfigSetup = new HubFieldScreenSchemeSetup(settingService, jiraServices);
		fieldConfigSetup = Mockito.spy(fieldConfigSetup);


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

		fieldConfigSetup.addHubFieldConfigurationToJira(issueTypes);

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
	}

	@Test
	public void testAddHubFieldConfigurationToJiraWithUserChanges() throws Exception {
		final PluginSettingsMock settingsMock = new PluginSettingsMock();
		final JiraSettingsService settingService = new JiraSettingsService(settingsMock);

		final CustomFieldManagerMock customFieldManager = new CustomFieldManagerMock();
		final FieldManagerMock fieldManager = new FieldManagerMock();
		final FieldScreenManagerMock fieldScreenManager = new FieldScreenManagerMock();
		final FieldScreenSchemeManagerMock fieldScreenSchemeManager = new FieldScreenSchemeManagerMock();
		final FieldScreen defaultScreen = getDefaultFieldScreen();
		fieldScreenManager.setDefaultFieldScreen(defaultScreen);

		final JiraServicesMock jiraServices = new JiraServicesMock();
		jiraServices.setCustomFieldManager(customFieldManager);
		jiraServices.setFieldManager(fieldManager);
		jiraServices.setFieldScreenManager(fieldScreenManager);
		jiraServices.setFieldScreenSchemeManager(fieldScreenSchemeManager);

		final List<IssueType> issueTypes = getHubIssueTypes();

		HubFieldScreenSchemeSetup fieldConfigSetup = new HubFieldScreenSchemeSetup(settingService, jiraServices);
		fieldConfigSetup = Mockito.spy(fieldConfigSetup);

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

		fieldConfigSetup.addHubFieldConfigurationToJira(issueTypes);
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

		// User edits
		final FieldScreenScheme scheme = fieldScreenSchemeManager.getFieldScreenSchemes().iterator().next();
		final FieldScreenSchemeItem schemeItem = scheme.getFieldScreenSchemeItems().iterator().next();
		schemeItem.setFieldScreen(defaultScreen);

		customFieldManager.removeCustomField(CustomFieldManagerMock.getCustomFields().get(0));


		fieldConfigSetup.addHubFieldConfigurationToJira(issueTypes);

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
		assertTrue(fieldScreenSchemeManager.getUpdatedSchemeItems().size() == 7);
		assertNull(settingsMock.get(HubJiraConstants.HUB_JIRA_ERROR));
	}

	private FieldScreen getDefaultFieldScreen(){
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

	private List<IssueType> getHubIssueTypes(){
		final List<IssueType> issueTypes = new ArrayList<>();

		final IssueTypeMock policyIssueType = new IssueTypeMock();
		policyIssueType.setName(HubJiraConstants.HUB_POLICY_VIOLATION_ISSUE);
		policyIssueType.setId(HubJiraConstants.HUB_POLICY_VIOLATION_ISSUE);
		issueTypes.add(policyIssueType);

		final IssueTypeMock securityIssueType = new IssueTypeMock();
		securityIssueType.setName(HubJiraConstants.HUB_VULNERABILITY_ISSUE);
		securityIssueType.setId(HubJiraConstants.HUB_VULNERABILITY_ISSUE);
		issueTypes.add(securityIssueType);

		return issueTypes;
	}
}
