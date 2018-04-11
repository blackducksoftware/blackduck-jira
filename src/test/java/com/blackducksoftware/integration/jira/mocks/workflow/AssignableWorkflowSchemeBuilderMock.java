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
package com.blackducksoftware.integration.jira.mocks.workflow;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.workflow.AssignableWorkflowScheme;
import com.atlassian.jira.workflow.AssignableWorkflowScheme.Builder;

public class AssignableWorkflowSchemeBuilderMock implements AssignableWorkflowScheme.Builder {

    private AssignableWorkflowScheme workflowScheme;

    private final Map<String, String> mappingIssueTypeNamesToWorkFlowNames = new HashMap<>();

    @Override
    public Builder clearMappings() {

        return null;
    }

    @Override
    public String getDefaultWorkflow() {

        return null;
    }

    @Override
    public String getDescription() {

        return null;
    }

    @Override
    public Long getId() {

        return null;
    }

    @Override
    public String getMapping(final String arg0) {

        return null;
    }

    @Override
    public Map<String, String> getMappings() {

        return mappingIssueTypeNamesToWorkFlowNames;
    }

    @Override
    public String getName() {

        return null;
    }

    @Override
    public boolean isDefault() {

        return false;
    }

    @Override
    public boolean isDraft() {

        return false;
    }

    @Override
    public Builder removeDefault() {

        return null;
    }

    @Override
    public Builder removeMapping(final String arg0) {

        return null;
    }

    @Override
    public Builder removeWorkflow(final String arg0) {

        return null;
    }

    @Override
    public Builder setDefaultWorkflow(final String arg0) {

        return null;
    }

    @Override
    public Builder setMapping(final String key, final String value) {
        mappingIssueTypeNamesToWorkFlowNames.put(key, value);
        return this;
    }

    @Override
    public Builder setMappings(final Map<String, String> mappings) {
        mappingIssueTypeNamesToWorkFlowNames.putAll(mappings);
        return this;
    }

    @Override
    public AssignableWorkflowScheme build() {
        workflowScheme.getMappings().putAll(mappingIssueTypeNamesToWorkFlowNames);
        return workflowScheme;
    }

    public void setWorkflowScheme(final AssignableWorkflowScheme workflowScheme) {
        this.workflowScheme = workflowScheme;
    }

    @Override
    public Builder setDescription(final String arg0) {

        return null;
    }

    @Override
    public Builder setName(final String arg0) {

        return null;
    }

}
