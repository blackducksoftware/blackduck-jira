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
import java.util.Set;

import org.ofbiz.core.entity.GenericValue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.project.DefaultAssigneeException;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.ApplicationUser;

public class ProjectManagerMock implements ProjectManager {

	public static final String JIRA_PROJECT_PREFIX = "Test JIRA Project";
	public static final long JIRA_PROJECT_ID_BASE = 153L;

	private List<Project> jiraProjects;

	@Override
	public List<Project> getProjectObjects() throws DataAccessException {
		return jiraProjects;
	}

	public void setProjectObjects(final List<Project> jiraProjects) {
		this.jiraProjects = jiraProjects;
	}

	public static List<Project> getTestProjectObjectsNullIssueTypes() throws DataAccessException {
		final List<Project> jiraProjects = getTestProjectObjectsWithoutIssueTypes();

		for(final Project project : jiraProjects){
			final ProjectMock pMock = (ProjectMock) project;
			pMock.setIssueTypes(null);
		}
		return jiraProjects;
	}

	public static List<Project> getTestProjectObjectsWithoutIssueTypes() throws DataAccessException {
		final List<Project> jiraProjects = new ArrayList<Project>();

		final ProjectMock jiraProject1 = new ProjectMock();
		jiraProject1.setId(0L);
		jiraProject1.setName("Project1");

		final ProjectMock jiraProject2 = new ProjectMock();
		jiraProject2.setId(153L);
		jiraProject2.setName("Project2");

		for (int i = 0; i < 5; i++) {
			final ProjectMock jiraProject = new ProjectMock();
			jiraProject.setId(JIRA_PROJECT_ID_BASE + i);
			jiraProject.setName(JIRA_PROJECT_PREFIX + i);
			jiraProjects.add(jiraProject);
		}

		jiraProjects.add(jiraProject1);
		jiraProjects.add(jiraProject2);
		return jiraProjects;
	}

	public static List<Project> getTestProjectObjectsWithoutTaskIssueType() throws DataAccessException {
		final List<Project> jiraProjects = getTestProjectObjectsWithoutIssueTypes();

		final IssueTypeMock issueType1 = new IssueTypeMock();
		issueType1.setName("Issue");
		final IssueTypeMock issueType2 = new IssueTypeMock();
		issueType2.setName("Bug");

		for (final Project project : jiraProjects) {
			final ProjectMock pMock = (ProjectMock) project;
			pMock.addIssueType(issueType1);
			pMock.addIssueType(issueType2);
		}
		return jiraProjects;
	}

	public static List<Project> getTestProjectObjectsWithTaskIssueType() throws DataAccessException {
		final List<Project> jiraProjects = getTestProjectObjectsWithoutIssueTypes();

		final IssueTypeMock issueType1 = new IssueTypeMock();
		issueType1.setName("Bug");
		final IssueTypeMock issueType2 = new IssueTypeMock();
		issueType2.setName("Task");
		final IssueTypeMock issueType3 = new IssueTypeMock();
		issueType3.setName("Issue");

		for (final Project project : jiraProjects) {
			final ProjectMock pMock = (ProjectMock) project;
			pMock.addIssueType(issueType1);
			pMock.addIssueType(issueType2);
			pMock.addIssueType(issueType3);
		}
		return jiraProjects;
	}



	@Override
	public List<Project> convertToProjectObjects(final Collection<Long> arg0) {

		return null;
	}

	@Override
	public List<GenericValue> convertToProjects(final Collection<Long> arg0) {

		return null;
	}

	@Override
	public Project createProject(final String arg0, final String arg1, final String arg2, final String arg3,
			final String arg4, final Long arg5) {

		return null;
	}

	@Override
	public Project createProject(final String arg0, final String arg1, final String arg2, final String arg3,
			final String arg4, final Long arg5, final Long arg6) {

		return null;
	}

	@Override
	public ProjectCategory createProjectCategory(final String arg0, final String arg1) {

		return null;
	}

	@Override
	public Collection<ProjectCategory> getAllProjectCategories() throws DataAccessException {

		return null;
	}

	@Override
	public GenericValue getComponent(final Long arg0) throws DataAccessException {

		return null;
	}

	@Override
	public GenericValue getComponent(final GenericValue arg0, final String arg1) throws DataAccessException {

		return null;
	}

	@Override
	public Collection<GenericValue> getComponents(final GenericValue arg0) throws DataAccessException {

		return null;
	}

	@Override
	public long getCurrentCounterForProject(final Long arg0) {

		return 0;
	}

	@Override
	public User getDefaultAssignee(final GenericValue arg0, final GenericValue arg1) {

		return null;
	}

	@Override
	public User getDefaultAssignee(final Project arg0, final ProjectComponent arg1) {

		return null;
	}

	@Override
	public User getDefaultAssignee(final Project arg0, final Collection<ProjectComponent> arg1)
			throws DefaultAssigneeException {

		return null;
	}

	@Override
	public long getNextId(final Project arg0) throws DataAccessException {

		return 0;
	}

