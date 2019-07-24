/**
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
package com.blackducksoftware.integration.jira.data.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.util.Stringable;

public class PluginGroupsConfigModel extends Stringable {
    public static final String BLACK_DUCK_GROUPS_LIST_DELIMETER = ",";

    private final Collection<String> groups;

    public static PluginGroupsConfigModel fromDelimitedString(final String delimitedString) {
        if (StringUtils.isNotBlank(delimitedString)) {
            final String[] groups = delimitedString.split(PluginGroupsConfigModel.BLACK_DUCK_GROUPS_LIST_DELIMETER);
            return PluginGroupsConfigModel.of(groups);
        }
        return none();
    }

    public static PluginGroupsConfigModel of(final String[] groups) {
        if (groups != null) {
            return new PluginGroupsConfigModel(Arrays.asList(groups));
        }
        return none();
    }

    public static PluginGroupsConfigModel none() {
        return new PluginGroupsConfigModel(Collections.emptySet());
    }

    private PluginGroupsConfigModel(final Collection<String> groups) {
        this.groups = groups;
    }

    public Collection<String> getGroups() {
        return groups;
    }

    public String getGroupsStringDelimited() {
        return StringUtils.join(groups, BLACK_DUCK_GROUPS_LIST_DELIMETER);
    }

}
