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
import io.joshworks.snappy.rest.ErrorHandler;
import io.joshworks.snappy.rest.ExceptionMapper;
import io.joshworks.snappy.rest.ExceptionWrapper;
import io.joshworks.snappy.rest.Interceptor;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.ResponseCodeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

import static io.joshworks.snappy.SnappyServer.*;

/**
 * Created by Josh Gontijo on 3/19/17.
 */
public class InterceptorHandler implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(LOGGER_NAME);
    private final List<Interceptor> interceptors;
    private HttpHandler next = ResponseCodeHandler.HANDLE_404;
    private ExceptionMapper exceptionMapper = new ExceptionMapper();

    public InterceptorHandler(List<Interceptor> interceptors) {
        this.interceptors = interceptors;
    }

    public InterceptorHandler(List<Interceptor> interceptors, ExceptionMapper exceptionMapper) {
        this.interceptors = interceptors;
        this.exceptionMapper = exceptionMapper;
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
        Exchange requestExchange = new Exchange(exchange);
        List<Interceptor> matches = interceptors.stream().filter(i -> i.match(type, url)).collect(Collectors.toList());
        for (Interceptor interceptor : matches) {
            try {
                interceptor.intercept(requestExchange);
            } catch (Exception ex) {
                ExceptionWrapper<Exception> wrapper = new ExceptionWrapper<>(ex);
                String message = "Error handling " + type.name() + " interceptor, next interceptor will not proceed";
                logger.error(HandlerUtil.exceptionMessageTemplate(exchange, wrapper.timestamp, message), ex);
                if (Interceptor.Type.BEFORE.equals(type)) {
                    ErrorHandler<Exception> orFallback = exceptionMapper.getOrFallback(ex);
                    orFallback.onException(wrapper, requestExchange);
                }
                if (!exchange.isComplete()) {
                    exchange.endExchange();
                }
                return false;
            }
        }
        return !exchange.isComplete();
    }

    public void setNext(HttpHandler next) {
        this.next = next;
    }
}
