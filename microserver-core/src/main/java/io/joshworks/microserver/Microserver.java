package io.joshworks.microserver;

import io.joshworks.microserver.executor.AppExecutors;
import io.joshworks.microserver.metric.Metric;
import io.joshworks.microserver.metric.RestMetricHandler;
import io.joshworks.microserver.property.PropertyLoader;
import io.joshworks.microserver.rest.RestEndpoint;
import io.joshworks.microserver.rest.RestHandler;
import io.joshworks.microserver.websocket.WebsocketEndpoint;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.predicate.Predicate;
import io.undertow.predicate.Predicates;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.MetricsHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.PathTemplateHandler;
import io.undertow.server.handlers.PredicateHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.WebSocketProtocolHandshakeHandler;
import io.undertow.websockets.core.AbstractReceiveListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.Xnio;
import org.xnio.XnioWorker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by josh on 3/5/17.
 */
public class Microserver {

    private static final Logger logger = LoggerFactory.getLogger(Microserver.class);
    //Handlers
    private final RoutingHandler routingRestHandler = Handlers.routing();
    private final List<RestMetricHandler> metricsHandlers = new ArrayList<>();
    private final PathTemplateHandler websocketHandler = Handlers.pathTemplate();
    private final List<MappedEndpoint> mappedEndpoints = new ArrayList<>();

    private final Config config;
    private PathHandler staticHandler;
    private Undertow.Builder serverBuilder;
    private Undertow server;


    public Microserver() {
        this(new Config());
    }

