/**
 * Black Duck JIRA Plugin
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
package com.blackducksoftware.integration.jira.task.issue.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.user.ApplicationUser;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.JiraUserContext;
import com.blackducksoftware.integration.jira.common.TicketInfoFromSetup;
import com.blackducksoftware.integration.jira.common.model.PluginField;
import com.blackducksoftware.integration.jira.config.JiraServices;
import com.blackducksoftware.integration.jira.config.JiraSettingsService;
import com.blackducksoftware.integration.jira.config.model.ProjectFieldCopyMapping;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventData;

public class IssueFieldHandler {
    private final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final JiraServices jiraServices;
    private final JiraSettingsService jiraSettingsService;
    private final TicketInfoFromSetup ticketInfoFromSetup;
    private final JiraUserContext jiraContext;

    public IssueFieldHandler(final JiraServices jiraServices, final JiraSettingsService jiraSettingsService, final JiraUserContext jiraContext, final TicketInfoFromSetup ticketInfoFromSetup) {
        this.jiraServices = jiraServices;
        this.jiraSettingsService = jiraSettingsService;
        this.jiraContext = jiraContext;
        this.ticketInfoFromSetup = ticketInfoFromSetup;
    }

    public void addLabels(final MutableIssue issue, final List<String> labels) {
        for (final String label : labels) {
            logger.debug("Adding label: " + label);
            jiraServices.getLabelManager().addLabel(jiraContext.getJiraIssueCreatorUser(), issue.getId(), label, false);
        }
    }

    public void setPluginFieldValues(final EventData eventData, final IssueInputParameters issueInputParameters) {
        if (ticketInfoFromSetup != null && ticketInfoFromSetup.getCustomFields() != null && !ticketInfoFromSetup.getCustomFields().isEmpty()) {
            addIssueInputParameter(eventData, PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT, issueInputParameters, eventData.getBlackDuckProjectName());
            addIssueInputParameter(eventData, PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION, issueInputParameters, eventData.getBlackDuckProjectVersion());
            addIssueInputParameter(eventData, PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION_URL, issueInputParameters, eventData.getBlackDuckProjectVersionUrl());
            addIssueInputParameter(eventData, PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION_NICKNAME, issueInputParameters, eventData.getBlackDuckProjectVersionNickname());

            addIssueInputParameter(eventData, PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT, issueInputParameters, eventData.getBlackDuckComponentName());
            addIssueInputParameter(eventData, PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT_URL, issueInputParameters, eventData.getBlackDuckComponentUrl());
            addIssueInputParameter(eventData, PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT_VERSION, issueInputParameters, eventData.getBlackDuckComponentVersion());
            addIssueInputParameter(eventData, PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT_VERSION_URL, issueInputParameters, eventData.getBlackDuckComponentVersionUrl());
            addIssueInputParameter(eventData, PluginField.BLACKDUCK_CUSTOM_FIELD_LICENSE_NAMES, issueInputParameters, eventData.getBlackDuckLicenseNames());
            addIssueInputParameter(eventData, PluginField.BLACKDUCK_CUSTOM_FIELD_LICENSE_URL, issueInputParameters, eventData.getBlackDuckLicenseUrl());

            addIssueInputParameter(eventData, PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT_USAGE, issueInputParameters, eventData.getBlackDuckComponentUsage());
            addIssueInputParameter(eventData, PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT_ORIGIN, issueInputParameters, eventData.getBlackDuckComponentOrigin());
            addIssueInputParameter(eventData, PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT_ORIGIN_ID, issueInputParameters, eventData.getBlackDuckComponentOriginId());
            addIssueInputParameter(eventData, PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION_LAST_UPDATED, issueInputParameters, eventData.getBlackDuckProjectVersionLastUpdated());

            if (eventData.isPolicy()) {
                addIssueInputParameter(eventData, PluginField.BLACKDUCK_CUSTOM_FIELD_POLICY_RULE, issueInputParameters, eventData.getBlackDuckRuleName());
                addIssueInputParameter(eventData, PluginField.BLACKDUCK_CUSTOM_FIELD_POLICY_RULE_OVERRIDABLE, issueInputParameters, eventData.getBlackDuckRuleOverridable());
                addIssueInputParameter(eventData, PluginField.BLACKDUCK_CUSTOM_FIELD_POLICY_RULE_DESCRIPTION, issueInputParameters, eventData.getBlackDuckRuleDescription());
                // TODO use this when the Hub supports policy redirect: addIssueInputParameter(eventData, PluginField.HUB_CUSTOM_FIELD_POLICY_RULE_URL, issueInputParameters, eventData.getHubRuleUrl());
                addIssueInputParameter(eventData, PluginField.BLACKDUCK_CUSTOM_FIELD_POLICY_RULE_URL, issueInputParameters, eventData.getBlackDuckBaseUrl() + "/ui/policy-management");
            }
            if (eventData.getBlackDuckProjectOwner() != null) {
                addIssueInputParameter(eventData, PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT_OWNER, issueInputParameters, eventData.getBlackDuckProjectOwner());
            }
        }
    }

    private void addIssueInputParameter(final EventData eventData, final PluginField pluginField, final IssueInputParameters issueInputParameters, final ApplicationUser fieldValue) {
        addIssueInputParameter(eventData, pluginField, issueInputParameters, fieldValue.getUsername());
    }

    private void addIssueInputParameter(final EventData eventData, final PluginField pluginField, final IssueInputParameters issueInputParameters, final String fieldValue) {
        final CustomField jiraCustomField = ticketInfoFromSetup.getCustomFields().get(pluginField);
        if (jiraCustomField != null) {
            final Long fieldId = jiraCustomField.getIdAsLong();
            issueInputParameters.addCustomFieldValue(fieldId, fieldValue);
        } else {
            final String errorMessage = "JIRA custom field " + pluginField.getName() + " not found";
            logger.error(errorMessage);
            jiraSettingsService.addBlackDuckError(errorMessage,
                    eventData.getBlackDuckProjectName(),
                    eventData.getBlackDuckProjectVersion(),
                    eventData.getJiraProjectName(),
                    eventData.getJiraAdminUsername(),
                    eventData.getJiraIssueCreatorUsername(),
                    "addIssueInputParameter");
        }
    }

    public List<String> setOtherFieldValues(final EventData eventData, final IssueInputParameters issueInputParameters) {
        final List<String> labels = new ArrayList<>();
        final Set<ProjectFieldCopyMapping> projectFieldCopyMappings = eventData.getJiraFieldCopyMappings();
        if (projectFieldCopyMappings == null || projectFieldCopyMappings.size() == 0) {
            logger.debug("projectFieldCopyMappings is empty");
            return labels;
        }

        for (final ProjectFieldCopyMapping fieldCopyMapping : projectFieldCopyMappings) {
            logger.debug("projectFieldCopyMapping: " + fieldCopyMapping);
            if ((!eventData.getJiraProjectName().equals(fieldCopyMapping.getJiraProjectName()))
                    && (!BlackDuckJiraConstants.FIELD_COPY_MAPPING_WILDCARD.equals(fieldCopyMapping.getJiraProjectName()))) {
                logger.debug("This field copy mapping is for JIRA project " + fieldCopyMapping.getJiraProjectName() + "; skipping it");
                continue;
            }
            logger.debug("This field copy mapping is for this JIRA project (or all JIRA projects); using it");

            final String targetFieldId = fieldCopyMapping.getTargetFieldId();
            logger.debug("Setting field with ID " + targetFieldId + " from field " + fieldCopyMapping.getSourceFieldName());

            final Field targetField = jiraServices.getFieldManager().getField(targetFieldId);
            logger.debug("\ttargetField: " + targetField);
            if (targetField == null) {
                final String errorMessage = "Custom field with ID " + targetFieldId + " not found; won't be set";
                logger.error(errorMessage);
                jiraSettingsService.addBlackDuckError(errorMessage,
                        eventData.getBlackDuckProjectName(),
                        eventData.getBlackDuckProjectVersion(),
                        eventData.getJiraProjectName(),
                        eventData.getJiraAdminUsername(),
                        eventData.getJiraIssueCreatorUsername(),
                        "setOtherFieldValues");
                continue;
            }

            final String fieldValue = getPluginFieldValue(eventData, fieldCopyMapping.getSourceFieldId());
            if (fieldValue == null) {
                continue;
            }
            logger.debug("New target field value: " + fieldValue);

            if (targetField.getId().startsWith(FieldManager.CUSTOM_FIELD_PREFIX)) {
                logger.debug("Setting custom field " + targetField.getName() + " to " + fieldValue);
                issueInputParameters.addCustomFieldValue(targetField.getId(), fieldValue);
            } else {
                logger.debug("Setting standard field " + targetField.getName() + " to " + fieldValue);
                final String label = setSystemField(eventData, issueInputParameters, targetField, fieldValue);
                if (label != null) {
                    labels.add(label);
                }
            }
        }
        return labels;
    }

    /**
     * If target field is labels field, the label value is returned (labels cannot be applied to an issue during creation).
     */
    private String setSystemField(final EventData eventData, final IssueInputParameters issueInputParameters, final Field targetField, final String targetFieldValue) {
        if (targetField.getId().equals(BlackDuckJiraConstants.VERSIONS_FIELD_ID)) {
            setAffectedVersion(eventData, issueInputParameters, targetFieldValue);
        } else if (targetField.getId().equals(BlackDuckJiraConstants.COMPONENTS_FIELD_ID)) {
            setComponent(eventData, issueInputParameters, targetFieldValue);
        } else if (targetField.getId().equals("labels")) {
            logger.debug("Recording label to add after issue is created: " + targetFieldValue);
            return targetFieldValue;
        } else {
            final String errorMessage = "Unrecognized field id (" + targetField.getId() + "); field cannot be set";
            logger.error(errorMessage);
            jiraSettingsService.addBlackDuckError(errorMessage,
                    eventData.getBlackDuckProjectName(),
                    eventData.getBlackDuckProjectVersion(),
                    eventData.getJiraProjectName(),
                    eventData.getJiraAdminUsername(),
                    eventData.getJiraIssueCreatorUsername(),
                    "setSystemField");
        }
        return null;
    }

    private void setComponent(final EventData eventData, final IssueInputParameters issueInputParameters, final String targetFieldValue) {
        Long compId = null;
        final Collection<ProjectComponent> components = jiraServices.getJiraProjectManager().getProjectObj(eventData.getJiraProjectId()).getComponents();
        for (final ProjectComponent component : components) {
            if (targetFieldValue.equals(component.getName())) {
                compId = component.getId();
                break;
            }
        }
        if (compId != null) {
            issueInputParameters.setComponentIds(compId);
        } else {
            final String errorMessage = "No component matching '" + targetFieldValue + "' found on project";
            logger.error(errorMessage);
            jiraSettingsService.addBlackDuckError(errorMessage,
                    eventData.getBlackDuckProjectName(),
                    eventData.getBlackDuckProjectVersion(),
                    eventData.getJiraProjectName(),
                    eventData.getJiraAdminUsername(),
                    eventData.getJiraIssueCreatorUsername(),
                    "setComponent");
        }
    }

    private void setAffectedVersion(final EventData eventData, final IssueInputParameters issueInputParameters, final String targetFieldValue) {
        Long versionId = null;
        final Collection<Version> versions = jiraServices.getJiraProjectManager()
                .getProjectObj(eventData.getJiraProjectId()).getVersions();
        for (final Version version : versions) {
            if (targetFieldValue.equals(version.getName())) {
                versionId = version.getId();
                break;
            }
        }
        if (versionId != null) {
            issueInputParameters.setAffectedVersionIds(versionId);
        } else {
            final String errorMessage = "No version matching '" + targetFieldValue + "' found on project";
            logger.error(errorMessage);
            jiraSettingsService.addBlackDuckError(errorMessage,
                    eventData.getBlackDuckProjectName(),
                    eventData.getBlackDuckProjectVersion(),
                    eventData.getJiraProjectName(),
                    eventData.getJiraAdminUsername(),
                    eventData.getJiraIssueCreatorUsername(),
                    "setAffectedVersion");
        }
    }

    private String getPluginFieldValue(final EventData eventData, final String pluginFieldId) {
        String fieldValue = null;
        if (PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT.getId().equals(pluginFieldId)) {
            fieldValue = eventData.getBlackDuckComponentName();
        } else if (PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT_URL.getId().equals(pluginFieldId)) {
            fieldValue = eventData.getBlackDuckComponentUrl();
        } else if (PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT_VERSION.getId().equals(pluginFieldId)) {
            fieldValue = eventData.getBlackDuckComponentVersion();
        } else if (PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT_VERSION_URL.getId().equals(pluginFieldId)) {
            fieldValue = eventData.getBlackDuckComponentVersionUrl();
        } else if (PluginField.BLACKDUCK_CUSTOM_FIELD_LICENSE_NAMES.getId().equals(pluginFieldId)) {
            fieldValue = eventData.getBlackDuckLicenseNames();
        } else if (PluginField.BLACKDUCK_CUSTOM_FIELD_LICENSE_URL.getId().equals(pluginFieldId)) {
            fieldValue = eventData.getBlackDuckLicenseUrl();
        } else if (PluginField.BLACKDUCK_CUSTOM_FIELD_POLICY_RULE.getId().equals(pluginFieldId)) {
            fieldValue = getPolicyFieldValue(eventData, eventData.getBlackDuckRuleName());
        } else if (PluginField.BLACKDUCK_CUSTOM_FIELD_POLICY_RULE_OVERRIDABLE.getId().equals(pluginFieldId)) {
            fieldValue = getPolicyFieldValue(eventData, eventData.getBlackDuckRuleOverridable());
        } else if (PluginField.BLACKDUCK_CUSTOM_FIELD_POLICY_RULE_DESCRIPTION.getId().equals(pluginFieldId)) {
            fieldValue = getPolicyFieldValue(eventData, eventData.getBlackDuckRuleDescription());
        } else if (PluginField.BLACKDUCK_CUSTOM_FIELD_POLICY_RULE_URL.getId().equals(pluginFieldId)) {
            // TODO use this when the Hub supports policy redirect: fieldValue = getPolicyFieldValue(eventData, eventData.getHubRuleUrl());
            fieldValue = eventData.getBlackDuckBaseUrl() + "/ui/policy-management";
        } else if (PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT.getId().equals(pluginFieldId)) {
            fieldValue = eventData.getBlackDuckProjectName();
        } else if (PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION.getId().equals(pluginFieldId)) {
            fieldValue = eventData.getBlackDuckProjectVersion();
        } else if (PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION_URL.getId().equals(pluginFieldId)) {
            fieldValue = eventData.getBlackDuckProjectVersionUrl();
        } else if (PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT_OWNER.getId().equals(pluginFieldId)) {
            fieldValue = eventData.getBlackDuckProjectOwner() != null ? eventData.getBlackDuckProjectOwner().getUsername() : "";
        } else if (PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION_LAST_UPDATED.getId().equals(pluginFieldId)) {
            fieldValue = eventData.getBlackDuckProjectVersionLastUpdated();
        } else {
            final String errorMessage = "Unrecognized plugin field ID: " + pluginFieldId;
            logger.error(errorMessage);
            jiraSettingsService.addBlackDuckError(errorMessage,
                    eventData.getBlackDuckProjectName(),
                    eventData.getBlackDuckProjectVersion(),
                    eventData.getJiraProjectName(),
                    eventData.getJiraAdminUsername(),
                    eventData.getJiraIssueCreatorUsername(),
                    "getPluginFieldValue");
        }

        return fieldValue;
    }

    private String getPolicyFieldValue(final EventData eventData, final String value) {
        if (eventData.isPolicy()) {
            return value;
        }
        logger.debug("Skipping field " + PluginField.BLACKDUCK_CUSTOM_FIELD_POLICY_RULE.getName() + " for vulnerability issue");
        return null;
    }

}
