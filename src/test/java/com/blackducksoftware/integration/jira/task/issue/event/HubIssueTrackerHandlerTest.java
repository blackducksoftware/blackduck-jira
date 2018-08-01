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
package com.blackducksoftware.integration.jira.task.issue.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.sql.Timestamp;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.user.ApplicationUser;
import com.blackducksoftware.integration.hub.api.generated.view.IssueView;
import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.config.JiraSettingsService;
import com.blackducksoftware.integration.jira.mocks.ApplicationUserMock;
import com.blackducksoftware.integration.jira.mocks.PluginSettingsMock;
import com.blackducksoftware.integration.jira.mocks.ProjectMock;
import com.blackducksoftware.integration.jira.mocks.StatusMock;
import com.blackducksoftware.integration.jira.mocks.issue.IssueMock;
import com.blackducksoftware.integration.jira.mocks.issue.IssueServiceMock;
import com.blackducksoftware.integration.jira.task.issue.handler.HubIssueTrackerHandler;
import com.blackducksoftware.integration.rest.connection.RestConnection;
import com.blackducksoftware.integration.rest.proxy.ProxyInfo;

public class HubIssueTrackerHandlerTest {
    private static final String JIRA_PROJECT_NAME = "JiraProjectName";
    private static final Long JIRA_PROJECT_ID = new Long(1);
    private static final String ISSUE_URL = "ISSUE URL";
    private static final String STATUS_NAME = "STATUS NAME";
    private static final String ISSUE_DESCRIPTION = "ISSUE DESCRIPTION";
    private static final String ASSIGNEE_USER_NAME = "assignedUser";

    private PluginSettingsMock settings;
    private IssueServiceMock issueServiceMock;
    private HubIssueTrackerHandler issueHandler;

    @Before
    public void initTest() throws Exception {
        settings = new PluginSettingsMock();
        final URL url = new URL("http://www.google.com");
        final RestConnection restConnection = new CredentialsRestConnection(Mockito.mock(HubJiraLogger.class), url, "", "", 120, ProxyInfo.NO_PROXY_INFO);
        final HubService hubService = Mockito.mock(HubService.class);
        Mockito.when(hubService.getRestConnection()).thenReturn(restConnection);

        issueServiceMock = new IssueServiceMock(hubService);
        issueHandler = new HubIssueTrackerHandler(new JiraSettingsService(settings), issueServiceMock);
    }

    private Issue createIssue(final Long id, final Long projectId, final String projectName, final Status status, final ApplicationUser assignee) {
        final IssueMock issue = new IssueMock();
        issue.setId(id);
        final ProjectMock project = new ProjectMock();
        project.setId(projectId);
        project.setName(projectName);
        issue.setProject(project);
        issue.setStatusObject(status);
        issue.setDescription(ISSUE_DESCRIPTION);
        issue.setCreated(new Timestamp(System.currentTimeMillis()));
        issue.setUpdated(new Timestamp(System.currentTimeMillis()));
        issue.setAssignee(assignee);

        return issue;
    }

    @Test
    public void testCreate() throws Exception {
        final StatusMock status = new StatusMock();
        status.setName(STATUS_NAME);
        final ApplicationUserMock assignee = new ApplicationUserMock();
        assignee.setName(ASSIGNEE_USER_NAME);
        final Issue issue = createIssue(new Long(1), JIRA_PROJECT_ID, JIRA_PROJECT_NAME, status, assignee);

        issueHandler.createHubIssue(ISSUE_URL, issue);

        assertFalse(issueServiceMock.issueMap.isEmpty());

        final IssueView hubIssue = issueServiceMock.issueMap.get(ISSUE_URL);

        assertEquals(issue.getKey(), hubIssue.issueId);
        assertEquals(issue.getDescription(), hubIssue.issueDescription);
        assertEquals(issue.getStatus().getName(), hubIssue.issueStatus);
        assertEquals(issue.getCreated(), hubIssue.issueCreatedAt);
        assertEquals(issue.getUpdated(), hubIssue.issueUpdatedAt);
        assertEquals(issue.getAssignee().getDisplayName(), hubIssue.issueAssignee);
    }

