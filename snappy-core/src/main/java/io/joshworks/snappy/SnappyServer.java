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

package io.joshworks.snappy;

import io.joshworks.snappy.admin.AdminManager;
import io.joshworks.snappy.client.RestClient;
import io.joshworks.snappy.executor.AppExecutors;
import io.joshworks.snappy.executor.ExecutorBootstrap;
import io.joshworks.snappy.executor.ExecutorConfig;
import io.joshworks.snappy.executor.SchedulerConfig;
import io.joshworks.snappy.handler.HandlerManager;
import io.joshworks.snappy.handler.HandlerUtil;
import io.joshworks.snappy.handler.MappedEndpoint;
import io.joshworks.snappy.multipart.MultipartExchange;
import io.joshworks.snappy.parser.JsonParser;
import io.joshworks.snappy.parser.MediaTypes;
import io.joshworks.snappy.parser.Parsers;
import io.joshworks.snappy.parser.PlainTextParser;
import io.joshworks.snappy.property.PropertyLoader;
import io.joshworks.snappy.rest.ErrorHandler;
import io.joshworks.snappy.rest.ExceptionMapper;
import io.joshworks.snappy.rest.Group;
import io.joshworks.snappy.rest.Interceptor;
import io.joshworks.snappy.rest.Interceptors;
import io.joshworks.snappy.rest.RestExchange;
import io.joshworks.snappy.ext.ExtensionProxy;
import io.joshworks.snappy.ext.ServerData;
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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by josh on 3/5/17.
 */
public class SnappyServer {

    public static final String LOGGER_NAME = "snappy";

    private static final Logger logger = LoggerFactory.getLogger(LOGGER_NAME);

    private final HandlerManager handlerManager = new HandlerManager();
    private final AdminManager adminManager = new AdminManager();
    private final ExtensionProxy extensions = new ExtensionProxy();
    //--------------------------------------------
    private final OptionMap.Builder optionBuilder = OptionMap.builder();
    private final List<ExecutorConfig> executors = new ArrayList<>();
    private final List<SchedulerConfig> schedulers = new ArrayList<>();
    //-------------------------------------------
    private final List<MappedEndpoint> endpoints = new ArrayList<>();
    private final ExceptionMapper exceptionMapper = new ExceptionMapper();
    //--------------------- REST -------------------
    private final Deque<String> groups = new ArrayDeque<>();
    private Undertow server;
    private int port = 9000;
    private String bindAddress = "0.0.0.0";
    private boolean httpTracer;
    private boolean httpMetrics;
    private int adminPort = 9001;
    private String adminBindAddress = "127.0.0.1";
    private List<Interceptor> interceptors = new LinkedList<>();
    private List<Interceptor> rootInterceptors = new LinkedList<>();
    private String basePath = HandlerUtil.BASE_PATH;
    private boolean started = false;

    private static final Object LOCK = new Object();
    private static SnappyServer INSTANCE;


    private SnappyServer() {
        optionBuilder.set(Options.TCP_NODELAY, true);
        int processors = Runtime.getRuntime().availableProcessors();
        this.optionBuilder.set(Options.WORKER_IO_THREADS, processors);
        this.optionBuilder.set(Options.WORKER_TASK_CORE_THREADS, processors * 2);
    }

    private static SnappyServer instance() {
        if (INSTANCE == null) {
            synchronized (LOCK) {
                if (INSTANCE == null) {
                    INSTANCE = new SnappyServer();
                }
            }
        }
        return INSTANCE;
    }

    private static SnappyServer createServer() {
        return new SnappyServer();
    }

    public static void start() {
        instance().startServer();
    }

    public static synchronized void stop() {
        instance().stopServer();
    }

    //TODO set defaults for options in the constructor
    public static synchronized void tcpNoDeplay(boolean tcpNoDelay) {
        checkStarted();
        instance().optionBuilder.set(Options.TCP_NODELAY, tcpNoDelay);
    }

    public static synchronized void adminPort(int adminPort) {
        checkStarted();
        instance().adminPort = adminPort;
    }

    public static synchronized void adminAddress(String address) {
        checkStarted();
        instance().adminBindAddress = address;
    }

    public static synchronized void port(int port) {
        checkStarted();
        instance().port = port;
    }

    public static synchronized void address(String address) {
        checkStarted();
        instance().bindAddress = address;
    }

    public static synchronized void ioThreads(int ioThreads) {
        checkStarted();
        instance().optionBuilder.set(Options.WORKER_IO_THREADS, ioThreads);
    }

    public static synchronized void workerThreads(int coreThreads, int maxThreads) {
        checkStarted();
        instance().optionBuilder.set(Options.WORKER_TASK_CORE_THREADS, coreThreads);
        instance().optionBuilder.set(Options.WORKER_TASK_MAX_THREADS, maxThreads);
    }

