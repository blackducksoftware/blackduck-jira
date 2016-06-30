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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.mockito.Mockito;

import com.atlassian.jira.project.ProjectManager;
import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.hub.meta.MetaInformation;
import com.blackducksoftware.integration.hub.meta.MetaLink;
import com.blackducksoftware.integration.hub.policy.api.PolicyRule;
import com.blackducksoftware.integration.hub.version.api.ReleaseItem;
import com.blackducksoftware.integration.jira.config.HubProject;
import com.blackducksoftware.integration.jira.config.HubProjectMapping;
import com.blackducksoftware.integration.jira.config.JiraProject;
import com.blackducksoftware.integration.jira.hub.model.component.BomComponentVersionPolicyStatus;
import com.blackducksoftware.integration.jira.hub.model.component.ComponentVersion;
import com.blackducksoftware.integration.jira.mocks.ProjectManagerMock;

public class JiraNotificationFilterTest {

	private static final String TEST_PROJECT_VERSION_PREFIX = "testVersionName";
	private static final String HUB_COMPONENT_NAME_PREFIX = "test Hub Component";
	private static final String HUB_PROJECT_NAME_PREFIX = "test Hub Project";
	private static final String NOTIF_URL_PREFIX = "http://test.notif.url";
	private static final String PROJECT_URL_PREFIX = "http://test.project.url";
	private static final String JIRA_ISSUE_TYPE = "Task";
	private static final String JIRA_USER_NAME = "TestUser";
	private static final String CURRENT_JIRA_PROJECT_NAME = "test JIRA Project0a";
	private static final String CURRENT_JIRA_PROJECT_KEY = "TEST0a";
	private static final String BOM_COMPONENT_VERSION_POLICY_STATUS_LINK_PREFIX = "bomComponentVersionPolicyStatusLink";
	private static final String COMPONENT_VERSION_LINK_PREFIX = "componentVersionLink";
	private static final String VERSION_NAME_PREFIX = "versionName";
	private static final String PROJECTVERSION_URL_PREFIX = "http://test.projectversion.url";
	private static final String RULE_URL_PREFIX = "ruleUrl";
	private static final String RULE_NAME_PREFIX = "ruleName";
	private static final String RULE_LINK_NAME = "policy-rule";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	// @Test
	// public void testWithRuleListWithMatches() throws
	// HubNotificationServiceException, UnexpectedHubResponseException {
	// final HubNotificationService mockHubNotificationService =
	// createMockHubNotificationService(true);
	// final ProjectManager jiraProjectManager = createJiraProjectManager();
	// final ApplicationUserMock jiraUser = new ApplicationUserMock();
	// jiraUser.setName(JIRA_USER_NAME);
	// final IssueServiceMock issueService = new IssueServiceMock();
	// final JiraAuthenticationContextMock authContext = new
	// JiraAuthenticationContextMock();
	//
	// final TicketGeneratorInfo ticketGenInfo = new
	// TicketGeneratorInfo(jiraProjectManager, issueService, jiraUser,
	// JIRA_ISSUE_TYPE, authContext);
	//
	// final Set<HubProjectMapping> mappings = createMappings(true);
	//
	// final List<NotificationItem> notifications = createNotifications();
	// final List<String> rulesToMonitor = new ArrayList<>();
	// rulesToMonitor.add("ruleUrl0");
	// rulesToMonitor.add("ruleUrl1");
	//
	// final JiraNotificationFilter filter = new
	// JiraNotificationFilter(mockHubNotificationService, mappings,
	// rulesToMonitor, ticketGenInfo);
	// final List<IssueInputParameters> issues =
	// filter.extractJiraReadyNotifications(notifications);
	//
	// System.out.println("IssueInputParameters:");
	// for (final IssueInputParameters issue : issues) {
	// System.out.println(issue);
	// assertEquals(JIRA_USER_NAME, issue.getReporterId());
	//
	// assertTrue(issue.getSummary().contains(HUB_PROJECT_NAME_PREFIX));
	// assertTrue(issue.getSummary().contains(TEST_PROJECT_VERSION_PREFIX));
	// assertTrue(issue.getSummary().contains(JiraNotificationFilter.ISSUE_TYPE_DESCRIPTION_RULE_VIOLATION));
	// assertTrue(issue.getSummary().contains(HUB_COMPONENT_NAME_PREFIX));
	// assertTrue(issue.getSummary().contains(VERSION_NAME_PREFIX));
	//
	// assertTrue(issue.getDescription().contains(HUB_PROJECT_NAME_PREFIX));
	// assertTrue(!issue.getDescription().contains(TEST_PROJECT_VERSION_PREFIX));
	// assertTrue(issue.getDescription().contains(JiraNotificationFilter.ISSUE_TYPE_DESCRIPTION_RULE_VIOLATION));
	// assertTrue(issue.getDescription().contains(HUB_COMPONENT_NAME_PREFIX));
	// assertTrue(issue.getDescription().contains(VERSION_NAME_PREFIX));
	//
	// }
	// assertEquals(6, issues.size());
	// }
	//
	// @Test
	// public void testWithRuleListNoMatch() throws
	// HubNotificationServiceException, UnexpectedHubResponseException {
	// final HubNotificationService mockHubNotificationService =
	// createMockHubNotificationService(false);
	// final ProjectManager jiraProjectManager = createJiraProjectManager();
	// final ApplicationUserMock jiraUser = new ApplicationUserMock();
	// jiraUser.setName(JIRA_USER_NAME);
	// final IssueServiceMock issueService = new IssueServiceMock();
	// final JiraAuthenticationContextMock authContext = new
	// JiraAuthenticationContextMock();
	//
	// final TicketGeneratorInfo ticketGenInfo = new
	// TicketGeneratorInfo(jiraProjectManager, issueService, jiraUser,
	// JIRA_ISSUE_TYPE, authContext);
	//
	// final Set<HubProjectMapping> mappings = createMappings(true);
	//
	// final List<NotificationItem> notifications = createNotifications();
	// final List<String> rulesToMonitor = new ArrayList<>();
	// rulesToMonitor.add("ruleUrl0");
	// rulesToMonitor.add("ruleUrl1");
	//
	// final JiraNotificationFilter filter = new
	// JiraNotificationFilter(mockHubNotificationService, mappings,
	// rulesToMonitor, ticketGenInfo);
	// final List<IssueInputParameters> issues =
	// filter.extractJiraReadyNotifications(notifications);
	//
	// assertEquals(0, issues.size());
	// }
	//
	// @Test
	// public void testNoMappingMatch() throws HubNotificationServiceException,
	// UnexpectedHubResponseException {
	// final HubNotificationService mockHubNotificationService =
	// createMockHubNotificationService(true);
	// final ProjectManager jiraProjectManager = createJiraProjectManager();
	// final ApplicationUserMock jiraUser = new ApplicationUserMock();
	// jiraUser.setName(JIRA_USER_NAME);
	// final IssueServiceMock issueService = new IssueServiceMock();
	// final JiraAuthenticationContextMock authContext = new
	// JiraAuthenticationContextMock();
	//
	// final TicketGeneratorInfo ticketGenInfo = new
	// TicketGeneratorInfo(jiraProjectManager, issueService, jiraUser,
	// JIRA_ISSUE_TYPE, authContext);
	//
	// final Set<HubProjectMapping> mappings = createMappings(false);
	//
	// final List<NotificationItem> notifications = createNotifications();
	//
	// final JiraNotificationFilter filter = new
	// JiraNotificationFilter(mockHubNotificationService, mappings,
	// null, ticketGenInfo);
	// final List<IssueInputParameters> issues =
	// filter.extractJiraReadyNotifications(notifications);
	//
	// System.out.println("Issues: " + issues);
	//
	// assertEquals(0, issues.size());
	// }
	//
	// @Test
	// public void testWithoutMappings() throws HubNotificationServiceException,
	// UnexpectedHubResponseException {
	// final HubNotificationService mockHubNotificationService =
	// createMockHubNotificationService(true);
	// final ProjectManager jiraProjectManager = createJiraProjectManager();
	// final ApplicationUserMock jiraUser = new ApplicationUserMock();
	// jiraUser.setName(JIRA_USER_NAME);
	// final IssueServiceMock issueService = new IssueServiceMock();
	// final JiraAuthenticationContextMock authContext = new
	// JiraAuthenticationContextMock();
	//
	// final TicketGeneratorInfo ticketGenInfo = new
	// TicketGeneratorInfo(jiraProjectManager, issueService, jiraUser,
	// JIRA_ISSUE_TYPE, authContext);
	//
	// final Set<HubProjectMapping> mappings = null;
	//
	// final List<NotificationItem> notifications = createNotifications();
	//
	// final JiraNotificationFilter filter = new
	// JiraNotificationFilter(mockHubNotificationService, mappings, null,
	// ticketGenInfo);
	// final List<IssueInputParameters> issues =
	// filter.extractJiraReadyNotifications(notifications);
	//
	// System.out.println("Issues:");
	// assertEquals(0, issues.size());
	// }
	//
	// private List<NotificationItem> createNotifications() {
	// final List<NotificationItem> notifications = new ArrayList<>();
	// for (int i = 2; i >= 0; i--) {
	// final MetaInformation meta = new MetaInformation(null, NOTIF_URL_PREFIX +
	// i, null);
	// final RuleViolationNotificationItem notif = new
	// RuleViolationNotificationItem(meta);
	// notif.setCreatedAt(new Date());
	// notif.setType(NotificationType.RULE_VIOLATION);
	// final RuleViolationNotificationContent content = new
	// RuleViolationNotificationContent();
	// content.setProjectName(HUB_PROJECT_NAME_PREFIX + i);
	// content.setProjectVersionLink(PROJECTVERSION_URL_PREFIX + i);
	// final List<ComponentVersionStatus> componentVersionStatuses = new
	// ArrayList<>();
	// final ComponentVersionStatus compVerStatus = new
	// ComponentVersionStatus();
	// compVerStatus.setComponentName(HUB_COMPONENT_NAME_PREFIX + i);
	// compVerStatus.setComponentVersionLink(COMPONENT_VERSION_LINK_PREFIX + i);
	// compVerStatus.setBomComponentVersionPolicyStatusLink(BOM_COMPONENT_VERSION_POLICY_STATUS_LINK_PREFIX
	// + i);
	// componentVersionStatuses.add(compVerStatus);
	// content.setComponentVersionStatuses(componentVersionStatuses);
	// notif.setContent(content);
	// System.out.println("Notif: " + notif);
	// notifications.add(notif);
	// }
	// return notifications;
	// }

