/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
import java.util.Collection;
import java.util.List;

import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItem;

public class FieldScreenSchemeManagerMock implements com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager {

    private final List<FieldScreenScheme> updatedSchemes = new ArrayList<>();

    private final List<FieldScreenSchemeItem> updatedSchemeItems = new ArrayList<>();

    public List<FieldScreenScheme> getUpdatedSchemes() {
        return updatedSchemes;
    }

    public List<FieldScreenSchemeItem> getUpdatedSchemeItems() {
        return updatedSchemeItems;
    }

    @Override
    public void createFieldScreenScheme(final FieldScreenScheme arg0) {

    }

    @Override
    public void createFieldScreenSchemeItem(final FieldScreenSchemeItem arg0) {

    }

    @Override
    public FieldScreenScheme getFieldScreenScheme(final Long arg0) {

        return null;
    }

    @Override
    public Collection<FieldScreenSchemeItem> getFieldScreenSchemeItems(final FieldScreenScheme screenScheme) {

        return screenScheme.getFieldScreenSchemeItems();
    }

    @Override
    public Collection<FieldScreenScheme> getFieldScreenSchemes() {

        return updatedSchemes;
    }

    @Override
    public Collection<FieldScreenScheme> getFieldScreenSchemes(final FieldScreen arg0) {

        return null;
    }

    @Override
    public void refresh() {

    }

    @Override
    public void removeFieldSchemeItems(final FieldScreenScheme arg0) {

    }

    @Override
    public void removeFieldScreenScheme(final FieldScreenScheme arg0) {

    }

    @Override
    public void removeFieldScreenSchemeItem(final FieldScreenSchemeItem arg0) {

    }

    @Override
    public void updateFieldScreenScheme(final FieldScreenScheme scheme) {
        updatedSchemes.add(scheme);
    }

    @Override
    public void updateFieldScreenSchemeItem(final FieldScreenSchemeItem schemeItem) {
        updatedSchemeItems.add(schemeItem);

    }

}
