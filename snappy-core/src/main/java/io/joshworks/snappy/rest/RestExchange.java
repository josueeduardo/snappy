package io.joshworks.snappy.rest;

import io.joshworks.snappy.Exchange;
import io.undertow.server.HttpServerExchange;

/**
 * Created by josh on 3/5/17.
 */
public class RestExchange extends Exchange {

    private Body body;

    public RestExchange(HttpServerExchange exchange) {
        super(exchange);
        this.body = new Body(exchange);
    }

    public Body body() {
        return body;
    }

}
