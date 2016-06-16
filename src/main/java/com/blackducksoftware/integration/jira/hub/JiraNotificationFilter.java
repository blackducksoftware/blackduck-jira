package com.blackducksoftware.integration.jira.hub;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
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
	private final List<String> linksOfRulesToMonitor;
	private final JiraService jiraService;

	public JiraNotificationFilter(final HubNotificationService hubNotificationService, final JiraService jiraService,
			final Set<HubProjectMapping> mappings, final List<String> linksOfRulesToMonitor) {
		this.hubNotificationService = hubNotificationService;
		this.jiraService = jiraService;
		this.mappings = mappings;
		this.linksOfRulesToMonitor = linksOfRulesToMonitor;
	}

	public List<JiraReadyNotification> extractJiraReadyNotifications(final List<NotificationItem> notifications) {
		final List<JiraReadyNotification> jiraReadyNotifications = new ArrayList<>();

		logger.debug("JiraNotificationFilter.extractJiraReadyNotifications(): Sifting through " + notifications.size()
				+ " notifications");
		for (final NotificationItem notif : notifications) {
			logger.debug("Notification: " + notif);
			String notifHubProjectName = "<unknown>";
			String notifHubProjectUrl = "<unknown>";
			List<String> ruleUrls = null;
			try {
				if (notif instanceof VulnerabilityNotificationItem) {
					logger.debug("This is a vulnerability notification; skipping it.");
					continue;
				} else if (notif instanceof RuleViolationNotificationItem) {
					final RuleViolationNotificationItem ruleViolationNotificationItem = (RuleViolationNotificationItem) notif;
					notifHubProjectName = ruleViolationNotificationItem.getContent().getProjectName();
					notifHubProjectUrl = getNotifHubProjectUrl(ruleViolationNotificationItem);
					ruleUrls = hubNotificationService.getLinksOfRulesViolated(ruleViolationNotificationItem);
					if (!isRuleMatch(ruleUrls)) {
						continue;
					}
				} else if (notif instanceof PolicyOverrideNotificationItem) {
					final PolicyOverrideNotificationItem policyOverrideNotificationItem = (PolicyOverrideNotificationItem) notif;
					notifHubProjectName = policyOverrideNotificationItem.getContent().getProjectName();
					notifHubProjectUrl = getNotifHubProjectUrl(policyOverrideNotificationItem);
				}
			} catch (final HubNotificationServiceException e) {
				logger.error("Error extracting details from the Hub notification: " + notif + ": " + e.getMessage());
				continue;
			}

			final HubProjectMapping mapping = getMatchingMapping(notifHubProjectUrl);
			if (mapping == null) {
				logger.debug("No configuration project mapping matching this notification found; skipping this notification");
				continue;
			}

			final JiraProject mappingJiraProject = mapping.getJiraProject();
			JiraProject freshBdsJiraProject;
			try {
				freshBdsJiraProject = jiraService.getProject(mappingJiraProject.getProjectId());
			} catch (final JiraServiceException e) {
				logger.warn("Mapped project '" + mappingJiraProject.getProjectName() + "' with ID "
						+ mappingJiraProject.getProjectId() + " not found in JIRA; skipping this notification");
				continue;
			}

			logger.debug("Notification hub project " + notifHubProjectName + " matches mapping hub project: "
					+ mapping.getHubProject().getProjectName());
			logger.debug("The corresponding JIRA project is: " + freshBdsJiraProject.getProjectName());
			final JiraReadyNotification jiraReadyNotification = new JiraReadyNotification(
					freshBdsJiraProject.getProjectKey(), freshBdsJiraProject.getProjectName(), notifHubProjectName,
					notif, ruleUrls);
			jiraReadyNotifications.add(jiraReadyNotification);
		}
		return jiraReadyNotifications;
	}

	private boolean isRuleMatch(final List<String> linksOfRulesViolated)
			throws HubNotificationServiceException {

		logger.debug("Rules violated: " + linksOfRulesViolated);
		if (linksOfRulesToMonitor == null) {
			logger.debug("There is no list of rules to monitor, so we'll generate a ticket for the violation of any rule (as long as it's on a project being monitored)");
		} else {
			logger.debug("Rules we're monitoring: " + linksOfRulesToMonitor);
			if (!overlap(linksOfRulesToMonitor, linksOfRulesViolated)) {
				logger.debug("None of the violated rules are in the configured list of rules to monitor");
				return false;
			}
		}
		return true;
	}

	private String getNotifHubProjectUrl(final PolicyOverrideNotificationItem policyOverrideNotificationItem)
			throws HubNotificationServiceException {
		String notifHubProjectUrl;
		final String notifHubProjectVersionUrl = policyOverrideNotificationItem.getContent()
				.getProjectVersionLink();
		try {
			notifHubProjectUrl = hubNotificationService.getProjectUrlFromProjectReleaseUrl(notifHubProjectVersionUrl);
		} catch (final UnexpectedHubResponseException e) {
			throw new HubNotificationServiceException("Error getting project URL for: " + notifHubProjectVersionUrl
					+ ": " + e.getMessage(), e);
		}
		return notifHubProjectUrl;
	}

	private String getNotifHubProjectUrl(final RuleViolationNotificationItem ruleViolationNotificationItem)
			throws HubNotificationServiceException {
		String notifHubProjectUrl;
		final String notifHubProjectVersionUrl = ruleViolationNotificationItem.getContent()
				.getProjectVersionLink();
		try {
			notifHubProjectUrl = hubNotificationService.getProjectUrlFromProjectReleaseUrl(notifHubProjectVersionUrl);
		} catch (final UnexpectedHubResponseException e) {
			throw new HubNotificationServiceException("Error getting project URL for: " + notifHubProjectVersionUrl
					+ ": " + e.getMessage(), e);
		}
		return notifHubProjectUrl;
	}

	private boolean overlap(final List<String> list1, final List<String> list2) {
		for (final String s1 : list1) {
			if (list2.contains(s1)) {
				return true;
			}
		}
		return false;
	}

	private HubProjectMapping getMatchingMapping(final String notifHubProjectUrl) {
		logger.debug("JiraNotificationFilter.getMatchingMapping() Sifting through " + mappings.size()
				+ " mappings, looking for a match for this notification's Hub project: " + notifHubProjectUrl);
		for (final HubProjectMapping mapping : mappings) {
			logger.debug("Mapping: " + mapping);
			final String mappingHubProjectUrl = mapping.getHubProject().getProjectUrl();
			if (mappingHubProjectUrl.equals(notifHubProjectUrl)) {
				return mapping;
			}
		}
		return null;
	}
}
