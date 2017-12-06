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

import io.joshworks.snappy.Exchange;
import io.joshworks.snappy.http.ErrorHandler;
import io.joshworks.snappy.http.ExceptionMapper;
import io.joshworks.snappy.http.Interceptor;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

import static io.joshworks.snappy.SnappyServer.LOGGER_NAME;

/**
 * Created by Josh Gontijo on 3/19/17.
 */
public class InterceptorHandler extends ChainHandler {

    private static final Logger logger = LoggerFactory.getLogger(LOGGER_NAME);
    private final List<Interceptor> interceptors;
    private final ExceptionMapper exceptionMapper = new ExceptionMapper();

    public InterceptorHandler(HttpHandler next, List<Interceptor> interceptors) {
        super(next);
        this.interceptors = interceptors;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String url = exchange.getRequestPath();

        boolean proceed = intercept(Interceptor.Type.BEFORE, exchange, url);
        if (!proceed) {
            return;
        }

        exchange.addExchangeCompleteListener((completeExchange, nextListener) -> {
            intercept(Interceptor.Type.AFTER, completeExchange, url);
            nextListener.proceed(); //always proceed
        });

        next.handleRequest(exchange);
    }

    private boolean intercept(Interceptor.Type type, HttpServerExchange exchange, String url) {
        List<Interceptor> matches = interceptors.stream().filter(i -> i.match(type, url)).collect(Collectors.toList());
        for (Interceptor interceptor : matches) {
            Exchange requestExchange = new Exchange(exchange);
            try {
                interceptor.intercept(requestExchange);
            } catch (Exception ex) {
                long now = System.currentTimeMillis();
                String message = "Error handling " + type.name() + " interceptor, next interceptor will not proceed";
                logger.error(HandlerUtil.exceptionMessageTemplate(exchange, now, message), ex);
                if (Interceptor.Type.BEFORE.equals(type)) {
                    ErrorHandler<Exception> orFallback = exceptionMapper.getOrFallback(ex);
                    orFallback.accept(ex, requestExchange);
                }
                if (!exchange.isComplete()) {
                    exchange.endExchange();
                }
                return false;
            }
        }
        return !exchange.isComplete();
    }
}
