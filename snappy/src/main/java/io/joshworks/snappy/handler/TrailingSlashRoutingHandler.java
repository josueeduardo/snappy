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

import io.undertow.server.HttpServerExchange;
import io.undertow.server.RoutingHandler;

/**
 * Created by Josh Gontijo on 3/17/17.
 * <p>
 * Handler that removes trailing slash from incoming requests in order to avoid null path parameters
 * For example:
 * For a given resource <b>/users/{id}</b>
 * <b>/users/</b> would match with the resource, passing in an null id
 * Instead it would rewrite to
 * <b>/users</b> causing a 404, thus avoiding null path parameter
 */
public class TrailingSlashRoutingHandler extends RoutingHandler {

    private static final String TRAILING_SLASH = "/";

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String requestPath = exchange.getRelativePath();
        if (requestPath != null && !requestPath.isEmpty() && !requestPath.equals(TRAILING_SLASH)) {
            requestPath = requestPath.endsWith(TRAILING_SLASH) ? requestPath.substring(0, requestPath.length() - 1) : requestPath;
            exchange.setRelativePath(requestPath);
        }
        super.handleRequest(exchange);
    }
}