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
function readBlackduckServerData() {
    AJS.$.ajax({
        url: createRequestPath('config/blackduck'),
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
        url: createRequestPath('config/blackduck/projects/'),
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
        url: createRequestPath('config/blackduck/policies/'),
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

function readBlackduckTicketCreationErrors() {
    AJS.$.ajax({
        url: createRequestPath('config/blackduck/ticket/errors/'),
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

function testConnection() {
    putHubDetails(createRequestPath('config/blackduck/test'), 'Test Connection successful.', 'Test Connection failed.');
}

function updateHubDetails() {
    putHubDetails(createRequestPath('config/blackduck'), 'Save successful.', 'The Hub details are not valid.');
}

function putHubDetails(restUrl, successMessage, failureMessage) {
    const config = Object.assign({}, {
        hubUrl: encodeURI(AJS.$("#hubServerUrl").val()),
        timeout: encodeURI(AJS.$("#hubTimeout").val()),
        trustCert: encodeURI(AJS.$("#hubTrustCert")[0].checked),
        apiToken: encodeURI(AJS.$("#bdApiToken").val()),
        hubProxyHost: encodeURI(AJS.$("#proxyHost").val()),
        hubProxyPort: encodeURI(AJS.$("#proxyPort").val()),
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
            hideError('configurationErrorRow', 'configurationError');

            showStatusMessage(successStatus, 'Success!', successMessage);

            // Since the hub server may have changed, go fetch all hub data
            initProjectMappingRows();
            populateFormBlackduckData();

        },
        error: function (response) {
            console.log("putConfig(): " + response.responseText);
            if (!redirectIfUnauthenticated(response)) {
                let config = JSON.parse(response.responseText);

                handleError(errorMessageFieldId, config.errorMessage, true, true);
                handleErrorHubDetails('hubServerUrlErrorRow', 'hubServerUrlError', config.hubUrlError);
                handleErrorHubDetails('hubTimeoutErrorRow', 'hubTimeoutError', config.timeoutError);
                handleErrorHubDetails('hubTrustCertErrorRow', 'hubTrustCertError', config.trustCertError);
                handleErrorHubDetails('bdApiTokenErrorRow', 'bdApiTokenError', config.apiTokenError);
                handleErrorHubDetails('proxyHostErrorRow', 'proxyHostError', config.hubProxyHostError);
                handleErrorHubDetails('proxyPortErrorRow', 'proxyPortError', config.hubProxyPortError);
                handleErrorHubDetails('proxyUsernameErrorRow', 'proxyUsernameError', config.hubProxyUserError);
                handleErrorHubDetails('proxyPasswordErrorRow', 'proxyPasswordError', config.hubProxyPasswordError);
                handleErrorHubDetails('configurationErrorRow', 'configurationError', config.testConnectionError);

                showStatusMessage(errorStatus, 'ERROR!', failureMessage);
            }
        },
        complete: function (jqXHR, textStatus) {
            stopProgressSpinner('hubDetailsProgressSpinner');
        }
    });
}
