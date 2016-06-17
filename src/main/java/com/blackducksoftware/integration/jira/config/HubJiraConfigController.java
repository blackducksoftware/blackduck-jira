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
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
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

import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
import com.blackducksoftware.integration.atlassian.utils.HubConfigKeys;
import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.HubSupportHelper;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.builder.ValidationResults;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.EncryptionException;
import com.blackducksoftware.integration.hub.exception.ResourceDoesNotExistException;
import com.blackducksoftware.integration.hub.global.GlobalFieldKey;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.item.HubItemsService;
import com.blackducksoftware.integration.hub.policy.api.PolicyRule;
import com.blackducksoftware.integration.hub.project.api.ProjectItem;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.jira.utils.HubJiraConfigKeys;
import com.google.gson.reflect.TypeToken;

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

	@Path("/interval")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getInterval(@Context final HttpServletRequest request) {
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

				config.setIntervalBetweenChecks(intervalBetweenChecks);

				checkIntervalErrors(config);
				return config;
			}
		});

		return Response.ok(obj).build();
	}

	@Path("/jiraProjects")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getJiraProjects(@Context final HttpServletRequest request) {
		final String username = userManager.getRemoteUsername(request);
		if (username == null || !userManager.isSystemAdmin(username)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		final Object obj = transactionTemplate.execute(new TransactionCallback() {
			@Override
			public Object doInTransaction() {
				final List<JiraProject> jiraProjects = getJiraProjects(projectManager.getProjectObjects());

				final HubJiraConfigSerializable config = new HubJiraConfigSerializable();

				config.setJiraProjects(jiraProjects);
				return config;
			}
		});
		return Response.ok(obj).build();
	}

	@Path("/hubProjects")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getHubProjects(@Context final HttpServletRequest request) {
		final String username = userManager.getRemoteUsername(request);
		if (username == null || !userManager.isSystemAdmin(username)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		final Object obj = transactionTemplate.execute(new TransactionCallback() {
			@Override
			public Object doInTransaction() {
				final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();

				final HubJiraConfigSerializable config = new HubJiraConfigSerializable();

				final HubIntRestService restService = getHubRestService(settings, config);

				final List<HubProject> hubProjects = getHubProjects(restService, config);
				config.setHubProjects(hubProjects);
				return config;
			}
		});
		return Response.ok(obj).build();
	}

	@Path("/hubPolicies")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getHubPolicies(@Context final HttpServletRequest request) {
		final String username = userManager.getRemoteUsername(request);
		if (username == null || !userManager.isSystemAdmin(username)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		final Object obj = transactionTemplate.execute(new TransactionCallback() {
			@Override
			public Object doInTransaction() {
				final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();

				final String policyRulesJson = getStringValue(settings,
						HubJiraConfigKeys.HUB_CONFIG_JIRA_POLICY_RULES_JSON);

				final HubJiraConfigSerializable config = new HubJiraConfigSerializable();

				final HubIntRestService restService = getHubRestService(settings, config);

				if (StringUtils.isNotBlank(policyRulesJson)) {
					config.setPolicyRulesJson(policyRulesJson);
				}
				setHubPolicyRules(restService, config);
				return config;
			}
		});
		return Response.ok(obj).build();
	}

	@Path("/mappings")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMappings(@Context final HttpServletRequest request) {
		final String username = userManager.getRemoteUsername(request);
		if (username == null || !userManager.isSystemAdmin(username)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		final Object obj = transactionTemplate.execute(new TransactionCallback() {
			@Override
			public Object doInTransaction() {
				final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();

				final HubJiraConfigSerializable config = new HubJiraConfigSerializable();

				final String hubProjectMappingsJson = getStringValue(settings,
						HubJiraConfigKeys.HUB_CONFIG_JIRA_PROJECT_MAPPINGS_JSON);

				config.setHubProjectMappingsJson(hubProjectMappingsJson);

				checkMappingErrors(config);
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
				final List<HubProject> hubProjects = getHubProjects(restService, config);
				config.setHubProjects(hubProjects);
				config.setJiraProjects(jiraProjects);
				checkIntervalErrors(config);
				checkMappingErrors(config);
				if (getValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_FIRST_SAVE_TIME) == null) {
					final SimpleDateFormat dateFormatter = new SimpleDateFormat(RestConnection.JSON_DATE_FORMAT);
					dateFormatter.setTimeZone(java.util.TimeZone.getTimeZone("Zulu"));
					setValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_FIRST_SAVE_TIME,
							dateFormatter.format(new Date()));
				}
				setValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS,
						config.getIntervalBetweenChecks());
				setValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_POLICY_RULES_JSON,
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

	private void checkIntervalErrors(final HubJiraConfigSerializable config) {
		if (StringUtils.isBlank(config.getIntervalBetweenChecks())) {
			config.setIntervalBetweenChecksError(JiraConfigErrors.NO_INTERVAL_FOUND_ERROR);
		} else {
			try {
				final int interval = stringToInteger(config.getIntervalBetweenChecks());
				if (interval <= 0) {
					config.setIntervalBetweenChecksError(JiraConfigErrors.INVALID_INTERVAL_FOUND_ERROR);
				}
			} catch (final IllegalArgumentException e) {
				config.setIntervalBetweenChecksError(e.getMessage());
			}
		}
	}

	private void checkMappingErrors(final HubJiraConfigSerializable config) {
		if (config.getHubProjectMappings() != null && !config.getHubProjectMappings().isEmpty()) {
			boolean hasEmptyMapping = false;
			for (final HubProjectMapping mapping : config.getHubProjectMappings()) {
				boolean jiraProjectBlank = true;
				boolean hubProjectBlank = true;
				if (mapping.getJiraProject() != null) {
					if (mapping.getJiraProject().getProjectId() != null) {
						jiraProjectBlank = false;
					}
				}
				if (mapping.getHubProject() != null) {
					if (StringUtils.isNotBlank(mapping.getHubProject().getProjectUrl())) {
						hubProjectBlank = false;
					} else if (StringUtils.isNotBlank(mapping.getHubProject().getProjectName())) {
						hubProjectBlank = false;
					}
				}
				if(jiraProjectBlank || hubProjectBlank){
					hasEmptyMapping = true;
				}
			}
			if(hasEmptyMapping){
				config.setHubProjectMappingError(
						concatErrorMessage(config.getHubProjectMappingError(),
								JiraConfigErrors.MAPPING_HAS_EMPTY_ERROR));
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
		if (jiraProjects != null && !jiraProjects.isEmpty()) {
			for (final Project oldProject : jiraProjects) {
				final JiraProject newProject = new JiraProject();
				newProject.setProjectName(oldProject.getName());
				newProject.setProjectId(oldProject.getId());

				if(oldProject.getIssueTypes() == null || oldProject.getIssueTypes().isEmpty()){
					newProject.setProjectError(JiraConfigErrors.JIRA_PROJECT_NO_ISSUE_TYPES_FOUND_ERROR);
				} else {
					final boolean foundBugType = false;
					for (final IssueType issueType : oldProject.getIssueTypes()) {
						System.out.println(issueType.getName());
						System.out.println(issueType.getId());
						System.out.println(issueType.getPropertySet());
						System.out.println(issueType.getDescription());
						System.out.println(issueType.getGenericValue().toString());

					}
				}

				newJiraProjects.add(newProject);
			}
		}
		return newJiraProjects;
	}

	public HubIntRestService getHubRestService(final PluginSettings settings, final HubJiraConfigSerializable config) {
		final String hubUrl = getStringValue(settings, HubConfigKeys.CONFIG_HUB_URL);
		final String hubUser = getStringValue(settings, HubConfigKeys.CONFIG_HUB_USER);
		final String encHubPassword = getStringValue(settings, HubConfigKeys.CONFIG_HUB_PASS);
		final String encHubPasswordLength = getStringValue(settings, HubConfigKeys.CONFIG_HUB_PASS_LENGTH);
		final String hubTimeout = getStringValue(settings, HubConfigKeys.CONFIG_HUB_TIMEOUT);

		if (StringUtils.isBlank(hubUrl) && StringUtils.isBlank(hubUser) && StringUtils.isBlank(encHubPassword)
				&& StringUtils.isBlank(hubTimeout)) {
			config.setErrorMessage(JiraConfigErrors.HUB_CONFIG_PLUGIN_MISSING);
			return null;
		} else if (StringUtils.isBlank(hubUrl) || StringUtils.isBlank(hubUser) || StringUtils.isBlank(encHubPassword)
				|| StringUtils.isBlank(hubTimeout)) {
			config.setErrorMessage(
					JiraConfigErrors.HUB_SERVER_MISCONFIGURATION + JiraConfigErrors.CHECK_HUB_SERVER_CONFIGURATION);
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
			final HubServerConfigBuilder configBuilder = new HubServerConfigBuilder();
			configBuilder.setHubUrl(hubUrl);
			configBuilder.setUsername(hubUser);
			configBuilder.setPassword(encHubPassword);
			configBuilder.setPasswordLength(NumberUtils.toInt(encHubPasswordLength));
			configBuilder.setTimeout(hubTimeout);
			configBuilder.setProxyHost(hubProxyHost);
			configBuilder.setProxyPort(hubProxyPort);
			configBuilder.setIgnoredProxyHosts(hubNoProxyHost);
			configBuilder.setProxyUsername(hubProxyUser);
			configBuilder.setProxyPassword(encHubProxyPassword);
			configBuilder.setProxyPasswordLength(NumberUtils.toInt(hubProxyPasswordLength));

			final ValidationResults<GlobalFieldKey, HubServerConfig> result = configBuilder.build();
			final HubServerConfig serverConfig = result.getConstructedObject();

			final RestConnection restConnection = new RestConnection(serverConfig.getHubUrl().toString());
			restConnection.setTimeout(serverConfig.getTimeout());
			restConnection.setProxyProperties(serverConfig.getProxyInfo());
			restConnection.setCookies(serverConfig.getGlobalCredentials().getUsername(),
					serverConfig.getGlobalCredentials().getDecryptedPassword());

			hubRestService = new HubIntRestService(restConnection);

			if(result.hasErrors()){
				config.setErrorMessage(JiraConfigErrors.CHECK_HUB_SERVER_CONFIGURATION);
			}
		} catch (IllegalArgumentException | URISyntaxException | BDRestException | EncryptionException e) {
			config.setErrorMessage(JiraConfigErrors.CHECK_HUB_SERVER_CONFIGURATION + " :: " + e.getMessage());
			return null;
		}
		return hubRestService;
	}


	private List<HubProject> getHubProjects(final HubIntRestService hubRestService,
			final HubJiraConfigSerializable config) {
		final List<HubProject> hubProjects = new ArrayList<HubProject>();
		if (hubRestService != null) {
			List<ProjectItem> hubProjectItems = null;
			try {
				hubProjectItems = hubRestService.getProjectMatches(null);
			} catch (IOException | BDRestException | URISyntaxException e) {
				config.setErrorMessage(concatErrorMessage(config.getErrorMessage(), e.getMessage()));
			}

			if (hubProjectItems != null && !hubProjectItems.isEmpty()) {
				for (final ProjectItem project : hubProjectItems) {
					final HubProject newHubProject = new HubProject();
					newHubProject.setProjectName(project.getName());
					newHubProject.setProjectUrl(project.get_meta().getHref());
					hubProjects.add(newHubProject);
				}
			}
		}
		return hubProjects;
	}

	private void setHubPolicyRules(final HubIntRestService restService,
			final HubJiraConfigSerializable config) {

		final List<PolicyRuleSerializable> newPolicyRules = new ArrayList<PolicyRuleSerializable>();
		if (restService != null) {
			final HubSupportHelper supportHelper = new HubSupportHelper();
			try {
				supportHelper.checkHubSupport(restService, null);

				if (supportHelper.isPolicyApiSupport()) {

					final HubItemsService<PolicyRule> policyService = getPolicyService(restService.getRestConnection());

					final List<String> urlSegments = new ArrayList<>();
					urlSegments.add("api");
					urlSegments.add("policy-rules");

					final Set<AbstractMap.SimpleEntry<String, String>> queryParameters = new HashSet<>();
					queryParameters.add(new AbstractMap.SimpleEntry<String, String>("limit", String.valueOf(Integer.MAX_VALUE)));

					List<PolicyRule> policyRules = null;
					try {
						policyRules = policyService.httpGetItemList(urlSegments, queryParameters);
					} catch (IOException | URISyntaxException | ResourceDoesNotExistException
							| BDRestException e) {
						config.setPolicyRulesError(e.getMessage());
					}

					if (policyRules != null && !policyRules.isEmpty()) {
						for (final PolicyRule rule : policyRules) {
							final PolicyRuleSerializable newRule = new PolicyRuleSerializable();
							newRule.setDescription(rule.getDescription().trim());
							newRule.setName(rule.getName().trim());
							newRule.setPolicyUrl(rule.getMeta().getHref());
							newPolicyRules.add(newRule);
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
				} else {
					config.setPolicyRulesError(JiraConfigErrors.HUB_SERVER_NO_POLICY_SUPPORT_ERROR);
				}
			} catch (IOException | URISyntaxException e) {
				config.setPolicyRulesError(e.getMessage());
			}
		}
		config.setPolicyRules(newPolicyRules);
		if (config.getPolicyRules().isEmpty()) {
			config.setPolicyRulesError(
					concatErrorMessage(config.getPolicyRulesError(), JiraConfigErrors.NO_POLICY_RULES_FOUND_ERROR));
		}

	}

	private String concatErrorMessage(final String originalMessage, final String newMessage) {
		String errorMsg = "";
		if (StringUtils.isNotBlank(originalMessage)) {
			errorMsg = originalMessage;
			errorMsg += " : ";
		}
		errorMsg += newMessage;
		return errorMsg;
	}

	public HubItemsService<PolicyRule> getPolicyService(final RestConnection restConnection) {
		final TypeToken<PolicyRule> typeToken = new TypeToken<PolicyRule>() {
		};
		return new HubItemsService<PolicyRule>(restConnection, typeToken);
	}
}
