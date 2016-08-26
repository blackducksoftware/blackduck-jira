package com.blackducksoftware.integration.jira.task;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
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

			final AssignableWorkflowScheme.Builder projectWorkflowSchemeBuidler = projectWorkflowScheme.builder();

			final Map<String, String> issueMappings = projectWorkflowScheme.getMappings();

			// IMPORTANT we assume our custom issue types are already in this
			// Projects Workflow scheme
			for (final IssueType issueType : issueTypes) {
				final String workflowName = issueMappings.get(issueType.getId());
				if (!workflowName.equals(hubWorkflow.getName())) {
					projectWorkflowSchemeBuidler.setMapping(issueType.getId(), hubWorkflow.getName());
				}
			}
			workflowSchemeManager.updateWorkflowScheme(projectWorkflowScheme);
		} catch (final Exception e) {
			logger.error("Failed to add the Hub Jira worflow to the Hub scheme.", e);
			settingService.addHubError(e, null, null, project.getName(), null, "addWorkflowToProjectsWorkflowScheme");
		}
	}

}
