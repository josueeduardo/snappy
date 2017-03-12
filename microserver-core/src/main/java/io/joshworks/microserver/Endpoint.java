package io.joshworks.microserver;

import io.joshworks.microserver.handler.HandlerUtil;
import io.joshworks.microserver.rest.RestEndpoint;
import io.joshworks.microserver.websocket.WebsocketEndpoint;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.AbstractReceiveListener;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by josh on 3/10/17.
 */
public class Endpoint {

    static Endpoint root;

    private MappedEndpoint defaultEndpoint;
    private final List<MappedEndpoint> endpoints = new ArrayList<>();

    private final String path;
    private final List<Endpoint> routes = new ArrayList<>();

    private Endpoint(String path) {
        this.path = path;
    }

    public static Endpoint root() {
        return path(HandlerUtil.BASE_PATH);
    }

    public static Endpoint path(String baseUrl) {
        Endpoint endpoint = new Endpoint(baseUrl);
        if (root == null) {
            root = endpoint;
        }
        return endpoint;
    }

    public Endpoint group(Endpoint endpoint) {
        routes.add(endpoint);
        return this;
    }

    public Endpoint group(String url, Consumer<Endpoint> endpoint) {
        Endpoint route = new Endpoint(url);
        endpoint.accept(route);
        routes.add(route);
        return this;
    }

    public Endpoint get(String url, RestEndpoint endpoint) {
        endpoints.add(HandlerUtil.rest(Methods.GET, url, endpoint));
        return this;
    }

    public Endpoint post(String url, RestEndpoint endpoint) {
        endpoints.add(HandlerUtil.rest(Methods.POST, url, endpoint));
        return this;
    }

    public Endpoint put(String url, RestEndpoint endpoint) {
        endpoints.add(HandlerUtil.rest(Methods.PUT, url, endpoint));
        return this;
    }

    public Endpoint delete(String url, RestEndpoint endpoint) {
        endpoints.add(HandlerUtil.rest(Methods.DELETE, url, endpoint));
        return this;
    }

    public Endpoint add(HttpString method, String url, RestEndpoint endpoint) {
        endpoints.add(HandlerUtil.rest(method, url, endpoint));
        return this;
    }

    public Endpoint websocket(String url, AbstractReceiveListener endpoint) {
        endpoints.add(HandlerUtil.websocket(url, endpoint));
        return this;
    }

    public Endpoint websocket(String url, WebSocketConnectionCallback connectionCallback) {
        endpoints.add(HandlerUtil.websocket(url, connectionCallback));
        return this;
    }

    public Endpoint websocket(String url, WebsocketEndpoint websocketEndpoint) {
        endpoints.add(HandlerUtil.websocket(url, websocketEndpoint));
        return this;
    }

    public Endpoint sse(String url) {
        endpoints.add(HandlerUtil.sse(url));
        return this;
    }

    public Endpoint staticFiles(String url, String docPath) {
        endpoints.add(HandlerUtil.staticFiles(url, docPath));
        return this;
    }

    public Endpoint staticFiles(String url) {
        endpoints.add(HandlerUtil.staticFiles(url));
        return this;
    }


    //Default endpoints
    public Endpoint get(RestEndpoint endpoint) {
        defaultEndpoint = HandlerUtil.rest(Methods.GET, HandlerUtil.BASE_PATH, endpoint);
        return this;
    }

    public Endpoint post(RestEndpoint endpoint) {
        defaultEndpoint = HandlerUtil.rest(Methods.POST, HandlerUtil.BASE_PATH, endpoint);
        return this;
    }

    public Endpoint put(RestEndpoint endpoint) {
        defaultEndpoint = HandlerUtil.rest(Methods.PUT, HandlerUtil.BASE_PATH, endpoint);
        return this;
    }

    public Endpoint delete(RestEndpoint endpoint) {
        defaultEndpoint = HandlerUtil.rest(Methods.DELETE, HandlerUtil.BASE_PATH, endpoint);
        return this;
    }

    public Endpoint add(HttpString method, RestEndpoint endpoint) {
        defaultEndpoint = HandlerUtil.rest(method, HandlerUtil.BASE_PATH, endpoint);
        return this;
    }

    public Endpoint websocket(AbstractReceiveListener endpoint) {
        defaultEndpoint = HandlerUtil.websocket(HandlerUtil.BASE_PATH, endpoint);
        return this;
    }

    public Endpoint websocket(WebSocketConnectionCallback connectionCallback) {
        defaultEndpoint = HandlerUtil.websocket(HandlerUtil.BASE_PATH, connectionCallback);
        return this;
    }

    public Endpoint websocket(WebsocketEndpoint websocketEndpoint) {
        defaultEndpoint = HandlerUtil.websocket(path, websocketEndpoint);
        return this;
    }


    public List<MappedEndpoint> getEndpoints() {
        return endpoints;
    }

    public String getPath() {
        return path;
    }

    public List<Endpoint> getRoutes() {
        return routes;
    }

    public MappedEndpoint getDefaultEndpoint() {
        return defaultEndpoint;
    }

    @Override
    public String toString() {
        return "{" +
                "defaultEndpoint=" + defaultEndpoint +
                ", endpoints=" + endpoints +
                ", path='" + path + '\'' +
                ", routes=" + routes +
                '}';
    }
}
