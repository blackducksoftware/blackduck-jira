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
let ticketCreationErrorCounter = 0;
let mappingElementCounter = 0;

let gotCreatorCandidates = false;
let gotJiraProjects = false;
let gotHubProjects = false;
let gotProjectMappings = false;
let gotSourceFields = false;
let gotTargetFields = false;
let gotFieldCopyMappings = false;

let jiraProjectMap = new Map();
let hubProjectMap = new Map();

function openTab() {
    resetStatusMessage();
}

function updateConfig() {
    putConfig(createRequestPath(''), 'Save successful.', 'The configuration is not valid.');
}

function populateForm() {
    readAdminData();
    readBlackduckServerData();
    readIntervalData();
    readCreatorData();
    initCreatorCandidates();
    readJiraProjects();
    readSourceFields();
    readTargetFields();
    readFieldCopyMappings();
    readBlackduckTicketCreationErrors();

    AJS.$.ajax({
        url: createRequestPath('pluginInfo/'),
        dataType: "text",
        success: function (pluginVersion) {
            console.log("pluginVersion: " + pluginVersion);
            fillInPluginVersion(pluginVersion);
        },
        error: function (response) {
            console.log("Error getting pluginInfo");
            console.log("Response text: " + response.responseText);
            fillInPluginVersion("(error)");
        },
        complete: function (jqXHR, textStatus) {
            console.log("Completed get of pluginInfo: " + textStatus);
        }
    });

    populateFormBlackduckData();
    console.log("populateForm() Finished");
}


