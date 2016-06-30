package com.blackducksoftware.integration.jira.hub;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.atlassian.jira.issue.issuetype.IssueType;
import com.blackducksoftware.integration.hub.exception.MissingUUIDException;
import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.hub.policy.api.PolicyRule;
import com.blackducksoftware.integration.hub.version.api.ReleaseItem;
import com.blackducksoftware.integration.jira.HubJiraLogger;
import com.blackducksoftware.integration.jira.config.HubProjectMapping;
import com.blackducksoftware.integration.jira.config.JiraProject;
import com.blackducksoftware.integration.jira.hub.model.component.BomComponentVersionPolicyStatus;
import com.blackducksoftware.integration.jira.hub.model.component.ComponentVersionStatus;
import com.blackducksoftware.integration.jira.hub.model.notification.NotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.NotificationType;
import com.blackducksoftware.integration.jira.hub.model.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.RuleViolationNotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.VulnerabilityNotificationItem;

public class JiraNotificationFilter {
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

	public FilteredNotificationResults extractJiraReadyNotifications(final List<NotificationItem> notifications)
			throws HubNotificationServiceException {
		final FilteredNotificationResults allResults = new FilteredNotificationResults();

		logger.debug("JiraNotificationFilter.extractJiraReadyNotifications(): Sifting through " + notifications.size()
		+ " notifications");
		for (final NotificationItem notif : notifications) {
			logger.debug("Notification: " + notif);

			FilteredNotificationResults notifResults;
			try {
				notifResults = convertNotificationToIssues(notif);
			} catch (final UnexpectedHubResponseException e) {
				throw new HubNotificationServiceException("Error converting notifications to issues", e);
			}
			allResults.addAllResults(notifResults);
		}
		return allResults;
	}

