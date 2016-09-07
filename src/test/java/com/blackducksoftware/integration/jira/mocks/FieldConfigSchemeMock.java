package com.blackducksoftware.integration.jira.mocks;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.MultiMap;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.fields.ConfigurableField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;

public class FieldConfigSchemeMock implements FieldConfigScheme {

	public FieldConfigSchemeMock() {
	}

	@Override
	public Collection<String> getAssociatedIssueTypeIds() {
		return null;
	}

	@Override
	public Collection<IssueType> getAssociatedIssueTypeObjects() {
		return null;
	}

	@Override
	public Set<GenericValue> getAssociatedIssueTypes() {
		return null;
	}

	@Override
	public List<GenericValue> getAssociatedProjectCategories() {
		return null;
	}

	@Override
	public List<ProjectCategory> getAssociatedProjectCategoryObjects() {
		return null;
	}

	@Override
	public List<Long> getAssociatedProjectIds() {
		return null;
	}

	@Override
	public List<Project> getAssociatedProjectObjects() {
		return null;
	}

	@Override
	public List<GenericValue> getAssociatedProjects() {
		return null;
	}

	@Override
	public Map<String, FieldConfig> getConfigs() {
		return null;
	}

	@Override
	public MultiMap getConfigsByConfig() {
		return null;
	}

	@Override
	public List<JiraContextNode> getContexts() {
		return null;
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public ConfigurableField getField() {
		return null;
	}

	@Override
	public Long getId() {
		return null;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public FieldConfig getOneAndOnlyConfig() {
		return null;
	}

	@Override
	public boolean isAllIssueTypes() {
		return false;
	}

	@Override
	public boolean isAllProjects() {
		return false;
	}

	@Override
	public boolean isBasicMode() {
		return false;
	}

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public boolean isGlobal() {
		return false;
	}

	@Override
	public boolean isInContext(final IssueContext arg0) {
		return false;
	}

}
