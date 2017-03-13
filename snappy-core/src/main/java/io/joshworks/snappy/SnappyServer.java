package io.joshworks.snappy;

import com.mashape.unirest.http.Unirest;
import io.joshworks.snappy.executor.AppExecutors;
import io.joshworks.snappy.handler.HandlerManager;
import io.joshworks.snappy.handler.HandlerUtil;
import io.joshworks.snappy.handler.MappedEndpoint;
import io.joshworks.snappy.parser.MediaTypes;
import io.joshworks.snappy.property.PropertyLoader;
import io.joshworks.snappy.rest.RestEndpoint;
import io.joshworks.snappy.websocket.WebsocketEndpoint;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
            try {
                Unirest.shutdown();
            } catch (IOException e) {
                logger.error("Error closing client connections", e);
            }
        }
    }


    private final List<MappedEndpoint> endpoints = new ArrayList<>();
    private String basePath = HandlerUtil.BASE_PATH;


    public SnappyServer basePath(String basePath) {
        this.basePath = basePath;
        return this;
    }

    public SnappyServer accepts(String mime) {
        return this;
    }

    public SnappyServer get(String url, RestEndpoint endpoint, MediaTypes... mimeTypes) {
        endpoints.add(HandlerUtil.rest(Methods.GET, url, endpoint, mimeTypes));
        return this;
    }

    public SnappyServer post(String url, RestEndpoint endpoint, MediaTypes... mimeTypes) {
        endpoints.add(HandlerUtil.rest(Methods.POST, url, endpoint, mimeTypes));
        return this;
    }

    public SnappyServer put(String url, RestEndpoint endpoint, MediaTypes... mimeTypes) {
        endpoints.add(HandlerUtil.rest(Methods.PUT, url, endpoint, mimeTypes));
        return this;
    }

    public SnappyServer delete(String url, RestEndpoint endpoint, MediaTypes... mimeTypes) {
        endpoints.add(HandlerUtil.rest(Methods.DELETE, url, endpoint, mimeTypes));
        return this;
    }

    public SnappyServer options(String url, RestEndpoint endpoint, MediaTypes... mimeTypes) {
        endpoints.add(HandlerUtil.rest(Methods.OPTIONS, url, endpoint, mimeTypes));
        return this;
    }

    public SnappyServer head(String url, RestEndpoint endpoint, MediaTypes... mimeTypes) {
        endpoints.add(HandlerUtil.rest(Methods.HEAD, url, endpoint, mimeTypes));
        return this;
    }

    public SnappyServer add(HttpString method, String url, RestEndpoint endpoint, MediaTypes... mimeTypes) {
        endpoints.add(HandlerUtil.rest(method, url, endpoint, mimeTypes));
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

    public List<MappedEndpoint> getEndpoints() {
        return Collections.unmodifiableList(endpoints);
    }

    public String getBasePath() {
        return basePath;
    }

    private static class Shutdown implements Runnable {

        @Override
        public void run() {
//            Clients.shutdown();
            AppExecutors.shutdownAll();
        }
    }
}
