package io.joshworks.microserver.app;

import io.joshworks.microserver.Config;
import io.joshworks.microserver.Microserver;
import io.joshworks.microserver.metric.Metrics;

import static io.joshworks.microserver.Endpoint.root;

/**
 * Created by josh on 3/10/17.
 */
public class Main  {

    public static void main(String[] args) {
        Microserver microserver = new Microserver(new Config().enableTracer().enableHttpMetrics());

        root().get("/echo", (exchange) -> {
            exchange.send("{}");
            Metrics.addMetric("Yolo", 1);
        });

        microserver.start();
    }
}
