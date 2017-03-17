package io.joshworks.snappy;

import io.joshworks.snappy.client.RestClient;
import io.joshworks.snappy.executor.AppExecutors;
import io.joshworks.snappy.executor.ExecutorBootstrap;
import io.joshworks.snappy.executor.ExecutorConfig;
import io.joshworks.snappy.executor.SchedulerConfig;
import io.joshworks.snappy.handler.HandlerManager;
import io.joshworks.snappy.handler.HandlerUtil;
import io.joshworks.snappy.handler.MappedEndpoint;
import io.joshworks.snappy.parser.MediaTypes;
import io.joshworks.snappy.property.PropertyLoader;
import io.joshworks.snappy.rest.ErrorHandler;
import io.joshworks.snappy.rest.ExceptionMapper;
import io.joshworks.snappy.rest.Interceptor;
import io.joshworks.snappy.rest.RestExchange;
import io.joshworks.snappy.websocket.WebsocketEndpoint;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.AbstractReceiveListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.OptionMap;
import org.xnio.Options;
import org.xnio.Xnio;
import org.xnio.XnioWorker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Created by josh on 3/5/17.
 */
public class SnappyServer {

    private static final Logger logger = LoggerFactory.getLogger(SnappyServer.class);

    private final HandlerManager handlerManager = new HandlerManager();

    private final Config config;
    private Undertow server;


    public SnappyServer() {
        this(new Config());
    }

    public SnappyServer(Config config) {
        this.config = config;
    }


    public void start() {
        try {
            Info.logo();
            Info.version();
            PropertyLoader.load();
            Info.deploymentInfo(config, endpoints, basePath);
            ExecutorBootstrap.init(config.schedulers, config.executors);

            logger.info("Starting server...");

            Undertow.Builder serverBuilder = Undertow.builder();

            XnioWorker worker = Xnio.getInstance().createWorker(config.optionBuilder.getMap());
            serverBuilder.setWorker(worker);


            HttpHandler rootHandler = handlerManager.resolveHandlers(endpoints, basePath, config.httpMetrics, config.httpTracer);
            server = serverBuilder.addHttpListener(config.getPort(), config.getBindAddress()).setHandler(rootHandler).build();


            Runtime.getRuntime().addShutdownHook(new Thread(new Shutdown()));

            server.start();

        } catch (Exception e) {
            logger.error("Error while starting the server", e);
            throw new RuntimeException(e);
        }
    }


    public void stop() {
        if (server != null) {
            logger.info("Stopping server...");
            server.stop();
        }
    }

    //--------------------------------------------
    final OptionMap.Builder optionBuilder = OptionMap.builder();
    final List<ExecutorConfig> executors = new ArrayList<>();
    final List<SchedulerConfig> schedulers = new ArrayList<>();
    int port = 8080;
    String bindAddress = "0.0.0.0";
    boolean httpTracer;
    boolean httpMetrics;
    private List<Interceptor> interceptors = new LinkedList<>();

    //TODO set defaults for options in the constructor
    public void tcpNoDeplay(boolean tcpNoDelay) {
        optionBuilder.set(Options.TCP_NODELAY, tcpNoDelay);
    }

    public void port(int port) {
        this.port = port;
    }

    public void address(String address) {
        this.bindAddress = address;
    }

    public void ioThreads(int ioThreads) {
        optionBuilder.set(Options.WORKER_IO_THREADS, ioThreads);
    }

    public void workerThreads(int coreThreads, int maxThreads) {
        optionBuilder.set(Options.WORKER_TASK_CORE_THREADS, coreThreads);
        optionBuilder.set(Options.WORKER_TASK_MAX_THREADS, maxThreads);
    }

    public void enableTracer() {
        this.httpTracer = true;
    }

    public void enableHttpMetrics() {
        this.httpMetrics = true;
    }

    public OptionMap.Builder xnioOptions() {
        return optionBuilder;
    }

    public void executor(String name, int corePoolSize, int maxPoolSize, long keepAliveMillis) {
        validateThreadPool(name, corePoolSize, maxPoolSize, keepAliveMillis);
        ExecutorConfig config = ExecutorConfig.withDefaults(name);
        config.getExecutor().setCorePoolSize(corePoolSize);
        config.getExecutor().setMaximumPoolSize(maxPoolSize);
        config.getExecutor().setKeepAliveTime(keepAliveMillis, TimeUnit.MILLISECONDS);
        this.executors.add(config);
    }

    public void scheduler(String name, int corePoolSize, int maxPoolSize, long keepAliveMillis) {
        validateThreadPool(name, corePoolSize, maxPoolSize, keepAliveMillis);
        SchedulerConfig schedulerConfig = SchedulerConfig.withDefaults(name);
        schedulerConfig.getScheduler().setCorePoolSize(corePoolSize);
        schedulerConfig.getScheduler().setMaximumPoolSize(maxPoolSize);
        schedulerConfig.getScheduler().setKeepAliveTime(keepAliveMillis, TimeUnit.MILLISECONDS);
        this.schedulers.add(schedulerConfig);
    }

