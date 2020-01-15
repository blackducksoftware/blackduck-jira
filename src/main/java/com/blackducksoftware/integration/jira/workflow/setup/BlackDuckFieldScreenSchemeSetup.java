/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2020 Synopsys, Inc.
 * https://www.synopsys.com/
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
package com.blackducksoftware.integration.jira.workflow.setup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.ofbiz.core.entity.GenericEntityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.issue.context.GlobalIssueContext;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
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
import com.blackducksoftware.integration.jira.common.model.PluginField;
import com.blackducksoftware.integration.jira.data.accessor.PluginErrorAccessor;
import com.blackducksoftware.integration.jira.web.JiraServices;

public class BlackDuckFieldScreenSchemeSetup {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final PluginErrorAccessor pluginErrorAccessor;
    private final JiraServices jiraServices;
    private final Map<PluginField, CustomField> customFields = new HashMap<>();

    public BlackDuckFieldScreenSchemeSetup(PluginErrorAccessor pluginErrorAccessor, JiraServices jiraServices) {
        this.pluginErrorAccessor = pluginErrorAccessor;
        this.jiraServices = jiraServices;
    }

    public JiraServices getJiraServices() {
        return jiraServices;
    }

    public Map<PluginField, CustomField> getCustomFields() {
        return customFields;
    }

    public Map<IssueType, FieldScreenScheme> addBlackDuckFieldConfigurationToJira(List<IssueType> blackDuckIssueTypes) {
        Map<IssueType, FieldScreenScheme> fieldScreenSchemes = new HashMap<>();
        try {
            if (blackDuckIssueTypes != null && !blackDuckIssueTypes.isEmpty()) {
                List<IssueType> policyIssueTypes = new ArrayList<>();
                for (IssueType issueType : blackDuckIssueTypes) {
                    if (issueType.getName().equals(BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_ISSUE) || issueType.getName().equals(BlackDuckJiraConstants.BLACKDUCK_SECURITY_POLICY_VIOLATION_ISSUE)) {
                        policyIssueTypes.add(issueType);
                    } else if (issueType.getName().equals(BlackDuckJiraConstants.BLACKDUCK_VULNERABILITY_ISSUE)) {
                        FieldScreenScheme fieldScreenScheme = createSecurityScreenScheme(blackDuckIssueTypes);
                        fieldScreenSchemes.put(issueType, fieldScreenScheme);
                    }
                }
                FieldScreenScheme fieldScreenScheme = createPolicyViolationScreenScheme(policyIssueTypes, blackDuckIssueTypes);
                for (IssueType policyIssueType : policyIssueTypes) {
                    fieldScreenSchemes.put(policyIssueType, fieldScreenScheme);
                }
            }
        } catch (Exception e) {
            logger.error("An error occurred while creating the screen scheme.", e);
            pluginErrorAccessor.addBlackDuckError(e, "addBlackDuckFieldConfigurationToJira");
        }
        return fieldScreenSchemes;
    }

    public FieldScreen createNewScreenImpl(FieldScreenManager fieldScreenManager) {
        return new FieldScreenImpl(fieldScreenManager);
    }

    public FieldScreenScheme createNewScreenSchemeImpl(FieldScreenSchemeManager fieldScreenSchemeManager) {
        return new FieldScreenSchemeImpl(fieldScreenSchemeManager);
    }

    public FieldScreenSchemeItem createNewFieldScreenSchemeItemImpl(FieldScreenSchemeManager fieldScreenSchemeManager, FieldScreenManager fieldScreenManager) {
        return new FieldScreenSchemeItemImpl(fieldScreenSchemeManager, fieldScreenManager);
    }

    private CustomField createCustomTextField(List<IssueType> issueTypeList, String fieldName) throws RuntimeException {
        return createCustomField(issueTypeList, fieldName, "textarea", "textsearcher");
    }

    private CustomField createCustomUserField(List<IssueType> issueTypeList, String fieldName) throws RuntimeException {
        return createCustomField(issueTypeList, fieldName, "userpicker", "userpickersearcher");
    }

