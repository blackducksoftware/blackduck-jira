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
package com.blackducksoftware.integration.jira.web.action;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.scheduler.SchedulerService;
import com.atlassian.scheduler.SchedulerServiceException;
import com.atlassian.scheduler.config.JobConfig;
import com.atlassian.scheduler.config.JobId;
import com.atlassian.scheduler.config.JobRunnerKey;
import com.atlassian.scheduler.config.RunMode;
import com.atlassian.scheduler.config.Schedule;
import com.blackducksoftware.integration.jira.data.accessor.JiraSettingsAccessor;
import com.blackducksoftware.integration.jira.data.accessor.MigrationAccessor;
import com.blackducksoftware.integration.jira.issue.model.ProjectMappingConfigModel;
import com.blackducksoftware.integration.jira.task.maintenance.AlertMigrationRunner;
import com.blackducksoftware.integration.jira.web.model.BlackDuckJiraConfigSerializable;
import com.blackducksoftware.integration.jira.web.model.BlackDuckProjectMapping;
import com.blackducksoftware.integration.jira.web.model.JiraProject;
import com.blackducksoftware.integration.jira.web.model.MigrationDetails;

public class MigrationActions {
    private static final String MIGRATION_JOB_NAME = MigrationActions.class.getName() + ":migration";
    private static final JobRunnerKey MIGRATION_JOB_RUNNER_KEY = JobRunnerKey.of(MIGRATION_JOB_NAME);
    private static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss '(UTC)'";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final SchedulerService schedulerService;
    private final JiraSettingsAccessor jiraSettingsAccessor;
    private final MigrationAccessor migrationAccessor;
    private final AlertMigrationRunner alertMigrationRunner;

    public MigrationActions(SchedulerService schedulerService, JiraSettingsAccessor jiraSettingsAccessor, MigrationAccessor migrationAccessor) {
        this.schedulerService = schedulerService;
        this.jiraSettingsAccessor = jiraSettingsAccessor;
        this.alertMigrationRunner = new AlertMigrationRunner(jiraSettingsAccessor);
        this.migrationAccessor = migrationAccessor;
        schedulerService.registerJobRunner(MIGRATION_JOB_RUNNER_KEY, alertMigrationRunner);
    }

    public void removeProjectsFromCompletedList(List<String> projectsToDelete) {
        List<String> updatedProjects = migrationAccessor.getMigratedProjects().stream()
                                           .filter(project -> !projectsToDelete.contains(project))
                                           .collect(Collectors.toList());
        migrationAccessor.updateMigratedProjects(updatedProjects);
    }

    public void startMigration() throws SchedulerServiceException {
        JobRunnerKey jobRunnerKey = JobRunnerKey.of(MIGRATION_JOB_NAME);
        JobConfig jobConfig = JobConfig
                                  .forJobRunnerKey(jobRunnerKey)
                                  .withRunMode(RunMode.RUN_LOCALLY)
                                  .withSchedule(Schedule.runOnce(Date.from(Instant.now())));

        JobId jobId = JobId.of(MIGRATION_JOB_NAME);
        schedulerService.scheduleJob(jobId, jobConfig);
        logger.info(String.format("%s scheduled to run now.", AlertMigrationRunner.HUMAN_READABLE_TASK_NAME));
    }

    public MigrationDetails getMigrationDetails() {
        ProjectMappingConfigModel projectMapping = jiraSettingsAccessor.createGlobalConfigurationAccessor().getIssueCreationConfig().getProjectMapping();
        BlackDuckJiraConfigSerializable config = new BlackDuckJiraConfigSerializable();
        config.setHubProjectMappingsJson(projectMapping.getMappingsJson());

        List<JiraProject> jiraProjects = config.getHubProjectMappings().stream().map(BlackDuckProjectMapping::getJiraProject).collect(Collectors.toList());
        List<String> projectsToBeMigrated = new ArrayList<>();

        List<String> migratedProjects = migrationAccessor.getMigratedProjects();
        if (null != jiraProjects && !jiraProjects.isEmpty()) {
            projectsToBeMigrated = jiraProjects.stream()
                                       .map(JiraProject::getProjectName)
                                       .filter(projectName -> !migratedProjects.contains(projectName))
                                       .collect(Collectors.toList());
        }

        MigrationDetails migrationDetails = new MigrationDetails();
        migrationDetails.setMigratedProjects(migratedProjects);
        migrationDetails.setProjectsToMigrate(projectsToBeMigrated);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TIME_FORMAT);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date startTime = alertMigrationRunner.getStartTime();
        Date endTime = alertMigrationRunner.getEndTime();
        if (null != startTime) {
            migrationDetails.setMigrationStartTime(simpleDateFormat.format(startTime));
        }
        if (null != endTime) {
            migrationDetails.setMigrationEndTime(simpleDateFormat.format(endTime));
        }
        migrationDetails.setMigrationStatus(alertMigrationRunner.getStatus());
        return migrationDetails;
    }

}

