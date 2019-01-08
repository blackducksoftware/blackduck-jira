/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;

public class BlackDuckJiraLogger extends IntLogger {
    private final Logger jiraLogger;

    private LogLevel logLevel = LogLevel.INFO;

    public BlackDuckJiraLogger(final Logger logger) {
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
            jiraLogger.info(txt);
        }
    }

    @Override
    public void error(final Throwable t) {
        if (isEnabledFor(Level.ERROR)) {
            jiraLogger.error("An error was caused by: ", t);
        }
    }

    @Override
    public void error(final String txt, final Throwable t) {
        if (isEnabledFor(Level.ERROR)) {
            jiraLogger.error(txt, t);
        }
    }

    @Override
    public void error(final String txt) {
        if (isEnabledFor(Level.ERROR)) {
            jiraLogger.error(txt);
        }
    }

    @Override
    public void warn(final String txt) {
        if (isEnabledFor(Level.WARN)) {
            jiraLogger.warn(txt);
        }
    }

    @Override
    public void trace(final String txt) {
        if (jiraLogger.isTraceEnabled()) {
            jiraLogger.trace(txt);
        }
    }

    @Override
    public void trace(final String txt, final Throwable t) {
        if (jiraLogger.isTraceEnabled()) {
            jiraLogger.trace(txt, t);
        }
    }

    @Override
    public void debug(final String txt) {
        if (jiraLogger.isDebugEnabled()) {
            jiraLogger.debug(txt);
        }
    }

    @Override
    public void debug(final String txt, final Throwable t) {
        if (jiraLogger.isDebugEnabled()) {
            jiraLogger.debug(txt, t);
        }
    }

    @Override
    public void alwaysLog(final String txt) {
        final Level level = jiraLogger.getLevel();
        if (level != null && !isEnabledFor(Level.INFO)) {
            jiraLogger.log(level, txt);
        } else {
            jiraLogger.info(txt);
        }
    }

    private boolean isEnabledFor(final Level level) {
        if (jiraLogger.isEnabledFor(level)) {
            return true;
        }
        final LogLevel logLevel = LogLevel.fromString(level.toString());
        return this.logLevel != null && this.logLevel.isLoggable(logLevel);
    }

}
