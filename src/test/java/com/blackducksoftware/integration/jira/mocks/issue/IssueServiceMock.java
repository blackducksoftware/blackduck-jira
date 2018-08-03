/**
 * Black Duck JIRA Plugin
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
package com.blackducksoftware.integration.jira.mocks.issue;

import java.util.HashMap;
import java.util.Map;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.view.IssueView;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.hub.service.IssueService;

public class IssueServiceMock extends IssueService {
    public final static String CREATION_SUCCESS_URL = "SUCCESS_URL";
    public final static String CREATION_FAILURE_URL = "";
    public Map<String, IssueView> issueMap = new HashMap<>();

    public IssueServiceMock(final HubService hubService) {
        super(hubService);
    }

    @Override
    public String createIssue(final IssueView issueItem, final String url) throws IntegrationException {
        issueMap.put(url, issueItem);
        return CREATION_SUCCESS_URL;
    }

    @Override
    public void updateIssue(final IssueView issueItem, final String url) throws IntegrationException {
        issueMap.put(url, issueItem);
    }

    @Override
    public void deleteIssue(final String issueItemUrl) throws IntegrationException {
        issueMap.remove(issueItemUrl);
    }
}