    public Microserver(Config config) {
        this.config = config;

        serverBuilder = Undertow.builder();

        try {
            XnioWorker worker = Xnio.getInstance().createWorker(config.optionBuilder.getMap());
            serverBuilder.setWorker(worker);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //best effort to resolve url that may be unique
    private String[] cleanedUrls() {
        return mappedEndpoints.stream()
                .filter(me -> !me.type.equals(MappedEndpoint.Type.STATIC))
                .map(me -> {
                    int idx = me.url.indexOf("/{");
                    return idx >= 0 ? me.url.substring(0, idx) : me.url;
                })
                .distinct().toArray(String[]::new);
    }


    private HttpHandler resolveHandlers() {

        PredicateHandler websocketRestResolved = Handlers.predicate(value -> {
            HeaderValues upgradeHeader = value.getRequestHeaders().get(Headers.UPGRADE);
            return upgradeHeader != null && upgradeHeader.stream().anyMatch(v -> v.equalsIgnoreCase("websocket"));
        }, websocketHandler, routingRestHandler);

        if (staticHandler != null) {
            String[] mappedServices = cleanedUrls();
            Predicate mappedPredicate = Predicates.prefixes(mappedServices);
            return Handlers.predicate(mappedPredicate, websocketRestResolved, staticHandler);
        }
        return websocketRestResolved;
    }

    private void configureMetrics() {
        get("/metrics", (exchange) -> exchange.send(new Metric(AppExecutors.executors(), AppExecutors.schedulers(), metricsHandlers), "application/json"));

        delete("/metrics", (exchange) -> {
            for (MetricsHandler mh : metricsHandlers) {
                mh.reset();
            }
        });
    }

    public void start() {
        logger.info("Starting server...");

        PropertyLoader.load();
        configureMetrics();

        HttpHandler rootHandler = resolveHandlers();
        server = serverBuilder.addHttpListener(config.getPort(), config.getBindAddress()).setHandler(rootHandler).build();


        AppExecutors.init(config.executors, config.schedulers);
        Runtime.getRuntime().addShutdownHook(new Thread(AppExecutors::shutdownAll));

        logConfig();
        server.start();

    }

    public void stop() {
        if (server != null) {
            logger.info("Stopping server...");
            server.stop();
        }
    }

    public Microserver get(String url, RestEndpoint endpoint) {
        String getString = Methods.GET_STRING;
        routingRestHandler.get(url, resolveRestEndpoints(url, getString, endpoint));
        mappedEndpoints.add(new MappedEndpoint(getString, url, MappedEndpoint.Type.REST));
        return this;
    }

    public Microserver post(String url, RestEndpoint endpoint) {
        String postString = Methods.POST_STRING;
        routingRestHandler.post(url, resolveRestEndpoints(url, postString, endpoint));
        mappedEndpoints.add(new MappedEndpoint(postString, url, MappedEndpoint.Type.REST));
        return this;
    }

    public Microserver put(String url, RestEndpoint endpoint) {
        String putString = Methods.PUT_STRING;
        routingRestHandler.put(url, resolveRestEndpoints(url, putString, endpoint));
        mappedEndpoints.add(new MappedEndpoint(putString, url, MappedEndpoint.Type.REST));
        return this;
    }

    public Microserver delete(String url, RestEndpoint endpoint) {
        String deleteString = Methods.DELETE_STRING;
        routingRestHandler.delete(url, resolveRestEndpoints(url, deleteString, endpoint));
        mappedEndpoints.add(new MappedEndpoint(deleteString, url, MappedEndpoint.Type.REST));
        return this;
    }

    public Microserver add(HttpString method, String url, RestEndpoint endpoint) {
        routingRestHandler.add(method, url, resolveRestEndpoints(url, method.toString(), endpoint));
        mappedEndpoints.add(new MappedEndpoint(method.toString(), url, MappedEndpoint.Type.REST));
        return this;
    }

    public Microserver websocket(String url, AbstractReceiveListener endpoint) {
        WebSocketProtocolHandshakeHandler websocket = Handlers.websocket((exchange, channel) -> {
            channel.getReceiveSetter().set(endpoint);
            channel.resumeReceives();
        });

        mappedEndpoints.add(new MappedEndpoint("WS", url, MappedEndpoint.Type.WS));
        websocketHandler.add(url, websocket);

        return this;
    }

    public Microserver websocket(String url, WebSocketConnectionCallback connectionCallback) {

        WebSocketProtocolHandshakeHandler websocket = Handlers.websocket(connectionCallback);

        mappedEndpoints.add(new MappedEndpoint("WS", url, MappedEndpoint.Type.WS));
        websocketHandler.add(url, websocket);

        return this;
    }

    public Microserver websocket(String url, WebsocketEndpoint websocketEndpoint) {

        WebSocketProtocolHandshakeHandler websocket = Handlers.websocket((exchange, channel) -> {
            websocketEndpoint.onConnect(exchange, channel);

            channel.getReceiveSetter().set(websocketEndpoint);
            channel.resumeReceives();
        });

        mappedEndpoints.add(new MappedEndpoint(MappedEndpoint.Type.WS.name(), url, MappedEndpoint.Type.WS));
        websocketHandler.add(url, websocket);

        return this;
    }

    public Microserver sse(String url) {
        routingRestHandler.get(url, Handlers.serverSentEvents());
        mappedEndpoints.add(new MappedEndpoint(MappedEndpoint.Type.SSE.name(), url, MappedEndpoint.Type.SSE));
        return this;
    }

    public Microserver staticFiles(String url, String docPath) {
        docPath = docPath.startsWith("/") ? docPath.replaceFirst("/", "") : docPath;
        staticHandler = Handlers.path()
                .addPrefixPath(url,
                        Handlers.resource(new ClassPathResourceManager(Thread.currentThread().getContextClassLoader(), docPath))
                                .addWelcomeFiles("static/index.html"));

        mappedEndpoints.add(new MappedEndpoint("STATIC", url, MappedEndpoint.Type.STATIC));

        return this;
    }

    public Microserver staticFiles(String url) {
        this.staticFiles(url, "static");
        return this;
    }


    private HttpHandler resolveRestEndpoints(String url, String method, RestEndpoint endpoint) {
        HttpHandler baseHandler = new BlockingHandler(new RestHandler(endpoint, config.interceptors));

        if (config.httpTracer) {
            baseHandler = Handlers.requestDump(baseHandler);
        }
        if (config.httpMetrics) {
            RestMetricHandler restMetricHandler = new RestMetricHandler(url, method, baseHandler);
            metricsHandlers.add(restMetricHandler);
            baseHandler = restMetricHandler;
        }

        return baseHandler;
    }

    private void logConfig() {
        logger.info("-------------------- HTTP CONFIG --------------------");
        logger.info("Bind address: {}", config.bindAddress);
        logger.info("Port: {}", config.port);
        logger.info("Http tracer : {}", config.httpTracer);
        logger.info("Http metrics: {}", config.httpMetrics);

        logger.info("---------------- SERVER CONFIG ---------------");
        config.optionBuilder.getMap().forEach(option -> {
            logger.info("{}: {}", option.getName(), config.optionBuilder.getMap().get(option));
        });

        logger.info("----------------- APP THREAD CONFIG -----------------");
        if (config.executors.isEmpty() && config.schedulers.isEmpty()) {
            logger.info("No executors configured");
        }
        config.executors.entrySet().forEach(entry -> logExecutors(entry.getKey(), entry.getValue()));
        config.schedulers.entrySet().forEach(entry -> logExecutors(entry.getKey(), entry.getValue()));

        logger.info("-------------------- ENDPOINTS --------------------");
        Collections.sort(mappedEndpoints, Comparator.comparing(me -> me.url));
        for (MappedEndpoint endpoint : mappedEndpoints) {
            String ws = "";
            for (int i = 0; i < 10 - endpoint.prefix.length(); i++) {
                ws += " ";
            }
            logger.info("{}{}", endpoint.prefix, ws + endpoint.url);
        }
    }

    private void logExecutors(String name, ThreadPoolExecutor executor) {
        logger.info("Pool name: {}", name);
        logger.info("   Core pool size: {}", executor.getCorePoolSize());
        logger.info("   Maximum pool size: {}", executor.getMaximumPoolSize());
        logger.info("   Queue size: {}", executor.getQueue().remainingCapacity());
        logger.info("   Rejection policy: {}", executor.getRejectedExecutionHandler().getClass().getSimpleName());
    }

}
