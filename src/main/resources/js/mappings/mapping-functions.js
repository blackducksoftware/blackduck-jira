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

function readMappingData() {
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

function readSourceFields() {
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
}

function readTargetFields() {
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
}

function readFieldCopyMappings() {
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

function updateFieldCopyConfig() {
    console.log("updateFieldCopyConfig()");
    putFieldCopyConfig(AJS.contextPath() + '/rest/blackduck-jira-integration/1.0/updateFieldCopyMappings', 'Save successful.', 'The field copy configuration is not valid.');
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

