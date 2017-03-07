package com.josue.simpletow;

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

        server = builder.addHttpListener(config.getPort(), config.getBindAddress()).setHandler(routingHandler).build();
    }


    public void start() {
        logger.info("Starting server...");

        AppExecutors.init(config.threadPoolExecutor);
        logConfig();

        server.start();
    }

    public void stop() {
        logger.info("Stopping server...");
        AppExecutors.shutdownAll();
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
        logger.info("Core pool size: {}", config.threadPoolExecutor.getCorePoolSize());
        logger.info("Maximum pool size: {}", config.threadPoolExecutor.getMaximumPoolSize());
        logger.info("Queue size: {}", config.threadPoolExecutor.getQueue().remainingCapacity());
        logger.info("Rejection interceptors: {}", config.threadPoolExecutor.getRejectedExecutionHandler().getClass().getSimpleName());

        logger.info("-------------------- REST CONFIG --------------------");
        for (MappedEndpoint endpoint : mappedEndpoints) {
            logger.info("{}  {}", endpoint.method, endpoint.url);
        }
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
