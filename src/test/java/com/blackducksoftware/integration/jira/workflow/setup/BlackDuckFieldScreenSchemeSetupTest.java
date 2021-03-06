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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;
import com.blackducksoftware.integration.jira.common.model.PluginField;
import com.blackducksoftware.integration.jira.data.accessor.JiraSettingsAccessor;
import com.blackducksoftware.integration.jira.data.accessor.PluginErrorAccessor;
import com.blackducksoftware.integration.jira.mocks.JiraServicesMock;
import com.blackducksoftware.integration.jira.mocks.PluginSettingsMock;
import com.blackducksoftware.integration.jira.mocks.field.CustomFieldManagerMock;
import com.blackducksoftware.integration.jira.mocks.field.CustomFieldMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldConfigSchemeManagerMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldConfigSchemeMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldManagerMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldScreenManagerMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldScreenMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldScreenSchemeItemMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldScreenSchemeManagerMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldScreenSchemeMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldScreenTabMock;
import com.blackducksoftware.integration.jira.mocks.issue.IssueTypeMock;
import com.blackducksoftware.integration.jira.web.JiraServices;

public class BlackDuckFieldScreenSchemeSetupTest {
    private static final String POLICY_RULE_ERROR_MESSAGE = "The custom field " + BlackDuckJiraConstants.BLACKDUCK_CUSTOM_FIELD_POLICY_RULE + " has no IssueType associations";

    private static final int NUM_FIELDS_TOTAL = PluginField.values().length;
    private static final int NUM_FIELDS_POLICY = NUM_FIELDS_TOTAL - 2;
    private static final int NUM_FIELDS_VULNERABILITY = NUM_FIELDS_TOTAL - 4;

    @Test
    public void testAddBlackDuckFieldConfigurationToJiraOneMissingIssueTypeAssoc() throws GenericEntityException {
        final PluginSettingsMock settingsMock = new PluginSettingsMock();
        final PluginErrorAccessor pluginErrorAccessor = new PluginErrorAccessor(new JiraSettingsAccessor(settingsMock));

        final JiraServicesMock jiraServices = new JiraServicesMock();
        final CustomFieldManagerMock customFieldManagerMock = new CustomFieldManagerMock();
        final FieldManager fieldManager = new FieldManagerMock(customFieldManagerMock);
        jiraServices.setFieldManager(fieldManager);
        final FieldScreenManager fieldScreenManager = new FieldScreenManagerMock();
        jiraServices.setFieldScreenManager(fieldScreenManager);
        final FieldScreenSchemeManager fieldScreenSchemeManager = new FieldScreenSchemeManagerMock();
        jiraServices.setFieldScreenSchemeManager(fieldScreenSchemeManager);
        FieldConfigSchemeManager fieldConfigSchemeManager = new FieldConfigSchemeManagerMock();
        jiraServices.setFieldConfigSchemeManager(fieldConfigSchemeManager);

        final BlackDuckFieldScreenSchemeSetup fieldConfigSetupOrig = new BlackDuckFieldScreenSchemeSetup(pluginErrorAccessor, jiraServices);
        final BlackDuckFieldScreenSchemeSetup fieldConfigSetup = Mockito.spy(fieldConfigSetupOrig);
        Mockito.when(fieldConfigSetup.createNewScreenSchemeImpl(Mockito.any(FieldScreenSchemeManager.class))).thenAnswer(x -> new FieldScreenSchemeMock());
        final FieldScreen screen = new FieldScreenMock();
        Mockito.when(fieldConfigSetup.createNewScreenImpl(Mockito.any(FieldScreenManager.class))).thenReturn(screen);

        // Create a custom field
        jiraServices.setCustomFieldManager(customFieldManagerMock);
        final List<IssueType> blackDuckIssueTypes = getBlackDuckIssueTypes();
        // Associated only ONE Black Duck IssueType with it (an incomplete/broken config)
        final List<IssueType> associatedIssueTypes = new ArrayList<>();
        associatedIssueTypes.add(blackDuckIssueTypes.get(0));

        createCustomField(jiraServices, BlackDuckJiraConstants.BLACKDUCK_CUSTOM_FIELD_PROJECT, BlackDuckJiraConstants.BLACKDUCK_CUSTOM_FIELD_PROJECT, associatedIssueTypes);

        // See how this handles the incomplete config
        fieldConfigSetup.addBlackDuckFieldConfigurationToJira(blackDuckIssueTypes);

        assertTrue(settingsMock.isEmpty());
    }

