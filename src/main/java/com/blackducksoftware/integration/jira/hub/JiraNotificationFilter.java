package com.blackducksoftware.integration.jira.hub;

import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.integration.jira.config.HubProjectMapping;
import com.blackducksoftware.integration.jira.config.JiraProject;
import com.blackducksoftware.integration.jira.hub.model.notification.NotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.RuleViolationNotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.VulnerabilityNotificationItem;
import com.blackducksoftware.integration.jira.service.JiraService;
import com.blackducksoftware.integration.jira.service.JiraServiceException;

public class JiraNotificationFilter {
	private final HubNotificationService hubNotificationService;
	private final List<HubProjectMapping> mappings;
	private final JiraService jiraService;

	public JiraNotificationFilter(HubNotificationService hubNotificationService, JiraService jiraService,
			List<HubProjectMapping> mappings) {
		this.hubNotificationService = hubNotificationService;
		this.jiraService = jiraService;
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
				continue;
			}

			HubProjectMapping mapping = getMatchingMapping(notifHubProjectUrl);
			if (mapping == null) {
				System.out.println("No mapping matching this notification found; skipping this notification");
				continue;
			}

			JiraProject mappingJiraProject = mapping.getJiraProject();
			JiraProject freshBdsJiraProject;
			try {
				freshBdsJiraProject = jiraService.getProject(mappingJiraProject.getProjectId());
			} catch (JiraServiceException e) {
				System.out.println("Mapped project '" + mappingJiraProject.getProjectName() + "' with ID "
						+ mappingJiraProject.getProjectId() + " not found in JIRA; skipping this notification");
				continue;
			}

			System.out.println("Notification hub project " + notifHubProjectName + " matches mapping hub project: "
					+ mapping.getHubProject().getProjectName());
			System.out.println("The corresponding JIRA project is: " + freshBdsJiraProject.getProjectName());
			JiraReadyNotification jiraReadyNotification = new JiraReadyNotification(
					freshBdsJiraProject.getProjectKey(), freshBdsJiraProject.getProjectName(), notifHubProjectName,
					notif);
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
