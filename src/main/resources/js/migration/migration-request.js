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
function readMigrationStatus() {
    AJS.$.ajax({
        url: createRequestPath('blackduck/migration/status/'),
        dataType: "json",
        success: function (migrationStatus) {
            updateValue("migrationStatus", migrationStatus);
        },
        error: function (response) {
            handleDataRetrievalError(response, "migrationStatusError", "There was a problem retrieving the Status.", "Migration Status Error");
        },
        complete: function (jqXHR, textStatus) {
            console.debug("Completed get of the Migration Status: " + textStatus);
        }
    });
}

function readMigrationStartTime() {
    AJS.$.ajax({
        url: createRequestPath('blackduck/migration/start/'),
        dataType: "json",
        success: function (migrationStartTime) {
            updateValue("migrationStartTime", migrationStartTime);
        },
        error: function (response) {
            handleDataRetrievalError(response, "migrationStartTimeError", "There was a problem retrieving the Migration Start Time.", "Migration Start Time Error");
        },
        complete: function (jqXHR, textStatus) {
            console.debug("Completed get of the Migration Start Time: " + textStatus);
        }
    });
}

function readMigrationEndTime() {
    AJS.$.ajax({
        url: createRequestPath('blackduck/migration/end/'),
        dataType: "json",
        success: function (migrationEndTime) {
            updateValue("migrationEndTime", migrationEndTime);
        },
        error: function (response) {
            handleDataRetrievalError(response, "migrationEndTimeError", "There was a problem retrieving the Migration End Time.", "Migration End Time Error");
        },
        complete: function (jqXHR, textStatus) {
            console.debug("Completed get of the Migration End Time: " + textStatus);
        }
    });
}