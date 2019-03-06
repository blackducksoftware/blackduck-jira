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

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeEntity;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.config.JiraServices;
import com.blackducksoftware.integration.jira.config.JiraSettingsService;

public class NotificationsSetup {
    private final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));
    private final JiraSettingsService jiraSettingsService;
    private final JiraServices jiraServices;

    public NotificationsSetup(final JiraSettingsService jiraSettingsService, final JiraServices jiraServices) {
        this.jiraSettingsService = jiraSettingsService;
        this.jiraServices = jiraServices;
    }

    public void addNotificationSchemeToProject(final Project project) {
        NotificationSchemeManager schemeManager = jiraServices.getNotificationSchemeManager();
        final CustomFieldManager customFieldManager = jiraServices.getCustomFieldManager();
        Scheme currentScheme = schemeManager.getSchemeFor(project);
        Scheme schemeCopy = schemeManager.copyScheme(currentScheme);
        Collection<CustomField> reviewerFields = customFieldManager.getCustomFieldObjectsByName(BlackDuckJiraConstants.BLACKDUCK_CUSTOM_FIELD_COMPONENT_REVIEWER);
        Collection<SchemeEntity> entities = new LinkedList<>();
        entities.addAll(schemeCopy.getEntities());
        if (null != reviewerFields) {
            Optional<CustomField> reviewerField = reviewerFields.stream().findFirst();
            if (reviewerField.isPresent()) {
                CustomField field = reviewerField.get();
                Set<Object> entityTypeIds = entities.stream().map(SchemeEntity::getEntityTypeId).collect(Collectors.toSet());
                Collection<SchemeEntity> projectReviewerEntities = entityTypeIds.stream()
                                                                       .map(typeId -> new SchemeEntity("USER_CUSTOM_FIELD_VALUE", field.getId(), typeId))
                                                                       .collect(Collectors.toList());
                entities.addAll(projectReviewerEntities);
            }
        }
        Scheme newScheme = new Scheme(schemeCopy.getId(),
            schemeCopy.getType(),
            schemeCopy.getName(),
            schemeCopy.getDescription(),
            entities);
        updateSchemeForProject(schemeManager, project, newScheme);
    }

    public void deleteNotificationSchemeFromProject(final Project project) {
        NotificationSchemeManager schemeManager = jiraServices.getNotificationSchemeManager();
        final CustomFieldManager customFieldManager = jiraServices.getCustomFieldManager();
        Collection<CustomField> reviewerFields = customFieldManager.getCustomFieldObjectsByName(BlackDuckJiraConstants.BLACKDUCK_CUSTOM_FIELD_COMPONENT_REVIEWER);
        if (null != reviewerFields) {
            Optional<CustomField> reviewerField = reviewerFields.stream().findFirst();
            if (reviewerField.isPresent()) {
                CustomField field = reviewerField.get();
                String fieldId = field.getId();
                try {
                    schemeManager.removeSchemeEntitiesForField(fieldId);
                } catch (RemoveException ex) {
                    logger.error("Error removing entities for field " + fieldId, ex);
                }
            }
        }
    }

    private void updateSchemeForProject(final NotificationSchemeManager schemeManager, final Project project, final Scheme scheme) {
        schemeManager.removeSchemesFromProject(project);
        schemeManager.addSchemeToProject(project, scheme);
    }
}
