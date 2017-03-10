package io.joshworks.microserver.metric;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by josh on 3/8/17.
 */
public class PoolMetric {

    private final int activeCount;
    private final long completedTaskCount;
    private final int corePoolSize;
    private final int largestPoolSize;
    private final int maximumPoolSize;
    private final int poolSize;
    private final String rejectionPolicy;
    private final long taskCount;
    private final int queueCapacity;
    private final int queuedTasks;
    private String poolName;

    public PoolMetric(String poolName, ThreadPoolExecutor executor) {
        this.poolName = poolName;
        activeCount = executor.getActiveCount();
        completedTaskCount = executor.getCompletedTaskCount();
        corePoolSize = executor.getCorePoolSize();
        largestPoolSize = executor.getLargestPoolSize();
        maximumPoolSize = executor.getMaximumPoolSize();
        poolSize = executor.getPoolSize();
        rejectionPolicy = executor.getRejectedExecutionHandler().getClass().getSimpleName();
        taskCount = executor.getTaskCount();
        queuedTasks = executor.getQueue().size();
        queueCapacity = executor.getQueue().remainingCapacity() + executor.getQueue().size();
    }

    public int getActiveCount() {
        return activeCount;
    }

    public long getCompletedTaskCount() {
        return completedTaskCount;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public int getLargestPoolSize() {
        return largestPoolSize;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public String getPoolName() {
        return poolName;
    }

    public long getTaskCount() {
        return taskCount;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }

    public int getQueuedTasks() {
        return queuedTasks;
    }

    public String getRejectionPolicy() {
        return rejectionPolicy;
    }
}
