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
package com.blackducksoftware.integration.jira.data.accessor;

import java.time.LocalDate;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.data.PluginConfigKeys;

public class PluginConfigurationAccessor {
    private final JiraSettingsAccessor jiraSettingsAccessor;

    public PluginConfigurationAccessor(final JiraSettingsAccessor jiraSettingsAccessor) {
        this.jiraSettingsAccessor = jiraSettingsAccessor;
    }

    public String getFirstTimeSave() {
        return jiraSettingsAccessor.getStringValue(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_FIRST_SAVE_TIME);
    }

    public void setFirstTimeSave(final String firstTimeSave) {
        jiraSettingsAccessor.setValue(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_FIRST_SAVE_TIME, firstTimeSave);
    }

    public String getLastRunDate() {
        return jiraSettingsAccessor.getStringValue(PluginConfigKeys.BLACKDUCK_CONFIG_LAST_RUN_DATE);
    }

    public void setLastRunDate(final String lastRunDate) {
        if (StringUtils.isNotBlank(lastRunDate)) {
            jiraSettingsAccessor.setValue(PluginConfigKeys.BLACKDUCK_CONFIG_LAST_RUN_DATE, lastRunDate);
        }
    }

    public String getJiraAdminUser() {
        return jiraSettingsAccessor.getStringValue(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_ADMIN_USER);
    }

    public void setJiraAdminUser(final String adminUser) {
        jiraSettingsAccessor.setValue(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_ADMIN_USER, adminUser);
    }

    public Object getPluginError() {
        return jiraSettingsAccessor.getObjectValue(BlackDuckJiraConstants.BLACKDUCK_JIRA_ERROR);
    }

    public void setPluginError(final Object pluginError) {
        jiraSettingsAccessor.setValue(BlackDuckJiraConstants.BLACKDUCK_JIRA_ERROR, pluginError);
    }

    public LocalDate getLastPhoneHome(final BlackDuckJiraLogger logger) {
        try {
            final String stringDate = jiraSettingsAccessor.getStringValue(BlackDuckJiraConstants.DATE_LAST_PHONED_HOME);
            return LocalDate.parse(stringDate);
        } catch (final Exception e) {
            logger.warn("Cannot find the date of last phone-home: " + e.getMessage());
        }
        return LocalDate.MIN;
    }

    public void setLastPhoneHome(final LocalDate date) {
        if (date != null) {
            jiraSettingsAccessor.setValue(BlackDuckJiraConstants.DATE_LAST_PHONED_HOME, date.toString());
        }
    }

}
