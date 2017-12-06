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

package io.joshworks.snappy.http;

import io.joshworks.snappy.handler.ChainHandler;
import io.joshworks.snappy.handler.HandlerUtil;
import io.joshworks.snappy.handler.UnsupportedMediaType;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.joshworks.snappy.SnappyServer.*;

/**
 * Created by Josh Gontijo on 3/5/17.
 * <p>
 * This should be the higher level Rest class, ideally this is wrapped in the Error handler which will return
 * handler exceptions thrown by the endpoints
 */
public class HttpDispatcher extends ChainHandler {

    private static final Logger logger = LoggerFactory.getLogger(LOGGER_NAME);

    private final ExceptionMapper exceptionMapper;

    public HttpDispatcher(HttpHandler next, ExceptionMapper exceptionMapper) {
        super(next);
        this.exceptionMapper = exceptionMapper;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        try {
            this.next.handleRequest(exchange);
        } catch (UnsupportedMediaType connex) {

            long now = System.currentTimeMillis();
            String description = "Unsupported media type " + connex.headerValues + " supported types: " + connex.types;
            logger.error(HandlerUtil.exceptionMessageTemplate(exchange, now, description));

            sendErrorResponse(exchange, connex);
            exchange.endExchange();

        } catch (Exception e) { //Should not happen (server error)
            long now = System.currentTimeMillis();
            logger.error(HandlerUtil.exceptionMessageTemplate(exchange, now, "Server error"), e);
            sendErrorResponse(exchange, e);
            exchange.endExchange();

        }
    }

    private <T extends Exception> void sendErrorResponse(HttpServerExchange exchange, T ex) {
        ErrorHandler<T> errorHandler = exceptionMapper.getOrFallback(ex);
        try {
            errorHandler.accept(ex, new HttpExchange(exchange));
        } catch (Exception handlingError) {
            logger.error("Exception was thrown when executing original handler: {}, body will be null", errorHandler.getClass().getName(), handlingError);
        }
    }

}
