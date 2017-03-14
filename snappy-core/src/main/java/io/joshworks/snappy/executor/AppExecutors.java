package io.joshworks.snappy.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by josh on 3/7/17.
 */
public class AppExecutors {

    private static final Map<String, ThreadPoolExecutor> executors = new HashMap<>();
    private static final Map<String, ScheduledThreadPoolExecutor> schedulers = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(AppExecutors.class);
    private static String defaultExecutor;
    private static String defaultScheduler;

    static void init(
            Map<String, ThreadPoolExecutor> execs, String defaultExec,
            Map<String, ScheduledThreadPoolExecutor> scheds, String defaultSched) {

        defaultExecutor = defaultExec;
        defaultScheduler = defaultSched;

        executors.putAll(execs);
        schedulers.putAll(scheds);
    }

    public static void submit(Runnable runnable) {
        submit(defaultExecutor, runnable);
    }

    public static void submit(String poolName, Runnable runnable) {
        ExecutorService executor = getExecutor(poolName);
        executor.submit(runnable);
    }

    public static <T> Future<T> submit(Runnable runnable, T result) {
        return submit(defaultExecutor, runnable, result);
    }

    public static <T> Future<T> submit(String poolName, Runnable runnable, T result) {
        ExecutorService executor = getExecutor(poolName);
        return executor.submit(runnable, result);
    }

    public static <T> ScheduledFuture<T> schedule(Callable<T> callable, long delay, TimeUnit timeUnit) {
        return schedule(defaultScheduler, callable, delay, timeUnit);
    }

    public static <T> ScheduledFuture<T> schedule(String poolName, Callable<T> callable, long delay, TimeUnit timeUnit) {
        ScheduledThreadPoolExecutor scheduler = getScheduler(poolName);
        return scheduler.schedule(callable, delay, timeUnit);
    }

    public static void scheduleAtFixedRate(Runnable runnable, long delay, long period, TimeUnit timeUnit) {
        scheduleAtFixedRate(defaultScheduler, runnable, delay, period, timeUnit);
    }

    public static void scheduleAtFixedRate(String poolName, Runnable runnable, long delay, long period, TimeUnit timeUnit) {
        ScheduledThreadPoolExecutor scheduler = getScheduler(poolName);
        scheduler.scheduleAtFixedRate(runnable, delay, period, timeUnit);
    }

    public static ScheduledFuture<?> scheduleWithFixedDelay(Runnable runnable, long initialDelay, long delay, TimeUnit timeUnit) {
        return scheduleWithFixedDelay(defaultScheduler, runnable, initialDelay, delay, timeUnit);
    }

    public static ScheduledFuture<?> scheduleWithFixedDelay(String poolName, Runnable runnable, long initialDelay, long delay, TimeUnit timeUnit) {
        ScheduledThreadPoolExecutor scheduler = getScheduler(poolName);
        return scheduler.scheduleWithFixedDelay(runnable, initialDelay, delay, timeUnit);
    }

    private static ExecutorService getExecutor(String poolName) {
        ThreadPoolExecutor threadPoolExecutor = executors.get(poolName);
        if (threadPoolExecutor == null) {
            throw new IllegalArgumentException("Thread pool not found for name " + poolName);
        }
        return threadPoolExecutor;
    }

    private static ScheduledThreadPoolExecutor getScheduler(String poolName) {
        ScheduledThreadPoolExecutor threadPoolExecutor = schedulers.get(poolName);
        if (threadPoolExecutor == null) {
            throw new IllegalArgumentException("Thread pool not found for name " + poolName);
        }
        return threadPoolExecutor;
    }

    static Map<String, ThreadPoolExecutor> executors() {
        return new HashMap<>(executors);
    }

    static Map<String, ScheduledThreadPoolExecutor> schedulers() {
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
