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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.atlassian.jira.project.ProjectManager;
import com.blackducksoftware.integration.hub.component.api.BomComponentVersionPolicyStatus;
import com.blackducksoftware.integration.hub.component.api.ComponentVersion;
import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.hub.meta.MetaInformation;
import com.blackducksoftware.integration.hub.meta.MetaLink;
import com.blackducksoftware.integration.hub.notification.NotificationService;
import com.blackducksoftware.integration.hub.notification.NotificationServiceException;
import com.blackducksoftware.integration.hub.notification.api.NotificationItem;
import com.blackducksoftware.integration.hub.notification.api.NotificationType;
import com.blackducksoftware.integration.hub.notification.api.VulnerabilityNotificationContent;
import com.blackducksoftware.integration.hub.notification.api.VulnerabilityNotificationItem;
import com.blackducksoftware.integration.hub.notification.api.VulnerabilitySourceQualifiedId;
import com.blackducksoftware.integration.hub.policy.api.PolicyRule;
import com.blackducksoftware.integration.hub.project.api.ProjectVersion;
import com.blackducksoftware.integration.hub.version.api.ReleaseItem;
import com.blackducksoftware.integration.jira.common.HubProject;
import com.blackducksoftware.integration.jira.common.HubProjectMapping;
import com.blackducksoftware.integration.jira.common.HubProjectMappings;
import com.blackducksoftware.integration.jira.common.JiraContext;
import com.blackducksoftware.integration.jira.common.JiraProject;
import com.blackducksoftware.integration.jira.mocks.ApplicationUserMock;
import com.blackducksoftware.integration.jira.mocks.ProjectManagerMock;
import com.blackducksoftware.integration.jira.task.conversion.NotificationToEventConverter;
import com.blackducksoftware.integration.jira.task.conversion.VulnerabilityNotificationConverter;
import com.blackducksoftware.integration.jira.task.conversion.output.HubEvent;
import com.blackducksoftware.integration.jira.task.conversion.output.HubEventAction;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;

public class PolicyOverrideNotificationConverterTest {

	private static final String SAMPLE_VULN = "CVE-2016-0006";
	private static final String BOM_COMPONENT_VERSION_POLICY_STATUS_LINK = "bomComponentVersionPolicyStatusLink";
	private static final String TEST_PROJECT_VERSION = "testVersionName";
	private static final String HUB_COMPONENT_NAME = "test Hub Component";
	private static final String HUB_PROJECT_NAME = "test Hub Project";
	private static final String NOTIF_URL = "http://test.notif.url";
	private static final String PROJECT_URL = "http://test.project.url";
	private static final String JIRA_ISSUE_TYPE = "Task";
	private static final String BOM_COMPONENT_VERSION_POLICY_STATUS_LINK_PREFIX = BOM_COMPONENT_VERSION_POLICY_STATUS_LINK;
	private static final String COMPONENT_VERSION_LINK = "http://eng-hub-valid03.dc1.lan/api/components/0934ea45-c739-4b58-bcb1-ee777022ce4f/versions/7c45d411-92ca-45b0-80fc-76b765b954ef";
	private static final String COMPONENT_VERSION_NAME = "versionName";
	private static final String PROJECTVERSION_URL = "http://eng-hub-valid03.dc1.lan/api/projects/a3b48f57-9c00-453f-8672-804e08c317f2/versions/7d4fdbed-936b-468f-af7f-826dfc072c5b";
	private static final String RULE_URL_PREFIX = "ruleUrl";
	private static final String RULE_NAME = "ruleName";
	private static final String RULE_URL = "http://eng-hub-valid03.dc1.lan/api/policy-rules/138d0d0f-45b5-4e51-8a32-42ed8946434c";
	private static final String RULE_LINK_NAME = "policy-rule";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void test() throws NotificationServiceException, UnexpectedHubResponseException {
		final List<HubEvent> events = generateEvents(true, true);

		assertEquals(1, events.size());

		System.out.println(events.get(0));
		assertTrue(events.get(0).getIssueSummary().contains(HUB_PROJECT_NAME));
		assertTrue(events.get(0).getIssueSummary()
				.contains(TEST_PROJECT_VERSION));
		assertTrue(events.get(0).getIssueSummary()
				.contains(HUB_COMPONENT_NAME));
		assertTrue(events.get(0).getIssueSummary().contains(COMPONENT_VERSION_NAME));

		assertTrue(events.get(0).getIssueSummary().contains(HUB_PROJECT_NAME));
		assertTrue(events.get(0).getIssueSummary()
				.contains(TEST_PROJECT_VERSION));
		assertTrue(events.get(0).getIssueSummary()
				.contains(HUB_COMPONENT_NAME));
		assertTrue(events.get(0).getIssueSummary().contains(COMPONENT_VERSION_NAME));

		assertEquals(HubEventAction.ADD_COMMENT, events.get(0).getIfExistsAction());
		System.out.println(events.get(0).getComment());
		assertTrue(events.get(0).getComment().contains(SAMPLE_VULN));
	}


