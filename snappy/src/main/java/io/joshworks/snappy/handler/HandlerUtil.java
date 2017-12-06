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

package io.joshworks.snappy.handler;

import io.joshworks.snappy.Messages;
import io.joshworks.snappy.http.ExceptionMapper;
import io.joshworks.snappy.http.Group;
import io.joshworks.snappy.http.HttpConsumer;
import io.joshworks.snappy.http.HttpEntrypoint;
import io.joshworks.snappy.http.HttpExchange;
import io.joshworks.snappy.http.MediaType;
import io.joshworks.snappy.http.multipart.MultipartExchange;
import io.joshworks.snappy.parser.MediaTypes;
import io.joshworks.snappy.sse.BroadcasterSetup;
import io.joshworks.snappy.websocket.WebsocketEndpoint;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.EagerFormParsingHandler;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.server.handlers.form.MultiPartParserDefinition;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.sse.ServerSentEventConnectionCallback;
import io.undertow.server.handlers.sse.ServerSentEventHandler;
import io.undertow.util.HeaderValues;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;
import io.undertow.websockets.WebSocketProtocolHandshakeHandler;
import io.undertow.websockets.core.AbstractReceiveListener;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.joshworks.snappy.Messages.INVALID_URL;

/**
 * Created by josh on 3/11/17.
 */
public class HandlerUtil {

    public static final String BASE_PATH = "/";
    public static final String WILDCARD = "*";
    public static final String HEADER_VALUE_SEPARATOR = ",";
    public static final String STATIC_FILES_DEFAULT_LOCATION = "static";
    public static final String INDEX_HTML = "index.html";

    private static final Deque<String> groups = new ArrayDeque<>();

    public static MappedEndpoint rest(HttpString method,
                                      String url,
                                      HttpConsumer<HttpExchange> endpoint,
                                      ExceptionMapper exceptionMapper,
                                      MediaTypes... mimeTypes) {

        Objects.requireNonNull(method, Messages.INVALID_METHOD);
        Objects.requireNonNull(endpoint, Messages.INVALID_HANDLER);

        url = resolveUrl(url);

        HttpHandler handler = new HttpEntrypoint<HttpExchange>(endpoint, exceptionMapper) {
            @Override
            protected HttpExchange createExchange(HttpServerExchange exchange) {
                return new HttpExchange(exchange);
            }
        };
        return new MappedEndpoint(method.toString(), url, MappedEndpoint.Type.REST, handler, mimeTypes);
    }

    public static MappedEndpoint websocket(String url, AbstractReceiveListener endpoint) {
        Objects.requireNonNull(endpoint, Messages.INVALID_HANDLER);
        url = resolveUrl(url);
        WebSocketProtocolHandshakeHandler websocket = Handlers.websocket((exchange, channel) -> {
            channel.getReceiveSetter().set(endpoint);
            channel.resumeReceives();
        });

        return new MappedEndpoint(MappedEndpoint.Type.WS.name(), url, MappedEndpoint.Type.WS, websocket);
    }

    public static MappedEndpoint websocket(String url, WebsocketEndpoint websocketEndpoint) {
        Objects.requireNonNull(websocketEndpoint, Messages.INVALID_HANDLER);
        url = resolveUrl(url);

        WebSocketProtocolHandshakeHandler websocket = Handlers.websocket((exchange, channel) -> {
            websocketEndpoint.onConnect(exchange, channel);

            channel.getReceiveSetter().set(websocketEndpoint);
            channel.resumeReceives();
        });

        return new MappedEndpoint(MappedEndpoint.Type.WS.name(), url, MappedEndpoint.Type.WS, websocket);
    }

    public static MappedEndpoint sse(String url, ServerSentEventConnectionCallback connectionCallback) {
        url = resolveUrl(url);

        ServerSentEventHandler serverSentEventHandler = Handlers.serverSentEvents(connectionCallback);
        BroadcasterSetup.register(serverSentEventHandler);

        return new MappedEndpoint(Methods.GET_STRING, url, MappedEndpoint.Type.SSE, serverSentEventHandler);
    }

