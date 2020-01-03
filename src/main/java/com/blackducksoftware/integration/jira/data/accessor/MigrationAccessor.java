/**
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
package com.blackducksoftware.integration.jira.data.accessor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.jira.data.PluginConfigKeys;

public class MigrationAccessor {
    private final JiraSettingsAccessor jiraSettingsAccessor;

    public MigrationAccessor(JiraSettingsAccessor jiraSettingsAccessor) {
        this.jiraSettingsAccessor = jiraSettingsAccessor;
    }

    public List<String> getMigratedProjects() {
        String storedMigratedProjects = jiraSettingsAccessor.getStringValue(PluginConfigKeys.PROJECTS_MIGRATED_TO_ALERT);
        if (null == storedMigratedProjects) {
            return new ArrayList<>();
        }
        return Stream.of(storedMigratedProjects.split(",")).collect(Collectors.toCollection(ArrayList::new));
    }

    public void updateMigratedProjects(List<String> migratedProjects) {
        if (null == migratedProjects || migratedProjects.isEmpty()) {
            jiraSettingsAccessor.setValue(PluginConfigKeys.PROJECTS_MIGRATED_TO_ALERT, "");
        }
        String migratedProjectsToStore = StringUtils.join(migratedProjects, ",");
        jiraSettingsAccessor.setValue(PluginConfigKeys.PROJECTS_MIGRATED_TO_ALERT, migratedProjectsToStore);
    }

}
