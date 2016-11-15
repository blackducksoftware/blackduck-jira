/*
 * Copyright (C) 2016 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.jira.task.issue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.user.ApplicationUser;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.common.JiraContext;
import com.blackducksoftware.integration.jira.common.PluginField;
import com.blackducksoftware.integration.jira.common.TicketInfoFromSetup;
import com.blackducksoftware.integration.jira.config.ProjectFieldCopyMapping;
import com.blackducksoftware.integration.jira.task.conversion.output.HubEvent;
import com.blackducksoftware.integration.jira.task.conversion.output.PolicyEvent;

public class IssueFieldHandler {
    private static final String FIELD_COPY_MAPPING_WILDCARD = "*";

    private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final JiraServices jiraServices;

    private final TicketInfoFromSetup ticketInfoFromSetup;

    private final JiraContext jiraContext;

    public IssueFieldHandler(JiraServices jiraServices, JiraContext jiraContext, TicketInfoFromSetup ticketInfoFromSetup) {
        super();
        this.jiraServices = jiraServices;
        this.jiraContext = jiraContext;
        this.ticketInfoFromSetup = ticketInfoFromSetup;
    }

    public void addLabels(MutableIssue issue, List<String> labels) {
        for (String label : labels) {
            logger.debug("Adding label: " + label);
            jiraServices.getLabelManager().addLabel(jiraContext.getJiraUser(), issue.getId(), label, false);
        }
    }

    public void setPluginFieldValues(final HubEvent notificationEvent, IssueInputParameters issueInputParameters) {
        if (ticketInfoFromSetup != null && ticketInfoFromSetup.getCustomFields() != null
                && !ticketInfoFromSetup.getCustomFields().isEmpty()) {
            final Long projectFieldId = ticketInfoFromSetup.getCustomFields()
                    .get(PluginField.HUB_CUSTOM_FIELD_PROJECT).getIdAsLong();
            issueInputParameters.addCustomFieldValue(projectFieldId,
                    notificationEvent.getNotif().getProjectVersion().getProjectName());

            final Long projectVersionFieldId = ticketInfoFromSetup.getCustomFields()
                    .get(PluginField.HUB_CUSTOM_FIELD_PROJECT_VERSION).getIdAsLong();
            issueInputParameters.addCustomFieldValue(projectVersionFieldId,
                    notificationEvent.getNotif().getProjectVersion().getProjectVersionName());

            final Long componentFieldId = ticketInfoFromSetup.getCustomFields()
                    .get(PluginField.HUB_CUSTOM_FIELD_COMPONENT).getIdAsLong();
            issueInputParameters.addCustomFieldValue(componentFieldId, notificationEvent.getNotif().getComponentName());

            final Long componentVersionFieldId = ticketInfoFromSetup.getCustomFields()
                    .get(PluginField.HUB_CUSTOM_FIELD_COMPONENT_VERSION).getIdAsLong();
            issueInputParameters.addCustomFieldValue(componentVersionFieldId,
                    notificationEvent.getNotif().getComponentVersion());

            if (notificationEvent.getClass().equals(PolicyEvent.class)) {
                final PolicyEvent policyNotif = (PolicyEvent) notificationEvent;
                final Long policyRuleFieldId = ticketInfoFromSetup.getCustomFields()
                        .get(PluginField.HUB_CUSTOM_FIELD_POLICY_RULE).getIdAsLong();
                issueInputParameters.addCustomFieldValue(policyRuleFieldId, policyNotif.getPolicyRule().getName());
            }
        }
    }

    public List<String> setOtherFieldValues(final HubEvent notificationEvent, IssueInputParameters issueInputParameters) {
        List<String> labels = new ArrayList<>();
        Set<ProjectFieldCopyMapping> projectFieldCopyMappings = notificationEvent.getProjectFieldCopyMappings();
        if (projectFieldCopyMappings == null) {
            logger.debug("projectFieldCopyMappings is null");
            return labels;
        }
        if (projectFieldCopyMappings.size() == 0) {
            logger.debug("projectFieldCopyMappings is null");
            return labels;
        }
        for (ProjectFieldCopyMapping fieldCopyMapping : projectFieldCopyMappings) {
            logger.debug("projectFieldCopyMappings: " + projectFieldCopyMappings);
            if ((!notificationEvent.getJiraProjectName().equals(fieldCopyMapping.getJiraProjectName()))
                    && (!FIELD_COPY_MAPPING_WILDCARD.equals(fieldCopyMapping.getJiraProjectName()))) {
                logger.debug("This field copy mapping is for JIRA project " + fieldCopyMapping.getJiraProjectName()
                        + "; skipping it");
                continue;
            }
            logger.debug("This field copy mapping is for this JIRA project (or all JIRA projects); using it");

            String targetFieldId = fieldCopyMapping.getTargetFieldId();
            logger.debug("Setting field with ID " + targetFieldId + " from field " + fieldCopyMapping.getSourceFieldName());

            Field targetField = jiraServices.getFieldManager().getField(targetFieldId);
            logger.debug("\ttargetField: " + targetField);
            if (targetField == null) {
                logger.error("Custom field with ID " + targetFieldId + " not found; won't be set");
                continue;
            }

            String fieldValue = getPluginFieldValue(notificationEvent, fieldCopyMapping.getSourceFieldId());
            if (fieldValue == null) {
                continue;
            }
            logger.debug("New target field value: " + fieldValue);

            if (targetField.getId().startsWith(FieldManager.CUSTOM_FIELD_PREFIX)) {
                logger.debug("Setting custom field " + targetField.getName() + " to " + fieldValue);
                issueInputParameters.addCustomFieldValue(targetField.getId(), fieldValue);
            } else {
                logger.debug("Setting standard field " + targetField.getName() + " to " + fieldValue);
                String label = setSystemField(notificationEvent, issueInputParameters, targetField, fieldValue);
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
    private String setSystemField(HubEvent notificationEvent, IssueInputParameters issueInputParameters, Field targetField, String targetFieldValue) {
        if (targetField.getId().equals("versions")) {
            setAffectedVersion(notificationEvent, issueInputParameters, targetFieldValue);
        } else if (targetField.getId().equals("components")) {
            setComponent(notificationEvent, issueInputParameters, targetFieldValue);
        } else if (targetField.getId().equals("labels")) {
            logger.debug("Recording label to add after issue is created: " + targetFieldValue);
            return targetFieldValue;
        } else {
            logger.error("Unrecognized field id (" + targetField.getId() + "); field cannot be set");
        }
        return null;
    }

    private void setComponent(HubEvent notificationEvent, IssueInputParameters issueInputParameters, String targetFieldValue) {
        Long compId = null;
        Collection<ProjectComponent> components = jiraServices.getJiraProjectManager().getProjectObj(notificationEvent.getJiraProjectId()).getComponents();
        for (ProjectComponent component : components) {
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

    private void setAffectedVersion(HubEvent notificationEvent, IssueInputParameters issueInputParameters, String targetFieldValue) {
        Long versionId = null;
        Collection<Version> versions = jiraServices.getJiraProjectManager().getProjectObj(notificationEvent.getJiraProjectId()).getVersions();
        for (Version version : versions) {
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

    // TODO TEMP
    void printFields(ApplicationUser user, Issue issue) {
        try {
            Set<NavigableField> navFields = jiraServices.getFieldManager().getAllAvailableNavigableFields();
            for (NavigableField field : navFields) {
                logger.debug("NavigableField: Id: " + field.getId() + "; Name: " + field.getName() + "; nameKey: " + field.getNameKey());
            }
        } catch (Exception e) {
            logger.debug("Error getting fields: " + e.getMessage());
        }
    }

    private String getPluginFieldValue(final HubEvent notificationEvent, String pluginFieldId) {
        String fieldValue = null;
        if (PluginField.HUB_CUSTOM_FIELD_COMPONENT.getId().equals(pluginFieldId)) {
            fieldValue = notificationEvent.getNotif().getComponentName();
        } else if (PluginField.HUB_CUSTOM_FIELD_COMPONENT_VERSION.getId().equals(pluginFieldId)) {
            fieldValue = notificationEvent.getNotif().getComponentVersion();
        } else if (PluginField.HUB_CUSTOM_FIELD_POLICY_RULE.getId().equals(pluginFieldId)) {
            final PolicyEvent policyNotif = (PolicyEvent) notificationEvent;
            fieldValue = policyNotif.getPolicyRule().getName();
        } else if (PluginField.HUB_CUSTOM_FIELD_PROJECT.getId().equals(pluginFieldId)) {
            fieldValue = notificationEvent.getNotif().getProjectVersion().getProjectName();
        } else if (PluginField.HUB_CUSTOM_FIELD_PROJECT_VERSION.getId().equals(pluginFieldId)) {
            fieldValue = notificationEvent.getNotif().getProjectVersion().getProjectVersionName();
        } else {
            logger.error("Unrecognized plugin field ID: " + pluginFieldId);
        }

        return fieldValue;
    }
}
