package io.joshworks.snappy.handler;

import io.joshworks.snappy.rest.Interceptor;
import io.joshworks.snappy.rest.RestExchange;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Josh Gontijo on 3/15/17.
 */
public class RestEntrypoint implements HttpHandler{

    private final Consumer<RestExchange> endpoint;
    private final List<Interceptor> interceptors = new ArrayList<>();

    public RestEntrypoint(Consumer<RestExchange> endpoint, List<Interceptor> interceptors) {
        this.endpoint = endpoint;
        this.interceptors.addAll(interceptors);
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        RestExchange restExchange = new RestExchange(exchange);

        for (Interceptor interceptor : interceptors) {
            if (!exchange.isResponseComplete()) {
                interceptor.handleRequest(restExchange);
            }
        }
        if (!exchange.isResponseComplete()) {
            endpoint.accept(restExchange);
        }
    }

}
