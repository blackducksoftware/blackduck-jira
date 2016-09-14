package com.blackducksoftware.integration.jira.mocks.field;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItem;

public class FieldScreenSchemeManagerMock implements com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager {

	private final List<FieldScreenScheme> updatedSchemes = new ArrayList<>();

	private final List<FieldScreenSchemeItem> updatedSchemeItems = new ArrayList<>();

	public List<FieldScreenScheme> getUpdatedSchemes() {
		return updatedSchemes;
	}

	public List<FieldScreenSchemeItem> getUpdatedSchemeItems() {
		return updatedSchemeItems;
	}

	@Override
	public void createFieldScreenScheme(final FieldScreenScheme arg0) {


	}

	@Override
	public void createFieldScreenSchemeItem(final FieldScreenSchemeItem arg0) {


	}

	@Override
	public FieldScreenScheme getFieldScreenScheme(final Long arg0) {

		return null;
	}

	@Override
	public Collection<FieldScreenSchemeItem> getFieldScreenSchemeItems(final FieldScreenScheme screneScheme) {

		return screneScheme.getFieldScreenSchemeItems();
	}

	@Override
	public Collection<FieldScreenScheme> getFieldScreenSchemes() {

		return updatedSchemes;
	}

	@Override
	public Collection<FieldScreenScheme> getFieldScreenSchemes(final FieldScreen arg0) {

		return null;
	}

	@Override
	public void refresh() {


	}

	@Override
	public void removeFieldSchemeItems(final FieldScreenScheme arg0) {


	}

	@Override
	public void removeFieldScreenScheme(final FieldScreenScheme arg0) {


	}

	@Override
	public void removeFieldScreenSchemeItem(final FieldScreenSchemeItem arg0) {


	}

	@Override
	public void updateFieldScreenScheme(final FieldScreenScheme scheme) {
		updatedSchemes.add(scheme);
	}

	@Override
	public void updateFieldScreenSchemeItem(final FieldScreenSchemeItem schemeItem) {
		updatedSchemeItems.add(schemeItem);

	}

}
