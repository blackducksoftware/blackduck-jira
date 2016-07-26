package com.blackducksoftware.integration.jira.hub;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.item.HubItemsService;
import com.blackducksoftware.integration.hub.meta.MetaInformation;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.jira.HubJiraLogger;
import com.blackducksoftware.integration.jira.hub.model.notification.NotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.VulnerabilityNotificationContent;
import com.blackducksoftware.integration.jira.hub.model.notification.VulnerabilityNotificationItem;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class HubNotificationServiceMock extends HubNotificationService {
	private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));

	public HubNotificationServiceMock(final RestConnection restConnection, final HubIntRestService hub,
			final HubItemsService<NotificationItem> hubItemsService) {
		super(restConnection, hub, hubItemsService);
	}

	@Override
	public List<NotificationItem> fetchNotifications(final NotificationDateRange dateRange)
			throws HubNotificationServiceException {
		logger.debug("fetchNotifications(");
		final String filePath = "/tmp/json/VulnerabilityNotificationContent_new.json";
		List<NotificationItem> notificationItems;
		try {
			notificationItems = mockNewVulnerabilityNotificationItems(filePath, true);
		} catch (final IOException e) {
			logger.error("Error mocking notifications from file: " + filePath + ": " + e.getMessage());
			return new ArrayList<>(0);
		}

		return notificationItems;
	}

	private List<NotificationItem> mockNewVulnerabilityNotificationItems(final String filePath,
			final boolean createDuplicate) throws IOException {
		final List<NotificationItem> notificationItems = new ArrayList<>();
		final MetaInformation meta = new MetaInformation(null, null, null);
		final VulnerabilityNotificationItem notificationItem = new VulnerabilityNotificationItem(meta);
		final String jsonString = readFile(filePath);
		final VulnerabilityNotificationContent content = createVulnerabilityNotificationContent(jsonString);
		notificationItem.setContent(content);

		notificationItems.add(notificationItem);
		if (createDuplicate) {
			notificationItems.add(notificationItem);
		}
		return notificationItems;
	}

	private VulnerabilityNotificationContent createVulnerabilityNotificationContent(final String jsonString) {
		final Gson gson = new GsonBuilder().create();
		final VulnerabilityNotificationContent vulnContent = gson.fromJson(jsonString,
				VulnerabilityNotificationContent.class);
		return vulnContent;
	}

	private String readFile(final String path) throws IOException {
		final byte[] jsonBytes = Files.readAllBytes(Paths.get(path));
		final String jsonString = new String(jsonBytes, Charset.forName("UTF-8"));
		return jsonString;
	}
}
