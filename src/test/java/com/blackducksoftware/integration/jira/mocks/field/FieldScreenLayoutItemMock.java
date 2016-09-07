package com.blackducksoftware.integration.jira.mocks.field;

import java.util.Map;

import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;

import webwork.action.Action;

public class FieldScreenLayoutItemMock implements FieldScreenLayoutItem {

	private Long id;

	private OrderableField field;

	@Override
	public String getCreateHtml(final FieldLayoutItem arg0, final OperationContext arg1, final Action arg2, final Issue arg3) {

		return null;
	}

	@Override
	public String getCreateHtml(final FieldLayoutItem arg0, final OperationContext arg1, final Action arg2, final Issue arg3,
			final Map<String, Object> arg4) {

		return null;
	}

	@Override
	public String getEditHtml(final FieldLayoutItem arg0, final OperationContext arg1, final Action arg2, final Issue arg3) {

		return null;
	}

	@Override
	public String getEditHtml(final FieldLayoutItem arg0, final OperationContext arg1, final Action arg2, final Issue arg3,
			final Map<String, Object> arg4) {

		return null;
	}

	@Override
	public String getFieldId() {

		return null;
	}

	@Override
	public FieldScreenTab getFieldScreenTab() {

		return null;
	}

	@Override
	public GenericValue getGenericValue() {

		return null;
	}

	@Override
	public Long getId() {

		return id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	@Override
	public OrderableField getOrderableField() {

		return field;
	}

	public void setOrderableField(final OrderableField field) {
		this.field = field;
	}

	@Override
	public int getPosition() {

		return 0;
	}

	@Override
	public String getViewHtml(final FieldLayoutItem arg0, final OperationContext arg1, final Action arg2, final Issue arg3) {

		return null;
	}

	@Override
	public String getViewHtml(final FieldLayoutItem arg0, final OperationContext arg1, final Action arg2, final Issue arg3,
			final Map<String, Object> arg4) {

		return null;
	}

	@Override
	public boolean isShown(final Issue arg0) {

		return false;
	}

	@Override
	public void remove() {


	}

	@Override
	public void setFieldId(final String arg0) {


	}

	@Override
	public void setFieldScreenTab(final FieldScreenTab arg0) {


	}

	@Override
	public void setGenericValue(final GenericValue arg0) {


	}

	@Override
	public void setPosition(final int arg0) {


	}

	@Override
	public void store() {


	}

}
