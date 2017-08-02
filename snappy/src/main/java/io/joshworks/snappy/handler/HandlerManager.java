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

import io.joshworks.snappy.rest.Interceptor;
import io.undertow.Handlers;
import io.undertow.predicate.Predicate;
import io.undertow.predicate.Predicates;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.PathTemplateHandler;
import io.undertow.server.handlers.PredicateHandler;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;

import java.util.List;

/**
 * Created by josh on 3/11/17.
 */
public class HandlerManager {

    private static final String WS_UPGRADE_HEADER_VALUE = "websocket";

    //chain of responsibility
    public static HttpHandler createRootHandler(
            List<MappedEndpoint> mappedEndpoints,
            List<Interceptor> rootInterceptors,
            String basePath,
            boolean httpTracer) {

        final RoutingHandler routingRestHandler = new TrailingSlashRoutingHandler();
        final PathTemplateHandler websocketHandler = Handlers.pathTemplate();
        HttpHandler staticHandler = null;

        for (MappedEndpoint me : mappedEndpoints) {

            if (MappedEndpoint.Type.REST.equals(me.type)) {
                String endpointPath = HandlerUtil.BASE_PATH.equals(basePath) ? me.url : basePath + me.url;
                routingRestHandler.add(me.method, endpointPath, me.handler);
            }
            if (MappedEndpoint.Type.MULTIPART.equals(me.type)) {
                routingRestHandler.add(me.method, me.url, me.handler);
            }
            if (MappedEndpoint.Type.SSE.equals(me.type)) {
                routingRestHandler.add(me.method, me.url, me.handler);
            }
            if (MappedEndpoint.Type.WS.equals(me.type)) {
                websocketHandler.add(me.url, me.handler);
            }
            if (MappedEndpoint.Type.STATIC.equals(me.type)) {
                staticHandler = me.handler;
            }
        }

        HttpHandler resolved = resolveHandlers(routingRestHandler, websocketHandler, staticHandler, mappedEndpoints);
        HttpHandler root = resolveRootInterceptors(resolved, rootInterceptors);

        HttpHandler handler = httpTracer ? Handlers.requestDump(root) : root;

        return Handlers.gracefulShutdown(handler);
    }


    private static HttpHandler resolveRootInterceptors(HttpHandler original, List<Interceptor> interceptors) {
        if (!interceptors.isEmpty()) {
            InterceptorHandler interceptorHandler = new InterceptorHandler(interceptors);
            interceptorHandler.setNext(original);
            return interceptorHandler;
        }
        return original;
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
