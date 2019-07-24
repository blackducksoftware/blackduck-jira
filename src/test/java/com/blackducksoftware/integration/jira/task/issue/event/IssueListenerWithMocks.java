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
package com.blackducksoftware.integration.jira.task.issue.event;

import java.util.concurrent.ExecutorService;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.issue.Issue;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.blackducksoftware.integration.jira.issue.handler.JiraIssuePropertyWrapper;
import com.blackducksoftware.integration.jira.issue.tracker.IssueEventListener;
import com.blackducksoftware.integration.jira.issue.tracker.IssueTrackerTask;
import com.blackducksoftware.integration.jira.mocks.issue.ExecutorServiceMock;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;

public class IssueListenerWithMocks extends IssueEventListener {
    private final BlackDuckServicesFactory blackDuckServicesFactory;
    private final JiraIssuePropertyWrapper issuePropertyWrapper;

    public IssueListenerWithMocks(final EventPublisher eventPublisher, final PluginSettingsFactory pluginSettingsFactory, final JiraIssuePropertyWrapper issuePropertyWrapper, final BlackDuckServicesFactory blackDuckServicesFactory) {
        super(eventPublisher, pluginSettingsFactory, issuePropertyWrapper);
        this.blackDuckServicesFactory = blackDuckServicesFactory;
        this.issuePropertyWrapper = issuePropertyWrapper;
    }

    @Override
    public ExecutorService createExecutorService() {
        return new ExecutorServiceMock();
    }

    @Override
    // final Issue issue, final IssueServiceWrapper issueServiceWrapper, final Long eventTypeID, final String jiraBaseUrl, final PluginSettings settings, final String propertyKey, final EntityProperty property
    public IssueTrackerTask createTask(final Issue issue, final Long eventTypeID, final PluginSettings settings, final String propertyKey, final EntityProperty property) {
        return new IssueTrackerTaskWithMocks(issue, issuePropertyWrapper, eventTypeID, settings, propertyKey, property, this.blackDuckServicesFactory);
    }

}
