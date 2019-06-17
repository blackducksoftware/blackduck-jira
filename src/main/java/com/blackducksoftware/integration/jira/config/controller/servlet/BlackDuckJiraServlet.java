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
package com.blackducksoftware.integration.jira.config.controller.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;

public class BlackDuckJiraServlet extends HttpServlet {
    private static final long serialVersionUID = 8293922701957754642L;

    private final TemplateRenderer renderer;
    private final LoginRedirect loginRedirect;

    public BlackDuckJiraServlet(final UserManager userManager, final LoginUriProvider loginUriProvider, final TemplateRenderer renderer, final PluginSettingsFactory pluginSettingsFactory) {
        this.renderer = renderer;
        this.loginRedirect = new LoginRedirect(loginUriProvider, userManager, pluginSettingsFactory);
    }

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        if (loginRedirect.redirectIfUnauthorized(request, response)) {
            return;
        }
        response.setContentType("text/html;charset=utf-8");
        renderer.render("templates/blackduck-jira.vm", response.getWriter());
    }

}
