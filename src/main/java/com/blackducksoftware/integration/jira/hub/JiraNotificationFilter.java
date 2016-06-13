package com.blackducksoftware.integration.jira.hub;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
	private final Set<HubProjectMapping> mappings;
	private final JiraService jiraService;

	public JiraNotificationFilter(HubNotificationService hubNotificationService, JiraService jiraService,
			Set<HubProjectMapping> mappings) {
		this.hubNotificationService = hubNotificationService;
		this.jiraService = jiraService;
		this.mappings = mappings;
	}

	public List<JiraReadyNotification> extractJiraReadyNotifications(List<NotificationItem> notifications) {
		List<JiraReadyNotification> jiraReadyNotifications = new ArrayList<>();

		System.out.println("JiraNotificationFilter.extractJiraReadyNotifications(): Sifting through "
				+ notifications.size() + " notifications");
		for (NotificationItem notif : notifications) {
			System.out.println("Notification: " + notif);
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
				System.out
						.println("No configuration project mapping matching this notification found; skipping this notification");
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
		System.out.println("JiraNotificationFilter.getMatchingMapping() Sifting through " + mappings.size()
				+ " mappings, looking for a match for this notification's Hub project: " + notifHubProjectUrl);
		for (HubProjectMapping mapping : mappings) {
			System.out.println("Mapping: " + mapping);
			String mappingHubProjectUrl = mapping.getHubProject().getProjectUrl();
			if (mappingHubProjectUrl.equals(notifHubProjectUrl)) {
				return mapping;
			}
		}
		return null;
	}
}
