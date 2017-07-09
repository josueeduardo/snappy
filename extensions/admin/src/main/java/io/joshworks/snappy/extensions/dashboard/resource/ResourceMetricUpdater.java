package io.joshworks.snappy.extensions.dashboard.resource;

import io.joshworks.snappy.extensions.dashboard.AdminExtension;
import io.joshworks.snappy.extensions.dashboard.TimeMetric;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by Josh Gontijo on 7/9/17.
 */
public class ResourceMetricUpdater {

    private static final long UPDATE_FREQUENCY = Duration.ofMinutes(1).toMillis();
    private static final long DATA_INTERVAL = Duration.ofMinutes(1).toMillis();
    private final List<ResourceMetricItem> registeredEndpoints = new ArrayList<>();

    public void add(String method, String url, RestMetricsHandler handler) {
        registeredEndpoints.add(new ResourceMetricItem(method, url, handler::getMetrics));
    }

    public void start() {
        if(registeredEndpoints.isEmpty()) {
            return;
        }
        AdminExtension.scheduler.scheduleAtFixedRate(() -> {
            registeredEndpoints.forEach(rm -> rm.metrics.update());
        }, UPDATE_FREQUENCY, UPDATE_FREQUENCY, TimeUnit.MILLISECONDS);
    }

    public List<RestMetric> getMetrics() {
        return registeredEndpoints.stream()
                .map(re -> new RestMetric(re.id, re.url, re.method, re.metrics.getData()))
                .collect(Collectors.toList());

    }

    public static class ResourceMetricItem {
        public final String id;
        private final String method;
        private final String url;
        private final TimeMetric<RestMetricsHandler.MetricResult> metrics;

        public ResourceMetricItem(String method, String url, Supplier<RestMetricsHandler.MetricResult> supplier) {
            this.id = UUID.randomUUID().toString().substring(0, 8);
            this.method = method;
            this.url = url;
            this.metrics = new TimeMetric<>(id, DATA_INTERVAL, supplier);
        }
    }

}
