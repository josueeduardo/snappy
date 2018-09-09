package io.joshworks.snappy.sse;

import io.undertow.server.handlers.sse.ServerSentEventConnection;
import io.undertow.server.handlers.sse.ServerSentEventConnectionCallback;

import java.util.function.BiConsumer;

public abstract class SseCallback implements ServerSentEventConnectionCallback, BiConsumer<ServerSentEventConnection, String> {

    public void onClose(ServerSentEventConnection connection) {
        //do nothing
    }

    @Override
    public void accept(ServerSentEventConnection serverSentEventConnection, String s) {
        connected(serverSentEventConnection, s);
    }
}
