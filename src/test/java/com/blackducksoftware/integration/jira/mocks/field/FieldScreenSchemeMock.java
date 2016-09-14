package com.blackducksoftware.integration.jira.mocks.field;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItem;
import com.atlassian.jira.issue.operation.IssueOperation;

public class FieldScreenSchemeMock implements FieldScreenScheme {

	private String name;

	private boolean attemptedScreenSchemeStore;

	private final List<FieldScreenSchemeItem> schemeItems = new ArrayList<>();

	public boolean getAttemptedScreenSchemeStore() {
		return attemptedScreenSchemeStore;
	}

	@Override
	public void addFieldScreenSchemeItem(final FieldScreenSchemeItem schemeItem) {
		schemeItems.add(schemeItem);
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
	public FieldScreenSchemeItem getFieldScreenSchemeItem(final IssueOperation issueOperation) {
		for(final FieldScreenSchemeItem schemeItem : schemeItems){
			if (schemeItem.getIssueOperation().getNameKey().equals(issueOperation.getNameKey())) {
				return schemeItem;
			}
		}
		return null;
	}

	@Override
	public Collection<FieldScreenSchemeItem> getFieldScreenSchemeItems() {

		return schemeItems;
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
