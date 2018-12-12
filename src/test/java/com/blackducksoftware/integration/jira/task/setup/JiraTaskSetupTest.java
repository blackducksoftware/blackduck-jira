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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutSchemeEntity;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutSchemeEntityImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntity;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.blackducksoftware.integration.jira.JiraVersionCheck;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;
import com.blackducksoftware.integration.jira.common.JiraUserContext;
import com.blackducksoftware.integration.jira.common.TicketInfoFromSetup;
import com.blackducksoftware.integration.jira.common.exception.ConfigurationException;
import com.blackducksoftware.integration.jira.common.model.BlackDuckProject;
import com.blackducksoftware.integration.jira.common.model.BlackDuckProjectMapping;
import com.blackducksoftware.integration.jira.common.model.JiraProject;
import com.blackducksoftware.integration.jira.common.model.PluginField;
import com.blackducksoftware.integration.jira.config.JiraServices;
import com.blackducksoftware.integration.jira.config.JiraSettingsService;
import com.blackducksoftware.integration.jira.config.model.BlackDuckJiraConfigSerializable;
import com.blackducksoftware.integration.jira.mocks.ApplicationUserMock;
import com.blackducksoftware.integration.jira.mocks.AvatarManagerMock;
import com.blackducksoftware.integration.jira.mocks.ConstantsManagerMock;
import com.blackducksoftware.integration.jira.mocks.GroupPickerSearchServiceMock;
import com.blackducksoftware.integration.jira.mocks.JiraServicesMock;
import com.blackducksoftware.integration.jira.mocks.MockBuildUtilsInfoImpl;
import com.blackducksoftware.integration.jira.mocks.PluginSettingsMock;
import com.blackducksoftware.integration.jira.mocks.ProjectManagerMock;
import com.blackducksoftware.integration.jira.mocks.SearchServiceMock;
import com.blackducksoftware.integration.jira.mocks.UserManagerMock;
import com.blackducksoftware.integration.jira.mocks.UserUtilMock;
import com.blackducksoftware.integration.jira.mocks.field.CustomFieldManagerMock;
import com.blackducksoftware.integration.jira.mocks.field.EditableFieldLayoutMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldConfigSchemeMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldConfigurationSchemeMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldLayoutItemMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldLayoutManagerMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldLayoutSchemeMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldManagerMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldScreenManagerMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldScreenMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldScreenSchemeItemMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldScreenSchemeManagerMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldScreenSchemeMock;
import com.blackducksoftware.integration.jira.mocks.field.FieldScreenTabMock;
import com.blackducksoftware.integration.jira.mocks.field.OrderableFieldMock;
import com.blackducksoftware.integration.jira.mocks.issue.IssueTypeMock;
import com.blackducksoftware.integration.jira.mocks.issue.IssueTypeSchemeManagerMock;
import com.blackducksoftware.integration.jira.mocks.issue.IssueTypeScreenSchemeManagerMock;
import com.blackducksoftware.integration.jira.mocks.issue.IssueTypeScreenSchemeMock;
import com.blackducksoftware.integration.jira.mocks.workflow.AssignableWorkflowSchemeBuilderMock;
import com.blackducksoftware.integration.jira.mocks.workflow.AssignableWorkflowSchemeMock;
import com.blackducksoftware.integration.jira.mocks.workflow.WorkflowManagerMock;
import com.blackducksoftware.integration.jira.mocks.workflow.WorkflowSchemeManagerMock;
import com.blackducksoftware.integration.jira.task.JiraTaskTimed;

public class JiraTaskSetupTest {
    private static final int NUM_FIELDS = PluginField.values().length;

    private final static String BLACKDUCK_JIRA_GROUP = "hub-jira";
    private static final String BLACKDUCK_PROJECT_NAME = "Test Black Duck Project";
    private static final String JIRA_PROJECT_NAME = ProjectManagerMock.JIRA_PROJECT_PREFIX;
    private static final long JIRA_PROJECT_ID = ProjectManagerMock.JIRA_PROJECT_ID_BASE;
    private static final String JIRA_USER = "Jira User";

