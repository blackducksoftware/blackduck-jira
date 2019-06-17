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
package com.blackducksoftware.integration.jira.config.model;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.blackducksoftware.integration.jira.config.TicketCreationError;
import com.synopsys.integration.util.Stringable;

@XmlAccessorType(XmlAccessType.FIELD)
public class TicketCreationErrorSerializable extends Stringable implements Serializable {
    private static final long serialVersionUID = 3871965442453250995L;

    @XmlElement
    private List<TicketCreationError> hubJiraTicketErrors;

    @XmlElement
    private String configError;

    public TicketCreationErrorSerializable() {
    }

    public List<TicketCreationError> getHubJiraTicketErrors() {
        return hubJiraTicketErrors;
    }

    public void setHubJiraTicketErrors(final List<TicketCreationError> hubJiraTicketErrors) {
        this.hubJiraTicketErrors = hubJiraTicketErrors;
    }

    public String getConfigError() {
        return configError;
    }

    public void setConfigError(final String configError) {
        this.configError = configError;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("TicketCreationErrorSerializable [hubJiraTicketErrors=");
        builder.append(hubJiraTicketErrors);
        builder.append(", configError=");
        builder.append(configError);
        builder.append("]");
        return builder.toString();
    }

}