    private CustomField createCustomField(List<IssueType> issueTypeList, String fieldName, String typeSuffix, String searcherSuffix) throws RuntimeException {
        logger.debug("createCustomField(): " + fieldName);
        CustomFieldType fieldType = jiraServices.getCustomFieldManager().getCustomFieldType(CreateCustomField.FIELD_TYPE_PREFIX + typeSuffix);
        CustomFieldSearcher fieldSearcher = jiraServices.getCustomFieldManager().getCustomFieldSearcher(CreateCustomField.FIELD_TYPE_PREFIX + searcherSuffix);

        List<JiraContextNode> contexts = new ArrayList<>();
        contexts.add(GlobalIssueContext.getInstance());

        try {
            return jiraServices.getCustomFieldManager().createCustomField(fieldName, "", fieldType, fieldSearcher, contexts, issueTypeList);
        } catch (GenericEntityException e) {
            // This will be caught by this::getOrderedFieldFromCustomField
            throw new RuntimeException(e);
        }
    }

    private OrderableField getOrderedTextFieldFromCustomField(List<IssueType> issueTypeList, PluginField pluginField) {
        return getOrderedFieldFromCustomField(issueTypeList, pluginField, this::createCustomTextField);
    }

    private OrderableField getOrderedUserFieldFromCustomField(List<IssueType> issueTypeList, PluginField pluginField) {
        return getOrderedFieldFromCustomField(issueTypeList, pluginField, this::createCustomUserField);
    }

    private OrderableField getOrderedFieldFromCustomField(List<IssueType> issueTypeList, PluginField pluginField, BiFunction<List<IssueType>, String, CustomField> createCustomFieldFunction) {
        try {
            Optional<CustomField> optionalCustomField = jiraServices.getCustomFieldManager()
                                                            .getCustomFieldObjectsByName(pluginField.getName())
                                                            .stream()
                                                            .findFirst();
            CustomField customField = optionalCustomField
                                          .orElseGet(() -> createCustomFieldFunction.apply(issueTypeList, pluginField.getName()));
            if (customField.getAssociatedIssueTypes() != null && !customField.getAssociatedIssueTypes().isEmpty()) {
                List<IssueType> associatatedIssueTypeList = customField.getAssociatedIssueTypes();
                boolean needToUpdateCustomField = false;
                for (IssueType issueTypeValue : issueTypeList) {
                    if (!associatatedIssueTypeList.contains(issueTypeValue)) {
                        needToUpdateCustomField = true;
                    }
                }
                if (needToUpdateCustomField) {
                    FieldConfigSchemeManager fieldConfigSchemeManager = jiraServices.getFieldConfigSchemeManager();
                    for (FieldConfigScheme fieldConfigScheme : customField.getConfigurationSchemes()) {
                        Collection<IssueType> associatedIssueTypes = fieldConfigScheme.getAssociatedIssueTypes();
                        Map<String, FieldConfig> issueTypeIdToFieldConfig = fieldConfigScheme.getConfigs();
                        Optional<IssueType> associatedBDIssueType = issueTypeList.stream()
                                                                        .filter(issueType -> associatedIssueTypes.contains(issueType))
                                                                        .findFirst();
                        if (!associatedBDIssueType.isPresent()) {
                            // Setup is incomplete, but the only available way to recover (by deleting the custom attribute and re-creating it) is too dangerous
                            String msg = "The custom field " + customField.getName() + " is missing the Black Duck IssueType associations.";
                            logger.error(msg);
                            pluginErrorAccessor.addBlackDuckError(msg, "getOrderedFieldFromCustomField");
                        } else {
                            Map<String, FieldConfig> updatedConfig = new HashMap<>();
                            IssueType existingBDIssueType = associatedBDIssueType.get();
                            FieldConfig existingFieldConfig = issueTypeIdToFieldConfig.get(existingBDIssueType.getId());

                            List<IssueType> missingIssueTypes = issueTypeList.stream()
                                                                    .filter(issueType -> !associatedIssueTypes.contains(issueType))
                                                                    .collect(Collectors.toList());

                            updatedConfig.putAll(issueTypeIdToFieldConfig);
                            for (IssueType issueType : missingIssueTypes) {
                                updatedConfig.put(issueType.getId(), existingFieldConfig);
                            }

                            FieldConfigScheme.Builder builder = new FieldConfigScheme.Builder(fieldConfigScheme);
                            builder.setConfigs(updatedConfig);
                            FieldConfigScheme updatedFieldConfigScheme = builder.toFieldConfigScheme();

                            fieldConfigSchemeManager.updateFieldConfigScheme(updatedFieldConfigScheme);
                        }
                    }
                }
            } else {
                // Setup is incomplete, but the only available way to recover (by deleting the custom attribute and re-creating it) is too dangerous
                String msg = "The custom field " + customField.getName() + " has no IssueType associations.";
                logger.error(msg);
                pluginErrorAccessor.addBlackDuckError(msg, "getOrderedFieldFromCustomField");
            }
            customFields.put(pluginField, customField);
            OrderableField myField = jiraServices.getFieldManager().getOrderableField(customField.getId());
            return myField;
        } catch (Exception e) {
            logger.error("Error in getOrderedFieldFromCustomField(): " + e.getMessage(), e);
            pluginErrorAccessor.addBlackDuckError(e, "getOrderedFieldFromCustomField");
        }
        return null;
    }

