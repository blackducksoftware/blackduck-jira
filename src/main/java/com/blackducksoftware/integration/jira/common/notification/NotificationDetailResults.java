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
package com.blackducksoftware.integration.jira.common.notification;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.synopsys.integration.blackduck.api.UriSingleResponse;
import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;

public class NotificationDetailResults extends NotificationResults<NotificationDetailResult> {
    private final List<NotificationDetailResult> resultList;

    public NotificationDetailResults(final List<NotificationDetailResult> resultList, final Optional<Date> latestNotificationCreatedAtDate, final Optional<String> latestNotificationCreatedAtString) {
        super(latestNotificationCreatedAtDate, latestNotificationCreatedAtString);
        this.resultList = resultList;
    }

    public List<UriSingleResponse<? extends BlackDuckResponse>> getAllLinks() {
        final List<UriSingleResponse<? extends BlackDuckResponse>> uriResponses = new ArrayList<>();
        resultList.forEach(result -> {
            result.getNotificationContentDetails().forEach(contentDetail -> {
                uriResponses.addAll(contentDetail.getPresentLinks());
            });
        });

        return uriResponses;
    }

    @Override
    public List<NotificationDetailResult> getResults() {
        return resultList;
    }

}