    @Test
    public void testServerSetupIssueTypesAlreadyCreated() throws Exception {
        final JiraEnvironment jiraEnv = generateJiraMocks(true);
        final ApplicationUser jiraUser = Mockito.mock(ApplicationUser.class);
        Mockito.when(jiraUser.getName()).thenReturn(JIRA_USER);
        final JiraUserContext jiraContext = new JiraUserContext(jiraUser, jiraUser);

        final JiraTaskTimed task = jiraEnv.getJiraTask();
        final TicketInfoFromSetup x = new TicketInfoFromSetup();
        task.jiraSetup(jiraEnv.getJiraServices(), jiraEnv.getJiraSettingsService(), jiraEnv.getMappingJson(), x, jiraContext);

        assertTrue(jiraEnv.getWorkflowManagerMock().getAttemptedCreateWorkflow());
        assertTrue(jiraEnv.getWorkflowSchemeManagerMock().getAttemptedWorkflowUpdate());
        assertEquals(0, jiraEnv.getConstantsManagerMock().getIssueTypesCreatedCount());

        assertEquals(NUM_FIELDS, jiraEnv.getCustomFieldManagerMock().getCustomFieldObjects().size());
        for (final FieldScreen fieldScreen : jiraEnv.getFieldScreenManagerMock().getUpdatedScreens()) {
            final FieldScreenMock fieldScreenMock = (FieldScreenMock) fieldScreen;
            assertTrue(fieldScreenMock.getAttemptedScreenStore());
        }
        assertTrue(jiraEnv.getFieldScreenManagerMock().getUpdatedTabs().size() == 2);

        for (final FieldScreenTab tab : jiraEnv.getFieldScreenManagerMock().getUpdatedTabs()) {
            final String screenName = tab.getFieldScreen().getName();
            if (screenName.equals(BlackDuckJiraConstants.BLACKDUCK_POLICY_SCREEN_NAME)) {
                assertEquals(NUM_FIELDS + 2, tab.getFieldScreenLayoutItems().size());
            } else if (screenName.equals(BlackDuckJiraConstants.BLACKDUCK_SECURITY_SCREEN_NAME)) {
                assertEquals(NUM_FIELDS - 1, tab.getFieldScreenLayoutItems().size());
            }
        }
        assertTrue(jiraEnv.getFieldScreenManagerMock().getUpdatedScreens().size() == 2);
        for (final FieldScreenScheme fieldScreenScheme : jiraEnv.getFieldScreenSchemeManagerMock()
                                                                 .getUpdatedSchemes()) {
            final FieldScreenSchemeMock fieldScreenSchemeMock = (FieldScreenSchemeMock) fieldScreenScheme;
            assertTrue(fieldScreenSchemeMock.getAttemptedScreenSchemeStore());

            for (final FieldScreenSchemeItem currentSchemeItem : fieldScreenScheme.getFieldScreenSchemeItems()) {
                assertTrue(currentSchemeItem.getFieldScreen().getName()
                                   .equals(BlackDuckJiraConstants.BLACKDUCK_POLICY_SCREEN_NAME)
                                   || currentSchemeItem.getFieldScreen().getName()
                                              .equals(BlackDuckJiraConstants.BLACKDUCK_SECURITY_SCREEN_NAME));
            }
        }
        assertEquals(2, jiraEnv.getFieldScreenSchemeManagerMock().getUpdatedSchemes().size());
        assertEquals(6, jiraEnv.getFieldScreenSchemeManagerMock().getUpdatedSchemeItems().size());
        assertNotNull(jiraEnv.getPluginSettingsMock());
        assertTrue(((String) jiraEnv.getPluginSettingsMock().get(BlackDuckJiraConstants.BLACKDUCK_JIRA_ERROR)).contains("The custom field " + BlackDuckJiraConstants.BLACKDUCK_CUSTOM_FIELD_POLICY_RULE + " has no IssueType associations"));

        final List<Avatar> avatarTemplates = jiraEnv.getAvatarManagerMock().getAvatarTemplatesUsedToCreateAvatars();
        assertEquals(0, avatarTemplates.size());

        assertEquals(2, jiraEnv.getFieldLayoutSchemeMock().getStoreCount());
    }

