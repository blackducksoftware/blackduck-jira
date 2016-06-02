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

import java.util.List;

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

import org.apache.commons.lang3.StringUtils;

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

				final String intervalBetweenChecks = getStringValue(settings,
						HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS);
				final List<HubProjectMapping> hubProjectMappings = (List<HubProjectMapping>) getValue(settings,
						HubJiraConfigKeys.HUB_CONFIG_JIRA_PROJECT_MAPPINGS);

				setConfigValues(config, intervalBetweenChecks, hubProjectMappings);

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

				setValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS,
						config.getIntervalBetweenChecks());
				setValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_PROJECT_MAPPINGS, config.getHubProjectMappings());

				setConfigValues(config, config.getIntervalBetweenChecks(), config.getHubProjectMappings());

				return null;
			}
		});
		if (config.hasErrors()) {
			return Response.ok(config).status(Status.BAD_REQUEST).build();
		}

		return Response.noContent().build();
	}

	private void setConfigValues(final HubJiraConfigSerializable config, final String intervalBetweenChecks,
			final List<HubProjectMapping> hubProjectMappings) {
		config.setIntervalBetweenChecks(intervalBetweenChecks);
		config.setHubProjectMappings(hubProjectMappings);

		final String intervalString = StringUtils.trimToNull(intervalBetweenChecks);
		if (intervalString == null) {
			config.setIntervalBetweenChecksError("No interval between checks was found.");
		} else {
			try {
				stringToInteger(intervalString);
			} catch (final IllegalArgumentException e) {
				config.setIntervalBetweenChecksError(e.getMessage());
			}
		}
		if (hubProjectMappings == null || hubProjectMappings.isEmpty()) {
			config.setHubProjectMappingError("No project mappings were found.");
		}
	}

	private Object getValue(final PluginSettings settings, final String key) {
		return settings.get(key);
	}

	private String getStringValue(final PluginSettings settings, final String key) {
		return (String) getValue(settings, key);
	}

	private void setValue(final PluginSettings settings, final String key, final Object value) {
		if (value == null) {
			settings.remove(key);
		} else {
			settings.put(key, String.valueOf(value));
		}
	}

	private int stringToInteger(final String integer) throws IllegalArgumentException {
		try {
			return Integer.valueOf(integer);
		} catch (final NumberFormatException e) {
			throw new IllegalArgumentException("The String : " + integer + " , is not an Integer.", e);
		}
	}

}
