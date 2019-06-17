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
package com.blackducksoftware.integration.jira.task.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;

public class PluginExecutorService {
    public static final int MAX_QUEUED_TASKS = BlackDuckJiraConstants.PERIODIC_TASK_TIMEOUT_AS_MULTIPLE_OF_INTERVAL + 1;

    /* package */ final List<Future<String>> queuedTasks;
    private Integer queuedTaskLimit;
    private ExecutorService executorService;

    public static PluginExecutorService restricted(final int maxAllowedTasks) {
        final PluginExecutorService pluginExecutorService = new PluginExecutorService();
        pluginExecutorService.queuedTaskLimit = maxAllowedTasks;
        return pluginExecutorService;
    }

    public PluginExecutorService() {
        this.queuedTasks = new ArrayList<>();
        this.queuedTaskLimit = MAX_QUEUED_TASKS;
        start();
    }

    public void restart() {
        shutdownNow();
        start();
    }

    public void shutdown() {
        shutdown(executorService::shutdown);
    }

    public void shutdownNow() {
        shutdown(executorService::shutdownNow);
    }

    public PluginFuture submit(final Callable<String> task) throws RejectedExecutionException {
        if (canAcceptNewTasks()) {
            final Future<String> future = executorService.submit(task);
            queuedTasks.add(future);
            return new PluginFuture(future);
        } else {
            throw new RejectedExecutionException("The number of queued tasks is already at capacity.");
        }
    }

    public boolean isShutdown() {
        return executorService.isShutdown();
    }

    public boolean canAcceptNewTasks() {
        return !isShutdown() && queuedTasks.size() < queuedTaskLimit;
    }

    private void start() throws IllegalStateException {
        if (executorService == null || executorService.isShutdown()) {
            executorService = Executors.newSingleThreadExecutor();
        } else {
            throw new IllegalStateException("The executor service is already running.");
        }
    }

    private void shutdown(final Runnable shutdownFunction) {
        if (!executorService.isShutdown()) {
            shutdownFunction.run();
        }
        queuedTasks.clear();
    }

    public final class PluginFuture {
        private final Future<String> future;

        public PluginFuture(final Future<String> future) {
            this.future = future;
        }

        public String get(final long timeoutMinutes) throws InterruptedException, ExecutionException, TimeoutException {
            try {
                return future.get(timeoutMinutes, TimeUnit.MINUTES);
            } finally {
                queuedTasks.remove(future);
            }
        }

        public boolean isDone() {
            return future.isDone();
        }

        public void cancel() {
            future.cancel(true);
            queuedTasks.remove(future);
        }

    }

}
