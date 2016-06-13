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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
import com.blackducksoftware.integration.atlassian.utils.HubConfigKeys;
import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.builder.HubProxyInfoBuilder;
import com.blackducksoftware.integration.hub.builder.ValidationResults;
import com.blackducksoftware.integration.hub.encryption.PasswordDecrypter;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.EncryptionException;
import com.blackducksoftware.integration.hub.exception.ResourceDoesNotExistException;
import com.blackducksoftware.integration.hub.global.GlobalFieldKey;
import com.blackducksoftware.integration.hub.global.HubProxyInfo;
import com.blackducksoftware.integration.hub.item.HubItemsService;
import com.blackducksoftware.integration.hub.policy.api.PolicyRule;
import com.blackducksoftware.integration.hub.project.api.ProjectItem;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.jira.utils.HubJiraConfigKeys;
import com.google.gson.reflect.TypeToken;

@Path("/")
public class HubJiraConfigController {
	public static final String HUB_SERVER_MISCONFIGURATION = "There was a problem with the Hub Server configuration. ";
	public static final String CHECK_HUB_SERVER_CONFIGURATION = "Please verify the Hub Server information is configured correctly. ";
	public static final String HUB_CONFIG_PLUGIN_MISSING = "Could not find the Hub Server configuration. Please verify the correct dependent Hub configuration plugin is installed. ";
	public static final String MAPPING_HAS_EMPTY_ERROR = "There are invalid mapping(s) with empty Project(s).";

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

				final HubJiraConfigSerializable config = new HubJiraConfigSerializable();

				final HubIntRestService restService = getHubRestService(settings, config);
				if (restService != null) {
					final List<HubProject> hubProjects = getHubProjects(restService, config);
					config.setHubProjects(hubProjects);
				}

				config.setJiraProjects(jiraProjects);

				final String intervalBetweenChecks = getStringValue(settings,
						HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS);

