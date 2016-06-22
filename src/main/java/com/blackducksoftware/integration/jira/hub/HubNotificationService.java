package com.blackducksoftware.integration.jira.hub;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.ResourceDoesNotExistException;
import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.hub.item.HubItemsService;
import com.blackducksoftware.integration.hub.policy.api.PolicyRule;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.version.api.ReleaseItem;
import com.blackducksoftware.integration.jira.HubJiraLogger;
import com.blackducksoftware.integration.jira.hub.model.component.BomComponentVersionPolicyStatus;
import com.blackducksoftware.integration.jira.hub.model.component.ComponentVersion;
import com.blackducksoftware.integration.jira.hub.model.notification.NotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.RuleViolationNotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.VulnerabilityNotificationItem;
import com.google.gson.reflect.TypeToken;

/**
 * Hub Notification get methods. TODO: Move to hub-common.
 *
 * @author sbillings
 *
 */
public class HubNotificationService {
	private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));
	private final RestConnection restConnection;
	private final HubIntRestService hub;
	private final HubItemsService<NotificationItem> hubItemsService;

	private SimpleDateFormat dateFormatter;

	/**
	 * Construct with given hub-access objects.
	 *
	 * @param restConnection
	 *            fully initialized (setCookies() has been called)
	 * @param hub
	 * @param hubItemsService
	 */
	public HubNotificationService(final RestConnection restConnection, final HubIntRestService hub,
			final HubItemsService<NotificationItem> hubItemsService) {
		this.restConnection = restConnection;
		this.hub = hub;
		this.hubItemsService = hubItemsService;

		dateFormatter = new SimpleDateFormat(RestConnection.JSON_DATE_FORMAT);
		dateFormatter.setTimeZone(java.util.TimeZone.getTimeZone("Zulu"));
	}

	/**
	 * Construct with given Hub connection details.
	 *
	 * @param hubUrl
	 * @param username
	 * @param password
	 * @throws HubNotificationServiceException
	 */
	public HubNotificationService(final String hubUrl, final String username, final String password)
			throws HubNotificationServiceException {
		restConnection = new RestConnection(hubUrl);
		try {
			restConnection.setCookies(username, password);
		} catch (URISyntaxException | BDRestException e) {
			throw new HubNotificationServiceException("");
		}

		try {
			hub = new HubIntRestService(restConnection);
		} catch (final URISyntaxException e) {
			throw new HubNotificationServiceException("");
		}

		final TypeToken<NotificationItem> typeToken = new TypeToken<NotificationItem>() {
		};
		final Map<String, Class<? extends NotificationItem>> typeToSubclassMap = new HashMap<>();
		typeToSubclassMap.put("VULNERABILITY", VulnerabilityNotificationItem.class);
		typeToSubclassMap.put("RULE_VIOLATION", RuleViolationNotificationItem.class);
		typeToSubclassMap.put("POLICY_OVERRIDE", PolicyOverrideNotificationItem.class);

		hubItemsService = new HubItemsService<NotificationItem>(restConnection, NotificationItem.class, typeToken,
				typeToSubclassMap);
	}

	public String getHubVersion() throws HubNotificationServiceException {
		try {
			return hub.getHubVersion();
		} catch (IOException | BDRestException | URISyntaxException e) {
			throw new HubNotificationServiceException(e.getMessage());
		}
	}

	public List<NotificationItem> fetchNotifications(final NotificationDateRange dateRange)
			throws HubNotificationServiceException {

		final int limit = 1000; // TODO may need chunking and maybe retry logic to
		// handle large sets

		final String startDateString = dateFormatter.format(dateRange.getStartDate());
		final String endDateString = dateFormatter.format(dateRange.getEndDate());

		logger.info("fetchNotifications(): Getting notifications from " + startDateString + " to " + endDateString);

		final List<String> urlSegments = new ArrayList<>();
		urlSegments.add("api");
		urlSegments.add("notifications");

		final Set<AbstractMap.SimpleEntry<String, String>> queryParameters = new HashSet<>();
		queryParameters.add(new AbstractMap.SimpleEntry<String, String>("startDate", startDateString));
		queryParameters.add(new AbstractMap.SimpleEntry<String, String>("endDate", endDateString));
		queryParameters.add(new AbstractMap.SimpleEntry<String, String>("limit", String.valueOf(limit)));
		List<NotificationItem> items;
		try {
			items = hubItemsService.httpGetItemList(urlSegments, queryParameters);
		} catch (IOException | URISyntaxException | ResourceDoesNotExistException | BDRestException e) {
			throw new HubNotificationServiceException("Error parsing NotificationItemList: " + e.getMessage(), e);
		}
		return items;
	}

	public BomComponentVersionPolicyStatus getPolicyStatus(final String policyStatusUrl)
			throws HubNotificationServiceException {
		BomComponentVersionPolicyStatus bomComponentVersionPolicyStatus;
		try {
			bomComponentVersionPolicyStatus = restConnection.httpGetFromAbsoluteUrl(
					BomComponentVersionPolicyStatus.class, policyStatusUrl);
		} catch (ResourceDoesNotExistException | URISyntaxException | IOException | BDRestException e) {
			throw new HubNotificationServiceException("Error getting a BomComponentVersionPolicyStatus: "
					+ e.getMessage(), e);
		}
		return bomComponentVersionPolicyStatus;
	}

	public PolicyRule getPolicyRule(final String ruleUrl) throws HubNotificationServiceException {
		PolicyRule rule;
		try {
			rule = restConnection.httpGetFromAbsoluteUrl(PolicyRule.class, ruleUrl);
		} catch (ResourceDoesNotExistException | URISyntaxException | IOException | BDRestException e) {
			throw new HubNotificationServiceException("Error getting rule from: " + ruleUrl + ": " + e.getMessage(), e);
		}
		return rule;
	}

	public ComponentVersion getComponentVersion(final String componentVersionUrl)
			throws HubNotificationServiceException {
		ComponentVersion componentVersion;
		try {
			componentVersion = restConnection.httpGetFromAbsoluteUrl(ComponentVersion.class, componentVersionUrl);
		} catch (ResourceDoesNotExistException | URISyntaxException | IOException | BDRestException e) {
			throw new HubNotificationServiceException("Error getting component version from: " + componentVersionUrl
					+ ": " + e.getMessage(), e);
		}
		return componentVersion;
	}

	public ReleaseItem getProjectReleaseItemFromProjectReleaseUrl(final String versionUrl)
			throws HubNotificationServiceException,
			UnexpectedHubResponseException {
		ReleaseItem projectVersion;
		try {
			projectVersion = hub.getProjectVersion(versionUrl);
		} catch (IOException | BDRestException | URISyntaxException e) {
			throw new HubNotificationServiceException("Error getting Project Link from ProjectVersion: " + versionUrl,
					e);
		}
		return projectVersion;
	}
}
