package io.joshworks.microserver.handler;

import io.joshworks.microserver.rest.RestEndpoint;
import io.joshworks.microserver.rest.RestHandler;
import io.joshworks.microserver.websocket.WebsocketEndpoint;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.sse.ServerSentEventHandler;
import io.undertow.util.HttpString;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.WebSocketProtocolHandshakeHandler;
import io.undertow.websockets.core.AbstractReceiveListener;

import java.util.Objects;

import static io.joshworks.microserver.Messages.EMPTY_URL;
import static io.joshworks.microserver.Messages.INVALID_URL;

/**
 * Created by josh on 3/11/17.
 */
public class HandlerUtil {

    public static final String BASE_PATH = "/";
    public static final String STATIC_FILES_DEFAULT_LOCATION = "static";


    public static MappedEndpoint rest(HttpString method, String url, RestEndpoint endpoint) {
        url = resolveUrl(url);
        HttpHandler handler = new BlockingHandler(new RestHandler(endpoint));
        return new MappedEndpoint(method.toString(), url, MappedEndpoint.Type.REST, handler);
    }

    public static MappedEndpoint websocket(String url, AbstractReceiveListener endpoint) {
        url = resolveUrl(url);
        WebSocketProtocolHandshakeHandler websocket = Handlers.websocket((exchange, channel) -> {
            channel.getReceiveSetter().set(endpoint);
            channel.resumeReceives();
        });

        return new MappedEndpoint("WS", url, MappedEndpoint.Type.WS, websocket);
    }

    public static MappedEndpoint websocket(String url, WebSocketConnectionCallback connectionCallback) {

        WebSocketProtocolHandshakeHandler websocket = Handlers.websocket(connectionCallback);
        return new MappedEndpoint("WS", url, MappedEndpoint.Type.WS, websocket);

    }

    public static MappedEndpoint websocket(String url, WebsocketEndpoint websocketEndpoint) {
        url = resolveUrl(url);
        WebSocketProtocolHandshakeHandler websocket = Handlers.websocket((exchange, channel) -> {
            websocketEndpoint.onConnect(exchange, channel);

            channel.getReceiveSetter().set(websocketEndpoint);
            channel.resumeReceives();
        });

        return new MappedEndpoint("WS", url, MappedEndpoint.Type.WS, websocket);
    }

    public static MappedEndpoint sse(String url) {
        url = resolveUrl(url);
        ServerSentEventHandler sseHandler = Handlers.serverSentEvents();
        return new MappedEndpoint(MappedEndpoint.Type.SSE.name(), url, MappedEndpoint.Type.SSE, sseHandler);
    }

    public static MappedEndpoint staticFiles(String url, String docPath) {
        url = resolveUrl(url);
        docPath = docPath.startsWith(BASE_PATH) ? docPath.replaceFirst(BASE_PATH, "") : docPath;
        HttpHandler handler = Handlers.path()
                .addPrefixPath(url,
                        Handlers.resource(new ClassPathResourceManager(Thread.currentThread().getContextClassLoader(), docPath))
                                .addWelcomeFiles("static/index.html"));

        return new MappedEndpoint("STATIC", url, MappedEndpoint.Type.STATIC, handler);
    }

    public static MappedEndpoint staticFiles(String url) {
        return staticFiles(url, STATIC_FILES_DEFAULT_LOCATION);
    }

    public static String resolveUrl(String url) {
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
