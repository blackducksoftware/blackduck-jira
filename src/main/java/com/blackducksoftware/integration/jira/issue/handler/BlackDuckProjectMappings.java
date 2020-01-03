/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2020 Synopsys, Inc.
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
package com.blackducksoftware.integration.jira.issue.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.jira.web.JiraServices;
import com.blackducksoftware.integration.jira.web.model.BlackDuckProjectMapping;
import com.blackducksoftware.integration.jira.web.model.JiraProject;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;

public class BlackDuckProjectMappings {
    public static final String MAP_ALL_PROJECTS = "-- ALL BLACK DUCK PROJECTS --";

    private final Logger logger = LoggerFactory.getLogger(BlackDuckProjectMapping.class);

    private final Set<BlackDuckProjectMapping> mappings;
    private final JiraServices jiraServices;

    public BlackDuckProjectMappings(final JiraServices jiraServices, final Set<BlackDuckProjectMapping> mappings) {
        this.jiraServices = jiraServices;
        this.mappings = mappings;
    }

    public Collection<JiraProject> getJiraProjects(final List<String> blackDuckProjectNames) {
        final Set<JiraProject> matchingJiraProjects = new HashSet<>();
        for (final String blackDuckProjectName : blackDuckProjectNames) {
            final List<JiraProject> matches = getJiraProjects(blackDuckProjectName);
            matchingJiraProjects.addAll(matches);
        }
        return matchingJiraProjects;
    }

    public List<JiraProject> getJiraProjects(final String blackDuckProjectName) {
        final List<JiraProject> matchingJiraProjects = new ArrayList<>();
        if (mappings == null || mappings.isEmpty()) {
            logger.debug("There are no configured project mappings");
            return matchingJiraProjects;
        }

        for (final BlackDuckProjectMapping mapping : mappings) {
            final JiraProject mappingJiraProject = mapping.getJiraProject();
            final JiraProject jiraProject;
            try {
                jiraProject = jiraServices.getJiraProject(mappingJiraProject.getProjectId());
                jiraProject.setIssueCreator(mappingJiraProject.getIssueCreator());
                jiraProject.setConfiguredForVulnerabilities(mappingJiraProject.isConfiguredForVulnerabilities());
            } catch (final BlackDuckIntegrationException e) {
                logger.warn("Mapped project '" + mappingJiraProject.getProjectName() + "' with ID " + mappingJiraProject.getProjectId() + " not found in JIRA; skipping this notification");
                continue;
            }
            if (StringUtils.isNotBlank(jiraProject.getProjectError())) {
                logger.error(jiraProject.getProjectError());
            } else {
                logger.debug("JIRA Project: " + jiraProject);
                final String blackDuckProject = mapping.getBlackDuckProjectName();
                if (appliesForBlackDuckProject(blackDuckProject, blackDuckProjectName, mapping.isProjectPattern())) {
                    logger.debug("Match!");
                    matchingJiraProjects.add(jiraProject);
                }
            }
        }
        logger.debug("Number of matches found: " + matchingJiraProjects.size());
        return matchingJiraProjects;
    }

    public int size() {
        if (mappings == null) {
            return 0;
        }
        return mappings.size();
    }

    private boolean appliesForBlackDuckProject(final String blackDuckProjectNameFromMapping, final String blackDuckProjectNameFromNotification, final boolean isProjectPattern) {
        logger.debug("blackDuckProjectName (from notification content)       : " + blackDuckProjectNameFromNotification);
        if (isProjectPattern) {
            logger.debug("blackDuckProjectPattern (from config mapping): " + blackDuckProjectNameFromMapping);
            return blackDuckProjectNameFromNotification.matches(blackDuckProjectNameFromMapping);
        }

        logger.debug("blackDuckProject (from config mapping): " + blackDuckProjectNameFromMapping);
        if (MAP_ALL_PROJECTS.equals(blackDuckProjectNameFromMapping)) {
            return true;
        }
        // Check by name because the notifications may be for Black Duck projects that the User doesn't have access to
        return StringUtils.isNotBlank(blackDuckProjectNameFromMapping) && (blackDuckProjectNameFromMapping.equals(blackDuckProjectNameFromNotification));
    }

}