    @Test
    public void testServerSetupIssueTypesNotAlreadyCreated() throws Exception {
        final JiraEnvironment jiraEnv = generateJiraMocks(false);

        final ApplicationUser jiraUser = Mockito.mock(ApplicationUser.class);
        Mockito.when(jiraUser.getName()).thenReturn(JIRA_USER);
        final JiraUserContext jiraContext = new JiraUserContext(jiraUser, jiraUser);

        final JiraTaskTimed task = jiraEnv.getJiraTask();
        final TicketInfoFromSetup ticketInfoFromSetup = new TicketInfoFromSetup();
        task.jiraSetup(jiraEnv.getJiraServices(), jiraEnv.getJiraSettingsService(), jiraEnv.getMappingJson(), ticketInfoFromSetup, jiraContext);

        assertTrue(jiraEnv.getWorkflowManagerMock().getAttemptedCreateWorkflow());
        assertTrue(jiraEnv.getWorkflowSchemeManagerMock().getAttemptedWorkflowUpdate());
        assertEquals(2, jiraEnv.getConstantsManagerMock().getIssueTypesCreatedCount());
        assertEquals(NUM_FIELDS, jiraEnv.getCustomFieldManagerMock().getCustomFieldObjects().size());
        for (final FieldScreen fieldScreen : jiraEnv.getFieldScreenManagerMock().getUpdatedScreens()) {
            final FieldScreenMock fieldScreenMock = (FieldScreenMock) fieldScreen;
            assertTrue(fieldScreenMock.getAttemptedScreenStore());
        }
        assertEquals(2, jiraEnv.getFieldScreenManagerMock().getUpdatedTabs().size());

        for (final FieldScreenTab tab : jiraEnv.getFieldScreenManagerMock().getUpdatedTabs()) {
            final String screenName = tab.getFieldScreen().getName();
            if (screenName.equals(BlackDuckJiraConstants.BLACKDUCK_POLICY_SCREEN_NAME)) {
                assertEquals(NUM_FIELDS + 2, tab.getFieldScreenLayoutItems().size());
            } else if (screenName.equals(BlackDuckJiraConstants.BLACKDUCK_SECURITY_SCREEN_NAME)) {
                assertEquals(NUM_FIELDS - 1, tab.getFieldScreenLayoutItems().size());
            }
        }
        assertEquals(2, jiraEnv.getFieldScreenManagerMock().getUpdatedScreens().size());
        for (final FieldScreenScheme fieldScreenScheme : jiraEnv.getFieldScreenSchemeManagerMock()
                                                                 .getUpdatedSchemes()) {
            final FieldScreenSchemeMock fieldScreenSchemeMock = (FieldScreenSchemeMock) fieldScreenScheme;
            assertTrue(fieldScreenSchemeMock.getAttemptedScreenSchemeStore());

            for (final FieldScreenSchemeItem currentSchemeItem : fieldScreenScheme.getFieldScreenSchemeItems()) {
                assertTrue(
                        currentSchemeItem.getFieldScreen().getName().equals(BlackDuckJiraConstants.BLACKDUCK_POLICY_SCREEN_NAME) || currentSchemeItem.getFieldScreen().getName().equals(BlackDuckJiraConstants.BLACKDUCK_SECURITY_SCREEN_NAME));
            }
        }
        assertEquals(2, jiraEnv.getFieldScreenSchemeManagerMock().getUpdatedSchemes().size());
        assertEquals(6, jiraEnv.getFieldScreenSchemeManagerMock().getUpdatedSchemeItems().size());
        assertNotNull(jiraEnv.getPluginSettingsMock());
        assertTrue(((String) jiraEnv.getPluginSettingsMock().get(BlackDuckJiraConstants.BLACKDUCK_JIRA_ERROR)).contains("The custom field " + BlackDuckJiraConstants.BLACKDUCK_CUSTOM_FIELD_POLICY_RULE + " has no IssueType associations"));

        final List<Avatar> avatarTemplatesUsed = jiraEnv.getAvatarManagerMock().getAvatarTemplatesUsedToCreateAvatars();
        assertEquals(2, avatarTemplatesUsed.size());
        assertEquals(jiraEnv.getAvatarTemplate(), avatarTemplatesUsed.get(0));

        final List<IssueTypeScreenSchemeEntity> addedIssueTypeScreenSchemeEntities = jiraEnv
                                                                                             .getIssueTypeScreenSchemeMock().getAddedEntities();
        assertEquals(2, addedIssueTypeScreenSchemeEntities.size());
        for (final IssueTypeScreenSchemeEntity addedIssueTypeScreenSchemeEntity : addedIssueTypeScreenSchemeEntities) {
            assertTrue(addedIssueTypeScreenSchemeEntity.getIssueTypeId().startsWith("mockIssueTypeId"));
        }

        assertEquals(2, jiraEnv.getFieldLayoutSchemeMock().getStoreCount());
        final Collection<FieldLayoutSchemeEntity> entitiesAdded = jiraEnv.getFieldLayoutSchemeMock().getEntitiesAdded();
        assertEquals(4, entitiesAdded.size());
        for (final FieldLayoutSchemeEntity entityAdded : entitiesAdded) {
            assertTrue(entityAdded.getIssueTypeId().startsWith("mockIssueTypeId"));
            assertEquals("Field Layout Scheme", entityAdded.getFieldLayoutScheme().getName());
        }

    }