    @Test
    public void testUpdate() throws Exception {
        final StatusMock status = new StatusMock();
        status.setName(STATUS_NAME);
        final ApplicationUserMock assignee = new ApplicationUserMock();
        assignee.setName(ASSIGNEE_USER_NAME);
        final Issue issue = createIssue(new Long(1), JIRA_PROJECT_ID, JIRA_PROJECT_NAME, status, assignee);

        issueHandler.updateHubIssue(ISSUE_URL, issue);

        assertFalse(issueServiceMock.issueMap.isEmpty());

        final IssueView hubIssue = issueServiceMock.issueMap.get(ISSUE_URL);

        assertEquals(issue.getKey(), hubIssue.issueId);
        assertEquals(issue.getDescription(), hubIssue.issueDescription);
        assertEquals(issue.getStatus().getName(), hubIssue.issueStatus);
        assertEquals(issue.getCreated(), hubIssue.issueCreatedAt);
        assertEquals(issue.getUpdated(), hubIssue.issueUpdatedAt);
        assertEquals(issue.getAssignee().getDisplayName(), hubIssue.issueAssignee);
    }

    @Test
    public void testDelete() throws Exception {
        final StatusMock status = new StatusMock();
        status.setName(STATUS_NAME);
        final ApplicationUserMock assignee = new ApplicationUserMock();
        assignee.setName(ASSIGNEE_USER_NAME);
        final Issue issue = createIssue(new Long(1), JIRA_PROJECT_ID, JIRA_PROJECT_NAME, status, assignee);

        final IssueView hubIssue = new IssueView();
        hubIssue.issueId = issue.getKey();
        hubIssue.issueDescription = issue.getDescription();
        hubIssue.issueStatus = issue.getStatus().getName();
        hubIssue.issueCreatedAt = issue.getCreated();
        hubIssue.issueUpdatedAt = issue.getUpdated();
        hubIssue.issueAssignee = issue.getAssignee().getDisplayName();

        issueServiceMock.issueMap.put(ISSUE_URL, hubIssue);
        issueHandler.deleteHubIssue(ISSUE_URL, issue);

        assertTrue(issueServiceMock.issueMap.isEmpty());
    }

    @Test
    public void testCreateEmptyURL() throws Exception {
        final StatusMock status = new StatusMock();
        status.setName(STATUS_NAME);
        final ApplicationUserMock assignee = new ApplicationUserMock();
        assignee.setName(ASSIGNEE_USER_NAME);
        final Issue issue = createIssue(new Long(1), JIRA_PROJECT_ID, JIRA_PROJECT_NAME, status, assignee);

        issueHandler.createHubIssue("", issue);

        assertTrue(issueServiceMock.issueMap.isEmpty());
        assertNotNull(settings.get(HubJiraConstants.HUB_JIRA_ERROR));
    }

    @Test
    public void testUpdateEmptyURL() throws Exception {
        final StatusMock status = new StatusMock();
        status.setName(STATUS_NAME);
        final ApplicationUserMock assignee = new ApplicationUserMock();
        assignee.setName(ASSIGNEE_USER_NAME);
        final Issue issue = createIssue(new Long(1), JIRA_PROJECT_ID, JIRA_PROJECT_NAME, status, assignee);

        issueHandler.updateHubIssue("", issue);

        assertTrue(issueServiceMock.issueMap.isEmpty());
        assertNotNull(settings.get(HubJiraConstants.HUB_JIRA_ERROR));
    }

    @Test
    public void testDeleteEmptyURL() throws Exception {
        final StatusMock status = new StatusMock();
        status.setName(STATUS_NAME);
        final ApplicationUserMock assignee = new ApplicationUserMock();
        assignee.setName(ASSIGNEE_USER_NAME);
        final Issue issue = createIssue(new Long(1), JIRA_PROJECT_ID, JIRA_PROJECT_NAME, status, assignee);

        final IssueView hubIssue = new IssueView();
        hubIssue.issueId = issue.getKey();
        hubIssue.issueDescription = issue.getDescription();
        hubIssue.issueStatus = issue.getStatus().getName();
        hubIssue.issueCreatedAt = issue.getCreated();
        hubIssue.issueUpdatedAt = issue.getUpdated();
        hubIssue.issueAssignee = issue.getAssignee().getDisplayName();

        issueServiceMock.issueMap.put(ISSUE_URL, hubIssue);
        issueHandler.deleteHubIssue("", issue);

        assertFalse(issueServiceMock.issueMap.isEmpty());
        assertNotNull(settings.get(HubJiraConstants.HUB_JIRA_ERROR));
    }
}
