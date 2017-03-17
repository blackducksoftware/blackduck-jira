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
package com.blackducksoftware.integration.jira.task.issue;

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
import com.blackducksoftware.integration.hub.notification.processor.event.NotificationEvent;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.common.JiraContext;
import com.blackducksoftware.integration.jira.common.PluginField;
import com.blackducksoftware.integration.jira.common.TicketInfoFromSetup;
import com.blackducksoftware.integration.jira.config.ProjectFieldCopyMapping;
import com.blackducksoftware.integration.jira.task.JiraSettingsService;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventData;

public class IssueFieldHandler {

    private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final JiraServices jiraServices;

    private final JiraSettingsService jiraSettingsService;

    private final TicketInfoFromSetup ticketInfoFromSetup;

    private final JiraContext jiraContext;

    public IssueFieldHandler(final JiraServices jiraServices, final JiraSettingsService jiraSettingsService, final JiraContext jiraContext,
            final TicketInfoFromSetup ticketInfoFromSetup) {
        this.jiraServices = jiraServices;
        this.jiraSettingsService = jiraSettingsService;
        this.jiraContext = jiraContext;
        this.ticketInfoFromSetup = ticketInfoFromSetup;
    }

    public void addLabels(final MutableIssue issue, final List<String> labels) {
        for (final String label : labels) {
            logger.debug("Adding label: " + label);
            jiraServices.getLabelManager().addLabel(jiraContext.getJiraAdminUser(), issue.getId(), label, false);
        }
    }

    public void setPluginFieldValues(final NotificationEvent notificationEvent, final EventData eventData,
            final IssueInputParameters issueInputParameters) {
        if (ticketInfoFromSetup != null && ticketInfoFromSetup.getCustomFields() != null
                && !ticketInfoFromSetup.getCustomFields().isEmpty()) {
            addIssueInputParameter(eventData, PluginField.HUB_CUSTOM_FIELD_PROJECT, issueInputParameters, eventData.getHubProjectName());
            addIssueInputParameter(eventData, PluginField.HUB_CUSTOM_FIELD_PROJECT_VERSION, issueInputParameters, eventData.getHubProjectVersion());
            addIssueInputParameter(eventData, PluginField.HUB_CUSTOM_FIELD_COMPONENT, issueInputParameters, eventData.getHubComponentName());
            addIssueInputParameter(eventData, PluginField.HUB_CUSTOM_FIELD_COMPONENT_VERSION, issueInputParameters, eventData.getHubComponentVersion());
            addIssueInputParameter(eventData, PluginField.HUB_CUSTOM_FIELD_LICENSE_NAMES, issueInputParameters, eventData.getHubLicenseNames());

            addIssueInputParameter(eventData, PluginField.HUB_CUSTOM_FIELD_COMPONENT_USAGE, issueInputParameters, eventData.getHubComponentUsage());
            addIssueInputParameter(eventData, PluginField.HUB_CUSTOM_FIELD_COMPONENT_ORIGIN, issueInputParameters, eventData.getHubComponentOrigin());
            addIssueInputParameter(eventData, PluginField.HUB_CUSTOM_FIELD_COMPONENT_ORIGIN_ID, issueInputParameters, eventData.getHubComponentOriginId());
            addIssueInputParameter(eventData, PluginField.HUB_CUSTOM_FIELD_PROJECT_VERSION_NICKNAME, issueInputParameters,
                    eventData.getHubProjectVersionNickname());

            if (notificationEvent.isPolicyEvent()) {
                addIssueInputParameter(eventData, PluginField.HUB_CUSTOM_FIELD_POLICY_RULE, issueInputParameters, eventData.getHubRuleName());
            }
        }
    }

    private void addIssueInputParameter(final EventData eventData, final PluginField pluginField,
            final IssueInputParameters issueInputParameters, final String fieldValue) {

        final CustomField jiraCustomField = ticketInfoFromSetup.getCustomFields()
                .get(pluginField);
        if (jiraCustomField == null) {
            final String errorMessage = "JIRA custom field " + pluginField.getName() + " not found";
            logger.error(errorMessage);
            jiraSettingsService.addHubError(errorMessage,
                    eventData.getHubProjectName(),
                    eventData.getHubProjectVersion(),
                    eventData.getJiraProjectName(),
                    eventData.getJiraUserName(),
                    "addIssueInputParameter");
            return;
        }
        final Long fieldId = jiraCustomField.getIdAsLong();
        issueInputParameters.addCustomFieldValue(fieldId,
                fieldValue);
    }

