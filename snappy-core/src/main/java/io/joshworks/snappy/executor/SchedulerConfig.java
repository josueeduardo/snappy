package io.joshworks.snappy.executor;

import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Created by josh on 3/14/17.
 */
public class SchedulerConfig extends ExecutorConfigBase {

    private ScheduledThreadPoolExecutor scheduler;

    public SchedulerConfig(String name) {
        super(name);
    }

    public SchedulerConfig executor(ScheduledThreadPoolExecutor scheduler) {
        this.scheduler = scheduler;
        return this;
    }

    public static SchedulerConfig withDefaults(String name) {
        SchedulerConfig defaultConfig = new SchedulerConfig(name);
        defaultConfig.scheduler = new ScheduledThreadPoolExecutor(2);
        return defaultConfig;
    }

    ScheduledThreadPoolExecutor getScheduler() {
        return scheduler;
    }
}
