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

import java.util.Map;

import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.workflow.WorkflowScheme;
import com.atlassian.jira.workflow.WorkflowSchemeManager;

public class WorkflowHelper {
    private WorkflowSchemeManager workflowSchemeManager;
    private IssueTypeManager issueTypeManager;

    public WorkflowHelper(final WorkflowSchemeManager workflowSchemeManager, final IssueTypeManager issueTypeManager) {
        this.workflowSchemeManager = workflowSchemeManager;
        this.issueTypeManager = issueTypeManager;
    }

    public BlackDuckWorkflowStatus getBlackDuckWorkflowStatus() {
        final WorkflowScheme workflowScheme = workflowSchemeManager.getWorkflowSchemeObj(BlackDuckJiraConstants.BLACKDUCK_JIRA_WORKFLOW);
        final Map<String, String> issueTypeIdToWorkflowName = workflowScheme.getMappings();

        final IssueType policyIssueType = issueTypeManager.getIssueType(BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_ISSUE);
        final IssueType vulnIssueType = issueTypeManager.getIssueType(BlackDuckJiraConstants.BLACKDUCK_VULNERABILITY_ISSUE);

        boolean policyUsesBlackDuckWorkflow = usesBlackduckWorkflow(issueTypeIdToWorkflowName, policyIssueType.getId());
        boolean vulnUsesBlackDuckWorkflow = usesBlackduckWorkflow(issueTypeIdToWorkflowName, vulnIssueType.getId());

        if (policyUsesBlackDuckWorkflow && vulnUsesBlackDuckWorkflow) {
            return BlackDuckWorkflowStatus.ENABLED;
        } else if (policyUsesBlackDuckWorkflow) {
            return BlackDuckWorkflowStatus.POLICY_ONLY;
        } else if (vulnUsesBlackDuckWorkflow) {
            return BlackDuckWorkflowStatus.VULN_ONLY;
        }
        return BlackDuckWorkflowStatus.DISABLED;
    }

    public boolean usesBlackduckWorkflow(final Map<String, String> issueTypeIdToWorkflowName, final String issueTypeId) {
        final String workflowName = issueTypeIdToWorkflowName.get(issueTypeId);
        return BlackDuckJiraConstants.BLACKDUCK_JIRA_WORKFLOW.equals(workflowName);
    }

}
