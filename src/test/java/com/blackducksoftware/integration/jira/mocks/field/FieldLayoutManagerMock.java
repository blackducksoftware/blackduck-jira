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
import java.util.Set;

import javax.annotation.Nonnull;

import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.layout.field.EditableDefaultFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldConfigurationScheme;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutSchemeEntity;
import com.atlassian.jira.project.Project;

public class FieldLayoutManagerMock implements FieldLayoutManager {
    private FieldLayoutScheme fieldLayoutScheme;
    private EditableDefaultFieldLayout editableDefaultFieldLayout;
    private final List<EditableFieldLayout> editableFieldLayouts = new ArrayList<>();
    private FieldConfigurationScheme projectFieldConfigScheme;
    private boolean attemptedToPersistFieldLayout;
    private List<FieldLayoutSchemeEntity> createdFieldLayoutSchemeEntities = new ArrayList<>();
    private int createdFieldLayoutSchemeEntitiesIndex = 0;

    public void setProjectFieldConfigScheme(final FieldConfigurationScheme projectFieldConfigScheme) {
        this.projectFieldConfigScheme = projectFieldConfigScheme;
    }

    public boolean getAttemptedToPersistFieldLayout() {
        return attemptedToPersistFieldLayout;
    }

    public void setCreatedFieldLayoutSchemeEntities(final Collection<FieldLayoutSchemeEntity> createdFieldLayoutSchemeEntities) {
        this.createdFieldLayoutSchemeEntities = new ArrayList<>();
        this.createdFieldLayoutSchemeEntities.addAll(createdFieldLayoutSchemeEntities);
    }

    @Override
    public void addSchemeAssociation(final GenericValue arg0, final Long arg1) {
    }

    @Override
    public void addSchemeAssociation(final Project arg0, final Long arg1) {
    }

    @Override
    public FieldLayoutScheme copyFieldLayoutScheme(final FieldLayoutScheme arg0, final String arg1, final String arg2) {
        return null;
    }

    @Override
    public FieldLayoutScheme createFieldLayoutScheme(final FieldLayoutScheme arg0) {
        return null;
    }

    @Override
    public FieldLayoutScheme createFieldLayoutScheme(final String arg0, final String arg1) {
        return fieldLayoutScheme;
    }

    public void setFieldLayoutScheme(final FieldLayoutScheme fieldLayoutScheme) {
        this.fieldLayoutScheme = fieldLayoutScheme;
    }

    @Override
    public void createFieldLayoutSchemeEntity(final FieldLayoutSchemeEntity arg0) {
        createdFieldLayoutSchemeEntitiesIndex++;
        System.out.println("createFieldLayoutSchemeEntity()");
    }

    @Override
    public FieldLayoutSchemeEntity createFieldLayoutSchemeEntity(final FieldLayoutScheme arg0, final String arg1, final Long arg2) {
        System.out.println("createFieldLayoutSchemeEntity(); returning null");
        return createdFieldLayoutSchemeEntities.get(createdFieldLayoutSchemeEntitiesIndex++);
    }

    @Override
    public void deleteFieldLayout(final FieldLayout arg0) {
    }

    @Override
    public void deleteFieldLayoutScheme(final FieldLayoutScheme arg0) {
    }

    @Override
    public boolean fieldConfigurationSchemeExists(final String arg0) {
        return false;
    }

    @Override
    public EditableDefaultFieldLayout getEditableDefaultFieldLayout() {
        return editableDefaultFieldLayout;
    }

    public void setEditableDefaultFieldLayout(final EditableDefaultFieldLayout editableDefaultFieldLayout) {
        this.editableDefaultFieldLayout = editableDefaultFieldLayout;
    }

    @Override
    public EditableFieldLayout getEditableFieldLayout(final Long arg0) {
        return null;
    }

    @Override
    public List<EditableFieldLayout> getEditableFieldLayouts() {

        return editableFieldLayouts;
    }

    public void addEditableFieldLayout(final EditableFieldLayout fieldLayout) {
        editableFieldLayouts.add(fieldLayout);
    }

    @Override
    public FieldConfigurationScheme getFieldConfigurationScheme(final Long arg0) {
        return null;
    }

