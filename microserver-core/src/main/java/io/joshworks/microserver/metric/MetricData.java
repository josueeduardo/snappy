package io.joshworks.microserver.metric;

import io.joshworks.microserver.executor.AppExecutors;
import io.joshworks.microserver.handler.HandlerManager;

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

    public MetricData() {
        Runtime runtime = Runtime.getRuntime();
        this.maxMemory = runtime.maxMemory();
        this.totalMemory = runtime.totalMemory();
        this.freeMemory = runtime.freeMemory();
        this.usedMemory = (totalMemory - freeMemory);

        AppExecutors.executors().entrySet().forEach(entry -> threads.add(new PoolMetric(entry.getKey(), entry.getValue())));
        AppExecutors.schedulers().entrySet().forEach(entry -> threads.add(new PoolMetric(entry.getKey(), entry.getValue())));

        resources = HandlerManager.metricsHandlers.stream().map(RestMetricHandler::getRestMetrics).collect(Collectors.toList());
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
