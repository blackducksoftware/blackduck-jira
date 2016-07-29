package com.blackducksoftware.integration.jira.hub;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blackducksoftware.integration.jira.config.HubProjectMappings;
import com.blackducksoftware.integration.jira.hub.model.notification.NotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.RuleViolationNotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.VulnerabilityNotificationItem;
import com.blackducksoftware.integration.jira.hub.policy.PolicyOverrideNotificationConverter;
import com.blackducksoftware.integration.jira.hub.policy.PolicyViolationNotificationConverter;
import com.blackducksoftware.integration.jira.hub.vulnerability.VulnerabilityNotificationConverter;

public class ConverterLookupTable {
	private final Map<Class<? extends NotificationItem>, NotificationToEventConverter> lookupTable = new HashMap<>();

	public ConverterLookupTable(final HubProjectMappings mappings, final TicketGeneratorInfo ticketGenInfo,
			final List<String> linksOfRulesToMonitor, final HubNotificationService hubNotificationService) {

		final NotificationToEventConverter vulnerabilityNotificationConverter = new VulnerabilityNotificationConverter(mappings,
				ticketGenInfo, linksOfRulesToMonitor, hubNotificationService);
		final NotificationToEventConverter policyViolationNotificationFilter = new PolicyViolationNotificationConverter(mappings,
				ticketGenInfo, linksOfRulesToMonitor, hubNotificationService);
		final NotificationToEventConverter policyOverrideNotificationConverter = new PolicyOverrideNotificationConverter(
				mappings, ticketGenInfo, linksOfRulesToMonitor, hubNotificationService);

		lookupTable.put(RuleViolationNotificationItem.class, policyViolationNotificationFilter);
		lookupTable.put(PolicyOverrideNotificationItem.class, policyOverrideNotificationConverter);
		lookupTable.put(VulnerabilityNotificationItem.class, vulnerabilityNotificationConverter);
	}

	public NotificationToEventConverter getConverter(final NotificationItem notif)
			throws HubNotificationServiceException {
		final Class c = notif.getClass();
		final NotificationToEventConverter converter = lookupTable.get(c);
		if (converter == null) {
			throw new HubNotificationServiceException("Notification type unknown for notification: " + notif);
		}
		return converter;
	}
}
