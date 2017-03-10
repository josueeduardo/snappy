package io.joshworks.microserver.sse;

import io.undertow.server.handlers.sse.ServerSentEventConnection;
import io.undertow.server.handlers.sse.ServerSentEventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Created by josh on 3/9/17.
 */
public class SSEBroadcaster {

    private static final List<ServerSentEventHandler> endpoints = new ArrayList<>();

    private SSEBroadcaster() {
    }

    public static void broadcast(String data) {
        endpoints.stream().flatMap(sse -> sse.getConnections().stream()).forEach(sseConn -> sseConn.send(data));
    }

    public static void broadcast(String data, Predicate<ServerSentEventConnection> filter) {
        endpoints.stream().flatMap(endpoint -> endpoint.getConnections().stream())
                .filter(filter)
                .forEach(conn -> conn.send(data));
    }

    static void register(ServerSentEventHandler handler) {
        endpoints.add(handler);
    }

}
