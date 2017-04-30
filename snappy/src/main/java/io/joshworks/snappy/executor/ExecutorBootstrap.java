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

import io.joshworks.snappy.metric.PoolMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import static io.joshworks.snappy.SnappyServer.LOGGER_NAME;
import static io.joshworks.snappy.SnappyServer.onShutdown;

/**
 * Created by josh on 3/14/17.
 */
public class ExecutorBootstrap {

    private static final Logger logger = LoggerFactory.getLogger(LOGGER_NAME);

    static final String DEFAULT_SCHEDULER = "default-scheduler";
    static final String DEFAULT_EXECUTOR = "default-executor";

    public static void init(List<SchedulerConfig> scheds, List<ExecutorConfig> execs) {
        final Map<String, ThreadPoolExecutor> executors = new HashMap<>();
        final Map<String, ScheduledThreadPoolExecutor> schedulers = new HashMap<>();


        if (scheds.isEmpty()) {
            SchedulerConfig schedulerConfig = SchedulerConfig.withDefaults(DEFAULT_SCHEDULER);
            schedulerConfig.markAsDefault();
            scheds.add(schedulerConfig);
        }

        if (execs.isEmpty()) {
            ExecutorConfig executorConfig = ExecutorConfig.withDefaults(DEFAULT_EXECUTOR);
            executorConfig.markAsDefault();
            execs.add(executorConfig);
        }

        schedulers.putAll(scheds.stream().collect(Collectors.toMap(ExecutorConfigBase::getName, SchedulerConfig::getScheduler)));
        executors.putAll(execs.stream().collect(Collectors.toMap(ExecutorConfigBase::getName, ExecutorConfig::getExecutor)));


        String defaultExecutor = execs.stream()
                .filter(ExecutorConfig::isDefaultExecutor)
                .findFirst()
                .orElse(execs.stream()
                        .findFirst()
                        .orElse(new ExecutorConfig(DEFAULT_EXECUTOR)))
                .getName();

        String defaultScheduler = scheds.stream()
                .filter(SchedulerConfig::isDefaultExecutor)
                .findFirst()
                .orElse(scheds.stream()
                        .findFirst()
                        .orElse(new SchedulerConfig(DEFAULT_SCHEDULER)))
                .getName();

        ExecutorContainer executorContainer = new ExecutorContainer(defaultExecutor, defaultScheduler, executors, schedulers);

        onShutdown(() -> {
            executorContainer.shutdownAll();
            logger.info("Executors shutdown");
        });

        AppExecutors.init(executorContainer);

    }

    public static List<PoolMetric> executorMetrics() {
        return AppExecutors.executors().entrySet().stream()
                .map(entry -> new PoolMetric(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public static List<PoolMetric> schedulerMetrics() {
        return AppExecutors.schedulers().entrySet().stream()
                .map(entry -> new PoolMetric(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

}
