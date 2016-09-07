package com.blackducksoftware.integration.jira.mocks;

import java.util.Collection;
import java.util.List;

import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;

public class IssueTypeSchemeManagerMock implements IssueTypeSchemeManager {
	private FieldConfigScheme fieldConfigScheme;
	private Collection<IssueType> issueTypes;

	public IssueTypeSchemeManagerMock() {
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
