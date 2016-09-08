/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
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
 *******************************************************************************/
package com.blackducksoftware.integration.jira.task.issue;

import java.util.Collection;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.properties.IssuePropertyService;
import com.atlassian.jira.component.ComponentAccessor;
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
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;

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

	public GroupManager getGroupManager() {
		return ComponentAccessor.getGroupManager();
	}

	public UserManager getUserManager() {
		return ComponentAccessor.getUserManager();
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

	public FieldScreenSchemeManager getFieldScreenSchemeManager(){
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
}
