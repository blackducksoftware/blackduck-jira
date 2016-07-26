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
package com.blackducksoftware.integration.jira.hub;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.IssueService.CreateValidationResult;
import com.atlassian.jira.bc.issue.IssueService.IssueResult;
import com.atlassian.jira.bc.issue.IssueService.TransitionValidationResult;
import com.atlassian.jira.bc.issue.properties.IssuePropertyService;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.entity.property.EntityPropertyQuery;
import com.atlassian.jira.entity.property.EntityPropertyQuery.ExecutableQuery;
import com.atlassian.jira.entity.property.EntityPropertyService.PropertyInput;
import com.atlassian.jira.entity.property.EntityPropertyService.PropertyResult;
import com.atlassian.jira.entity.property.EntityPropertyService.SetPropertyValidationResult;
import com.atlassian.jira.entity.property.JsonEntityPropertyManager;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.MissingUUIDException;
import com.blackducksoftware.integration.hub.exception.ResourceDoesNotExistException;
import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.hub.item.HubItemsService;
import com.blackducksoftware.integration.hub.meta.MetaInformation;
import com.blackducksoftware.integration.hub.meta.MetaLink;
import com.blackducksoftware.integration.hub.policy.api.PolicyExpression;
import com.blackducksoftware.integration.hub.policy.api.PolicyExpressions;
import com.blackducksoftware.integration.hub.policy.api.PolicyRule;
import com.blackducksoftware.integration.hub.policy.api.PolicyValue;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.version.api.ReleaseItem;
import com.blackducksoftware.integration.jira.config.HubProject;
import com.blackducksoftware.integration.jira.config.HubProjectMapping;
import com.blackducksoftware.integration.jira.config.JiraProject;
import com.blackducksoftware.integration.jira.hub.model.component.BomComponentVersionPolicyStatus;
import com.blackducksoftware.integration.jira.hub.model.component.ComponentVersion;
import com.blackducksoftware.integration.jira.hub.model.component.ComponentVersionStatus;
import com.blackducksoftware.integration.jira.hub.model.notification.NotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.PolicyOverrideNotificationContent;
import com.blackducksoftware.integration.jira.hub.model.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.RuleViolationNotificationContent;
import com.blackducksoftware.integration.jira.hub.model.notification.RuleViolationNotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.VulnerabilityNotificationContent;
import com.blackducksoftware.integration.jira.hub.model.notification.VulnerabilityNotificationItem;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;

/**
 *
 *
 */
public class TicketGeneratorTest {
	private static final long JIRA_ISSUE_ID = 10000L;
	private static final long JAN_2_2016 = 1451710800000L;
	private static final long JAN_1_2016 = 1451624400000L;

	private static SimpleDateFormat dateFormatter;
	private static ErrorCollection succeeded;

	private static PolicyRule rule;
	private static BomComponentVersionPolicyStatus bomComponentVersionPolicyStatus;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		dateFormatter = new SimpleDateFormat(RestConnection.JSON_DATE_FORMAT);
		dateFormatter.setTimeZone(java.util.TimeZone.getTimeZone("Zulu"));

		succeeded = Mockito.mock(ErrorCollection.class);
		Mockito.when(succeeded.hasAnyErrors()).thenReturn(false);

		final MetaInformation policyRuleMeta = new MetaInformation(null,
				"http://eng-hub-valid03.dc1.lan/api/policy-rules/0068397a-3e23-46bc-b1b7-82fb800e34ad", null);
		final List<PolicyValue> policyValues = new ArrayList<>();
		final PolicyValue policyValue = new PolicyValue("policyLabel", "policyValue");
		policyValues.add(policyValue);
		final List<PolicyExpression> policyExpressionList = new ArrayList<>();
		final PolicyExpression policyExpression = new PolicyExpression("COMPONENT_USAGE", "AND", policyValues);
		policyExpressionList.add(policyExpression);
		final PolicyExpressions policyExpressionsObject = new PolicyExpressions("AND", policyExpressionList);
		rule = new PolicyRule(policyRuleMeta, "someRule", "Some Rule", true, true, policyExpressionsObject, null, null,
				null, null);

		final List<MetaLink> links = new ArrayList<>();
		links.add(new MetaLink("policy-rule", "ruleUrl"));
		final MetaInformation bomComponentVersionPolicyStatusMeta = new MetaInformation(null, null, links);
		bomComponentVersionPolicyStatus = new BomComponentVersionPolicyStatus(bomComponentVersionPolicyStatusMeta);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	// @Test
	public void testCreateNewVulnerabilityJiraIssue() throws HubNotificationServiceException, ParseException,
	IOException, URISyntaxException, ResourceDoesNotExistException, BDRestException,
	UnexpectedHubResponseException, MissingUUIDException {
		testVulnerabilityNotifications(false, true, false);
	}

