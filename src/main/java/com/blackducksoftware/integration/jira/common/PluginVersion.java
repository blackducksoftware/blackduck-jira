/*
 * Copyright (C) 2016 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.jira.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class PluginVersion {
    private static final String VERSION_UNKNOWN = "(unknown)";

    private static final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(PluginVersion.class.getName()));

    public static String getVersion() {
        Properties prop = new Properties();
        InputStream is = PluginVersion.class.getClassLoader().getResourceAsStream("META-INF/MANIFEST.MF");
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
