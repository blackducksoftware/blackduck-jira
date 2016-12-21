/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
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
 *******************************************************************************/
package com.blackducksoftware.integration.jira.task.issue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.project.version.Version;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.common.JiraContext;
import com.blackducksoftware.integration.jira.common.PluginField;
import com.blackducksoftware.integration.jira.common.TicketInfoFromSetup;
import com.blackducksoftware.integration.jira.config.ProjectFieldCopyMapping;
import com.blackducksoftware.integration.jira.task.conversion.output.JiraEvent;
import com.blackducksoftware.integration.jira.task.conversion.output.JiraPolicyEvent;

public class IssueFieldHandler {

    private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final JiraServices jiraServices;

    private final TicketInfoFromSetup ticketInfoFromSetup;

    private final JiraContext jiraContext;

    public IssueFieldHandler(JiraServices jiraServices, JiraContext jiraContext, TicketInfoFromSetup ticketInfoFromSetup) {
        this.jiraServices = jiraServices;
        this.jiraContext = jiraContext;
        this.ticketInfoFromSetup = ticketInfoFromSetup;
    }

    public void addLabels(MutableIssue issue, List<String> labels) {
        for (final String label : labels) {
            logger.debug("Adding label: " + label);
            jiraServices.getLabelManager().addLabel(jiraContext.getJiraUser(), issue.getId(), label, false);
        }
    }

    public void setPluginFieldValues(final JiraEvent notificationEvent, IssueInputParameters issueInputParameters) {
        if (ticketInfoFromSetup != null && ticketInfoFromSetup.getCustomFields() != null
                && !ticketInfoFromSetup.getCustomFields().isEmpty()) {
            final Long projectFieldId = ticketInfoFromSetup.getCustomFields()
                    .get(PluginField.HUB_CUSTOM_FIELD_PROJECT).getIdAsLong();
            issueInputParameters.addCustomFieldValue(projectFieldId,
                    notificationEvent.getNotificationContent().getProjectVersion().getProjectName());

            final Long projectVersionFieldId = ticketInfoFromSetup.getCustomFields()
                    .get(PluginField.HUB_CUSTOM_FIELD_PROJECT_VERSION).getIdAsLong();
            issueInputParameters.addCustomFieldValue(projectVersionFieldId,
                    notificationEvent.getNotificationContent().getProjectVersion().getProjectVersionName());

            final Long componentFieldId = ticketInfoFromSetup.getCustomFields()
                    .get(PluginField.HUB_CUSTOM_FIELD_COMPONENT).getIdAsLong();
            issueInputParameters.addCustomFieldValue(componentFieldId, notificationEvent.getNotificationContent().getComponentName());

            final Long componentVersionFieldId = ticketInfoFromSetup.getCustomFields()
                    .get(PluginField.HUB_CUSTOM_FIELD_COMPONENT_VERSION).getIdAsLong();
            issueInputParameters.addCustomFieldValue(componentVersionFieldId,
                    notificationEvent.getNotificationContent().getComponentVersion());

            if (notificationEvent.getClass().equals(JiraPolicyEvent.class)) {
                final JiraPolicyEvent policyNotif = (JiraPolicyEvent) notificationEvent;
                final Long policyRuleFieldId = ticketInfoFromSetup.getCustomFields()
                        .get(PluginField.HUB_CUSTOM_FIELD_POLICY_RULE).getIdAsLong();
                issueInputParameters.addCustomFieldValue(policyRuleFieldId, policyNotif.getPolicyRule().getName());
            }
        }
    }