	@Test
	public void testCreateNewPolicyViolationJiraIssue() throws HubNotificationServiceException, ParseException,
	IOException,
	URISyntaxException, ResourceDoesNotExistException, BDRestException,
	UnexpectedHubResponseException, MissingUUIDException {
		testRuleNotifications(false, true, false);
	}

	@Test
	public void testDuplicatePolicyViolationIssueAvoidance() throws HubNotificationServiceException, ParseException,
	IOException,
	URISyntaxException, ResourceDoesNotExistException, BDRestException,
	UnexpectedHubResponseException, MissingUUIDException {
		testRuleNotifications(false, true, true);
	}

	@Test
	public void testClosePolicyViolationJiraIssue() throws HubNotificationServiceException, ParseException,
	IOException,
	URISyntaxException, ResourceDoesNotExistException, BDRestException,
	UnexpectedHubResponseException, MissingUUIDException {
		testRuleNotifications(true, false, false);
	}

	@Test
	public void testReOpenPolicyViolationJiraIssue() throws HubNotificationServiceException, ParseException,
	IOException,
	URISyntaxException,
	ResourceDoesNotExistException, BDRestException,
	UnexpectedHubResponseException, MissingUUIDException {
		testRuleNotifications(true, true, false);
	}

	@Test
	public void testLoadVulnerabilityContentJson() throws IOException {
		final String jsonString = readFile("src/test/resources/json/VulnerabilityNotificationContent_new.json");
		final VulnerabilityNotificationContent vulnContent = createVulnerabilityNotificationContent(jsonString);

		assertEquals("TestNG", vulnContent.getComponentName());
		assertEquals("2.0.0", vulnContent.getVersionName());
		assertEquals(
				"http://eng-hub-valid01.dc1.lan:8080/api/components/d15b7f61-c5b9-4f31-8605-769b12198d91/versions/0ce0a7b7-1872-4643-b389-da58a753d70d",
				vulnContent.getComponentVersionLink());
		assertEquals(1, vulnContent.getNewVulnerabilityCount());
		assertEquals("NVD", vulnContent.getNewVulnerabilityIds().get(0).getSource());
		assertEquals("CVE-2016-0001", vulnContent.getNewVulnerabilityIds().get(0).getId());
		assertEquals(0, vulnContent.getUpdatedVulnerabilityCount());
		assertEquals(0, vulnContent.getDeletedVulnerabilityCount());
		assertEquals("TestProject", vulnContent.getAffectedProjectVersions().get(0).getProjectName());
		assertEquals("1.0.0", vulnContent.getAffectedProjectVersions().get(0).getProjectVersionName());
	}

	private String readFile(final String path) throws IOException {
		final byte[] jsonBytes = Files.readAllBytes(Paths
				.get(path));
		final String jsonString = new String(jsonBytes, Charset.forName("UTF-8"));
		return jsonString;
	}

	private VulnerabilityNotificationContent createVulnerabilityNotificationContent(final String jsonString) {
		final Gson gson = new GsonBuilder().create();
		final VulnerabilityNotificationContent vulnContent = gson.fromJson(jsonString,
				VulnerabilityNotificationContent.class);
		return vulnContent;
	}

