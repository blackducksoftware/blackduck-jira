package com.blackducksoftware.integration.jira.task.setup;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.mocks.JiraServicesMock;
import com.blackducksoftware.integration.jira.mocks.PluginSettingsMock;
import com.blackducksoftware.integration.jira.mocks.field.EditableDefaultFieldLayoutMock;
import com.blackducksoftware.integration.jira.mocks.field.EditableFieldLayoutMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldLayoutItemMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldLayoutManagerMock;
import com.blackducksoftware.integration.jira.mocks.field.OrderableFieldMock;
import com.blackducksoftware.integration.jira.task.JiraSettingsService;

public class HubFieldConfigurationSetupTest {

	@Test
	public void testAddHubFieldConfigurationToJiraFirstTimeCreate() {
		final PluginSettingsMock settingsMock = new PluginSettingsMock();
		final JiraSettingsService settingService = new JiraSettingsService(settingsMock);
		final EditableDefaultFieldLayoutMock defaultFieldLayout = new EditableDefaultFieldLayoutMock();
		addDefaultFieldLayoutItems(defaultFieldLayout);
		addDefaultFieldLayoutItem(defaultFieldLayout, "custom", true);
		final FieldLayoutManagerMock fieldLayoutManager = new FieldLayoutManagerMock();
		fieldLayoutManager.setEditableDefaultFieldLayout(defaultFieldLayout);
		final JiraServicesMock jiraServices = new JiraServicesMock();
		jiraServices.setFieldLayoutManager(fieldLayoutManager);

		HubFieldConfigurationSetup fieldConfigSetup = new HubFieldConfigurationSetup(settingService,
				jiraServices);
		fieldConfigSetup = Mockito.spy(fieldConfigSetup);
		final EditableFieldLayoutMock fieldLayout = mockCreateEditableFieldLayout(fieldConfigSetup);

		fieldConfigSetup.addHubFieldConfigurationToJira();

		assertTrue(fieldLayoutManager.getAttemptedToPersistFieldLayout());
		assertTrue(fieldLayout.getFieldsToMakeOptional().size() == 1);
		assertTrue(fieldLayout.getFieldsToMakeOptional().get(0).getOrderableField().getName().equals("custom"));
		assertNull(settingsMock.get(HubJiraConstants.HUB_JIRA_ERROR));
	}

	@Test
	public void testAddHubFieldConfigurationToJiraFirstTimeCreateNoCustomRequiredFields() {
		final PluginSettingsMock settingsMock = new PluginSettingsMock();
		final JiraSettingsService settingService = new JiraSettingsService(settingsMock);
		final EditableDefaultFieldLayoutMock defaultFieldLayout = new EditableDefaultFieldLayoutMock();
		addDefaultFieldLayoutItems(defaultFieldLayout);
		final FieldLayoutManagerMock fieldLayoutManager = new FieldLayoutManagerMock();
		fieldLayoutManager.setEditableDefaultFieldLayout(defaultFieldLayout);
		final JiraServicesMock jiraServices = new JiraServicesMock();
		jiraServices.setFieldLayoutManager(fieldLayoutManager);

		HubFieldConfigurationSetup fieldConfigSetup = new HubFieldConfigurationSetup(settingService,
				jiraServices);
		fieldConfigSetup = Mockito.spy(fieldConfigSetup);
		final EditableFieldLayoutMock fieldLayout = mockCreateEditableFieldLayout(fieldConfigSetup);

		fieldConfigSetup.addHubFieldConfigurationToJira();

		assertTrue(fieldLayoutManager.getAttemptedToPersistFieldLayout());
		assertTrue(fieldLayout.getFieldsToMakeOptional().size() == 0);
		assertNull(settingsMock.get(HubJiraConstants.HUB_JIRA_ERROR));
	}

	@Test
	public void testAddHubFieldConfigurationToJiraNotFound() {
		final PluginSettingsMock settingsMock = new PluginSettingsMock();
		final JiraSettingsService settingService = new JiraSettingsService(settingsMock);
		final EditableDefaultFieldLayoutMock defaultFieldLayout = new EditableDefaultFieldLayoutMock();
		addDefaultFieldLayoutItems(defaultFieldLayout);
		addDefaultFieldLayoutItem(defaultFieldLayout, "custom", true);

		final EditableFieldLayoutMock otherFieldLayout = new EditableFieldLayoutMock();
		otherFieldLayout.setName("NotHubFieldConfiguration");
		addFieldLayoutItems(otherFieldLayout);
		addFieldLayoutItem(otherFieldLayout, "custom", true);
		final FieldLayoutManagerMock fieldLayoutManager = new FieldLayoutManagerMock();
		fieldLayoutManager.addEditableFieldLayout(otherFieldLayout);

		fieldLayoutManager.setEditableDefaultFieldLayout(defaultFieldLayout);
		final JiraServicesMock jiraServices = new JiraServicesMock();
		jiraServices.setFieldLayoutManager(fieldLayoutManager);

		HubFieldConfigurationSetup fieldConfigSetup = new HubFieldConfigurationSetup(settingService, jiraServices);
		fieldConfigSetup = Mockito.spy(fieldConfigSetup);
		final EditableFieldLayoutMock fieldLayout = mockCreateEditableFieldLayout(fieldConfigSetup);

		fieldConfigSetup.addHubFieldConfigurationToJira();

		assertTrue(fieldLayoutManager.getAttemptedToPersistFieldLayout());
		assertTrue(fieldLayout.getFieldsToMakeOptional().size() == 1);
		assertTrue(fieldLayout.getFieldsToMakeOptional().get(0).getOrderableField().getName().equals("custom"));
		assertNull(settingsMock.get(HubJiraConstants.HUB_JIRA_ERROR));
	}

