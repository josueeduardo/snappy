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
    }


    public void start() {
        try {
            displayInfo();
            logger.info("Starting server...");

            serverBuilder = Undertow.builder();

            XnioWorker worker = Xnio.getInstance().createWorker(config.optionBuilder.getMap());
            serverBuilder.setWorker(worker);

            PropertyLoader.load();

            HttpHandler rootHandler = HandlerManager.resolveHandlers(Endpoint.mappedEndpoints, config.httpMetrics, config.httpTracer);
            server = serverBuilder.addHttpListener(config.getPort(), config.getBindAddress()).setHandler(rootHandler).build();

            AppExecutors.init(config.executors, config.schedulers);
            Runtime.getRuntime().addShutdownHook(new Thread(new Shutdown()));

            server.start();

        } catch (Exception e) {
            logger.error("Error while starting the server", e);
            throw new RuntimeException(e);
        }
    }

    private void displayInfo() {
        Info.logo();
        Info.deploymentInfo(config);
    }

    public void stop() {
        if (server != null) {
            logger.info("Stopping server...");
            server.stop();
        }
    }

    private static class Shutdown implements Runnable {

        @Override
        public void run() {
            AppExecutors.shutdownAll();
        }
    }
}