    private JiraEnvironment generateJiraMocks(final boolean bdIssueTypesAlreadyAdded) throws ConfigurationException {
        final GroupPickerSearchServiceMock groupPickerSearchService = getGroupPickerSearchServiceMock(false);
        final WorkflowManagerMock workflowManager = getWorkflowManagerMock();
        final WorkflowSchemeManagerMock workflowSchemeManager = getWorkflowSchemeManagerMock(false);
        final UserManagerMock userManager = getUserManagerMockManagerMock();
        final ProjectManagerMock projectManager = getProjectManagerMock(true);
        final AvatarManagerMock avatarManager = getAvatarManagerMock();
        final ConstantsManagerMock constantsManager = getConstantsManagerMock();
        final IssueTypeSchemeManagerMock issueTypeSchemeManager = getIssueTypeSchemeManagerMock(constantsManager);
        final FieldConfigSchemeMock fieldConfigScheme = new FieldConfigSchemeMock();
        issueTypeSchemeManager.setConfigScheme(fieldConfigScheme);
        final FieldLayoutManagerMock fieldLayoutManager = getFieldLayoutManagerMock();
        final IssueTypeScreenSchemeManagerMock issueTypeScreenSchemeManager = new IssueTypeScreenSchemeManagerMock();
        final IssueTypeScreenSchemeMock issueTypeScreenScheme = new IssueTypeScreenSchemeMock();
        issueTypeScreenSchemeManager.setIssueTypeScreenScheme(issueTypeScreenScheme);
        final FieldLayoutSchemeMock fieldLayoutScheme = new FieldLayoutSchemeMock();
        fieldLayoutScheme.setName("Field Layout Scheme");

        final Collection<FieldLayoutSchemeEntity> fieldLayoutSchemeEntities = new ArrayList<>();
        FieldLayoutSchemeEntity issueTypeToFieldConfiguration = new FieldLayoutSchemeEntityImpl(fieldLayoutManager, null, constantsManager);
        issueTypeToFieldConfiguration.setFieldLayoutScheme(fieldLayoutScheme);
        issueTypeToFieldConfiguration.setFieldLayoutId(1L);
        issueTypeToFieldConfiguration.setIssueTypeId("mockIssueTypeId2");
        fieldLayoutSchemeEntities.add(issueTypeToFieldConfiguration);

        issueTypeToFieldConfiguration = new FieldLayoutSchemeEntityImpl(fieldLayoutManager, null, constantsManager);
        issueTypeToFieldConfiguration.setFieldLayoutScheme(fieldLayoutScheme);
        issueTypeToFieldConfiguration.setFieldLayoutId(2L);
        issueTypeToFieldConfiguration.setIssueTypeId("mockIssueTypeId3");
        fieldLayoutSchemeEntities.add(issueTypeToFieldConfiguration);

        fieldLayoutScheme.setEntities(fieldLayoutSchemeEntities);

        fieldLayoutManager.setFieldLayoutScheme(fieldLayoutScheme);

        fieldLayoutManager.setCreatedFieldLayoutSchemeEntities(fieldLayoutSchemeEntities);

        final FieldConfigurationSchemeMock projectFieldConfigScheme = new FieldConfigurationSchemeMock();
        projectFieldConfigScheme.setName("Project Field Config Scheme");
        projectFieldConfigScheme.setId(356l);
        fieldLayoutManager.setProjectFieldConfigScheme(projectFieldConfigScheme);

        final EditableFieldLayoutMock fieldLayout = new EditableFieldLayoutMock();
        fieldLayout.setName(BlackDuckJiraConstants.BLACKDUCK_FIELD_CONFIGURATION);
        fieldLayout.setDescription("mock");
        final List<FieldLayoutItem> fields = new ArrayList<>();
        final FieldLayoutItemMock field = new FieldLayoutItemMock();
        field.setIsRequired(false);
        final OrderableFieldMock orderableField = new OrderableFieldMock();
        orderableField.setId("1");
        orderableField.setName(BlackDuckJiraConstants.BLACKDUCK_CUSTOM_FIELD_POLICY_RULE);
        field.setOrderableField(orderableField);
        fields.add(field);
        fieldLayout.setFieldLayoutItems(fields);
        fieldLayoutManager.addEditableFieldLayout(fieldLayout);

        final Collection<IssueType> issueTypes = getIssueTypes(bdIssueTypesAlreadyAdded);
        issueTypeSchemeManager.setIssueTypes(issueTypes);

        final UserUtil userUtil = getUserUtil(true);

        final CustomFieldManagerMock customFieldManager = new CustomFieldManagerMock();
        final FieldManagerMock fieldManager = new FieldManagerMock(customFieldManager);
        final FieldScreenManagerMock fieldScreenManager = new FieldScreenManagerMock();
        final FieldScreenSchemeManagerMock fieldScreenSchemeManager = new FieldScreenSchemeManagerMock();
        final SearchService searchService = new SearchServiceMock();

        fieldScreenManager.setDefaultFieldScreen(getDefaultFieldScreen());

        final PluginSettingsMock settingsMock = new PluginSettingsMock();
        final JiraSettingsService settingService = new JiraSettingsService(settingsMock);
        final JiraServices jiraServices = getJiraServices(workflowManager, workflowSchemeManager,
                userManager, projectManager, avatarManager, constantsManager, issueTypeSchemeManager,
                fieldLayoutManager, issueTypeScreenSchemeManager, issueTypes, userUtil, customFieldManager,
                fieldManager, fieldScreenManager, fieldScreenSchemeManager, searchService);

        BlackDuckFieldScreenSchemeSetup fieldScreenSchemeSetup = new BlackDuckFieldScreenSchemeSetup(settingService, jiraServices);
        fieldScreenSchemeSetup = Mockito.spy(fieldScreenSchemeSetup);
        JiraTaskTimed jiraTask = new JiraTaskTimed(settingsMock, jiraServices, 1);
        jiraTask = Mockito.spy(jiraTask);

        mockCreationMethods(jiraTask, fieldScreenSchemeSetup);

        final String mappingJson = getProjectMappingJson(true, JIRA_PROJECT_NAME, JIRA_PROJECT_ID);

        final Avatar avatarTemplate = Mockito.mock(Avatar.class);
        Mockito.when(avatarTemplate.getId()).thenReturn(123L);
        Mockito.when(avatarTemplate.getContentType()).thenReturn("image/png");
        Mockito.when(avatarTemplate.getFileName()).thenReturn(BlackDuckJiraConstants.BLACKDUCK_AVATAR_IMAGE_FILENAME_POLICY);
        Mockito.when(avatarTemplate.getOwner()).thenReturn("avatarOwner");

        Mockito.when(
                jiraServices.createIssueTypeAvatarTemplate(BlackDuckJiraConstants.BLACKDUCK_AVATAR_IMAGE_FILENAME_POLICY,
                        "image/png", "Jira User"))
                .thenReturn(avatarTemplate);

        final JiraEnvironment jiraMocks = new JiraEnvironment().setAvatarManagerMock(avatarManager)
                                                  .setConstantsManagerMock(constantsManager).setCustomFieldManagerMock(customFieldManager)
                                                  .setEditableFieldLayoutMock(fieldLayout).setFieldConfigSchemeMock(fieldConfigScheme)
                                                  .setFieldConfigurationSchemeMock(projectFieldConfigScheme).setFieldLayoutManagerMock(fieldLayoutManager)
                                                  .setFieldLayoutSchemeMock(fieldLayoutScheme).setFieldManagerMock(fieldManager)
                                                  .setFieldScreenManagerMock(fieldScreenManager).setFieldScreenSchemeManagerMock(fieldScreenSchemeManager)
                                                  .setGroupPickerSearchServiceMock(groupPickerSearchService)
                                                  .setBlackDuckFieldScreenSchemeSetup(fieldScreenSchemeSetup)
                                                  .setIssueTypes(issueTypes).setIssueTypes(issueTypes)
                                                  .setIssueTypeSchemeManagerMock(issueTypeSchemeManager)
                                                  .setIssueTypeScreenSchemeManagerMock(issueTypeScreenSchemeManager)
                                                  .setIssueTypeScreenSchemeMock(issueTypeScreenScheme).setJiraServices(jiraServices)
                                                  .setJiraSettingsService(settingService).setJiraTask(jiraTask).setMappingJson(mappingJson)
                                                  .setOrderableFieldMock(orderableField).setPluginSettingsMock(settingsMock)
                                                  .setProjectManagerMock(projectManager).setUserManagerMock(userManager).setUserUtil(userUtil)
                                                  .setWorkflowManagerMock(workflowManager).setWorkflowSchemeManagerMock(workflowSchemeManager)
                                                  .setAvatarTemplate(avatarTemplate);

        return jiraMocks;
    }

