/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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

import java.sql.Timestamp;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.user.ApplicationUser;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.settings.JiraSettingsAccessor;
import com.blackducksoftware.integration.jira.common.settings.PluginErrorAccessor;
import com.blackducksoftware.integration.jira.mocks.ApplicationUserMock;
import com.blackducksoftware.integration.jira.mocks.PluginSettingsMock;
import com.blackducksoftware.integration.jira.mocks.ProjectMock;
import com.blackducksoftware.integration.jira.mocks.StatusMock;
import com.blackducksoftware.integration.jira.mocks.issue.IssueMock;
import com.blackducksoftware.integration.jira.mocks.issue.IssueServiceMock;
import com.blackducksoftware.integration.jira.task.issue.tracker.IssueTrackerHandler;
import com.synopsys.integration.blackduck.api.generated.component.ResourceMetadata;
import com.synopsys.integration.blackduck.api.generated.view.IssueView;
import com.synopsys.integration.blackduck.rest.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.rest.CredentialsBlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.rest.credentials.Credentials;
import com.synopsys.integration.rest.proxy.ProxyInfo;

public class IssueTrackerHandlerTest {
    private static final String JIRA_PROJECT_NAME = "JiraProjectName";
    private static final Long JIRA_PROJECT_ID = new Long(1);
    private static final String ISSUE_URL = "api/project/1/versions/2/components/3/component-versions/4/issues";
    private static final String STATUS_NAME = "STATUS NAME";
    private static final String ISSUE_DESCRIPTION = "ISSUE DESCRIPTION";
    private static final String ASSIGNEE_USER_NAME = "assignedUser";

    private PluginSettingsMock settings;
    private IssueServiceMock issueServiceMock;
    private IssueTrackerHandler issueHandler;

    @Before
    public void initTest() throws Exception {
        settings = new PluginSettingsMock();
        final String url = "http://www.google.com";
        final BlackDuckHttpClient restConnection = new CredentialsBlackDuckHttpClient(Mockito.mock(BlackDuckJiraLogger.class), 120, true, ProxyInfo.NO_PROXY_INFO, url, null, Credentials.NO_CREDENTIALS);
        final BlackDuckService blackDuckService = Mockito.mock(BlackDuckService.class);
        Mockito.when(blackDuckService.getBlackDuckHttpClient()).thenReturn(restConnection);

        final JiraSettingsAccessor jiraSettingsAccessor = new JiraSettingsAccessor(settings);
        issueServiceMock = new IssueServiceMock(restConnection);
        issueHandler = new IssueTrackerHandler(new PluginErrorAccessor(jiraSettingsAccessor), issueServiceMock);
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

        issueHandler.createBlackDuckIssue(ISSUE_URL, issue);

        assertFalse(issueServiceMock.issueMap.isEmpty());

        final IssueView blackDuckIssue = (IssueView) issueServiceMock.issueMap.get(ISSUE_URL);

        assertEquals(issue.getKey(), blackDuckIssue.getIssueId());
        assertEquals(issue.getDescription(), blackDuckIssue.getIssueDescription());
        assertEquals(issue.getStatus().getName(), blackDuckIssue.getIssueStatus());
        assertEquals(issue.getCreated(), blackDuckIssue.getIssueCreatedAt());
        assertEquals(issue.getUpdated(), blackDuckIssue.getIssueUpdatedAt());
        assertEquals(issue.getAssignee().getDisplayName(), blackDuckIssue.getIssueAssignee());
    }

    @Test
    public void testUpdate() throws Exception {
        final StatusMock status = new StatusMock();
        status.setName(STATUS_NAME);
        final ApplicationUserMock assignee = new ApplicationUserMock();
        assignee.setName(ASSIGNEE_USER_NAME);
        final Issue issue = createIssue(new Long(1), JIRA_PROJECT_ID, JIRA_PROJECT_NAME, status, assignee);

        final IssueView testIssue = new IssueView();
        testIssue.setIssueId(issue.getKey());
        testIssue.setIssueDescription(issue.getDescription());
        testIssue.setIssueStatus("a status");
        testIssue.setIssueCreatedAt(issue.getCreated());
        testIssue.setIssueUpdatedAt(issue.getUpdated());
        testIssue.setIssueAssignee("someRandomUser");
        final ResourceMetadata meta = new ResourceMetadata();
        meta.setHref(ISSUE_URL);
        testIssue.setMeta(meta);
        issueServiceMock.responseList = Arrays.asList(testIssue);

        issueHandler.updateBlackDuckIssue(ISSUE_URL, issue);

        assertFalse(issueServiceMock.issueMap.isEmpty());

        final IssueView blackDuckIssue = (IssueView) issueServiceMock.issueMap.get(IssueServiceMock.TEST_PUT_URL);

        assertEquals(issue.getKey(), blackDuckIssue.getIssueId());
        assertEquals(issue.getDescription(), blackDuckIssue.getIssueDescription());
        assertEquals(issue.getStatus().getName(), blackDuckIssue.getIssueStatus());
        assertEquals(issue.getCreated(), blackDuckIssue.getIssueCreatedAt());
        assertEquals(issue.getUpdated(), blackDuckIssue.getIssueUpdatedAt());
        assertEquals(issue.getAssignee().getDisplayName(), blackDuckIssue.getIssueAssignee());
    }

