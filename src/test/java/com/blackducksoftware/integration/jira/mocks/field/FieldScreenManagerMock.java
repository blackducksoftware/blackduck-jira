package com.blackducksoftware.integration.jira.mocks.field;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;

public class FieldScreenManagerMock implements FieldScreenManager {

	private final List<FieldScreenTab> updatedTabs = new ArrayList<>();

	private final List<FieldScreen> updatedScreens = new ArrayList<>();

	public List<FieldScreenTab> getUpdatedTabs() {
		return updatedTabs;
	}

	public List<FieldScreen> getUpdatedScreens() {
		return updatedScreens;
	}

	@Override
	public FieldScreenLayoutItem buildNewFieldScreenLayoutItem(final String arg0) {

		return null;
	}

	@Override
	public void createFieldScreen(final FieldScreen arg0) {


	}

	@Override
	public void createFieldScreenLayoutItem(final FieldScreenLayoutItem arg0) {


	}

	@Override
	public void createFieldScreenTab(final FieldScreenTab arg0) {


	}

	@Override
	public FieldScreen getFieldScreen(final Long arg0) {

		return null;
	}

	@Override
	public List<FieldScreenLayoutItem> getFieldScreenLayoutItems(final FieldScreenTab arg0) {

		return null;
	}

	@Override
	public FieldScreenTab getFieldScreenTab(final Long arg0) {

		return null;
	}

	@Override
	public Collection<FieldScreenTab> getFieldScreenTabs(final String arg0) {

		return null;
	}

	@Override
	public List<FieldScreenTab> getFieldScreenTabs(final FieldScreen arg0) {

		return null;
	}

	@Override
	public Collection<FieldScreen> getFieldScreens() {

		return null;
	}

	@Override
	public void refresh() {


	}

	@Override
	public void removeFieldScreen(final Long arg0) {


	}

	@Override
	public void removeFieldScreenItems(final String arg0) {


	}

	@Override
	public void removeFieldScreenLayoutItem(final FieldScreenLayoutItem arg0) {


	}

	@Override
	public void removeFieldScreenLayoutItems(final FieldScreenTab arg0) {


	}

	@Override
	public void removeFieldScreenTab(final Long arg0) {


	}

	@Override
	public void removeFieldScreenTabs(final FieldScreen arg0) {


	}

	@Override
	public void updateFieldScreen(final FieldScreen screen) {
		updatedScreens.add(screen);

	}

	@Override
	public void updateFieldScreenLayoutItem(final FieldScreenLayoutItem arg0) {


	}

	@Override
	public void updateFieldScreenTab(final FieldScreenTab tab) {
		updatedTabs.add(tab);

	}

}
