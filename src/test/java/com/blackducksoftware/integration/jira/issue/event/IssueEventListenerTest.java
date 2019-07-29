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
package com.blackducksoftware.integration.jira.issue.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.entity.property.EntityPropertyQuery;
import com.atlassian.jira.entity.property.EntityPropertyQuery.ExecutableQuery;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.user.ApplicationUser;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;
import com.blackducksoftware.integration.jira.data.BlackDuckConfigKeys;
import com.blackducksoftware.integration.jira.data.PluginConfigKeys;
import com.blackducksoftware.integration.jira.issue.handler.JiraIssuePropertyWrapper;
import com.blackducksoftware.integration.jira.issue.tracker.IssueEventListener;
import com.blackducksoftware.integration.jira.issue.tracker.IssueTrackerHandler;
import com.blackducksoftware.integration.jira.issue.tracker.IssueTrackerProperties;
import com.blackducksoftware.integration.jira.mocks.ApplicationUserMock;
import com.blackducksoftware.integration.jira.mocks.EntityPropertyMock;
import com.blackducksoftware.integration.jira.mocks.EntityPropertyQueryMock;
import com.blackducksoftware.integration.jira.mocks.EventPublisherMock;
import com.blackducksoftware.integration.jira.mocks.ExecutableQueryMock;
import com.blackducksoftware.integration.jira.mocks.JSonEntityPropertyManagerMock;
import com.blackducksoftware.integration.jira.mocks.JiraServicesMock;
import com.blackducksoftware.integration.jira.mocks.PluginSettingsFactoryMock;
import com.blackducksoftware.integration.jira.mocks.PluginSettingsMock;
import com.blackducksoftware.integration.jira.mocks.ProjectMock;
import com.blackducksoftware.integration.jira.mocks.StatusMock;
import com.blackducksoftware.integration.jira.mocks.UserManagerMock;
import com.blackducksoftware.integration.jira.mocks.issue.IssueMock;
import com.blackducksoftware.integration.jira.mocks.issue.IssueServiceMock;
import com.blackducksoftware.integration.jira.mocks.issue.JiraIssuePropertyWrapperMock;
import com.blackducksoftware.integration.jira.web.model.BlackDuckProjectMapping;
import com.blackducksoftware.integration.jira.web.model.JiraProject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.synopsys.integration.blackduck.api.generated.view.IssueView;
import com.synopsys.integration.blackduck.rest.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.rest.CredentialsBlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.rest.credentials.Credentials;
import com.synopsys.integration.rest.proxy.ProxyInfo;

public class IssueEventListenerTest {
    private static final Logger logger = LoggerFactory.getLogger(IssueEventListenerTest.class);

    private static final String JIRA_USER = "auser";
    private static final String BLACKDUCK_PROJECT_NAME = "HubProjectName";
    private static final String JIRA_PROJECT_NAME = "JiraProjectName";
    private static final Long JIRA_PROJECT_ID = 1L;
    private static final String ISSUE_URL = "ISSUE URL";
    private static final String STATUS_NAME = "STATUS NAME";
    private static final String ISSUE_DESCRIPTION = "ISSUE DESCRIPTION";
    private static final String ASSIGNEE_USER_NAME = "assignedUser";

    private final EventPublisherMock eventPublisher = new EventPublisherMock();
    private IssueEventListener listener;
    private PluginSettingsMock settings;
    private JiraServicesMock jiraServices;
    private IssueServiceMock issueServiceMock;

    @Before
    public void initTest() {
        settings = createPluginSettings();
        final PluginSettingsFactoryMock pluginSettingsFactory = new PluginSettingsFactoryMock(settings);
        jiraServices = new JiraServicesMock();
        jiraServices.setJsonEntityPropertyManager(new JSonEntityPropertyManagerMock());
        final UserManagerMock userManager = new UserManagerMock();
        userManager.setMockApplicationUser(createApplicationUser());
        jiraServices.setUserManager(userManager);
        final String url = "http://www.google.com";
        final BlackDuckHttpClient restConnection = new CredentialsBlackDuckHttpClient(new Slf4jIntLogger(logger), 120, true, ProxyInfo.NO_PROXY_INFO, url, null, Credentials.NO_CREDENTIALS);

        final BlackDuckServicesFactory blackDuckServicesFactory = Mockito.mock(BlackDuckServicesFactory.class);
        Mockito.when(blackDuckServicesFactory.getBlackDuckHttpClient()).thenReturn(restConnection);
        issueServiceMock = new IssueServiceMock(restConnection);

        final ApplicationUser jiraUser = Mockito.mock(ApplicationUser.class);
        Mockito.when(jiraUser.getName()).thenReturn(JIRA_USER);

        final JiraIssuePropertyWrapper issuePropertyWrapper = new JiraIssuePropertyWrapperMock(jiraServices);
        listener = new IssueListenerWithMocks(eventPublisher, pluginSettingsFactory, issuePropertyWrapper, blackDuckServicesFactory);
    }

