package com.blackducksoftware.integration.jira.task.conversion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.blackducksoftware.integration.hub.dataservices.notification.items.PolicyOverrideContentItem;
import com.blackducksoftware.integration.hub.dataservices.notification.items.PolicyViolationContentItem;
import com.blackducksoftware.integration.hub.dataservices.notification.items.VulnerabilityContentItem;
import com.blackducksoftware.integration.hub.exception.NotificationServiceException;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.exception.ConfigurationException;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;

public class ConverterLookupTableTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void test() throws NotificationServiceException, ConfigurationException {
		final JiraServices jiraServices = mockJiraServices();
		final ConverterLookupTable table = new ConverterLookupTable(null, jiraServices, null, null);

		try {
			assertEquals(null, table.getConverter(null));
			fail("Expected null pointer exception");
		} catch (final NullPointerException e) {
			// expected
		}

		NotificationToEventConverter converter = table
.getConverter(new VulnerabilityContentItem(new Date(), null,
				null, null, null, null, null, null, null));
		assertEquals("com.blackducksoftware.integration.jira.task.conversion.VulnerabilityNotificationConverter",
				converter.getClass().getName());

		converter = table.getConverter(new PolicyViolationContentItem(new Date(), null, null, null, null, null, null));
		assertEquals("com.blackducksoftware.integration.jira.task.conversion.PolicyViolationNotificationConverter",
				converter.getClass().getName());

		converter = table.getConverter(new PolicyOverrideContentItem(new Date(), null, null, null, null, null, null,
				null, null));
		assertEquals("com.blackducksoftware.integration.jira.task.conversion.PolicyOverrideNotificationConverter",
				converter.getClass().getName());
	}

	private JiraServices mockJiraServices() {

		final ConstantsManager constantsManager = Mockito.mock(ConstantsManager.class);
		final Collection<IssueType> issueTypes = new ArrayList<>();
		final IssueType policyIssueType = Mockito.mock(IssueType.class);
		Mockito.when(policyIssueType.getName()).thenReturn(HubJiraConstants.HUB_POLICY_VIOLATION_ISSUE);
		Mockito.when(policyIssueType.getId()).thenReturn("policyIssueTypeId");
		issueTypes.add(policyIssueType);
		final IssueType vulnerabilityIssueType = Mockito.mock(IssueType.class);
		Mockito.when(vulnerabilityIssueType.getName()).thenReturn(HubJiraConstants.HUB_VULNERABILITY_ISSUE);
		Mockito.when(vulnerabilityIssueType.getId()).thenReturn("vulnerabilityIssueTypeId");
		issueTypes.add(vulnerabilityIssueType);

		Mockito.when(constantsManager.getAllIssueTypeObjects()).thenReturn(issueTypes);

		final JiraServices jiraServices = Mockito.mock(JiraServices.class);
		Mockito.when(jiraServices.getConstantsManager()).thenReturn(constantsManager);

		return jiraServices;
	}

}
