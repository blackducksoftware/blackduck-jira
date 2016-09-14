package com.blackducksoftware.integration.jira.mocks.issue;

import java.util.Collection;

import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntity;
import com.atlassian.jira.issue.issuetype.IssueType;

public class IssueTypeScreenSchemeMock implements IssueTypeScreenScheme {

	public IssueTypeScreenSchemeMock() {
	}

	@Override
	public void addEntity(final IssueTypeScreenSchemeEntity arg0) {

	}

	@Override
	public boolean containsEntity(final String arg0) {
		return false;
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public FieldScreenScheme getEffectiveFieldScreenScheme(final IssueType arg0) {
		return null;
	}

	@Override
	public Collection<IssueTypeScreenSchemeEntity> getEntities() {
		return null;
	}

	@Override
	public IssueTypeScreenSchemeEntity getEntity(final String arg0) {
		return null;
	}

	@Override
	public GenericValue getGenericValue() {
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
	public Collection<GenericValue> getProjects() {
		return null;
	}

	@Override
	public boolean isDefault() {
		return false;
	}

	@Override
	public void remove() {

	}

	@Override
	public void removeEntity(final String arg0) {

	}

	@Override
	public void setDescription(final String arg0) {

	}

	@Override
	public void setGenericValue(final GenericValue arg0) {

	}

	@Override
	public void setId(final Long arg0) {

	}

	@Override
	public void setName(final String arg0) {

	}

	@Override
	public void store() {

	}

}
