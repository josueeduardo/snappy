package io.joshworks.microserver;

import io.undertow.server.HttpHandler;

/**
 * Created by josh on 3/7/17.
 */
public class MappedEndpoint {

    public final String method; //HTTP methods or WS etc
    public final String url;
    public final Type type;
    public final HttpHandler handler;

//    public MappedEndpoint(String method, String url, HttpHandler handler, MappedEndpoint child) {
//        this.method = method;
//        this.url = url;
//        this.type = Type.GROUP;
//        this.handler = handler;
//        this.child = child;
//    }

    public MappedEndpoint(String method, String url, Type type, HttpHandler handler) {
        this.method = method;
        this.url = url;
        this.type = type;
        this.handler = handler;
    }

    public enum Type {
        REST, WS, STATIC, SSE, GROUP
    }
}
