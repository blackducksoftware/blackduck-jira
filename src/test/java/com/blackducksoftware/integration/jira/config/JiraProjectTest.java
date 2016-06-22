package com.blackducksoftware.integration.jira.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class JiraProjectTest {

	@Test
	public void testJiraProject() {
		final String name1 = "name1";
		final Long id1 = 0L;
		final String key1 = "key1";
		final String issueTypeId1 = "type1";
		final String projectError1 = "error1";

		final String name2 = "name2";
		final Long id2 = 2L;
		final String key2 = "key2";
		final String issueTypeId2 = "type2";
		final String projectError2 = "error2";

		final JiraProject item1 = new JiraProject();
		item1.setProjectName(name1);
		item1.setProjectId(id1);
		item1.setProjectKey(key1);
		item1.setIssueTypeId(issueTypeId1);
		item1.setProjectError(projectError1);
		final JiraProject item2 = new JiraProject();
		item2.setProjectName(name2);
		item2.setProjectId(id2);
		item2.setProjectKey(key2);
		item2.setIssueTypeId(issueTypeId2);
		item2.setProjectError(projectError2);
		final JiraProject item3 = new JiraProject();
		item3.setProjectName(name1);
		item3.setProjectId(id1);
		item3.setProjectKey(key1);
		item3.setIssueTypeId(issueTypeId1);
		item3.setProjectError(projectError1);

		assertEquals(name1, item1.getProjectName());
		assertEquals(id1, item1.getProjectId());
		assertEquals(key1, item1.getProjectKey());

		assertEquals(projectError1, item1.getProjectError());

		assertEquals(name2, item2.getProjectName());
		assertEquals(id2, item2.getProjectId());
		assertEquals(key2, item2.getProjectKey());
		assertEquals(issueTypeId2, item2.getIssueTypeId());
		assertEquals(projectError2, item2.getProjectError());

		assertTrue(!item1.equals(item2));
		assertTrue(item1.equals(item3));

		assertTrue(item1.hashCode() != item2.hashCode());
		assertEquals(item1.hashCode(), item3.hashCode());

		final StringBuilder builder = new StringBuilder();
		builder.append("JiraProject [projectName=");
		builder.append(item1.getProjectName());
		builder.append(", projectId=");
		builder.append(item1.getProjectId());
		builder.append(", projectKey=");
		builder.append(item1.getProjectKey());
		builder.append(", issueTypeId=");
		builder.append(item1.getIssueTypeId());
		builder.append(", projectError=");
		builder.append(item1.getProjectError());
		builder.append("]");

		assertEquals(builder.toString(), item1.toString());
	}

}
