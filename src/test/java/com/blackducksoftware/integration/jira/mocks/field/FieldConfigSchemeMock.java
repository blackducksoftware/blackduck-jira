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
package com.blackducksoftware.integration.jira.mocks.field;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MultiMap;

import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.fields.ConfigurableField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;

public class FieldConfigSchemeMock implements FieldConfigScheme {

    public FieldConfigSchemeMock() {
    }

    @Override
    public Collection<String> getAssociatedIssueTypeIds() {
        return null;
    }

    @Override
    public Collection<IssueType> getAssociatedIssueTypeObjects() {
        return null;
    }

    @Override
    public List<Long> getAssociatedProjectIds() {
        return null;
    }

    @Override
    public List<Project> getAssociatedProjectObjects() {
        return null;
    }

    @Override
    public Map<String, FieldConfig> getConfigs() {
        return null;
    }

    @Override
    public MultiMap getConfigsByConfig() {
        return null;
    }

    @Override
    public List<JiraContextNode> getContexts() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public ConfigurableField getField() {
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
    public FieldConfig getOneAndOnlyConfig() {
        return null;
    }

    @Override
    public boolean isAllIssueTypes() {
        return false;
    }

    @Override
    public boolean isAllProjects() {
        return false;
    }

    @Override
    public boolean isBasicMode() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public boolean isGlobal() {
        return false;
    }

    @Override
    public boolean isInContext(final IssueContext arg0) {
        return false;
    }

    @Override
    public Collection<IssueType> getAssociatedIssueTypes() {
        // Auto-generated method stub
        return null;
    }

}
