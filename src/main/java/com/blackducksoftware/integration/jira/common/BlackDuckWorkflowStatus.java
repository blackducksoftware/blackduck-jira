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
package com.blackducksoftware.integration.jira.common;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import com.blackducksoftware.integration.jira.common.exception.JiraException;

public enum BlackDuckWorkflowStatus {
    ENABLED("Enabled"),
    POLICY("Policy"),
    SECURITY_POLICY("Security Policy"),
    VULN("Vulnerability"),
    DISABLED("Disabled");

    private final String prettyPrintName;

    BlackDuckWorkflowStatus(final String prettyPrintName) {
        this.prettyPrintName = prettyPrintName;
    }

    public String getPrettyPrintName() {
        return prettyPrintName;
    }

    public static String getPrettyListNames(final EnumSet<BlackDuckWorkflowStatus> statuses) throws JiraException {
        if (statuses.contains(POLICY) || statuses.contains(SECURITY_POLICY) || statuses.contains(VULN)) {
            final List<String> prettyStatuses = statuses.stream().map(BlackDuckWorkflowStatus::getPrettyPrintName).collect(Collectors.toList());
            final String joinedStatus = StringUtils.join(prettyStatuses, ", ");
            return joinedStatus + " Only";
        } else if (1 == statuses.size()) {
            return statuses.stream().map(BlackDuckWorkflowStatus::getPrettyPrintName).findFirst().orElse("");
        }
        final List<String> names = statuses.stream().map(BlackDuckWorkflowStatus::name).collect(Collectors.toList());
        final String joinedNames = StringUtils.join(names, ", ");
        throw new JiraException("This is an invalid list of status's. " + joinedNames);
    }

}