	private Set<HubProjectMapping> createMappings(final boolean match) {
		String suffix;
		if (match) {
			suffix = "";
		} else {
			suffix = "XX";
		}
		final Set<HubProjectMapping> mappings = new HashSet<>();

		for (int i = 0; i < 5; i++) {
			final HubProjectMapping mapping = new HubProjectMapping();
			final HubProject hubProject = new HubProject();
			hubProject.setProjectName(HUB_PROJECT_NAME_PREFIX + i);
			hubProject.setProjectUrl(PROJECT_URL_PREFIX + i + suffix);
			mapping.setHubProject(hubProject);
			final JiraProject jiraProject = new JiraProject();
			jiraProject.setProjectId(ProjectManagerMock.JIRA_PROJECT_ID_BASE + i);
			jiraProject.setProjectName(ProjectManagerMock.JIRA_PROJECT_PREFIX + i);
			mapping.setJiraProject(jiraProject);

			System.out.println("Mapping: " + mapping);
			mappings.add(mapping);
		}
		return mappings;
	}

	private ProjectManager createJiraProjectManager() {
		final ProjectManagerMock projectManager = new ProjectManagerMock();
		projectManager.setProjectObjects(ProjectManagerMock.getTestProjectObjectsWithTaskIssueType());
		return projectManager;
	}

