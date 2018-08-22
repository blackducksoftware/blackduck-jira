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
package com.blackducksoftware.integration.jira.config;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.blackducksoftware.integration.jira.common.BlackDuckProjectMappings;
import com.blackducksoftware.integration.jira.common.model.BlackDuckProject;
import com.blackducksoftware.integration.jira.common.model.BlackDuckProjectMapping;
import com.blackducksoftware.integration.jira.common.model.JiraProject;
import com.synopsys.integration.blackduck.exception.HubIntegrationException;

public class BlackDuckProjectMappingsTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void test() throws HubIntegrationException {
        final JiraServices jiraServices = Mockito.mock(JiraServices.class);

        final Collection<IssueType> issueTypes = new ArrayList<>();
        final IssueType issueType = Mockito.mock(IssueType.class);
        Mockito.when(issueType.getName()).thenReturn("Issue");
        Mockito.when(issueType.getId()).thenReturn("issueTypeId");
        issueTypes.add(issueType);

        final ProjectManager jiraProjectManager = Mockito.mock(ProjectManager.class);
        Mockito.when(jiraServices.getJiraProjectManager()).thenReturn(jiraProjectManager);

        // ticketGenInfo.getJiraIssueTypeName()

        for (int i = 0; i < 10; i++) {
            final Project mockAtlassianJiraProject = Mockito.mock(Project.class);
            Mockito.when(mockAtlassianJiraProject.getKey()).thenReturn("projectKey" + i);
            Mockito.when(mockAtlassianJiraProject.getName()).thenReturn("projectName" + i);

            Mockito.when(mockAtlassianJiraProject.getIssueTypes()).thenReturn(issueTypes);

            Mockito.when(jiraProjectManager.getProjectObj((long) i)).thenReturn(mockAtlassianJiraProject);

            final JiraProject jiraProject = new JiraProject();
            jiraProject.setAssigneeUserId("assigneeUserId" + i);
            jiraProject.setProjectError("");
            jiraProject.setProjectId((long) i);
            jiraProject.setProjectKey("projectKey" + i);
            jiraProject.setProjectName("projectName" + i);
            Mockito.when(jiraServices.getJiraProject(i)).thenReturn(jiraProject);
        }

        final Set<BlackDuckProjectMapping> underlyingMappings = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            final BlackDuckProjectMapping mapping = new BlackDuckProjectMapping();
            final BlackDuckProject blackDuckProject = new BlackDuckProject();
            blackDuckProject.setProjectName("projectName" + i);
            blackDuckProject.setProjectUrl("projectUrl" + i);
            mapping.setHubProject(blackDuckProject);
            final JiraProject jiraProject = new JiraProject();
            // jiraProject.setAssigneeUserId("assigneeUserId" + i);
            jiraProject.setProjectError("");
            jiraProject.setProjectId((long) i);
            jiraProject.setProjectKey("projectKey" + i);
            jiraProject.setProjectName("projectName" + i);
            mapping.setJiraProject(jiraProject);
            underlyingMappings.add(mapping);
        }

        final BlackDuckProjectMappings mappings = new BlackDuckProjectMappings(jiraServices, underlyingMappings);

        final List<JiraProject> mappedJiraProjects = mappings.getJiraProjects("projectName7");
        assertEquals(1, mappedJiraProjects.size());
        final JiraProject mappedJiraProject = mappedJiraProjects.get(0);

        System.out.println(mappedJiraProject);
        assertEquals(Long.valueOf(7L), mappedJiraProject.getProjectId());
        assertEquals("assigneeUserId7", mappedJiraProject.getAssigneeUserId());
    }

}
