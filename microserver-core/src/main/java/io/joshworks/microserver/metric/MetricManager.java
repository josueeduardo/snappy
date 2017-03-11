package io.joshworks.microserver.metric;

import io.joshworks.microserver.handler.HandlerManager;
import io.undertow.server.handlers.MetricsHandler;

/**
 * Created by josh on 3/11/17.
 */
public class MetricManager {

    public static void clear() {
        HandlerManager.metricsHandlers.forEach(MetricsHandler::reset);
//        Metrics.getData() //TODO clear user defined metrics, how ? key ? val ?
    }

}
