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
package com.blackducksoftware.integration.jira.issue.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.label.LabelManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.JiraUserContext;
import com.blackducksoftware.integration.jira.common.model.PluginField;
import com.blackducksoftware.integration.jira.web.JiraServices;
import com.blackducksoftware.integration.jira.web.model.ProjectFieldCopyMapping;

public class IssueFieldCopyMappingHandler {
    private final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final ProjectManager projectManager;
    private final FieldManager fieldManager;
    private final LabelManager labelManager;

    private final JiraUserContext jiraUserContext;
    private final Map<PluginField, CustomField> customFields;

    public IssueFieldCopyMappingHandler(final JiraServices jiraServices, final JiraUserContext jiraUserContext, final Map<PluginField, CustomField> customFields) {
        this.projectManager = jiraServices.getJiraProjectManager();
        this.fieldManager = jiraServices.getFieldManager();
        this.labelManager = jiraServices.getLabelManager();
        this.jiraUserContext = jiraUserContext;
        this.customFields = customFields;
    }

    public List<String> setFieldCopyMappings(final IssueInputParameters issueInputParameters, final Set<ProjectFieldCopyMapping> projectFieldCopyMappings, final Map<Long, String> blackDuckFieldMappings,
        final String jiraProjectName, final Long jiraProjectId) {
        final List<String> labels = new ArrayList<>();
        if (projectFieldCopyMappings == null || projectFieldCopyMappings.size() == 0) {
            logger.debug("projectFieldCopyMappings is empty");
            return labels;
        }

        for (final ProjectFieldCopyMapping fieldCopyMapping : projectFieldCopyMappings) {
            logger.debug("projectFieldCopyMapping: " + fieldCopyMapping);
            if (jiraProjectName.equals(fieldCopyMapping.getJiraProjectName()) && !BlackDuckJiraConstants.FIELD_COPY_MAPPING_WILDCARD.equals(fieldCopyMapping.getJiraProjectName())) {
                logger.debug("This field copy mapping is for JIRA project " + fieldCopyMapping.getJiraProjectName() + "; skipping it");
                continue;
            }
            logger.debug("This field copy mapping is for this JIRA project (or all JIRA projects); using it");

            final String targetFieldId = fieldCopyMapping.getTargetFieldId();
            logger.debug("Setting field with ID " + targetFieldId + " from field " + fieldCopyMapping.getSourceFieldName());

            final Field targetField = fieldManager.getField(targetFieldId);
            logger.debug("\ttargetField: " + targetField);
            if (targetField == null) {
                final String errorMessage = "Custom field with ID " + targetFieldId + " not found; won't be set";
                logger.error(errorMessage);
                continue;
            }

            final String fieldValue = getFieldValue(blackDuckFieldMappings, fieldCopyMapping.getSourceFieldId());
            logger.debug("New target field value: " + fieldValue);

            if (targetField.getId() != null && targetField.getId().startsWith(FieldManager.CUSTOM_FIELD_PREFIX)) {
                logger.debug("Setting custom field " + targetField.getName() + " to " + fieldValue);
                issueInputParameters.addCustomFieldValue(targetField.getId(), fieldValue);
            } else {
                logger.debug("Setting standard field " + targetField.getName() + " to " + fieldValue);
                final String label = setSystemField(jiraProjectId, issueInputParameters, targetField, fieldValue);
                if (label != null) {
                    labels.add(label);
                }
            }
        }
        return labels;
    }

    public void addLabels(final Long issueId, final List<String> labels) {
        for (final String label : labels) {
            logger.debug("Adding label: " + label);
            labelManager.addLabel(jiraUserContext.getDefaultJiraIssueCreatorUser(), issueId, label, false);
        }
    }

    private String getFieldValue(final Map<Long, String> blackDuckFieldMappings, final String sourceFieldId) {
        if (sourceFieldId != null) {
            for (final PluginField pluginField : PluginField.values()) {
                if (sourceFieldId.equals(pluginField.getId())) {
                    final CustomField customField = customFields.get(pluginField);
                    if (customField != null) {
                        final String mappedValue = blackDuckFieldMappings.get(customField.getIdAsLong());
                        return mappedValue;
                    }
                }
            }
        }
        return null;
    }

    /**
     * If target field is labels field, the label value is returned (labels cannot be applied to an issue during creation).
     */
    private String setSystemField(final Long jiraProjectId, final IssueInputParameters issueInputParameters, final Field targetField, final String targetFieldValue) {
        final Project jiraProject = projectManager.getProjectObj(jiraProjectId);
        if (BlackDuckJiraConstants.VERSIONS_FIELD_ID.equals(targetField.getId())) {
            setAffectedVersion(jiraProject, issueInputParameters, targetFieldValue);
        } else if (BlackDuckJiraConstants.COMPONENTS_FIELD_ID.equals(targetField.getId())) {
            setComponent(jiraProject, issueInputParameters, targetFieldValue);
        } else if ("labels".equals(targetField.getId())) {
            logger.debug("Recording label to add after issue is created: " + targetFieldValue);
            return targetFieldValue;
        } else {
            final String errorMessage = "Unrecognized field id (" + targetField.getId() + "); field cannot be set";
            logger.error(errorMessage);
        }
        return null;
    }

    private void setComponent(final Project jiraProject, final IssueInputParameters issueInputParameters, final String targetFieldValue) {
        Long compId = null;
        if (targetFieldValue != null) {
            final Collection<ProjectComponent> components = jiraProject.getComponents();
            for (final ProjectComponent component : components) {
                if (targetFieldValue.equals(component.getName())) {
                    compId = component.getId();
                    break;
                }
            }
        }
        if (compId != null) {
            issueInputParameters.setComponentIds(compId);
        } else {
            logger.error("No component matching '" + targetFieldValue + "' found on project");
        }
    }

    private void setAffectedVersion(final Project jiraProject, final IssueInputParameters issueInputParameters, final String targetFieldValue) {
        Long versionId = null;
        if (targetFieldValue != null) {
            final Collection<Version> versions = jiraProject.getVersions();
            for (final Version version : versions) {
                if (targetFieldValue.equals(version.getName())) {
                    versionId = version.getId();
                    break;
                }
            }
        }
        if (versionId != null) {
            issueInputParameters.setAffectedVersionIds(versionId);
        } else {
            logger.error("No version matching '" + targetFieldValue + "' found on project");
        }
    }

}
