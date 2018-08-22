/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
 */
package com.blackducksoftware.integration.jira.task.setup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;

import com.atlassian.jira.issue.CustomFieldManager;
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
import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.model.PluginField;
import com.blackducksoftware.integration.jira.config.JiraServices;
import com.blackducksoftware.integration.jira.config.JiraSettingsService;

// TODO investigate why the JIRA API returns raw types
@SuppressWarnings("rawtypes")
public class BlackDuckFieldScreenSchemeSetup {
    private final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final JiraSettingsService settingService;
    private final JiraServices jiraServices;
    private final Map<PluginField, CustomField> customFields = new HashMap<>();

    public BlackDuckFieldScreenSchemeSetup(final JiraSettingsService settingService, final JiraServices jiraServices) {
        this.settingService = settingService;
        this.jiraServices = jiraServices;
    }

    public JiraSettingsService getSettingService() {
        return settingService;
    }

    public JiraServices getJiraServices() {
        return jiraServices;
    }

    public Map<PluginField, CustomField> getCustomFields() {
        return customFields;
    }

    public Map<IssueType, FieldScreenScheme> addBlackDuckFieldConfigurationToJira(final List<IssueType> blackDuckIssueTypes) {
        final Map<IssueType, FieldScreenScheme> fieldScreenSchemes = new HashMap<>();
        try {
            renameCustomFields();
            if (blackDuckIssueTypes != null && !blackDuckIssueTypes.isEmpty()) {
                for (final IssueType issueType : blackDuckIssueTypes) {
                    if (issueType.getName().equals(BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_ISSUE)) {
                        final FieldScreenScheme fss = createPolicyViolationScreenScheme(issueType, blackDuckIssueTypes);
                        fieldScreenSchemes.put(issueType, fss);
                    } else if (issueType.getName().equals(BlackDuckJiraConstants.BLACKDUCK_VULNERABILITY_ISSUE)) {
                        final FieldScreenScheme fss = createSecurityScreenScheme(blackDuckIssueTypes);
                        fieldScreenSchemes.put(issueType, fss);
                    }
                }
            }
        } catch (final Exception e) {
            logger.error(e);
            settingService.addBlackDuckError(e, "addBlackDuckFieldConfigurationToJira");
        }
        return fieldScreenSchemes;
    }

    public FieldScreen createNewScreenImpl(final FieldScreenManager fieldScreenManager) {
        return new FieldScreenImpl(fieldScreenManager);
    }

    public FieldScreenScheme createNewScreenSchemeImpl(final FieldScreenSchemeManager fieldScreenSchemeManager) {
        return new FieldScreenSchemeImpl(fieldScreenSchemeManager);
    }

    public FieldScreenSchemeItem createNewFieldScreenSchemeItemImpl(final FieldScreenSchemeManager fieldScreenSchemeManager, final FieldScreenManager fieldScreenManager) {
        return new FieldScreenSchemeItemImpl(fieldScreenSchemeManager, fieldScreenManager);
    }

    private IssueType getIssueTypeObject(final IssueType blackDuckIssueType) {
        return blackDuckIssueType;
    }

    private void renameCustomFields() {
        final CustomFieldManager customFieldManager = jiraServices.getCustomFieldManager();
        for (final String customFieldName : V3PluginConstants.V3_TO_LATEST_CUSTOM_FIELD_NAME_MAP.keySet()) {
            final Collection<CustomField> foundCustomFields = customFieldManager.getCustomFieldObjectsByName(customFieldName);
            if (foundCustomFields != null && !foundCustomFields.isEmpty()) {
                // If we have configured everything correctly, this only have one entry. If the customer has duplicated a custom field name, we have a different problem.
                final CustomField firstCustomField = foundCustomFields.iterator().next();
                if (firstCustomField.isEditable()) {
                    final String newCustomFieldName = V3PluginConstants.V3_TO_LATEST_CUSTOM_FIELD_NAME_MAP.get(firstCustomField.getName());
                    customFieldManager.updateCustomField(firstCustomField.getIdAsLong(), newCustomFieldName, newCustomFieldName, firstCustomField.getCustomFieldSearcher());
                } else {
                    logger.debug("Unable to update custom field: " + firstCustomField.getName());
                }
            }
        }
    }