    public static synchronized void workerThreads(int coreThreads, int maxThreads, int keepAliveMillis) {
        checkStarted();
        workerThreads(coreThreads, maxThreads);
        instance().optionBuilder.set(Options.WORKER_TASK_KEEPALIVE, keepAliveMillis);
    }

    public static synchronized void enableTracer() {
        checkStarted();
        instance().httpTracer = true;
    }

    public static synchronized void enableHttpMetrics() {
        checkStarted();
        instance().httpMetrics = true;
    }

    public static synchronized OptionMap.Builder xnioOptions() {
        checkStarted();
        return instance().optionBuilder;
    }

    public static synchronized void executor(String name, int corePoolSize, int maxPoolSize, long keepAliveMillis) {
        checkStarted();
        validateThreadPool(name, corePoolSize, maxPoolSize, keepAliveMillis);
        ExecutorConfig config = ExecutorConfig.withDefaults(name);
        config.getExecutor().setCorePoolSize(corePoolSize);
        config.getExecutor().setMaximumPoolSize(maxPoolSize);
        config.getExecutor().setKeepAliveTime(keepAliveMillis, TimeUnit.MILLISECONDS);
        instance().executors.add(config);
    }

    public static synchronized void scheduler(String name, int corePoolSize, long keepAliveMillis) {
        checkStarted();
        validateThreadPool(name, corePoolSize, corePoolSize, keepAliveMillis);
        SchedulerConfig schedulerConfig = SchedulerConfig.withDefaults(name);
        schedulerConfig.getScheduler().setCorePoolSize(corePoolSize);
        schedulerConfig.getScheduler().setKeepAliveTime(keepAliveMillis, TimeUnit.MILLISECONDS);
        instance().schedulers.add(schedulerConfig);
    }

