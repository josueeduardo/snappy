package io.joshworks.microserver.handler;

import io.joshworks.microserver.Endpoint;
import io.joshworks.microserver.MappedEndpoint;
import io.joshworks.microserver.metric.MetricData;
import io.joshworks.microserver.metric.MetricManager;
import io.joshworks.microserver.metric.RestMetricHandler;
import io.undertow.Handlers;
import io.undertow.predicate.Predicate;
import io.undertow.predicate.Predicates;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.PathTemplateHandler;
import io.undertow.server.handlers.PredicateHandler;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by josh on 3/11/17.
 */
public class HandlerManager {

    public static final List<RestMetricHandler> metricsHandlers = new ArrayList<>();

    public static HttpHandler resolveHandlers(List<MappedEndpoint> mappedEndpoints, boolean httpMetrics, boolean httpTracer) {
        registerMetrics();

        final RoutingHandler routingRestHandler = Handlers.routing();
        final PathTemplateHandler websocketHandler = Handlers.pathTemplate();
        HttpHandler staticHandler = null;

        for (MappedEndpoint me : mappedEndpoints) {
            if (MappedEndpoint.Type.REST.equals(me.type)) {
                if (httpMetrics) {
                    RestMetricHandler restMetricHandler = new RestMetricHandler(me.method, me.url, me.handler);
                    metricsHandlers.add(restMetricHandler);
                    routingRestHandler.add(me.method, me.url, restMetricHandler);
                } else {
                    routingRestHandler.add(me.method, me.url, me.handler);
                }
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


        HttpHandler root = resolveHandlers(routingRestHandler, websocketHandler, staticHandler, mappedEndpoints);
        return httpTracer ? Handlers.requestDump(root) : root;
    }


    private static HttpHandler resolveHandlers(HttpHandler rest, HttpHandler ws, HttpHandler file, List<MappedEndpoint> mappedEndpoints) {

        PredicateHandler websocketRestResolved = Handlers.predicate(value -> {
            HeaderValues upgradeHeader = value.getRequestHeaders().get(Headers.UPGRADE);
            return upgradeHeader != null && upgradeHeader.stream().anyMatch(v -> v.equalsIgnoreCase("websocket"));
        }, ws, rest);

        if (file != null) {
            String[] mappedServices = cleanedUrls(mappedEndpoints);
            Predicate mappedPredicate = Predicates.prefixes(mappedServices);
            return Handlers.predicate(mappedPredicate, websocketRestResolved, file);
        }
        return websocketRestResolved;
    }

    //best effort to resolve url that may be unique
    private static String[] cleanedUrls(List<MappedEndpoint> mappedEndpoints) {

        return mappedEndpoints.stream()
                .filter(me -> !me.type.equals(MappedEndpoint.Type.STATIC))
                .map(me -> {
                    int idx = me.url.indexOf("/{");
                    return idx >= 0 ? me.url.substring(0, idx) : me.url;
                })
                .distinct().toArray(String[]::new);
    }


    private static void registerMetrics() {
        Endpoint.get("/metrics", (exchange) -> exchange.send(
                new MetricData(), "application/json"));

        Endpoint.delete("/metrics", (exchange) -> {
            MetricManager.clear();
            exchange.status(StatusCodes.NO_CONTENT);
        });
    }


}
