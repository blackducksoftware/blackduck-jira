/**
 * Hub JIRA Plugin
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
import java.util.List;

import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;

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
        fields.parallelStream().filter((field) -> field.equals(fieldToUpdate))
                .forEach((field) -> fieldsToMakeOptional.add(field));
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

    @Override
    public List<FieldLayoutItem> getVisibleLayoutItems(final ApplicationUser arg0, final Project arg1,
            final List<String> arg2) {
        // TODO Auto-generated method stub
        return null;
    }

}
