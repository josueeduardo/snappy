package com.josue.simpletow.rest;

import com.josue.simpletow.parser.Parser;
import com.josue.simpletow.parser.Parsers;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;
import io.undertow.util.StatusCodes;

import java.io.InputStream;
import java.util.Deque;

/**
 * Created by josh on 3/5/17.
 */
public class RestExchange {

    private static final String APPLICATION_JSON = "application/json";
    public final HttpServerExchange httpServerExchange;
    private int status = -1;

    public RestExchange(HttpServerExchange httpServerExchange) {
        this.httpServerExchange = httpServerExchange;
    }

    public InputStream bodyStream() {
        return read();
    }

    public <T> T body(Class<T> type) {
        InputStream inputStream = read();
        return getReadParserForContentType().read(inputStream, type);
    }

    public <T> T body(Class<T> type, String contentType) {
        InputStream inputStream = read();
        return getReadParser(contentType).read(inputStream, type);
    }

    public void send(Object response) {
        this.response(response, APPLICATION_JSON);
    }

    public void send(Object response, String contentType) {
        this.response(response, contentType);
    }

    private void response(Object response, String contentType) {
        Parser responseParser = getWriteParser(contentType);
        httpServerExchange.getResponseHeaders().put(Headers.CONTENT_TYPE, contentType);
        httpServerExchange.setStatusCode(status > 0 ? status : StatusCodes.OK);
        httpServerExchange.getResponseSender().send(responseParser.write(response));
    }

    public RestExchange status(int status) {
        this.status = status;
        return this;
    }

    public String pathParameter(String key) {
        PathTemplateMatch pathMatch = httpServerExchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
        return pathMatch.getParameters().get(key);
    }

    public String queryParameter(String key) {
        Deque<String> parameters = httpServerExchange.getQueryParameters().get(key);
        return parameters.getFirst();
    }

    public Deque<String> queryParameters(String key) {
        return httpServerExchange.getQueryParameters().get(key);
    }

    private InputStream read() {
        httpServerExchange.startBlocking();
        return httpServerExchange.getInputStream();
    }

    private Parser getWriteParser(String contentType) {
        //TODO investigate content negotiation
//        return Parsers.find(httpServerExchange.getRequestHeaders().get(Headers.ACCEPT));
        return Parsers.getParser(contentType);
    }

    private Parser getReadParserForContentType() {
        return Parsers.find(httpServerExchange.getRequestHeaders().get(Headers.CONTENT_TYPE));
    }

    private Parser getReadParser(String contentType) {
        return Parsers.getParser(contentType);
    }


}
