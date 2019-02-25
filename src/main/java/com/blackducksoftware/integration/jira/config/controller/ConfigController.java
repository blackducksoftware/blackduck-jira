/**
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
package com.blackducksoftware.integration.jira.config.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;

public class ConfigController {

    // This must be "package protected" to avoid synthetic access
    final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));
    final PluginSettingsFactory pluginSettingsFactory;
    private final TransactionTemplate transactionTemplate;
    private final Properties i18nProperties;

    public ConfigController(final PluginSettingsFactory pluginSettingsFactory, final TransactionTemplate transactionTemplate) {
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.transactionTemplate = transactionTemplate;
        // TODO: May need to remove this. May only be needed by the field mapping controller.
        i18nProperties = new Properties();
        populateI18nProperties();
    }

    public PluginSettingsFactory getPluginSettingsFactory() {
        return pluginSettingsFactory;
    }

    public TransactionTemplate getTransactionTemplate() {
        return transactionTemplate;
    }

    private void populateI18nProperties() {
        try (final InputStream stream = ClassLoaderUtils.getResourceAsStream(BlackDuckJiraConstants.PROPERTY_FILENAME, this.getClass())) {
            if (stream != null) {
                i18nProperties.load(stream);
            } else {
                logger.warn("Error opening property file: " + BlackDuckJiraConstants.PROPERTY_FILENAME);
            }
        } catch (final IOException e) {
            logger.warn("Error reading property file: " + BlackDuckJiraConstants.PROPERTY_FILENAME);
        }
        logger.debug("i18nProperties: " + i18nProperties);
    }

    // This must be "package protected" to avoid synthetic access
    String getI18nProperty(final String key) {
        if (i18nProperties == null) {
            return key;
        }
        final String value = i18nProperties.getProperty(key);
        if (value == null) {
            return key;
        }
        return value;
    }

    // This must be "package protected" to avoid synthetic access
    Object getValue(final PluginSettings settings, final String key) {
        return settings.get(key);
    }

    // This must be "package protected" to avoid synthetic access
    String getStringValue(final PluginSettings settings, final String key) {
        return (String) getValue(settings, key);
    }

    // This must be "package protected" to avoid synthetic access
    void setValue(final PluginSettings settings, final String key, final Object value) {
        if (value == null) {
            settings.remove(key);
        } else {
            settings.put(key, value);
        }
    }

    private int stringToInteger(final String integer) throws IllegalArgumentException {
        try {
            return Integer.valueOf(integer);
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException("The String : " + integer + " , is not an Integer.", e);
        }
    }
}