	private void testVulnerabilityNotifications(final boolean jiraIssueExistsAsClosed, final boolean openIssue,
			final boolean createDuplicateNotification) throws HubNotificationServiceException, ParseException,
			IOException, URISyntaxException, ResourceDoesNotExistException, BDRestException,
			UnexpectedHubResponseException, MissingUUIDException {

		// Setup

		final HubItemsService<NotificationItem> hubItemsService = Mockito.mock(HubItemsService.class);
		final HubIntRestService hub = Mockito.mock(HubIntRestService.class);
		final RestConnection restConnection = Mockito.mock(RestConnection.class);
		final HubNotificationService notificationService = createHubNotificationService(restConnection, hub,
				hubItemsService);
		final TicketGeneratorInfo jiraTicketGeneratorInfoService = Mockito.mock(TicketGeneratorInfo.class);
		final TicketGenerator ticketGenerator = new TicketGenerator(notificationService, jiraTicketGeneratorInfoService);

		List<NotificationItem> notificationItems;
		// if (openIssue) {
		notificationItems = mockNewVulnerabilityNotificationItems(createDuplicateNotification);
		// } else {
		// notificationItems = mock... TODO
		// }
		final Set<SimpleEntry<String, String>> hubNotificationQueryParameters = mockHubQueryParameters(JAN_1_2016,
				JAN_2_2016);
		mockNotificationServiceDependencies(restConnection, hub, hubItemsService, notificationService,
				hubNotificationQueryParameters,
				notificationItems);

		final ApplicationUser user = mockUser();
		final IssueService issueService = Mockito.mock(IssueService.class);
		final IssueInputParameters issueInputParameters = mockJiraIssueParameters();
		final Project atlassianJiraProject = mockJira(jiraTicketGeneratorInfoService, user, issueService,
				issueInputParameters);
		final IssuePropertyService propertyService = Mockito.mock(IssuePropertyService.class);
		Mockito.when(jiraTicketGeneratorInfoService.getPropertyService()).thenReturn(propertyService);

		SetPropertyValidationResult setPropValidationResult = null;
		MutableIssue oldIssue = null;

		if (openIssue) {
			if (jiraIssueExistsAsClosed) {
				oldIssue = mockIssueExists(issueService, atlassianJiraProject, jiraTicketGeneratorInfoService, false,
						user);
			} else {
				setPropValidationResult = mockIssueDoesNotExist(issueService, issueInputParameters, user,
						atlassianJiraProject, jiraTicketGeneratorInfoService, propertyService);
			}
		} else {
			oldIssue = mockIssueExists(issueService, atlassianJiraProject, jiraTicketGeneratorInfoService, true, user);
		}

		final TransitionValidationResult transitionValidationResult = mockTransition(issueService, oldIssue);

		final Set<HubProjectMapping> hubProjectMappings = mockProjectMappings();

		final List<String> linksOfRulesToMonitor = mockRules();

		final NotificationDateRange notificationDateRange = new NotificationDateRange(new Date(JAN_1_2016), new Date(
				JAN_2_2016));

		// Test

		ticketGenerator.generateTicketsForRecentNotifications(hubProjectMappings, linksOfRulesToMonitor,
				notificationDateRange);

		// Verify

		int expectedFindIssueCount = 1;
		final int expectedCreateIssueCount = 1;
		final int expectedCloseIssueCount = 1;
		if (createDuplicateNotification) {
			expectedFindIssueCount = 2;
		}

		if (openIssue) {
			Mockito.verify(issueInputParameters, Mockito.times(expectedFindIssueCount))
			.setSummary(
					"Black Duck Vulnerability added to Hub Project 'projectName' / 'hubProjectVersionName', component 'componentName' / 'componentVersionName' [Vulnerability: 'someVuln']");
			Mockito.verify(issueInputParameters, Mockito.times(expectedFindIssueCount))
			.setDescription(
					"The Black Duck Hub has detected a new Vulnerability on Hub Project 'projectName', component 'componentName' / 'componentVersionName'. The rule new vulnerability is: 'someVuln'. Remediation Status : 'remStatus'");
			if (jiraIssueExistsAsClosed) {
				Mockito.verify(issueService, Mockito.times(expectedCreateIssueCount)).transition(user,
						transitionValidationResult);
			} else {
				Mockito.verify(propertyService, Mockito.times(expectedCreateIssueCount)).setProperty(user,
						setPropValidationResult);
				Mockito.verify(issueService, Mockito.times(expectedCreateIssueCount)).create(
						Mockito.any(ApplicationUser.class), Mockito.any(CreateValidationResult.class));
			}
		} else {
			Mockito.verify(issueService, Mockito.times(expectedCloseIssueCount)).transition(user,
					transitionValidationResult);
		}

	}

