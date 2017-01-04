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
import java.util.Map;
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

        final Map<String, Object> dataSet = jiraEventInfo1.getDataSet();

        final JiraEventInfo jiraEventInfo2 = new JiraEventInfo(dataSet);

        assertEquals(HubEventAction.ADD_COMMENT, jiraEventInfo2.getAction());
        assertEquals("hubComponentName", jiraEventInfo2.getHubComponentName());
        assertEquals("hubComponentVersion", jiraEventInfo2.getHubComponentVersion());
        assertEquals("hubProjectName", jiraEventInfo2.getHubProjectName());
        assertEquals("hubProjectVersion", jiraEventInfo2.getHubProjectVersion());
        assertEquals("hubRuleName", jiraEventInfo2.getHubRuleName());
        assertEquals(jiraFieldCopyMappings, jiraEventInfo2.getJiraFieldCopyMappings());
        assertEquals("jiraIssueAssigneeUserId", jiraEventInfo2.getJiraIssueAssigneeUserId());
        assertEquals("jiraIssueComment", jiraEventInfo2.getJiraIssueComment());
        assertEquals("jiraIssueCommentForExistingIssue", jiraEventInfo2.getJiraIssueCommentForExistingIssue());
        assertEquals("jiraIssueCommentForExistingIssue", jiraEventInfo2.getJiraIssueCommentForExistingIssue());
        assertEquals("jiraIssueCommentInLieuOfStateChange", jiraEventInfo2.getJiraIssueCommentInLieuOfStateChange());
        assertEquals("jiraIssueDescription", jiraEventInfo2.getJiraIssueDescription());
        assertEquals(issuePropertiesGenerator, jiraEventInfo2.getJiraIssuePropertiesGenerator());
        assertEquals("jiraIssueReOpenComment", jiraEventInfo2.getJiraIssueReOpenComment());
        assertEquals("jiraIssueResolveComment", jiraEventInfo2.getJiraIssueResolveComment());
        assertEquals("jiraIssueSummary", jiraEventInfo2.getJiraIssueSummary());
        assertEquals("jiraIssueTypeId", jiraEventInfo2.getJiraIssueTypeId());
        assertEquals(Long.valueOf(123L), jiraEventInfo2.getJiraProjectId());
        assertEquals("jiraProjectName", jiraEventInfo2.getJiraProjectName());
        assertEquals("jiraUserKey", jiraEventInfo2.getJiraUserKey());
        assertEquals("jiraUserName", jiraEventInfo2.getJiraUserName());

    }
}
