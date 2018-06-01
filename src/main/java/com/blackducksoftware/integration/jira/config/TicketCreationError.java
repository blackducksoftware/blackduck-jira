/**
 * Hub JIRA Plugin
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
package com.blackducksoftware.integration.jira.config;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@XmlAccessorType(XmlAccessType.FIELD)
public class TicketCreationError implements Serializable, Comparable<TicketCreationError> {
    public static final DateTimeFormatter ERROR_TIME_FORMAT = new DateTimeFormatterBuilder()
            .appendPattern("MM")
            .appendLiteral('/')
            .appendPattern("dd")
            .appendLiteral('/')
            .appendPattern("y")
            .appendLiteral(' ')
            .appendPattern("hh")
            .appendLiteral(':')
            .appendPattern("mm")
            .appendPattern("a")
            .toFormatter();

    private static final long serialVersionUID = 8705688400750977007L;
    private static final Gson gson = new Gson();
    private static final Type listType = new TypeToken<List<TicketCreationError>>() {
    }.getType();

    @XmlElement
    private String stackTrace;

    @XmlElement
    private String timeStamp;

    public TicketCreationError() {
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(final String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(final String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public LocalDateTime getTimeStampDateTime() {
        final LocalDateTime errorTime = LocalDateTime.parse(timeStamp, ERROR_TIME_FORMAT);
        return errorTime;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("TicketCreationError [stackTrace=");
        builder.append(stackTrace);
        builder.append(", timeStamp=");
        builder.append(timeStamp);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int compareTo(final TicketCreationError o) {
        // This will ensure oldest first
        return o.getTimeStampDateTime().compareTo(getTimeStampDateTime());
    }

    public static List<TicketCreationError> fromJson(final String json) {
        return gson.fromJson(json, listType);
    }

    public static String toJson(final List<TicketCreationError> errors) {
        return gson.toJson(errors);
    }

}
