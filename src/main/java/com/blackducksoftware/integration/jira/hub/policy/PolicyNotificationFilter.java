package com.blackducksoftware.integration.jira.hub.policy;

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
import com.blackducksoftware.integration.jira.hub.FilteredNotificationResult;
import com.blackducksoftware.integration.jira.hub.FilteredNotificationResultRule;
import com.blackducksoftware.integration.jira.hub.FilteredNotificationResults;
import com.blackducksoftware.integration.jira.hub.HubNotificationService;
import com.blackducksoftware.integration.jira.hub.HubNotificationServiceException;
import com.blackducksoftware.integration.jira.hub.TicketGeneratorInfo;
import com.blackducksoftware.integration.jira.hub.model.component.BomComponentVersionPolicyStatus;
import com.blackducksoftware.integration.jira.hub.model.component.ComponentVersionStatus;
import com.blackducksoftware.integration.jira.issue.EventType;

public class PolicyNotificationFilter {
	private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));
	public static final String PROJECT_LINK = "project";
	private final Set<HubProjectMapping> mappings;
	private final TicketGeneratorInfo ticketGenInfo;
	private final List<String> linksOfRulesToMonitor;
	private final HubNotificationService hubNotificationService;


	public PolicyNotificationFilter(final Set<HubProjectMapping> mappings,
			final TicketGeneratorInfo ticketGenInfo, final List<String> linksOfRulesToMonitor,
			final HubNotificationService hubNotificationService) {
		this.mappings = mappings;
		this.ticketGenInfo = ticketGenInfo;
		this.linksOfRulesToMonitor = linksOfRulesToMonitor;
		this.hubNotificationService = hubNotificationService;
	}

	public FilteredNotificationResults handleNotification(final EventType eventType,
			final String projectName, final String projectVersionName,
			final List<ComponentVersionStatus> compVerStatuses, final ReleaseItem notifHubProjectReleaseItem)
					throws UnexpectedHubResponseException, HubNotificationServiceException {
		final FilteredNotificationResults notifResults = new FilteredNotificationResults();

		final String projectUrl = getProjectLink(notifHubProjectReleaseItem);

		// TODO use HubProjectMappings instead
		final List<HubProjectMapping> mappings = getMatchingMappings(projectUrl);
		if (mappings == null || mappings.isEmpty()) {
			logger.debug("No configured project mapping matching this notification found; skipping this notification");
			return null;
		}
		for (final HubProjectMapping mapping : mappings) {
			final JiraProject mappingJiraProject = mapping.getJiraProject();
			final JiraProject jiraProject;
			try {
				jiraProject = getJiraProject(mappingJiraProject.getProjectId());
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

			final FilteredNotificationResults oneProjectsResults = handleNotificationPerJiraProject(eventType,
					projectName, projectVersionName, compVerStatuses, notifHubProjectReleaseItem, jiraProject);
			if (oneProjectsResults != null) {
				notifResults.addAllResults(oneProjectsResults);
			}
		}
		return notifResults;
	}

	private FilteredNotificationResults handleNotificationPerJiraProject(final EventType eventType,
			final String projectName, final String projectVersionName,
			final List<ComponentVersionStatus> compVerStatuses, final ReleaseItem notifHubProjectReleaseItem,
			final JiraProject jiraProject)
					throws UnexpectedHubResponseException, HubNotificationServiceException {
		final FilteredNotificationResults notifResults = new FilteredNotificationResults();
		if ((linksOfRulesToMonitor == null) || (linksOfRulesToMonitor.size() == 0)) {
			logger.warn("No rules-to-monitor provided, skipping policy notifications.");
			return null;
		}
		for (final ComponentVersionStatus compVerStatus : compVerStatuses) {
			if (eventType == EventType.POLICY_VIOLATION
					&& compVerStatus.getComponentVersionLink() == null) {
				// FIXME see HUB-7571
				logger.error(
						"Cannot create tickets for component level violations at this time. This will be fixed in future releases.");
				continue;
			}
			final String componentVersionName = hubNotificationService
					.getComponentVersion(
							compVerStatus.getComponentVersionLink()).getVersionName();

			final String policyStatusUrl = compVerStatus.getBomComponentVersionPolicyStatusLink();

			final BomComponentVersionPolicyStatus bomComponentVersionPolicyStatus = hubNotificationService
					.getPolicyStatus(policyStatusUrl);

			logger.debug("BomComponentVersionPolicyStatus: " + bomComponentVersionPolicyStatus);
			final List<String> monitoredUrls = getMonitoredRules(bomComponentVersionPolicyStatus
					.getLinks(BomComponentVersionPolicyStatus.POLICY_RULE_URL));
			if(monitoredUrls == null || monitoredUrls.isEmpty()){
				logger.warn(
						"No configured policy violations matching this notification found; skipping this notification");
				continue;
			}

			for (final String ruleUrl : monitoredUrls) {
				final PolicyRule rule = hubNotificationService.getPolicyRule(ruleUrl);
				logger.debug("Rule : " + rule);

				if (rule.getExpression() != null && rule.getExpression().hasOnlyProjectLevelConditions()) {
					logger.warn("Skipping this Violation since it is a Project only violation.");
					continue;
				}

				UUID versionId;
				UUID componentId;
				UUID componentVersionId;
				UUID ruleId;
				try {
					versionId = notifHubProjectReleaseItem.getVersionId();

					componentId = compVerStatus.getComponentId();

					componentVersionId = compVerStatus.getComponentVersionId();

					ruleId = rule.getPolicyRuleId();
				} catch (final MissingUUIDException e) {
					logger.error(e);
					continue;
				}

				final FilteredNotificationResult result = new FilteredNotificationResultRule(projectName,
						projectVersionName, compVerStatus.getComponentName(), componentVersionName, versionId,
						componentId, componentVersionId,
						getTicketGenInfo().getJiraUser().getName(),
						jiraProject.getIssueTypeId(),
						jiraProject.getProjectId(), jiraProject.getProjectName(),
						eventType, rule, ruleId);

				if (result.getEventType() == EventType.POLICY_VIOLATION) {
					notifResults.addPolicyViolationResult(result);
				} else if (result.getEventType() == EventType.POLICY_OVERRIDE) {
					notifResults.addPolicyViolationOverrideResult(result);
				}
			}
		}
		return notifResults;
	}

	private List<String> getMonitoredRules(final List<String> rulesViolated) throws HubNotificationServiceException {
		logger.debug("getMonitoredRules(): Configured rules to monitor: " + linksOfRulesToMonitor);
		if (rulesViolated == null || rulesViolated.isEmpty()) {
			logger.warn("No violated Rules provided.");
			return null;
		}
		final List<String> matchingRules = new ArrayList<>();
		for (final String ruleViolated : rulesViolated) {
			logger.debug("Violated rule (original): " + ruleViolated);
			final String fixedRuleUrl = fixRuleUrl(ruleViolated);
			logger.debug("Checking configured rules to monitor for fixed url: " + fixedRuleUrl);
			if (linksOfRulesToMonitor.contains(fixedRuleUrl)) {
				logger.debug("Monitored Rule : " + fixedRuleUrl);
				matchingRules.add(fixedRuleUrl);
			}
		}
		return matchingRules;
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

	private String getProjectLink(final ReleaseItem version) throws UnexpectedHubResponseException {
		final List<String> projectLinks = version.getLinks(PROJECT_LINK);
		if (projectLinks.size() != 1) {
			throw new UnexpectedHubResponseException("The release " + version.getVersionName() + " has "
					+ projectLinks.size() + " " + PROJECT_LINK + " links; expected one");
		}
		final String projectLink = projectLinks.get(0);
		return projectLink;
	}

	private List<HubProjectMapping> getMatchingMappings(final String notifHubProjectUrl) {
		if ((mappings == null) || (mappings.size() == 0)) {
			logger.warn("No mappings provided");
			return null;
		}
		final List<HubProjectMapping> matchingMappings = new ArrayList<HubProjectMapping>();
		logger.debug("NotificationFilter.getMatchingMapping() Sifting through " + mappings.size()
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

	private TicketGeneratorInfo getTicketGenInfo() {
		return ticketGenInfo;
	}

	private JiraProject getJiraProject(final long jiraProjectId) throws HubNotificationServiceException {
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
				bdsJiraProject.setProjectError("The Jira project is missing the "
						+ ticketGenInfo.getJiraIssueTypeName() + " issue type.");
			}
		}
		return bdsJiraProject;
	}
}
