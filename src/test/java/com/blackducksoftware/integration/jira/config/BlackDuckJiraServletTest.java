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

import org.junit.Test;

import com.blackducksoftware.integration.jira.config.controller.BlackDuckJiraServlet;
import com.blackducksoftware.integration.jira.mocks.HttpServletRequestMock;
import com.blackducksoftware.integration.jira.mocks.HttpServletResponseMock;
import com.blackducksoftware.integration.jira.mocks.LoginUriProviderMock;
import com.blackducksoftware.integration.jira.mocks.PluginSettingsFactoryMock;
import com.blackducksoftware.integration.jira.mocks.TemplateRendererMock;
import com.blackducksoftware.integration.jira.mocks.UserManagerUIMock;

public class BlackDuckJiraServletTest {
    private static final String TEMPLATE_NAME = "blackduck-jira.vm";

    @Test
    public void testDoGetUserNull() throws Exception {
        final String redirectUrl = "http://testRedirect";
        final StringBuffer requestUrl = new StringBuffer();
        requestUrl.append(redirectUrl);

        final UserManagerUIMock managerMock = new UserManagerUIMock();
        final LoginUriProviderMock loginProviderMock = new LoginUriProviderMock();
        final TemplateRendererMock rendererMock = new TemplateRendererMock();
        final HttpServletResponseMock responseMock = new HttpServletResponseMock();
        final PluginSettingsFactoryMock pluginSettingsFactory = new PluginSettingsFactoryMock();
        final HttpServletRequestMock requestMock = new HttpServletRequestMock();
        requestMock.setRequestURL(requestUrl);

        final BlackDuckJiraServlet servlet = new BlackDuckJiraServlet(managerMock, loginProviderMock, rendererMock, pluginSettingsFactory);
        servlet.doGet(requestMock, responseMock);

        assertEquals(redirectUrl, responseMock.getRedirectedLocation());
    }

