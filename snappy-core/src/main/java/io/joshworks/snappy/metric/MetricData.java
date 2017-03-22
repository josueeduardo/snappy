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

package io.joshworks.snappy.metric;

import io.joshworks.snappy.executor.ExecutorBootstrap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Josue on 08/02/2017.
 */
public class MetricData {

    private final long maxMemory;
    private final long totalMemory;
    private final long freeMemory;
    private final long usedMemory;
    private final List<RestMetricHandler.RestMetrics> resources;
    private final List<PoolMetric> threads = new ArrayList<>();
    private final Map<String, Object> custom = new HashMap<>();

    public MetricData(List<RestMetricHandler> metricsHandlers) {
        Runtime runtime = Runtime.getRuntime();
        this.maxMemory = runtime.maxMemory();
        this.totalMemory = runtime.totalMemory();
        this.freeMemory = runtime.freeMemory();
        this.usedMemory = (totalMemory - freeMemory);

        threads.addAll(ExecutorBootstrap.executorMetrics());
        threads.addAll(ExecutorBootstrap.schedulerMetrics());

        resources = metricsHandlers.stream().map(RestMetricHandler::getRestMetrics).collect(Collectors.toList());
        custom.putAll(Metrics.getData());
    }

    public long getMaxMemory() {
        return maxMemory;
    }

    public long getTotalMemory() {
        return totalMemory;
    }

    public long getFreeMemory() {
        return freeMemory;
    }

    public long getUsedMemory() {
        return usedMemory;
    }

    public List<PoolMetric> getThreads() {
        return threads;
    }

    public List<RestMetricHandler.RestMetrics> getResources() {
        return resources;
    }
}
