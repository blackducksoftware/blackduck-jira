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
package com.blackducksoftware.integration.jira.dal;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.atlassian.sal.api.pluginsettings.PluginSettings;

public class JiraSettingsAccessor {
    private final PluginSettings pluginSettings;

    public JiraSettingsAccessor(final PluginSettings pluginSettings) {
        this.pluginSettings = pluginSettings;
    }

    public PluginConfigurationAccessor createPluginConfigurationAccessor() {
        return new PluginConfigurationAccessor(this);
    }

    public PluginErrorAccessor createPluginErrorAccessor() {
        return new PluginErrorAccessor(this);
    }

    public GlobalConfigurationAccessor createGlobalConfigurationAccessor() {
        return new GlobalConfigurationAccessor(this);
    }

    public Object getObjectValue(final String key) {
        return pluginSettings.get(key);
    }

    public String getStringValue(final String key) {
        final Object foundObject = pluginSettings.get(key);
        if (foundObject == null) {
            return null;
        }
        return String.valueOf(foundObject);
    }

    public Optional<Integer> getIntegerValue(final String key) {
        final String value = getStringValue(key);
        if (NumberUtils.isParsable(value)) {
            return Optional.of(Integer.parseInt(value));
        }
        return Optional.empty();
    }

    public Boolean getBooleanValue(final String key) {
        return getBooleanValue(key, false);
    }

    public Boolean getBooleanValue(final String key, final Boolean defaultValue) {
        final String stringValue = getStringValue(key);
        if (StringUtils.isBlank(stringValue)) {
            return defaultValue;
        }
        return Boolean.parseBoolean(stringValue);
    }

    public void setValue(final String key, final Object value) {
        if (value == null) {
            pluginSettings.remove(key);
        } else {
            pluginSettings.put(key, String.valueOf(value));
        }
    }

}
