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

import io.joshworks.snappy.client.sse.EventData;
import io.joshworks.snappy.parser.Parser;
import io.joshworks.snappy.parser.Parsers;
import io.joshworks.snappy.rest.MediaType;
import io.undertow.server.handlers.sse.ServerSentEventConnection;
import io.undertow.server.handlers.sse.ServerSentEventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Created by josh on 3/9/17.
 */
public class SseBroadcaster {

    static final List<ServerSentEventHandler> endpoints = new ArrayList<>();

    private SseBroadcaster() {
    }

    public static void broadcast(String data) {
        all().forEach(sseConn -> sseConn.send(data));
    }

    public static void broadcast(Object data, MediaType mediaType) {
        Parser parser = Parsers.getParser(mediaType);
        broadcast(parser.writeValue(data));
    }

    public static void broadcast(String data, Predicate<ServerSentEventConnection> filter) {
        all().filter(filter)
                .forEach(conn -> conn.send(data));
    }

    public static void broadcast(Object data, MediaType mediaType, Predicate<ServerSentEventConnection> filter) {
        Parser parser = Parsers.getParser(mediaType);
        broadcast(parser.writeValue(data), filter);
    }

    public static void broadcast(EventData eventData) {
        all().forEach(sseConn -> sseConn.send(eventData.data, eventData.event, eventData.id, null));
    }

    public static void broadcast(EventData eventData, Predicate<ServerSentEventConnection> filter) {
        all().filter(filter).forEach(sseConn -> sseConn.send(eventData.data, eventData.event, eventData.id, null));
    }

    private static Stream<ServerSentEventConnection> all() {
        return endpoints.stream().flatMap(sse -> sse.getConnections().stream());
    }

    static void register(ServerSentEventHandler handler) {
        endpoints.add(handler);
    }

}
