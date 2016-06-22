package com.blackducksoftware.integration.jira.hub;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.hub.policy.api.PolicyRule;
import com.blackducksoftware.integration.hub.version.api.ReleaseItem;
import com.blackducksoftware.integration.jira.HubJiraLogger;
import com.blackducksoftware.integration.jira.config.HubProjectMapping;
import com.blackducksoftware.integration.jira.config.JiraProject;
import com.blackducksoftware.integration.jira.hub.model.component.BomComponentVersionPolicyStatus;
import com.blackducksoftware.integration.jira.hub.model.component.ComponentVersionStatus;
import com.blackducksoftware.integration.jira.hub.model.notification.NotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.RuleViolationNotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.VulnerabilityNotificationItem;

public class JiraNotificationFilter {
	private static final String ISSUE_TYPE_DESCRIPTION_RULE_VIOLATION = "Policy Violation";
	private static final String ISSUE_TYPE_DESCRIPTION_POLICY_OVERRIDE = "Policy Override";
	private static final String ISSUE_TYPE_DESCRIPTION_VULNERABILITY = "Vulnerability";
	private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));
	private static final String PROJECT_LINK = "project";
	private final HubNotificationService hubNotificationService;
	private final Set<HubProjectMapping> mappings;
	private final List<String> linksOfRulesToMonitor;
	private final TicketGeneratorInfo ticketGenInfo;

	public JiraNotificationFilter(final HubNotificationService hubNotificationService,
			final Set<HubProjectMapping> mappings, final List<String> linksOfRulesToMonitor,
			final TicketGeneratorInfo ticketGenInfo) {
		this.hubNotificationService = hubNotificationService;
		this.mappings = mappings;
		this.linksOfRulesToMonitor = linksOfRulesToMonitor;
		this.ticketGenInfo = ticketGenInfo;
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
			try {
				final RuleViolationNotificationItem ruleViolationNotif = (RuleViolationNotificationItem) notif;
				issueTypeDescription = ISSUE_TYPE_DESCRIPTION_RULE_VIOLATION;
				compVerStatuses = ruleViolationNotif.getContent().getComponentVersionStatuses();
				projectName = ruleViolationNotif.getContent().getProjectName();
				notifHubProjectReleaseItem = hubNotificationService
						.getProjectReleaseItemFromProjectReleaseUrl(ruleViolationNotif.getContent().getProjectVersionLink());
				projectVersionName = notifHubProjectReleaseItem.getVersionName();
			} catch (final HubNotificationServiceException e) {
				logger.error(e);
				return issues;
			}
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
		final JiraProject jiraProject;
		try {
			jiraProject = getProject(mappingJiraProject.getProjectId());
		} catch (final HubNotificationServiceException e) {
			logger.warn("Mapped project '" + mappingJiraProject.getProjectName() + "' with ID "
					+ mappingJiraProject.getProjectId() + " not found in JIRA; skipping this notification");
			return issues;
		}
		if (StringUtils.isNotBlank(jiraProject.getProjectError())) {
			logger.error(jiraProject.getProjectError());
			return issues;
		}

		logger.debug("JIRA Project: " + jiraProject);

		for (final ComponentVersionStatus compVerStatus : compVerStatuses) {

			final String componentVersionName = hubNotificationService
					.getComponentVersion(
							compVerStatus.getComponentVersionLink()).getVersionName();

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


					final String issueSummary = "Black Duck " + issueTypeDescription + " detected on Hub Project '"
							+ projectName
							+ "' / '" + projectVersionName + "', component '" + compVerStatus.getComponentName()
							+ "' / '" + componentVersionName + "' [Rule: '" + rule.getName() + "']";
					final String issueDescription = "The Black Duck Hub has detected a " + issueTypeDescription
							+ " on Hub Project '" + projectName + "', component '" + compVerStatus.getComponentName()
							+ "' / '" + componentVersionName + "'. The rule violated is: '"
							+ rule.getName() + "'";

					final IssueInputParameters issueInputParameters = ticketGenInfo.getIssueService()
							.newIssueInputParameters();
					issueInputParameters.setProjectId(mappingJiraProject.getProjectId())
					.setIssueTypeId(jiraProject.getIssueTypeId()).setSummary(issueSummary)
					.setReporterId(ticketGenInfo.getJiraUser().getName())
					.setDescription(issueDescription).setStatusId("2")
					.setPriorityId("2").setResolutionId("2")
					.setSecurityLevelId(10000L);

					issues.add(issueInputParameters);
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
			logger.warn("No rules-to-monitor provided, so no JIRA issues will be generated");
			return false;
		}
		final String fixedRuleUrl = fixRuleUrl(ruleViolated);
		if (!linksOfRulesToMonitor.contains(fixedRuleUrl)) {
			logger.debug("This rule is not one of the rules we are monitoring : " + fixedRuleUrl);
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
			logger.warn("No mappings provided");
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
		final List<String> projectLinks = version.getLinks(PROJECT_LINK);
		if (projectLinks.size() != 1) {
			throw new UnexpectedHubResponseException("The release " + version.getVersionName() + " has "
					+ projectLinks.size() + " " + PROJECT_LINK + " links; expected one");
		}
		final String projectLink = projectLinks.get(0);
		return projectLink;
	}

	public JiraProject getProject(final long jiraProjectId) throws HubNotificationServiceException {
		if (ticketGenInfo.getJiraProjectManager() == null) {
			throw new HubNotificationServiceException("The JIRA projectManager has not been set");
		}
		final com.atlassian.jira.project.Project atlassianJiraProject = ticketGenInfo.getJiraProjectManager()
				.getProjectObj(jiraProjectId);
		if (atlassianJiraProject == null) {
			throw new HubNotificationServiceException("Error: JIRA Project with ID " + jiraProjectId + " not found");
		}
		final String jiraProjectKey = atlassianJiraProject.getKey();
		final String jiraProjectName = atlassianJiraProject.getName();
		final JiraProject bdsJiraProject = new JiraProject();
		bdsJiraProject.setProjectId(jiraProjectId);
		bdsJiraProject.setProjectKey(jiraProjectKey);
		bdsJiraProject.setProjectName(jiraProjectName);

		if (atlassianJiraProject.getIssueTypes() == null || atlassianJiraProject.getIssueTypes().isEmpty()) {
			bdsJiraProject.setProjectError("The Jira project : " + bdsJiraProject.getProjectName()
			+ " does not have any issue types, we will not be able to create tickets for this project.");
		} else {
			boolean projectHasIssueType = false;
			if (atlassianJiraProject.getIssueTypes() != null && !atlassianJiraProject.getIssueTypes().isEmpty()) {
				for (final IssueType issueType : atlassianJiraProject.getIssueTypes()) {
					if (issueType.getName().equals(ticketGenInfo.getJiraIssueTypeName())) {
						bdsJiraProject.setIssueTypeId(issueType.getId());
						projectHasIssueType = true;
					}
				}
			}
			if (!projectHasIssueType) {
				bdsJiraProject.setProjectError(
						"The Jira project is missing the " + ticketGenInfo.getJiraIssueTypeName() + " issue type.");
			}
		}
		return bdsJiraProject;
	}
}
