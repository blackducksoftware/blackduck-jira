/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
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
 *******************************************************************************/
package com.blackducksoftware.integration.jira.mocks.workflow;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.workflow.AssignableWorkflowScheme;

public class AssignableWorkflowSchemeMock implements AssignableWorkflowScheme {

    private final Map<String, String> mappingIssueTypeNamesToWorkFlowNames = new HashMap<>();

    private String name;

    private Builder builder;

    public void addMappingIssueToWorkflow(final String issueTypeName, final String workflowName) {
        mappingIssueTypeNamesToWorkFlowNames.put(issueTypeName, workflowName);
    }

    @Override
    public String getActualDefaultWorkflow() {

        return null;
    }

    @Override
    public String getActualWorkflow(final String arg0) {

        return null;
    }

    @Override
    public String getConfiguredDefaultWorkflow() {

        return null;
    }

    @Override
    public String getConfiguredWorkflow(final String issueId) {
        return mappingIssueTypeNamesToWorkFlowNames.get(issueId);
    }

    @Override
    public Long getId() {

        return null;
    }

    @Override
    public Map<String, String> getMappings() {

        return mappingIssueTypeNamesToWorkFlowNames;
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
    public Builder builder() {
        return builder;
    }

    public void setBuilder(final Builder builder) {
        this.builder = builder;
    }

    @Override
    public String getDescription() {

        return null;
    }

    @Override
    public String getName() {

        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

}