    private static void validateThreadPool(String name, int corePoolSize, int maxPoolSize, long keepAliveMillis) {
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

    public static synchronized <T extends Exception> void exception(Class<T> exception, ErrorHandler handler) {
        checkStarted();
        instance().exceptionMapper.put(exception, handler);
    }

    public static synchronized void basePath(String basePath) {
        checkStarted();
        instance().basePath = basePath;
    }

    public static synchronized void group(String groupPath, Group group) {
        checkStarted();
        instance().groups.addLast(groupPath);
        group.addResources();
        instance().groups.removeLast();
    }

    //TODO add url validation

    public static synchronized void beforeAll(String url, Consumer<Exchange> consumer) {
        checkStarted();
        instance().rootInterceptors.add(new Interceptor(Interceptor.Type.BEFORE, url, consumer));
    }

    public static synchronized void afterAll(String url, Consumer<Exchange> consumer) {
        checkStarted();
        instance().rootInterceptors.add(new Interceptor(Interceptor.Type.AFTER, url, consumer));
    }

    public static synchronized void cors() {
        checkStarted();
        instance().rootInterceptors.add(Interceptors.cors());
    }

    public static synchronized void before(String url, Consumer<Exchange> consumer) {
        checkStarted();
        instance().interceptors.add(new Interceptor(Interceptor.Type.BEFORE, url, consumer));
    }

    public static synchronized void after(String url, Consumer<Exchange> consumer) {
        checkStarted();
        instance().interceptors.add(new Interceptor(Interceptor.Type.AFTER, url, consumer));
    }

    public static synchronized void get(String url, Consumer<RestExchange> endpoint, MediaTypes... mediaTypes) {
        checkStarted();
        instance().endpoints.add(HandlerUtil.rest(Methods.GET, resolvePath(url), endpoint, instance().exceptionMapper, instance().interceptors, mediaTypes));
    }

    public static synchronized void post(String url, Consumer<RestExchange> endpoint, MediaTypes... mediaTypes) {
        checkStarted();
        instance().endpoints.add(HandlerUtil.rest(Methods.POST, resolvePath(url), endpoint, instance().exceptionMapper, instance().interceptors, mediaTypes));
    }

    public static synchronized void put(String url, Consumer<RestExchange> endpoint, MediaTypes... mediaTypes) {
        checkStarted();
        instance().endpoints.add(HandlerUtil.rest(Methods.PUT, resolvePath(url), endpoint, instance().exceptionMapper, instance().interceptors, mediaTypes));
    }

    public static synchronized void delete(String url, Consumer<RestExchange> endpoint, MediaTypes... mediaTypes) {
        checkStarted();
        instance().endpoints.add(HandlerUtil.rest(Methods.DELETE, resolvePath(url), endpoint, instance().exceptionMapper, instance().interceptors, mediaTypes));
    }

    public static synchronized void options(String url, Consumer<RestExchange> endpoint, MediaTypes... mediaTypes) {
        checkStarted();
        instance().endpoints.add(HandlerUtil.rest(Methods.OPTIONS, resolvePath(url), endpoint, instance().exceptionMapper, instance().interceptors, mediaTypes));
    }

    public static synchronized void head(String url, Consumer<RestExchange> endpoint, MediaTypes... mediaTypes) {
        checkStarted();
        instance().endpoints.add(HandlerUtil.rest(Methods.HEAD, resolvePath(url), endpoint, instance().exceptionMapper, instance().interceptors, mediaTypes));
    }

    public static synchronized void add(HttpString method, String url, Consumer<RestExchange> endpoint, MediaTypes... mediaTypes) {
        checkStarted();
        instance().endpoints.add(HandlerUtil.rest(method, resolvePath(url), endpoint, instance().exceptionMapper, instance().interceptors, mediaTypes));
    }

    public static synchronized void websocket(String url, AbstractReceiveListener endpoint) {
        checkStarted();
        instance().endpoints.add(HandlerUtil.websocket(resolvePath(url), endpoint, instance().interceptors));
    }

    public static synchronized void websocket(String url, WebSocketConnectionCallback connectionCallback) {
        checkStarted();
        instance().endpoints.add(HandlerUtil.websocket(resolvePath(url), connectionCallback, instance().interceptors));
    }

    public static synchronized void websocket(String url, WebsocketEndpoint websocketEndpoint) {
        checkStarted();
        instance().endpoints.add(HandlerUtil.websocket(resolvePath(url), websocketEndpoint, instance().interceptors));
    }

    public static synchronized void sse(String url) {
        checkStarted();
        Objects.requireNonNull(url, Messages.INVALID_URL);
        instance().endpoints.add(HandlerUtil.sse(resolvePath(url), instance().interceptors));
    }

    public static synchronized void staticFiles(String url, String docPath) {
        checkStarted();
        Objects.requireNonNull(url, Messages.INVALID_URL);
        instance().endpoints.add(HandlerUtil.staticFiles(resolvePath(url), docPath, instance().interceptors));
    }

    public static synchronized void staticFiles(String url) {
        checkStarted();
        Objects.requireNonNull(url, Messages.INVALID_URL);
        instance().endpoints.add(HandlerUtil.staticFiles(resolvePath(url), instance().interceptors));
    }

    public static synchronized void multipart(String url, Consumer<MultipartExchange> endpoint) {
        checkStarted();
        Objects.requireNonNull(url, Messages.INVALID_URL);
        instance().endpoints.add(HandlerUtil.multipart(url, endpoint, instance().interceptors));

    }

    public static synchronized void multipart(String url, Consumer<MultipartExchange> endpoint, long maxSize) {
        checkStarted();
        Objects.requireNonNull(url, Messages.INVALID_URL);
        instance().endpoints.add(HandlerUtil.multipart(url, endpoint, instance().interceptors, maxSize));
    }

    private static String resolvePath(String url) {
        return instance().groups.stream().collect(Collectors.joining("")) + url;
    }

    private static void checkStarted() {
        if (instance().started) {
            throw new IllegalStateException("Server already started");
        }
    }

    private synchronized void startServer() {
        try {
            checkStarted();
            Info.logo();
            Info.version();

            long start = System.currentTimeMillis();
            logger.info("Starting server...");

            PropertyLoader.load();
            Info.deploymentInfo(httpMetrics, httpTracer, port, httpMetrics, executors, schedulers, optionBuilder, endpoints, basePath);
            ExecutorBootstrap.init(schedulers, executors);

            //register default parsers
            Parsers.register(new JsonParser());
            Parsers.register(new PlainTextParser());

            RestClient.init();

            extensions.load();

            Undertow.Builder serverBuilder = Undertow.builder();

            XnioWorker worker = Xnio.getInstance().createWorker(optionBuilder.getMap());
            serverBuilder.setWorker(worker);

            HttpHandler rootHandler = handlerManager.createRootHandler(endpoints, rootInterceptors, adminManager, basePath, httpMetrics, httpTracer);

            server = serverBuilder
                    .addHttpListener(port, bindAddress, rootHandler)
                    .addHttpListener(adminPort, adminBindAddress, adminManager.resolveHandlers())
                    .build();

            server.start();
            started = true;

            logger.info("Server started in {}ms", System.currentTimeMillis() - start);

            extensions.onStart(new ServerData(adminPort, bindAddress, httpTracer, httpMetrics, adminPort, adminBindAddress, basePath, endpoints));

        } catch (Exception e) {
            started = false;
            logger.error("Error while starting the server", e);
            throw new RuntimeException(e);
        }
    }

    private synchronized void stopServer() {
        if (server != null && started) {
            logger.info("Stopping server...");

            extensions.onShutdown();

            RestClient.shutdown();
            AppExecutors.shutdownAll();

            server.stop();
            INSTANCE = null;
            started = false;
        }
    }

}
