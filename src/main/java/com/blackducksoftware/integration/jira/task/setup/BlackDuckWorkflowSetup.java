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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.AssignableWorkflowScheme;
import com.atlassian.jira.workflow.ConfigurableJiraWorkflow;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowUtil;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.config.JiraServices;
import com.blackducksoftware.integration.jira.config.JiraSettingsService;
import com.opensymphony.workflow.FactoryException;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

public class BlackDuckWorkflowSetup {
    private final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final JiraSettingsService settingService;
    private final JiraServices jiraServices;

    public BlackDuckWorkflowSetup(final JiraSettingsService settingService, final JiraServices jiraServices) {
        this.settingService = settingService;
        this.jiraServices = jiraServices;
    }

    public JiraSettingsService getSettingService() {
        return settingService;
    }

    public JiraServices getJiraServices() {
        return jiraServices;
    }

    public Optional<JiraWorkflow> addBlackDuckWorkflowToJira() {
        try {
            final WorkflowManager workflowManager = jiraServices.getWorkflowManager();
            final JiraWorkflow blackDuckWorkflow = workflowManager.getWorkflow(BlackDuckJiraConstants.BLACKDUCK_JIRA_WORKFLOW);
            if (blackDuckWorkflow == null) {
                final Optional<ApplicationUser> jiraAppUser = getJiraSystemAdmin();
                if (!jiraAppUser.isPresent()) {
                    logger.error("Could not find any JIRA System Admins to create the workflow.");
                    return Optional.empty();
                }
                final Optional<WorkflowDescriptor> workflowDescriptor = getWorkflowDescriptorFromResource();
                final Optional<JiraWorkflow> workflowOptional = workflowDescriptor.map(descriptor -> new ConfigurableJiraWorkflow(BlackDuckJiraConstants.BLACKDUCK_JIRA_WORKFLOW, descriptor, workflowManager));
                workflowOptional.ifPresent(workflow -> {
                    workflowManager.createWorkflow(jiraAppUser.get(), workflow);
                    logger.debug("Created the Black Duck Workflow : " + BlackDuckJiraConstants.BLACKDUCK_JIRA_WORKFLOW);
                });
                return workflowOptional;
            }
            return Optional.of(blackDuckWorkflow);
        } catch (final Exception e) {
            logger.error("Failed to add the Black Duck JIRA workflow.", e);
            settingService.addBlackDuckError(e, "addBlackDuckWorkflow");
        }
        return Optional.empty();
    }

    private Optional<WorkflowDescriptor> getWorkflowDescriptorFromResource() throws IOException, FactoryException {
        // https://developer.atlassian.com/confdev/development-resources/confluence-developer-faq/what-is-the-best-way-to-load-a-class-or-resource-from-a-plugin
        final InputStream inputStream = ClassLoaderUtils.getResourceAsStream(BlackDuckJiraConstants.BLACKDUCK_JIRA_WORKFLOW_RESOURCE, this.getClass());
        if (inputStream == null) {
            logger.error("Could not find the Black Duck JIRA workflow resource.");
            settingService.addBlackDuckError("Could not find the Black Duck JIRA workflow resource.", "addBlackDuckWorkflow");
            return Optional.empty();
        }
        final String workflowXml = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());

        return Optional.ofNullable(WorkflowUtil.convertXMLtoWorkflowDescriptor(workflowXml));
    }

    // FIXME this should happen when the global project mappings are saved
    public void addWorkflowToProjectsWorkflowScheme(final JiraWorkflow blackDuckWorkflow, final Project project, final List<IssueType> issueTypes) {
        try {
            final AssignableWorkflowScheme projectWorkflowScheme = jiraServices.getWorkflowSchemeManager().getWorkflowSchemeObj(project);
            final AssignableWorkflowScheme.Builder projectWorkflowSchemeBuilder = projectWorkflowScheme.builder();
            boolean needsToBeUpdated = false;
            if (issueTypes != null && !issueTypes.isEmpty()) {
                for (final IssueType issueType : issueTypes) {
                    needsToBeUpdated = mapIssueTypeToBdsWorkflow(project, blackDuckWorkflow, projectWorkflowScheme, projectWorkflowSchemeBuilder, issueType, needsToBeUpdated);
                }
            }
            if (needsToBeUpdated) {
                jiraServices.getWorkflowSchemeManager().updateWorkflowScheme(projectWorkflowSchemeBuilder.build());
            }
        } catch (final Exception e) {
            logger.error("Failed to add the Black Duck JIRA worflow to the Black Duck scheme.", e);
            settingService.addBlackDuckError(e, null, null, project.getName(), null, null, "addWorkflowToProjectsWorkflowScheme");
        }
    }

    private boolean mapIssueTypeToBdsWorkflow(final Project project, final JiraWorkflow blackDuckWorkflow, final AssignableWorkflowScheme projectWorkflowScheme, final AssignableWorkflowScheme.Builder projectWorkflowSchemeBuilder,
        final IssueType issueType, boolean needsToBeUpdated) {
        final String configuredWorkflowName = projectWorkflowScheme.getConfiguredWorkflow(issueType.getId());
        final String actualWorkflowName = projectWorkflowScheme.getActualWorkflow(issueType.getId());
        logger.debug("Configured workflow: " + configuredWorkflowName);
        logger.debug("Actual workflow: " + actualWorkflowName);
        if ((StringUtils.isBlank(actualWorkflowName)) || (!actualWorkflowName.equals(blackDuckWorkflow.getName()))) {
            projectWorkflowSchemeBuilder.setMapping(issueType.getId(), blackDuckWorkflow.getName());
            logger.debug("Updating JIRA Project : " + project.getName() + ", Issue Type : " + issueType.getName() + ", to the Black Duck workflow '" + blackDuckWorkflow.getName() + "'");
            needsToBeUpdated = true;
        }
        return needsToBeUpdated;
    }

    private Optional<ApplicationUser> getJiraSystemAdmin() {
        final Collection<ApplicationUser> jiraSysAdmins = getJiraServices().getUserUtil().getJiraSystemAdministrators();
        if (jiraSysAdmins == null || jiraSysAdmins.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(jiraSysAdmins.iterator().next());
    }

}