    @Override
    public FieldConfigurationScheme getFieldConfigurationScheme(final Project arg0) {
        return null;
    }

    @Override
    public FieldConfigurationScheme getFieldConfigurationSchemeForProject(final Long arg0) {
        return projectFieldConfigScheme;
    }

    @Override
    public Collection<FieldConfigurationScheme> getFieldConfigurationSchemes(final FieldLayout arg0) {
        return null;
    }

    @Override
    public FieldLayout getFieldLayout() {
        return null;
    }

    @Override
    public FieldLayout getFieldLayout(final Long arg0) {
        return null;
    }

    @Override
    public FieldLayout getFieldLayout(final GenericValue arg0) {
        return null;
    }

    @Override
    public FieldLayout getFieldLayout(final Issue arg0) {
        return null;
    }

    @Override
    public FieldLayout getFieldLayout(final Project arg0, final String arg1) {
        return null;
    }

    @Override
    public FieldLayout getFieldLayout(final Long arg0, final String arg1) {
        return null;
    }

    @Override
    public Collection<FieldLayoutSchemeEntity> getFieldLayoutSchemeEntities(final FieldLayoutScheme arg0) {
        return null;
    }

    @Override
    public List<FieldLayoutScheme> getFieldLayoutSchemes() {
        return null;
    }

    @Override
    public FieldLayoutScheme getMutableFieldLayoutScheme(final Long arg0) {
        return fieldLayoutScheme;
    }

    @Override
    public Collection<GenericValue> getProjects(final FieldConfigurationScheme arg0) {
        return null;
    }

    @Override
    public Collection<GenericValue> getProjects(final FieldLayoutScheme arg0) {
        return null;
    }

    @Override
    public Collection<Project> getProjectsUsing(final FieldConfigurationScheme arg0) {
        return null;
    }

    @Override
    public Collection<Project> getProjectsUsing(final FieldLayoutScheme arg0) {
        return null;
    }

    @Override
    public Collection<Project> getProjectsUsing(final FieldLayout arg0) {
        return null;
    }

    @Override
    public Collection<GenericValue> getRelatedProjects(final FieldLayout arg0) {
        return null;
    }

    @Override
    public Set<FieldLayout> getUniqueFieldLayouts(final Project arg0) {
        return null;
    }

    @Override
    public Set<FieldLayout> getUniqueFieldLayouts(final Collection<Project> arg0, final Collection<String> arg1) {
        return null;
    }

    @Override
    public boolean hasDefaultFieldLayout() {
        return false;
    }

    @Override
    public boolean isFieldLayoutSchemesVisiblyEquivalent(final Long arg0, final Long arg1) {
        return false;
    }

    @Override
    public boolean isFieldLayoutsVisiblyEquivalent(final Long arg0, final Long arg1) {
        return false;
    }

    @Override
    public void updateFieldLayoutItemAttributesForCustomField(@Nonnull final CustomField customField, @Nonnull final String s, final boolean b, final boolean b1) {
    }

    @Override
    public void refresh() {
    }

    @Override
    public void removeFieldLayoutScheme(final FieldLayoutScheme arg0) {
    }

    @Override
    public void removeFieldLayoutSchemeEntity(final FieldLayoutSchemeEntity arg0) {
    }

    @Override
    public void removeSchemeAssociation(final GenericValue arg0, final Long arg1) {
    }

    @Override
    public void removeSchemeAssociation(final Project arg0, final Long arg1) {
    }

    @Override
    public void restoreDefaultFieldLayout() {
    }

    @Override
    public EditableFieldLayout storeAndReturnEditableFieldLayout(final EditableFieldLayout arg0) {
        return null;
    }

    @Override
    public void storeEditableDefaultFieldLayout(final EditableDefaultFieldLayout arg0) {
    }

    @Override
    public void storeEditableFieldLayout(final EditableFieldLayout arg0) {
        attemptedToPersistFieldLayout = true;
    }

    @Override
    public void updateFieldLayoutScheme(final FieldLayoutScheme arg0) {
    }

    @Override
    public void updateFieldLayoutSchemeEntity(final FieldLayoutSchemeEntity arg0) {
    }

}
