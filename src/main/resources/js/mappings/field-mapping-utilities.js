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
let mappingElementCounter = 0;
let gotProjectMappings = false;
let gotSourceFields = false;
let gotTargetFields = false;
let gotFieldCopyMappings = false;

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

function addNewFieldCopyMappingElement(fieldId) {
    const elementToAdd = AJS.$("#" + fieldId).clone();
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


function removeFieldCopyMappingElement(childElement) {
    if (AJS.$("#" + fieldCopyMappingContainer).find("tr[name*='" + fieldCopyMappingElement + "']").length > 1) {
        AJS.$(childElement).closest("tr[name*='" + fieldCopyMappingElement + "']").remove();
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

    const configuredForVulnerabilities = AJS.$(mappingElement).find("input[name*='configuredForVulnerabilitiesOption']");
    console.log("Setting val: " + configuredForVulnerabilities);
    configuredForVulnerabilities.prop("checked", storedJiraProject.configuredForVulnerabilities);

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

function getJsonArrayFromFieldCopyMapping() {
    console.log("getJsonArrayFromFieldCopyMapping()");
    let jsonArray = [];
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

            jsonArray.push({
                jiraProjectName: "*",
                hubProjectName: "*",
                sourceFieldId: currentSourceFieldId,
                sourceFieldName: currentSourceFieldDisplayName,
                targetFieldId: currentTargetFieldId,
                targetFieldName: currentTargetFieldDisplayName
            });
            numRowsAdded++;
        }
    }
    return jsonArray;
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
