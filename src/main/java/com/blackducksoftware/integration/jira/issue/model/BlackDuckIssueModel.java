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
package com.blackducksoftware.integration.jira.issue.model;

import java.util.Date;
import java.util.Set;

import com.blackducksoftware.integration.jira.issue.conversion.output.BlackDuckIssueAction;
import com.blackducksoftware.integration.jira.web.model.ProjectFieldCopyMapping;
import com.synopsys.integration.util.Stringable;

public class BlackDuckIssueModel extends Stringable {
    private final BlackDuckIssueAction issueAction;
    private final JiraIssueFieldTemplate jiraIssueFieldTemplate;
    private final BlackDuckIssueFieldTemplate blackDuckIssueFieldTemplate;
    private final Set<ProjectFieldCopyMapping> projectFieldCopyMappings;
    private final String bomComponentUri;
    private final String componentIssueUrl;
    private final Date lastBatchStartDate;

    private Long jiraIssueId = null;
    private String jiraIssueComment = null;
    private String jiraIssueReOpenComment = null;
    private String jiraIssueCommentForExistingIssue = null;
    private String jiraIssueResolveComment = null;
    private String jiraIssueCommentInLieuOfStateChange = null;

    // @formatter:off
    public BlackDuckIssueModel(
             final BlackDuckIssueAction issueAction
            ,final JiraIssueFieldTemplate jiraIssueFieldTemplate
            ,final BlackDuckIssueFieldTemplate blackDuckIssueTemplate
            ,final Set<ProjectFieldCopyMapping> projectFieldCopyMappings
            ,final String bomComponentUri
            ,final String componentIssueUrl
            ,final Date lastBatchStartDate
            ) {
        this.issueAction = issueAction;
        this.jiraIssueFieldTemplate = jiraIssueFieldTemplate;
        this.blackDuckIssueFieldTemplate = blackDuckIssueTemplate;
        this.projectFieldCopyMappings = projectFieldCopyMappings;
        this.bomComponentUri = bomComponentUri;
        this.componentIssueUrl = componentIssueUrl;
        this.lastBatchStartDate = lastBatchStartDate;
    }
    // @formatter:on

    public Long getJiraIssueId() {
        return jiraIssueId;
    }

    public void setJiraIssueId(final Long jiraIssueId) {
        this.jiraIssueId = jiraIssueId;
    }

    public BlackDuckIssueAction getIssueAction() {
        return issueAction;
    }

    public JiraIssueFieldTemplate getJiraIssueFieldTemplate() {
        return jiraIssueFieldTemplate;
    }

    public BlackDuckIssueFieldTemplate getBlackDuckIssueTemplate() {
        return blackDuckIssueFieldTemplate;
    }

    public Set<ProjectFieldCopyMapping> getProjectFieldCopyMappings() {
        return projectFieldCopyMappings;
    }

    public BlackDuckIssueFieldTemplate getBlackDuckIssueFieldTemplate() {
        return blackDuckIssueFieldTemplate;
    }

    public String getBomComponentUri() {
        return bomComponentUri;
    }

    public String getComponentIssueUrl() {
        return componentIssueUrl;
    }

    public Date getLastBatchStartDate() {
        return lastBatchStartDate;
    }

    public String getJiraIssueComment() {
        return jiraIssueComment;
    }

    public void setJiraIssueComment(final String jiraIssueComment) {
        this.jiraIssueComment = jiraIssueComment;
    }

    public String getJiraIssueReOpenComment() {
        return jiraIssueReOpenComment;
    }

    public void setJiraIssueReOpenComment(final String jiraIssueReOpenComment) {
        this.jiraIssueReOpenComment = jiraIssueReOpenComment;
    }

    public String getJiraIssueCommentForExistingIssue() {
        return jiraIssueCommentForExistingIssue;
    }

    public void setJiraIssueCommentForExistingIssue(final String jiraIssueCommentForExistingIssue) {
        this.jiraIssueCommentForExistingIssue = jiraIssueCommentForExistingIssue;
    }

    public String getJiraIssueResolveComment() {
        return jiraIssueResolveComment;
    }

    public void setJiraIssueResolveComment(final String jiraIssueResolveComment) {
        this.jiraIssueResolveComment = jiraIssueResolveComment;
    }

    public String getJiraIssueCommentInLieuOfStateChange() {
        return jiraIssueCommentInLieuOfStateChange;
    }

    public void setJiraIssueCommentInLieuOfStateChange(final String jiraIssueCommentInLieuOfStateChange) {
        this.jiraIssueCommentInLieuOfStateChange = jiraIssueCommentInLieuOfStateChange;
    }

    public boolean isPolicy() {
        return IssueCategory.POLICY.equals(blackDuckIssueFieldTemplate.getIssueCategory());
    }

    public boolean isSecurityPolicy() {
        return IssueCategory.SECURITY_POLICY.equals(blackDuckIssueFieldTemplate.getIssueCategory());
    }

    public boolean isVulnerability() {
        return IssueCategory.VULNERABILITY.equals(blackDuckIssueFieldTemplate.getIssueCategory());
    }
}
