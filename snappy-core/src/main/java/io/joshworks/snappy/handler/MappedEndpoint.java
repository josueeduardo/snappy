package io.joshworks.snappy.handler;

import io.undertow.server.HttpHandler;

/**
 * Created by josh on 3/7/17.
 */
public class MappedEndpoint {

    public final String method; //HTTP methods or WS etc
    public final String url;
    public final Type type;
    public final HttpHandler handler;

    public MappedEndpoint(String method, String url, Type type, HttpHandler handler) {
        this.method = method;
        this.url = url;
        this.type = type;
        this.handler = handler;
    }

    @Override
    public String toString() {
        return "{" +
                "method='" + method + '\'' +
                ", url='" + url + '\'' +
                ", type=" + type +
                ", handler=" + handler +
                '}';
    }

    public enum Type {
        REST, WS, STATIC, SSE
    }
}
