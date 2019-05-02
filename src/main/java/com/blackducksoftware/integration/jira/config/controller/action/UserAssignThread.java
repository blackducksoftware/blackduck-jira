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
package com.blackducksoftware.integration.jira.config.controller.action;

import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

import com.blackducksoftware.integration.jira.common.BlackDuckAssignUtil;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.model.BlackDuckProjectMapping;
import com.blackducksoftware.integration.jira.common.settings.GlobalConfigurationAccessor;
import com.blackducksoftware.integration.jira.common.settings.PluginErrorAccessor;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.exception.IntegrationException;

public class UserAssignThread extends Thread {
    private final BlackDuckJiraLogger logger;
    private final GlobalConfigurationAccessor globalConfigurationAccessor;
    private final PluginErrorAccessor pluginErrorAccessor;

    private transient boolean shouldTimeout;

    public UserAssignThread(final String threadName, final BlackDuckJiraLogger logger, final GlobalConfigurationAccessor globalConfigurationAccessor, final PluginErrorAccessor pluginErrorAccessor) {
        super(threadName);
        this.logger = logger;
        this.globalConfigurationAccessor = globalConfigurationAccessor;
        this.pluginErrorAccessor = pluginErrorAccessor;
    }

    @Override
    public void run() {
        logger.debug("Starting User Assignment thread");
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                shouldTimeout = true;
            }
        }, 600000);

        final BlackDuckAssignUtil blackDuckAssignUtil = new BlackDuckAssignUtil();
        try {
            final Set<BlackDuckProjectMapping> blackDuckProjectMappings = blackDuckAssignUtil.getBlackDuckProjectMappings(globalConfigurationAccessor);
            if (blackDuckProjectMappings.isEmpty()) {
                return;
            }
            checkShouldInterruptOrTimeout();
            final BlackDuckService blackDuckService = blackDuckAssignUtil.getBlackDuckService(globalConfigurationAccessor);
            final List<ProjectView> allProjects = blackDuckAssignUtil.getAllBDProjects(blackDuckService);
            checkShouldInterruptOrTimeout();
            final Set<ProjectView> matchingProjects = blackDuckAssignUtil.getMatchingBDProjects(blackDuckProjectMappings, allProjects);
            if (matchingProjects.isEmpty()) {
                return;
            }
            checkShouldInterruptOrTimeout();
            final UserView currentUser = blackDuckAssignUtil.getCurrentUser(blackDuckService);
            checkShouldInterruptOrTimeout();
            final Set<ProjectView> nonAssignedProjects = blackDuckAssignUtil.getProjectsThatNeedAssigning(blackDuckService, currentUser, matchingProjects);
            if (nonAssignedProjects.isEmpty()) {
                return;
            }
            checkShouldInterruptOrTimeout();
            blackDuckAssignUtil.assignUserToProjects(pluginErrorAccessor, blackDuckService, currentUser, nonAssignedProjects);
            logger.debug("Completed User Assignment");
        } catch (final IntegrationException e) {
            logger.error("Could not assign the Black Duck user to the configured Black Duck projects. " + e.getMessage(), e);
            pluginErrorAccessor.addBlackDuckError(e, "assignUserToBlackDuckProject");
        } catch (final InterruptedException e) {
            logger.warn("The user assignment thread was interrupted.");
        } catch (final TimeoutException e) {
            logger.error("The user assignment thread timed out after 10 minutes.");
        }
    }

    private void checkShouldInterruptOrTimeout() throws InterruptedException, TimeoutException {
        if (!Thread.currentThread().isAlive() || Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
        if (shouldTimeout) {
            throw new TimeoutException();
        }
    }
}
