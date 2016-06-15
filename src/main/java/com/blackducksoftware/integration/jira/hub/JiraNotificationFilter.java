package com.blackducksoftware.integration.jira.hub;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.blackducksoftware.integration.jira.HubJiraLogger;
import com.blackducksoftware.integration.jira.config.HubProjectMapping;
import com.blackducksoftware.integration.jira.config.JiraProject;
import com.blackducksoftware.integration.jira.hub.model.notification.NotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.RuleViolationNotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.VulnerabilityNotificationItem;
import com.blackducksoftware.integration.jira.service.JiraService;
import com.blackducksoftware.integration.jira.service.JiraServiceException;

public class JiraNotificationFilter {
	private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));
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

		logger.debug("JiraNotificationFilter.extractJiraReadyNotifications(): Sifting through " + notifications.size()
				+ " notifications");
		for (NotificationItem notif : notifications) {
			logger.debug("Notification: " + notif);
			String notifHubProjectName = "<unknown>";
			String notifHubProjectUrl = "<unknown>";
			try {
				if (notif instanceof VulnerabilityNotificationItem) {
					logger.debug("This is a vulnerability notification; skipping it.");
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
				logger.error("Error extracting details from the Hub notification: " + notif + ": " + e.getMessage());
				continue;
			}

			HubProjectMapping mapping = getMatchingMapping(notifHubProjectUrl);
			if (mapping == null) {
				logger.debug("No configuration project mapping matching this notification found; skipping this notification");
				continue;
			}

			JiraProject mappingJiraProject = mapping.getJiraProject();
			JiraProject freshBdsJiraProject;
			try {
				freshBdsJiraProject = jiraService.getProject(mappingJiraProject.getProjectId());
			} catch (JiraServiceException e) {
				logger.warn("Mapped project '" + mappingJiraProject.getProjectName() + "' with ID "
						+ mappingJiraProject.getProjectId() + " not found in JIRA; skipping this notification");
				continue;
			}

			logger.debug("Notification hub project " + notifHubProjectName + " matches mapping hub project: "
					+ mapping.getHubProject().getProjectName());
			logger.debug("The corresponding JIRA project is: " + freshBdsJiraProject.getProjectName());
			JiraReadyNotification jiraReadyNotification = new JiraReadyNotification(
					freshBdsJiraProject.getProjectKey(), freshBdsJiraProject.getProjectName(), notifHubProjectName,
					notif);
			jiraReadyNotifications.add(jiraReadyNotification);
		}
		return jiraReadyNotifications;
	}

	private HubProjectMapping getMatchingMapping(String notifHubProjectUrl) {
		logger.debug("JiraNotificationFilter.getMatchingMapping() Sifting through " + mappings.size()
				+ " mappings, looking for a match for this notification's Hub project: " + notifHubProjectUrl);
		for (HubProjectMapping mapping : mappings) {
			logger.debug("Mapping: " + mapping);
			String mappingHubProjectUrl = mapping.getHubProject().getProjectUrl();
			if (mappingHubProjectUrl.equals(notifHubProjectUrl)) {
				return mapping;
			}
		}
		return null;
	}
}
