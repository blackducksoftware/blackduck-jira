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
package com.blackducksoftware.integration.jira.task;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;

import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.scheduling.PluginJob;
import com.blackducksoftware.integration.atlassian.utils.HubConfigKeys;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.builder.ValidationResults;
import com.blackducksoftware.integration.hub.global.GlobalFieldKey;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.jira.common.HubJiraConfigKeys;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.common.HubProjectMapping;
import com.blackducksoftware.integration.jira.config.HubJiraConfigSerializable;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;
import com.blackducksoftware.integration.jira.task.setup.HubFieldConfigurationSetup;
import com.blackducksoftware.integration.jira.task.setup.HubFieldScreenSchemeSetup;
import com.blackducksoftware.integration.jira.task.setup.HubGroupSetup;
import com.blackducksoftware.integration.jira.task.setup.HubIssueTypeSetup;
import com.blackducksoftware.integration.jira.task.setup.HubWorkflowSetup;

/**
 * A scheduled JIRA task that collects recent notifications from the Hub, and
 * generates JIRA tickets for them.
 *
 * @author sbillings
 *
 */
public class JiraTask implements PluginJob {
	private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));

	public JiraTask() {
	}

	@Override
	public void execute(final Map<String, Object> jobDataMap) {
		final PluginSettings settings = (PluginSettings) jobDataMap.get(HubMonitor.KEY_SETTINGS);
		final String hubUrl = getStringValue(settings, HubConfigKeys.CONFIG_HUB_URL);
		final String hubUsername = getStringValue(settings, HubConfigKeys.CONFIG_HUB_USER);
		final String hubPasswordEncrypted = getStringValue(settings, HubConfigKeys.CONFIG_HUB_PASS);
		final String hubPasswordLength = getStringValue(settings, HubConfigKeys.CONFIG_HUB_PASS_LENGTH);
		final String hubTimeoutString = getStringValue(settings, HubConfigKeys.CONFIG_HUB_TIMEOUT);

		final String hubProxyHost = getStringValue(settings, HubConfigKeys.CONFIG_PROXY_HOST);
		final String hubProxyPort = getStringValue(settings, HubConfigKeys.CONFIG_PROXY_PORT);
		final String hubProxyNoHost = getStringValue(settings, HubConfigKeys.CONFIG_PROXY_NO_HOST);
		final String hubProxyUser = getStringValue(settings, HubConfigKeys.CONFIG_PROXY_USER);
		final String hubProxyPassEncrypted = getStringValue(settings, HubConfigKeys.CONFIG_PROXY_PASS);
		final String hubProxyPassLength = getStringValue(settings, HubConfigKeys.CONFIG_PROXY_PASS_LENGTH);

		final String intervalString = getStringValue(settings,
				HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS);
		final String projectMappingJson = getStringValue(settings,
				HubJiraConfigKeys.HUB_CONFIG_JIRA_PROJECT_MAPPINGS_JSON);
		final String policyRulesJson = getStringValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_POLICY_RULES_JSON);
		final String jiraIssueTypeName = getStringValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_ISSUE_TYPE_NAME);
		final String installDateString = getStringValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_FIRST_SAVE_TIME);
		final String lastRunDateString = getStringValue(settings, HubJiraConfigKeys.HUB_CONFIG_LAST_RUN_DATE);

		final String jiraUser = getStringValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_USER);

		final JiraSettingsService jiraSettingsService = new JiraSettingsService(settings);

		// Do Jira setup here
		jiraSetup(new JiraServices(), jiraSettingsService, projectMappingJson);

		final HubServerConfigBuilder hubConfigBuilder = new HubServerConfigBuilder();
		hubConfigBuilder.setHubUrl(hubUrl);
		hubConfigBuilder.setUsername(hubUsername);
		hubConfigBuilder.setPassword(hubPasswordEncrypted);
		hubConfigBuilder.setPasswordLength(NumberUtils.toInt(hubPasswordLength));
		hubConfigBuilder.setTimeout(hubTimeoutString);

		hubConfigBuilder.setProxyHost(hubProxyHost);
		hubConfigBuilder.setProxyPort(hubProxyPort);
		hubConfigBuilder.setIgnoredProxyHosts(hubProxyNoHost);
		hubConfigBuilder.setProxyUsername(hubProxyUser);
		hubConfigBuilder.setProxyPassword(hubProxyPassEncrypted);
		hubConfigBuilder.setProxyPasswordLength(NumberUtils.toInt(hubProxyPassLength));

		final ValidationResults<GlobalFieldKey, HubServerConfig> configResult = hubConfigBuilder.build();

		if (configResult.hasErrors()) {
			logger.error("There was a problem with the Server configuration.");
			return;
		}
		final HubServerConfig serverConfig = configResult.getConstructedObject();

		final HubJiraTask processor = new HubJiraTask(serverConfig, intervalString, jiraIssueTypeName,
				installDateString, lastRunDateString, projectMappingJson, policyRulesJson, jiraUser,
				jiraSettingsService);
		final String runDateString = processor.execute();
		if (runDateString != null) {
			settings.put(HubJiraConfigKeys.HUB_CONFIG_LAST_RUN_DATE, runDateString);
		}
	}

	public void jiraSetup(final JiraServices jiraServices, final JiraSettingsService jiraSettingsService,
			final String projectMappingJson) {

		final HubGroupSetup groupSetup = new HubGroupSetup(jiraSettingsService, jiraServices.getGroupManager());
		groupSetup.addHubJiraGroupToJira();

		// TODO create our issueTypes AND add them to each Projects workflow
		// scheme before we try addWorkflowToProjectsWorkflowScheme

		//////////////////////// Create Issue Types, workflow, etc ////////////
		final HubIssueTypeSetup issueTypeSetup = new HubIssueTypeSetup(jiraSettingsService,
				jiraServices.getIssueTypes());
		final List<IssueType> issueTypes = issueTypeSetup.addIssueTypesToJira();
		logger.debug("Found our issue types : " + issueTypes.size());

		final HubFieldScreenSchemeSetup fieldConfigurationSetup = new HubFieldScreenSchemeSetup(jiraSettingsService,
				jiraServices);
		if (issueTypes != null && !issueTypes.isEmpty()) {
			fieldConfigurationSetup.addHubFieldConfigurationToJira(issueTypes);
		}

		final HubFieldConfigurationSetup hubFieldConfigurationSetup = new HubFieldConfigurationSetup(
				jiraSettingsService, jiraServices);
		hubFieldConfigurationSetup.addHubFieldConfigurationToJira();

		final HubWorkflowSetup workflowSetup = new HubWorkflowSetup(jiraSettingsService, jiraServices);
		final JiraWorkflow workflow = workflowSetup.addHubWorkflowToJira();
		////////////////////////////////////////////////////////////////////////

		/////////////////////// Associate with projects ///////////////////////
		if (projectMappingJson != null && issueTypes != null && !issueTypes.isEmpty()) {
			final HubJiraConfigSerializable config = new HubJiraConfigSerializable();
			// Converts Json to list of mappings
			config.setHubProjectMappingsJson(projectMappingJson);
			if (!config.getHubProjectMappings().isEmpty()) {
				for (final HubProjectMapping projectMapping : config.getHubProjectMappings()) {
					if (projectMapping.getJiraProject() != null
							&& projectMapping.getJiraProject().getProjectId() != null) {
						// Get jira Project object by Id
						// from the JiraProject in the mapping
						final Project jiraProject = jiraServices.getJiraProjectManager()
								.getProjectObj(projectMapping.getJiraProject().getProjectId());
						// TODO add issuetypes to this project
						issueTypeSetup.addIssueTypesToProject(jiraProject, issueTypes);
						workflowSetup.addWorkflowToProjectsWorkflowScheme(workflow, jiraProject, issueTypes);
					}
				}
			}
		}
		/////////////////////////////////////////////////////////////////////////
	}

	private Object getValue(final PluginSettings settings, final String key) {
		return settings.get(key);
	}

	private String getStringValue(final PluginSettings settings, final String key) {
		return (String) getValue(settings, key);
	}
}
