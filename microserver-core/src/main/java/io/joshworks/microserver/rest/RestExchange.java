package io.joshworks.microserver.rest;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.HeaderValues;
import io.undertow.util.PathTemplateMatch;

import java.util.Deque;

/**
 * Created by josh on 3/5/17.
 */
public class RestExchange {


    public final HttpServerExchange httpServerExchange;
    private Response response;
    private Body body;

    public RestExchange(HttpServerExchange httpServerExchange) {
        this.httpServerExchange = httpServerExchange;
        this.body = new Body(httpServerExchange);
        this.response = new Response(httpServerExchange);
    }


    public Body body() {
        return body;
    }

    public HeaderMap headers() {
        return httpServerExchange.getRequestHeaders();
    }

    public HeaderValues header(String headerName) {
        return httpServerExchange.getRequestHeaders().get(headerName);
    }

    public int status() {
        return httpServerExchange.getStatusCode();
    }

    public String parameters(String key) {
        PathTemplateMatch pathMatch = httpServerExchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
        return pathMatch.getParameters().get(key);
    }

    public Deque<String> queryParameters(String key) {
        return httpServerExchange.getQueryParameters().get(key);
    }

    public String queryParameter(String key) {
        Deque<String> parameters = httpServerExchange.getQueryParameters().get(key);
        return parameters.getFirst();
    }

    public Response response() {
        return response;
    }

}
