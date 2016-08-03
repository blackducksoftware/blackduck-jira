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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;
import org.mockito.Mockito;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.blackducksoftware.integration.atlassian.utils.HubConfigKeys;
import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.encryption.PasswordEncrypter;
import com.blackducksoftware.integration.hub.item.HubItemsService;
import com.blackducksoftware.integration.hub.meta.MetaInformation;
import com.blackducksoftware.integration.hub.policy.api.PolicyRule;
import com.blackducksoftware.integration.hub.project.api.ProjectItem;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.jira.mocks.HttpServletRequestMock;
import com.blackducksoftware.integration.jira.mocks.PluginSettingsFactoryMock;
import com.blackducksoftware.integration.jira.mocks.ProjectManagerMock;
import com.blackducksoftware.integration.jira.mocks.TransactionTemplateMock;
import com.blackducksoftware.integration.jira.mocks.UserManagerMock;
import com.blackducksoftware.integration.jira.utils.HubJiraConfigKeys;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class HubJiraConfigControllerTest {

	private List<ProjectItem> getHubProjects() {
		final List<ProjectItem> hubProjects = new ArrayList<ProjectItem>();

		final MetaInformation metaInfo1 = new MetaInformation(null, "projectURL1", null);
		final ProjectItem project1 = new ProjectItem("HubProject1", null, metaInfo1);

		final MetaInformation metaInfo2 = new MetaInformation(null, "projectURL2", null);
		final ProjectItem project2 = new ProjectItem("HubProject2", null, metaInfo2);

		final MetaInformation metaInfo3 = new MetaInformation(null, "projectURL3", null);
		final ProjectItem project3 = new ProjectItem("HubProject3", null, metaInfo3);

		hubProjects.add(project1);
		hubProjects.add(project2);
		hubProjects.add(project3);
		return hubProjects;
	}

	private List<PolicyRule> getHubPolicies() {
		final List<PolicyRule> policyRules = new ArrayList<PolicyRule>();
		final MetaInformation metaInfo1 = new MetaInformation(null, "policyURL1", null);
		final PolicyRule rule1 = new PolicyRule(metaInfo1, "PolicyRule1", "1TestDescription", null, null, null, null,
				null,
				null, null);

		final MetaInformation metaInfo2 = new MetaInformation(null, "policyURL2", null);
		final PolicyRule rule2 = new PolicyRule(metaInfo2, "PolicyRule2", "2TestDescription", null, null, null, null,
				null,
				null, null);

		final MetaInformation metaInfo3 = new MetaInformation(null, "policyURL3", null);
		final PolicyRule rule3 = new PolicyRule(metaInfo3, "PolicyRule3", "3TestDescription", null, null, null, null,
				null,
				null, null);

		policyRules.add(rule1);
		policyRules.add(rule2);
		policyRules.add(rule3);
		return policyRules;
	}

	private List<PolicyRuleSerializable> getJiraPolicies() {
		final List<PolicyRuleSerializable> newPolicyRules = new ArrayList<PolicyRuleSerializable>();

		final PolicyRuleSerializable rule1 = new PolicyRuleSerializable();
		rule1.setName("PolicyRule1");
		rule1.setPolicyUrl("policyURL1");
		rule1.setDescription("1TestDescription");
		rule1.setChecked(true);

		final PolicyRuleSerializable rule2 = new PolicyRuleSerializable();
		rule2.setName("PolicyRule2");
		rule2.setPolicyUrl("policyURL2");
		rule2.setDescription("2TestDescription");
		rule2.setChecked(true);

		final PolicyRuleSerializable rule3 = new PolicyRuleSerializable();
		rule3.setName("PolicyRule3");
		rule3.setPolicyUrl("policyURL3");
		rule3.setDescription("3TestDescription");
		rule3.setChecked(true);

		newPolicyRules.add(rule1);
		newPolicyRules.add(rule2);
		newPolicyRules.add(rule3);
		return newPolicyRules;
	}

	private Set<HubProjectMapping> getMappings() {
		final JiraProject jiraProject1 = new JiraProject();
		jiraProject1.setProjectName("Project1");
		jiraProject1.setProjectKey("ProjectKey");
		jiraProject1.setProjectId(0L);

		final HubProject hubProject1 = new HubProject();
		hubProject1.setProjectName("HubProject1");
		hubProject1.setProjectUrl("projectURL1");

		final JiraProject jiraProject2 = new JiraProject();
		jiraProject2.setProjectName("Project2");
		jiraProject2.setProjectKey("ProjectKey");
		jiraProject2.setProjectId(153L);

		final HubProject hubProject2 = new HubProject();
		hubProject2.setProjectName("HubProject2");
		hubProject2.setProjectUrl("projectURL2");

		final HubProjectMapping mapping1 = new HubProjectMapping();
		mapping1.setHubProject(hubProject1);
		mapping1.setJiraProject(jiraProject1);

		final HubProjectMapping mapping2 = new HubProjectMapping();
		mapping2.setHubProject(hubProject1);
		mapping2.setJiraProject(jiraProject2);

		final HubProjectMapping mapping3 = new HubProjectMapping();
		mapping3.setHubProject(hubProject2);
		mapping3.setJiraProject(jiraProject1);

		final HubProjectMapping mapping4 = new HubProjectMapping();
		mapping4.setHubProject(hubProject2);
		mapping4.setJiraProject(jiraProject2);

		final Set<HubProjectMapping> mappings = new HashSet<HubProjectMapping>();
		mappings.add(mapping1);
		mappings.add(mapping2);
		mappings.add(mapping3);
		mappings.add(mapping4);

		return mappings;
	}

	@Test
	public void testGetIntervalNullUser() {
		final UserManagerMock managerMock = new UserManagerMock();
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();
		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();

		final HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		final Response response = controller.getInterval(requestMock);
		assertNotNull(response);
		assertEquals(Integer.valueOf(Status.UNAUTHORIZED.getStatusCode()), Integer.valueOf(response.getStatus()));
	}

	@Test
	public void testGetIntervalNotAdmin() {
		final UserManagerMock managerMock = new UserManagerMock();
		managerMock.setRemoteUsername("User");
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();
		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();

		final HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		final Response response = controller.getInterval(requestMock);
		assertNotNull(response);
		assertEquals(Integer.valueOf(Status.UNAUTHORIZED.getStatusCode()), Integer.valueOf(response.getStatus()));
	}

	@Test
	public void testGetIntervalEmpty() {
		final UserManagerMock managerMock = new UserManagerMock();
		managerMock.setRemoteUsername("User");
		managerMock.setIsSystemAdmin(true);
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();
		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();

		final HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		final Response response = controller.getInterval(requestMock);
		assertNotNull(response);
		final Object configObject = response.getEntity();
		assertNotNull(configObject);
		final HubJiraConfigSerializable config = (HubJiraConfigSerializable) configObject;
		assertNull(config.getIntervalBetweenChecks());
		assertNull(config.getPolicyRules());
		assertNull(config.getJiraProjects());
		assertNull(config.getHubProjects());
		assertNull(config.getHubProjectMappings());

		assertNull(config.getErrorMessage());
		assertEquals(JiraConfigErrors.NO_INTERVAL_FOUND_ERROR, config.getIntervalBetweenChecksError());
		assertNull(config.getPolicyRulesError());
		assertNull(config.getHubProjectMappingError());
		assertTrue(config.hasErrors());
	}

	@Test
	public void testGetIntervalInvalid() {
		final String intervalBetweenChecks = "intervalBetweenChecks";

		final UserManagerMock managerMock = new UserManagerMock();
		managerMock.setRemoteUsername("User");
		managerMock.setIsSystemAdmin(true);
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();
		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();

		final HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		final PluginSettings settings = settingsFactory.createGlobalSettings();
		settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS, intervalBetweenChecks);

		final Response response = controller.getInterval(requestMock);
		assertNotNull(response);
		final Object configObject = response.getEntity();
		assertNotNull(configObject);
		final HubJiraConfigSerializable config = (HubJiraConfigSerializable) configObject;
		assertEquals(intervalBetweenChecks, config.getIntervalBetweenChecks());
		assertNull(config.getPolicyRules());
		assertNull(config.getJiraProjects());
		assertNull(config.getHubProjects());
		assertNull(config.getHubProjectMappings());

		assertNull(config.getErrorMessage());
		assertEquals("The String : " + intervalBetweenChecks + " , is not an Integer.",
				config.getIntervalBetweenChecksError());
		assertNull(config.getPolicyRulesError());
		assertNull(config.getHubProjectMappingError());
		assertTrue(config.hasErrors());
	}

	@Test
	public void testGetIntervalNegative() {
		final String intervalBetweenChecks = "-30";

		final UserManagerMock managerMock = new UserManagerMock();
		managerMock.setRemoteUsername("User");
		managerMock.setIsSystemAdmin(true);
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();
		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();

		final HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		final PluginSettings settings = settingsFactory.createGlobalSettings();
		settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS, intervalBetweenChecks);

		final Response response = controller.getInterval(requestMock);
		assertNotNull(response);
		final Object configObject = response.getEntity();
		assertNotNull(configObject);
		final HubJiraConfigSerializable config = (HubJiraConfigSerializable) configObject;
		assertEquals(intervalBetweenChecks, config.getIntervalBetweenChecks());
		assertNull(config.getPolicyRules());
		assertNull(config.getJiraProjects());
		assertNull(config.getHubProjects());
		assertNull(config.getHubProjectMappings());

		assertNull(config.getErrorMessage());
		assertEquals(JiraConfigErrors.INVALID_INTERVAL_FOUND_ERROR, config.getIntervalBetweenChecksError());
		assertNull(config.getPolicyRulesError());
		assertNull(config.getHubProjectMappingError());
		assertTrue(config.hasErrors());
	}

	@Test
	public void testGetIntervalZero() {
		final String intervalBetweenChecks = "0";

		final UserManagerMock managerMock = new UserManagerMock();
		managerMock.setRemoteUsername("User");
		managerMock.setIsSystemAdmin(true);
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();
		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();

		final HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		final PluginSettings settings = settingsFactory.createGlobalSettings();
		settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS, intervalBetweenChecks);

		final Response response = controller.getInterval(requestMock);
		assertNotNull(response);
		final Object configObject = response.getEntity();
		assertNotNull(configObject);
		final HubJiraConfigSerializable config = (HubJiraConfigSerializable) configObject;
		assertEquals(intervalBetweenChecks, config.getIntervalBetweenChecks());
		assertNull(config.getPolicyRules());
		assertNull(config.getJiraProjects());
		assertNull(config.getHubProjects());
		assertNull(config.getHubProjectMappings());

		assertNull(config.getErrorMessage());
		assertEquals(JiraConfigErrors.INVALID_INTERVAL_FOUND_ERROR, config.getIntervalBetweenChecksError());
		assertNull(config.getPolicyRulesError());
		assertNull(config.getHubProjectMappingError());
		assertTrue(config.hasErrors());
	}

	@Test
	public void testGetIntervalValid() {
		final String intervalBetweenChecks = "30";

		final UserManagerMock managerMock = new UserManagerMock();
		managerMock.setRemoteUsername("User");
		managerMock.setIsSystemAdmin(true);
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();
		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();

		final HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		final PluginSettings settings = settingsFactory.createGlobalSettings();
		settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS, intervalBetweenChecks);

		final Response response = controller.getInterval(requestMock);
		assertNotNull(response);
		final Object configObject = response.getEntity();
		assertNotNull(configObject);
		final HubJiraConfigSerializable config = (HubJiraConfigSerializable) configObject;
		assertEquals(intervalBetweenChecks, config.getIntervalBetweenChecks());
		assertNull(config.getPolicyRules());
		assertNull(config.getJiraProjects());
		assertNull(config.getHubProjects());
		assertNull(config.getHubProjectMappings());

		assertNull(config.getErrorMessage());
		assertNull(config.getIntervalBetweenChecksError());
		assertNull(config.getPolicyRulesError());
		assertNull(config.getHubProjectMappingError());
		assertTrue(!config.hasErrors());
	}

	@Test
	public void testGetIntervalValidInGroup() {
		final String intervalBetweenChecks = "30";

		final UserManagerMock managerMock = new UserManagerMock();
		managerMock.setRemoteUsername("User");
		managerMock.setInGroup(true);
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();
		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();

		final HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		final PluginSettings settings = settingsFactory.createGlobalSettings();
		settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS, intervalBetweenChecks);

		final Response response = controller.getInterval(requestMock);
		assertNotNull(response);
		final Object configObject = response.getEntity();
		assertNotNull(configObject);
		final HubJiraConfigSerializable config = (HubJiraConfigSerializable) configObject;
		assertEquals(intervalBetweenChecks, config.getIntervalBetweenChecks());
		assertNull(config.getPolicyRules());
		assertNull(config.getJiraProjects());
		assertNull(config.getHubProjects());
		assertNull(config.getHubProjectMappings());

		assertNull(config.getErrorMessage());
		assertNull(config.getIntervalBetweenChecksError());
		assertNull(config.getPolicyRulesError());
		assertNull(config.getHubProjectMappingError());
		assertTrue(!config.hasErrors());
	}

	@Test
	public void testGetHubPoliciesNullUser() {
		final UserManagerMock managerMock = new UserManagerMock();
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();
		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();

		final HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		final Response response = controller.getHubPolicies(requestMock);
		assertNotNull(response);
		assertEquals(Integer.valueOf(Status.UNAUTHORIZED.getStatusCode()), Integer.valueOf(response.getStatus()));
	}

	@Test
	public void testGetHubPoliciesNotAdmin() {
		final UserManagerMock managerMock = new UserManagerMock();
		managerMock.setRemoteUsername("User");
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();
		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();

		final HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		final Response response = controller.getHubPolicies(requestMock);
		assertNotNull(response);
		assertEquals(Integer.valueOf(Status.UNAUTHORIZED.getStatusCode()), Integer.valueOf(response.getStatus()));
	}

	@Test
	public void testGetHubPoliciesWithNoServerConfig() throws Exception {
		final UserManagerMock managerMock = new UserManagerMock();
		managerMock.setRemoteUsername("User");
		managerMock.setIsSystemAdmin(true);
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();

		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();

		final HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		final Response response = controller.getHubPolicies(requestMock);
		assertNotNull(response);
		final Object configObject = response.getEntity();
		assertNotNull(configObject);
		final HubJiraConfigSerializable config = (HubJiraConfigSerializable) configObject;

		assertNull(config.getIntervalBetweenChecks());
		assertTrue(config.getPolicyRules().isEmpty());
		assertNull(config.getJiraProjects());
		assertNull(config.getHubProjects());
		assertNull(config.getHubProjectMappings());

		assertEquals(JiraConfigErrors.HUB_CONFIG_PLUGIN_MISSING, config.getErrorMessage());
		assertNull(config.getIntervalBetweenChecksError());
		assertEquals(JiraConfigErrors.NO_POLICY_RULES_FOUND_ERROR, config.getPolicyRulesError());
		assertNull(config.getHubProjectMappingError());
		assertTrue(config.hasErrors());
	}

	@Test
	public void testGetHubPoliciesWithPartialServerConfig() throws Exception {
		final UserManagerMock managerMock = new UserManagerMock();
		managerMock.setRemoteUsername("User");
		managerMock.setIsSystemAdmin(true);
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();

		final PluginSettings settings = settingsFactory.createGlobalSettings();
		settings.put(HubConfigKeys.CONFIG_HUB_URL, "Test Server Url");
		settings.put(HubConfigKeys.CONFIG_HUB_USER, "Test User");
		settings.put(HubConfigKeys.CONFIG_HUB_TIMEOUT, "300");

		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();

		final HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		final Response response = controller.getHubPolicies(requestMock);
		assertNotNull(response);
		final Object configObject = response.getEntity();
		assertNotNull(configObject);
		final HubJiraConfigSerializable config = (HubJiraConfigSerializable) configObject;

		assertNull(config.getIntervalBetweenChecks());
		assertTrue(config.getPolicyRules().isEmpty());
		assertNull(config.getJiraProjects());
		assertNull(config.getHubProjects());
		assertNull(config.getHubProjectMappings());

		assertEquals(JiraConfigErrors.HUB_SERVER_MISCONFIGURATION
				+ JiraConfigErrors.CHECK_HUB_SERVER_CONFIGURATION, config.getErrorMessage());
		assertNull(config.getIntervalBetweenChecksError());
		assertEquals(JiraConfigErrors.NO_POLICY_RULES_FOUND_ERROR, config.getPolicyRulesError());
		assertNull(config.getHubProjectMappingError());
		assertTrue(config.hasErrors());
	}

	@Test
	public void testGetHubPoliciesNoPolicyRulesOldHub() throws Exception {
		final UserManagerMock managerMock = new UserManagerMock();
		managerMock.setRemoteUsername("User");
		managerMock.setIsSystemAdmin(true);
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();

		final PluginSettings settings = settingsFactory.createGlobalSettings();
		settings.put(HubConfigKeys.CONFIG_HUB_URL, "http://www.google.com");
		settings.put(HubConfigKeys.CONFIG_HUB_USER, "Test User");
		settings.put(HubConfigKeys.CONFIG_HUB_PASS, PasswordEncrypter.encrypt("Test"));
		settings.put(HubConfigKeys.CONFIG_HUB_PASS_LENGTH, "4");
		settings.put(HubConfigKeys.CONFIG_HUB_TIMEOUT, "300");

		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();

		HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		controller = Mockito.spy(controller);

		final HubItemsService<PolicyRule> policyServiceMock = Mockito.mock(HubItemsService.class);

		final List<PolicyRule> emptyPolicyRules = new ArrayList<PolicyRule>();

		Mockito.doReturn(emptyPolicyRules).when(policyServiceMock).httpGetItemList(Mockito.anyList(), Mockito.anySet());

		Mockito.doReturn(policyServiceMock).when(controller).getPolicyService(Mockito.any(RestConnection.class));

		final HubIntRestService restServiceMock = Mockito.mock(HubIntRestService.class);

		Mockito.doReturn("2.5.0").when(restServiceMock).getHubVersion();

		Mockito.doReturn(restServiceMock).when(controller).getHubRestService(Mockito.any(PluginSettings.class),
				Mockito.any(HubJiraConfigSerializable.class));

		final Response response = controller.getHubPolicies(requestMock);
		assertNotNull(response);
		final Object configObject = response.getEntity();
		assertNotNull(configObject);
		final HubJiraConfigSerializable config = (HubJiraConfigSerializable) configObject;

		assertNull(config.getIntervalBetweenChecks());
		assertTrue(config.getPolicyRules().isEmpty());
		assertNull(config.getJiraProjects());
		assertNull(config.getHubProjects());
		assertNull(config.getHubProjectMappings());

		assertNull(config.getErrorMessage());
		assertNull(config.getIntervalBetweenChecksError());
		assertEquals(JiraConfigErrors.HUB_SERVER_NO_POLICY_SUPPORT_ERROR + " : "
				+ JiraConfigErrors.NO_POLICY_RULES_FOUND_ERROR, config.getPolicyRulesError());
		assertNull(config.getHubProjectMappingError());
		assertTrue(config.hasErrors());
	}

	@Test
	public void testGetHubPoliciesNoPolicyRules() throws Exception {
		final UserManagerMock managerMock = new UserManagerMock();
		managerMock.setRemoteUsername("User");
		managerMock.setIsSystemAdmin(true);
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();

		final PluginSettings settings = settingsFactory.createGlobalSettings();
		settings.put(HubConfigKeys.CONFIG_HUB_URL, "http://www.google.com");
		settings.put(HubConfigKeys.CONFIG_HUB_USER, "Test User");
		settings.put(HubConfigKeys.CONFIG_HUB_PASS, PasswordEncrypter.encrypt("Test"));
		settings.put(HubConfigKeys.CONFIG_HUB_PASS_LENGTH, "4");
		settings.put(HubConfigKeys.CONFIG_HUB_TIMEOUT, "300");

		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();

		HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		controller = Mockito.spy(controller);

		final HubItemsService<PolicyRule> policyServiceMock = Mockito.mock(HubItemsService.class);

		final List<PolicyRule> emptyPolicyRules = new ArrayList<PolicyRule>();

		Mockito.doReturn(emptyPolicyRules).when(policyServiceMock).httpGetItemList(Mockito.anyList(), Mockito.anySet());

		Mockito.doReturn(policyServiceMock).when(controller).getPolicyService(Mockito.any(RestConnection.class));

		final HubIntRestService restServiceMock = Mockito.mock(HubIntRestService.class);

		Mockito.doReturn("3.1.0").when(restServiceMock).getHubVersion();

		Mockito.doReturn(restServiceMock).when(controller).getHubRestService(Mockito.any(PluginSettings.class),
				Mockito.any(HubJiraConfigSerializable.class));

		final Response response = controller.getHubPolicies(requestMock);
		assertNotNull(response);
		final Object configObject = response.getEntity();
		assertNotNull(configObject);
		final HubJiraConfigSerializable config = (HubJiraConfigSerializable) configObject;

		assertNull(config.getIntervalBetweenChecks());
		assertTrue(config.getPolicyRules().isEmpty());
		assertNull(config.getJiraProjects());
		assertNull(config.getHubProjects());
		assertNull(config.getHubProjectMappings());

		assertNull(config.getErrorMessage());
		assertNull(config.getIntervalBetweenChecksError());
		assertEquals(JiraConfigErrors.NO_POLICY_RULES_FOUND_ERROR, config.getPolicyRulesError());
		assertNull(config.getHubProjectMappingError());
		assertTrue(config.hasErrors());
	}

	@Test
	public void testGetHubPoliciesWithPolicyRules() throws Exception {
		final UserManagerMock managerMock = new UserManagerMock();
		managerMock.setRemoteUsername("User");
		managerMock.setIsSystemAdmin(true);
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();

		final PluginSettings settings = settingsFactory.createGlobalSettings();
		settings.put(HubConfigKeys.CONFIG_HUB_URL, "http://www.google.com");
		settings.put(HubConfigKeys.CONFIG_HUB_USER, "Test User");
		settings.put(HubConfigKeys.CONFIG_HUB_PASS, PasswordEncrypter.encrypt("Test"));
		settings.put(HubConfigKeys.CONFIG_HUB_PASS_LENGTH, "4");
		settings.put(HubConfigKeys.CONFIG_HUB_TIMEOUT, "300");

		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();

		HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		controller = Mockito.spy(controller);

		final HubItemsService<PolicyRule> policyServiceMock = Mockito.mock(HubItemsService.class);

		Mockito.doReturn(getHubPolicies()).when(policyServiceMock).httpGetItemList(Mockito.anyList(), Mockito.anySet());

		Mockito.doReturn(policyServiceMock).when(controller).getPolicyService(Mockito.any(RestConnection.class));

		final HubIntRestService restServiceMock = Mockito.mock(HubIntRestService.class);

		Mockito.doReturn("3.1.0").when(restServiceMock).getHubVersion();

		Mockito.doReturn(restServiceMock).when(controller).getHubRestService(Mockito.any(PluginSettings.class),
				Mockito.any(HubJiraConfigSerializable.class));

		final Response response = controller.getHubPolicies(requestMock);
		assertNotNull(response);
		final Object configObject = response.getEntity();
		assertNotNull(configObject);
		final HubJiraConfigSerializable config = (HubJiraConfigSerializable) configObject;

		assertNull(config.getIntervalBetweenChecks());
		assertTrue(!config.getPolicyRules().isEmpty());
		assertNull(config.getJiraProjects());
		assertNull(config.getHubProjects());
		assertNull(config.getHubProjectMappings());

		assertNull(config.getErrorMessage());
		assertNull(config.getIntervalBetweenChecksError());
		assertNull(config.getPolicyRulesError());
		assertNull(config.getHubProjectMappingError());
		assertTrue(!config.hasErrors());
	}

	@Test
	public void testGetHubPoliciesWithPolicyRulesInGroup() throws Exception {
		final UserManagerMock managerMock = new UserManagerMock();
		managerMock.setRemoteUsername("User");
		managerMock.setInGroup(true);
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();

		final PluginSettings settings = settingsFactory.createGlobalSettings();
		settings.put(HubConfigKeys.CONFIG_HUB_URL, "http://www.google.com");
		settings.put(HubConfigKeys.CONFIG_HUB_USER, "Test User");
		settings.put(HubConfigKeys.CONFIG_HUB_PASS, PasswordEncrypter.encrypt("Test"));
		settings.put(HubConfigKeys.CONFIG_HUB_PASS_LENGTH, "4");
		settings.put(HubConfigKeys.CONFIG_HUB_TIMEOUT, "300");

		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();

		HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		controller = Mockito.spy(controller);

		final HubItemsService<PolicyRule> policyServiceMock = Mockito.mock(HubItemsService.class);

		Mockito.doReturn(getHubPolicies()).when(policyServiceMock).httpGetItemList(Mockito.anyList(), Mockito.anySet());

		Mockito.doReturn(policyServiceMock).when(controller).getPolicyService(Mockito.any(RestConnection.class));

		final HubIntRestService restServiceMock = Mockito.mock(HubIntRestService.class);

		Mockito.doReturn("3.1.0").when(restServiceMock).getHubVersion();

		Mockito.doReturn(restServiceMock).when(controller).getHubRestService(Mockito.any(PluginSettings.class),
				Mockito.any(HubJiraConfigSerializable.class));

		final Response response = controller.getHubPolicies(requestMock);
		assertNotNull(response);
		final Object configObject = response.getEntity();
		assertNotNull(configObject);
		final HubJiraConfigSerializable config = (HubJiraConfigSerializable) configObject;

		assertNull(config.getIntervalBetweenChecks());
		assertTrue(!config.getPolicyRules().isEmpty());
		assertNull(config.getJiraProjects());
		assertNull(config.getHubProjects());
		assertNull(config.getHubProjectMappings());

		assertNull(config.getErrorMessage());
		assertNull(config.getIntervalBetweenChecksError());
		assertNull(config.getPolicyRulesError());
		assertNull(config.getHubProjectMappingError());
		assertTrue(!config.hasErrors());
	}

	@Test
	public void testGetJiraProjectsNullUser() {
		final UserManagerMock managerMock = new UserManagerMock();
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();
		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();

		final HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		final Response response = controller.getJiraProjects(requestMock);
		assertNotNull(response);
		assertEquals(Integer.valueOf(Status.UNAUTHORIZED.getStatusCode()), Integer.valueOf(response.getStatus()));
	}

	@Test
	public void testGetJiraProjectsNotAdmin() {
		final UserManagerMock managerMock = new UserManagerMock();
		managerMock.setRemoteUsername("User");
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();
		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();

		final HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		final Response response = controller.getJiraProjects(requestMock);
		assertNotNull(response);
		assertEquals(Integer.valueOf(Status.UNAUTHORIZED.getStatusCode()), Integer.valueOf(response.getStatus()));
	}

	@Test
	public void testGetJiraProjectsNone() {
		final UserManagerMock managerMock = new UserManagerMock();
		managerMock.setRemoteUsername("User");
		managerMock.setIsSystemAdmin(true);
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();
		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();

		final HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		final Response response = controller.getJiraProjects(requestMock);
		assertNotNull(response);
		final Object configObject = response.getEntity();
		assertNotNull(configObject);
		final HubJiraConfigSerializable config = (HubJiraConfigSerializable) configObject;
		assertNull(config.getIntervalBetweenChecks());
		assertNull(config.getPolicyRules());
		assertTrue(config.getJiraProjects().isEmpty());
		assertNull(config.getHubProjects());
		assertNull(config.getHubProjectMappings());

		assertNull(config.getErrorMessage());
		assertNull(config.getIntervalBetweenChecksError());
		assertNull(config.getPolicyRulesError());
		assertNull(config.getHubProjectMappingError());
		assertTrue(config.hasErrors());
		assertEquals(JiraConfigErrors.NO_JIRA_PROJECTS_FOUND, config.getJiraProjectsError());
	}

	@Test
	public void testGetJiraProjectsMultipleProjects() {
		final UserManagerMock managerMock = new UserManagerMock();
		managerMock.setRemoteUsername("User");
		managerMock.setIsSystemAdmin(true);
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();
		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();

		projectManagerMock.setProjectObjects(ProjectManagerMock.getTestProjectObjectsWithTaskIssueType());

		final HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		final Response response = controller.getJiraProjects(requestMock);
		assertNotNull(response);
		final Object configObject = response.getEntity();
		assertNotNull(configObject);
		final HubJiraConfigSerializable config = (HubJiraConfigSerializable) configObject;
		assertNull(config.getIntervalBetweenChecks());
		assertNull(config.getPolicyRules());
		assertTrue(!config.getJiraProjects().isEmpty());
		for (final JiraProject proj : config.getJiraProjects()) {
			assertNull(proj.getProjectError());
		}
		assertNull(config.getHubProjects());
		assertNull(config.getHubProjectMappings());

		assertNull(config.getErrorMessage());
		assertNull(config.getIntervalBetweenChecksError());
		assertNull(config.getPolicyRulesError());
		assertNull(config.getHubProjectMappingError());
		assertTrue(!config.hasErrors());
	}

	@Test
	public void testGetJiraProjectsMultipleProjectsInGroup() {
		final UserManagerMock managerMock = new UserManagerMock();
		managerMock.setRemoteUsername("User");
		managerMock.setInGroup(true);
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();
		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();

		projectManagerMock.setProjectObjects(ProjectManagerMock.getTestProjectObjectsWithTaskIssueType());

		final HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		final Response response = controller.getJiraProjects(requestMock);
		assertNotNull(response);
		final Object configObject = response.getEntity();
		assertNotNull(configObject);
		final HubJiraConfigSerializable config = (HubJiraConfigSerializable) configObject;
		assertNull(config.getIntervalBetweenChecks());
		assertNull(config.getPolicyRules());
		assertTrue(!config.getJiraProjects().isEmpty());
		for (final JiraProject proj : config.getJiraProjects()) {
			assertNull(proj.getProjectError());
		}
		assertNull(config.getHubProjects());
		assertNull(config.getHubProjectMappings());

		assertNull(config.getErrorMessage());
		assertNull(config.getIntervalBetweenChecksError());
		assertNull(config.getPolicyRulesError());
		assertNull(config.getHubProjectMappingError());
		assertTrue(!config.hasErrors());
	}

	@Test
	public void testGetJiraProjectsMultipleProjectsWithOutBugType() {
		final UserManagerMock managerMock = new UserManagerMock();
		managerMock.setRemoteUsername("User");
		managerMock.setIsSystemAdmin(true);
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();
		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();

		projectManagerMock.setProjectObjects(ProjectManagerMock.getTestProjectObjectsWithoutTaskIssueType());

		final HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		final Response response = controller.getJiraProjects(requestMock);
		assertNotNull(response);
		final Object configObject = response.getEntity();
		assertNotNull(configObject);
		final HubJiraConfigSerializable config = (HubJiraConfigSerializable) configObject;
		assertNull(config.getIntervalBetweenChecks());
		assertNull(config.getPolicyRules());
		assertTrue(!config.getJiraProjects().isEmpty());
		for (final JiraProject proj : config.getJiraProjects()) {
			assertEquals(JiraConfigErrors.JIRA_PROJECT_MISSING_ISSUE_TYPES_ERROR, proj.getProjectError());
		}

		assertNull(config.getHubProjects());
		assertNull(config.getHubProjectMappings());

		assertNull(config.getErrorMessage());
		assertNull(config.getIntervalBetweenChecksError());
		assertNull(config.getPolicyRulesError());
		assertNull(config.getHubProjectMappingError());
		assertTrue(!config.hasErrors());
	}

	@Test
	public void testGetJiraProjectsMultipleProjectsWithOutIssuesTypes() {
		final UserManagerMock managerMock = new UserManagerMock();
		managerMock.setRemoteUsername("User");
		managerMock.setIsSystemAdmin(true);
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();
		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();

		projectManagerMock.setProjectObjects(ProjectManagerMock.getTestProjectObjectsWithoutIssueTypes());

		final HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		final Response response = controller.getJiraProjects(requestMock);
		assertNotNull(response);
		final Object configObject = response.getEntity();
		assertNotNull(configObject);
		final HubJiraConfigSerializable config = (HubJiraConfigSerializable) configObject;
		assertNull(config.getIntervalBetweenChecks());
		assertNull(config.getPolicyRules());
		assertTrue(!config.getJiraProjects().isEmpty());
		for (final JiraProject proj : config.getJiraProjects()) {
			assertEquals(JiraConfigErrors.JIRA_PROJECT_NO_ISSUE_TYPES_FOUND_ERROR, proj.getProjectError());
		}

		assertNull(config.getHubProjects());
		assertNull(config.getHubProjectMappings());

		assertNull(config.getErrorMessage());
		assertNull(config.getIntervalBetweenChecksError());
		assertNull(config.getPolicyRulesError());
		assertNull(config.getHubProjectMappingError());
		assertTrue(!config.hasErrors());
	}

	@Test
	public void testGetJiraProjectsMultipleProjectsNullIssuesTypes() {
		final UserManagerMock managerMock = new UserManagerMock();
		managerMock.setRemoteUsername("User");
		managerMock.setIsSystemAdmin(true);
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();
		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();

		projectManagerMock.setProjectObjects(ProjectManagerMock.getTestProjectObjectsNullIssueTypes());

		final HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		final Response response = controller.getJiraProjects(requestMock);
		assertNotNull(response);
		final Object configObject = response.getEntity();
		assertNotNull(configObject);
		final HubJiraConfigSerializable config = (HubJiraConfigSerializable) configObject;
		assertNull(config.getIntervalBetweenChecks());
		assertNull(config.getPolicyRules());
		assertTrue(!config.getJiraProjects().isEmpty());
		for (final JiraProject proj : config.getJiraProjects()) {
			assertEquals(JiraConfigErrors.JIRA_PROJECT_NO_ISSUE_TYPES_FOUND_ERROR, proj.getProjectError());
		}

		assertNull(config.getHubProjects());
		assertNull(config.getHubProjectMappings());

		assertNull(config.getErrorMessage());
		assertNull(config.getIntervalBetweenChecksError());
		assertNull(config.getPolicyRulesError());
		assertNull(config.getHubProjectMappingError());
		assertTrue(!config.hasErrors());
	}

	@Test
	public void testGetHubProjectsNullUser() {
		final UserManagerMock managerMock = new UserManagerMock();
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();
		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();

		final HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		final Response response = controller.getHubProjects(requestMock);
		assertNotNull(response);
		assertEquals(Integer.valueOf(Status.UNAUTHORIZED.getStatusCode()), Integer.valueOf(response.getStatus()));
	}

	@Test
	public void testGetHubProjectsNotAdmin() {
		final UserManagerMock managerMock = new UserManagerMock();
		managerMock.setRemoteUsername("User");
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();
		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();

		final HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		final Response response = controller.getHubProjects(requestMock);
		assertNotNull(response);
		assertEquals(Integer.valueOf(Status.UNAUTHORIZED.getStatusCode()), Integer.valueOf(response.getStatus()));
	}

	@Test
	public void testGetHubProjectsPartialServerConfig() {
		final UserManagerMock managerMock = new UserManagerMock();
		managerMock.setRemoteUsername("User");
		managerMock.setIsSystemAdmin(true);
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();

		final PluginSettings settings = settingsFactory.createGlobalSettings();
		settings.put(HubConfigKeys.CONFIG_HUB_URL, "Test Server Url");
		settings.put(HubConfigKeys.CONFIG_HUB_USER, "Test User");
		settings.put(HubConfigKeys.CONFIG_HUB_TIMEOUT, "300");

		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();

		final HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		final Response response = controller.getHubProjects(requestMock);
		assertNotNull(response);
		final Object configObject = response.getEntity();
		assertNotNull(configObject);
		final HubJiraConfigSerializable config = (HubJiraConfigSerializable) configObject;

		assertNull(config.getIntervalBetweenChecks());
		assertNull(config.getPolicyRules());
		assertNull(config.getJiraProjects());
		assertTrue(config.getHubProjects().isEmpty());
		assertNull(config.getHubProjectMappings());

		assertEquals(JiraConfigErrors.HUB_SERVER_MISCONFIGURATION
				+ JiraConfigErrors.CHECK_HUB_SERVER_CONFIGURATION, config.getErrorMessage());
		assertNull(config.getIntervalBetweenChecksError());
		assertNull(config.getPolicyRulesError());
		assertNull(config.getHubProjectMappingError());
		assertTrue(config.hasErrors());
	}

	@Test
	public void testGetHubProjectsNoHubProjects() throws Exception {
		final UserManagerMock managerMock = new UserManagerMock();
		managerMock.setRemoteUsername("User");
		managerMock.setIsSystemAdmin(true);
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();

		final PluginSettings settings = settingsFactory.createGlobalSettings();
		settings.put(HubConfigKeys.CONFIG_HUB_URL, "http://www.google.com");
		settings.put(HubConfigKeys.CONFIG_HUB_USER, "Test User");
		settings.put(HubConfigKeys.CONFIG_HUB_PASS, PasswordEncrypter.encrypt("Test"));
		settings.put(HubConfigKeys.CONFIG_HUB_PASS_LENGTH, "4");
		settings.put(HubConfigKeys.CONFIG_HUB_TIMEOUT, "300");

		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();

		HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		controller = Mockito.spy(controller);

		final HubIntRestService restServiceMock = Mockito.mock(HubIntRestService.class);

		final List<ProjectItem> emptyHubProjects = new ArrayList<ProjectItem>();
		Mockito.doReturn(emptyHubProjects).when(restServiceMock).getProjectMatches(Mockito.anyString());
		// Mockito.doReturn(getHubProjects()).when(restServiceMock).getProjectMatches(Mockito.anyString());

		Mockito.doReturn(restServiceMock).when(controller).getHubRestService(Mockito.any(PluginSettings.class),
				Mockito.any(HubJiraConfigSerializable.class));

		final Response response = controller.getHubProjects(requestMock);
		assertNotNull(response);
		final Object configObject = response.getEntity();
		assertNotNull(configObject);
		final HubJiraConfigSerializable config = (HubJiraConfigSerializable) configObject;

		assertNull(config.getIntervalBetweenChecks());
		assertNull(config.getPolicyRules());
		assertNull(config.getJiraProjects());
		assertTrue(config.getHubProjects().isEmpty());
		assertNull(config.getHubProjectMappings());

		assertNull(config.getErrorMessage());
		assertNull(config.getIntervalBetweenChecksError());
		assertNull(config.getPolicyRulesError());
		assertNull(config.getHubProjectMappingError());
		assertTrue(config.hasErrors());
		assertEquals(JiraConfigErrors.NO_HUB_PROJECTS_FOUND, config.getHubProjectsError());
	}

	@Test
	public void testGetHubProjectsHasHubProjects() throws Exception {
		final UserManagerMock managerMock = new UserManagerMock();
		managerMock.setRemoteUsername("User");
		managerMock.setIsSystemAdmin(true);
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();

		final PluginSettings settings = settingsFactory.createGlobalSettings();
		settings.put(HubConfigKeys.CONFIG_HUB_URL, "http://www.google.com");
		settings.put(HubConfigKeys.CONFIG_HUB_USER, "Test User");
		settings.put(HubConfigKeys.CONFIG_HUB_PASS, PasswordEncrypter.encrypt("Test"));
		settings.put(HubConfigKeys.CONFIG_HUB_PASS_LENGTH, "4");
		settings.put(HubConfigKeys.CONFIG_HUB_TIMEOUT, "300");

		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();

		HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		controller = Mockito.spy(controller);

		final HubIntRestService restServiceMock = Mockito.mock(HubIntRestService.class);

		Mockito.doReturn(getHubProjects()).when(restServiceMock).getProjectMatches(Mockito.anyString());

		Mockito.doReturn(restServiceMock).when(controller).getHubRestService(Mockito.any(PluginSettings.class),
				Mockito.any(HubJiraConfigSerializable.class));

		final Response response = controller.getHubProjects(requestMock);
		assertNotNull(response);
		final Object configObject = response.getEntity();
		assertNotNull(configObject);
		final HubJiraConfigSerializable config = (HubJiraConfigSerializable) configObject;

		assertNull(config.getIntervalBetweenChecks());
		assertNull(config.getPolicyRules());
		assertNull(config.getJiraProjects());
		assertTrue(!config.getHubProjects().isEmpty());
		assertNull(config.getHubProjectMappings());

		assertNull(config.getErrorMessage());
		assertNull(config.getIntervalBetweenChecksError());
		assertNull(config.getPolicyRulesError());
		assertNull(config.getHubProjectMappingError());
		assertTrue(!config.hasErrors());
	}

	@Test
	public void testGetHubProjectsHasHubProjectsInGroup() throws Exception {
		final UserManagerMock managerMock = new UserManagerMock();
		managerMock.setRemoteUsername("User");
		managerMock.setInGroup(true);
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();

		final PluginSettings settings = settingsFactory.createGlobalSettings();
		settings.put(HubConfigKeys.CONFIG_HUB_URL, "http://www.google.com");
		settings.put(HubConfigKeys.CONFIG_HUB_USER, "Test User");
		settings.put(HubConfigKeys.CONFIG_HUB_PASS, PasswordEncrypter.encrypt("Test"));
		settings.put(HubConfigKeys.CONFIG_HUB_PASS_LENGTH, "4");
		settings.put(HubConfigKeys.CONFIG_HUB_TIMEOUT, "300");

		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();

		HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		controller = Mockito.spy(controller);

		final HubIntRestService restServiceMock = Mockito.mock(HubIntRestService.class);

		Mockito.doReturn(getHubProjects()).when(restServiceMock).getProjectMatches(Mockito.anyString());

		Mockito.doReturn(restServiceMock).when(controller).getHubRestService(Mockito.any(PluginSettings.class),
				Mockito.any(HubJiraConfigSerializable.class));

		final Response response = controller.getHubProjects(requestMock);
		assertNotNull(response);
		final Object configObject = response.getEntity();
		assertNotNull(configObject);
		final HubJiraConfigSerializable config = (HubJiraConfigSerializable) configObject;

		assertNull(config.getIntervalBetweenChecks());
		assertNull(config.getPolicyRules());
		assertNull(config.getJiraProjects());
		assertTrue(!config.getHubProjects().isEmpty());
		assertNull(config.getHubProjectMappings());

		assertNull(config.getErrorMessage());
		assertNull(config.getIntervalBetweenChecksError());
		assertNull(config.getPolicyRulesError());
		assertNull(config.getHubProjectMappingError());
		assertTrue(!config.hasErrors());
	}

	@Test
	public void testGetMappingsNullUser() {
		final UserManagerMock managerMock = new UserManagerMock();
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();
		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();

		final HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		final Response response = controller.getMappings(requestMock);
		assertNotNull(response);
		assertEquals(Integer.valueOf(Status.UNAUTHORIZED.getStatusCode()), Integer.valueOf(response.getStatus()));
	}

	@Test
	public void testGetMappingsNotAdmin() {
		final UserManagerMock managerMock = new UserManagerMock();
		managerMock.setRemoteUsername("User");
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();
		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();

		final HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		final Response response = controller.getMappings(requestMock);
		assertNotNull(response);
		assertEquals(Integer.valueOf(Status.UNAUTHORIZED.getStatusCode()), Integer.valueOf(response.getStatus()));
	}

	@Test
	public void testGetMappingsNoMappings() {
		final UserManagerMock managerMock = new UserManagerMock();
		managerMock.setRemoteUsername("User");
		managerMock.setIsSystemAdmin(true);
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();

		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();

		projectManagerMock.setProjectObjects(ProjectManagerMock.getTestProjectObjectsWithTaskIssueType());

		final HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		final Response response = controller.getMappings(requestMock);
		assertNotNull(response);
		final Object configObject = response.getEntity();
		assertNotNull(configObject);
		final HubJiraConfigSerializable config = (HubJiraConfigSerializable) configObject;

		assertNull(config.getIntervalBetweenChecks());
		assertNull(config.getPolicyRules());
		assertNull(config.getJiraProjects());
		assertNull(config.getHubProjects());
		assertNull(config.getHubProjectMappings());

		assertNull(config.getErrorMessage());
		assertNull(config.getIntervalBetweenChecksError());
		assertNull(config.getPolicyRulesError());
		assertNull(config.getHubProjectMappingError());
		assertTrue(!config.hasErrors());
	}

	@Test
	public void testGetMappingsWithMappings() {
		final Set<HubProjectMapping> mappings = getMappings();

		final UserManagerMock managerMock = new UserManagerMock();
		managerMock.setRemoteUsername("User");
		managerMock.setIsSystemAdmin(true);
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();

		final Gson gson = new GsonBuilder().create();
		final PluginSettings settings = settingsFactory.createGlobalSettings();
		settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_PROJECT_MAPPINGS_JSON, gson.toJson(mappings));

		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();

		projectManagerMock.setProjectObjects(ProjectManagerMock.getTestProjectObjectsWithTaskIssueType());

		final HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		final Response response = controller.getMappings(requestMock);
		assertNotNull(response);
		final Object configObject = response.getEntity();
		assertNotNull(configObject);
		final HubJiraConfigSerializable config = (HubJiraConfigSerializable) configObject;

		assertNull(config.getIntervalBetweenChecks());
		assertNull(config.getPolicyRules());
		assertNull(config.getJiraProjects());
		assertNull(config.getHubProjects());
		assertTrue(!config.getHubProjectMappings().isEmpty());

		assertEquals(mappings, config.getHubProjectMappings());

		assertNull(config.getErrorMessage());
		assertNull(config.getIntervalBetweenChecksError());
		assertNull(config.getPolicyRulesError());
		assertNull(config.getHubProjectMappingError());
		assertTrue(!config.hasErrors());
	}

	@Test
	public void testGetMappingsWithMappingsInGroup() {
		final Set<HubProjectMapping> mappings = getMappings();

		final UserManagerMock managerMock = new UserManagerMock();
		managerMock.setRemoteUsername("User");
		managerMock.setInGroup(true);
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();

		final Gson gson = new GsonBuilder().create();
		final PluginSettings settings = settingsFactory.createGlobalSettings();
		settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_PROJECT_MAPPINGS_JSON, gson.toJson(mappings));

		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();

		projectManagerMock.setProjectObjects(ProjectManagerMock.getTestProjectObjectsWithTaskIssueType());

		final HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		final Response response = controller.getMappings(requestMock);
		assertNotNull(response);
		final Object configObject = response.getEntity();
		assertNotNull(configObject);
		final HubJiraConfigSerializable config = (HubJiraConfigSerializable) configObject;

		assertNull(config.getIntervalBetweenChecks());
		assertNull(config.getPolicyRules());
		assertNull(config.getJiraProjects());
		assertNull(config.getHubProjects());
		assertTrue(!config.getHubProjectMappings().isEmpty());

		assertEquals(mappings, config.getHubProjectMappings());

		assertNull(config.getErrorMessage());
		assertNull(config.getIntervalBetweenChecksError());
		assertNull(config.getPolicyRulesError());
		assertNull(config.getHubProjectMappingError());
		assertTrue(!config.hasErrors());
	}


	@Test
	public void testSaveConfigNullUser() {
		final UserManagerMock managerMock = new UserManagerMock();
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();
		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();

		final HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		final HubJiraConfigSerializable config = new HubJiraConfigSerializable();

		final Response response = controller.put(config, requestMock);
		assertNotNull(response);
		assertEquals(Integer.valueOf(Status.UNAUTHORIZED.getStatusCode()), Integer.valueOf(response.getStatus()));
	}

	@Test
	public void testSaveConfigNotAdmin() {
		final UserManagerMock managerMock = new UserManagerMock();
		managerMock.setRemoteUsername("User");
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();
		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();

		final HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		final HubJiraConfigSerializable config = new HubJiraConfigSerializable();

		final Response response = controller.put(config, requestMock);
		assertNotNull(response);
		assertEquals(Integer.valueOf(Status.UNAUTHORIZED.getStatusCode()), Integer.valueOf(response.getStatus()));
	}

	@Test
	public void testSaveConfigEmptyNoServerConfig() {
		final UserManagerMock managerMock = new UserManagerMock();
		managerMock.setRemoteUsername("User");
		managerMock.setIsSystemAdmin(true);
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();
		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();

		final HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		HubJiraConfigSerializable config = new HubJiraConfigSerializable();

		final Response response = controller.put(config, requestMock);
		assertNotNull(response);
		final Object configObject = response.getEntity();
		assertNotNull(configObject);
		config = (HubJiraConfigSerializable) configObject;

		assertEquals(JiraConfigErrors.HUB_CONFIG_PLUGIN_MISSING, config.getErrorMessage());
		assertEquals(JiraConfigErrors.NO_INTERVAL_FOUND_ERROR, config.getIntervalBetweenChecksError());
		assertNull(config.getHubProjectMappingError());
		assertNull(config.getPolicyRulesError());
		assertNull(config.getIntervalBetweenChecks());
		assertTrue(config.getJiraProjects().isEmpty());
		assertTrue(config.getHubProjects().isEmpty());
		assertNull(config.getHubProjectMappings());
		assertNull(config.getPolicyRules());
		assertTrue(config.hasErrors());
	}

	@Test
	public void testSaveConfigEmpty() throws Exception {
		final UserManagerMock managerMock = new UserManagerMock();
		managerMock.setRemoteUsername("User");
		managerMock.setIsSystemAdmin(true);
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();
		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();
		projectManagerMock.setProjectObjects(ProjectManagerMock.getTestProjectObjectsWithTaskIssueType());

		HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		HubJiraConfigSerializable config = new HubJiraConfigSerializable();

		final PluginSettings settings = settingsFactory.createGlobalSettings();
		settings.put(HubConfigKeys.CONFIG_HUB_URL, "http://www.google.com");
		settings.put(HubConfigKeys.CONFIG_HUB_USER, "Test User");
		settings.put(HubConfigKeys.CONFIG_HUB_PASS, PasswordEncrypter.encrypt("Test"));
		settings.put(HubConfigKeys.CONFIG_HUB_PASS_LENGTH, "4");
		settings.put(HubConfigKeys.CONFIG_HUB_TIMEOUT, "300");

		controller = Mockito.spy(controller);

		final HubItemsService<PolicyRule> policyServiceMock = Mockito.mock(HubItemsService.class);

		Mockito.doReturn(getHubPolicies()).when(policyServiceMock).httpGetItemList(Mockito.anyList(), Mockito.anySet());

		Mockito.doReturn(policyServiceMock).when(controller).getPolicyService(Mockito.any(RestConnection.class));

		final HubIntRestService restServiceMock = Mockito.mock(HubIntRestService.class);

		Mockito.doReturn(getHubProjects()).when(restServiceMock).getProjectMatches(Mockito.anyString());

		Mockito.doReturn("3.1.0").when(restServiceMock).getHubVersion();

		Mockito.doReturn(restServiceMock).when(controller).getHubRestService(Mockito.any(PluginSettings.class),
				Mockito.any(HubJiraConfigSerializable.class));

		final Response response = controller.put(config, requestMock);
		assertNotNull(response);
		final Object configObject = response.getEntity();
		assertNotNull(configObject);
		config = (HubJiraConfigSerializable) configObject;

		assertNull(config.getErrorMessage());
		assertEquals(JiraConfigErrors.NO_INTERVAL_FOUND_ERROR, config.getIntervalBetweenChecksError());
		assertNull(config.getHubProjectMappingError());
		assertNull(config.getPolicyRulesError());
		assertNull(config.getIntervalBetweenChecks());
		assertTrue(!config.getJiraProjects().isEmpty());
		assertTrue(!config.getHubProjects().isEmpty());
		assertNull(config.getHubProjectMappings());
		assertNull(config.getPolicyRules());
		assertTrue(config.hasErrors());
	}

	@Test
	public void testSaveConfigResetToBlank() throws Exception {
		final String intervalBetweenChecks = "30";

		final Set<HubProjectMapping> mappings = getMappings();

		final List<PolicyRuleSerializable> jiraPolices = getJiraPolicies();

		final UserManagerMock managerMock = new UserManagerMock();
		managerMock.setRemoteUsername("User");
		managerMock.setIsSystemAdmin(true);
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();
		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();
		projectManagerMock.setProjectObjects(ProjectManagerMock.getTestProjectObjectsWithTaskIssueType());

		HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		HubJiraConfigSerializable config = new HubJiraConfigSerializable();

		final Gson gson = new GsonBuilder().create();
		final PluginSettings settings = settingsFactory.createGlobalSettings();
		settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS, intervalBetweenChecks);
		settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_PROJECT_MAPPINGS_JSON, gson.toJson(mappings));
		settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_POLICY_RULES_JSON, gson.toJson(jiraPolices));
		settings.put(HubConfigKeys.CONFIG_HUB_URL, "http://www.google.com");
		settings.put(HubConfigKeys.CONFIG_HUB_USER, "Test User");
		settings.put(HubConfigKeys.CONFIG_HUB_PASS, PasswordEncrypter.encrypt("Test"));
		settings.put(HubConfigKeys.CONFIG_HUB_PASS_LENGTH, "4");
		settings.put(HubConfigKeys.CONFIG_HUB_TIMEOUT, "300");

		controller = Mockito.spy(controller);

		final HubItemsService<PolicyRule> policyServiceMock = Mockito.mock(HubItemsService.class);

		Mockito.doReturn(getHubPolicies()).when(policyServiceMock).httpGetItemList(Mockito.anyList(), Mockito.anySet());

		Mockito.doReturn(policyServiceMock).when(controller).getPolicyService(Mockito.any(RestConnection.class));

		final HubIntRestService restServiceMock = Mockito.mock(HubIntRestService.class);

		Mockito.doReturn(getHubProjects()).when(restServiceMock).getProjectMatches(Mockito.anyString());

		Mockito.doReturn("3.1.0").when(restServiceMock).getHubVersion();

		Mockito.doReturn(restServiceMock).when(controller).getHubRestService(Mockito.any(PluginSettings.class),
				Mockito.any(HubJiraConfigSerializable.class));

		final Response response = controller.put(config, requestMock);
		assertNotNull(response);
		final Object configObject = response.getEntity();
		assertNotNull(configObject);
		config = (HubJiraConfigSerializable) configObject;

		assertNull(config.getErrorMessage());
		assertEquals(JiraConfigErrors.NO_INTERVAL_FOUND_ERROR, config.getIntervalBetweenChecksError());
		assertNull(config.getHubProjectMappingError());
		assertNull(config.getPolicyRulesError());
		assertNull(config.getIntervalBetweenChecks());
		assertTrue(!config.getJiraProjects().isEmpty());
		assertTrue(!config.getHubProjects().isEmpty());
		assertNull(config.getHubProjectMappings());
		assertNull(config.getPolicyRules());
		assertTrue(config.hasErrors());

	}


	@Test
	public void testSaveConfigNoUpdate() throws Exception {
		final String intervalBetweenChecks = "30";

		final Set<HubProjectMapping> mappings = getMappings();

		final List<PolicyRuleSerializable> jiraPolices = getJiraPolicies();

		for (final PolicyRuleSerializable policyRule : jiraPolices) {
			policyRule.setChecked(false);
		}

		final UserManagerMock managerMock = new UserManagerMock();
		managerMock.setRemoteUsername("User");
		managerMock.setIsSystemAdmin(true);
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();
		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();
		projectManagerMock.setProjectObjects(ProjectManagerMock.getTestProjectObjectsWithTaskIssueType());

		HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		final HubJiraConfigSerializable config = new HubJiraConfigSerializable();
		config.setIntervalBetweenChecks(intervalBetweenChecks);
		config.setHubProjectMappings(mappings);
		config.setPolicyRules(jiraPolices);

		final Gson gson = new GsonBuilder().create();
		PluginSettings settings = settingsFactory.createGlobalSettings();
		settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS, intervalBetweenChecks);
		settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_PROJECT_MAPPINGS_JSON, gson.toJson(mappings));
		settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_POLICY_RULES_JSON, gson.toJson(jiraPolices));
		settings.put(HubConfigKeys.CONFIG_HUB_URL, "http://www.google.com");
		settings.put(HubConfigKeys.CONFIG_HUB_USER, "Test User");
		settings.put(HubConfigKeys.CONFIG_HUB_PASS, PasswordEncrypter.encrypt("Test"));
		settings.put(HubConfigKeys.CONFIG_HUB_PASS_LENGTH, "4");
		settings.put(HubConfigKeys.CONFIG_HUB_TIMEOUT, "300");

		controller = Mockito.spy(controller);

		final HubItemsService<PolicyRule> policyServiceMock = Mockito.mock(HubItemsService.class);

		Mockito.doReturn(getHubPolicies()).when(policyServiceMock).httpGetItemList(Mockito.anyList(), Mockito.anySet());

		Mockito.doReturn(policyServiceMock).when(controller).getPolicyService(Mockito.any(RestConnection.class));

		final HubIntRestService restServiceMock = Mockito.mock(HubIntRestService.class);

		Mockito.doReturn(getHubProjects()).when(restServiceMock).getProjectMatches(Mockito.anyString());

		Mockito.doReturn("3.1.0").when(restServiceMock).getHubVersion();

		Mockito.doReturn(restServiceMock).when(controller).getHubRestService(Mockito.any(PluginSettings.class),
				Mockito.any(HubJiraConfigSerializable.class));

		final Response response = controller.put(config, requestMock);
		assertNotNull(response);
		final Object configObject = response.getEntity();
		assertNull(configObject);

		settings = settingsFactory.createGlobalSettings();
		assertEquals(intervalBetweenChecks, settings.get(HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS));
		assertEquals(gson.toJson(mappings), settings.get(HubJiraConfigKeys.HUB_CONFIG_JIRA_PROJECT_MAPPINGS_JSON));
		assertEquals(gson.toJson(jiraPolices), settings.get(HubJiraConfigKeys.HUB_CONFIG_JIRA_POLICY_RULES_JSON));
		assertEquals("User", settings.get(HubJiraConfigKeys.HUB_CONFIG_JIRA_USER));
	}

	@Test
	public void testSaveConfigNoUpdateInGroup() throws Exception {
		final String intervalBetweenChecks = "30";

		final Set<HubProjectMapping> mappings = getMappings();

		final List<PolicyRuleSerializable> jiraPolices = getJiraPolicies();

		for (final PolicyRuleSerializable policyRule : jiraPolices) {
			policyRule.setChecked(false);
		}

		final UserManagerMock managerMock = new UserManagerMock();
		managerMock.setRemoteUsername("User");
		managerMock.setInGroup(true);
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();
		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();
		projectManagerMock.setProjectObjects(ProjectManagerMock.getTestProjectObjectsWithTaskIssueType());

		HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		final HubJiraConfigSerializable config = new HubJiraConfigSerializable();
		config.setIntervalBetweenChecks(intervalBetweenChecks);
		config.setHubProjectMappings(mappings);
		config.setPolicyRules(jiraPolices);

		final Gson gson = new GsonBuilder().create();
		PluginSettings settings = settingsFactory.createGlobalSettings();
		settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS, intervalBetweenChecks);
		settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_PROJECT_MAPPINGS_JSON, gson.toJson(mappings));
		settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_POLICY_RULES_JSON, gson.toJson(jiraPolices));
		settings.put(HubConfigKeys.CONFIG_HUB_URL, "http://www.google.com");
		settings.put(HubConfigKeys.CONFIG_HUB_USER, "Test User");
		settings.put(HubConfigKeys.CONFIG_HUB_PASS, PasswordEncrypter.encrypt("Test"));
		settings.put(HubConfigKeys.CONFIG_HUB_PASS_LENGTH, "4");
		settings.put(HubConfigKeys.CONFIG_HUB_TIMEOUT, "300");

		controller = Mockito.spy(controller);

		final HubItemsService<PolicyRule> policyServiceMock = Mockito.mock(HubItemsService.class);

		Mockito.doReturn(getHubPolicies()).when(policyServiceMock).httpGetItemList(Mockito.anyList(), Mockito.anySet());

		Mockito.doReturn(policyServiceMock).when(controller).getPolicyService(Mockito.any(RestConnection.class));

		final HubIntRestService restServiceMock = Mockito.mock(HubIntRestService.class);

		Mockito.doReturn(getHubProjects()).when(restServiceMock).getProjectMatches(Mockito.anyString());

		Mockito.doReturn("3.1.0").when(restServiceMock).getHubVersion();

		Mockito.doReturn(restServiceMock).when(controller).getHubRestService(Mockito.any(PluginSettings.class),
				Mockito.any(HubJiraConfigSerializable.class));

		final Response response = controller.put(config, requestMock);
		assertNotNull(response);
		final Object configObject = response.getEntity();
		assertNull(configObject);

		settings = settingsFactory.createGlobalSettings();
		assertEquals(intervalBetweenChecks, settings.get(HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS));
		assertEquals(gson.toJson(mappings), settings.get(HubJiraConfigKeys.HUB_CONFIG_JIRA_PROJECT_MAPPINGS_JSON));
		assertEquals(gson.toJson(jiraPolices), settings.get(HubJiraConfigKeys.HUB_CONFIG_JIRA_POLICY_RULES_JSON));
		assertEquals("User", settings.get(HubJiraConfigKeys.HUB_CONFIG_JIRA_USER));
	}

	@Test
	public void testSaveConfigEmptyInterval() throws Exception {
		final String intervalBetweenChecks = "";

		final Set<HubProjectMapping> mappings = getMappings();

		final List<PolicyRuleSerializable> jiraPolices = getJiraPolicies();

		final UserManagerMock managerMock = new UserManagerMock();
		managerMock.setRemoteUsername("User");
		managerMock.setIsSystemAdmin(true);
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();
		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();
		projectManagerMock.setProjectObjects(ProjectManagerMock.getTestProjectObjectsWithTaskIssueType());

		HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		HubJiraConfigSerializable config = new HubJiraConfigSerializable();
		config.setIntervalBetweenChecks(intervalBetweenChecks);
		config.setHubProjectMappings(mappings);
		config.setPolicyRules(jiraPolices);

		final Gson gson = new GsonBuilder().create();
		PluginSettings settings = settingsFactory.createGlobalSettings();
		settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS, "30");
		settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_PROJECT_MAPPINGS_JSON, gson.toJson(mappings));
		settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_POLICY_RULES_JSON, gson.toJson(jiraPolices));
		settings.put(HubConfigKeys.CONFIG_HUB_URL, "http://www.google.com");
		settings.put(HubConfigKeys.CONFIG_HUB_USER, "Test User");
		settings.put(HubConfigKeys.CONFIG_HUB_PASS, PasswordEncrypter.encrypt("Test"));
		settings.put(HubConfigKeys.CONFIG_HUB_PASS_LENGTH, "4");
		settings.put(HubConfigKeys.CONFIG_HUB_TIMEOUT, "300");

		controller = Mockito.spy(controller);

		final HubItemsService<PolicyRule> policyServiceMock = Mockito.mock(HubItemsService.class);

		Mockito.doReturn(getHubPolicies()).when(policyServiceMock).httpGetItemList(Mockito.anyList(), Mockito.anySet());

		Mockito.doReturn(policyServiceMock).when(controller).getPolicyService(Mockito.any(RestConnection.class));

		final HubIntRestService restServiceMock = Mockito.mock(HubIntRestService.class);

		Mockito.doReturn(getHubProjects()).when(restServiceMock).getProjectMatches(Mockito.anyString());

		Mockito.doReturn("3.1.0").when(restServiceMock).getHubVersion();

		Mockito.doReturn(restServiceMock).when(controller).getHubRestService(Mockito.any(PluginSettings.class),
				Mockito.any(HubJiraConfigSerializable.class));

		final Response response = controller.put(config, requestMock);
		assertNotNull(response);
		final Object configObject = response.getEntity();
		assertNotNull(configObject);
		config = (HubJiraConfigSerializable) configObject;

		assertNull(config.getErrorMessage());
		assertEquals(JiraConfigErrors.NO_INTERVAL_FOUND_ERROR, config.getIntervalBetweenChecksError());
		assertEquals(intervalBetweenChecks, config.getIntervalBetweenChecks());
		assertNull(config.getHubProjectMappingError());
		assertNull(config.getPolicyRulesError());
		assertEquals(intervalBetweenChecks, config.getIntervalBetweenChecks());
		assertTrue(!config.getJiraProjects().isEmpty());
		assertTrue(!config.getHubProjects().isEmpty());
		assertTrue(!config.getHubProjectMappings().isEmpty());
		assertTrue(!config.getPolicyRules().isEmpty());
		assertEquals(mappings, config.getHubProjectMappings());
		assertTrue(config.hasErrors());

		settings = settingsFactory.createGlobalSettings();
		assertEquals(intervalBetweenChecks, settings.get(HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS));
		assertEquals(gson.toJson(mappings), settings.get(HubJiraConfigKeys.HUB_CONFIG_JIRA_PROJECT_MAPPINGS_JSON));
		assertEquals(gson.toJson(jiraPolices), settings.get(HubJiraConfigKeys.HUB_CONFIG_JIRA_POLICY_RULES_JSON));
		assertEquals("User", settings.get(HubJiraConfigKeys.HUB_CONFIG_JIRA_USER));
	}

	@Test
	public void testSaveConfigInvalidInterval() throws Exception {
		final String intervalBetweenChecks = "intervalBetweenChecks";

		final Set<HubProjectMapping> mappings = getMappings();

		final List<PolicyRuleSerializable> jiraPolices = getJiraPolicies();

		final UserManagerMock managerMock = new UserManagerMock();
		managerMock.setRemoteUsername("User");
		managerMock.setIsSystemAdmin(true);
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();
		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();
		projectManagerMock.setProjectObjects(ProjectManagerMock.getTestProjectObjectsWithTaskIssueType());

		HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		HubJiraConfigSerializable config = new HubJiraConfigSerializable();
		config.setIntervalBetweenChecks(intervalBetweenChecks);
		config.setHubProjectMappings(mappings);
		config.setPolicyRules(jiraPolices);

		final Gson gson = new GsonBuilder().create();
		PluginSettings settings = settingsFactory.createGlobalSettings();
		settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS, "30");
		settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_PROJECT_MAPPINGS_JSON, gson.toJson(mappings));
		settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_POLICY_RULES_JSON, gson.toJson(jiraPolices));
		settings.put(HubConfigKeys.CONFIG_HUB_URL, "http://www.google.com");
		settings.put(HubConfigKeys.CONFIG_HUB_USER, "Test User");
		settings.put(HubConfigKeys.CONFIG_HUB_PASS, PasswordEncrypter.encrypt("Test"));
		settings.put(HubConfigKeys.CONFIG_HUB_PASS_LENGTH, "4");
		settings.put(HubConfigKeys.CONFIG_HUB_TIMEOUT, "300");

		controller = Mockito.spy(controller);

		final HubItemsService<PolicyRule> policyServiceMock = Mockito.mock(HubItemsService.class);

		Mockito.doReturn(getHubPolicies()).when(policyServiceMock).httpGetItemList(Mockito.anyList(), Mockito.anySet());

		Mockito.doReturn(policyServiceMock).when(controller).getPolicyService(Mockito.any(RestConnection.class));

		final HubIntRestService restServiceMock = Mockito.mock(HubIntRestService.class);

		Mockito.doReturn(getHubProjects()).when(restServiceMock).getProjectMatches(Mockito.anyString());

		Mockito.doReturn("3.1.0").when(restServiceMock).getHubVersion();

		Mockito.doReturn(restServiceMock).when(controller).getHubRestService(Mockito.any(PluginSettings.class),
				Mockito.any(HubJiraConfigSerializable.class));

		final Response response = controller.put(config, requestMock);
		assertNotNull(response);
		final Object configObject = response.getEntity();
		assertNotNull(configObject);
		config = (HubJiraConfigSerializable) configObject;

		assertNull(config.getErrorMessage());
		assertEquals("The String : " + intervalBetweenChecks + " , is not an Integer.",
				config.getIntervalBetweenChecksError());
		assertEquals(intervalBetweenChecks, config.getIntervalBetweenChecks());
		assertNull(config.getHubProjectMappingError());
		assertNull(config.getPolicyRulesError());
		assertEquals(intervalBetweenChecks, config.getIntervalBetweenChecks());
		assertTrue(!config.getJiraProjects().isEmpty());
		assertTrue(!config.getHubProjects().isEmpty());
		assertTrue(!config.getHubProjectMappings().isEmpty());
		assertTrue(!config.getPolicyRules().isEmpty());
		assertEquals(mappings, config.getHubProjectMappings());
		assertTrue(config.hasErrors());

		settings = settingsFactory.createGlobalSettings();
		assertEquals(intervalBetweenChecks, settings.get(HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS));
		assertEquals(gson.toJson(mappings), settings.get(HubJiraConfigKeys.HUB_CONFIG_JIRA_PROJECT_MAPPINGS_JSON));
		assertEquals(gson.toJson(jiraPolices), settings.get(HubJiraConfigKeys.HUB_CONFIG_JIRA_POLICY_RULES_JSON));
		assertEquals("User", settings.get(HubJiraConfigKeys.HUB_CONFIG_JIRA_USER));
	}

	@Test
	public void testSaveConfigUpdate() throws Exception {
		final String intervalBetweenChecks = "30";

		final Set<HubProjectMapping> mappings = getMappings();

		final List<PolicyRuleSerializable> jiraPolices = getJiraPolicies();

		for (final PolicyRuleSerializable policyRule : jiraPolices) {
			policyRule.setChecked(false);
		}

		final UserManagerMock managerMock = new UserManagerMock();
		managerMock.setRemoteUsername("User");
		managerMock.setIsSystemAdmin(true);
		final PluginSettingsFactoryMock settingsFactory = new PluginSettingsFactoryMock();
		final TransactionTemplateMock transactionManager = new TransactionTemplateMock();
		final HttpServletRequestMock requestMock = new HttpServletRequestMock();
		final ProjectManagerMock projectManagerMock = new ProjectManagerMock();
		projectManagerMock.setProjectObjects(ProjectManagerMock.getTestProjectObjectsWithTaskIssueType());

		HubJiraConfigController controller = new HubJiraConfigController(managerMock, settingsFactory,
				transactionManager, projectManagerMock);

		final HubJiraConfigSerializable config = new HubJiraConfigSerializable();
		config.setIntervalBetweenChecks(intervalBetweenChecks);
		config.setHubProjectMappings(mappings);
		config.setPolicyRules(jiraPolices);

		final Gson gson = new GsonBuilder().create();
		PluginSettings settings = settingsFactory.createGlobalSettings();
		settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS, "560");
		settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_PROJECT_MAPPINGS_JSON, gson.toJson(mappings));
		settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_POLICY_RULES_JSON, gson.toJson(jiraPolices));
		settings.put(HubConfigKeys.CONFIG_HUB_URL, "http://www.google.com");
		settings.put(HubConfigKeys.CONFIG_HUB_USER, "Test User");
		settings.put(HubConfigKeys.CONFIG_HUB_PASS, PasswordEncrypter.encrypt("Test"));
		settings.put(HubConfigKeys.CONFIG_HUB_PASS_LENGTH, "4");
		settings.put(HubConfigKeys.CONFIG_HUB_TIMEOUT, "300");

		for (final PolicyRuleSerializable policyRule : jiraPolices) {
			policyRule.setChecked(true);
		}

		final JiraProject jiraProject = new JiraProject();
		jiraProject.setProjectName("Project");
		jiraProject.setProjectKey("ProjectKey");
		jiraProject.setProjectId(0L);
		jiraProject.setProjectError("");

		final HubProject hubProject = new HubProject();
		hubProject.setProjectName("HubProject");
		hubProject.setProjectUrl("projectURL");

		final HubProjectMapping newMapping = new HubProjectMapping();
		newMapping.setHubProject(hubProject);
		newMapping.setJiraProject(jiraProject);
		mappings.add(newMapping);

		controller = Mockito.spy(controller);

		final HubItemsService<PolicyRule> policyServiceMock = Mockito.mock(HubItemsService.class);

		Mockito.doReturn(getHubPolicies()).when(policyServiceMock).httpGetItemList(Mockito.anyList(), Mockito.anySet());

		Mockito.doReturn(policyServiceMock).when(controller).getPolicyService(Mockito.any(RestConnection.class));

		final HubIntRestService restServiceMock = Mockito.mock(HubIntRestService.class);

		Mockito.doReturn(getHubProjects()).when(restServiceMock).getProjectMatches(Mockito.anyString());

		Mockito.doReturn("3.1.0").when(restServiceMock).getHubVersion();

		Mockito.doReturn(restServiceMock).when(controller).getHubRestService(Mockito.any(PluginSettings.class),
				Mockito.any(HubJiraConfigSerializable.class));

		final Response response = controller.put(config, requestMock);
		assertNotNull(response);
		final Object configObject = response.getEntity();
		assertNull(configObject);

		settings = settingsFactory.createGlobalSettings();
		assertEquals(intervalBetweenChecks, settings.get(HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS));
		assertEquals(gson.toJson(mappings), settings.get(HubJiraConfigKeys.HUB_CONFIG_JIRA_PROJECT_MAPPINGS_JSON));
		assertEquals(gson.toJson(jiraPolices), settings.get(HubJiraConfigKeys.HUB_CONFIG_JIRA_POLICY_RULES_JSON));
		assertEquals("User", settings.get(HubJiraConfigKeys.HUB_CONFIG_JIRA_USER));
	}

}
