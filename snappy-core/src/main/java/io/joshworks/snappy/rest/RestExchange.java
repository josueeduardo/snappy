package io.joshworks.snappy.rest;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;

import java.util.Deque;
import java.util.Map;

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

    public Map<String, Deque<String>> queryParams() {
        return httpServerExchange.getQueryParameters();
    }

    public String queryParams(String key) {
        Deque<String> params = httpServerExchange.getQueryParameters().get(key);
        return params.isEmpty() ? null : params.getFirst();
    }

    public Deque<String> queryParamsValues(String key) {
        return httpServerExchange.getQueryParameters().get(key);
    }

    public String protocol() {
        return httpServerExchange.getProtocol().toString();
    }

    public String host() {
        return httpServerExchange.getHostName();
    }

    public int port() {
        return httpServerExchange.getHostPort();
    }

    public String method() {
        return httpServerExchange.getRequestMethod().toString();
    }

    public String scheme() {
        return httpServerExchange.getRequestScheme();
    }

    public String path() {
        return httpServerExchange.getRequestPath();
    }

    public String userAgent() {
        HeaderValues userAgent = httpServerExchange.getRequestHeaders().get(Headers.USER_AGENT);
        if (userAgent != null && !userAgent.isEmpty()) {
            return userAgent.getFirst();
        }
        return null;
    }

    public MediaType type() {
        HeaderValues contentType = httpServerExchange.getRequestHeaders().get(Headers.CONTENT_TYPE);
        if (contentType != null && !contentType.isEmpty()) {
            return MediaType.valueOf(contentType.getFirst());
        }
        return MediaType.WILDCARD_TYPE;
    }

    public Response response() {
        return response;
    }

}
