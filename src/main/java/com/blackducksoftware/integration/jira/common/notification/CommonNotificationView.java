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
package com.blackducksoftware.integration.jira.common.notification;

import java.util.Date;

import com.synopsys.integration.blackduck.api.core.BlackDuckView;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationStateRequestStateType;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.view.NotificationUserView;
import com.synopsys.integration.blackduck.api.manual.view.NotificationView;

/**
 * This is a flattened view of both NotificationView and NotificationUserView and must be manually maintained to support both views and their api. The only common class between both views is HubView which is why sourceView is of that type,
 * but it should only ever be either NotificationView or NotificationUserView.
 */
public class CommonNotificationView extends BlackDuckView {
    private final BlackDuckView sourceView;
    private final String contentType;
    private final Date createdAt;
    private final NotificationType type;
    private final NotificationStateRequestStateType notificationState;

    public CommonNotificationView(final NotificationView notificationView) {
        sourceView = notificationView;
        contentType = notificationView.getContentType();
        createdAt = notificationView.getCreatedAt();
        type = notificationView.getType();
        notificationState = null;
        setMeta(notificationView.getMeta());
        setJson(notificationView.getJson());
    }

    public CommonNotificationView(final NotificationUserView notificationUserView) {
        sourceView = notificationUserView;
        contentType = notificationUserView.getContentType();
        createdAt = notificationUserView.getCreatedAt();
        type = notificationUserView.getType();
        notificationState = notificationUserView.getNotificationState();
        setMeta(notificationUserView.getMeta());
        setJson(notificationUserView.getJson());
    }

    public BlackDuckView getSourceView() {
        return sourceView;
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

    public NotificationStateRequestStateType getNotificationState() {
        return notificationState;
    }

}
