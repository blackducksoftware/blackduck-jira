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
package com.blackducksoftware.integration.jira.mocks.issue;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Set;

import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.user.ApplicationUser;

public class IssueMock implements Issue {
    private Long id;
    private Timestamp created;
    private Timestamp updated;
    private Status status;
    private String description;
    private ApplicationUser assignee;
    private Project project;

    @Override
    public Long getLong(final String arg0) {
        return null;
    }

    @Override
    public String getString(final String arg0) {
        return null;
    }

    @Override
    public Timestamp getTimestamp(final String arg0) {
        return null;
    }

    @Override
    public void store() {
    }

    @Override
    public Collection<Version> getAffectedVersions() {
        return null;
    }

    @Override
    public ApplicationUser getAssignee() {
        return assignee;
    }

    public void setAssignee(final ApplicationUser assignee) {
        this.assignee = assignee;
    }

    @Override
    public String getAssigneeId() {
        return null;
    }

    @Override
    public ApplicationUser getAssigneeUser() {
        return null;
    }

    @Override
    public Collection<Attachment> getAttachments() {
        return null;
    }

    @Override
    public Collection<ProjectComponent> getComponentObjects() {
        return null;
    }

    @Override
    public Collection<ProjectComponent> getComponents() {
        return null;
    }

    @Override
    public Timestamp getCreated() {
        return created;
    }

    public void setCreated(final Timestamp created) {
        this.created = created;
    }

    @Override
    public ApplicationUser getCreator() {
        return null;
    }

    @Override
    public String getCreatorId() {
        return null;
    }

    @Override
    public Object getCustomFieldValue(final CustomField arg0) {
        return null;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public Timestamp getDueDate() {
        return null;
    }

    @Override
    public String getEnvironment() {
        return null;
    }

    @Override
    public Long getEstimate() {
        return null;
    }

    @Override
    public Object getExternalFieldValue(final String arg0) {
        return null;
    }

    @Override
    public Collection<Version> getFixVersions() {
        return null;
    }

    @Override
    public GenericValue getGenericValue() {
        return null;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public IssueRenderContext getIssueRenderContext() {
        return null;
    }

    @Override
    public IssueType getIssueType() {
        return null;
    }

    @Override
    public String getIssueTypeId() {
        return null;
    }

    @Override
    public IssueType getIssueTypeObject() {
        return null;
    }

    @Override
    public String getKey() {
        return String.format("%s-%s", project.getName(), getId());
    }

    @Override
    public Set<Label> getLabels() {
        return null;
    }

    @Override
    public Long getNumber() {
        return null;
    }

    @Override
    public Long getOriginalEstimate() {
        return null;
    }

    @Override
    public GenericValue getParent() {
        return null;
    }

    @Override
    public Long getParentId() {
        return null;
    }

    @Override
    public Issue getParentObject() {
        return null;
    }

    @Override
    public Priority getPriority() {
        return null;
    }

    @Override
    public Priority getPriorityObject() {
        return null;
    }

    @Override
    public GenericValue getProject() {
        return null;
    }

    @Override
    public Long getProjectId() {
        return project.getId();
    }

    @Override
    public Project getProjectObject() {
        return project;
    }

    public void setProject(final Project project) {
        this.project = project;
    }

    @Override
    public ApplicationUser getReporter() {
        return null;
    }

    @Override
    public String getReporterId() {
        return null;
    }

    @Override
    public ApplicationUser getReporterUser() {
        return null;
    }

    @Override
    public Resolution getResolution() {
        return null;
    }

    @Override
    public Timestamp getResolutionDate() {
        return null;
    }

    @Override
    public String getResolutionId() {
        return null;
    }

    @Override
    public Resolution getResolutionObject() {
        return null;
    }

    @Override
    public GenericValue getSecurityLevel() {
        return null;
    }

    @Override
    public Long getSecurityLevelId() {
        return null;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public String getStatusId() {
        return status.getId();
    }

    @Override
    public Status getStatusObject() {
        return status;
    }

    public void setStatusObject(final Status status) {
        this.status = status;
    }

    @Override
    public Collection<Issue> getSubTaskObjects() {
        return null;
    }

    @Override
    public Collection<GenericValue> getSubTasks() {
        return null;
    }

    @Override
    public String getSummary() {
        return description;
    }

    @Override
    public Long getTimeSpent() {
        return null;
    }

    @Override
    public Timestamp getUpdated() {
        return updated;
    }

    public void setUpdated(final Timestamp updated) {
        this.updated = updated;
    }

    @Override
    public Long getVotes() {
        return null;
    }

    @Override
    public Long getWatches() {
        return null;
    }

    @Override
    public Long getWorkflowId() {
        return null;
    }

    @Override
    public boolean isCreated() {
        return false;
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public boolean isSubTask() {
        return false;
    }
}
