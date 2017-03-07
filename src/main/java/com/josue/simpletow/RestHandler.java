package com.josue.simpletow;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

/**
 * Created by josh on 3/5/17.
 */
public class RestHandler implements HttpHandler {

    private final RestEndpoint endpoint;

    public RestHandler(RestEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
       endpoint.handle(new RestExchange(httpServerExchange));
    }


}
