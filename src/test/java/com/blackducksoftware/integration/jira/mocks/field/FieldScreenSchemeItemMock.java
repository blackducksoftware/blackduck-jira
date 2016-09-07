package com.blackducksoftware.integration.jira.mocks.field;

import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItem;
import com.atlassian.jira.issue.operation.ScreenableIssueOperation;

public class FieldScreenSchemeItemMock implements FieldScreenSchemeItem {

	private ScreenableIssueOperation issueOperation;

	private FieldScreen fieldScreen;

	@Override
	public int compareTo(final FieldScreenSchemeItem o) {

		return 0;
	}

	@Override
	public FieldScreen getFieldScreen() {

		return fieldScreen;
	}

	@Override
	public Long getFieldScreenId() {

		return null;
	}

	@Override
	public FieldScreenScheme getFieldScreenScheme() {

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
	public ScreenableIssueOperation getIssueOperation() {
		return issueOperation;
	}

	@Override
	public String getIssueOperationName() {

		return null;
	}

	@Override
	public void remove() {


	}

	@Override
	public void setFieldScreen(final FieldScreen fieldScreen) {
		this.fieldScreen = fieldScreen;
	}

	@Override
	public void setFieldScreenScheme(final FieldScreenScheme arg0) {


	}

	@Override
	public void setGenericValue(final GenericValue arg0) {


	}

	@Override
	public void setIssueOperation(final ScreenableIssueOperation issueOperation) {
		this.issueOperation = issueOperation;

	}

	@Override
	public void store() {

	}

}