    private CustomField createCustomTextField(final List<IssueType> issueTypeList, final String fieldName) throws RuntimeException {
        return createCustomField(issueTypeList, fieldName, "textarea", "textsearcher");
    }

    private CustomField createCustomUserField(final List<IssueType> issueTypeList, final String fieldName) throws RuntimeException {
        return createCustomField(issueTypeList, fieldName, "userpicker", "userpickersearcher");
    }

    private CustomField createCustomField(final List<IssueType> issueTypeList, final String fieldName, final String typeSuffix, final String searcherSuffix) throws RuntimeException {
        logger.debug("createCustomField(): " + fieldName);
        final CustomFieldType fieldType = jiraServices.getCustomFieldManager().getCustomFieldType(CreateCustomField.FIELD_TYPE_PREFIX + typeSuffix);
        final CustomFieldSearcher fieldSearcher = jiraServices.getCustomFieldManager().getCustomFieldSearcher(CreateCustomField.FIELD_TYPE_PREFIX + searcherSuffix);

        final List<JiraContextNode> contexts = new ArrayList<>();
        contexts.add(GlobalIssueContext.getInstance());

        try {
            return jiraServices.getCustomFieldManager().createCustomField(fieldName, "", fieldType, fieldSearcher, contexts, issueTypeList);
        } catch (final GenericEntityException e) {
            // This will be caught by this::getOrderedFieldFromCustomField
            throw new RuntimeException(e);
        }
    }

    private OrderableField getOrderedTextFieldFromCustomField(final List<IssueType> issueTypeList, final PluginField pluginField) {
        return getOrderedFieldFromCustomField(issueTypeList, pluginField, this::createCustomTextField);
    }

    private OrderableField getOrderedUserFieldFromCustomField(final List<IssueType> issueTypeList, final PluginField pluginField) {
        return getOrderedFieldFromCustomField(issueTypeList, pluginField, this::createCustomUserField);
    }

    private OrderableField getOrderedFieldFromCustomField(final List<IssueType> issueTypeList, final PluginField pluginField, final BiFunction<List<IssueType>, String, CustomField> createCustomFieldFunction) {
        try {
            @SuppressWarnings("deprecation")
            // The method is deprecated because custom fields are no longer guaranteed to be unique. This impl will get the first (if there are multiple options).
            CustomField customField = jiraServices.getCustomFieldManager().getCustomFieldObjectByName(pluginField.getName());
            if (customField == null) {
                customField = createCustomFieldFunction.apply(issueTypeList, pluginField.getName());
            }
            if (customField.getAssociatedIssueTypes() != null && !customField.getAssociatedIssueTypes().isEmpty()) {
                final List<IssueType> associatatedIssueTypeList = customField.getAssociatedIssueTypes();
                boolean needToUpdateCustomField = false;
                for (final IssueType issueTypeValue : issueTypeList) {
                    if (!associatatedIssueTypeList.contains(issueTypeValue)) {
                        logger.debug("This issue type is not in the associated issue type list. Adding it.");
                        needToUpdateCustomField = true;
                        associatatedIssueTypeList.add(issueTypeValue);
                    }
                }
                if (needToUpdateCustomField) {
                    // Setup is incomplete, but the only available way to recover (by deleting the custom attribute and re-creating it) is too dangerous
                    final String msg = "The custom field " + customField.getName() + " is missing one or more IssueType associations.";
                    logger.error(msg);
                    settingService.addBlackDuckError(msg, "getOrderedFieldFromCustomField");
                }
            } else {
                // Setup is incomplete, but the only available way to recover (by deleting the custom attribute and re-creating it) is too dangerous
                final String msg = "The custom field " + customField.getName() + " has no IssueType associations.";
                logger.error(msg);
                settingService.addBlackDuckError(msg, "getOrderedFieldFromCustomField");
            }
            customFields.put(pluginField, customField);
            final OrderableField myField = jiraServices.getFieldManager().getOrderableField(customField.getId());
            return myField;
        } catch (final Exception e) {
            logger.error("Error in getOrderedFieldFromCustomField(): " + e.getMessage(), e);
            settingService.addBlackDuckError(e, "getOrderedFieldFromCustomField");
        }
        return null;
    }

