package io.joshworks.snappy.extensions.dashboard.metrics;

import io.joshworks.snappy.rest.MediaType;
import io.joshworks.snappy.rest.RestExchange;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Josh Gontijo on 7/8/17.
 */
public class AppMetricsResource {

    public void getMetrics(RestExchange exchange) {
        exchange.send(Metrics.getData(), MediaType.APPLICATION_JSON_TYPE);
    }

    public void getMetric(RestExchange exchange) {
        String id = exchange.pathParameter("id");
        exchange.send(Metrics.getData(id));
    }

    public void updateMetricState(RestExchange exchange) {
        Map<String, Object> map = exchange.body().asMap();
        Object enabled = map.get("enabled");
        if (enabled != null) {
            boolean metricsEnabled = Boolean.parseBoolean(String.valueOf(enabled));
            Metrics.setEnabled(metricsEnabled);
        }
        exchange.status(204);
    }

    public void metricStatus(RestExchange exchange) {
        Map<String, Object> response = new HashMap<>();
        response.put("enabled", Metrics.isEnabled());
        exchange.send(response);
    }

}
