package io.joshworks.simpletow.metric;

import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.MetricsHandler;

/**
 * Created by josh on 3/9/17.
 */
public class RestMetricHandler extends MetricsHandler {

    private final String url;
    private final String method;

    public RestMetricHandler(String url, String method, HttpHandler next) {
        super(next);
        this.url = url;
        this.method = method;
    }


    public RestMetrics getRestMetrics() {
        return new RestMetrics(url, method, getMetrics());
    }

    public static class RestMetrics {
        private final String url;
        private final String method;
        private final MetricResult metrics;

        public RestMetrics(String url, String method, MetricResult metrics) {
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

        public MetricResult getMetrics() {
            return metrics;
        }
    }

}