    @Test
    public void testDelete() throws Exception {
        final StatusMock status = new StatusMock();
        status.setName(STATUS_NAME);
        final ApplicationUserMock assignee = new ApplicationUserMock();
        assignee.setName(ASSIGNEE_USER_NAME);
        final Issue issue = createIssue(new Long(1), JIRA_PROJECT_ID, JIRA_PROJECT_NAME, status, assignee);

        final IssueView blackDuckIssue = new IssueView();
        blackDuckIssue.setIssueId(issue.getKey());
        blackDuckIssue.setIssueDescription(issue.getDescription());
        blackDuckIssue.setIssueStatus(issue.getStatus().getName());
        blackDuckIssue.setIssueCreatedAt(issue.getCreated());
        blackDuckIssue.setIssueUpdatedAt(issue.getUpdated());
        blackDuckIssue.setIssueAssignee(issue.getAssignee().getDisplayName());

        issueServiceMock.issueMap.put(ISSUE_URL, blackDuckIssue);
        issueHandler.deleteBlackDuckIssue(ISSUE_URL, issue);

        assertTrue(issueServiceMock.issueMap.isEmpty());
    }

    @Test
    public void testCreateEmptyURL() throws Exception {
        final StatusMock status = new StatusMock();
        status.setName(STATUS_NAME);
        final ApplicationUserMock assignee = new ApplicationUserMock();
        assignee.setName(ASSIGNEE_USER_NAME);
        final Issue issue = createIssue(new Long(1), JIRA_PROJECT_ID, JIRA_PROJECT_NAME, status, assignee);

        issueHandler.createBlackDuckIssue("", issue);

        assertTrue(issueServiceMock.issueMap.isEmpty());
        assertNotNull(settings.get(BlackDuckJiraConstants.BLACKDUCK_JIRA_ERROR));
    }

    @Test
    public void testUpdateEmptyURL() throws Exception {
        final StatusMock status = new StatusMock();
        status.setName(STATUS_NAME);
        final ApplicationUserMock assignee = new ApplicationUserMock();
        assignee.setName(ASSIGNEE_USER_NAME);
        final Issue issue = createIssue(new Long(1), JIRA_PROJECT_ID, JIRA_PROJECT_NAME, status, assignee);

        issueHandler.updateBlackDuckIssue("", issue);

        assertTrue(issueServiceMock.issueMap.isEmpty());
        assertNotNull(settings.get(BlackDuckJiraConstants.BLACKDUCK_JIRA_ERROR));
    }

    @Test
    public void testDeleteEmptyURL() throws Exception {
        final StatusMock status = new StatusMock();
        status.setName(STATUS_NAME);
        final ApplicationUserMock assignee = new ApplicationUserMock();
        assignee.setName(ASSIGNEE_USER_NAME);
        final Issue issue = createIssue(new Long(1), JIRA_PROJECT_ID, JIRA_PROJECT_NAME, status, assignee);

        final IssueView blackDuckIssue = new IssueView();
        blackDuckIssue.setIssueId(issue.getKey());
        blackDuckIssue.setIssueDescription(issue.getDescription());
        blackDuckIssue.setIssueStatus(issue.getStatus().getName());
        blackDuckIssue.setIssueCreatedAt(issue.getCreated());
        blackDuckIssue.setIssueUpdatedAt(issue.getUpdated());
        blackDuckIssue.setIssueAssignee(issue.getAssignee().getDisplayName());

        issueServiceMock.issueMap.put(ISSUE_URL, blackDuckIssue);
        issueHandler.deleteBlackDuckIssue("", issue);

        assertFalse(issueServiceMock.issueMap.isEmpty());
        assertNotNull(settings.get(BlackDuckJiraConstants.BLACKDUCK_JIRA_ERROR));
    }
}
