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

package io.joshworks.snappy.extensions.ssr.client.connector;

import io.joshworks.snappy.extensions.ssr.Instance;
import io.joshworks.snappy.extensions.ssr.client.ServiceRegister;
import io.joshworks.snappy.extensions.ssr.client.ServiceStore;
import io.joshworks.snappy.extensions.ssr.client.WSRegistryClient;
import io.joshworks.stream.client.StreamClient;
import io.joshworks.stream.client.ws.WsConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;

import static io.joshworks.snappy.extensions.ssr.SSRKeys.SSR_LOGGER;

/**
 * Created by Josh Gontijo on 4/1/17.
 */
public class WSServiceRegister extends ServiceRegister {

    private static final Logger logger = LoggerFactory.getLogger(SSR_LOGGER);

    public static final String SSR_ENDPOINT = "/ssr";
    public static final String PROTOCOL = "ws://"; //TODO read from properties
    private WsConnection connection;

    public WSServiceRegister(ServiceStore store, Instance instance, String serverUrl, ScheduledExecutorService executorService) {
        super(store, instance, serverUrl, executorService);
    }

    @Override
    protected void connect() {
        String registryUrl = PROTOCOL + this.registryUrl + SSR_ENDPOINT + "/" + instance.getName();
        connection = StreamClient.connect(registryUrl, new WSRegistryClient(this, store, instance));
    }

    @Override
    protected void disconnect() {
        if (connection != null && connection.isOpen()) {
            logger.info("Closing WS connection");
            connection.close();
        }
    }


}
