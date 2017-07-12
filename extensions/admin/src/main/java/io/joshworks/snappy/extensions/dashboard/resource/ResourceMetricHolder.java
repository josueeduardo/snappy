package io.joshworks.snappy.extensions.dashboard.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by Josh Gontijo on 7/9/17.
 */
public class ResourceMetricHolder {

    private final List<ResourceMetricItem> registeredEndpoints = new ArrayList<>();

    public void add(String method, String url, RestMetricsHandler handler) {
        registeredEndpoints.add(new ResourceMetricItem(method, url, handler::getMetrics));
    }

    public List<RestMetric> getMetrics() {
        return registeredEndpoints.stream()
                .map(re -> new RestMetric(re.id, re.url, re.method, re.metricsSupplier.get()))
                .collect(Collectors.toList());
    }

    public static class ResourceMetricItem {
        public final String id;
        private final String method;
        private final String url;
        private final Supplier<RestMetricsHandler.MetricResult> metricsSupplier;

        public ResourceMetricItem(String method, String url, Supplier<RestMetricsHandler.MetricResult> metricsSupplier) {
            this.id = UUID.randomUUID().toString().substring(0, 8);
            this.method = method;
            this.url = url;
            this.metricsSupplier = metricsSupplier;
        }
    }

}