    public static MappedEndpoint staticFiles(String url, String docPath) {
        url = resolveUrl(url);
        url = url.isEmpty() ? BASE_PATH : url;
        docPath = docPath.startsWith(BASE_PATH) ? docPath.replaceFirst(BASE_PATH, "") : docPath;
        HttpHandler handler = Handlers.path()
                .addPrefixPath(url,
                        Handlers.resource(new ClassPathResourceManager(Thread.currentThread().getContextClassLoader(), docPath))
                                .addWelcomeFiles(INDEX_HTML));

        return new MappedEndpoint(Methods.GET_STRING, url, MappedEndpoint.Type.STATIC, handler);
    }

    public static MappedEndpoint staticFiles(String url) {
        return staticFiles(url, STATIC_FILES_DEFAULT_LOCATION);
    }


    public static MappedEndpoint multipart(HttpString method, String url, HttpConsumer<MultipartExchange> endpoint, ExceptionMapper exceptionMapper,long maxSize) {
        url = resolveUrl(url);

        validateHttpMethod(method);

        MultiPartParserDefinition multiPartParserDefinition = new MultiPartParserDefinition();
        multiPartParserDefinition.setMaxIndividualFileSize(maxSize);

        EagerFormParsingHandler formHandler = new EagerFormParsingHandler(FormParserFactory.builder().addParser(multiPartParserDefinition).build());
        formHandler.setNext(new HttpEntrypoint<MultipartExchange>(endpoint, exceptionMapper) {
            @Override
            protected MultipartExchange createExchange(HttpServerExchange exchange) {
                return new MultipartExchange(exchange);
            }
        });

        return new MappedEndpoint(
                method.toString(),
                url,
                MappedEndpoint.Type.MULTIPART,
                formHandler,
                new MediaTypes[]{MediaTypes.consumes(MediaType.APPLICATION_FORM_URLENCODED), MediaTypes.consumes(MediaType.MULTIPART_FORM_DATA)});
    }

    private static void validateHttpMethod(HttpString method) {
        if(!Methods.POST.equals(method) && !Methods.PUT.equals(method) && !Methods.DELETE.equals(method)) {
            throw new IllegalArgumentException("Only POST, PUT and DELETE are supported");
        }
    }


    public static synchronized void group(String groupPath, Group group) {
        groups.addLast(groupPath);
        group.addResources();
        groups.removeLast();
    }

    private static String resolveGroup(String url) {
        return groups.stream().map(HandlerUtil::parseUrl).collect(Collectors.joining("")) + url;
    }

    public static String parseUrl(String url) {
        Objects.requireNonNull(url, INVALID_URL);
        if (url.isEmpty()) {
            return url;
        }
        if (BASE_PATH.equals(url)) {
            return "";
        }
        url = url.startsWith(BASE_PATH) ? url : BASE_PATH + url;
        url = url.endsWith(BASE_PATH) ? url.substring(0, url.length() - 1) : url;
        return url;
    }

    private static String resolveUrl(String url) {
        url = parseUrl(url);
        url = resolveGroup(url);
        return url;
    }

    public static List<String> splitHeaderValues(HeaderValues headerValues) {
        List<String> splitted = new ArrayList<>();
        if (headerValues == null) {
            return splitted;
        }
        for (String vals : headerValues) {
            if (!vals.isEmpty()) {
                String[] split = vals.split(HEADER_VALUE_SEPARATOR);
                for (String val : split) {
                    val = val.trim();
                    if (!val.isEmpty()) {
                        splitted.add(val);
                    }
                }
            }
        }
        return splitted;
    }

    //best effort to resolve url that may be unique
    public static String[] removePathTemplate(List<MappedEndpoint> mappedEndpoints) {

        return mappedEndpoints.stream()
                .filter(me -> !me.type.equals(MappedEndpoint.Type.STATIC))
                .map(me -> {
                    int idx = me.url.indexOf("/{");
                    return idx >= 0 ? me.url.substring(0, idx) : me.url;
                })
                .distinct().toArray(String[]::new);
    }

    public static String exceptionMessageTemplate(HttpServerExchange exchange, long timestamp, String shortMessage) {
        HttpString requestMethod = exchange.getRequestMethod();
        String requestPath = exchange.getRequestPath();
        return String.format("[%d] %s %s - %s", timestamp, requestMethod, requestPath, shortMessage);
    }

}
