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

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
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
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.entity.property.EntityPropertyQuery;
import com.atlassian.jira.entity.property.EntityPropertyQuery.ExecutableQuery;
import com.atlassian.jira.entity.property.EntityPropertyService.PropertyInput;
import com.atlassian.jira.entity.property.EntityPropertyService.PropertyResult;
import com.atlassian.jira.entity.property.EntityPropertyService.SetPropertyValidationResult;
import com.atlassian.jira.entity.property.JsonEntityPropertyManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueImpl;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.UpdateIssueRequest;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.api.component.BomComponentVersionPolicyStatus;
import com.blackducksoftware.integration.hub.api.notification.VulnerabilityNotificationContent;
import com.blackducksoftware.integration.hub.api.notification.VulnerabilitySourceQualifiedId;
import com.blackducksoftware.integration.hub.api.policy.PolicyExpression;
import com.blackducksoftware.integration.hub.api.policy.PolicyExpressions;
import com.blackducksoftware.integration.hub.api.policy.PolicyRule;
import com.blackducksoftware.integration.hub.api.policy.PolicyValue;
import com.blackducksoftware.integration.hub.api.project.ProjectVersion;
import com.blackducksoftware.integration.hub.api.vulnerableBomComponent.VulnerableBomComponentRestService;
import com.blackducksoftware.integration.hub.dataservices.DataServicesFactory;
import com.blackducksoftware.integration.hub.dataservices.notification.NotificationDataService;
import com.blackducksoftware.integration.hub.dataservices.notification.items.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservices.notification.items.PolicyOverrideContentItem;
import com.blackducksoftware.integration.hub.dataservices.notification.items.PolicyViolationContentItem;
import com.blackducksoftware.integration.hub.dataservices.notification.items.VulnerabilityContentItem;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.MissingUUIDException;
import com.blackducksoftware.integration.hub.exception.NotificationServiceException;
import com.blackducksoftware.integration.hub.exception.ResourceDoesNotExistException;
import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.hub.meta.MetaInformation;
import com.blackducksoftware.integration.hub.meta.MetaLink;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.common.HubProject;
import com.blackducksoftware.integration.jira.common.HubProjectMapping;
import com.blackducksoftware.integration.jira.common.HubProjectMappings;
import com.blackducksoftware.integration.jira.common.JiraContext;
import com.blackducksoftware.integration.jira.common.JiraProject;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;

/**
 *
 *
 */
public class TicketGeneratorTest {
	private static final String VULNERABILITY_ISSUE_COMMENT = "(Black Duck Hub JIRA plugin-generated comment)\n"
			+ "Vulnerabilities added: CVE-2016-0001 (NVD)\n" + "Vulnerabilities updated: None\n"
			+ "Vulnerabilities deleted: None\n";
	private static final String VULNERABILITY_ISSUE_DESCRIPTION = "This issue tracks vulnerability status changes on Hub Project '4Drew' / '2Drew', component 'TestNG' / '2.0.0'. See comments for details.";
	private static final String VULNERABILITY_ISSUE_SUMMARY = "Black Duck vulnerability status changes on Hub Project '4Drew' / '2Drew', component 'TestNG' / '2.0.0'";
	private static final String POLICY_RULE_URL = "http://eng-hub-valid03.dc1.lan/api/policy-rules/0068397a-3e23-46bc-b1b7-82fb800e34ad";
	private static final long JIRA_ISSUE_ID = 10000L;
	private static final long JAN_2_2016 = 1451710800000L;
	private static final long JAN_1_2016 = 1451624400000L;

	private static SimpleDateFormat dateFormatter;
	private static ErrorCollection succeeded;

	private static PolicyRule rule;
	private static BomComponentVersionPolicyStatus bomComponentVersionPolicyStatus;
	private static DataServicesFactory dataServicesFactory;
	private static VulnerableBomComponentRestService vulnerableBomComponentRestService;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		vulnerableBomComponentRestService = Mockito.mock(VulnerableBomComponentRestService.class);
		dataServicesFactory = Mockito.mock(DataServicesFactory.class);
		dateFormatter = new SimpleDateFormat(RestConnection.JSON_DATE_FORMAT);
		dateFormatter.setTimeZone(java.util.TimeZone.getTimeZone("Zulu"));