    private FieldScreen getDefaultFieldScreen() {
        final FieldScreenMock fieldScreen = new FieldScreenMock();
        final FieldScreenTabMock defaultTab1 = new FieldScreenTabMock();
        defaultTab1.setFieldScreen(fieldScreen);
        defaultTab1.addFieldScreenLayoutItem("Default Field 1");
        defaultTab1.addFieldScreenLayoutItem("Default Field 2");
        defaultTab1.addFieldScreenLayoutItem("Default Field 3");
        final FieldScreenTabMock defaultTab2 = new FieldScreenTabMock();
        defaultTab2.setFieldScreen(fieldScreen);
        defaultTab2.addFieldScreenLayoutItem("Default Field 1");
        defaultTab2.addFieldScreenLayoutItem("Default Field 2");
        defaultTab2.addFieldScreenLayoutItem("Default Field 3");
        defaultTab2.addFieldScreenLayoutItem("Default Field 4");

        fieldScreen.addTab(defaultTab1);
        fieldScreen.addTab(defaultTab2);

        fieldScreen.setName("Default Screen");
        return fieldScreen;
    }

    private void mockCreationMethods(final JiraTaskTimed jiraTask, final BlackDuckFieldScreenSchemeSetup fieldConfigSetup)
            throws ConfigurationException {
        final MockBuildUtilsInfoImpl buildInfoUtil = new MockBuildUtilsInfoImpl();
        buildInfoUtil.setVersion("7.3.0");
        final int[] versionNumbers = { 7, 3, 0 };
        buildInfoUtil.setVersionNumbers(versionNumbers);
        final JiraVersionCheck jiraVersionCheck = new JiraVersionCheck(buildInfoUtil);
        Mockito.when(jiraTask.getJiraVersionCheck()).thenReturn(jiraVersionCheck);

        Mockito.when(fieldConfigSetup.createNewScreenImpl(Mockito.any(FieldScreenManager.class)))
                .thenAnswer(new Answer<FieldScreen>() {
                    @Override
                    public FieldScreen answer(final InvocationOnMock invocation) throws Throwable {
                        return new FieldScreenMock();
                    }
                });

        Mockito.when(fieldConfigSetup.createNewScreenSchemeImpl(Mockito.any(FieldScreenSchemeManager.class)))
                .thenAnswer(new Answer<FieldScreenScheme>() {
                    @Override
                    public FieldScreenScheme answer(final InvocationOnMock invocation) throws Throwable {
                        return new FieldScreenSchemeMock();
                    }
                });

        Mockito.when(fieldConfigSetup.createNewFieldScreenSchemeItemImpl(Mockito.any(FieldScreenSchemeManager.class),
                Mockito.any(FieldScreenManager.class))).thenAnswer(new Answer<FieldScreenSchemeItem>() {
            @Override
            public FieldScreenSchemeItem answer(final InvocationOnMock invocation) throws Throwable {
                return new FieldScreenSchemeItemMock();
            }
        });

        Mockito.doReturn(fieldConfigSetup).when(jiraTask).getBlackDuckFieldScreenSchemeSetup(
                Mockito.any(JiraSettingsService.class), Mockito.any(JiraServices.class));
    }

    private FieldLayoutManagerMock getFieldLayoutManagerMock() {
        return new FieldLayoutManagerMock();
    }

    private IssueTypeSchemeManagerMock getIssueTypeSchemeManagerMock(final ConstantsManagerMock constantsManager) {
        return new IssueTypeSchemeManagerMock(constantsManager);
    }

    private ConstantsManagerMock getConstantsManagerMock() {
        return new ConstantsManagerMock();
    }

    private AvatarManagerMock getAvatarManagerMock() {
        return new AvatarManagerMock();
    }

    private UserUtil getUserUtil(final boolean hasSystemAdmin) {
        final UserUtilMock userUtil = new UserUtilMock();
        if (hasSystemAdmin) {
            final ApplicationUserMock user = new ApplicationUserMock();
            user.setName(JIRA_USER);
            userUtil.setUser(user);
        }
        return userUtil;
    }

    private WorkflowManagerMock getWorkflowManagerMock() {
        final WorkflowManagerMock workflowManagerMock = new WorkflowManagerMock();

        return workflowManagerMock;
    }

