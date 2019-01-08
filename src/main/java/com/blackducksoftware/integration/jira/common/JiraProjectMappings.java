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
package com.blackducksoftware.integration.jira.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.blackducksoftware.integration.jira.common.model.BlackDuckProject;
import com.blackducksoftware.integration.jira.common.model.BlackDuckProjectMapping;
import com.blackducksoftware.integration.jira.common.model.JiraProject;

public class JiraProjectMappings {
    private final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final Set<BlackDuckProjectMapping> mappings;

    public JiraProjectMappings(final Set<BlackDuckProjectMapping> mappings) {
        this.mappings = mappings;
    }

    public List<BlackDuckProject> getHubProjects(final Long jiraProjectId) {
        final List<BlackDuckProject> matchingBlackDuckProjects = new ArrayList<>();

        if (mappings == null || mappings.isEmpty()) {
            logger.debug("There are no configured project mapping");
            return matchingBlackDuckProjects;
        }

        for (final BlackDuckProjectMapping mapping : mappings) {
            final JiraProject jiraProject = mapping.getJiraProject();
            final BlackDuckProject blackDuckProject = mapping.getHubProject();

            // Check by name because the notifications may be for Black Duck projects that the User doesnt have access to
            logger.debug("Black Duck Project:                                 " + blackDuckProject.getProjectName());
            logger.debug("jiraProject.getProjectName() (from config mapping): " + jiraProject.getProjectName());
            logger.debug("jiraProject Id:                                     " + jiraProjectId);
            if ((jiraProject.getProjectId() != null) && (jiraProject.getProjectId().equals(jiraProjectId))) {
                logger.debug("Match!");
                matchingBlackDuckProjects.add(blackDuckProject);
            }
        }
        logger.debug("Number of matches found: " + matchingBlackDuckProjects.size());
        return matchingBlackDuckProjects;
    }

    public int size() {
        if (mappings == null) {
            return 0;
        }
        return mappings.size();
    }
}
