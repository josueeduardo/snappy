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

import io.joshworks.snappy.Exchange;
import io.joshworks.snappy.handler.HandlerUtil;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.joshworks.snappy.SnappyServer.*;

/**
 * Created by Josh Gontijo on 3/15/17.
 */
public abstract class HttpEntrypoint<T extends Exchange> implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(LOGGER_NAME);

    private final HttpConsumer<T> endpoint;
    private final ExceptionMapper exceptionMapper;

    public HttpEntrypoint(HttpConsumer<T> endpoint, ExceptionMapper exceptionMapper) {
        this.endpoint = endpoint;
        this.exceptionMapper = exceptionMapper;
    }

    protected abstract T createExchange(HttpServerExchange exchange);

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        T httpExchange = createExchange(exchange);
        try {
            if (!exchange.isResponseComplete()) {
                endpoint.accept(httpExchange);
            }
        } catch (Exception e) {
            if (exchange.isResponseChannelAvailable() && !exchange.isResponseComplete()) {
                //unwraps the original caught from RestConsumer
                if (e instanceof ApplicationException) {
                    e = ((ApplicationException) e).original;
                }

                long now = System.currentTimeMillis();
                logger.error(HandlerUtil.exceptionMessageTemplate(exchange, now, "Application error"), e);
                exceptionMapper.getOrFallback(e).accept(e, httpExchange);
                exchange.endExchange();
            } else {
                logger.error(e.getMessage(), e);
            }
        }
    }

}
