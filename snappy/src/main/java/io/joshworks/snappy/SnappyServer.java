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

import io.joshworks.snappy.executor.ExecutorBootstrap;
import io.joshworks.snappy.executor.ExecutorConfig;
import io.joshworks.snappy.executor.SchedulerConfig;
import io.joshworks.snappy.ext.ExtensionProxy;
import io.joshworks.snappy.ext.ServerData;
import io.joshworks.snappy.ext.SnappyExtension;
import io.joshworks.snappy.handler.HandlerManager;
import io.joshworks.snappy.handler.HandlerUtil;
import io.joshworks.snappy.handler.MappedEndpoint;
import io.joshworks.snappy.http.HttpException;
import io.joshworks.snappy.http.multipart.MultipartExchange;
import io.joshworks.snappy.parser.JsonParser;
import io.joshworks.snappy.parser.MediaTypes;
import io.joshworks.snappy.parser.Parsers;
import io.joshworks.snappy.parser.PlainTextParser;
import io.joshworks.snappy.property.AppProperties;
import io.joshworks.snappy.property.PropertyKey;
import io.joshworks.snappy.http.ErrorHandler;
import io.joshworks.snappy.http.ExceptionMapper;
import io.joshworks.snappy.http.Group;
import io.joshworks.snappy.http.Interceptor;
import io.joshworks.snappy.http.Interceptors;
import io.joshworks.snappy.http.HttpConsumer;
import io.joshworks.snappy.http.HttpExchange;
import io.joshworks.snappy.websocket.WebsocketEndpoint;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.sse.ServerSentEventConnectionCallback;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.OptionMap;
import org.xnio.Options;
import org.xnio.Xnio;
import org.xnio.XnioWorker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static io.joshworks.snappy.handler.HandlerUtil.BASE_PATH;

/**
 * Created by josh on 3/5/17.
 */
public class SnappyServer {

    public static final String LOGGER_NAME = "snappy";
    private static final int PORT = 9000;

    private static final Logger logger = LoggerFactory.getLogger(LOGGER_NAME);

    private final AdminManager adminManager = new AdminManager();
    private final ExtensionProxy extensions = new ExtensionProxy();
    private XnioWorker worker;

    //--------------------------------------------
    private final List<Runnable> startListeners = new ArrayList<>();
    private final List<Runnable> shutdownListeners = new ArrayList<>();

    //--------------------------------------------
    private final OptionMap.Builder optionBuilder = OptionMap.builder();
    private final List<ExecutorConfig> executors = new ArrayList<>();
    private final List<SchedulerConfig> schedulers = new ArrayList<>();
    //-------------------------------------------

    private final List<MappedEndpoint> endpoints = new ArrayList<>();
    private final ExceptionMapper exceptionMapper = new ExceptionMapper();

    //--------------------- HTTP -------------------
    private Undertow server;
    private int port = 9000;
    private String bindAddress = "0.0.0.0";
    private boolean httpTracer;
    private final List<Interceptor> interceptors = new LinkedList<>();
    private final List<Interceptor> rootInterceptors = new LinkedList<>();
    private boolean gzipEnabled = false;
    private String basePath = BASE_PATH;
    private boolean started = false;

    private static final Object LOCK = new Object();
    private static SnappyServer INSTANCE;


