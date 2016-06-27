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