	@Test
	public void testNoProjectMappingMatch() throws NotificationServiceException, UnexpectedHubResponseException {
		final List<HubEvent> events = generateEvents(true, false);
		assertEquals(0, events.size());
	}

	@Test
	public void testWithoutMappings() throws NotificationServiceException, UnexpectedHubResponseException {
		final List<HubEvent> events = generateEvents(false, false);

		assertEquals(0, events.size());
	}

	private List<HubEvent> generateEvents(final boolean includeProjectMappings,
			final boolean projectMappingMatch) throws NotificationServiceException, UnexpectedHubResponseException {
		final NotificationService mockHubNotificationService = createMockHubNotificationService(true);
		final ProjectManager jiraProjectManager = createJiraProjectManager();
		final ApplicationUserMock jiraUser = new ApplicationUserMock();

		final JiraContext ticketGenInfo = new JiraContext(jiraUser, JIRA_ISSUE_TYPE);

		final JiraServices jiraServices = Mockito.mock(JiraServices.class);
		Mockito.when(jiraServices.getJiraProjectManager()).thenReturn(jiraProjectManager);
		final HubProjectMappings mappings = new HubProjectMappings(jiraServices, ticketGenInfo, createProjectMappings(
				includeProjectMappings, projectMappingMatch));

		final NotificationItem notification = createNotification();

		final NotificationToEventConverter converter = new VulnerabilityNotificationConverter(mappings, jiraServices,
				ticketGenInfo, mockHubNotificationService);
		final List<HubEvent> events = converter.generateEvents(notification);
		return events;
	}

	private NotificationItem createNotification() {

		final MetaInformation meta = new MetaInformation(null, NOTIF_URL, null);
		final VulnerabilityNotificationItem notif = new VulnerabilityNotificationItem(meta);
		notif.setCreatedAt(new Date());
		notif.setType(NotificationType.POLICY_OVERRIDE);
		final VulnerabilityNotificationContent content = new VulnerabilityNotificationContent();

		content.setComponentName(HUB_COMPONENT_NAME);
		content.setVersionName(COMPONENT_VERSION_NAME);
		content.setComponentVersionLink(COMPONENT_VERSION_LINK);

		final List<VulnerabilitySourceQualifiedId> deletedVulnerabilityIds = new ArrayList<>();
		deletedVulnerabilityIds.add(new VulnerabilitySourceQualifiedId("NVD", "CVE-2016-0001"));
		deletedVulnerabilityIds.add(new VulnerabilitySourceQualifiedId("NVD", "CVE-2016-0002"));
		content.setDeletedVulnerabilityIds(deletedVulnerabilityIds);

		final List<VulnerabilitySourceQualifiedId> newVulnerabilityIds = new ArrayList<>();
		newVulnerabilityIds.add(new VulnerabilitySourceQualifiedId("NVD", "CVE-2016-0003"));
		newVulnerabilityIds.add(new VulnerabilitySourceQualifiedId("NVD", "CVE-2016-0004"));
		content.setNewVulnerabilityIds(newVulnerabilityIds);

		final List<VulnerabilitySourceQualifiedId> updatedVulnerabilityIds = new ArrayList<>();
		updatedVulnerabilityIds.add(new VulnerabilitySourceQualifiedId("NVD", "CVE-2016-0005"));
		updatedVulnerabilityIds.add(new VulnerabilitySourceQualifiedId("NVD", SAMPLE_VULN));
		content.setUpdatedVulnerabilityIds(updatedVulnerabilityIds);

		final List<ProjectVersion> affectedProjectVersions = new ArrayList<>();
		final ProjectVersion projectVersion = new ProjectVersion();
		projectVersion.setProjectName(HUB_PROJECT_NAME);
		projectVersion.setProjectVersionLink(PROJECTVERSION_URL);
		projectVersion.setProjectVersionName(TEST_PROJECT_VERSION);
		affectedProjectVersions.add(projectVersion);
		content.setAffectedProjectVersions(affectedProjectVersions);

		notif.setContent(content);
		System.out.println("Notif: " + notif);

		return notif;
	}

