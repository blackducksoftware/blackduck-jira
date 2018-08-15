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
package com.blackducksoftware.integration.jira.task.conversion.output.eventdata;

import java.util.Date;

import com.blackducksoftware.integration.jira.common.model.JiraProject;
import com.blackducksoftware.integration.jira.task.conversion.output.BlackDuckEventAction;

public class SpecialEventData extends EventData {

    private SpecialEventData() {
        super();
    }

    public static SpecialEventData create404EventData(final JiraProject jiraProject, final String bomComponentUri, final Date batchStartDate) {
        final SpecialEventData specialEventData = new SpecialEventData();
        // TODO Implement this
        specialEventData.setAction(BlackDuckEventAction.RESOLVE);

        specialEventData.setJiraIssueAssigneeUserId(jiraProject.getAssigneeUserId());
        specialEventData.setJiraProjectName(jiraProject.getProjectName());
        specialEventData.setJiraProjectId(jiraProject.getProjectId());

        specialEventData.setLastBatchStartDate(batchStartDate);

        // V3 Event Key

        // BOM Component URI
        specialEventData.setBlackDuckBomComponentUri(bomComponentUri);

        return specialEventData;
    }

}
