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
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.issue.context.GlobalIssueContext;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItemImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.issue.operation.ScreenableIssueOperation;
import com.atlassian.jira.web.action.admin.customfields.CreateCustomField;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.task.JiraSettingsService;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;

public class HubFieldScreenSchemeSetup {
	public static final String HUB_POLICY_SCREEN_SCHEME_NAME = "Hub Policy Screen Scheme";
	public static final String HUB_SECURITY_SCREEN_SCHEME_NAME = "Hub Security Screen Scheme";

	public static final String HUB_POLICY_SCREEN_NAME = "Hub Policy Screen";
	public static final String HUB_SECURITY_SCREEN_NAME = "Hub Security Screen";

	public static final String HUB_SCREEN_TAB = "Hub Screen Tab";

	private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));

	private final JiraSettingsService settingService;

	private final JiraServices jiraServices;


	public HubFieldScreenSchemeSetup(final JiraSettingsService settingService,
			final JiraServices jiraServices) {
		this.settingService = settingService;
		this.jiraServices = jiraServices;
	}

	public void addHubFieldConfigurationToJira(final List<IssueType> hubIssueTypes) {
		try {
			// ComponentAccessor.getFieldLayoutManager().get
			if (hubIssueTypes != null && !hubIssueTypes.isEmpty()) {
				for (final IssueType issueType : hubIssueTypes) {
					if (issueType.getName().equals(HubJiraConstants.HUB_POLICY_VIOLATION_ISSUE)) {
						createPolicyViolationScreenScheme(issueType);
					} else if (issueType.getName().equals(HubJiraConstants.HUB_VULNERABILITY_ISSUE)) {
						createSecurityScreenScheme(issueType);
					}
				}
			}
		} catch (final Exception e) {
			logger.error(e);
			settingService.addHubError(e, "addHubFieldConfigurationToJira");
		}
	}

	private OrderableField createCustomField(final IssueType issueType, final String fieldName) {
		try {
			CustomField customField = jiraServices.getCustomFieldManager().getCustomFieldObjectByName(fieldName);
			if (customField == null) {
				final CustomFieldType fieldType = jiraServices.getCustomFieldManager()
						.getCustomFieldType(CreateCustomField.FIELD_TYPE_PREFIX + "textfield");
				final CustomFieldSearcher fieldSearcher = jiraServices.getCustomFieldManager()
						.getCustomFieldSearcher(CreateCustomField.FIELD_TYPE_PREFIX + "textsearcher");

				final List<JiraContextNode> contexts = new ArrayList<>();
				contexts.add(GlobalIssueContext.getInstance());

				final List<GenericValue> issueTypeGenericValueList = new ArrayList<>();
				issueTypeGenericValueList.add(issueType.getGenericValue());

				customField = jiraServices.getCustomFieldManager().createCustomField(fieldName, "",
						fieldType, fieldSearcher, contexts, issueTypeGenericValueList);
			}
			final OrderableField myField = jiraServices.getFieldManager().getOrderableField(customField.getId());
			return myField;
		} catch (final Exception e) {
			logger.error(e);
			settingService.addHubError(e, "createCustomField");
		}
		return null;
	}

	private List<OrderableField> createCommonFields(final IssueType issueType) {
		final List<OrderableField> customFields = new ArrayList<>();
		customFields.add(createCustomField(issueType, "Project"));
		customFields.add(createCustomField(issueType, "Project Version"));
		customFields.add(createCustomField(issueType, "Component"));
		customFields.add(createCustomField(issueType, "Component Version"));

		return customFields;
	}

	private List<OrderableField> createPolicyViolationFields(final IssueType issueType) {
		final List<OrderableField> customFields = new ArrayList<>();
		customFields.add(createCustomField(issueType, "Policy Rule"));
		customFields.addAll(createCommonFields(issueType));
		return customFields;
	}

	private List<OrderableField> createSecurityFields(final IssueType issueType) {
		final List<OrderableField> customFields = new ArrayList<>();
		customFields.addAll(createCommonFields(issueType));
		return customFields;
	}

	private FieldScreen createScreen(final IssueType issueType, final String screenName,
			final List<OrderableField> customFields) {
		final Collection<FieldScreen> fieldScreens = jiraServices.getFieldScreenManager().getFieldScreens();
		FieldScreen hubScreen = null;
		for(final FieldScreen fieldScreen : fieldScreens){
			if (fieldScreen.getName().equals(screenName)) {
				hubScreen = fieldScreen;
				break;
			}
		}
		if (hubScreen == null) {
			hubScreen = new FieldScreenImpl(jiraServices.getFieldScreenManager());
			hubScreen.setName(screenName);
			hubScreen.store();
		}
		final FieldScreen defaultScreen = jiraServices.getFieldScreenManager()
				.getFieldScreen(FieldScreen.DEFAULT_SCREEN_ID);

		final FieldScreenTab myTab = createHubScreenTab(hubScreen, customFields);

		for (final FieldScreenTab tab : defaultScreen.getTabs()) {
			final List<FieldScreenLayoutItem> layoutItems = tab.getFieldScreenLayoutItems();
			for (final FieldScreenLayoutItem layoutItem : layoutItems) {
				final FieldScreenLayoutItem existingField = myTab
						.getFieldScreenLayoutItem(layoutItem.getOrderableField().getId());
				if (existingField == null) {
					myTab.addFieldScreenLayoutItem(layoutItem.getOrderableField().getId());
				}
			}
		}

		return hubScreen;
	}

	private FieldScreenTab createHubScreenTab(final FieldScreen hubScreen, final List<OrderableField> customFields) {
		FieldScreenTab myTab = null;
		for (final FieldScreenTab screenTab : hubScreen.getTabs()) {
			if (screenTab.getName().equals(HUB_SCREEN_TAB)) {
				myTab = screenTab;
				break;
			}
		}
		if (myTab == null) {
			myTab = hubScreen.addTab(HUB_SCREEN_TAB);
		}
		for (final OrderableField field : customFields) {
			final FieldScreenLayoutItem existingField = myTab.getFieldScreenLayoutItem(field.getId());
			if(existingField == null){
				myTab.addFieldScreenLayoutItem(field.getId());
			}
		}
		return myTab;
	}

	private FieldScreen createPolicyViolationScreen(final IssueType issueType) {
		final List<OrderableField> customFields = createPolicyViolationFields(issueType);
		final FieldScreen screen = createScreen(issueType, HUB_POLICY_SCREEN_NAME, customFields);
		return screen;
	}

	private FieldScreen createSecurityScreen(final IssueType issueType) {
		final List<OrderableField> customFields = createSecurityFields(issueType);
		final FieldScreen screen = createScreen(issueType, HUB_SECURITY_SCREEN_NAME, customFields);
		return screen;
	}

	private void createScreenScheme(final IssueType issueType, final String screenSchemeName,
			final FieldScreen screen) {
		final Collection<FieldScreenScheme> fieldScreenSchemes = jiraServices.getFieldScreenSchemeManager()
				.getFieldScreenSchemes();
		FieldScreenScheme hubScreenScheme = null;
		for (final FieldScreenScheme fieldScreenScheme : fieldScreenSchemes) {
			if (fieldScreenScheme.getName().equals(screenSchemeName)) {
				hubScreenScheme = fieldScreenScheme;
				break;
			}
		}
		if (hubScreenScheme == null) {
			hubScreenScheme = new FieldScreenSchemeImpl(jiraServices.getFieldScreenSchemeManager());
			hubScreenScheme.setName(screenSchemeName);
			hubScreenScheme.store();

			final List<ScreenableIssueOperation> issueOpertations = new ArrayList<>();
			issueOpertations.add(IssueOperations.CREATE_ISSUE_OPERATION);
			issueOpertations.add(IssueOperations.EDIT_ISSUE_OPERATION);
			issueOpertations.add(IssueOperations.VIEW_ISSUE_OPERATION);

			for (final ScreenableIssueOperation issueOperation : issueOpertations) {
				final FieldScreenSchemeItem hubScreenSchemeItem = new FieldScreenSchemeItemImpl(
						jiraServices.getFieldScreenSchemeManager(), jiraServices.getFieldScreenManager());
				hubScreenSchemeItem.setIssueOperation(issueOperation);
				hubScreenSchemeItem.setFieldScreen(screen);
				hubScreenScheme.addFieldScreenSchemeItem(hubScreenSchemeItem);
				jiraServices.getFieldScreenSchemeManager().updateFieldScreenSchemeItem(hubScreenSchemeItem);
			}
			jiraServices.getFieldScreenSchemeManager().updateFieldScreenScheme(hubScreenScheme);
		}
	}

	private void createPolicyViolationScreenScheme(final IssueType issueType) {
		final FieldScreen screen = createPolicyViolationScreen(issueType);
		createScreenScheme(issueType, HUB_POLICY_SCREEN_SCHEME_NAME, screen);
	}

	private void createSecurityScreenScheme(final IssueType issueType) {
		final FieldScreen screen = createSecurityScreen(issueType);
		createScreenScheme(issueType, HUB_SECURITY_SCREEN_SCHEME_NAME, screen);
	}

}
