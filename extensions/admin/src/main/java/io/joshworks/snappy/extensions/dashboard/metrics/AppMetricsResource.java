package io.joshworks.snappy.extensions.dashboard.metrics;

import io.joshworks.snappy.rest.MediaType;
import io.joshworks.snappy.rest.RestExchange;

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
        Map<String, Object> map = exchange.body().asJsonMap();
        boolean enabled = Boolean.parseBoolean(String.valueOf(map.get("enabled")));
        Metrics.setEnable(enabled);
        exchange.status(204);
    }

}
