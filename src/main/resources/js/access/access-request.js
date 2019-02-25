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
function readAdminData() {
    AJS.$.ajax({
        url: createRequestPath('admin/'),
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
}

function updateAccessConfig() {
    putAccessConfig(createRequestPath('admin'), 'Save successful.', 'The configuration is not valid.');
}

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
