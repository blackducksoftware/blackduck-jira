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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;
import com.blackducksoftware.integration.jira.config.JiraSettingsService;
import com.blackducksoftware.integration.jira.mocks.ApplicationUserMock;
import com.blackducksoftware.integration.jira.mocks.JiraServicesMock;
import com.blackducksoftware.integration.jira.mocks.PluginSettingsMock;
import com.blackducksoftware.integration.jira.mocks.ProjectMock;
import com.blackducksoftware.integration.jira.mocks.UserUtilMock;
import com.blackducksoftware.integration.jira.mocks.issue.IssueTypeMock;
import com.blackducksoftware.integration.jira.mocks.workflow.AssignableWorkflowSchemeBuilderMock;
import com.blackducksoftware.integration.jira.mocks.workflow.AssignableWorkflowSchemeMock;
import com.blackducksoftware.integration.jira.mocks.workflow.JiraWorkflowMock;
import com.blackducksoftware.integration.jira.mocks.workflow.WorkflowManagerMock;
import com.blackducksoftware.integration.jira.mocks.workflow.WorkflowSchemeManagerMock;

public class BlackDuckWorkflowSetupTest {

    @Test
    public void testAddHubWorkflowToJiraNoUser() {
        final PluginSettingsMock settingsMock = new PluginSettingsMock();
        final JiraSettingsService settingService = new JiraSettingsService(settingsMock);
        final WorkflowManagerMock workflowManager = new WorkflowManagerMock();
        final WorkflowSchemeManagerMock workflowSchemeManager = new WorkflowSchemeManagerMock();
        final UserUtilMock userUtil = new UserUtilMock();

        final JiraServicesMock services = new JiraServicesMock();
        services.setWorkflowManager(workflowManager);
        services.setWorkflowSchemeManager(workflowSchemeManager);
        services.setUserUtil(userUtil);

        final String jiraUserName = "FakeUser";
        final ApplicationUserMock user = new ApplicationUserMock();
        user.setName(jiraUserName);
        final BlackDuckWorkflowSetup workflowSetup = new BlackDuckWorkflowSetup(settingService, services);

        assertNull(workflowSetup.addBlackDuckWorkflowToJira());
        assertTrue(!workflowManager.getAttemptedCreateWorkflow());
        assertNull(settingsMock.get(BlackDuckJiraConstants.BLACK_DUCK_JIRA_ERROR));
    }

    @Test
    public void testAddHubWorkflowToJiraAlreadyExisting() {
        final PluginSettingsMock settingsMock = new PluginSettingsMock();
        final JiraSettingsService settingService = new JiraSettingsService(settingsMock);

        final JiraWorkflowMock workflowExisitng = new JiraWorkflowMock();
        workflowExisitng.setName(BlackDuckJiraConstants.BLACK_DUCK_JIRA_WORKFLOW);

        final WorkflowManagerMock workflowManager = new WorkflowManagerMock();

        workflowManager.addWorkflow(workflowExisitng);

        final WorkflowSchemeManagerMock workflowSchemeManager = new WorkflowSchemeManagerMock();

        final String jiraUserName = "FakeUser";

        final UserUtilMock userUtil = new UserUtilMock();
        final ApplicationUserMock user = new ApplicationUserMock();
        user.setName(jiraUserName);
        userUtil.setUser(user);

        final JiraServicesMock services = new JiraServicesMock();
        services.setWorkflowManager(workflowManager);
        services.setWorkflowSchemeManager(workflowSchemeManager);
        services.setUserUtil(userUtil);

        final BlackDuckWorkflowSetup workflowSetup = new BlackDuckWorkflowSetup(settingService, services);

        assertEquals(workflowExisitng, workflowSetup.addBlackDuckWorkflowToJira());
        assertTrue(!workflowManager.getAttemptedCreateWorkflow());
        assertNull(settingsMock.get(BlackDuckJiraConstants.BLACK_DUCK_JIRA_ERROR));
    }

