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

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
    private final String poolName;
    private final long keepAliveTime;
    private final boolean shutdown;

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
        keepAliveTime = executor.getKeepAliveTime(TimeUnit.MILLISECONDS);
        shutdown = executor.isShutdown();
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

    public long getKeepAliveTime() {
        return keepAliveTime;
    }

    public boolean isShutdown() {
        return shutdown;
    }
}
