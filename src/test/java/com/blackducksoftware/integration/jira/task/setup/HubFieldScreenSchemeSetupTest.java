package com.blackducksoftware.integration.jira.task.setup;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
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

		final FieldScreenMock fieldScreen = new FieldScreenMock();

		final FieldScreenSchemeMock fieldScreenScheme = new FieldScreenSchemeMock();

		Mockito.when(fieldConfigSetup.createNewScreenImpl(Mockito.any(FieldScreenManager.class)))
		.thenReturn(fieldScreen);

		Mockito.when(fieldConfigSetup.createNewScreenSchemeImpl(Mockito.any(FieldScreenSchemeManager.class)))
		.thenReturn(fieldScreenScheme);

		Mockito.when(fieldConfigSetup.createNewFieldScreenSchemeItemImpl(Mockito.any(FieldScreenSchemeManager.class),
				Mockito.any(FieldScreenManager.class))).thenReturn(new FieldScreenSchemeItemMock());

		fieldConfigSetup.addHubFieldConfigurationToJira(issueTypes);

		assertTrue(customFieldManager.getCustomFieldObjects().size() == 5);
		assertTrue(fieldScreen.getAttemptedScreenStore());
		assertTrue(fieldScreenScheme.getAttemptedScreenSchemeStore());
		assertTrue(fieldScreenSchemeManager.getUpdatedSchemes().size() == 2);
		assertTrue(fieldScreenSchemeManager.getUpdatedSchemeItems().size() == 6);
		assertNull(settingsMock.get(HubJiraConstants.HUB_JIRA_ERROR));
	}

	// test default screen tabs get added to our screen
	// test user changed the screen of the screen scheme item on our screen
	// scheme

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
