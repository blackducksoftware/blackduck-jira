package com.blackducksoftware.integration.jira.web.action;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.bc.issue.properties.IssuePropertyService;
import com.atlassian.scheduler.SchedulerService;
import com.atlassian.scheduler.SchedulerServiceException;
import com.atlassian.scheduler.config.JobConfig;
import com.atlassian.scheduler.config.JobId;
import com.atlassian.scheduler.config.JobRunnerKey;
import com.atlassian.scheduler.config.RunMode;
import com.atlassian.scheduler.config.Schedule;
import com.blackducksoftware.integration.jira.data.accessor.JiraSettingsAccessor;
import com.blackducksoftware.integration.jira.data.accessor.MigrationAccessor;
import com.blackducksoftware.integration.jira.task.maintenance.AlertMigrationRunner;
import com.blackducksoftware.integration.jira.web.model.MigrationDetails;

public class MigrationActions {
    private static final String MIGRATION_JOB_NAME = MigrationActions.class.getName() + ":migration";
    private static final JobRunnerKey MIGRATION_JOB_RUNNER_KEY = JobRunnerKey.of(MIGRATION_JOB_NAME);

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final SchedulerService schedulerService;
    private final MigrationAccessor migrationAccessor;
    private final AlertMigrationRunner alertMigrationRunner;

    public MigrationActions(SchedulerService schedulerService, JiraSettingsAccessor jiraSettingsAccessor, IssuePropertyService issuePropertyService, MigrationAccessor migrationAccessor) {
        this.schedulerService = schedulerService;
        this.alertMigrationRunner = new AlertMigrationRunner(jiraSettingsAccessor, issuePropertyService);
        this.migrationAccessor = migrationAccessor;
        schedulerService.registerJobRunner(MIGRATION_JOB_RUNNER_KEY, alertMigrationRunner);
    }

    public void removeProjectsFromCompletedList(List<String> projectsToDelete) {
        List<String> migratedProjects = migrationAccessor.getMigratedProjects();
        List<String> updatedProjects = migratedProjects.stream()
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
        MigrationDetails migrationDetails = new MigrationDetails();
        migrationDetails.setMigratedProjects(migrationAccessor.getMigratedProjects());
        migrationDetails.setMigrationStartTime(alertMigrationRunner.getStartTime());
        migrationDetails.setMigrationEndTime(alertMigrationRunner.getEndTime());
        migrationDetails.setMigrationStatus(alertMigrationRunner.getStatus());
        return migrationDetails;
    }

}