	private HubNotificationService createMockHubNotificationService(final boolean ruleMatches)
			throws HubNotificationServiceException, UnexpectedHubResponseException {
		String suffix;
		if (ruleMatches) {
			suffix = "";
		} else {
			suffix = "XX";
		}
		final HubNotificationService mockHubNotificationService = Mockito.mock(HubNotificationService.class);
		for (int i = 0; i < 3; i++) {
			final ReleaseItem releaseItem = getReleaseItem(i);
			Mockito.when(mockHubNotificationService
					.getProjectReleaseItemFromProjectReleaseUrl(PROJECTVERSION_URL_PREFIX + i)).thenReturn(releaseItem);
			List<MetaLink> links = new ArrayList<>();
			MetaInformation meta = new MetaInformation(null, null, links);
			final ComponentVersion componentVersion = new ComponentVersion(meta);
			componentVersion.setVersionName(VERSION_NAME_PREFIX + i);
			Mockito.when(mockHubNotificationService.getComponentVersion(COMPONENT_VERSION_LINK_PREFIX + i))
			.thenReturn(componentVersion);

			links = new ArrayList<>();
			for (int j = 0; j < 3; j++) {
				links.add(new MetaLink(RULE_LINK_NAME, RULE_URL_PREFIX + j + suffix));

				final PolicyRule rule = new PolicyRule(null, RULE_NAME_PREFIX + j, "description", true, true, null,
						"createdAt", "createdBy", "updatedAt", "updatedBy");
				Mockito.when(mockHubNotificationService.getPolicyRule(RULE_URL_PREFIX + j)).thenReturn(rule);
			}
			meta = new MetaInformation(null, null, links);
			final BomComponentVersionPolicyStatus status = new BomComponentVersionPolicyStatus(meta);
			Mockito.when(
					mockHubNotificationService.getPolicyStatus(BOM_COMPONENT_VERSION_POLICY_STATUS_LINK_PREFIX + i))
			.thenReturn(status);
		}
		return mockHubNotificationService;
	}

	private ReleaseItem getReleaseItem(final int i) {
		final List<MetaLink> links = new ArrayList<>();
		final MetaLink link = new MetaLink("project", PROJECT_URL_PREFIX + i);
		links.add(link);
		final MetaInformation _meta = new MetaInformation(null, null, links);
		final ReleaseItem releaseItem = new ReleaseItem(TEST_PROJECT_VERSION_PREFIX + i, "testPhase",
				"testDistribution", "testSource", _meta);
		return releaseItem;
	}

}