package io.joshworks.microserver.property;

/**
 * Created by josh on 3/10/17.
 */
public final class MicroserverProperties {

    public static final String PROPERTIES_NAME = "io.joshworks.microserver.properties";

    //http
    public static final String HTTP_PORT = "http.port";
    public static final String HTTP_BIND_ADDRESS = "http.bind";
    public static final String HTTP_TRACER = "http.tracer";
    public static final String HTTP_METRICS = "http.metrics";
    public static final String TCP_NO_DELAY = "tcp.nodelay";

    //core server
    public static final String XNIO_IO_THREADS = "xnio.io.threads";
    public static final String XNIO_MAX_WORKER_THREAD = "xnio.worker.maxThread";
    public static final String XNIO_CORE_WORKER_THREAD = "xnio.worker.coreThread";

    //app executors
    public static final String EXECUTOR_PREFIX = "executor.";
    public static final String SCHEDULER_PREFIX = "scheduler.";
    public static final String EXECUTOR_CORE_POOL_SIZE = "core";
    public static final String EXECUTOR_MAX_POOL_SIZE = "max";

    //ssr discovery registry
    public static final String SSR_AWS = "ssr.isAws";
    public static final String SSR_REGISTRY_HOST = "ssr.registry.host";
    public static final String SSR_REGISTRY_PORT = "ssr.registry.port";
    //ssr discovery service
    public static final String SSR_SERVICE_HOST = "ssr.service.host";
    public static final String SSR_SERVICE_PORT = "ssr.service.port";
    public static final String SSR_USE_HOST = "ssr.service.useHostname";

//    public static final String SSR_ENVIRONMENT_SELECTOR = "ssr.environment";
//    public static final String PROPERTIES_FILE_NAME = "registry";




}
