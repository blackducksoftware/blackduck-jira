/**
 * Hub JIRA Plugin
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.ConfigurableField;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.HideableField;
import com.atlassian.jira.issue.fields.IssueTypeField;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.ProjectField;
import com.atlassian.jira.issue.fields.RequirableField;
import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.jql.context.QueryContext;
import com.atlassian.jira.user.ApplicationUser;

public class FieldManagerMock implements FieldManager {
    private final CustomFieldManagerMock customFieldManagerMock;

    public FieldManagerMock(final CustomFieldManagerMock customFieldManagerMock) {
        this.customFieldManagerMock = customFieldManagerMock;
    }

    @Override
    public Set<NavigableField> getAllAvailableNavigableFields() throws FieldException {
        final Set<NavigableField> navFields = new HashSet<>();

        final List<CustomField> customFields = customFieldManagerMock.getCustomFields();
        navFields.addAll(customFields);
        return navFields;
    }

    @Override
    public Set<SearchableField> getAllSearchableFields() {

        return null;
    }

    @Override
    public ConfigurableField getConfigurableField(final String arg0) {

        return null;
    }

    @Override
    public CustomField getCustomField(final String arg0) {

        return null;
    }

    @Override
    public Field getField(final String arg0) {

        return null;
    }

    @Override
    public HideableField getHideableField(final String arg0) {

        return null;
    }

    @Override
    public IssueTypeField getIssueTypeField() {

        return null;
    }

    @Override
    public NavigableField getNavigableField(final String arg0) {

        return null;
    }

    @Override
    public Set<NavigableField> getNavigableFields() {

        return null;
    }

    @Override
    public OrderableField getOrderableField(final String fieldName) {
        final List<CustomField> customFields = customFieldManagerMock.getCustomFields();
        if (customFields == null) {
            return null;
        }
        return customFields.parallelStream().filter((field) -> field.getName().equals(fieldName))
                .findAny().orElse(null);
    }

    @Override
    public Set<OrderableField> getOrderableFields() {

        return null;
    }

    @Override
    public ProjectField getProjectField() {

        return null;
    }

    @Override
    public RequirableField getRequiredField(final String arg0) {

        return null;
    }

    @Override
    public Set<SearchableField> getSystemSearchableFields() {

        return null;
    }

    @Override
    public Set<Field> getUnavailableFields() {

        return null;
    }

    @Override
    public boolean isCustomField(final String arg0) {

        return false;
    }

    @Override
    public boolean isCustomField(final Field arg0) {

        return false;
    }

    @Override
    public boolean isFieldHidden(final Set<FieldLayout> arg0, final Field arg1) {

        return false;
    }

    @Override
    public boolean isHideableField(final String arg0) {

        return false;
    }

    @Override
    public boolean isHideableField(final Field arg0) {

        return false;
    }

    @Override
    public boolean isMandatoryField(final String arg0) {

        return false;
    }

    @Override
    public boolean isMandatoryField(final Field arg0) {

        return false;
    }

    @Override
    public boolean isNavigableField(final String arg0) {

        return false;
    }

    @Override
    public boolean isNavigableField(final Field arg0) {

        return false;
    }

    @Override
    public boolean isOrderableField(final String arg0) {

        return false;
    }

    @Override
    public boolean isOrderableField(final Field arg0) {

        return false;
    }

    @Override
    public boolean isRenderableField(final String arg0) {

        return false;
    }

    @Override
    public boolean isRenderableField(final Field arg0) {

        return false;
    }

    @Override
    public boolean isRequirableField(final String arg0) {

        return false;
    }

    @Override
    public boolean isRequirableField(final Field arg0) {

        return false;
    }

    @Override
    public boolean isTimeTrackingOn() {

        return false;
    }

    @Override
    public boolean isUnscreenableField(final String arg0) {

        return false;
    }

    @Override
    public boolean isUnscreenableField(final Field arg0) {

        return false;
    }

    @Override
    public void refresh() {

    }

    @Override
    public Set<CustomField> getAvailableCustomFields(final ApplicationUser arg0, final Issue arg1)
            throws FieldException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<NavigableField> getAvailableNavigableFields(final ApplicationUser arg0) throws FieldException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<NavigableField> getAvailableNavigableFieldsWithScope(final ApplicationUser arg0) throws FieldException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<NavigableField> getAvailableNavigableFieldsWithScope(final ApplicationUser arg0, final QueryContext arg1)
            throws FieldException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<FieldLayout> getVisibleFieldLayouts(final ApplicationUser arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isCustomFieldId(final String arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isExistingCustomField(final String arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isFieldHidden(final ApplicationUser arg0, final Field arg1) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isFieldHidden(final ApplicationUser arg0, final String arg1) {
        // TODO Auto-generated method stub
        return false;
    }

}