    @Test
    public void testAddHubWorkflowToJira() {
        final PluginSettingsMock settingsMock = new PluginSettingsMock();
        final JiraSettingsService settingService = new JiraSettingsService(settingsMock);
        final WorkflowManagerMock workflowManager = new WorkflowManagerMock();
        final WorkflowSchemeManagerMock workflowSchemeManager = new WorkflowSchemeManagerMock();

        final String jiraUserName = "FakeUser";

        final UserUtilMock userUtil = new UserUtilMock();
        final ApplicationUserMock user = new ApplicationUserMock();
        user.setName(jiraUserName);
        userUtil.setUser(user);

        final JiraServicesMock services = new JiraServicesMock();
        services.setWorkflowManager(workflowManager);
        services.setWorkflowSchemeManager(workflowSchemeManager);
        services.setUserUtil(userUtil);
        final BlackDuckWorkflowSetup workflowSetup = new BlackDuckWorkflowSetup(settingService, services);

        final JiraWorkflow workflow = workflowSetup.addBlackDuckWorkflowToJira();

        assertNotNull(workflow);
        assertTrue(workflowManager.getAttemptedCreateWorkflow());
        assertNull(settingsMock.get(BlackDuckJiraConstants.BLACK_DUCK_JIRA_ERROR));
    }

    @Test
    public void testAddWorkflowToProjectsWorkflowSchemeNoWorkflow() {
        final PluginSettingsMock settingsMock = new PluginSettingsMock();
        final JiraSettingsService settingService = new JiraSettingsService(settingsMock);

        final String workflowName = "TestWorkflow";
        final WorkflowManagerMock workflowManager = new WorkflowManagerMock();
        final WorkflowSchemeManagerMock workflowSchemeManager = new WorkflowSchemeManagerMock();
        final String jiraUserName = "FakeUser";

        final UserUtilMock userUtil = new UserUtilMock();
        final ApplicationUserMock user = new ApplicationUserMock();
        user.setName(jiraUserName);
        userUtil.setUser(user);

        final JiraServicesMock services = new JiraServicesMock();
        services.setWorkflowManager(workflowManager);
        services.setWorkflowSchemeManager(workflowSchemeManager);
        services.setUserUtil(userUtil);
        final BlackDuckWorkflowSetup workflowSetup = new BlackDuckWorkflowSetup(settingService, services);

        final JiraWorkflowMock workflow = new JiraWorkflowMock();
        workflow.setName(workflowName);

        final ProjectMock project = new ProjectMock();
        project.setName("TestProject");

        workflowSetup.addWorkflowToProjectsWorkflowScheme(workflow, project, null);

        assertTrue(!workflowSchemeManager.getAttemptedWorkflowUpdate());
        assertNotNull(settingsMock.get(BlackDuckJiraConstants.BLACK_DUCK_JIRA_ERROR));
    }

    @Test
    public void testAddWorkflowToProjectsWorkflowSchemeNoIssueTypes() {
        final PluginSettingsMock settingsMock = new PluginSettingsMock();
        final JiraSettingsService settingService = new JiraSettingsService(settingsMock);
        final WorkflowManagerMock workflowManager = new WorkflowManagerMock();
        final String workflowName = "TestWorkflow";

        final AssignableWorkflowSchemeMock hubWorkflow = new AssignableWorkflowSchemeMock();
        hubWorkflow.setName(workflowName);

        final AssignableWorkflowSchemeBuilderMock builder = new AssignableWorkflowSchemeBuilderMock();
        builder.setWorkflowScheme(hubWorkflow);

        hubWorkflow.setBuilder(builder);

        final WorkflowSchemeManagerMock workflowSchemeManager = new WorkflowSchemeManagerMock();
        workflowSchemeManager.setAssignableWorkflowScheme(hubWorkflow);

        final String jiraUserName = "FakeUser";

        final UserUtilMock userUtil = new UserUtilMock();
        final ApplicationUserMock user = new ApplicationUserMock();
        user.setName(jiraUserName);
        userUtil.setUser(user);

        final JiraServicesMock services = new JiraServicesMock();
        services.setWorkflowManager(workflowManager);
        services.setWorkflowSchemeManager(workflowSchemeManager);
        services.setUserUtil(userUtil);
        final BlackDuckWorkflowSetup workflowSetup = new BlackDuckWorkflowSetup(settingService, services);

        final JiraWorkflowMock workflow = new JiraWorkflowMock();
        workflow.setName(workflowName);

        final ProjectMock project = new ProjectMock();
        project.setName("TestProject");

        workflowSetup.addWorkflowToProjectsWorkflowScheme(workflow, project, null);

        assertTrue(!workflowSchemeManager.getAttemptedWorkflowUpdate());
        assertNull(settingsMock.get(BlackDuckJiraConstants.BLACK_DUCK_JIRA_ERROR));
    }

