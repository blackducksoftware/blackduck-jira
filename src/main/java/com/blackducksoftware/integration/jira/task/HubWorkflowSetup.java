package com.blackducksoftware.integration.jira.task;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.workflow.AssignableWorkflowScheme;
import com.atlassian.jira.workflow.ConfigurableJiraWorkflow;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.jira.workflow.WorkflowUtil;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

public class HubWorkflowSetup {

	private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));

	private final JiraSettingsService settingService;

	private final WorkflowManager workflowManager;

	private final WorkflowSchemeManager workflowSchemeManager;

	private final UserManager jiraUserManager;

	private final String jiraUser;

	public HubWorkflowSetup(final JiraSettingsService settingService,
			final WorkflowManager workflowManager, final WorkflowSchemeManager workflowSchemeManager,
			final UserManager jiraUserManager, final String jiraUser) {
		this.settingService = settingService;
		this.workflowManager = workflowManager;
		this.workflowSchemeManager = workflowSchemeManager;
		this.jiraUserManager = jiraUserManager;
		this.jiraUser = jiraUser;
	}

	public JiraWorkflow addHubWorkflowToJira() {
		try {
			JiraWorkflow hubWorkflow = workflowManager.getWorkflow(HubJiraConstants.HUB_JIRA_WORKFLOW);
			if (hubWorkflow == null) {
				// https://developer.atlassian.com/confdev/development-resources/confluence-developer-faq/what-is-the-best-way-to-load-a-class-or-resource-from-a-plugin
				final InputStream inputStream = ClassLoaderUtils
						.getResourceAsStream(HubJiraConstants.HUB_JIRA_WORKFLOW_RESOURCE, this.getClass());
				if (inputStream == null) {
					logger.error("Could not find the Hub Jira workflow resource.");
					settingService.addHubError("Could not find the Hub Jira workflow resource.", "addHubWorkflow");
					return null;
				}
				final ApplicationUser jiraAppUser = jiraUserManager.getUserByName(jiraUser);
				if (jiraAppUser == null) {
					logger.error("Could not find the Jira User that saved the Hub Jira config.");
					return null;
				}
				final String workflowXml = IOUtils.toString(inputStream, StandardCharsets.UTF_8);

				final WorkflowDescriptor workflowDescriptor = WorkflowUtil.convertXMLtoWorkflowDescriptor(workflowXml);

				hubWorkflow = new ConfigurableJiraWorkflow(HubJiraConstants.HUB_JIRA_WORKFLOW,
						workflowDescriptor, workflowManager);

				workflowManager.createWorkflow(jiraAppUser, hubWorkflow);
				logger.debug("Created the Hub Workflow : " + HubJiraConstants.HUB_JIRA_WORKFLOW);
			}
			return hubWorkflow;
		} catch (final Exception e) {
			logger.error("Failed to add the Hub Jira worflow.", e);
			settingService.addHubError(e, "addHubWorkflow");
		}
		return null;
	}

	public void addWorkflowToProjectsWorkflowScheme(final JiraWorkflow hubWorkflow, final Project project,
			final List<IssueType> issueTypes) {
		try {
			final AssignableWorkflowScheme projectWorkflowScheme = workflowSchemeManager.getWorkflowSchemeObj(project);

			if (projectWorkflowScheme != null) {
				final AssignableWorkflowScheme.Builder projectWorkflowSchemeBuilder = projectWorkflowScheme.builder();
				// FIXME should check if the workflow scheme is the default, we dont
				// want to modify the default scheme right??

				boolean needsToBeUpdated = false;
				// IMPORTANT we assume our custom issue types are already in this
				// Projects Workflow scheme
				if (issueTypes != null && !issueTypes.isEmpty()) {
					for (final IssueType issueType : issueTypes) {
						final String workflowName = projectWorkflowScheme.getConfiguredWorkflow(issueType.getId());

						if (StringUtils.isBlank(workflowName)) {
							projectWorkflowSchemeBuilder.setMapping(issueType.getId(), hubWorkflow.getName());
							logger.debug("Updating Jira Project : " + project.getName() + ", Issue Type : "
									+ issueType.getName() + ", to the Hub workflow '" + hubWorkflow.getName() + "'");
							needsToBeUpdated = true;
						} else {
							if (!workflowName.equals(hubWorkflow.getName())) {
								projectWorkflowSchemeBuilder.setMapping(issueType.getId(), hubWorkflow.getName());
								logger.debug("Updating Jira Project : " + project.getName() + ", Issue Type : "
										+ issueType.getName() + ", to the Hub workflow '" + hubWorkflow.getName()
										+ "'");
								needsToBeUpdated = true;
							}
						}
					}
				}
				if (needsToBeUpdated) {
					workflowSchemeManager.updateWorkflowScheme(projectWorkflowSchemeBuilder.build());
				}
			} else {
				// TODO what if the project has no scheme??
			}
		} catch (final Exception e) {
			logger.error("Failed to add the Hub Jira worflow to the Hub scheme.", e);
			settingService.addHubError(e, null, null, project.getName(), null, "addWorkflowToProjectsWorkflowScheme");
		}
	}

}
