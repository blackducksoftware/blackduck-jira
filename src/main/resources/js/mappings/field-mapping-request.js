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
function readMappingData() {
    AJS.$.ajax({
        url: createRequestPath('config/issue/field/mapping'),
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
        url: createRequestPath('config/issue/field/mapping/sources'),
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
        url: createRequestPath('config/issue/field/mapping/targets'),
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
        url: createRequestPath('config/issue/field/mapping/copies'),
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

function updateFieldCopyConfig() {
    console.log("updateFieldCopyConfig()");
    putFieldCopyConfig(createRequestPath('config/issue/field/mapping'), 'Save successful.', 'The field copy configuration is not valid.');
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
            + JSON.stringify(jsonFieldCopyMappingArray)
            + ' }',
        processData: false,
        success: function () {
            hideError('hubJiraGroupsError');

            showStatusMessage(successStatus, 'Success!', successMessage);
        },
        error: function (response) {
            try {
                if (!redirectIfUnauthenticated(response)) {
                    let admin = JSON.parse(response.responseText);
                    handleError('hubJiraGroupsError', admin.hubJiraGroupsError, true, true);

                    showStatusMessage(errorStatus, 'ERROR!', failureMessage);
                }
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
