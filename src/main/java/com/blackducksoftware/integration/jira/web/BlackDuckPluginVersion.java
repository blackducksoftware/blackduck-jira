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
package com.blackducksoftware.integration.jira.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlackDuckPluginVersion {
    private static final String VERSION_UNKNOWN = "(unknown)";

    private static final Logger logger = LoggerFactory.getLogger(BlackDuckPluginVersion.class);

    public static String getVersion() {
        final Properties prop = new Properties();
        final InputStream is = BlackDuckPluginVersion.class.getClassLoader().getResourceAsStream("META-INF/MANIFEST.MF");
        try {
            prop.load(is);
        } catch (final IOException e) {
            return VERSION_UNKNOWN;
        }
        String bundleName = null;
        String bundleVersion = null;
        for (final Object key : prop.keySet()) {
            if (key instanceof String) {
                final String keyString = (String) key;
                if (("Bundle-Name".equals(keyString)) || ("Bundle-Version".equals(keyString))) {
                    logger.debug(keyString + "=" + prop.getProperty(keyString) + "\n");
                }
                if ("Bundle-Name".equals(keyString)) {
                    bundleName = prop.getProperty(keyString);
                }
                if ("Bundle-Version".equals(keyString)) {
                    bundleVersion = prop.getProperty(keyString);
                }
            } else {
                logger.debug("nonString manifest key: " + key.toString() + "=" + prop.getProperty(key.toString()) + "\n");
            }
        }
        if ((bundleName != null) && (bundleVersion != null)) {
            return bundleVersion;
        }
        return VERSION_UNKNOWN;
    }
}
