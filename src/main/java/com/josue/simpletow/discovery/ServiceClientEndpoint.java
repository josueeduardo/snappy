package com.josue.simpletow.discovery;

import com.josue.simpletow.discovery.common.Instance;
import com.josue.simpletow.discovery.config.Configurator;
import com.josue.simpletow.parser.JsonParser;
import com.josue.simpletow.parser.Parser;
import com.josue.simpletow.websocket.WebsocketEndpoint;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.StreamSourceFrameChannel;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by Josue on 16/06/2016.
 */
public class ServiceClientEndpoint extends WebsocketEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(ServiceClientEndpoint.class);

    private final ServiceRegister register;
    private final ServiceStore store;
    private final Parser parser = new JsonParser();

    public ServiceClientEndpoint(ServiceRegister register, ServiceStore store) {
        this.register = register;
        this.store = store;
    }

    @Override
    public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
        logger.info("SSR: Sending connection event");
        store.newSession(); //TODO WS is application scoped bean now
        Instance currentInstance = Configurator.getCurrentInstance();
        WebSockets.sendText(parser.write(currentInstance), channel, null);
    }

    @Override
    protected void onClose(WebSocketChannel webSocketChannel, StreamSourceFrameChannel channel) throws IOException {
        if (!ServiceRegister.shutdownSignal) {
            logger.error("SSR: Connection closed, reason: {} ::", webSocketChannel.getCloseReason());
            register.register(); //reconnect
        } else {
            logger.info("SSR: Client initiated shutdown process, not reconnecting", webSocketChannel.getCloseReason());
        }
        super.onClose(webSocketChannel, channel);
    }

    @Override
    protected void onError(WebSocketChannel channel, Throwable error) {
        String message;
        if (error instanceof IOException) {
            message = "SSR: The server may have shutdown unexpectedly, error message: {}";
        } else {
            message = "SSR: Error handling event, error message {}";
        }
        logger.error(message, error.getMessage());
        super.onError(channel, error);
    }

    @Override
    protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) throws IOException {
        Instance instance = parser.read(message.getData(), Instance.class);
        logger.info("SSR: New Event: {}", instance);

        if (instance == null || instance.getState() == null) {
            logger.warn("SSR: Invalid instance state");
            return;
        }

        if (Instance.State.UP.equals(instance.getState())) {
            store.onConnect(instance);
        }
        if (Instance.State.DOWN.equals(instance.getState())
                || Instance.State.OUT_OF_SERVICE.equals(instance.getState())) {
            store.onDisconnect(instance);
        }
    }
}