	private void testRuleNotifications(final boolean jiraIssueExistsAsClosed, final boolean openIssue,
			final boolean createDuplicateNotification)
					throws HubNotificationServiceException, ParseException,
					IOException, URISyntaxException,
					ResourceDoesNotExistException, BDRestException,
					UnexpectedHubResponseException, MissingUUIDException {

		// Setup

		final HubItemsService<NotificationItem> hubItemsService = Mockito.mock(HubItemsService.class);
		final HubIntRestService hub = Mockito.mock(HubIntRestService.class);
		final RestConnection restConnection = Mockito.mock(RestConnection.class);
		final HubNotificationService notificationService = createHubNotificationService(restConnection, hub,
				hubItemsService);
		final TicketGeneratorInfo jiraTicketGeneratorInfoService = Mockito.mock(TicketGeneratorInfo.class);
		final TicketGenerator ticketGenerator = new TicketGenerator(notificationService, jiraTicketGeneratorInfoService);

		List<NotificationItem> notificationItems;
		if (openIssue) {
			notificationItems = mockRuleViolationNotificationItems(createDuplicateNotification);
		} else {
			notificationItems = mockPolicyOverrideNotificationItems();
		}
		final Set<SimpleEntry<String, String>> hubNotificationQueryParameters = mockHubQueryParameters(JAN_1_2016,
				JAN_2_2016);
		mockNotificationServiceDependencies(restConnection, hub, hubItemsService, notificationService,
				hubNotificationQueryParameters,
				notificationItems);

		final ApplicationUser user = mockUser();
		final IssueService issueService = Mockito.mock(IssueService.class);
		final IssueInputParameters issueInputParameters = mockJiraIssueParameters();
		final Project atlassianJiraProject = mockJira(jiraTicketGeneratorInfoService, user, issueService,
				issueInputParameters);
		final IssuePropertyService propertyService = Mockito.mock(IssuePropertyService.class);
		Mockito.when(jiraTicketGeneratorInfoService.getPropertyService()).thenReturn(propertyService);

		SetPropertyValidationResult setPropValidationResult = null;
		MutableIssue oldIssue = null;

		if (openIssue) {
			if (jiraIssueExistsAsClosed) {
				oldIssue = mockIssueExists(issueService, atlassianJiraProject, jiraTicketGeneratorInfoService, false,
						user);
			} else {
				setPropValidationResult = mockIssueDoesNotExist(issueService, issueInputParameters, user,
						atlassianJiraProject, jiraTicketGeneratorInfoService, propertyService);
			}
		} else {
			oldIssue = mockIssueExists(issueService, atlassianJiraProject, jiraTicketGeneratorInfoService, true, user);
		}

		final TransitionValidationResult transitionValidationResult = mockTransition(issueService, oldIssue);


		final Set<HubProjectMapping> hubProjectMappings = mockProjectMappings();

		final List<String> linksOfRulesToMonitor = mockRules();

		final NotificationDateRange notificationDateRange = new NotificationDateRange(new Date(JAN_1_2016), new Date(
				JAN_2_2016));

		// Test

		ticketGenerator.generateTicketsForRecentNotifications(hubProjectMappings, linksOfRulesToMonitor,
				notificationDateRange);

		// Verify

		int expectedFindIssueCount = 1;
		final int expectedCreateIssueCount = 1;
		final int expectedCloseIssueCount = 1;
		if (createDuplicateNotification) {
			expectedFindIssueCount = 2;
		}

		if (openIssue) {
			Mockito.verify(issueInputParameters, Mockito.times(expectedFindIssueCount))
			.setSummary(
					"Black Duck Policy Violation detected on Hub Project 'projectName' / 'hubProjectVersionName', component 'componentName' / 'componentVersionName' [Rule: 'someRule']");
			Mockito.verify(issueInputParameters, Mockito.times(expectedFindIssueCount))
			.setDescription(
					"The Black Duck Hub has detected a Policy Violation on Hub Project 'projectName', component 'componentName' / 'componentVersionName'. The rule violated is: 'someRule'. Rule overridable : true");
			if (jiraIssueExistsAsClosed) {
				Mockito.verify(issueService, Mockito.times(expectedCreateIssueCount)).transition(user,
						transitionValidationResult);
			} else {
				Mockito.verify(propertyService, Mockito.times(expectedCreateIssueCount)).setProperty(user,
						setPropValidationResult);
				Mockito.verify(issueService, Mockito.times(expectedCreateIssueCount)).create(
						Mockito.any(ApplicationUser.class),
						Mockito.any(CreateValidationResult.class));
			}
		} else {
			Mockito.verify(issueService, Mockito.times(expectedCloseIssueCount)).transition(user,
					transitionValidationResult);
		}


	}

	private List<String> mockRules() {
		final List<String> linksOfRulesToMonitor = new ArrayList<>();
		linksOfRulesToMonitor.add("ruleUrl");
		return linksOfRulesToMonitor;
	}

	private Project mockJira(final TicketGeneratorInfo jiraTicketGeneratorInfoService, final ApplicationUser user,
			final IssueService issueService, final IssueInputParameters issueInputParameters) {
		Mockito.when(jiraTicketGeneratorInfoService.getJiraUser()).thenReturn(user);
		Mockito.when(issueService.newIssueInputParameters()).thenReturn(issueInputParameters);
		Mockito.when(jiraTicketGeneratorInfoService.getIssueService()).thenReturn(issueService);
		final JiraAuthenticationContext authContext = Mockito.mock(JiraAuthenticationContext.class);
		Mockito.when(jiraTicketGeneratorInfoService.getAuthContext()).thenReturn(authContext);
		Mockito.when(jiraTicketGeneratorInfoService.getJiraIssueTypeName()).thenReturn("jiraIssueTypeName");
		mockJsonEntityPropertyManager(jiraTicketGeneratorInfoService);
		final Project atlassianJiraProject = mockJiraProject(jiraTicketGeneratorInfoService);
		return atlassianJiraProject;
	}

