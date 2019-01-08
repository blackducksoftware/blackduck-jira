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
package com.blackducksoftware.integration.jira.config.controller;

import java.io.IOException;
import java.net.URI;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.blackducksoftware.integration.jira.config.PluginConfigKeys;

public class BlackDuckJiraServlet extends HttpServlet {
    private static final long serialVersionUID = 8293922701957754642L;

    private final UserManager userManager;
    private final LoginUriProvider loginUriProvider;
    private final TemplateRenderer renderer;
    private final PluginSettingsFactory pluginSettingsFactory;

    public BlackDuckJiraServlet(final UserManager userManager, final LoginUriProvider loginUriProvider, final TemplateRenderer renderer, final PluginSettingsFactory pluginSettingsFactory) {
        this.userManager = userManager;
        this.loginUriProvider = loginUriProvider;
        this.renderer = renderer;
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    private boolean isUserAuthorized(final HttpServletRequest request) {
        final String username = userManager.getRemoteUsername(request);
        if (username == null) {
            return false;
        }
        if (userManager.isSystemAdmin(username)) {
            return true;
        }

        final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        final String blackDuckJiraGroupsString = (String) settings.get(PluginConfigKeys.BLACKDUCK_CONFIG_GROUPS);
        if (StringUtils.isNotBlank(blackDuckJiraGroupsString)) {
            final String[] blackDuckJiraGroups = blackDuckJiraGroupsString.split(",");
            boolean userIsInGroups = false;
            for (final String blackDuckJiraGroup : blackDuckJiraGroups) {
                if (userManager.isUserInGroup(username, blackDuckJiraGroup.trim())) {
                    userIsInGroups = true;
                    break;
                }
            }
            if (userIsInGroups) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        if (!isUserAuthorized(request)) {
            redirectToLogin(request, response);
            return;
        }

        response.setContentType("text/html;charset=utf-8");
        renderer.render("blackduck-jira.vm", response.getWriter());
    }

    private void redirectToLogin(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
    }

    private URI getUri(final HttpServletRequest request) {
        final StringBuffer builder = request.getRequestURL();
        if (request.getQueryString() != null) {
            builder.append("?");
            builder.append(request.getQueryString());
        }
        return URI.create(builder.toString());
    }
}
