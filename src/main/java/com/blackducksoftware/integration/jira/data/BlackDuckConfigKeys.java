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
package com.blackducksoftware.integration.jira.data;

public class BlackDuckConfigKeys {
    public static final String BLACKDUCK_CONFIG_KEY_PREFIX = "com.blackducksoftware.integration.hub.configuration";

    public static final String CONFIG_BLACKDUCK_URL = BLACKDUCK_CONFIG_KEY_PREFIX + ".huburl";
    public static final String CONFIG_BLACKDUCK_API_TOKEN = BLACKDUCK_CONFIG_KEY_PREFIX + ".bdApiToken";
    public static final String CONFIG_BLACKDUCK_TIMEOUT = BLACKDUCK_CONFIG_KEY_PREFIX + ".hubtimeout";
    public static final String CONFIG_BLACKDUCK_TRUST_CERT = BLACKDUCK_CONFIG_KEY_PREFIX + ".hubtrustcert";

    public static final String CONFIG_PROXY_HOST = BLACKDUCK_CONFIG_KEY_PREFIX + ".hubproxyhost";
    public static final String CONFIG_PROXY_PORT = BLACKDUCK_CONFIG_KEY_PREFIX + ".hubproxyport";
    public static final String CONFIG_PROXY_USER = BLACKDUCK_CONFIG_KEY_PREFIX + ".hubproxyuser";
    public static final String CONFIG_PROXY_PASS = BLACKDUCK_CONFIG_KEY_PREFIX + ".hubproxypass";
    public static final String CONFIG_PROXY_PASS_LENGTH = BLACKDUCK_CONFIG_KEY_PREFIX + ".hubproxypasslength";

    public static final String BLACKDUCK_CONFIG_GROUPS = BLACKDUCK_CONFIG_KEY_PREFIX + ".hubGroups";

}