    @Test
    public void testAddBlackDuckFieldConfigurationToJiraMissingAllIssueTypeAssoc() throws GenericEntityException {
        final PluginSettingsMock settingsMock = new PluginSettingsMock();
        final PluginErrorAccessor pluginErrorAccessor = new PluginErrorAccessor(new JiraSettingsAccessor(settingsMock));

        final JiraServicesMock jiraServices = new JiraServicesMock();
        final CustomFieldManagerMock customFieldManagerMock = new CustomFieldManagerMock();
        final FieldManager fieldManager = new FieldManagerMock(customFieldManagerMock);
        jiraServices.setFieldManager(fieldManager);
        final FieldScreenManager fieldScreenManager = new FieldScreenManagerMock();
        jiraServices.setFieldScreenManager(fieldScreenManager);
        final FieldScreenSchemeManager fieldScreenSchemeManager = new FieldScreenSchemeManagerMock();
        jiraServices.setFieldScreenSchemeManager(fieldScreenSchemeManager);
        FieldConfigSchemeManager fieldConfigSchemeManager = new FieldConfigSchemeManagerMock();
        jiraServices.setFieldConfigSchemeManager(fieldConfigSchemeManager);

        final BlackDuckFieldScreenSchemeSetup fieldConfigSetupOrig = new BlackDuckFieldScreenSchemeSetup(pluginErrorAccessor, jiraServices);
        final BlackDuckFieldScreenSchemeSetup fieldConfigSetup = Mockito.spy(fieldConfigSetupOrig);
        Mockito.when(fieldConfigSetup.createNewScreenSchemeImpl(Mockito.any(FieldScreenSchemeManager.class))).thenAnswer(x -> new FieldScreenSchemeMock());
        final FieldScreen screen = new FieldScreenMock();
        Mockito.when(fieldConfigSetup.createNewScreenImpl(Mockito.any(FieldScreenManager.class))).thenReturn(screen);

        // Create a custom field
        jiraServices.setCustomFieldManager(customFieldManagerMock);
        // Associated no Black Duck IssueType with it (an incomplete/broken config)
        final IssueTypeMock randomIssueType = createMockIssueType("Random", "Random");
        final List<IssueType> associatedIssueTypes = new ArrayList<>();
        associatedIssueTypes.add(randomIssueType);

        createCustomField(jiraServices, BlackDuckJiraConstants.BLACKDUCK_CUSTOM_FIELD_PROJECT, BlackDuckJiraConstants.BLACKDUCK_CUSTOM_FIELD_PROJECT, associatedIssueTypes);

        final List<IssueType> blackDuckIssueTypes = getBlackDuckIssueTypes();

        // See how this handles the incomplete config
        fieldConfigSetup.addBlackDuckFieldConfigurationToJira(blackDuckIssueTypes);

        assertFalse(settingsMock.isEmpty());
        assertTrue(((String) settingsMock.get(BlackDuckJiraConstants.BLACKDUCK_JIRA_ERROR)).contains("The custom field " + BlackDuckJiraConstants.BLACKDUCK_CUSTOM_FIELD_PROJECT + " is missing the Black Duck IssueType associations"));
    }

    @Test
    public void testAddBlackDuckFieldConfigurationToJiraFirstTimeCreateNullIssueTypes() {
        final PluginSettingsMock settingsMock = new PluginSettingsMock();
        final PluginErrorAccessor pluginErrorAccessor = new PluginErrorAccessor(new JiraSettingsAccessor(settingsMock));

        final JiraServicesMock jiraServices = new JiraServicesMock();
        jiraServices.setCustomFieldManager(new CustomFieldManagerMock());

        final BlackDuckFieldScreenSchemeSetup fieldConfigSetup = new BlackDuckFieldScreenSchemeSetup(pluginErrorAccessor, jiraServices);
        fieldConfigSetup.addBlackDuckFieldConfigurationToJira(null);

        assertNull(settingsMock.get(BlackDuckJiraConstants.BLACKDUCK_JIRA_ERROR));
    }

