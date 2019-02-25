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
package com.blackducksoftware.integration.jira.mocks.issue;

import java.util.HashMap;
import java.util.Map;

import com.synopsys.integration.blackduck.api.core.BlackDuckView;
import com.synopsys.integration.blackduck.rest.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;

public class IssueServiceMock extends BlackDuckService {
    public final static String CREATION_SUCCESS_URL = "SUCCESS_URL";
    public final static String CREATION_FAILURE_URL = "";
    public final static String TEST_PUT_URL = "testPut";
    public Map<String, Object> issueMap = new HashMap<>();

    public IssueServiceMock(final BlackDuckHttpClient blackDuckHttpClient) {
        super(new PrintStreamIntLogger(System.out, LogLevel.DEBUG), blackDuckHttpClient, BlackDuckServicesFactory.createDefaultGson(), BlackDuckServicesFactory.createDefaultObjectMapper());
    }

    @Override
    public void put(final BlackDuckView blackDuckView) {
        issueMap.put(TEST_PUT_URL, blackDuckView);
    }

    @Override
    public String post(final String uri, final Object object) {
        issueMap.put(uri, object);
        return CREATION_SUCCESS_URL;
    }

    @Override
    public void delete(final String url) throws IntegrationException {
        issueMap.remove(url);
    }

}
