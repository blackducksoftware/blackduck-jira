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

import java.io.IOException;
import java.net.URISyntaxException;
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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.IssueService.IssueResult;
import com.atlassian.jira.bc.issue.IssueService.TransitionValidationResult;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.entity.property.EntityPropertyQuery;
import com.atlassian.jira.entity.property.EntityPropertyQuery.ExecutableQuery;
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
import com.blackducksoftware.integration.jira.hub.model.notification.RuleViolationNotificationContent;
import com.blackducksoftware.integration.jira.hub.model.notification.RuleViolationNotificationItem;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;

/**
 *
 *
 */
public class TicketGeneratorTest {
	private static final long JAN_2_2016 = 1451710800000L;
	private static final long JAN_1_2016 = 1451624400000L;

	private static SimpleDateFormat dateFormatter;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		dateFormatter = new SimpleDateFormat(RestConnection.JSON_DATE_FORMAT);
		dateFormatter.setTimeZone(java.util.TimeZone.getTimeZone("Zulu"));
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	// TODO write tests

	@Test
	public void testCreateNewJiraIssue() {

	}

	@Test
	public void testCloseJiraIssue() {

	}

	@Test
	public void testReOpenJiraIssue() throws HubNotificationServiceException, ParseException, IOException,
	URISyntaxException,
	ResourceDoesNotExistException, BDRestException, UnexpectedHubResponseException {
		test();
	}

