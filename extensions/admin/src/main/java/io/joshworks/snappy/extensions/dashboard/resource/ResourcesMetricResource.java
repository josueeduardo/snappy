package io.joshworks.snappy.extensions.dashboard.resource;


import io.joshworks.snappy.http.Request;
import io.joshworks.snappy.http.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Josh Gontijo on 7/9/17.
 */
public class ResourcesMetricResource {

    private final ResourceMetricHolder resourceMetricHolder;

    public ResourcesMetricResource(ResourceMetricHolder registeredMetricHandlers) {
        this.resourceMetricHolder = registeredMetricHandlers;
    }

    public Response getMetrics(Request exchange) {
        return Response.withBody(resourceMetricHolder.getMetrics());
    }

    public Response getMetric(Request exchange) {
        String id = exchange.pathParameter("id");
        List<RestMetric> foundById = resourceMetricHolder.getMetrics().stream()
                .filter(rm -> id.equals(rm.id))
                .collect(Collectors.toList());

        return foundById.isEmpty() ? Response.notFound() : Response.withBody(foundById.get(0));
    }

    public Response updateMetric(Request exchange) {
        Object enabled = exchange.body().asMap().get("enabled");
        if (enabled != null) {
            boolean metricsEnabled = Boolean.parseBoolean(String.valueOf(enabled));
            resourceMetricHolder.setMetricsEnabled(metricsEnabled);
        }
        return Response.noContent();
    }

    public Response metricStatus(Request exchange) {
        Map<String, Object> response = new HashMap<>();
        response.put("enabled", resourceMetricHolder.isMetricsEnabled());
        return Response.withBody(response);
    }
}
