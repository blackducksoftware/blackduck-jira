/**
 * Black Duck JIRA Plugin
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

import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;
import com.blackducksoftware.integration.jira.common.exception.EventDataBuilderException;
import com.blackducksoftware.integration.jira.config.model.ProjectFieldCopyMapping;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventData;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventDataBuilder;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.IssueCategory;

public class JiraEventInfoTest {
    private static final String COMPONENT_URL = "https://blackDuckComponentUrl";
    private static final String COMPONENT_VERSION_URL = "https://blackDuckComponentVersionUrl";
    private static final String PROJECT_VERSION_URL = "https://blackDuckProjectVersionUrl";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    private static final String EXPECTED_ISSUE_SUMMARY(final EventData eventData) {
        String issueType = BlackDuckJiraConstants.BLACKDUCK_VULNERABILITY_ISSUE;
        String suffix = "";
        if (IssueCategory.POLICY.equals(eventData.getCategory())) {
            issueType = BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_ISSUE;
            suffix = String.format(" [Rule: '%s']", eventData.getBlackDuckRuleName());
        }
        final String issueSummary = String.format("%s: Project '%s' / '%s', Component '%s' / '%s'",
            issueType, eventData.getBlackDuckProjectName(), eventData.getBlackDuckProjectVersionName(), eventData.getBlackDuckComponentName(), eventData.getBlackDuckComponentVersionName());
        return issueSummary + suffix;
    }

    @Test
    public void testValidVulnerabilityEvent() throws EventDataBuilderException, URISyntaxException {
        final Set<ProjectFieldCopyMapping> jiraFieldCopyMappings = new HashSet<>();

        final EventDataBuilder eventDataBuilder = createEventDataBuilder(IssueCategory.VULNERABILITY, jiraFieldCopyMappings);

        final EventData eventData = eventDataBuilder.build();

        checkCommonValues(jiraFieldCopyMappings, eventData);
        assertEquals(null, eventData.getBlackDuckRuleName());
        assertEquals(null, eventData.getBlackDuckRuleUrl());
    }

    @Test
    public void testInValidVulnerabilityEvent() throws EventDataBuilderException, URISyntaxException {
        final Set<ProjectFieldCopyMapping> jiraFieldCopyMappings = new HashSet<>();

        final EventDataBuilder eventDataBuilder = createEventDataBuilder(IssueCategory.VULNERABILITY, jiraFieldCopyMappings);
        eventDataBuilder.setBlackDuckProjectVersionUrl(null);

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

        final String bdRuleName = "blackDuckRuleName";
        final String bdRuleUrl = "https://bdRuleUrl:443";

        final EventDataBuilder eventDataBuilder = createEventDataBuilder(IssueCategory.POLICY, jiraFieldCopyMappings);
        eventDataBuilder.setBlackDuckRuleName(bdRuleName);
        eventDataBuilder.setBlackDuckRuleUrl(bdRuleUrl);

        final EventData eventData = eventDataBuilder.build();

        checkCommonValues(jiraFieldCopyMappings, eventData);
        assertEquals(bdRuleName, eventData.getBlackDuckRuleName());
        assertEquals(bdRuleUrl, eventData.getBlackDuckRuleUrl());
    }

    @Test
    public void testInValidPolicyEventMissingRuleUrl() throws EventDataBuilderException, URISyntaxException {
        final Set<ProjectFieldCopyMapping> jiraFieldCopyMappings = new HashSet<>();

        final EventDataBuilder eventDataBuilder = createEventDataBuilder(IssueCategory.POLICY, jiraFieldCopyMappings);
        eventDataBuilder.setBlackDuckRuleName("blackDuckRuleName");

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

        final EventDataBuilder eventDataBuilder = createEventDataBuilder(IssueCategory.POLICY, jiraFieldCopyMappings);
        eventDataBuilder.setBlackDuckRuleUrl("https://blackDuckRuleUrl");

        try {
            eventDataBuilder.build();
            fail("Expected exception");
        } catch (final EventDataBuilderException e) {
            // expected
        }
    }

    private void checkCommonValues(final Set<ProjectFieldCopyMapping> jiraFieldCopyMappings, final EventData eventData) {
        assertEquals(BlackDuckEventAction.ADD_COMMENT, eventData.getAction());
        assertEquals("blackDuckComponentName", eventData.getBlackDuckComponentName());
        assertEquals(COMPONENT_URL, eventData.getBlackDuckComponentUrl());
        assertEquals("blackDuckComponentVersion", eventData.getBlackDuckComponentVersionName());
        assertEquals(COMPONENT_VERSION_URL, eventData.getBlackDuckComponentVersionUrl());

        assertEquals("blackDuckComponentUsage", eventData.getBlackDuckComponentUsage());
        assertEquals("blackDuckComponentOrigin", eventData.getBlackDuckComponentOrigin());
        assertEquals("blackDuckComponentOriginId", eventData.getBlackDuckComponentOriginId());

        assertEquals(PROJECT_VERSION_URL, eventData.getBlackDuckProjectVersionUrl());
        assertEquals("blackDuckProjectVersionNickname", eventData.getBlackDuckProjectVersionNickname());
        assertEquals("blackDuckLicenseNames", eventData.getBlackDuckLicenseNames());

        assertEquals("blackDuckProjectName", eventData.getBlackDuckProjectName());
        assertEquals("blackDuckProjectVersion", eventData.getBlackDuckProjectVersionName());

        assertEquals(jiraFieldCopyMappings, eventData.getJiraFieldCopyMappings());
        assertEquals("jiraIssueAssigneeUserId", eventData.getJiraIssueAssigneeUserId());
        assertEquals("jiraIssueComment", eventData.getJiraIssueComment());
        assertEquals("jiraIssueCommentForExistingIssue", eventData.getJiraIssueCommentForExistingIssue());
        assertEquals("jiraIssueCommentForExistingIssue", eventData.getJiraIssueCommentForExistingIssue());
        assertEquals("jiraIssueCommentInLieuOfStateChange", eventData.getJiraIssueCommentInLieuOfStateChange());
        assertEquals("jiraIssueDescription", eventData.getJiraIssueDescription());
        assertEquals("jiraIssueReOpenComment", eventData.getJiraIssueReOpenComment());
        assertEquals("jiraIssueResolveComment", eventData.getJiraIssueResolveComment());
        assertEquals(EXPECTED_ISSUE_SUMMARY(eventData), eventData.getJiraIssueSummary());
        assertEquals("jiraIssueTypeId", eventData.getJiraIssueTypeId());
        assertEquals(Long.valueOf(123L), eventData.getJiraProjectId());
        assertEquals("jiraProjectName", eventData.getJiraProjectName());
        assertEquals("jiraAdminUserKey", eventData.getJiraAdminUserKey());
        assertEquals("jiraAdminUserName", eventData.getJiraAdminUsername());
        assertEquals("jiraIssueCreatorUserKey", eventData.getJiraIssueCreatorUserKey());
        assertEquals("jiraIssueCreatorUserName", eventData.getJiraIssueCreatorUsername());
    }

    private EventDataBuilder createEventDataBuilder(final IssueCategory issueCategory, final Set<ProjectFieldCopyMapping> jiraFieldCopyMappings) {
        final EventDataBuilder eventDataBuilder = new EventDataBuilder(issueCategory);
        eventDataBuilder.setAction(BlackDuckEventAction.ADD_COMMENT)
            .setBlackDuckComponentName("blackDuckComponentName")
            .setBlackDuckComponentUrl(COMPONENT_URL)
            .setBlackDuckComponentVersionName("blackDuckComponentVersion")
            .setBlackDuckComponentVersionUrl(COMPONENT_VERSION_URL)
            .setBlackDuckComponentUsages("blackDuckComponentUsage")
            .setBlackDuckComponentOrigins("blackDuckComponentOrigin")
            .setBlackDuckComponentOriginId("blackDuckComponentOriginId")
            .setBlackDuckProjectName("blackDuckProjectName")
            .setBlackDuckProjectVersionName("blackDuckProjectVersion")
            .setBlackDuckProjectVersionUrl(PROJECT_VERSION_URL)
            .setBlackDuckProjectVersionNickname("blackDuckProjectVersionNickname")
            .setBlackDuckLicenseNames("blackDuckLicenseNames")
            .setJiraFieldCopyMappings(jiraFieldCopyMappings)
            .setJiraIssueAssigneeUserId("jiraIssueAssigneeUserId")
            .setJiraIssueComment("jiraIssueComment")
            .setJiraIssueCommentForExistingIssue("jiraIssueCommentForExistingIssue")
            .setJiraIssueCommentForExistingIssue("jiraIssueCommentForExistingIssue")
            .setJiraIssueCommentInLieuOfStateChange("jiraIssueCommentInLieuOfStateChange")
            .setJiraIssueDescription("jiraIssueDescription")
            .setJiraIssueReOpenComment("jiraIssueReOpenComment")
            .setJiraIssueResolveComment("jiraIssueResolveComment")
            .setJiraIssueTypeId("jiraIssueTypeId")
            .setJiraProjectId(123L)
            .setJiraProjectName("jiraProjectName")
            .setJiraAdminUserKey("jiraAdminUserKey")
            .setJiraAdminUserName("jiraAdminUserName")
            .setJiraIssueCreatorUserKey("jiraIssueCreatorUserKey")
            .setJiraIssueCreatorUserName("jiraIssueCreatorUserName");
        return eventDataBuilder;
    }

}
