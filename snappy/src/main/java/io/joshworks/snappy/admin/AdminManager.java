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

package io.joshworks.snappy.admin;

import io.joshworks.snappy.handler.HandlerUtil;
import io.joshworks.snappy.handler.MappedEndpoint;
import io.joshworks.snappy.handler.TrailingSlashRoutingHandler;
import io.joshworks.snappy.metric.MetricData;
import io.joshworks.snappy.metric.MetricsHandler;
import io.joshworks.snappy.metric.RestMetricHandler;
import io.joshworks.snappy.rest.ExceptionMapper;
import io.joshworks.snappy.rest.Interceptor;
import io.joshworks.snappy.rest.MediaType;
import io.undertow.Handlers;
import io.undertow.predicate.Predicate;
import io.undertow.predicate.Predicates;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Josh Gontijo on 3/19/17.
 */
public class AdminManager {

    private final List<MappedEndpoint> adminEndpoints = new ArrayList<>();
    private final RoutingHandler routingAdminHandler = new TrailingSlashRoutingHandler();
    private final HttpHandler controlPanel;

    //TODO add security interceptor etc
    private final List<Interceptor> adminInterceptor = new ArrayList<>();

    public AdminManager() {
        controlPanel = HandlerUtil.staticFiles("/", "admin", adminInterceptor).handler;
    }

    public void registerMetrics(List<RestMetricHandler> metricsHandlers) {

        ExceptionMapper internalExceptionMapper = new ExceptionMapper();

        MappedEndpoint getMetrics = HandlerUtil.rest(Methods.GET, "/metrics", (exchange) -> exchange.send(
                new MetricData(metricsHandlers), MediaType.APPLICATION_JSON_TYPE), internalExceptionMapper, new ArrayList<>());

        MappedEndpoint clearMetrics = HandlerUtil.rest(Methods.DELETE, "/metrics", (exchange) -> {
            metricsHandlers.forEach(MetricsHandler::reset);
            exchange.status(StatusCodes.NO_CONTENT);
        }, internalExceptionMapper, new ArrayList<>());

        routingAdminHandler.add(getMetrics.method, getMetrics.url, getMetrics.handler);
        routingAdminHandler.add(clearMetrics.method, clearMetrics.url, clearMetrics.handler);

        adminEndpoints.add(getMetrics);
        adminEndpoints.add(clearMetrics);
    }

    public HttpHandler resolveHandlers() {
        String[] mappedServices = HandlerUtil.removePathTemplate(adminEndpoints);
        Predicate mappedPredicate = Predicates.prefixes(mappedServices);
        return Handlers.predicate(mappedPredicate, routingAdminHandler, controlPanel);
    }

    public List<MappedEndpoint> getAdminEndpoints() {
        return adminEndpoints;
    }
}
