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
package com.blackducksoftware.integration.jira.config;

import java.io.InputStream;
import java.util.Collection;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarImpl;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.properties.IssuePropertyService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.project.property.ProjectPropertyService;
import com.atlassian.jira.bc.user.search.DefaultUserPickerSearchService;
import com.atlassian.jira.bc.user.search.UserSearchService;
import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.entity.property.JsonEntityPropertyManager;
import com.atlassian.jira.icon.IconType;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.label.LabelManager;
import com.atlassian.jira.issue.watchers.WatcherManager;
import com.atlassian.jira.project.AssigneeTypes;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.plugin.util.ClassLoaderUtils;
import com.blackducksoftware.integration.jira.common.model.JiraProject;
import com.blackducksoftware.integration.jira.task.issue.handler.JiraIssuePropertyWrapper;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;

@InjectableComponent
public class JiraServices {
    public ConstantsManager getConstantsManager() {
        return ComponentAccessor.getConstantsManager();
    }

    public ProjectManager getJiraProjectManager() {
        return ComponentAccessor.getProjectManager();
    }

    public AvatarManager getAvatarManager() {
        return ComponentAccessor.getAvatarManager();
    }

    public IssueTypeSchemeManager getIssueTypeSchemeManager() {
        return ComponentAccessor.getIssueTypeSchemeManager();
    }

    public IssueService getIssueService() {
        return ComponentAccessor.getIssueService();
    }

    public IssueManager getIssueManager() {
        return ComponentAccessor.getIssueManager();
    }

    public Collection<IssueType> getIssueTypes() {
        return getConstantsManager().getAllIssueTypeObjects();
    }

    public JiraAuthenticationContext getAuthContext() {
        return ComponentAccessor.getJiraAuthenticationContext();
    }

    public IssuePropertyService getPropertyService() {
        return ComponentAccessor.getComponentOfType(IssuePropertyService.class);
    }

    public WorkflowManager getWorkflowManager() {
        return ComponentAccessor.getWorkflowManager();
    }

    public WorkflowSchemeManager getWorkflowSchemeManager() {
        return ComponentAccessor.getWorkflowSchemeManager();
    }

    public JsonEntityPropertyManager getJsonEntityPropertyManager() {
        return ComponentAccessor.getComponentOfType(JsonEntityPropertyManager.class);
    }

    public CommentManager getCommentManager() {
        return ComponentAccessor.getCommentManager();
    }

    public ClusterManager getClusterManager() {
        return ComponentAccessor.getComponentOfType(ClusterManager.class);
    }

    public UserManager getUserManager() {
        return ComponentAccessor.getUserManager();
    }

    public UserSearchService createUserSearchService() {
        return new DefaultUserPickerSearchService(getUserManager(), ComponentAccessor.getApplicationProperties(), getAuthContext(), ComponentAccessor.getPermissionManager(), getGroupManager(),
            getJiraProjectManager(), null, ComponentAccessor.getCrowdService(), null, null, null);
    }

    public UserUtil getUserUtil() {
        return ComponentAccessor.getUserUtil();
    }

    public ApplicationUser userToApplicationUser(final User user) {
        return ApplicationUsers.from(user);
    }

    public FieldScreenManager getFieldScreenManager() {
        return ComponentAccessor.getFieldScreenManager();
    }

    public FieldScreenSchemeManager getFieldScreenSchemeManager() {
        return ComponentAccessor.getComponentOfType(FieldScreenSchemeManager.class);
    }

    public FieldManager getFieldManager() {
        return ComponentAccessor.getFieldManager();
    }

    public CustomFieldManager getCustomFieldManager() {
        return ComponentAccessor.getCustomFieldManager();
    }

    public FieldLayoutManager getFieldLayoutManager() {
        return ComponentAccessor.getFieldLayoutManager();
    }

    public IssueTypeScreenSchemeManager getIssueTypeScreenSchemeManager() {
        return ComponentAccessor.getIssueTypeScreenSchemeManager();
    }

    public JiraIssuePropertyWrapper createIssuePropertyWrapper() {
        return new JiraIssuePropertyWrapper(getPropertyService(), getProjectPropertyService(), getJsonEntityPropertyManager());
    }

    public JiraProject getJiraProject(final Long jiraProjectId) throws BlackDuckIntegrationException {
        if (jiraProjectId == null) {
            throw new BlackDuckIntegrationException("Error: JIRA Project ID is null; one of the Project Mappings is likely misconfigured");
        }
        final com.atlassian.jira.project.Project atlassianJiraProject = getJiraProjectManager().getProjectObj(jiraProjectId);
        if (atlassianJiraProject == null) {
            throw new BlackDuckIntegrationException("Error: JIRA Project with ID " + jiraProjectId + " not found");
        }
        final String jiraProjectKey = atlassianJiraProject.getKey();
        final String jiraProjectName = atlassianJiraProject.getName();
        final JiraProject bdsJiraProject = new JiraProject();
        bdsJiraProject.setProjectId(jiraProjectId);
        bdsJiraProject.setProjectKey(jiraProjectKey);
        bdsJiraProject.setProjectName(jiraProjectName);
        bdsJiraProject.setAssigneeUserId(getAssigneeUserId(atlassianJiraProject));

        return bdsJiraProject;
    }

    private String getAssigneeUserId(final Project jiraProject) {
        final Long assigneeType = jiraProject.getAssigneeType();
        if (assigneeType != null && AssigneeTypes.UNASSIGNED == assigneeType) {
            return null;
        }
        // There other AssigneeTypes, but we use Project Lead for all of them
        return jiraProject.getProjectLead().getKey();
    }

    public Avatar createIssueTypeAvatarTemplate(final String filename, final String contentType, final String userId) {
        final Avatar avatarTemplate = AvatarImpl.createCustomAvatar(filename, contentType, userId, IconType.ISSUE_TYPE_ICON_TYPE);
        return avatarTemplate;
    }

    public InputStream getResourceAsStream(final String resource) {
        return ClassLoaderUtils.getResourceAsStream(resource, getClass());
    }

    public String getPluginVersion() {
        // TODO update this string if the plugin key changes
        return ComponentAccessor.getPluginAccessor().getPlugin("com.blackducksoftware.integration.hub-jira").getPluginInformation().getVersion();
    }

    public LabelManager getLabelManager() {
        return ComponentAccessor.getComponentOfType(LabelManager.class);
    }

    public GroupManager getGroupManager() {
        return ComponentAccessor.getGroupManager();
    }

    public String getJiraBaseUrl() {
        return ComponentAccessor.getApplicationProperties().getString(APKeys.JIRA_BASEURL);
    }

    public ProjectPropertyService getProjectPropertyService() {
        return ComponentAccessor.getComponentOfType(ProjectPropertyService.class);
    }

    public SearchService getSearchService() {
        return ComponentAccessor.getComponent(SearchService.class);
    }

    public WatcherManager getWatcherManager() {
        return ComponentAccessor.getWatcherManager();
    }

}
