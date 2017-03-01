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

        final JiraEventInfo jiraEventInfo1 = new JiraEventInfo();
        jiraEventInfo1.setAction(HubEventAction.ADD_COMMENT)
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

        assertEquals(HubEventAction.ADD_COMMENT, jiraEventInfo1.getAction());
        assertEquals("hubComponentName", jiraEventInfo1.getHubComponentName());
        assertEquals("hubComponentVersion", jiraEventInfo1.getHubComponentVersion());
        assertEquals("hubProjectName", jiraEventInfo1.getHubProjectName());
        assertEquals("hubProjectVersion", jiraEventInfo1.getHubProjectVersion());
        assertEquals("hubRuleName", jiraEventInfo1.getHubRuleName());
        assertEquals(jiraFieldCopyMappings, jiraEventInfo1.getJiraFieldCopyMappings());
        assertEquals("jiraIssueAssigneeUserId", jiraEventInfo1.getJiraIssueAssigneeUserId());
        assertEquals("jiraIssueComment", jiraEventInfo1.getJiraIssueComment());
        assertEquals("jiraIssueCommentForExistingIssue", jiraEventInfo1.getJiraIssueCommentForExistingIssue());
        assertEquals("jiraIssueCommentForExistingIssue", jiraEventInfo1.getJiraIssueCommentForExistingIssue());
        assertEquals("jiraIssueCommentInLieuOfStateChange", jiraEventInfo1.getJiraIssueCommentInLieuOfStateChange());
        assertEquals("jiraIssueDescription", jiraEventInfo1.getJiraIssueDescription());
        assertEquals(issuePropertiesGenerator, jiraEventInfo1.getJiraIssuePropertiesGenerator());
        assertEquals("jiraIssueReOpenComment", jiraEventInfo1.getJiraIssueReOpenComment());
        assertEquals("jiraIssueResolveComment", jiraEventInfo1.getJiraIssueResolveComment());
        assertEquals("jiraIssueSummary", jiraEventInfo1.getJiraIssueSummary());
        assertEquals("jiraIssueTypeId", jiraEventInfo1.getJiraIssueTypeId());
        assertEquals(Long.valueOf(123L), jiraEventInfo1.getJiraProjectId());
        assertEquals("jiraProjectName", jiraEventInfo1.getJiraProjectName());
        assertEquals("jiraUserKey", jiraEventInfo1.getJiraUserKey());
        assertEquals("jiraUserName", jiraEventInfo1.getJiraUserName());

    }
}
