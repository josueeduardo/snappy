package com.josue.simpletow;

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


    public RestExchange(HttpServerExchange httpServerExchange) {
        this.httpServerExchange = httpServerExchange;
    }

    public InputStream bodyStream() {
        return read();
    }

    public <T> T body(Class<T> type) {
        InputStream inputStream = read();
        return getRequestParser().read(inputStream, type);
    }

    public void send(Object response) {
        Parser responseParser = getResponseParser();
        httpServerExchange.getResponseHeaders().put(Headers.CONTENT_TYPE, responseParser.mediaType());
        httpServerExchange.setStatusCode(StatusCodes.OK);
        httpServerExchange.getResponseSender().send(responseParser.write(response));
    }

    public RestExchange status(int status) {
        httpServerExchange.setStatusCode(status);
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

    private Parser getResponseParser(){
        return Parsers.getParser(httpServerExchange.getRequestHeaders().get(Headers.ACCEPT));
    }

    private Parser getRequestParser(){
        return Parsers.getParser(httpServerExchange.getRequestHeaders().get(Headers.CONTENT_TYPE));
    }


}
