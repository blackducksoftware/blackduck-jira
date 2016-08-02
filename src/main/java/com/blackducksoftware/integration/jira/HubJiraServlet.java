/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
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
 *******************************************************************************/
package com.blackducksoftware.integration.jira;

import java.io.IOException;
import java.net.URI;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.blackducksoftware.integration.jira.utils.HubJiraConstants;

public class HubJiraServlet extends HttpServlet {

	private static final long serialVersionUID = 8293922701957754642L;

	private final UserManager userManager;
	private final LoginUriProvider loginUriProvider;
	private final TemplateRenderer renderer;

	public HubJiraServlet(final UserManager userManager, final LoginUriProvider loginUriProvider,
			final TemplateRenderer renderer) {
		this.userManager = userManager;
		this.loginUriProvider = loginUriProvider;
		this.renderer = renderer;
	}

	@Override
	public void doGet(final HttpServletRequest request, final HttpServletResponse response)
			throws IOException, ServletException {
		final String username = userManager.getRemoteUsername(request);
		if (username == null
				|| (!userManager.isSystemAdmin(username)
						&& !userManager.isUserInGroup(username, HubJiraConstants.HUB_JIRA_GROUP))) {
			redirectToLogin(request, response);
			return;
		}

		response.setContentType("text/html;charset=utf-8");
		renderer.render("hub-jira.vm", response.getWriter());
	}

	private void redirectToLogin(final HttpServletRequest request, final HttpServletResponse response)
			throws IOException {
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