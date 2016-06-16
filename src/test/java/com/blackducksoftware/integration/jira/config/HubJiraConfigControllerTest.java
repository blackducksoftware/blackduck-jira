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
				null, null);

		final MetaInformation metaInfo2 = new MetaInformation(null, "policyURL2", null);
		final PolicyRule rule2 = new PolicyRule(metaInfo2, "PolicyRule2", "2TestDescription", null, null, null, null,
				null, null);

		final MetaInformation metaInfo3 = new MetaInformation(null, "policyURL3", null);
		final PolicyRule rule3 = new PolicyRule(metaInfo3, "PolicyRule3", "3TestDescription", null, null, null, null,
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
		jiraProject1.setProjectExists(true);

		final HubProject hubProject1 = new HubProject();
		hubProject1.setProjectName("HubProject1");
		hubProject1.setProjectUrl("projectURL1");
		hubProject1.setProjectExists(true);

		final JiraProject jiraProject2 = new JiraProject();
		jiraProject2.setProjectName("Project2");
		jiraProject2.setProjectKey("ProjectKey");
		jiraProject2.setProjectId(153L);
		jiraProject2.setProjectExists(true);

		final HubProject hubProject2 = new HubProject();
		hubProject2.setProjectName("HubProject2");
		hubProject2.setProjectUrl("projectURL2");
		hubProject2.setProjectExists(true);

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

	// @Test
	// public void testGetConfigNullUser() {
	// final UserManagerMock managerMock = new UserManagerMock();
	// final PluginSettingsFactoryMock settingsFactory = new
	// PluginSettingsFactoryMock();
	// final TransactionTemplateMock transactionManager = new
	// TransactionTemplateMock();
	// final HttpServletRequestMock requestMock = new HttpServletRequestMock();
	// final ProjectManagerMock projectManagerMock = new ProjectManagerMock();
	//
	// final HubJiraConfigController controller = new
	// HubJiraConfigController(managerMock, settingsFactory,
	// transactionManager, projectManagerMock);
	//
	// final Response response = controller.get(requestMock);
	// assertNotNull(response);
	// assertEquals(Integer.valueOf(Status.UNAUTHORIZED.getStatusCode()),
	// Integer.valueOf(response.getStatus()));
	// }
	//
	// @Test
	// public void testGetConfigNotAdmin() {
	// final UserManagerMock managerMock = new UserManagerMock();
	// managerMock.setRemoteUsername("User");
	// final PluginSettingsFactoryMock settingsFactory = new
	// PluginSettingsFactoryMock();
	// final TransactionTemplateMock transactionManager = new
	// TransactionTemplateMock();
	// final HttpServletRequestMock requestMock = new HttpServletRequestMock();
	// final ProjectManagerMock projectManagerMock = new ProjectManagerMock();
	//
	// final HubJiraConfigController controller = new
	// HubJiraConfigController(managerMock, settingsFactory,
	// transactionManager, projectManagerMock);
	//
	// final Response response = controller.get(requestMock);
	// assertNotNull(response);
	// assertEquals(Integer.valueOf(Status.UNAUTHORIZED.getStatusCode()),
	// Integer.valueOf(response.getStatus()));
	// }
	//
	// @Test
	// public void testGetConfigEmpty() {
	// final UserManagerMock managerMock = new UserManagerMock();
	// managerMock.setRemoteUsername("User");
	// managerMock.setIsSystemAdmin(true);
	// final PluginSettingsFactoryMock settingsFactory = new
	// PluginSettingsFactoryMock();
	// final TransactionTemplateMock transactionManager = new
	// TransactionTemplateMock();
	// final HttpServletRequestMock requestMock = new HttpServletRequestMock();
	// final ProjectManagerMock projectManagerMock = new ProjectManagerMock();
	//
	// final HubJiraConfigController controller = new
	// HubJiraConfigController(managerMock, settingsFactory,
	// transactionManager, projectManagerMock);
	//
	// final Response response = controller.get(requestMock);
	// assertNotNull(response);
	// final Object configObject = response.getEntity();
	// assertNotNull(configObject);
	// final HubJiraConfigSerializable config = (HubJiraConfigSerializable)
	// configObject;
	// assertNull(config.getIntervalBetweenChecks());
	// assertTrue(config.getPolicyRules().isEmpty());
	// assertTrue(config.getJiraProjects().isEmpty());
	// assertTrue(config.getHubProjects().isEmpty());
	// assertNull(config.getHubProjectMappings());
	//
	// assertEquals(HubJiraConfigController.HUB_CONFIG_PLUGIN_MISSING,
	// config.getErrorMessage());
	// assertNotNull(config.getIntervalBetweenChecksError());
	// assertEquals(HubJiraConfigController.NO_POLICY_RULES_FOUND_ERROR,
	// config.getPolicyRulesError());
	// assertNull(config.getHubProjectMappingError());
	// assertTrue(config.hasErrors());
	// }
	//
	// @Test
	// public void testGetConfigWithInvalidInterval() throws Exception {
	// final String intervalBetweenChecks = "intervalBetweenChecks";
	// final UserManagerMock managerMock = new UserManagerMock();
	// managerMock.setRemoteUsername("User");
	// managerMock.setIsSystemAdmin(true);
	// final PluginSettingsFactoryMock settingsFactory = new
	// PluginSettingsFactoryMock();
	//
	// final PluginSettings settings = settingsFactory.createGlobalSettings();
	// settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS,
	// intervalBetweenChecks);
	//
	// final TransactionTemplateMock transactionManager = new
	// TransactionTemplateMock();
	// final HttpServletRequestMock requestMock = new HttpServletRequestMock();
	// final ProjectManagerMock projectManagerMock = new ProjectManagerMock();
	//
	// final HubJiraConfigController controller = new
	// HubJiraConfigController(managerMock, settingsFactory,
	// transactionManager, projectManagerMock);
	//
	// final Response response = controller.get(requestMock);
	// assertNotNull(response);
	// final Object configObject = response.getEntity();
	// assertNotNull(configObject);
	// final HubJiraConfigSerializable config = (HubJiraConfigSerializable)
	// configObject;
	//
	// assertEquals(HubJiraConfigController.HUB_CONFIG_PLUGIN_MISSING,
	// config.getErrorMessage());
	// assertNull(config.getHubProjectMappingError());
	// assertEquals("The String : " + intervalBetweenChecks + " , is not an
	// Integer.",
	// config.getIntervalBetweenChecksError());
	//
	// assertTrue(config.hasErrors());
	// }
	//
	// @Test
	// public void testGetConfigWithEmptyMapping() throws Exception {
	// final String intervalBetweenChecks = "30";
	//
	// final HubProjectMapping mapping = new HubProjectMapping();
	//
	// final Set<HubProjectMapping> mappings = new HashSet<HubProjectMapping>();
	// mappings.add(mapping);
	//
	// final UserManagerMock managerMock = new UserManagerMock();
	// managerMock.setRemoteUsername("User");
	// managerMock.setIsSystemAdmin(true);
	// final PluginSettingsFactoryMock settingsFactory = new
	// PluginSettingsFactoryMock();
	//
	// final Gson gson = new GsonBuilder().create();
	// final PluginSettings settings = settingsFactory.createGlobalSettings();
	// settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS,
	// intervalBetweenChecks);
	// settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_PROJECT_MAPPINGS_JSON,
	// gson.toJson(mappings));
	//
	// final TransactionTemplateMock transactionManager = new
	// TransactionTemplateMock();
	// final HttpServletRequestMock requestMock = new HttpServletRequestMock();
	// final ProjectManagerMock projectManagerMock = new ProjectManagerMock();
	//
	// final HubJiraConfigController controller = new
	// HubJiraConfigController(managerMock, settingsFactory,
	// transactionManager, projectManagerMock);
	//
	// final Response response = controller.get(requestMock);
	// assertNotNull(response);
	// final Object configObject = response.getEntity();
	// assertNotNull(configObject);
	// final HubJiraConfigSerializable config = (HubJiraConfigSerializable)
	// configObject;
	//
	// assertEquals(HubJiraConfigController.HUB_CONFIG_PLUGIN_MISSING,
	// config.getErrorMessage());
	// assertEquals(HubJiraConfigController.MAPPING_HAS_EMPTY_ERROR,
	// config.getHubProjectMappingError());
	// assertEquals(intervalBetweenChecks, config.getIntervalBetweenChecks());
	// assertTrue(config.getJiraProjects().isEmpty());
	//
	// final Set<HubProjectMapping> configMappings =
	// config.getHubProjectMappings();
	// assertEquals(mappings, configMappings);
	//
	// assertTrue(config.hasErrors());
	// }
	//
	// @Test
	// public void testGetConfigWithOldMapping() throws Exception {
	// final String intervalBetweenChecks = "30";
	//
	// final JiraProject jiraProject = new JiraProject();
	// jiraProject.setProjectName("JiraProject");
	// jiraProject.setProjectKey("ProjectKey");
	// jiraProject.setProjectId(450L);
	// jiraProject.setProjectExists(true);
	//
	// final HubProject hubProject = new HubProject();
	// hubProject.setProjectName("HubProject");
	// hubProject.setProjectUrl("ProjectUrl");
	// hubProject.setProjectExists(true);
	//
	// final HubProjectMapping mapping = new HubProjectMapping();
	// mapping.setHubProject(hubProject);
	// mapping.setJiraProject(jiraProject);
	//
	// final Set<HubProjectMapping> mappings = new HashSet<HubProjectMapping>();
	// mappings.add(mapping);
	//
	// final UserManagerMock managerMock = new UserManagerMock();
	// managerMock.setRemoteUsername("User");
	// managerMock.setIsSystemAdmin(true);
	// final PluginSettingsFactoryMock settingsFactory = new
	// PluginSettingsFactoryMock();
	//
	// final Gson gson = new GsonBuilder().create();
	// final PluginSettings settings = settingsFactory.createGlobalSettings();
	// settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS,
	// intervalBetweenChecks);
	// settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_PROJECT_MAPPINGS_JSON,
	// gson.toJson(mappings));
	//
	// final TransactionTemplateMock transactionManager = new
	// TransactionTemplateMock();
	// final HttpServletRequestMock requestMock = new HttpServletRequestMock();
	// final ProjectManagerMock projectManagerMock = new ProjectManagerMock();
	//
	// final HubJiraConfigController controller = new
	// HubJiraConfigController(managerMock, settingsFactory,
	// transactionManager, projectManagerMock);
	//
	// final Response response = controller.get(requestMock);
	// assertNotNull(response);
	// final Object configObject = response.getEntity();
	// assertNotNull(configObject);
	// final HubJiraConfigSerializable config = (HubJiraConfigSerializable)
	// configObject;
	//
	// assertEquals(HubJiraConfigController.HUB_CONFIG_PLUGIN_MISSING,
	// config.getErrorMessage());
	// assertNull(config.getHubProjectMappingError());
	// assertEquals(intervalBetweenChecks, config.getIntervalBetweenChecks());
	// assertTrue(config.getJiraProjects().isEmpty());
	//
	// final HubProjectMapping configMapping =
	// config.getHubProjectMappings().iterator().next();
	// assertEquals(hubProject.getProjectName(),
	// configMapping.getHubProject().getProjectName());
	// assertEquals(hubProject.getProjectUrl(),
	// configMapping.getHubProject().getProjectUrl());
	// assertEquals(hubProject.getProjectExists(),
	// !configMapping.getHubProject().getProjectExists());
	//
	// assertEquals(jiraProject.getProjectName(),
	// configMapping.getJiraProject().getProjectName());
	// assertEquals(jiraProject.getProjectId(),
	// configMapping.getJiraProject().getProjectId());
	// assertEquals(jiraProject.getProjectExists(),
	// !configMapping.getJiraProject().getProjectExists());
	// assertEquals(jiraProject.getProjectKey(),
	// configMapping.getJiraProject().getProjectKey());
	//
	// assertTrue(config.hasErrors());
	// }
	//
	// @Test
	// public void testGetConfigNoServerSettingsConfigured() throws Exception {
	// final String intervalBetweenChecks = "30";
	//
	// final UserManagerMock managerMock = new UserManagerMock();
	// managerMock.setRemoteUsername("User");
	// managerMock.setIsSystemAdmin(true);
	// final PluginSettingsFactoryMock settingsFactory = new
	// PluginSettingsFactoryMock();
	//
	// final PluginSettings settings = settingsFactory.createGlobalSettings();
	// settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS,
	// intervalBetweenChecks);
	//
	// final TransactionTemplateMock transactionManager = new
	// TransactionTemplateMock();
	// final HttpServletRequestMock requestMock = new HttpServletRequestMock();
	// final ProjectManagerMock projectManagerMock = new ProjectManagerMock();
	//
	// final HubJiraConfigController controller = new
	// HubJiraConfigController(managerMock, settingsFactory,
	// transactionManager, projectManagerMock);
	//
	// final Response response = controller.get(requestMock);
	// assertNotNull(response);
	// final Object configObject = response.getEntity();
	// assertNotNull(configObject);
	// final HubJiraConfigSerializable config = (HubJiraConfigSerializable)
	// configObject;
	//
	// assertEquals(HubJiraConfigController.HUB_CONFIG_PLUGIN_MISSING,
	// config.getErrorMessage());
	// assertNull(config.getHubProjectMappingError());
	// assertEquals(intervalBetweenChecks, config.getIntervalBetweenChecks());
	//
	// assertTrue(config.hasErrors());
	// }
	//
	// @Test
	// public void testGetConfigWithPartialServerConfig() throws Exception {
	// final String intervalBetweenChecks = "30";
	//
	// final UserManagerMock managerMock = new UserManagerMock();
	// managerMock.setRemoteUsername("User");
	// managerMock.setIsSystemAdmin(true);
	// final PluginSettingsFactoryMock settingsFactory = new
	// PluginSettingsFactoryMock();
	//
	// final PluginSettings settings = settingsFactory.createGlobalSettings();
	// settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS,
	// intervalBetweenChecks);
	// settings.put(HubConfigKeys.CONFIG_HUB_URL, "Test Server Url");
	// settings.put(HubConfigKeys.CONFIG_HUB_USER, "Test User");
	// settings.put(HubConfigKeys.CONFIG_HUB_TIMEOUT, "300");
	//
	// final TransactionTemplateMock transactionManager = new
	// TransactionTemplateMock();
	// final HttpServletRequestMock requestMock = new HttpServletRequestMock();
	// final ProjectManagerMock projectManagerMock = new ProjectManagerMock();
	//
	// final HubJiraConfigController controller = new
	// HubJiraConfigController(managerMock, settingsFactory,
	// transactionManager, projectManagerMock);
	//
	// final Response response = controller.get(requestMock);
	// assertNotNull(response);
	// final Object configObject = response.getEntity();
	// assertNotNull(configObject);
	// final HubJiraConfigSerializable config = (HubJiraConfigSerializable)
	// configObject;
	//
	// assertEquals(HubJiraConfigController.HUB_SERVER_MISCONFIGURATION
	// + HubJiraConfigController.CHECK_HUB_SERVER_CONFIGURATION,
	// config.getErrorMessage());
	// assertNull(config.getHubProjectMappingError());
	// assertEquals(intervalBetweenChecks, config.getIntervalBetweenChecks());
	//
	// assertTrue(config.hasErrors());
	// }
	//
	// @Test
	// public void testGetConfigNoPolicyRulesOldHub() throws Exception {
	// final String intervalBetweenChecks = "30";
	//
	// final UserManagerMock managerMock = new UserManagerMock();
	// managerMock.setRemoteUsername("User");
	// managerMock.setIsSystemAdmin(true);
	// final PluginSettingsFactoryMock settingsFactory = new
	// PluginSettingsFactoryMock();
	//
	// final PluginSettings settings = settingsFactory.createGlobalSettings();
	// settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS,
	// intervalBetweenChecks);
	// settings.put(HubConfigKeys.CONFIG_HUB_URL, "http://www.google.com");
	// settings.put(HubConfigKeys.CONFIG_HUB_USER, "Test User");
	// settings.put(HubConfigKeys.CONFIG_HUB_PASS,
	// PasswordEncrypter.encrypt("Test"));
	// settings.put(HubConfigKeys.CONFIG_HUB_PASS_LENGTH, "4");
	// settings.put(HubConfigKeys.CONFIG_HUB_TIMEOUT, "300");
	//
	// final TransactionTemplateMock transactionManager = new
	// TransactionTemplateMock();
	// final HttpServletRequestMock requestMock = new HttpServletRequestMock();
	// final ProjectManagerMock projectManagerMock = new ProjectManagerMock();
	//
	// HubJiraConfigController controller = new
	// HubJiraConfigController(managerMock, settingsFactory,
	// transactionManager, projectManagerMock);
	//
	// controller = Mockito.spy(controller);
	//
	// final HubItemsService<PolicyRule> policyServiceMock =
	// Mockito.mock(HubItemsService.class);
	//
	// final List<PolicyRule> emptyPolicyRules = new ArrayList<PolicyRule>();
	//
	// Mockito.doReturn(emptyPolicyRules).when(policyServiceMock).httpGetItemList(Mockito.anyList(),
	// Mockito.anySet());
	//
	// Mockito.doReturn(policyServiceMock).when(controller).getPolicyService(Mockito.any(RestConnection.class));
	//
	// final HubIntRestService restServiceMock =
	// Mockito.mock(HubIntRestService.class);
	//
	// Mockito.doReturn(getHubProjects()).when(restServiceMock).getProjectMatches(Mockito.anyString());
	//
	// Mockito.doReturn("2.5.0").when(restServiceMock).getHubVersion();
	//
	// Mockito.doReturn(restServiceMock).when(controller).getHubRestService(Mockito.any(PluginSettings.class),
	// Mockito.any(HubJiraConfigSerializable.class));
	//
	// final Response response = controller.get(requestMock);
	// assertNotNull(response);
	// final Object configObject = response.getEntity();
	// assertNotNull(configObject);
	// final HubJiraConfigSerializable config = (HubJiraConfigSerializable)
	// configObject;
	//
	// assertNull(config.getHubProjectMappingError());
	// assertNull(config.getIntervalBetweenChecksError());
	// assertEquals(HubJiraConfigController.HUB_SERVER_NO_POLICY_SUPPORT_ERROR +
	// " : "
	// + HubJiraConfigController.NO_POLICY_RULES_FOUND_ERROR,
	// config.getPolicyRulesError());
	// assertEquals(intervalBetweenChecks, config.getIntervalBetweenChecks());
	// assertTrue(config.getJiraProjects().isEmpty());
	// assertTrue(!config.getHubProjects().isEmpty());
	// assertNull(config.getHubProjectMappings());
	// assertTrue(config.getPolicyRules().isEmpty());
	// assertTrue(config.hasErrors());
	// }
	//
	// @Test
	// public void testGetConfigNoPolicyRules() throws Exception {
	// final String intervalBetweenChecks = "30";
	//
	// final UserManagerMock managerMock = new UserManagerMock();
	// managerMock.setRemoteUsername("User");
	// managerMock.setIsSystemAdmin(true);
	// final PluginSettingsFactoryMock settingsFactory = new
	// PluginSettingsFactoryMock();
	//
	// final PluginSettings settings = settingsFactory.createGlobalSettings();
	// settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS,
	// intervalBetweenChecks);
	// settings.put(HubConfigKeys.CONFIG_HUB_URL, "http://www.google.com");
	// settings.put(HubConfigKeys.CONFIG_HUB_USER, "Test User");
	// settings.put(HubConfigKeys.CONFIG_HUB_PASS,
	// PasswordEncrypter.encrypt("Test"));
	// settings.put(HubConfigKeys.CONFIG_HUB_PASS_LENGTH, "4");
	// settings.put(HubConfigKeys.CONFIG_HUB_TIMEOUT, "300");
	//
	// final TransactionTemplateMock transactionManager = new
	// TransactionTemplateMock();
	// final HttpServletRequestMock requestMock = new HttpServletRequestMock();
	// final ProjectManagerMock projectManagerMock = new ProjectManagerMock();
	//
	// HubJiraConfigController controller = new
	// HubJiraConfigController(managerMock, settingsFactory,
	// transactionManager, projectManagerMock);
	//
	// controller = Mockito.spy(controller);
	//
	// final HubItemsService<PolicyRule> policyServiceMock =
	// Mockito.mock(HubItemsService.class);
	//
	// final List<PolicyRule> emptyPolicyRules = new ArrayList<PolicyRule>();
	//
	// Mockito.doReturn(emptyPolicyRules).when(policyServiceMock).httpGetItemList(Mockito.anyList(),
	// Mockito.anySet());
	//
	// Mockito.doReturn(policyServiceMock).when(controller).getPolicyService(Mockito.any(RestConnection.class));
	//
	// final HubIntRestService restServiceMock =
	// Mockito.mock(HubIntRestService.class);
	//
	// Mockito.doReturn(getHubProjects()).when(restServiceMock).getProjectMatches(Mockito.anyString());
	//
	// Mockito.doReturn("3.1.0").when(restServiceMock).getHubVersion();
	//
	// Mockito.doReturn(restServiceMock).when(controller).getHubRestService(Mockito.any(PluginSettings.class),
	// Mockito.any(HubJiraConfigSerializable.class));
	//
	// final Response response = controller.get(requestMock);
	// assertNotNull(response);
	// final Object configObject = response.getEntity();
	// assertNotNull(configObject);
	// final HubJiraConfigSerializable config = (HubJiraConfigSerializable)
	// configObject;
	//
	// assertNull(config.getHubProjectMappingError());
	// assertNull(config.getIntervalBetweenChecksError());
	// assertEquals(HubJiraConfigController.NO_POLICY_RULES_FOUND_ERROR,
	// config.getPolicyRulesError());
	// assertEquals(intervalBetweenChecks, config.getIntervalBetweenChecks());
	// assertTrue(config.getJiraProjects().isEmpty());
	// assertTrue(!config.getHubProjects().isEmpty());
	// assertNull(config.getHubProjectMappings());
	// assertTrue(config.getPolicyRules().isEmpty());
	// assertTrue(config.hasErrors());
	// }
	//
	// @Test
	// public void testGetConfigValid() throws Exception {
	// final String intervalBetweenChecks = "30";
	//
	// final Set<HubProjectMapping> mappings = getMappings();
	//
	// final UserManagerMock managerMock = new UserManagerMock();
	// managerMock.setRemoteUsername("User");
	// managerMock.setIsSystemAdmin(true);
	// final PluginSettingsFactoryMock settingsFactory = new
	// PluginSettingsFactoryMock();
	//
	// final Gson gson = new GsonBuilder().create();
	// final PluginSettings settings = settingsFactory.createGlobalSettings();
	// settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS,
	// intervalBetweenChecks);
	// settings.put(HubJiraConfigKeys.HUB_CONFIG_JIRA_PROJECT_MAPPINGS_JSON,
	// gson.toJson(mappings));
	// settings.put(HubConfigKeys.CONFIG_HUB_URL, "http://www.google.com");
	// settings.put(HubConfigKeys.CONFIG_HUB_USER, "Test User");
	// settings.put(HubConfigKeys.CONFIG_HUB_PASS,
	// PasswordEncrypter.encrypt("Test"));
	// settings.put(HubConfigKeys.CONFIG_HUB_PASS_LENGTH, "4");
	// settings.put(HubConfigKeys.CONFIG_HUB_TIMEOUT, "300");
	//
	// final TransactionTemplateMock transactionManager = new
	// TransactionTemplateMock();
	// final HttpServletRequestMock requestMock = new HttpServletRequestMock();
	// final ProjectManagerMock projectManagerMock = new ProjectManagerMock();
	//
	// projectManagerMock.setProjectObjects(ProjectManagerMock.getTestProjectObjects());
	//
	// HubJiraConfigController controller = new
	// HubJiraConfigController(managerMock, settingsFactory,
	// transactionManager, projectManagerMock);
	//
	// controller = Mockito.spy(controller);
	//
	// final HubItemsService<PolicyRule> policyServiceMock =
	// Mockito.mock(HubItemsService.class);
	//
	// Mockito.doReturn(getHubPolicies()).when(policyServiceMock).httpGetItemList(Mockito.anyList(),
	// Mockito.anySet());
	//
	// Mockito.doReturn(policyServiceMock).when(controller).getPolicyService(Mockito.any(RestConnection.class));
	//
	// final HubIntRestService restServiceMock =
	// Mockito.mock(HubIntRestService.class);
	//
	// Mockito.doReturn(getHubProjects()).when(restServiceMock).getProjectMatches(Mockito.anyString());
	//
	// Mockito.doReturn("3.1.0").when(restServiceMock).getHubVersion();
	//
	// Mockito.doReturn(restServiceMock).when(controller).getHubRestService(Mockito.any(PluginSettings.class),
	// Mockito.any(HubJiraConfigSerializable.class));
	//
	// final Response response = controller.get(requestMock);
	// assertNotNull(response);
	// final Object configObject = response.getEntity();
	// assertNotNull(configObject);
	// final HubJiraConfigSerializable config = (HubJiraConfigSerializable)
	// configObject;
	//
	// assertNull(config.getHubProjectMappingError());
	// assertNull(config.getIntervalBetweenChecksError());
	// assertNull(config.getPolicyRulesError());
	// assertEquals(intervalBetweenChecks, config.getIntervalBetweenChecks());
	// assertTrue(!config.getJiraProjects().isEmpty());
	// assertTrue(!config.getHubProjects().isEmpty());
	//
	// assertTrue(!config.getHubProjectMappings().isEmpty());
	//
	// assertTrue(!config.getPolicyRules().isEmpty());
	//
	// final Iterator<HubProjectMapping> configMappingIterator =
	// config.getHubProjectMappings().iterator();
	//
	// while (configMappingIterator.hasNext()) {
	// final HubProjectMapping currentMapping = configMappingIterator.next();
	// assertTrue(currentMapping.getHubProject().getProjectExists());
	// assertTrue(currentMapping.getJiraProject().getProjectExists());
	// }
	//
	// assertTrue(!config.hasErrors());
	// }

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

		assertEquals(HubJiraConfigController.HUB_CONFIG_PLUGIN_MISSING, config.getErrorMessage());
		assertEquals(HubJiraConfigController.NO_INTERVAL_FOUND_ERROR, config.getIntervalBetweenChecksError());
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
		projectManagerMock.setProjectObjects(ProjectManagerMock.getTestProjectObjects());

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
		assertEquals(HubJiraConfigController.NO_INTERVAL_FOUND_ERROR, config.getIntervalBetweenChecksError());
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
		projectManagerMock.setProjectObjects(ProjectManagerMock.getTestProjectObjects());

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
		assertEquals(HubJiraConfigController.NO_INTERVAL_FOUND_ERROR, config.getIntervalBetweenChecksError());
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
		projectManagerMock.setProjectObjects(ProjectManagerMock.getTestProjectObjects());

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
		projectManagerMock.setProjectObjects(ProjectManagerMock.getTestProjectObjects());

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
		assertEquals(HubJiraConfigController.NO_INTERVAL_FOUND_ERROR, config.getIntervalBetweenChecksError());
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
		projectManagerMock.setProjectObjects(ProjectManagerMock.getTestProjectObjects());

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
		projectManagerMock.setProjectObjects(ProjectManagerMock.getTestProjectObjects());

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
		jiraProject.setProjectExists(true);

		final HubProject hubProject = new HubProject();
		hubProject.setProjectName("HubProject");
		hubProject.setProjectUrl("projectURL");
		hubProject.setProjectExists(true);

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
	}

}
