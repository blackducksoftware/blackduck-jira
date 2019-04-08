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
package com.blackducksoftware.integration.jira.task.setup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import com.atlassian.jira.issue.fields.layout.field.EditableDefaultFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayoutImpl;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutSchemeEntity;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutSchemeEntityImpl;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.config.JiraServices;
import com.blackducksoftware.integration.jira.config.JiraSettingsService;

public class BlackDuckFieldConfigurationSetup {
    public final List<String> requiredDefaultFields = new ArrayList<>();
    private final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));
    private final JiraSettingsService settingService;
    private final JiraServices jiraServices;

    public BlackDuckFieldConfigurationSetup(final JiraSettingsService settingService, final JiraServices jiraServices) {
        this.settingService = settingService;
        this.jiraServices = jiraServices;
        requiredDefaultFields.add("summary");
        requiredDefaultFields.add("issuetype");
    }

    public FieldLayoutScheme createFieldConfigurationScheme(final List<IssueType> issueTypes, final FieldLayout fieldConfiguration) {
        boolean changesToStore = false;
        FieldLayoutScheme fieldConfigurationScheme = null;

        // Check to see if it already exists
        final FieldLayoutManager fieldLayoutManager = jiraServices.getFieldLayoutManager();
        final List<FieldLayoutScheme> fieldLayoutSchemes = fieldLayoutManager.getFieldLayoutSchemes();
        if (fieldLayoutSchemes != null) {
            for (final FieldLayoutScheme existingFieldConfigurationScheme : fieldLayoutSchemes) {
                final String existingName = existingFieldConfigurationScheme.getName();
                if (BlackDuckJiraConstants.BLACKDUCK_FIELD_CONFIGURATION_SCHEME_NAME.equals(existingName)) {
                    logger.debug("Field Configuration Scheme " + BlackDuckJiraConstants.BLACKDUCK_FIELD_CONFIGURATION_SCHEME_NAME + " already exists");
                    fieldConfigurationScheme = existingFieldConfigurationScheme;
                    break;
                }
            }
        }

        if (fieldConfigurationScheme == null) {
            fieldConfigurationScheme = fieldLayoutManager.createFieldLayoutScheme(BlackDuckJiraConstants.BLACKDUCK_FIELD_CONFIGURATION_SCHEME_NAME, BlackDuckJiraConstants.BLACKDUCK_FIELD_CONFIGURATION_SCHEME_NAME);
            changesToStore = true;
        }

        for (final IssueType issueType : issueTypes) {
            boolean issueTypeAlreadyAssociated = false;
            final Collection<FieldLayoutSchemeEntity> entities = fieldConfigurationScheme.getEntities();
            for (final FieldLayoutSchemeEntity entity : entities) {
                if (entity.getIssueTypeObject() == null) {
                    continue;
                }
                if (entity.getIssueTypeObject().getName().equals(issueType.getName())) {
                    logger.debug("IssueType " + issueType.getName() + " is already associated with Field Configuration ID: " + entity.getFieldLayoutId());
                    logger.debug("\tTarget field configuration ID is: " + fieldConfiguration.getId());
                    if (entity.getFieldLayoutId() != null && entity.getFieldLayoutId().equals(fieldConfiguration.getId())) {
                        issueTypeAlreadyAssociated = true;
                        break;
                    } else {
                        logger.info("\tRemoving incorrect association for IssueType " + issueType.getName());
                        fieldConfigurationScheme.removeEntity(issueType.getId());
                    }
                }
            }

            if (!issueTypeAlreadyAssociated) {
                logger.debug("Associating issue type " + issueType.getName() + " with Field Configuration " + fieldConfiguration.getName());
                final FieldLayoutSchemeEntity issueTypeToFieldConfiguration = new FieldLayoutSchemeEntityImpl(fieldLayoutManager, null, jiraServices.getConstantsManager());
                issueTypeToFieldConfiguration.setFieldLayoutScheme(fieldConfigurationScheme);
                issueTypeToFieldConfiguration.setFieldLayoutId(fieldConfiguration.getId());
                issueTypeToFieldConfiguration.setIssueTypeId(issueType.getId());
                fieldConfigurationScheme.addEntity(issueTypeToFieldConfiguration);
                changesToStore = true;
            }
        }

        if (changesToStore) {
            fieldConfigurationScheme.store();
        }

        return fieldConfigurationScheme;
    }

    public EditableFieldLayout addBlackDuckFieldConfigurationToJira() {
        boolean fieldConfigurationNeedsUpdate = false;
        EditableFieldLayout blackDuckFieldLayout = null;
        try {
            final FieldLayoutManager layoutManager = jiraServices.getFieldLayoutManager();
            final List<EditableFieldLayout> fieldLayouts = layoutManager.getEditableFieldLayouts();
            if (fieldLayouts != null && !fieldLayouts.isEmpty()) {
                for (final EditableFieldLayout layout : fieldLayouts) {
                    final String layoutName = layout.getName();
                    if (BlackDuckJiraConstants.BLACKDUCK_FIELD_CONFIGURATION.equals(layoutName)) {
                        logger.debug("addBlackDuckFieldConfigurationToJira(): found Black Duck field configuration: " + layout.getName());
                        blackDuckFieldLayout = layout;
                        break;
                    }
                }
            }
            if (blackDuckFieldLayout == null) {
                final EditableDefaultFieldLayout editableFieldLayout = layoutManager.getEditableDefaultFieldLayout();

                // Creates a copy of the default field layout
                blackDuckFieldLayout = createEditableFieldLayout(editableFieldLayout.getFieldLayoutItems());

                blackDuckFieldLayout.setName(BlackDuckJiraConstants.BLACKDUCK_FIELD_CONFIGURATION);
                fieldConfigurationNeedsUpdate = true;
            }
            final List<FieldLayoutItem> fields = blackDuckFieldLayout.getFieldLayoutItems();
            if (fields != null && !fields.isEmpty()) {
                for (final FieldLayoutItem field : fields) {
                    final String fieldName = field.getOrderableField().getName();
                    String normalizedFieldName = fieldName.replace(" ", "");
                    normalizedFieldName = normalizedFieldName.toLowerCase();
                    if (!requiredDefaultFields.contains(normalizedFieldName) && field.isRequired()) {
                        logger.debug("addBlackDuckFieldConfigurationToJira(): Making field optional");
                        try {
                            blackDuckFieldLayout.makeOptional(field);
                            fieldConfigurationNeedsUpdate = true;
                        } catch (final IllegalArgumentException e) {
                            final String msg = String.format("Unable to make field %s optional: %s", fieldName, e.getMessage());
                            if ("Assignee".equals(fieldName)) {
                                logger.debug(msg);
                            } else {
                                logger.error(msg);
                                settingService.addBlackDuckError(msg, "addBlackDuckFieldConfigurationToJira");
                            }
                        }
                    }
                }
            }
            if (fieldConfigurationNeedsUpdate) {
                // Persists our field configuration, creates it if it doesn't exist, updates it if it does exist
                logger.debug("addBlackDuckFieldConfigurationToJira(): Updating Black Duck field configuration");
                layoutManager.storeEditableFieldLayout(blackDuckFieldLayout);
            }
        } catch (final Exception e) {
            logger.error(e);
            settingService.addBlackDuckError(e, "addBlackDuckFieldConfigurationToJira");
        }
        return blackDuckFieldLayout;
    }

    public EditableFieldLayout createEditableFieldLayout(final List<FieldLayoutItem> fields) {
        return new EditableFieldLayoutImpl(null, fields);
    }

    private void renameFieldConfigurationScheme(final FieldLayoutManager fieldLayoutManager, final FieldLayoutScheme oldFieldConfigurationScheme) {
        logger.debug("Updating Field Configuration Scheme name and description...");
        oldFieldConfigurationScheme.setName(BlackDuckJiraConstants.BLACKDUCK_FIELD_CONFIGURATION_SCHEME_NAME);
        oldFieldConfigurationScheme.setDescription(BlackDuckJiraConstants.BLACKDUCK_FIELD_CONFIGURATION_SCHEME_NAME);
        fieldLayoutManager.updateFieldLayoutScheme(oldFieldConfigurationScheme);
    }
}
