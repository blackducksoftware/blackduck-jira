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
package com.blackducksoftware.integration.jira.mocks.field;

import java.util.Collection;
import java.util.Map;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.util.MessagedResult;
import com.atlassian.jira.issue.search.SearchHandler;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.BulkEditBean;

import webwork.action.Action;

@SuppressWarnings("rawtypes")
public class OrderableFieldMock implements OrderableField {

    private String name;

    private String id;

    @Override
    public String getId() {

        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    @Override
    public String getName() {

        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getNameKey() {

        return null;
    }

    @Override
    public int compareTo(final Object o) {

        return 0;
    }

    @Override
    public SearchHandler createAssociatedSearchHandler() {

        return null;
    }

    @Override
    public String availableForBulkEdit(final BulkEditBean arg0) {

        return null;
    }

    @Override
    public boolean canRemoveValueFromIssueObject(final Issue arg0) {

        return false;
    }

    @Override
    public void createValue(final Issue arg0, final Object arg1) {

    }

    @Override
    public String getBulkEditHtml(final OperationContext arg0, final Action arg1, final BulkEditBean arg2,
            final Map arg3) {

        return null;
    }

    @Override
    public String getCreateHtml(final FieldLayoutItem arg0, final OperationContext arg1, final Action arg2,
            final Issue arg3) {

        return null;
    }

    @Override
    public String getCreateHtml(final FieldLayoutItem arg0, final OperationContext arg1, final Action arg2,
            final Issue arg3, final Map arg4) {

        return null;
    }

    @Override
    public Object getDefaultValue(final Issue arg0) {

        return null;
    }

    @Override
    public String getEditHtml(final FieldLayoutItem arg0, final OperationContext arg1, final Action arg2,
            final Issue arg3) {

        return null;
    }

    @Override
    public String getEditHtml(final FieldLayoutItem arg0, final OperationContext arg1, final Action arg2,
            final Issue arg3, final Map arg4) {

        return null;
    }

    @Override
    public Object getValueFromParams(final Map arg0) throws FieldValidationException {

        return null;
    }

    @Override
    public String getViewHtml(final FieldLayoutItem arg0, final Action arg1, final Issue arg2) {

        return null;
    }

    @Override
    public String getViewHtml(final FieldLayoutItem arg0, final Action arg1, final Issue arg2, final Map arg3) {

        return null;
    }

    @Override
    public String getViewHtml(final FieldLayoutItem arg0, final Action arg1, final Issue arg2, final Object arg3,
            final Map arg4) {

        return null;
    }

    @Override
    public boolean hasValue(final Issue arg0) {

        return false;
    }

    @Override
    public boolean isShown(final Issue arg0) {

        return false;
    }

    @Override
    public MessagedResult needsMove(final Collection arg0, final Issue arg1, final FieldLayoutItem arg2) {

        return null;
    }

    @Override
    public void removeValueFromIssueObject(final MutableIssue arg0) {

    }

    @Override
    public void updateIssue(final FieldLayoutItem arg0, final MutableIssue arg1, final Map arg2) {

    }

    @Override
    public void updateValue(final FieldLayoutItem arg0, final Issue arg1, final ModifiedValue arg2,
            final IssueChangeHolder arg3) {

    }

    @Override
    public void validateParams(final OperationContext arg0, final ErrorCollection arg1, final I18nHelper arg2,
            final Issue arg3, final FieldScreenRenderLayoutItem arg4) {

    }

    @Override
    public boolean hasParam(final Map arg0) {
        // Auto-generated method stub
        return false;
    }

    @Override
    public void populateDefaults(final Map arg0, final Issue arg1) {
        // Auto-generated method stub

    }

    @Override
    public void populateForMove(final Map arg0, final Issue arg1, final Issue arg2) {
        // Auto-generated method stub

    }

    @Override
    public void populateFromIssue(final Map arg0, final Issue arg1) {
        // Auto-generated method stub

    }

    @Override
    public void populateFromParams(final Map arg0, final Map arg1) {
        // Auto-generated method stub

    }

    @Override
    public void populateParamsFromString(final Map arg0, final String arg1, final Issue arg2)
            throws FieldValidationException {
        // Auto-generated method stub

    }

}