    private List<OrderableField> createCommonFields(final List<IssueType> issueTypeList) {
        final List<OrderableField> customFields = new ArrayList<>();
        customFields.add(getOrderedTextFieldFromCustomField(issueTypeList, PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT));
        customFields.add(getOrderedTextFieldFromCustomField(issueTypeList, PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION));
        customFields.add(getOrderedTextFieldFromCustomField(issueTypeList, PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION_URL));
        customFields.add(getOrderedUserFieldFromCustomField(issueTypeList, PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT_OWNER));
        customFields.add(getOrderedTextFieldFromCustomField(issueTypeList, PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION_NICKNAME));

        customFields.add(getOrderedTextFieldFromCustomField(issueTypeList, PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT));
        customFields.add(getOrderedTextFieldFromCustomField(issueTypeList, PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT_URL));
        customFields.add(getOrderedTextFieldFromCustomField(issueTypeList, PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT_VERSION));
        customFields.add(getOrderedTextFieldFromCustomField(issueTypeList, PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT_VERSION_URL));
        customFields.add(getOrderedTextFieldFromCustomField(issueTypeList, PluginField.BLACKDUCK_CUSTOM_FIELD_LICENSE_NAMES));
        customFields.add(getOrderedTextFieldFromCustomField(issueTypeList, PluginField.BLACKDUCK_CUSTOM_FIELD_LICENSE_URL));

        customFields.add(getOrderedTextFieldFromCustomField(issueTypeList, PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT_USAGE));
        customFields.add(getOrderedTextFieldFromCustomField(issueTypeList, PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION_LAST_UPDATED));

        return customFields;
    }

    private List<OrderableField> createPolicyViolationFields(final IssueType issueType, final List<IssueType> issueTypeList) {
        final List<OrderableField> customFields = new ArrayList<>();
        final List<IssueType> policyViolationIssueTypeObjectList = new ArrayList<>();
        policyViolationIssueTypeObjectList.add(getIssueTypeObject(issueType));
        customFields.add(getOrderedTextFieldFromCustomField(policyViolationIssueTypeObjectList, PluginField.BLACKDUCK_CUSTOM_FIELD_POLICY_RULE));
        customFields.add(getOrderedTextFieldFromCustomField(policyViolationIssueTypeObjectList, PluginField.BLACKDUCK_CUSTOM_FIELD_POLICY_RULE_OVERRIDABLE));
        customFields.add(getOrderedTextFieldFromCustomField(policyViolationIssueTypeObjectList, PluginField.BLACKDUCK_CUSTOM_FIELD_POLICY_RULE_DESCRIPTION));
        customFields.add(getOrderedTextFieldFromCustomField(policyViolationIssueTypeObjectList, PluginField.BLACKDUCK_CUSTOM_FIELD_POLICY_RULE_URL));
        customFields.addAll(createCommonFields(issueTypeList));
        return customFields;
    }

    private List<OrderableField> createSecurityFields(final List<IssueType> issueTypeList) {
        final List<OrderableField> customFields = new ArrayList<>();
        customFields.add(getOrderedTextFieldFromCustomField(issueTypeList, PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT_ORIGIN));
        customFields.add(getOrderedTextFieldFromCustomField(issueTypeList, PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT_ORIGIN_ID));
        customFields.addAll(createCommonFields(issueTypeList));
        return customFields;
    }

