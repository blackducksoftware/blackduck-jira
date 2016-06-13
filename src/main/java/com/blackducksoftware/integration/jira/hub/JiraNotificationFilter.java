package com.blackducksoftware.integration.jira.hub;

import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.integration.jira.config.HubProjectMapping;
import com.blackducksoftware.integration.jira.config.JiraProject;
import com.blackducksoftware.integration.jira.hub.model.notification.NotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.RuleViolationNotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.VulnerabilityNotificationItem;
import com.atlassian.jira.project.ProjectManager;

public class JiraNotificationFilter {
	private final HubNotificationService hubNotificationService;
	private final List<HubProjectMapping> mappings;
	private final ProjectManager jiraProjectManager;

	public JiraNotificationFilter(HubNotificationService hubNotificationService, ProjectManager jiraProjectManager,
			List<HubProjectMapping> mappings) {
		this.hubNotificationService = hubNotificationService;
		this.jiraProjectManager = jiraProjectManager;
		this.mappings = mappings;
	}

	public List<JiraReadyNotification> extractJiraReadyNotifications(List<NotificationItem> notifications) {
		List<JiraReadyNotification> jiraReadyNotifications = new ArrayList<>();

		for (NotificationItem notif : notifications) {
			String notifHubProjectName = "<unknown>";
			String notifHubProjectUrl = "<unknown>";
			try {
				if (notif instanceof VulnerabilityNotificationItem) {
					System.out.println("This is a vulnerability notification; skipping it.");
					continue;
				} else if (notif instanceof RuleViolationNotificationItem) {
					RuleViolationNotificationItem ruleViolationNotificationItem = (RuleViolationNotificationItem) notif;
					notifHubProjectName = ruleViolationNotificationItem.getContent().getProjectName();
					String notifHubProjectVersionUrl = ruleViolationNotificationItem.getContent()
							.getProjectVersionLink();
					notifHubProjectUrl = hubNotificationService
							.getProjectUrlFromProjectReleaseUrl(notifHubProjectVersionUrl);
				} else if (notif instanceof PolicyOverrideNotificationItem) {
					PolicyOverrideNotificationItem policyOverrideNotificationItem = (PolicyOverrideNotificationItem) notif;
					notifHubProjectName = policyOverrideNotificationItem.getContent().getProjectName();
					String notifHubProjectVersionUrl = policyOverrideNotificationItem.getContent()
							.getProjectVersionLink();
					notifHubProjectUrl = hubNotificationService
							.getProjectUrlFromProjectReleaseUrl(notifHubProjectVersionUrl);
				}
			} catch (HubNotificationServiceException e) {
				// TODO log error
				System.out.println("Error extracting details from the Hub notification: " + notif + ": "
						+ e.getMessage());
			}

			HubProjectMapping mapping = getMatchingMapping(notifHubProjectUrl);
			if (mapping == null) {
				System.out.println("No mapping matching this notification found; skipping this notification");
			}

			JiraProject bdsJiraProject = mapping.getJiraProject();
			// TODO get the jira interaction out of this class? Move it to
			// JiraService
			String mappingHubProjectName = mapping.getHubProject().getProjectName();
			String mappingHubProjectUrl = mapping.getHubProject().getProjectUrl();

			long jiraProjectId = bdsJiraProject.getProjectId();
			com.atlassian.jira.project.Project atlassianJiraProject = jiraProjectManager.getProjectObj(jiraProjectId);
			if (atlassianJiraProject == null) {
				System.out.println("Error: JIRA Project '" + bdsJiraProject.getProjectName() + "' with ID "
						+ jiraProjectId + " not found. No tickets will be created for it");
				// TODO remove it from the mapping
				continue;
			}
			String jiraProjectKey = atlassianJiraProject.getKey();
			String jiraProjectName = atlassianJiraProject.getName();

			System.out.println("Notification hub project " + notifHubProjectName + " matches mapping hub project: "
					+ mappingHubProjectName);
			System.out.println("The corresponding JIRA project is: " + jiraProjectName);
			JiraReadyNotification jiraReadyNotification = new JiraReadyNotification(jiraProjectKey, jiraProjectName,
					notifHubProjectName, notif);
			jiraReadyNotifications.add(jiraReadyNotification);
		}
		return jiraReadyNotifications;
	}

	private HubProjectMapping getMatchingMapping(String notifHubProjectUrl) {
		for (HubProjectMapping mapping : mappings) {
			String mappingHubProjectUrl = mapping.getHubProject().getProjectUrl();
			if (mappingHubProjectUrl.equals(notifHubProjectUrl)) {
				return mapping;
			}
		}
		return null;
	}
}
