/*
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2019 Synopsys, Inc.
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
// Constant Strings
const statusMessageFieldId = "aui-hub-message-field";
const statusMessageTitleId = "aui-hub-message-title";
const statusMessageTitleTextId = "aui-hub-message-title-text";
const statusMessageTextId = "aui-hub-message-text";
const errorMessageFieldId = "error-message-field";
const errorStatus = "error";
const successStatus = "success";
const hiddenClass = "hidden";
const sourceFieldListId = "sourceFields";
const targetFieldListId = "targetFields";

const hubProjectListId = "hubProjects";
const hubProjectListErrorId = "hubProjectListError";
const hubProjectMappingContainer = "hubProjectMappingContainer";
const hubProjectMappingElement = "hubProjectMappingElement";
const hubProjectMappingTable = "hubProjectMappingTable";
const hubJiraGroupsId = "hubJiraGroups";
const hubMappingStatus = "mappingStatus";

const ticketCreationFieldSetId = "ticketCreationFieldSet";
const ticketCreationErrorsTableId = "ticketCreationErrorsTable";
const ticketCreationErrorRowId = "ticketCreationErrorRow";

const fieldCopyMappingContainer = "fieldCopyMappingContainer";
const fieldCopyMappingElement = "fieldCopyMappingElement";
const fieldCopyMappingStatus = "fieldCopyMappingStatus";

const jiraProjectListErrorId = "jiraProjectListError";
const jiraProjectListId = "jiraProjects";
const jiraProjectErrorId = "jiraProjectError";
const jiraProjectDisplayName = "projectName";
const jiraProjectKey = "projectId";
const jiraProjectIssueCreatorDisplayName = "issueCreator";
const jiraProjectConfiguredForVulnerabilitiesDisplayName = "configuredForVulnerabilities";

const policyRuleTicketCreation = "policyRuleTicketCreation";
const policyRuleName = "name";
const policyRuleDescription = "description";
const policyRuleUrl = "policyUrl";
const policyRuleChecked = "checked";

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

function createRequestPath(relativePath) {
    return AJS.contextPath() + "/rest/blackduck-jira-integration/" + relativePath;
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

function resetStatusMessage() {
    removeClassFromFieldById(statusMessageFieldId, 'error');
    removeClassFromFieldById(statusMessageFieldId, 'success');
    removeClassFromFieldById(statusMessageTitleId, 'icon-error');
    removeClassFromFieldById(statusMessageTitleId, 'icon-success');
    AJS.$("#" + statusMessageTitleTextId).text('');
    AJS.$("#" + statusMessageTextId).text('');
    addClassToFieldById(statusMessageFieldId, hiddenClass);
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

function clearList(list) {
    for (let i = list.options.length - 1; i >= 0; i--) {
        list.options[i].remove(i);
    }
}

function removeAllChildren(parent) {
    for (let i = parent.children.length - 1; i >= 0; i--) {
        parent.children[i].remove();
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

function redirectIfUnauthenticated(response) {
    let status = response.status;
    if (401 == status) {
        location.reload(true);
        return true;
    }
    return false;
}
