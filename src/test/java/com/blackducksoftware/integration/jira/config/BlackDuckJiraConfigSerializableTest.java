/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
 */
package com.blackducksoftware.integration.jira.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Test;

import com.blackducksoftware.integration.jira.common.model.BlackDuckProject;
import com.blackducksoftware.integration.jira.common.model.BlackDuckProjectMapping;
import com.blackducksoftware.integration.jira.common.model.JiraProject;
import com.blackducksoftware.integration.jira.common.model.PolicyRuleSerializable;
import com.blackducksoftware.integration.jira.config.model.BlackDuckJiraConfigSerializable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class BlackDuckJiraConfigSerializableTest {
    private static final String USER1 = "user1";
    private static final String USER2 = "user2";

    @Test
    public void testBlackDuckJiraConfigSerializable() {
        final String errorMessage1 = "errorMessage1";
        final String intervalBetweenChecks1 = "intervalBetweenChecks1";
        final String generalSettingsError1 = "generalSettingsError1";
        final String blackDuckProjectMappingError1 = "blackDuckProjectMappingError1";
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

        final String blackDuckName1 = "name1";
        final String blackDuckProjectUrl1 = "projectUrl1";

        final BlackDuckProject blackDuck1 = new BlackDuckProject();
        blackDuck1.setProjectName(blackDuckName1);
        blackDuck1.setProjectUrl(blackDuckProjectUrl1);

        final String errorMessage2 = "";
        final String intervalBetweenChecks2 = "";
        final String generalSettingsError2 = "";
        final String blackDuckProjectMappingError2 = "";
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

        final String blackDuckName2 = "name2";
        final String blackDuckProjectUrl2 = "projectUrl2";

        final BlackDuckProject blackDuck2 = new BlackDuckProject();
        blackDuck2.setProjectName(blackDuckName2);
        blackDuck2.setProjectUrl(blackDuckProjectUrl2);

        final BlackDuckProjectMapping mapping1 = new BlackDuckProjectMapping();
        mapping1.setJiraProject(jira1);
        mapping1.setHubProject(blackDuck1);
        final BlackDuckProjectMapping mapping2 = new BlackDuckProjectMapping();
        mapping2.setJiraProject(jira2);
        mapping2.setHubProject(blackDuck2);

        final List<JiraProject> jiraProjects1 = new ArrayList<>();
        jiraProjects1.add(jira1);

        final String jiraProjectsError1 = "jiraProjectsError1";

        final List<BlackDuckProject> blackDuckProjects1 = new ArrayList<>();
        blackDuckProjects1.add(blackDuck1);

        final String blackDuckProjectsError1 = "blackDuckProjectsError1";

        final Set<BlackDuckProjectMapping> mappings1 = new HashSet<>();
        mappings1.add(mapping1);

        final List<PolicyRuleSerializable> policyRules1 = new ArrayList<>();
        policyRules1.add(policy1);

        final List<JiraProject> jiraProjects2 = new ArrayList<>();
        jiraProjects2.add(jira2);

        final String jiraProjectsError2 = "jiraProjectsError2";

        final List<BlackDuckProject> blackDuckProjects2 = new ArrayList<>();
        blackDuckProjects2.add(blackDuck2);

        final String blackDuckProjectsError2 = "blackDuckProjectsError2";

        final Set<BlackDuckProjectMapping> mappings2 = new HashSet<>();
        mappings2.add(mapping2);

        final List<PolicyRuleSerializable> policyRules2 = new ArrayList<>();
        policyRules2.add(policy2);

        final SortedSet<String> creatorCandidates1 = new TreeSet<>();
        creatorCandidates1.add("user1c");
        creatorCandidates1.add("user1b");
        creatorCandidates1.add("user1a");
        creatorCandidates1.add("user1b");
        creatorCandidates1.add("user1b");
        creatorCandidates1.add("user1b");
        creatorCandidates1.add("user1b");

        final SortedSet<String> creatorCandidates2 = new TreeSet<>();
        creatorCandidates2.add("user2a");
        creatorCandidates2.add("user2b");
        creatorCandidates2.add("user2c");

        final BlackDuckJiraConfigSerializable item1 = new BlackDuckJiraConfigSerializable();
        item1.setErrorMessage(errorMessage1);
        item1.setHubProjectMappingError(blackDuckProjectMappingError1);
        item1.setHubProjectMappings(mappings1);
        item1.setJiraProjectsError(jiraProjectsError1);
        item1.setHubProjects(blackDuckProjects1);
        item1.setHubProjectsError(blackDuckProjectsError1);
        item1.setIntervalBetweenChecks(intervalBetweenChecks1);
        item1.setGeneralSettingsError(generalSettingsError1);
        item1.setJiraProjects(jiraProjects1);
        item1.setPolicyRules(policyRules1);
        item1.setPolicyRulesError(policyRulesError1);
        item1.setCreatorCandidates(creatorCandidates1);
        item1.setCreator(USER1);

        final BlackDuckJiraConfigSerializable item2 = new BlackDuckJiraConfigSerializable();
        item2.setErrorMessage(errorMessage2);
        item2.setHubProjectMappingError(blackDuckProjectMappingError2);
        item2.setJiraProjectsError(jiraProjectsError2);
        item2.setHubProjectMappings(mappings2);
        item2.setHubProjects(blackDuckProjects2);
        item2.setHubProjectsError(blackDuckProjectsError2);
        item2.setIntervalBetweenChecks(intervalBetweenChecks2);
        item2.setGeneralSettingsError(generalSettingsError2);
        item2.setJiraProjects(jiraProjects2);
        item2.setPolicyRules(policyRules2);
        item2.setPolicyRulesError(policyRulesError2);
        item2.setCreatorCandidates(creatorCandidates2);
        item2.setCreator(USER2);

        final Gson gson = new GsonBuilder().create();

        final String mappingJson = gson.toJson(mappings1);
        final String rulesJson = gson.toJson(policyRules1);

        final BlackDuckJiraConfigSerializable item3 = new BlackDuckJiraConfigSerializable();
        item3.setErrorMessage(errorMessage1);
        item3.setHubProjectMappingError(blackDuckProjectMappingError1);
        item3.setJiraProjectsError(jiraProjectsError1);
        item3.setHubProjectMappingsJson(mappingJson);
        item3.setHubProjects(blackDuckProjects1);
        item3.setHubProjectsError(blackDuckProjectsError1);
        item3.setIntervalBetweenChecks(intervalBetweenChecks1);
        item3.setGeneralSettingsError(generalSettingsError1);
        item3.setJiraProjects(jiraProjects1);
        item3.setPolicyRulesJson(rulesJson);
        item3.setPolicyRulesError(policyRulesError1);
        item3.setCreatorCandidates(creatorCandidates1);
        item3.setCreator(USER1);

        assertEquals(errorMessage1, item1.getErrorMessage());
        assertEquals(blackDuckProjectMappingError1, item1.getHubProjectMappingError());
        assertEquals(mappings1, item1.getHubProjectMappings());
        assertEquals(jiraProjectsError1, item1.getJiraProjectsError());
        assertEquals(blackDuckProjects1, item1.getHubProjects());
        assertEquals(blackDuckProjectsError1, item1.getHubProjectsError());
        assertEquals(intervalBetweenChecks1, item1.getIntervalBetweenChecks());
        assertEquals(generalSettingsError1, item1.getGeneralSettingsError());
        assertEquals(jiraProjects1, item1.getJiraProjects());
        assertEquals(policyRules1, item1.getPolicyRules());
        assertEquals(policyRulesError1, item1.getPolicyRulesError());

        assertEquals(errorMessage2, item2.getErrorMessage());
        assertEquals(blackDuckProjectMappingError2, item2.getHubProjectMappingError());
        assertEquals(mappings2, item2.getHubProjectMappings());
        assertEquals(jiraProjectsError2, item2.getJiraProjectsError());
        assertEquals(blackDuckProjects2, item2.getHubProjects());
        assertEquals(blackDuckProjectsError2, item2.getHubProjectsError());
        assertEquals(intervalBetweenChecks2, item2.getIntervalBetweenChecks());
        assertEquals(generalSettingsError2, item2.getGeneralSettingsError());
        assertEquals(jiraProjects2, item2.getJiraProjects());
        assertEquals(policyRules2, item2.getPolicyRules());
        assertEquals(policyRulesError2, item2.getPolicyRulesError());

        assertEquals(errorMessage1, item3.getErrorMessage());
        assertEquals(blackDuckProjectMappingError1, item3.getHubProjectMappingError());
        assertEquals(mappings1, item3.getHubProjectMappings());
        assertEquals(jiraProjectsError1, item3.getJiraProjectsError());
        assertEquals(mappingJson, item3.getHubProjectMappingsJson());
        assertEquals(blackDuckProjects1, item3.getHubProjects());
        assertEquals(blackDuckProjectsError1, item3.getHubProjectsError());
        assertEquals(intervalBetweenChecks1, item3.getIntervalBetweenChecks());
        assertEquals(generalSettingsError1, item3.getGeneralSettingsError());
        assertEquals(jiraProjects1, item3.getJiraProjects());
        assertEquals(policyRules1, item3.getPolicyRules());
        assertEquals(rulesJson, item3.getPolicyRulesJson());
        assertEquals(policyRulesError1, item3.getPolicyRulesError());

        assertTrue(!item1.equals(item2));
        assertTrue(item1.equals(item3));

        assertTrue(item1.hashCode() != item2.hashCode());
        assertEquals(item1.hashCode(), item3.hashCode());

        final StringBuilder builder = new StringBuilder();
        builder.append("BlackDuckJiraConfigSerializable [errorMessage=");
        builder.append(item1.getErrorMessage());
        builder.append(", intervalBetweenChecks=");
        builder.append(item1.getIntervalBetweenChecks());
        builder.append(", generalSettingsError=");
        builder.append(item1.getGeneralSettingsError());
        builder.append(", jiraProjects=");
        builder.append(item1.getJiraProjects());
        builder.append(", jiraProjectsError=");
        builder.append(item1.getJiraProjectsError());
        builder.append(", creatorCandidates=");
        builder.append(item1.getCreatorCandidates());
        builder.append(", creator=");
        builder.append(USER1);
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

    @Test
    public void testMessagesAllThree() {
        final BlackDuckJiraConfigSerializable config = new BlackDuckJiraConfigSerializable();
        config.setHubProjectMappingError("blackDuckProjectMappingError");
        config.setJiraProjectsError("jiraProjectsError");
        config.setHubProjectsError("blackDuckProjectsError");
        config.enhanceMappingErrorMessage();
        assertEquals("blackDuckProjectMappingError; JIRA Project Error: jiraProjectsError; Black Duck Project Error: blackDuckProjectsError", config.getHubProjectMappingError());
    }

    @Test
    public void testMessagesNulls() {
        final BlackDuckJiraConfigSerializable config = new BlackDuckJiraConfigSerializable();
        config.setHubProjectMappingError(null);
        config.setJiraProjectsError(null);
        config.setHubProjectsError(null);
        config.enhanceMappingErrorMessage();
        assertNull(config.getHubProjectMappingError());
    }

    @Test
    public void testMessagesEmpties() {
        final BlackDuckJiraConfigSerializable config = new BlackDuckJiraConfigSerializable();
        config.setHubProjectMappingError("");
        config.setJiraProjectsError("");
        config.setHubProjectsError("");
        config.enhanceMappingErrorMessage();
        assertEquals("", config.getHubProjectMappingError());
    }

    @Test
    public void testMessagesBlackDuckOnly() {
        final BlackDuckJiraConfigSerializable config = new BlackDuckJiraConfigSerializable();
        config.setHubProjectsError("BlackDuckProjectsError");
        config.enhanceMappingErrorMessage();
        assertEquals("Black Duck Project Error: BlackDuckProjectsError", config.getHubProjectMappingError());
    }

    @Test
    public void testMessagesJiraOnly() {
        final BlackDuckJiraConfigSerializable config = new BlackDuckJiraConfigSerializable();
        config.setJiraProjectsError("JiraProjectsError");
        config.enhanceMappingErrorMessage();
        assertEquals("JIRA Project Error: JiraProjectsError", config.getHubProjectMappingError());
    }

    @Test
    public void testMessagesMappingOnly() {
        final BlackDuckJiraConfigSerializable config = new BlackDuckJiraConfigSerializable();
        config.setHubProjectMappingError("mappingError");
        config.enhanceMappingErrorMessage();
        assertEquals("mappingError", config.getHubProjectMappingError());
    }

}
