/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package com.blackducksoftware.integration.jira.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class HubJiraConfigSerializableTest {

	@Test
	public void testHubJiraConfigSerializable() {
		final String errorMessage1 = "errorMessage1";
		final String intervalBetweenChecks1 = "intervalBetweenChecks1";
		final String intervalBetweenChecksError1 = "intervalBetweenChecksError1";
		final String hubProjectMappingError1 = "hubProjectMappingError1";
		final String policyRulesError1 = "policyRulesError1";

		final String policyName1 = "name1";
		final String policyDescription1 = "description1";
		final String policyUrl1 = "policyUrl1";
		final Boolean policyChecked1 = true;

		final PolicyRuleSerializable policy1 = new PolicyRuleSerializable();
		policy1.setName(policyName1);
		policy1.setDescription(policyDescription1);
		policy1.setPolicyUrl(policyUrl1);
		policy1.setChecked(policyChecked1);

		final String jiraName1 = "name1";
		final Long jiraId1 = 0L;
		final String jiraKey1 = "key1";
		final String jiraProjectError1 = "error1";

		final JiraProject jira1 = new JiraProject();
		jira1.setProjectName(jiraName1);
		jira1.setProjectId(jiraId1);
		jira1.setProjectKey(jiraKey1);
		jira1.setProjectError(jiraProjectError1);

		final String hubName1 = "name1";
		final String hubProjectUrl1 = "projectUrl1";

		final HubProject hub1 = new HubProject();
		hub1.setProjectName(hubName1);
		hub1.setProjectUrl(hubProjectUrl1);

		final String errorMessage2 = "";
		final String intervalBetweenChecks2 = "";
		final String intervalBetweenChecksError2 = "";
		final String hubProjectMappingError2 = "";
		final String policyRulesError2 = "";

		final String policyName2 = "name2";
		final String policyDescription2 = "description2";
		final String policyUrl2 = "policyUrl2";
		final Boolean policyChecked2 = false;

		final PolicyRuleSerializable policy2 = new PolicyRuleSerializable();
		policy2.setName(policyName2);
		policy2.setDescription(policyDescription2);
		policy2.setPolicyUrl(policyUrl2);
		policy2.setChecked(policyChecked2);

		final String jiraName2 = "name2";
		final Long jiraId2 = 2L;
		final String jiraKey2 = "key2";
		final String jiraProjectError2 = "error2";

		final JiraProject jira2 = new JiraProject();
		jira2.setProjectName(jiraName2);
		jira2.setProjectId(jiraId2);
		jira2.setProjectKey(jiraKey2);
		jira2.setProjectError(jiraProjectError2);

		final String hubName2 = "name2";
		final String hubProjectUrl2 = "projectUrl2";

		final HubProject hub2 = new HubProject();
		hub2.setProjectName(hubName2);
		hub2.setProjectUrl(hubProjectUrl2);

		final HubProjectMapping mapping1 = new HubProjectMapping();
		mapping1.setJiraProject(jira1);
		mapping1.setHubProject(hub1);
		final HubProjectMapping mapping2 = new HubProjectMapping();
		mapping2.setJiraProject(jira2);
		mapping2.setHubProject(hub2);

		final List<JiraProject> jiraProjects1 = new ArrayList<JiraProject>();
		jiraProjects1.add(jira1);

		final String jiraProjectsError1 = "jiraProjectsError1";

		final List<HubProject> hubProjects1 = new ArrayList<HubProject>();
		hubProjects1.add(hub1);

		final String hubProjectsError1 = "hubProjectsError1";

		final Set<HubProjectMapping> mappings1 = new HashSet<HubProjectMapping>();
		mappings1.add(mapping1);

		final List<PolicyRuleSerializable> policyRules1 = new ArrayList<PolicyRuleSerializable>();
		policyRules1.add(policy1);

		final List<JiraProject> jiraProjects2 = new ArrayList<JiraProject>();
		jiraProjects2.add(jira2);

		final String jiraProjectsError2 = "jiraProjectsError2";

		final List<HubProject> hubProjects2 = new ArrayList<HubProject>();
		hubProjects2.add(hub2);

		final String hubProjectsError2 = "hubProjectsError2";

		final Set<HubProjectMapping> mappings2 = new HashSet<HubProjectMapping>();
		mappings2.add(mapping2);

		final List<PolicyRuleSerializable> policyRules2 = new ArrayList<PolicyRuleSerializable>();
		policyRules2.add(policy2);


		final HubJiraConfigSerializable item1 = new HubJiraConfigSerializable();
		item1.setErrorMessage(errorMessage1);
		item1.setHubProjectMappingError(hubProjectMappingError1);
		item1.setHubProjectMappings(mappings1);
		item1.setJiraProjectsError(jiraProjectsError1);
		item1.setHubProjects(hubProjects1);
		item1.setHubProjectsError(hubProjectsError1);
		item1.setIntervalBetweenChecks(intervalBetweenChecks1);
		item1.setIntervalBetweenChecksError(intervalBetweenChecksError1);
		item1.setJiraProjects(jiraProjects1);
		item1.setPolicyRules(policyRules1);
		item1.setPolicyRulesError(policyRulesError1);

		final HubJiraConfigSerializable item2 = new HubJiraConfigSerializable();
		item2.setErrorMessage(errorMessage2);
		item2.setHubProjectMappingError(hubProjectMappingError2);
		item2.setJiraProjectsError(jiraProjectsError2);
		item2.setHubProjectMappings(mappings2);
		item2.setHubProjects(hubProjects2);
		item2.setHubProjectsError(hubProjectsError2);
		item2.setIntervalBetweenChecks(intervalBetweenChecks2);
		item2.setIntervalBetweenChecksError(intervalBetweenChecksError2);
		item2.setJiraProjects(jiraProjects2);
		item2.setPolicyRules(policyRules2);
		item2.setPolicyRulesError(policyRulesError2);

		final Gson gson = new GsonBuilder().create();

		final String mappingJson = gson.toJson(mappings1);
		final String rulesJson = gson.toJson(policyRules1);

		final HubJiraConfigSerializable item3 = new HubJiraConfigSerializable();
		item3.setErrorMessage(errorMessage1);
		item3.setHubProjectMappingError(hubProjectMappingError1);
		item3.setJiraProjectsError(jiraProjectsError1);
		item3.setHubProjectMappingsJson(mappingJson);
		item3.setHubProjects(hubProjects1);
		item3.setHubProjectsError(hubProjectsError1);
		item3.setIntervalBetweenChecks(intervalBetweenChecks1);
		item3.setIntervalBetweenChecksError(intervalBetweenChecksError1);
		item3.setJiraProjects(jiraProjects1);
		item3.setPolicyRulesJson(rulesJson);
		item3.setPolicyRulesError(policyRulesError1);

		assertEquals(errorMessage1, item1.getErrorMessage());
		assertEquals(hubProjectMappingError1, item1.getHubProjectMappingError());
		assertEquals(mappings1, item1.getHubProjectMappings());
		assertEquals(jiraProjectsError1, item1.getJiraProjectsError());
		assertEquals(hubProjects1, item1.getHubProjects());
		assertEquals(hubProjectsError1, item1.getHubProjectsError());
		assertEquals(intervalBetweenChecks1, item1.getIntervalBetweenChecks());
		assertEquals(intervalBetweenChecksError1, item1.getIntervalBetweenChecksError());
		assertEquals(jiraProjects1, item1.getJiraProjects());
		assertEquals(policyRules1, item1.getPolicyRules());
		assertEquals(policyRulesError1, item1.getPolicyRulesError());

		assertEquals(errorMessage2, item2.getErrorMessage());
		assertEquals(hubProjectMappingError2, item2.getHubProjectMappingError());
		assertEquals(mappings2, item2.getHubProjectMappings());
		assertEquals(jiraProjectsError2, item2.getJiraProjectsError());
		assertEquals(hubProjects2, item2.getHubProjects());
		assertEquals(hubProjectsError2, item2.getHubProjectsError());
		assertEquals(intervalBetweenChecks2, item2.getIntervalBetweenChecks());
		assertEquals(intervalBetweenChecksError2, item2.getIntervalBetweenChecksError());
		assertEquals(jiraProjects2, item2.getJiraProjects());
		assertEquals(policyRules2, item2.getPolicyRules());
		assertEquals(policyRulesError2, item2.getPolicyRulesError());

		assertEquals(errorMessage1, item3.getErrorMessage());
		assertEquals(hubProjectMappingError1, item3.getHubProjectMappingError());
		assertEquals(mappings1, item3.getHubProjectMappings());
		assertEquals(jiraProjectsError1, item3.getJiraProjectsError());
		assertEquals(mappingJson, item3.getHubProjectMappingsJson());
		assertEquals(hubProjects1, item3.getHubProjects());
		assertEquals(hubProjectsError1, item3.getHubProjectsError());
		assertEquals(intervalBetweenChecks1, item3.getIntervalBetweenChecks());
		assertEquals(intervalBetweenChecksError1, item3.getIntervalBetweenChecksError());
		assertEquals(jiraProjects1, item3.getJiraProjects());
		assertEquals(policyRules1, item3.getPolicyRules());
		assertEquals(rulesJson, item3.getPolicyRulesJson());
		assertEquals(policyRulesError1, item3.getPolicyRulesError());

		assertTrue(!item1.equals(item2));
		assertTrue(item1.equals(item3));

		assertTrue(item1.hashCode() != item2.hashCode());
		assertEquals(item1.hashCode(), item3.hashCode());

		final StringBuilder builder = new StringBuilder();
		builder.append("HubJiraConfigSerializable [errorMessage=");
		builder.append(item1.getErrorMessage());
		builder.append(", intervalBetweenChecks=");
		builder.append(item1.getIntervalBetweenChecks());
		builder.append(", intervalBetweenChecksError=");
		builder.append(item1.getIntervalBetweenChecksError());
		builder.append(", jiraProjects=");
		builder.append(item1.getJiraProjects());
		builder.append(", jiraProjectsError=");
		builder.append(item1.getJiraProjectsError());
		builder.append(", hubProjects=");
		builder.append(item1.getHubProjects());
		builder.append(", hubProjectsError=");
		builder.append(item1.getHubProjectsError());
		builder.append(", hubProjectMappings=");
		builder.append(item1.getHubProjectMappings());
		builder.append(", hubProjectMappingError=");
		builder.append(item1.getHubProjectMappingError());
		builder.append(", policyRules=");
		builder.append(item1.getPolicyRules());
		builder.append(", policyRulesError=");
		builder.append(item1.getPolicyRulesError());
		builder.append("]");

		assertEquals(builder.toString(), item1.toString());
	}

}
