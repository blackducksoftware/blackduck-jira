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
package com.blackducksoftware.integration.jira.config.controller.servlet;

import java.io.IOException;
import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.user.UserManager;
import com.blackducksoftware.integration.jira.common.settings.GlobalConfigurationAccessor;
import com.blackducksoftware.integration.jira.common.settings.JiraSettingsAccessor;
import com.blackducksoftware.integration.jira.common.settings.model.PluginGroupsConfigModel;
import com.blackducksoftware.integration.jira.config.controller.AuthorizationChecker;

public class LoginRedirect {
    private final LoginUriProvider loginUriProvider;
    private final AuthorizationChecker authorizationChecker;
    private final PluginSettingsFactory pluginSettingsFactory;

    public LoginRedirect(final LoginUriProvider loginUriProvider, final UserManager userManager, final PluginSettingsFactory pluginSettingsFactory) {
        this.loginUriProvider = loginUriProvider;
        this.pluginSettingsFactory = pluginSettingsFactory;
        authorizationChecker = new AuthorizationChecker(userManager);
    }

    public boolean redirectIfUnauthorized(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        final JiraSettingsAccessor jiraSettingsAccessor = new JiraSettingsAccessor(pluginSettingsFactory.createGlobalSettings());
        final GlobalConfigurationAccessor globalConfigurationAccessor = new GlobalConfigurationAccessor(jiraSettingsAccessor);

        final PluginGroupsConfigModel groupsConfig = globalConfigurationAccessor.getGroupsConfig();
        final String[] parsedBlackDuckConfigGroups = (String[]) groupsConfig.getGroups().toArray();
        if (!authorizationChecker.isValidAuthorization(request, parsedBlackDuckConfigGroups)) {
            redirectToLogin(request, response);
            return true;
        }
        return false;
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
