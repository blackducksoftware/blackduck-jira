package com.blackducksoftware.integration.jira.hub;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blackducksoftware.integration.jira.config.HubProjectMappings;
import com.blackducksoftware.integration.jira.hub.model.notification.NotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.RuleViolationNotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.VulnerabilityNotificationItem;
import com.blackducksoftware.integration.jira.hub.policy.PolicyOverrideNotificationFilter;
import com.blackducksoftware.integration.jira.hub.policy.PolicyViolationNotificationFilter;
import com.blackducksoftware.integration.jira.hub.vulnerability.VulnerabilityNotificationFilter;

public class ConverterLookupTable {
	private final Map<Class<? extends NotificationItem>, NotificationFilter> lookupTable = new HashMap<>();

	public ConverterLookupTable(final HubProjectMappings mappings, final TicketGeneratorInfo ticketGenInfo,
			final List<String> linksOfRulesToMonitor, final HubNotificationService hubNotificationService) {

		final NotificationFilter vulnerabilityNotificationConverter = new VulnerabilityNotificationFilter(mappings,
				ticketGenInfo, linksOfRulesToMonitor, hubNotificationService);
		final NotificationFilter policyViolationNotificationFilter = new PolicyViolationNotificationFilter(mappings,
				ticketGenInfo, linksOfRulesToMonitor, hubNotificationService);
		final NotificationFilter policyOverrideNotificationConverter = new PolicyOverrideNotificationFilter(null, null,
				null, null);

		lookupTable.put(RuleViolationNotificationItem.class, policyViolationNotificationFilter);
		lookupTable.put(PolicyOverrideNotificationItem.class, policyOverrideNotificationConverter);
		lookupTable.put(VulnerabilityNotificationItem.class, vulnerabilityNotificationConverter);
	}

	public NotificationFilter getConverter(final NotificationItem notif) {
		if (notif == null) {
			return null;
		}
		final Class c = notif.getClass();
		final NotificationFilter converter = lookupTable.get(c);
		return converter;
	}
}