    @Test
    public void testAddBlackDuckFieldConfigurationToJiraFirstTimeCreateNoIssueTypes() {
        final PluginSettingsMock settingsMock = new PluginSettingsMock();
        final PluginErrorAccessor pluginErrorAccessor = new PluginErrorAccessor(new JiraSettingsAccessor(settingsMock));

        final JiraServicesMock jiraServices = new JiraServicesMock();
        jiraServices.setCustomFieldManager(new CustomFieldManagerMock());
        final BlackDuckFieldScreenSchemeSetup fieldConfigSetup = new BlackDuckFieldScreenSchemeSetup(pluginErrorAccessor, jiraServices);
        fieldConfigSetup.addBlackDuckFieldConfigurationToJira(new ArrayList<>());

        assertNull(settingsMock.get(BlackDuckJiraConstants.BLACKDUCK_JIRA_ERROR));
    }

    @Test
    public void testAddBlackDuckFieldConfigurationToJiraFirstTimeCreate() {
        final PluginSettingsMock settingsMock = new PluginSettingsMock();
        final PluginErrorAccessor pluginErrorAccessor = new PluginErrorAccessor(new JiraSettingsAccessor(settingsMock));

        final CustomFieldManagerMock customFieldManager = new CustomFieldManagerMock();
        final FieldManagerMock fieldManager = new FieldManagerMock(customFieldManager);
        final FieldScreenManagerMock fieldScreenManager = new FieldScreenManagerMock();
        final FieldScreenSchemeManagerMock fieldScreenSchemeManager = new FieldScreenSchemeManagerMock();

        final JiraServicesMock jiraServices = new JiraServicesMock();
        jiraServices.setCustomFieldManager(customFieldManager);
        jiraServices.setFieldManager(fieldManager);
        jiraServices.setFieldScreenManager(fieldScreenManager);
        jiraServices.setFieldScreenSchemeManager(fieldScreenSchemeManager);

        final List<IssueType> issueTypes = getBlackDuckIssueTypes();

        BlackDuckFieldScreenSchemeSetup fieldConfigSetup = new BlackDuckFieldScreenSchemeSetup(pluginErrorAccessor, jiraServices);
        fieldConfigSetup = Mockito.spy(fieldConfigSetup);

        Mockito.when(fieldConfigSetup.createNewScreenImpl(Mockito.any(FieldScreenManager.class))).thenAnswer(x -> new FieldScreenMock());
        Mockito.when(fieldConfigSetup.createNewScreenSchemeImpl(Mockito.any(FieldScreenSchemeManager.class))).thenAnswer(x -> new FieldScreenSchemeMock());
        Mockito.when(fieldConfigSetup.createNewFieldScreenSchemeItemImpl(Mockito.any(FieldScreenSchemeManager.class), Mockito.any(FieldScreenManager.class))).thenAnswer(x -> new FieldScreenSchemeItemMock());

        fieldConfigSetup.addBlackDuckFieldConfigurationToJira(issueTypes);

        assertEquals(NUM_FIELDS_TOTAL, customFieldManager.getCustomFieldObjects().size());
        for (final FieldScreen fieldScreen : fieldScreenManager.getUpdatedScreens()) {
            final FieldScreenMock fieldScreenMock = (FieldScreenMock) fieldScreen;
            assertTrue(fieldScreenMock.getAttemptedScreenStore());
        }
        assertEquals(2, fieldScreenManager.getUpdatedTabs().size());

        for (final FieldScreenTab tab : fieldScreenManager.getUpdatedTabs()) {
            final String screenName = tab.getFieldScreen().getName();
            if (screenName.equals(BlackDuckJiraConstants.BLACKDUCK_POLICY_SCREEN_NAME)) {
                assertEquals(NUM_FIELDS_POLICY, tab.getFieldScreenLayoutItems().size());
            } else if (screenName.equals(BlackDuckJiraConstants.BLACKDUCK_POLICY_SCREEN_NAME)) {
                assertEquals(NUM_FIELDS_VULNERABILITY, tab.getFieldScreenLayoutItems().size());
            }
        }
        assertEquals(2, fieldScreenManager.getUpdatedScreens().size());
        for (final FieldScreenScheme fieldScreenScheme : fieldScreenSchemeManager.getUpdatedSchemes()) {
            final FieldScreenSchemeMock fieldScreenSchemeMock = (FieldScreenSchemeMock) fieldScreenScheme;
            assertTrue(fieldScreenSchemeMock.getAttemptedScreenSchemeStore());

            for (final FieldScreenSchemeItem currentSchemeItem : fieldScreenScheme.getFieldScreenSchemeItems()) {
                assertTrue(currentSchemeItem.getFieldScreen().getName()
                               .equals(BlackDuckJiraConstants.BLACKDUCK_POLICY_SCREEN_NAME)
                               || currentSchemeItem.getFieldScreen().getName()
                                      .equals(BlackDuckJiraConstants.BLACKDUCK_SECURITY_SCREEN_NAME));
            }
        }
        assertEquals(2, fieldScreenSchemeManager.getUpdatedSchemes().size());
        assertEquals(6, fieldScreenSchemeManager.getUpdatedSchemeItems().size());
        assertNotNull(settingsMock);
        assertTrue(settingsMock.isEmpty());
    }

