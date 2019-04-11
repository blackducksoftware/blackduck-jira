/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.jira.common;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.workflow.AssignableWorkflowScheme;
import com.atlassian.jira.workflow.WorkflowSchemeManager;

public class WorkflowHelper {
    private WorkflowSchemeManager workflowSchemeManager;
    private ProjectManager projectManager;

    public WorkflowHelper(final WorkflowSchemeManager workflowSchemeManager, final ProjectManager projectManager) {
        this.workflowSchemeManager = workflowSchemeManager;
        this.projectManager = projectManager;
    }

    public BlackDuckWorkflowStatus getBlackDuckWorkflowStatus(final Long jiraProjectId) {
        if (null != jiraProjectId) {
            final Project jiraProject = projectManager.getProjectObj(jiraProjectId);
            return getBlackDuckWorkflowStatus(jiraProject);
        }
        return BlackDuckWorkflowStatus.DISABLED;
    }

    public BlackDuckWorkflowStatus getBlackDuckWorkflowStatus(final Project jiraProject) {
        final AssignableWorkflowScheme projectWorkflowScheme = workflowSchemeManager.getWorkflowSchemeObj(jiraProject);
        final Map<String, String> issueTypeIdToWorkflowName = projectWorkflowScheme.getMappings();

        final Collection<IssueType> issueTypes = jiraProject.getIssueTypes();
        final IssueType policyIssueType = getIssueTypeByName(issueTypes, BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_ISSUE).orElse(null);
        final IssueType vulnIssueType = getIssueTypeByName(issueTypes, BlackDuckJiraConstants.BLACKDUCK_VULNERABILITY_ISSUE).orElse(null);

        boolean policyUsesBlackDuckWorkflow = usesBlackduckWorkflow(issueTypeIdToWorkflowName, policyIssueType);
        boolean vulnUsesBlackDuckWorkflow = usesBlackduckWorkflow(issueTypeIdToWorkflowName, vulnIssueType);

        if (policyUsesBlackDuckWorkflow && vulnUsesBlackDuckWorkflow) {
            return BlackDuckWorkflowStatus.ENABLED;
        } else if (policyUsesBlackDuckWorkflow) {
            return BlackDuckWorkflowStatus.POLICY_ONLY;
        } else if (vulnUsesBlackDuckWorkflow) {
            return BlackDuckWorkflowStatus.VULN_ONLY;
        }
        return BlackDuckWorkflowStatus.DISABLED;
    }

    public boolean usesBlackduckWorkflow(final Map<String, String> issueTypeIdToWorkflowName, final IssueType issueType) {
        if (null != issueType) {
            final String workflowName = issueTypeIdToWorkflowName.get(issueType.getId());
            return BlackDuckJiraConstants.BLACKDUCK_JIRA_WORKFLOW.equals(workflowName);
        }
        return false;
    }

    public Optional<IssueType> getIssueTypeByName(final Collection<IssueType> issueTypes, final String name) {
        return issueTypes
                   .stream()
                   .filter(issueType -> name.equals(issueType.getName()))
                   .findFirst();
    }

}
