package io.joshworks.snappy.handler;

import io.joshworks.snappy.admin.AdminManager;
import io.joshworks.snappy.metric.RestMetricHandler;
import io.undertow.Handlers;
import io.undertow.predicate.Predicate;
import io.undertow.predicate.Predicates;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.PathTemplateHandler;
import io.undertow.server.handlers.PredicateHandler;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by josh on 3/11/17.
 */
public class HandlerManager {


    //chain of responsibility
    public HttpHandler createRootHandler(
            List<MappedEndpoint> mappedEndpoints,
            AdminManager adminManager,
            String basePath,
            boolean httpMetrics,
            boolean httpTracer) {

        final List<RestMetricHandler> metricsHandlers = new ArrayList<>();
        final RoutingHandler routingRestHandler = new TrailingSlashRoutingHandler();
        final PathTemplateHandler websocketHandler = Handlers.pathTemplate();
        HttpHandler staticHandler = null;

        for (MappedEndpoint me : mappedEndpoints) {

            //TODO clean this up
            //TODO at the moment only rest has metrics
            if (MappedEndpoint.Type.REST.equals(me.type)) {
                String endpointPath = HandlerUtil.BASE_PATH.equals(basePath) ? me.url : basePath + me.url;
                if (httpMetrics) {
                    RestMetricHandler restMetricHandler = new RestMetricHandler(me.method, endpointPath, me.handler);
                    metricsHandlers.add(restMetricHandler);
                    routingRestHandler.add(me.method, endpointPath, restMetricHandler);
                } else {
                    routingRestHandler.add(me.method, endpointPath, me.handler);
                }
            }
            if (MappedEndpoint.Type.MULTIPART.equals(me.type)) {
                routingRestHandler.add(me.method, me.url, me.handler);
            }
            if (MappedEndpoint.Type.SSE.equals(me.type)) {
                routingRestHandler.add(me.method, me.url, me.handler);
            }
            if (MappedEndpoint.Type.WS.equals(me.type)) {
                websocketHandler.add(me.url, me.handler);
            }
            if (MappedEndpoint.Type.STATIC.equals(me.type)) {
                staticHandler = me.handler;
            }
        }

        adminManager.registerMetrics(metricsHandlers);

        HttpHandler root = resolveHandlers(routingRestHandler, websocketHandler, staticHandler, mappedEndpoints);
        HttpHandler handler = httpTracer ? Handlers.requestDump(root) : root;

        return Handlers.gracefulShutdown(handler);
    }


    private HttpHandler resolveHandlers(HttpHandler rest, HttpHandler ws, HttpHandler file, List<MappedEndpoint> mappedEndpoints) {

        PredicateHandler websocketRestResolved = Handlers.predicate(value -> {
            HeaderValues upgradeHeader = value.getRequestHeaders().get(Headers.UPGRADE);
            return upgradeHeader != null && upgradeHeader.stream().anyMatch(v -> v.equalsIgnoreCase("websocket"));
        }, ws, rest);

        if (file != null) {
            String[] mappedServices = HandlerUtil.removedPathTemplate(mappedEndpoints);
            Predicate mappedPredicate = Predicates.prefixes(mappedServices);
            return Handlers.predicate(mappedPredicate, websocketRestResolved, file);
        }
        return websocketRestResolved;
    }


}