	private TransitionValidationResult mockTransition(final IssueService issueService, final MutableIssue oldIssue) {
		final TransitionValidationResult validationResult = Mockito.mock(TransitionValidationResult.class);
		Mockito.when(validationResult.isValid()).thenReturn(true);
		final IssueResult transitionResult = Mockito.mock(IssueResult.class);
		Mockito.when(transitionResult.getErrorCollection()).thenReturn(succeeded);
		Mockito.when(transitionResult.getIssue()).thenReturn(oldIssue);
		Mockito.when(
				issueService.transition(Mockito.any(ApplicationUser.class),
						Mockito.any(TransitionValidationResult.class))).thenReturn(transitionResult);
		Mockito.when(
				issueService.validateTransition(Mockito.any(ApplicationUser.class), Mockito.anyLong(),
						Mockito.anyInt(), Mockito.any(IssueInputParameters.class))).thenReturn(validationResult);
		return validationResult;
	}

	private JsonEntityPropertyManager mockJsonEntityPropertyManager(
			final TicketGeneratorInfo jiraTicketGeneratorInfoService) {
		final EntityPropertyQuery entityPropertyQuery = mockEntityPropertyQuery();
		final JsonEntityPropertyManager jsonEntityPropertyManager = Mockito.mock(JsonEntityPropertyManager.class);
		Mockito.when(jsonEntityPropertyManager.query()).thenReturn(entityPropertyQuery);
		Mockito.when(jiraTicketGeneratorInfoService.getJsonEntityPropertyManager()).thenReturn(
				jsonEntityPropertyManager);
		return jsonEntityPropertyManager;
	}

	private EntityPropertyQuery mockEntityPropertyQuery() {
		final ExecutableQuery executableQuery = mockExecutableQuery();
		final EntityPropertyQuery entityPropertyQuery = Mockito.mock(EntityPropertyQuery.class);
		Mockito.when(entityPropertyQuery.key(Mockito.anyString())).thenReturn(executableQuery);
		return entityPropertyQuery;
	}

	private ExecutableQuery mockExecutableQuery() {
		final ExecutableQuery executableQuery = Mockito.mock(ExecutableQuery.class);
		Mockito.when(executableQuery.maxResults(1)).thenReturn(executableQuery);
		final List<EntityProperty> props = new ArrayList<>();
		final EntityProperty entityProperty = Mockito.mock(EntityProperty.class);
		props.add(entityProperty);
		Mockito.when(executableQuery.find()).thenReturn(props);
		Mockito.when(entityProperty.getValue())
		.thenReturn(
				"{\"projectName\":\"SB001\",\"projectVersion\":\"1\",\"componentName\":\"SeaMonkey\",\"componentVersion\":\"1.0.3\",\"ruleName\":\"apr28\",\"jiraIssueId\":"
						+ JIRA_ISSUE_ID + "}");
		return executableQuery;
	}

	private Set<HubProjectMapping> mockProjectMappings() {
		HubProjectMapping hubProjectMapping = new HubProjectMapping();
		HubProject hubProject = new HubProject();
		hubProject.setProjectName("hubProjectName");
		hubProject.setProjectUrl("hubProjectUrl");
		hubProjectMapping.setHubProject(hubProject);

		JiraProject bdsJiraProject = mockBdsJiraProject();
		hubProjectMapping.setJiraProject(bdsJiraProject);

		final Set<HubProjectMapping> hubProjectMappings = new HashSet<>();
		hubProjectMappings.add(hubProjectMapping);

		// Add a second, bogus, mapping
		hubProjectMapping = new HubProjectMapping();
		hubProject = new HubProject();
		hubProject.setProjectName("bogusHubProjectName");
		hubProject.setProjectUrl("bogusHubProjectUrl");
		hubProjectMapping.setHubProject(hubProject);

		bdsJiraProject = mockBdsJiraProject();
		hubProjectMapping.setJiraProject(bdsJiraProject);
		hubProjectMappings.add(hubProjectMapping);
		return hubProjectMappings;
	}

	private HubNotificationService createHubNotificationService(final RestConnection restConnection,
			final HubIntRestService hub,
			final HubItemsService<NotificationItem> hubItemsService) {

		final HubNotificationService notificationService = new HubNotificationService(restConnection, hub,
				hubItemsService);
		return notificationService;
	}

	private JiraProject mockBdsJiraProject() {
		final JiraProject jiraProject = new JiraProject();
		jiraProject.setProjectId(123L);
		jiraProject.setProjectKey("jiraProjectKey");
		jiraProject.setProjectName("jiraProjectName");
		jiraProject.setIssueTypeId("jiraIssueTypeName");
		return jiraProject;
	}

	private Set<SimpleEntry<String, String>> mockHubQueryParameters(final long startDate,
			final long endDate) {
		final String startDateString = dateFormatter.format(startDate);
		final String endDateString = dateFormatter.format(endDate);
		final Set<SimpleEntry<String, String>> queryParameters = new HashSet<>();
		queryParameters.add(new SimpleEntry<String, String>("startDate", startDateString));
		queryParameters.add(new SimpleEntry<String, String>("endDate", endDateString));
		queryParameters.add(new AbstractMap.SimpleEntry<String, String>("limit", String.valueOf(1000)));
		return queryParameters;
	}