    @Test
    public void testAddBlackDuckFieldConfigurationToJiraFirstTimeCreateWithDefaultTabsAndFields() {
        doBasicTest(false);
    }

    @Test
    public void testAddBlackDuckFieldConfigurationToJiraFirstTimeCreateWithDefaultTabsWithNullFields() {
        doBasicTest(true);
    }

    private void doBasicTest(final boolean includeSomeNullCustomFields) {
        int expectedPolicyFields = NUM_FIELDS_POLICY + 3;
        if (!includeSomeNullCustomFields) {
            expectedPolicyFields++;
        }
        int expectedVulnerabilityFields = 7;
        if (!includeSomeNullCustomFields) {
            expectedVulnerabilityFields++;
        }
        final PluginSettingsMock settingsMock = new PluginSettingsMock();
        final PluginErrorAccessor pluginErrorAccessor = new PluginErrorAccessor(new JiraSettingsAccessor(settingsMock));

        final CustomFieldManagerMock customFieldManager = new CustomFieldManagerMock();
        final FieldManagerMock fieldManager = new FieldManagerMock(customFieldManager);
        final FieldScreenManagerMock fieldScreenManager = new FieldScreenManagerMock();
        final FieldScreenSchemeManagerMock fieldScreenSchemeManager = new FieldScreenSchemeManagerMock();

        fieldScreenManager.setDefaultFieldScreen(getDefaultFieldScreen(includeSomeNullCustomFields));

        final JiraServicesMock jiraServices = new JiraServicesMock();
        jiraServices.setCustomFieldManager(customFieldManager);
        jiraServices.setFieldManager(fieldManager);
        jiraServices.setFieldScreenManager(fieldScreenManager);
        jiraServices.setFieldScreenSchemeManager(fieldScreenSchemeManager);

        final List<IssueType> issueTypes = getBlackDuckIssueTypes();

        BlackDuckFieldScreenSchemeSetup fieldConfigSetup = new BlackDuckFieldScreenSchemeSetup(pluginErrorAccessor, jiraServices);
        fieldConfigSetup = Mockito.spy(fieldConfigSetup);

        mockCreationMethods(fieldConfigSetup);

        fieldConfigSetup.addBlackDuckFieldConfigurationToJira(issueTypes);

        assertEquals(NUM_FIELDS_TOTAL, customFieldManager.getCustomFieldObjects().size());
        for (final FieldScreen fieldScreen : fieldScreenManager.getUpdatedScreens()) {
            final FieldScreenMock fieldScreenMock = (FieldScreenMock) fieldScreen;
            assertTrue(fieldScreenMock.getAttemptedScreenStore());
        }
        assertTrue(fieldScreenManager.getUpdatedTabs().size() == 2);

        for (final FieldScreenTab tab : fieldScreenManager.getUpdatedTabs()) {
            final String screenName = tab.getFieldScreen().getName();
            if (screenName.equals(BlackDuckJiraConstants.BLACKDUCK_POLICY_SCREEN_NAME)) {
                assertEquals(expectedPolicyFields, tab.getFieldScreenLayoutItems().size());
            } else if (screenName.equals(BlackDuckJiraConstants.BLACKDUCK_POLICY_SCREEN_NAME)) {
                assertEquals(expectedVulnerabilityFields, tab.getFieldScreenLayoutItems().size());
            }
        }
        assertTrue(fieldScreenManager.getUpdatedScreens().size() == 2);
        for (final FieldScreenScheme fieldScreenScheme : fieldScreenSchemeManager.getUpdatedSchemes()) {
            final FieldScreenSchemeMock fieldScreenSchemeMock = (FieldScreenSchemeMock) fieldScreenScheme;
            assertTrue(fieldScreenSchemeMock.getAttemptedScreenSchemeStore());

            for (final FieldScreenSchemeItem currentSchemeItem : fieldScreenScheme.getFieldScreenSchemeItems()) {
                assertTrue(currentSchemeItem.getFieldScreen().getName()
                               .equals(BlackDuckJiraConstants.BLACKDUCK_POLICY_SCREEN_NAME)
                               || currentSchemeItem.getFieldScreen().getName()
                                      .equals(BlackDuckJiraConstants.BLACKDUCK_SECURITY_SCREEN_NAME));
            }
        }
        assertTrue(fieldScreenSchemeManager.getUpdatedSchemes().size() == 2);
        assertTrue(fieldScreenSchemeManager.getUpdatedSchemeItems().size() == 6);
    }

