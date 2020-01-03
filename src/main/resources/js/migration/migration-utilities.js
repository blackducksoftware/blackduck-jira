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
    const projectToBeMigratedContainer = AJS.$('#projectToBeMigrated');
    removeAllChildren(projectToBeMigratedContainer[0]);
    if (projectsToMigrate != null && projectsToMigrate.length > 0) {
        for (let pTM = 0; pTM < projectsToMigrate.length; pTM++) {
            let newProjectToMigrate = AJS.$('<div>', {});

            let newPolicyLabel = AJS.$('<label>', {
                text: projectsToMigrate[pTM]
            });
            newPolicyLabel.addClass('textStyle');
            newPolicyLabel.css('padding', '0px 5px 0px 5px')

            newProjectToMigrate.append(newPolicyLabel)

            newProjectToMigrate.appendTo(projectToBeMigratedContainer);
        }
    }
}

function addAlreadyMigratedProjects(migratedProjects) {
    const projectsAlreadyMigratedContainer = AJS.$('#projectsAlreadyMigrated');
    removeAllChildren(projectsAlreadyMigratedContainer[0]);
    if (migratedProjects != null && migratedProjects.length > 0) {
        for (let mP = 0; mP < migratedProjects.length; mP++) {
            let newProjectToMigrate = AJS.$('<div>', {});

            let newPolicyLabel = AJS.$('<label>', {
                text: migratedProjects[mP]
            });
            newPolicyLabel.addClass('textStyle');
            newPolicyLabel.css('padding', '0px 5px 0px 5px')

            newProjectToMigrate.append(newPolicyLabel)

            newProjectToMigrate.appendTo(projectsAlreadyMigratedContainer);
        }
    }
}