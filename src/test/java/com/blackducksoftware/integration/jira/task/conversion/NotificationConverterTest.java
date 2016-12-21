/*
 * Copyright (C) 2016 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.jira.task.conversion;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.user.ApplicationUser;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.notification.VulnerabilitySourceQualifiedId;
import com.blackducksoftware.integration.hub.api.project.ProjectVersion;
import com.blackducksoftware.integration.hub.api.vulnerablebomcomponent.VulnerableBomComponentRequestService;
import com.blackducksoftware.integration.hub.dataservice.notification.item.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.item.VulnerabilityContentItem;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.service.HubRequestService;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.jira.common.HubProject;
import com.blackducksoftware.integration.jira.common.HubProjectMapping;
import com.blackducksoftware.integration.jira.common.HubProjectMappings;
import com.blackducksoftware.integration.jira.common.JiraContext;
import com.blackducksoftware.integration.jira.common.JiraProject;
import com.blackducksoftware.integration.jira.common.exception.ConfigurationException;
import com.blackducksoftware.integration.jira.config.HubJiraFieldCopyConfigSerializable;
import com.blackducksoftware.integration.jira.config.ProjectFieldCopyMapping;
import com.blackducksoftware.integration.jira.task.JiraSettingsService;
import com.blackducksoftware.integration.jira.task.conversion.output.HubEvent;
import com.blackducksoftware.integration.jira.task.conversion.output.HubEventAction;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;

public class NotificationConverterTest {
    private static final String TARGET_FIELD_NAME = "targetFieldName";

    private static final String TARGET_FIELD_ID = "targetFieldId";

    private static final String SOURCE_FIELD_NAME = "sourceFieldName";

    private static final String SOURCE_FIELD_ID = "sourceFieldId";

    private static final String WILDCARD_STRING = "*";

    private static final String HUB_PROJECT_URL = "hubProjectUrl";

    private static final String PROJECT_VERSION_NAME = "projectVersionName";

    private static final String PROJECT_VERSION_URL = "http://int-hub01.dc1.lan:8080/api/projects/projectId/versions/versionId";

    private static final String COMPONENT_VERSION_URL = "http://int-hub01.dc1.lan:8080/api/components/componentId/versions/versionId";

    private static final String COMPONENT_VERSION = "componentVersion";

    private static final String COMPONENT_NAME = "componentName";

    private static final String ASSIGNEE_USER_ID = "assigneeUserId";

    private static final String HUB_PROJECT_NAME = "hubProjectName";

    private static final long JIRA_PROJECT_ID = 123L;

    private static final int EXPECTED_EVENT_COUNT = 1;

    private static final String JIRA_USER_NAME = "jiraUserName";

    private static final String JIRA_USER_KEY = "jiraUserKey";

    private static final String JIRA_PROJECT_NAME = "jiraProjectName";

    private static final String HUB_SECURITY_VULNERABILITY_ID = "Hub Security Vulnerability ID";

    private static final String VULN_SOURCE = "NVD";

    private static final String EXPECTED_PROPERTY_KEY = "t=v|jp=123|hpv=-32224582|hc=|hcv=1816144506";

    private static final String EXPECTED_RESOLVED_COMMENT = "Automatically resolved; the Black Duck Hub reports no remaining vulnerabilities on this project from this component";

    private static final String EXPECTED_REOPEN_COMMENT = "Automatically re-opened in response to new Black Duck Hub vulnerabilities on this project from this component";

    private final static String EXPECTED_COMMENT = "(Black Duck Hub JIRA plugin-generated comment)\n" +
            "Vulnerabilities added: http://int-hub01.dc1.lan:8080/api/components/componentId/versions/versionId (NVD)\n" +
            "Vulnerabilities updated: None\n" +
            "Vulnerabilities deleted: None\n";

    private final static String EXPECTED_DESCRIPTION = "This issue tracks vulnerability status changes on " +
            "Hub project 'hubProjectName' / 'projectVersionName', component 'componentName' / 'componentVersion'. " +
            "For details, see the comments below, or the project's vulnerabilities view in the Hub:\n" +
            "<error getting vulnerable components URL>";

    private final static String EXPECTED_SUMMARY = "Black Duck vulnerability status changes on Hub project " +
            "'hubProjectName' / 'projectVersionName', component 'componentName' / 'componentVersion'";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void testVulnerability() throws ConfigurationException, HubIntegrationException, URISyntaxException {
        final JiraServices jiraServices = Mockito.mock(JiraServices.class);
        final Set<HubProjectMapping> mappings = new HashSet<>();
        HubProjectMapping mapping = new HubProjectMapping();
        HubProject hubProject = new HubProject();
        hubProject.setProjectName(HUB_PROJECT_NAME);
        hubProject.setProjectUrl(HUB_PROJECT_URL);
        mapping.setHubProject(hubProject);
        JiraProject jiraProject = new JiraProject();
        jiraProject.setProjectName(JIRA_PROJECT_NAME);
        jiraProject.setProjectId(JIRA_PROJECT_ID);
        jiraProject.setAssigneeUserId(ASSIGNEE_USER_ID);
        mapping.setJiraProject(jiraProject);
        mappings.add(mapping);
        final HubProjectMappings mappingObject = new HubProjectMappings(jiraServices,
                mappings);
        final HubJiraFieldCopyConfigSerializable fieldCopyConfig = new HubJiraFieldCopyConfigSerializable();
        Set<ProjectFieldCopyMapping> projectFieldCopyMappings = new HashSet<>();
        ProjectFieldCopyMapping projectFieldCopyMapping = new ProjectFieldCopyMapping();
        projectFieldCopyMapping.setHubProjectName(WILDCARD_STRING);
        projectFieldCopyMapping.setJiraProjectName(WILDCARD_STRING);
        projectFieldCopyMapping.setSourceFieldId(SOURCE_FIELD_ID);
        projectFieldCopyMapping.setSourceFieldName(SOURCE_FIELD_NAME);
        projectFieldCopyMapping.setTargetFieldId(TARGET_FIELD_ID);
        projectFieldCopyMapping.setTargetFieldName(TARGET_FIELD_NAME);
        projectFieldCopyMappings.add(projectFieldCopyMapping);
        fieldCopyConfig.setProjectFieldCopyMappings(projectFieldCopyMappings);

        ConstantsManager constantsManager = Mockito.mock(ConstantsManager.class);
        List<IssueType> issueTypes = new ArrayList<>();
        IssueType issueType = Mockito.mock(IssueType.class);
        Mockito.when(issueType.getName()).thenReturn("Hub Security Vulnerability");
        Mockito.when(issueType.getId()).thenReturn(HUB_SECURITY_VULNERABILITY_ID);
        issueTypes.add(issueType);
        Mockito.when(constantsManager.getAllIssueTypeObjects()).thenReturn(issueTypes);
        Mockito.when(jiraServices.getConstantsManager()).thenReturn(constantsManager);
        Mockito.when(jiraServices.getJiraProject(JIRA_PROJECT_ID)).thenReturn(jiraProject);

        ApplicationUser jiraUser = Mockito.mock(ApplicationUser.class);
        Mockito.when(jiraUser.getName()).thenReturn(JIRA_USER_NAME);
        Mockito.when(jiraUser.getKey()).thenReturn(JIRA_USER_KEY);
        final JiraContext jiraContext = new JiraContext(jiraUser);
        final JiraSettingsService jiraSettingsService = null;
        final HubServicesFactory hubServicesFactory = Mockito.mock(HubServicesFactory.class);
        VulnerableBomComponentRequestService vulnBomCompReqSvc = Mockito.mock(VulnerableBomComponentRequestService.class);
        HubRequestService hubRequestService = Mockito.mock(HubRequestService.class);
        Mockito.when(hubServicesFactory.createHubRequestService()).thenReturn(hubRequestService);
        Mockito.when(hubServicesFactory.createVulnerableBomComponentRequestService()).thenReturn(vulnBomCompReqSvc);
        final MetaService metaService = null;

        VulnerabilityNotificationConverter conv = new VulnerabilityNotificationConverter(
                mappingObject,
                fieldCopyConfig,
                jiraServices,
                jiraContext, jiraSettingsService,
                hubServicesFactory, metaService);

        Date now = new Date();
        final Date createdAt = now;
        final ProjectVersion projectVersion = new ProjectVersion();
        projectVersion.setProjectName(HUB_PROJECT_NAME);
        projectVersion.setProjectVersionName(PROJECT_VERSION_NAME);
        projectVersion.setUrl(PROJECT_VERSION_URL);
        final String componentName = COMPONENT_NAME;
        final String componentVersion = COMPONENT_VERSION;
        final String componentVersionUrl = COMPONENT_VERSION_URL;

        VulnerabilitySourceQualifiedId vuln = new VulnerabilitySourceQualifiedId(VULN_SOURCE, componentVersionUrl);
        final List<VulnerabilitySourceQualifiedId> addedVulnList = new ArrayList<>();
        final List<VulnerabilitySourceQualifiedId> updatedVulnList = new ArrayList<>();
        final List<VulnerabilitySourceQualifiedId> deletedVulnList = new ArrayList<>();
        addedVulnList.add(vuln);
        NotificationContentItem notif = new VulnerabilityContentItem(createdAt, projectVersion,
                componentName,
                componentVersion,
                componentVersionUrl,
                addedVulnList,
                updatedVulnList,
                deletedVulnList);
        List<HubEvent> events = conv.generateEvents(notif);

        assertEquals(EXPECTED_EVENT_COUNT, events.size());
        HubEvent<VulnerabilityContentItem> event = events.get(0);
        assertEquals(HubEventAction.ADD_COMMENT, event.getAction());
        assertEquals(EXPECTED_COMMENT, event.getComment());
        assertEquals(EXPECTED_COMMENT, event.getCommentIfExists());
        assertEquals(EXPECTED_COMMENT, event.getCommentInLieuOfStateChange());
        assertEquals(ASSIGNEE_USER_ID, event.getIssueAssigneeId());
        assertEquals(EXPECTED_DESCRIPTION, event.getIssueDescription());
        assertEquals(EXPECTED_SUMMARY, event.getIssueSummary());
        assertEquals(HUB_SECURITY_VULNERABILITY_ID, event.getJiraIssueTypeId());

        assertEquals(Long.valueOf(JIRA_PROJECT_ID), event.getJiraProjectId());
        assertEquals(JIRA_PROJECT_NAME, event.getJiraProjectName());
        assertEquals(JIRA_USER_KEY, event.getJiraUserId());
        assertEquals(JIRA_USER_NAME, event.getJiraUserName());
        VulnerabilityContentItem vulnContentItem = event.getNotif();
        assertEquals(VULN_SOURCE, vulnContentItem.getAddedVulnList().get(0).getSource());
        assertEquals(1, event.getProjectFieldCopyMappings().size());
        Iterator<ProjectFieldCopyMapping> iter = event.getProjectFieldCopyMappings().iterator();
        ProjectFieldCopyMapping actualProjectFieldCopyMapping = iter.next();
        assertEquals(WILDCARD_STRING, actualProjectFieldCopyMapping.getHubProjectName());
        assertEquals(WILDCARD_STRING, actualProjectFieldCopyMapping.getJiraProjectName());
        assertEquals(SOURCE_FIELD_ID, actualProjectFieldCopyMapping.getSourceFieldId());
        assertEquals(SOURCE_FIELD_NAME, actualProjectFieldCopyMapping.getSourceFieldName());
        assertEquals(TARGET_FIELD_ID, actualProjectFieldCopyMapping.getTargetFieldId());
        assertEquals(TARGET_FIELD_NAME, actualProjectFieldCopyMapping.getTargetFieldName());

        assertEquals(EXPECTED_REOPEN_COMMENT, event.getReopenComment());
        assertEquals(EXPECTED_RESOLVED_COMMENT, event.getResolveComment());
        assertEquals(EXPECTED_PROPERTY_KEY, event.getUniquePropertyKey());
    }

}
