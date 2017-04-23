/*
 * Copyright 2017 Josue Gontijo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.joshworks.snappy.extensions.ssr.client;

import io.joshworks.snappy.extensions.ssr.Instance;
import io.joshworks.snappy.parser.JsonParser;
import io.joshworks.snappy.parser.Parser;
import io.joshworks.snappy.websocket.WebsocketEndpoint;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.StreamSourceFrameChannel;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static io.joshworks.snappy.SnappyServer.*;

/**
 * Created by Josue on 16/06/2016.
 */
public class WSRegistryClient extends WebsocketEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(LOGGER_NAME);

    private final ServiceRegister register;
    private final ServiceStore store;
    private final Instance currentInstance;
    private final Parser parser = new JsonParser();

    public WSRegistryClient(ServiceRegister register, ServiceStore store, Instance currentInstance) {
        this.register = register;
        this.store = store;
        this.currentInstance = currentInstance;
    }

    @Override
    public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
        try {
            logger.info("Sending connection event");
            store.newSession(); //TODO WS is application scoped bean now
            WebSockets.sendText(parser.writeValue(currentInstance), channel, null);
        } catch (Exception e) {
            logger.error("Error sending Instance information on connect", e);
        }
    }

    @Override
    protected void onClose(WebSocketChannel webSocketChannel, StreamSourceFrameChannel channel) throws IOException {
        if (!register.shutdownRequested()) {
            logger.error("Connection closed, reason: {}", webSocketChannel.getCloseReason());
            register.register(); //reconnect
        } else {
            logger.info("Client initiated shutdown process, not reconnecting", webSocketChannel.getCloseReason());
        }
        super.onClose(webSocketChannel, channel);
    }

    @Override
    protected void onError(WebSocketChannel channel, Throwable error) {
        String message;
        if (error instanceof IOException) {
            message = "The server may have shutdown unexpectedly, error message: {}";
        } else {
            message = "Error handling event, error message {}";
        }
        logger.error(message, error.getMessage());
        super.onError(channel, error);
    }

    @Override
    protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) throws IOException {
        try {
            Instance instance = parser.readValue(message.getData(), Instance.class);
            store.proccessInstance(instance);

        } catch (Exception e) {
            logger.error("Error receiving Instance event", e);
        }
    }
}

