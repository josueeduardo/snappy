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

package io.joshworks.snappy.extras.ssr.client.connector;

import io.joshworks.snappy.client.WsClient;
import io.joshworks.snappy.extras.ssr.Instance;
import io.joshworks.snappy.extras.ssr.client.ServiceRegister;
import io.joshworks.snappy.extras.ssr.client.ServiceStore;
import io.joshworks.snappy.extras.ssr.client.WSRegistryClient;
import io.undertow.websockets.core.WebSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;

import static io.joshworks.snappy.extras.ssr.SSRKeys.SSR_LOGGER;

/**
 * Created by Josh Gontijo on 4/1/17.
 */
public class WSServiceRegister extends ServiceRegister {

    private static final Logger logger = LoggerFactory.getLogger(SSR_LOGGER);

    public static final String SSR_ENDPOINT = "/ssr";
    public static final String PROTOCOL = "ws://"; //TODO read from properties
    private WebSocketChannel webSocketChannel;

    public WSServiceRegister(ServiceStore store, Instance instance, String serverUrl, ScheduledExecutorService executorService) {
        super(store, instance, serverUrl, executorService);
    }

    @Override
    protected void connect() {
        String registryUrl = PROTOCOL + this.registryUrl + SSR_ENDPOINT + "/" + instance.getName();
        webSocketChannel = WsClient.connect(registryUrl, new WSRegistryClient(this, store, instance));
    }

    @Override
    protected void disconnect() {
        try {
            if (webSocketChannel != null && webSocketChannel.isOpen()) {
                logger.info("Closing WS webSocketChannel");

                webSocketChannel.setCloseCode(1000); //normal closure
                webSocketChannel.setCloseReason("Service disconnected");
                webSocketChannel.sendClose();
            }
        } catch (IOException e) {
            logger.error("Error while closing the webSocketChannel", e);
        }
    }


}
