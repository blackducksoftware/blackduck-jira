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
package com.blackducksoftware.integration.jira.task.conversion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.mockito.Mockito;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.ProjectManager;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;
import com.blackducksoftware.integration.jira.common.model.JiraProject;
import com.blackducksoftware.integration.jira.config.JiraServices;
import com.blackducksoftware.integration.jira.mocks.ProjectManagerMock;
import com.synopsys.integration.hub.exception.HubIntegrationException;

public class ConverterTestUtils {

    public static final String JIRA_PROJECT_PREFIX = "Test JIRA Project";

    public static final long JIRA_PROJECT_ID_BASE = 153L;

    static JiraServices mockJiraServices() throws HubIntegrationException {

        final ConstantsManager constantsManager = Mockito.mock(ConstantsManager.class);
        final Collection<IssueType> issueTypes = new ArrayList<>();
        final IssueType policyIssueType = Mockito.mock(IssueType.class);
        Mockito.when(policyIssueType.getName()).thenReturn(BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_ISSUE);
        Mockito.when(policyIssueType.getId()).thenReturn("policyIssueTypeId");
        issueTypes.add(policyIssueType);
        final IssueType vulnerabilityIssueType = Mockito.mock(IssueType.class);
        Mockito.when(vulnerabilityIssueType.getName()).thenReturn(BlackDuckJiraConstants.BLACKDUCK_VULNERABILITY_ISSUE);
        Mockito.when(vulnerabilityIssueType.getId()).thenReturn("vulnerabilityIssueTypeId");
        issueTypes.add(vulnerabilityIssueType);

        Mockito.when(constantsManager.getAllIssueTypeObjects()).thenReturn(issueTypes);

        final JiraServices jiraServices = Mockito.mock(JiraServices.class);
        Mockito.when(jiraServices.getConstantsManager()).thenReturn(constantsManager);
        final ProjectManager jiraProjectManager = createJiraProjectManager();
        Mockito.when(jiraServices.getJiraProjectManager()).thenReturn(jiraProjectManager);

        final List<JiraProject> mockJiraProjects = ConverterTestUtils.getTestJiraProjectObjects("assigneeUserId");
        for (final JiraProject mockJiraProject : mockJiraProjects) {
            Mockito.when(jiraServices.getJiraProject(mockJiraProject.getProjectId())).thenReturn(mockJiraProject);
        }

        return jiraServices;
    }

    static List<JiraProject> getTestJiraProjectObjects(final String assigneeUserId) {
        final List<JiraProject> jiraProjects = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            final JiraProject jiraProject = new JiraProject();
            jiraProject.setAssigneeUserId(assigneeUserId);
            jiraProject.setProjectId(JIRA_PROJECT_ID_BASE + i);
            jiraProject.setProjectKey("KEY");
            jiraProject.setProjectName(JIRA_PROJECT_PREFIX + i);
            jiraProjects.add(jiraProject);
        }

        return jiraProjects;
    }

    private static ProjectManager createJiraProjectManager() {
        final ProjectManagerMock projectManager = new ProjectManagerMock();
        projectManager.setProjectObjects(projectManager.getTestProjectObjectsWithTaskIssueType());
        return projectManager;
    }
}
