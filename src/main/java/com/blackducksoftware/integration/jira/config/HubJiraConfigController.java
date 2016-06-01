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
package com.blackducksoftware.integration.jira.config;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
import com.blackducksoftware.integration.jira.utils.HubJiraConfigKeys;

@Path("/")
public class HubJiraConfigController {
	private final UserManager userManager;
	private final PluginSettingsFactory pluginSettingsFactory;
	private final TransactionTemplate transactionTemplate;

	public HubJiraConfigController(final UserManager userManager, final PluginSettingsFactory pluginSettingsFactory,
			final TransactionTemplate transactionTemplate) {
		this.userManager = userManager;
		this.pluginSettingsFactory = pluginSettingsFactory;
		this.transactionTemplate = transactionTemplate;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@Context final HttpServletRequest request) {
		final String username = userManager.getRemoteUsername(request);
		if (username == null || !userManager.isSystemAdmin(username)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}

		final Object obj = transactionTemplate.execute(new TransactionCallback() {
			@Override
			public Object doInTransaction() {
				final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();

				final HubJiraConfigSerializable config = new HubJiraConfigSerializable();

				config.setCheckHowOften(getValue(settings, HubJiraConfigKeys.CONFIG_HUB_HOW_OFTEN_TO_CHECK));

				return config;
			}
		});

		return Response.ok(obj).build();
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response put(final HubJiraConfigSerializable config, @Context final HttpServletRequest request) {
		final String username = userManager.getRemoteUsername(request);
		if (username == null || !userManager.isSystemAdmin(username)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}

		transactionTemplate.execute(new TransactionCallback() {
			@Override
			public Object doInTransaction() {
				final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();

				setValue(settings, HubJiraConfigKeys.CONFIG_HUB_HOW_OFTEN_TO_CHECK, config.getCheckHowOften());

				return null;
			}
		});

		return Response.noContent().build();
	}

	private String getValue(final PluginSettings settings, final String key) {
		return (String) settings.get(key);
	}

	private void setValue(final PluginSettings settings, final String key, final Object value) {
		if (value == null) {
			settings.remove(key);
		} else {
			settings.put(key, String.valueOf(value));
		}
	}

}
