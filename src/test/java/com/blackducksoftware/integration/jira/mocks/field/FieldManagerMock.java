package com.blackducksoftware.integration.jira.mocks.field;

import java.util.List;
import java.util.Set;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.ConfigurableField;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.HideableField;
import com.atlassian.jira.issue.fields.IssueTypeField;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.ProjectField;
import com.atlassian.jira.issue.fields.RequirableField;
import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.jql.context.QueryContext;

public class FieldManagerMock implements FieldManager {

	@Override
	public Set<NavigableField> getAllAvailableNavigableFields() throws FieldException {

		return null;
	}

	@Override
	public Set<SearchableField> getAllSearchableFields() {

		return null;
	}

	@Override
	public Set<CustomField> getAvailableCustomFields(final User arg0, final Issue arg1) throws FieldException {

		return null;
	}

	@Override
	public Set<NavigableField> getAvailableNavigableFields(final User arg0) throws FieldException {

		return null;
	}

	@Override
	public Set<NavigableField> getAvailableNavigableFieldsWithScope(final User arg0) throws FieldException {

		return null;
	}

	@Override
	public Set<NavigableField> getAvailableNavigableFieldsWithScope(final User arg0, final QueryContext arg1)
			throws FieldException {

		return null;
	}

	@Override
	public ColumnLayoutManager getColumnLayoutManager() {

		return null;
	}

	@Override
	public ConfigurableField getConfigurableField(final String arg0) {

		return null;
	}

	@Override
	public CustomField getCustomField(final String arg0) {

		return null;
	}

	@Override
	public Field getField(final String arg0) {

		return null;
	}

	@Override
	public FieldLayoutManager getFieldLayoutManager() {

		return null;
	}

	@Override
	public HideableField getHideableField(final String arg0) {

		return null;
	}

	@Override
	public IssueTypeField getIssueTypeField() {

		return null;
	}

	@Override
	public NavigableField getNavigableField(final String arg0) {

		return null;
	}

	@Override
	public Set<NavigableField> getNavigableFields() {

		return null;
	}

	@Override
	public OrderableField getOrderableField(final String fieldName) {
		final List<CustomField> customFields = CustomFieldManagerMock.getCustomFields();
		if (customFields != null && !customFields.isEmpty()) {
			for (final CustomField field : customFields) {
				if (field.getName().equals(fieldName)) {
					return field;
				}
			}
		}
		return null;
	}

	@Override
	public Set<OrderableField> getOrderableFields() {

		return null;
	}

	@Override
	public ProjectField getProjectField() {

		return null;
	}

	@Override
	public RequirableField getRequiredField(final String arg0) {

		return null;
	}

	@Override
	public Set<SearchableField> getSystemSearchableFields() {

		return null;
	}

	@Override
	public Set<Field> getUnavailableFields() {

		return null;
	}

	@Override
	public Set<FieldLayout> getVisibleFieldLayouts(final User arg0) {

		return null;
	}

	@Override
	public boolean isCustomField(final String arg0) {

		return false;
	}

	@Override
	public boolean isCustomField(final Field arg0) {

		return false;
	}

	@Override
	public boolean isFieldHidden(final User arg0, final Field arg1) {

		return false;
	}

	@Override
	public boolean isFieldHidden(final User arg0, final String arg1) {

		return false;
	}

	@Override
	public boolean isFieldHidden(final Set<FieldLayout> arg0, final Field arg1) {

		return false;
	}

	@Override
	public boolean isHideableField(final String arg0) {

		return false;
	}

	@Override
	public boolean isHideableField(final Field arg0) {

		return false;
	}

	@Override
	public boolean isMandatoryField(final String arg0) {

		return false;
	}

	@Override
	public boolean isMandatoryField(final Field arg0) {

		return false;
	}

	@Override
	public boolean isNavigableField(final String arg0) {

		return false;
	}

	@Override
	public boolean isNavigableField(final Field arg0) {

		return false;
	}

	@Override
	public boolean isOrderableField(final String arg0) {

		return false;
	}

	@Override
	public boolean isOrderableField(final Field arg0) {

		return false;
	}

	@Override
	public boolean isRenderableField(final String arg0) {

		return false;
	}

	@Override
	public boolean isRenderableField(final Field arg0) {

		return false;
	}

	@Override
	public boolean isRequirableField(final String arg0) {

		return false;
	}

	@Override
	public boolean isRequirableField(final Field arg0) {

		return false;
	}

	@Override
	public boolean isTimeTrackingOn() {

		return false;
	}

	@Override
	public boolean isUnscreenableField(final String arg0) {

		return false;
	}

	@Override
	public boolean isUnscreenableField(final Field arg0) {

		return false;
	}

	@Override
	public void refresh() {


	}

}
