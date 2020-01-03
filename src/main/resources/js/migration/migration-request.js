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

function readMigrationDetails() {
    AJS.$.ajax({
        url: createRequestPath('blackduck/migration/details/'),
        dataType: "json",
        success: function (details) {
            updateDiv("migrationStartTime", details.migrationStartTime);
            updateDiv("migrationEndTime", details.migrationEndTime);
            updateDiv("migrationStatus", details.migrationStatus);

            addProjectsToBeMigrated(details.projectsToMigrate)
            addAlreadyMigratedProjects(details.migratedProjects)
        },
        error: function (response) {
            handleDataRetrievalError(response, "migrationDetailsError", "There was a problem retrieving the Migration Details.", "Migration Details Error");
        },
        complete: function (jqXHR, textStatus) {
            console.debug("Completed the get of the Migration Details: " + textStatus);
        }
    });
}

function removeMigratedProject(project) {
    AJS.$.ajax({
        url: createRequestPath('blackduck/migration/project/'),
        type: "DELETE",
        dataType: "json",
        contentType: "application/json",
        data: '{ "projectToDelete": ' + project
            + '}',
        success: function () {
        },
        error: function (response) {
            handleDataRetrievalError(response, "removeMigratedProjectError", "There was a problem removing the migrated project.", "Migrated Project Remove Error");
        },
        complete: function (jqXHR, textStatus) {
            console.debug("Completed the Migrated Project removal: " + textStatus);
            readMigrationDetails();
        }
    });
}

function startMigration() {
    AJS.$.ajax({
        url: createRequestPath('blackduck/migration/'),
        type: "POST",
        dataType: "json",
        contentType: "application/json",
        data: '{}',
        success: function () {
        },
        error: function (response) {
            handleError('startMigrationError', response, true, false);
        },
        complete: function (jqXHR, textStatus) {
            console.debug("Completed the start Migration: " + textStatus);
            readMigrationDetails();
        }
    });
}

function updateDiv(fieldId, configField) {
    if (configField) {
        const fieldObject = AJS.$("#" + fieldId);
        fieldObject.text(decodeURI(configField));
    }
}