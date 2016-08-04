package com.blackducksoftware.integration.jira.hub;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blackducksoftware.integration.hub.notification.NotificationService;
import com.blackducksoftware.integration.hub.notification.NotificationServiceException;
import com.blackducksoftware.integration.hub.notification.api.NotificationItem;
import com.blackducksoftware.integration.hub.notification.api.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.hub.notification.api.RuleViolationNotificationItem;
import com.blackducksoftware.integration.hub.notification.api.VulnerabilityNotificationItem;
import com.blackducksoftware.integration.jira.config.HubProjectMappings;
import com.blackducksoftware.integration.jira.hub.policy.PolicyOverrideNotificationConverter;
import com.blackducksoftware.integration.jira.hub.policy.PolicyViolationNotificationConverter;
import com.blackducksoftware.integration.jira.hub.vulnerability.VulnerabilityNotificationConverter;
import com.blackducksoftware.integration.jira.issue.JiraServices;

public class ConverterLookupTable {
	private final Map<Class<? extends NotificationItem>, NotificationToEventConverter> lookupTable = new HashMap<>();

	public ConverterLookupTable(final HubProjectMappings mappings, final JiraServices jiraServices,
			final JiraContext jiraContext,
			final List<String> linksOfRulesToMonitor, final NotificationService hubNotificationService) {

		final NotificationToEventConverter vulnerabilityNotificationConverter = new VulnerabilityNotificationConverter(mappings,
 jiraServices, jiraContext, hubNotificationService);
		final NotificationToEventConverter policyViolationNotificationConverter = new PolicyViolationNotificationConverter(mappings,
				jiraServices, jiraContext, linksOfRulesToMonitor, hubNotificationService);
		final NotificationToEventConverter policyOverrideNotificationConverter = new PolicyOverrideNotificationConverter(
				mappings, jiraServices, jiraContext, linksOfRulesToMonitor, hubNotificationService);

		lookupTable.put(RuleViolationNotificationItem.class, policyViolationNotificationConverter);
		lookupTable.put(PolicyOverrideNotificationItem.class, policyOverrideNotificationConverter);
		lookupTable.put(VulnerabilityNotificationItem.class, vulnerabilityNotificationConverter);
	}

	public NotificationToEventConverter getConverter(final NotificationItem notif)
			throws NotificationServiceException {
		final Class c = notif.getClass();
		final NotificationToEventConverter converter = lookupTable.get(c);
		if (converter == null) {
			throw new NotificationServiceException("Notification type unknown for notification: " + notif);
		}
		return converter;
	}
}
