package io.joshworks.streams.client.ws;

import io.undertow.server.DefaultByteBufferPool;
import io.undertow.websockets.client.WebSocketClient;
import io.undertow.websockets.core.BufferedBinaryMessage;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.CloseMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import org.xnio.XnioWorker;

import java.net.URI;
import java.util.function.BiConsumer;

/**
 * Created by Josh Gontijo on 6/8/17.
 */
public class WsConfiguration {
    private final String url;
    private final XnioWorker worker;

    BiConsumer<WebSocketChannel, WebSocketHttpExchange> onConnect = (exchange, channel) -> {};
    BiConsumer<WebSocketChannel, BufferedBinaryMessage> onPing = (wsChannel, channel) -> {};
    BiConsumer<WebSocketChannel, BufferedBinaryMessage> onPong = (wsChannel, channel) -> {};
    BiConsumer<WebSocketChannel, BufferedTextMessage> onText = (wsChannel, channel) -> {};
    BiConsumer<WebSocketChannel, BufferedBinaryMessage> onBinary = (wsChannel, channel) -> {};
    BiConsumer<WebSocketChannel, Exception> onError = (wsChannel, error) -> {};
    BiConsumer<WebSocketChannel, CloseMessage> onClose = (wsChannel, message) -> {};

    public WsConfiguration(String url, XnioWorker worker, WebsocketClientEndpoint endpoint) {
        this(url, worker);
        this.onConnect = endpoint::onConnect;
        this.onPing = endpoint::onPing;
        this.onPong = endpoint::onPong;
        this.onText = endpoint::onText;
        this.onBinary = endpoint::onBinary;
        this.onError = endpoint::onError;
        this.onClose = endpoint::onClose;
    }

    public WsConfiguration(String url, XnioWorker worker) {
        this.url = url;
        this.worker = worker;
    }

    public WsConfiguration onConnect(BiConsumer<WebSocketChannel, WebSocketHttpExchange> onConnect) {
        this.onConnect = onConnect;
        return this;
    }

    public WsConfiguration onClose(BiConsumer<WebSocketChannel, CloseMessage> onClose) {
        this.onClose = onClose;
        return this;
    }

    public WsConfiguration onPing(BiConsumer<WebSocketChannel, BufferedBinaryMessage> onPing) {
        this.onPing = onPing;
        return this;
    }

    public WsConfiguration onPong(BiConsumer<WebSocketChannel, BufferedBinaryMessage> onPong) {
        this.onPong = onPong;
        return this;
    }

    public WsConfiguration onText(BiConsumer<WebSocketChannel, BufferedTextMessage> onText) {
        this.onText = onText;
        return this;
    }

    public WsConfiguration onBinary(BiConsumer<WebSocketChannel, BufferedBinaryMessage> onBinary) {
        this.onBinary = onBinary;
        return this;
    }

    public WsConfiguration onError(BiConsumer<WebSocketChannel, Exception> onError) {
        this.onError = onError;
        return this;
    }

    public WebSocketChannel connect() {
        try {
            WebSocketChannel webSocketChannel = new WebSocketClient.ConnectionBuilder(
                    worker,
                    new DefaultByteBufferPool(false, 2048),
                    URI.create(url))
                    .connect().get();


            webSocketChannel.getReceiveSetter().set(new WSClientEndpoint(this));
            webSocketChannel.resumeReceives();
            return webSocketChannel;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