    private WorkflowSchemeManagerMock getWorkflowSchemeManagerMock(final boolean workflowMappedToOurIssueTypes) {
        final WorkflowSchemeManagerMock workflowSchemeManagerMock = new WorkflowSchemeManagerMock();

        final AssignableWorkflowSchemeMock blackDuckWorkflow = new AssignableWorkflowSchemeMock();
        blackDuckWorkflow.setName(BlackDuckJiraConstants.BLACKDUCK_JIRA_WORKFLOW);
        if (workflowMappedToOurIssueTypes) {
            blackDuckWorkflow.addMappingIssueToWorkflow(BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_ISSUE,
                    BlackDuckJiraConstants.BLACKDUCK_JIRA_WORKFLOW);
            blackDuckWorkflow.addMappingIssueToWorkflow(BlackDuckJiraConstants.BLACKDUCK_VULNERABILITY_ISSUE,
                    BlackDuckJiraConstants.BLACKDUCK_JIRA_WORKFLOW);
        } else {
            blackDuckWorkflow.addMappingIssueToWorkflow(BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_ISSUE, "Fake Workflow");
            blackDuckWorkflow.addMappingIssueToWorkflow(BlackDuckJiraConstants.BLACKDUCK_VULNERABILITY_ISSUE, "Fake Workflow");
        }
        final AssignableWorkflowSchemeBuilderMock builder = new AssignableWorkflowSchemeBuilderMock();
        builder.setWorkflowScheme(blackDuckWorkflow);

        blackDuckWorkflow.setBuilder(builder);

        workflowSchemeManagerMock.setAssignableWorkflowScheme(blackDuckWorkflow);

        return workflowSchemeManagerMock;
    }

    private UserManagerMock getUserManagerMockManagerMock() {
        final UserManagerMock userManager = new UserManagerMock();
        final ApplicationUserMock applicationUser = new ApplicationUserMock();
        applicationUser.setName(JIRA_USER);
        userManager.setMockApplicationUser(applicationUser);
        return userManager;
    }

    private ProjectManagerMock getProjectManagerMock(final boolean hasJiraProjects) {
        final ProjectManagerMock projectManagerMock = new ProjectManagerMock();
        if (hasJiraProjects) {
            projectManagerMock.setProjectObjects(projectManagerMock.getTestProjectObjectsWithTaskIssueType());
        }
        return projectManagerMock;
    }

    private GroupPickerSearchServiceMock getGroupPickerSearchServiceMock(final boolean groupAlreadyExists) {
        final GroupPickerSearchServiceMock groupPickerSearchService = new GroupPickerSearchServiceMock();
        if (groupAlreadyExists) {
            groupPickerSearchService.addGroupByName(BLACKDUCK_JIRA_GROUP);
        }
        return groupPickerSearchService;
    }

    private Collection<IssueType> getIssueTypes(final boolean bdIssueTypesAlreadyAdded) {
        final Collection<IssueType> issueTypes = new ArrayList<>();
        final IssueTypeMock issueType = new IssueTypeMock();
        issueType.setName("Task");
        issueType.setId("Task");
        issueType.setValue(Mockito.mock(GenericValue.class));
        issueTypes.add(issueType);
        if (bdIssueTypesAlreadyAdded) {
            final IssueTypeMock policyViolationIssue = new IssueTypeMock();
            policyViolationIssue.setName(BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_ISSUE);
            policyViolationIssue.setId(BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_ISSUE);
            policyViolationIssue.setValue(Mockito.mock(GenericValue.class));
            issueTypes.add(policyViolationIssue);
            final IssueTypeMock securityIssue = new IssueTypeMock();
            securityIssue.setName(BlackDuckJiraConstants.BLACKDUCK_VULNERABILITY_ISSUE);
            securityIssue.setId(BlackDuckJiraConstants.BLACKDUCK_VULNERABILITY_ISSUE);
            securityIssue.setValue(Mockito.mock(GenericValue.class));
            issueTypes.add(securityIssue);
        }
        return issueTypes;
    }

    private JiraServices getJiraServices(final WorkflowManager workflowManager,
            final WorkflowSchemeManager workflowSchemeManager, final UserManager userManager,
            final ProjectManager projectManager, final AvatarManager avatarManager,
            final ConstantsManager constantsManager, final IssueTypeSchemeManager issueTypeSchemeManager,
            final FieldLayoutManager fieldLayoutManager,
            final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager, final Collection<IssueType> issueTypes,
            final UserUtil userUtil, final CustomFieldManagerMock customFieldManager,
            final FieldManagerMock fieldManager, final FieldScreenManagerMock fieldScreenManager,
            final FieldScreenSchemeManagerMock fieldScreenSchemeManager, final SearchService searchService) {

        JiraServicesMock jiraServices = new JiraServicesMock();
        jiraServices.setWorkflowManager(workflowManager);
        jiraServices.setWorkflowSchemeManager(workflowSchemeManager);
        jiraServices.setUserManager(userManager);
        jiraServices.setProjectManager(projectManager);
        jiraServices.setAvatarManager(avatarManager);
        jiraServices.setConstantsManager(constantsManager);
        jiraServices.setIssueTypeSchemeManager(issueTypeSchemeManager);
        jiraServices.setFieldLayoutManager(fieldLayoutManager);
        jiraServices.setIssueTypes(issueTypes);
        jiraServices.setUserUtil(userUtil);
        jiraServices.setIssueTypeScreenSchemeManager(issueTypeScreenSchemeManager);
        jiraServices.setCustomFieldManager(customFieldManager);
        jiraServices.setFieldManager(fieldManager);
        jiraServices.setFieldScreenManager(fieldScreenManager);
        jiraServices.setFieldScreenSchemeManager(fieldScreenSchemeManager);
        jiraServices.setSearchService(searchService);

        jiraServices = Mockito.spy(jiraServices);

        Mockito.when(jiraServices.getResourceAsStream(Mockito.anyString())).then(new Answer<InputStream>() {
            @Override
            public InputStream answer(final InvocationOnMock invocation) throws Throwable {
                final Object[] arguments = invocation.getArguments();

                return getClass().getResourceAsStream((String) arguments[0]);
            }
        });

        return jiraServices;
    }

