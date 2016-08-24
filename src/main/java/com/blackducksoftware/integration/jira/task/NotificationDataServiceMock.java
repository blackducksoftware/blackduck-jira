package com.blackducksoftware.integration.jira.task;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.blackducksoftware.integration.hub.api.notification.VulnerabilitySourceQualifiedId;
import com.blackducksoftware.integration.hub.api.project.ProjectVersion;
import com.blackducksoftware.integration.hub.dataservices.NotificationDataService;
import com.blackducksoftware.integration.hub.dataservices.items.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservices.items.PolicyNotificationFilter;
import com.blackducksoftware.integration.hub.dataservices.items.VulnerabilityContentItem;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

//TODO remove this mock once we have a way to trigger vulnerability notifications
public class NotificationDataServiceMock extends NotificationDataService {

	public NotificationDataServiceMock(final RestConnection restConnection, final Gson gson,
			final JsonParser jsonParser, final PolicyNotificationFilter policyFilter) {
		super(restConnection, gson, jsonParser, policyFilter);
	}

	@Override
	public List<NotificationContentItem> getAllNotifications(final Date startDate, final Date endDate)
			throws IOException, URISyntaxException, BDRestException {

		final List<NotificationContentItem> notifications = super.getAllNotifications(startDate, endDate);

		if (notifications != null && !notifications.isEmpty()) {
			final ProjectVersion projectVersion = new ProjectVersion();
			projectVersion.setProjectName("TestProject");
			projectVersion.setProjectVersionName("TestVersion");
			projectVersion
			.setProjectVersionLink(
					"http://test/projects/" + UUID.fromString("3670db83-7916-4398-af2c-a05798bbf2ef")
					+ "/versions/" + UUID.fromString("17b5cf06-439f-4ffe-9b4f-d262f56b2d8f"));

			final List<VulnerabilitySourceQualifiedId> newVulns = new ArrayList<>();
			final VulnerabilitySourceQualifiedId newVuln = new VulnerabilitySourceQualifiedId("FAKE NEW",
					"CVE-test new ");
			newVulns.add(newVuln);

			final List<VulnerabilitySourceQualifiedId> updatedVulns = new ArrayList<>();
			final VulnerabilitySourceQualifiedId updatedVuln = new VulnerabilitySourceQualifiedId("FAKE UPDATED",
					"CVE-test updated");
			updatedVulns.add(updatedVuln);

			final List<VulnerabilitySourceQualifiedId> deletedVulns = new ArrayList<>();
			final VulnerabilitySourceQualifiedId deletedVuln = new VulnerabilitySourceQualifiedId("FAKE DELETED",
					"CVE-test deleted");
			deletedVulns.add(deletedVuln);


			final VulnerabilityContentItem vulnNotification = new VulnerabilityContentItem(projectVersion, "TestComp",
					"TestCompVersion", UUID.fromString("d15b7f61-c5b9-4f31-8605-769b12198d91"),
					UUID.fromString("0ce0a7b7-1872-4643-b389-da58a753d70d"),
					newVulns, updatedVulns, deletedVulns);
			notifications.add(vulnNotification);
		}
		return notifications;
	}

}
