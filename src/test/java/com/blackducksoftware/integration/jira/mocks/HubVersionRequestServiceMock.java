/**
 * Hub JIRA Plugin
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.jira.mocks;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.nonpublic.HubVersionRequestService;
import com.blackducksoftware.integration.hub.model.response.VersionComparison;
import com.blackducksoftware.integration.hub.rest.RestConnection;

public class HubVersionRequestServiceMock extends HubVersionRequestService {

    private VersionComparison versionComparison;

    public HubVersionRequestServiceMock(final RestConnection restConnection) {
        super(restConnection);
    }

    @Override
    public String getHubVersion() throws IntegrationException {
        return versionComparison.producerVersion;
    }

    @Override
    public VersionComparison getHubVersionComparison(final String consumerVersion) throws IntegrationException {
        return versionComparison;
    }

    public void setHubVersionComparison(final VersionComparison versionComparison) {
        this.versionComparison = versionComparison;
    }

    @Override
    public boolean isConsumerVersionLessThanOrEqualToServerVersion(final String consumerVersion) throws IntegrationException {
        if (versionComparison.numericResult <= 0) {
            return true;
        } else {
            return false;
        }
    }
}