    @Test
    public void testDoGetUserNotAdmin() throws Exception {
        final String userName = "TestUser";
        final String redirectUrl = "http://testRedirect";
        final StringBuffer requestUrl = new StringBuffer();
        requestUrl.append(redirectUrl);

        final UserManagerUIMock managerMock = new UserManagerUIMock();
        managerMock.setRemoteUsername(userName);

        final LoginUriProviderMock loginProviderMock = new LoginUriProviderMock();
        final TemplateRendererMock rendererMock = new TemplateRendererMock();
        final HttpServletResponseMock responseMock = new HttpServletResponseMock();
        final PluginSettingsFactoryMock pluginSettingsFactory = new PluginSettingsFactoryMock();
        final HttpServletRequestMock requestMock = new HttpServletRequestMock();
        requestMock.setRequestURL(requestUrl);

        final BlackDuckJiraServlet servlet = new BlackDuckJiraServlet(managerMock, loginProviderMock, rendererMock, pluginSettingsFactory);
        servlet.doGet(requestMock, responseMock);

        assertEquals(redirectUrl, responseMock.getRedirectedLocation());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testDoGetUserNotAdminNotInOldGroup() throws Exception {
        final String userName = "TestUser";
        final String redirectUrl = "http://testRedirect";
        final StringBuffer requestUrl = new StringBuffer();
        requestUrl.append(redirectUrl);

        final UserManagerUIMock managerMock = new UserManagerUIMock();
        managerMock.setRemoteUsername(userName);
        managerMock.addGroup("Group3");

        final LoginUriProviderMock loginProviderMock = new LoginUriProviderMock();
        final TemplateRendererMock rendererMock = new TemplateRendererMock();
        final HttpServletResponseMock responseMock = new HttpServletResponseMock();
        final PluginSettingsFactoryMock pluginSettingsFactory = new PluginSettingsFactoryMock();
        pluginSettingsFactory.createGlobalSettings().put(PluginConfigKeys.HUB_CONFIG_JIRA_GROUPS, "Group1, Group2");

        final HttpServletRequestMock requestMock = new HttpServletRequestMock();
        requestMock.setRequestURL(requestUrl);

        final BlackDuckJiraServlet servlet = new BlackDuckJiraServlet(managerMock, loginProviderMock, rendererMock, pluginSettingsFactory);
        servlet.doGet(requestMock, responseMock);

        assertEquals(redirectUrl, responseMock.getRedirectedLocation());
    }

    @Test
    public void testDoGetUserNotAdminNotInGroup() throws Exception {
        final String userName = "TestUser";
        final String redirectUrl = "http://testRedirect";
        final StringBuffer requestUrl = new StringBuffer();
        requestUrl.append(redirectUrl);

        final UserManagerUIMock managerMock = new UserManagerUIMock();
        managerMock.setRemoteUsername(userName);
        managerMock.addGroup("Group3");

        final LoginUriProviderMock loginProviderMock = new LoginUriProviderMock();
        final TemplateRendererMock rendererMock = new TemplateRendererMock();
        final HttpServletResponseMock responseMock = new HttpServletResponseMock();
        final PluginSettingsFactoryMock pluginSettingsFactory = new PluginSettingsFactoryMock();
        pluginSettingsFactory.createGlobalSettings().put(PluginConfigKeys.HUB_CONFIG_GROUPS, "Group1, Group2");

        final HttpServletRequestMock requestMock = new HttpServletRequestMock();
        requestMock.setRequestURL(requestUrl);

        final BlackDuckJiraServlet servlet = new BlackDuckJiraServlet(managerMock, loginProviderMock, rendererMock,
                pluginSettingsFactory);

        servlet.doGet(requestMock, responseMock);

        assertEquals(redirectUrl, responseMock.getRedirectedLocation());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testDoGetUserNotAdminInOldGroup() throws Exception {
        final String userName = "TestUser";
        final String redirectUrl = "http://testRedirect";
        final StringBuffer requestUrl = new StringBuffer();
        requestUrl.append(redirectUrl);

        final UserManagerUIMock managerMock = new UserManagerUIMock();
        managerMock.setRemoteUsername(userName);
        managerMock.addGroup("Group1");

        final LoginUriProviderMock loginProviderMock = new LoginUriProviderMock();
        final TemplateRendererMock rendererMock = new TemplateRendererMock();
        final HttpServletResponseMock responseMock = new HttpServletResponseMock();
        final PluginSettingsFactoryMock pluginSettingsFactory = new PluginSettingsFactoryMock();
        pluginSettingsFactory.createGlobalSettings().put(PluginConfigKeys.HUB_CONFIG_JIRA_GROUPS, "Group1, Group2");

        final HttpServletRequestMock requestMock = new HttpServletRequestMock();
        requestMock.setRequestURL(requestUrl);

        final BlackDuckJiraServlet servlet = new BlackDuckJiraServlet(managerMock, loginProviderMock, rendererMock,
                pluginSettingsFactory);

        servlet.doGet(requestMock, responseMock);

        assertEquals("text/html;charset=utf-8", responseMock.getContentType());
        assertEquals(TEMPLATE_NAME, rendererMock.getRenderedString());
    }

    @Test
    public void testDoGetUserNotAdminInGroup() throws Exception {
        final String userName = "TestUser";
        final String redirectUrl = "http://testRedirect";
        final StringBuffer requestUrl = new StringBuffer();
        requestUrl.append(redirectUrl);

        final UserManagerUIMock managerMock = new UserManagerUIMock();
        managerMock.setRemoteUsername(userName);
        managerMock.addGroup("Group1");

        final LoginUriProviderMock loginProviderMock = new LoginUriProviderMock();
        final TemplateRendererMock rendererMock = new TemplateRendererMock();
        final HttpServletResponseMock responseMock = new HttpServletResponseMock();
        final PluginSettingsFactoryMock pluginSettingsFactory = new PluginSettingsFactoryMock();
        pluginSettingsFactory.createGlobalSettings().put(PluginConfigKeys.HUB_CONFIG_GROUPS, "Group1, Group2");

        final HttpServletRequestMock requestMock = new HttpServletRequestMock();
        requestMock.setRequestURL(requestUrl);

        final BlackDuckJiraServlet servlet = new BlackDuckJiraServlet(managerMock, loginProviderMock, rendererMock, pluginSettingsFactory);
        servlet.doGet(requestMock, responseMock);

        assertEquals("text/html;charset=utf-8", responseMock.getContentType());
        assertEquals(TEMPLATE_NAME, rendererMock.getRenderedString());
    }

    @Test
    public void testDoGetUserAdmin() throws Exception {
        final String userName = "TestUser";
        final String redirectUrl = "http://testRedirect";
        final StringBuffer requestUrl = new StringBuffer();
        requestUrl.append(redirectUrl);

        final UserManagerUIMock managerMock = new UserManagerUIMock();
        managerMock.setRemoteUsername(userName);
        managerMock.setIsSystemAdmin(true);

        final LoginUriProviderMock loginProviderMock = new LoginUriProviderMock();
        final TemplateRendererMock rendererMock = new TemplateRendererMock();
        final HttpServletResponseMock responseMock = new HttpServletResponseMock();
        final PluginSettingsFactoryMock pluginSettingsFactory = new PluginSettingsFactoryMock();
        final HttpServletRequestMock requestMock = new HttpServletRequestMock();
        requestMock.setRequestURL(requestUrl);

        final BlackDuckJiraServlet servlet = new BlackDuckJiraServlet(managerMock, loginProviderMock, rendererMock, pluginSettingsFactory);
        servlet.doGet(requestMock, responseMock);

        assertEquals("text/html;charset=utf-8", responseMock.getContentType());
        assertEquals(TEMPLATE_NAME, rendererMock.getRenderedString());
    }
}
