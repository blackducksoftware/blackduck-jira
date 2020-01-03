/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2020 Synopsys, Inc.
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
package com.blackducksoftware.integration.jira.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.workflow.AssignableWorkflowScheme;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowScheme;
import com.atlassian.jira.workflow.WorkflowSchemeManager;

public class WorkflowHelper {
    private final WorkflowManager workflowManager;
    private final WorkflowSchemeManager workflowSchemeManager;
    private final ProjectManager projectManager;

    public static boolean matchesBlackDuckWorkflowName(final String workflowName) {
        if (StringUtils.isNotBlank(workflowName)) {
            return StringUtils.containsIgnoreCase(workflowName, BlackDuckJiraConstants.BLACKDUCK_JIRA_WORKFLOW);
        }
        return false;
    }

    public WorkflowHelper(final WorkflowManager workflowManager, final WorkflowSchemeManager workflowSchemeManager, final ProjectManager projectManager) {
        this.workflowManager = workflowManager;
        this.workflowSchemeManager = workflowSchemeManager;
        this.projectManager = projectManager;
    }

    public EnumSet<BlackDuckWorkflowStatus> getBlackDuckWorkflowStatus(final Long jiraProjectId) {
        if (null != jiraProjectId) {
            final Project jiraProject = projectManager.getProjectObj(jiraProjectId);
            return getBlackDuckWorkflowStatus(jiraProject);
        }
        return EnumSet.of(BlackDuckWorkflowStatus.DISABLED);
    }

    public EnumSet<BlackDuckWorkflowStatus> getBlackDuckWorkflowStatus(final Project jiraProject) {
        final Collection<IssueType> issueTypes = jiraProject.getIssueTypes();
        final IssueType policyIssueType = getIssueTypeByName(issueTypes, BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_ISSUE).orElse(null);
        final IssueType securityPolicyIssueType = getIssueTypeByName(issueTypes, BlackDuckJiraConstants.BLACKDUCK_SECURITY_POLICY_VIOLATION_ISSUE).orElse(null);
        final IssueType vulnIssueType = getIssueTypeByName(issueTypes, BlackDuckJiraConstants.BLACKDUCK_VULNERABILITY_ISSUE).orElse(null);

        final JiraWorkflow blackduckWorkflow = workflowManager.getWorkflow(BlackDuckJiraConstants.BLACKDUCK_JIRA_WORKFLOW);
        if (!doesBlackDuckDataExistYet(blackduckWorkflow, policyIssueType, securityPolicyIssueType, vulnIssueType)) {
            // No mappings have been created, so this will get enabled on the first run of the timed tasks.
            return EnumSet.of(BlackDuckWorkflowStatus.ENABLED);
        }

        final AssignableWorkflowScheme projectWorkflowScheme = workflowSchemeManager.getWorkflowSchemeObj(jiraProject);

        final boolean policyUsesBlackDuckWorkflow = usesBlackDuckWorkflow(projectWorkflowScheme, policyIssueType);
        final boolean securityPolicyUsesBlackDuckWorkflow = usesBlackDuckWorkflow(projectWorkflowScheme, securityPolicyIssueType);
        final boolean vulnUsesBlackDuckWorkflow = usesBlackDuckWorkflow(projectWorkflowScheme, vulnIssueType);

        if (policyUsesBlackDuckWorkflow && securityPolicyUsesBlackDuckWorkflow && vulnUsesBlackDuckWorkflow) {
            return EnumSet.of(BlackDuckWorkflowStatus.ENABLED);
        }
        final List<BlackDuckWorkflowStatus> statuses = new ArrayList<BlackDuckWorkflowStatus>();
        if (policyUsesBlackDuckWorkflow) {
            statuses.add(BlackDuckWorkflowStatus.POLICY);
        }
        if (securityPolicyUsesBlackDuckWorkflow) {
            statuses.add(BlackDuckWorkflowStatus.SECURITY_POLICY);
        }
        if (vulnUsesBlackDuckWorkflow) {
            statuses.add(BlackDuckWorkflowStatus.VULN);
        }
        if (statuses.size() > 0) {
            return EnumSet.copyOf(statuses);
        }
        return EnumSet.of(BlackDuckWorkflowStatus.DISABLED);
    }

    public boolean usesBlackDuckWorkflow(final WorkflowScheme workflowScheme, final IssueType issueType) {
        if (null != issueType) {
            final String workflowName = getWorkflowFromScheme(workflowScheme, issueType.getId());
            return matchesBlackDuckWorkflowName(workflowName);
        }
        return false;
    }

    private String getWorkflowFromScheme(WorkflowScheme workflowScheme, String issueTypeId) {
        String workflowName = workflowScheme.getConfiguredWorkflow(issueTypeId);
        // Jira will return null if the workflow is the "default" workflow
        if (StringUtils.isBlank(workflowName)) {
            workflowName = workflowScheme.getConfiguredDefaultWorkflow();
        }
        return workflowName;
    }

    public Optional<IssueType> getIssueTypeByName(final Collection<IssueType> issueTypes, final String name) {
        return issueTypes
                   .stream()
                   .filter(issueType -> name.equals(issueType.getName()))
                   .findFirst();
    }

    public boolean doesBlackDuckDataExistYet(final JiraWorkflow blackduckWorkflowNullable, final IssueType policyIssueTypeNullable, final IssueType securityPolicyIssueTypeNullable, final IssueType vulnIssueTypeNullable) {
        return null != blackduckWorkflowNullable || null != policyIssueTypeNullable || null != securityPolicyIssueTypeNullable || null != vulnIssueTypeNullable;
    }

}
