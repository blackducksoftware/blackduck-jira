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
function toggleDisplayById(iconId, fieldId) {
    const iconObject = AJS.$('#' + iconId);
    if (iconObject.hasClass('fa-angle-down')) {
        removeClassFromFieldById(iconId, 'fa-angle-down');
        addClassToFieldById(iconId, 'fa-angle-right');

        addClassToFieldById(fieldId, hiddenClass);
    } else if (iconObject.hasClass('fa-angle-right')) {
        removeClassFromFieldById(iconId, 'fa-angle-right');
        addClassToFieldById(iconId, 'fa-angle-down');

        removeClassFromFieldById(fieldId, hiddenClass);
    }
}

function checkProxyConfig() {
    const proxyHost = AJS.$("#proxyHost").val();
    const proxyPort = AJS.$("#proxyPort").val();
    const noProxyHost = AJS.$("#noProxyHost").val();
    const proxyUsername = AJS.$("#proxyUsername").val();
    const proxyPassword = AJS.$("#proxyPassword").val();

    if (!proxyHost && !proxyPort && !noProxyHost && !proxyUsername && !proxyPassword) {
        toggleDisplayById("proxyConfigDisplayIcon", 'proxyConfigArea');
    }
}

function fillInHubProjects(hubProjects) {
    hubProjectMap = new Map();
    const mappingElement = AJS.$("#" + hubProjectMappingElement);
    const hubProjectList = mappingElement.find("datalist[id='" + hubProjectListId + "']");
    if (hubProjectList.length > 0) {
        clearList(hubProjectList[0]);
    }
    if (hubProjects != null && hubProjects.length > 0) {
        for (let h = 0; h < hubProjects.length; h++) {
            hubProjectMap.set(hubProjects[h], hubProjects[h]);
            console.log("fillInHubProjects(): adding: " + hubProjects[h]);
            let newOption = AJS.$('<option>', {
                value: hubProjects[h],
                projectKey: hubProjects[h]
            });
            hubProjectList.append(newOption);
        }
    }
}

function handleErrorHubDetails(fieldRowId, fieldId, configField) {
    if (configField) {
        showErrorHubDetails(fieldRowId, fieldId, configField);
    } else {
        hideError(fieldRowId, fieldId);
    }
}

function addPolicyViolationRules(policyRules) {
    const policyRuleContainer = AJS.$("#" + policyRuleTicketCreation);
    removeAllChildren(policyRuleContainer[0]);
    if (policyRules != null && policyRules.length > 0) {
        for (p = 0; p < policyRules.length; p++) {
            let newPolicy = AJS.$('<div>', {});

            let newPolicyRuleCheckbox = AJS.$('<input>', {
                type: "checkbox",
                policyurl: decodeURI(policyRules[p].policyUrl),
                title: decodeURI(policyRules[p].description),
                name: decodeURI(policyRules[p].name),
                checked: policyRules[p].checked
            });
            let description = decodeURI(policyRules[p].description);
            let newPolicyLabel = AJS.$('<label>', {
                text: policyRules[p].name,
                title: description,
            });
            newPolicyLabel.addClass("textStyle");
            newPolicyLabel.css("padding", "0px 5px 0px 5px")
            if (!policyRules[p].enabled) {
                newPolicyLabel.addClass("disabledPolicyRule");
            }


            newPolicy.append(newPolicyRuleCheckbox, newPolicyLabel)

            if (description) {
                let newDescription = AJS.$('<i>', {
                    title: description,
                });
                AJS.$(newDescription).addClass("fa");
                AJS.$(newDescription).addClass("fa-info-circle");
                AJS.$(newDescription).addClass("infoIcon");
                newPolicy.append(newDescription);
            }
            newPolicy.appendTo(policyRuleContainer);
        }
    }
}

