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
package com.blackducksoftware.integration.jira.mocks;

import java.util.Collection;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.properties.IssuePropertyService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.project.property.ProjectPropertyService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.entity.property.JsonEntityPropertyManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.blackducksoftware.integration.jira.config.JiraServices;

public class JiraServicesMock extends JiraServices {
    private ConstantsManager constantsManager;
    private ProjectManager projectManager;
    private AvatarManager avatarManager;
    private IssueTypeSchemeManager issueTypeSchemeManager;
    private IssueService issueService;
    private JiraAuthenticationContext jiraAuthenticationContext;
    private IssuePropertyService issuePropertyService;
    private ProjectPropertyService projectPropertyService;
    private WorkflowManager workflowManager;
    private WorkflowSchemeManager workflowSchemeManager;
    private JsonEntityPropertyManager jsonEntityPropertyManager;
    private CommentManager commentManager;
    private UserManager userManager;
    private Collection<IssueType> issueTypes;
    private UserUtil userUtil;
    private FieldScreenManager fieldScreenManager;
    private FieldScreenSchemeManager fieldScreenSchemeManager;
    private FieldManager fieldManager;
    private CustomFieldManager customFieldManager;
    private FieldLayoutManager fieldLayoutManager;
    private IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;
    private SearchService searchService;
    private NotificationSchemeManager notificationSchemeManager;

    @Override
    public UserUtil getUserUtil() {
        return userUtil;
    }

    public void setUserUtil(final UserUtil userUtil) {
        this.userUtil = userUtil;
    }

    @Override
    public Collection<IssueType> getIssueTypes() {
        return issueTypes;
    }

    public void setIssueTypes(final Collection<IssueType> issueTypes) {
        this.issueTypes = issueTypes;
    }

    public void setProjectManager(final ProjectManager projectManager) {
        this.projectManager = projectManager;
    }

    public void setJiraAuthenticationContext(final JiraAuthenticationContext jiraAuthenticationContext) {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    public void setIssuePropertyService(final IssuePropertyService issuePropertyService) {
        this.issuePropertyService = issuePropertyService;
    }

    @Override
    public ConstantsManager getConstantsManager() {
        return constantsManager;
    }

    public void setConstantsManager(final ConstantsManager constantsManager) {
        this.constantsManager = constantsManager;
    }

    @Override
    public IssueTypeSchemeManager getIssueTypeSchemeManager() {
        return issueTypeSchemeManager;
    }

    public void setIssueTypeSchemeManager(final IssueTypeSchemeManager issueTypeSchemeManager) {
        this.issueTypeSchemeManager = issueTypeSchemeManager;
    }

    @Override
    public ProjectManager getJiraProjectManager() {
        return projectManager;
    }

    @Override
    public AvatarManager getAvatarManager() {
        return avatarManager;
    }

    public void setAvatarManager(final AvatarManager avatarManager) {
        this.avatarManager = avatarManager;
    }

    @Override
    public IssueService getIssueService() {
        return issueService;
    }

    public void setIssueService(final IssueService issueService) {
        this.issueService = issueService;
    }

    @Override
    public JiraAuthenticationContext getAuthContext() {
        return jiraAuthenticationContext;
    }

    @Override
    public IssuePropertyService getPropertyService() {
        return issuePropertyService;
    }

    @Override
    public ProjectPropertyService getProjectPropertyService() {
        return projectPropertyService;
    }

    public void setProjectPropertyService(final ProjectPropertyService projectPropertyService) {
        this.projectPropertyService = projectPropertyService;
    }

    @Override
    public WorkflowManager getWorkflowManager() {
        return workflowManager;
    }

    public void setWorkflowManager(final WorkflowManager workflowManager) {
        this.workflowManager = workflowManager;
    }

    @Override
    public WorkflowSchemeManager getWorkflowSchemeManager() {
        return workflowSchemeManager;
    }

    public void setWorkflowSchemeManager(final WorkflowSchemeManager workflowSchemeManager) {
        this.workflowSchemeManager = workflowSchemeManager;
    }

    @Override
    public JsonEntityPropertyManager getJsonEntityPropertyManager() {
        return jsonEntityPropertyManager;
    }

    public void setJsonEntityPropertyManager(final JsonEntityPropertyManager jsonEntityPropertyManager) {
        this.jsonEntityPropertyManager = jsonEntityPropertyManager;
    }

    @Override
    public CommentManager getCommentManager() {
        return commentManager;
    }

    public void setCommentManager(final CommentManager commentManager) {
        this.commentManager = commentManager;
    }

    @Override
    public UserManager getUserManager() {
        return userManager;
    }

    public void setUserManager(final UserManager userManager) {
        this.userManager = userManager;
    }

    @Override
    public ApplicationUser userToApplicationUser(final User user) {
        final ApplicationUserMock userMock = new ApplicationUserMock();
        userMock.setName(user.getName());
        return userMock;
    }

    @Override
    public FieldScreenManager getFieldScreenManager() {
        return fieldScreenManager;
    }

    public void setFieldScreenManager(final FieldScreenManager fieldScreenManager) {
        this.fieldScreenManager = fieldScreenManager;
    }

    @Override
    public FieldScreenSchemeManager getFieldScreenSchemeManager() {
        return fieldScreenSchemeManager;
    }

    public void setFieldScreenSchemeManager(final FieldScreenSchemeManager fieldScreenSchemeManager) {
        this.fieldScreenSchemeManager = fieldScreenSchemeManager;
    }

    @Override
    public FieldManager getFieldManager() {
        return fieldManager;
    }

    public void setFieldManager(final FieldManager fieldManager) {
        this.fieldManager = fieldManager;
    }

    @Override
    public CustomFieldManager getCustomFieldManager() {
        return customFieldManager;
    }

    public void setCustomFieldManager(final CustomFieldManager customFieldManager) {
        this.customFieldManager = customFieldManager;
    }

    @Override
    public FieldLayoutManager getFieldLayoutManager() {
        return fieldLayoutManager;
    }

    public void setFieldLayoutManager(final FieldLayoutManager fieldLayoutManager) {
        this.fieldLayoutManager = fieldLayoutManager;
    }

    @Override
    public IssueTypeScreenSchemeManager getIssueTypeScreenSchemeManager() {
        return issueTypeScreenSchemeManager;
    }

    public void setIssueTypeScreenSchemeManager(final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager) {
        this.issueTypeScreenSchemeManager = issueTypeScreenSchemeManager;
    }

    @Override
    public SearchService getSearchService() {
        return searchService;
    }

    public void setSearchService(final SearchService searchService) {
        this.searchService = searchService;
    }

    @Override
    public String getJiraBaseUrl() {
        return "jira-base-url";
    }
    
}