    private FieldScreen createScreen(final String screenName, final String v3ScreenName, final List<OrderableField> blackDuckCustomFields) {
        final Collection<FieldScreen> fieldScreens = jiraServices.getFieldScreenManager().getFieldScreens();

        boolean needToUpdateScreen = false;
        FieldScreen blackDuckScreen = null;
        if (fieldScreens != null && !fieldScreens.isEmpty()) {
            for (final FieldScreen fieldScreen : fieldScreens) {
                final String fieldScreenName = fieldScreen.getName();
                if (screenName.equals(fieldScreenName)) {
                    blackDuckScreen = fieldScreen;
                    break;
                } else if (v3ScreenName.equals(fieldScreenName)) {
                    needToUpdateScreen = true;
                    blackDuckScreen = fieldScreen;
                    blackDuckScreen.setName(screenName);
                    blackDuckScreen.setDescription(screenName);
                    break;
                }
            }
        }
        if (blackDuckScreen == null) {
            blackDuckScreen = createNewScreenImpl(jiraServices.getFieldScreenManager());
            blackDuckScreen.setName(screenName);
            blackDuckScreen.store();
        }
        final FieldScreen defaultScreen = jiraServices.getFieldScreenManager().getFieldScreen(FieldScreen.DEFAULT_SCREEN_ID);

        List<FieldScreenTab> defaultTabs = null;
        if (defaultScreen != null) {
            defaultTabs = defaultScreen.getTabs();
        }

        final boolean wasTabUpdated = addBlackDuckTabToScreen(blackDuckScreen, blackDuckCustomFields, defaultTabs);
        if (needToUpdateScreen || wasTabUpdated) {
            jiraServices.getFieldScreenManager().updateFieldScreen(blackDuckScreen);
        }

        return blackDuckScreen;
    }

    private boolean addBlackDuckTabToScreen(final FieldScreen blackDuckScreen, final List<OrderableField> blackDuckCustomFields, final List<FieldScreenTab> defaultTabs) {
        boolean needToUpdateTabAndScreen = false;
        FieldScreenTab myTab = null;
        if (blackDuckScreen != null && blackDuckScreen.getTabs() != null && !blackDuckScreen.getTabs().isEmpty()) {
            for (final FieldScreenTab screenTab : blackDuckScreen.getTabs()) {
                final String screenTabName = screenTab.getName();
                if (BlackDuckJiraConstants.BLACKDUCK_SCREEN_TAB.equals(screenTabName)) {
                    myTab = screenTab;
                    break;
                } else if (V3PluginConstants.V3_SCREEN_TAB.equals(screenTabName)) {
                    needToUpdateTabAndScreen = true;
                    myTab = screenTab;
                    myTab.setName(BlackDuckJiraConstants.BLACKDUCK_SCREEN_TAB);
                    break;
                }
            }
        }
        if (myTab == null) {
            logger.debug("addBlackDuckTabToScreen(): did not find Black Duck screen tab; adding it");
            myTab = blackDuckScreen.addTab(BlackDuckJiraConstants.BLACKDUCK_SCREEN_TAB);
            needToUpdateTabAndScreen = true;
        }
        if (blackDuckCustomFields != null && !blackDuckCustomFields.isEmpty()) {
            for (final OrderableField field : blackDuckCustomFields) {
                if (field == null) {
                    logger.error("addBlackDuckTabToScreen(): this Black Duck custom field is null; skipping it");
                    continue;
                }
                final FieldScreenLayoutItem existingField = myTab.getFieldScreenLayoutItem(field.getId());
                if (existingField == null) {
                    logger.debug("addBlackDuckTabToScreen(): custom field " + field.getName() + " is not on Black Duck screen tab; adding it");
                    myTab.addFieldScreenLayoutItem(field.getId());
                    needToUpdateTabAndScreen = true;
                }
            }
        }
        if (defaultTabs != null && !defaultTabs.isEmpty()) {
            for (final FieldScreenTab tab : defaultTabs) {
                final List<FieldScreenLayoutItem> layoutItems = tab.getFieldScreenLayoutItems();
                for (final FieldScreenLayoutItem layoutItem : layoutItems) {
                    FieldScreenLayoutItem existingField = null;
                    if (configIsOk(myTab, layoutItem)) {
                        logger.debug("addBlackDuckTabToScreen(): layoutItem: " + layoutItem.getOrderableField().getName());
                        existingField = myTab.getFieldScreenLayoutItem(layoutItem.getOrderableField().getId());
                        if (existingField == null) {
                            logger.debug("addBlackDuckTabToScreen(): field " + layoutItem.getOrderableField().getName() + " is not yet on Black Duck screen tab; adding it");
                            myTab.addFieldScreenLayoutItem(layoutItem.getOrderableField().getId());
                            needToUpdateTabAndScreen = true;
                        }
                    }
                }
            }
        }
        if (needToUpdateTabAndScreen) {
            logger.debug("addBlackDuckTabToScreen(): applying updates to Black Duck screen tab");
            jiraServices.getFieldScreenManager().updateFieldScreenTab(myTab);
        }

        return needToUpdateTabAndScreen;
    }

