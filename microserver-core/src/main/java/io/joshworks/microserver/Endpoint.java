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

/**
 * Created by josh on 3/10/17.
 */
public class Endpoint {

    static final List<MappedEndpoint> mappedEndpoints = new ArrayList<>();

    public static void get(String url, RestEndpoint endpoint) {
        String getString = Methods.GET_STRING;
        HttpHandler handler = new BlockingHandler(new RestHandler(endpoint));
        mappedEndpoints.add(new MappedEndpoint(getString, url, MappedEndpoint.Type.REST, handler));
    }

    public static void post(String url, RestEndpoint endpoint) {
        String postString = Methods.POST_STRING;
        HttpHandler handler = new BlockingHandler(new RestHandler(endpoint));
        mappedEndpoints.add(new MappedEndpoint(postString, url, MappedEndpoint.Type.REST, handler));
    }

    public static void put(String url, RestEndpoint endpoint) {
        String putString = Methods.PUT_STRING;
        HttpHandler handler = new BlockingHandler(new RestHandler(endpoint));
        mappedEndpoints.add(new MappedEndpoint(putString, url, MappedEndpoint.Type.REST, handler));
    }

    public static void delete(String url, RestEndpoint endpoint) {
        String deleteString = Methods.DELETE_STRING;
        HttpHandler handler = new BlockingHandler(new RestHandler(endpoint));
        mappedEndpoints.add(new MappedEndpoint(deleteString, url, MappedEndpoint.Type.REST, handler));
    }

    public static void add(HttpString method, String url, RestEndpoint endpoint) {
        HttpHandler handler = new BlockingHandler(new RestHandler(endpoint));
        mappedEndpoints.add(new MappedEndpoint(method.toString(), url, MappedEndpoint.Type.REST, handler));
    }

    public static void websocket(String url, AbstractReceiveListener endpoint) {
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

        WebSocketProtocolHandshakeHandler websocket = Handlers.websocket((exchange, channel) -> {
            websocketEndpoint.onConnect(exchange, channel);

            channel.getReceiveSetter().set(websocketEndpoint);
            channel.resumeReceives();
        });

        mappedEndpoints.add(new MappedEndpoint("WS", url, MappedEndpoint.Type.WS, websocket));
    }

    public static void sse(String url) {
        ServerSentEventHandler sseHandler = Handlers.serverSentEvents();
        mappedEndpoints.add(new MappedEndpoint(MappedEndpoint.Type.SSE.name(), url, MappedEndpoint.Type.SSE, sseHandler));
    }

    public static void staticFiles(String url, String docPath) {
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

}