		succeeded = Mockito.mock(ErrorCollection.class);
		Mockito.when(succeeded.hasAnyErrors()).thenReturn(false);

		final MetaInformation policyRuleMeta = new MetaInformation(null, POLICY_RULE_URL, null);
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

	@Test
	public void testCreateNewVulnerabilityJiraIssue()
			throws NotificationServiceException, ParseException, IOException, URISyntaxException,
			ResourceDoesNotExistException, BDRestException, UnexpectedHubResponseException, MissingUUIDException {
		testVulnerabilityNotifications(false, true, false, VULNERABILITY_ISSUE_SUMMARY,
				VULNERABILITY_ISSUE_DESCRIPTION);
	}

	@Test
	public void testDeDupeVulnerability()
			throws NotificationServiceException, ParseException, IOException, URISyntaxException,
			ResourceDoesNotExistException, BDRestException, UnexpectedHubResponseException, MissingUUIDException {
		testVulnerabilityNotifications(false, true, true, VULNERABILITY_ISSUE_SUMMARY, VULNERABILITY_ISSUE_DESCRIPTION);
	}

	@Test
	public void testCreateNewPolicyViolationJiraIssue()
			throws NotificationServiceException, ParseException, IOException, URISyntaxException,
			ResourceDoesNotExistException, BDRestException, UnexpectedHubResponseException, MissingUUIDException {
		testRuleNotifications(false, true, false);
	}

	@Test
	public void testDuplicatePolicyViolationIssueAvoidance()
			throws NotificationServiceException, ParseException, IOException, URISyntaxException,
			ResourceDoesNotExistException, BDRestException, UnexpectedHubResponseException, MissingUUIDException {
		testRuleNotifications(false, true, true);
	}

	@Test
	public void testClosePolicyViolationJiraIssue()
			throws NotificationServiceException, ParseException, IOException, URISyntaxException,
			ResourceDoesNotExistException, BDRestException, UnexpectedHubResponseException, MissingUUIDException {
		testRuleNotifications(true, false, false);
	}

	@Test
	public void testReOpenPolicyViolationJiraIssue()
			throws NotificationServiceException, ParseException, IOException, URISyntaxException,
			ResourceDoesNotExistException, BDRestException, UnexpectedHubResponseException, MissingUUIDException {
		testRuleNotifications(true, true, false);
	}

