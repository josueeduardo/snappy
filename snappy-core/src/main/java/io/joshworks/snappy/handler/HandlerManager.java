package io.joshworks.snappy.handler;

import io.joshworks.snappy.metric.MetricData;
import io.joshworks.snappy.metric.MetricsHandler;
import io.joshworks.snappy.metric.RestMetricHandler;
import io.joshworks.snappy.rest.ExceptionMapper;
import io.joshworks.snappy.rest.MediaType;
import io.undertow.Handlers;
import io.undertow.predicate.Predicate;
import io.undertow.predicate.Predicates;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.PathTemplateHandler;
import io.undertow.server.handlers.PredicateHandler;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by josh on 3/11/17.
 */
public class HandlerManager {


    //chain of responsibility
    public HttpHandler resolveHandlers(List<MappedEndpoint> mappedEndpoints, String basePath, boolean httpMetrics, boolean httpTracer) {

        final List<RestMetricHandler> metricsHandlers = new ArrayList<>();
        final RoutingHandler routingRestHandler = new TrailingSlashRoutingHandler();
        final PathTemplateHandler websocketHandler = Handlers.pathTemplate();
        HttpHandler staticHandler = null;

        for (MappedEndpoint me : mappedEndpoints) {

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

        registerMetrics(routingRestHandler, mappedEndpoints, metricsHandlers);


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
            String[] mappedServices = cleanedUrls(mappedEndpoints);
            Predicate mappedPredicate = Predicates.prefixes(mappedServices);
            return Handlers.predicate(mappedPredicate, websocketRestResolved, file);
        }
        return websocketRestResolved;
    }

    //best effort to resolve url that may be unique
    private String[] cleanedUrls(List<MappedEndpoint> mappedEndpoints) {

        return mappedEndpoints.stream()
                .filter(me -> !me.type.equals(MappedEndpoint.Type.STATIC))
                .map(me -> {
                    int idx = me.url.indexOf("/{");
                    return idx >= 0 ? me.url.substring(0, idx) : me.url;
                })
                .distinct().toArray(String[]::new);
    }


    private void registerMetrics(
            RoutingHandler routingRestHandler,
            List<MappedEndpoint> mappedEndpoints,
            List<RestMetricHandler> metricsHandlers) {

        ExceptionMapper internalExceptionMapper = new ExceptionMapper();

        MappedEndpoint getMetrics = HandlerUtil.rest(Methods.GET, "/metrics", (exchange) -> exchange.send(
                new MetricData(metricsHandlers), MediaType.APPLICATION_JSON_TYPE), internalExceptionMapper, new ArrayList<>());

        MappedEndpoint clearMetrics = HandlerUtil.rest(Methods.DELETE, "/metrics", (exchange) -> {
            metricsHandlers.forEach(MetricsHandler::reset);
            exchange.status(StatusCodes.NO_CONTENT);
        }, internalExceptionMapper, new ArrayList<>());

        routingRestHandler.add(getMetrics.method, getMetrics.url, getMetrics.handler);
        routingRestHandler.add(clearMetrics.method, clearMetrics.url, clearMetrics.handler);

        mappedEndpoints.add(getMetrics);
        mappedEndpoints.add(clearMetrics);

    }


}