    private String getProjectMappingJson(final boolean hasProjectMapping, final String jiraProjectName,
            final long jiraProjectId) {
        final Set<BlackDuckProjectMapping> mappings = new HashSet<>();
        if (hasProjectMapping) {
            final BlackDuckProjectMapping mapping = new BlackDuckProjectMapping();
            final JiraProject jiraProject = new JiraProject();
            jiraProject.setProjectName(jiraProjectName);
            jiraProject.setProjectId(jiraProjectId);
            mapping.setJiraProject(jiraProject);
            final BlackDuckProject blackDuckProject = new BlackDuckProject();
            blackDuckProject.setProjectName(BLACKDUCK_PROJECT_NAME);
            mapping.setHubProject(blackDuckProject);
            mappings.add(mapping);
        }
        final BlackDuckJiraConfigSerializable config = new BlackDuckJiraConfigSerializable();
        config.setHubProjectMappings(mappings);

        return config.getHubProjectMappingsJson();
    }

    @SuppressWarnings("unused")
    class JiraEnvironment {
        private GroupPickerSearchServiceMock groupPickerSearchServiceMock;
        private WorkflowManagerMock workflowManagerMock;
        private WorkflowSchemeManagerMock workflowSchemeManagerMock;
        private UserManagerMock userManagerMock;
        private ProjectManagerMock projectManagerMock;
        private AvatarManagerMock avatarManagerMock;
        private ConstantsManagerMock constantsManagerMock;
        private IssueTypeSchemeManagerMock issueTypeSchemeManagerMock;
        private FieldConfigSchemeMock fieldConfigSchemeMock;
        private FieldLayoutManagerMock fieldLayoutManagerMock;
        private IssueTypeScreenSchemeManagerMock issueTypeScreenSchemeManagerMock;
        private IssueTypeScreenSchemeMock issueTypeScreenSchemeMock;
        private FieldLayoutSchemeMock fieldLayoutSchemeMock;
        private FieldConfigurationSchemeMock fieldConfigurationSchemeMock;
        private EditableFieldLayoutMock editableFieldLayoutMock;
        private OrderableFieldMock orderableFieldMock;
        private CustomFieldManagerMock customFieldManagerMock;
        private FieldManagerMock fieldManagerMock;
        private FieldScreenManagerMock fieldScreenManagerMock;
        private FieldScreenSchemeManagerMock fieldScreenSchemeManagerMock;
        private PluginSettingsMock pluginSettingsMock;
        private JiraSettingsService jiraSettingsService;
        private Collection<IssueType> issueTypes;
        private UserUtil userUtil;
        private JiraServices jiraServices;
        private BlackDuckFieldScreenSchemeSetup blackDuckFieldScreenSchemeSetup;
        private JiraTaskTimed jiraTask;
        private String mappingJson;
        private Avatar avatarTemplate;

        JiraEnvironment setGroupPickerSearchServiceMock(
                final GroupPickerSearchServiceMock groupPickerSearchServiceMock) {
            this.groupPickerSearchServiceMock = groupPickerSearchServiceMock;
            return this;
        }

        WorkflowManagerMock getWorkflowManagerMock() {
            return workflowManagerMock;
        }

        JiraEnvironment setWorkflowManagerMock(final WorkflowManagerMock workflowManagerMock) {
            this.workflowManagerMock = workflowManagerMock;
            return this;
        }

        WorkflowSchemeManagerMock getWorkflowSchemeManagerMock() {
            return workflowSchemeManagerMock;
        }

        JiraEnvironment setWorkflowSchemeManagerMock(
                final WorkflowSchemeManagerMock workflowSchemeManagerMock) {
            this.workflowSchemeManagerMock = workflowSchemeManagerMock;
            return this;
        }

        JiraEnvironment setUserManagerMock(final UserManagerMock userManagerMock) {
            this.userManagerMock = userManagerMock;
            return this;
        }

        JiraEnvironment setProjectManagerMock(final ProjectManagerMock projectManagerMock) {
            this.projectManagerMock = projectManagerMock;
            return this;
        }

        AvatarManagerMock getAvatarManagerMock() {
            return avatarManagerMock;
        }

        JiraEnvironment setAvatarManagerMock(final AvatarManagerMock avatarManagerMock) {
            this.avatarManagerMock = avatarManagerMock;
            return this;
        }

        ConstantsManagerMock getConstantsManagerMock() {
            return constantsManagerMock;
        }

        JiraEnvironment setConstantsManagerMock(final ConstantsManagerMock constantsManagerMock) {
            this.constantsManagerMock = constantsManagerMock;
            return this;
        }

        JiraEnvironment setIssueTypeSchemeManagerMock(
                final IssueTypeSchemeManagerMock issueTypeSchemeManagerMock) {
            this.issueTypeSchemeManagerMock = issueTypeSchemeManagerMock;
            return this;
        }

