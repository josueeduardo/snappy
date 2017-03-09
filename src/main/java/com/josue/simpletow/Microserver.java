package com.josue.simpletow;

import com.josue.simpletow.metric.Metric;
import com.josue.simpletow.parser.JsonParser;
import com.josue.simpletow.parser.Parsers;
import com.josue.simpletow.parser.PlainTextParser;
import com.josue.simpletow.websocket.WebsocketEndpoint;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.predicate.Predicate;
import io.undertow.predicate.Predicates;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.BlockingHandler;
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
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by josh on 3/5/17.
 */
public class Microserver {

    private static final Logger logger = LoggerFactory.getLogger(Microserver.class);
    //extends PathTemplateHandler
    private final RoutingHandler routingRestHandler = Handlers.routing();
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

        Parsers.register("application/json", new JsonParser());
        Parsers.register("text/plain", new PlainTextParser());
        Parsers.register("*/*", new JsonParser());


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
        get("/metrics", (exchange) -> exchange.send(new Metric(AppExecutors.executors, AppExecutors.schedulers), "application/json"));
    }

    public void start() {
        logger.info("Starting server...");

        HttpHandler rootHandler = resolveHandlers();
        server = serverBuilder.addHttpListener(config.getPort(), config.getBindAddress()).setHandler(rootHandler).build();


        configureMetrics();

        AppExecutors.init(config);
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
        routingRestHandler.get(url, resolveRestEndpoints(endpoint));
        mappedEndpoints.add(new MappedEndpoint(Methods.GET_STRING, url, MappedEndpoint.Type.REST));
        return this;
    }

    public Microserver post(String url, RestEndpoint endpoint) {
        routingRestHandler.post(url, resolveRestEndpoints(endpoint));
        mappedEndpoints.add(new MappedEndpoint(Methods.POST_STRING, url, MappedEndpoint.Type.REST));
        return this;
    }

    public Microserver put(String url, RestEndpoint endpoint) {
        routingRestHandler.put(url, resolveRestEndpoints(endpoint));
        mappedEndpoints.add(new MappedEndpoint(Methods.PUT_STRING, url, MappedEndpoint.Type.REST));
        return this;
    }

    public Microserver delete(String url, RestEndpoint endpoint) {
        routingRestHandler.delete(url, resolveRestEndpoints(endpoint));
        mappedEndpoints.add(new MappedEndpoint(Methods.DELETE_STRING, url, MappedEndpoint.Type.REST));
        return this;
    }

    public Microserver add(HttpString method, String url, RestEndpoint endpoint) {
        routingRestHandler.add(method, url, resolveRestEndpoints(endpoint));
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


    private HttpHandler resolveRestEndpoints(RestEndpoint endpoint) {
        HttpHandler baseHandler = new BlockingHandler(new RestHandler(endpoint, config.interceptors));

        if (config.httpTracer) {
            baseHandler = Handlers.requestDump(baseHandler);
        }

        return baseHandler;
    }


    private void logConfig() {
        logger.info("-------------------- HTTP CONFIG --------------------");
        logger.info("Bind address: {}", config.bindAddress);
        logger.info("Port: {}", config.port);
        logger.info("Tracer enabled: {}", config.httpTracer);

        logger.info("---------------- SERVER CONFIG ---------------");
        config.optionBuilder.getMap().forEach(option -> {
            logger.info("{}: {}", option.getName(), config.optionBuilder.getMap().get(option));
        });

        logger.info("----------------- APP THREAD CONFIG -----------------");
        if (AppExecutors.executors.isEmpty() && AppExecutors.schedulers.isEmpty()) {
            logger.info("No executors configured");
        }
        AppExecutors.executors.entrySet().forEach(entry -> logExecutors(entry.getKey(), entry.getValue()));
        AppExecutors.schedulers.entrySet().forEach(entry -> logExecutors(entry.getKey(), entry.getValue()));

        logger.info("-------------------- REST CONFIG --------------------");
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
