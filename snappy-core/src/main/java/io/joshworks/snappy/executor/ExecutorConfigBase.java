package io.joshworks.snappy.executor;

/**
 * Created by josh on 3/14/17.
 */
public abstract class ExecutorConfigBase {

    private final String name;
    private boolean defaultExecutor;

    public ExecutorConfigBase(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    boolean isDefaultExecutor() {
        return defaultExecutor;
    }

    public ExecutorConfigBase markAsDefault() {
        this.defaultExecutor = true;
        return this;
    }
}
