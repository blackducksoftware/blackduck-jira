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
import com.blackducksoftware.integration.jira.common.exception.JiraException;
import com.blackducksoftware.integration.jira.common.model.PluginField;

public abstract class BlackDuckIssueFieldTemplate {
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

    public Map<Long, String> getHubFieldMappings(final TicketInfoFromSetup ticketInfoFromSetup) throws JiraException {
        final Map<Long, String> hubFieldMappings = new HashMap<>();
        // TODO add all
        addCustomField(ticketInfoFromSetup, hubFieldMappings, PluginField.HUB_CUSTOM_FIELD_PROJECT, getProjectName());

        hubFieldMappings.putAll(getAddtionalHubFieldMappings(ticketInfoFromSetup));

        return hubFieldMappings;
    }

    protected abstract Map<Long, String> getAddtionalHubFieldMappings(final TicketInfoFromSetup ticketInfoFromSetup);

    protected final void addCustomField(final TicketInfoFromSetup ticketInfoFromSetup, final Map<Long, String> hubFieldMappings, final PluginField pluginField, final String fieldValue) throws JiraException {
        final CustomField customField = ticketInfoFromSetup.getCustomFields().get(pluginField);
        if (customField != null) {
            hubFieldMappings.put(customField.getIdAsLong(), fieldValue);
        } else {
            throw new JiraException("Could not create custom field: " + pluginField.getName());
        }
    }

}