	@Test
	public void testAddHubFieldConfigurationToJiraAlreadyAdded() {
		final PluginSettingsMock settingsMock = new PluginSettingsMock();
		final JiraSettingsService settingService = new JiraSettingsService(settingsMock);
		final EditableFieldLayoutMock fieldLayout = new EditableFieldLayoutMock();
		fieldLayout.setName(HubFieldConfigurationSetup.HUB_FIELD_CONFIGURATION);
		addFieldLayoutItems(fieldLayout);
		addFieldLayoutItem(fieldLayout, "custom", true);
		final FieldLayoutManagerMock fieldLayoutManager = new FieldLayoutManagerMock();
		fieldLayoutManager.addEditableFieldLayout(fieldLayout);
		final JiraServicesMock jiraServices = new JiraServicesMock();
		jiraServices.setFieldLayoutManager(fieldLayoutManager);

		final HubFieldConfigurationSetup fieldConfigSetup = new HubFieldConfigurationSetup(settingService, jiraServices);

		fieldConfigSetup.addHubFieldConfigurationToJira();

		assertTrue(fieldLayoutManager.getAttemptedToPersistFieldLayout());
		assertTrue(fieldLayout.getFieldsToMakeOptional().size() == 1);
		assertTrue(fieldLayout.getFieldsToMakeOptional().get(0).getOrderableField().getName().equals("custom"));
		assertNull(settingsMock.get(HubJiraConstants.HUB_JIRA_ERROR));
	}

	@Test
	public void testAddHubFieldConfigurationToJiraAlreadyAddedNoCustomRequiredFields() {
		final PluginSettingsMock settingsMock = new PluginSettingsMock();
		final JiraSettingsService settingService = new JiraSettingsService(settingsMock);
		final EditableFieldLayoutMock fieldLayout = new EditableFieldLayoutMock();
		fieldLayout.setName(HubFieldConfigurationSetup.HUB_FIELD_CONFIGURATION);
		addFieldLayoutItems(fieldLayout);
		final FieldLayoutManagerMock fieldLayoutManager = new FieldLayoutManagerMock();
		fieldLayoutManager.addEditableFieldLayout(fieldLayout);
		final JiraServicesMock jiraServices = new JiraServicesMock();
		jiraServices.setFieldLayoutManager(fieldLayoutManager);

		final HubFieldConfigurationSetup fieldConfigSetup = new HubFieldConfigurationSetup(settingService,
				jiraServices);

		fieldConfigSetup.addHubFieldConfigurationToJira();

		assertTrue(!fieldLayoutManager.getAttemptedToPersistFieldLayout());
		assertTrue(fieldLayout.getFieldsToMakeOptional().size() == 0);
		assertNull(settingsMock.get(HubJiraConstants.HUB_JIRA_ERROR));
	}

	private EditableFieldLayoutMock mockCreateEditableFieldLayout(
			final HubFieldConfigurationSetup fieldConfigSetupSpy) {
		final EditableFieldLayoutMock fieldLayout = new EditableFieldLayoutMock();

		Mockito.when(fieldConfigSetupSpy.createEditableFieldLayout(Mockito.anyListOf(FieldLayoutItem.class)))
		.thenAnswer(new Answer<EditableFieldLayout>() {
			@Override
			public EditableFieldLayout answer(final InvocationOnMock invocation) throws Throwable {
				final Object[] arguments = invocation.getArguments();
				final List<FieldLayoutItem> fields = (List<FieldLayoutItem>) arguments[0];
				fieldLayout.setFieldLayoutItems(fields);
				return fieldLayout;
			}
		});
		return fieldLayout;
	}

	private void addDefaultFieldLayoutItems(final EditableDefaultFieldLayoutMock defaultFieldLayout) {
		addDefaultFieldLayoutItem(defaultFieldLayout, "summary", true);
		addDefaultFieldLayoutItem(defaultFieldLayout, "issueType", true);
		addDefaultFieldLayoutItem(defaultFieldLayout, "description", false);
	}

	private void addDefaultFieldLayoutItem(final EditableDefaultFieldLayoutMock defaultFieldLayout, final String name,
			final boolean isRequired) {
		final FieldLayoutItemMock fieldLayoutItem = new FieldLayoutItemMock();
		final OrderableFieldMock field = new OrderableFieldMock();
		field.setName(name);
		fieldLayoutItem.setOrderableField(field);
		fieldLayoutItem.setIsRequired(isRequired);
		defaultFieldLayout.addFieldLayoutItem(fieldLayoutItem);
	}

	private void addFieldLayoutItems(final EditableFieldLayoutMock fieldLayout) {
		addFieldLayoutItem(fieldLayout, "summary", true);
		addFieldLayoutItem(fieldLayout, "issueType", true);
		addFieldLayoutItem(fieldLayout, "description", false);
	}

	private void addFieldLayoutItem(final EditableFieldLayoutMock fieldLayout, final String name,
			final boolean isRequired) {
		final FieldLayoutItemMock fieldLayoutItem = new FieldLayoutItemMock();
		final OrderableFieldMock field = new OrderableFieldMock();
		field.setName(name);
		fieldLayoutItem.setOrderableField(field);
		fieldLayoutItem.setIsRequired(isRequired);
		fieldLayout.addFieldLayoutItem(fieldLayoutItem);
	}
}
