package io.joshworks.snappy.metric;

/**
 * Created by Josh Gontijo on 4/29/17.
 */
public class RestMetrics {
    private final String url;
    private final String method;
    private final RestMetricsHandler.MetricResult metrics;

    RestMetrics(String url, String method, RestMetricsHandler.MetricResult metrics) {
        this.url = url;
        this.method = method;
        this.metrics = metrics;
    }

    public String getUrl() {
        return url;
    }

    public String getMethod() {
        return method;
    }

    public RestMetricsHandler.MetricResult getMetrics() {
        return metrics;
    }
}