    private List<OrderableField> createCommonFields(List<IssueType> issueTypeList) {
        List<OrderableField> customFields = new ArrayList<>();
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
        customFields.add(getOrderedUserFieldFromCustomField(issueTypeList, PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT_REVIEWER));
        customFields.add(getOrderedTextFieldFromCustomField(issueTypeList, PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION_LAST_UPDATED));

        return customFields;
    }

    private List<OrderableField> createPolicyViolationFields(List<IssueType> policyIssueTypes, List<IssueType> issueTypeList) {
        List<OrderableField> customFields = new ArrayList<>();
        customFields.add(getOrderedTextFieldFromCustomField(policyIssueTypes, PluginField.BLACKDUCK_CUSTOM_FIELD_POLICY_RULE));
        customFields.add(getOrderedTextFieldFromCustomField(policyIssueTypes, PluginField.BLACKDUCK_CUSTOM_FIELD_POLICY_RULE_OVERRIDABLE));
        customFields.add(getOrderedTextFieldFromCustomField(policyIssueTypes, PluginField.BLACKDUCK_CUSTOM_FIELD_POLICY_RULE_SEVERITY));
        customFields.add(getOrderedTextFieldFromCustomField(policyIssueTypes, PluginField.BLACKDUCK_CUSTOM_FIELD_POLICY_RULE_DESCRIPTION));
        customFields.add(getOrderedTextFieldFromCustomField(policyIssueTypes, PluginField.BLACKDUCK_CUSTOM_FIELD_POLICY_RULE_URL));
        customFields.addAll(createCommonFields(issueTypeList));
        return customFields;
    }

    private List<OrderableField> createSecurityFields(List<IssueType> issueTypeList) {
        List<OrderableField> customFields = new ArrayList<>();
        customFields.add(getOrderedTextFieldFromCustomField(issueTypeList, PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT_ORIGIN));
        customFields.add(getOrderedTextFieldFromCustomField(issueTypeList, PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT_ORIGIN_ID));
        customFields.addAll(createCommonFields(issueTypeList));
        return customFields;
    }

