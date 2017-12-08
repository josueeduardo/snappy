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

import io.joshworks.snappy.http.ConnegHandler;
import io.joshworks.snappy.http.ExceptionMapper;
import io.joshworks.snappy.http.HttpDispatcher;
import io.joshworks.snappy.http.Interceptor;
import io.undertow.Handlers;
import io.undertow.predicate.Predicate;
import io.undertow.predicate.Predicates;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.PathTemplateHandler;
import io.undertow.server.handlers.PredicateHandler;
import io.undertow.server.handlers.encoding.EncodingHandler;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;

import static io.joshworks.snappy.SnappyServer.*;

/**
 * Created by josh on 3/11/17.
 */
public class HandlerManager {

    private static final Logger logger = LoggerFactory.getLogger(LOGGER_NAME);
    private static final String WS_UPGRADE_HEADER_VALUE = "websocket";

    //chain of responsibility
    public static HttpHandler createRootHandler(
            List<MappedEndpoint> mappedEndpoints,
            List<Interceptor> rootInterceptors,
            List<Interceptor> endpointInterceptors,
            ExceptionMapper exceptionMapper,
            String basePath,
            boolean httpTracer) {

        final RoutingHandler routingRestHandler = new HttpRootHandler(false);
        final PathTemplateHandler websocketHandler = Handlers.pathTemplate(false);
        HttpHandler staticHandler = null;


        for (MappedEndpoint me : mappedEndpoints) {

            if (MappedEndpoint.Type.REST.equals(me.type)) {
                HttpHandler httpDispatcher =
                        new BlockingHandler(
                                new HttpDispatcher(
                                        new ConnegHandler(
                                                new InterceptorHandler(me.handler, endpointInterceptors), me.mediaTypes
                                        ),
                                        exceptionMapper
                                )
                        );

                String endpointPath = HandlerUtil.BASE_PATH.equals(basePath) ? me.url : basePath + me.url;

                HttpHandler httpHandler = wrapCompressionHandler(httpDispatcher, me);
                routingRestHandler.add(me.method, endpointPath, httpHandler);
            }
            if (MappedEndpoint.Type.MULTIPART.equals(me.type)) {
                HttpHandler httpDispatcher =
                        new BlockingHandler(
                                new HttpDispatcher(
                                        new ConnegHandler(
                                                new InterceptorHandler(me.handler, endpointInterceptors), me.mediaTypes),
                                        exceptionMapper)
                        );

                String endpointPath = HandlerUtil.BASE_PATH.equals(basePath) ? me.url : basePath + me.url;

                HttpHandler httpHandler = wrapCompressionHandler(httpDispatcher, me);
                routingRestHandler.add(me.method, endpointPath, httpHandler);
            }
            if (MappedEndpoint.Type.SSE.equals(me.type)) {
                InterceptorHandler interceptorHandler = new InterceptorHandler(me.handler, endpointInterceptors);
                HttpHandler httpHandler = wrapCompressionHandler(interceptorHandler, me);

                String endpointPath = HandlerUtil.BASE_PATH.equals(basePath) ? me.url : basePath + me.url;
                routingRestHandler.add(me.method, endpointPath, httpHandler);
            }
            if (MappedEndpoint.Type.WS.equals(me.type)) {
                InterceptorHandler interceptorHandler = new InterceptorHandler(me.handler, endpointInterceptors);
                HttpHandler httpHandler = wrapCompressionHandler(interceptorHandler, me);
                websocketHandler.add(me.url, httpHandler);
            }
            if (MappedEndpoint.Type.STATIC.equals(me.type)) {
                staticHandler = wrapCompressionHandler(me.handler, me);
            }
        }

        HttpHandler resolved = resolveHandlers(routingRestHandler, websocketHandler, staticHandler, mappedEndpoints);

        HttpHandler root = wrapRootInterceptorHandler(resolved, rootInterceptors);
        HttpHandler handler = wrapRequestDump(root, httpTracer);
//        handler = wrapServerName(handler);

        return Handlers.gracefulShutdown(handler);
    }


    private static HttpHandler wrapRootInterceptorHandler(HttpHandler original, List<Interceptor> interceptors) {
        return interceptors.isEmpty() ? original : new InterceptorHandler(original, interceptors);
    }

    private static HttpHandler wrapCompressionHandler(HttpHandler original, MappedEndpoint endpoint) {
        if (MappedEndpoint.Type.REST.equals(endpoint.type) || MappedEndpoint.Type.MULTIPART.equals(endpoint.type)) {
            return defaultCompressionHandler(original);
        }
        //TODO not supported
//        else if(MappedEndpoint.Type.SSE.equals(endpoint.type)) {
//            return gzipHandler(original, new GzipEncodingProvider());
//        }
        else {
            logger.warn("GZIP encoding not implemented for {} endpoints, response will not be compressed for '{}'.", endpoint.type, endpoint.url);
            return original;
        }
    }

    private static HttpHandler defaultCompressionHandler(HttpHandler handler) {
        return new EncodingHandler.Builder().build(new HashMap<>()).wrap(handler);
    }

    private static HttpHandler wrapRequestDump(HttpHandler original, boolean httpTracer) {
        return httpTracer ? Handlers.requestDump(original) : original;
    }

    private static HttpHandler wrapServerName(HttpHandler original) {
        return new ServerNameHandler(original);
    }

    private static HttpHandler resolveHandlers(HttpHandler rest, HttpHandler ws, HttpHandler file, List<MappedEndpoint> mappedEndpoints) {

        PredicateHandler websocketRestResolved = Handlers.predicate(value -> {
            HeaderValues upgradeHeader = value.getRequestHeaders().get(Headers.UPGRADE);
            return upgradeHeader != null && upgradeHeader.stream().anyMatch(v -> v.equalsIgnoreCase(WS_UPGRADE_HEADER_VALUE));
        }, ws, rest);

        if (file != null) {
            String[] mappedServices = HandlerUtil.removePathTemplate(mappedEndpoints);
            Predicate mappedPredicate = Predicates.prefixes(mappedServices);
            return Handlers.predicate(mappedPredicate, websocketRestResolved, file);
        }
        return websocketRestResolved;
    }


}
