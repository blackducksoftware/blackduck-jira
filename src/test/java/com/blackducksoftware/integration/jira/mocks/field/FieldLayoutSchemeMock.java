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

import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutSchemeEntity;
import com.atlassian.jira.project.Project;

public class FieldLayoutSchemeMock implements FieldLayoutScheme {

    private String name;

    private Collection<FieldLayoutSchemeEntity> entities;

    private final Collection<FieldLayoutSchemeEntity> entitiesAdded = new ArrayList<>();

    private int storeCount = 0;

    @Override
    public void addEntity(final FieldLayoutSchemeEntity entity) {
        entitiesAdded.add(entity);
    }

    public Collection<FieldLayoutSchemeEntity> getEntitiesAdded() {
        return entitiesAdded;
    }

    @Override
    public boolean containsEntity(final String arg0) {
        return false;
    }

    @Override
    public String getDescription() {
        return null;
    }

    public void setEntities(final Collection<FieldLayoutSchemeEntity> entities) {
        this.entities = entities;
    }

    @Override
    public Collection<FieldLayoutSchemeEntity> getEntities() {
        return entities;
    }

    @Override
    public FieldLayoutSchemeEntity getEntity(final String arg0) {
        return null;
    }

    @Override
    public FieldLayoutSchemeEntity getEntity(final EditableFieldLayout arg0) {
        return null;
    }

    @Override
    public Long getFieldLayoutId(final String arg0) {
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
        return name;
    }

    @Override
    public Collection<GenericValue> getProjects() {
        return null;
    }

    @Override
    public Collection<Project> getProjectsUsing() {
        return null;
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
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public void store() {
        this.storeCount++;
    }

    public int getStoreCount() {
        return storeCount;
    }

}
