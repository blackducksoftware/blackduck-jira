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
package com.blackducksoftware.integration.jira.hub;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.properties.IssuePropertyService;
import com.atlassian.jira.entity.property.JsonEntityPropertyManager;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.WorkflowManager;

public class TicketGeneratorInfo {

	private final ProjectManager jiraProjectManager;
	private final IssueService issueService;
	private final ApplicationUser jiraUser;
	private final String jiraIssueTypeName;
	private final JiraAuthenticationContext authContext;
	private final IssuePropertyService propertyService;

	private final WorkflowManager workflowManager;

	private final JsonEntityPropertyManager jsonEntityPropertyManager;
	private final CommentManager commentManager;

	public TicketGeneratorInfo(final ProjectManager jiraProjectManager, final IssueService issueService,
			final ApplicationUser jiraUser, final String jiraIssueTypeName, final JiraAuthenticationContext authContext,
			final IssuePropertyService propertyService,
			final WorkflowManager workflowManager, final JsonEntityPropertyManager jsonEntityPropertyManager,
			final CommentManager commentManager) {
		this.jiraProjectManager = jiraProjectManager;
		this.issueService = issueService;
		this.jiraUser = jiraUser;
		this.jiraIssueTypeName = jiraIssueTypeName;
		this.authContext = authContext;
		this.propertyService = propertyService;
		this.workflowManager = workflowManager;
		this.jsonEntityPropertyManager = jsonEntityPropertyManager;
		this.commentManager = commentManager;
	}

	public ProjectManager getJiraProjectManager() {
		return jiraProjectManager;
	}
	public IssueService getIssueService() {
		return issueService;
	}
	public ApplicationUser getJiraUser() {
		return jiraUser;
	}
	public String getJiraIssueTypeName() {
		return jiraIssueTypeName;
	}
	public JiraAuthenticationContext getAuthContext() {
		return authContext;
	}
	public IssuePropertyService getPropertyService() {
		return propertyService;
	}

	public WorkflowManager getWorkflowManager() {
		return workflowManager;
	}

	public JsonEntityPropertyManager getJsonEntityPropertyManager() {
		return jsonEntityPropertyManager;
	}

	public CommentManager getCommentManager() {
		return commentManager;
	}

}
