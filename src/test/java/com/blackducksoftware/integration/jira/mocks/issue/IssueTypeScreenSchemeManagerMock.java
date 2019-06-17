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

import java.util.Collection;

import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntity;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.project.Project;

public class IssueTypeScreenSchemeManagerMock implements IssueTypeScreenSchemeManager {
    private IssueTypeScreenScheme issueTypeScreenScheme;

    public IssueTypeScreenSchemeManagerMock() {
    }

    @Override
    public void addSchemeAssociation(final GenericValue arg0, final IssueTypeScreenScheme arg1) {

    }

    @Override
    public void addSchemeAssociation(final Project arg0, final IssueTypeScreenScheme arg1) {

    }

    @Override
    public void associateWithDefaultScheme(final GenericValue arg0) {

    }

    @Override
    public void associateWithDefaultScheme(final Project arg0) {

    }

    @Override
    public void createIssueTypeScreenScheme(final IssueTypeScreenScheme arg0) {

    }

    @Override
    public void createIssueTypeScreenSchemeEntity(final IssueTypeScreenSchemeEntity arg0) {

    }

    @Override
    public IssueTypeScreenScheme getDefaultScheme() {
        return null;
    }

    @Override
    public FieldScreenScheme getFieldScreenScheme(final Issue arg0) {
        return null;
    }

    @Override
    public IssueTypeScreenScheme getIssueTypeScreenScheme(final Long arg0) {
        return null;
    }

    @Override
    public IssueTypeScreenScheme getIssueTypeScreenScheme(final GenericValue arg0) {
        return null;
    }

    @Override
    public IssueTypeScreenScheme getIssueTypeScreenScheme(final Project arg0) {
        return issueTypeScreenScheme;
    }

    public void setIssueTypeScreenScheme(final IssueTypeScreenScheme issueTypeScreenScheme) {
        this.issueTypeScreenScheme = issueTypeScreenScheme;
    }

    @Override
    public Collection getIssueTypeScreenSchemeEntities(final IssueTypeScreenScheme arg0) {
        return null;
    }

    @Override
    public Collection<IssueTypeScreenScheme> getIssueTypeScreenSchemes() {
        return null;
    }

    @Override
    public Collection getIssueTypeScreenSchemes(final FieldScreenScheme arg0) {
        return null;
    }

    @Override
    public Collection<GenericValue> getProjects(final IssueTypeScreenScheme arg0) {
        return null;
    }

    @Override
    public void refresh() {

    }

    @Override
    public void removeIssueTypeSchemeEntities(final IssueTypeScreenScheme arg0) {

    }

    @Override
    public void removeIssueTypeScreenScheme(final IssueTypeScreenScheme arg0) {

    }

    @Override
    public void removeIssueTypeScreenSchemeEntity(final IssueTypeScreenSchemeEntity arg0) {

    }

    @Override
    public void removeSchemeAssociation(final GenericValue arg0, final IssueTypeScreenScheme arg1) {

    }

    @Override
    public void removeSchemeAssociation(final Project arg0, final IssueTypeScreenScheme arg1) {

    }

    @Override
    public void updateIssueTypeScreenScheme(final IssueTypeScreenScheme arg0) {

    }

    @Override
    public void updateIssueTypeScreenSchemeEntity(final IssueTypeScreenSchemeEntity arg0) {

    }

}
