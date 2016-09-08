package com.blackducksoftware.integration.jira.mocks.field;

import java.util.Collection;

import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutSchemeEntity;
import com.atlassian.jira.project.Project;

public class FieldLayoutSchemeMock implements FieldLayoutScheme {

	private String name;

	@Override
	public void addEntity(final FieldLayoutSchemeEntity arg0) {

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
	public Collection<FieldLayoutSchemeEntity> getEntities() {
		return null;
	}

	@Override
	public FieldLayoutSchemeEntity getEntity(final String arg0) {
		return null;
	}

	@Override
	public FieldLayoutSchemeEntity getEntity(final EditableFieldLayout arg0) {
		return null;
	}

	@Override
	public Long getFieldLayoutId(final String arg0) {
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
		return name;
	}

	@Override
	public Collection<GenericValue> getProjects() {
		return null;
	}

	@Override
	public Collection<Project> getProjectsUsing() {
		return null;
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
	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public void store() {

	}

}
