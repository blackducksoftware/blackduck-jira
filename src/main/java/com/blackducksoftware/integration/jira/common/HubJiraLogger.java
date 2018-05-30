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
package com.blackducksoftware.integration.jira.common;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.log.LogLevel;

public class HubJiraLogger extends IntLogger {
    private final Logger jiraLogger;

    private LogLevel logLevel = LogLevel.TRACE;

    public HubJiraLogger(final Logger logger) {
        this.jiraLogger = logger;
    }

    public Logger getJiraLogger() {
        return jiraLogger;
    }

    @Override
    public LogLevel getLogLevel() {
        return logLevel;
    }

    @Override
    public void setLogLevel(final LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    @Override
    public void info(final String txt) {
        if (isEnabledFor(Level.INFO)) {
            logMessageInfo(txt);
        }
    }

    @Override
    public void error(final Throwable t) {
        if (isEnabledFor(Level.ERROR)) {
            logThrowable(t);
        }
    }

    @Override
    public void error(final String txt, final Throwable t) {
        if (isEnabledFor(Level.ERROR)) {
            logThrowable(txt, t);
        }
    }

    @Override
    public void error(final String txt) {
        if (isEnabledFor(Level.ERROR)) {
            logErrorMessage(txt);
        }
    }

    @Override
    public void warn(final String txt) {
        if (isEnabledFor(Level.WARN)) {
            logMessageWarn(txt);
        }
    }

    @Override
    public void trace(final String txt) {
        logMessageTrace(txt);
    }

    @Override
    public void trace(final String txt, final Throwable t) {
        if (jiraLogger != null && jiraLogger.isTraceEnabled()) {
            logThrowable(txt, t);
        }
    }

    @Override
    public void debug(final String txt) {
        logMessageDebug(txt);
    }

    @Override
    public void debug(final String txt, final Throwable t) {
        if (jiraLogger != null && jiraLogger.isDebugEnabled()) {
            logThrowable(txt, t);
        }
    }

    @Override
    public void alwaysLog(final String txt) {
        logMessageInfo(txt);
    }

    private boolean isEnabledFor(final Level logLevel) {
        return jiraLogger != null && jiraLogger.isEnabledFor(logLevel);
    }

    private void logMessageInfo(final String txt) {
        if (txt != null) {
            if (jiraLogger != null) {
                jiraLogger.info(txt);
            } else {
                System.out.println(txt);
            }
        }
    }

    private void logMessageDebug(final String txt) {
        if (txt != null) {
            if (jiraLogger != null && jiraLogger.isDebugEnabled()) {
                jiraLogger.debug(txt);
            } else {
                System.out.println(txt);
            }
        }
    }

    private void logMessageWarn(final String txt) {
        if (txt != null) {
            if (jiraLogger != null) {
                jiraLogger.warn(txt);
            } else {
                System.out.println(txt);
            }
        }
    }

    private void logMessageTrace(final String txt) {
        if (txt != null) {
            if (jiraLogger != null && jiraLogger.isTraceEnabled()) {
                jiraLogger.trace(txt);
            } else {
                System.out.println(txt);
            }
        }
    }

    private void logThrowable(final Throwable throwable) {
        logThrowable("An error occurred caused by ", throwable);
    }

    private void logThrowable(final String txt, final Throwable throwable) {
        if (txt != null) {
            if (jiraLogger != null) {
                jiraLogger.error(txt, throwable);
            } else {
                final StringWriter sw = new StringWriter();
                throwable.printStackTrace(new PrintWriter(sw));
                System.err.println(sw.toString());
            }
        }
    }

    private void logErrorMessage(final String txt) {
        if (txt != null) {
            if (jiraLogger != null) {
                jiraLogger.error(txt);
            } else {
                System.out.println(txt);
            }
        }
    }
}
