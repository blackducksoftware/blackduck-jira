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
package com.blackducksoftware.integration.jira.task.conversion;

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

import com.blackducksoftware.integration.hub.api.policy.PolicyRule;
import com.blackducksoftware.integration.hub.api.project.ProjectVersion;
import com.blackducksoftware.integration.hub.dataservices.notification.items.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservices.notification.items.PolicyViolationContentItem;
import com.blackducksoftware.integration.hub.exception.NotificationServiceException;
import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.jira.common.HubProject;
import com.blackducksoftware.integration.jira.common.HubProjectMapping;
import com.blackducksoftware.integration.jira.common.HubProjectMappings;
import com.blackducksoftware.integration.jira.common.JiraContext;
import com.blackducksoftware.integration.jira.common.JiraProject;
import com.blackducksoftware.integration.jira.common.exception.ConfigurationException;
import com.blackducksoftware.integration.jira.mocks.ApplicationUserMock;
import com.blackducksoftware.integration.jira.mocks.PluginSettingsMock;
import com.blackducksoftware.integration.jira.mocks.ProjectManagerMock;
import com.blackducksoftware.integration.jira.task.JiraSettingsService;
import com.blackducksoftware.integration.jira.task.conversion.output.HubEvent;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;

public class PolicyViolationNotificationConverterTest {

	private static final String TEST_PROJECT_VERSION_PREFIX = "testVersionName";
	private static final String HUB_COMPONENT_NAME_PREFIX = "test Hub Component";
	private static final String HUB_PROJECT_NAME_PREFIX = "test Hub Project";
	private static final String PROJECT_URL_PREFIX = "http://test.project.url";
	private static final String VERSION_NAME_PREFIX = "versionName";
	private static final String PROJECTVERSION_URL_PREFIX = "http://test.projectversion.url";

	private static List<PolicyRule> rules;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		rules = new ArrayList<>();
		final PolicyRule includedRule1 = new PolicyRule(null, "ruleUrl0", null, null, null, null, null, null, null,
				null);
		final PolicyRule includedRule2 = new PolicyRule(null, "ruleUrl", null, null, null, null, null, null, null,
				null);
		final PolicyRule includedRule3 = new PolicyRule(null, "ruleUrl99", null, null, null, null, null, null, null,
				null);
		rules.add(includedRule1);
		rules.add(includedRule2);
		rules.add(includedRule3);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testWithRuleListWithMatches()
			throws NotificationServiceException, UnexpectedHubResponseException, ConfigurationException {
		final List<HubEvent> events = generateEvents(rules, true, true);

		assertEquals(3, events.size());

		assertTrue(events.get(0).getIssueSummary().contains(HUB_PROJECT_NAME_PREFIX));
		assertTrue(events.get(0).getIssueSummary().contains(TEST_PROJECT_VERSION_PREFIX));
		assertTrue(events.get(0).getIssueSummary().contains(HUB_COMPONENT_NAME_PREFIX));
		assertTrue(events.get(0).getIssueSummary().contains(VERSION_NAME_PREFIX));
	}

	@Test
	public void testNoProjectMappingMatch()
			throws NotificationServiceException, UnexpectedHubResponseException, ConfigurationException {
		final List<HubEvent> events = generateEvents(rules, true, false);
		assertEquals(0, events.size());
	}

	@Test
	public void testWithoutMappings()
			throws NotificationServiceException, UnexpectedHubResponseException, ConfigurationException {
		final List<HubEvent> events = generateEvents(rules, false, false);

		assertEquals(0, events.size());
	}

	private List<HubEvent> generateEvents(final List<PolicyRule> rules, final boolean includeProjectMappings,
			final boolean projectMappingMatch)
					throws NotificationServiceException, UnexpectedHubResponseException, ConfigurationException {

		final ApplicationUserMock jiraUser = new ApplicationUserMock();

		final JiraContext jiraContext = new JiraContext(jiraUser);

		final JiraServices jiraServices = ConverterTestUtils.mockJiraServices();

		final HubProjectMappings mappings = new HubProjectMappings(jiraServices,
				createProjectMappings(includeProjectMappings, projectMappingMatch));

		final NotificationContentItem notification = createNotification(rules);

		final JiraSettingsService jiraSettingsService = new JiraSettingsService(new PluginSettingsMock());

		final NotificationToEventConverter converter = new PolicyViolationNotificationConverter(mappings, jiraServices,
				jiraContext, jiraSettingsService);
		final List<HubEvent> events = converter.generateEvents(notification);

		return events;
	}

	private NotificationContentItem createNotification(final List<PolicyRule> policyRule) {

		final ProjectVersion projectVersion = new ProjectVersion();
		projectVersion.setProjectName(HUB_PROJECT_NAME_PREFIX);
		projectVersion.setProjectVersionLink(PROJECTVERSION_URL_PREFIX);
		projectVersion.setProjectVersionName(TEST_PROJECT_VERSION_PREFIX);

		final PolicyViolationContentItem notif = new PolicyViolationContentItem(new Date(), projectVersion,
				HUB_COMPONENT_NAME_PREFIX, VERSION_NAME_PREFIX, null, null, policyRule);
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
			hubProject.setProjectName(HUB_PROJECT_NAME_PREFIX + suffix);
			hubProject.setProjectUrl(PROJECT_URL_PREFIX);
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

}