	@Override
	public GenericValue getProject(final GenericValue arg0) throws DataAccessException {

		return null;
	}

	@Override
	public GenericValue getProject(final Long arg0) throws DataAccessException {

		return null;
	}

	@Override
	public GenericValue getProjectByKey(final String arg0) throws DataAccessException {

		return null;
	}

	@Override
	public GenericValue getProjectByName(final String arg0) throws DataAccessException {

		return null;
	}

	@Override
	public Collection<GenericValue> getProjectCategories() throws DataAccessException {

		return null;
	}

	@Override
	public GenericValue getProjectCategory(final Long arg0) throws DataAccessException {

		return null;
	}

	@Override
	public GenericValue getProjectCategoryByName(final String arg0) throws DataAccessException {

		return null;
	}

	@Override
	public GenericValue getProjectCategoryByNameIgnoreCase(final String arg0) {

		return null;
	}

	@Override
	public ProjectCategory getProjectCategoryForProject(final Project arg0) throws DataAccessException {

		return null;
	}

	@Override
	public GenericValue getProjectCategoryFromProject(final GenericValue arg0) throws DataAccessException {

		return null;
	}

	@Override
	public ProjectCategory getProjectCategoryObject(final Long arg0) throws DataAccessException {

		return null;
	}

	@Override
	public ProjectCategory getProjectCategoryObjectByName(final String arg0) {

		return null;
	}

	@Override
	public ProjectCategory getProjectCategoryObjectByNameIgnoreCase(final String arg0) {

		return null;
	}

	@Override
	public Project getProjectObj(final Long id) throws DataAccessException {
		for (final Project p : jiraProjects) {
			if (p.getId().equals(id)) {
				return p;
			}
		}
		return null;
	}

	@Override
	public Project getProjectObjByKey(final String arg0) {

		return null;
	}

	@Override
	public Project getProjectObjByKeyIgnoreCase(final String arg0) {

		return null;
	}

	@Override
	public Project getProjectObjByName(final String arg0) {

		return null;
	}

	@Override
	public Collection<Project> getProjectObjectsFromProjectCategory(final Long arg0) throws DataAccessException {

		return null;
	}

	@Override
	public Collection<Project> getProjectObjectsWithNoCategory() throws DataAccessException {

		return null;
	}

	@Override
	public Collection<GenericValue> getProjects() throws DataAccessException {

		return null;
	}

	@Override
	public Collection<GenericValue> getProjectsByLead(final User arg0) {

		return null;
	}

	@Override
	public Collection<GenericValue> getProjectsFromProjectCategory(final GenericValue arg0) throws DataAccessException {

		return null;
	}

	@Override
	public Collection<Project> getProjectsFromProjectCategory(final ProjectCategory arg0) throws DataAccessException {

		return null;
	}

	@Override
	public List<Project> getProjectsLeadBy(final User arg0) {

		return null;
	}

	@Override
	public Collection<GenericValue> getProjectsWithNoCategory() throws DataAccessException {

		return null;
	}

	@Override
	public boolean isDefaultAssignee(final GenericValue arg0) {

		return false;
	}

	@Override
	public boolean isDefaultAssignee(final GenericValue arg0, final GenericValue arg1) {

		return false;
	}

	@Override
	public void refresh() {

	}

	@Override
	public void removeProject(final Project arg0) {

	}

	@Override
	public void removeProjectCategory(final Long arg0) {

	}

	@Override
	public void removeProjectIssues(final Project arg0) throws RemoveException {

	}

	@Override
	public void setCurrentCounterForProject(final Project arg0, final long arg1) {

	}

	@Override
	public void setProjectCategory(final GenericValue arg0, final GenericValue arg1) throws DataAccessException {

	}

	@Override
	public void setProjectCategory(final Project arg0, final ProjectCategory arg1) throws DataAccessException {

	}

	@Override
	public Project updateProject(final Project arg0, final String arg1, final String arg2, final String arg3,
			final String arg4, final Long arg5) {

		return null;
	}

	@Override
	public Project updateProject(final Project arg0, final String arg1, final String arg2, final String arg3,
			final String arg4, final Long arg5, final Long arg6) {

		return null;
	}

	@Override
	public void updateProjectCategory(final GenericValue arg0) throws DataAccessException {

	}

	@Override
	public void updateProjectCategory(final ProjectCategory arg0) throws DataAccessException {

	}

	@Override
	public Set<String> getAllProjectKeys(final Long arg0) {
		return null;
	}

	@Override
	public Project getProjectByCurrentKey(final String arg0) {
		return null;
	}

	@Override
	public Project getProjectByCurrentKeyIgnoreCase(final String arg0) {
		return null;
	}

	@Override
	public long getProjectCount() throws DataAccessException {
		return 0;
	}

	@Override
	public List<Project> getProjectsLeadBy(final ApplicationUser arg0) {
		return null;
	}

	@Override
	public Project updateProject(final Project arg0, final String arg1, final String arg2, final String arg3,
			final String arg4, final Long arg5, final Long arg6, final String arg7) {
		return null;
	}

}
