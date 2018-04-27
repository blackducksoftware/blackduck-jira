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
package com.blackducksoftware.integration.jira.mocks.issue;

import java.util.Map;
import java.util.Optional;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.workflow.TransitionOptions;

public class JiraIssueServiceMock implements IssueService {

    @Override
    public IssueResult assign(final ApplicationUser arg0, final AssignValidationResult arg1) {

        return null;
    }

    @Override
    public IssueResult create(final ApplicationUser arg0, final CreateValidationResult arg1) {

        return null;
    }

    @Override
    public IssueResult create(final ApplicationUser arg0, final CreateValidationResult arg1, final String arg2) {

        return null;
    }

    @Override
    public ErrorCollection delete(final ApplicationUser arg0, final DeleteValidationResult arg1) {

        return null;
    }

    @Override
    public ErrorCollection delete(final ApplicationUser arg0, final DeleteValidationResult arg1,
            final EventDispatchOption arg2, final boolean arg3) {

        return null;
    }

    @Override
    public IssueResult getIssue(final ApplicationUser arg0, final Long arg1) {

        return null;
    }

    @Override
    public IssueResult getIssue(final ApplicationUser arg0, final String arg1) {

        return null;
    }

    @Override
    public boolean isEditable(final Issue arg0, final ApplicationUser arg1) {

        return false;
    }

    @Override
    public IssueInputParameters newIssueInputParameters() {

        return new IssueInputParametersMock();
    }

    @Override
    public IssueInputParameters newIssueInputParameters(final Map<String, String[]> arg0) {

        return null;
    }

    @Override
    public IssueResult transition(final ApplicationUser arg0, final TransitionValidationResult arg1) {

        return null;
    }

    @Override
    public IssueResult update(final ApplicationUser arg0, final UpdateValidationResult arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IssueResult update(final ApplicationUser arg0, final UpdateValidationResult arg1,
            final EventDispatchOption arg2, final boolean arg3) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AssignValidationResult validateAssign(final ApplicationUser arg0, final Long arg1, final String arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CreateValidationResult validateCreate(final ApplicationUser arg0, final IssueInputParameters arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DeleteValidationResult validateDelete(final ApplicationUser arg0, final Long arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CreateValidationResult validateSubTaskCreate(final ApplicationUser arg0, final Long arg1,
            final IssueInputParameters arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TransitionValidationResult validateTransition(final ApplicationUser arg0, final Long arg1, final int arg2,
            final IssueInputParameters arg3) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TransitionValidationResult validateTransition(final ApplicationUser arg0, final Long arg1, final int arg2,
            final IssueInputParameters arg3, final TransitionOptions arg4) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UpdateValidationResult validateUpdate(final ApplicationUser arg0, final Long arg1,
            final IssueInputParameters arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AsynchronousTaskResult clone(final ApplicationUser arg0, final CloneValidationResult arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CloneValidationResult validateClone(final ApplicationUser arg0, final Issue arg1, final String arg2, final boolean arg3, final boolean arg4,
            final boolean arg5,
            final Map<CustomField, Optional<Boolean>> arg6) {
        // TODO Auto-generated method stub
        return null;
    }

}
