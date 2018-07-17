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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@XmlAccessorType(XmlAccessType.FIELD)
public class TicketCreationError implements Serializable, Comparable<TicketCreationError> {
    public static final String YEAR_PATTERN = "y";
    public static final String MONTH_PATTERN = "MM";
    public static final String DAY_OF_MONTH_PATTERN = "dd";
    public static final String HOUR_PATTERN = "hh";
    public static final String MINUTE_PATTERN = "mm";

    public static final char DATE_TIME_SEPARATOR = ' ';
    public static final char DATE_FIELD_SEPARATOR = '/';
    public static final char TIME_FIELD_SEPARATOR = ':';

    public static final String ERROR_TIME_PATTERN = MONTH_PATTERN + DATE_FIELD_SEPARATOR + DAY_OF_MONTH_PATTERN + DATE_FIELD_SEPARATOR + YEAR_PATTERN + DATE_TIME_SEPARATOR + HOUR_PATTERN + TIME_FIELD_SEPARATOR + MINUTE_PATTERN + "a";

    public static final DateTimeFormatter ERROR_TIME_FORMAT = new DateTimeFormatterBuilder().appendPattern(ERROR_TIME_PATTERN).toFormatter();

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
        LocalDateTime errorDateTime = null;
        try {
            final TemporalAccessor parsedDateTime = ERROR_TIME_FORMAT.parse(timeStamp);
            errorDateTime = LocalDateTime.from(parsedDateTime);
        } catch (final DateTimeParseException e) {
            errorDateTime = parseExpectedFormat(timeStamp);
        }
        return errorDateTime;
    }

    private LocalDateTime parseExpectedFormat(final String dateAndTime) {
        final String delimiter = String.valueOf(DATE_TIME_SEPARATOR);
        final String[] dateAndTimeTemplates = ERROR_TIME_PATTERN.split(delimiter);
        final String[] dateAndTimeValues = dateAndTime.split(delimiter);
        if ((2 == dateAndTimeTemplates.length) && (2 == dateAndTimeValues.length)) {
            final LocalDate localDate = parseExpectedDate(dateAndTimeTemplates[0], dateAndTimeValues[0]);
            final LocalTime localTime = parseExpectedTime(dateAndTimeTemplates[1], dateAndTimeValues[1]);
            return LocalDateTime.of(localDate, localTime);
        }
        return LocalDateTime.now();
    }

    private LocalDate parseExpectedDate(final String dateTemplate, final String dateString) {
        final String delimiter = String.valueOf(DATE_FIELD_SEPARATOR);
        final String[] dateFormatTokens = dateTemplate.split(delimiter);
        final String[] dateTokens = dateString.split(delimiter);

        final int year = getFieldValue(dateFormatTokens, dateTokens, YEAR_PATTERN, 2000);
        final int month = getFieldValue(dateFormatTokens, dateTokens, MONTH_PATTERN, 1);
        final int day = getFieldValue(dateFormatTokens, dateTokens, DAY_OF_MONTH_PATTERN, 1);

        return LocalDate.of(year, month, day);
    }

    private LocalTime parseExpectedTime(final String timeTemplate, final String timeString) {
        final String delimiter = String.valueOf(TIME_FIELD_SEPARATOR);
        final String[] timeFormatTokens = timeTemplate.split(delimiter);
        final String[] timeTokens = timeString.split(delimiter);

        int hour = getFieldValue(timeFormatTokens, timeTokens, HOUR_PATTERN, 0);
        int minute = 0;
        if (timeTokens.length >= 1 && timeFormatTokens.length >= 1 && timeFormatTokens[1].contains(MINUTE_PATTERN)) {
            final int minuteFieldLength = timeTokens[1].length();
            if (minuteFieldLength == 4) {
                final String number = timeTokens[1].substring(0, 2);
                final String meridian = timeTokens[1].substring(2, 4);
                minute = Integer.parseInt(number);
                if ("PM".equalsIgnoreCase(meridian)) {
                    hour += 12;
                }
            } else if (minuteFieldLength == 2) {
                minute = Integer.parseInt(timeTokens[1]);
            }
        }

        return LocalTime.of(hour, minute);

    }

    private int getFieldValue(final String[] templateTokens, final String[] actualTokens, final String pattern, final int defaultValue) {
        for (int i = 0; i < templateTokens.length; i++) {
            if (pattern.equals(templateTokens[i])) {
                if (i < actualTokens.length) {
                    return Integer.parseInt(actualTokens[i]);
                }
            }
        }
        return defaultValue;
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
