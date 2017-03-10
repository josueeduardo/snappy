package com.josue.simpletow.property;

/**
 * Created by josh on 3/10/17.
 */
public final class MicroserverProperties {

    public static final String PROPERTIES_NAME = "microserver.properties";

    //http
    public static final String HTTP_PORT = "http.port";
    public static final String HTTP_BIND_ADDRESS = "http.bind";
    public static final String HTTP_TRACER = "http.tracer";
    public static final String HTTP_METRICS = "http.metrics";
    public static final String TCP_NO_DELAY = "tcp.nodelay";

    //core server
    public static final String XNIO_IO_THREADS = "xnio.io.threads";
    public static final String XNIO_MAX_WORKER_THREAD = "xnio.worker.max.thread";
    public static final String XNIO_CORE_WORKER_THREAD = "xnio.worker.core.thread";

    //app executors
    public static final String EXECUTOR_PREFIX = "executor.";
    public static final String SCHEDULER_PREFIX = "scheduler.";
    public static final String EXECUTOR_CORE_POOL_SIZE = "core";
    public static final String EXECUTOR_MAX_POOL_SIZE = "max";




}