    @Test
    @Ignore
    // TODO This test is not easily maintainable and should be improved
    public void testAddBlackDuckFieldConfigurationToJiraWithUserChanges() throws Exception {
        final PluginSettingsMock settingsMock = new PluginSettingsMock();
        final PluginErrorAccessor pluginErrorAccessor = new PluginErrorAccessor(new JiraSettingsAccessor(settingsMock));

        final CustomFieldManagerMock customFieldManager = new CustomFieldManagerMock();
        final FieldManagerMock fieldManager = new FieldManagerMock(customFieldManager);
        final FieldScreenManagerMock fieldScreenManager = new FieldScreenManagerMock();
        final FieldScreenSchemeManagerMock fieldScreenSchemeManager = new FieldScreenSchemeManagerMock();
        final FieldScreen defaultScreen = getDefaultFieldScreen(false);
        fieldScreenManager.setDefaultFieldScreen(defaultScreen);

        final JiraServicesMock jiraServices = new JiraServicesMock();
        jiraServices.setCustomFieldManager(customFieldManager);
        jiraServices.setFieldManager(fieldManager);
        jiraServices.setFieldScreenManager(fieldScreenManager);
        jiraServices.setFieldScreenSchemeManager(fieldScreenSchemeManager);

        final List<IssueType> issueTypes = getBlackDuckIssueTypes();

        BlackDuckFieldScreenSchemeSetup fieldConfigSetup = new BlackDuckFieldScreenSchemeSetup(pluginErrorAccessor, jiraServices);
        fieldConfigSetup = Mockito.spy(fieldConfigSetup);

        mockCreationMethods(fieldConfigSetup);

        fieldConfigSetup.addBlackDuckFieldConfigurationToJira(issueTypes);
        assertEquals(NUM_FIELDS_TOTAL, customFieldManager.getCustomFieldObjects().size());
        for (final FieldScreen fieldScreen : fieldScreenManager.getUpdatedScreens()) {
            final FieldScreenMock fieldScreenMock = (FieldScreenMock) fieldScreen;
            assertTrue(fieldScreenMock.getAttemptedScreenStore());
        }
        assertTrue(fieldScreenManager.getUpdatedTabs().size() == 2);

        for (final FieldScreenTab tab : fieldScreenManager.getUpdatedTabs()) {
            final String screenName = tab.getFieldScreen().getName();
            if (screenName.equals(BlackDuckJiraConstants.BLACKDUCK_POLICY_SCREEN_NAME)) {
                assertEquals(NUM_FIELDS_POLICY + 4, tab.getFieldScreenLayoutItems().size());
            } else if (screenName.equals(BlackDuckJiraConstants.BLACKDUCK_SECURITY_SCREEN_NAME)) {
                assertEquals(NUM_FIELDS_TOTAL - 1, tab.getFieldScreenLayoutItems().size());
            }
        }
        assertTrue(fieldScreenManager.getUpdatedScreens().size() == 2);
        for (final FieldScreenScheme fieldScreenScheme : fieldScreenSchemeManager.getUpdatedSchemes()) {
            final FieldScreenSchemeMock fieldScreenSchemeMock = (FieldScreenSchemeMock) fieldScreenScheme;
            assertTrue(fieldScreenSchemeMock.getAttemptedScreenSchemeStore());

            for (final FieldScreenSchemeItem currentSchemeItem : fieldScreenScheme.getFieldScreenSchemeItems()) {

                assertTrue(currentSchemeItem.getFieldScreen().getName()
                               .equals(BlackDuckJiraConstants.BLACKDUCK_POLICY_SCREEN_NAME)
                               || currentSchemeItem.getFieldScreen().getName()
                                      .equals(BlackDuckJiraConstants.BLACKDUCK_SECURITY_SCREEN_NAME));
            }
        }
        assertTrue(fieldScreenSchemeManager.getUpdatedSchemes().size() == 2);
        assertTrue(fieldScreenSchemeManager.getUpdatedSchemeItems().size() == 6);
        assertNotNull(settingsMock);
        assertTrue(((String) settingsMock.get(BlackDuckJiraConstants.BLACKDUCK_JIRA_ERROR)).contains(POLICY_RULE_ERROR_MESSAGE));

        // User edits
        final FieldScreenScheme scheme = fieldScreenSchemeManager.getFieldScreenSchemes().iterator().next();
        final FieldScreenSchemeItem schemeItem = scheme.getFieldScreenSchemeItems().iterator().next();
        schemeItem.setFieldScreen(defaultScreen);

        customFieldManager.removeCustomField(customFieldManager.getCustomFields().get(0));

        fieldConfigSetup.addBlackDuckFieldConfigurationToJira(issueTypes);

        assertEquals(NUM_FIELDS_TOTAL, customFieldManager.getCustomFieldObjects().size());
        for (final FieldScreen fieldScreen : fieldScreenManager.getUpdatedScreens()) {
            final FieldScreenMock fieldScreenMock = (FieldScreenMock) fieldScreen;
            assertTrue(fieldScreenMock.getAttemptedScreenStore());
        }
        assertTrue(fieldScreenManager.getUpdatedTabs().size() == 2);

        for (final FieldScreenTab tab : fieldScreenManager.getUpdatedTabs()) {
            final String screenName = tab.getFieldScreen().getName();
            if (screenName.equals(BlackDuckJiraConstants.BLACKDUCK_POLICY_SCREEN_NAME)) {
                assertEquals(NUM_FIELDS_POLICY + 4, tab.getFieldScreenLayoutItems().size());
            } else if (screenName.equals(BlackDuckJiraConstants.BLACKDUCK_SECURITY_SCREEN_NAME)) {
                assertEquals(NUM_FIELDS_TOTAL - 1, tab.getFieldScreenLayoutItems().size());
            }
        }
        assertEquals(2, fieldScreenManager.getUpdatedScreens().size());
        for (final FieldScreenScheme fieldScreenScheme : fieldScreenSchemeManager.getUpdatedSchemes()) {
            final FieldScreenSchemeMock fieldScreenSchemeMock = (FieldScreenSchemeMock) fieldScreenScheme;
            assertTrue(fieldScreenSchemeMock.getAttemptedScreenSchemeStore());

            for (final FieldScreenSchemeItem currentSchemeItem : fieldScreenScheme.getFieldScreenSchemeItems()) {
                assertTrue(currentSchemeItem.getFieldScreen().getName()
                               .equals(BlackDuckJiraConstants.BLACKDUCK_POLICY_SCREEN_NAME)
                               || currentSchemeItem.getFieldScreen().getName()
                                      .equals(BlackDuckJiraConstants.BLACKDUCK_SECURITY_SCREEN_NAME));
            }
        }
        assertEquals(2, fieldScreenSchemeManager.getUpdatedSchemes().size());
        assertEquals(7, fieldScreenSchemeManager.getUpdatedSchemeItems().size());
        assertNotNull(settingsMock);
        assertTrue(((String) settingsMock.get(BlackDuckJiraConstants.BLACKDUCK_JIRA_ERROR)).contains(POLICY_RULE_ERROR_MESSAGE));
    }

