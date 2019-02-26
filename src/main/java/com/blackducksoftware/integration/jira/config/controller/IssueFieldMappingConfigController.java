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
package com.blackducksoftware.integration.jira.config.controller;

import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;

import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
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

@Path("/config/issue/field/mapping")
public class IssueFieldMappingConfigController extends ConfigController {

    final FieldManager fieldManager;

    public IssueFieldMappingConfigController(final PluginSettingsFactory pluginSettingsFactory, final TransactionTemplate transactionTemplate,
        final UserManager userManager, final FieldManager fieldManager) {
        super(pluginSettingsFactory, transactionTemplate, userManager);
        this.fieldManager = fieldManager;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMappings(@Context final HttpServletRequest request) {
        final Object config;
        try {
            final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
            final PluginSettingsWrapper pluginSettingsWrapper = new PluginSettingsWrapper(settings);
            final boolean validAuthentication = getAuthenticationChecker().isValidAuthentication(request, pluginSettingsWrapper.getParsedBlackDuckConfigGroups());
            if (!validAuthentication) {
                return Response.status(Status.UNAUTHORIZED).build();
            }
            config = getTransactionTemplate().execute(new TransactionCallback() {
                @Override
                public Object doInTransaction() {
                    final BlackDuckJiraConfigSerializable txConfig = new BlackDuckJiraConfigSerializable();
                    final String blackDuckProjectMappingsJson = pluginSettingsWrapper.getProjectMappingsJson();

                    txConfig.setHubProjectMappingsJson(blackDuckProjectMappingsJson);

                    validateMapping(txConfig);
                    return txConfig;
                }
            });
        } catch (final Exception e) {
            final BlackDuckJiraConfigSerializable errorConfig = new BlackDuckJiraConfigSerializable();
            final String msg = "Error getting project mappings: " + e.getMessage();
            logger.error(msg, e);
            return Response.ok(errorConfig).build();
        }
        return Response.ok(config).build();
    }

    @Path("/sources")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSourceFields(@Context final HttpServletRequest request) {
        final Object sourceFields;
        try {
            final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
            final PluginSettingsWrapper pluginSettingsWrapper = new PluginSettingsWrapper(settings);
            final boolean validAuthentication = getAuthenticationChecker().isValidAuthentication(request, pluginSettingsWrapper.getParsedBlackDuckConfigGroups());
            if (!validAuthentication) {
                return Response.status(Status.UNAUTHORIZED).build();
            }

            sourceFields = getTransactionTemplate().execute(new TransactionCallback() {
                @Override
                public Object doInTransaction() {
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
            });
        } catch (final Exception e) {
            final Fields errorSourceFields = new Fields();
            final String msg = "Error getting source fields: " + e.getMessage();
            logger.error(msg, e);
            errorSourceFields.setErrorMessage(msg);
            return Response.ok(errorSourceFields).build();
        }
        return Response.ok(sourceFields).build();
    }

    @Path("/targets")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTargetFields(@Context final HttpServletRequest request) {
        final Object targetFields;
        try {
            final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
            final PluginSettingsWrapper pluginSettingsWrapper = new PluginSettingsWrapper(settings);
            final boolean validAuthentication = getAuthenticationChecker().isValidAuthentication(request, pluginSettingsWrapper.getParsedBlackDuckConfigGroups());
            if (!validAuthentication) {
                return Response.status(Status.UNAUTHORIZED).build();
            }

            targetFields = getTransactionTemplate().execute(new TransactionCallback() {
                @Override
                public Object doInTransaction() {
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
            });
        } catch (final Exception e) {
            final Fields errorTargetFields = new Fields();
            final String msg = "Error getting target fields: " + e.getMessage();
            logger.error(msg, e);
            errorTargetFields.setErrorMessage(msg);
            return Response.ok(errorTargetFields).build();
        }
        return Response.ok(targetFields).build();
    }

    @Path("/copies")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFieldCopyMappings(@Context final HttpServletRequest request) {
        Object config = null;
        try {
            logger.debug("Get /copies");
            final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
            final PluginSettingsWrapper pluginSettingsWrapper = new PluginSettingsWrapper(settings);
            final boolean validAuthentication = getAuthenticationChecker().isValidAuthentication(request, pluginSettingsWrapper.getParsedBlackDuckConfigGroups());
            if (!validAuthentication) {
                return Response.status(Status.UNAUTHORIZED).build();
            }
            config = getTransactionTemplate().execute(new TransactionCallback() {
                @Override
                public Object doInTransaction() {
                    final BlackDuckJiraFieldCopyConfigSerializable txConfig = new BlackDuckJiraFieldCopyConfigSerializable();
                    final String blackDuckFieldCopyMappingsJson = pluginSettingsWrapper.getFieldMappingsCopyJson();

                    logger.debug("Get /copies returning JSON: " + blackDuckFieldCopyMappingsJson);
                    txConfig.setJson(blackDuckFieldCopyMappingsJson);
                    logger.debug("BlackDuckJiraFieldCopyConfigSerializable.getJson(): " + txConfig.getJson());
                    return txConfig;
                }
            });
        } catch (final Exception e) {
            final BlackDuckJiraConfigSerializable errorConfig = new BlackDuckJiraConfigSerializable();
            final String msg = "Error getting field mappings: " + e.getMessage();
            logger.error(msg, e);
            return Response.ok(errorConfig).build();
        }

        final BlackDuckJiraFieldCopyConfigSerializable returnValue = (BlackDuckJiraFieldCopyConfigSerializable) config;
        logger.debug("returnValue: " + returnValue);
        return Response.ok(config).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateFieldCopyConfiguration(final BlackDuckJiraFieldCopyConfigSerializable fieldCopyConfig, @Context final HttpServletRequest request) {
        try {
            logger.debug("updateFieldCopyConfiguration() received " + fieldCopyConfig.getProjectFieldCopyMappings().size() + " rows.");
            logger.debug("fieldCopyConfig.getProjectFieldCopyMappings(): " + fieldCopyConfig.getProjectFieldCopyMappings());
            for (final ProjectFieldCopyMapping projectFieldCopyMapping : fieldCopyConfig.getProjectFieldCopyMappings()) {
                logger.debug("projectFieldCopyMapping: " + projectFieldCopyMapping);
            }

            final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
            final PluginSettingsWrapper pluginSettingsWrapper = new PluginSettingsWrapper(settings);
            final boolean validAuthentication = getAuthenticationChecker().isValidAuthentication(request, pluginSettingsWrapper.getParsedBlackDuckConfigGroups());
            if (!validAuthentication) {
                return Response.status(Status.UNAUTHORIZED).build();
            }
            getTransactionTemplate().execute(new TransactionCallback() {
                @Override
                public Object doInTransaction() {
                    if (!isValid(fieldCopyConfig)) {
                        return null;
                    }

                    pluginSettingsWrapper.setFieldMappingsCopyJson(fieldCopyConfig.getJson());
                    return null;
                }
            });
        } catch (final Exception e) {
            final String msg = "Exception during admin save: " + e.getMessage();
            logger.error(msg, e);
            fieldCopyConfig.setErrorMessage(msg);
        }
        if (fieldCopyConfig.hasErrors()) {
            return Response.ok(fieldCopyConfig).status(Status.BAD_REQUEST).build();
        }
        return Response.noContent().build();
    }

    // This must be "package protected" to avoid synthetic access
    void validateMapping(final BlackDuckJiraConfigSerializable config) {
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
                config.setHubProjectMappingError(concatErrorMessage(config.getHubProjectMappingError(), JiraConfigErrorStrings.MAPPING_HAS_EMPTY_ERROR));
            }
        }
    }

    // This must be "package protected" to avoid synthetic access
    boolean isValid(final BlackDuckJiraFieldCopyConfigSerializable fieldCopyConfig) {
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
}