	private String readFile(final String path) throws IOException {
		final byte[] jsonBytes = Files.readAllBytes(Paths.get(path));
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
			final boolean createDuplicateNotification, final String expectedIssueSummary,
			final String expectedIssueDescription)
					throws NotificationServiceException, ParseException, IOException, URISyntaxException,
					ResourceDoesNotExistException, BDRestException, UnexpectedHubResponseException, MissingUUIDException {

		// Setup

		final NotificationDataService notificationDataService = Mockito.mock(NotificationDataService.class);
		final JiraContext jiraContext = Mockito.mock(JiraContext.class);
		final JiraServices jiraServices = Mockito.mock(JiraServices.class);
		final JiraSettingsService settingsService = Mockito.mock(JiraSettingsService.class);
		final HubIntRestService hubIntRestService = Mockito.mock(HubIntRestService.class);
		final TicketGenerator ticketGenerator = new TicketGenerator(hubIntRestService,
				vulnerableBomComponentRestService, notificationDataService,
				jiraServices, jiraContext,
				settingsService, null);

		final SortedSet<NotificationContentItem> notificationItems = new TreeSet<>();
		notificationItems.addAll(mockNewVulnerabilityNotificationItems(createDuplicateNotification));

		mockNotificationServiceDependencies(notificationDataService, notificationItems);

		final ApplicationUser user = mockUser();
		final IssueService issueService = Mockito.mock(IssueService.class);
		final IssueManager issueManager = Mockito.mock(IssueManager.class);
		final IssueInputParameters issueInputParameters = mockJiraIssueParameters();
		final Project atlassianJiraProject = mockJira(jiraServices, jiraContext, user, issueService,
				issueInputParameters, issueManager);
		final IssuePropertyService propertyService = Mockito.mock(IssuePropertyService.class);
		Mockito.when(jiraServices.getPropertyService()).thenReturn(propertyService);
		final CommentManager commentManager = Mockito.mock(CommentManager.class);
		Mockito.when(jiraServices.getCommentManager()).thenReturn(commentManager);

		SetPropertyValidationResult setPropValidationResult = null;
		MutableIssue oldIssue = null;

		if (openIssue) {
			if (jiraIssueExistsAsClosed) {
				oldIssue = mockIssueExists(issueService, atlassianJiraProject, jiraServices, jiraContext, false, user);
			} else {
				setPropValidationResult = mockIssueDoesNotExist(issueService, issueInputParameters, user,
						atlassianJiraProject, jiraContext, propertyService);
			}
		} else {
			oldIssue = mockIssueExists(issueService, atlassianJiraProject, jiraServices, jiraContext, true, user);
		}

		final TransitionValidationResult transitionValidationResult = mockTransition(issueService, oldIssue);

		final Set<HubProjectMapping> hubProjectMappings = mockProjectMappings();

		// Test
		ticketGenerator.generateTicketsForRecentNotifications(
				new HubProjectMappings(jiraServices, hubProjectMappings), new Date(JAN_1_2016),
				new Date(JAN_2_2016));

		// Verify

		final int expectedCreateIssueCount = 1;
		final int expectedCloseIssueCount = 1;
		final int expectedCommentCount = 1;

		if (openIssue) {
			if (jiraIssueExistsAsClosed) {
				Mockito.verify(issueService, Mockito.times(expectedCreateIssueCount)).transition(user,
						transitionValidationResult);
			} else {
				Mockito.verify(issueInputParameters, Mockito.times(expectedCreateIssueCount))
				.setSummary(expectedIssueSummary);
				Mockito.verify(issueInputParameters, Mockito.times(expectedCreateIssueCount))
				.setDescription(expectedIssueDescription);
				Mockito.verify(propertyService, Mockito.times(expectedCreateIssueCount)).setProperty(user,
						setPropValidationResult);
				Mockito.verify(issueService, Mockito.times(expectedCreateIssueCount))
				.create(Mockito.any(ApplicationUser.class), Mockito.any(CreateValidationResult.class));
			}
		} else {
			Mockito.verify(issueService, Mockito.times(expectedCloseIssueCount)).transition(user,
					transitionValidationResult);
		}

		Mockito.verify(commentManager, Mockito.times(expectedCommentCount)).create(Mockito.any(Issue.class),
				Mockito.eq(user), Mockito.eq(VULNERABILITY_ISSUE_COMMENT), Mockito.eq(true));

	}