    private PluginSettingsMock createPluginSettings() {
        final PluginSettingsMock settings = new PluginSettingsMock();
        settings.put(BlackDuckConfigKeys.CONFIG_BLACKDUCK_URL, "http://www.google.com");
        settings.put(BlackDuckConfigKeys.CONFIG_BLACKDUCK_TIMEOUT, "120");

        settings.put(BlackDuckConfigKeys.CONFIG_PROXY_HOST, "");
        settings.put(BlackDuckConfigKeys.CONFIG_PROXY_PORT, "");
        settings.put(BlackDuckConfigKeys.CONFIG_PROXY_USER, "");
        settings.put(BlackDuckConfigKeys.CONFIG_PROXY_PASS, "");
        settings.put(BlackDuckConfigKeys.CONFIG_PROXY_PASS_LENGTH, "");

        settings.put(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS, "1");
        settings.put(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_PROJECT_MAPPINGS_JSON, "");
        settings.put(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_POLICY_RULES_JSON, "");
        settings.put(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_FIRST_SAVE_TIME, "");
        settings.put(PluginConfigKeys.BLACKDUCK_CONFIG_LAST_RUN_DATE, "");

        settings.put(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_ISSUE_CREATOR_USER, JIRA_USER);
        settings.put(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_ADMIN_USER, JIRA_USER);

        settings.put(PluginConfigKeys.BLACKDUCK_CONFIG_FIELD_COPY_MAPPINGS_JSON, "");
        settings.put(PluginConfigKeys.BLACKDUCK_CONFIG_CREATE_VULN_ISSUES_CHOICE, "false");
        return settings;
    }

    private ApplicationUser createApplicationUser() {
        final ApplicationUserMock user = new ApplicationUserMock();
        user.setName(JIRA_USER);
        return user;
    }

    private String createProjectJSon(final Set<BlackDuckProjectMapping> projectMappings) {
        final Gson gson = new GsonBuilder().create();
        return gson.toJson(projectMappings);
    }

    private IssueEvent createIssueEvent(final Issue issue, final Long eventTypeId) {
        return new IssueEvent(issue, new HashMap<>(), createApplicationUser(), eventTypeId);
    }

    private Issue createIssue(final Long projectId, final Status status, final ApplicationUser assignee) {
        final IssueMock issue = new IssueMock();
        issue.setId(1L);
        final ProjectMock project = new ProjectMock();
        project.setId(projectId);
        project.setName(JIRA_PROJECT_NAME);
        issue.setProject(project);
        issue.setStatusObject(status);
        issue.setDescription(ISSUE_DESCRIPTION);
        issue.setCreated(new Timestamp(System.currentTimeMillis()));
        issue.setUpdated(new Timestamp(System.currentTimeMillis()));
        issue.setAssignee(assignee);

        return issue;
    }

    private String createIssuePropertiesJSON(final IssueTrackerProperties issueProperties) {
        final Gson gson = new GsonBuilder().create();
        return gson.toJson(issueProperties);
    }

    private void populateProjectSettings() {
        final Set<BlackDuckProjectMapping> projectSet = new HashSet<>();
        final BlackDuckProjectMapping mapping = new BlackDuckProjectMapping();
        final JiraProject jiraProject = new JiraProject();
        jiraProject.setProjectId(JIRA_PROJECT_ID);
        jiraProject.setProjectName(JIRA_PROJECT_NAME);
        mapping.setJiraProject(jiraProject);
        mapping.setBlackDuckProjectName(BLACKDUCK_PROJECT_NAME);
        projectSet.add(mapping);
        settings.put(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_PROJECT_MAPPINGS_JSON, createProjectJSon(projectSet));
    }

