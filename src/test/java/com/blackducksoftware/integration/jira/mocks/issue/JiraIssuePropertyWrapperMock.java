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

import java.lang.reflect.Field;

import com.atlassian.jira.entity.property.EntityProperty;
import com.blackducksoftware.integration.jira.config.JiraServices;
import com.blackducksoftware.integration.jira.task.issue.handler.JiraIssuePropertyWrapper;

public class JiraIssuePropertyWrapperMock extends JiraIssuePropertyWrapper {
    private final JiraServices jiraServices;

    public JiraIssuePropertyWrapperMock(final JiraServices jiraServices) {
        super(jiraServices.getPropertyService(), jiraServices.getProjectPropertyService(), jiraServices.getJsonEntityPropertyManager());
        this.jiraServices = jiraServices;
    }

    @Override
    public EntityProperty findProperty(final String queryString) {
        overrideField("jsonEntityPropertyManager", jiraServices.getJsonEntityPropertyManager());
        return super.findProperty(queryString);
    }

    private void overrideField(final String fieldName, final Object newValue) {
        try {
            final Class<?> superClass = this.getClass().getSuperclass();
            final Field field = superClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(this, newValue);
        } catch (final Exception e) {
            System.out.println("Reflection failed. Class: " + JiraIssuePropertyWrapperMock.class.getName() + ", Field: " + fieldName + ", New Value Class: " + newValue.getClass().getName());
            throw new RuntimeException(e);
        }
    }

}
