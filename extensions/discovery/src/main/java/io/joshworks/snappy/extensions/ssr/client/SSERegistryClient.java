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

import io.joshworks.snappy.client.sse.EventData;
import io.joshworks.snappy.client.sse.SseClientCallback;
import io.joshworks.snappy.extensions.ssr.Instance;
import io.joshworks.snappy.extensions.ssr.server.sse.EventType;
import io.joshworks.snappy.parser.JsonParser;
import io.joshworks.snappy.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static io.joshworks.snappy.extensions.ssr.SSRKeys.SSR_LOGGER;

/**
 * Created by Josh Gontijo on 4/1/17.
 */
public class SSERegistryClient extends SseClientCallback {

    private static final Logger logger = LoggerFactory.getLogger(SSR_LOGGER);
    private final Parser parser = new JsonParser();

    private final ServiceRegister register;
    private final ServiceStore store;

    public SSERegistryClient(ServiceRegister register, ServiceStore store) {
        this.register = register;
        this.store = store;
    }

    @Override
    public void onOpen() {
        try {
            store.newSession();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEvent(EventData data) {
        try {
            if (EventType.valueOf(data.event).equals(EventType.INSTANCE)) {
                logger.info("New Instance event: {}", data.data);
                Instance instanceEvent = parser.readValue(data.data, Instance.class);
                store.proccessInstance(instanceEvent);
            } else if (EventType.valueOf(data.event).equals(EventType.ACK)) {
                //ACK, do nothing
                logger.debug("ACK received from server, event ID: {}", data.id);
            } else {
                logger.warn("Unknown event type: {}", data.event);
            }

        } catch (Exception e) {
            logger.error("Error while receiving EventData", e);
        }
    }

    @Override
    public void onClose() {
        if (!register.shutdownRequested()) {
            logger.error("Connection with registry was closed, trying to reconnect");
            register.register(); //reconnect
        } else {
            logger.info("Client initiated shutdown process, not reconnecting");
        }
    }

    @Override
    public void onError(Exception error) {
        String message;
        if (error instanceof IOException) {
            message = "The server may have shutdown unexpectedly";
        } else {
            message = "Error handling event, error message";
        }
        logger.error(message, error);
    }

}