    private SnappyServer() {
        optionBuilder.set(Options.TCP_NODELAY, true);
        int processors = Runtime.getRuntime().availableProcessors();
        this.optionBuilder.set(Options.WORKER_IO_THREADS, processors);
        this.optionBuilder.set(Options.WORKER_TASK_CORE_THREADS, processors);
        this.optionBuilder.set(Options.WORKER_TASK_MAX_THREADS, processors * 10);
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

    public static void start() {
        instance().startServer();
    }

    public static synchronized void stop() {
        instance().stopServer();
        INSTANCE = null;
    }

    public static synchronized void tcpNoDeplay(boolean tcpNoDelay) {
        checkStarted();
        instance().optionBuilder.set(Options.TCP_NODELAY, tcpNoDelay);
    }

    public static synchronized void adminPort(int adminPort) {
        checkStarted();
        instance().adminManager.setPort(adminPort);
    }

    public static synchronized void adminAddress(String address) {
        checkStarted();
        instance().adminManager.setBindAddress(address);
    }

    public static synchronized void port(int port) {
        checkStarted();
        instance().port = port;
    }

    public static synchronized void portOffset(int offset) {
        checkStarted();
        instance().port = PORT + offset;
        instance().adminManager.setPort(AdminManager.ADMIN_PORT + offset);
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

    public static synchronized OptionMap.Builder xnioOptions() {
        checkStarted();
        return instance().optionBuilder;
    }

    public static synchronized void executor(String name, int corePoolSize, int maxPoolSize, long keepAliveMillis) {
        checkStarted();
        ExecutorConfig config = ExecutorConfig.withDefaults(name);
        config.getExecutor().setCorePoolSize(corePoolSize);
        config.getExecutor().setMaximumPoolSize(maxPoolSize);
        config.getExecutor().setKeepAliveTime(keepAliveMillis, TimeUnit.MILLISECONDS);
        instance().executors.add(config);
    }

    public static synchronized void scheduler(String name, int corePoolSize, long keepAliveMillis) {
        checkStarted();
        SchedulerConfig schedulerConfig = SchedulerConfig.withDefaults(name);
        schedulerConfig.getScheduler().setCorePoolSize(corePoolSize);
        schedulerConfig.getScheduler().setMaximumPoolSize(corePoolSize);
        schedulerConfig.getScheduler().setKeepAliveTime(keepAliveMillis, TimeUnit.MILLISECONDS);
        instance().schedulers.add(schedulerConfig);
    }

    /**
     * Register an error interceptor that captures exception thrown from endpoints, allowing to change the http response.
     * The default response is mapped to any {@link Exception} and returns an {@link HttpException} body
     *
     * @param exception The exception type that will trigger the handler
     * @param handler   The handler that handles the exception and send the appropriate response.
     */
    public static synchronized <T extends Exception> void exception(Class<T> exception, ErrorHandler<T> handler) {
        checkStarted();
        instance().exceptionMapper.put(exception, handler);
    }

    /**
     * Set the base path for REST endpoints only. This configuration has no effect on ws, sse, staticFiles and multipart endpoints
     *
     * @param basePath The base path for the REST endpoints
     */
    public static synchronized void basePath(String basePath) {
        checkStarted();
        instance().basePath = HandlerUtil.parseUrl(basePath);
    }

    public static synchronized void enableGzip() {
       instance().gzipEnabled = true;
    }

    /**
     * @param groupPath The path to be used by the endpoints declared under this group
     * @param group     The grouped endpoints, meant to be used as lambda function.
     */
    public static synchronized void group(String groupPath, Group group) {
        checkStarted();
        HandlerUtil.group(groupPath, group);
    }

    /**
     * Register a new Extension that will be executed on startup. Extensions allows the implementation to change the server behavior.
     *
     * @param extension The extension to be registered
     */
    public static synchronized void register(SnappyExtension extension) {
        checkStarted();
        instance().extensions.register(extension);
    }

    /**
     * Adds an interceptor that executes before any other handler.
     * Calling {@link Exchange#end()} or {@link Exchange#send(Object)} causes the request to complete.
     *
     * @param url      The URL pattern the interceptor will execute, only exact matches and wildcard (*) is allowed
     * @param consumer The code to be executed when a URL matches the provided pattern
     */
    public static synchronized void beforeAll(String url, Consumer<Exchange> consumer) {
        checkStarted();
        instance().rootInterceptors.add(new Interceptor(Interceptor.Type.BEFORE, HandlerUtil.parseUrl(url), consumer));
    }

    /**
     * Adds an interceptor that executed after all other handlers are executed, changes to {@link Exchange} has no effect.
     *
     * @param url      The URL pattern the interceptor will execute, only exact matches and wildcard (*) is allowed
     * @param consumer The code to be executed when a URL matches the provided pattern
     */
    public static synchronized void afterAll(String url, Consumer<Exchange> consumer) {
        checkStarted();
        instance().rootInterceptors.add(new Interceptor(Interceptor.Type.AFTER, HandlerUtil.parseUrl(url), consumer));
    }

    /**
     * Adds an interceptor that executed right before the endpoint.
     * Calling {@link Exchange#end()} or {@link Exchange#send(Object)} causes the request to complete.
     *
     * @param url      The URL pattern the interceptor will execute, only exact matches and wildcard (*) is allowed
     * @param consumer The code to be executed when a URL matches the provided pattern
     */
    public static synchronized void before(String url, HttpConsumer<Exchange> consumer) {
        checkStarted();
        instance().interceptors.add(new Interceptor(Interceptor.Type.BEFORE, HandlerUtil.parseUrl(url), consumer));
    }

    /**
     * Adds an interceptor that executed after the endpoint, changes to {@link Exchange} has no effect.
     *
     * @param url      The URL pattern the interceptor will execute, only exact matches and wildcard (*) is allowed
     * @param consumer The code to be executed when a URL matches the provided pattern
     */
    public static synchronized void after(String url, HttpConsumer<Exchange> consumer) {
        checkStarted();
        instance().interceptors.add(new Interceptor(Interceptor.Type.AFTER, HandlerUtil.parseUrl(url), consumer));
    }

    /**
     * Enable CORS by setting:
     * <ul>
     * <li>Access-Control-Allow-Origin: *</li>
     * <li>Access-Control-Allow-Credentials: true</li>
     * <li>Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, HEAD</li>
     * <li>Access-Control-Allow-Headers: Origin, Accept, X-Requested-With, Content-Type, Authorization, Access-Control-Request-Method, Access-Control-Request-Headers</li>
     * </ul>
     * <p>
     * For custom headers, use {@link SnappyServer#before(String, HttpConsumer)}
     */
    public static synchronized void cors() {
        checkStarted();
        instance().rootInterceptors.add(Interceptors.cors());
        instance().adminManager.addInterceptor(Interceptors.cors());
    }

    /**
     * Define a REST endpoint mapped to HTTP GET
     *
     * @param url        The relative URL to be map this endpoint.
     * @param endpoint   The endpoint handler
     * @param mediaTypes (Optional) The accepted and returned types for this endpoint
     */
    public static synchronized void get(String url, HttpConsumer<HttpExchange> endpoint, MediaTypes... mediaTypes) {
        checkStarted();
        instance().endpoints.add(HandlerUtil.rest(Methods.GET, url, endpoint, instance().exceptionMapper,  mediaTypes));
    }

    /**
     * Define a REST endpoint mapped to HTTP POST
     *
     * @param url        The relative URL to be map this endpoint.
     * @param endpoint   The endpoint handler
     * @param mediaTypes (Optional) The accepted and returned types for this endpoint
     */
    public static synchronized void post(String url, HttpConsumer<HttpExchange> endpoint, MediaTypes... mediaTypes) {
        checkStarted();
        instance().endpoints.add(HandlerUtil.rest(Methods.POST, url, endpoint, instance().exceptionMapper,  mediaTypes));
    }

    /**
     * Define a REST endpoint mapped to HTTP PUT
     *
     * @param url        The relative URL to be map this endpoint.
     * @param endpoint   The endpoint handler
     * @param mediaTypes (Optional) The accepted and returned types for this endpoint
     */
    public static synchronized void put(String url, HttpConsumer<HttpExchange> endpoint, MediaTypes... mediaTypes) {
        checkStarted();
        instance().endpoints.add(HandlerUtil.rest(Methods.PUT, url, endpoint, instance().exceptionMapper,  mediaTypes));
    }

    /**
     * Define a REST endpoint mapped to HTTP DELETE
     *
     * @param url        The relative URL to be map this endpoint.
     * @param endpoint   The endpoint handler
     * @param mediaTypes (Optional) The accepted and returned types for this endpoint
     */
    public static synchronized void delete(String url, HttpConsumer<HttpExchange> endpoint, MediaTypes... mediaTypes) {
        checkStarted();
        instance().endpoints.add(HandlerUtil.rest(Methods.DELETE, url, endpoint, instance().exceptionMapper,  mediaTypes));
    }

    /**
     * Define a REST endpoint mapped to HTTP OPTIONS
     *
     * @param url        The relative URL to be map this endpoint.
     * @param endpoint   The endpoint handler
     * @param mediaTypes (Optional) The accepted and returned types for this endpoint
     *                   </pre>
     */
    public static synchronized void options(String url, HttpConsumer<HttpExchange> endpoint, MediaTypes... mediaTypes) {
        checkStarted();
        instance().endpoints.add(HandlerUtil.rest(Methods.OPTIONS, url, endpoint, instance().exceptionMapper,  mediaTypes));
    }

    /**
     * Define a REST endpoint mapped to HTTP HEAD
     *
     * @param url        The relative URL to be map this endpoint.
     * @param endpoint   The endpoint handler
     * @param mediaTypes (Optional) The accepted and returned types for this endpoint
     */
    public static synchronized void head(String url, HttpConsumer<HttpExchange> endpoint, MediaTypes... mediaTypes) {
        checkStarted();
        instance().endpoints.add(HandlerUtil.rest(Methods.HEAD, url, endpoint, instance().exceptionMapper,  mediaTypes));
    }

    /**
     * Define a REST endpoint mapped to HTTP GET, uses "/" as url
     *
     * @param endpoint   The endpoint handler
     * @param mediaTypes (Optional) The accepted and returned types for this endpoint
     */
    public static synchronized void get(HttpConsumer<HttpExchange> endpoint, MediaTypes... mediaTypes) {
        get(HandlerUtil.BASE_PATH, endpoint, mediaTypes);
    }

    /**
     * Define a REST endpoint mapped to HTTP POST, uses "/" as url
     *
     * @param endpoint   The endpoint handler
     * @param mediaTypes (Optional) The accepted and returned types for this endpoint
     */
    public static synchronized void post(HttpConsumer<HttpExchange> endpoint, MediaTypes... mediaTypes) {
        post(HandlerUtil.BASE_PATH, endpoint, mediaTypes);
    }

    /**
     * Define a REST endpoint mapped to HTTP PUT, uses "/" as url
     *
     * @param endpoint   The endpoint handler
     * @param mediaTypes (Optional) The accepted and returned types for this endpoint
     */
    public static synchronized void put(HttpConsumer<HttpExchange> endpoint, MediaTypes... mediaTypes) {
        put(HandlerUtil.BASE_PATH, endpoint, mediaTypes);
    }

    /**
     * Define a REST endpoint mapped to HTTP DELETE, uses "/" as url
     *
     * @param endpoint   The endpoint handler
     * @param mediaTypes (Optional) The accepted and returned types for this endpoint
     */
    public static synchronized void delete(HttpConsumer<HttpExchange> endpoint, MediaTypes... mediaTypes) {
        delete(HandlerUtil.BASE_PATH, endpoint, mediaTypes);
    }

    /**
     * Define a REST endpoint mapped to HTTP OPTIONS, uses "/" as url
     *
     * @param endpoint   The endpoint handler
     * @param mediaTypes (Optional) The accepted and returned types for this endpoint
     */
    public static synchronized void options(HttpConsumer<HttpExchange> endpoint, MediaTypes... mediaTypes) {
        options(HandlerUtil.BASE_PATH, endpoint, mediaTypes);
    }

    /**
     * Define a REST endpoint mapped to HTTP HEAD, uses "/" as url
     *
     * @param endpoint   The endpoint handler
     * @param mediaTypes (Optional) The accepted and returned types for this endpoint
     */
    public static synchronized void head(HttpConsumer<HttpExchange> endpoint, MediaTypes... mediaTypes) {
        head(HandlerUtil.BASE_PATH, endpoint, mediaTypes);
    }

    /**
     * Define a REST endpoint mapped to the specified HTTP method, uses "/" as url
     *
     * @param method     The HTTP method
     * @param url        The relative URL to be map this endpoint.
     * @param endpoint   The endpoint handler
     * @param mediaTypes (Optional) The accepted and returned types for this endpoint
     */
    public static synchronized void add(HttpString method, String url, HttpConsumer<HttpExchange> endpoint, MediaTypes... mediaTypes) {
        checkStarted();
        instance().endpoints.add(HandlerUtil.rest(method, url, endpoint, instance().exceptionMapper,  mediaTypes));
    }

    /**
     * Define a Websocket endpoint. Supports path variables
     *
     * @param url      The relative URL to be map this endpoint.
     * @param endpoint The endpoint handler
     */
    public static synchronized void websocket(String url, AbstractReceiveListener endpoint) {
        checkStarted();
        instance().endpoints.add(HandlerUtil.websocket(url, endpoint));
    }

    /**
     * A simplified Websocket endpoint. Supports path variables
     *
     * @param url       The relative URL to be map this endpoint.
     * @param onMessage The handler for when a new message is received
     */
    public static synchronized void websocket(String url, BiConsumer<WebSocketChannel, BufferedTextMessage> onMessage) {
        checkStarted();
        instance().endpoints.add(HandlerUtil.websocket(url, new WebsocketEndpoint() {
            @Override
            public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {

            }

            @Override
            protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) throws IOException {
                onMessage.accept(channel, message);
            }
        }));
    }

    /**
     * Define a Websocket endpoint. Supports path variables
     *
     * @param url      The relative URL to be map this endpoint.
     * @param endpoint The endpoint handler
     */
    public static synchronized void websocket(String url, WebsocketEndpoint endpoint) {
        checkStarted();
        instance().endpoints.add(HandlerUtil.websocket(url, endpoint));
    }

    /**
     * Define a Server sent events endpoint without a handler. Supports path variables
     * Data can be broadcast to this endpoint by using {@link io.joshworks.snappy.sse.SseBroadcaster}
     *
     * @param url The relative URL to be map this endpoint.
     */
    public static synchronized void sse(String url) {
        checkStarted();
        instance().endpoints.add(HandlerUtil.sse(url,  null));
    }

    /**
     * Define a Server sent events endpoint with a specified handler. Supports path variables
     * Data can be broadcast to this endpoint by using {@link io.joshworks.snappy.sse.SseBroadcaster}
     *
     * @param url                The relative URL to be map this endpoint.
     * @param connectionCallback Endpoint handler.
     */
    public static synchronized void sse(String url, ServerSentEventConnectionCallback connectionCallback) {
        checkStarted();
        instance().endpoints.add(HandlerUtil.sse(url,  connectionCallback));
    }

    /**
     * Serve static files from a given url. Path variables are not supported.
     *
     * @param url     The relative URL to be map this endpoint.
     * @param docPath The relative path to the classpath.
     */
    public static synchronized void staticFiles(String url, String docPath) {
        checkStarted();
        instance().endpoints.add(HandlerUtil.staticFiles(url, docPath));
    }

    /**
     * Serve static files from a given url from the default "/static" folder in the classpath. Path variables are not supported.
     *
     * @param url The relative URL to be map this endpoint.
     */
    public static synchronized void staticFiles(String url) {
        checkStarted();
        instance().endpoints.add(HandlerUtil.staticFiles(url));
    }

    /**
     * Define a endpoint that accepts a specialized {@link Exchange} to handle multipart requests. The data is parsed eagerly.
     *
     * @param url      The relative URL to be map this endpoint.
     * @param endpoint The endpoint handler
     */
    public static synchronized void multipart(String url, Consumer<MultipartExchange> endpoint) {
        checkStarted();
        instance().endpoints.add(HandlerUtil.multipart(url, endpoint));
    }

    /**
     * Define a endpoint that accepts a specialized {@link Exchange} to handle multipart requests. The data is parsed eagerly.
     *
     * @param url      The relative URL to be map this endpoint.
     * @param endpoint The endpoint handler
     * @param maxSize  The maximum request size for this endpoint.
     */
    public static synchronized void multipart(String url, Consumer<MultipartExchange> endpoint, long maxSize) {
        checkStarted();
        instance().endpoints.add(HandlerUtil.multipart(url, endpoint,  maxSize));
    }

    /**
     * Register a server startup listener that executes after all resources and extensions are loaded.
     *
     * @param task the {@link Runnable} to be executed.
     */
    public static synchronized void onStart(Runnable task) {
        checkStarted();
        instance().startListeners.add(task);
    }

    /**
     * Register a server shutdown listener that executes after all resources have been shut down.
     *
     * @param task the {@link Runnable} to be executed.
     */
    public static synchronized void onShutdown(Runnable task) {
        checkStarted();
        instance().shutdownListeners.add(task);
    }

    private static void checkStarted() {
        if (instance().started) {
            throw new IllegalStateException("Server already started");
        }
    }

    private void startServer() {
        try {
            checkStarted();
            Info.logo();
            Info.version();

            Runtime.getRuntime().addShutdownHook(new Thread(SnappyServer::stop));

            long start = System.currentTimeMillis();
            logger.info("Starting server...");

            AppProperties.load();
            overrideFromProps();

            ExecutorBootstrap.init(schedulers, executors);

            Parsers.register(new JsonParser());
            Parsers.register(new PlainTextParser());

            Undertow.Builder serverBuilder = Undertow.builder();

            worker = Xnio.getInstance().createWorker(optionBuilder.getMap());
            serverBuilder.setWorker(worker);

            //Extension are capable of adding / removing mapped endpoints,
            // therefore they must execute before the handler resolution
            bootstrapExtensions();

            Info.httpConfig(bindAddress, port, adminManager.getBindAddress(), adminManager.getPort(), httpTracer);
            Info.serverConfig(optionBuilder);
            Info.threadConfig(executors, schedulers);
            Info.endpoints("ENDPOINTS", endpoints, basePath);
            Info.endpoints("ADMIN ENDPOINTS", adminManager.getEndpoints(), BASE_PATH);


            HttpHandler rootHandler = HandlerManager.createRootHandler(
                    endpoints,
                    rootInterceptors,
                    interceptors,
                    exceptionMapper,
                    gzipEnabled,
                    basePath,
                    httpTracer);


            server = serverBuilder
                    .addHttpListener(port, bindAddress, rootHandler)
                    .addHttpListener(adminManager.getPort(), adminManager.getBindAddress(), adminManager.resolveHandlers())
                    .build();

            server.start();
            started = true;

            logger.info("Server started in {}ms", System.currentTimeMillis() - start);

            startListeners.forEach(Runnable::run);

        } catch (Exception e) {
            started = false;
            logger.error("Error while starting the server", e);
            stop();
            System.exit(1);
        }
    }

    private void overrideFromProps() {
        //http
        this.port = AppProperties.getInt(PropertyKey.HTTP_PORT).orElse(this.port);
        this.httpTracer = AppProperties.getBoolean(PropertyKey.HTTP_TRACER).orElse(this.httpTracer);
        this.bindAddress = AppProperties.get(PropertyKey.HTTP_BIND_ADDRESS).orElse(this.bindAddress);

        //admin http
        this.adminManager.setPort(AppProperties.getInt(PropertyKey.ADMIN_HTTP_PORT).orElse(this.adminManager.getPort()));
        this.adminManager.setBindAddress(AppProperties.get(PropertyKey.ADMIN_HTTP_BIND_ADDRESS).orElse(this.adminManager.getBindAddress()));

        //xnio
        OptionMap map = optionBuilder.getMap();
        Integer ioThreads = map.get(Options.WORKER_IO_THREADS);
        Integer maxWorkers = map.get(Options.WORKER_TASK_MAX_THREADS);
        Integer coreWorkers = map.get(Options.WORKER_TASK_CORE_THREADS);
        Boolean tcpNoDelay = map.get(Options.TCP_NODELAY);

        ioThreads = AppProperties.getInt(PropertyKey.XNIO_IO_THREADS).orElse(ioThreads);
        maxWorkers = AppProperties.getInt(PropertyKey.XNIO_MAX_WORKER_THREAD).orElse(maxWorkers);
        coreWorkers = AppProperties.getInt(PropertyKey.XNIO_CORE_WORKER_THREAD).orElse(coreWorkers);
        tcpNoDelay = AppProperties.getBoolean(PropertyKey.TCP_NO_DELAY).orElse(tcpNoDelay);

        optionBuilder.set(Options.WORKER_IO_THREADS, ioThreads);
        optionBuilder.set(Options.WORKER_TASK_MAX_THREADS, maxWorkers);
        optionBuilder.set(Options.WORKER_TASK_CORE_THREADS, coreWorkers);
        optionBuilder.set(Options.TCP_NODELAY, tcpNoDelay);

        exportDefaultProperties();
    }

    //sets the properties that are may be useful outside the application
    private void exportDefaultProperties() {
        AppProperties.set(PropertyKey.HTTP_PORT, String.valueOf(this.port));
        AppProperties.set(PropertyKey.ADMIN_HTTP_PORT, String.valueOf(this.adminManager.getPort()));
    }

    private void bootstrapExtensions() {
        extensions.onStart(
                new ServerData(port,
                        bindAddress,
                        httpTracer,
                        interceptors,
                        exceptionMapper,
                        basePath,
                        adminManager,
                        endpoints));
    }

    private void stopServer() {
        try {
            if (server != null && started) {
                logger.info("Stopping server...");

                shutdownExtensions();
                server.stop();
                shutdownWorkers();

                server.stop();

                INSTANCE = null;
                started = false;

                shutdownListeners.forEach(Runnable::run);

            }
        } catch (Exception e) {
            logger.error("Error while shutting down", e);
        }
    }

    private void shutdownExtensions() {
        try {
            extensions.onShutdown();
        } catch (Exception e) {
            logger.error("Error shutting down extensions", e);
        }
    }

    private void shutdownWorkers() {
        try {
            worker.shutdownNow();
        } catch (Exception e) {
            logger.error("Error shutting down workers", e);
        }
    }

}