	private FilteredNotificationResults convertNotificationToIssues(final NotificationItem notif)
			throws HubNotificationServiceException, UnexpectedHubResponseException {
		final FilteredNotificationResults notifResults = new FilteredNotificationResults();
		NotificationType notificationType;
		String projectName;
		String projectVersionName;
		List<ComponentVersionStatus> compVerStatuses;
		final ReleaseItem notifHubProjectReleaseItem;
		if (notif instanceof RuleViolationNotificationItem) {
			try {
				final RuleViolationNotificationItem ruleViolationNotif = (RuleViolationNotificationItem) notif;
				notificationType = NotificationType.POLICY_VIOLATION;
				compVerStatuses = ruleViolationNotif.getContent().getComponentVersionStatuses();
				projectName = ruleViolationNotif.getContent().getProjectName();
				notifHubProjectReleaseItem = hubNotificationService
						.getProjectReleaseItemFromProjectReleaseUrl(ruleViolationNotif.getContent().getProjectVersionLink());
				projectVersionName = notifHubProjectReleaseItem.getVersionName();
			} catch (final HubNotificationServiceException e) {
				logger.error(e);
				return notifResults;
			}
		} else if (notif instanceof PolicyOverrideNotificationItem) {
			try {
				final PolicyOverrideNotificationItem ruleViolationNotif = (PolicyOverrideNotificationItem) notif;
				notificationType = NotificationType.POLICY_OVERRIDE;

				compVerStatuses = new ArrayList<>();
				final ComponentVersionStatus componentStatus = new ComponentVersionStatus();
				componentStatus.setBomComponentVersionPolicyStatusLink(
						ruleViolationNotif.getContent().getBomComponentVersionPolicyStatusLink());
				componentStatus.setComponentName(ruleViolationNotif.getContent().getComponentName());
				componentStatus.setComponentVersionLink(ruleViolationNotif.getContent().getComponentVersionLink());

				compVerStatuses.add(componentStatus);

				projectName = ruleViolationNotif.getContent().getProjectName();

				notifHubProjectReleaseItem = hubNotificationService.getProjectReleaseItemFromProjectReleaseUrl(
						ruleViolationNotif.getContent().getProjectVersionLink());
				projectVersionName = notifHubProjectReleaseItem.getVersionName();
			} catch (final HubNotificationServiceException e) {
				logger.error(e);
				return notifResults;
			}
		} else if (notif instanceof VulnerabilityNotificationItem) {
			notificationType = NotificationType.VULNERABILITY;
			return notifResults; // TODO
		} else {
			throw new HubNotificationServiceException("Notification type unknown for notification: " + notif);
		}

		final String projectUrl = getProjectLink(notifHubProjectReleaseItem);
		final List<HubProjectMapping> mappings = getMatchingMappings(projectUrl);
		if (mappings.isEmpty()) {
			logger.debug("No configuration project mapping matching this notification found; skipping this notification");
			return notifResults;
		}
		for (final HubProjectMapping mapping : mappings) {
			final JiraProject mappingJiraProject = mapping.getJiraProject();
			final JiraProject jiraProject;
			try {
				jiraProject = getProject(mappingJiraProject.getProjectId());
			} catch (final HubNotificationServiceException e) {
				logger.warn("Mapped project '" + mappingJiraProject.getProjectName() + "' with ID "
						+ mappingJiraProject.getProjectId() + " not found in JIRA; skipping this notification");
				continue;
			}
			if (StringUtils.isNotBlank(jiraProject.getProjectError())) {
				logger.error(jiraProject.getProjectError());
				continue;
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

				for (final String ruleUrl : ruleUrls) {
					if (isRuleMatch(ruleUrl)) {
						final PolicyRule rule = hubNotificationService.getPolicyRule(ruleUrl);
						logger.debug("Rule : " + rule);

						UUID projectId;
						UUID versionId;
						UUID componentId;
						UUID componentVersionId;
						UUID ruleId;
						try {
							projectId = notifHubProjectReleaseItem.getProjectId();

							versionId = notifHubProjectReleaseItem.getVersionId();

							componentId = compVerStatus.getComponentId();

							componentVersionId = compVerStatus.getComponentVersionId();

							ruleId = rule.getPolicyRuleId();
						} catch (final MissingUUIDException e) {
							logger.error(e);
							continue;
						}
						final FilteredNotificationResult result = new FilteredNotificationResult(projectName,
								projectVersionName, compVerStatus.getComponentName(), componentVersionName,
								rule.getName(), projectId, versionId, componentId, componentVersionId, ruleId,
								ticketGenInfo.getJiraUser(), jiraProject.getIssueTypeId(),
								jiraProject.getProjectId(), jiraProject.getProjectName(), notificationType);

						if (result.getNotificationType() == NotificationType.POLICY_VIOLATION) {
							notifResults.addPolicyViolationResult(result);
						} else if (result.getNotificationType() == NotificationType.POLICY_OVERRIDE) {
							notifResults.addPolicyViolationOverrideResult(result);
						} else if (result.getNotificationType() == NotificationType.VULNERABILITY) {
							notifResults.addVulnerabilityResult(result);
						} else {

						}
					}
				}
			}
		}
		return notifResults;
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

	private List<HubProjectMapping> getMatchingMappings(final String notifHubProjectUrl) {
		if ((mappings == null) || (mappings.size() == 0)) {
			logger.warn("No mappings provided");
			return null;
		}
		final List<HubProjectMapping> matchingMappings = new ArrayList<HubProjectMapping>();
		logger.debug("JiraNotificationFilter.getMatchingMapping() Sifting through " + mappings.size()
		+ " mappings, looking for a match for this notification's Hub project: " + notifHubProjectUrl);
		for (final HubProjectMapping mapping : mappings) {
			final String mappingHubProjectUrl = mapping.getHubProject().getProjectUrl();
			if (mappingHubProjectUrl.equals(notifHubProjectUrl)) {
				logger.debug("Mapping: " + mapping);
				matchingMappings.add(mapping);
			}
		}
		return matchingMappings;
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
