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

import io.joshworks.snappy.parser.Parser;
import io.joshworks.snappy.parser.Parsers;
import io.joshworks.snappy.rest.MediaType;
import io.undertow.server.handlers.sse.ServerSentEventConnection;
import io.undertow.server.handlers.sse.ServerSentEventHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Created by josh on 3/9/17.
 */
public class SseBroadcaster {

    static final List<ServerSentEventHandler> endpoints = new ArrayList<>();
    private static final Map<String, Set<ServerSentEventConnection>> groups = new ConcurrentHashMap<>();

    private SseBroadcaster() {
    }

    public static void broadcast(String data, String... groups) {
        all(groups).forEach(sseConn -> sseConn.send(data));
    }

    public static void broadcast(Object data, MediaType mediaType, String... groups) {
        Parser parser = Parsers.getParser(mediaType);
        broadcast(parser.writeValue(data), groups);
    }

    public static void broadcast(String data, Predicate<ServerSentEventConnection> filter, String... groups) {
        all(groups).filter(filter).forEach(conn -> conn.send(data));
    }

    public static void broadcast(Object data, MediaType mediaType, Predicate<ServerSentEventConnection> filter, String... groups) {
        Parser parser = Parsers.getParser(mediaType);
        broadcast(parser.writeValue(data), filter, groups);
    }

    public static void broadcast(EventData eventData, String... groups) {
        all(groups).forEach(sseConn -> sseConn.send(eventData.data, eventData.event, eventData.id, null));
    }

    public static void broadcast(EventData eventData, Predicate<ServerSentEventConnection> filter, String... groups) {
        all(groups).filter(filter).forEach(sseConn -> sseConn.send(eventData.data, eventData.event, eventData.id, null));
    }

    private static Stream<ServerSentEventConnection> all(String... groupFilter) {
        if (groupFilter.length > 0) {
            Set<String> groupVals = new HashSet<>(Arrays.asList(groupFilter));
            return groups.entrySet().stream()
                    .filter(es -> groupVals.contains(es.getKey()))
                    .flatMap(sse -> sse.getValue().stream());
        }
        return endpoints.stream().flatMap(sse -> sse.getConnections().stream());
    }

    public static void addToGroup(String name, ServerSentEventConnection connection) {
        groups.putIfAbsent(name, new HashSet<>());
        groups.get(name).add(connection);
        connection.addCloseTask(channel ->
                groups.computeIfPresent(name, (s, serverSentEventConnections) -> {
                    serverSentEventConnections.remove(connection);
                    return serverSentEventConnections.isEmpty() ? null : serverSentEventConnections;
                }));
    }

    static void register(ServerSentEventHandler handler) {
        endpoints.add(handler);
    }

}
