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
package com.blackducksoftware.integration.jira.config;

public class BlackDuckConfigKeys {
    public static final String HUB_CONFIG_KEY_PREFIX = "com.blackducksoftware.integration.hub.configuration";

    public static final String CONFIG_HUB_URL = HUB_CONFIG_KEY_PREFIX + ".huburl";
    public static final String CONFIG_HUB_USER = HUB_CONFIG_KEY_PREFIX + ".hubuser";
    public static final String CONFIG_HUB_PASS = HUB_CONFIG_KEY_PREFIX + ".hubpassword";
    public static final String CONFIG_HUB_PASS_LENGTH = HUB_CONFIG_KEY_PREFIX + ".hubpasswordlength";
    public static final String CONFIG_HUB_TIMEOUT = HUB_CONFIG_KEY_PREFIX + ".hubtimeout";
    public static final String CONFIG_HUB_TRUST_CERT = HUB_CONFIG_KEY_PREFIX + ".hubtrustcert";

    public static final String CONFIG_PROXY_HOST = HUB_CONFIG_KEY_PREFIX + ".hubproxyhost";
    public static final String CONFIG_PROXY_PORT = HUB_CONFIG_KEY_PREFIX + ".hubproxyport";
    public static final String CONFIG_PROXY_USER = HUB_CONFIG_KEY_PREFIX + ".hubproxyuser";
    public static final String CONFIG_PROXY_PASS = HUB_CONFIG_KEY_PREFIX + ".hubproxypass";
    public static final String CONFIG_PROXY_PASS_LENGTH = HUB_CONFIG_KEY_PREFIX + ".hubproxypasslength";
    public static final String CONFIG_PROXY_NO_HOST = HUB_CONFIG_KEY_PREFIX + ".hubproxynohost";

    public static final String HUB_CONFIG_GROUPS = HUB_CONFIG_KEY_PREFIX + ".hubGroups";

}
