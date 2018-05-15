/**
 * Hub JIRA Plugin
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
 */
package com.blackducksoftware.integration.jira.task.conversion.output;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.integration.hub.notification.content.NotificationContentDetail;
import com.blackducksoftware.integration.hub.notification.content.VulnerabilityNotificationContent;
import com.blackducksoftware.integration.jira.common.exception.EventDataBuilderException;
import com.blackducksoftware.integration.jira.config.ProjectFieldCopyMapping;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventCategory;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventData;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventDataBuilder;

public class JiraEventInfoTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void testValidVulnerabilityEvent() throws EventDataBuilderException, URISyntaxException {
        final Set<ProjectFieldCopyMapping> jiraFieldCopyMappings = new HashSet<>();
        final IssuePropertiesGenerator issuePropertiesGenerator = createIssuePropertyGenerator();

        final EventDataBuilder eventDataBuilder = createEventDataBuilder(EventCategory.VULNERABILITY, jiraFieldCopyMappings, issuePropertiesGenerator);

        final EventData eventData = eventDataBuilder.build();

        checkCommonValues(jiraFieldCopyMappings, issuePropertiesGenerator, eventData);
        assertEquals(null, eventData.getHubRuleName());
        assertEquals(null, eventData.getHubRuleUrl());
    }

    @Test
    public void testInValidVulnerabilityEvent() throws EventDataBuilderException, URISyntaxException {
        final Set<ProjectFieldCopyMapping> jiraFieldCopyMappings = new HashSet<>();
        final IssuePropertiesGenerator issuePropertiesGenerator = createIssuePropertyGenerator();

        final EventDataBuilder eventDataBuilder = createEventDataBuilder(EventCategory.VULNERABILITY, jiraFieldCopyMappings, issuePropertiesGenerator);
        eventDataBuilder.setHubProjectVersionUrl(null);

        try {
            eventDataBuilder.build();
            fail("Expected exception");
        } catch (final EventDataBuilderException e) {
            // expected
        }
    }

    @Test
    public void testValidPolicyEvent() throws EventDataBuilderException, URISyntaxException {
        final Set<ProjectFieldCopyMapping> jiraFieldCopyMappings = new HashSet<>();
        final IssuePropertiesGenerator issuePropertiesGenerator = createIssuePropertyGenerator();

        final EventDataBuilder eventDataBuilder = createEventDataBuilder(EventCategory.POLICY, jiraFieldCopyMappings, issuePropertiesGenerator);
        eventDataBuilder.setHubRuleName("hubRuleName");
        eventDataBuilder.setHubRuleUrl("hubRuleUrl");

        final EventData eventData = eventDataBuilder.build();

        checkCommonValues(jiraFieldCopyMappings, issuePropertiesGenerator, eventData);
        assertEquals("hubRuleName", eventData.getHubRuleName());
        assertEquals("hubRuleUrl", eventData.getHubRuleUrl());
    }

    @Test
    public void testInValidPolicyEventMissingRuleUrl() throws EventDataBuilderException, URISyntaxException {
        final Set<ProjectFieldCopyMapping> jiraFieldCopyMappings = new HashSet<>();
        final IssuePropertiesGenerator issuePropertiesGenerator = createIssuePropertyGenerator();

        final EventDataBuilder eventDataBuilder = createEventDataBuilder(EventCategory.POLICY, jiraFieldCopyMappings, issuePropertiesGenerator);
        eventDataBuilder.setHubRuleName("hubRuleName");

        try {
            eventDataBuilder.build();
            fail("Expected exception");
        } catch (final EventDataBuilderException e) {
            // expected
        }
    }

    @Test
    public void testInValidPolicyEventMissingRuleName() throws EventDataBuilderException, URISyntaxException {
        final Set<ProjectFieldCopyMapping> jiraFieldCopyMappings = new HashSet<>();
        final IssuePropertiesGenerator issuePropertiesGenerator = createIssuePropertyGenerator();

        final EventDataBuilder eventDataBuilder = createEventDataBuilder(EventCategory.POLICY, jiraFieldCopyMappings, issuePropertiesGenerator);
        eventDataBuilder.setHubRuleUrl("hubRuleUrl");

        try {
            eventDataBuilder.build();
            fail("Expected exception");
        } catch (final EventDataBuilderException e) {
            // expected
        }
    }

    private void checkCommonValues(final Set<ProjectFieldCopyMapping> jiraFieldCopyMappings, final IssuePropertiesGenerator issuePropertiesGenerator,
            final EventData eventData) {
        assertEquals(HubEventAction.ADD_COMMENT, eventData.getAction());
        assertEquals("hubComponentName", eventData.getHubComponentName());
        assertEquals("hubComponentUrl", eventData.getHubComponentUrl());
        assertEquals("hubComponentVersion", eventData.getHubComponentVersion());
        assertEquals("hubComponentVersionUrl", eventData.getHubComponentVersionUrl());

        assertEquals("hubComponentUsage", eventData.getHubComponentUsage());
        assertEquals("hubComponentOrigin", eventData.getHubComponentOrigin());
        assertEquals("hubComponentOriginId", eventData.getHubComponentOriginId());

        assertEquals("hubProjectVersionUrl", eventData.getHubProjectVersionUrl());
        assertEquals("hubProjectVersionNickname", eventData.getHubProjectVersionNickname());
        assertEquals("hubLicenseNames", eventData.getHubLicenseNames());

        assertEquals("hubProjectName", eventData.getHubProjectName());
        assertEquals("hubProjectVersion", eventData.getHubProjectVersion());

        assertEquals(jiraFieldCopyMappings, eventData.getJiraFieldCopyMappings());
        assertEquals("jiraIssueAssigneeUserId", eventData.getJiraIssueAssigneeUserId());
        assertEquals("jiraIssueComment", eventData.getJiraIssueComment());
        assertEquals("jiraIssueCommentForExistingIssue", eventData.getJiraIssueCommentForExistingIssue());
        assertEquals("jiraIssueCommentForExistingIssue", eventData.getJiraIssueCommentForExistingIssue());
        assertEquals("jiraIssueCommentInLieuOfStateChange", eventData.getJiraIssueCommentInLieuOfStateChange());
        assertEquals("jiraIssueDescription", eventData.getJiraIssueDescription());
        assertEquals(issuePropertiesGenerator, eventData.getJiraIssuePropertiesGenerator());
        assertEquals("jiraIssueReOpenComment", eventData.getJiraIssueReOpenComment());
        assertEquals("jiraIssueResolveComment", eventData.getJiraIssueResolveComment());
        assertEquals("jiraIssueSummary", eventData.getJiraIssueSummary());
        assertEquals("jiraIssueTypeId", eventData.getJiraIssueTypeId());
        assertEquals(Long.valueOf(123L), eventData.getJiraProjectId());
        assertEquals("jiraProjectName", eventData.getJiraProjectName());
        assertEquals("jiraAdminUserKey", eventData.getJiraAdminUserKey());
        assertEquals("jiraAdminUserName", eventData.getJiraAdminUsername());
        assertEquals("jiraIssueCreatorUserKey", eventData.getJiraIssueCreatorUserKey());
        assertEquals("jiraIssueCreatorUserName", eventData.getJiraIssueCreatorUsername());
    }

    private EventDataBuilder createEventDataBuilder(final EventCategory eventCategory, final Set<ProjectFieldCopyMapping> jiraFieldCopyMappings,
            final IssuePropertiesGenerator issuePropertiesGenerator) {
        final EventDataBuilder eventDataBuilder = new EventDataBuilder(eventCategory);
        eventDataBuilder.setAction(HubEventAction.ADD_COMMENT)
                .setHubComponentName("hubComponentName")
                .setHubComponentUrl("hubComponentUrl")
                .setHubComponentVersion("hubComponentVersion")
                .setHubComponentVersionUrl("hubComponentVersionUrl")
                .setHubComponentUsage("hubComponentUsage")
                .setHubComponentOrigin("hubComponentOrigin")
                .setHubComponentOriginId("hubComponentOriginId")
                .setHubProjectName("hubProjectName")
                .setHubProjectVersion("hubProjectVersion")
                .setHubProjectVersionUrl("hubProjectVersionUrl")
                .setHubProjectVersionNickname("hubProjectVersionNickname")
                .setHubLicenseNames("hubLicenseNames")
                .setJiraFieldCopyMappings(jiraFieldCopyMappings)
                .setJiraIssueAssigneeUserId("jiraIssueAssigneeUserId")
                .setJiraIssueComment("jiraIssueComment")
                .setJiraIssueCommentForExistingIssue("jiraIssueCommentForExistingIssue")
                .setJiraIssueCommentForExistingIssue("jiraIssueCommentForExistingIssue")
                .setJiraIssueCommentInLieuOfStateChange("jiraIssueCommentInLieuOfStateChange")
                .setJiraIssueDescription("jiraIssueDescription")
                .setJiraIssuePropertiesGenerator(issuePropertiesGenerator)
                .setJiraIssueReOpenComment("jiraIssueReOpenComment")
                .setJiraIssueResolveComment("jiraIssueResolveComment")
                .setJiraIssueSummary("jiraIssueSummary")
                .setJiraIssueTypeId("jiraIssueTypeId")
                .setJiraProjectId(123L)
                .setJiraProjectName("jiraProjectName")
                .setJiraAdminUserKey("jiraAdminUserKey")
                .setJiraAdminUserName("jiraAdminUserName")
                .setJiraIssueCreatorUserKey("jiraIssueCreatorUserKey")
                .setJiraIssueCreatorUserName("jiraIssueCreatorUserName");
        ;
        return eventDataBuilder;
    }

    private IssuePropertiesGenerator createIssuePropertyGenerator() throws URISyntaxException {
        final VulnerabilityNotificationContent content = new VulnerabilityNotificationContent();
        final NotificationContentDetail detail = NotificationContentDetail.createVulnerabilityDetail(content, "projectName", "projectVersionName", "projectVersionUri", "componentName", "componentVersionName", "componentVersionUri",
                "componentVersionOriginName", "componentIssueUri", "componentVersionOriginId");
        final IssuePropertiesGenerator issuePropertiesGenerator = new VulnerabilityIssuePropertiesGenerator(detail);
        return issuePropertiesGenerator;
    }
}
