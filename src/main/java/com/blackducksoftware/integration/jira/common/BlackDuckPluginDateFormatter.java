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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

import com.synopsys.integration.rest.RestConstants;

public final class BlackDuckPluginDateFormatter {
    private static final String INTERNAL_PLUGIN_TIME_ZONE = "Zulu";

    private final SimpleDateFormat dateFormat;

    public BlackDuckPluginDateFormatter() {
        dateFormat = new SimpleDateFormat(RestConstants.JSON_DATE_FORMAT);
        dateFormat.setTimeZone(java.util.TimeZone.getTimeZone(INTERNAL_PLUGIN_TIME_ZONE));
    }

    public String format(final Date date) {
        return dateFormat.format(date);
    }

    public Date parse(final String dateString) throws ParseException {
        return dateFormat.parse(dateString);
    }

    public Date fromLocalDateTime(final LocalDateTime localDateTime) {
        return Date.from(
            localDateTime
                .atZone(dateFormat.getTimeZone().toZoneId())
                .toInstant()
        );
    }

    public LocalDateTime toLocalDateTime(final Date date) {
        return LocalDateTime.ofInstant(
            date.toInstant(),
            dateFormat.getTimeZone().toZoneId()
        );
    }

}
