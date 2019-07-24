/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2019 Synopsys, Inc.
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;
import com.blackducksoftware.integration.jira.dal.JiraSettingsAccessor;
import com.blackducksoftware.integration.jira.dal.PluginErrorAccessor;
import com.blackducksoftware.integration.jira.mocks.JiraServicesMock;
import com.blackducksoftware.integration.jira.mocks.PluginSettingsMock;
import com.blackducksoftware.integration.jira.mocks.field.EditableDefaultFieldLayoutMock;
import com.blackducksoftware.integration.jira.mocks.field.EditableFieldLayoutMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldLayoutItemMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldLayoutManagerMock;
import com.blackducksoftware.integration.jira.mocks.field.OrderableFieldMock;

public class BlackDuckFieldConfigurationSetupTest {

    @Test
    public void testAddBlackDuckFieldConfigurationToJiraFirstTimeCreate() {
        final PluginSettingsMock settingsMock = new PluginSettingsMock();
        final PluginErrorAccessor pluginErrorAccessor = new PluginErrorAccessor(new JiraSettingsAccessor(settingsMock));
        final EditableDefaultFieldLayoutMock defaultFieldLayout = new EditableDefaultFieldLayoutMock();
        addDefaultFieldLayoutItems(defaultFieldLayout);
        addDefaultFieldLayoutItem(defaultFieldLayout, "custom", true);
        final FieldLayoutManagerMock fieldLayoutManager = new FieldLayoutManagerMock();
        fieldLayoutManager.setEditableDefaultFieldLayout(defaultFieldLayout);
        final JiraServicesMock jiraServices = new JiraServicesMock();
        jiraServices.setFieldLayoutManager(fieldLayoutManager);

        BlackDuckFieldConfigurationSetup fieldConfigSetup = new BlackDuckFieldConfigurationSetup(pluginErrorAccessor, jiraServices);
        fieldConfigSetup = Mockito.spy(fieldConfigSetup);
        final EditableFieldLayoutMock fieldLayout = mockCreateEditableFieldLayout(fieldConfigSetup);

        fieldConfigSetup.addBlackDuckFieldConfigurationToJira();

        assertTrue(fieldLayoutManager.getAttemptedToPersistFieldLayout());
        assertEquals(1, fieldLayout.getFieldsToMakeOptional().size());
        assertTrue(fieldLayout.getFieldsToMakeOptional().get(0).getOrderableField().getName().equals("custom"));
        assertNull(settingsMock.get(BlackDuckJiraConstants.BLACKDUCK_JIRA_ERROR));
    }

    @Test
    public void testAddBlackDuckFieldConfigurationToJiraFirstTimeCreateNoCustomRequiredFields() {
        final PluginSettingsMock settingsMock = new PluginSettingsMock();
        final PluginErrorAccessor pluginErrorAccessor = new PluginErrorAccessor(new JiraSettingsAccessor(settingsMock));
        final EditableDefaultFieldLayoutMock defaultFieldLayout = new EditableDefaultFieldLayoutMock();
        addDefaultFieldLayoutItems(defaultFieldLayout);
        final FieldLayoutManagerMock fieldLayoutManager = new FieldLayoutManagerMock();
        fieldLayoutManager.setEditableDefaultFieldLayout(defaultFieldLayout);
        final JiraServicesMock jiraServices = new JiraServicesMock();
        jiraServices.setFieldLayoutManager(fieldLayoutManager);

        BlackDuckFieldConfigurationSetup fieldConfigSetup = new BlackDuckFieldConfigurationSetup(pluginErrorAccessor, jiraServices);
        fieldConfigSetup = Mockito.spy(fieldConfigSetup);
        final EditableFieldLayoutMock fieldLayout = mockCreateEditableFieldLayout(fieldConfigSetup);

        fieldConfigSetup.addBlackDuckFieldConfigurationToJira();

        assertTrue(fieldLayoutManager.getAttemptedToPersistFieldLayout());
        assertEquals(0, fieldLayout.getFieldsToMakeOptional().size());
        assertNull(settingsMock.get(BlackDuckJiraConstants.BLACKDUCK_JIRA_ERROR));
    }

