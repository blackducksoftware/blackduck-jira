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
const statusMessageFieldId = "aui-hub-message-field";
const statusMessageTitleId = "aui-hub-message-title";
const statusMessageTitleTextId = "aui-hub-message-title-text";
const statusMessageTextId = "aui-hub-message-text";

const errorMessageFieldId = "error-message-field";

const errorStatus = "error";
const successStatus = "success";

const hiddenClass = "hidden";

const hubJiraGroupsId = "hubJiraGroups";

const hubProjectMappingTable = "hubProjectMappingTable";
const hubProjectMappingContainer = "hubProjectMappingContainer";
const hubProjectMappingElement = "hubProjectMappingElement";
const hubMappingStatus = "mappingStatus";

const fieldCopyMappingContainer = "fieldCopyMappingContainer";
const fieldCopyMappingElement = "fieldCopyMappingElement";
const fieldCopyMappingStatus = "fieldCopyMappingStatus";

const jiraProjectListId = "jiraProjects";
const hubProjectListId = "hubProjects";

const sourceFieldListId = "sourceFields";
const targetFieldListId = "targetFields";

const jiraProjectListErrorId = "jiraProjectListError";
const hubProjectListErrorId = "hubProjectListError";

const jiraProjectErrorId = "jiraProjectError";

const jiraProjectDisplayName = "projectName";
const jiraProjectKey = "projectId";
const hubProjectDisplayName = "projectName";
const hubProjectKey = "projectUrl";
const jiraProjectIssueCreatorDisplayName = "issueCreator";

const policyRuleTicketCreation = "policyRuleTicketCreation";
const policyRuleName = "name";
const policyRuleDescription = "description";
const policyRuleUrl = "policyUrl";
const policyRuleChecked = "checked";

const ticketCreationFieldSetId = "ticketCreationFieldSet";
const ticketCreationErrorsTableId = "ticketCreationErrorsTable";
const ticketCreationErrorRowId = "ticketCreationErrorRow";


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

function initTabs() {
    console.log("Initializing tabs");

    // Declare all variables
    let i, tabcontent, tablinks;

    // Get all elements with class="tabcontent" and hide them
    tabcontent = document.getElementsByClassName("tabcontent");
    for (i = 0; i < tabcontent.length; i++) {
        tabcontent[i].style.display = "none";
    }

    // Get all elements with class="tablinks" and remove the class "active"
    tablinks = document.getElementsByClassName("tablinks");
    for (i = 0; i < tablinks.length; i++) {
        tablinks[i].className = tablinks[i].className.replace(" active", "");
    }

    // Show the current tab, and add an "active" class to the link that opened the tab
    document.getElementById("Admin").style.display = "block";

    // evt.currentTarget.className += " active";
}

function openTab(evt, tabId) {
    console.log("Opening: " + tabId);

    resetStatusMessage();

    // Declare all variables
    let i, tabcontent, tablinks;

    // Get all elements with class="tabcontent" and hide them
    tabcontent = document.getElementsByClassName("tabcontent");
    for (i = 0; i < tabcontent.length; i++) {
        tabcontent[i].style.display = "none";
    }

    // Get all elements with class="tablinks" and remove the class "active"
    tablinks = document.getElementsByClassName("tablinks");
    for (i = 0; i < tablinks.length; i++) {
        tablinks[i].className = tablinks[i].className.replace(" active", "");
    }

    // Show the current tab, and add an "active" class to the link that opened the tab
    document.getElementById(tabId).style.display = "block";
    evt.currentTarget.className += " active";
}

function testConnection() {
    putHubDetails(AJS.contextPath() + '/rest/blackduck-jira-integration/1.0/blackDuckDetails/testConnection', 'Test Connection successful.', 'Test Connection failed.');
}

function updateHubDetails() {
    putHubDetails(AJS.contextPath() + '/rest/blackduck-jira-integration/1.0/blackDuckDetails/save', 'Save successful.', 'The Hub details are not valid.');
}

function updateConfig() {
    putConfig(AJS.contextPath() + '/rest/blackduck-jira-integration/1.0/', 'Save successful.', 'The configuration is not valid.');
}

function updateAccessConfig() {
    putAccessConfig(AJS.contextPath() + '/rest/blackduck-jira-integration/1.0/admin', 'Save successful.', 'The configuration is not valid.');
}

function updateFieldCopyConfig() {
    console.log("updateFieldCopyConfig()");
    putFieldCopyConfig(AJS.contextPath() + '/rest/blackduck-jira-integration/1.0/updateFieldCopyMappings', 'Save successful.', 'The field copy configuration is not valid.');
}

