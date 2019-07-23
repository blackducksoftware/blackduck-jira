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
function closeOldIssues() {
    startProgressSpinner('managementProgressSpinner');

    const oldUrl = $("input[name=jiraCleanOldIssues]").val();
    const requestData = Object.assign({}, {
        oldUrl: oldUrl
    });

    AJS.$.ajax({
        url: createRequestPath('config/management'),
        dataType: "json",
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify(requestData),
        processData: false,
        success: function (config) {
            console.log("Successful issue update.");
        },
        error: function (response) {
            console.log("postConfig(): " + response.responseText);
            alert("There was an error updating issues.");
        },
        complete: function (jqXHR, textStatus) {
            stopProgressSpinner('managementProgressSpinner');
            console.log("Completed get of hub details: " + textStatus);
        }
    });
}
