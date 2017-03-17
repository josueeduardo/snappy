package io.joshworks.snappy.app;

import static io.joshworks.snappy.SnappyServer.get;
import static io.joshworks.snappy.SnappyServer.start;
import static io.joshworks.snappy.metric.Metrics.addMetric;


/**
 * Created by josh on 3/10/17.
 */
public class Main {

    public static void main(String[] args) {
        get("/echo/{ex}", (exchange) -> {
            String number = exchange.parameters("ex");
            if (Boolean.parseBoolean(number)) {
                throw new RuntimeException("Yolo");
            }
            exchange.send("{}");
            addMetric("Yolo", 1);
        });

        start();
    }
}
