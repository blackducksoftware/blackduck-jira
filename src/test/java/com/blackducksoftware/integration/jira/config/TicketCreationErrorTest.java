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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;

import org.junit.Test;

public class TicketCreationErrorTest {
    public static final String DATE_TIME_SINGLE_DIGIT_FIELDS_STRING = "02/02/2002 02:02AM";
    public static final LocalDateTime DATE_TIME_SINGLE_DIGIT_FILEDS_OBJECT = LocalDateTime.of(2002, 2, 2, 2, 2);
    public static final String DATE_TIME_MULTI_DIGIT_FIELDS_STRING = "10/10/2010 10:10AM";
    public static final LocalDateTime DATE_TIME_MULTI_DIGIT_FILEDS_OBJECT = LocalDateTime.of(2010, 10, 10, 10, 10);

    @Test
    public void errorTimeFormatSingleDigitFieldsTest() {
        final String timeStamp = DATE_TIME_SINGLE_DIGIT_FILEDS_OBJECT.format(TicketCreationError.ERROR_TIME_FORMAT);
        assertEquals(DATE_TIME_SINGLE_DIGIT_FIELDS_STRING, timeStamp);
    }

    @Test
    public void errorTimeFormatMultiDigitFieldsTest() {
        final String timeStamp = DATE_TIME_MULTI_DIGIT_FILEDS_OBJECT.format(TicketCreationError.ERROR_TIME_FORMAT);
        assertEquals(DATE_TIME_MULTI_DIGIT_FIELDS_STRING, timeStamp);
    }

    @Test
    public void getTimeStampDateTimeSingleDigitFieldsTest() {
        final TicketCreationError ticketCreationError = new TicketCreationError();
        ticketCreationError.setTimeStamp(DATE_TIME_SINGLE_DIGIT_FIELDS_STRING);
        assertEquals(DATE_TIME_SINGLE_DIGIT_FILEDS_OBJECT, ticketCreationError.getTimeStampDateTime());
    }

    @Test
    public void getTimeStampDateTimeMultiDigitFieldsTest() {
        final TicketCreationError ticketCreationError = new TicketCreationError();
        ticketCreationError.setTimeStamp(DATE_TIME_MULTI_DIGIT_FIELDS_STRING);
        assertEquals(DATE_TIME_MULTI_DIGIT_FILEDS_OBJECT, ticketCreationError.getTimeStampDateTime());
    }

    @Test
    public void toStringTest() {
        final TicketCreationError ticketCreationError = new TicketCreationError();
        ticketCreationError.setTimeStamp(DATE_TIME_MULTI_DIGIT_FIELDS_STRING);

        assertEquals("TicketCreationError [stackTrace=null, timeStamp=10/10/2010 10:10AM]", ticketCreationError.toString());
    }

    @Test
    public void compareToTest() {
        final TicketCreationError earlierError = new TicketCreationError();
        earlierError.setTimeStamp(DATE_TIME_SINGLE_DIGIT_FIELDS_STRING);

        final TicketCreationError laterError = new TicketCreationError();
        laterError.setTimeStamp(DATE_TIME_MULTI_DIGIT_FIELDS_STRING);

        assertEquals(0, earlierError.compareTo(earlierError));
        assertEquals(0, laterError.compareTo(laterError));
        assertTrue(earlierError.compareTo(laterError) > 0);
        assertTrue(laterError.compareTo(earlierError) < 0);
    }

}
