package io.joshworks.microserver;

import io.joshworks.microserver.executor.AppExecutors;
import io.joshworks.microserver.handler.HandlerManager;
import io.joshworks.microserver.property.PropertyLoader;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.Xnio;
import org.xnio.XnioWorker;

import java.io.IOException;

/**
 * Created by josh on 3/5/17.
 */
public class Microserver {

    private static final Logger logger = LoggerFactory.getLogger(Microserver.class);

    private final Config config;

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





    public void start() {
        logger.info("Starting server...");

        PropertyLoader.load();


        HttpHandler rootHandler = HandlerManager.resolveHandlers(Endpoint.mappedEndpoints, config.httpMetrics, config.httpTracer);
        server = serverBuilder.addHttpListener(config.getPort(), config.getBindAddress()).setHandler(rootHandler).build();

        AppExecutors.init(config.executors, config.schedulers);
        Runtime.getRuntime().addShutdownHook(new Thread(AppExecutors::shutdownAll));

        displayInfo();
        server.start();

    }

    private void displayInfo() {
        Info.deploymentInfo(config);
    }

    public void stop() {
        if (server != null) {
            logger.info("Stopping server...");
            server.stop();
        }
    }




}