    public List<String> setOtherFieldValues(final JiraEvent notificationEvent, IssueInputParameters issueInputParameters) {
        final List<String> labels = new ArrayList<>();
        final Set<ProjectFieldCopyMapping> projectFieldCopyMappings = notificationEvent.getProjectFieldCopyMappings();
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
            if ((!notificationEvent.getJiraProjectName().equals(fieldCopyMapping.getJiraProjectName()))
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
                logger.error("Custom field with ID " + targetFieldId + " not found; won't be set");
                continue;
            }

            final String fieldValue = getPluginFieldValue(notificationEvent, fieldCopyMapping.getSourceFieldId());
            if (fieldValue == null) {
                continue;
            }
            logger.debug("New target field value: " + fieldValue);

            if (targetField.getId().startsWith(FieldManager.CUSTOM_FIELD_PREFIX)) {
                logger.debug("Setting custom field " + targetField.getName() + " to " + fieldValue);
                issueInputParameters.addCustomFieldValue(targetField.getId(), fieldValue);
            } else {
                logger.debug("Setting standard field " + targetField.getName() + " to " + fieldValue);
                final String label = setSystemField(notificationEvent, issueInputParameters, targetField, fieldValue);
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
    private String setSystemField(JiraEvent notificationEvent, IssueInputParameters issueInputParameters, Field targetField,
            String targetFieldValue) {
        if (targetField.getId().equals(HubJiraConstants.VERSIONS_FIELD_ID)) {
            setAffectedVersion(notificationEvent, issueInputParameters, targetFieldValue);
        } else if (targetField.getId().equals(HubJiraConstants.COMPONENTS_FIELD_ID)) {
            setComponent(notificationEvent, issueInputParameters, targetFieldValue);
        } else if (targetField.getId().equals("labels")) {
            logger.debug("Recording label to add after issue is created: " + targetFieldValue);
            return targetFieldValue;
        } else {
            logger.error("Unrecognized field id (" + targetField.getId() + "); field cannot be set");
        }
        return null;
    }

    private void setComponent(JiraEvent notificationEvent, IssueInputParameters issueInputParameters, String targetFieldValue) {
        Long compId = null;
        final Collection<ProjectComponent> components = jiraServices.getJiraProjectManager().getProjectObj(notificationEvent.getJiraProjectId())
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
            logger.error("No component matching '" + targetFieldValue + "' found on project");
        }
    }

    private void setAffectedVersion(JiraEvent notificationEvent, IssueInputParameters issueInputParameters, String targetFieldValue) {
        Long versionId = null;
        final Collection<Version> versions = jiraServices.getJiraProjectManager().getProjectObj(notificationEvent.getJiraProjectId()).getVersions();
        for (final Version version : versions) {
            if (targetFieldValue.equals(version.getName())) {
                versionId = version.getId();
                break;
            }
        }
        if (versionId != null) {
            issueInputParameters.setAffectedVersionIds(versionId);
        } else {
            logger.error("No version matching '" + targetFieldValue + "' found on project");
        }
    }

    private String getPluginFieldValue(final JiraEvent notificationEvent, String pluginFieldId) {
        String fieldValue = null;
        if (PluginField.HUB_CUSTOM_FIELD_COMPONENT.getId().equals(pluginFieldId)) {
            fieldValue = notificationEvent.getNotificationContent().getComponentName();
        } else if (PluginField.HUB_CUSTOM_FIELD_COMPONENT_VERSION.getId().equals(pluginFieldId)) {
            fieldValue = notificationEvent.getNotificationContent().getComponentVersion();
        } else if (PluginField.HUB_CUSTOM_FIELD_POLICY_RULE.getId().equals(pluginFieldId)) {
            if (notificationEvent instanceof JiraPolicyEvent) {
                final JiraPolicyEvent policyNotif = (JiraPolicyEvent) notificationEvent;
                fieldValue = policyNotif.getPolicyRule().getName();
            } else {
                logger.debug("Skipping field " + PluginField.HUB_CUSTOM_FIELD_POLICY_RULE.getName() + " for vulnerability issue");
            }
        } else if (PluginField.HUB_CUSTOM_FIELD_PROJECT.getId().equals(pluginFieldId)) {
            fieldValue = notificationEvent.getNotificationContent().getProjectVersion().getProjectName();
        } else if (PluginField.HUB_CUSTOM_FIELD_PROJECT_VERSION.getId().equals(pluginFieldId)) {
            fieldValue = notificationEvent.getNotificationContent().getProjectVersion().getProjectVersionName();
        } else {
            logger.error("Unrecognized plugin field ID: " + pluginFieldId);
        }

        return fieldValue;
    }
}