function readBlackduckServerData() {
    AJS.$.ajax({
        url: createRequestPath('blackDuckDetails/read'),
        dataType: "json",
        success: function (config) {
            console.log("Successful get of hub details for " + config.hubUrl);

            updateValue("hubServerUrl", config.hubUrl);
            updateValue("hubTimeout", config.timeout);
            updateValue("hubTrustCert", config.trustCert);
            updateValue("bdApiToken", config.apiToken);
            updateValue("proxyHost", config.hubProxyHost);
            updateValue("proxyPort", config.hubProxyPort);
            updateValue("proxyUsername", config.hubProxyUser);
            updateValue("proxyPassword", config.hubProxyPassword);
            updateValue("noProxyHost", config.hubNoProxyHosts);

            checkProxyConfig();

            handleError(errorMessageFieldId, config.errorMessage, true, true);
            handleErrorHubDetails('hubServerUrlErrorRow', 'hubServerUrlError', config.hubUrlError);
            handleErrorHubDetails('hubTimeoutErrorRow', 'hubTimeoutError', config.timeoutError);
            handleErrorHubDetails('hubTrustCertErrorRow', 'hubTrustCertError', config.trustCertError);
            handleErrorHubDetails('bdApiTokenErrorRow', 'bdApiTokenError', config.apiTokenError);
            handleErrorHubDetails('proxyHostErrorRow', 'proxyHostError', config.hubProxyHostError);
            handleErrorHubDetails('proxyPortErrorRow', 'proxyPortError', config.hubProxyPortError);
            handleErrorHubDetails('proxyUsernameErrorRow', 'proxyUsernameError', config.hubProxyUserError);
            handleErrorHubDetails('proxyPasswordErrorRow', 'proxyPasswordError', config.hubProxyPasswordError);
            handleErrorHubDetails('noProxyHostErrorRow', 'noProxyHostError', config.hubNoProxyHostsError);

        }, error: function (response) {
            console.log("putConfig(): " + response.responseText);
            alert("There was an error loading the configuration.");
            handleDataRetrievalError(response, 'configurationError', "There was a problem retrieving the configuration.", "Configuration Error");
        },
        complete: function (jqXHR, textStatus) {
            stopProgressSpinner('hubDetailsProgressSpinner');
            console.log("Completed get of hub details: " + textStatus);
        }
    });
}

function readBlackduckProjectData() {
    AJS.$.ajax({
        url: createRequestPath('blackDuckProjects/'),
        dataType: "json",
        success: function (response) {
            if (Array.isArray(response)) {
                fillInHubProjects(response);
                gotHubProjects = true;
            } else {
                handleError(hubProjectListErrorId, response, false, false);
                handleError(errorMessageFieldId, response, true, false);
            }
        },
        error: function (response) {
            handleDataRetrievalError(response, hubProjectListErrorId, "There was a problem retrieving the Hub Projects.", "Hub Project Error");
        },
        complete: function (jqXHR, textStatus) {
            console.log("Completed get of Hub projects: " + textStatus);
        }
    });
}

function readBlackduckPolicyData() {
    AJS.$.ajax({
        url: createRequestPath('blackDuckPolicies/'),
        dataType: "json",
        success: function (config) {
            addPolicyViolationRules(config.policyRules);

            handleError(errorMessageFieldId, config.errorMessage, true, false);
            handleError('policyRulesError', config.policyRulesError, true, false);
        },
        error: function (response) {
            handleDataRetrievalError(response, "policyRulesError", "There was a problem retrieving the Black Duck Policy Rules.", "Black Duck Policy Rules Error");
        },
        complete: function (jqXHR, textStatus) {
            AJS.$('#policyRuleSpinner').remove();
            console.log("Completed get of Black Duck policies: " + textStatus);
        }
    });
}

function testConnection() {
    putHubDetails(createRequestPath('blackDuckDetails/testConnection'), 'Test Connection successful.', 'Test Connection failed.');
}

