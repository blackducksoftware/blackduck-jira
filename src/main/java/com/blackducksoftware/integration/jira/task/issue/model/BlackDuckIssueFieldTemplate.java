/*
 * Copyright (C) 2018 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.jira.task.issue.model;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.user.ApplicationUser;
import com.blackducksoftware.integration.jira.common.TicketInfoFromSetup;
import com.blackducksoftware.integration.jira.common.model.PluginField;
import com.blackducksoftware.integration.util.Stringable;

public abstract class BlackDuckIssueFieldTemplate extends Stringable {
    private final ApplicationUser projectOwner;
    private final String projectName;
    private final String projectVersionName;
    private final String projectVersionUri;
    private final String projectVersionNickname;

    private final String componentName;
    private final String componentUri;
    private final String componentVersionName;
    private final String componentVersionUri;

    private final String licenseString;
    private final String licenseLink;
    private final String usagesString;
    private final String updatedTimeString;

    // @formatter:off
    public BlackDuckIssueFieldTemplate(
             final ApplicationUser projectOwner
            ,final String projectName
            ,final String projectVersionName
            ,final String projectVersionUri
            ,final String projectVersionNickname
            ,final String componentName
            ,final String componentUri
            ,final String componentVersionName
            ,final String componentVersionUri
            ,final String licenseString
            ,final String licenseLink
            ,final String usagesString
            ,final String updatedTimeString
            ) {
        this.projectOwner = projectOwner;
        this.projectName = projectName;
        this.projectVersionName = projectVersionName;
        this.projectVersionUri = projectVersionUri;
        this.projectVersionNickname = projectVersionNickname;
        this.componentName = componentName;
        this.componentUri = componentUri;
        this.componentVersionName = componentVersionName;
        this.componentVersionUri = componentVersionUri;
        this.licenseString = licenseString;
        this.licenseLink = licenseLink;
        this.usagesString = usagesString;
        this.updatedTimeString = updatedTimeString;
    }
    // @formatter:on

    public ApplicationUser getProjectOwner() {
        return projectOwner;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectVersionName() {
        return projectVersionName;
    }

    public String getProjectVersionUri() {
        return projectVersionUri;
    }

    public String getProjectVersionNickname() {
        return projectVersionNickname;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getComponentUri() {
        return componentUri;
    }

    public String getComponentVersionName() {
        return componentVersionName;
    }

    public String getComponentVersionUri() {
        return componentVersionUri;
    }

    public String getLicenseString() {
        return licenseString;
    }

    public String getLicenseLink() {
        return licenseLink;
    }

    public String getUsagesString() {
        return usagesString;
    }

    public String getUpdatedTimeString() {
        return updatedTimeString;
    }

    public final Map<Long, String> getBlackDuckFieldMappings(final TicketInfoFromSetup ticketInfoFromSetup) {
        final Map<PluginField, CustomField> customFields = ticketInfoFromSetup.getCustomFields();
        final Map<Long, String> blackDuckFieldMappings = new HashMap<>();
        // TODO add all
        if (projectOwner != null) {
            addCustomField(customFields, blackDuckFieldMappings, PluginField.HUB_CUSTOM_FIELD_PROJECT_OWNER, projectOwner.getUsername());
        }
        addCustomField(customFields, blackDuckFieldMappings, PluginField.HUB_CUSTOM_FIELD_PROJECT, projectName);
        addCustomField(customFields, blackDuckFieldMappings, PluginField.HUB_CUSTOM_FIELD_PROJECT_VERSION, projectVersionName);
        addCustomField(customFields, blackDuckFieldMappings, PluginField.HUB_CUSTOM_FIELD_PROJECT_VERSION_URL, projectVersionUri);
        addCustomField(customFields, blackDuckFieldMappings, PluginField.HUB_CUSTOM_FIELD_PROJECT_VERSION_NICKNAME, projectVersionNickname);

        addCustomField(customFields, blackDuckFieldMappings, PluginField.HUB_CUSTOM_FIELD_COMPONENT, componentName);
        addCustomField(customFields, blackDuckFieldMappings, PluginField.HUB_CUSTOM_FIELD_COMPONENT_URL, componentUri);
        addCustomField(customFields, blackDuckFieldMappings, PluginField.HUB_CUSTOM_FIELD_COMPONENT_VERSION, componentVersionName);
        addCustomField(customFields, blackDuckFieldMappings, PluginField.HUB_CUSTOM_FIELD_COMPONENT_VERSION_URL, componentVersionUri);

        addCustomField(customFields, blackDuckFieldMappings, PluginField.HUB_CUSTOM_FIELD_LICENSE_NAMES, licenseString);
        addCustomField(customFields, blackDuckFieldMappings, PluginField.HUB_CUSTOM_FIELD_LICENSE_URL, licenseLink);
        addCustomField(customFields, blackDuckFieldMappings, PluginField.HUB_CUSTOM_FIELD_COMPONENT_USAGE, usagesString);
        addCustomField(customFields, blackDuckFieldMappings, PluginField.HUB_CUSTOM_FIELD_PROJECT_VERSION_LAST_UPDATED, updatedTimeString);

        blackDuckFieldMappings.putAll(getAddtionalHubFieldMappings(customFields));

        return blackDuckFieldMappings;
    }

    protected final void addCustomField(final Map<PluginField, CustomField> customFields, final Map<Long, String> hubFieldMappings, final PluginField pluginField, final String fieldValue) {
        final CustomField customField = customFields.get(pluginField);
        if (customField != null) {
            hubFieldMappings.put(customField.getIdAsLong(), fieldValue);
        } else {
            // TODO should this class have logging at all?
            // logger.warn("JIRA custom field " + pluginField.getName() + " not found");
        }
    }

    protected abstract Map<Long, String> getAddtionalHubFieldMappings(final Map<PluginField, CustomField> customFields);

}
