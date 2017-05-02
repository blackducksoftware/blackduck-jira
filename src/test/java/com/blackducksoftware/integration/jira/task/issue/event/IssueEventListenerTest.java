/**
 * Hub JIRA Plugin
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
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
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.entity.property.EntityPropertyQuery;
import com.atlassian.jira.entity.property.EntityPropertyQuery.ExecutableQuery;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.user.ApplicationUser;
import com.blackducksoftware.integration.hub.model.response.VersionComparison;
import com.blackducksoftware.integration.hub.model.view.IssueView;
import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.jira.common.HubJiraConfigKeys;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.common.HubProject;
import com.blackducksoftware.integration.jira.common.HubProjectMapping;
import com.blackducksoftware.integration.jira.common.JiraProject;
import com.blackducksoftware.integration.jira.config.HubConfigKeys;
import com.blackducksoftware.integration.jira.mocks.ApplicationUserMock;
import com.blackducksoftware.integration.jira.mocks.BomComponentIssueServiceMock;
import com.blackducksoftware.integration.jira.mocks.EntityPropertyMock;
import com.blackducksoftware.integration.jira.mocks.EntityPropertyQueryMock;
import com.blackducksoftware.integration.jira.mocks.EventPublisherMock;
import com.blackducksoftware.integration.jira.mocks.ExecutableQueryMock;
import com.blackducksoftware.integration.jira.mocks.HubVersionRequestServiceMock;
import com.blackducksoftware.integration.jira.mocks.JSonEntityPropertyManagerMock;
import com.blackducksoftware.integration.jira.mocks.JiraServicesMock;
import com.blackducksoftware.integration.jira.mocks.PluginSettingsFactoryMock;
import com.blackducksoftware.integration.jira.mocks.PluginSettingsMock;
import com.blackducksoftware.integration.jira.mocks.ProjectMock;
import com.blackducksoftware.integration.jira.mocks.StatusMock;
import com.blackducksoftware.integration.jira.mocks.UserManagerMock;
import com.blackducksoftware.integration.jira.mocks.issue.IssueMock;
import com.blackducksoftware.integration.jira.task.conversion.output.HubIssueTrackerProperties;
import com.blackducksoftware.integration.jira.task.issue.HubIssueTrackerHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class IssueEventListenerTest {

    private static final String JIRA_USER = "auser";

    private static final String JIRA_ADMIN_USER = "adminuser";

    private static final String HUB_PROJECT_NAME = "HubProjectName";

    private static final String JIRA_PROJECT_NAME = "JiraProjectName";

    private static final Long JIRA_PROJECT_ID = new Long(1);

    private static final String ISSUE_URL = "ISSUE URL";

    private static final String STATUS_NAME = "STATUS NAME";

    private static final String ISSUE_DESCRIPTION = "ISSUE DESCRIPTION";

    private static final String ASSIGNEE_USER_NAME = "assignedUser";

    private final EventPublisherMock eventPublisher = new EventPublisherMock();

    private IssueEventListener listener;

    private PluginSettingsMock settings;

    private PluginSettingsFactoryMock pluginSettingsFactory;

    private JiraServicesMock jiraServices;

    private BomComponentIssueServiceMock issueServiceMock;

    private HubVersionRequestServiceMock versionRequestServiceMock;

    @Before
    public void initTest() throws MalformedURLException {
        settings = createPluginSettings();
        pluginSettingsFactory = new PluginSettingsFactoryMock(settings);
        jiraServices = new JiraServicesMock();
        jiraServices.setJsonEntityPropertyManager(new JSonEntityPropertyManagerMock());
        final UserManagerMock userManager = new UserManagerMock();
        userManager.setMockApplicationUser(createApplicationUser());
        jiraServices.setUserManager(userManager);
        final URL url = new URL("http://www.google.com");
        final RestConnection restConnection = new CredentialsRestConnection(Mockito.mock(HubJiraLogger.class), url, "", "", 120);

        issueServiceMock = new BomComponentIssueServiceMock(restConnection);
        versionRequestServiceMock = new HubVersionRequestServiceMock(restConnection);
        final VersionComparison versionComparison = new VersionComparison("3.7.0", "3.7.0", 0, "=");
        versionRequestServiceMock.setHubVersionComparison(versionComparison);
        final HubServicesFactory hubServicesFactory = Mockito.mock(HubServicesFactory.class);
        Mockito.when(hubServicesFactory.createBomComponentIssueRequestService(Mockito.any())).thenReturn(issueServiceMock);
        Mockito.when(hubServicesFactory.createHubVersionRequestService()).thenReturn(versionRequestServiceMock);
        final ApplicationUser jiraUser = Mockito.mock(ApplicationUser.class);
        Mockito.when(jiraUser.getName()).thenReturn(JIRA_USER);

        listener = new IssueListenerWithMocks(eventPublisher, pluginSettingsFactory, jiraServices, hubServicesFactory);
    }

    private PluginSettingsMock createPluginSettings() {
        final PluginSettingsMock settings = new PluginSettingsMock();
        settings.put(HubConfigKeys.CONFIG_HUB_URL, "www.google.com");
        settings.put(HubConfigKeys.CONFIG_HUB_USER, JIRA_USER);
        settings.put(HubConfigKeys.CONFIG_HUB_PASS, "apassword");
        settings.put(HubConfigKeys.CONFIG_HUB_PASS_LENGTH, "");
        settings.put(HubConfigKeys.CONFIG_HUB_TIMEOUT, "120");

        settings.put(HubConfigKeys.CONFIG_PROXY_HOST, "");
        settings.put(HubConfigKeys.CONFIG_PROXY_PORT, "");
        settings.put(HubConfigKeys.CONFIG_PROXY_NO_HOST, "");
        settings.put(HubConfigKeys.CONFIG_PROXY_USER, "");
        settings.put(HubConfigKeys.CONFIG_PROXY_PASS, "");
        settings.put(HubConfigKeys.CONFIG_PROXY_PASS_LENGTH, "");

        settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS, "1");
        settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_PROJECT_MAPPINGS_JSON, "");
        settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_POLICY_RULES_JSON, "");
        settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_FIRST_SAVE_TIME, "");
        settings.put(HubJiraConfigKeys.HUB_CONFIG_LAST_RUN_DATE, "");

        settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_ISSUE_CREATOR_USER, JIRA_USER);
        settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_ADMIN_USER, JIRA_USER);

        settings.put(HubJiraConfigKeys.HUB_CONFIG_FIELD_COPY_MAPPINGS_JSON, "");
        settings.put(HubJiraConfigKeys.HUB_CONFIG_CREATE_VULN_ISSUES_CHOICE, "false");
        return settings;
    }

    private ApplicationUser createApplicationUser() {
        final ApplicationUserMock user = new ApplicationUserMock();
        user.setName(JIRA_USER);
        return user;
    }

    private String createProjectJSon(final Set<HubProjectMapping> projectMappings) {
        final Gson gson = new GsonBuilder().create();
        return gson.toJson(projectMappings);
    }

    private IssueEvent createIssueEvent(final Issue issue, final Long eventTypeId) {
        return new IssueEvent(issue, new HashMap<>(), createApplicationUser(), eventTypeId);
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

    private String createIssuePropertiesJSON(final HubIssueTrackerProperties issueProperties) {
        final Gson gson = new GsonBuilder().create();
        return gson.toJson(issueProperties);
    }

    private void populateProjectSettings() {
        final Set<HubProjectMapping> projectSet = new HashSet<>();
        final HubProjectMapping mapping = new HubProjectMapping();
        final JiraProject jiraProject = new JiraProject();
        jiraProject.setProjectId(JIRA_PROJECT_ID);
        jiraProject.setProjectName(JIRA_PROJECT_NAME);
        mapping.setJiraProject(jiraProject);
        final HubProject hubProject = new HubProject();
        hubProject.setProjectName(HUB_PROJECT_NAME);
        mapping.setHubProject(hubProject);
        projectSet.add(mapping);
        settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_PROJECT_MAPPINGS_JSON, createProjectJSon(projectSet));
    }

    private void createEntityProperty() {
        final EntityPropertyMock entityProperty = new EntityPropertyMock();
        entityProperty.setEntityName(HubJiraConstants.ISSUE_PROPERTY_ENTITY_NAME);
        entityProperty.setKey(HubIssueTrackerHandler.JIRA_ISSUE_PROPERTY_HUB_ISSUE_URL);
        final HubIssueTrackerProperties issueTrackerProperties = new HubIssueTrackerProperties(ISSUE_URL, JIRA_PROJECT_ID);
        entityProperty.setValue(createIssuePropertiesJSON(issueTrackerProperties));
        final List<EntityProperty> propList = new ArrayList<>(1);
        propList.add(entityProperty);
        final EntityPropertyQuery<?> query = Mockito.mock(EntityPropertyQueryMock.class);
        final ExecutableQuery executableQuery = Mockito.mock(ExecutableQueryMock.class);
        Mockito.when(query.key(Mockito.anyString())).thenReturn(executableQuery);
        Mockito.when(executableQuery.maxResults(Mockito.anyInt())).thenReturn(executableQuery);
        Mockito.when(executableQuery.find()).thenReturn(propList);
        final JSonEntityPropertyManagerMock jsonManager = Mockito.mock(JSonEntityPropertyManagerMock.class);
        Mockito.when(jsonManager.query()).thenAnswer(new Answer<EntityPropertyQuery<?>>() {

            @Override
            public EntityPropertyQuery<?> answer(final InvocationOnMock invocation) throws Throwable {
                return query;
            }

        });
        jiraServices.setJsonEntityPropertyManager(jsonManager);
        // jiraServices.getJsonEntityPropertyManager().put(HubJiraConstants.ISSUE_PROPERTY_ENTITY_NAME, JIRA_PROJECT_ID,
        // entityProperty.getKey(),
        // entityProperty.getValue());
    }

    private Issue createValidIssue() {
        final StatusMock status = new StatusMock();
        status.setName(STATUS_NAME);
        final ApplicationUserMock assignee = new ApplicationUserMock();
        assignee.setName(ASSIGNEE_USER_NAME);
        return createIssue(new Long(1), JIRA_PROJECT_ID, JIRA_PROJECT_NAME, status, assignee);
    }

    private void assertIssueCreated(final Long eventTypeId) {
        final Issue issue = createValidIssue();
        final IssueEvent event = createIssueEvent(issue, eventTypeId);
        listener.onIssueEvent(event);

        assertFalse(issueServiceMock.issueMap.isEmpty());

        final IssueView hubIssue = issueServiceMock.issueMap.get(ISSUE_URL);

        final SimpleDateFormat dateFormatter = new SimpleDateFormat(RestConnection.JSON_DATE_FORMAT);
        dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String expectedCreatedAt = dateFormatter.format(issue.getCreated());
        final String expectedUpdatedAt = dateFormatter.format(issue.getUpdated());

        assertEquals(issue.getKey(), hubIssue.issueId);
        assertEquals(issue.getDescription(), hubIssue.issueDescription);
        assertEquals(issue.getStatus().getName(), hubIssue.issueStatus);
        assertEquals(expectedCreatedAt, hubIssue.issueCreatedAt);
        assertEquals(expectedUpdatedAt, hubIssue.issueUpdatedAt);
        assertEquals(issue.getAssignee().getDisplayName(), hubIssue.issueAssignee);
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
        final Issue issue = createIssue(new Long(1), new Long(1), JIRA_PROJECT_NAME, new StatusMock(), new ApplicationUserMock());
        final IssueEvent event = createIssueEvent(issue, EventType.ISSUE_CREATED_ID);
        listener.onIssueEvent(event);
        assertTrue(issueServiceMock.issueMap.isEmpty());
    }

    @Test
    public void testEmptyProjectMapping() {
        final Issue issue = createIssue(new Long(1), new Long(1), JIRA_PROJECT_NAME, new StatusMock(), new ApplicationUserMock());
        final IssueEvent event = createIssueEvent(issue, EventType.ISSUE_UPDATED_ID);
        listener.onIssueEvent(event);
        assertTrue(issueServiceMock.issueMap.isEmpty());
    }

    @Test
    public void testOldHubVersion() {
        final VersionComparison versionComparison = new VersionComparison("3.7.0", "3.6.0", 1, ">");
        versionRequestServiceMock.setHubVersionComparison(versionComparison);
        final Issue issue = createIssue(new Long(1), new Long(1), JIRA_PROJECT_NAME, new StatusMock(), new ApplicationUserMock());
        final IssueEvent event = createIssueEvent(issue, EventType.ISSUE_UPDATED_ID);
        listener.onIssueEvent(event);
        assertTrue(issueServiceMock.issueMap.isEmpty());
    }

    @Test
    public void testEventType() {
        final Issue issue = createIssue(new Long(1), new Long(1), JIRA_PROJECT_NAME, new StatusMock(), new ApplicationUserMock());
        final IssueEvent event = createIssueEvent(issue, EventType.ISSUE_UPDATED_ID);
        listener.onIssueEvent(event);
        assertTrue(issueServiceMock.issueMap.isEmpty());
    }

    @Test
    public void testUpdateEventWithJiraProjectNotMapped() {
        populateProjectSettings();
        final Issue issue = createIssue(new Long(1), new Long(999), JIRA_PROJECT_NAME, new StatusMock(), new ApplicationUserMock());
        final IssueEvent event = createIssueEvent(issue, EventType.ISSUE_UPDATED_ID);
        listener.onIssueEvent(event);
        assertTrue(issueServiceMock.issueMap.isEmpty());
    }

    @Test
    public void testUpdateEventWithNullEntityProperty() {
        populateProjectSettings();
        final Issue issue = createIssue(new Long(1), JIRA_PROJECT_ID, JIRA_PROJECT_NAME, new StatusMock(), new ApplicationUserMock());
        final IssueEvent event = createIssueEvent(issue, EventType.ISSUE_UPDATED_ID);
        listener.onIssueEvent(event);
        assertTrue(issueServiceMock.issueMap.isEmpty());
    }

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

    @Test
    public void testUpdateEventWithEntityProperty() {
        populateProjectSettings();
        createEntityProperty();
        assertIssueCreated(EventType.ISSUE_UPDATED_ID);
    }

    @Test
    public void testDeleteEventWithEntityProperty() {
        populateProjectSettings();
        createEntityProperty();

        final StatusMock status = new StatusMock();
        status.setName(STATUS_NAME);
        final ApplicationUserMock assignee = new ApplicationUserMock();
        assignee.setName(ASSIGNEE_USER_NAME);
        final Issue issue = createIssue(new Long(1), JIRA_PROJECT_ID, JIRA_PROJECT_NAME, status, assignee);
        final IssueEvent event = createIssueEvent(issue, EventType.ISSUE_DELETED_ID);

        final SimpleDateFormat dateFormatter = new SimpleDateFormat(RestConnection.JSON_DATE_FORMAT);
        dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String expectedCreatedAt = dateFormatter.format(issue.getCreated());
        final String expectedUpdatedAt = dateFormatter.format(issue.getUpdated());
        final IssueView hubIssue = new IssueView();
        hubIssue.issueId = issue.getKey();
        hubIssue.issueDescription = issue.getDescription();
        hubIssue.issueStatus = issue.getStatus().getName();
        hubIssue.issueCreatedAt = expectedCreatedAt;
        hubIssue.issueUpdatedAt = expectedUpdatedAt;
        hubIssue.issueAssignee = issue.getAssignee().getDisplayName();

        issueServiceMock.issueMap.put(ISSUE_URL, hubIssue);
        listener.onIssueEvent(event);

        assertTrue(issueServiceMock.issueMap.isEmpty());
    }
}
