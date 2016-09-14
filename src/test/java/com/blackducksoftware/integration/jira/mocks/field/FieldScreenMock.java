package com.blackducksoftware.integration.jira.mocks.field;

import java.util.ArrayList;
import java.util.List;

import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;

public class FieldScreenMock implements FieldScreen {

	private String name;

	private boolean attemptedScreenStore;

	private final List<FieldScreenTab> tabs = new ArrayList<>();

	public boolean getAttemptedScreenStore() {
		return attemptedScreenStore;
	}

	public FieldScreenTab addTab(final FieldScreenTab tab) {
		tabs.add(tab);
		return tab;
	}

	@Override
	public FieldScreenTab addTab(final String tabName) {
		final FieldScreenTabMock tab = new FieldScreenTabMock();
		tab.setName(tabName);
		tab.setFieldScreen(this);
		tabs.add(tab);
		return tab;
	}

	@Override
	public boolean containsField(final String arg0) {

		return false;
	}

	@Override
	public String getDescription() {

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
	public FieldScreenTab getTab(final int arg0) {

		return null;
	}

	@Override
	public List<FieldScreenTab> getTabs() {

		return tabs;
	}

	@Override
	public boolean isModified() {

		return false;
	}

	@Override
	public void moveFieldScreenTabLeft(final int arg0) {


	}

	@Override
	public void moveFieldScreenTabRight(final int arg0) {


	}

	@Override
	public void moveFieldScreenTabToPosition(final int arg0, final int arg1) {


	}

	@Override
	public void remove() {


	}

	@Override
	public void removeFieldScreenLayoutItem(final String arg0) {


	}

	@Override
	public void removeTab(final int arg0) {


	}

	@Override
	public void resequence() {


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
		attemptedScreenStore = true;

	}

}
