package com.blackducksoftware.integration.jira.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class HubProjectTest {

	@Test
	public void testHubProject() {
		final String name1 = "name1";
		final String projectUrl1 = "projectUrl1";
		final Boolean projectExists1 = true;

		final String name2 = "name2";
		final String projectUrl2 = "projectUrl2";
		final Boolean projectExists2 = false;

		final HubProject item1 = new HubProject();
		item1.setProjectName(name1);
		item1.setProjectUrl(projectUrl1);
		item1.setProjectExists(projectExists1);
		final HubProject item2 = new HubProject();
		item2.setProjectName(name2);
		item2.setProjectUrl(projectUrl2);
		item2.setProjectExists(projectExists2);
		final HubProject item3 = new HubProject();
		item3.setProjectName(name1);
		item3.setProjectUrl(projectUrl1);
		item3.setProjectExists(projectExists1);

		assertEquals(name1, item1.getProjectName());
		assertEquals(projectUrl1, item1.getProjectUrl());
		assertEquals(projectExists1, item1.getProjectExists());

		assertEquals(name2, item2.getProjectName());
		assertEquals(projectUrl2, item2.getProjectUrl());
		assertEquals(projectExists2, item2.getProjectExists());

		assertTrue(!item1.equals(item2));
		assertTrue(item1.equals(item3));

		assertTrue(item1.hashCode() != item2.hashCode());
		assertEquals(item1.hashCode(), item3.hashCode());

		final StringBuilder builder = new StringBuilder();
		builder.append("HubProject [projectName=");
		builder.append(item1.getProjectName());
		builder.append(", projectUrl=");
		builder.append(item1.getProjectUrl());
		builder.append(", projectExists=");
		builder.append(item1.getProjectExists());
		builder.append("]");

		assertEquals(builder.toString(), item1.toString());
	}

}
