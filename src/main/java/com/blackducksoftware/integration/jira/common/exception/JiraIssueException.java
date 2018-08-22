/**
 * Black Duck JIRA Plugin
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
package com.blackducksoftware.integration.jira.common.exception;

import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ErrorCollections;

public class JiraIssueException extends JiraException {
    private static final long serialVersionUID = -2838852966435691111L;

    private ErrorCollection errorCollection = ErrorCollections.empty();

    public JiraIssueException(final String methodAttempt) {
        super();
        super.setMethodAttempt(methodAttempt);
    }

    public JiraIssueException(final String message, final String methodAttempt) {
        super(message);
        super.setMethodAttempt(methodAttempt);
    }

    public JiraIssueException(final String methodAttempt, final ErrorCollection errorCollection) {
        super();
        super.setMethodAttempt(methodAttempt);
        this.errorCollection = errorCollection;
    }

    public void setErrorCollection(final ErrorCollection errorCollection) {
        this.errorCollection = errorCollection;
    }

    public ErrorCollection getErrorCollection() {
        return errorCollection;
    }

}
