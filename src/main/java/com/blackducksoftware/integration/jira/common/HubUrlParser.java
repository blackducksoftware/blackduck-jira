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
package com.blackducksoftware.integration.jira.common;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.exception.HubIntegrationException;

public class HubUrlParser {

    public static String getBaseUrl(final String url) throws URISyntaxException {
        final URI uri = new URI(url);
        final String derivedUrlPrefix = StringUtils.join(uri.getScheme(), "://", uri.getAuthority(), "/");
        return derivedUrlPrefix;
    }

    public static String getRelativeUrl(final String url) throws HubIntegrationException {
        if (url == null) {
            return null;
        }
        try {
            final String baseUrl = getBaseUrl(url);
            final URI baseUri = new URI(baseUrl);
            final URI origUri = new URI(url);
            final URI relativeUri = baseUri.relativize(origUri);
            return relativeUri.toString();
        } catch (URISyntaxException e) {
            throw new HubIntegrationException("Invalid URI syntax exception on " + url + ": " + e.getMessage());
        }
    }
}