    private void mockCreationMethods(final BlackDuckFieldScreenSchemeSetup fieldConfigSetup) {
        Mockito.when(fieldConfigSetup.createNewScreenImpl(Mockito.any(FieldScreenManager.class))).thenAnswer(x -> new FieldScreenMock());
        Mockito.when(fieldConfigSetup.createNewScreenSchemeImpl(Mockito.any(FieldScreenSchemeManager.class))).thenAnswer(x -> new FieldScreenSchemeMock());
        Mockito.when(fieldConfigSetup.createNewFieldScreenSchemeItemImpl(Mockito.any(FieldScreenSchemeManager.class), Mockito.any(FieldScreenManager.class))).thenAnswer(x -> new FieldScreenSchemeItemMock());
    }

    private FieldScreen getDefaultFieldScreen(final boolean includeSomeNullFields) {
        final FieldScreenMock fieldScreen = new FieldScreenMock();
        final FieldScreenTabMock defaultTab1 = new FieldScreenTabMock();
        defaultTab1.setFieldScreen(fieldScreen);
        defaultTab1.addFieldScreenLayoutItem("Default Field 1");
        if (includeSomeNullFields) {
            defaultTab1.addFieldScreenLayoutItem(null);
        } else {
            defaultTab1.addFieldScreenLayoutItem("Default Field 2");
        }
        defaultTab1.addFieldScreenLayoutItem("Default Field 3");
        final FieldScreenTabMock defaultTab2 = new FieldScreenTabMock();
        defaultTab2.setFieldScreen(fieldScreen);
        defaultTab2.addFieldScreenLayoutItem("Default Field 1");
        if (includeSomeNullFields) {
            defaultTab2.addFieldScreenLayoutItem(null);
        } else {
            defaultTab2.addFieldScreenLayoutItem("Default Field 2");
        }
        defaultTab2.addFieldScreenLayoutItem("Default Field 3");
        defaultTab2.addFieldScreenLayoutItem("Default Field 4");

        fieldScreen.addTab(defaultTab1);
        fieldScreen.addTab(defaultTab2);

        fieldScreen.setName("Default Screen");
        return fieldScreen;
    }

