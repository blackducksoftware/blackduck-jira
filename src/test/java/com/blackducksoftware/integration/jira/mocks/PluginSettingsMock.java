/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
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
 *******************************************************************************/
package com.blackducksoftware.integration.jira.mocks;

import java.util.HashMap;

import com.atlassian.sal.api.pluginsettings.PluginSettings;

public class PluginSettingsMock implements PluginSettings {

    private final HashMap<String, Object> settings = new HashMap<String, Object>();

    @Override
    public Object get(final String key) {
        return settings.get(key);
    }

    @Override
    public Object put(final String key, final Object value) {
        return settings.put(key, value);
    }

    @Override
    public Object remove(final String key) {
        return settings.remove(key);
    }

}