    @Test
    public void testAddBlackDuckFieldConfigurationToJiraNotFound() {
        final PluginSettingsMock settingsMock = new PluginSettingsMock();
        final PluginErrorAccessor pluginErrorAccessor = new PluginErrorAccessor(new JiraSettingsAccessor(settingsMock));
        final EditableDefaultFieldLayoutMock defaultFieldLayout = new EditableDefaultFieldLayoutMock();
        addDefaultFieldLayoutItems(defaultFieldLayout);
        addDefaultFieldLayoutItem(defaultFieldLayout, "custom", true);

        final EditableFieldLayoutMock otherFieldLayout = new EditableFieldLayoutMock();
        otherFieldLayout.setName("NotBlackDuckFieldConfiguration");
        addFieldLayoutItems(otherFieldLayout);
        addFieldLayoutItem(otherFieldLayout, "custom", true);
        final FieldLayoutManagerMock fieldLayoutManager = new FieldLayoutManagerMock();
        fieldLayoutManager.addEditableFieldLayout(otherFieldLayout);

        fieldLayoutManager.setEditableDefaultFieldLayout(defaultFieldLayout);
        final JiraServicesMock jiraServices = new JiraServicesMock();
        jiraServices.setFieldLayoutManager(fieldLayoutManager);

        BlackDuckFieldConfigurationSetup fieldConfigSetup = new BlackDuckFieldConfigurationSetup(pluginErrorAccessor, jiraServices);
        fieldConfigSetup = Mockito.spy(fieldConfigSetup);
        final EditableFieldLayoutMock fieldLayout = mockCreateEditableFieldLayout(fieldConfigSetup);

        fieldConfigSetup.addBlackDuckFieldConfigurationToJira();

        assertTrue(fieldLayoutManager.getAttemptedToPersistFieldLayout());
        assertTrue(fieldLayout.getFieldsToMakeOptional().size() == 1);
        assertTrue(fieldLayout.getFieldsToMakeOptional().get(0).getOrderableField().getName().equals("custom"));
        assertNull(settingsMock.get(BlackDuckJiraConstants.BLACKDUCK_JIRA_ERROR));
    }

    @Test
    public void testAddBlackDuckFieldConfigurationToJiraAlreadyAdded() {
        final PluginSettingsMock settingsMock = new PluginSettingsMock();
        final PluginErrorAccessor pluginErrorAccessor = new PluginErrorAccessor(new JiraSettingsAccessor(settingsMock));
        final EditableFieldLayoutMock fieldLayout = new EditableFieldLayoutMock();
        fieldLayout.setName(BlackDuckJiraConstants.BLACKDUCK_FIELD_CONFIGURATION);
        addFieldLayoutItems(fieldLayout);
        addFieldLayoutItem(fieldLayout, "custom", true);
        final FieldLayoutManagerMock fieldLayoutManager = new FieldLayoutManagerMock();
        fieldLayoutManager.addEditableFieldLayout(fieldLayout);
        final JiraServicesMock jiraServices = new JiraServicesMock();
        jiraServices.setFieldLayoutManager(fieldLayoutManager);

        final BlackDuckFieldConfigurationSetup fieldConfigSetup = new BlackDuckFieldConfigurationSetup(pluginErrorAccessor, jiraServices);

        fieldConfigSetup.addBlackDuckFieldConfigurationToJira();

        assertTrue(fieldLayoutManager.getAttemptedToPersistFieldLayout());
        assertTrue(fieldLayout.getFieldsToMakeOptional().size() == 1);
        assertTrue(fieldLayout.getFieldsToMakeOptional().get(0).getOrderableField().getName().equals("custom"));
        assertNull(settingsMock.get(BlackDuckJiraConstants.BLACKDUCK_JIRA_ERROR));
    }

    @Test
    public void testAddBlackDuckFieldConfigurationToJiraAlreadyAddedNoCustomRequiredFields() {
        final PluginSettingsMock settingsMock = new PluginSettingsMock();
        final PluginErrorAccessor pluginErrorAccessor = new PluginErrorAccessor(new JiraSettingsAccessor(settingsMock));
        final EditableFieldLayoutMock fieldLayout = new EditableFieldLayoutMock();
        fieldLayout.setName(BlackDuckJiraConstants.BLACKDUCK_FIELD_CONFIGURATION);
        addFieldLayoutItems(fieldLayout);
        final FieldLayoutManagerMock fieldLayoutManager = new FieldLayoutManagerMock();
        fieldLayoutManager.addEditableFieldLayout(fieldLayout);
        final JiraServicesMock jiraServices = new JiraServicesMock();
        jiraServices.setFieldLayoutManager(fieldLayoutManager);

        final BlackDuckFieldConfigurationSetup fieldConfigSetup = new BlackDuckFieldConfigurationSetup(pluginErrorAccessor, jiraServices);

        fieldConfigSetup.addBlackDuckFieldConfigurationToJira();

        assertTrue(!fieldLayoutManager.getAttemptedToPersistFieldLayout());
        assertTrue(fieldLayout.getFieldsToMakeOptional().size() == 0);
        assertNull(settingsMock.get(BlackDuckJiraConstants.BLACKDUCK_JIRA_ERROR));
    }

    private EditableFieldLayoutMock mockCreateEditableFieldLayout(
        final BlackDuckFieldConfigurationSetup fieldConfigSetupSpy) {
        final EditableFieldLayoutMock fieldLayout = new EditableFieldLayoutMock();

        Mockito.when(fieldConfigSetupSpy.createEditableFieldLayout(Mockito.anyList()))
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