    private boolean configIsOk(final FieldScreenTab myTab, final FieldScreenLayoutItem layoutItem) {
        boolean isOk = true;
        String msg;
        if (myTab == null) {
            msg = "addBlackDuckTabToScreen(): Black Duck screen tab is null";
            logger.error(msg);
            settingService.addBlackDuckError(msg, "addBlackDuckTabToScreen");
            isOk = false;
        }
        if (layoutItem == null) {
            msg = "addBlackDuckTabToScreen(): layoutItem is null";
            logger.error(msg);
            settingService.addBlackDuckError(msg, "addBlackDuckTabToScreen");
            return false;
        }
        if (layoutItem.getOrderableField() == null) {
            msg = "addBlackDuckTabToScreen(): layoutItem's field is null";
            logger.debug(msg);
            return false;
        }
        return isOk;
    }

    private FieldScreen createPolicyViolationScreen(final IssueType issueType, final List<IssueType> issueTypeList) {
        final List<OrderableField> blackDuckCustomFields = createPolicyViolationFields(issueType, issueTypeList);
        final FieldScreen screen = createScreen(BlackDuckJiraConstants.BLACKDUCK_POLICY_SCREEN_NAME, V3PluginConstants.V3_POLICY_SCREEN_NAME, blackDuckCustomFields);
        return screen;
    }

    private FieldScreen createSecurityScreen(final List<IssueType> issueTypeList) {
        final List<OrderableField> blackDuckCustomFields = createSecurityFields(issueTypeList);
        final FieldScreen screen = createScreen(BlackDuckJiraConstants.BLACKDUCK_SECURITY_SCREEN_NAME, V3PluginConstants.V3_SECURITY_SCREEN_NAME, blackDuckCustomFields);
        return screen;
    }

    private FieldScreenScheme createScreenScheme(final String screenSchemeName, final String v3ScreenSchemeName, final FieldScreen screen) {
        final Collection<FieldScreenScheme> fieldScreenSchemes = jiraServices.getFieldScreenSchemeManager().getFieldScreenSchemes();

        boolean blackDuckScreenSchemeNeedsUpdate = false;
        FieldScreenScheme blackDuckScreenScheme = null;
        if (fieldScreenSchemes != null && !fieldScreenSchemes.isEmpty()) {
            for (final FieldScreenScheme fieldScreenScheme : fieldScreenSchemes) {
                final String foundFieldScreenSchemeName = fieldScreenScheme.getName();
                if (screenSchemeName.equals(foundFieldScreenSchemeName)) {
                    blackDuckScreenScheme = fieldScreenScheme;
                    break;
                } else if (v3ScreenSchemeName.equals(foundFieldScreenSchemeName)) {
                    blackDuckScreenSchemeNeedsUpdate = true;
                    blackDuckScreenScheme = fieldScreenScheme;
                    blackDuckScreenScheme.setName(screenSchemeName);
                    blackDuckScreenScheme.setDescription(screenSchemeName);
                    break;
                }
            }
        }
        if (blackDuckScreenScheme == null) {
            blackDuckScreenScheme = createNewScreenSchemeImpl(jiraServices.getFieldScreenSchemeManager());
            blackDuckScreenScheme.setName(screenSchemeName);
            blackDuckScreenScheme.store();
        }

        final FieldScreen defaultScreen = jiraServices.getFieldScreenManager().getFieldScreen(FieldScreen.DEFAULT_SCREEN_ID);

        final List<ScreenableIssueOperation> issueOpertations = new ArrayList<>();
        issueOpertations.add(IssueOperations.CREATE_ISSUE_OPERATION);
        issueOpertations.add(IssueOperations.VIEW_ISSUE_OPERATION);

        final List<ScreenableIssueOperation> issueOpertationsForDefaultScreen = new ArrayList<>();
        issueOpertations.add(IssueOperations.EDIT_ISSUE_OPERATION);

        if (blackDuckScreenSchemeNeedsUpdate || settingScreenForIssueOperation(issueOpertations, blackDuckScreenScheme, screen) || settingScreenForIssueOperation(issueOpertationsForDefaultScreen, blackDuckScreenScheme, defaultScreen)) {
            jiraServices.getFieldScreenSchemeManager().updateFieldScreenScheme(blackDuckScreenScheme);
        }
        return blackDuckScreenScheme;
    }

