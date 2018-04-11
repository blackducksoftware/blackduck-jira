/**
 * Hub JIRA Plugin
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

import com.atlassian.jira.issue.status.SimpleStatus;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.status.category.StatusCategory;
import com.atlassian.jira.util.I18nHelper;

public class StatusMock implements Status {

    private String name;

    @Override
    public String getDescTranslation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDescTranslation(final String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDescTranslation(final I18nHelper arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getIconUrlHtml() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getNameTranslation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getNameTranslation(final String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getNameTranslation(final I18nHelper arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long getSequence() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int compareTo(final Object o) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getCompleteIconUrl() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getIconUrl() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SimpleStatus getSimpleStatus() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SimpleStatus getSimpleStatus(final I18nHelper arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public StatusCategory getStatusCategory() {
        // TODO Auto-generated method stub
        return null;
    }

}
