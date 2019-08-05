package com.blackducksoftware.integration.jira.mocks.field;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.fields.ConfigurableField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;

public class FieldConfigSchemeManagerMock implements FieldConfigSchemeManager {
    @Override
    public void init() {

    }

    @Override
    public List<FieldConfigScheme> getConfigSchemesForField(final ConfigurableField configurableField) {
        return null;
    }

    @Override
    public FieldConfigScheme getConfigSchemeForFieldConfig(final FieldConfig fieldConfig) {
        return null;
    }

    @Override
    public FieldConfigScheme getFieldConfigScheme(final Long aLong) {
        return null;
    }

    @Override
    public FieldConfigScheme createDefaultScheme(final ConfigurableField configurableField, final List<JiraContextNode> list, final List<IssueType> list1) {
        return null;
    }

    @Override
    public FieldConfigScheme updateFieldConfigScheme(final FieldConfigScheme fieldConfigScheme, final List<JiraContextNode> list, final ConfigurableField configurableField) {
        return null;
    }

    @Override
    public FieldConfigScheme updateFieldConfigScheme(final FieldConfigScheme fieldConfigScheme) {
        return null;
    }

    @Override
    public void removeFieldConfigScheme(final Long aLong) {

    }

    @Override
    public boolean isRelevantForIssueContext(final IssueContext issueContext, final ConfigurableField configurableField) {
        return false;
    }

    @Override
    public FieldConfig getRelevantConfig(final IssueContext issueContext, final ConfigurableField configurableField) {
        return null;
    }

    @Override
    public FieldConfigScheme createFieldConfigScheme(final FieldConfigScheme fieldConfigScheme, final List<JiraContextNode> list, final List<IssueType> list1, final ConfigurableField configurableField) {
        return null;
    }

    @Override
    public FieldConfigScheme createDefaultScheme(final ConfigurableField configurableField, final List<JiraContextNode> list) {
        return null;
    }

    @Override
    public void removeSchemeAssociation(final List<JiraContextNode> list, final ConfigurableField configurableField) {

    }

    @Override
    public List<Project> getAssociatedProjectObjects(final ConfigurableField configurableField) {
        return null;
    }

    @Nullable
    @Override
    public FieldConfigScheme getRelevantConfigScheme(final IssueContext issueContext, final ConfigurableField configurableField) {
        return null;
    }

    @Nullable
    @Override
    public FieldConfigScheme getRelevantConfigScheme(final Project project, final ConfigurableField configurableField) {
        return null;
    }

    @Override
    public Collection getInvalidFieldConfigSchemesForIssueTypeRemoval(final IssueType issueType) {
        return null;
    }

    @Override
    public void removeInvalidFieldConfigSchemesForIssueType(final IssueType issueType) {

    }

    @Override
    public void removeInvalidFieldConfigSchemesForCustomField(final String s) {

    }
}
