/*
 * Copyright 2017 Josue Gontijo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.joshworks.snappy.http;

import io.joshworks.snappy.http.body.Body;
import io.joshworks.snappy.http.body.MultiPartBody;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.util.HeaderMap;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;

import java.net.InetSocketAddress;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Josh Gontijo on 3/19/17.
 */
public class Request {

    protected final HttpServerExchange exchange;
    private Body body;
    private MultiPartBody multiPartBody;

    public Request(HttpServerExchange exchange) {
        this.exchange = exchange;
    }

    public Body body() {
        if (body == null) {
            this.body = new Body(exchange);
        }
        return body;
    }

    public MultiPartBody multiPartBody() {
        if (multiPartBody == null) {
            this.multiPartBody = new MultiPartBody(exchange);
        }
        return multiPartBody;
    }

    public HeaderMap headers() {
        return exchange.getRequestHeaders();
    }

    public HeaderValues headers(String headerName) {
        return exchange.getRequestHeaders().get(headerName);
    }

    public String header(String headerName) {
        HeaderValues values = headers(headerName);
        return values == null || values.isEmpty() ? null : values.getFirst();
    }

    public int status() {
        return exchange.getStatusCode();
    }

    public Map<String, String> pathParameters() {
        PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
        Map<String, String> parameters = new HashMap<>();
        for (String key : pathMatch.getParameters().keySet()) {
            parameters.put(key, pathMatch.getParameters().get(key));
        }
        return parameters;
    }

    public String pathParameter(String parameterName) {
        PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
        return pathMatch.getParameters().get(parameterName);
    }

    public Parameter pathParameterVal(String parameterName) {
        return new Parameter(pathParameter(parameterName));
    }

    public Map<String, Deque<String>> queryParameters() {
        return exchange.getQueryParameters();
    }

    public String queryParameter(String key) {
        Deque<String> params = exchange.getQueryParameters().get(key);
        return params == null || params.isEmpty() ? null : params.getFirst();
    }

    public Parameter queryParameterVal(String key) {
        return new Parameter(queryParameter(key));
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

    public InetSocketAddress remoteAddress() {
        return exchange.getSourceAddress();
    }

    /**
     * Returns the 'Authorization' header where the value must be present and must contain the 'Basic' prefix
     * The value returned is the stripped header value
     * e.g: Authorization: Basic abc-123
     * Will return abc-123
     *
     * @return The first basic authorization header value without the 'Basic' prefix, null if no header is available,
     * no 'Basic' prefix was found or
     */
    public String basicAuth() {
        return extractAuthorizationValue("Basic", this);
    }

    /**
     * Returns the 'Authorization' header where the value must be present and must contain the 'Basic' prefix
     * The value returned is the stripped header value
     * e.g: Authorization: Basic abc-123
     * Will return abc-123
     *
     * @return The first basic authorization header value without the 'Basic' prefix, null if no header is available,
     * no 'Basic' prefix was found or
     */
    public String bearerAuth() {
        return extractAuthorizationValue("Bearer", this);
    }

    static String extractAuthorizationValue(String type, Request request) {
        HeaderValues values = request.headers(Headers.AUTHORIZATION_STRING);
        if (values == null || values.isEmpty()) {
            return null;
        }

        for (String value : values) {
            String[] parts = value.split(" ");
            if (parts.length == 2 && parts[0].trim().equals(type)) {
                String valTrimmed = parts[1].trim();
                return valTrimmed.isEmpty() ? null : valTrimmed;
            }
        }
        return null;
    }

    public String userAgent() {
        HeaderValues userAgent = exchange.getRequestHeaders().get(Headers.USER_AGENT);
        if (userAgent != null && !userAgent.isEmpty()) {
            return userAgent.getFirst();
        }
        return null;
    }

    public MediaType contentType() {
        HeaderValues contentType = exchange.getRequestHeaders().get(Headers.CONTENT_TYPE);
        if (contentType != null && !contentType.isEmpty()) {
            return MediaType.valueOf(contentType.getFirst());
        }
        return MediaType.WILDCARD_TYPE;
    }
}
