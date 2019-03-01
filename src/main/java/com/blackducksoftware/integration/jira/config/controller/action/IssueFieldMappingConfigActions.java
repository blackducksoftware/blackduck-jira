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
package com.blackducksoftware.integration.jira.config.controller.action;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.PluginSettingsWrapper;
import com.blackducksoftware.integration.jira.common.exception.JiraException;
import com.blackducksoftware.integration.jira.common.model.BlackDuckProjectMapping;
import com.blackducksoftware.integration.jira.common.model.PluginField;
import com.blackducksoftware.integration.jira.config.IdToNameMappingByNameComparator;
import com.blackducksoftware.integration.jira.config.JiraConfigErrorStrings;
import com.blackducksoftware.integration.jira.config.model.BlackDuckJiraConfigSerializable;
import com.blackducksoftware.integration.jira.config.model.BlackDuckJiraFieldCopyConfigSerializable;
import com.blackducksoftware.integration.jira.config.model.Fields;
import com.blackducksoftware.integration.jira.config.model.IdToNameMapping;
import com.blackducksoftware.integration.jira.config.model.ProjectFieldCopyMapping;
import com.blackducksoftware.integration.jira.task.issue.ui.JiraFieldUtils;

public class IssueFieldMappingConfigActions {
    final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));
    private final PluginSettingsFactory pluginSettingsFactory;
    private final Properties i18nProperties;
    private final FieldManager fieldManager;

    public IssueFieldMappingConfigActions(final PluginSettingsFactory pluginSettingsFactory, final Properties i18nProperties, final FieldManager fieldManager) {
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.i18nProperties = i18nProperties;
        this.fieldManager = fieldManager;
        populateI18nProperties();
    }

    public BlackDuckJiraConfigSerializable getMappings() {
        final PluginSettingsWrapper pluginSettingsWrapper = createPluginSettingsWrapper();
        final BlackDuckJiraConfigSerializable txConfig = new BlackDuckJiraConfigSerializable();
        final String blackDuckProjectMappingsJson = pluginSettingsWrapper.getProjectMappingsJson();

        txConfig.setHubProjectMappingsJson(blackDuckProjectMappingsJson);

        validateMapping(txConfig);
        return txConfig;
    }

    public Fields getSourceFields() {
        final Fields txSourceFields = new Fields();
        logger.debug("Adding source fields");
        txSourceFields.add(new IdToNameMapping(PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT.getId(), getI18nProperty(PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT.getLongNameProperty())));
        txSourceFields.add(new IdToNameMapping(PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION.getId(), getI18nProperty(PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION.getLongNameProperty())));
        txSourceFields.add(new IdToNameMapping(PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT.getId(), getI18nProperty(PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT.getLongNameProperty())));
        txSourceFields.add(new IdToNameMapping(PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT_VERSION.getId(), getI18nProperty(PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT_VERSION.getLongNameProperty())));
        txSourceFields.add(new IdToNameMapping(PluginField.BLACKDUCK_CUSTOM_FIELD_POLICY_RULE.getId(), getI18nProperty(PluginField.BLACKDUCK_CUSTOM_FIELD_POLICY_RULE.getLongNameProperty())));
        txSourceFields.add(new IdToNameMapping(PluginField.BLACKDUCK_CUSTOM_FIELD_LICENSE_NAMES.getId(), getI18nProperty(PluginField.BLACKDUCK_CUSTOM_FIELD_LICENSE_NAMES.getLongNameProperty())));
        txSourceFields.add(new IdToNameMapping(PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT_USAGE.getId(), getI18nProperty(PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT_USAGE.getLongNameProperty())));
        txSourceFields.add(new IdToNameMapping(PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT_OWNER.getId(), getI18nProperty(PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT_OWNER.getLongNameProperty())));
        txSourceFields.add(new IdToNameMapping(PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION_LAST_UPDATED.getId(), getI18nProperty(PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION_LAST_UPDATED.getLongNameProperty())));
        txSourceFields.add(new IdToNameMapping(PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT_ORIGIN.getId(), getI18nProperty(PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT_ORIGIN.getLongNameProperty())));
        txSourceFields.add(new IdToNameMapping(PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT_ORIGIN_ID.getId(), getI18nProperty(PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT_ORIGIN_ID.getLongNameProperty())));
        txSourceFields.add(new IdToNameMapping(PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION_NICKNAME.getId(), getI18nProperty(PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION_NICKNAME.getLongNameProperty())));
        Collections.sort(txSourceFields.getIdToNameMappings(), new IdToNameMappingByNameComparator());
        logger.debug("sourceFields: " + txSourceFields);
        return txSourceFields;
    }

    public Fields getTargetFields() {
        Fields txTargetFields;
        try {
            txTargetFields = JiraFieldUtils.getTargetFields(logger, fieldManager);
        } catch (final JiraException e) {
            txTargetFields = new Fields();
            txTargetFields.setErrorMessage("Error getting target field list: " + e.getMessage());
            return txTargetFields;
        }
        Collections.sort(txTargetFields.getIdToNameMappings(), new IdToNameMappingByNameComparator());
        logger.debug("targetFields: " + txTargetFields);
        return txTargetFields;
    }

    public BlackDuckJiraFieldCopyConfigSerializable getFieldCopyMappings() {
        final PluginSettingsWrapper pluginSettingsWrapper = createPluginSettingsWrapper();
        final BlackDuckJiraFieldCopyConfigSerializable txConfig = new BlackDuckJiraFieldCopyConfigSerializable();
        final String blackDuckFieldCopyMappingsJson = pluginSettingsWrapper.getFieldMappingsCopyJson();

        logger.debug("Get /copies returning JSON: " + blackDuckFieldCopyMappingsJson);
        txConfig.setJson(blackDuckFieldCopyMappingsJson);
        logger.debug("BlackDuckJiraFieldCopyConfigSerializable.getJson(): " + txConfig.getJson());
        return txConfig;
    }

    public Void updateFieldCopyMappings(final BlackDuckJiraFieldCopyConfigSerializable fieldCopyConfig) {
        if (!isValid(fieldCopyConfig)) {
            return null;
        }

        final PluginSettingsWrapper pluginSettingsWrapper = createPluginSettingsWrapper();
        pluginSettingsWrapper.setFieldMappingsCopyJson(fieldCopyConfig.getJson());
        return null;
    }

    // This must be "package protected" to avoid synthetic access
    private void validateMapping(final BlackDuckJiraConfigSerializable config) {
        if (config.getHubProjectMappings() != null && !config.getHubProjectMappings().isEmpty()) {
            boolean hasEmptyMapping = false;
            for (final BlackDuckProjectMapping mapping : config.getHubProjectMappings()) {
                boolean jiraProjectBlank = true;
                boolean blackDuckProjectBlank = true;
                if (mapping.getJiraProject() != null) {
                    if (mapping.getJiraProject().getProjectId() != null) {
                        jiraProjectBlank = false;
                    }
                }
                if (StringUtils.isNotBlank(mapping.getBlackDuckProjectName())) {
                    blackDuckProjectBlank = false;
                }
                if (jiraProjectBlank || blackDuckProjectBlank) {
                    hasEmptyMapping = true;
                }
            }
            if (hasEmptyMapping) {
                config.setHubProjectMappingError(StringUtils.joinWith(" : ", config.getHubProjectMappingError(), JiraConfigErrorStrings.MAPPING_HAS_EMPTY_ERROR));
            }
        }
    }

    private boolean isValid(final BlackDuckJiraFieldCopyConfigSerializable fieldCopyConfig) {
        if (fieldCopyConfig.getProjectFieldCopyMappings().size() == 0) {
            fieldCopyConfig.setErrorMessage(JiraConfigErrorStrings.NO_VALID_FIELD_CONFIGURATIONS);
            return false;
        }

        for (final ProjectFieldCopyMapping projectFieldCopyMapping : fieldCopyConfig.getProjectFieldCopyMappings()) {
            if (StringUtils.isBlank(projectFieldCopyMapping.getSourceFieldId())) {
                fieldCopyConfig.setErrorMessage(JiraConfigErrorStrings.FIELD_CONFIGURATION_INVALID_SOURCE_FIELD);
                return false;
            }
            if (StringUtils.isBlank(projectFieldCopyMapping.getTargetFieldId())) {
                fieldCopyConfig.setErrorMessage(JiraConfigErrorStrings.FIELD_CONFIGURATION_INVALID_TARGET_FIELD);
                return false;
            }
            if (StringUtils.isBlank(projectFieldCopyMapping.getSourceFieldName())) {
                fieldCopyConfig.setErrorMessage(JiraConfigErrorStrings.FIELD_CONFIGURATION_INVALID_SOURCE_FIELD);
                return false;
            }
            if (StringUtils.isBlank(projectFieldCopyMapping.getTargetFieldName())) {
                fieldCopyConfig.setErrorMessage(JiraConfigErrorStrings.FIELD_CONFIGURATION_INVALID_TARGET_FIELD);
                return false;
            }
        }
        return true;
    }

    private String getI18nProperty(final String key) {
        if (i18nProperties == null) {
            return key;
        }
        final String value = i18nProperties.getProperty(key);
        if (value == null) {
            return key;
        }
        return value;
    }

    private void populateI18nProperties() {
        try (final InputStream stream = ClassLoaderUtils.getResourceAsStream(BlackDuckJiraConstants.PROPERTY_FILENAME, this.getClass())) {
            if (stream != null) {
                i18nProperties.load(stream);
            } else {
                logger.warn("Error opening property file: " + BlackDuckJiraConstants.PROPERTY_FILENAME);
            }
        } catch (final IOException e) {
            logger.warn("Error reading property file: " + BlackDuckJiraConstants.PROPERTY_FILENAME);
        }
        logger.debug("i18nProperties: " + i18nProperties);
    }

    private PluginSettingsWrapper createPluginSettingsWrapper() {
        final PluginSettings globalSettings = pluginSettingsFactory.createGlobalSettings();
        return new PluginSettingsWrapper(globalSettings);
    }
}