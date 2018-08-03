/**
 * Hub JIRA Plugin
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.jira;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;

public class BlackDuckPluginVersion {
    private static final String VERSION_UNKNOWN = "(unknown)";

    private static final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(BlackDuckPluginVersion.class.getName()));

    public static String getVersion() {
        Properties prop = new Properties();
        InputStream is = BlackDuckPluginVersion.class.getClassLoader().getResourceAsStream("META-INF/MANIFEST.MF");
        try {
            prop.load(is);
        } catch (IOException e) {
            return VERSION_UNKNOWN;
        }
        String bundleName = null;
        String bundleVersion = null;
        for (Object key : prop.keySet()) {
            if (key instanceof String) {
                String keyString = (String) key;
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
