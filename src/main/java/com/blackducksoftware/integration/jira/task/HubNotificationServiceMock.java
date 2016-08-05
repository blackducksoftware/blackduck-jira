package com.blackducksoftware.integration.jira.task;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.item.HubItemsService;
import com.blackducksoftware.integration.hub.logging.IntLogger;
import com.blackducksoftware.integration.hub.meta.MetaInformation;
import com.blackducksoftware.integration.hub.notification.NotificationDateRange;
import com.blackducksoftware.integration.hub.notification.NotificationService;
import com.blackducksoftware.integration.hub.notification.NotificationServiceException;
import com.blackducksoftware.integration.hub.notification.api.NotificationItem;
import com.blackducksoftware.integration.hub.notification.api.VulnerabilityNotificationContent;
import com.blackducksoftware.integration.hub.notification.api.VulnerabilityNotificationItem;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class HubNotificationServiceMock extends NotificationService {
	private final IntLogger logger;

	public HubNotificationServiceMock(final RestConnection restConnection, final HubIntRestService hub,
			final HubItemsService<NotificationItem> hubItemsService, final IntLogger logger) {
		// final RestConnection restConnection, final HubIntRestService hub,
		// final HubItemsService<NotificationItem> hubItemsService, final
		// IntLogger logger
		super(restConnection, hub, hubItemsService, logger);
		this.logger = logger;
	}

	@Override
	public List<NotificationItem> fetchNotifications(final NotificationDateRange dateRange)
			throws NotificationServiceException {
		logger.debug("fetchNotifications(");
		final String filePath = "/tmp/json/VulnerabilityNotificationContent_current.json";
		List<NotificationItem> notificationItems;
		try {
			notificationItems = mockNewVulnerabilityNotificationItems(filePath, false);
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
