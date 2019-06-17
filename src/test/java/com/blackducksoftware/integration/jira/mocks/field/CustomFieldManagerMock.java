/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2019 Synopsys, Inc.
 * https://www.synopsys.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.jira.mocks.field;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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

@SuppressWarnings("rawtypes")
public class CustomFieldManagerMock implements CustomFieldManager {
    private final List<CustomField> customFields = new ArrayList<>();

    public List<CustomField> getCustomFields() {
        return customFields;
    }

    @Override
    public void clear() {

    }

    @Override
    public CustomField createCustomField(final String name, final String description, final CustomFieldType fieldType, final CustomFieldSearcher searcher, final List contexts, final List genericValues) throws GenericEntityException {
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
        return customFields.parallelStream().filter((field) -> field.getName().equals(name)).findAny().orElse(null);
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
    public Collection<CustomField> getCustomFieldObjectsByName(final String name) {
        return customFields.parallelStream().filter((field) -> field.getName().equals(name)).collect(Collectors.toList());
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
    public void removeCustomField(final CustomField field) throws RemoveException {
        customFields.remove(field);
    }

    @Override
    public void removeCustomFieldPossiblyLeavingOrphanedData(final Long arg0)
        throws RemoveException, IllegalArgumentException {

    }

    @Override
    public void removeCustomFieldValues(final GenericValue arg0) throws GenericEntityException {
    }

    @Override
    public void removeProjectAssociations(final Project arg0) {
    }

    @Override
    public void updateCustomField(final Long arg0, final String arg1, final String arg2, final CustomFieldSearcher arg3) {
    }

}
