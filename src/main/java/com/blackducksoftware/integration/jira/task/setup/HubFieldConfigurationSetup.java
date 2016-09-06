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
