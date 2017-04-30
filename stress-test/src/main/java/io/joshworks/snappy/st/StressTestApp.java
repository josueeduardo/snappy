package io.joshworks.snappy.st;

import java.util.Map;

import static io.joshworks.snappy.SnappyServer.get;
import static io.joshworks.snappy.SnappyServer.post;
import static io.joshworks.snappy.SnappyServer.start;

/**
 * Created by Josh Gontijo on 4/30/17.
 */
public class StressTestApp {


    public static void main(String[] args) {
        get("/a", exchange -> exchange.send(new Payload()));

        post("/b", exchange -> {
            Payload payload = exchange.body().asObject(Payload.class);
        });

        post("/c", exchange -> {
            Map<String, Object> map = exchange.body().asJsonMap();
        });

        get("/ex", exchange -> {
            throw new RuntimeException("Dummy Error");
        });

        start();
    }

}
