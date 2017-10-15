package io.joshworks.snappy.extensions.dashboard.resource;

import io.joshworks.snappy.http.HttpExchange;

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

    public void getMetrics(HttpExchange exchange) {
        exchange.send(resourceMetricHolder.getMetrics());
    }

    public void getMetric(HttpExchange exchange) {
        String id = exchange.pathParameter("id");
        List<RestMetric> foundById = resourceMetricHolder.getMetrics().stream()
                .filter(rm -> id.equals(rm.id))
                .collect(Collectors.toList());

        if (foundById.isEmpty()) {
            exchange.status(404);
        } else {
            exchange.send(foundById.get(0));
        }
    }

    public void updateMetric(HttpExchange exchange) {
        Object enabled = exchange.body().asMap().get("enabled");
        if(enabled != null) {
            boolean metricsEnabled = Boolean.parseBoolean(String.valueOf(enabled));
            resourceMetricHolder.setMetricsEnabled(metricsEnabled);
        }
        exchange.status(204);
    }

    public void metricStatus(HttpExchange exchange) {
        Map<String, Object> response = new HashMap<>();
        response.put("enabled", resourceMetricHolder.isMetricsEnabled());
        exchange.send(response);
    }
}
