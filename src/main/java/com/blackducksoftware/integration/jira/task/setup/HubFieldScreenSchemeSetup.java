/**
 * Hub JIRA Plugin
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
import com.blackducksoftware.integration.jira.common.PluginField;
import com.blackducksoftware.integration.jira.task.JiraSettingsService;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;

public class HubFieldScreenSchemeSetup {

    private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final JiraSettingsService settingService;

    private final JiraServices jiraServices;

    private final Map<PluginField, CustomField> customFields = new HashMap<>();

    public HubFieldScreenSchemeSetup(final JiraSettingsService settingService, final JiraServices jiraServices) {
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

    public Map<IssueType, FieldScreenScheme> addHubFieldConfigurationToJira(final List<IssueType> hubIssueTypes) {
        final Map<IssueType, FieldScreenScheme> fieldScreenSchemes = new HashMap<>();
        try {
            if (hubIssueTypes != null && !hubIssueTypes.isEmpty()) {
                for (final IssueType issueType : hubIssueTypes) {
                    if (issueType.getName().equals(HubJiraConstants.HUB_POLICY_VIOLATION_ISSUE)) {
                        final FieldScreenScheme fss = createPolicyViolationScreenScheme(issueType,
                                hubIssueTypes);
                        fieldScreenSchemes.put(issueType, fss);
                    } else if (issueType.getName().equals(HubJiraConstants.HUB_VULNERABILITY_ISSUE)) {
                        final FieldScreenScheme fss = createSecurityScreenScheme(issueType,
                                hubIssueTypes);
                        fieldScreenSchemes.put(issueType, fss);
                    }
                }
            }
        } catch (final Exception e) {
            logger.error(e);
            settingService.addHubError(e, "addHubFieldConfigurationToJira");
        }
        return fieldScreenSchemes;
    }

    private IssueType getIssueTypeObject(final IssueType hubIssueType) {
        return hubIssueType;
    }

    private CustomField createCustomField(final List<IssueType> issueTypeList, final String fieldName)
            throws GenericEntityException {
        logger.debug("createCustomField(): " + fieldName);
        final CustomFieldType fieldType = jiraServices.getCustomFieldManager()
                .getCustomFieldType(CreateCustomField.FIELD_TYPE_PREFIX + "textarea");
        final CustomFieldSearcher fieldSearcher = jiraServices.getCustomFieldManager()
                .getCustomFieldSearcher(CreateCustomField.FIELD_TYPE_PREFIX + "textsearcher");

        final List<JiraContextNode> contexts = new ArrayList<>();
        contexts.add(GlobalIssueContext.getInstance());

        return jiraServices.getCustomFieldManager().createCustomField(fieldName, "", fieldType, fieldSearcher, contexts,
                issueTypeList);
    }

    private OrderableField getOrderedFieldFromCustomField(final List<IssueType> issueTypeList,
            final PluginField pluginField) {
        try {
            CustomField customField = jiraServices.getCustomFieldManager().getCustomFieldObjectByName(pluginField.getName());
            if (customField == null) {
                customField = createCustomField(issueTypeList, pluginField.getName());
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
                    // Setup is incomplete, but the only available way to recover
                    // (by deleting the custom attribute and re-creating it) is too dangerous
                    final String msg = "The custom field " + customField.getName() +
                            " is missing " +
                            "one or more IssueType associations.";
                    logger.error(msg);
                    settingService.addHubError(msg, "getOrderedFieldFromCustomField");
                }
            } else {
                // Setup is incomplete, but the only available way to recover
                // (by deleting the custom attribute and re-creating it) is too dangerous
                final String msg = "The custom field " + customField.getName() +
                        " has no IssueType associations.";
                logger.error(msg);
                settingService.addHubError(msg, "getOrderedFieldFromCustomField");
            }
            customFields.put(pluginField, customField);
            final OrderableField myField = jiraServices.getFieldManager().getOrderableField(customField.getId());
            return myField;
        } catch (final Exception e) {
            logger.error("Error in getOrderedFieldFromCustomField(): " + e.getMessage(), e);
            settingService.addHubError(e, "getOrderedFieldFromCustomField");
        }
        return null;
    }

    private List<OrderableField> createCommonFields(final List<IssueType> issueTypeList) {
        final List<OrderableField> customFields = new ArrayList<>();
        customFields.add(getOrderedFieldFromCustomField(issueTypeList, PluginField.HUB_CUSTOM_FIELD_PROJECT));
        customFields.add(getOrderedFieldFromCustomField(issueTypeList, PluginField.HUB_CUSTOM_FIELD_PROJECT_VERSION));
        customFields.add(getOrderedFieldFromCustomField(issueTypeList, PluginField.HUB_CUSTOM_FIELD_COMPONENT));
        customFields.add(getOrderedFieldFromCustomField(issueTypeList, PluginField.HUB_CUSTOM_FIELD_COMPONENT_VERSION));
        customFields.add(getOrderedFieldFromCustomField(issueTypeList, PluginField.HUB_CUSTOM_FIELD_LICENSE_NAMES));

        customFields.add(getOrderedFieldFromCustomField(issueTypeList, PluginField.HUB_CUSTOM_FIELD_COMPONENT_USAGE));
        customFields.add(getOrderedFieldFromCustomField(issueTypeList, PluginField.HUB_CUSTOM_FIELD_COMPONENT_ORIGIN));
        customFields.add(getOrderedFieldFromCustomField(issueTypeList, PluginField.HUB_CUSTOM_FIELD_COMPONENT_ORIGIN_ID));
        customFields.add(getOrderedFieldFromCustomField(issueTypeList, PluginField.HUB_CUSTOM_FIELD_PROJECT_VERSION_NICKNAME));

        customFields.add(getOrderedFieldFromCustomField(issueTypeList, PluginField.HUB_CUSTOM_FIELD_PROJECT_OWNER));
        customFields.add(getOrderedFieldFromCustomField(issueTypeList, PluginField.HUB_CUSTOM_FIELD_PROJECT_VERSION_LAST_UPDATED));

        return customFields;
    }

    private List<OrderableField> createPolicyViolationFields(final IssueType issueType,
            final List<IssueType> issueTypeList) {
        final List<OrderableField> customFields = new ArrayList<>();
        final List<IssueType> policyViolationIssueTypeObjectList = new ArrayList<>();
        policyViolationIssueTypeObjectList.add(getIssueTypeObject(issueType));
        customFields.add(getOrderedFieldFromCustomField(policyViolationIssueTypeObjectList,
                PluginField.HUB_CUSTOM_FIELD_POLICY_RULE));
        customFields.addAll(createCommonFields(issueTypeList));
        return customFields;
    }

    private List<OrderableField> createSecurityFields(final IssueType issueType,
            final List<IssueType> issueTypeList) {
        final List<OrderableField> customFields = new ArrayList<>();
        customFields.addAll(createCommonFields(issueTypeList));
        return customFields;
    }

    public FieldScreen createNewScreenImpl(final FieldScreenManager fieldScreenManager) {
        return new FieldScreenImpl(fieldScreenManager);
    }

    private FieldScreen createScreen(final String screenName, final List<OrderableField> hubCustomFields) {
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

        final boolean needToUpdateScreen = addHubTabToScreen(hubScreen, hubCustomFields, defaultTabs);

        if (needToUpdateScreen) {
            jiraServices.getFieldScreenManager().updateFieldScreen(hubScreen);
        }

        return hubScreen;
    }

    private boolean addHubTabToScreen(final FieldScreen hubScreen, final List<OrderableField> hubCustomFields,
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
            logger.debug("addHubTabToScreen(): did not find hub screen tab; adding it");
            myTab = hubScreen.addTab(HubJiraConstants.HUB_SCREEN_TAB);
            needToUpdateTabAndScreen = true;
        }
        if (hubCustomFields != null && !hubCustomFields.isEmpty()) {
            for (final OrderableField field : hubCustomFields) {
                if (field == null) {
                    logger.error("addHubTabToScreen(): this Hub custom field is null; skipping it");
                    continue;
                }
                final FieldScreenLayoutItem existingField = myTab.getFieldScreenLayoutItem(field.getId());
                if (existingField == null) {
                    logger.debug("addHubTabToScreen(): custom field " + field.getName()
                            + " is not on hub screen tab; adding it");
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
                        logger.debug("addHubTabToScreen(): layoutItem: " + layoutItem.getOrderableField().getName());
                        existingField = myTab
                                .getFieldScreenLayoutItem(layoutItem.getOrderableField().getId());
                        if (existingField == null) {
                            logger.debug("addHubTabToScreen(): field " + layoutItem.getOrderableField().getName()
                                    + " is not yet on Hub screen tab; adding it");
                            myTab.addFieldScreenLayoutItem(layoutItem.getOrderableField().getId());
                            needToUpdateTabAndScreen = true;
                        }
                    }
                }
            }
        }
        if (needToUpdateTabAndScreen) {
            logger.debug("addHubTabToScreen(): applying updates to Hub screen tab");
            jiraServices.getFieldScreenManager().updateFieldScreenTab(myTab);
        }

        return needToUpdateTabAndScreen;
    }

    private boolean configIsOk(final FieldScreenTab myTab, final FieldScreenLayoutItem layoutItem) {
        boolean isOk = true;
        String msg;
        if (myTab == null) {
            msg = "addHubTabToScreen(): Hub screen tab is null";
            logger.error(msg);
            settingService.addHubError(msg, "addHubTabToScreen");
            isOk = false;
        }
        if (layoutItem == null) {
            msg = "addHubTabToScreen(): layoutItem is null";
            logger.error(msg);
            settingService.addHubError(msg, "addHubTabToScreen");
            return false;
        }
        if (layoutItem.getOrderableField() == null) {
            msg = "addHubTabToScreen(): layoutItem's field is null";
            logger.debug(msg);
            return false;
        }
        return isOk;
    }

    private FieldScreen createPolicyViolationScreen(final IssueType issueType,
            final List<IssueType> issueTypeList) {
        final List<OrderableField> hubCustomFields = createPolicyViolationFields(issueType,
                issueTypeList);
        final FieldScreen screen = createScreen(HubJiraConstants.HUB_POLICY_SCREEN_NAME, hubCustomFields);
        return screen;
    }

    private FieldScreen createSecurityScreen(final IssueType issueType,
            final List<IssueType> issueTypeList) {
        final List<OrderableField> hubCustomFields = createSecurityFields(issueType, issueTypeList);
        final FieldScreen screen = createScreen(HubJiraConstants.HUB_SECURITY_SCREEN_NAME, hubCustomFields);
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
            final List<IssueType> issueTypeList) {
        final FieldScreen screen = createPolicyViolationScreen(issueType, issueTypeList);
        final FieldScreenScheme fieldScreenScheme = createScreenScheme(HubJiraConstants.HUB_POLICY_SCREEN_SCHEME_NAME,
                screen);
        return fieldScreenScheme;
    }

    private FieldScreenScheme createSecurityScreenScheme(final IssueType issueType,
            final List<IssueType> issueTypeList) {
        final FieldScreen screen = createSecurityScreen(issueType, issueTypeList);
        final FieldScreenScheme fieldScreenScheme = createScreenScheme(
                HubJiraConstants.HUB_SECURITY_SCREEN_SCHEME_NAME, screen);
        return fieldScreenScheme;
    }

}
