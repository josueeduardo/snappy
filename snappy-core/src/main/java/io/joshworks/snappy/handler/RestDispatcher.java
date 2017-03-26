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

import io.joshworks.snappy.parser.MediaTypes;
import io.joshworks.snappy.rest.ErrorHandler;
import io.joshworks.snappy.rest.ExceptionMapper;
import io.joshworks.snappy.rest.ExceptionWrapper;
import io.joshworks.snappy.rest.RestExchange;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

import static io.joshworks.snappy.SnappyServer.*;

/**
 * Created by Josh Gontijo on 3/5/17.
 * <p>
 * This should be the higher level Rest class, ideally this is wrapped in the Error handler which will return
 * handler exceptions thrown by the endpoints
 */
public class RestDispatcher implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(LOGGER_NAME);

    private final ConnegHandler connegHandler;
    private final ExceptionMapper exceptionMapper;

    RestDispatcher(Consumer<RestExchange> endpoint, InterceptorHandler interceptorHandler, ExceptionMapper exceptionMapper, MediaTypes... mimeTypes) {
        this.exceptionMapper = exceptionMapper;
        interceptorHandler.setNext(new RestEntrypoint(endpoint, exceptionMapper));
        this.connegHandler = new ConnegHandler(interceptorHandler, exceptionMapper, mimeTypes);
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        try {
            this.connegHandler.handleRequest(exchange);
        } catch (UnsupportedMediaType connex) {

            ExceptionWrapper<UnsupportedMediaType> wrapper = new ExceptionWrapper<>(connex);
            String description = "Unsupported media type " + connex.headerValues + " supported types: " + connex.types;
            logger.error(HandlerUtil.exceptionMessageTemplate(exchange, wrapper.timestamp, description));

            sendErrorResponse(exchange, wrapper);
            exchange.endExchange();

        } catch (Exception e) { //Should not happen (server error)
            ExceptionWrapper<Exception> wrapper = new ExceptionWrapper<>(e);

            logger.error(HandlerUtil.exceptionMessageTemplate(exchange, wrapper.timestamp, "Server error"), e);
            sendErrorResponse(exchange, wrapper);
            exchange.endExchange();

        }
    }

    private <T extends Exception> void sendErrorResponse(HttpServerExchange exchange, ExceptionWrapper<T> wrapper) {
        ErrorHandler<T> errorHandler = exceptionMapper.getOrFallback(wrapper.exception);
        try {
            errorHandler.onException(wrapper, new RestExchange(exchange));
        } catch (Exception handlingError) {
            logger.error("Exception was thrown when executing exception handler: {}, no body will be sent", errorHandler.getClass().getName(), handlingError);
        }
    }

}
