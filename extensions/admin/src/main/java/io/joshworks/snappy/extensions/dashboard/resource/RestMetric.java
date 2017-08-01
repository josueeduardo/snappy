package io.joshworks.snappy.extensions.dashboard.resource;

/**
 * Created by Josh Gontijo on 4/29/17.
 */
public class RestMetric {

    public final String id;
    public final String url;
    public final String method;
    public final RestMetricsHandler.MetricResult metrics;

    public RestMetric(String id, String url, String method, RestMetricsHandler.MetricResult metrics) {
        this.id = id;
        this.url = url;
        this.method = method;
        this.metrics = metrics;
    }
}
