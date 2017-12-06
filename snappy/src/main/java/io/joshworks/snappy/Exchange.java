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

package io.joshworks.snappy;

import io.joshworks.snappy.http.DefaultIoCallback;
import io.joshworks.snappy.http.MediaType;
import io.joshworks.snappy.http.Parameter;
import io.joshworks.snappy.parser.Parser;
import io.joshworks.snappy.parser.Parsers;
import io.undertow.io.IoCallback;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.util.HeaderMap;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import io.undertow.util.PathTemplateMatch;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Josh Gontijo on 3/19/17.
 */
public class Exchange {

    protected final HttpServerExchange exchange;
    protected MediaType responseContentType = MediaType.TEXT_PLAIN_TYPE;


    public Exchange(HttpServerExchange exchange) {
        this.exchange = exchange;
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

    public Map<String, String> pathParameters() {
        PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
        Map<String, String> parameters = new HashMap<>();
        for (String key : pathMatch.getParameters().keySet()) {
            parameters.put(key, pathMatch.getParameters().get(key));
        }
        return parameters;

    }

    public String pathParameter(String key) {
        PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
        return pathMatch.getParameters().get(key);
    }

    public Parameter pathParameterVal(String key) {
        return new Parameter(pathParameter(key));
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

    //--------------- Response ---------------
    public Exchange header(String name, String value) {
        exchange.getResponseHeaders().put(HttpString.tryFromString(name), value);
        return this;
    }

    public Exchange header(String name, long value) {
        exchange.getResponseHeaders().put(HttpString.tryFromString(name), value);
        return this;
    }

    public Exchange cookie(Cookie cookie) {
        if (cookie != null) {
            exchange.getResponseCookies().put(cookie.getName(), cookie);
        }
        return this;
    }

    /**
     * Overrides any previous set Content-Type header value
     *
     * @param mediaType to be used in the response
     * @return The exchange
     */
    public Exchange type(MediaType mediaType) {
        return setResponseMediaType(mediaType);
    }

    public Exchange type(String mediaType) {
        return setResponseMediaType(MediaType.valueOf(mediaType));
    }

    public Exchange status(int status) {
        exchange.setStatusCode(status);
        return this;
    }

    public void send(Object response) {
        this.response(response);
    }

    public void send(Object response, String mediaType) {
        send(response, MediaType.valueOf(mediaType));
    }

    public void send(Object response, MediaType mediaType) {
        type(mediaType);
        this.response(response);
    }

    public void seeOther(URI uri) {
        Objects.requireNonNull(uri, "URI cannot be null");
        status(303);
        header(Headers.LOCATION_STRING, uri.toString());
        end();
    }

    public void temporaryRedirect(URI uri) {
        Objects.requireNonNull(uri, "URI cannot be null");
        status(307);
        header(Headers.LOCATION_STRING, uri.toString());
        end();
    }

    public void sendFile(File file) {
        sendFile(file, new DefaultIoCallback());
    }

    public void sendFile(File file, IoCallback callback) {
        if (file == null || !file.exists()) {
            throw new RuntimeException("Invalid file");
        }
        try {
            String fileExtension = getFileExtension(file.getName());
            MediaType mimeForFile = MediaType.getMimeForFile(fileExtension);
            setResponseMediaType(mimeForFile);

            HeaderValues contentDisposition = exchange.getResponseHeaders().get(Headers.CONTENT_DISPOSITION);
            if (contentDisposition == null || contentDisposition.isEmpty()) {
                exchange.getResponseHeaders().add(Headers.CONTENT_DISPOSITION, "filename=" + file.getName());
            }
            exchange.getResponseHeaders().add(Headers.CONTENT_LENGTH, file.length());

            exchange.getResponseSender().transferFrom(FileChannel.open(file.toPath()), callback);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Transfers blocking the bytes from a given InputStream to this response. Using application/octet-stream
     *
     * @param inputStream The data to be sent
     */
    public void stream(InputStream inputStream) {
        stream(inputStream, MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }

    /**
     * Transfers blocking the bytes from a given InputStream to this response
     *
     * @param inputStream The data to be sent
     * @param mediaType   The stream Content-Type
     */
    public void stream(InputStream inputStream, MediaType mediaType) {
        try {
            OutputStream outputStream = exchange.getOutputStream();
            setResponseMediaType(mediaType);

            byte[] buffer = new byte[10240];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Error transferring data", ex);
        }
    }

    public void end() {
        exchange.endExchange();
    }

    private String getFileExtension(String fileName) {
        String extension = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1);
        }
        return extension;

    }

    protected Exchange setResponseMediaType(MediaType mediaType) {
        responseContentType = mediaType;
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, mediaType.toString());
        return this;
    }

    private void response(Object response) {
        if (response == null) {
            return;
        }
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