	private void testRuleNotifications(final boolean jiraIssueExistsAsClosed, final boolean openIssue,
			final boolean createDuplicateNotification)
					throws NotificationServiceException, ParseException, IOException, URISyntaxException,
					ResourceDoesNotExistException, BDRestException, UnexpectedHubResponseException, MissingUUIDException {

		// Setup

		final NotificationDataService notificationDataService = Mockito.mock(NotificationDataService.class);
		final JiraContext jiraContext = Mockito.mock(JiraContext.class);
		final JiraServices jiraServices = Mockito.mock(JiraServices.class);
		final JiraSettingsService settingsService = Mockito.mock(JiraSettingsService.class);
		final HubIntRestService hubIntRestService = Mockito.mock(HubIntRestService.class);

		final TicketGenerator ticketGenerator = new TicketGenerator(hubIntRestService,
				vulnerableBomComponentRestService, notificationDataService,
				jiraServices, jiraContext,
				settingsService, null);

		final SortedSet<NotificationContentItem> notificationItems = new TreeSet<>();
		if (openIssue) {
			notificationItems.addAll(mockRuleViolationNotificationItems(createDuplicateNotification));
		} else {
			notificationItems.addAll(mockPolicyOverrideNotificationItems());
		}
		mockNotificationServiceDependencies(notificationDataService, notificationItems);

		final ApplicationUser user = mockUser();
		final IssueService issueService = Mockito.mock(IssueService.class);
		final IssueManager issueManager = Mockito.mock(IssueManager.class);
		final IssueInputParameters issueInputParameters = mockJiraIssueParameters();
		final Project atlassianJiraProject = mockJira(jiraServices, jiraContext, user, issueService,
				issueInputParameters, issueManager);
		final IssuePropertyService propertyService = Mockito.mock(IssuePropertyService.class);
		Mockito.when(jiraServices.getPropertyService()).thenReturn(propertyService);

		final CommentManager commentManager = Mockito.mock(CommentManager.class);
		Mockito.when(jiraServices.getCommentManager()).thenReturn(commentManager);

		SetPropertyValidationResult setPropValidationResult = null;
		MutableIssue oldIssue = null;

		if (openIssue) {
			if (jiraIssueExistsAsClosed) {
				oldIssue = mockIssueExists(issueService, atlassianJiraProject, jiraServices, jiraContext, false, user);
				Mockito.when(issueManager.updateIssue(Mockito.any(ApplicationUser.class),
						Mockito.any(MutableIssue.class), Mockito.any(UpdateIssueRequest.class))).thenReturn(oldIssue);
			} else {
				setPropValidationResult = mockIssueDoesNotExist(issueService, issueInputParameters, user,
						atlassianJiraProject, jiraContext, propertyService);
			}
		} else {
			oldIssue = mockIssueExists(issueService, atlassianJiraProject, jiraServices, jiraContext, true, user);
		}

		final TransitionValidationResult transitionValidationResult = mockTransition(issueService, oldIssue);

		final Set<HubProjectMapping> hubProjectMappings = mockProjectMappings();

		// Test

		ticketGenerator.generateTicketsForRecentNotifications(
				new HubProjectMappings(jiraServices, hubProjectMappings), new Date(JAN_1_2016),
				new Date(JAN_2_2016));

		// Verify

		final int expectedCreateIssueCount = 1;
		final int expectedCloseIssueCount = 1;

		if (openIssue) {
			if (jiraIssueExistsAsClosed) {
				Mockito.verify(issueService, Mockito.times(expectedCreateIssueCount)).transition(user,
						transitionValidationResult);
				Mockito.verify(commentManager, Mockito.times(1)).create(Mockito.any(Issue.class), Mockito.eq(user),
						Mockito.eq(HubJiraConstants.HUB_POLICY_VIOLATION_REOPEN), Mockito.eq(true));
			} else {
				Mockito.verify(issueInputParameters, Mockito.times(expectedCreateIssueCount)).setSummary(
						"Black Duck Policy Violation detected on Hub Project 'projectName' / 'hubProjectVersionName', component 'componentName' / 'componentVersionName' [Rule: 'someRule']");
				Mockito.verify(issueInputParameters, Mockito.times(expectedCreateIssueCount)).setDescription(
						"The Black Duck Hub has detected a Policy Violation on Hub Project 'projectName' / 'hubProjectVersionName', component 'componentName' / 'componentVersionName'. The rule violated is: 'someRule'. Rule overridable : true");
				Mockito.verify(issueService, Mockito.times(expectedCreateIssueCount))
				.create(Mockito.any(ApplicationUser.class), Mockito.any(CreateValidationResult.class));
				Mockito.verify(propertyService, Mockito.times(expectedCreateIssueCount)).setProperty(user,
						setPropValidationResult);
			}
		} else {
			Mockito.verify(issueService, Mockito.times(expectedCloseIssueCount)).transition(user,
					transitionValidationResult);
		}

	}

