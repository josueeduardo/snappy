package io.joshworks.snappy.extensions.dashboard.resource;

import io.joshworks.snappy.rest.RestExchange;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Josh Gontijo on 7/9/17.
 */
public class ResourcesMetricResource {


    private final ResourceMetricHolder control;

    public ResourcesMetricResource(ResourceMetricHolder registeredMetricHandlers) {
        this.control = registeredMetricHandlers;
    }

    public void getMetrics(RestExchange exchange) {
        exchange.send(control.getMetrics());
    }

    public void getMetric(RestExchange exchange) {
        String id = exchange.pathParameter("id");
        List<RestMetric> foundById = control.getMetrics().stream()
                .filter(rm -> id.equals(rm.id))
                .collect(Collectors.toList());

        if (foundById.isEmpty()) {
            exchange.status(404);
        } else {
            exchange.send(foundById.get(0));

        }
    }
}
