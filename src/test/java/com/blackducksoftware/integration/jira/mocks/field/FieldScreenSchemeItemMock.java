/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2020 Synopsys, Inc.
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
package com.blackducksoftware.integration.jira.mocks.field;

import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItem;
import com.atlassian.jira.issue.operation.ScreenableIssueOperation;

public class FieldScreenSchemeItemMock implements FieldScreenSchemeItem {

    private ScreenableIssueOperation issueOperation;

    private FieldScreen fieldScreen;

    @Override
    public int compareTo(final FieldScreenSchemeItem o) {

        return 0;
    }

    @Override
    public FieldScreen getFieldScreen() {

        return fieldScreen;
    }

    @Override
    public Long getFieldScreenId() {

        return null;
    }

    @Override
    public FieldScreenScheme getFieldScreenScheme() {

        return null;
    }

    @Override
    public GenericValue getGenericValue() {

        return null;
    }

    @Override
    public Long getId() {

        return null;
    }

    @Override
    public ScreenableIssueOperation getIssueOperation() {
        return issueOperation;
    }

    @Override
    public String getIssueOperationName() {

        return null;
    }

    @Override
    public void remove() {

    }

    @Override
    public void setFieldScreen(final FieldScreen fieldScreen) {
        this.fieldScreen = fieldScreen;
    }

    @Override
    public void setFieldScreenScheme(final FieldScreenScheme arg0) {

    }

    @Override
    public void setGenericValue(final GenericValue arg0) {

    }

    @Override
    public void setIssueOperation(final ScreenableIssueOperation issueOperation) {
        this.issueOperation = issueOperation;

    }

    @Override
    public void store() {

    }

}
