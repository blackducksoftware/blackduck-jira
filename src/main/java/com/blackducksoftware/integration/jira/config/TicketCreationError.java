/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
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
 *******************************************************************************/
package com.blackducksoftware.integration.jira.config;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class TicketCreationError implements Serializable {

    private static final long serialVersionUID = 8705688400750977007L;

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
}
