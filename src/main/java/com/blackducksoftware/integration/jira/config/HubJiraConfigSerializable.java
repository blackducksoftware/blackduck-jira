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

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class HubJiraConfigSerializable implements Serializable {

	@XmlElement
	private String checkHowOften;

	@XmlElement
	private String checkHowOftenError;

	public String getCheckHowOften() {
		return checkHowOften;
	}

	public void setCheckHowOften(final String checkHowOften) {
		this.checkHowOften = checkHowOften;
	}

	public String getCheckHowOftenError() {
		return checkHowOftenError;
	}

	public void setCheckHowOftenError(final String checkHowOftenError) {
		this.checkHowOftenError = checkHowOftenError;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((checkHowOften == null) ? 0 : checkHowOften.hashCode());
		result = prime * result + ((checkHowOftenError == null) ? 0 : checkHowOftenError.hashCode());
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
		if (checkHowOften == null) {
			if (other.checkHowOften != null) {
				return false;
			}
		} else if (!checkHowOften.equals(other.checkHowOften)) {
			return false;
		}
		if (checkHowOftenError == null) {
			if (other.checkHowOftenError != null) {
				return false;
			}
		} else if (!checkHowOftenError.equals(other.checkHowOftenError)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("HubJiraConfigSerializable [checkHowOften=");
		builder.append(checkHowOften);
		builder.append(", checkHowOftenError=");
		builder.append(checkHowOftenError);
		builder.append("]");
		return builder.toString();
	}

}
