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

function addProjectsToBeMigrated(projectsToMigrate) {
    const projectToBeMigratedTable = AJS.$('#projectToBeMigrated');
    removeAllChildren(projectToBeMigratedTable[0]);
    if (projectsToMigrate != null && projectsToMigrate.length > 0) {
        for (let pTM = 0; pTM < projectsToMigrate.length; pTM++) {
            const projectName = projectsToMigrate[pTM];
            if (projectName && !(/^\s*$/.test(projectName))) {
                let row = AJS.$('<tr>', {});
                let cell = AJS.$('<td>', {
                    text: projectName
                });
                cell.addClass('textStyle');
                cell.addClass('migrationTableCell');

                row.append(cell)

                row.appendTo(projectToBeMigratedTable);
            }
        }
    }
}

function addAlreadyMigratedProjects(migratedProjects) {
    const projectsAlreadyMigratedTable = AJS.$('#projectsAlreadyMigrated');
    removeAllChildren(projectsAlreadyMigratedTable[0]);
    if (migratedProjects != null && migratedProjects.length > 0) {
        for (let mP = 0; mP < migratedProjects.length; mP++) {
            const projectName = migratedProjects[mP];
            if (projectName && !(/^\s*$/.test(projectName))) {
                let row = AJS.$('<tr>', {});
                let checkBoxCell = AJS.$('<td>', {});
                let checkBox = AJS.$('<input type="checkbox">', {});
                checkBoxCell.append(checkBox)
                checkBoxCell.addClass('migrationTableCell');
                row.append(checkBoxCell)

                let cell = AJS.$('<td>', {
                    text: projectName
                });
                cell.addClass('textStyle');
                cell.addClass('migrationTableCell');

                row.append(cell)

                row.appendTo(projectsAlreadyMigratedTable);
            }
        }
    }
}

function selectAllMigratedProjects(selectAllCheckbox) {
    const projectsAlreadyMigratedTable = AJS.$('#projectsAlreadyMigrated');
    AJS.$('td input:checkbox', projectsAlreadyMigratedTable).prop('checked', selectAllCheckbox.checked);
}

function removeSelectedMigratedProjects(deleteIcon) {
    const confirmation = confirm("Are you sure you want to remove these projects?");
    if (confirmation) {
        const projectsAlreadyMigratedTable = AJS.$('#projectsAlreadyMigrated');
        const selectedProjects = AJS.$('td input:checkbox:checked', projectsAlreadyMigratedTable).closest("td").siblings("td");

        let projects = [];
        selectedProjects.each(function (index, element) {
            var project = AJS.$(element);
            const text = project.text();
            projects[index] = text;
        });
        removeMigratedProjects(projects);
    }
}