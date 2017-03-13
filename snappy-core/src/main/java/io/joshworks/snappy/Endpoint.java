//package io.joshworks.microserver;
//
//import io.joshworks.snappy.handler.HandlerUtil;
//import io.joshworks.snappy.handler.MappedEndpoint;
//import io.joshworks.snappy.rest.RestEndpoint;
//import io.joshworks.snappy.websocket.WebsocketEndpoint;
//import io.undertow.util.HttpString;
//import io.undertow.util.Methods;
//import io.undertow.websockets.WebSocketConnectionCallback;
//import io.undertow.websockets.core.AbstractReceiveListener;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
///**
// * Created by josh on 3/10/17.
// */
//public class Endpoint {
//
//    static volatile Endpoint root;
//
//    final List<MappedEndpoint> endpoints = new ArrayList<>();
//    private final String basePath;
//
//    private Endpoint(String basePath) {
//        this.basePath = basePath;
//    }
//
//    public static Endpoint root() {
//        root = new Endpoint(HandlerUtil.BASE_PATH);
//        return root;
//    }
//
//    public static Endpoint basePath(String baseUrl) {
//        root = new Endpoint(baseUrl);
//        return root;
//    }
//
//    public Endpoint get(String url, RestEndpoint endpoint) {
//        endpoints.add(HandlerUtil.rest(Methods.GET, url, endpoint));
//        return this;
//    }
//
//    public Endpoint post(String url, RestEndpoint endpoint) {
//        endpoints.add(HandlerUtil.rest(Methods.POST, url, endpoint));
//        return this;
//    }
//
//    public Endpoint put(String url, RestEndpoint endpoint) {
//        endpoints.add(HandlerUtil.rest(Methods.PUT, url, endpoint));
//        return this;
//    }
//
//    public Endpoint delete(String url, RestEndpoint endpoint) {
//        endpoints.add(HandlerUtil.rest(Methods.DELETE, url, endpoint));
//        return this;
//    }
//
//    public Endpoint add(HttpString method, String url, RestEndpoint endpoint) {
//        endpoints.add(HandlerUtil.rest(method, url, endpoint));
//        return this;
//    }
//
//    public Endpoint websocket(String url, AbstractReceiveListener endpoint) {
//        endpoints.add(HandlerUtil.websocket(url, endpoint));
//        return this;
//    }
//
//    public Endpoint websocket(String url, WebSocketConnectionCallback connectionCallback) {
//        endpoints.add(HandlerUtil.websocket(url, connectionCallback));
//        return this;
//    }
//
//    public Endpoint websocket(String url, WebsocketEndpoint websocketEndpoint) {
//        endpoints.add(HandlerUtil.websocket(url, websocketEndpoint));
//        return this;
//    }
//
//    public Endpoint sse(String url) {
//        endpoints.add(HandlerUtil.sse(url));
//        return this;
//    }
//
//    public Endpoint staticFiles(String url, String docPath) {
//        endpoints.add(HandlerUtil.staticFiles(url, docPath));
//        return this;
//    }
//
//    public Endpoint staticFiles(String url) {
//        endpoints.add(HandlerUtil.staticFiles(url));
//        return this;
//    }
//
//    public List<MappedEndpoint> getEndpoints() {
//        return Collections.unmodifiableList(endpoints);
//    }
//
//    public String getBasePath() {
//        return basePath;
//    }
//}
