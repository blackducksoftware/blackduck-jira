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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.fugue.Function2;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.entity.property.EntityPropertyQuery;
import com.atlassian.jira.entity.property.JsonEntityPropertyManager;
import com.atlassian.jira.event.entity.EntityPropertySetEvent;
import com.atlassian.jira.user.ApplicationUser;

@SuppressWarnings("deprecation")
public class JSonEntityPropertyManagerMock implements JsonEntityPropertyManager {
    Map<String, Map<Long, Map<String, EntityProperty>>> entityMap;

    public JSonEntityPropertyManagerMock() {
        entityMap = new HashMap<>();
    }

    @Override
    public long countByEntity(final String arg0, final Long arg1) {
        return 0;
    }

    @Override
    public long countByEntityNameAndPropertyKey(final String arg0, final String arg1) {
        return 0;
    }

    @Override
    public void delete(final String arg0, final Long arg1, final String arg2) {
    }

    @Override
    public void deleteByEntity(final String arg0, final Long arg1) {

    }

    @Override
    public void deleteByEntityNameAndEntityIds(final String arg0, final List<Long> arg1) {

    }

    @Override
    public void deleteByEntityNameAndPropertyKey(final String arg0, final String arg1) {

    }

    @Override
    public boolean exists(final String arg0, final Long arg1, final String arg2) {
        return false;
    }

    @Override
    public List<String> findKeys(final String arg0, final Long arg1) {
        return null;
    }

    @Override
    public List<String> findKeys(final String arg0, final String arg1) {
        return null;
    }

    @Override
    public Map<String, EntityProperty> get(final String arg0, final Long arg1, final List<String> arg2) {
        return null;
    }

    @Override
    public EntityProperty get(final String arg0, final Long arg1, final String arg2) {
        final Map<String, EntityProperty> entityKeyMap = get(arg0, arg1);

        if (entityKeyMap != null && entityKeyMap.containsKey(arg2)) {
            return entityKeyMap.get(arg2);
        } else {
            return null;
        }
    }

    @Override
    public Map<String, EntityProperty> get(final String arg0, final Long arg1) {
        if (entityMap.containsKey(arg0)) {
            final Map<Long, Map<String, EntityProperty>> issueEntityMap = entityMap.get(arg0);
            if (issueEntityMap.containsKey(arg1)) {
                final Map<String, EntityProperty> entityKeyMap = issueEntityMap.get(arg1);
                return entityKeyMap;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public int getMaximumValueLength() {
        return 0;
    }

    @Override
    public void put(final ApplicationUser arg0, final String arg1, final Long arg2, final String arg3, final String arg4,
            final Function2<ApplicationUser, EntityProperty, ? extends EntityPropertySetEvent> arg5, final boolean arg6) {
    }

    @Override
    public void put(final String arg0, final Long arg1, final String arg2, final String arg3) {
        final Map<String, EntityProperty> entityKeyMap;
        if (entityMap.containsKey(arg0)) {
            final Map<Long, Map<String, EntityProperty>> issueEntityMap = entityMap.get(arg0);
            if (issueEntityMap.containsKey(arg1)) {
                entityKeyMap = issueEntityMap.get(arg1);
            } else {
                entityKeyMap = new HashMap<>();
                issueEntityMap.put(arg1, entityKeyMap);
            }
        } else {
            // new entry
            final Map<Long, Map<String, EntityProperty>> issueEntityMap = new HashMap<>();
            entityKeyMap = new HashMap<>();
            issueEntityMap.put(arg1, entityKeyMap);
            entityMap.put(arg0, issueEntityMap);
        }
        final EntityPropertyMock entity = new EntityPropertyMock();
        entity.setKey(arg2);
        entity.setValue(arg3);
        entityKeyMap.put(arg2, entity);
    }

    @Override
    public void putDryRun(final String arg0, final String arg1, final String arg2) {

    }

    @Override
    public EntityPropertyQuery<?> query() {
        return null;
    }
}
