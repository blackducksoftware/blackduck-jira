package com.blackducksoftware.integration.jira.hub;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.ResourceDoesNotExistException;
import com.blackducksoftware.integration.hub.item.HubItemsService;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.jira.hub.model.notification.NotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.RuleViolationNotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.VulnerabilityNotificationItem;

import com.google.gson.reflect.TypeToken;

public class HubNotificationService {
	private final RestConnection restConnection;
	private final HubIntRestService hub;
	private final HubItemsService<NotificationItem> hubItemsService;

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
		} catch (URISyntaxException e) {
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

	public <T> T getFromRelativeUrl(final Class<T> modelClass, final List<String> urlSegments,
			final Set<AbstractMap.SimpleEntry<String, String>> queryParameters) throws HubNotificationServiceException {

		try {
			return restConnection.httpGetFromRelativeUrl(modelClass, urlSegments, queryParameters);
		} catch (URISyntaxException | IOException | ResourceDoesNotExistException | BDRestException e) {
			throw new HubNotificationServiceException("Error getting resource from relative url segments "
					+ urlSegments + " and query parameters " + queryParameters + "; errorCode: " + e.getMessage());
		}
	}

	public <T> T getFromAbsoluteUrl(final Class<T> modelClass, final String url) throws HubNotificationServiceException {
		if (url == null) {
			return null;
		}
		try {
			return restConnection.httpGetFromAbsoluteUrl(modelClass, url);
		} catch (ResourceDoesNotExistException | URISyntaxException | IOException | BDRestException e) {
			throw new HubNotificationServiceException("Error getting resource from " + url + ": " + e.getMessage());
		}
	}

	public String getVersion() throws HubNotificationServiceException {
		try {
			return hub.getHubVersion();
		} catch (IOException | BDRestException | URISyntaxException e) {
			throw new HubNotificationServiceException(e.getMessage());
		}
	}

	public List<NotificationItem> getNotifications(final String startDate, final String endDate, final int limit)
			throws HubNotificationServiceException {

		final List<String> urlSegments = new ArrayList<>();
		urlSegments.add("api");
		urlSegments.add("notifications");

		final Set<AbstractMap.SimpleEntry<String, String>> queryParameters = new HashSet<>();
		queryParameters.add(new AbstractMap.SimpleEntry<String, String>("startDate", startDate));
		queryParameters.add(new AbstractMap.SimpleEntry<String, String>("endDate", endDate));
		queryParameters.add(new AbstractMap.SimpleEntry<String, String>("limit", String.valueOf(limit)));
		try {
			return hubItemsService.httpGetItemList(urlSegments, queryParameters);
		} catch (IOException | URISyntaxException | ResourceDoesNotExistException | BDRestException e) {
			throw new HubNotificationServiceException("Error parsing NotificationItemList: " + e.getMessage(), e);
		}
	}
}
