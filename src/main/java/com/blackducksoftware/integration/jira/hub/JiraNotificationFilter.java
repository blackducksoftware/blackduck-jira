package com.blackducksoftware.integration.jira.hub;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.hub.version.api.ReleaseItem;
import com.blackducksoftware.integration.jira.HubJiraLogger;
import com.blackducksoftware.integration.jira.config.HubProjectMapping;
import com.blackducksoftware.integration.jira.config.JiraProject;
import com.blackducksoftware.integration.jira.hub.model.NameVersion;
import com.blackducksoftware.integration.jira.hub.model.component.BomComponentVersionPolicyStatus;
import com.blackducksoftware.integration.jira.hub.model.component.ComponentVersionStatus;
import com.blackducksoftware.integration.jira.hub.model.notification.NotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.RuleViolationNotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.VulnerabilityNotificationItem;
import com.blackducksoftware.integration.jira.issue.Issue;
import com.blackducksoftware.integration.jira.issue.IssueLevel;
import com.blackducksoftware.integration.jira.service.JiraService;
import com.blackducksoftware.integration.jira.service.JiraServiceException;

public class JiraNotificationFilter {
	private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));
	private static final String PROJECT_LINK = "project";
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

	public List<Issue> extractJiraReadyNotifications(final List<NotificationItem> notifications)
			throws HubNotificationServiceException {
		final List<Issue> allIssues = new ArrayList<>();

		logger.debug("JiraNotificationFilter.extractJiraReadyNotifications(): Sifting through " + notifications.size()
				+ " notifications");
		for (final NotificationItem notif : notifications) {
			logger.debug("Notification: " + notif);

			List<Issue> notifIssues;
			try {
				notifIssues = convertNotificationToIssues(notif);
			} catch (final UnexpectedHubResponseException e) {
				throw new HubNotificationServiceException("Error converting notifications to issues", e);
			}

			// String notifHubProjectName = "<unknown>";
			// String notifHubProjectVersionName = "<unknown>";
			// String notifHubProjectUrl = "<unknown>";
			// List<String> ruleUrls = null;
			// try {
			// if (notif instanceof VulnerabilityNotificationItem) {
			// logger.debug("This is a vulnerability notification; skipping it.");
			// continue;
			// } else if (notif instanceof RuleViolationNotificationItem) {
			// final RuleViolationNotificationItem ruleViolationNotificationItem
			// = (RuleViolationNotificationItem) notif;
			// notifHubProjectName =
			// ruleViolationNotificationItem.getContent().getProjectName();
			// final ReleaseItem notifHubProjectReleaseItem =
			// getNotifHubProjectReleaseItem(ruleViolationNotificationItem.getContent().getProjectVersionLink());
			// notifHubProjectUrl = getProjectLink(notifHubProjectReleaseItem);
			// notifHubProjectVersionName =
			// notifHubProjectReleaseItem.getVersionName();
			// ruleUrls =
			// hubNotificationService.getLinksOfRulesViolated(ruleViolationNotificationItem);
			// if (!isRuleMatch(ruleUrls)) {
			// continue;
			// }
			// } else if (notif instanceof PolicyOverrideNotificationItem) {
			// final PolicyOverrideNotificationItem
			// policyOverrideNotificationItem = (PolicyOverrideNotificationItem)
			// notif;
			// notifHubProjectName =
			// policyOverrideNotificationItem.getContent().getProjectName();
			// final ReleaseItem notifHubProjectReleaseItem =
			// getNotifHubProjectReleaseItem(policyOverrideNotificationItem.getContent().getProjectVersionLink());
			// notifHubProjectUrl = getProjectLink(notifHubProjectReleaseItem);
			// notifHubProjectVersionName =
			// notifHubProjectReleaseItem.getVersionName();
			// }
			// } catch (final HubNotificationServiceException e) {
			// logger.error("Error extracting details from the Hub notification: "
			// + notif + ": " + e.getMessage());
			// continue;
			// }
			//
			// final HubProjectMapping mapping =
			// getMatchingMapping(notifHubProjectUrl);
			// if (mapping == null) {
			// logger.debug("No configuration project mapping matching this notification found; skipping this notification");
			// continue;
			// }
			//
			// final JiraProject mappingJiraProject = mapping.getJiraProject();
			// JiraProject freshBdsJiraProject;
			// try {
			// freshBdsJiraProject =
			// jiraService.getProject(mappingJiraProject.getProjectId());
			// } catch (final JiraServiceException e) {
			// logger.warn("Mapped project '" +
			// mappingJiraProject.getProjectName() + "' with ID "
			// + mappingJiraProject.getProjectId() +
			// " not found in JIRA; skipping this notification");
			// continue;
			// }
			//
			// logger.debug("Notification hub project " + notifHubProjectName +
			// " matches mapping hub project: "
			// + mapping.getHubProject().getProjectName());
			// logger.debug("The corresponding JIRA project is: " +
			// freshBdsJiraProject.getProjectName());
			//
			// final IssueLevel level = IssueLevel.COMPONENT;
			// final NameVersion project = new NameVersion(notifHubProjectName,
			// notifHubProjectVersionName);
			// final NameVersion component;
			// final List<String> ruleUrls
			// Issue jiraIssue = new Issue(IssueLevel level, final NameVersion
			// project, final NameVersion component,
			// final List<String> ruleUrls);

			// final JiraReadyNotification jiraReadyNotification = new
			// JiraReadyNotification(
			// freshBdsJiraProject.getProjectKey(),
			// freshBdsJiraProject.getProjectName(), notifHubProjectName,
			// notif, issues);
			// jiraReadyNotifications.add(jiraReadyNotification);
			allIssues.addAll(notifIssues);
		}
		return allIssues;
	}

	// Fix exceptions... this is throwing the wrong exception type?
	private List<Issue> convertNotificationToIssues(final NotificationItem notif)
			throws HubNotificationServiceException, UnexpectedHubResponseException {
		final List<Issue> issues = new ArrayList<>();
		String projectName;
		String projectVersionName;
		List<ComponentVersionStatus> compVerStatuses;
		final ReleaseItem notifHubProjectReleaseItem;
		if (notif instanceof RuleViolationNotificationItem) {
			final RuleViolationNotificationItem ruleViolationNotif = (RuleViolationNotificationItem) notif;
			compVerStatuses = ruleViolationNotif.getContent().getComponentVersionStatuses();
			projectName = ruleViolationNotif.getContent().getProjectName();
			notifHubProjectReleaseItem = hubNotificationService
					.getProjectReleaseItemFromProjectReleaseUrl(ruleViolationNotif.getContent().getProjectVersionLink());
			projectVersionName = notifHubProjectReleaseItem.getVersionName();
		} else if (notif instanceof PolicyOverrideNotificationItem) {
			return issues; // TODO
		} else if (notif instanceof VulnerabilityNotificationItem) {
			return issues; // TODO
		} else {
			throw new HubNotificationServiceException("Notification type unknown for notification: " + notif);
		}

		final String projectUrl = getProjectLink(notifHubProjectReleaseItem);
		final HubProjectMapping mapping = getMatchingMapping(projectUrl);
		if (mapping == null) {
			logger.debug("No configuration project mapping matching this notification found; skipping this notification");
			return issues;
		}

		final JiraProject mappingJiraProject = mapping.getJiraProject();
		JiraProject freshBdsJiraProject;
		try {
			freshBdsJiraProject = jiraService.getProject(mappingJiraProject.getProjectId());
		} catch (final JiraServiceException e) {
			logger.warn("Mapped project '" + mappingJiraProject.getProjectName() + "' with ID "
					+ mappingJiraProject.getProjectId() + " not found in JIRA; skipping this notification");
			return issues;
		}
		logger.debug("JIRA Project: " + freshBdsJiraProject);

		for (final ComponentVersionStatus compVerStatus : compVerStatuses) {

			final NameVersion project = new NameVersion(projectName, projectVersionName);
			String componentVersionName;

			componentVersionName = hubNotificationService.getComponentVersion(
					compVerStatus.getComponentVersionLink()).getVersionName();

			final NameVersion component = new NameVersion(compVerStatus.getComponentName(), componentVersionName);
			final String policyStatusUrl = compVerStatus.getBomComponentVersionPolicyStatusLink();
			final BomComponentVersionPolicyStatus bomComponentVersionPolicyStatus = hubNotificationService
					.getPolicyStatus(policyStatusUrl);

			logger.debug("BomComponentVersionPolicyStatus: " + bomComponentVersionPolicyStatus);
			final List<String> ruleUrls = bomComponentVersionPolicyStatus.getLinks("policy-rule");
			logger.debug("Rules violated: " + ruleUrls);


			for (final String ruleUrl : ruleUrls) {
				if (isRuleMatch(ruleUrl)) {
					final Issue issue = new Issue(IssueLevel.COMPONENT, project, component, ruleUrl,
							freshBdsJiraProject.getProjectKey());
					issues.add(issue);
				}
			}
		}
		return issues;
	}

	private boolean isRuleMatch(final String ruleViolated)
			throws HubNotificationServiceException {

		logger.debug("Rule violated: " + ruleViolated);
		logger.debug("Rules we're monitoring: " + linksOfRulesToMonitor);
		if (linksOfRulesToMonitor == null) {
			logger.debug("No rules-to-monitor provided, so we're monitoring all rules");
			return true;
		}
		if (!linksOfRulesToMonitor.contains(ruleViolated)) {
			logger.debug("This rule is not one of the rules we are monitoring");
			return false;
		}

		return true;
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
		if ((mappings == null) || (mappings.size() == 0)) {
			logger.debug("No mappings provided");
			return null;
		}

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

	private String getProjectLink(final ReleaseItem version) throws UnexpectedHubResponseException {
		final List<String> versionLinks = version.getLinks(PROJECT_LINK);
		if (versionLinks.size() != 1) {
			throw new UnexpectedHubResponseException("The release " + version.getVersionName() + " has "
					+ versionLinks.size() + " " + PROJECT_LINK + " links; expected one");
		}
		final String versionLink = versionLinks.get(0);
		return versionLink;
	}
}
