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
package com.blackducksoftware.integration.jira.mocks.workflow;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowProgressAware;
import com.atlassian.jira.workflow.WorkflowScheme;
import com.opensymphony.workflow.StoreException;
import com.opensymphony.workflow.Workflow;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.opensymphony.workflow.spi.WorkflowStore;

public class WorkflowManagerMock implements WorkflowManager {

    private final Map<String, JiraWorkflow> workflowMap = new HashMap<>();

    boolean attemptedCreateWorkflow;

    public boolean getAttemptedCreateWorkflow() {
        return attemptedCreateWorkflow;
    }

    public void addWorkflow(final JiraWorkflow jiraWorkflow) {
        workflowMap.put(jiraWorkflow.getName(), jiraWorkflow);
    }

    @Override
    public JiraWorkflow copyWorkflow(final String arg0, final String arg1, final String arg2, final JiraWorkflow arg3) {

        return null;
    }

    @Override
    public JiraWorkflow copyWorkflow(final ApplicationUser arg0, final String arg1, final String arg2,
            final JiraWorkflow arg3) {

        return null;
    }

    @Override
    public JiraWorkflow createDraftWorkflow(final String arg0, final String arg1)
            throws IllegalStateException, IllegalArgumentException {

        return null;
    }

    @Override
    public JiraWorkflow createDraftWorkflow(final ApplicationUser arg0, final String arg1)
            throws IllegalStateException, IllegalArgumentException {

        return null;
    }

    @Override
    public GenericValue createIssue(final String arg0, final Map<String, Object> arg1) throws WorkflowException {

        return null;
    }

    @Override
    public void createWorkflow(final String arg0, final JiraWorkflow arg1) throws WorkflowException {

    }

    @Override
    public void createWorkflow(final ApplicationUser arg0, final JiraWorkflow arg1) throws WorkflowException {
        attemptedCreateWorkflow = true;
    }

    @Override
    public boolean deleteDraftWorkflow(final String arg0) throws IllegalArgumentException {

        return false;
    }

    @Override
    public void deleteWorkflow(final JiraWorkflow arg0) throws WorkflowException {

    }

    @Override
    public void doWorkflowAction(final WorkflowProgressAware arg0) {

    }

    @Override
    public ActionDescriptor getActionDescriptor(final WorkflowProgressAware arg0) throws Exception {

        return null;
    }

    @Override
    public Collection<JiraWorkflow> getActiveWorkflows() throws WorkflowException {

        return null;
    }

    @Override
    public JiraWorkflow getDefaultWorkflow() throws WorkflowException {

        return null;
    }

    @Override
    public JiraWorkflow getDraftWorkflow(final String arg0) throws IllegalArgumentException {

        return null;
    }

    @Override
    public String getNextStatusIdForAction(final Issue arg0, final int arg1) {

        return null;
    }

    @Override
    public Map<ActionDescriptor, Collection<FunctionDescriptor>> getPostFunctionsForWorkflow(final JiraWorkflow arg0) {

        return null;
    }

    @Override
    public String getStepId(final long arg0, final String arg1) {

        return null;
    }

    @Override
    public WorkflowStore getStore() throws StoreException {

        return null;
    }

    @Override
    public JiraWorkflow getWorkflow(final String workflowName) {

        return workflowMap.get(workflowName);
    }

    @Override
    public JiraWorkflow getWorkflow(final GenericValue arg0) throws WorkflowException {

        return null;
    }

    @Override
    public JiraWorkflow getWorkflow(final Issue arg0) throws WorkflowException {

        return null;
    }

    @Override
    public JiraWorkflow getWorkflow(final Long arg0, final String arg1) throws WorkflowException {

        return null;
    }

    @Override
    public JiraWorkflow getWorkflowClone(final String arg0) {

        return null;
    }

    @Override
    public JiraWorkflow getWorkflowFromScheme(final GenericValue arg0, final String arg1) throws WorkflowException {

        return null;
    }

    @Override
    public JiraWorkflow getWorkflowFromScheme(final WorkflowScheme arg0, final String arg1) throws WorkflowException {

        return null;
    }

    @Override
    public Collection<JiraWorkflow> getWorkflows() {

        return null;
    }

    @Override
    public Collection<JiraWorkflow> getWorkflowsFromScheme(final GenericValue arg0) throws WorkflowException {

        return null;
    }

    @Override
    public Iterable<JiraWorkflow> getWorkflowsFromScheme(final Scheme arg0) throws WorkflowException {

        return null;
    }

    @Override
    public List<JiraWorkflow> getWorkflowsIncludingDrafts() {

        return null;
    }

    @Override
    public boolean isActive(final JiraWorkflow arg0) throws WorkflowException {

        return false;
    }

    @Override
    public boolean isEditable(final Issue arg0) {

        return false;
    }

    @Override
    public boolean isSystemWorkflow(final JiraWorkflow arg0) throws WorkflowException {

        return false;
    }

    @Override
    public Workflow makeWorkflow(final String arg0) {

        return null;
    }

    @Override
    public Workflow makeWorkflow(final ApplicationUser arg0) {

        return null;
    }

    @Override
    public Workflow makeWorkflowWithUserKey(final String arg0) {

        return null;
    }

    @Override
    public Workflow makeWorkflowWithUserName(final String arg0) {

        return null;
    }

    @Override
    public void migrateIssueToWorkflow(final MutableIssue arg0, final JiraWorkflow arg1, final Status arg2)
            throws WorkflowException {

    }

    @Override
    public void migrateIssueToWorkflow(final GenericValue arg0, final JiraWorkflow arg1, final GenericValue arg2)
            throws WorkflowException {

    }

    @Override
    public boolean migrateIssueToWorkflowNoReindex(final GenericValue arg0, final JiraWorkflow arg1,
            final GenericValue arg2) throws WorkflowException {

        return false;
    }

    @Override
    public void overwriteActiveWorkflow(final String arg0, final String arg1) {

    }

    @Override
    public void overwriteActiveWorkflow(final ApplicationUser arg0, final String arg1) {

    }

    @Override
    public void removeWorkflowEntries(final GenericValue arg0) {

    }

    @Override
    public void saveWorkflowWithoutAudit(final JiraWorkflow arg0) throws WorkflowException {

    }

    @Override
    public void updateWorkflow(final String arg0, final JiraWorkflow arg1) {

    }

    @Override
    public void updateWorkflow(final ApplicationUser arg0, final JiraWorkflow arg1) {

    }

    @Override
    public void updateWorkflowNameAndDescription(final String arg0, final JiraWorkflow arg1, final String arg2,
            final String arg3) {

    }

    @Override
    public void updateWorkflowNameAndDescription(final ApplicationUser arg0, final JiraWorkflow arg1, final String arg2,
            final String arg3) {

    }

    @Override
    public boolean workflowExists(final String arg0) throws WorkflowException {

        return false;
    }

    @Override
    public void copyAndDeleteDraftWorkflows(final ApplicationUser arg0, final Set<JiraWorkflow> arg1) {
        // Auto-generated method stub

    }

    @Override
    public void copyAndDeleteDraftsForInactiveWorkflowsIn(final ApplicationUser arg0, final Iterable<JiraWorkflow> arg1) {
        // Auto-generated method stub

    }

    @Override
    public ActionDescriptor getActionDescriptor(final Issue arg0, final int arg1) {
        // Auto-generated method stub
        return null;
    }

    @Override
    public void replaceConditionInTransition(final ActionDescriptor arg0, final Map<String, String> arg1,
            final Map<String, String> arg2) {
        // Auto-generated method stub

    }

}
