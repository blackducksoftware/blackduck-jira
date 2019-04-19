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
function openTab() {
    resetStatusMessage();
}

function fillInPluginVersion(pluginVersion) {
    console.log("fillInPluginVersion(): pluginVersion: " + pluginVersion);
    const pluginVersionElements = AJS.$("#" + "pluginVersion");
    for (let i = 0; i < pluginVersionElements.length; i++) {
        pluginVersionElements[i].innerHTML = pluginVersion;
    }
}

function readPluginInfo() {
    AJS.$.ajax({
        url: createRequestPath('config/pluginInfo/'),
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
    readPluginInfo();
    populateFormBlackduckData();
    console.log("populateForm() Finished");
}


function populateFormBlackduckData() {
    console.log("populateFormBlackduckData()");
    gotHubProjects = false;
    gotProjectMappings = false;

    readBlackduckProjectData();
    readBlackduckPolicyData();
    readCommentOnUpdateChoice();
    readProjectReviewerNotificationsChoice();
    readMappingData();
}


function resetSalKeys() {
    const restUrl = createRequestPath('config/reset');
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
        type: "DELETE",
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
                let currentProjectPatternOption = AJS.$(mappingElements[m]).find("input[name*='projectPatternOption']")[0];
                let hubProjectError = true;
                if (currentHubProject != null) {
                    let isProjectPattern = currentProjectPatternOption.checked;
                    let key = String(currentHubProject.attr("projectkey"));
                    console.log("ajaxComplete(): Black Duck project key: " + key);
                    console.log("ajaxComplete(): Black Duck project pattern: " + isProjectPattern);
                    if (isProjectPattern) {
                        hubProjectError = false;
                    } else if (key) {
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

(function ($) {

    $(document).ready(function () {
        console.log("DOM loaded");
        populateForm();
        AJS.tabs.setup();
        AJS.$("#blackduck-navigation-tabs").on('tabSelect', openTab);
    });

})(AJS.$ || jQuery);