function populateFormBlackduckData() {
    console.log("populateFormBlackduckData()");
    gotHubProjects = false;
    gotProjectMappings = false;

    readBlackduckProjectData();
    readBlackduckPolicyData();

    AJS.$.ajax({
        url: createRequestPath('createVulnerabilityTicketsChoice/'),
        dataType: "json",
        success: function (config) {
            console.log("success: get of ticketsChoice");
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
    AJS.$.ajax({
        url: createRequestPath('commentOnIssueUpdatesChoice/'),
        dataType: "json",
        success: function (config) {
            console.log("success: get of commentOnIssueUpdatesChoice");
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
    readMappingData();
}


function resetSalKeys() {
    const restUrl = createRequestPath('reset');
    AJS.$.ajax({
        url: restUrl,
        type: "PUT",
        dataType: "json",
        contentType: "application/json",
        data: '{}',
        processData: false,
        success: function () {
            alert('Black Duck JIRA keys reset!');
        },
        error: function (response) {
            alert(response.responseText);
        },
        complete: function (jqXHR, textStatus) {
            stopProgressSpinner('resetSpinner');
        }
    });

    var ticketCreationErrorTable = AJS.$('#' + ticketCreationErrorsTableId);
    ticketCreationErrorTable.empty();
}

function handleErrorResize(expansionIcon) {
    const currentIcon = AJS.$(expansionIcon);
    const errorRow = currentIcon.closest("tr");
    const errorColumn = errorRow.find('td');
    const errorMessageDiv = AJS.$(errorColumn).children("div[name*='ticketCreationErrorMessageName']");
    const stackTraceDiv = AJS.$(errorColumn).children("div[name*='ticketCreationStackTraceName']");

    if (currentIcon.hasClass('fa-plus-square-o')) {
        currentIcon.removeClass('fa-plus-square-o');
        currentIcon.addClass('fa-minus-square-o');
        if (!errorMessageDiv.hasClass(hiddenClass)) {
            errorMessageDiv.addClass(hiddenClass);
        }
        if (stackTraceDiv.hasClass(hiddenClass)) {
            stackTraceDiv.removeClass(hiddenClass);
        }
    } else if (currentIcon.hasClass('fa-minus-square-o')) {
        currentIcon.removeClass('fa-minus-square-o');
        currentIcon.addClass('fa-plus-square-o');
        if (errorMessageDiv.hasClass(hiddenClass)) {
            errorMessageDiv.removeClass(hiddenClass);
        }
        if (!stackTraceDiv.hasClass(hiddenClass)) {
            stackTraceDiv.addClass(hiddenClass);
        }
    }
}

function getJsonArrayFromErrors(errorRow) {
    let jsonArray = [];

    const errorColumn = AJS.$(errorRow).find('td');

    const creationErrorMessage = AJS.$(errorColumn).children("div[name*='ticketCreationErrorMessageName']");

    const creationErrorStackTrace = AJS.$(errorColumn).children("div[name*='ticketCreationStackTraceName']");

    const creationErrorTimeStamp = AJS.$(errorColumn).children("div[name*='ticketCreationTimeStampName']");

    let stackTrace = creationErrorStackTrace.text().trim();
    if (stackTrace) {
        stackTrace = encodeURIComponent(stackTrace);
    } else {
        const errorMessage = creationErrorMessage.text().trim();

        stackTrace = encodeURIComponent(errorMessage);
    }
    let timeStamp = creationErrorTimeStamp.text().trim();
    timeStamp = encodeURIComponent(timeStamp);

    jsonArray.push({
        stackTrace: stackTrace,
        timeStamp: timeStamp
    });
    return jsonArray;
}

function handleErrorRemoval(trashIcon) {
    const currentIcon = AJS.$(trashIcon);
    const errorRow = currentIcon.closest("tr");

    const restUrl = createRequestPath('/removeErrors');

    const hubJiraTicketErrors = getJsonArrayFromErrors(errorRow);
    AJS.$.ajax({
        url: restUrl,
        type: "PUT",
        dataType: "json",
        contentType: "application/json",
        data: '{ "hubJiraTicketErrors": ' + hubJiraTicketErrors
            + '}',
        processData: false,
        success: function () {
            alert('Error successfully removed');
        },
        error: function (response) {
            try {
                var creationErrorObj = JSON.parse(response.responseText);
                alert(creationErrorObj.configError);
            } catch (err) {
                // in case the response is not our error object
                alert("Unexpected format of response while removing error message");
                console.log("Unexpected format of response while removing error message: " + response.responseText);
            }
        }
    });

    errorRow.remove();

    const ticketCreationErrorContainer = AJS.$("#" + ticketCreationErrorsTableId);
    const creationErrors = ticketCreationErrorContainer.find("tr[name*='" + ticketCreationErrorRowId + "']");
    if (creationErrors.length <= 1) {
        const fieldSet = AJS.$('#' + ticketCreationFieldSetId);
        if (!fieldSet.hasClass(hiddenClass)) {
            fieldSet.addClass(hiddenClass);
        }
    }
}


AJS.$(document).ajaxComplete(function (event, xhr, settings) {
    console.log("ajaxComplete()");
    if (gotJiraProjects && gotHubProjects && gotProjectMappings && gotCreatorCandidates) {
        console.log("ajaxComplete(): data is ready");
        const mappingContainer = AJS.$("#" + hubProjectMappingContainer);
        const mappingElements = mappingContainer.find("tr[name*='" + hubProjectMappingElement + "']");

        if (mappingElements.length > 0) {
            for (let m = 0; m < mappingElements.length; m++) {
                let currentJiraProject = AJS.$(mappingElements[m]).find("input[name*='jiraProject']");
                let jiraProjectError = true;
                if (currentJiraProject != null) {
                    let key = String(currentJiraProject.attr("projectkey"));
                    console.log("ajaxComplete(): jira project key: " + key);
                    if (key) {
                        let jiraProject = jiraProjectMap.get(key);
                        if (jiraProject) {
                            console.log("ajaxComplete(): jiraProject.projectError: " + jiraProject.projectError);
                            let jiraProjectMappingParent = currentJiraProject.parent();
                            let jiraProjectMappingError = jiraProjectMappingParent.children("#" + jiraProjectErrorId);
                            if (jiraProject.projectError) {
                                jiraProjectError = true;
                                jiraProjectMappingError.text(jiraProject.projectError.trim());
                                if (!jiraProjectMappingError.hasClass('error')) {
                                    jiraProjectMappingError.addClass('error');
                                }
                            } else {
                                jiraProjectError = false;
                                if (jiraProjectMappingError.hasClass('error')) {
                                    jiraProjectMappingError.removeClass('error');
                                }
                            }
                        }
                    }
                }

                if (jiraProjectError) {
                    if (!currentJiraProject.hasClass('error')) {
                        currentJiraProject.addClass('error');
                    }
                } else {
                    if (currentJiraProject.hasClass('error')) {
                        currentJiraProject.removeClass('error');
                    }
                }

                let currentHubProject = AJS.$(mappingElements[m]).find("input[name*='hubProject']");
                let hubProjectError = true;
                if (currentHubProject != null) {
                    let key = String(currentHubProject.attr("projectkey"));
                    console.log("ajaxComplete(): Black Duck project key: " + key);
                    if (key) {
                        let hubProject = hubProjectMap.get(key);
                        if (hubProject) {
                            hubProjectError = false;
                        }
                    }
                }
                if (hubProjectError) {
                    console.log("ajaxComplete(): this Black Duck project is in error");
                    if (!currentHubProject.hasClass('error')) {
                        currentHubProject.addClass('error');
                    }
                } else {
                    if (currentHubProject.hasClass('error')) {
                        currentHubProject.removeClass('error');
                    }
                }
                if (jiraProjectError || hubProjectError) {
                    console.log("ajaxComplete(): adding mapping error status on row: " + m);
                    addMappingErrorStatus(AJS.$(mappingElements[m]));
                } else {
                    removeMappingErrorStatus(AJS.$(mappingElements[m]));
                }
            }
        }
    }
});

function getCreateVulnerabilityIssuesChoice() {
    const createVulnerabilityIssuesYesElement = AJS.$("#" + "createVulnerabilityTicketsYes");

    if (createVulnerabilityIssuesYesElement[0].checked) {
        return "true";
    } else {
        return "false";
    }
}

function putConfig(restUrl, successMessage, failureMessage) {
    const creatorUsername = encodeURI(AJS.$("#creatorInput").val());
    console.log("putConfig(): " + creatorUsername);
    const jsonMappingArray = getJsonArrayFromMapping();
    const policyRuleArray = getJsonArrayFromPolicyRules();
    const createVulnerabilityIssues = getCreateVulnerabilityIssuesChoice();
    const requestData = Object.assign({}, {
        intervalBetweenChecks: encodeURI(AJS.$("#intervalBetweenChecks").val()),
        creator: creatorUsername,
        hubProjectMappings: jsonMappingArray,
        policyRules: policyRuleArray,
        createVulnerabilityIssues: createVulnerabilityIssues
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
                var config = JSON.parse(response.responseText);
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


function getJsonArrayFromPolicyRules() {
    let jsonArray = [];
    const policyRuleContainer = AJS.$("#" + policyRuleTicketCreation);
    const policyRules = policyRuleContainer.find("input");
    for (i = 0; i < policyRules.length; i++) {
        let policyRule = AJS.$(policyRules[i]);
        let currentPolicyRuleUrl = policyRule.attr("policyurl");
        // Names and descriptions with chars like ", <, and > cause problems on save
        // and we don't actually use them; just omitting them for now
//		var currentPolicyRuleDescription = policyRule.attr("title");
//		var currentPolicyRuleName = policyRule.attr("name");
        let currentPolicyRuleChecked = policyRules[i].checked;
        console.log("Constructing rules jsonArray, but omitting name");
        jsonArray.push({
            [policyRuleName]: 'name omitted by hub-jira.js',
            [policyRuleUrl]: currentPolicyRuleUrl,
            [policyRuleDescription]: 'description omitted by hub-jira.js',
            [policyRuleChecked]: currentPolicyRuleChecked
        });
    }
    return jsonArray;
}

function setCreateVulnerabilityIssuesChoice(createVulnerabilityIssues) {
    const createVulnerabilityIssuesYesElement = AJS.$("#" + "createVulnerabilityTicketsYes");
    const createVulnerabilityIssuesNoElement = AJS.$("#" + "createVulnerabilityTicketsNo");
    console.log("createVulnerabilityIssuesYesElement: " + createVulnerabilityIssuesYesElement);
    console.log("createVulnerabilityIssuesNoElement: " + createVulnerabilityIssuesNoElement);
    if (createVulnerabilityIssuesYesElement.length == 0) {
        console.log("*** createVulnerabilityIssuesYesElement is not ready");
    }
    if (createVulnerabilityIssuesNoElement.length == 0) {
        console.log("*** createVulnerabilityIssuesNoElement is not ready");
    }
    if (createVulnerabilityIssues) {
        console.log("Setting createVulnerabilityIssuesChoice to Yes");
        createVulnerabilityIssuesYesElement[0].checked = true;
        createVulnerabilityIssuesNoElement[0].checked = false;
    } else {
        console.log("Setting createVulnerabilityIssuesChoice to No");
        createVulnerabilityIssuesYesElement[0].checked = false;
        createVulnerabilityIssuesNoElement[0].checked = true;
    }
}

function setCommentOnIssueUpdatesChoice(commentOnIssueUpdatesChoice) {
    const commentOnIssueUpdatesChoiceYesElement = AJS.$("#" + "commentOnIssueUpdatesChoiceYes");
    const commentOnIssueUpdatesChoiceNoElement = AJS.$("#" + "commentOnIssueUpdatesChoiceNo");
    console.log("commentOnIssueUpdatesChoiceYesElement: " + commentOnIssueUpdatesChoiceYesElement);
    console.log("commentOnIssueUpdatesChoiceNoElement: " + commentOnIssueUpdatesChoiceNoElement);
    if (commentOnIssueUpdatesChoiceYesElement.length == 0) {
        console.log("*** createVulnerabilityIssuesYesElement is not ready");
    }
    if (commentOnIssueUpdatesChoiceNoElement.length == 0) {
        console.log("*** createVulnerabilityIssuesNoElement is not ready");
    }
    if (commentOnIssueUpdatesChoice == undefined || commentOnIssueUpdatesChoice) {
        console.log("Setting createVulnerabilityIssuesChoice to Yes");
        commentOnIssueUpdatesChoiceYesElement[0].checked = true;
        commentOnIssueUpdatesChoiceNoElement[0].checked = false;
    } else {
        console.log("Setting createVulnerabilityIssuesChoice to No");
        commentOnIssueUpdatesChoiceYesElement[0].checked = false;
        commentOnIssueUpdatesChoiceNoElement[0].checked = true;
    }
}

function fillInPluginVersion(pluginVersion) {
    console.log("fillInPluginVersion(): pluginVersion: " + pluginVersion);
    const pluginVersionElements = AJS.$("#" + "pluginVersion");
    for (let i = 0; i < pluginVersionElements.length; i++) {
        pluginVersionElements[i].innerHTML = pluginVersion;
    }
}

function onCreatorInputChange(inputField) {
    console.log("onCreatorInputChange()");
    const field = AJS.$(inputField);
    const datalist = inputField.list;
    const options = datalist.options;

    let optionFound = false;
    for (let i = 0; i < options.length; i++) {
        if (options[i].value == inputField.value) {
            optionFound = true;
            let option = AJS.$(options[i]);

            let username = option.attr("id");
            console.log("onCreatorInputChange(): username: " + username);
        }
    }
}

function onMappingInputChange(inputField) {
    const field = AJS.$(inputField);
    const datalist = inputField.list;
    const options = datalist.options;

    let optionFound = false;
    for (let i = 0; i < options.length; i++) {
        if (options[i].value == inputField.value) {
            optionFound = true;
            let option = AJS.$(options[i]);

            let projectKey = option.attr("projectKey");
            field.val(option.val());
            field.attr("projectKey", projectKey);

            let projectError = option.attr("projectError");

            let fieldParent = field.parent();
            let fieldError = fieldParent.children("#" + jiraProjectErrorId);
            if (projectError) {
                fieldError.text(projectError);
                if (!fieldError.hasClass('error')) {
                    fieldError.addClass('error');
                }
                if (!field.hasClass('error')) {
                    field.addClass('error');
                }
            } else {
                fieldError.text("");
                if (field.hasClass('error')) {
                    field.removeClass('error');
                }
            }

            break;
        }
    }
    if (!optionFound) {
        field.attr("projectKey", "");
        if (!field.hasClass('error')) {
            field.addClass('error');
        }
    }
}

function onFieldCopyMappingInputChange(inputField) {
    const field = AJS.$(inputField);
    const datalist = inputField.list;
    const options = datalist.options;

    let optionFound = false;
    for (let i = 0; i < options.length; i++) {
        if (options[i].value == inputField.value) {
            optionFound = true;
            let option = AJS.$(options[i]);
            let id = option.attr("id");
            field.val(option.val());
            field.attr("id", id);

            let projectError = option.attr("fieldError");

            let fieldParent = field.parent();
            let fieldError = fieldParent.children("#" + jiraProjectErrorId);
            if (projectError) {
                fieldError.text(projectError);
                if (!fieldError.hasClass('error')) {
                    fieldError.addClass('error');
                }
                if (!field.hasClass('error')) {
                    field.addClass('error');
                }
            } else {
                fieldError.text("");
                if (field.hasClass('error')) {
                    field.removeClass('error');
                }
            }
            break;
        }
    }
    if (!optionFound) {
        field.attr("projectKey", "");
        if (!field.hasClass('error')) {
            field.addClass('error');
        }
    }
}

function toggleDisplay(icon, fieldId) {
    const iconObject = AJS.$(icon);
    if (iconObject.hasClass('fa-angle-down')) {
        removeClassFromField(icon, 'fa-angle-down');
        addClassToField(icon, 'fa-angle-right');

        addClassToFieldById(fieldId, hiddenClass);
    } else if (iconObject.hasClass('fa-angle-right')) {
        removeClassFromField(icon, 'fa-angle-right');
        addClassToField(icon, 'fa-angle-down');

        removeClassFromFieldById(fieldId, hiddenClass);
    }
}

function toggleAuthenticationType() {
    const apiTokenInput = AJS.$('#bdAuthenticationTypeToken')[0];

    const apiTokenRowId = 'bdApiTokenRow';
    const apiTokenErrorRowId = 'bdApiTokenErrorRow';

    const deprecationWarningRowId = 'credentialsDeprecatedWarningRow';
    const usernameRowId = 'bdUsernameRow';
    const usernameErrorRowId = 'hubUsernameErrorRow';
    const passwordRowId = 'bdPasswordRow';
    const passwordErrorRowId = 'hubPasswordErrorRow';

    if (apiTokenInput && apiTokenInput.checked) {
        removeClassFromFieldById(apiTokenRowId, "hidden");

        addClassToFieldById(deprecationWarningRowId, "hidden");
        addClassToFieldById(usernameRowId, "hidden");
        addClassToFieldById(usernameErrorRowId, "hidden");
        addClassToFieldById(passwordRowId, "hidden");
        addClassToFieldById(passwordErrorRowId, "hidden");
    } else {
        addClassToFieldById(apiTokenRowId, "hidden");
        addClassToFieldById(apiTokenErrorRowId, "hidden");

        removeClassFromFieldById(deprecationWarningRowId, "hidden");
        removeClassFromFieldById(usernameRowId, "hidden");
        removeClassFromFieldById(passwordRowId, "hidden");
    }
}

(function ($) {

    $(document).ready(function () {
        console.log("DOM loaded");
        populateForm();
        AJS.tabs.setup();
        console.log("AJS Tabs", AJS.tabs);
        AJS.$("#blackduck-navigation-tabs").on('tabSelect', openTab);
    });

})(AJS.$ || jQuery);
