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

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationStateRequestStateType;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.component.NotificationContentComponent;

public class NotificationDetailResult {
    private final NotificationContentComponent notificationContent;
    private final String contentType;
    private final Date createdAt;
    private final NotificationType type;
    private final String notificationGroup;
    private final Optional<NotificationStateRequestStateType> notificationState;

    private final List<NotificationContentDetail> notificationContentDetails;

    public NotificationDetailResult(final NotificationContentComponent notificationContent, final String contentType, final Date createdAt, final NotificationType type, final String notificationGroup,
        final Optional<NotificationStateRequestStateType> notificationState, final List<NotificationContentDetail> notificationContentDetails) {
        this.notificationContent = notificationContent;
        this.contentType = contentType;
        this.createdAt = createdAt;
        this.type = type;
        this.notificationGroup = notificationGroup;
        this.notificationState = notificationState;
        this.notificationContentDetails = notificationContentDetails;
    }

    public NotificationContentComponent getNotificationContent() {
        return notificationContent;
    }

    public String getContentType() {
        return contentType;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public NotificationType getType() {
        return type;
    }

    public String getNotificationGroup() {
        return notificationGroup;
    }

    public Optional<NotificationStateRequestStateType> getNotificationState() {
        return notificationState;
    }

    public List<NotificationContentDetail> getNotificationContentDetails() {
        return notificationContentDetails;
    }

}
