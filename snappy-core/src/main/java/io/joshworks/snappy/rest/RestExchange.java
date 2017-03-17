package io.joshworks.snappy.rest;

import io.joshworks.snappy.handler.ConnegHandler;
import io.joshworks.snappy.parser.Parser;
import io.joshworks.snappy.parser.Parsers;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.util.HeaderMap;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import io.undertow.util.PathTemplateMatch;

import java.util.Deque;
import java.util.Map;

/**
 * Created by josh on 3/5/17.
 */
public class RestExchange {

    public final HttpServerExchange exchange;
    private MediaType responseContentType = MediaType.APPLICATION_JSON_TYPE;
    private Body body;

    public RestExchange(HttpServerExchange exchange) {
        this.exchange = exchange;
        this.body = new Body(exchange);

        setNegotiatedContentType();
    }

    //If client accepts anything, json will be used
    private void setNegotiatedContentType() {
        ConnegHandler.NegotiatedMediaType attachment = exchange.getAttachment(ConnegHandler.NEGOTIATED_MEDIA_TYPE);
        if (attachment != null) { //should not happen
            MediaType negotiated = attachment.produces;
            if (negotiated != null && !negotiated.equals(MediaType.WILDCARD_TYPE)) {
                responseContentType = negotiated;
            }
        }
        setResponseMediaType(responseContentType);
    }


    public Body body() {
        return body;
    }

    public HeaderMap headers() {
        return exchange.getRequestHeaders();
    }

    public HeaderValues header(String headerName) {
        return exchange.getRequestHeaders().get(headerName);
    }

    public int status() {
        return exchange.getStatusCode();
    }

    public String pathParameter(String key) {
        PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
        return pathMatch.getParameters().get(key);
    }

    public Property pathParameterValue(String key) {
       return new Property(pathParameter(key));
    }

    public Map<String, Deque<String>> queryParameters() {
        return exchange.getQueryParameters();
    }

    public String queryParameter(String key) {
        Deque<String> params = exchange.getQueryParameters().get(key);
        return params.isEmpty() ? null : params.getFirst();
    }

    public Property queryParameterVal(String key) {
        return new Property(queryParameter(key));
    }

    public Deque<String> queryParameters(String key) {
        return exchange.getQueryParameters().get(key);
    }

    public Cookie cookie(String key) {
        return exchange.getRequestCookies().get(key);
    }

    public Map<String, Cookie> cookies() {
        return exchange.getRequestCookies();
    }

    public String protocol() {
        return exchange.getProtocol().toString();
    }

    public String host() {
        return exchange.getHostName();
    }

    public int port() {
        return exchange.getHostPort();
    }

    public String method() {
        return exchange.getRequestMethod().toString();
    }

    public String scheme() {
        return exchange.getRequestScheme();
    }

    public String path() {
        return exchange.getRequestPath();
    }

    public String userAgent() {
        HeaderValues userAgent = exchange.getRequestHeaders().get(Headers.USER_AGENT);
        if (userAgent != null && !userAgent.isEmpty()) {
            return userAgent.getFirst();
        }
        return null;
    }

    public MediaType type() {
        HeaderValues contentType = exchange.getRequestHeaders().get(Headers.CONTENT_TYPE);
        if (contentType != null && !contentType.isEmpty()) {
            return MediaType.valueOf(contentType.getFirst());
        }
        return MediaType.WILDCARD_TYPE;
    }

//    public Response response() {
//        return response;
//    }

    //--------------- Response ---------------
    public RestExchange header(String name, String value) {
        exchange.getResponseHeaders().add(HttpString.tryFromString(name), value);
        return this;
    }

    public RestExchange header(String name, long value) {
        exchange.getResponseHeaders().add(HttpString.tryFromString(name), value);
        return this;
    }

    public RestExchange type(MediaType mediaType) {
        return setResponseMediaType(mediaType);
    }

    public RestExchange type(String mediaType) {
        return setResponseMediaType(MediaType.valueOf(mediaType));
    }

    public RestExchange status(int status) {
        exchange.setStatusCode(status);
        return this;
    }

    public RestExchange send(Object response) {
        this.response(response);
        return this;
    }

    public void send(Object response, String mediaType) {
        send(response, MediaType.valueOf(mediaType));
    }

    public void send(Object response, MediaType mediaType) {
        type(mediaType);
        this.response(response);
    }

    private RestExchange setResponseMediaType(MediaType mediaType) {
        responseContentType = mediaType;
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, mediaType.toString());
        return this;
    }

    private void response(Object response) {
        try {

            Parser responseParser = Parsers.getParser(responseContentType);
            if (responseParser == null) {
                throw new RuntimeException("Could not find Parser for type " + responseContentType.toString());
            }
            exchange.getResponseSender().send(responseParser.writeValue(response));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