    private FieldScreen createScreen(String screenName, List<OrderableField> blackDuckCustomFields) {
        Collection<FieldScreen> fieldScreens = jiraServices.getFieldScreenManager().getFieldScreens();

        FieldScreen blackDuckScreen = null;
        if (fieldScreens != null && !fieldScreens.isEmpty()) {
            for (FieldScreen fieldScreen : fieldScreens) {
                String fieldScreenName = fieldScreen.getName();
                if (screenName.equals(fieldScreenName)) {
                    blackDuckScreen = fieldScreen;
                    break;
                }
            }
        }
        if (blackDuckScreen == null) {
            blackDuckScreen = createNewScreenImpl(jiraServices.getFieldScreenManager());
            blackDuckScreen.setName(screenName);
            blackDuckScreen.store();
        }
        FieldScreen defaultScreen = jiraServices.getFieldScreenManager().getFieldScreen(FieldScreen.DEFAULT_SCREEN_ID);

        List<FieldScreenTab> defaultTabs = null;
        if (defaultScreen != null) {
            defaultTabs = defaultScreen.getTabs();
        }

        boolean wasTabUpdated = addBlackDuckTabToScreen(blackDuckScreen, blackDuckCustomFields, defaultTabs);
        if (wasTabUpdated) {
            jiraServices.getFieldScreenManager().updateFieldScreen(blackDuckScreen);
        }

        return blackDuckScreen;
    }

