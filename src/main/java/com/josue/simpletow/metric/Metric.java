package com.josue.simpletow.metric;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * Created by Josue on 08/02/2017.
 */
public class Metric {

    private final long maxMemory;
    private final long totalMemory;
    private final long freeMemory;
    private final long usedMemory;
    private final List<RestMetricHandler.RestMetrics> resources;

    private final List<PoolMetric> threads = new ArrayList<>();

    public Metric(Map<String, ThreadPoolExecutor> executors, Map<String, ScheduledThreadPoolExecutor> schedulers, List<RestMetricHandler> metricHandlers) {
        Runtime runtime = Runtime.getRuntime();
        this.maxMemory = runtime.maxMemory();
        this.totalMemory = runtime.totalMemory();
        this.freeMemory = runtime.freeMemory();
        this.usedMemory = (totalMemory - freeMemory);

        executors.entrySet().forEach(entry -> threads.add(new PoolMetric(entry.getKey(), entry.getValue())));
        schedulers.entrySet().forEach(entry -> threads.add(new PoolMetric(entry.getKey(), entry.getValue())));

        resources = metricHandlers.stream().map(RestMetricHandler::getRestMetrics).collect(Collectors.toList());

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
