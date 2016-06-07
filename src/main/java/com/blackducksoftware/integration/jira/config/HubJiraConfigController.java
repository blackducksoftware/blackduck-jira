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

import java.util.ArrayList;
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

import org.apache.commons.lang.StringUtils;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
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
	private final ProjectManager projectManager;

	public HubJiraConfigController(final UserManager userManager, final PluginSettingsFactory pluginSettingsFactory,
			final TransactionTemplate transactionTemplate, final ProjectManager projectManager) {
		this.userManager = userManager;
		this.pluginSettingsFactory = pluginSettingsFactory;
		this.transactionTemplate = transactionTemplate;
		this.projectManager = projectManager;
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

				final List<JiraProject> jiraProjects = getJiraProjects(projectManager.getProjectObjects());
				final List<HubProject> hubProjects = getHubProjects(settings);

				final HubJiraConfigSerializable config = new HubJiraConfigSerializable();

				config.setJiraProjects(jiraProjects);
				config.setHubProjects(hubProjects);

				final String intervalBetweenChecks = getStringValue(settings,
						HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS);

				final String hubProjectMappingsJson = getStringValue(settings,
						HubJiraConfigKeys.HUB_CONFIG_JIRA_PROJECT_MAPPINGS_JSON);

				config.setIntervalBetweenChecks(intervalBetweenChecks);
				config.setHubProjectMappingsJson(hubProjectMappingsJson);

				checkConfigErrors(config);
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

				setValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_PROJECT_MAPPINGS_JSON,
						config.getHubProjectMappingsJson());

				final List<JiraProject> jiraProjects = getJiraProjects(projectManager.getProjectObjects());
				final List<HubProject> hubProjects = getHubProjects(settings);

				config.setJiraProjects(jiraProjects);
				config.setHubProjects(hubProjects);

				checkConfigErrors(config);
				return null;
			}
		});
		if (config.hasErrors()) {
			return Response.ok(config).status(Status.BAD_REQUEST).build();
		}

		return Response.noContent().build();
	}

	private void checkConfigErrors(final HubJiraConfigSerializable config) {
		if (config.getIntervalBetweenChecks() == null) {
			config.setIntervalBetweenChecksError("No interval between checks was found.");
		} else {
			try {
				stringToInteger(config.getIntervalBetweenChecks());
			} catch (final IllegalArgumentException e) {
				config.setIntervalBetweenChecksError(e.getMessage());
			}
		}
		if (config.getHubProjectMappings() != null && !config.getHubProjectMappings().isEmpty()) {
			for (final HubProjectMapping mapping : config.getHubProjectMappings()) {
				boolean jiraProjectExists = false;
				if (mapping.getJiraProject() != null) {
					if (mapping.getJiraProject().getProjectId() != null) {
						for (final JiraProject jiraProject : config.getJiraProjects()) {
							if (jiraProject.getProjectId().equals(mapping.getJiraProject().getProjectId())) {
								jiraProjectExists = true;
								break;
							}
						}
					}
					if (jiraProjectExists) {
						mapping.getJiraProject().setProjectExists(true);
					} else {
						mapping.getJiraProject().setProjectExists(false);
					}
				}

				boolean hubProjectExists = false;
				if (mapping.getHubProject() != null) {
					if (StringUtils.isNotBlank(mapping.getHubProject().getProjectUrl())) {
						for (final HubProject hubProject : config.getHubProjects()) {
							if (hubProject.getProjectUrl().equals(mapping.getHubProject().getProjectUrl())) {
								hubProjectExists = true;
								break;
							}
						}
					}
					if (hubProjectExists) {
						mapping.getHubProject().setProjectExists(true);
					} else {
						mapping.getHubProject().setProjectExists(false);
					}
				}
			}
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
			settings.put(key, value);
		}
	}

	private int stringToInteger(final String integer) throws IllegalArgumentException {
		try {
			return Integer.valueOf(integer);
		} catch (final NumberFormatException e) {
			throw new IllegalArgumentException("The String : " + integer + " , is not an Integer.", e);
		}
	}

	private List<JiraProject> getJiraProjects(final List<Project> jiraProjects) {
		final List<JiraProject> newJiraProjects = new ArrayList<JiraProject>();
		for (final Project oldProject : jiraProjects) {
			final JiraProject newProject = new JiraProject();
			newProject.setProjectName(oldProject.getName());
			newProject.setProjectId(oldProject.getId());
			newProject.setProjectExists(true);
			newJiraProjects.add(newProject);
		}
		return newJiraProjects;
	}

	private List<HubProject> getHubProjects(final PluginSettings settings) {
		final List<HubProject> hubProjects = new ArrayList<HubProject>();
		final HubProject project1 = new HubProject();
		project1.setProjectName("HubProject 1");
		project1.setProjectUrl("Project URL 1");
		project1.setProjectExists(true);

		final HubProject project2 = new HubProject();
		project2.setProjectName("HubProject 2");
		project2.setProjectUrl("Project URL 2");
		project2.setProjectExists(true);

		final HubProject project3 = new HubProject();
		project3.setProjectName("HubProject 3");
		project3.setProjectUrl("Project URL 3");
		project3.setProjectExists(false);

		hubProjects.add(project1);
		hubProjects.add(project2);
		hubProjects.add(project3);
		return hubProjects;
	}
}
