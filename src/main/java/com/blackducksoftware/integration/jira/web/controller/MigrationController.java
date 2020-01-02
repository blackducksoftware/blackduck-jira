package com.blackducksoftware.integration.jira.web.controller;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.bc.issue.properties.IssuePropertyService;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.scheduler.SchedulerService;
import com.atlassian.scheduler.SchedulerServiceException;
import com.blackducksoftware.integration.jira.data.accessor.JiraSettingsAccessor;
import com.blackducksoftware.integration.jira.data.accessor.MigrationAccessor;
import com.blackducksoftware.integration.jira.task.maintenance.AlertMigrationRunner;
import com.blackducksoftware.integration.jira.web.action.MigrationActions;

@Path("/blackduck/migration")
public class MigrationController extends ConfigController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final MigrationActions migrationActions;
    private final MigrationAccessor migrationAccessor;

    public MigrationController(PluginSettingsFactory pluginSettingsFactory, TransactionTemplate transactionTemplate, UserManager userManager, SchedulerService schedulerService, JiraSettingsAccessor jiraSettingsAccessor,
        IssuePropertyService issuePropertyService, MigrationAccessor migrationAccessor) {
        super(pluginSettingsFactory, transactionTemplate, userManager);
        this.migrationActions = new MigrationActions(schedulerService, jiraSettingsAccessor, issuePropertyService);
        this.migrationAccessor = migrationAccessor;
    }

    @Path("/projects")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMigratedProjects(@Context HttpServletRequest request) {
        boolean validAuthentication = isAuthorized(request);
        if (!validAuthentication) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        List<String> migratedProjects = migrationAccessor.getMigratedProjects();
        return Response.ok(migratedProjects).build();
    }

    @Path("/projects")
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeMigratedProjects(List<String> projectsToDelete, @Context HttpServletRequest request) {
        boolean validAuthentication = isAuthorized(request);
        if (!validAuthentication) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        List<String> migratedProjects = migrationAccessor.getMigratedProjects();
        List<String> updatedProjects = migratedProjects.stream()
                                           .filter(project -> !projectsToDelete.contains(project))
                                           .collect(Collectors.toList());
        migrationAccessor.updateMigratedProjects(updatedProjects);

        return Response.ok().build();
    }

    @Path("/start")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMigrationTaskStartTime(@Context HttpServletRequest request) {
        boolean validAuthentication = isAuthorized(request);
        if (!validAuthentication) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        Date migrationStartTime = migrationActions.getMigrationStartTime();
        return Response.ok(migrationStartTime).build();
    }

    @Path("/end")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMigrationTaskEndTime(@Context HttpServletRequest request) {
        boolean validAuthentication = isAuthorized(request);
        if (!validAuthentication) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Date migrationEndTime = migrationActions.getMigrationStartTime();
        return Response.ok(migrationEndTime).build();
    }

    @Path("/status")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMigrationTaskStatus(@Context HttpServletRequest request) {
        boolean validAuthentication = isAuthorized(request);
        if (!validAuthentication) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String status = migrationActions.getMigrationStatus();
        return Response.ok(status).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response startMigration(@Context HttpServletRequest request) {
        boolean validAuthentication = isAuthorized(request);
        if (!validAuthentication) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        try {
            migrationActions.startMigration();
        } catch (SchedulerServiceException e) {
            logger.error(String.format("Could not start %s.", AlertMigrationRunner.HUMAN_READABLE_TASK_NAME), e);
            return Response.ok(String.format("Could not start %s. Error: %s", AlertMigrationRunner.HUMAN_READABLE_TASK_NAME, e.getMessage())).status(Response.Status.BAD_REQUEST).build();
        }
        return Response.ok().build();
    }

}
