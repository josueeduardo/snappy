package io.joshworks.snappy.websocket;

import io.joshworks.snappy.handler.InterceptorHandler;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.WebSocketProtocolHandshakeHandler;
import io.undertow.websockets.core.AbstractReceiveListener;

/**
 * Created by Josh Gontijo on 3/19/17.
 */
public class WebsocketHandler implements HttpHandler {

    private WebSocketProtocolHandshakeHandler websocket;

    public WebsocketHandler(AbstractReceiveListener endpoint, InterceptorHandler interceptorHandler) {
        websocket = Handlers.websocket((exchange, channel) -> {
            channel.getReceiveSetter().set(endpoint);
            channel.resumeReceives();
        });
    }

    public WebsocketHandler(WebSocketConnectionCallback connectionCallback) {
        websocket = Handlers.websocket(connectionCallback);
    }

    public WebsocketHandler(WebsocketEndpoint websocketEndpoint) {
        websocket = Handlers.websocket((exchange, channel) -> {
            websocketEndpoint.onConnect(exchange, channel);

            channel.getReceiveSetter().set(websocketEndpoint);
            channel.resumeReceives();
        });
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        websocket.handleRequest(exchange);
    }
}