	private void mockNotificationServiceDependencies(final RestConnection restConnection, final HubIntRestService hub,
			final HubItemsService<NotificationItem> hubItemsService,
			final HubNotificationService notificationService, final Set<SimpleEntry<String, String>> queryParameters,
			final List<NotificationItem> notificationItems)
					throws IOException, URISyntaxException, ResourceDoesNotExistException, BDRestException,
					HubNotificationServiceException, UnexpectedHubResponseException, MissingUUIDException {

		final List<String> urlSegments = new ArrayList<>();
		urlSegments.add("api");
		urlSegments.add("notifications");
		Mockito.when(hubItemsService.httpGetItemList(urlSegments, queryParameters)).thenReturn(notificationItems);
		final List<MetaLink> links = new ArrayList<>();
		links.add(new MetaLink("project", "hubProjectUrl"));
		final String href = "http://eng-hub-valid03.dc1.lan/api/projects/073e0506-0d91-4d95-bd51-740d9ba52d96/versions/35430a68-3007-4777-90af-2e3f41738ac0";
		final MetaInformation projectMeta = new MetaInformation(null, href, links);
		final ReleaseItem releaseItem = new ReleaseItem("hubProjectVersionName", "projectPhase", "projectDistribution",
				"projectSource", projectMeta);
		final ReleaseItem projectRelease = Mockito.mock(ReleaseItem.class);
		final UUID projectUuid = UUID.randomUUID();
		Mockito.when(projectRelease.getProjectId()).thenReturn(projectUuid);
		final UUID versionUuid = UUID.randomUUID();
		Mockito.when(projectRelease.getVersionId()).thenReturn(versionUuid);
		Mockito.when(hub.getProjectVersion("hubProjectVersionUrl")).thenReturn(projectRelease);
		// projectRelease.getVersionName
		Mockito.when(projectRelease.getVersionName()).thenReturn("hubProjectVersionName");
		final List<String> projectLinks = new ArrayList<>();
		projectLinks.add("hubProjectUrl");
		Mockito.when(projectRelease.getLinks("project")).thenReturn(projectLinks);
		final ComponentVersion componentVersion = Mockito.mock(ComponentVersion.class);
		Mockito.when(componentVersion.getVersionName()).thenReturn("componentVersionName");
		Mockito.when(
				restConnection
				.httpGetFromAbsoluteUrl(
						ComponentVersion.class,
						"http://eng-hub-valid03.dc1.lan/api/components/0934ea45-c739-4b58-bcb1-ee777022ce4f/versions/7c45d411-92ca-45b0-80fc-76b765b954ef"))
						.thenReturn(componentVersion);
		Mockito.when(
				restConnection.httpGetFromAbsoluteUrl(BomComponentVersionPolicyStatus.class,
						"bomComponentVersionPolicyStatusLink")).thenReturn(bomComponentVersionPolicyStatus);
		Mockito.when(restConnection.httpGetFromAbsoluteUrl(PolicyRule.class, "ruleUrl")).thenReturn(rule);
	}

	private List<NotificationItem> mockRuleViolationNotificationItems(final boolean createDuplicate) {
		final List<NotificationItem> notificationItems = new ArrayList<>();
		final MetaInformation meta = new MetaInformation(null, null, null);
		final RuleViolationNotificationItem notificationItem = new RuleViolationNotificationItem(meta);
		final RuleViolationNotificationContent content = new RuleViolationNotificationContent();
		content.setComponentVersionsInViolation(1);
		final List<ComponentVersionStatus> componentVersionStatuses = new ArrayList<>();
		final ComponentVersionStatus componentVersionStatus = new ComponentVersionStatus();
		componentVersionStatus.setComponentName("componentName");
		componentVersionStatus
		.setComponentVersionLink("http://eng-hub-valid03.dc1.lan/api/components/0934ea45-c739-4b58-bcb1-ee777022ce4f/versions/7c45d411-92ca-45b0-80fc-76b765b954ef");
		componentVersionStatus.setBomComponentVersionPolicyStatusLink("bomComponentVersionPolicyStatusLink");
		componentVersionStatuses.add(componentVersionStatus);
		content.setComponentVersionStatuses(componentVersionStatuses);
		content.setProjectVersionLink("hubProjectVersionUrl");
		content.setProjectName("projectName");
		notificationItem.setContent(content);

		notificationItems.add(notificationItem);
		if (createDuplicate) {
			notificationItems.add(notificationItem);
		}
		return notificationItems;
	}

