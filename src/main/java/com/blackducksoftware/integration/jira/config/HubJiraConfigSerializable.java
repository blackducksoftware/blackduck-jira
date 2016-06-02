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
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class HubJiraConfigSerializable implements Serializable {

	private static final long serialVersionUID = -7842817229604772101L;

	@XmlElement
	private String intervalBetweenChecks;

	@XmlElement
	private String intervalBetweenChecksError;

	@XmlElement
	private String hubProjectMappings;

	@XmlElement
	private String hubProjectMappingError;

	public boolean hasErrors() {
		boolean hasErrors = false;
		if (StringUtils.isNotBlank(getIntervalBetweenChecksError())) {
			hasErrors = true;
		}
		return hasErrors;
	}

	public String getIntervalBetweenChecks() {
		return intervalBetweenChecks;
	}

	public void setIntervalBetweenChecks(final String intervalBetweenChecks) {
		this.intervalBetweenChecks = intervalBetweenChecks;
	}

	public String getIntervalBetweenChecksError() {
		return intervalBetweenChecksError;
	}

	public void setIntervalBetweenChecksError(final String intervalBetweenChecksError) {
		this.intervalBetweenChecksError = intervalBetweenChecksError;
	}

	public String getHubProjectMappings() {
		return hubProjectMappings;
	}

	public void setHubProjectMappings(final String hubProjectMappings) {
		this.hubProjectMappings = hubProjectMappings;
	}

	public String getHubProjectMappingError() {
		return hubProjectMappingError;
	}

	public void setHubProjectMappingError(final String hubProjectMappingError) {
		this.hubProjectMappingError = hubProjectMappingError;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hubProjectMappingError == null) ? 0 : hubProjectMappingError.hashCode());
		result = prime * result + ((hubProjectMappings == null) ? 0 : hubProjectMappings.hashCode());
		result = prime * result + ((intervalBetweenChecks == null) ? 0 : intervalBetweenChecks.hashCode());
		result = prime * result + ((intervalBetweenChecksError == null) ? 0 : intervalBetweenChecksError.hashCode());
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
		if (!(obj instanceof HubJiraConfigSerializable)) {
			return false;
		}
		final HubJiraConfigSerializable other = (HubJiraConfigSerializable) obj;
		if (hubProjectMappingError == null) {
			if (other.hubProjectMappingError != null) {
				return false;
			}
		} else if (!hubProjectMappingError.equals(other.hubProjectMappingError)) {
			return false;
		}
		if (hubProjectMappings == null) {
			if (other.hubProjectMappings != null) {
				return false;
			}
		} else if (!hubProjectMappings.equals(other.hubProjectMappings)) {
			return false;
		}
		if (intervalBetweenChecks == null) {
			if (other.intervalBetweenChecks != null) {
				return false;
			}
		} else if (!intervalBetweenChecks.equals(other.intervalBetweenChecks)) {
			return false;
		}
		if (intervalBetweenChecksError == null) {
			if (other.intervalBetweenChecksError != null) {
				return false;
			}
		} else if (!intervalBetweenChecksError.equals(other.intervalBetweenChecksError)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("HubJiraConfigSerializable [intervalBetweenChecks=");
		builder.append(intervalBetweenChecks);
		builder.append(", intervalBetweenChecksError=");
		builder.append(intervalBetweenChecksError);
		builder.append(", hubProjectMappings=");
		builder.append(hubProjectMappings);
		builder.append(", hubProjectMappingError=");
		builder.append(hubProjectMappingError);
		builder.append("]");
		return builder.toString();
	}


}
