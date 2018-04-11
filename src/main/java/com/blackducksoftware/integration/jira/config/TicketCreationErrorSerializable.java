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
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class TicketCreationErrorSerializable implements Serializable {

    private static final long serialVersionUID = -5335895094076488435L;

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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((configError == null) ? 0 : configError.hashCode());
        result = prime * result + ((hubJiraTicketErrors == null) ? 0 : hubJiraTicketErrors.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TicketCreationErrorSerializable)) {
            return false;
        }
        final TicketCreationErrorSerializable other = (TicketCreationErrorSerializable) obj;
        if (configError == null) {
            if (other.configError != null) {
                return false;
            }
        } else if (!configError.equals(other.configError)) {
            return false;
        }
        if (hubJiraTicketErrors == null) {
            if (other.hubJiraTicketErrors != null) {
                return false;
            }
        } else if (!hubJiraTicketErrors.equals(other.hubJiraTicketErrors)) {
            return false;
        }
        return true;
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
