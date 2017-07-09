package io.joshworks.snappy.extensions.dashboard.resource;

import io.joshworks.snappy.extensions.dashboard.TimeMetric;

import java.time.Duration;
import java.util.Collection;

/**
 * Created by Josh Gontijo on 4/29/17.
 */
public class RestMetric {

    private static final long UPDATE_FREQUENCY = Duration.ofMinutes(1).toMillis();

    public final String id;
    public final String url;
    public final String method;
    public final Collection<TimeMetric.Serie> metrics;

    public RestMetric(String id, String url, String method, Collection<TimeMetric.Serie> metrics) {
        this.id = id;
        this.url = url;
        this.method = method;
        this.metrics = metrics;
    }
}
