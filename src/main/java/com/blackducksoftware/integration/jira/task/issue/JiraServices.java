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
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package com.blackducksoftware.integration.jira.task.issue;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.properties.IssuePropertyService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.entity.property.JsonEntityPropertyManager;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.workflow.WorkflowManager;

public class JiraServices {

	public ProjectManager getJiraProjectManager() {
		return ComponentAccessor.getProjectManager();
	}
	public IssueService getIssueService() {
		return ComponentAccessor.getIssueService();
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

	public JsonEntityPropertyManager getJsonEntityPropertyManager() {
		return ComponentAccessor.getComponentOfType(JsonEntityPropertyManager.class);
	}

	public CommentManager getCommentManager() {
		return ComponentAccessor.getCommentManager();
	}

}