    private void createEntityProperty() {
        final EntityPropertyMock entityProperty = new EntityPropertyMock();
        entityProperty.setEntityName(BlackDuckJiraConstants.ISSUE_PROPERTY_ENTITY_NAME);
        entityProperty.setKey(IssueTrackerHandler.JIRA_ISSUE_PROPERTY_BLACKDUCK_ISSUE_URL);
        final IssueTrackerProperties issueTrackerProperties = new IssueTrackerProperties(ISSUE_URL, JIRA_PROJECT_ID);
        entityProperty.setValue(createIssuePropertiesJSON(issueTrackerProperties));
        final List<EntityProperty> propList = new ArrayList<>(1);
        propList.add(entityProperty);
        final EntityPropertyQuery<?> query = Mockito.mock(EntityPropertyQueryMock.class);
        final ExecutableQuery executableQuery = Mockito.mock(ExecutableQueryMock.class);
        Mockito.when(query.key(Mockito.anyString())).thenReturn(executableQuery);
        Mockito.when(executableQuery.maxResults(Mockito.anyInt())).thenReturn(executableQuery);
        Mockito.when(executableQuery.find()).thenReturn(propList);
        final JSonEntityPropertyManagerMock jsonManager = Mockito.mock(JSonEntityPropertyManagerMock.class);
        Mockito.when(jsonManager.query()).thenAnswer(invocation -> query);
        jiraServices.setJsonEntityPropertyManager(jsonManager);
    }

    private Issue createValidIssue() {
        final StatusMock status = new StatusMock();
        status.setName(STATUS_NAME);
        final ApplicationUserMock assignee = new ApplicationUserMock();
        assignee.setName(ASSIGNEE_USER_NAME);
        return createIssue(JIRA_PROJECT_ID, status, assignee);
    }

    private void assertIssueCreated(final Long eventTypeId) {
        final Issue issue = createValidIssue();
        final IssueEvent event = createIssueEvent(issue, eventTypeId);
        listener.onIssueEvent(event);

        assertFalse(issueServiceMock.issueMap.isEmpty());

        final IssueView blackDuckIssue = (IssueView) issueServiceMock.issueMap.get(ISSUE_URL);

        assertNotNull(blackDuckIssue);
        assertEquals(issue.getKey(), blackDuckIssue.getIssueId());
        assertEquals(issue.getDescription(), blackDuckIssue.getIssueDescription());
        assertEquals(issue.getStatus().getName(), blackDuckIssue.getIssueStatus());
        assertEquals(issue.getCreated(), blackDuckIssue.getIssueCreatedAt());
        assertEquals(issue.getUpdated(), blackDuckIssue.getIssueUpdatedAt());
        assertEquals(issue.getAssignee().getDisplayName(), blackDuckIssue.getIssueAssignee());
    }

    private void assertIssueNotCreated(final Long eventTypeId) {
        final Issue issue = createValidIssue();
        final IssueEvent event = createIssueEvent(issue, eventTypeId);
        listener.onIssueEvent(event);
        assertTrue(issueServiceMock.issueMap.isEmpty());
    }

    @Test
    public void testDestroy() throws Exception {
        eventPublisher.registered = true;
        listener.destroy();
        assertFalse(eventPublisher.registered);
    }

    @Test
    public void testAfterPropertiesSet() throws Exception {
        eventPublisher.registered = false;
        listener.afterPropertiesSet();
        assertTrue(eventPublisher.registered);
    }

    @Test
    public void testCreatedEventId() {
        final Issue issue = createIssue(1L, new StatusMock(), new ApplicationUserMock());
        final IssueEvent event = createIssueEvent(issue, EventType.ISSUE_CREATED_ID);
        listener.onIssueEvent(event);
        assertTrue(issueServiceMock.issueMap.isEmpty());
    }

    @Test
    public void testEmptyProjectMapping() {
        final Issue issue = createIssue(1L, new StatusMock(), new ApplicationUserMock());
        final IssueEvent event = createIssueEvent(issue, EventType.ISSUE_UPDATED_ID);
        listener.onIssueEvent(event);
        assertTrue(issueServiceMock.issueMap.isEmpty());
    }

