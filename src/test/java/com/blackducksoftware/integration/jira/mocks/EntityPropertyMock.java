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
package com.blackducksoftware.integration.jira.mocks;

import java.sql.Timestamp;

import com.atlassian.jira.entity.property.EntityProperty;

public class EntityPropertyMock implements EntityProperty {

    private String entityName;

    private String key;

    private String value;

    @Override
    public Timestamp getCreated() {
        return null;
    }

    @Override
    public Long getEntityId() {
        return null;
    }

    @Override
    public String getEntityName() {
        return entityName;
    }

    @Override
    public Long getId() {
        return null;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Timestamp getUpdated() {
        return null;
    }

    @Override
    public String getValue() {
        return value;
    }

    public void setEntityName(final String entityName) {
        this.entityName = entityName;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public void setValue(final String value) {
        this.value = value;
    }
}
