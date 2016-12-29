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
package com.blackducksoftware.integration.jira.task.conversion.output;

import com.blackducksoftware.integration.hub.dataservice.notification.item.PolicyContentItem;

public class PolicyIssuePropertiesGenerator implements IssuePropertiesGenerator {
    private final String projectName;

    private final String projectVersion;

    private final String componentName;

    private final String componentVersion;

    private final String ruleName;

    public PolicyIssuePropertiesGenerator(final PolicyContentItem notifContentItem,
            final String ruleName) {
        this.projectName = notifContentItem.getProjectVersion().getProjectName();
        this.projectVersion = notifContentItem.getProjectVersion().getProjectVersionName();
        this.componentName = notifContentItem.getComponentName();
        this.componentVersion = notifContentItem.getComponentVersion();
        this.ruleName = ruleName;
    }

    @Override
    public IssueProperties createIssueProperties(final Long issueId) {
        final IssueProperties properties = new PolicyViolationIssueProperties(
                projectName,
                projectVersion,
                componentName, componentVersion,
                issueId, ruleName);
        return properties;
    }

}
