package com.blackducksoftware.integration.jira.task.conversion.output;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.integration.hub.api.policy.PolicyExpression;
import com.blackducksoftware.integration.hub.api.policy.PolicyExpressions;
import com.blackducksoftware.integration.hub.api.policy.PolicyRule;
import com.blackducksoftware.integration.hub.api.project.ProjectVersion;
import com.blackducksoftware.integration.hub.dataservices.notification.items.PolicyViolationContentItem;
import com.blackducksoftware.integration.hub.exception.MissingUUIDException;
import com.blackducksoftware.integration.hub.meta.MetaAllowEnum;
import com.blackducksoftware.integration.hub.meta.MetaInformation;
import com.blackducksoftware.integration.hub.meta.MetaLink;

public class PolicyEventTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testHttpWithoutPort() throws MissingUUIDException, URISyntaxException {
		final String urlPrefix = "http://hub.bds.com/";
		final String projectVersionRelativeUrl = "api/projects/1234/versions/5678";
		final String projectVersionUrl = urlPrefix + projectVersionRelativeUrl;

		final String componentVersionRelativeUrl = "api/components/12345/versions/56789";
		final String componentVersionUrl = urlPrefix + componentVersionRelativeUrl;

		final ProjectVersion projectVersion = new ProjectVersion();
		projectVersion.setProjectName("projectName");
		projectVersion.setProjectVersionName("projectVersionName");
		projectVersion.setUrl(projectVersionUrl);

		final PolicyViolationContentItem vulnerabilityContentItem = new PolicyViolationContentItem(new Date(),
				projectVersion, "componentName", "componentVersion", null, componentVersionUrl, null);
		final MetaInformation meta = new MetaInformation(new ArrayList<MetaAllowEnum>(),
				"http://hub.bds.com/policies/1234",
				new ArrayList<MetaLink>());
		final PolicyExpressions expression = new PolicyExpressions("AND", new ArrayList<PolicyExpression>());
		final PolicyRule policyRule = new PolicyRule(meta, "name", "description", true, true, expression,
				(new Date()).toString(), "createdBy", (new Date()).toString(), "updatedBy");

		final PolicyEvent event = new PolicyEvent(HubEventAction.OPEN, "jiraUserName", "jiraUserId", "issueAssigneeId",
				"jiraIssueTypeId", 1L, "jiraProjectName", vulnerabilityContentItem, policyRule, "resolveComment");

		final String expectedKey = "t=p|jp=1|hpv="
				+ String.valueOf(projectVersionRelativeUrl.hashCode())
				+ "|hc=|hcv="
				+ String.valueOf(componentVersionRelativeUrl.hashCode() + "|hr="
						+ String.valueOf(policyRule.getMeta().getRelativeHref().hashCode()));

		assertEquals(expectedKey, event.getUniquePropertyKey());
	}

}
