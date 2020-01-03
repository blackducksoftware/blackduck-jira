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
package com.blackducksoftware.integration.jira.workflow.notification;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import com.synopsys.integration.blackduck.api.UriSingleResponse;
import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.manual.view.NotificationUserView;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucket;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucketService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.RestConstants;

public class CommonNotificationService {
    private final NotificationContentDetailFactory notificationContentDetailFactory;
    private final boolean oldestFirst;

    public CommonNotificationService(final NotificationContentDetailFactory notificationContentDetailFactory, final boolean oldestFirst) {
        this.notificationContentDetailFactory = notificationContentDetailFactory;
        this.oldestFirst = oldestFirst;
    }

    public List<CommonNotificationView> getCommonUserNotifications(final List<NotificationUserView> notificationUserViews) {
        return notificationUserViews.stream()
                   .map(CommonNotificationView::new)
                   .collect(Collectors.toList());
    }

    public NotificationDetailResults getNotificationDetailResults(final List<CommonNotificationView> commonNotifications) {
        if (commonNotifications == null || commonNotifications.isEmpty()) {
            return new NotificationDetailResults(Collections.emptyList(), Optional.empty(), Optional.empty());
        }

        List<NotificationDetailResult> sortedDetails = commonNotifications.stream()
                                                           .map(notificationContentDetailFactory::generateContentDetails)
                                                           .collect(Collectors.toList());

        if (oldestFirst) {
            // we don't want to use the default sorting from the hub
            sortedDetails = sortedDetails.stream()
                                .sorted(Comparator.comparing(NotificationDetailResult::getCreatedAt))
                                .collect(Collectors.toList());
        }

        final DatePair datePair = getLatestCreatedAtString(commonNotifications);
        return new NotificationDetailResults(sortedDetails, datePair.date, datePair.dateString);
    }

    public void populateHubBucket(final BlackDuckBucketService hubBucketService, final BlackDuckBucket hubBucket, final NotificationDetailResults notificationDetailResults) throws IntegrationException {
        final List<UriSingleResponse<? extends BlackDuckResponse>> uriResponseList = new ArrayList<>();
        uriResponseList.addAll(notificationDetailResults.getAllLinks());
        hubBucketService.addToTheBucket(hubBucket, uriResponseList);
    }

    private DatePair getLatestCreatedAtString(final List<CommonNotificationView> views) {
        final SimpleDateFormat sdf = new SimpleDateFormat(RestConstants.JSON_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        // sortedViews will be sorted most recent to oldest
        // we know that the first in the list is the most current
        final Date latestCreatedAtDate = views.stream()
                                             .map(CommonNotificationView::getCreatedAt)
                                             .sorted(Comparator.reverseOrder())
                                             .findFirst()
                                             .orElse(new Date());
        final String latestCreatedAtString = sdf.format(latestCreatedAtDate);
        return new DatePair(latestCreatedAtDate, latestCreatedAtString);
    }

    private static class DatePair {
        public final Optional<Date> date;
        public final Optional<String> dateString;

        public DatePair(final Date date, final String dateString) {
            this.date = Optional.ofNullable(date);
            this.dateString = Optional.ofNullable(dateString);
        }
    }

}