	private List<NotificationItem> mockNewVulnerabilityNotificationItems(final boolean createDuplicate)
			throws IOException {
		final List<NotificationItem> notificationItems = new ArrayList<>();
		final MetaInformation meta = new MetaInformation(null, null, null);
		final VulnerabilityNotificationItem notificationItem = new VulnerabilityNotificationItem(meta);
		final String jsonString = readFile("src/test/resources/json/VulnerabilityNotificationContent_new.json");
		final VulnerabilityNotificationContent content = createVulnerabilityNotificationContent(jsonString);
		notificationItem.setContent(content);

		notificationItems.add(notificationItem);
		if (createDuplicate) {
			notificationItems.add(notificationItem);
		}
		return notificationItems;
	}

	private List<NotificationItem> mockPolicyOverrideNotificationItems() {
		final List<NotificationItem> notificationItems = new ArrayList<>();
		final MetaInformation meta = new MetaInformation(null, null, null);
		final PolicyOverrideNotificationItem notificationItem = new PolicyOverrideNotificationItem(meta);
		final PolicyOverrideNotificationContent content = new PolicyOverrideNotificationContent();
		content.setBomComponentVersionPolicyStatusLink("bomComponentVersionPolicyStatusLink");
		content.setProjectVersionLink("hubProjectVersionUrl");
		content.setProjectName("projectName");
		content.setComponentName("componentName");
		content.setComponentVersionLink("http://eng-hub-valid03.dc1.lan/api/components/0934ea45-c739-4b58-bcb1-ee777022ce4f/versions/7c45d411-92ca-45b0-80fc-76b765b954ef");
		content.setComponentVersionName("componentVersionName");
		content.setFirstName("firstName");
		content.setLastName("lastName");
		content.setProjectVersionName("projectVersionName");
		notificationItem.setContent(content);
		notificationItems.add(notificationItem);
		return notificationItems;
	}

	private ApplicationUser mockUser() {
		final ApplicationUser user = Mockito.mock(ApplicationUser.class);
		Mockito.when(user.getDisplayName()).thenReturn("userDisplayName");
		Mockito.when(user.getName()).thenReturn("userName");
		Mockito.when(user.isActive()).thenReturn(true);
		return user;
	}

	private IssueInputParameters mockJiraIssueParameters() {
		final IssueInputParameters issueInputParameters = Mockito.mock(IssueInputParameters.class);
		Mockito.when(issueInputParameters.setProjectId(123L)).thenReturn(issueInputParameters);
		Mockito.when(issueInputParameters.setIssueTypeId(Mockito.anyString())).thenReturn(issueInputParameters);
		Mockito.when(issueInputParameters.setSummary(Mockito.anyString())).thenReturn(issueInputParameters);
		Mockito.when(issueInputParameters.setReporterId("userName")).thenReturn(issueInputParameters);
		Mockito.when(issueInputParameters.setDescription(Mockito.anyString())).thenReturn(issueInputParameters);
		return issueInputParameters;
	}

	private Project mockJiraProject(final TicketGeneratorInfo jiraTicketGeneratorInfoService) {
		final ProjectManager jiraProjectManager = Mockito.mock(ProjectManager.class);
		Mockito.when(jiraTicketGeneratorInfoService.getJiraProjectManager()).thenReturn(jiraProjectManager);

		final Project atlassianJiraProject = Mockito.mock(Project.class);
		Mockito.when(atlassianJiraProject.getKey()).thenReturn("jiraProjectKey");
		Mockito.when(atlassianJiraProject.getName()).thenReturn("jiraProjectName");
		Mockito.when(atlassianJiraProject.getId()).thenReturn(123L);
		final Collection<IssueType> jiraProjectIssueTypes = new ArrayList<>();
		final IssueType issueType = Mockito.mock(IssueType.class);
		Mockito.when(issueType.getName()).thenReturn("jiraIssueTypeName");
		jiraProjectIssueTypes.add(issueType);
		Mockito.when(atlassianJiraProject.getIssueTypes()).thenReturn(jiraProjectIssueTypes);

		Mockito.when(jiraProjectManager.getProjectObj(123L)).thenReturn(atlassianJiraProject);

		return atlassianJiraProject;
	}

