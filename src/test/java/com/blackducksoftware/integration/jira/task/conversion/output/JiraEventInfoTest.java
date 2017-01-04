/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
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
