package com.josue.simpletow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by josh on 3/7/17.
 */
public class AppExecutors {

    private static final Logger logger = LoggerFactory.getLogger(AppExecutors.class);

    private static ExecutorService defaultExecutor;
    private static ScheduledExecutorService defaultScheduler;

    private static final Map<String, ExecutorService> executors = new HashMap<>();
    private static final Map<String, ScheduledExecutorService> schedulers = new HashMap<>();

    static void init(ThreadPoolExecutor threadPoolExecutor) {
        defaultExecutor = threadPoolExecutor;
        defaultScheduler = Executors.newSingleThreadScheduledExecutor(threadPoolExecutor.getThreadFactory());
    }

    public static ExecutorService executor() {
        return defaultExecutor;
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

    static void shutdownAll() {
        List<ExecutorService> all = new ArrayList<>();
        all.add(defaultExecutor);
        all.add(defaultScheduler);
        all.addAll(executors.values());
        all.addAll(schedulers.values());

        logger.info("Shutting down executors...");
        for(ExecutorService executorService : all) {
            executorService.shutdown();
            try {
                executorService.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.warn("Failed to shutdown executor, halting tasks");
                executorService.shutdownNow();
            }
        }
        logger.info("Executors finished");
    }




}