function initProjectMappingRows() {
    console.log("initProjectMapping()");
    const mappingContainer = AJS.$("#" + hubProjectMappingContainer);
    let mappingElements = mappingContainer.find("tr[name*='" + hubProjectMappingElement + "']");
    console.log("initProjectMapping(): Before: #rows: " + mappingElements.length);
    for (let rowIndex = mappingElements.length - 1; rowIndex > 0; rowIndex--) {
        console.log("initProjectMapping: Removing project mapping row: " + rowIndex);
        let mappingElement = mappingElements[rowIndex];
        AJS.$('#' + mappingElement.id).remove();
    }
    mappingElements = mappingContainer.find("tr[name*='" + hubProjectMappingElement + "']");
    console.log("initProjectMapping(): After re-fetch: #rows: " + mappingElements.length);
}

function putHubDetails(restUrl, successMessage, failureMessage) {
    const apiTokenInput = AJS.$('#bdAuthenticationTypeToken')[0];
    const config = Object.assign({}, {
        hubUrl: encodeURI(AJS.$("#hubServerUrl").val()),
        timeout: encodeURI(AJS.$("#hubTimeout").val()),
        trustCert: encodeURI(AJS.$("#hubTrustCert")[0].checked),
        apiToken: encodeURI(AJS.$("#bdApiToken").val()),
        hubProxyHost: encodeURI(AJS.$("#proxyHost").val()),
        hubProxyPort: encodeURI(AJS.$("#proxyPort").val()),
        hubNoProxyHosts: encodeURI(AJS.$("#noProxyHost").val()),
        hubProxyUser: encodeURI(AJS.$("#proxyUsername").val()),
        hubProxyPassword: encodeURI(AJS.$("#proxyPassword").val())
    });

    console.log("putHubDetails()");
    AJS.$.ajax({
        url: restUrl,
        type: "PUT",
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify(config),
        processData: false,
        success: function () {
            handleError(errorMessageFieldId, "", true, true);
            hideError('hubServerUrlErrorRow', 'hubServerUrlError');
            hideError('hubTimeoutErrorRow', 'hubTimeoutError');
            hideError('hubTrustCertErrorRow', 'hubTrustCertError');
            hideError('bdApiTokenErrorRow', 'bdApiTokenError');
            hideError('proxyHostErrorRow', 'proxyHostError');
            hideError('proxyPortErrorRow', 'proxyPortError');
            hideError('proxyUsernameErrorRow', 'proxyUsernameError');
            hideError('proxyPasswordErrorRow', 'proxyPasswordError');
            hideError('noProxyHostErrorRow', 'noProxyHostError');
            hideError('configurationErrorRow', 'configurationError');

            showStatusMessage(successStatus, 'Success!', successMessage);

            // Since the hub server may have changed, go fetch all hub data
            initProjectMappingRows();
            populateFormBlackduckData();

        },
        error: function (response) {
            console.log("putConfig(): " + response.responseText);
            var config = JSON.parse(response.responseText);

            handleError(errorMessageFieldId, config.errorMessage, true, true);
            handleErrorHubDetails('hubServerUrlErrorRow', 'hubServerUrlError', config.hubUrlError);
            handleErrorHubDetails('hubTimeoutErrorRow', 'hubTimeoutError', config.timeoutError);
            handleErrorHubDetails('hubTrustCertErrorRow', 'hubTrustCertError', config.trustCertError);
            handleErrorHubDetails('bdApiTokenErrorRow', 'bdApiTokenError', config.apiTokenError);
            handleErrorHubDetails('proxyHostErrorRow', 'proxyHostError', config.hubProxyHostError);
            handleErrorHubDetails('proxyPortErrorRow', 'proxyPortError', config.hubProxyPortError);
            handleErrorHubDetails('proxyUsernameErrorRow', 'proxyUsernameError', config.hubProxyUserError);
            handleErrorHubDetails('proxyPasswordErrorRow', 'proxyPasswordError', config.hubProxyPasswordError);
            handleErrorHubDetails('noProxyHostErrorRow', 'noProxyHostError', config.hubNoProxyHostsError);
            handleErrorHubDetails('configurationErrorRow', 'configurationError', config.testConnectionError);

            showStatusMessage(errorStatus, 'ERROR!', failureMessage);
        },
        complete: function (jqXHR, textStatus) {
            stopProgressSpinner('hubDetailsProgressSpinner');
        }
    });
}

