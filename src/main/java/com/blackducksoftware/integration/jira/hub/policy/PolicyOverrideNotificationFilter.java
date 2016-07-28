package com.blackducksoftware.integration.jira.hub.policy;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.hub.version.api.ReleaseItem;
import com.blackducksoftware.integration.jira.HubJiraLogger;
import com.blackducksoftware.integration.jira.config.HubProjectMapping;
import com.blackducksoftware.integration.jira.hub.HubEvents;
import com.blackducksoftware.integration.jira.hub.HubNotificationService;
import com.blackducksoftware.integration.jira.hub.HubNotificationServiceException;
import com.blackducksoftware.integration.jira.hub.TicketGeneratorInfo;
import com.blackducksoftware.integration.jira.hub.model.component.ComponentVersionStatus;
import com.blackducksoftware.integration.jira.hub.model.notification.NotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.jira.issue.HubEventType;

public class PolicyOverrideNotificationFilter extends PolicyNotificationFilter {
	private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));

	public PolicyOverrideNotificationFilter(final Set<HubProjectMapping> mappings, final TicketGeneratorInfo ticketGenInfo,
			final List<String> linksOfRulesToMonitor, final HubNotificationService hubNotificationService) {
		super(mappings, ticketGenInfo, linksOfRulesToMonitor, hubNotificationService);
	}

	@Override
	public HubEvents generateEvents(final NotificationItem notif) {
		HubEvents events;

		HubEventType eventType;
		String projectName;
		String projectVersionName;
		List<ComponentVersionStatus> compVerStatuses;
		final ReleaseItem notifHubProjectReleaseItem;

		try {
			final PolicyOverrideNotificationItem ruleViolationNotif = (PolicyOverrideNotificationItem) notif;
			eventType = HubEventType.POLICY_OVERRIDE;

			compVerStatuses = new ArrayList<>();
			final ComponentVersionStatus componentStatus = new ComponentVersionStatus();
			componentStatus.setBomComponentVersionPolicyStatusLink(ruleViolationNotif.getContent()
					.getBomComponentVersionPolicyStatusLink());
			componentStatus.setComponentName(ruleViolationNotif.getContent().getComponentName());
			componentStatus.setComponentVersionLink(ruleViolationNotif.getContent().getComponentVersionLink());

			compVerStatuses.add(componentStatus);

			projectName = ruleViolationNotif.getContent().getProjectName();

			notifHubProjectReleaseItem = getHubNotificationService().getProjectReleaseItemFromProjectReleaseUrl(
					ruleViolationNotif.getContent().getProjectVersionLink());
			projectVersionName = notifHubProjectReleaseItem.getVersionName();
		} catch (final HubNotificationServiceException | UnexpectedHubResponseException e) {
			logger.error(e);
			return null;
		}

		try {
			events = handleNotification(eventType, projectName, projectVersionName, compVerStatuses,
					notifHubProjectReleaseItem);
		} catch (UnexpectedHubResponseException | HubNotificationServiceException e) {
			logger.error(e);
			return null;
		}
		return events;
	}

}
