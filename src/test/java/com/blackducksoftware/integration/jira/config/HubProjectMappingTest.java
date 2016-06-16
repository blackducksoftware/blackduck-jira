package com.blackducksoftware.integration.jira.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class HubProjectMappingTest {

	@Test
	public void testHubProjectMapping() {
		final String jiraName1 = "name1";
		final Long jiraId1 = 0L;
		final String jiraKey1 = "key1";
		final Boolean jiraProjectExists1 = true;

		final JiraProject jira1 = new JiraProject();
		jira1.setProjectName(jiraName1);
		jira1.setProjectId(jiraId1);
		jira1.setProjectKey(jiraKey1);
		jira1.setProjectExists(jiraProjectExists1);

		final String hubName1 = "name1";
		final String hubProjectUrl1 = "projectUrl1";
		final Boolean hubProjectExists1 = true;

		final HubProject hub1 = new HubProject();
		hub1.setProjectName(hubName1);
		hub1.setProjectUrl(hubProjectUrl1);
		hub1.setProjectExists(hubProjectExists1);

		final String jiraName2 = "name2";
		final Long jiraId2 = 2L;
		final String jiraKey2 = "key2";
		final Boolean jiraProjectExists2 = false;

		final JiraProject jira2 = new JiraProject();
		jira2.setProjectName(jiraName2);
		jira2.setProjectId(jiraId2);
		jira2.setProjectKey(jiraKey2);
		jira2.setProjectExists(jiraProjectExists2);

		final String hubName2 = "name2";
		final String hubProjectUrl2 = "projectUrl2";
		final Boolean hubProjectExists2 = false;

		final HubProject hub2 = new HubProject();
		hub2.setProjectName(hubName2);
		hub2.setProjectUrl(hubProjectUrl2);
		hub2.setProjectExists(hubProjectExists2);

		final HubProjectMapping item1 = new HubProjectMapping();
		item1.setJiraProject(jira1);
		item1.setHubProject(hub1);
		final HubProjectMapping item2 = new HubProjectMapping();
		item2.setJiraProject(jira2);
		item2.setHubProject(hub2);
		final HubProjectMapping item3 = new HubProjectMapping();
		item3.setJiraProject(jira1);
		item3.setHubProject(hub1);

		assertEquals(jira1, item1.getJiraProject());
		assertEquals(hub1, item1.getHubProject());

		assertEquals(jira2, item2.getJiraProject());
		assertEquals(hub2, item2.getHubProject());

		assertTrue(!item1.equals(item2));
		assertTrue(item1.equals(item3));

		assertTrue(item1.hashCode() != item2.hashCode());
		assertEquals(item1.hashCode(), item3.hashCode());

		final StringBuilder builder = new StringBuilder();
		builder.append("HubProjectMapping [jiraProject=");
		builder.append(item1.getJiraProject());
		builder.append(", hubProject=");
		builder.append(item1.getHubProject());
		builder.append("]");

		assertEquals(builder.toString(), item1.toString());
	}

}
