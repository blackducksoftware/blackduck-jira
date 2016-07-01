package com.blackducksoftware.integration.jira.hub;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.blackducksoftware.integration.hub.exception.MissingUUIDException;
import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.hub.policy.api.PolicyRule;
import com.blackducksoftware.integration.hub.version.api.ReleaseItem;
import com.blackducksoftware.integration.jira.HubJiraLogger;
import com.blackducksoftware.integration.jira.config.HubProjectMapping;
import com.blackducksoftware.integration.jira.config.JiraProject;
import com.blackducksoftware.integration.jira.hub.model.component.BomComponentVersionPolicyStatus;
import com.blackducksoftware.integration.jira.hub.model.component.ComponentVersionStatus;
import com.blackducksoftware.integration.jira.hub.model.notification.NotificationType;

public class PolicyNotificationFilter extends NotificationFilter {


	private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));
	private final List<String> linksOfRulesToMonitor;
	private final HubNotificationService hubNotificationService;


	public PolicyNotificationFilter(final Set<HubProjectMapping> mappings,
			final TicketGeneratorInfo ticketGenInfo, final List<String> linksOfRulesToMonitor,
			final HubNotificationService hubNotificationService) {
		super(mappings, ticketGenInfo);
		this.linksOfRulesToMonitor = linksOfRulesToMonitor;
		this.hubNotificationService = hubNotificationService;
	}

	@Override
	public FilteredNotificationResults handleNotificationPerJiraProject(final NotificationType notificationType,
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

			if (notificationType == NotificationType.POLICY_VIOLATION
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

				final FilteredNotificationResult result = new FilteredNotificationResult(projectName,
						projectVersionName, compVerStatus.getComponentName(), componentVersionName,
						rule, versionId, componentId, componentVersionId, ruleId,
						getTicketGenInfo().getJiraUser(), jiraProject.getIssueTypeId(),
						jiraProject.getProjectId(), jiraProject.getProjectName(), notificationType);

				if (result.getNotificationType() == NotificationType.POLICY_VIOLATION) {
					notifResults.addPolicyViolationResult(result);
				} else if (result.getNotificationType() == NotificationType.POLICY_OVERRIDE) {
					notifResults.addPolicyViolationOverrideResult(result);
				}
			}
		}
		return notifResults;
	}

	private List<String> getMonitoredRules(final List<String> rulesViolated) throws HubNotificationServiceException {
		if (rulesViolated == null || rulesViolated.isEmpty()) {
			logger.warn("No violated Rules provided.");
			return null;
		}
		final List<String> matchingRules = new ArrayList<>();
		for (final String ruleViolated : rulesViolated) {
			final String fixedRuleUrl = fixRuleUrl(ruleViolated);
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
}