        JiraEnvironment setFieldConfigSchemeMock(final FieldConfigSchemeMock fieldConfigSchemeMock) {
            this.fieldConfigSchemeMock = fieldConfigSchemeMock;
            return this;
        }

        JiraEnvironment setFieldLayoutManagerMock(final FieldLayoutManagerMock fieldLayoutManagerMock) {
            this.fieldLayoutManagerMock = fieldLayoutManagerMock;
            return this;
        }

        JiraEnvironment setIssueTypeScreenSchemeManagerMock(
                final IssueTypeScreenSchemeManagerMock issueTypeScreenSchemeManagerMock) {
            this.issueTypeScreenSchemeManagerMock = issueTypeScreenSchemeManagerMock;
            return this;
        }

        IssueTypeScreenSchemeMock getIssueTypeScreenSchemeMock() {
            return issueTypeScreenSchemeMock;
        }

        JiraEnvironment setIssueTypeScreenSchemeMock(
                final IssueTypeScreenSchemeMock issueTypeScreenSchemeMock) {
            this.issueTypeScreenSchemeMock = issueTypeScreenSchemeMock;
            return this;
        }

        FieldLayoutSchemeMock getFieldLayoutSchemeMock() {
            return fieldLayoutSchemeMock;
        }

        JiraEnvironment setFieldLayoutSchemeMock(final FieldLayoutSchemeMock fieldLayoutSchemeMock) {
            this.fieldLayoutSchemeMock = fieldLayoutSchemeMock;
            return this;
        }

        JiraEnvironment setFieldConfigurationSchemeMock(
                final FieldConfigurationSchemeMock fieldConfigurationSchemeMock) {
            this.fieldConfigurationSchemeMock = fieldConfigurationSchemeMock;
            return this;
        }

        JiraEnvironment setEditableFieldLayoutMock(final EditableFieldLayoutMock editableFieldLayoutMock) {
            this.editableFieldLayoutMock = editableFieldLayoutMock;
            return this;
        }

        JiraEnvironment setOrderableFieldMock(final OrderableFieldMock orderableFieldMock) {
            this.orderableFieldMock = orderableFieldMock;
            return this;
        }

        CustomFieldManagerMock getCustomFieldManagerMock() {
            return customFieldManagerMock;
        }

        JiraEnvironment setCustomFieldManagerMock(final CustomFieldManagerMock customFieldManagerMock) {
            this.customFieldManagerMock = customFieldManagerMock;
            return this;
        }

        JiraEnvironment setFieldManagerMock(final FieldManagerMock fieldManagerMock) {
            this.fieldManagerMock = fieldManagerMock;
            return this;
        }

        FieldScreenManagerMock getFieldScreenManagerMock() {
            return fieldScreenManagerMock;
        }

        JiraEnvironment setFieldScreenManagerMock(final FieldScreenManagerMock fieldScreenManagerMock) {
            this.fieldScreenManagerMock = fieldScreenManagerMock;
            return this;
        }

        FieldScreenSchemeManagerMock getFieldScreenSchemeManagerMock() {
            return fieldScreenSchemeManagerMock;
        }

        JiraEnvironment setFieldScreenSchemeManagerMock(
                final FieldScreenSchemeManagerMock fieldScreenSchemeManagerMock) {
            this.fieldScreenSchemeManagerMock = fieldScreenSchemeManagerMock;
            return this;
        }

        PluginSettingsMock getPluginSettingsMock() {
            return pluginSettingsMock;
        }

        JiraEnvironment setPluginSettingsMock(final PluginSettingsMock pluginSettingsMock) {
            this.pluginSettingsMock = pluginSettingsMock;
            return this;
        }

        JiraSettingsService getJiraSettingsService() {
            return jiraSettingsService;
        }

        JiraEnvironment setJiraSettingsService(final JiraSettingsService jiraSettingsService) {
            this.jiraSettingsService = jiraSettingsService;
            return this;
        }

        JiraEnvironment setIssueTypes(final Collection<IssueType> issueTypes) {
            this.issueTypes = issueTypes;
            return this;
        }

        JiraEnvironment setUserUtil(final UserUtil userUtil) {
            this.userUtil = userUtil;
            return this;
        }

        JiraServices getJiraServices() {
            return jiraServices;
        }

        JiraEnvironment setJiraServices(final JiraServices jiraServices) {
            this.jiraServices = jiraServices;
            return this;
        }

        JiraEnvironment setBlackDuckFieldScreenSchemeSetup(final BlackDuckFieldScreenSchemeSetup blackDuckFieldScreenSchemeSetup) {
            this.blackDuckFieldScreenSchemeSetup = blackDuckFieldScreenSchemeSetup;
            return this;
        }

        JiraTaskTimed getJiraTask() {
            return jiraTask;
        }

        JiraEnvironment setJiraTask(final JiraTaskTimed jiraTask) {
            this.jiraTask = jiraTask;
            return this;
        }

        String getMappingJson() {
            return mappingJson;
        }

        JiraEnvironment setMappingJson(final String mappingJson) {
            this.mappingJson = mappingJson;
            return this;
        }

        Avatar getAvatarTemplate() {
            return avatarTemplate;
        }

        JiraEnvironment setAvatarTemplate(final Avatar avatarTemplate) {
            this.avatarTemplate = avatarTemplate;
            return this;
        }

    }
}
