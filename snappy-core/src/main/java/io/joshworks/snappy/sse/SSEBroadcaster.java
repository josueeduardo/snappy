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

package io.joshworks.snappy.sse;

import io.undertow.server.handlers.sse.ServerSentEventConnection;
import io.undertow.server.handlers.sse.ServerSentEventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Created by josh on 3/9/17.
 */
public class SSEBroadcaster {

    private static final List<ServerSentEventHandler> endpoints = new ArrayList<>();

    private SSEBroadcaster() {
    }

    public static void broadcast(String data) {
        endpoints.stream().flatMap(sse -> sse.getConnections().stream()).forEach(sseConn -> sseConn.send(data));
    }

    public static void broadcast(String data, Predicate<ServerSentEventConnection> filter) {
        endpoints.stream().flatMap(endpoint -> endpoint.getConnections().stream())
                .filter(filter)
                .forEach(conn -> conn.send(data));
    }

    static void register(ServerSentEventHandler handler) {
        endpoints.add(handler);
    }

}
