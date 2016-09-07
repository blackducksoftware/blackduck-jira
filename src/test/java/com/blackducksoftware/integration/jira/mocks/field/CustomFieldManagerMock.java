package com.blackducksoftware.integration.jira.mocks.field;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;

public class CustomFieldManagerMock implements CustomFieldManager {

	private static final List<CustomField> customFields = new ArrayList<>();

	public static List<CustomField> getCustomFields() {
		return customFields;
	}

	@Override
	public void clear() {

	}

	@Override
	public CustomField createCustomField(final String name, final String description, final CustomFieldType fieldType,
			final CustomFieldSearcher searcher,
			final List contexts, final List genericValues) throws GenericEntityException {
		final CustomFieldMock customField = new CustomFieldMock();
		customField.setName(name);
		customField.setDescription(description);
		customField.setCustomFieldType(fieldType);
		customField.setCustomFieldSearcher(searcher);
		customFields.add(customField);
		return customField;
	}

	@Override
	public boolean exists(final String arg0) {

		return false;
	}

	@Override
	public CustomField getCustomFieldInstance(final GenericValue arg0) {

		return null;
	}

	@Override
	public CustomField getCustomFieldObject(final Long arg0) {

		return null;
	}

	@Override
	public CustomField getCustomFieldObject(final String arg0) {

		return null;
	}

	@Override
	public CustomField getCustomFieldObjectByName(final String name) {
		if (!customFields.isEmpty()) {
			for (final CustomField field : customFields) {
				if (field.getName().equals(name)) {
					return field;
				}
			}
		}
		return null;
	}

	@Override
	public List<CustomField> getCustomFieldObjects() {

		return customFields;
	}

	@Override
	public List<CustomField> getCustomFieldObjects(final SearchContext arg0) {

		return null;
	}

	@Override
	public List<CustomField> getCustomFieldObjects(final GenericValue arg0) {

		return null;
	}

	@Override
	public List<CustomField> getCustomFieldObjects(final Issue arg0) {

		return null;
	}

	@Override
	public List<CustomField> getCustomFieldObjects(final Long arg0, final String arg1) {

		return null;
	}

	@Override
	public List<CustomField> getCustomFieldObjects(final Long arg0, final List<String> arg1) {

		return null;
	}

	@Override
	public Collection<CustomField> getCustomFieldObjectsByName(final String arg0) {

		return null;
	}

	@Override
	public CustomFieldSearcher getCustomFieldSearcher(final String arg0) {

		return null;
	}

	@Override
	public Class<? extends CustomFieldSearcher> getCustomFieldSearcherClass(final String arg0) {

		return null;
	}

	@Override
	public List<CustomFieldSearcher> getCustomFieldSearchers(final CustomFieldType arg0) {

		return null;
	}

	@Override
	public CustomFieldType getCustomFieldType(final String arg0) {

		return null;
	}

	@Override
	public List<CustomFieldType<?, ?>> getCustomFieldTypes() {

		return null;
	}

	@Override
	public CustomFieldSearcher getDefaultSearcher(final CustomFieldType<?, ?> arg0) {

		return null;
	}

	@Override
	public List<CustomField> getGlobalCustomFieldObjects() {

		return null;
	}

	@Override
	public boolean isCustomField(final String arg0) {

		return false;
	}

	@Override
	public void refresh() {


	}

	@Override
	public void refreshConfigurationSchemes(final Long arg0) {


	}

	@Override
	public void removeCustomField(final CustomField arg0) throws RemoveException {


	}

	@Override
	public void removeCustomFieldPossiblyLeavingOrphanedData(final Long arg0)
			throws RemoveException, IllegalArgumentException {


	}

	@Override
	public void removeCustomFieldValues(final GenericValue arg0) throws GenericEntityException {


	}

	@Override
	public void removeProjectAssociations(final GenericValue arg0) {


	}

	@Override
	public void removeProjectAssociations(final Project arg0) {


	}

	@Override
	public void removeProjectCategoryAssociations(final ProjectCategory arg0) {


	}

	@Override
	public void updateCustomField(final CustomField arg0) {


	}

	@Override
	public void updateCustomField(final Long arg0, final String arg1, final String arg2, final CustomFieldSearcher arg3) {


	}

}
