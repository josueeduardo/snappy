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

package io.joshworks.snappy;

import io.joshworks.snappy.handler.HandlerUtil;
import io.joshworks.snappy.handler.MappedEndpoint;
import io.joshworks.snappy.handler.TrailingSlashRoutingHandler;
import io.joshworks.snappy.rest.Interceptor;
import io.undertow.Handlers;
import io.undertow.predicate.Predicate;
import io.undertow.predicate.Predicates;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.ResponseCodeHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Josh Gontijo on 3/19/17.
 */
public class AdminManager {

    int port = 9100;
    String bindAddress = "127.0.0.1";

    private final List<MappedEndpoint> endpoints = new ArrayList<>();
    private final RoutingHandler routingAdminHandler = new TrailingSlashRoutingHandler();
    private HttpHandler controlPanel = new ResponseCodeHandler(404);

    private final List<Interceptor> interceptors = new ArrayList<>();

    AdminManager() {

    }

    public void addEndpoint(MappedEndpoint endpoint) {
        endpoints.add(endpoint);
    }

    HttpHandler resolveHandlers() {
        endpoints.forEach(me -> routingAdminHandler.add(me.method, me.url, me.handler));

        String[] mappedServices = HandlerUtil.removePathTemplate(endpoints);
        Predicate mappedPredicate = Predicates.prefixes(mappedServices);
        return Handlers.predicate(mappedPredicate, routingAdminHandler, controlPanel);
    }

    List<MappedEndpoint> getEndpoints() {
        return new ArrayList<>(endpoints);
    }

    public void setAdminPage(String url, String docPath, List<Interceptor> interceptors) {
        controlPanel = HandlerUtil.staticFiles(url, docPath, interceptors).handler;
    }

    public List<Interceptor> getInterceptors() {
        return interceptors;
    }


}
