package com.blackducksoftware.integration.jira.task.setup;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.atlassian.jira.issue.fields.layout.field.EditableDefaultFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayoutImpl;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.task.JiraSettingsService;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;

public class HubFieldConfigurationSetup {
	private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));

	private final JiraSettingsService settingService;

	private final JiraServices jiraServices;

	public final List<String> requiredDefaultFields = new ArrayList<>();

	public HubFieldConfigurationSetup(final JiraSettingsService settingService, final JiraServices jiraServices) {
		this.settingService = settingService;
		this.jiraServices = jiraServices;
		requiredDefaultFields.add("summary");
		requiredDefaultFields.add("issuetype");
	}

	public void addHubFieldConfigurationToJira() {
		try {
			EditableFieldLayout hubFieldLayout =null;
			final List<EditableFieldLayout> fieldLayouts = jiraServices.getFieldLayoutManager()
					.getEditableFieldLayouts();
			for(final EditableFieldLayout layout : fieldLayouts){
				if(layout.getName().equals("Hub Field Configuration")) {
					hubFieldLayout = layout;
					break;
				}
			}
			boolean fieldConfigurationNeedsUpdate = false;
			if (hubFieldLayout == null) {
				final EditableDefaultFieldLayout editableFieldLayout = jiraServices.getFieldLayoutManager()
						.getEditableDefaultFieldLayout();

				// Creates a copy of the default field layout
				hubFieldLayout = new EditableFieldLayoutImpl(null,
						editableFieldLayout.getFieldLayoutItems());

				hubFieldLayout.setName("Hub Field Configuration");
				fieldConfigurationNeedsUpdate = true;
			}
			final List<FieldLayoutItem> fields = hubFieldLayout.getFieldLayoutItems();
			for (final FieldLayoutItem field : fields) {
				String fieldName = field.getOrderableField().getName();
				fieldName = fieldName.replace(" ", "");
				fieldName = fieldName.toLowerCase();
				if (!requiredDefaultFields.contains(fieldName) && field.isRequired()) {
					hubFieldLayout.makeOptional(field);
					fieldConfigurationNeedsUpdate = true;
				}
			}
			if (fieldConfigurationNeedsUpdate) {
				// Persists our field configuration,
				// creates it if it doesnt exist,
				// updates it if it does exist
				jiraServices.getFieldLayoutManager().storeEditableFieldLayout(hubFieldLayout);
			}
		} catch (final Exception e) {
			logger.error(e);
			settingService.addHubError(e, "addHubFieldConfigurationToJira");
		}
	}
}
