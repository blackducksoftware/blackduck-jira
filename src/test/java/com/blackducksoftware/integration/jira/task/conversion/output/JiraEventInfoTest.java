/**
 * Hub JIRA Plugin
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
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

import java.util.HashSet;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.integration.jira.config.ProjectFieldCopyMapping;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventData;

public class JiraEventInfoTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void test() {
        final Set<ProjectFieldCopyMapping> jiraFieldCopyMappings = new HashSet<>();
        final IssuePropertiesGenerator issuePropertiesGenerator = null;

        final EventData eventData = new EventData();
        eventData.setAction(HubEventAction.ADD_COMMENT)
                .setHubComponentName("hubComponentName")
                .setHubComponentVersion("hubComponentVersion")
                .setHubProjectName("hubProjectName")
                .setHubProjectVersion("hubProjectVersion")
                .setHubRuleName("hubRuleName")
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
                .setJiraUserKey("jiraUserKey")
                .setJiraUserName("jiraUserName");

        assertEquals(HubEventAction.ADD_COMMENT, eventData.getAction());
        assertEquals("hubComponentName", eventData.getHubComponentName());
        assertEquals("hubComponentVersion", eventData.getHubComponentVersion());
        assertEquals("hubProjectName", eventData.getHubProjectName());
        assertEquals("hubProjectVersion", eventData.getHubProjectVersion());
        assertEquals("hubRuleName", eventData.getHubRuleName());
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
        assertEquals("jiraUserKey", eventData.getJiraUserKey());
        assertEquals("jiraUserName", eventData.getJiraUserName());

    }
}
