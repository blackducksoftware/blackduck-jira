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
package com.blackducksoftware.integration.jira.issue.handler;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.atlassian.jira.bc.ServiceResultImpl;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.IssueService.CreateValidationResult;
import com.atlassian.jira.bc.issue.IssueService.IssueResult;
import com.atlassian.jira.bc.issue.IssueService.TransitionValidationResult;
import com.atlassian.jira.bc.issue.IssueService.UpdateValidationResult;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.blackducksoftware.integration.jira.common.exception.JiraIssueException;
import com.blackducksoftware.integration.jira.web.model.ProjectFieldCopyMapping;

public class IssueEditor {
    private IssueService issueService;
    private IssueFieldCopyMappingHandler issueFieldCopyMappingHandler;
    private CommentManager commentManager;

    public IssueEditor(final IssueService issueService, final IssueFieldCopyMappingHandler issueFieldCopyMappingHandler, final CommentManager commentManager) {
        this.issueService = issueService;
        this.issueFieldCopyMappingHandler = issueFieldCopyMappingHandler;
        this.commentManager = commentManager;
    }

    public Issue createIssue(ApplicationUser issueCreator, IssueInputParameters issueInputParameters) throws JiraIssueException {
        final CreateValidationResult validationResult = issueService.validateCreate(issueCreator, issueInputParameters);
        return issueHandling(validationResult, "createIssue", () -> issueService.create(issueCreator, validationResult));
    }

    public Issue editIssue(Long issueId, ApplicationUser issueCreator, IssueInputParameters issueInputParameters) throws JiraIssueException {
        final UpdateValidationResult validationResult = issueService.validateUpdate(issueCreator, issueId, issueInputParameters);
        return issueHandling(validationResult, "updateIssue", () -> issueService.update(issueCreator, validationResult, EventDispatchOption.ISSUE_UPDATED, false));
    }

    public Issue transitionIssue(Long issueId, ApplicationUser issueCreator, Integer transitionActionId, IssueInputParameters issueInputParameters) throws JiraIssueException {
        final TransitionValidationResult validationResult = issueService.validateTransition(issueCreator, issueId, transitionActionId, issueInputParameters);
        return issueHandling(validationResult, "transitionIssue", () -> issueService.transition(issueCreator, validationResult));
    }

    public void addComment(Issue issue, String comment) {
        commentManager.create(issue, issue.getCreator(), comment, false);
    }

    public void addLabel(Issue issue, Set<ProjectFieldCopyMapping> projectFieldCopyMappings, IssueInputParameters issueInputParameters, Map<Long, String> blackDuckFieldMappings) {
        final List<String> labels = issueFieldCopyMappingHandler.setFieldCopyMappings(issueInputParameters, projectFieldCopyMappings, blackDuckFieldMappings,
            issue.getProjectObject().getName(), issue.getProjectId());
        issueFieldCopyMappingHandler.addLabels(issue.getId(), labels);
    }

    private Issue issueHandling(ServiceResultImpl validationResult, String methodName, Supplier<IssueResult> issueResultFunction) throws JiraIssueException {
        if (validationResult.isValid()) {
            final IssueResult result = issueResultFunction.get();
            final ErrorCollection errors = result.getErrorCollection();
            if (!errors.hasAnyErrors()) {
                return result.getIssue();
            }
            throw new JiraIssueException(methodName, errors);
        }
        throw new JiraIssueException(methodName, validationResult.getErrorCollection());
    }

}
