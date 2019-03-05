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
        url: createRequestPath('config/issue/creator/'),
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
        url: createRequestPath('config/issue/creator/candidates/'),
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

function readJiraProjects() {
    AJS.$.ajax({
        url: createRequestPath('config/issue/creator/jira/projects/'),
        dataType: "json",
        success: function (config) {
            fillInJiraProjects(config.jiraProjects);
            handleError(jiraProjectListErrorId, config.jiraProjectsError, false, false);
            handleError(errorMessageFieldId, config.errorMessage, true, false);

            gotJiraProjects = true;
        },
        error: function (response) {
            handleDataRetrievalError(response, jiraProjectListErrorId, "There was a problem retrieving the JIRA Projects.", "JIRA Project Error");
        },
        complete: function (jqXHR, textStatus) {
            console.log("Completed get of JIRA projects: " + textStatus);
        }
    });
}

function readCreateVulnerabilityTicketsChoice() {
    AJS.$.ajax({
        url: createRequestPath('config/issue/creator/vulnerability/ticketchoice/'),
        dataType: "json",
        success: function (config) {
            console.log("success: get of ticketsChoice", config);
            setCreateVulnerabilityIssuesChoice(config.createVulnerabilityIssues);

//	      handleError(errorMessageFieldId, config.errorMessage, true, false);
            handleError('createVulnerabilityIssuesChoiceError', config.createVulnerabilityIssuesError, true, false);
            console.log("Finished handling ticketsChoice");
        },
        error: function (response) {
            console.log("error: get of ticketsChoice");
            handleDataRetrievalError(response, "createVulnerabilityIssuesError", "There was a problem retrieving the 'create vulnerability issues' choice.", "Black Duck Create Vulnerability Issues Choice Error");
        },
        complete: function (jqXHR, textStatus) {
            console.log("Completed get of ticketsChoice: " + textStatus);
        }
    });
}

function readCommentOnUpdateChoice() {
    AJS.$.ajax({
        url: createRequestPath('config/issue/creator/comment/updatechoice'),
        dataType: "json",
        success: function (config) {
            console.log("success: get of commentOnIssueUpdatesChoice", config);
            setCommentOnIssueUpdatesChoice(config.commentOnIssueUpdatesChoice);

//	      handleError(errorMessageFieldId, config.errorMessage, true, false);
            handleError('createVulnerabilityIssuesChoiceError', config.commentOnIssueUpdatesChoiceError, true, false);
            console.log("Finished handling commentOnIssueUpdatesChoice");
        },
        error: function (response) {
            console.log("error: get of commentOnIssueUpdatesChoice");
            handleDataRetrievalError(response, "commentOnIssueUpdatesChoiceError", "There was a problem retrieving the 'comment on issue updates' choice.", "Black Duck Comment On Issue Updates Choice Error");
        },
        complete: function (jqXHR, textStatus) {
            console.log("Completed get of commentOnIssueUpdatesChoice: " + textStatus);
        }
    });
}

function readIntervalData() {
    AJS.$.ajax({
        url: createRequestPath('config/issue/creator/interval/'),
        dataType: "json",
        success: function (config) {
            updateValue("intervalBetweenChecks", config.intervalBetweenChecks);
            handleError('generalSettingsError', config.generalSettingsError, true, false);
        },
        error: function (response) {
            handleDataRetrievalError(response, "generalSettingsError", "There was a problem retrieving the Interval.", "Interval Error");
        },
        complete: function (jqXHR, textStatus) {
            console.log("Completed get of interval: " + textStatus);
        }
    });
}

function updateConfig() {
    putConfig(createRequestPath('config/issue/creator/'), 'Save successful.', 'The configuration is not valid.');
}

function putConfig(restUrl, successMessage, failureMessage) {
    const creatorUsername = encodeURI(AJS.$("#creatorInput").val());
    console.log("putConfig(): " + creatorUsername);
    const jsonMappingArray = getJsonArrayFromMapping();
    const policyRuleArray = getJsonArrayFromPolicyRules();
    const createVulnerabilityIssues = getCreateVulnerabilityIssuesChoice();
    const commentOnIssueUpdatesChoice = getCommentOnIssueUpdatesChoice();
    const requestData = Object.assign({}, {
        intervalBetweenChecks: encodeURI(AJS.$("#intervalBetweenChecks").val()),
        creator: creatorUsername,
        hubProjectMappings: jsonMappingArray,
        policyRules: policyRuleArray,
        createVulnerabilityIssues: createVulnerabilityIssues,
        commentOnIssueUpdatesChoice: commentOnIssueUpdatesChoice

    });
    AJS.$.ajax({
        url: restUrl,
        type: "PUT",
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify(requestData),
        processData: false,
        success: function () {
            hideError(errorMessageFieldId);
            hideError('generalSettingsError');
            hideError('hubProjectMappingsError');
            hideError('policyRulesError');

            showStatusMessage(successStatus, 'Success!', successMessage);
        },
        error: function (response) {
            try {
                if (!redirectIfUnauthenticated(response)) {
                    let config = JSON.parse(response.responseText);
                    handleError(errorMessageFieldId, config.errorMessage, true, true);
                    handleError('generalSettingsError', config.generalSettingsError, true, true);
                    handleError('hubProjectMappingsError', config.hubProjectMappingError, true, true);
                    handleError('policyRulesError', config.policyRulesError, true, true);

                    showStatusMessage(errorStatus, 'ERROR!', failureMessage);

                    console.log("errorMessage: " + config.errorMessage); // x
                    console.log("hubProjectMappingError: " + config.hubProjectMappingError);
                    console.log("hubProjectsError: " + config.hubProjectsError);
                    console.log("generalSettingsError: " + config.generalSettingsError);
                    console.log("jiraProjectsError: " + config.jiraProjectsError);
                    console.log("policyRulesError: " + config.policyRulesError);
                }
            } catch (err) {
                // in case the response is not our error object
                alert(response.responseText);
            }
        },
        complete: function (jqXHR, textStatus) {
            stopProgressSpinner('saveSpinner');
        }
    });
}
