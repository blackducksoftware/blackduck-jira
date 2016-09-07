package com.blackducksoftware.integration.jira.mocks.field;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;

public class FieldScreenTabMock implements FieldScreenTab {

	private String name;

	private final List<FieldScreenLayoutItem> layoutItems = new ArrayList<>();

	@Override
	public void addFieldScreenLayoutItem(final String fieldId) {
		final FieldScreenLayoutItemMock layoutItem = new FieldScreenLayoutItemMock();
		final OrderableFieldMock field = new OrderableFieldMock();
		field.setId(fieldId);
		layoutItem.setOrderableField(field);
		layoutItems.add(layoutItem);

	}

	@Override
	public void addFieldScreenLayoutItem(final String arg0, final int arg1) {


	}

	@Override
	public FieldScreen getFieldScreen() {

		return null;
	}

	@Override
	public FieldScreenLayoutItem getFieldScreenLayoutItem(final int arg0) {

		return null;
	}

	@Override
	public FieldScreenLayoutItem getFieldScreenLayoutItem(final String arg0) {

		return null;
	}

	@Override
	public List<FieldScreenLayoutItem> getFieldScreenLayoutItems() {

		return layoutItems;
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
	public int getPosition() {

		return 0;
	}

	@Override
	public boolean isContainsField(final String arg0) {

		return false;
	}

	@Override
	public boolean isModified() {

		return false;
	}

	@Override
	public void moveFieldScreenLayoutItemDown(final int arg0) {


	}

	@Override
	public void moveFieldScreenLayoutItemFirst(final int arg0) {


	}

	@Override
	public void moveFieldScreenLayoutItemLast(final int arg0) {


	}

	@Override
	public void moveFieldScreenLayoutItemToPosition(final Map<Integer, FieldScreenLayoutItem> arg0) {


	}

	@Override
	public void moveFieldScreenLayoutItemUp(final int arg0) {


	}

	@Override
	public void remove() {


	}

	@Override
	public FieldScreenLayoutItem removeFieldScreenLayoutItem(final int arg0) {

		return null;
	}

	@Override
	public void rename(final String arg0) {


	}

	@Override
	public void setFieldScreen(final FieldScreen arg0) {


	}

	@Override
	public void setGenericValue(final GenericValue arg0) {


	}

	@Override
	public void setName(final String name) {
		this.name = name;

	}

	@Override
	public void setPosition(final int arg0) {


	}

	@Override
	public void store() {


	}

}