    private void validateThreadPool(String name, int corePoolSize, int maxPoolSize, long keepAliveMillis) {
        Objects.requireNonNull(name, Messages.INVALID_POOL_NAME);
        if (corePoolSize < 1) {
            throw new IllegalArgumentException("Core pool size must be greater than zero");
        }
        if (maxPoolSize < 1) {
            throw new IllegalArgumentException("Max pool size must be greater than zero");
        }
        if (corePoolSize < maxPoolSize) {
            throw new IllegalArgumentException("Max pool size must be greater than core pool size");
        }
        if (keepAliveMillis < 0) {
            throw new IllegalArgumentException("Keep alive must be greater or equals zero");
        }

    }


    private final List<MappedEndpoint> endpoints = new ArrayList<>();
    private final ExceptionMapper exceptionMapper = new ExceptionMapper();
    private String basePath = HandlerUtil.BASE_PATH;

    public <T extends Exception> SnappyServer exception(Class<T> exception, ErrorHandler handler) {
        exceptionMapper.put(exception, handler);
        return this;
    }

    public SnappyServer basePath(String basePath) {
        this.basePath = basePath;
        return this;
    }

    public SnappyServer get(String url, Consumer<RestExchange> endpoint, MediaTypes... mediaTypes) {
        endpoints.add(HandlerUtil.rest(Methods.GET, url, endpoint, exceptionMapper, mediaTypes));
        return this;
    }

    public SnappyServer post(String url, Consumer<RestExchange> endpoint, MediaTypes... mediaTypes) {
        endpoints.add(HandlerUtil.rest(Methods.POST, url, endpoint, exceptionMapper, mediaTypes));
        return this;
    }

    public SnappyServer put(String url, Consumer<RestExchange> endpoint, MediaTypes... mediaTypes) {
        endpoints.add(HandlerUtil.rest(Methods.PUT, url, endpoint, exceptionMapper, mediaTypes));
        return this;
    }

    public SnappyServer delete(String url, Consumer<RestExchange> endpoint, MediaTypes... mediaTypes) {
        endpoints.add(HandlerUtil.rest(Methods.DELETE, url, endpoint, exceptionMapper, mediaTypes));
        return this;
    }

    public SnappyServer options(String url, Consumer<RestExchange> endpoint, MediaTypes... mediaTypes) {
        endpoints.add(HandlerUtil.rest(Methods.OPTIONS, url, endpoint, exceptionMapper, mediaTypes));
        return this;
    }

    public SnappyServer head(String url, Consumer<RestExchange> endpoint, MediaTypes... mediaTypes) {
        endpoints.add(HandlerUtil.rest(Methods.HEAD, url, endpoint, exceptionMapper, mediaTypes));
        return this;
    }

    public SnappyServer add(HttpString method, String url, Consumer<RestExchange> endpoint, MediaTypes... mediaTypes) {
        endpoints.add(HandlerUtil.rest(method, url, endpoint, exceptionMapper, mediaTypes));
        return this;
    }

    public SnappyServer websocket(String url, AbstractReceiveListener endpoint) {
        endpoints.add(HandlerUtil.websocket(url, endpoint));
        return this;
    }

    public SnappyServer websocket(String url, WebSocketConnectionCallback connectionCallback) {
        endpoints.add(HandlerUtil.websocket(url, connectionCallback));
        return this;
    }

    public SnappyServer websocket(String url, WebsocketEndpoint websocketEndpoint) {
        endpoints.add(HandlerUtil.websocket(url, websocketEndpoint));
        return this;
    }

    public SnappyServer sse(String url) {
        Objects.requireNonNull(url, Messages.INVALID_URL);
        endpoints.add(HandlerUtil.sse(url));
        return this;
    }

    public SnappyServer staticFiles(String url, String docPath) {
        Objects.requireNonNull(url, Messages.INVALID_URL);
        endpoints.add(HandlerUtil.staticFiles(url, docPath));
        return this;
    }

    public SnappyServer staticFiles(String url) {
        Objects.requireNonNull(url, Messages.INVALID_URL);
        endpoints.add(HandlerUtil.staticFiles(url));
        return this;
    }

    private void checkStarted() {

    }

    public List<MappedEndpoint> getEndpoints() {
        return Collections.unmodifiableList(endpoints);
    }

    public String getBasePath() {
        return basePath;
    }

    private static class Shutdown implements Runnable {

        @Override
        public void run() {
            RestClient.shutdown();
            AppExecutors.shutdownAll();
        }
    }
}