	private Project mockJira(final JiraServices jiraServices, final JiraContext jiraContext, final ApplicationUser user,
			final IssueService issueService, final IssueInputParameters issueInputParameters,
			final IssueManager issueManager) throws NotificationServiceException {
		Mockito.when(jiraContext.getJiraUser()).thenReturn(user);
		Mockito.when(issueService.newIssueInputParameters()).thenReturn(issueInputParameters);
		Mockito.when(jiraServices.getIssueService()).thenReturn(issueService);
		final JiraAuthenticationContext authContext = Mockito.mock(JiraAuthenticationContext.class);
		Mockito.when(jiraServices.getAuthContext()).thenReturn(authContext);
		mockJsonEntityPropertyManager(jiraServices, jiraContext);
		final Project atlassianJiraProject = mockJiraProject(jiraServices, jiraContext);
		final ConstantsManager constantsManager = Mockito.mock(ConstantsManager.class);
		final Collection<IssueType> issueTypes = new ArrayList<>();
		final IssueType policyIssueType = Mockito.mock(IssueType.class);
		Mockito.when(policyIssueType.getName()).thenReturn(HubJiraConstants.HUB_POLICY_VIOLATION_ISSUE);
		Mockito.when(policyIssueType.getId()).thenReturn("policyIssueTypeId");
		issueTypes.add(policyIssueType);
		final IssueType vulnerabilityIssueType = Mockito.mock(IssueType.class);
		Mockito.when(vulnerabilityIssueType.getName()).thenReturn(HubJiraConstants.HUB_VULNERABILITY_ISSUE);
		Mockito.when(vulnerabilityIssueType.getId()).thenReturn("vulnerabilityIssueTypeId");
		issueTypes.add(vulnerabilityIssueType);

		Mockito.when(constantsManager.getAllIssueTypeObjects()).thenReturn(issueTypes);
		Mockito.when(jiraServices.getConstantsManager()).thenReturn(constantsManager);
		// Mockito.when(issueManager.updateIssue(Mockito.any(ApplicationUser.class),
		// Mockito.any(MutableIssue.class),
		// Mockito.any(UpdateIssueRequest.class))).thenReturn(value);
		Mockito.when(jiraServices.getIssueManager()).thenReturn(issueManager);
		return atlassianJiraProject;
	}

	private TransitionValidationResult mockTransition(final IssueService issueService, final MutableIssue oldIssue) {
		final TransitionValidationResult validationResult = Mockito.mock(TransitionValidationResult.class);
		Mockito.when(validationResult.isValid()).thenReturn(true);
		final IssueResult transitionResult = Mockito.mock(IssueResult.class);
		Mockito.when(transitionResult.getErrorCollection()).thenReturn(succeeded);
		Mockito.when(transitionResult.getIssue()).thenReturn(oldIssue);
		Mockito.when(issueService.transition(Mockito.any(ApplicationUser.class),
				Mockito.any(TransitionValidationResult.class))).thenReturn(transitionResult);
		Mockito.when(issueService.validateTransition(Mockito.any(ApplicationUser.class), Mockito.anyLong(),
				Mockito.anyInt(), Mockito.any(IssueInputParameters.class))).thenReturn(validationResult);
		return validationResult;
	}

	private JsonEntityPropertyManager mockJsonEntityPropertyManager(final JiraServices jiraServices,
			final JiraContext jiraContext) {
		final EntityPropertyQuery entityPropertyQuery = mockEntityPropertyQuery();
		final JsonEntityPropertyManager jsonEntityPropertyManager = Mockito.mock(JsonEntityPropertyManager.class);
		Mockito.when(jsonEntityPropertyManager.query()).thenReturn(entityPropertyQuery);
		Mockito.when(jiraServices.getJsonEntityPropertyManager()).thenReturn(jsonEntityPropertyManager);
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
		Mockito.when(entityProperty.getValue()).thenReturn(
				"{\"projectName\":\"SB001\",\"projectVersion\":\"1\",\"componentName\":\"SeaMonkey\",\"componentVersion\":\"1.0.3\",\"ruleName\":\"apr28\",\"jiraIssueId\":"
						+ JIRA_ISSUE_ID + "}");
		return executableQuery;
	}

