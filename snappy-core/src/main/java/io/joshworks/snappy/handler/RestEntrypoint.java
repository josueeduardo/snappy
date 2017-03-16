package io.joshworks.snappy.handler;

import io.joshworks.snappy.rest.ErrorHandler;
import io.joshworks.snappy.rest.ExceptionMapper;
import io.joshworks.snappy.rest.Interceptor;
import io.joshworks.snappy.rest.RestExchange;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Josh Gontijo on 3/15/17.
 */
public class RestEntrypoint implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(RestEntrypoint.class);

    private final Consumer<RestExchange> endpoint;
    private final List<Interceptor> interceptors = new ArrayList<>();
    private final ExceptionMapper exceptionMapper;

    public RestEntrypoint(Consumer<RestExchange> endpoint, List<Interceptor> interceptors, ExceptionMapper exceptionMapper) {
        this.endpoint = endpoint;
        this.interceptors.addAll(interceptors);
        this.exceptionMapper = exceptionMapper;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        RestExchange restExchange = new RestExchange(exchange);
        for (Interceptor interceptor : interceptors) {
            if (!exchange.isResponseComplete()) {
                interceptor.handleRequest(restExchange);
            }
        }

        //TODO handle here after request interceptors
        exchange.addExchangeCompleteListener((exchange1, nextListener) -> {
            interceptors.forEach(i -> i.handleRequest(restExchange));
            nextListener.proceed();
        });

        if (!exchange.isResponseComplete()) {
            tryHandle(restExchange);
        }
    }

    private void tryHandle(RestExchange restExchange) {
        try {

            endpoint.accept(restExchange);

        } catch (Exception e) {
            ErrorHandler errorHandler = exceptionMapper.getOrFallback(e);
            if (errorHandler == null) {
                logger.error("No event handler mapped for exception " + e.getClass().getName());
                errorHandler = exceptionMapper.getFallbackInternalError();
            }
            errorHandler.onException(e, restExchange);

        }
    }

}
