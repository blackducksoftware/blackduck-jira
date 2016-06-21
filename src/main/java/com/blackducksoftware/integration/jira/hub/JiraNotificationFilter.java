package com.blackducksoftware.integration.jira.hub;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.issue.IssueInputParameters;
import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.hub.policy.api.PolicyRule;
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
	private static final String ISSUE_TYPE_DESCRIPTION_RULE_VIOLATION = "Rule Violation";
	private static final String ISSUE_TYPE_DESCRIPTION_POLICY_OVERRIDE = "Policy Override";
	private static final String ISSUE_TYPE_DESCRIPTION_VULNERABILITY = "Vulnerability";
	private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));
	private static final String PROJECT_LINK = "project";
	private final HubNotificationService hubNotificationService;
	private final Set<HubProjectMapping> mappings;
	private final List<String> linksOfRulesToMonitor;
	private final JiraService jiraService;
	private final IssueService issueService;

	public JiraNotificationFilter(final HubNotificationService hubNotificationService, final JiraService jiraService,
			final Set<HubProjectMapping> mappings, final List<String> linksOfRulesToMonitor,
			final IssueService issueService) {
		this.hubNotificationService = hubNotificationService;
		this.jiraService = jiraService;
		this.mappings = mappings;
		this.linksOfRulesToMonitor = linksOfRulesToMonitor;
		this.issueService = issueService;
	}

	public List<IssueInputParameters> extractJiraReadyNotifications(final List<NotificationItem> notifications)
			throws HubNotificationServiceException {
		final List<IssueInputParameters> allIssues = new ArrayList<>();

		logger.debug("JiraNotificationFilter.extractJiraReadyNotifications(): Sifting through " + notifications.size()
		+ " notifications");
		for (final NotificationItem notif : notifications) {
			logger.debug("Notification: " + notif);

			List<IssueInputParameters> notifIssues;
			try {
				notifIssues = convertNotificationToIssues(notif);
			} catch (final UnexpectedHubResponseException e) {
				throw new HubNotificationServiceException("Error converting notifications to issues", e);
			}
			allIssues.addAll(notifIssues);
		}
		return allIssues;
	}

	private List<IssueInputParameters> convertNotificationToIssues(final NotificationItem notif)
			throws HubNotificationServiceException, UnexpectedHubResponseException {
		final List<IssueInputParameters> issues = new ArrayList<>();
		String issueTypeDescription;
		String projectName;
		String projectVersionName;
		List<ComponentVersionStatus> compVerStatuses;
		final ReleaseItem notifHubProjectReleaseItem;
		if (notif instanceof RuleViolationNotificationItem) {
			final RuleViolationNotificationItem ruleViolationNotif = (RuleViolationNotificationItem) notif;
			issueTypeDescription = ISSUE_TYPE_DESCRIPTION_RULE_VIOLATION;
			compVerStatuses = ruleViolationNotif.getContent().getComponentVersionStatuses();
			projectName = ruleViolationNotif.getContent().getProjectName();
			notifHubProjectReleaseItem = hubNotificationService
					.getProjectReleaseItemFromProjectReleaseUrl(ruleViolationNotif.getContent().getProjectVersionLink());
			projectVersionName = notifHubProjectReleaseItem.getVersionName();
		} else if (notif instanceof PolicyOverrideNotificationItem) {
			issueTypeDescription = ISSUE_TYPE_DESCRIPTION_POLICY_OVERRIDE;
			return issues; // TODO
		} else if (notif instanceof VulnerabilityNotificationItem) {
			issueTypeDescription = ISSUE_TYPE_DESCRIPTION_VULNERABILITY;
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
					final PolicyRule rule = hubNotificationService.getPolicyRule(ruleUrl);
					logger.debug("Rule violated: " + rule);

					final IssueInputParameters issueInputParameters = issueService.newIssueInputParameters();
					issueInputParameters.setProjectId(12345L)
					.setIssueTypeId("2");
					.setSummary("This is a summary");
					.setReporterId("joeuser");
					.setAssigneeId("otheruser");
					.setDescription("I am a description");
					.setEnvironment("I am an environment");
					.setStatusId("2");
					.setPriorityId("2");
					.setResolutionId("2");
					.setSecurityLevelId(10000L);
					.setFixVersionIds(10000L, 10001L);

					final IssueInputParameters issue = new IssueInputParameters(issueTypeDescription, IssueLevel.COMPONENT, project, component,
							ruleUrl, rule.getName(),
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
		if ((linksOfRulesToMonitor == null) || (linksOfRulesToMonitor.size() == 0)) {
			logger.debug("No rules-to-monitor provided, so no JIRA issues will be generated");
			return false;
		}
		final String fixedRuleUrl = fixRuleUrl(ruleViolated);
		if (!linksOfRulesToMonitor.contains(fixedRuleUrl)) {
			logger.debug("This rule is not one of the rules we are monitoring");
			return false;
		}

		return true;
	}

	/**
	 * In Hub versions prior to 3.2, the rule URLs contained in notifications
	 * are internal. To match the configured rule URLs, the "internal" segment
	 * of the URL from the notification must be removed. This is the workaround
	 * recommended by Rob P. In Hub 3.2 on, these URLs will exclude the
	 * "internal" segment.
	 *
	 * @param origRuleUrl
	 * @return
	 */
	private String fixRuleUrl(final String origRuleUrl) {
		String fixedRuleUrl = origRuleUrl;
		if (origRuleUrl.contains("/internal/")) {
			fixedRuleUrl = origRuleUrl.replace("/internal/", "/");
			logger.debug("Adjusted rule URL from " + origRuleUrl + " to " + fixedRuleUrl);
		}
		return fixedRuleUrl;
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