    private List<IssueType> getBlackDuckIssueTypes() {
        final List<IssueType> issueTypes = new ArrayList<>();
        addPolicyIssueType(issueTypes);
        addSecurityPolicyIssueType(issueTypes);
        addVulnIssueType(issueTypes);
        return issueTypes;
    }

    private void addVulnIssueType(final List<IssueType> issueTypes) {
        issueTypes.add(createMockIssueType(BlackDuckJiraConstants.BLACKDUCK_VULNERABILITY_ISSUE, BlackDuckJiraConstants.BLACKDUCK_VULNERABILITY_ISSUE));
    }

    private void addPolicyIssueType(final List<IssueType> issueTypes) {
        issueTypes.add(createMockIssueType(BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_ISSUE, BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_ISSUE));
    }

    private void addSecurityPolicyIssueType(final List<IssueType> issueTypes) {
        issueTypes.add(createMockIssueType(BlackDuckJiraConstants.BLACKDUCK_SECURITY_POLICY_VIOLATION_ISSUE, BlackDuckJiraConstants.BLACKDUCK_SECURITY_POLICY_VIOLATION_ISSUE));
    }

    private IssueTypeMock createMockIssueType(String name, String id) {
        final IssueTypeMock issueTypeMock = new IssueTypeMock();
        issueTypeMock.setName(name);
        issueTypeMock.setId(id);
        issueTypeMock.setValue(Mockito.mock(GenericValue.class));
        return issueTypeMock;
    }

    private void createCustomField(JiraServices jiraServices, String name, String description, List<IssueType> associatedIssueTypes) throws GenericEntityException {
        final CustomField customField = jiraServices.getCustomFieldManager().createCustomField(name, description, null, null, null, associatedIssueTypes);
        final CustomFieldMock customFieldMock = (CustomFieldMock) customField;
        customFieldMock.setAssociatedIssueTypes(associatedIssueTypes);

        FieldConfig fieldConfig = Mockito.mock(FieldConfig.class);

        Map<String, FieldConfig> configs = new HashMap<>();
        associatedIssueTypes.stream()
            .forEach(issueType -> {
                configs.put(issueType.getId(), fieldConfig);
            });

        FieldConfigSchemeMock fieldConfigSchemeMock = new FieldConfigSchemeMock();
        fieldConfigSchemeMock.setConfigs(configs);
        fieldConfigSchemeMock.setAssociatedIssueTypes(associatedIssueTypes);

        customFieldMock.setFieldConfigSchemes(Arrays.asList(fieldConfigSchemeMock));
    }
}
