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

import java.util.Set;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.user.ApplicationUser;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.common.exception.JiraException;
import com.blackducksoftware.integration.jira.config.Fields;
import com.blackducksoftware.integration.jira.config.IdToNameMapping;

public class JiraFieldUtils {

    public static void printFields(HubJiraLogger logger, FieldManager fieldManager, ApplicationUser user, Issue issue) {
        try {
            Set<NavigableField> navFields = fieldManager.getAllAvailableNavigableFields();
            for (NavigableField field : navFields) {
                logger.debug("NavigableField: Id: " + field.getId() + "; Name: " + field.getName() + "; nameKey: " + field.getNameKey());
            }
        } catch (Exception e) {
            logger.debug("Error getting fields: " + e.getMessage());
        }
    }

    public static Fields getTargetFields(HubJiraLogger logger, FieldManager fieldManager) throws JiraException {
        final Fields targetFields = new Fields();
        addEligibleSystemFields(logger, fieldManager, targetFields);
        addNonBdsCustomFields(logger, fieldManager, targetFields);
        logger.debug("targetFields: " + targetFields);
        return targetFields;
    }

    private static void addNonBdsCustomFields(HubJiraLogger logger, FieldManager fieldManager, final Fields targetFields) throws JiraException {
        Set<NavigableField> navFields;
        try {
            navFields = fieldManager.getAllAvailableNavigableFields();
        } catch (FieldException e) {
            String msg = "Error getting JIRA fields: " + e.getMessage();
            logger.error(msg, e);
            throw new JiraException(msg, e);
        }
        for (NavigableField field : navFields) {
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

    private static boolean isBdsCustomField(Field field) {
        if ((HubJiraConstants.HUB_CUSTOM_FIELD_COMPONENT.equals(field.getName())) ||
                (HubJiraConstants.HUB_CUSTOM_FIELD_COMPONENT_VERSION.equals(field.getName())) ||
                (HubJiraConstants.HUB_CUSTOM_FIELD_POLICY_RULE.equals(field.getName())) ||
                (HubJiraConstants.HUB_CUSTOM_FIELD_PROJECT.equals(field.getName())) ||
                (HubJiraConstants.HUB_CUSTOM_FIELD_PROJECT_VERSION.equals(field.getName()))) {
            return true;
        }
        return false;
    }

    private static void addEligibleSystemFields(HubJiraLogger logger, FieldManager fieldManager, final Fields targetFields) {
        Field componentsField = fieldManager.getField(HubJiraConstants.COMPONENTS_FIELD_ID);
        if (componentsField == null) {
            logger.error("Error getting components field (field id: " + HubJiraConstants.COMPONENTS_FIELD_ID + ") for field copy target field list");
        } else {
            targetFields.add(new IdToNameMapping(HubJiraConstants.COMPONENTS_FIELD_ID, componentsField.getName()));
        }

        Field versionsField = fieldManager.getField(HubJiraConstants.VERSIONS_FIELD_ID);
        if (versionsField == null) {
            logger.error("Error getting versions field (field id: " + HubJiraConstants.VERSIONS_FIELD_ID + ") for field copy target field list");
        } else {
            targetFields.add(new IdToNameMapping(HubJiraConstants.VERSIONS_FIELD_ID, versionsField.getName()));
        }
    }
}
