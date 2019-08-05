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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.search.FieldComparatorSource;
import org.apache.lucene.search.SortField;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.admin.RenderableProperty;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.FieldJsonRepresentation;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfo;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfoContext;
import com.atlassian.jira.issue.fields.rest.RestFieldOperationsHandler;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.JsonType;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.util.MessagedResult;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.SearchHandler;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.opensymphony.module.propertyset.PropertySet;

import webwork.action.Action;

public class CustomFieldMock implements CustomField {

    private String name;

    private String description;

    private List<FieldConfigScheme> fieldConfigSchemes;

    @SuppressWarnings("rawtypes")
    private CustomFieldType fieldType;

    private CustomFieldSearcher searcher;

    private List<IssueType> associatedIssueTypes;

    public void setName(final String name) {
        this.name = name;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setFieldType(@SuppressWarnings("rawtypes") final CustomFieldType fieldType) {
        this.fieldType = fieldType;
    }

    public void setCustomFieldSearcher(final CustomFieldSearcher searcher) {
        this.searcher = searcher;
    }

    @Override
    public String getColumnCssClass() {

        return null;
    }

    @Override
    public String getColumnHeadingKey() {

        return null;
    }

    @Override
    public String getColumnViewHtml(final FieldLayoutItem arg0, @SuppressWarnings("rawtypes") final Map arg1, final Issue arg2) {

        return null;
    }

    @Override
    public String getDefaultSortOrder() {

        return null;
    }

    @Override
    public String getHiddenFieldId() {

        return null;
    }

    @Override
    public FieldComparatorSource getSortComparatorSource() {

        return null;
    }

    @Override
    public List<SortField> getSortFields(final boolean arg0) {

        return null;
    }

    @Override
    public LuceneFieldSorter getSorter() {

        return null;
    }

    @Override
    public String prettyPrintChangeHistory(final String arg0) {

        return null;
    }

    @Override
    public String prettyPrintChangeHistory(final String arg0, final I18nHelper arg1) {

        return null;
    }

    @Override
    public String getId() {
        return name;
    }

    @Override
    public String getName() {

        return name;
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
    public List<FieldConfigItemType> getConfigurationItemTypes() {

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
    public boolean hasParam(final Map<String, String[]> arg0) {

        return false;
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
    public void populateDefaults(final Map<String, Object> arg0, final Issue arg1) {

    }

    @Override
    public void populateForMove(final Map<String, Object> arg0, final Issue arg1, final Issue arg2) {

    }

    @Override
    public void populateFromIssue(final Map<String, Object> arg0, final Issue arg1) {

    }

    @Override
    public void populateFromParams(final Map<String, Object> arg0, final Map<String, String[]> arg1) {

    }

    @Override
    public void populateParamsFromString(final Map<String, Object> arg0, final String arg1, final Issue arg2)
        throws FieldValidationException {

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
    public SearchHandler createAssociatedSearchHandler() {

        return null;
    }

    @Override
    public String getValueFromIssue(final Issue arg0) {

        return null;
    }

    @Override
    public boolean isRenderable() {

        return false;
    }

    @Override
    public FieldTypeInfo getFieldTypeInfo(final FieldTypeInfoContext arg0) {

        return null;
    }

    @Override
    public FieldJsonRepresentation getJsonFromIssue(final Issue arg0, final boolean arg1, final FieldLayoutItem arg2) {

        return null;
    }

    @Override
    public JsonType getJsonSchema() {

        return null;
    }

    @Override
    public JsonData getJsonDefaultValue(final IssueContext arg0) {

        return null;
    }

    @Override
    public RestFieldOperationsHandler getRestFieldOperation() {

        return null;
    }

    @Override
    public int compare(final Issue arg0, final Issue arg1) throws IllegalArgumentException {

        return 0;
    }

    @Override
    public List<Project> getAssociatedProjectObjects() {

        return null;
    }

    @Override
    public ClauseNames getClauseNames() {

        return null;
    }

    @Override
    public List<FieldConfigScheme> getConfigurationSchemes() {
        return fieldConfigSchemes;
    }

    public void setFieldConfigSchemes(final List<FieldConfigScheme> fieldConfigSchemes) {
        this.fieldConfigSchemes = fieldConfigSchemes;
    }

    @Override
    public CustomFieldSearcher getCustomFieldSearcher() {

        return searcher;
    }

    @Override
    public CustomFieldType getCustomFieldType() {

        return fieldType;
    }

    public void setCustomFieldType(@SuppressWarnings("rawtypes") final CustomFieldType customFieldType) {
        this.fieldType = customFieldType;
    }

    @Override
    public CustomFieldParams getCustomFieldValues(final Map arg0) {

        return null;
    }

    @Override
    public String getDescription() {

        return description;
    }

    @Override
    public RenderableProperty getDescriptionProperty() {

        return null;
    }

    @Override
    public String getFieldName() {

        return null;
    }

    @Override
    public GenericValue getGenericValue() {

        return null;
    }

    @Override
    public Long getIdAsLong() {

        return null;
    }

    @Override
    public Options getOptions(final String arg0, final JiraContextNode arg1) {

        return null;
    }

    @Override
    public Options getOptions(final String arg0, final FieldConfig arg1, final JiraContextNode arg2) {

        return null;
    }

    @Override
    public PropertySet getPropertySet() {

        return null;
    }

    @Override
    public FieldConfig getRelevantConfig(final Issue arg0) {

        return null;
    }

    @Override
    public FieldConfig getRelevantConfig(final IssueContext arg0) {

        return null;
    }

    @Override
    public FieldConfig getReleventConfig(final SearchContext arg0) {

        return null;
    }

    @Override
    public String getUntranslatedDescription() {

        return null;
    }

    @Override
    public RenderableProperty getUntranslatedDescriptionProperty() {

        return null;
    }

    @Override
    public String getUntranslatedName() {

        return null;
    }

    @Override
    public Object getValue(final Issue arg0) {

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
    public boolean isEditable() {

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
    public boolean isInScope(final SearchContext arg0) {

        return false;
    }

    @Override
    public boolean isInScope(final Project arg0, final List<String> arg1) {

        return false;
    }

    @Override
    public boolean isInScope(final long arg0, final String arg1) {

        return false;
    }

    @Override
    public boolean isInScopeForSearch(final Project arg0, final List<String> arg1) {

        return false;
    }

    @Override
    public boolean isRelevantForIssueContext(final IssueContext arg0) {

        return false;
    }

    @Override
    public Set<Long> remove() {

        return null;
    }

    @Override
    public void validateFromActionParams(final Map arg0, final ErrorCollection arg1, final FieldConfig arg2) {

    }

    @Override
    public List<IssueType> getAssociatedIssueTypeObjects() {
        // Auto-generated method stub
        return null;
    }

    @Override
    public List<IssueType> getAssociatedIssueTypes() {
        return associatedIssueTypes;
    }

    public void setAssociatedIssueTypes(final List<IssueType> issueTypes) {
        associatedIssueTypes = issueTypes;
    }
}
