package io.joshworks.microserver.rest;

import io.joshworks.microserver.parser.Parser;
import io.joshworks.microserver.parser.Parsers;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;

/**
 * Created by josh on 3/12/17.
 */
public class Response {


    private final HttpServerExchange exchange;
    private int status = 200;
    private String contentType = "application/json";

    public Response(HttpServerExchange exchange) {
        this.exchange = exchange;
    }


    public Response header(String name, String value) {
        exchange.getResponseHeaders().add(HttpString.tryFromString(name), value);
        return this;
    }

    public Response header(String name, long value) {
        exchange.getResponseHeaders().add(HttpString.tryFromString(name), value);
        return this;
    }

    public Response contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public Response status(int status) {
        this.status = status;
        return this;
    }

    public void send(Object response) {
        this.response(response);
    }

    public void send(Object response, String contentType) {
        contentType(contentType);
        this.response(response);
    }

    private void response(Object response) {
        try {
            Parser responseParser = Parsers.getParser(contentType);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, contentType);
            exchange.setStatusCode(status);
            exchange.getResponseSender().send(responseParser.writeValue(response));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
