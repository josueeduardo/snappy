package io.joshworks.microserver;

import io.joshworks.microserver.rest.RestEndpoint;
import io.joshworks.microserver.rest.RestHandler;
import io.joshworks.microserver.websocket.WebsocketEndpoint;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.sse.ServerSentEventHandler;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.WebSocketProtocolHandshakeHandler;
import io.undertow.websockets.core.AbstractReceiveListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.joshworks.microserver.Messages.EMPTY_URL;
import static io.joshworks.microserver.Messages.INVALID_URL;

/**
 * Created by josh on 3/10/17.
 */
public class Endpoint {

    public static final String BASE_PATH = "/";
    static String basePath = BASE_PATH;

    static final List<MappedEndpoint> mappedEndpoints = new ArrayList<>();

    public static void basePath(String baseUrl) {
        basePath = resolveUrl(baseUrl);
    }

    public static void get(String url, RestEndpoint endpoint) {
        url = resolveUrl(url);
        String getString = Methods.GET_STRING;
        HttpHandler handler = new BlockingHandler(new RestHandler(endpoint));
        mappedEndpoints.add(new MappedEndpoint(getString, url, MappedEndpoint.Type.REST, handler));
    }

    public static void post(String url, RestEndpoint endpoint) {
        url = resolveUrl(url);
        String postString = Methods.POST_STRING;
        HttpHandler handler = new BlockingHandler(new RestHandler(endpoint));
        mappedEndpoints.add(new MappedEndpoint(postString, url, MappedEndpoint.Type.REST, handler));
    }

    public static void put(String url, RestEndpoint endpoint) {
        url = resolveUrl(url);
        String putString = Methods.PUT_STRING;
        HttpHandler handler = new BlockingHandler(new RestHandler(endpoint));
        mappedEndpoints.add(new MappedEndpoint(putString, url, MappedEndpoint.Type.REST, handler));
    }

    public static void delete(String url, RestEndpoint endpoint) {
        url = resolveUrl(url);
        String deleteString = Methods.DELETE_STRING;
        HttpHandler handler = new BlockingHandler(new RestHandler(endpoint));
        mappedEndpoints.add(new MappedEndpoint(deleteString, url, MappedEndpoint.Type.REST, handler));
    }

    public static void add(HttpString method, String url, RestEndpoint endpoint) {
        url = resolveUrl(url);
        HttpHandler handler = new BlockingHandler(new RestHandler(endpoint));
        mappedEndpoints.add(new MappedEndpoint(method.toString(), url, MappedEndpoint.Type.REST, handler));
    }

    public static void websocket(String url, AbstractReceiveListener endpoint) {
        url = resolveUrl(url);
        WebSocketProtocolHandshakeHandler websocket = Handlers.websocket((exchange, channel) -> {
            channel.getReceiveSetter().set(endpoint);
            channel.resumeReceives();
        });

        mappedEndpoints.add(new MappedEndpoint("WS", url, MappedEndpoint.Type.WS, websocket));
    }

    public static void websocket(String url, WebSocketConnectionCallback connectionCallback) {

        WebSocketProtocolHandshakeHandler websocket = Handlers.websocket(connectionCallback);
        mappedEndpoints.add(new MappedEndpoint("WS", url, MappedEndpoint.Type.WS, websocket));

    }

    public static void websocket(String url, WebsocketEndpoint websocketEndpoint) {
        url = resolveUrl(url);
        WebSocketProtocolHandshakeHandler websocket = Handlers.websocket((exchange, channel) -> {
            websocketEndpoint.onConnect(exchange, channel);

            channel.getReceiveSetter().set(websocketEndpoint);
            channel.resumeReceives();
        });

        mappedEndpoints.add(new MappedEndpoint("WS", url, MappedEndpoint.Type.WS, websocket));
    }

    public static void sse(String url) {
        url = resolveUrl(url);
        ServerSentEventHandler sseHandler = Handlers.serverSentEvents();
        mappedEndpoints.add(new MappedEndpoint(MappedEndpoint.Type.SSE.name(), url, MappedEndpoint.Type.SSE, sseHandler));
    }

    public static void staticFiles(String url, String docPath) {
        url = resolveUrl(url);
        docPath = docPath.startsWith("/") ? docPath.replaceFirst("/", "") : docPath;
        HttpHandler handler = Handlers.path()
                .addPrefixPath(url,
                        Handlers.resource(new ClassPathResourceManager(Thread.currentThread().getContextClassLoader(), docPath))
                                .addWelcomeFiles("static/index.html"));

        mappedEndpoints.add(new MappedEndpoint("STATIC", url, MappedEndpoint.Type.STATIC, handler));
    }


    public static void staticFiles(String url) {
        staticFiles(url, "static");
    }


    private static String resolveUrl(String url) {
        Objects.requireNonNull(url, INVALID_URL);
        if (url.isEmpty()) {
            Objects.requireNonNull(url, EMPTY_URL);
        }
        if (BASE_PATH.equals(url)) {
            return url;
        }
        url = url.startsWith(BASE_PATH) ? url : BASE_PATH + url;
        url = url.endsWith(BASE_PATH) ? url.substring(0, url.length() - 1) : url;
        return url;
    }

}