	private MutableIssue mockIssueExists(final IssueService issueService, final Project atlassianJiraProject,
			final TicketGeneratorInfo jiraTicketGeneratorInfoService, final boolean open, final ApplicationUser user) {
		final IssueResult getOldIssueResult = Mockito.mock(IssueResult.class);
		Mockito.when(getOldIssueResult.isValid()).thenReturn(true);
		final MutableIssue oldIssue = Mockito.mock(MutableIssue.class);
		Mockito.when(getOldIssueResult.getIssue()).thenReturn(oldIssue);
		final Status oldIssueStatus = Mockito.mock(Status.class);
		String state;
		if (open) {
			state = "Open";
		} else {
			state = "Done";
		}
		Mockito.when(oldIssueStatus.getName()).thenReturn(state);
		Mockito.when(oldIssue.getStatusObject()).thenReturn(oldIssueStatus);
		Mockito.when(issueService.getIssue(user, JIRA_ISSUE_ID)).thenReturn(
				getOldIssueResult);
		Mockito.when(oldIssue.getProjectObject()).thenReturn(atlassianJiraProject);
		final IssueType oldIssueType = Mockito.mock(IssueType.class);
		Mockito.when(oldIssueType.getName()).thenReturn("Mocked issue type");
		Mockito.when(oldIssue.getIssueTypeObject()).thenReturn(oldIssueType);
		final WorkflowManager workflowManager = Mockito.mock(WorkflowManager.class);
		Mockito.when(jiraTicketGeneratorInfoService.getWorkflowManager()).thenReturn(workflowManager);
		final JiraWorkflow jiraWorkflow = Mockito.mock(JiraWorkflow.class);
		final StepDescriptor stepDescriptor = Mockito.mock(StepDescriptor.class);
		Mockito.when(jiraWorkflow.getLinkedStep(oldIssueStatus)).thenReturn(stepDescriptor);
		final List<ActionDescriptor> actions = new ArrayList<>();
		final ActionDescriptor actionDescriptor = Mockito.mock(ActionDescriptor.class);
		actions.add(actionDescriptor);
		String transition;
		if (open) {
			transition = "Done";
		} else {
			transition = "Reopen";
		}
		Mockito.when(actionDescriptor.getName()).thenReturn(transition);
		Mockito.when(stepDescriptor.getActions()).thenReturn(actions);
		Mockito.when(workflowManager.getWorkflow(oldIssue)).thenReturn(jiraWorkflow);

		return oldIssue;
	}

	private SetPropertyValidationResult mockIssueDoesNotExist(final IssueService issueService,
			final IssueInputParameters issueInputParameters, final ApplicationUser user,
			final Project atlassianJiraProject, final TicketGeneratorInfo jiraTicketGeneratorInfoService,
			final IssuePropertyService propertyService) {

		final MutableIssue newIssue = Mockito.mock(MutableIssue.class);
		Mockito.when(newIssue.getProjectObject()).thenReturn(atlassianJiraProject);
		final Status newIssueStatus = Mockito.mock(Status.class);
		Mockito.when(newIssueStatus.getName()).thenReturn("Open");
		Mockito.when(newIssue.getStatusObject()).thenReturn(newIssueStatus);
		final IssueType newIssueType = Mockito.mock(IssueType.class);
		Mockito.when(newIssueType.getName()).thenReturn("Mocked issue type");
		Mockito.when(newIssue.getIssueTypeObject()).thenReturn(newIssueType);

		final IssueResult issueNotFoundResult = Mockito.mock(IssueResult.class);
		Mockito.when(issueNotFoundResult.isValid()).thenReturn(false);
		Mockito.when(issueNotFoundResult.getIssue()).thenReturn(null);
		Mockito.when(issueNotFoundResult.getErrorCollection()).thenReturn(succeeded);

		final IssueResult issueExistsResult = Mockito.mock(IssueResult.class);
		Mockito.when(issueExistsResult.isValid()).thenReturn(true);
		Mockito.when(issueExistsResult.getIssue()).thenReturn(newIssue);
		Mockito.when(issueExistsResult.getErrorCollection()).thenReturn(succeeded);

		Mockito.when(issueService.getIssue(user, JIRA_ISSUE_ID)).thenReturn(issueNotFoundResult)
		.thenReturn(issueExistsResult);

		final CreateValidationResult createValidationResult = Mockito.mock(CreateValidationResult.class);
		Mockito.when(createValidationResult.isValid()).thenReturn(true);
		Mockito.when(issueService.validateCreate(user, issueInputParameters)).thenReturn(createValidationResult);

		final IssueResult createResult = Mockito.mock(IssueResult.class);

		Mockito.when(createResult.getErrorCollection()).thenReturn(succeeded);


		Mockito.when(createResult.getIssue()).thenReturn(newIssue);
		Mockito.when(issueService.create(user, createValidationResult)).thenReturn(createResult);


		final SetPropertyValidationResult setPropValidationResult = Mockito.mock(SetPropertyValidationResult.class);
		Mockito.when(setPropValidationResult.isValid()).thenReturn(true);
		Mockito.when(
				propertyService.validateSetProperty(Mockito.any(ApplicationUser.class), Mockito.anyLong(),
						Mockito.any(PropertyInput.class))).thenReturn(setPropValidationResult);


		final PropertyResult setPropertyResult = Mockito.mock(PropertyResult.class);
		Mockito.when(setPropertyResult.getErrorCollection()).thenReturn(succeeded);
		Mockito.when(propertyService.setProperty(user, setPropValidationResult)).thenReturn(setPropertyResult);

		return setPropValidationResult;
	}

}
