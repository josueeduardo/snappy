package io.joshworks.snappy.extensions.dashboard.metrics;

import io.joshworks.snappy.http.MediaType;
import io.joshworks.snappy.http.Request;
import io.joshworks.snappy.http.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Josh Gontijo on 7/8/17.
 */
public class AppMetricsResource {

    public Response getMetrics(Request request) {
        return Response.withBody(Metrics.getData()).type(MediaType.APPLICATION_JSON_TYPE);
    }

    public Response getMetric(Request exchange) {
        String id = exchange.pathParameter("id");
        return Response.withBody(Metrics.getData(id));
    }

    public Response updateMetricState(Request exchange) {
        Map<String, Object> map = exchange.body().asMap();
        Object enabled = map.get("enabled");
        if (enabled != null) {
            boolean metricsEnabled = Boolean.parseBoolean(String.valueOf(enabled));
            Metrics.setEnabled(metricsEnabled);
        }
        return Response.withStatus(204);
    }

    public Response metricStatus(Request exchange) {
        Map<String, Object> response = new HashMap<>();
        response.put("enabled", Metrics.isEnabled());
        return Response.withBody(response);
    }

}
