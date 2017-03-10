package io.joshworks.simpletow.rest;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import java.util.List;

/**
 * Created by josh on 3/5/17.
 */
public class RestHandler implements HttpHandler {

    private final RestEndpoint endpoint;
    private List<Interceptor> interceptors;

    public RestHandler(RestEndpoint endpoint, List<Interceptor> interceptors) {
        this.endpoint = endpoint;
        this.interceptors = interceptors;
    }

    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
        RestExchange restExchange = new RestExchange(httpServerExchange);
        for (Interceptor interceptor : interceptors) {
            interceptor.handleRequest(restExchange);
        }
        if (!httpServerExchange.isResponseComplete()) {
            endpoint.handle(restExchange);
        }
    }


}