	private Set<HubProjectMapping> mockProjectMappings() {
		final Set<HubProjectMapping> hubProjectMappings = new HashSet<>();

		HubProjectMapping hubProjectMapping = new HubProjectMapping();
		HubProject hubProject = new HubProject();
		hubProject.setProjectName("4Drew");
		hubProject.setProjectUrl("4DrewProjectUrl");
		hubProjectMapping.setHubProject(hubProject);

		JiraProject bdsJiraProject = mockBdsJiraProject();
		hubProjectMapping.setJiraProject(bdsJiraProject);

		hubProjectMappings.add(hubProjectMapping);

		hubProjectMapping = new HubProjectMapping();
		hubProject = new HubProject();
		hubProject.setProjectName("projectName");
		hubProject.setProjectUrl("projectUrl");
		hubProjectMapping.setHubProject(hubProject);

		bdsJiraProject = mockBdsJiraProject();
		hubProjectMapping.setJiraProject(bdsJiraProject);

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

	private JiraProject mockBdsJiraProject() {
		final JiraProject jiraProject = new JiraProject();
		jiraProject.setProjectId(123L);
		jiraProject.setProjectKey("jiraProjectKey");
		jiraProject.setProjectName("jiraProjectName");
		jiraProject.setAssigneeUserId("assigneeUserId");
		return jiraProject;
	}

	private void mockNotificationServiceDependencies(final NotificationDataService notificationDataService,
			final SortedSet<NotificationContentItem> notificationItems)
					throws IOException, URISyntaxException, ResourceDoesNotExistException, BDRestException,
					NotificationServiceException, UnexpectedHubResponseException, MissingUUIDException {

		Mockito.when(
				notificationDataService.getAllNotifications(Mockito.any(Date.class), Mockito.any(Date.class)))
				.thenReturn(notificationItems);
	}

	private List<PolicyViolationContentItem> mockRuleViolationNotificationItems(final boolean createDuplicate)
			throws URISyntaxException {
		final List<PolicyViolationContentItem> notificationItems = new ArrayList<>();

		final List<PolicyRule> policyRules = new ArrayList<>();
		final MetaInformation meta = new MetaInformation(null, POLICY_RULE_URL, null);
		final PolicyRule rule = new PolicyRule(meta, "someRule", null, null, true, null, null, null, null, null);
		policyRules.add(rule);

		final ProjectVersion projectVersion = new ProjectVersion();
		projectVersion.setProjectName("projectName");
		projectVersion.setProjectVersionName("hubProjectVersionName");

		projectVersion.setUrl(
				"http://localhost/projects/" + UUID.randomUUID() + "/versions/" + UUID.randomUUID());

		final String componentVersionUrl = "http://hub.blackducksoftware.com/api/projects/" + UUID.randomUUID()
				+ "/versions/" + UUID.randomUUID() + "/";
		final PolicyViolationContentItem notif = new PolicyViolationContentItem(new Date(), projectVersion,
				"componentName",
				"componentVersionName", componentVersionUrl, policyRules);

		notificationItems.add(notif);
		if (createDuplicate) {
			notificationItems.add(notif);
		}
		return notificationItems;
	}

	private List<VulnerabilityContentItem> mockNewVulnerabilityNotificationItems(final boolean createDuplicate)
			throws IOException, URISyntaxException {
		final List<VulnerabilityContentItem> notificationItems = new ArrayList<>();

		final String projectVersionUrl = "http://eng-hub-valid01.dc1.lan/api/projects/3670db83-7916-4398-af2c-a05798bbf2ef/versions/17b5cf06-439f-4ffe-9b4f-d262f56b2d8f";
		final ProjectVersion projectVersion = new ProjectVersion();
		projectVersion.setProjectName("4Drew");
		projectVersion.setProjectVersionName("2Drew");
		projectVersion.setUrl(projectVersionUrl);

		final List<VulnerabilitySourceQualifiedId> addedVulnList = new ArrayList<>();
		final VulnerabilitySourceQualifiedId vuln = new VulnerabilitySourceQualifiedId("NVD", "CVE-2016-0001");
		addedVulnList.add(vuln);

		final VulnerabilityContentItem notif = new VulnerabilityContentItem(new Date(), projectVersion, "TestNG",
				"2.0.0",
				projectVersionUrl, addedVulnList,
				new ArrayList<VulnerabilitySourceQualifiedId>(), new ArrayList<VulnerabilitySourceQualifiedId>());

		notificationItems.add(notif);
		if (createDuplicate) {
			notificationItems.add(notif);
		}
		return notificationItems;
	}

	private List<PolicyOverrideContentItem> mockPolicyOverrideNotificationItems() throws URISyntaxException {
		final List<PolicyOverrideContentItem> notificationItems = new ArrayList<>();

		final ProjectVersion projectVersion = new ProjectVersion();
		projectVersion.setProjectName("projectName");
		projectVersion.setProjectVersionName("projectVersionName");
		projectVersion.setUrl(
				"http://localhost/projects/" + UUID.randomUUID() + "/versions/" + UUID.randomUUID());

		final List<PolicyRule> policyRules = new ArrayList<>();
		final MetaInformation meta = new MetaInformation(null, POLICY_RULE_URL, null);
		final PolicyRule rule = new PolicyRule(meta, "someRule", null, null, true, null, null, null, null, null);
		policyRules.add(rule);

		final String componentVersionUrl = "http://hub.blackducksoftware.com/api/projects/"
				+ "0934ea45-c739-4b58-bcb1-ee777022ce4f" + "/versions/" + "7c45d411-92ca-45b0-80fc-76b765b954ef";
		final PolicyOverrideContentItem notif = new PolicyOverrideContentItem(new Date(), projectVersion,
				"componentName",
				"componentVersionName", componentVersionUrl, policyRules, "firstName", "lastName");

		notificationItems.add(notif);
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
		Mockito.when(issueInputParameters.setAssigneeId(Mockito.anyString())).thenReturn(issueInputParameters);
		return issueInputParameters;
	}

	private Project mockJiraProject(final JiraServices jiraServices, final JiraContext jiraContext)
			throws NotificationServiceException {
		final ProjectManager jiraProjectManager = Mockito.mock(ProjectManager.class);
		Mockito.when(jiraServices.getJiraProjectManager()).thenReturn(jiraProjectManager);

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

		// jiraServices.getJiraProject(mappingJiraProject.getProjectId());
		Mockito.when(jiraServices.getJiraProject(123L)).thenReturn(mockBdsJiraProject());
		return atlassianJiraProject;
	}

	private MutableIssue mockIssueExists(final IssueService issueService, final Project atlassianJiraProject,
			final JiraServices jiraServices, final JiraContext jiraContext, final boolean open,
			final ApplicationUser user) {
		final IssueResult getOldIssueResult = Mockito.mock(IssueResult.class);
		Mockito.when(getOldIssueResult.isValid()).thenReturn(true);
		final IssueImpl oldIssue = Mockito.mock(IssueImpl.class);
		Mockito.when(getOldIssueResult.getIssue()).thenReturn(oldIssue);
		final Status oldIssueStatus = Mockito.mock(Status.class);
		final Resolution oldIssueResolution = Mockito.mock(Resolution.class);
		String state;
		String resolution;
		if (open) {
			state = HubJiraConstants.HUB_WORKFLOW_STATUS_OPEN;
			resolution = "Unresolved";
		} else {
			state = HubJiraConstants.HUB_WORKFLOW_STATUS_RESOLVED;
			resolution = "Resolved";
		}
		Mockito.when(oldIssueStatus.getName()).thenReturn(state);
		Mockito.when(oldIssue.getStatusObject()).thenReturn(oldIssueStatus);
		Mockito.when(oldIssue.getStatus()).thenReturn(oldIssueStatus);
		Mockito.when(oldIssueResolution.getName()).thenReturn(resolution);
		Mockito.when(oldIssue.getResolutionObject()).thenReturn(oldIssueResolution);
		Mockito.when(issueService.getIssue(user, JIRA_ISSUE_ID)).thenReturn(getOldIssueResult);
		Mockito.when(oldIssue.getProjectObject()).thenReturn(atlassianJiraProject);
		final IssueType oldIssueType = Mockito.mock(IssueType.class);
		Mockito.when(oldIssueType.getName()).thenReturn("Mocked issue type");
		Mockito.when(oldIssue.getIssueTypeObject()).thenReturn(oldIssueType);
		Mockito.when(oldIssue.getIssueType()).thenReturn(oldIssueType);
		final WorkflowManager workflowManager = Mockito.mock(WorkflowManager.class);
		Mockito.when(jiraServices.getWorkflowManager()).thenReturn(workflowManager);
		final JiraWorkflow jiraWorkflow = Mockito.mock(JiraWorkflow.class);
		final StepDescriptor stepDescriptor = Mockito.mock(StepDescriptor.class);
		Mockito.when(jiraWorkflow.getLinkedStep(oldIssueStatus)).thenReturn(stepDescriptor);
		final List<ActionDescriptor> actions = new ArrayList<>();
		final ActionDescriptor actionDescriptor = Mockito.mock(ActionDescriptor.class);
		actions.add(actionDescriptor);
		String transition;
		if (open) {
			transition = HubJiraConstants.HUB_WORKFLOW_TRANSITION_REMOVE_OR_OVERRIDE;
		} else {
			transition = HubJiraConstants.HUB_WORKFLOW_TRANSITION_READD_OR_OVERRIDE_REMOVED;
		}
		Mockito.when(actionDescriptor.getName()).thenReturn(transition);
		Mockito.when(stepDescriptor.getActions()).thenReturn(actions);
		Mockito.when(workflowManager.getWorkflow(oldIssue)).thenReturn(jiraWorkflow);

		return oldIssue;
	}

	private SetPropertyValidationResult mockIssueDoesNotExist(final IssueService issueService,
			final IssueInputParameters issueInputParameters, final ApplicationUser user,
			final Project atlassianJiraProject, final JiraContext jiraContext,
			final IssuePropertyService propertyService) {

		final MutableIssue newIssue = Mockito.mock(MutableIssue.class);
		Mockito.when(newIssue.getProjectObject()).thenReturn(atlassianJiraProject);
		final Status newIssueStatus = Mockito.mock(Status.class);
		Mockito.when(newIssueStatus.getName()).thenReturn(HubJiraConstants.HUB_WORKFLOW_STATUS_OPEN);
		Mockito.when(newIssue.getStatusObject()).thenReturn(newIssueStatus);
		Mockito.when(newIssue.getStatus()).thenReturn(newIssueStatus);
		final Resolution newIssueResolution = Mockito.mock(Resolution.class);
		Mockito.when(newIssueResolution.getName()).thenReturn("Unresolved");
		Mockito.when(newIssue.getResolutionObject()).thenReturn(newIssueResolution);

		final IssueType newIssueType = Mockito.mock(IssueType.class);
		Mockito.when(newIssueType.getName()).thenReturn("Mocked issue type");
		Mockito.when(newIssue.getIssueTypeObject()).thenReturn(newIssueType);
		Mockito.when(newIssue.getIssueType()).thenReturn(newIssueType);
		Mockito.when(newIssue.getKey()).thenReturn("TEST-1");
		Mockito.when(newIssue.getAssigneeId()).thenReturn("assignedUserId");
		Mockito.when(newIssue.getAssignee()).thenReturn(user);

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
		Mockito.when(propertyService.validateSetProperty(Mockito.any(ApplicationUser.class), Mockito.anyLong(),
				Mockito.any(PropertyInput.class))).thenReturn(setPropValidationResult);

		final PropertyResult setPropertyResult = Mockito.mock(PropertyResult.class);
		Mockito.when(setPropertyResult.getErrorCollection()).thenReturn(succeeded);
		Mockito.when(propertyService.setProperty(user, setPropValidationResult)).thenReturn(setPropertyResult);

		return setPropValidationResult;
	}

}
