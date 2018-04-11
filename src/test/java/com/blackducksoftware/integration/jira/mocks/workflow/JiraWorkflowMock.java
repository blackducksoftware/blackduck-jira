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
package com.blackducksoftware.integration.jira.mocks.workflow;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

public class JiraWorkflowMock implements JiraWorkflow {

    private String name;

    @Override
    public int compareTo(final JiraWorkflow o) {

        return 0;
    }

    @Override
    public String getActionType(final ActionDescriptor arg0) {

        return null;
    }

    @Override
    public Collection<ActionDescriptor> getActionsByName(final String arg0) {

        return null;
    }

    @Override
    public Collection<ActionDescriptor> getActionsForScreen(final FieldScreen arg0) {

        return null;
    }

    @Override
    public Collection<ActionDescriptor> getActionsWithResult(final StepDescriptor arg0) {

        return null;
    }

    @Override
    public Collection<ActionDescriptor> getAllActions() {

        return null;
    }

    @Override
    public String getDescription() {

        return null;
    }

    @Override
    public WorkflowDescriptor getDescriptor() {

        return null;
    }

    @Override
    public String getDisplayName() {

        return null;
    }

    @Override
    public String getLinkedStatusId(final StepDescriptor arg0) {

        return null;
    }

    @Override
    public Set<String> getLinkedStatusIds() {

        return null;
    }

    @Override
    public Status getLinkedStatusObject(final StepDescriptor arg0) {

        return null;
    }

    @Override
    public List<Status> getLinkedStatusObjects() {

        return null;
    }

    @Override
    public List<GenericValue> getLinkedStatuses() {

        return null;
    }

    @Override
    public StepDescriptor getLinkedStep(final GenericValue arg0) {

        return null;
    }

    @Override
    public StepDescriptor getLinkedStep(final Status arg0) {

        return null;
    }

    @Override
    public String getMode() {

        return null;
    }

    @Override
    public String getName() {

        return name;
    }

    public void setName(final String name) {

        this.name = name;
    }

    @Override
    public int getNextActionId() {

        return 0;
    }

    @Override
    public Collection<FunctionDescriptor> getPostFunctionsForTransition(final ActionDescriptor arg0) {

        return null;
    }

    @Override
    public Collection<StepDescriptor> getStepsForTransition(final ActionDescriptor arg0) {

        return null;
    }

    @Override
    public ApplicationUser getUpdateAuthor() {

        return null;
    }

    @Override
    public String getUpdateAuthorName() {

        return null;
    }

    @Override
    public Date getUpdatedDate() {

        return null;
    }

    @Override
    public boolean hasDraftWorkflow() {

        return false;
    }

    @Override
    public boolean isActive() throws WorkflowException {

        return false;
    }

    @Override
    public boolean isCommonAction(final ActionDescriptor arg0) {

        return false;
    }

    @Override
    public boolean isDefault() {

        return false;
    }

    @Override
    public boolean isDraftWorkflow() {

        return false;
    }

    @Override
    public boolean isEditable() throws WorkflowException {

        return false;
    }

    @Override
    public boolean isGlobalAction(final ActionDescriptor arg0) {

        return false;
    }

    @Override
    public boolean isInitialAction(final ActionDescriptor arg0) {

        return false;
    }

    @Override
    public boolean isOrdinaryAction(final ActionDescriptor arg0) {

        return false;
    }

    @Override
    public boolean isSystemWorkflow() throws WorkflowException {

        return false;
    }

    @Override
    public boolean removeStep(final StepDescriptor arg0) {

        return false;
    }

    @Override
    public void reset() {

    }

    @Override
    public Status getLinkedStatus(final StepDescriptor arg0) {
        // TODO Auto-generated method stub
        return null;
    }

}