function putHubDetails(restUrl, successMessage, failureMessage) {
    const apiTokenInput = AJS.$('#bdAuthenticationTypeToken')[0];

    let config = '';
    if (apiTokenInput && apiTokenInput.checked) {
        config = Object.assign({}, {
            hubUrl: encodeURI(AJS.$("#hubServerUrl").val()),
            timeout: encodeURI(AJS.$("#hubTimeout").val()),
            trustCert: encodeURI(AJS.$("#hubTrustCert")[0].checked),
            apiToken: encodeURI(AJS.$("#bdApiToken").val()),
            username: "",
            password: "",
            hubProxyHost: encodeURI(AJS.$("#proxyHost").val()),
            hubProxyPort: encodeURI(AJS.$("#proxyPort").val()),
            hubNoProxyHosts: encodeURI(AJS.$("#noProxyHost").val()),
            hubProxyUser: encodeURI(AJS.$("#proxyUsername").val()),
            hubProxyPassword: encodeURI(AJS.$("#proxyPassword").val())
        });
    } else {
        config = Object.assign({}, {
            hubUrl: encodeURI(AJS.$("#hubServerUrl").val()),
            timeout: encodeURI(AJS.$("#hubTimeout").val()),
            trustCert: encodeURI(AJS.$("#hubTrustCert")[0].checked),
            apiToken: "",
            username: encodeURI(AJS.$("#hubUsername").val()),
            password: encodeURI(AJS.$("#hubPassword").val()),
            hubProxyHost: encodeURI(AJS.$("#proxyHost").val()),
            hubProxyPort: encodeURI(AJS.$("#proxyPort").val()),
            hubNoProxyHosts: encodeURI(AJS.$("#noProxyHost").val()),
            hubProxyUser: encodeURI(AJS.$("#proxyUsername").val()),
            hubProxyPassword: encodeURI(AJS.$("#proxyPassword").val())
        });
    }

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
            hideError('hubUsernameErrorRow', 'hubUsernameError');
            hideError('hubPasswordErrorRow', 'hubPasswordError');
            hideError('proxyHostErrorRow', 'proxyHostError');
            hideError('proxyPortErrorRow', 'proxyPortError');
            hideError('proxyUsernameErrorRow', 'proxyUsernameError');
            hideError('proxyPasswordErrorRow', 'proxyPasswordError');
            hideError('noProxyHostErrorRow', 'noProxyHostError');
            hideError('configurationErrorRow', 'configurationError');

            showStatusMessage(successStatus, 'Success!', successMessage);

            // Since the hub server may have changed, go fetch all hub data
            initProjectMappingRows();
            populateFormHubData();

        },
        error: function (response) {
            console.log("putConfig(): " + response.responseText);
            var config = JSON.parse(response.responseText);

            handleError(errorMessageFieldId, config.errorMessage, true, true);
            handleErrorHubDetails('hubServerUrlErrorRow', 'hubServerUrlError', config.hubUrlError);
            handleErrorHubDetails('hubTimeoutErrorRow', 'hubTimeoutError', config.timeoutError);
            handleErrorHubDetails('hubTrustCertErrorRow', 'hubTrustCertError', config.trustCertError);
            handleErrorHubDetails('bdApiTokenErrorRow', 'bdApiTokenError', config.apiTokenError);
            handleErrorHubDetails('hubUsernameErrorRow', 'hubUsernameError', config.usernameError);
            handleErrorHubDetails('hubPasswordErrorRow', 'hubPasswordError', config.passwordError);
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


function handleErrorHubDetails(fieldRowId, fieldId, configField) {
    if (configField) {
        showErrorHubDetails(fieldRowId, fieldId, configField);
    } else {
        hideError(fieldRowId, fieldId);
    }
}

function populateForm() {
    AJS.$.ajax({
        url: AJS.contextPath() + "/rest/blackduck-jira-integration/1.0/admin/",
        dataType: "json",
        success: function (admin) {
            fillInJiraGroups(admin.hubJiraGroups, admin.jiraGroups);

            handleError('hubJiraGroupsError', admin.hubJiraGroupsError, true, false);
        },
        error: function (response) {
            handleDataRetrievalError(response, "hubJiraGroupsError", "There was a problem retrieving the Admin configuration.", "Admin Error");
        },
        complete: function (jqXHR, textStatus) {
            console.log("Completed get of groups: " + textStatus);
        }
    });

    AJS.$.ajax({
        url: AJS.contextPath() + "/rest/blackduck-jira-integration/1.0/blackDuckDetails/read",
        dataType: "json",
        success: function (config) {
            console.log("Successful get of hub details for " + config.hubUrl);

            updateValue("hubServerUrl", config.hubUrl);
            updateValue("hubTimeout", config.timeout);
            updateValue("hubTrustCert", config.trustCert);
            updateValue("bdApiToken", config.apiToken);
            updateValue("hubUsername", config.username);
            updateValue("hubPassword", config.password);
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
            handleErrorHubDetails('hubUsernameErrorRow', 'hubUsernameError', config.usernameError);
            handleErrorHubDetails('hubPasswordErrorRow', 'hubPasswordError', config.passwordError);
            handleErrorHubDetails('proxyHostErrorRow', 'proxyHostError', config.hubProxyHostError);
            handleErrorHubDetails('proxyPortErrorRow', 'proxyPortError', config.hubProxyPortError);
            handleErrorHubDetails('proxyUsernameErrorRow', 'proxyUsernameError', config.hubProxyUserError);
            handleErrorHubDetails('proxyPasswordErrorRow', 'proxyPasswordError', config.hubProxyPasswordError);
            handleErrorHubDetails('noProxyHostErrorRow', 'noProxyHostError', config.hubNoProxyHostsError);

            const apiTokenRowId = 'bdApiTokenRow';
            const apiTokenErrorRowId = 'bdApiTokenErrorRow';

            const deprecationWarningRowId = 'credentialsDeprecatedWarningRow';
            const usernameRowId = 'bdUsernameRow';
            const usernameErrorRowId = 'hubUsernameErrorRow';
            const passwordRowId = 'bdPasswordRow';
            const passwordErrorRowId = 'hubPasswordErrorRow';

            if (config.username || config.password) {
                AJS.$('#bdAuthenticationTypeCredentials')[0].checked = true;
                addClassToFieldById(apiTokenRowId, "hidden");
                addClassToFieldById(apiTokenErrorRowId, "hidden");

                removeClassFromFieldById(deprecationWarningRowId, "hidden");
                removeClassFromFieldById(usernameRowId, "hidden");
                removeClassFromFieldById(passwordRowId, "hidden");
            } else {
                AJS.$('#bdAuthenticationTypeToken')[0].checked = true;
                removeClassFromFieldById(apiTokenRowId, "hidden");

                addClassToFieldById(deprecationWarningRowId, "hidden");
                addClassToFieldById(usernameRowId, "hidden");
                addClassToFieldById(usernameErrorRowId, "hidden");
                addClassToFieldById(passwordRowId, "hidden");
                addClassToFieldById(passwordErrorRowId, "hidden");
            }

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

    AJS.$.ajax({
        url: AJS.contextPath() + "/rest/blackduck-jira-integration/1.0/interval/",
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

    AJS.$.ajax({
        url: AJS.contextPath() + "/rest/blackduck-jira-integration/1.0/creator/",
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

    initCreatorCandidates();

    AJS.$.ajax({
        url: AJS.contextPath() + "/rest/blackduck-jira-integration/1.0/jiraProjects/",
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

    AJS.$.ajax({
        url: AJS.contextPath() + "/rest/blackduck-jira-integration/1.0/sourceFields/",
        dataType: "json",
        success: function (sourceFieldNames) {
            fillInSourceFields(sourceFieldNames);
            gotSourceFields = true;
        },
        error: function (response) {
            handleDataRetrievalError(response, sourceFieldListErrorId, "There was a problem retrieving the source fields.", "Source Field Error");
        },
        complete: function (jqXHR, textStatus) {
            console.log("Completed get of sourceFields: " + textStatus);
        }
    });
    AJS.$.ajax({
        url: AJS.contextPath() + "/rest/blackduck-jira-integration/1.0/targetFields/",
        dataType: "json",
        success: function (targetFields) {
            fillInTargetFields(targetFields);

            handleError("fieldCopyTargetFieldError", targetFields.errorMessage, true, false);

            gotTargetFields = true;
        },
        error: function (response) {
            handleDataRetrievalError(response, targetFieldListErrorId, "There was a problem retrieving the target fields.", "Target Field Error");
        },
        complete: function (jqXHR, textStatus) {
            console.log("Completed get of targetFields: " + textStatus);
        }
    });

    AJS.$.ajax({
        url: AJS.contextPath() + "/rest/blackduck-jira-integration/1.0/fieldCopyMappings/",
        dataType: "json",
        success: function (config) {
            console.log("Success getting field copy mappings");
            fillInFieldCopyMappings(config.projectFieldCopyMappings);

            handleError("fieldCopyMappingError", config.errorMessage, true, false);

            gotFieldCopyMappings = true;
        },
        error: function (response) {
            console.log("Error getting field copy mappings");
            handleDataRetrievalError(response, "fieldCopyMappingsError", "There was a problem retrieving the Field Copy Mappings.", "Field Copy Mapping Error");
        },
        complete: function (jqXHR, textStatus) {
            console.log("Completed get of field copy mappings: " + textStatus);
        }
    });
    AJS.$.ajax({
        url: AJS.contextPath() + "/rest/blackduck-jira-integration/1.0/blackDuckJiraTicketErrors/",
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
    AJS.$.ajax({
        url: AJS.contextPath() + "/rest/blackduck-jira-integration/1.0/pluginInfo/",
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

    populateFormHubData();
    console.log("populateForm() Finished");
}

function initCreatorCandidates() {
    console.log("Initializing issue creator candidate list");
    AJS.$.ajax({
        url: AJS.contextPath() + "/rest/blackduck-jira-integration/1.0/creatorCandidates/",
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

function populateFormHubData() {
    console.log("populateFormHubData()");
    gotHubProjects = false;
    gotProjectMappings = false;

    AJS.$.ajax({
        url: AJS.contextPath() + "/rest/blackduck-jira-integration/1.0/blackDuckProjects/",
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
    AJS.$.ajax({
        url: AJS.contextPath() + "/rest/blackduck-jira-integration/1.0/blackDuckPolicies/",
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
    AJS.$.ajax({
        url: AJS.contextPath() + "/rest/blackduck-jira-integration/1.0/createVulnerabilityTicketsChoice/",
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
        url: AJS.contextPath() + "/rest/blackduck-jira-integration/1.0/commentOnIssueUpdatesChoice/",
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
    AJS.$.ajax({
        url: AJS.contextPath() + "/rest/blackduck-jira-integration/1.0/mappings/",
        dataType: "json",
        success: function (config) {
            fillInMappings(config.hubProjectMappings);

            handleError(errorMessageFieldId, config.errorMessage, true, false);
            handleError('hubProjectMappingsError', config.hubProjectMappingError, true, false);

            gotProjectMappings = true;
        },
        error: function (response) {
            handleDataRetrievalError(response, "hubProjectMappingsError", "There was a problem retrieving the Project Mappings.", "Project Mapping Error");
        },
        complete: function (jqXHR, textStatus) {
            AJS.$('#projectMappingSpinner').remove();
            console.log("Completed get of project mappings: " + textStatus);
        }
    });
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

function resetSalKeys() {
    const restUrl = AJS.contextPath() + '/rest/blackduck-jira-integration/1.0/reset';
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

function getJsonArrayFromErrors(errorRow) {
    let jsonArray = "[";

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

    jsonArray += '{"'
        + 'stackTrace" : "' + stackTrace
        + '","'
        + 'timeStamp" : "' + timeStamp
        + '"}';
    jsonArray += "]";
    return jsonArray;
}

function handleErrorRemoval(trashIcon) {
    const currentIcon = AJS.$(trashIcon);
    const errorRow = currentIcon.closest("tr");

    const restUrl = AJS.contextPath() + '/rest/blackduck-jira-integration/1.0/removeErrors';

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

function handleDataRetrievalError(response, errorId, errorText, dialogTitle) {
    const errorField = AJS.$('#' + errorId);
    errorField.text(errorText);
    removeClassFromField(errorField, hiddenClass);
    addClassToField(errorField, "clickable");
    let error = JSON.parse(response.responseText);
    error = AJS.$(error);
    errorField.click(function () {
        showErrorDialog(dialogTitle, error.attr("message"), error.attr("status-code"), error.attr("stack-trace"))
    });
}

function showErrorDialog(header, errorMessage, errorCode, stackTrace) {
    const errorDialog = new AJS.Dialog({
        width: 800,
        height: 500,
        id: 'error-dialog',
        closeOnOutsideClick: true
    });

    errorDialog.addHeader(header);

    const errorBody = AJS.$('<div>', {});
    var errorMessage = AJS.$('<p>', {
        text: "Error Message : " + errorMessage
    });
    var errorCode = AJS.$('<p>', {
        text: "Error Code : " + errorCode
    });
    var errorStackTrace = AJS.$('<p>', {
        text: stackTrace
    });

    errorBody.append(errorMessage, errorCode, errorStackTrace);

    errorDialog.addPanel(header, errorBody, "panel-body");

    errorDialog.addButton("OK", function (dialog) {
        errorDialog.hide();
    });

    errorDialog.show();
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

function putAccessConfig(restUrl, successMessage, failureMessage) {
    const hubJiraGroups = encodeURI(AJS.$("#" + hubJiraGroupsId).val());

    AJS.$.ajax({
        url: restUrl,
        type: "PUT",
        dataType: "json",
        contentType: "application/json",
        data: '{ "hubJiraGroups": "' + hubJiraGroups
            + '"}',
        processData: false,
        success: function () {
            hideError('hubJiraGroupsError');
            showStatusMessage(successStatus, 'Success!', successMessage);
            initCreatorCandidates();
        },
        error: function (response) {
            try {
                var admin = JSON.parse(response.responseText);
                handleError('hubJiraGroupsError', admin.hubJiraGroupsError, true, true);

                showStatusMessage(errorStatus, 'ERROR!', failureMessage);
            } catch (err) {
                // in case the response is not our error object
                alert(response.responseText);
            }
        },
        complete: function (jqXHR, textStatus) {
            stopProgressSpinner('adminSaveSpinner');
        }
    });
}

function putFieldCopyConfig(restUrl, successMessage, failureMessage) {
    console.log("putFieldCopyConfig()");
    const jsonFieldCopyMappingArray = getJsonArrayFromFieldCopyMapping();
    console.log("jsonFieldCopyMappingArray: " + jsonFieldCopyMappingArray);

    AJS.$.ajax({
        url: restUrl,
        type: "PUT",
        dataType: "json",
        contentType: "application/json",
        data: '{ "projectFieldCopyMappings": '
            + jsonFieldCopyMappingArray
            + ' }',
        processData: false,
        success: function () {
            hideError('hubJiraGroupsError');

            showStatusMessage(successStatus, 'Success!', successMessage);
        },
        error: function (response) {
            try {
                var admin = JSON.parse(response.responseText);
                handleError('hubJiraGroupsError', admin.hubJiraGroupsError, true, true);

                showStatusMessage(errorStatus, 'ERROR!', failureMessage);
            } catch (err) {
                // in case the response is not our error object
                alert(response.responseText);
            }
        },
        complete: function (jqXHR, textStatus) {
            stopProgressSpinner('adminSaveSpinner');
        }
    });
}

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
    AJS.$.ajax({
        url: restUrl,
        type: "PUT",
        dataType: "json",
        contentType: "application/json",
        data: '{ "intervalBetweenChecks": "' + encodeURI(AJS.$("#intervalBetweenChecks").val())
            + '", "creator": "' + creatorUsername
            + '", "hubProjectMappings": ' + jsonMappingArray
            + ', "policyRules": ' + policyRuleArray
            + ', "createVulnerabilityIssues": ' + createVulnerabilityIssues
            + '}',
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

function getJsonArrayFromMapping() {
    let jsonArray = "[";
    const mappingContainer = AJS.$("#" + hubProjectMappingContainer);
    const mappingElements = mappingContainer.find("tr[name*='" + hubProjectMappingElement + "']");
    for (i = 1; i < mappingElements.length; i++) {
        if (i > 1) {
            jsonArray += ","
        }
        let mappingElement = mappingElements[i];
        let currentJiraProject = AJS.$(mappingElement).find("input[name*='jiraProject']");

        let currentJiraProjectDisplayName = currentJiraProject.val();
        let currentJiraProjectValue = currentJiraProject.attr('projectKey');
        let currentJiraProjectError = currentJiraProject.attr('projectError');

        let currentIssueCreator = AJS.$(mappingElement).find("input[name*='issueCreator']");
        let currentJiraProjectIssueCreator = currentIssueCreator.val();

        let currentHubProject = AJS.$(mappingElement).find("input[name*='hubProject']");

        let currentHubProjectDisplayName = currentHubProject.val();
        let currentHubProjectError = currentHubProject.attr('projectError');


        if (isNullOrWhitespace(currentJiraProjectValue) || currentJiraProjectError || currentHubProjectError) {
            addMappingErrorStatus(mappingElement);
        } else {
            removeMappingErrorStatus(mappingElement);
        }

        jsonArray += '{'
            + '"jiraProject" : {"'
            + jiraProjectDisplayName + '":"' + currentJiraProjectDisplayName
            + '","'
            + jiraProjectKey + '":"' + currentJiraProjectValue
            + '","'
            + jiraProjectIssueCreatorDisplayName + '":"' + currentJiraProjectIssueCreator
            + '"},'
            + '"blackDuckProjectName" : "'
            + currentHubProjectDisplayName
            + '"}';
    }
    jsonArray += "]";
    return jsonArray;
}

function getJsonArrayFromFieldCopyMapping() {
    console.log("getJsonArrayFromFieldCopyMapping()");
    let jsonArray = "[";
    const mappingContainer = AJS.$("#" + fieldCopyMappingContainer);
    const mappingElements = mappingContainer.find("tr[name*='" + fieldCopyMappingElement + "']");
    console.log("mappingElements.length: " + mappingElements.length);
    let numRowsAdded = 0;
    for (i = 0; i < mappingElements.length; i++) {
        let mappingElement = mappingElements[i];
        let currentSourceField = AJS.$(mappingElement).find("input[name*='sourceField']");

        let currentSourceFieldDisplayName = currentSourceField.val();
        let currentSourceFieldId = currentSourceField.attr('id');

        let currentTargetField = AJS.$(mappingElement).find("input[name*='targetField']");

        let currentTargetFieldDisplayName = currentTargetField.val();
        let currentTargetFieldId = currentTargetField.attr('id');
        let currentTargetFieldError = currentTargetField.attr('fieldError');

        if (isNullOrWhitespace(currentSourceFieldId) && isNullOrWhitespace(currentTargetFieldId)) {
            console.log("Skipping empty field copy mapping row");
            addMappingErrorStatus(mappingElement);
        } else {
            console.log("Adding field copy mapping row to data for server");
            removeFieldCopyMappingErrorStatus(mappingElement);
            if (numRowsAdded > 0) {
                jsonArray += ",";
            }
            jsonArray += '{ '
                + '"jiraProjectName": "*", '
                + '"hubProjectName": "*", '
                + '"sourceFieldId": "' + currentSourceFieldId + '", '
                + '"sourceFieldName": "' + currentSourceFieldDisplayName + '", '
                + '"targetFieldId": "' + currentTargetFieldId + '", '
                + '"targetFieldName": "' + currentTargetFieldDisplayName + '" '
                + '} ';
            numRowsAdded++;
        }
    }
    jsonArray += "]";
    return jsonArray;
}

function getJsonArrayFromPolicyRules() {
    let jsonArray = "[";
    const policyRuleContainer = AJS.$("#" + policyRuleTicketCreation);
    const policyRules = policyRuleContainer.find("input");
    for (i = 0; i < policyRules.length; i++) {
        if (i > 0) {
            jsonArray += ","
        }
        let policyRule = AJS.$(policyRules[i]);

        let currentPolicyRuleUrl = policyRule.attr("policyurl");
        // Names and descriptions with chars like ", <, and > cause problems on save
        // and we don't actually use them; just omitting them for now
//		var currentPolicyRuleDescription = policyRule.attr("title");
//		var currentPolicyRuleName = policyRule.attr("name");
        let currentPolicyRuleChecked = policyRules[i].checked;
        console.log("Constructing rules jsonArray, but omitting name");
        jsonArray += '{"'
            + policyRuleName + '":"' + "name omitted by hub-jira.js"
            + '","'
            + policyRuleUrl + '":"' + currentPolicyRuleUrl
            + '","'
            + policyRuleDescription + '":"' + "description omitted by hub-jira.js"
            + '","'
            + policyRuleChecked + '":"' + currentPolicyRuleChecked
            + '"}';
    }
    jsonArray += "]";
    return jsonArray;
}

function updateValue(fieldId, configField) {
    if (configField) {
        const fieldObject = AJS.$("#" + fieldId);
        if (fieldObject.type == "checkbox" || (fieldObject[0] && fieldObject[0].type == "checkbox")) {
            fieldObject.prop("checked", decodeURI(configField));
        } else {
            fieldObject.val(decodeURI(configField));
        }
    }
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

function removeAllChildren(parent) {
    for (let i = parent.children.length - 1; i >= 0; i--) {
        parent.children[i].remove();
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

function fillInJiraProjects(jiraProjects) {
    const mappingElement = AJS.$("#" + hubProjectMappingElement);
    const jiraProjectList = mappingElement.find("datalist[id='" + jiraProjectListId + "']");
    if (jiraProjects != null && jiraProjects.length > 0) {
        for (let j = 0; j < jiraProjects.length; j++) {
            jiraProjectMap.set(String(jiraProjects[j].projectId), jiraProjects[j]);
            let newOption = AJS.$('<option>', {
                value: jiraProjects[j].projectName,
                projectKey: String(jiraProjects[j].projectId),
                projectError: jiraProjects[j].projectError
            });

            jiraProjectList.append(newOption);
        }
    }
}

function clearList(list) {
    for (let i = list.options.length - 1; i >= 0; i--) {
        list.options[i].remove(i);
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

function fillInPluginVersion(pluginVersion) {
    console.log("fillInPluginVersion(): pluginVersion: " + pluginVersion);
    const pluginVersionElements = AJS.$("#" + "pluginVersion");
    for (let i = 0; i < pluginVersionElements.length; i++) {
        pluginVersionElements[i].innerHTML = pluginVersion;
    }
}

function fillInSourceFields(sourceFields) {
    const mappingElement = AJS.$("#" + fieldCopyMappingElement);
    console.log("fieldCopyMappingElement: " + mappingElement);
    const sourceFieldList = mappingElement.find("datalist[id='" + sourceFieldListId + "']");
    if ((sourceFields != null) && (sourceFields.idToNameMappings != null)) {
        for (let i = 0; i < sourceFields.idToNameMappings.length; i++) {
            let sourceFieldIdToNameMapping = sourceFields.idToNameMappings[i];
            console.log("Adding source field: Field ID: " + sourceFieldIdToNameMapping.id + "; Name: " + sourceFieldIdToNameMapping.name);
            let newOption = AJS.$('<option>', {
                value: sourceFieldIdToNameMapping.name,
                id: sourceFieldIdToNameMapping.id,
                fieldError: ""
            });
            sourceFieldList.append(newOption);
        }
    }
}

function fillInTargetFields(targetFields) {
    const mappingElement = AJS.$("#" + fieldCopyMappingElement);
    console.log("fieldCopyMappingElement: " + mappingElement);
    const targetFieldList = mappingElement.find("datalist[id='" + targetFieldListId + "']");
    if ((targetFields != null) && (targetFields.idToNameMappings != null)) {
        for (let i = 0; i < targetFields.idToNameMappings.length; i++) {
            let targetFieldIdToNameMapping = targetFields.idToNameMappings[i];
            console.log("Adding target field: Field ID: " + targetFieldIdToNameMapping.id + "; Name: " + targetFieldIdToNameMapping.name);
            let newOption = AJS.$('<option>', {
                value: targetFieldIdToNameMapping.name,
                id: targetFieldIdToNameMapping.id,
                fieldError: ""
            });
            targetFieldList.append(newOption);
        }
    }
}

function fillInMappings(storedMappings) {
    const mappingContainer = AJS.$("#" + hubProjectMappingContainer);
    const mappingElements = mappingContainer.find("tr[name*='" + hubProjectMappingElement + "']");
    // On loading the page, there should only be one original mapping element
    if (storedMappings != null && storedMappings.length > 0) {
        for (let i = 0; i < storedMappings.length; i++) {
            let newMappingElement = addNewMappingElement(hubProjectMappingElement);
            fillInMapping(newMappingElement, storedMappings[i]);
        }
    } else {
        addNewMappingElement(hubProjectMappingElement);
    }
}

function fillInFieldCopyMappings(storedMappings) {
    const mappingContainer = AJS.$("#" + fieldCopyMappingContainer);
    const mappingElements = mappingContainer.find("tr[name*='" + fieldCopyMappingElement + "']");
    // On loading the page, there should only be one original mapping element
    if (storedMappings != null && storedMappings.length > 0) {
        for (let i = 0; i < storedMappings.length; i++) {
            let newMappingElement = addNewFieldCopyMappingElement(fieldCopyMappingElement);
            fillInFieldCopyMapping(newMappingElement, storedMappings[i]);
        }
    } else {
        addNewFieldCopyMappingElement(fieldCopyMappingElement);
    }
}

function fillInMapping(mappingElement, storedMapping) {
    const currentJiraProject = AJS.$(mappingElement).find("input[name*='jiraProject']");

    const storedJiraProject = storedMapping.jiraProject;
    const storedJiraProjectDisplayName = storedJiraProject.projectName;
    const storedJiraProjectValue = storedJiraProject.projectId;
    const storedJiraProjectError = storedJiraProject.projectError;

    currentJiraProject.val(storedJiraProjectDisplayName);
    currentJiraProject.attr("projectKey", storedJiraProjectValue);

    const currentIssueCreator = AJS.$(mappingElement).find("input[name*='issueCreator']");
    currentIssueCreator.val(storedJiraProject.issueCreator);

    const currentHubProject = AJS.$(mappingElement).find("input[name*='hubProject']");

    const storedBlackDuckProject = storedMapping.blackDuckProjectName;

    currentHubProject.val(storedBlackDuckProject);
    currentHubProject.attr("projectKey", storedBlackDuckProject);
}

function fillInFieldCopyMapping(mappingElement, storedMapping) {
    const currentSourceField = AJS.$(mappingElement).find("input[name*='sourceField']");

    const storedSourceFieldId = storedMapping.sourceFieldId;
    const storedSourceFieldName = storedMapping.sourceFieldName;

    currentSourceField.val(storedSourceFieldName);
    currentSourceField.attr("id", storedSourceFieldId);

    const currentTargetField = AJS.$(mappingElement).find("input[name*='targetField']");

    const storedTargetFieldId = storedMapping.targetFieldId;
    const storedTargetFieldName = storedMapping.targetFieldName;

    currentTargetField.val(storedTargetFieldName);
    currentTargetField.attr("id", storedTargetFieldId);
}

function addNewMappingElement(fieldId) {
    const elementToAdd = AJS.$("#" + fieldId).clone();
    mappingElementCounter = mappingElementCounter + 1;
    elementToAdd.attr("id", elementToAdd.attr("id") + mappingElementCounter);
    elementToAdd.appendTo("#" + hubProjectMappingContainer);

    removeClassFromField(elementToAdd, hiddenClass);

    removeMappingErrorStatus(elementToAdd);

    const currentJiraProject = AJS.$(elementToAdd).find("input[name*='jiraProject']");

    currentJiraProject.val("");
    currentJiraProject.attr("projectKey", "");
    if (currentJiraProject.hasClass('error')) {
        currentJiraProject.removeClass('error');
    }
    const currentJiraProjectParent = currentJiraProject.parent();
    const currentJiraProjectError = currentJiraProjectParent.children("#" + jiraProjectErrorId);
    currentJiraProjectError.text("");
    if (currentJiraProjectError.hasClass('error')) {
        currentJiraProjectError.removeClass('error');
    }

    const currentHubProject = AJS.$(elementToAdd).find("input[name*='hubProject']");

    currentHubProject.val("");
    currentHubProject.attr("projectKey", "");
    if (currentHubProject.hasClass('error')) {
        currentHubProject.removeClass('error');
    }

    const mappingArea = AJS.$('#mappingArea')[0];
    if (mappingArea) {
        AJS.$('#mappingArea').scrollTop(mappingArea.scrollHeight);
    }
    return elementToAdd;
}

function addNewFieldCopyMappingElement(fieldId) {
    const elementToAdd = AJS.$("#" + fieldId).clone(); // TODO typo?
    mappingElementCounter = mappingElementCounter + 1;
    elementToAdd.attr("id", elementToAdd.attr("id") + mappingElementCounter);
    elementToAdd.appendTo("#" + fieldCopyMappingContainer);

    removeClassFromField(elementToAdd, hiddenClass);

    removeMappingErrorStatus(elementToAdd);

    const currentSourceField = AJS.$(elementToAdd).find("input[name*='sourceField']");

    currentSourceField.val("");
    currentSourceField.attr("id", "");
    if (currentSourceField.hasClass('fieldError')) {
        currentSourceField.removeClass('fieldError');
    }
    const currentSourceFieldParent = currentSourceField.parent();
    const currentTargetField = AJS.$(elementToAdd).find("input[name*='hubProject']");
    currentTargetField.val("");
    currentTargetField.attr("id", "");

    const mappingArea = AJS.$('#fieldCopyMappingArea')[0];
    if (mappingArea) {
        AJS.$('#fieldCopyMappingArea').scrollTop(fieldCopyMappingArea.scrollHeight);
    }
    return elementToAdd;
}

function removeMappingElement(childElement) {
    if (AJS.$("#" + hubProjectMappingContainer).find("tr[name*='" + hubProjectMappingElement + "']").length > 1) {
        AJS.$(childElement).closest("tr[name*='" + hubProjectMappingElement + "']").remove();
    }
}

function removeFieldCopyMappingElement(childElement) {
    if (AJS.$("#" + fieldCopyMappingContainer).find("tr[name*='" + fieldCopyMappingElement + "']").length > 1) {
        AJS.$(childElement).closest("tr[name*='" + fieldCopyMappingElement + "']").remove();
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

function handleError(fieldId, configField, hideErrorValue, clearOldMessage) {
    if (configField) {
        showError(fieldId, configField, clearOldMessage);
    } else if (hideErrorValue) {
        hideError(fieldId);
    } else {
        showError(fieldId, "", true);
    }
}

function showErrorHubDetails(fieldRowId, fieldId, configField) {
    AJS.$("#" + fieldId).text(decodeURI(configField));
    removeClassFromFieldById(fieldRowId, hiddenClass);
}

function showError(fieldId, configField, clearOldMessage) {
    let newMessage = decodeURI(configField).trim();
    if (!clearOldMessage) {
        const oldMessage = AJS.$("#" + fieldId).text().trim();
        if (oldMessage && oldMessage != newMessage) {
            newMessage = oldMessage + ' .... ' + newMessage;
        }
    }
    AJS.$("#" + fieldId).text(newMessage);
    removeClassFromFieldById(fieldId, hiddenClass);
}

function hideError(fieldId) {
    if (fieldId != errorMessageFieldId) {
        AJS.$("#" + fieldId).text('');
        addClassToFieldById(fieldId, hiddenClass);
    }
}

function hideError(fieldRowId, fieldId) {
    AJS.$("#" + fieldId).text('');
    addClassToFieldById(fieldRowId, hiddenClass);
}

function showStatusMessage(status, statusTitle, message) {
    resetStatusMessage();
    if (status == errorStatus) {
        addClassToFieldById(statusMessageFieldId, 'error');
        addClassToFieldById(statusMessageTitleId, 'icon-error');
    } else if (status == successStatus) {
        addClassToFieldById(statusMessageFieldId, 'success');
        addClassToFieldById(statusMessageTitleId, 'icon-success');
    }
    AJS.$("#" + statusMessageTitleTextId).text(statusTitle);
    AJS.$("#" + statusMessageTextId).text(message);
    removeClassFromFieldById(statusMessageFieldId, hiddenClass);
}

function resetStatusMessage() {
    removeClassFromFieldById(statusMessageFieldId, 'error');
    removeClassFromFieldById(statusMessageFieldId, 'success');
    removeClassFromFieldById(statusMessageTitleId, 'icon-error');
    removeClassFromFieldById(statusMessageTitleId, 'icon-success');
    AJS.$("#" + statusMessageTitleTextId).text('');
    AJS.$("#" + statusMessageTextId).text('');
    addClassToFieldById(statusMessageFieldId, hiddenClass);
}

function addClassToFieldById(fieldId, cssClass) {
    if (!AJS.$("#" + fieldId).hasClass(cssClass)) {
        AJS.$("#" + fieldId).addClass(cssClass);
    }
}

function removeClassFromFieldById(fieldId, cssClass) {
    if (AJS.$("#" + fieldId).hasClass(cssClass)) {
        AJS.$("#" + fieldId).removeClass(cssClass);
    }
}

function addClassToField(field, cssClass) {
    if (!AJS.$(field).hasClass(cssClass)) {
        AJS.$(field).addClass(cssClass);
    }
}

function removeClassFromField(field, cssClass) {
    if (AJS.$(field).hasClass(cssClass)) {
        AJS.$(field).removeClass(cssClass);
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

function addMappingErrorStatus(mappingElement) {
    const mappingStatus = AJS.$(mappingElement).find("#" + hubMappingStatus);
    if (mappingStatus.find("i").length == 0) {
        const newStatus = AJS.$('<i>', {});
        AJS.$(newStatus).addClass("error");
        AJS.$(newStatus).addClass("largeIcon");
        AJS.$(newStatus).addClass("fa");
        AJS.$(newStatus).addClass("fa-exclamation");

        newStatus.appendTo(mappingStatus);
    }
}

function removeMappingErrorStatus(mappingElement) {
    const mappingStatus = AJS.$(mappingElement).find("#" + hubMappingStatus);
    if (mappingStatus.children().length > 0) {
        AJS.$(mappingStatus).empty();
    }
}

function removeFieldCopyMappingErrorStatus(mappingElement) {
    const mappingStatus = AJS.$(mappingElement).find("#" + fieldCopyMappingStatus);
    if (mappingStatus.children().length > 0) {
        AJS.$(mappingStatus).empty();
    }
}

function startProgressSpinner(spinnerId) {
    const spinner = AJS.$('#' + spinnerId);

    if (spinner.find("i").length == 0) {
        const newSpinnerIcon = AJS.$('<i>', {});
        AJS.$(newSpinnerIcon).addClass("largeIcon");
        AJS.$(newSpinnerIcon).addClass("fa");
        AJS.$(newSpinnerIcon).addClass("fa-spinner");
        AJS.$(newSpinnerIcon).addClass("fa-spin");
        AJS.$(newSpinnerIcon).addClass("fa-fw");

        newSpinnerIcon.appendTo(spinner);
    }
}

function stopProgressSpinner(spinnerId) {
    const spinner = AJS.$('#' + spinnerId);
    if (spinner.children().length > 0) {
        AJS.$(spinner).empty();
    }
}

function isNullOrWhitespace(input) {
    if (input == null) {
        return true;
    }
    if (input == undefined) {
        return true;
    }
    if (input == "undefined") {
        return true;
    }
    input = String(input);
    return input.trim().length < 1;
}

(function ($) {

    $(document).ready(function () {
        console.log("DOM loaded");
        populateForm();
        initTabs();
    });

})(AJS.$ || jQuery);
