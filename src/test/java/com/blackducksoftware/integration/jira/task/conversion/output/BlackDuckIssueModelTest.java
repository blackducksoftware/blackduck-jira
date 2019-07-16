/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2019 Synopsys, Inc.
 * https://www.synopsys.com/
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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;
import com.blackducksoftware.integration.jira.config.model.ProjectFieldCopyMapping;
import com.blackducksoftware.integration.jira.mocks.ApplicationUserMock;
import com.blackducksoftware.integration.jira.task.issue.model.BlackDuckIssueFieldTemplate;
import com.blackducksoftware.integration.jira.task.issue.model.BlackDuckIssueModel;
import com.blackducksoftware.integration.jira.task.issue.model.IssueCategory;
import com.blackducksoftware.integration.jira.task.issue.model.JiraIssueFieldTemplate;

public class BlackDuckIssueModelTest {
    private static final Date DATE_INSTANCE = new Date();
    private static final String COMPONENT_URL = "https://blackDuckComponentUrl";
    private static final String COMPONENT_VERSION_URL = "https://blackDuckComponentVersionUrl";
    private static final String PROJECT_VERSION_URL = "https://blackDuckProjectVersionUrl";

    private static final String POLICY_RULE_NAME = "blackDuckRuleName";
    private static final String POLICY_RULE_URI = "https://bdRuleUrl:443";

    private static String EXPECTED_ISSUE_SUMMARY(final BlackDuckIssueModel blackDuckIssueModel) {
        final BlackDuckIssueFieldTemplate blackDuckIssueFieldTemplate = blackDuckIssueModel.getBlackDuckIssueFieldTemplate();
        String issueType = BlackDuckJiraConstants.BLACKDUCK_VULNERABILITY_ISSUE;
        String suffix = "";
        if (blackDuckIssueModel.isPolicy()) {
            issueType = BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_ISSUE;
            suffix = String.format(" [Rule: '%s']", blackDuckIssueFieldTemplate.getPolicyRuleName());
        } else if (blackDuckIssueModel.isSecurityPolicy()) {
            issueType = BlackDuckJiraConstants.BLACKDUCK_SECURITY_POLICY_VIOLATION_ISSUE;
            suffix = String.format(" [Rule: '%s']", blackDuckIssueFieldTemplate.getPolicyRuleName());
        }
        final String issueSummary = String.format("%s: Project '%s' / '%s', Component '%s' / '%s'",
            issueType, blackDuckIssueFieldTemplate.getProjectName(), blackDuckIssueFieldTemplate.getProjectVersionName(), blackDuckIssueFieldTemplate.getComponentName(), blackDuckIssueFieldTemplate.getComponentVersionName());
        return issueSummary + suffix;
    }

    @Test
    public void testValidVulnerabilityModel() {
        final Set<ProjectFieldCopyMapping> jiraFieldCopyMappings = new HashSet<>();
        final BlackDuckIssueModel blackDuckIssueModel = createBlackDuckIssueModel(IssueCategory.VULNERABILITY, jiraFieldCopyMappings);

        checkCommonValues(jiraFieldCopyMappings, blackDuckIssueModel);
        assertEquals(null, blackDuckIssueModel.getBlackDuckIssueTemplate().getPolicyRuleName());
        assertEquals(null, blackDuckIssueModel.getBlackDuckIssueTemplate().getPolicyRuleUri());
        assertEquals("blackDuckComponentOrigin", blackDuckIssueModel.getBlackDuckIssueTemplate().getComponentVersionOriginName());
        assertEquals("blackDuckComponentOriginId", blackDuckIssueModel.getBlackDuckIssueTemplate().getComponentVersionOriginId());
    }

    @Test
    public void testValidPolicyModel() {
        final Set<ProjectFieldCopyMapping> jiraFieldCopyMappings = new HashSet<>();
        final BlackDuckIssueModel blackDuckIssueModel = createBlackDuckIssueModel(IssueCategory.POLICY, jiraFieldCopyMappings);

        checkCommonValues(jiraFieldCopyMappings, blackDuckIssueModel);
        assertEquals(POLICY_RULE_NAME, blackDuckIssueModel.getBlackDuckIssueTemplate().getPolicyRuleName());
        assertEquals(POLICY_RULE_URI, blackDuckIssueModel.getBlackDuckIssueTemplate().getPolicyRuleUri());
    }

