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

import io.joshworks.restclient.http.HttpResponse;
import io.joshworks.restclient.http.Unirest;
import io.joshworks.snappy.extensions.ssr.Instance;
import io.joshworks.snappy.extensions.ssr.SSRException;
import io.joshworks.snappy.extensions.ssr.client.SSERegistryClient;
import io.joshworks.snappy.extensions.ssr.client.ServiceRegister;
import io.joshworks.snappy.extensions.ssr.client.ServiceStore;
import io.joshworks.snappy.extensions.ssr.server.SSRServerExtension;
import io.joshworks.snappy.parser.JsonParser;
import io.joshworks.snappy.parser.Parser;
import io.joshworks.stream.client.StreamClient;
import io.joshworks.stream.client.sse.SSEConnection;
import io.undertow.util.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;

import static io.joshworks.snappy.extensions.ssr.SSRKeys.SSR_LOGGER;

/**
 * Created by Josh Gontijo on 4/1/17.
 */
public class SSEServiceRegister extends ServiceRegister {

    private static final Logger logger = LoggerFactory.getLogger(SSR_LOGGER);

    public static final String PROTOCOL = "http://"; //TODO read from properties
    private SSEConnection connect;

    private final Parser parser = new JsonParser();

    public SSEServiceRegister(ServiceStore store, Instance instance, String registryUrl, ScheduledExecutorService executorService) {
        super(store, instance, registryUrl, executorService);
    }

    @Override
    protected void connect() throws Exception {
        String instancesUrl = PROTOCOL + registryUrl + SSRServerExtension.INSTANCES_URL;

        logger.info("Trying to register service to " + registryUrl);
        HttpResponse<Instance> response = Unirest.post(instancesUrl)
                .header(Headers.CONTENT_TYPE_STRING, "application/json")
                .body(parser.writeValue(instance))
                .asObject(Instance.class);

        if (response.getStatus() != 201) {
            throw new SSRException(response.getStatus() + " - " + response.getStatusText(), response.getStatus());
        }
        Instance registered = response.getBody();

        String registryUrl = PROTOCOL + this.registryUrl + SSRServerExtension.MONITOR_URL + "/" + registered.getId();
        disconnect();
        connect = StreamClient.sse(registryUrl)
                .clientCallback(new SSERegistryClient(this, store))
                .connect();

    }

    @Override
    protected void disconnect() {
        if (connect != null) {
            connect.close();
        }
    }
}
