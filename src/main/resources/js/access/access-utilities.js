/*
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
function fillInJiraGroups(hubJiraGroups, jiraGroups) {
    let splitHubJiraGroups = null;
    if (hubJiraGroups != null) {
        splitHubJiraGroups = hubJiraGroups.split(",");
    }
    const jiraGroupList = AJS.$("#" + hubJiraGroupsId);
    if (jiraGroups != null && jiraGroups.length > 0) {
        for (let j = 0; j < jiraGroups.length; j++) {
            let optionSelected = false;
            if (splitHubJiraGroups != null) {
                for (let g = 0; g < splitHubJiraGroups.length; g++) {
                    if (splitHubJiraGroups[g] === jiraGroups[j]) {
                        optionSelected = true;
                    }
                }
            }

            let newOption = AJS.$('<option>', {
                value: jiraGroups[j],
                text: jiraGroups[j],
                selected: optionSelected
            });

            jiraGroupList.append(newOption);
        }
    } else if (splitHubJiraGroups != null) {
        for (let j = 0; j < splitHubJiraGroups.length; j++) {
            let newOption = AJS.$('<option>', {
                value: splitHubJiraGroups[j],
                text: splitHubJiraGroups[j],
                selected: true
            });

            jiraGroupList.append(newOption);
        }
    }
    jiraGroupList.auiSelect2();
}


