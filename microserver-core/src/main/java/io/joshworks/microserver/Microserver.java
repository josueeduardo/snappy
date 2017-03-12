package io.joshworks.microserver;

import io.joshworks.microserver.executor.AppExecutors;
import io.joshworks.microserver.handler.HandlerManager;
import io.joshworks.microserver.handler.HandlerUtil;
import io.joshworks.microserver.handler.MappedEndpoint;
import io.joshworks.microserver.property.PropertyLoader;
import io.joshworks.microserver.rest.RestEndpoint;
import io.joshworks.microserver.websocket.WebsocketEndpoint;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.AbstractReceiveListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.Xnio;
import org.xnio.XnioWorker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Created by josh on 3/5/17.
 */
public class Microserver {

    private static final Logger logger = LoggerFactory.getLogger(Microserver.class);

    private final HandlerManager handlerManager = new HandlerManager();

    private final Config config;
    private Undertow server;


    public Microserver() {
        this(new Config());
    }

    public Microserver(Config config) {
        this.config = config;
    }


    public void start() {
        try {
            Info.logo();
            PropertyLoader.load();
            Info.deploymentInfo(config, endpoints, basePath);
            AppExecutors.init(config.executors, config.schedulers);

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


    private final List<MappedEndpoint> endpoints = new ArrayList<>();
    private String basePath = HandlerUtil.BASE_PATH;


    public Microserver basePath(String basePath) {
        Objects.requireNonNull(basePath, Messages.INVALID_URL);
        this.basePath = basePath;
        return this;
    }

    public Microserver get(String url, RestEndpoint endpoint) {
        endpoints.add(HandlerUtil.rest(Methods.GET, url, endpoint));
        return this;
    }

    public Microserver post(String url, RestEndpoint endpoint) {
        endpoints.add(HandlerUtil.rest(Methods.POST, url, endpoint));
        return this;
    }

    public Microserver put(String url, RestEndpoint endpoint) {
        endpoints.add(HandlerUtil.rest(Methods.PUT, url, endpoint));
        return this;
    }

    public Microserver delete(String url, RestEndpoint endpoint) {
        endpoints.add(HandlerUtil.rest(Methods.DELETE, url, endpoint));
        return this;
    }

    public Microserver add(HttpString method, String url, RestEndpoint endpoint) {
        endpoints.add(HandlerUtil.rest(method, url, endpoint));
        return this;
    }

    public Microserver websocket(String url, AbstractReceiveListener endpoint) {
        endpoints.add(HandlerUtil.websocket(url, endpoint));
        return this;
    }

    public Microserver websocket(String url, WebSocketConnectionCallback connectionCallback) {
        endpoints.add(HandlerUtil.websocket(url, connectionCallback));
        return this;
    }

    public Microserver websocket(String url, WebsocketEndpoint websocketEndpoint) {
        endpoints.add(HandlerUtil.websocket(url, websocketEndpoint));
        return this;
    }

    public Microserver sse(String url) {
        endpoints.add(HandlerUtil.sse(url));
        return this;
    }

    public Microserver staticFiles(String url, String docPath) {
        endpoints.add(HandlerUtil.staticFiles(url, docPath));
        return this;
    }

    public Microserver staticFiles(String url) {
        endpoints.add(HandlerUtil.staticFiles(url));
        return this;
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
            AppExecutors.shutdownAll();
        }
    }
}
