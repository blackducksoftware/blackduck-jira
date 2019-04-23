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
let hubProjectMap = new Map();
let gotHubProjects = false;

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
    const proxyUsername = AJS.$("#proxyUsername").val();
    const proxyPassword = AJS.$("#proxyPassword").val();

    if (!proxyHost && !proxyPort && !proxyUsername && !proxyPassword) {
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
            blackDuckProjectState.push(hubProjects[h]);
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
