/*
 * Copyright 2017 Josue Gontijo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.joshworks.snappy.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static io.joshworks.snappy.SnappyServer.LOGGER_NAME;

/**
 * Created by Josh Gontijo on 3/24/17.
 */
public class ExecutorContainer {

    private static final Logger logger = LoggerFactory.getLogger(LOGGER_NAME);

    final Map<String, ThreadPoolExecutor> executors = new ConcurrentHashMap<>();
    final Map<String, ScheduledThreadPoolExecutor> schedulers = new ConcurrentHashMap<>();
    final String defaultExecutor;
    final String defaultScheduler;

    ExecutorContainer(String defaultExecutor,
                             String defaultScheduler,
                             Map<String, ThreadPoolExecutor> execs,
                             Map<String, ScheduledThreadPoolExecutor> scheds) {
        this.defaultExecutor = defaultExecutor;
        this.defaultScheduler = defaultScheduler;

        executors.putAll(execs);
        schedulers.putAll(scheds);

    }

    void shutdownAll() {
        executors.values().forEach(this::shutdownExecutor);
        schedulers.values().forEach(this::shutdownExecutor);
    }

    private void shutdownExecutor(ThreadPoolExecutor executorService) {
        if (!executorService.isShutdown()) {
            executorService.shutdown();
            try {
                executorService.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.warn("Failed to shutdown executor, halting tasks");
                executorService.shutdownNow();
            }
        }
    }

}
