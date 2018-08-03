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
package com.blackducksoftware.integration.jira.mocks;

import java.util.List;

import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.entity.property.EntityPropertyQuery.ExecutableQuery;
import com.atlassian.jira.util.Visitor;

public class ExecutableQueryMock implements ExecutableQuery {

    @Override
    public com.atlassian.jira.entity.property.EntityPropertyQuery.ExecutableQuery entityId(final Long arg0) {
        // Auto-generated method stub
        return null;
    }

    @Override
    public com.atlassian.jira.entity.property.EntityPropertyQuery.ExecutableQuery entityName(final String arg0) {
        // Auto-generated method stub
        return null;
    }

    @Override
    public com.atlassian.jira.entity.property.EntityPropertyQuery.ExecutableQuery key(final String arg0) {
        // Auto-generated method stub
        return null;
    }

    @Override
    public com.atlassian.jira.entity.property.EntityPropertyQuery.ExecutableQuery keyPrefix(final String arg0) {
        // Auto-generated method stub
        return null;
    }

    @Override
    public com.atlassian.jira.entity.property.EntityPropertyQuery.ExecutableQuery maxResults(final int arg0) {
        // Auto-generated method stub
        return null;
    }

    @Override
    public com.atlassian.jira.entity.property.EntityPropertyQuery.ExecutableQuery offset(final int arg0) {
        // Auto-generated method stub
        return null;
    }

    @Override
    public long count() {
        // Auto-generated method stub
        return 0;
    }

    @Override
    public void delete() {
        // Auto-generated method stub

    }

    @Override
    public List<EntityProperty> find() {
        // Auto-generated method stub
        return null;
    }

    @Override
    public void find(final Visitor<EntityProperty> arg0) {
        // Auto-generated method stub
    }

    @Override
    public List<String> findDistinctKeys() {
        // Auto-generated method stub
        return null;
    }

    @Override
    public List<String> findKeys() {
        // Auto-generated method stub
        return null;
    }

}
