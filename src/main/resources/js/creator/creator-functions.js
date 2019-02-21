/*
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
function readCreatorData() {
    AJS.$.ajax({
        url: createRequestPath('creator/'),
        dataType: "json",
        success: function (config) {
            updateValue("creatorInput", config.creator);

            handleError('generalSettingsError', config.generalSettingsError, true, false);
        },
        error: function (response) {
            handleDataRetrievalError(response, "generalSettingsError", "There was a problem retrieving the Creator.", "Creator Error");
        },
        complete: function (jqXHR, textStatus) {
            console.log("Completed get of creator: " + textStatus);
        }
    });
}

function initCreatorCandidates() {
    console.log("Initializing issue creator candidate list");
    AJS.$.ajax({
        url: createRequestPath('creatorCandidates/'),
        dataType: "json",
        success: function (config) {
            fillInCreatorCandidates(config.creatorCandidates);
            handleError('generalSettingsError', config.generalSettingsError, true, false);
            gotCreatorCandidates = true;
        },
        error: function (response) {
            console.log("Error getting creator candidates");
            handleDataRetrievalError(response, "generalSettingsError", "There was a problem retrieving the issue creator candidates list.", "Creator Candidates Error");
        },
        complete: function (jqXHR, textStatus) {
            console.log("Completed get of Creator Candidates: " + textStatus);
        }
    });
}

function fillInCreatorCandidates(creatorCandidates) {
    console.log("fillInCreatorCandidates()");
    for (let i = 0; i < creatorCandidates.length; i++) {
        console.log("Creator candidate: " + creatorCandidates[i]);
    }

    const creatorElement = AJS.$("#" + "creatorCell");
    const creatorCandidatesList = creatorElement.find("datalist[id='" + "creatorCandidates" + "']");
    console.log("fillInCreatorCandidates() List: " + creatorCandidatesList);
    if (creatorCandidatesList.length > 0) {
        console.log("fillInCreatorCandidates(): removing option");
        clearList(creatorCandidatesList[0]);
    }
    if (creatorCandidates != null && creatorCandidates.length > 0) {
        for (let j = 0; j < creatorCandidates.length; j++) {
            console.log("Adding creator candidate: " + creatorCandidates[j]);
//			jiraProjectMap.set(String(jiraProjects[j].projectId), jiraProjects[j]);
            let newOption = AJS.$('<option>', {
                value: creatorCandidates[j],
                id: creatorCandidates[j]
            });
            creatorCandidatesList.append(newOption);
        }
    }
}
