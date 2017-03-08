package com.josue.simpletow;

import com.josue.simpletow.metric.Metric;
import com.josue.simpletow.parser.JsonParser;
import com.josue.simpletow.parser.Parsers;
import com.josue.simpletow.parser.PlainTextParser;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;
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

    //extends PathTemplateHandler
    private final RoutingHandler routingHandler = Handlers.routing();
    private final Undertow server;

    private final List<MappedEndpoint> mappedEndpoints = new ArrayList<>();
    private final Config config;

    private static final Logger logger = LoggerFactory.getLogger(Microserver.class);


    public Microserver() {
        this(new Config());
    }

    public Microserver(Config config) {
        this.config = config;

        Parsers.register("application/json", new JsonParser());
        Parsers.register("text/plain", new PlainTextParser());
        Parsers.register("*/*", new JsonParser());


        Undertow.Builder builder = Undertow.builder();

        try {
            XnioWorker worker = Xnio.getInstance().createWorker(config.optionBuilder.getMap());
            builder.setWorker(worker);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        configureMetrics();

        server = builder.addHttpListener(config.getPort(), config.getBindAddress()).setHandler(routingHandler).build();
    }

    private void configureMetrics() {
        get("/metrics", (exchange) -> exchange.send(new Metric(AppExecutors.executors, AppExecutors.schedulers), "application/json"));
    }


    public void start() {
        logger.info("Starting server...");

        AppExecutors.init(config);
        Runtime.getRuntime().addShutdownHook(new Thread(AppExecutors::shutdownAll));

        logConfig();

        server.start();
    }

    public void stop() {
        logger.info("Stopping server...");
    }


    private void logConfig() {
        logger.info("-------------------- HTTP CONFIG --------------------");
        logger.info("Bind address: {}", config.bindAddress);
        logger.info("Port: {}", config.port);
        logger.info("Tracer enabled: {}", config.httpTracer);

        logger.info("---------------- SERVER THREAD CONFIG ---------------");
        config.optionBuilder.getMap().forEach(option -> {
            logger.info("{}: {}", option.getName(), config.optionBuilder.getMap().get(option));
        });

        logger.info("----------------- APP THREAD CONFIG -----------------");
        if(AppExecutors.executors.isEmpty() && AppExecutors.schedulers.isEmpty()) {
            logger.info("No executors configured");
        }
        AppExecutors.executors.entrySet().forEach(entry -> logExecutors(entry.getKey(), entry.getValue()));
        AppExecutors.schedulers.entrySet().forEach(entry -> logExecutors(entry.getKey(), entry.getValue()));

        logger.info("-------------------- REST CONFIG --------------------");
        for (MappedEndpoint endpoint : mappedEndpoints) {
            logger.info("{}  {}", endpoint.method, endpoint.url);
        }
    }

    private void logExecutors(String name, ThreadPoolExecutor executor) {
        logger.info("Pool name: {}", name);
        logger.info("   Core pool size: {}", executor.getCorePoolSize());
        logger.info("   Maximum pool size: {}", executor.getMaximumPoolSize());
        logger.info("   Queue size: {}", executor.getQueue().remainingCapacity());
        logger.info("   Rejection policy: {}", executor.getRejectedExecutionHandler().getClass().getSimpleName());
    }


    public Microserver get(String url, RestEndpoint endpoint) {
        routingHandler.get(url, buildHandlers(endpoint));
        mappedEndpoints.add(new MappedEndpoint(Methods.GET_STRING, url));
        return this;
    }

    public Microserver post(String url, RestEndpoint endpoint) {
        routingHandler.post(url, buildHandlers(endpoint));
        mappedEndpoints.add(new MappedEndpoint(Methods.POST_STRING, url));
        return this;
    }

    public Microserver put(String url, RestEndpoint endpoint) {
        routingHandler.put(url, buildHandlers(endpoint));
        mappedEndpoints.add(new MappedEndpoint(Methods.PUT_STRING, url));
        return this;
    }

    public Microserver delete(String url, RestEndpoint endpoint) {
        routingHandler.delete(url, buildHandlers(endpoint));
        mappedEndpoints.add(new MappedEndpoint(Methods.DELETE_STRING, url));
        return this;
    }

    public Microserver add(HttpString method, String url, RestEndpoint endpoint) {
        routingHandler.add(method, url, buildHandlers(endpoint));
        mappedEndpoints.add(new MappedEndpoint(method.toString(), url));
        return this;
    }

    private HttpHandler buildHandlers(RestEndpoint endpoint) {
        HttpHandler baseHandler = new BlockingHandler(new RestHandler(endpoint, config.interceptors));

        if (config.httpTracer) {
            baseHandler = Handlers.requestDump(baseHandler);
        }

        return baseHandler;
    }

}
