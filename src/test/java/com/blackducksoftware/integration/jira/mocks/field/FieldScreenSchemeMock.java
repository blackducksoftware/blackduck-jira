package com.blackducksoftware.integration.jira.mocks.field;

import java.util.Collection;

import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItem;
import com.atlassian.jira.issue.operation.IssueOperation;

public class FieldScreenSchemeMock implements FieldScreenScheme {

	private String name;

	private boolean attemptedScreenSchemeStore;

	public boolean getAttemptedScreenSchemeStore() {
		return attemptedScreenSchemeStore;
	}

	@Override
	public void addFieldScreenSchemeItem(final FieldScreenSchemeItem arg0) {


	}

	@Override
	public String getDescription() {

		return null;
	}

	@Override
	public FieldScreen getFieldScreen(final IssueOperation arg0) {

		return null;
	}

	@Override
	public FieldScreenSchemeItem getFieldScreenSchemeItem(final IssueOperation arg0) {

		return null;
	}

	@Override
	public Collection<FieldScreenSchemeItem> getFieldScreenSchemeItems() {

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
	public void remove() {


	}

	@Override
	public FieldScreenSchemeItem removeFieldScreenSchemeItem(final IssueOperation arg0) {

		return null;
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
	public void setName(final String name) {
		this.name = name;

	}

	@Override
	public void store() {
		attemptedScreenSchemeStore = true;
	}

}