	private void test() throws HubNotificationServiceException, ParseException, IOException, URISyntaxException,
	ResourceDoesNotExistException, BDRestException, UnexpectedHubResponseException {



		final HubItemsService<NotificationItem> hubItemsService = Mockito.mock(HubItemsService.class);
		final HubNotificationService notificationService = mockHubNotificationService(hubItemsService);

		final TicketGeneratorInfo jiraTicketGeneratorInfoService = Mockito.mock(TicketGeneratorInfo.class);
		final TicketGenerator ticketGenerator = new TicketGenerator(notificationService, jiraTicketGeneratorInfoService);

		final List<NotificationItem> notificationItems = mockNotificationItems();
		final Set<SimpleEntry<String, String>> queryParameters = mockHubQueryParameters(JAN_1_2016, JAN_2_2016);
		mockHubNotificationsGet(hubItemsService, notificationService, queryParameters, notificationItems);

		final ApplicationUser user = mockUser();
		Mockito.when(jiraTicketGeneratorInfoService.getJiraUser()).thenReturn(user);

		final IssueService issueService = Mockito.mock(IssueService.class);
		final IssueInputParameters issueInputParameters = mockJiraIssueParameters();
		Mockito.when(issueService.newIssueInputParameters()).thenReturn(issueInputParameters);
		Mockito.when(jiraTicketGeneratorInfoService.getIssueService()).thenReturn(issueService);

		final JiraAuthenticationContext authContext = Mockito.mock(JiraAuthenticationContext.class);
		Mockito.when(jiraTicketGeneratorInfoService.getAuthContext()).thenReturn(authContext);
		Mockito.when(jiraTicketGeneratorInfoService.getJiraIssueTypeName()).thenReturn("jiraIssueTypeName");

		final JsonEntityPropertyManager jsonEntityPropertyManager = mockJsonEntityPropertyManager(jiraTicketGeneratorInfoService);

		final Project atlassianJiraProject = mockJiraProject(jiraTicketGeneratorInfoService);

		// The JIRA issue already exists, but it's closed
		mockExistingClosedIssue(issueService, atlassianJiraProject, jiraTicketGeneratorInfoService);

		final TransitionValidationResult validationResult = Mockito.mock(TransitionValidationResult.class);
		Mockito.when(validationResult.isValid()).thenReturn(true);

		final IssueResult transitionResult = Mockito.mock(IssueResult.class);
		final ErrorCollection errors = Mockito.mock(ErrorCollection.class);
		Mockito.when(errors.hasAnyErrors()).thenReturn(false);
		Mockito.when(transitionResult.getErrorCollection()).thenReturn(errors);
		Mockito.when(
				issueService.transition(Mockito.any(ApplicationUser.class),
						Mockito.any(TransitionValidationResult.class))).thenReturn(
								transitionResult);

		Mockito.when(
				issueService.validateTransition(Mockito.any(ApplicationUser.class), Mockito.anyLong(),
						Mockito.anyInt(), Mockito.any(IssueInputParameters.class))).thenReturn(validationResult);


		final Set<HubProjectMapping> hubProjectMappings = mockProjectMappings();

		final List<String> linksOfRulesToMonitor = new ArrayList<>();
		linksOfRulesToMonitor.add("ruleUrl");

		final NotificationDateRange notificationDateRange = new NotificationDateRange(new Date(JAN_1_2016), new Date(
				JAN_2_2016));

		ticketGenerator.generateTicketsForRecentNotifications(hubProjectMappings, linksOfRulesToMonitor,
				notificationDateRange);

		// Verify

		Mockito.verify(issueInputParameters)
		.setSummary(
				"Black Duck Policy Violation detected on Hub Project 'projectName' / 'hubProjectVersionName', component 'componentName' / 'componentVersionName' [Rule: 'someRule']");
		Mockito.verify(issueInputParameters)
		.setDescription(
				"The Black Duck Hub has detected a Policy Violation on Hub Project 'projectName', component 'componentName' / 'componentVersionName'. The rule violated is: 'someRule'. Rule overridable : true");

		// Verify re-open
		Mockito.verify(issueService).transition(user, validationResult);
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
				"{\"projectName\":\"SB001\",\"projectVersion\":\"1\",\"componentName\":\"SeaMonkey\",\"componentVersion\":\"1.0.3\",\"ruleName\":\"apr28\",\"jiraIssueId\":10000}");
		return executableQuery;
	}

	private Set<HubProjectMapping> mockProjectMappings() {
		final HubProjectMapping hubProjectMapping = new HubProjectMapping();
		final HubProject hubProject = new HubProject();
		hubProject.setProjectName("hubProjectName");
		hubProject.setProjectUrl("hubProjectUrl");
		hubProjectMapping.setHubProject(hubProject);

		final JiraProject bdsJiraProject = mockBdsJiraProject();
		hubProjectMapping.setJiraProject(bdsJiraProject);

		final Set<HubProjectMapping> hubProjectMappings = new HashSet<>();
		hubProjectMappings.add(hubProjectMapping);
		return hubProjectMappings;
	}

	private HubNotificationService mockHubNotificationService(final HubItemsService<NotificationItem> hubItemsService) {
		final RestConnection restConnection = Mockito.mock(RestConnection.class);
		final HubIntRestService hub = Mockito.mock(HubIntRestService.class);
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

	private void mockHubNotificationsGet(final HubItemsService<NotificationItem> hubItemsService,
			final HubNotificationService notificationService,
			final Set<SimpleEntry<String, String>> queryParameters, final List<NotificationItem> notificationItems)
					throws IOException, URISyntaxException, ResourceDoesNotExistException, BDRestException,
					HubNotificationServiceException, UnexpectedHubResponseException {
		final List<String> urlSegments = new ArrayList<>();
		urlSegments.add("api");
		urlSegments.add("notifications");
		Mockito.when(hubItemsService.httpGetItemList(urlSegments, queryParameters)).thenReturn(notificationItems);

		List<MetaLink> links = new ArrayList<>();
		links.add(new MetaLink("project", "hubProjectUrl"));
		final String href = "http://eng-hub-valid03.dc1.lan/api/projects/073e0506-0d91-4d95-bd51-740d9ba52d96/versions/35430a68-3007-4777-90af-2e3f41738ac0";
		final MetaInformation projectMeta = new MetaInformation(null, href, links);
		final ReleaseItem releaseItem = new ReleaseItem("hubProjectVersionName", "projectPhase", "projectDistribution",
				"projectSource", projectMeta);
		Mockito.when(notificationService.getProjectReleaseItemFromProjectReleaseUrl("hubProjectVersionUrl"))
		.thenReturn(
				releaseItem);
		final ComponentVersion componentVersion = Mockito.mock(ComponentVersion.class);
		Mockito.when(componentVersion.getVersionName()).thenReturn("componentVersionName");
		Mockito.when(
				notificationService
				.getComponentVersion("http://eng-hub-valid03.dc1.lan/api/components/0934ea45-c739-4b58-bcb1-ee777022ce4f/versions/7c45d411-92ca-45b0-80fc-76b765b954ef"))
				.thenReturn(componentVersion);
		links = new ArrayList<>();
		links.add(new MetaLink("policy-rule", "ruleUrl"));
		final MetaInformation bomComponentVersionPolicyStatusMeta = new MetaInformation(null, null, links);
		final BomComponentVersionPolicyStatus bomComponentVersionPolicyStatus = new BomComponentVersionPolicyStatus(
				bomComponentVersionPolicyStatusMeta);
		Mockito.when(notificationService.getPolicyStatus("bomComponentVersionPolicyStatusLink")).thenReturn(
				bomComponentVersionPolicyStatus);
		final MetaInformation policyRuleMeta = new MetaInformation(null,
				"http://eng-hub-valid03.dc1.lan/api/policy-rules/0068397a-3e23-46bc-b1b7-82fb800e34ad", null);

		final List<PolicyValue> policyValues = new ArrayList<>();
		final PolicyValue policyValue = new PolicyValue("policyLabel", "policyValue");
		policyValues.add(policyValue);
		final List<PolicyExpression> policyExpressionList = new ArrayList<>();
		final PolicyExpression policyExpression = new PolicyExpression("COMPONENT_USAGE", "AND", policyValues);
		policyExpressionList.add(policyExpression);
		final PolicyExpressions policyExpressionsObject = new PolicyExpressions("AND", policyExpressionList);
		final PolicyRule rule = new PolicyRule(policyRuleMeta, "someRule", "Some Rule", true, true,
				policyExpressionsObject,
				null,
				null, null, null);
		Mockito.when(notificationService.getPolicyRule("ruleUrl")).thenReturn(rule);
	}

	private List<NotificationItem> mockNotificationItems() {
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

	private void mockExistingClosedIssue(final IssueService issueService, final Project atlassianJiraProject,
			final TicketGeneratorInfo jiraTicketGeneratorInfoService) {
		final IssueResult getOldIssueResult = Mockito.mock(IssueResult.class);
		Mockito.when(getOldIssueResult.isValid()).thenReturn(true);
		final MutableIssue oldIssue = Mockito.mock(MutableIssue.class);
		Mockito.when(getOldIssueResult.getIssue()).thenReturn(oldIssue);
		final Status oldIssueStatus = Mockito.mock(Status.class);
		Mockito.when(oldIssueStatus.getName()).thenReturn("Done");
		Mockito.when(oldIssue.getStatusObject()).thenReturn(oldIssueStatus);
		Mockito.when(issueService.getIssue(Mockito.any(ApplicationUser.class), Mockito.anyLong())).thenReturn(
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
		Mockito.when(actionDescriptor.getName()).thenReturn("Reopen");
		Mockito.when(stepDescriptor.getActions()).thenReturn(actions);
		Mockito.when(workflowManager.getWorkflow(oldIssue)).thenReturn(jiraWorkflow);
	}

}
