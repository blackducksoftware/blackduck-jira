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
package com.blackducksoftware.integration.jira.task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.JiraUserContext;
import com.blackducksoftware.integration.jira.common.TicketInfoFromSetup;
import com.blackducksoftware.integration.jira.data.GlobalConfigurationAccessor;
import com.blackducksoftware.integration.jira.data.JiraSettingsAccessor;
import com.blackducksoftware.integration.jira.data.PluginConfigurationAccessor;
import com.blackducksoftware.integration.jira.data.PluginErrorAccessor;
import com.blackducksoftware.integration.jira.data.model.GeneralIssueCreationConfigModel;
import com.blackducksoftware.integration.jira.data.model.PluginGroupsConfigModel;
import com.blackducksoftware.integration.jira.data.model.PluginIssueCreationConfigModel;
import com.blackducksoftware.integration.jira.web.JiraServices;
import com.blackducksoftware.integration.jira.workflow.setup.PreTaskSetup;

public class JiraTaskTimed implements Callable<String> {
    private final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final JiraSettingsAccessor jiraSettingsAccessor;
    private final JiraServices jiraServices;

    public JiraTaskTimed(final JiraSettingsAccessor jiraSettingsAccessor, final JiraServices jiraServices) {
        this.jiraSettingsAccessor = jiraSettingsAccessor;
        this.jiraServices = jiraServices;
    }

    @Override
    public String call() throws Exception {
        logger.info("Running the Black Duck JIRA periodic timed task.");

        // These need to be created during execution because the task could have been queued for an arbitrarily long time
        logger.debug("Retrieving plugin settings for run...");
        final PluginConfigurationAccessor pluginConfigurationAccessor = jiraSettingsAccessor.createPluginConfigurationAccessor();
        final PluginErrorAccessor pluginErrorAccessor = jiraSettingsAccessor.createPluginErrorAccessor();
        final GlobalConfigurationAccessor globalConfigurationAccessor = jiraSettingsAccessor.createGlobalConfigurationAccessor();
        final PluginGroupsConfigModel groupsConfig = globalConfigurationAccessor.getGroupsConfig();
        final PluginIssueCreationConfigModel issueCreationConfig = globalConfigurationAccessor.getIssueCreationConfig();
        final GeneralIssueCreationConfigModel generalIssueConfig = issueCreationConfig.getGeneral();
        logger.debug("Retrieved plugin settings");
        logger.debug("Last run date based on SAL: " + pluginConfigurationAccessor.getFirstTimeSave());

        final Optional<JiraUserContext> optionalJiraUserContext = JiraUserContext.create(logger, pluginConfigurationAccessor.getJiraAdminUser(), generalIssueConfig.getDefaultIssueCreator(), jiraServices.getUserManager());
        if (!optionalJiraUserContext.isPresent()) {
            logger.error("No (valid) user in configuration data; The plugin has likely not yet been configured; The task cannot run (yet)");
            return "error";
        }
        final JiraUserContext jiraUserContext = optionalJiraUserContext.get();
        final String jiraPluginGroupsString = groupsConfig.getGroupsStringDelimited();
        if (!checkUserInPluginGroups(jiraPluginGroupsString, jiraServices.getGroupManager(), jiraUserContext.getDefaultJiraIssueCreatorUser())) {
            logger.error(String.format("User '%s' is no longer in the groups '%s'. The task cannot run.", jiraUserContext.getDefaultJiraIssueCreatorUser().getUsername(), jiraPluginGroupsString));
            return "error";
        }
        final LocalDateTime beforeSetup = LocalDateTime.now();
        final TicketInfoFromSetup ticketInfoFromSetup = new TicketInfoFromSetup();
        try {
            final PreTaskSetup preTaskSetup = new PreTaskSetup();
            preTaskSetup.runPluginSetup(jiraServices, pluginErrorAccessor, issueCreationConfig.getProjectMapping(), ticketInfoFromSetup, jiraUserContext);
        } catch (final Exception e) {
            logger.error("Error during JIRA setup: " + e.getMessage() + "; The task cannot run", e);
            return "error";
        }
        final LocalDateTime afterSetup = LocalDateTime.now();
        final Duration diff = Duration.between(beforeSetup, afterSetup);
        logger.info("Black Duck JIRA setup took " + diff.toMinutes() + "m," + (diff.getSeconds() % 60L) + "s," + (diff.toMillis() % 1000l) + "ms.");
        final BlackDuckJiraTask processor = new BlackDuckJiraTask(globalConfigurationAccessor, pluginConfigurationAccessor, pluginErrorAccessor, jiraUserContext, ticketInfoFromSetup);
        final String runResult = runBlackDuckJiraTaskAndSetLastRunDate(processor, pluginConfigurationAccessor);
        logger.info("blackduck-jira periodic timed task has completed");
        return runResult;
    }

    // Set the last run date immediately so that if the task is rescheduled on a different thread before this one completes, data will not be duplicated.
    private String runBlackDuckJiraTaskAndSetLastRunDate(final BlackDuckJiraTask processor, final PluginConfigurationAccessor pluginConfigurationAccessor) {
        String runStatus = "error";
        final String previousRunDateString = pluginConfigurationAccessor.getLastRunDate();
        final String currentRunDateString = processor.getRunDateString();
        if (previousRunDateString != null && currentRunDateString != null) {
            logger.debug("Before processing, going to set the last run date to the current date: " + currentRunDateString);
            pluginConfigurationAccessor.setLastRunDate(currentRunDateString);
        } else {
            logger.warn(String.format("Before processing, did not update the last run date. Previous run date: %s   Current run date: %s", previousRunDateString, currentRunDateString));
        }
        final Optional<String> newRunDateOptional = processor.execute(previousRunDateString);
        if (newRunDateOptional.isPresent()) {
            final String newRunDate = newRunDateOptional.get();
            logger.debug("After processing, going to set the last run date to the new date: " + newRunDate);
            pluginConfigurationAccessor.setLastRunDate(newRunDate);
            runStatus = newRunDate.equals(previousRunDateString) ? runStatus : "success";
        } else {
            logger.warn("After processing, the new run date was null.");
        }
        // TODO determine if an else case is needed to revert to old last run date
        return runStatus;
    }

    private boolean checkUserInPluginGroups(final String jiraPluginGroupsString, final GroupManager groupManager, final ApplicationUser issueCreator) {
        if (StringUtils.isNotBlank(jiraPluginGroupsString)) {
            final String[] jiraPluginGroups = jiraPluginGroupsString.split(",");
            for (final String blackDuckJiraGroup : jiraPluginGroups) {
                if (groupManager.isUserInGroup(issueCreator, blackDuckJiraGroup.trim())) {
                    return true;
                }
            }
        }
        return false;
    }

}
