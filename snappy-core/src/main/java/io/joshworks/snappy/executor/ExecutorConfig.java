package io.joshworks.snappy.executor;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by josh on 3/14/17.
 */
public class ExecutorConfig extends ExecutorConfigBase {

    private ThreadPoolExecutor executor;

    public ExecutorConfig(String name) {
        super(name);
    }

    public static ExecutorConfig withDefaults(String name) {
        ExecutorConfig defaultConfig = new ExecutorConfig(name);
        defaultConfig.executor = new ThreadPoolExecutor(0, 5, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
        return defaultConfig;
    }

    public ExecutorConfig executor(ThreadPoolExecutor executor) {
        this.executor = executor;
        return this;
    }

    public ThreadPoolExecutor getExecutor() {
        return executor;
    }
}
