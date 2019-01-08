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
package com.blackducksoftware.integration.jira.mocks.issue;

import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.util.I18nHelper;

@SuppressWarnings("unused")
public class IssueTypeMock implements IssueType {
    private String name;
    private String id;
    private GenericValue value;
    private String description;

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getCompleteIconUrl() {
        return null;
    }

    @Override
    public String getDescTranslation() {
        return null;
    }

    @Override
    public String getDescTranslation(final String arg0) {
        return null;
    }

    @Override
    public String getDescTranslation(final I18nHelper arg0) {

        return null;
    }

    @Override
    public String getDescription() {

        return null;
    }

    public void setValue(final GenericValue value) {
        this.value = value;
    }

    @Override
    public String getIconUrl() {

        return null;
    }

    @Override
    public String getIconUrlHtml() {

        return null;
    }

    @Override
    public String getId() {

        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    @Override
    public String getNameTranslation() {

        return null;
    }

    @Override
    public String getNameTranslation(final String arg0) {

        return null;
    }

    @Override
    public String getNameTranslation(final I18nHelper arg0) {

        return null;
    }

    @Override
    public Long getSequence() {

        return null;
    }

    @Override
    public int compareTo(final Object o) {

        return 0;
    }

    @Override
    public boolean isSubTask() {

        return false;
    }

    @Override
    public Avatar getAvatar() {
        return null;
    }

}
