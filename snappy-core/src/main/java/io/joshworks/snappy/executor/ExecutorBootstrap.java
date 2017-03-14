package io.joshworks.snappy.executor;

import io.joshworks.snappy.metric.PoolMetric;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * Created by josh on 3/14/17.
 */
public class ExecutorBootstrap {

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
                .orElse(new ExecutorConfig(DEFAULT_EXECUTOR)).getName();

        String defaultScheduler = scheds.stream()
                .filter(SchedulerConfig::isDefaultExecutor)
                .findFirst()
                .orElse(new SchedulerConfig(DEFAULT_SCHEDULER)).getName();

        AppExecutors.init(executors, defaultExecutor, schedulers, defaultScheduler);
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