    public List<String> setOtherFieldValues(final NotificationEvent notificationEvent, final EventData eventData,
            final IssueInputParameters issueInputParameters) {
        final List<String> labels = new ArrayList<>();
        final Set<ProjectFieldCopyMapping> projectFieldCopyMappings = eventData.getJiraFieldCopyMappings();
        if (projectFieldCopyMappings == null) {
            logger.debug("projectFieldCopyMappings is null");
            return labels;
        }
        if (projectFieldCopyMappings.size() == 0) {
            logger.debug("projectFieldCopyMappings is null");
            return labels;
        }

        for (final ProjectFieldCopyMapping fieldCopyMapping : projectFieldCopyMappings) {
            logger.debug("projectFieldCopyMappings: " + projectFieldCopyMappings);
            if ((!eventData.getJiraProjectName().equals(fieldCopyMapping.getJiraProjectName()))
                    && (!HubJiraConstants.FIELD_COPY_MAPPING_WILDCARD.equals(fieldCopyMapping.getJiraProjectName()))) {
                logger.debug("This field copy mapping is for JIRA project " + fieldCopyMapping.getJiraProjectName()
                        + "; skipping it");
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
                jiraSettingsService.addHubError(errorMessage,
                        eventData.getHubProjectName(),
                        eventData.getHubProjectVersion(),
                        eventData.getJiraProjectName(),
                        eventData.getJiraUserName(),
                        "setOtherFieldValues");
                continue;
            }

            final String fieldValue = getPluginFieldValue(notificationEvent, eventData, fieldCopyMapping.getSourceFieldId());
            if (fieldValue == null) {
                continue;
            }
            logger.debug("New target field value: " + fieldValue);

            if (targetField.getId().startsWith(FieldManager.CUSTOM_FIELD_PREFIX)) {
                logger.debug("Setting custom field " + targetField.getName() + " to " + fieldValue);
                issueInputParameters.addCustomFieldValue(targetField.getId(), fieldValue);
            } else {
                logger.debug("Setting standard field " + targetField.getName() + " to " + fieldValue);
                final String label = setSystemField(notificationEvent, eventData, issueInputParameters, targetField, fieldValue);
                if (label != null) {
                    labels.add(label);
                }
            }
        }
        return labels;
    }

    /**
     * If target field is labels field, the label value is returned (labels cannot be applied
     * to an issue during creation).
     */
    private String setSystemField(final NotificationEvent notificationEvent, final EventData eventData, final IssueInputParameters issueInputParameters,
            final Field targetField,
            final String targetFieldValue) {
        if (targetField.getId().equals(HubJiraConstants.VERSIONS_FIELD_ID)) {
            setAffectedVersion(notificationEvent, eventData, issueInputParameters, targetFieldValue);
        } else if (targetField.getId().equals(HubJiraConstants.COMPONENTS_FIELD_ID)) {
            setComponent(notificationEvent, eventData, issueInputParameters, targetFieldValue);
        } else if (targetField.getId().equals("labels")) {
            logger.debug("Recording label to add after issue is created: " + targetFieldValue);
            return targetFieldValue;
        } else {
            final String errorMessage = "Unrecognized field id (" + targetField.getId() + "); field cannot be set";
            logger.error(errorMessage);
            jiraSettingsService.addHubError(errorMessage,
                    eventData.getHubProjectName(),
                    eventData.getHubProjectVersion(),
                    eventData.getJiraProjectName(),
                    eventData.getJiraUserName(),
                    "setSystemField");
        }
        return null;
    }

    private void setComponent(final NotificationEvent notificationEvent, final EventData eventData, final IssueInputParameters issueInputParameters,
            final String targetFieldValue) {
        Long compId = null;
        final Collection<ProjectComponent> components = jiraServices.getJiraProjectManager()
                .getProjectObj(eventData.getJiraProjectId())
                .getComponents();
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
            jiraSettingsService.addHubError(errorMessage,
                    eventData.getHubProjectName(),
                    eventData.getHubProjectVersion(),
                    eventData.getJiraProjectName(),
                    eventData.getJiraUserName(),
                    "setComponent");
        }
    }

    private void setAffectedVersion(final NotificationEvent notificationEvent, final EventData eventData, final IssueInputParameters issueInputParameters,
            final String targetFieldValue) {
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
            jiraSettingsService.addHubError(errorMessage,
                    eventData.getHubProjectName(),
                    eventData.getHubProjectVersion(),
                    eventData.getJiraProjectName(),
                    eventData.getJiraUserName(),
                    "setAffectedVersion");
        }
    }

    private String getPluginFieldValue(final NotificationEvent notificationEvent, final EventData eventData, final String pluginFieldId) {
        String fieldValue = null;
        if (PluginField.HUB_CUSTOM_FIELD_COMPONENT.getId().equals(pluginFieldId)) {
            fieldValue = eventData.getHubComponentName();
        } else if (PluginField.HUB_CUSTOM_FIELD_COMPONENT_VERSION.getId().equals(pluginFieldId)) {
            fieldValue = eventData.getHubComponentVersion();
        } else if (PluginField.HUB_CUSTOM_FIELD_POLICY_RULE.getId().equals(pluginFieldId)) {
            if (notificationEvent.isPolicyEvent()) {
                fieldValue = eventData.getHubRuleName();
            } else {
                logger.debug("Skipping field " + PluginField.HUB_CUSTOM_FIELD_POLICY_RULE.getName() + " for vulnerability issue");
            }
        } else if (PluginField.HUB_CUSTOM_FIELD_PROJECT.getId().equals(pluginFieldId)) {
            fieldValue = eventData.getHubProjectName();
        } else if (PluginField.HUB_CUSTOM_FIELD_PROJECT_VERSION.getId().equals(pluginFieldId)) {
            fieldValue = eventData.getHubProjectVersion();
        } else {
            final String errorMessage = "Unrecognized plugin field ID: " + pluginFieldId;
            logger.error(errorMessage);
            jiraSettingsService.addHubError(errorMessage,
                    eventData.getHubProjectName(),
                    eventData.getHubProjectVersion(),
                    eventData.getJiraProjectName(),
                    eventData.getJiraUserName(),
                    "getPluginFieldValue");
        }

        return fieldValue;
    }
}