	private Set<HubProjectMapping> createProjectMappings(final boolean includeMapping, final boolean includeMatch) {

		final Set<HubProjectMapping> mappings = new HashSet<>();

		if (includeMapping) {
			String suffix;
			if (includeMatch) {
				suffix = "";
			} else {
				suffix = "XX";
			}

			final HubProjectMapping mapping = new HubProjectMapping();
			final HubProject hubProject = new HubProject();
			hubProject.setProjectName(HUB_PROJECT_NAME);
			hubProject.setProjectUrl(PROJECT_URL + suffix);
			mapping.setHubProject(hubProject);
			final JiraProject jiraProject = new JiraProject();
			jiraProject.setProjectId(ProjectManagerMock.JIRA_PROJECT_ID_BASE);
			jiraProject.setProjectName(ProjectManagerMock.JIRA_PROJECT_PREFIX);
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

	private NotificationService createMockHubNotificationService(final boolean ruleMatches)
			throws NotificationServiceException, UnexpectedHubResponseException {
		String suffix;
		if (ruleMatches) {
			suffix = "";
		} else {
			suffix = "XX";
		}
		final NotificationService mockHubNotificationService = Mockito.mock(NotificationService.class);
		final ReleaseItem releaseItem = getReleaseItem();
		Mockito.when(mockHubNotificationService.getProjectUrlFromProjectVersionUrl(PROJECTVERSION_URL)).thenReturn(
				PROJECT_URL);
		Mockito.when(mockHubNotificationService
				.getProjectReleaseItemFromProjectReleaseUrl(PROJECTVERSION_URL))
				.thenReturn(releaseItem);
		List<MetaLink> links = new ArrayList<>();
		MetaInformation meta = new MetaInformation(null, COMPONENT_VERSION_LINK, links);
		final ComponentVersion componentVersion = new ComponentVersion(meta);
		componentVersion.setVersionName(COMPONENT_VERSION_NAME);
		Mockito.when(mockHubNotificationService.getComponentVersion(COMPONENT_VERSION_LINK))
		.thenReturn(componentVersion);

		links = new ArrayList<>();

		links.add(new MetaLink(RULE_LINK_NAME, RULE_URL_PREFIX + suffix));

		meta = new MetaInformation(null, RULE_URL, null);
		final PolicyRule rule = new PolicyRule(meta, RULE_NAME, "description", true, true, null,
				"createdAt", "createdBy", "updatedAt", "updatedBy");
		Mockito.when(mockHubNotificationService.getPolicyRule(RULE_URL_PREFIX)).thenReturn(rule);

		meta = new MetaInformation(null, null, links);
		final BomComponentVersionPolicyStatus status = new BomComponentVersionPolicyStatus(meta);
		Mockito.when(
				mockHubNotificationService.getPolicyStatus(BOM_COMPONENT_VERSION_POLICY_STATUS_LINK_PREFIX))
				.thenReturn(status);

		return mockHubNotificationService;
	}

	private ReleaseItem getReleaseItem() {
		final List<MetaLink> links = new ArrayList<>();
		final MetaLink link = new MetaLink("project", PROJECT_URL);
		links.add(link);
		final MetaInformation _meta = new MetaInformation(null, PROJECTVERSION_URL, links);
		final ReleaseItem releaseItem = new ReleaseItem(TEST_PROJECT_VERSION, "testPhase",
				"testDistribution", "testSource", _meta);
		return releaseItem;
	}

}