				final String policyRulesJson = getStringValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_POLICY_RULES);

				final String hubProjectMappingsJson = getStringValue(settings,
						HubJiraConfigKeys.HUB_CONFIG_JIRA_PROJECT_MAPPINGS_JSON);

				config.setIntervalBetweenChecks(intervalBetweenChecks);
				config.setHubProjectMappingsJson(hubProjectMappingsJson);

				if (StringUtils.isNotBlank(policyRulesJson)) {
					config.setPolicyRulesJson(policyRulesJson);
				}
				setHubPolicyRules(restService.getRestConnection(),
						config);
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

				final List<JiraProject> jiraProjects = getJiraProjects(projectManager.getProjectObjects());

				final HubIntRestService restService = getHubRestService(settings, config);
				if (restService != null) {
					final List<HubProject> hubProjects = getHubProjects(restService, config);
					config.setHubProjects(hubProjects);
				}

				config.setJiraProjects(jiraProjects);

				checkConfigErrors(config);

				if (getValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_FIRST_SAVE_TIME) == null) {
					setValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_FIRST_SAVE_TIME, new DateTime().toString());
				}

				setValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS,
						config.getIntervalBetweenChecks());
				setValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_POLICY_RULES,
						config.getPolicyRulesJson());
				setValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_PROJECT_MAPPINGS_JSON,
						config.getHubProjectMappingsJson());

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

			boolean hasEmptyMapping = false;
			for (final HubProjectMapping mapping : config.getHubProjectMappings()) {

				boolean jiraProjectBlank = true;
				boolean hubProjectBlank = true;

				boolean jiraProjectExists = false;
				if (mapping.getJiraProject() != null) {
					if (mapping.getJiraProject().getProjectId() != null) {
						jiraProjectBlank = false;
						if (config.getJiraProjects() != null && !config.getJiraProjects().isEmpty()) {
							for (final JiraProject jiraProject : config.getJiraProjects()) {
								if (jiraProject.getProjectId().equals(mapping.getJiraProject().getProjectId())) {
									jiraProjectExists = true;
									break;
								}
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
						hubProjectBlank = false;
						if (config.getHubProjects() != null && !config.getHubProjects().isEmpty()) {
							for (final HubProject hubProject : config.getHubProjects()) {
								if (hubProject.getProjectUrl().equals(mapping.getHubProject().getProjectUrl())) {
									mapping.getHubProject().setProjectName(hubProject.getProjectName());
									hubProjectExists = true;
									break;
								}
							}
						}
					} else if (StringUtils.isNotBlank(mapping.getHubProject().getProjectName())) {
						hubProjectBlank = false;
						if (config.getHubProjects() != null && !config.getHubProjects().isEmpty()) {
							for (final HubProject hubProject : config.getHubProjects()) {
								if (hubProject.getProjectName().equals(mapping.getHubProject().getProjectName())) {
									mapping.getHubProject().setProjectUrl(hubProject.getProjectUrl());
									hubProjectExists = true;
									break;
								}
							}
						}
					}
					if (hubProjectExists) {
						mapping.getHubProject().setProjectExists(true);
					} else {
						mapping.getHubProject().setProjectExists(false);
					}
				}

				if(jiraProjectBlank || hubProjectBlank){
					hasEmptyMapping = true;
				}
			}
			if(hasEmptyMapping){
				String errorMsg = "";
				if (StringUtils.isNotBlank(config.getHubProjectMappingError())) {
					errorMsg = config.getHubProjectMappingError();
					errorMsg += " ";
				}
				errorMsg += MAPPING_HAS_EMPTY_ERROR;
				config.setHubProjectMappingError(errorMsg);
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

	private HubIntRestService getHubRestService(final PluginSettings settings, final HubJiraConfigSerializable config){
		final String hubUrl = getStringValue(settings, HubConfigKeys.CONFIG_HUB_URL);
		final String hubUser = getStringValue(settings, HubConfigKeys.CONFIG_HUB_USER);
		final String encHubPassword = getStringValue(settings, HubConfigKeys.CONFIG_HUB_PASS);
		final String hubTimeout = getStringValue(settings, HubConfigKeys.CONFIG_HUB_TIMEOUT);

		if (StringUtils.isBlank(hubUrl) && StringUtils.isBlank(hubUser) && StringUtils.isBlank(encHubPassword)
				&& StringUtils.isBlank(hubTimeout)) {
			config.setHubProjectMappingError(HUB_CONFIG_PLUGIN_MISSING);
			return null;
		} else if (StringUtils.isBlank(hubUrl) || StringUtils.isBlank(hubUser) || StringUtils.isBlank(encHubPassword)
				|| StringUtils.isBlank(hubTimeout)) {
			config.setHubProjectMappingError(HUB_SERVER_MISCONFIGURATION + CHECK_HUB_SERVER_CONFIGURATION);
			return null;
		}

		final String hubProxyHost = getStringValue(settings, HubConfigKeys.CONFIG_PROXY_HOST);
		final String hubProxyPort = getStringValue(settings, HubConfigKeys.CONFIG_PROXY_PORT);
		final String hubNoProxyHost = getStringValue(settings, HubConfigKeys.CONFIG_PROXY_NO_HOST);
		final String hubProxyUser = getStringValue(settings, HubConfigKeys.CONFIG_PROXY_USER);
		final String encHubProxyPassword = getStringValue(settings, HubConfigKeys.CONFIG_PROXY_PASS);
		final String hubProxyPasswordLength = getStringValue(settings, HubConfigKeys.CONFIG_PROXY_PASS_LENGTH);

		HubIntRestService hubRestService = null;
		try{
			final RestConnection restConnection = new RestConnection(hubUrl);
			final String hubPassword = PasswordDecrypter.decrypt(encHubPassword);
			restConnection.setTimeout(NumberUtils.toInt(hubTimeout));

			final HubProxyInfoBuilder proxyBuilder = new HubProxyInfoBuilder(true);
			proxyBuilder.setHost(hubProxyHost);
			proxyBuilder.setPort(hubProxyPort);
			proxyBuilder.setIgnoredProxyHosts(hubNoProxyHost);
			proxyBuilder.setUsername(hubProxyUser);
			proxyBuilder.setPassword(encHubProxyPassword);
			proxyBuilder.setPasswordLength(NumberUtils.toInt(hubProxyPasswordLength));
			final ValidationResults<GlobalFieldKey, HubProxyInfo> result = proxyBuilder.build();
			final HubProxyInfo proxyInfo = result.getConstructedObject();

			restConnection.setProxyProperties(proxyInfo);

			restConnection.setCookies(hubUser, hubPassword);
			hubRestService = new HubIntRestService(restConnection);

		} catch (BDRestException | IllegalArgumentException | URISyntaxException | EncryptionException e) {
			config.setHubProjectMappingError(CHECK_HUB_SERVER_CONFIGURATION + " :: " + e.getMessage());
			return null;
		}
		return hubRestService;
	}

	private List<HubProject> getHubProjects(final HubIntRestService hubRestService,
			final HubJiraConfigSerializable config) {
		final List<HubProject> hubProjects = new ArrayList<HubProject>();
		List<ProjectItem> hubProjectItems = null;
		try {
			hubProjectItems = hubRestService.getProjectMatches(null);
		} catch (IOException | BDRestException | URISyntaxException e) {
			final String originalError = config.getHubProjectMappingError();
			String newError = null;
			if (StringUtils.isNotBlank(originalError)) {
				newError = originalError + " :: ";
			}
			newError += e.getMessage();
			config.setHubProjectMappingError(newError);
		}

		if (hubProjectItems != null && !hubProjectItems.isEmpty()) {
			for (final ProjectItem project : hubProjectItems) {
				final HubProject newHubProject = new HubProject();
				newHubProject.setProjectExists(true);
				newHubProject.setProjectName(project.getName());
				newHubProject.setProjectUrl(project.get_meta().getHref());
				hubProjects.add(newHubProject);
			}
		}
		return hubProjects;
	}

	private void setHubPolicyRules(final RestConnection restConnection,
			final HubJiraConfigSerializable config) {
		final TypeToken<PolicyRule> typeToken = new TypeToken<PolicyRule>() {
		};
		final HubItemsService<PolicyRule> policyService = new HubItemsService<PolicyRule>(
				restConnection, PolicyRule.class, typeToken);

		final List<String> urlSegments = new ArrayList<>();
		urlSegments.add("api");
		urlSegments.add("notifications");

		final Set<AbstractMap.SimpleEntry<String, String>> queryParameters = new HashSet<>();
		queryParameters.add(new AbstractMap.SimpleEntry<String, String>("limit", String.valueOf(Integer.MAX_VALUE)));

		List<PolicyRule> policyRules = null;
		try {
			policyRules = policyService.httpGetItemList(urlSegments, queryParameters);
		} catch (IOException | URISyntaxException | ResourceDoesNotExistException | BDRestException e) {
			config.setPolicyRulesError(e.getMessage());
		}

		final List<PolicyRuleSerializable> newPolicyRules = new ArrayList<PolicyRuleSerializable>();
		if (policyRules != null && !policyRules.isEmpty()) {
			for (final PolicyRule rule : policyRules) {
				final PolicyRuleSerializable newRule = new PolicyRuleSerializable();
				newRule.setConditions(rule.getExpression());
				newRule.setDescription(rule.getDescription());
				newRule.setName(rule.getName());
				newRule.setPolicyUrl(rule.getMeta().getHref());
			}
		}
		if (config.getPolicyRules() != null) {
			for (final PolicyRuleSerializable oldRule : config.getPolicyRules()) {
				for (final PolicyRuleSerializable newRule : newPolicyRules) {
					if (oldRule.getName().equals(newRule.getName())) {
						newRule.setChecked(oldRule.isChecked());
						break;
					}
				}
			}
		}
		config.setPolicyRules(newPolicyRules);
	}
}
