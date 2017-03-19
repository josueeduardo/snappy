package io.joshworks.snappy.handler;

import io.joshworks.snappy.rest.ExceptionMapper;
import io.joshworks.snappy.rest.RestExchange;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import java.util.function.Consumer;

/**
 * Created by Josh Gontijo on 3/15/17.
 */
public class RestEntrypoint implements HttpHandler {

    private final Consumer<RestExchange> endpoint;
    private final ExceptionMapper exceptionMapper;

    public RestEntrypoint(Consumer<RestExchange> endpoint, ExceptionMapper exceptionMapper) {
        this.endpoint = endpoint;
        this.exceptionMapper = exceptionMapper;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        RestExchange restExchange = new RestExchange(exchange);
        try {
            if (!exchange.isResponseComplete()) {
                endpoint.accept(restExchange);
            }
        } catch (Exception e) {
            exceptionMapper.getOrFallback(e).onException(e, restExchange);
        }
    }

}