    @Test
    public void testAddWorkflowToProjectsWorkflowSchemeIssueTypesNotInScheme() {
        final PluginSettingsMock settingsMock = new PluginSettingsMock();
        final JiraSettingsService settingService = new JiraSettingsService(settingsMock);
        final String workflowName = "TestWorkflow";
        final WorkflowManagerMock workflowManager = new WorkflowManagerMock();
        final String issueTypeName = "CustomIssueType";
        final String issueTypeId = "CustomIssueType";

        final List<IssueType> issueTypes = new ArrayList<>();
        final IssueTypeMock issueType = new IssueTypeMock();
        issueType.setName(issueTypeName);
        issueType.setId(issueTypeId);
        issueTypes.add(issueType);

        final AssignableWorkflowSchemeMock hubWorkflow = new AssignableWorkflowSchemeMock();
        hubWorkflow.setName(workflowName);

        final AssignableWorkflowSchemeBuilderMock builder = new AssignableWorkflowSchemeBuilderMock();
        builder.setWorkflowScheme(hubWorkflow);

        hubWorkflow.setBuilder(builder);

        final WorkflowSchemeManagerMock workflowSchemeManager = new WorkflowSchemeManagerMock();
        workflowSchemeManager.setAssignableWorkflowScheme(hubWorkflow);

        final String jiraUserName = "FakeUser";

        final UserUtilMock userUtil = new UserUtilMock();
        final ApplicationUserMock user = new ApplicationUserMock();
        user.setName(jiraUserName);
        userUtil.setUser(user);

        final JiraServicesMock services = new JiraServicesMock();
        services.setWorkflowManager(workflowManager);
        services.setWorkflowSchemeManager(workflowSchemeManager);
        services.setUserUtil(userUtil);
        final BlackDuckWorkflowSetup workflowSetup = new BlackDuckWorkflowSetup(settingService, services);

        final JiraWorkflowMock workflow = new JiraWorkflowMock();
        workflow.setName(workflowName);

        final ProjectMock project = new ProjectMock();
        project.setName("TestProject");

        workflowSetup.addWorkflowToProjectsWorkflowScheme(workflow, project, issueTypes);

        assertTrue(workflowSchemeManager.getAttemptedWorkflowUpdate());
        assertNull(settingsMock.get(BlackDuckJiraConstants.BLACK_DUCK_JIRA_ERROR));
    }