    private boolean settingScreenForIssueOperation(final List<ScreenableIssueOperation> issueOpertations, final FieldScreenScheme blackDuckScreenScheme, final FieldScreen screen) {
        boolean blackDuckScreenSchemeNeedsUpdate = false;
        for (final ScreenableIssueOperation issueOperation : issueOpertations) {
            FieldScreenSchemeItem blackDuckScreenSchemeItem = blackDuckScreenScheme.getFieldScreenSchemeItem(issueOperation);
            boolean screenSchemeItemNeedsUpdate = false;
            if (blackDuckScreenSchemeItem == null) {
                blackDuckScreenSchemeItem = createNewFieldScreenSchemeItemImpl(jiraServices.getFieldScreenSchemeManager(), jiraServices.getFieldScreenManager());
                blackDuckScreenSchemeItem.setIssueOperation(issueOperation);
                blackDuckScreenSchemeItem.setFieldScreen(screen);
                blackDuckScreenScheme.addFieldScreenSchemeItem(blackDuckScreenSchemeItem);
                blackDuckScreenSchemeNeedsUpdate = true;
                screenSchemeItemNeedsUpdate = true;
            } else {
                if (blackDuckScreenSchemeItem.getFieldScreen() == null || !blackDuckScreenSchemeItem.getFieldScreen().equals(screen)) {
                    blackDuckScreenSchemeItem.setFieldScreen(screen);
                    screenSchemeItemNeedsUpdate = true;
                }
            }
            if (screenSchemeItemNeedsUpdate) {
                jiraServices.getFieldScreenSchemeManager().updateFieldScreenSchemeItem(blackDuckScreenSchemeItem);
            }
        }
        return blackDuckScreenSchemeNeedsUpdate;
    }

    private FieldScreenScheme createPolicyViolationScreenScheme(final IssueType issueType, final List<IssueType> issueTypeList) {
        final FieldScreen screen = createPolicyViolationScreen(issueType, issueTypeList);
        final FieldScreenScheme fieldScreenScheme = createScreenScheme(BlackDuckJiraConstants.BLACKDUCK_POLICY_SCREEN_SCHEME_NAME, V3PluginConstants.V3_POLICY_SCREEN_SCHEME_NAME, screen);
        return fieldScreenScheme;
    }

    private FieldScreenScheme createSecurityScreenScheme(final List<IssueType> issueTypeList) {
        final FieldScreen screen = createSecurityScreen(issueTypeList);
        final FieldScreenScheme fieldScreenScheme = createScreenScheme(BlackDuckJiraConstants.BLACKDUCK_SECURITY_SCREEN_SCHEME_NAME, V3PluginConstants.V3_SECURITY_SCREEN_SCHEME_NAME, screen);
        return fieldScreenScheme;
    }

}