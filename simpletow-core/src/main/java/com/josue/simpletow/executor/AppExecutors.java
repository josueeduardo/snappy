package com.josue.simpletow.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by josh on 3/7/17.
 */
public class AppExecutors {

    static final Map<String, ThreadPoolExecutor> executors = new HashMap<>();
    static final Map<String, ScheduledThreadPoolExecutor> schedulers = new HashMap<>();
    private static final String DEFAULT = "default";
    private static final Logger logger = LoggerFactory.getLogger(AppExecutors.class);
    private static ThreadPoolExecutor defaultExecutor;
    private static ScheduledExecutorService defaultScheduler;

    public static void init(Map<String, ThreadPoolExecutor> execs, Map<String, ScheduledThreadPoolExecutor> scheds) {
        executors.putAll(execs);
        schedulers.putAll(scheds);

        if (executors.isEmpty()) {
            ThreadPoolExecutor executorService = new ThreadPoolExecutor(0, 5, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
            executors.put(DEFAULT + "-executor", executorService);
            defaultExecutor = executorService;
        } else {
            defaultExecutor = executors.values().iterator().next();
        }

        if (schedulers.isEmpty()) {
            ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(2);
            schedulers.put(DEFAULT + "-scheduler", scheduler);
            defaultScheduler = scheduler;
        } else {
            defaultExecutor = executors.values().iterator().next();
        }

    }

    public static ExecutorService executor() {
        return defaultExecutor;
    }

    public static Map<String, ThreadPoolExecutor> executors() {
        return new HashMap<>(executors);
    }

    public static ExecutorService executor(String name) {
        return executors.get(name);
    }

    public static ScheduledExecutorService scheduler() {
        return defaultScheduler;
    }

    public static ScheduledExecutorService scheduler(String name) {
        return schedulers.get(name);
    }

    public static Map<String, ScheduledThreadPoolExecutor> schedulers() {
        return new HashMap<>(schedulers);
    }

    public static void shutdownAll() {
        executors.entrySet().forEach(entry -> shutdown(entry.getKey(), entry.getValue()));
        schedulers.entrySet().forEach(entry -> shutdown(entry.getKey(), entry.getValue()));
        logger.info("Executors shutdown");
    }

    private static void shutdown(String name, ExecutorService executorService) {
        logger.info("Shutting down pool: {}", name);
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
