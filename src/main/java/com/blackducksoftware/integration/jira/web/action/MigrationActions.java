package com.blackducksoftware.integration.jira.web.action;

import java.time.Instant;
import java.util.Date;

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
import com.blackducksoftware.integration.jira.task.maintenance.AlertMigrationRunner;

public class MigrationActions {
    private static final String MIGRATION_JOB_NAME = MigrationActions.class.getName() + ":migration";
    private static final JobRunnerKey MIGRATION_JOB_RUNNER_KEY = JobRunnerKey.of(MIGRATION_JOB_NAME);

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final SchedulerService schedulerService;
    private final JiraSettingsAccessor jiraSettingsAccessor;
    private final IssuePropertyService issuePropertyService;
    private final AlertMigrationRunner alertMigrationRunner;

    public MigrationActions(SchedulerService schedulerService, JiraSettingsAccessor jiraSettingsAccessor, IssuePropertyService issuePropertyService) {
        this.schedulerService = schedulerService;
        this.jiraSettingsAccessor = jiraSettingsAccessor;
        this.issuePropertyService = issuePropertyService;
        this.alertMigrationRunner = new AlertMigrationRunner(jiraSettingsAccessor, issuePropertyService);

        schedulerService.registerJobRunner(MIGRATION_JOB_RUNNER_KEY, alertMigrationRunner);
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

    public Date getMigrationStartTime() {
        return alertMigrationRunner.getStartTime();
    }

    public Date getMigrationEndTime() {
        return alertMigrationRunner.getEndTime();
    }

    public String getMigrationStatus() {
        return alertMigrationRunner.getStatus();
    }
}