    @Test
    public void testEventType() {
        final Issue issue = createIssue(1L, new StatusMock(), new ApplicationUserMock());
        final IssueEvent event = createIssueEvent(issue, EventType.ISSUE_UPDATED_ID);
        listener.onIssueEvent(event);
        assertTrue(issueServiceMock.issueMap.isEmpty());
    }

    @Test
    public void testUpdateEventWithJiraProjectNotMapped() {
        populateProjectSettings();
        final Issue issue = createIssue(999L, new StatusMock(), new ApplicationUserMock());
        final IssueEvent event = createIssueEvent(issue, EventType.ISSUE_UPDATED_ID);
        listener.onIssueEvent(event);
        assertTrue(issueServiceMock.issueMap.isEmpty());
    }

    @Test
    public void testUpdateEventWithNullEntityProperty() {
        populateProjectSettings();
        final Issue issue = createIssue(JIRA_PROJECT_ID, new StatusMock(), new ApplicationUserMock());
        final IssueEvent event = createIssueEvent(issue, EventType.ISSUE_UPDATED_ID);
        listener.onIssueEvent(event);
        assertTrue(issueServiceMock.issueMap.isEmpty());
    }

    @Ignore
    @Test
    public void testUpdateEventsWithEntityProperty() {
        populateProjectSettings();
        createEntityProperty();
        final List<Long> updateEventList = new ArrayList<>();
        updateEventList.add(EventType.ISSUE_ASSIGNED_ID);
        updateEventList.add(EventType.ISSUE_CLOSED_ID);
        updateEventList.add(EventType.ISSUE_MOVED_ID);
        updateEventList.add(EventType.ISSUE_REOPENED_ID);
        updateEventList.add(EventType.ISSUE_RESOLVED_ID);
        updateEventList.add(EventType.ISSUE_UPDATED_ID);
        updateEventList.add(EventType.ISSUE_COMMENT_DELETED_ID);
        updateEventList.add(EventType.ISSUE_COMMENT_EDITED_ID);
        updateEventList.add(EventType.ISSUE_COMMENTED_ID);
        updateEventList.add(EventType.ISSUE_GENERICEVENT_ID);
        updateEventList.add(EventType.ISSUE_WORKLOG_DELETED_ID);
        updateEventList.add(EventType.ISSUE_WORKLOG_UPDATED_ID);
        updateEventList.add(EventType.ISSUE_WORKSTARTED_ID);
        updateEventList.add(EventType.ISSUE_WORKSTOPPED_ID);

        for (final Long eventTypeId : updateEventList) {
            assertIssueCreated(eventTypeId);
        }
    }

    @Test
    public void testIgnoredEventsWithEntityProperty() {
        populateProjectSettings();
        createEntityProperty();
        final List<Long> updateEventList = new ArrayList<>();
        updateEventList.add(EventType.ISSUE_CREATED_ID);

        for (final Long eventTypeId : updateEventList) {
            assertIssueNotCreated(eventTypeId);
        }
    }

    @Ignore
    @Test
    public void testUpdateEventWithEntityProperty() {
        populateProjectSettings();
        createEntityProperty();
        assertIssueCreated(EventType.ISSUE_UPDATED_ID);
    }

    @Ignore
    @Test
    public void testDeleteEventWithEntityProperty() {
        populateProjectSettings();
        createEntityProperty();

        final StatusMock status = new StatusMock();
        status.setName(STATUS_NAME);
        final ApplicationUserMock assignee = new ApplicationUserMock();
        assignee.setName(ASSIGNEE_USER_NAME);
        final Issue issue = createIssue(JIRA_PROJECT_ID, status, assignee);
        final IssueEvent event = createIssueEvent(issue, EventType.ISSUE_DELETED_ID);

        final IssueView blackDuckIssue = new IssueView();
        blackDuckIssue.setIssueId(issue.getKey());
        blackDuckIssue.setIssueDescription(issue.getDescription());
        blackDuckIssue.setIssueStatus(issue.getStatus().getName());
        blackDuckIssue.setIssueCreatedAt(issue.getCreated());
        blackDuckIssue.setIssueUpdatedAt(issue.getUpdated());
        blackDuckIssue.setIssueAssignee(issue.getAssignee().getDisplayName());

        issueServiceMock.issueMap.put(ISSUE_URL, blackDuckIssue);
        listener.onIssueEvent(event);

        assertTrue(issueServiceMock.issueMap.isEmpty());
    }
}
