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

/**
 * Created by josh on 3/10/17.
 */
public class Endpoint {

    static Endpoint root;

    private final List<MappedEndpoint> mappedEndpoints = new ArrayList<>();

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

    public Endpoint group(String url, Endpoint endpoint) {
        routes.add(endpoint);
        return endpoint;
    }

    public Endpoint get(String url, RestEndpoint endpoint) {
        mappedEndpoints.add(HandlerUtil.rest(Methods.GET, url, endpoint));
        return this;
    }

    public Endpoint post(String url, RestEndpoint endpoint) {
        mappedEndpoints.add(HandlerUtil.rest(Methods.POST, url, endpoint));
        return this;
    }

    public Endpoint put(String url, RestEndpoint endpoint) {
        mappedEndpoints.add(HandlerUtil.rest(Methods.PUT, url, endpoint));
        return this;
    }

    public Endpoint delete(String url, RestEndpoint endpoint) {
        mappedEndpoints.add(HandlerUtil.rest(Methods.DELETE, url, endpoint));
        return this;
    }

    public Endpoint add(HttpString method, String url, RestEndpoint endpoint) {
        mappedEndpoints.add(HandlerUtil.rest(method, url, endpoint));
        return this;
    }

    public Endpoint websocket(String url, AbstractReceiveListener endpoint) {
        mappedEndpoints.add(HandlerUtil.websocket(url, endpoint));
        return this;
    }

    public Endpoint websocket(String url, WebSocketConnectionCallback connectionCallback) {
        mappedEndpoints.add(HandlerUtil.websocket(url, connectionCallback));
        return this;
    }

    public Endpoint websocket(String url, WebsocketEndpoint websocketEndpoint) {
        mappedEndpoints.add(HandlerUtil.websocket(url, websocketEndpoint));
        return this;
    }

    public Endpoint sse(String url) {
        mappedEndpoints.add(HandlerUtil.sse(url));
        return this;
    }

    public Endpoint staticFiles(String url, String docPath) {
        mappedEndpoints.add(HandlerUtil.staticFiles(url, docPath));
        return this;
    }

    public Endpoint staticFiles(String url) {
        mappedEndpoints.add(HandlerUtil.staticFiles(url));
        return this;
    }

    public List<MappedEndpoint> getMappedEndpoints() {
        return mappedEndpoints;
    }

    public String getPath() {
        return path;
    }

    public List<Endpoint> getRoutes() {
        return routes;
    }
}
