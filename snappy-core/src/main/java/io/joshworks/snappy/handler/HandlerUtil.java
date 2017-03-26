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
import io.joshworks.snappy.multipart.MultipartEntrypointHandler;
import io.joshworks.snappy.multipart.MultipartExchange;
import io.joshworks.snappy.parser.MediaTypes;
import io.joshworks.snappy.rest.ExceptionMapper;
import io.joshworks.snappy.rest.Interceptor;
import io.joshworks.snappy.rest.RestExchange;
import io.joshworks.snappy.websocket.WebsocketEndpoint;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.form.EagerFormParsingHandler;
import io.undertow.server.handlers.form.MultiPartParserDefinition;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.util.HeaderValues;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.WebSocketProtocolHandshakeHandler;
import io.undertow.websockets.core.AbstractReceiveListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static io.joshworks.snappy.Messages.EMPTY_URL;
import static io.joshworks.snappy.Messages.INVALID_URL;

/**
 * Created by josh on 3/11/17.
 */
public class HandlerUtil {

    public static final String BASE_PATH = "/";
    public static final String WILDCARD = "*";
    public static final String HEADER_VALUE_SEPARATOR = ",";
    public static final String STATIC_FILES_DEFAULT_LOCATION = "static";

    public static MappedEndpoint rest(HttpString method,
                                      String url,
                                      Consumer<RestExchange> endpoint,
                                      ExceptionMapper exceptionMapper,
                                      List<Interceptor> interceptors,
                                      MediaTypes... mimeTypes) {

        Objects.requireNonNull(method, Messages.INVALID_METHOD);
        Objects.requireNonNull(url, Messages.INVALID_URL);
        Objects.requireNonNull(endpoint, Messages.INVALID_HANDLER);

        url = resolveUrl(url);

        InterceptorHandler interceptorHandler = new InterceptorHandler(interceptors);
        HttpHandler handler = new BlockingHandler(new RestDispatcher(endpoint, interceptorHandler, exceptionMapper, mimeTypes));
        return new MappedEndpoint(method.toString(), url, MappedEndpoint.Type.REST, handler);
    }

    public static MappedEndpoint websocket(String url, AbstractReceiveListener endpoint, List<Interceptor> interceptors) {
        Objects.requireNonNull(url, Messages.INVALID_URL);
        Objects.requireNonNull(endpoint, Messages.INVALID_HANDLER);
        url = resolveUrl(url);
        WebSocketProtocolHandshakeHandler websocket = Handlers.websocket((exchange, channel) -> {
            channel.getReceiveSetter().set(endpoint);
            channel.resumeReceives();
        });

        InterceptorHandler interceptorHandler = new InterceptorHandler(interceptors);
        interceptorHandler.setNext(websocket);

        return new MappedEndpoint(MappedEndpoint.Type.WS.name(), url, MappedEndpoint.Type.WS, interceptorHandler);
    }

    public static MappedEndpoint websocket(String url, WebSocketConnectionCallback connectionCallback, List<Interceptor> interceptors) {
        Objects.requireNonNull(url, Messages.INVALID_URL);
        Objects.requireNonNull(connectionCallback, Messages.INVALID_HANDLER);
        WebSocketProtocolHandshakeHandler websocket = Handlers.websocket(connectionCallback);

        InterceptorHandler interceptorHandler = new InterceptorHandler(interceptors);
        interceptorHandler.setNext(websocket);
        return new MappedEndpoint(Methods.GET_STRING, url, MappedEndpoint.Type.WS, interceptorHandler);

    }

    public static MappedEndpoint websocket(String url, WebsocketEndpoint websocketEndpoint, List<Interceptor> interceptors) {
        Objects.requireNonNull(url, Messages.INVALID_URL);
        Objects.requireNonNull(websocketEndpoint, Messages.INVALID_HANDLER);
        url = resolveUrl(url);

        WebSocketProtocolHandshakeHandler websocket = Handlers.websocket((exchange, channel) -> {
            websocketEndpoint.onConnect(exchange, channel);

            channel.getReceiveSetter().set(websocketEndpoint);
            channel.resumeReceives();
        });

        InterceptorHandler interceptorHandler = new InterceptorHandler(interceptors);
        interceptorHandler.setNext(websocket);

        return new MappedEndpoint(MappedEndpoint.Type.WS.name(), url, MappedEndpoint.Type.WS, interceptorHandler);
    }

    public static MappedEndpoint sse(String url, List<Interceptor> interceptors) {
        Objects.requireNonNull(url, Messages.INVALID_URL);
        url = resolveUrl(url);


        InterceptorHandler interceptorHandler = new InterceptorHandler(interceptors);
        interceptorHandler.setNext(Handlers.serverSentEvents());

        return new MappedEndpoint(Methods.GET_STRING, url, MappedEndpoint.Type.SSE, interceptorHandler);
    }

    public static MappedEndpoint staticFiles(String url, String docPath, List<Interceptor> interceptors) {
        Objects.requireNonNull(url, Messages.INVALID_URL);
        url = resolveUrl(url);
        docPath = docPath.startsWith(BASE_PATH) ? docPath.replaceFirst(BASE_PATH, "") : docPath;
        HttpHandler handler = Handlers.path()
                .addPrefixPath(url,
                        Handlers.resource(new ClassPathResourceManager(Thread.currentThread().getContextClassLoader(), docPath))
                                .addWelcomeFiles("index.html"));

        InterceptorHandler interceptorHandler = new InterceptorHandler(interceptors);
        interceptorHandler.setNext(handler);

        return new MappedEndpoint(Methods.GET_STRING, url, MappedEndpoint.Type.STATIC, interceptorHandler);
    }

    public static MappedEndpoint staticFiles(String url, List<Interceptor> interceptors) {
        return staticFiles(url, STATIC_FILES_DEFAULT_LOCATION, interceptors);
    }

    public static MappedEndpoint multipart(String url, Consumer<MultipartExchange> endpoint, List<Interceptor> interceptors) {
        return multipart(url, endpoint, interceptors, -1);
    }

    public static MappedEndpoint multipart(String url, Consumer<MultipartExchange> endpoint, List<Interceptor> interceptors, long maxSize) {
        MultipartEntrypointHandler entrypointHandler = new MultipartEntrypointHandler(endpoint);

        InterceptorHandler interceptorHandler = new InterceptorHandler(interceptors);
        interceptorHandler.setNext(entrypointHandler);

        MultiPartParserDefinition multiPartParserDefinition = new MultiPartParserDefinition();
        multiPartParserDefinition.setMaxIndividualFileSize(maxSize);

        EagerFormParsingHandler formHandler = new EagerFormParsingHandler();
        formHandler.setNext(interceptorHandler);

        return new MappedEndpoint(Methods.POST_STRING, url, MappedEndpoint.Type.MULTIPART, formHandler);
    }


    private static String resolveUrl(String url) {
        Objects.requireNonNull(url, INVALID_URL);
        if (url.isEmpty()) {
            Objects.requireNonNull(url, EMPTY_URL);
        }
        if (BASE_PATH.equals(url)) {
            return url;
        }
        url = url.startsWith(BASE_PATH) ? url : BASE_PATH + url;
        url = url.endsWith(BASE_PATH) ? url.substring(0, url.length() - 1) : url;
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
    public static String[] removedPathTemplate(List<MappedEndpoint> mappedEndpoints) {

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
