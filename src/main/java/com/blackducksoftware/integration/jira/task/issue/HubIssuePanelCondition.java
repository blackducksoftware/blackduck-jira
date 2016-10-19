/*
 * Copyright (C) 2016 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.jira.task.issue;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.conditions.AbstractWebCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;

public class HubIssuePanelCondition extends AbstractWebCondition {

    @Override
    public boolean shouldDisplay(ApplicationUser arg0, JiraHelper jiraHelper) {
        final Issue currentIssue = (Issue) jiraHelper.getContextParams().get("issue");
        if (currentIssue.getIssueTypeObject().getName().equals(HubJiraConstants.HUB_VULNERABILITY_ISSUE)) {
            return true;
        } else if (currentIssue.getIssueTypeObject().getName().equals(HubJiraConstants.HUB_POLICY_VIOLATION_ISSUE)) {
            return true;
        }
        return false;
    }

}
