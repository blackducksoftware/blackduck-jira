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
package com.blackducksoftware.integration.jira.task.issue;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.conditions.AbstractWebCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;

public class HubIssuePanelCondition extends AbstractWebCondition {

    @Override
    public boolean shouldDisplay(final ApplicationUser arg0, final JiraHelper jiraHelper) {
        final Issue currentIssue = (Issue) jiraHelper.getContextParams().get("issue");
        final String issueType = currentIssue.getIssueType().getName();
        if (HubJiraConstants.HUB_VULNERABILITY_ISSUE.equals(issueType)) {
            return true;
        } else if (HubJiraConstants.HUB_POLICY_VIOLATION_ISSUE.equals(issueType)) {
            return true;
        }
        return false;
    }

}
