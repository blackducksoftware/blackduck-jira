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
package com.blackducksoftware.integration.jira.mocks.issue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.blackducksoftware.integration.jira.mocks.ConstantsManagerMock;

public class IssueTypeSchemeManagerMock implements IssueTypeSchemeManager {
	private FieldConfigScheme fieldConfigScheme;
	private Collection<IssueType> issueTypes;

	private final ConstantsManagerMock constantsManagerMock;

	public IssueTypeSchemeManagerMock(final ConstantsManagerMock constantsManagerMock) {
		this.constantsManagerMock = constantsManagerMock;
	}

	@Override
	public void addOptionToDefault(final String arg0) {
	}

	@Override
	public FieldConfigScheme create(final String arg0, final String arg1, final List arg2) {
		return null;
	}

	@Override
	public void deleteScheme(final FieldConfigScheme arg0) {
	}

	@Override
	public Collection getAllRelatedSchemes(final String arg0) {
		return null;
	}

	@Override
	public List<FieldConfigScheme> getAllSchemes() {
		return null;
	}

	@Override
	public FieldConfigScheme getConfigScheme(final GenericValue arg0) {
		return null;
	}

	@Override
	public FieldConfigScheme getConfigScheme(final Project arg0) {
		return fieldConfigScheme;
	}

	public void setConfigScheme(final FieldConfigScheme fieldConfigScheme) {
		this.fieldConfigScheme = fieldConfigScheme;
	}

	@Override
	public IssueType getDefaultIssueType(final Project arg0) {
		return null;
	}

	@Override
	public FieldConfigScheme getDefaultIssueTypeScheme() {
		return null;
	}

	@Override
	public IssueType getDefaultValue(final Issue arg0) {
		return null;
	}

	@Override
	public IssueType getDefaultValue(final FieldConfig arg0) {
		return null;
	}

	@Override
	public IssueType getDefaultValue(final GenericValue arg0) {
		return null;
	}

	@Override
	public Collection<IssueType> getIssueTypesForDefaultScheme() {
		return null;
	}

	@Override
	public Collection<IssueType> getIssueTypesForProject(final GenericValue arg0) {
		return null;
	}

	@Override
	public Collection<IssueType> getIssueTypesForProject(final Project arg0) {
		final List<IssueType> issues = new ArrayList<>();
		issues.addAll(issueTypes);
		issues.addAll(constantsManagerMock.getAllIssueTypeObjects());

		return issueTypes;
	}

	public void setIssueTypes(final Collection<IssueType> issueTypes) {
		this.issueTypes = issueTypes;
	}

	@Override
	public Collection<IssueType> getNonSubTaskIssueTypesForProject(final Project arg0) {
		return null;
	}

	@Override
	public Collection<IssueType> getSubTaskIssueTypesForProject(final Project arg0) {
		return null;
	}

	@Override
	public boolean isDefaultIssueTypeScheme(final FieldConfigScheme arg0) {
		return false;
	}

	@Override
	public void removeOptionFromAllSchemes(final String arg0) {

	}

	@Override
	public void setDefaultValue(final FieldConfig arg0, final String arg1) {

	}

	@Override
	public FieldConfigScheme update(final FieldConfigScheme arg0, final Collection arg1) {
		return null;
	}

}