    private void checkCommonValues(final Set<ProjectFieldCopyMapping> jiraFieldCopyMappings, final BlackDuckIssueModel blackDuckIssueModel) {
        final BlackDuckIssueFieldTemplate blackDuckIssueFieldTemplate = blackDuckIssueModel.getBlackDuckIssueFieldTemplate();
        assertEquals(BlackDuckIssueAction.ADD_COMMENT, blackDuckIssueModel.getIssueAction());
        assertEquals("blackDuckComponentName", blackDuckIssueFieldTemplate.getComponentName());
        if (blackDuckIssueModel.isPolicy()) {
            assertEquals(COMPONENT_URL, blackDuckIssueFieldTemplate.getComponentUri());
        }
        assertEquals("blackDuckComponentVersion", blackDuckIssueFieldTemplate.getComponentVersionName());
        assertEquals(COMPONENT_VERSION_URL, blackDuckIssueFieldTemplate.getComponentVersionUri());

        assertEquals("blackDuckComponentUsage", blackDuckIssueFieldTemplate.getUsagesString());

        assertEquals(PROJECT_VERSION_URL, blackDuckIssueFieldTemplate.getProjectVersionUri());
        assertEquals("blackDuckProjectVersionNickname", blackDuckIssueFieldTemplate.getProjectVersionNickname());
        assertEquals("blackDuckLicenseNames", blackDuckIssueFieldTemplate.getLicenseString());

        assertEquals("blackDuckProjectName", blackDuckIssueFieldTemplate.getProjectName());
        assertEquals("blackDuckProjectVersion", blackDuckIssueFieldTemplate.getProjectVersionName());

        final JiraIssueFieldTemplate jiraIssueFieldTemplate = blackDuckIssueModel.getJiraIssueFieldTemplate();
        assertEquals(jiraFieldCopyMappings, blackDuckIssueModel.getProjectFieldCopyMappings());
        assertEquals("jiraIssueAssigneeUserId", jiraIssueFieldTemplate.getAssigneeId());
        assertEquals("jiraIssueComment", blackDuckIssueModel.getJiraIssueComment());
        assertEquals("jiraIssueCommentForExistingIssue", blackDuckIssueModel.getJiraIssueCommentForExistingIssue());
        assertEquals("jiraIssueCommentForExistingIssue", blackDuckIssueModel.getJiraIssueCommentForExistingIssue());
        assertEquals("jiraIssueCommentInLieuOfStateChange", blackDuckIssueModel.getJiraIssueCommentInLieuOfStateChange());
        assertEquals("jiraIssueDescription", jiraIssueFieldTemplate.getIssueDescription());
        assertEquals("jiraIssueReOpenComment", blackDuckIssueModel.getJiraIssueReOpenComment());
        assertEquals("jiraIssueResolveComment", blackDuckIssueModel.getJiraIssueResolveComment());
        assertEquals(EXPECTED_ISSUE_SUMMARY(blackDuckIssueModel), jiraIssueFieldTemplate.getSummary());
        assertEquals("jiraIssueTypeId", jiraIssueFieldTemplate.getIssueTypeId());
        assertEquals(Long.valueOf(123L), jiraIssueFieldTemplate.getProjectId());
        assertEquals("jiraProjectName", jiraIssueFieldTemplate.getProjectName());
        assertEquals("jiraIssueCreatorUserName", jiraIssueFieldTemplate.getIssueCreator().getUsername());
    }

    private BlackDuckIssueModel createBlackDuckIssueModel(final IssueCategory issueCategory, final Set<ProjectFieldCopyMapping> jiraFieldCopyMappings) {
        final String jiraIssueSummary;
        final BlackDuckIssueFieldTemplate blackDuckIssueFieldTemplate;
        if (IssueCategory.POLICY.equals(issueCategory) || IssueCategory.SECURITY_POLICY.equals(issueCategory)) {
            blackDuckIssueFieldTemplate = BlackDuckIssueFieldTemplate.createPolicyIssueFieldTemplate(null,
                "blackDuckProjectName",
                "blackDuckProjectVersion",
                PROJECT_VERSION_URL,
                "blackDuckProjectVersionNickname",
                null,
                "blackDuckComponentName",
                COMPONENT_URL,
                "blackDuckComponentVersion",
                COMPONENT_VERSION_URL,
                "blackDuckLicenseNames",
                "blackDuckLicenseLink",
                "blackDuckComponentUsage",
                "blackDuckUpdatedTime",
                POLICY_RULE_NAME,
                POLICY_RULE_URI,
                "blackDuckPolicyRuleOverridable",
                "blackDuckPolicyRuleDescription",
                "blackDuckPolicyRuleSeverity"
            );
            jiraIssueSummary = "Black Duck Policy Violation: Project 'blackDuckProjectName' / 'blackDuckProjectVersion', Component 'blackDuckComponentName' / 'blackDuckComponentVersion' [Rule: 'blackDuckRuleName']";
        } else {
            blackDuckIssueFieldTemplate = BlackDuckIssueFieldTemplate.createVulnerabilityIssueFieldTemplate(null,
                "blackDuckProjectName",
                "blackDuckProjectVersion",
                PROJECT_VERSION_URL,
                "blackDuckProjectVersionNickname",
                null,
                "blackDuckComponentName",
                "blackDuckComponentVersion",
                COMPONENT_VERSION_URL,
                "blackDuckLicenseNames",
                "blackDuckLicenseLink",
                "blackDuckComponentUsage",
                DATE_INSTANCE.toString(),
                "blackDuckComponentOrigin",
                "blackDuckComponentOriginId"
            );
            jiraIssueSummary = "Black Duck Security Vulnerability: Project 'blackDuckProjectName' / 'blackDuckProjectVersion', Component 'blackDuckComponentName' / 'blackDuckComponentVersion'";
        }
        final ApplicationUserMock issueCreator = new ApplicationUserMock();
        issueCreator.setUsername("jiraIssueCreatorUserName");

        final JiraIssueFieldTemplate jiraIssueFieldTemplate = new JiraIssueFieldTemplate(123L, "jiraProjectName", "jiraIssueTypeId", jiraIssueSummary, issueCreator, "jiraIssueDescription", "jiraIssueAssigneeUserId");
        final BlackDuckIssueModel issueModel = new BlackDuckIssueModel(BlackDuckIssueAction.ADD_COMMENT, jiraIssueFieldTemplate, blackDuckIssueFieldTemplate, jiraFieldCopyMappings, "bomComponentUri", "componentIssueUrl", DATE_INSTANCE);
        issueModel.setJiraIssueComment("jiraIssueComment");
        issueModel.setJiraIssueCommentForExistingIssue("jiraIssueCommentForExistingIssue");
        issueModel.setJiraIssueCommentForExistingIssue("jiraIssueCommentForExistingIssue");
        issueModel.setJiraIssueCommentInLieuOfStateChange("jiraIssueCommentInLieuOfStateChange");
        issueModel.setJiraIssueReOpenComment("jiraIssueReOpenComment");
        issueModel.setJiraIssueResolveComment("jiraIssueResolveComment");

        return issueModel;
    }
}
