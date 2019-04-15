/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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

import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItem;
import com.atlassian.jira.issue.operation.IssueOperation;

public class FieldScreenSchemeMock implements FieldScreenScheme {

    private String name;

    private boolean attemptedScreenSchemeStore;

    private final List<FieldScreenSchemeItem> schemeItems = new ArrayList<>();

    public boolean getAttemptedScreenSchemeStore() {
        return attemptedScreenSchemeStore;
    }

    @Override
    public void addFieldScreenSchemeItem(final FieldScreenSchemeItem schemeItem) {
        schemeItems.add(schemeItem);
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public FieldScreen getFieldScreen(final IssueOperation arg0) {
        return null;
    }

    @Override
    public FieldScreenSchemeItem getFieldScreenSchemeItem(final IssueOperation issueOperation) {
        return schemeItems.parallelStream()
                   .filter((schemeItem) -> schemeItem.getIssueOperation().getNameKey().equals(issueOperation.getNameKey()))
                   .findAny().orElse(null);
    }

    @Override
    public Collection<FieldScreenSchemeItem> getFieldScreenSchemeItems() {
        return schemeItems;
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
    public String getName() {
        return name;
    }

    @Override
    public void remove() {

    }

    @Override
    public FieldScreenSchemeItem removeFieldScreenSchemeItem(final IssueOperation arg0) {
        return null;
    }

    @Override
    public void setDescription(final String arg0) {
    }

    @Override
    public void setGenericValue(final GenericValue arg0) {
    }

    @Override
    public void setId(final Long arg0) {
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public void store() {
        attemptedScreenSchemeStore = true;
    }

}
