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
package com.blackducksoftware.integration.jira.mocks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ofbiz.core.entity.GenericValue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.user.ApplicationUser;

public class ProjectMock implements Project {

	private String name;
	private Long id;
	private List<IssueType> issueTypes = new ArrayList<IssueType>();

	@Override
	public Collection<IssueType> getIssueTypes() {

		return issueTypes;
	}

	public void addIssueType(final IssueType issue) {
		issueTypes.add(issue);
	}

	public void setIssueTypes(final List<IssueType> issueTypes) {
		this.issueTypes = issueTypes;
	}

	@Override
	public Long getId() {

		return id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public Long getAssigneeType() {

		return null;
	}

	@Override
	public Avatar getAvatar() {

		return null;
	}

	@Override
	public Collection<GenericValue> getComponents() {

		return null;
	}

	@Override
	public Long getCounter() {

		return null;
	}

	@Override
	public String getDescription() {

		return null;
	}

	@Override
	public String getEmail() {

		return null;
	}

	@Override
	public GenericValue getGenericValue() {

		return null;
	}

	@Override
	public String getKey() {

		return null;
	}

	@Override
	public User getLead() {

		return null;
	}

	@Override
	public User getLeadUser() {

		return null;
	}

	@Override
	public String getLeadUserName() {

		return null;
	}

	@Override
	public GenericValue getProjectCategory() {

		return null;
	}

	@Override
	public ProjectCategory getProjectCategoryObject() {

		return null;
	}

	@Override
	public Collection<ProjectComponent> getProjectComponents() {

		return null;
	}

	@Override
	public String getUrl() {

		return null;
	}

	@Override
	public Collection<Version> getVersions() {

		return null;
	}

	@Override
	public String getLeadUserKey() {
		return null;
	}

	@Override
	public String getOriginalKey() {
		return null;
	}

	@Override
	public ApplicationUser getProjectLead() {
		return null;
	}

}
