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
package com.blackducksoftware.integration.jira.issue.conversion.output;

import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;

public enum BlackDuckIssueAction {
    ADD_COMMENT,
    ADD_COMMENT_IF_EXISTS,
    OPEN,
    RESOLVE,
    RESOLVE_ALL,
    UPDATE_OR_OPEN;

    public static BlackDuckIssueAction fromNotificationType(final NotificationType notificationType) {
        if (NotificationType.POLICY_OVERRIDE.equals(notificationType) || NotificationType.RULE_VIOLATION_CLEARED.equals(notificationType)) {
            return RESOLVE;
        } else if (NotificationType.RULE_VIOLATION.equals(notificationType)) {
            return OPEN;
        } else if (NotificationType.VULNERABILITY.equals(notificationType)) {
            // This seems to be the safest option of the many possibilities for vulnerability notifications
            return ADD_COMMENT_IF_EXISTS;
        } else if (NotificationType.BOM_EDIT.equals(notificationType)) {
            return UPDATE_OR_OPEN;
        } else {
            throw new IllegalArgumentException(String.format("Cannot determine an action from non-policy NotificationType: %s", notificationType));
        }
    }
}
