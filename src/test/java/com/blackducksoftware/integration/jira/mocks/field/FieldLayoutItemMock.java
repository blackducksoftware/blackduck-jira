package com.blackducksoftware.integration.jira.mocks.field;

import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;

public class FieldLayoutItemMock implements FieldLayoutItem {

	private OrderableField orderableField;

	private boolean isRequired;

	@Override
	public int compareTo(final FieldLayoutItem o) {

		return 0;
	}

	@Override
	public String getFieldDescription() {

		return null;
	}

	@Override
	public FieldLayout getFieldLayout() {

		return null;
	}

	@Override
	public OrderableField getOrderableField() {

		return orderableField;
	}

	public void setOrderableField(final OrderableField orderableField) {
		this.orderableField = orderableField;
	}

	@Override
	public String getRawFieldDescription() {

		return null;
	}

	@Override
	public String getRendererType() {

		return null;
	}

	@Override
	public boolean isHidden() {

		return false;
	}

	@Override
	public boolean isRequired() {

		return isRequired;
	}

	public void setIsRequired(final boolean isRequired) {
		this.isRequired = isRequired;
	}

}
