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


    public final List<RestMetrics> resources;
    public final Memory memory;
    public final List<PoolMetric> threadPools = new ArrayList<>();
    public final Map<String, Object> appMetrics = new HashMap<>();

    public MetricData(List<RestMetricsHandler> metricsHandlers) {

        memory = new Memory();

        threadPools.addAll(ExecutorBootstrap.executorMetrics());
        threadPools.addAll(ExecutorBootstrap.schedulerMetrics());

        resources = metricsHandlers.stream().map(RestMetricsHandler::getRestMetrics).collect(Collectors.toList());
        appMetrics.putAll(Metrics.getData());
    }

    static class Memory {
        public final long maxMemory;
        public final long totalMemory;
        public final long freeMemory;
        public final long usedMemory;

        public Memory() {
            Runtime runtime = Runtime.getRuntime();
            this.maxMemory = runtime.maxMemory();
            this.totalMemory = runtime.totalMemory();
            this.freeMemory = runtime.freeMemory();
            this.usedMemory = (totalMemory - freeMemory);
        }
    }

}
