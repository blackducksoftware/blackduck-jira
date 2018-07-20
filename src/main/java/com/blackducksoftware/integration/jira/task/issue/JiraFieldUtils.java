/**
 * Hub JIRA Plugin
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
package com.blackducksoftware.integration.jira.task.issue;

import java.util.Set;

import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.NavigableField;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.common.exception.JiraException;
import com.blackducksoftware.integration.jira.config.model.Fields;
import com.blackducksoftware.integration.jira.config.model.IdToNameMapping;

public class JiraFieldUtils {
    public static Fields getTargetFields(final HubJiraLogger logger, final FieldManager fieldManager) throws JiraException {
        final Fields targetFields = new Fields();
        addEligibleSystemFields(logger, fieldManager, targetFields);
        addNonBdsCustomFields(logger, fieldManager, targetFields);
        logger.debug("targetFields: " + targetFields);
        return targetFields;
    }

    private static void addNonBdsCustomFields(final HubJiraLogger logger, final FieldManager fieldManager, final Fields targetFields) throws JiraException {
        Set<NavigableField> navFields;
        try {
            navFields = fieldManager.getAllAvailableNavigableFields();
        } catch (final FieldException e) {
            final String msg = "Error getting JIRA fields: " + e.getMessage();
            logger.error(msg, e);
            throw new JiraException(msg, e);
        }
        for (final NavigableField field : navFields) {
            if (field.getId().startsWith(CustomFieldUtils.CUSTOM_FIELD_PREFIX)) {
                logger.debug("Found custom field: Id: " + field.getId() + "; Name: " + field.getName() + "; nameKey: " +
                        field.getNameKey());
                if (!isBdsCustomField(field)) {
                    targetFields.add(new IdToNameMapping(field.getId(), field.getName()));
                } else {
                    logger.debug("This is a BDS field; omitting it");
                }
            } else {
                logger.debug("Field with ID " + field.getId() + " is not a custom field");
            }
        }
    }

    private static boolean isBdsCustomField(final Field field) {
        if (
        // @formatter:off
                (HubJiraConstants.HUB_CUSTOM_FIELD_COMPONENT.equals(field.getName()))
                || (HubJiraConstants.HUB_CUSTOM_FIELD_COMPONENT_VERSION.equals(field.getName()))
                || (HubJiraConstants.HUB_CUSTOM_FIELD_POLICY_RULE.equals(field.getName()))
                || (HubJiraConstants.HUB_CUSTOM_FIELD_PROJECT.equals(field.getName()))
                || (HubJiraConstants.HUB_CUSTOM_FIELD_PROJECT_VERSION.equals(field.getName()))
                || (HubJiraConstants.HUB_CUSTOM_FIELD_LICENSE_NAMES.equals(field.getName()))
                || (HubJiraConstants.HUB_CUSTOM_FIELD_COMPONENT_ORIGIN_ID.equals(field.getName()))
                || (HubJiraConstants.HUB_CUSTOM_FIELD_COMPONENT_ORIGIN.equals(field.getName()))
                || (HubJiraConstants.HUB_CUSTOM_FIELD_COMPONENT_USAGE.equals(field.getName()))
                || (HubJiraConstants.HUB_CUSTOM_FIELD_PROJECT_VERSION_NICKNAME.equals(field.getName()))
                || (HubJiraConstants.HUB_CUSTOM_FIELD_PROJECT_OWNER.equals(field.getName()))
                || (HubJiraConstants.HUB_CUSTOM_FIELD_PROJECT_VERSION_LAST_UPDATED.equals(field.getName()))
                ) {
         // @formatter:on
            return true;
        }
        return false;
    }

    private static void addEligibleSystemFields(final HubJiraLogger logger, final FieldManager fieldManager, final Fields targetFields) {
        final Field componentsField = fieldManager.getField(HubJiraConstants.COMPONENTS_FIELD_ID);
        if (componentsField == null) {
            logger.error("Error getting components field (field id: " + HubJiraConstants.COMPONENTS_FIELD_ID + ") for field copy target field list");
        } else {
            targetFields.add(new IdToNameMapping(HubJiraConstants.COMPONENTS_FIELD_ID, componentsField.getName()));
        }

        final Field versionsField = fieldManager.getField(HubJiraConstants.VERSIONS_FIELD_ID);
        if (versionsField == null) {
            logger.error("Error getting versions field (field id: " + HubJiraConstants.VERSIONS_FIELD_ID + ") for field copy target field list");
        } else {
            targetFields.add(new IdToNameMapping(HubJiraConstants.VERSIONS_FIELD_ID, versionsField.getName()));
        }
    }
}
