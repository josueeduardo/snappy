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

package io.joshworks.snappy.extras.ssr.server.sse;

import io.joshworks.snappy.client.sse.EventData;
import io.joshworks.snappy.extras.ssr.Instance;
import io.joshworks.snappy.extras.ssr.server.service.ServiceControl;
import io.joshworks.snappy.parser.JsonParser;
import io.joshworks.snappy.parser.Parser;
import io.joshworks.snappy.sse.SseBroadcaster;
import io.undertow.server.handlers.sse.ServerSentEventConnection;
import io.undertow.server.handlers.sse.ServerSentEventConnectionCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.joshworks.snappy.extras.ssr.SSRKeys.SSR_LOGGER;

/**
 * Created by Josh Gontijo on 4/1/17.
 */
public class ServiceMonitor implements ServerSentEventConnectionCallback {

    private static final Logger logger = LoggerFactory.getLogger(SSR_LOGGER);

    private static final String INSTANCE_ID_PARAM = "instanceId";
    private static final Parser parser = new JsonParser();

    private ServiceControl control;

    public ServiceMonitor(ServiceControl control) {
        this.control = control;
    }

    @Override
    public void connected(ServerSentEventConnection connection, String lastEventId) {

        String instanceId = connection.getParameter(INSTANCE_ID_PARAM);
        if(instanceId != null) {
            connection.addCloseTask(this::onClose);
            Instance connected = control.updateInstanceState(instanceId, Instance.State.UP);
            logger.info("New service registered instance ID: {}", instanceId);
            broadcastInstanceEvent(connected);
        }
    }

    private void onClose(ServerSentEventConnection connection) {
        String instanceId = connection.getParameter(INSTANCE_ID_PARAM);
        logger.info("Service instance disconnected: {}", instanceId);

        Instance updated = control.updateInstanceState(instanceId, Instance.State.DOWN);
        broadcastInstanceEvent(updated);
    }

    private void broadcastInstanceEvent(Instance instance) {
        String data = parser.writeValue(instance);
        String id = String.valueOf(System.currentTimeMillis());
        SseBroadcaster.broadcast(new EventData(data, id, EventType.INSTANCE.name()));
    }

}
