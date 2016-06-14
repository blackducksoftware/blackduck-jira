package com.blackducksoftware.integration.jira.mocks;

import java.util.Collection;

import org.ofbiz.core.entity.GenericValue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.project.version.Version;

public class ProjectMock implements Project {

	private String name;
	private Long id;

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
	public Collection<IssueType> getIssueTypes() {

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

}