function updateHubDetails() {
    putHubDetails(createRequestPath('blackDuckDetails/save'), 'Save successful.', 'The Hub details are not valid.');
}

function readBlackduckTicketCreationErrors() {
    AJS.$.ajax({
        url: createRequestPath('blackDuckJiraTicketErrors/'),
        dataType: "json",
        success: function (creationError) {
            updateTicketCreationErrors(creationError.hubJiraTicketErrors);
        },
        error: function (response) {
            console.log("Error getting the ticket creation errors : " + response.responseText);
            const fieldSet = AJS.$('#' + ticketCreationFieldSetId);
            if (fieldSet.hasClass('hidden')) {
                fieldSet.removeClass('hidden');
            }
            handleDataRetrievalError(response, "ticketCreationLoadingError", "There was a problem retrieving the Ticket Creation Errors.", "Ticket Creation Error");

        },
        complete: function (jqXHR, textStatus) {
            console.log("Completed get of task errors: " + textStatus);
        }
    });
}

function updateTicketCreationErrors(hubJiraTicketErrors) {
    if (hubJiraTicketErrors != null && hubJiraTicketErrors.length > 0) {
        var fieldSet = AJS.$('#' + ticketCreationFieldSetId);
        if (fieldSet.hasClass(hiddenClass)) {
            fieldSet.removeClass(hiddenClass);
        }
        var ticketCreationErrorTable = AJS.$('#' + ticketCreationErrorsTableId);
        for (j = 0; j < hubJiraTicketErrors.length; j++) {

            var ticketErrorRow = AJS.$("#" + ticketCreationErrorRowId).clone();
            ticketCreationErrorCounter = ticketCreationErrorCounter + 1;
            ticketErrorRow.removeClass(hiddenClass);
            ticketErrorRow.attr("id", ticketErrorRow.attr("id") + ticketCreationErrorCounter);
            ticketErrorRow.appendTo(ticketCreationErrorTable);

            var errorColumn = ticketErrorRow.find('td');

            var stackTraceDiv = AJS.$(errorColumn).children("div[name*='ticketCreationStackTraceName']");

            var stackTrace = hubJiraTicketErrors[j].stackTrace;

            var errorMessageDiv = AJS.$(errorColumn).children("div[name*='ticketCreationErrorMessageName']");
            var errorMessage = "";
            if (stackTrace.indexOf("\n") > -1) {
                errorMessage = stackTrace.substring(0, stackTrace.indexOf("\n"));

                stackTraceDiv.text(stackTrace);
            } else {
                errorMessage = stackTrace;
                var expansionIconDiv = AJS.$(errorColumn).children("div[name*='expansionIconDiv']");
                var expansionIcon = AJS.$(expansionIconDiv).children("i[name*='expansionIcon']");
                if (expansionIcon.hasClass('fa-plus-square-o')) {
                    expansionIcon.removeClass('fa-plus-square-o');
                    expansionIcon.addClass('fa-square-o');
                }
            }

            errorMessageDiv.text(errorMessage);

            var timeStampDiv = AJS.$(errorColumn).children("div[name*='ticketCreationTimeStampName']");
            var timeStamp = hubJiraTicketErrors[j].timeStamp;

            if (timeStampDiv.hasClass(hiddenClass)) {
                timeStampDiv.removeClass(hiddenClass);
            }

            timeStampDiv.text(timeStamp);
        }
    }
}

function showErrorHubDetails(fieldRowId, fieldId, configField) {
    AJS.$("#" + fieldId).text(decodeURI(configField));
    removeClassFromFieldById(fieldRowId, hiddenClass);
}
