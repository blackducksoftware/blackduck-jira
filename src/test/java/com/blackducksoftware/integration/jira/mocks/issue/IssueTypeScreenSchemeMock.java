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
package com.blackducksoftware.integration.jira.mocks.issue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntity;
import com.atlassian.jira.issue.issuetype.IssueType;

public class IssueTypeScreenSchemeMock implements IssueTypeScreenScheme {
    private final List<IssueTypeScreenSchemeEntity> addedEntities = new ArrayList<>();

    public IssueTypeScreenSchemeMock() {
    }

    @Override
    public void addEntity(final IssueTypeScreenSchemeEntity arg0) {
        addedEntities.add(arg0);
    }

    public List<IssueTypeScreenSchemeEntity> getAddedEntities() {
        return addedEntities;
    }

    @Override
    public boolean containsEntity(final String arg0) {
        return false;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public FieldScreenScheme getEffectiveFieldScreenScheme(final IssueType arg0) {
        return null;
    }

    @Override
    public Collection<IssueTypeScreenSchemeEntity> getEntities() {
        return null;
    }

    @Override
    public IssueTypeScreenSchemeEntity getEntity(final String arg0) {
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
    public String getName() {
        return null;
    }

    @Override
    public Collection<GenericValue> getProjects() {
        return null;
    }

    @Override
    public boolean isDefault() {
        return false;
    }

    @Override
    public void remove() {

    }

    @Override
    public void removeEntity(final String arg0) {

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
    public void setName(final String arg0) {

    }

    @Override
    public void store() {

    }

}