    @Test
    public void testAddWorkflowToProjectsWorkflowSchemeIssueTypesNeedUpdate() {
        final PluginSettingsMock settingsMock = new PluginSettingsMock();
        final JiraSettingsService settingService = new JiraSettingsService(settingsMock);
        final String workflowName = "TestWorkflow";
        final WorkflowManagerMock workflowManager = new WorkflowManagerMock();
        final String issueTypeName = "CustomIssueType";
        final String issueTypeId = "CustomIssueType";

        final List<IssueType> issueTypes = new ArrayList<>();
        final IssueTypeMock issueType = new IssueTypeMock();
        issueType.setName(issueTypeName);
        issueType.setId(issueTypeId);
        issueTypes.add(issueType);

        final AssignableWorkflowSchemeMock hubWorkflow = new AssignableWorkflowSchemeMock();
        hubWorkflow.setName(workflowName);

        hubWorkflow.addMappingIssueToWorkflow(issueTypeName, "FakeWorkflow");

        final AssignableWorkflowSchemeBuilderMock builder = new AssignableWorkflowSchemeBuilderMock();
        builder.setWorkflowScheme(hubWorkflow);

        hubWorkflow.setBuilder(builder);

        final WorkflowSchemeManagerMock workflowSchemeManager = new WorkflowSchemeManagerMock();
        workflowSchemeManager.setAssignableWorkflowScheme(hubWorkflow);

        final String jiraUser = "FakeUser";

        final UserUtilMock userUtil = new UserUtilMock();
        final ApplicationUserMock user = new ApplicationUserMock();
        user.setName(jiraUser);
        userUtil.setUser(user);

        final JiraServicesMock services = new JiraServicesMock();
        services.setWorkflowManager(workflowManager);
        services.setWorkflowSchemeManager(workflowSchemeManager);
        services.setUserUtil(userUtil);
        final BlackDuckWorkflowSetup workflowSetup = new BlackDuckWorkflowSetup(settingService, services);

        final JiraWorkflowMock workflow = new JiraWorkflowMock();
        workflow.setName(workflowName);

        final ProjectMock project = new ProjectMock();
        project.setName("TestProject");

        workflowSetup.addWorkflowToProjectsWorkflowScheme(workflow, project, issueTypes);

        assertTrue(workflowSchemeManager.getAttemptedWorkflowUpdate());

        final Map<String, String> mappings = hubWorkflow.getMappings();

        final String workflowNameMapped = mappings.get(issueTypeName);
        assertEquals("TestWorkflow", workflowNameMapped);
        assertNull(settingsMock.get(BlackDuckJiraConstants.BLACK_DUCK_JIRA_ERROR));
    }

    @Test
    public void testAddWorkflowToProjectsWorkflowSchemeIssueTypesNoUpdate() {
        final PluginSettingsMock settingsMock = new PluginSettingsMock();
        final JiraSettingsService settingService = new JiraSettingsService(settingsMock);
        final String workflowName = "TestWorkflow";
        final WorkflowManagerMock workflowManager = new WorkflowManagerMock();
        final String issueTypeName = "CustomIssueType";
        final String issueTypeId = "CustomIssueType";

        final List<IssueType> issueTypes = new ArrayList<>();
        final IssueTypeMock issueType = new IssueTypeMock();
        issueType.setName(issueTypeName);
        issueType.setId(issueTypeId);
        issueTypes.add(issueType);

        final AssignableWorkflowSchemeMock hubWorkflow = new AssignableWorkflowSchemeMock();
        hubWorkflow.setName(workflowName);

        hubWorkflow.addMappingIssueToWorkflow(issueTypeName, workflowName);

        final AssignableWorkflowSchemeBuilderMock builder = new AssignableWorkflowSchemeBuilderMock();
        builder.setWorkflowScheme(hubWorkflow);

        hubWorkflow.setBuilder(builder);

        final WorkflowSchemeManagerMock workflowSchemeManager = new WorkflowSchemeManagerMock();
        workflowSchemeManager.setAssignableWorkflowScheme(hubWorkflow);

        final String jiraUser = "FakeUser";
        final UserUtilMock userUtil = new UserUtilMock();
        final ApplicationUserMock user = new ApplicationUserMock();
        user.setName(jiraUser);
        userUtil.setUser(user);

        final JiraServicesMock services = new JiraServicesMock();
        services.setWorkflowManager(workflowManager);
        services.setWorkflowSchemeManager(workflowSchemeManager);
        services.setUserUtil(userUtil);
        final BlackDuckWorkflowSetup workflowSetup = new BlackDuckWorkflowSetup(settingService, services);

        final JiraWorkflowMock workflow = new JiraWorkflowMock();
        workflow.setName(workflowName);

        final ProjectMock project = new ProjectMock();
        project.setName("TestProject");

        workflowSetup.addWorkflowToProjectsWorkflowScheme(workflow, project, issueTypes);

        assertTrue(!workflowSchemeManager.getAttemptedWorkflowUpdate());

        final Map<String, String> mappings = hubWorkflow.getMappings();

        final String workflowNameMapped = mappings.get(issueTypeName);
        assertEquals(workflowName, workflowNameMapped);
        assertNull(settingsMock.get(BlackDuckJiraConstants.BLACK_DUCK_JIRA_ERROR));
    }

}