    private boolean addBlackDuckTabToScreen(FieldScreen blackDuckScreen, List<OrderableField> blackDuckCustomFields, List<FieldScreenTab> defaultTabs) {
        boolean needToUpdateTabAndScreen = false;
        FieldScreenTab myTab = null;
        if (blackDuckScreen != null && blackDuckScreen.getTabs() != null && !blackDuckScreen.getTabs().isEmpty()) {
            for (FieldScreenTab screenTab : blackDuckScreen.getTabs()) {
                String screenTabName = screenTab.getName();
                if (BlackDuckJiraConstants.BLACKDUCK_SCREEN_TAB.equals(screenTabName)) {
                    myTab = screenTab;
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
            for (OrderableField field : blackDuckCustomFields) {
                if (field == null) {
                    logger.error("addBlackDuckTabToScreen(): this Black Duck custom field is null; skipping it");
                    continue;
                }
                Optional<FieldScreenLayoutItem> existingField = getExistingField(myTab, field.getId());
                if (!existingField.isPresent()) {
                    logger.debug("addBlackDuckTabToScreen(): custom field " + field.getName() + " is not on Black Duck screen tab; adding it");
                    myTab.addFieldScreenLayoutItem(field.getId());
                    needToUpdateTabAndScreen = true;
                }
            }
        }
        if (defaultTabs != null && !defaultTabs.isEmpty()) {
            for (FieldScreenTab tab : defaultTabs) {
                List<FieldScreenLayoutItem> layoutItems = tab.getFieldScreenLayoutItems();
                for (FieldScreenLayoutItem layoutItem : layoutItems) {
                    if (configIsOk(myTab, layoutItem)) {
                        Optional<FieldScreenLayoutItem> existingField = getExistingField(myTab, layoutItem.getOrderableField().getId());
                        if (!existingField.isPresent()) {
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

    private Optional<FieldScreenLayoutItem> getExistingField(FieldScreenTab tab, String id) {
        // Stream over the layout items to prevent the Jira internal Duplicate Key exception.
        return tab.getFieldScreenLayoutItems().stream()
                   .filter(fieldScreenLayoutItem -> fieldScreenLayoutItem.getFieldId().equals(id))
                   .findFirst();
    }

    private boolean configIsOk(FieldScreenTab myTab, FieldScreenLayoutItem layoutItem) {
        boolean isOk = true;
        String msg;
        if (myTab == null) {
            msg = "addBlackDuckTabToScreen(): Black Duck screen tab is null";
            logger.error(msg);
            pluginErrorAccessor.addBlackDuckError(msg, "addBlackDuckTabToScreen");
            isOk = false;
        }
        if (layoutItem == null) {
            msg = "addBlackDuckTabToScreen(): layoutItem is null";
            logger.error(msg);
            pluginErrorAccessor.addBlackDuckError(msg, "addBlackDuckTabToScreen");
            return false;
        }
        if (layoutItem.getOrderableField() == null) {
            msg = "addBlackDuckTabToScreen(): layoutItem's field is null";
            logger.debug(msg);
            return false;
        }
        return isOk;
    }

    private FieldScreen createPolicyViolationScreen(List<IssueType> policyIssueTypes, List<IssueType> issueTypeList) {
        List<OrderableField> blackDuckCustomFields = createPolicyViolationFields(policyIssueTypes, issueTypeList);
        FieldScreen screen = createScreen(BlackDuckJiraConstants.BLACKDUCK_POLICY_SCREEN_NAME, blackDuckCustomFields);
        return screen;
    }

    private FieldScreen createSecurityScreen(List<IssueType> issueTypeList) {
        List<OrderableField> blackDuckCustomFields = createSecurityFields(issueTypeList);
        FieldScreen screen = createScreen(BlackDuckJiraConstants.BLACKDUCK_SECURITY_SCREEN_NAME, blackDuckCustomFields);
        return screen;
    }

    private FieldScreenScheme createScreenScheme(String screenSchemeName, FieldScreen screen) {
        Collection<FieldScreenScheme> fieldScreenSchemes = jiraServices.getFieldScreenSchemeManager().getFieldScreenSchemes();

        FieldScreenScheme blackDuckScreenScheme = null;
        if (fieldScreenSchemes != null && !fieldScreenSchemes.isEmpty()) {
            for (FieldScreenScheme fieldScreenScheme : fieldScreenSchemes) {
                String foundFieldScreenSchemeName = fieldScreenScheme.getName();
                if (screenSchemeName.equals(foundFieldScreenSchemeName)) {
                    blackDuckScreenScheme = fieldScreenScheme;
                    break;
                }
            }
        }
        if (blackDuckScreenScheme == null) {
            blackDuckScreenScheme = createNewScreenSchemeImpl(jiraServices.getFieldScreenSchemeManager());
            blackDuckScreenScheme.setName(screenSchemeName);
            blackDuckScreenScheme.store();
        }

        FieldScreen defaultScreen = jiraServices.getFieldScreenManager().getFieldScreen(FieldScreen.DEFAULT_SCREEN_ID);

        List<ScreenableIssueOperation> issueOpertations = new ArrayList<>();
        issueOpertations.add(IssueOperations.CREATE_ISSUE_OPERATION);
        issueOpertations.add(IssueOperations.VIEW_ISSUE_OPERATION);

        List<ScreenableIssueOperation> issueOpertationsForDefaultScreen = new ArrayList<>();
        issueOpertations.add(IssueOperations.EDIT_ISSUE_OPERATION);

        if (settingScreenForIssueOperation(issueOpertations, blackDuckScreenScheme, screen) || settingScreenForIssueOperation(issueOpertationsForDefaultScreen, blackDuckScreenScheme, defaultScreen)) {
            jiraServices.getFieldScreenSchemeManager().updateFieldScreenScheme(blackDuckScreenScheme);
        }
        return blackDuckScreenScheme;
    }

    private boolean settingScreenForIssueOperation(List<ScreenableIssueOperation> issueOpertations, FieldScreenScheme blackDuckScreenScheme, FieldScreen screen) {
        boolean blackDuckScreenSchemeNeedsUpdate = false;
        for (ScreenableIssueOperation issueOperation : issueOpertations) {
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

    private FieldScreenScheme createPolicyViolationScreenScheme(List<IssueType> policyIssueTypes, List<IssueType> issueTypeList) {
        FieldScreen screen = createPolicyViolationScreen(policyIssueTypes, issueTypeList);
        FieldScreenScheme fieldScreenScheme = createScreenScheme(BlackDuckJiraConstants.BLACKDUCK_POLICY_SCREEN_SCHEME_NAME, screen);
        return fieldScreenScheme;
    }

    private FieldScreenScheme createSecurityScreenScheme(List<IssueType> issueTypeList) {
        FieldScreen screen = createSecurityScreen(issueTypeList);
        FieldScreenScheme fieldScreenScheme = createScreenScheme(BlackDuckJiraConstants.BLACKDUCK_SECURITY_SCREEN_SCHEME_NAME, screen);
        return fieldScreenScheme;
    }

}
