/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2020 Synopsys, Inc.
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
import java.util.List;

import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.EditableDefaultFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;

public class EditableDefaultFieldLayoutMock implements EditableDefaultFieldLayout {

    private final List<FieldLayoutItem> fieldLayoutItems = new ArrayList<>();

    @Override
    public String getType() {

        return null;
    }

    @Override
    public void hide(final FieldLayoutItem arg0) {

    }

    @Override
    public void makeOptional(final FieldLayoutItem arg0) {

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
    public void setName(final String arg0) {

    }

    @Override
    public void setRendererType(final FieldLayoutItem arg0, final String arg1) {

    }

    @Override
    public void show(final FieldLayoutItem arg0) {

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

        return fieldLayoutItems;
    }

    public void addFieldLayoutItem(final FieldLayoutItem fieldLayoutItem) {
        fieldLayoutItems.add(fieldLayoutItem);
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

        return null;
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
    public List<FieldLayoutItem> getVisibleLayoutItems(final ApplicationUser arg0, final Project arg1,
            final List<String> arg2) {
        // Auto-generated method stub
        return null;
    }

}
