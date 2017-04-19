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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.user.ApplicationUser;
import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.jira.common.HubJiraConfigKeys;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.common.HubProjectMapping;
import com.blackducksoftware.integration.jira.config.HubConfigKeys;
import com.blackducksoftware.integration.jira.mocks.ApplicationUserMock;
import com.blackducksoftware.integration.jira.mocks.BomComponentIssueServiceMock;
import com.blackducksoftware.integration.jira.mocks.EventPublisherMock;
import com.blackducksoftware.integration.jira.mocks.JiraServicesMock;
import com.blackducksoftware.integration.jira.mocks.PluginSettingsFactoryMock;
import com.blackducksoftware.integration.jira.mocks.PluginSettingsMock;
import com.blackducksoftware.integration.jira.mocks.issue.IssueMock;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class IssueEventListenerTest {

    private static final String JIRA_USER = "auser";

    private final EventPublisherMock eventPublisher = new EventPublisherMock();

    private IssueEventListener listener;

    private PluginSettingsMock settings;

    private PluginSettingsFactoryMock pluginSettingsFactory;

    private JiraServicesMock jiraServices;

    @Before
    public void initTest() throws MalformedURLException {
        settings = createPluginSettings();
        pluginSettingsFactory = new PluginSettingsFactoryMock(settings);
        jiraServices = new JiraServicesMock();
        final URL url = new URL("http://www.google.com");
        final RestConnection restConnection = new CredentialsRestConnection(Mockito.mock(HubJiraLogger.class), url, "", "", 120);

        final BomComponentIssueServiceMock issueServiceMock = new BomComponentIssueServiceMock(restConnection);
        final HubServicesFactory hubServicesFactory = Mockito.mock(HubServicesFactory.class);
        Mockito.when(hubServicesFactory.createBomComponentIssueRequestService(Mockito.any())).thenReturn(issueServiceMock);
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
        settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_ADMIN_USER, "adminuser");

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

    private Issue createIssue(final Long id, final Long projectId) {
        final IssueMock issue = new IssueMock();
        issue.setId(id);
        issue.setProjectId(projectId);
        return issue;
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
    public void testEmptyProjectMapping() {
        final Issue issue = createIssue(new Long(1), new Long(1));
        final IssueEvent event = createIssueEvent(issue, EventType.ISSUE_CREATED_ID);
        listener.onIssueEvent(event);
    }
}
