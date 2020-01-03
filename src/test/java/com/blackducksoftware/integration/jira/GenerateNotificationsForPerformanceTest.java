/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2020 Synopsys, Inc.
 * https://www.synopsys.com/
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
package com.blackducksoftware.integration.jira;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import com.synopsys.integration.bdio.model.Forge;
import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.blackduck.api.generated.component.ProjectRequest;
import com.synopsys.integration.blackduck.api.generated.component.ProjectVersionRequest;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionDistributionType;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionPhaseType;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.ProjectBomService;
import com.synopsys.integration.blackduck.service.ProjectService;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;

public class GenerateNotificationsForPerformanceTest {
    private static final List<ExternalId> VULNERABLE_COMPONENT_EXTERNAL_IDS = createVulnerableComponentExternalIds();

    public static void main(final String[] args) {
        final Scanner inputReader = new Scanner(System.in);
        final String url = readString(inputReader, "Black Duck Base URL: ");
        final String apiToken = readString(inputReader, "Black Duck API Token: ");
        final String projectName = readString(inputReader, "Black Duck Project Name: ");
        final String numberOfVersionsString = readString(inputReader, "Number of versions to generate for " + projectName + ": ");

        System.out.println();
        System.out.println("URL: " + url);
        System.out.println("API Token: " + apiToken);
        System.out.println("Project Name: " + projectName);
        System.out.println("Number of versions: " + numberOfVersionsString);
        System.out.println();

        final IntLogger intLogger = new PrintStreamIntLogger(System.out, LogLevel.INFO);
        final BlackDuckServerConfigBuilder serverConfigBuilder = BlackDuckServerConfig.newBuilder();
        serverConfigBuilder.setUrl(url);
        serverConfigBuilder.setApiToken(apiToken);
        serverConfigBuilder.setTimeoutInSeconds(30);
        serverConfigBuilder.setTrustCert(true);
        serverConfigBuilder.setLogger(intLogger);

        final BlackDuckServerConfig serverConfig = serverConfigBuilder.build();
        final BlackDuckServicesFactory blackDuckServicesFactory = serverConfig.createBlackDuckServicesFactory(intLogger);

        final ProjectService blackDuckProjectService = blackDuckServicesFactory.createProjectService();
        final ProjectBomService projectBomService = blackDuckServicesFactory.createProjectBomService();

        try {
            final Integer numberOfVersionToCreate = Integer.parseInt(numberOfVersionsString);
            final ProjectView blackDuckProject = getOrCreateProject(blackDuckProjectService, projectName);
            final List<ProjectVersionView> blackDuckProjectVersions = createProjectVersions(blackDuckProjectService, blackDuckProject, numberOfVersionToCreate);
            addVulnerableComponentsToBom(projectBomService, blackDuckProjectVersions);

            System.out.println();
            System.out.println("Completed Successfully!");
        } catch (final Exception e) {
            System.out.println();
            System.out.println("Generation failed :(");
            e.printStackTrace();
        } finally {
            inputReader.close();
        }
    }

    private static String readString(final Scanner inputReader, final String prompt) {
        System.out.println();
        System.out.print(prompt);

        return inputReader.nextLine();
    }

    private static ProjectView getOrCreateProject(final ProjectService blackDuckProjectService, final String projectName) throws IntegrationException {
        final Optional<ProjectView> projectByName = blackDuckProjectService.getProjectByName(projectName);
        if (projectByName.isPresent()) {
            System.out.println("Found project: " + projectName);
            return projectByName.get();
        }

        final ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setName(projectName);
        projectRequest.setDescription("Auto generated through jira performance test utility");
        final ProjectVersionWrapper projectVersionWrapper = blackDuckProjectService.createProject(projectRequest);

        System.out.println("Attempting to create project: " + projectName);
        return projectVersionWrapper.getProjectView();
    }

    private static List<ProjectVersionView> createProjectVersions(
        final ProjectService blackDuckProjectService, final ProjectView projectView, final Integer numberOfVersions) throws IntegrationException {
        final Date currentDate = new Date();
        final Long timeFromDate = currentDate.getTime();
        final String timeString = timeFromDate.toString();

        final List<ProjectVersionView> newVersions = new LinkedList<>();
        for (int i = 0; i < numberOfVersions; i++) {
            final String versionName = timeString + "-" + i;
            final ProjectVersionRequest projectVersionRequest = new ProjectVersionRequest();
            projectVersionRequest.setVersionName(versionName);
            projectVersionRequest.setNickname("Generated Version " + i);
            projectVersionRequest.setPhase(ProjectVersionPhaseType.DEVELOPMENT);
            projectVersionRequest.setDistribution(ProjectVersionDistributionType.INTERNAL);

            System.out.println("Attempting to create project-version: " + versionName);
            final ProjectVersionView newProjectVersion = blackDuckProjectService.createProjectVersion(projectView, projectVersionRequest);
            newVersions.add(newProjectVersion);
        }
        return newVersions;
    }

    private static void addVulnerableComponentsToBom(final ProjectBomService projectBomService, final List<ProjectVersionView> projectVersions) throws IntegrationException {
        System.out.println("External ids to add: " + VULNERABLE_COMPONENT_EXTERNAL_IDS);
        for (final ProjectVersionView projectVersion : projectVersions) {
            System.out.println("Adding external ids to project-versions: " + projectVersion.getVersionName());
            for (final ExternalId externalId : VULNERABLE_COMPONENT_EXTERNAL_IDS) {
                projectBomService.addComponentToProjectVersion(externalId, projectVersion);
            }
        }
    }

    private static List<ExternalId> createVulnerableComponentExternalIds() {
        final ExternalId externalId1 = new ExternalId(Forge.MAVEN);
        externalId1.group = "commons-fileupload";
        externalId1.name = "commons-fileupload";
        externalId1.version = "1.2.1";

        final ExternalId externalId2 = new ExternalId(Forge.MAVEN);
        externalId2.group = "org.apache.struts";
        externalId2.name = "struts-core";
        externalId2.version = "1.3.5";

        final ExternalId externalId3 = new ExternalId(Forge.MAVEN);
        externalId3.group = "org.apache.struts";
        externalId3.name = "struts-core";
        externalId3.version = "1.3.8";

        final ExternalId externalId4 = new ExternalId(Forge.MAVEN);
        externalId4.group = "org.apache.struts";
        externalId4.name = "struts-core";
        externalId4.version = "1.3.9";

        final ExternalId externalId5 = new ExternalId(Forge.MAVEN);
        externalId5.group = "org.apache.struts";
        externalId5.name = "struts-core";
        externalId5.version = "1.3.10";

        final ExternalId externalId6 = new ExternalId(Forge.COCOAPODS);
        externalId6.name = "openssl-static-library";
        externalId6.version = "1.0";

        final ExternalId externalId7 = new ExternalId(Forge.MAVEN);
        externalId7.group = "org.apache.tomcat";
        externalId7.name = "tomcat-util";
        externalId7.version = "7.0.0";

        final ExternalId externalId8 = new ExternalId(Forge.MAVEN);
        externalId8.group = "org.springframework.data";
        externalId8.name = "spring-data-jpa";
        externalId8.version = "2.1.0.RELEASE";

        return Arrays.asList(externalId1, externalId2, externalId3, externalId4, externalId5, externalId6, externalId7, externalId8);
    }

}
