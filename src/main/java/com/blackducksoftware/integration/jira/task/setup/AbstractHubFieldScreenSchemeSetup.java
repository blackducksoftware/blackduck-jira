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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;

import com.atlassian.jira.issue.context.GlobalIssueContext;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItemImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.issue.operation.ScreenableIssueOperation;
import com.atlassian.jira.web.action.admin.customfields.CreateCustomField;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.task.JiraSettingsService;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;

public abstract class AbstractHubFieldScreenSchemeSetup {

	private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));

	private final JiraSettingsService settingService;

	private final JiraServices jiraServices;

	private final Map<String, CustomField> customFields = new HashMap<>();

	public AbstractHubFieldScreenSchemeSetup(final JiraSettingsService settingService, final JiraServices jiraServices) {
		this.settingService = settingService;
		this.jiraServices = jiraServices;
	}

	public JiraSettingsService getSettingService() {
		return settingService;
	}

	public JiraServices getJiraServices() {
		return jiraServices;
	}

	public Map<String, CustomField> getCustomFields() {
		return customFields;
	}

	public Map<IssueType, FieldScreenScheme> addHubFieldConfigurationToJira(final List<IssueType> hubIssueTypes) {
		final Map<IssueType, FieldScreenScheme> fieldScreenSchemes = new HashMap<>();
		try {
			if (hubIssueTypes != null && !hubIssueTypes.isEmpty()) {
				final List<Object> commonIssueTypeList = getIssueTypeObjectList(hubIssueTypes);
				for (final IssueType issue : hubIssueTypes) {
					if (issue.getName().equals(HubJiraConstants.HUB_POLICY_VIOLATION_ISSUE)) {
						final FieldScreenScheme fss = createPolicyViolationScreenScheme(issue,
								commonIssueTypeList);
						fieldScreenSchemes.put(issue, fss);
					} else if (issue.getName().equals(HubJiraConstants.HUB_VULNERABILITY_ISSUE)) {
						final FieldScreenScheme fss = createSecurityScreenScheme(issue,
								commonIssueTypeList);
						fieldScreenSchemes.put(issue, fss);
					}
				}
			}
		} catch (final Exception e) {
			logger.error(e);
			settingService.addHubError(e, "addHubFieldConfigurationToJira");
		}
		return fieldScreenSchemes;
	}

	protected abstract List<Object> getIssueTypeObjectList(final List<IssueType> hubIssueTypes);

	protected abstract Object getIssueTypeObject(final IssueType hubIssueType);

	protected List<Object> getAsscociatedIssueTypeObjects(final CustomField customField) {
		final List<Object> genericValues = new ArrayList<>();
		genericValues.addAll(customField.getAssociatedIssueTypes());
		return genericValues;
	}

	private CustomField createCustomField(final List<Object> issueTypeList, final String fieldName)
			throws GenericEntityException {
		final CustomFieldType fieldType = jiraServices.getCustomFieldManager()
				.getCustomFieldType(CreateCustomField.FIELD_TYPE_PREFIX + "textfield");
		final CustomFieldSearcher fieldSearcher = jiraServices.getCustomFieldManager()
				.getCustomFieldSearcher(CreateCustomField.FIELD_TYPE_PREFIX + "textsearcher");

		final List<JiraContextNode> contexts = new ArrayList<>();
		contexts.add(GlobalIssueContext.getInstance());

		return jiraServices.getCustomFieldManager().createCustomField(fieldName, "", fieldType, fieldSearcher, contexts,
				issueTypeList);
	}

	private OrderableField getOrderedFieldFromCustomField(final List<Object> commonIssueTypeList,
			final String fieldName) {
		try {
			CustomField customField = jiraServices.getCustomFieldManager().getCustomFieldObjectByName(fieldName);
			if (customField == null) {
				customField = createCustomField(commonIssueTypeList, fieldName);
			}
			if (customField.getAssociatedIssueTypes() != null && !customField.getAssociatedIssueTypes().isEmpty()) {
				final List<Object> associatatedIssueTypeList = getAsscociatedIssueTypeObjects(customField);
				boolean needToUpdateCustomField = false;
				for (final Object issueTypeValue : commonIssueTypeList) {
					if (!associatatedIssueTypeList.contains(issueTypeValue)) {
						needToUpdateCustomField = true;
						associatatedIssueTypeList.add(issueTypeValue);
					}
				}
				if (needToUpdateCustomField) {
					// not sure how else to best update the custom field
					jiraServices.getCustomFieldManager().removeCustomField(customField);
					customField = createCustomField(associatatedIssueTypeList, fieldName);
				}
			}
			customFields.put(fieldName, customField);
			final OrderableField myField = jiraServices.getFieldManager().getOrderableField(customField.getId());
			return myField;
		} catch (final Exception e) {
			logger.error(e);
			settingService.addHubError(e, "getOrderedFieldFromCustomField");
		}
		return null;
	}

	private List<OrderableField> createCommonFields(final List<Object> commonIssueTypeList) {
		final List<OrderableField> customFields = new ArrayList<>();
		customFields.add(getOrderedFieldFromCustomField(commonIssueTypeList,
				HubJiraConstants.HUB_CUSTOM_FIELD_PROJECT));
		customFields.add(getOrderedFieldFromCustomField(commonIssueTypeList,
				HubJiraConstants.HUB_CUSTOM_FIELD_PROJECT_VERSION));
		customFields.add(getOrderedFieldFromCustomField(commonIssueTypeList,
				HubJiraConstants.HUB_CUSTOM_FIELD_COMPONENT));
		customFields.add(getOrderedFieldFromCustomField(commonIssueTypeList,
				HubJiraConstants.HUB_CUSTOM_FIELD_COMPONENT_VERSION));
		return customFields;
	}

	private List<OrderableField> createPolicyViolationFields(final IssueType issueType,
			final List<Object> commonIssueTypeList) {
		final List<OrderableField> customFields = new ArrayList<>();
		final List<Object> policyViolationIssueTypeObjectList = new ArrayList<>();
		policyViolationIssueTypeObjectList.add(getIssueTypeObject(issueType));
		customFields.add(getOrderedFieldFromCustomField(policyViolationIssueTypeObjectList,
				HubJiraConstants.HUB_CUSTOM_FIELD_POLICY_RULE));
		customFields.addAll(createCommonFields(commonIssueTypeList));
		return customFields;
	}

	private List<OrderableField> createSecurityFields(final IssueType issueType,
			final List<Object> commonIssueTypeList) {
		final List<OrderableField> customFields = new ArrayList<>();
		customFields.addAll(createCommonFields(commonIssueTypeList));
		return customFields;
	}

	public FieldScreen createNewScreenImpl(final FieldScreenManager fieldScreenManager) {
		return new FieldScreenImpl(fieldScreenManager);
	}

	private FieldScreen createScreen(final String screenName, final List<OrderableField> customFields) {
		final Collection<FieldScreen> fieldScreens = jiraServices.getFieldScreenManager().getFieldScreens();
		FieldScreen hubScreen = null;
		if (fieldScreens != null && !fieldScreens.isEmpty()) {
			for (final FieldScreen fieldScreen : fieldScreens) {
				if (fieldScreen.getName().equals(screenName)) {
					hubScreen = fieldScreen;
					break;
				}
			}
		}
		if (hubScreen == null) {
			hubScreen = createNewScreenImpl(jiraServices.getFieldScreenManager());
			hubScreen.setName(screenName);
			hubScreen.store();
		}
		final FieldScreen defaultScreen = jiraServices.getFieldScreenManager()
				.getFieldScreen(FieldScreen.DEFAULT_SCREEN_ID);

		List<FieldScreenTab> defaultTabs = null;
		if (defaultScreen != null) {
			defaultTabs = defaultScreen.getTabs();
		}

		final boolean needToUpdateScreen = addHubTabToScreen(hubScreen, customFields, defaultTabs);

		if (needToUpdateScreen) {
			jiraServices.getFieldScreenManager().updateFieldScreen(hubScreen);
		}

		return hubScreen;
	}

	private boolean addHubTabToScreen(final FieldScreen hubScreen, final List<OrderableField> customFields,
			final List<FieldScreenTab> defaultTabs) {
		FieldScreenTab myTab = null;
		if (hubScreen != null && hubScreen.getTabs() != null && !hubScreen.getTabs().isEmpty()) {
			for (final FieldScreenTab screenTab : hubScreen.getTabs()) {
				if (screenTab.getName().equals(HubJiraConstants.HUB_SCREEN_TAB)) {
					myTab = screenTab;
					break;
				}
			}
		}
		boolean needToUpdateTabAndScreen = false;
		if (myTab == null) {
			myTab = hubScreen.addTab(HubJiraConstants.HUB_SCREEN_TAB);
			needToUpdateTabAndScreen = true;
		}
		if (customFields != null && !customFields.isEmpty()) {
			for (final OrderableField field : customFields) {
				final FieldScreenLayoutItem existingField = myTab.getFieldScreenLayoutItem(field.getId());
				if (existingField == null) {
					myTab.addFieldScreenLayoutItem(field.getId());
					needToUpdateTabAndScreen = true;
				}
			}
		}
		if (defaultTabs != null && !defaultTabs.isEmpty()) {
			for (final FieldScreenTab tab : defaultTabs) {
				final List<FieldScreenLayoutItem> layoutItems = tab.getFieldScreenLayoutItems();
				for (final FieldScreenLayoutItem layoutItem : layoutItems) {
					final FieldScreenLayoutItem existingField = myTab
							.getFieldScreenLayoutItem(layoutItem.getOrderableField().getId());
					if (existingField == null) {
						myTab.addFieldScreenLayoutItem(layoutItem.getOrderableField().getId());
						needToUpdateTabAndScreen = true;
					}
				}
			}
		}
		if (needToUpdateTabAndScreen) {
			jiraServices.getFieldScreenManager().updateFieldScreenTab(myTab);
		}

		return needToUpdateTabAndScreen;
	}

	private FieldScreen createPolicyViolationScreen(final IssueType issueType,
			final List<Object> commonIssueTypeList) {
		final List<OrderableField> customFields = createPolicyViolationFields(issueType,
				commonIssueTypeList);
		final FieldScreen screen = createScreen(HubJiraConstants.HUB_POLICY_SCREEN_NAME, customFields);
		return screen;
	}

	private FieldScreen createSecurityScreen(final IssueType issueType,
			final List<Object> commonIssueTypeList) {
		final List<OrderableField> customFields = createSecurityFields(issueType, commonIssueTypeList);
		final FieldScreen screen = createScreen(HubJiraConstants.HUB_SECURITY_SCREEN_NAME, customFields);
		return screen;
	}

	public FieldScreenScheme createNewScreenSchemeImpl(final FieldScreenSchemeManager fieldScreenSchemeManager) {
		return new FieldScreenSchemeImpl(fieldScreenSchemeManager);
	}

	public FieldScreenSchemeItem createNewFieldScreenSchemeItemImpl(
			final FieldScreenSchemeManager fieldScreenSchemeManager, final FieldScreenManager fieldScreenManager) {
		return new FieldScreenSchemeItemImpl(fieldScreenSchemeManager, fieldScreenManager);
	}

	private FieldScreenScheme createScreenScheme(final String screenSchemeName, final FieldScreen screen) {
		final Collection<FieldScreenScheme> fieldScreenSchemes = jiraServices.getFieldScreenSchemeManager()
				.getFieldScreenSchemes();
		FieldScreenScheme hubScreenScheme = null;
		if (fieldScreenSchemes != null && !fieldScreenSchemes.isEmpty()) {
			for (final FieldScreenScheme fieldScreenScheme : fieldScreenSchemes) {
				if (fieldScreenScheme.getName().equals(screenSchemeName)) {
					hubScreenScheme = fieldScreenScheme;
					break;
				}
			}
		}
		if (hubScreenScheme == null) {
			hubScreenScheme = createNewScreenSchemeImpl(jiraServices.getFieldScreenSchemeManager());
			hubScreenScheme.setName(screenSchemeName);
			hubScreenScheme.store();
		}

		final FieldScreen defaultScreen = jiraServices.getFieldScreenManager()
				.getFieldScreen(FieldScreen.DEFAULT_SCREEN_ID);

		final List<ScreenableIssueOperation> issueOpertations = new ArrayList<>();
		issueOpertations.add(IssueOperations.CREATE_ISSUE_OPERATION);
		issueOpertations.add(IssueOperations.VIEW_ISSUE_OPERATION);

		final List<ScreenableIssueOperation> issueOpertationsForDefaultScreen = new ArrayList<>();
		issueOpertations.add(IssueOperations.EDIT_ISSUE_OPERATION);

		final boolean hubScreenSchemeNeedsUpdate = settingScreenForIssueOperation(issueOpertations, hubScreenScheme,
				screen)
				|| settingScreenForIssueOperation(issueOpertationsForDefaultScreen, hubScreenScheme, defaultScreen);

		if (hubScreenSchemeNeedsUpdate) {
			jiraServices.getFieldScreenSchemeManager().updateFieldScreenScheme(hubScreenScheme);
		}
		return hubScreenScheme;
	}

	private boolean settingScreenForIssueOperation(final List<ScreenableIssueOperation> issueOpertations,
			final FieldScreenScheme hubScreenScheme, final FieldScreen screen) {
		boolean hubScreenSchemeNeedsUpdate = false;
		for (final ScreenableIssueOperation issueOperation : issueOpertations) {
			FieldScreenSchemeItem hubScreenSchemeItem = hubScreenScheme.getFieldScreenSchemeItem(issueOperation);
			boolean screenSchemeItemNeedsUpdate = false;
			if (hubScreenSchemeItem == null) {
				hubScreenSchemeItem = createNewFieldScreenSchemeItemImpl(jiraServices.getFieldScreenSchemeManager(),
						jiraServices.getFieldScreenManager());
				hubScreenSchemeItem.setIssueOperation(issueOperation);
				hubScreenSchemeItem.setFieldScreen(screen);
				hubScreenScheme.addFieldScreenSchemeItem(hubScreenSchemeItem);
				hubScreenSchemeNeedsUpdate = true;
				screenSchemeItemNeedsUpdate = true;
			} else {
				if (hubScreenSchemeItem.getFieldScreen() == null
						|| !hubScreenSchemeItem.getFieldScreen().equals(screen)) {
					hubScreenSchemeItem.setFieldScreen(screen);
					screenSchemeItemNeedsUpdate = true;
				}
			}
			if (screenSchemeItemNeedsUpdate) {
				jiraServices.getFieldScreenSchemeManager().updateFieldScreenSchemeItem(hubScreenSchemeItem);
			}
		}
		return hubScreenSchemeNeedsUpdate;
	}

	private FieldScreenScheme createPolicyViolationScreenScheme(final IssueType issueType,
			final List<Object> commonIssueTypeList) {
		final FieldScreen screen = createPolicyViolationScreen(issueType, commonIssueTypeList);
		final FieldScreenScheme fieldScreenScheme = createScreenScheme(HubJiraConstants.HUB_POLICY_SCREEN_SCHEME_NAME,
				screen);
		return fieldScreenScheme;
	}

	private FieldScreenScheme createSecurityScreenScheme(final IssueType issueType,
			final List<Object> commonIssueTypeList) {
		final FieldScreen screen = createSecurityScreen(issueType, commonIssueTypeList);
		final FieldScreenScheme fieldScreenScheme = createScreenScheme(
				HubJiraConstants.HUB_SECURITY_SCREEN_SCHEME_NAME, screen);
		return fieldScreenScheme;
	}

}
