package com.blackducksoftware.integration.jira.mocks.field;

import java.util.ArrayList;
import java.util.List;

import org.ofbiz.core.entity.GenericValue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.project.Project;

public class EditableFieldLayoutMock implements EditableFieldLayout {

	private List<FieldLayoutItem> fields = new ArrayList<>();
	private String name;

	private final List<FieldLayoutItem> fieldsToMakeOptional = new ArrayList<>();

	public List<FieldLayoutItem> getFieldsToMakeOptional() {
		return fieldsToMakeOptional;
	}

	@Override
	public String getDescription() {

		return null;
	}

	@Override
	public FieldLayoutItem getFieldLayoutItem(final OrderableField arg0) {

		return null;
	}

	@Override
	public FieldLayoutItem getFieldLayoutItem(final String arg0) {

		return null;
	}

	@Override
	public List<FieldLayoutItem> getFieldLayoutItems() {

		return fields;
	}

	public void setFieldLayoutItems(final List<FieldLayoutItem> fields) {
		this.fields = fields;
	}

	public void addFieldLayoutItem(final FieldLayoutItem field) {
		this.fields.add(field);
	}

	@Override
	public GenericValue getGenericValue() {

		return null;
	}

	@Override
	public List<Field> getHiddenFields(final Project arg0, final List<String> arg1) {

		return null;
	}

	@Override
	public List<Field> getHiddenFields(final User arg0, final GenericValue arg1, final List<String> arg2) {

		return null;
	}

	@Override
	public List<Field> getHiddenFields(final User arg0, final Project arg1, final List<String> arg2) {

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
	public void setName(final String name) {
		this.name = name;

	}

	@Override
	public String getRendererTypeForField(final String arg0) {

		return null;
	}

	@Override
	public List<FieldLayoutItem> getRequiredFieldLayoutItems(final Project arg0, final List<String> arg1) {

		return null;
	}

	@Override
	public List<FieldLayoutItem> getVisibleCustomFieldLayoutItems(final Project arg0, final List<String> arg1) {

		return null;
	}

	@Override
	public List<FieldLayoutItem> getVisibleLayoutItems(final Project arg0, final List<String> arg1) {

		return null;
	}

	@Override
	public List<FieldLayoutItem> getVisibleLayoutItems(final User arg0, final Project arg1, final List<String> arg2) {

		return null;
	}

	@Override
	public boolean isDefault() {

		return false;
	}

	@Override
	public boolean isFieldHidden(final String arg0) {

		return false;
	}

	@Override
	public String getType() {

		return null;
	}

	@Override
	public void hide(final FieldLayoutItem arg0) {


	}

	@Override
	public void makeOptional(final FieldLayoutItem fieldToUpdate) {
		for (final FieldLayoutItem field : fields) {
			if (field.equals(fieldToUpdate)) {
				fieldsToMakeOptional.add(field);
			}
		}

	}

	@Override
	public void makeRequired(final FieldLayoutItem arg0) {


	}

	@Override
	public void setDescription(final String arg0) {


	}

	@Override
	public void setDescription(final FieldLayoutItem arg0, final String arg1) {


	}

	@Override
	public void setRendererType(final FieldLayoutItem arg0, final String arg1) {


	}

	@Override
	public void show(final FieldLayoutItem arg0) {


	}

}
