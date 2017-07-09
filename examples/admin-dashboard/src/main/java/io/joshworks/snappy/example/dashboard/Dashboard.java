package io.joshworks.snappy.example.dashboard;

import io.joshworks.restclient.http.SimpleClient;
import io.joshworks.snappy.executor.AppExecutors;
import io.joshworks.snappy.extensions.dashboard.AdminExtension;
import io.joshworks.snappy.extensions.dashboard.metrics.Metrics;

import java.io.File;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static io.joshworks.snappy.SnappyServer.*;

/**
 * Created by Josh Gontijo on 4/27/17.
 */
public class Dashboard {

    private static final String LOG_PATH = System.getProperty("user.home") + File.separator + "sample.log";

    private static int count = 0;
    private static boolean toggle = false;

    public static void main(String[] args) throws Exception {
        register(new AdminExtension());

        get("/test-endpoint", exc -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int val = ThreadLocalRandom.current().nextInt(1, 100);
            if (val % 2 == 0) {
                exc.status(200);
            } else {
                exc.status(500);
            }
        });

        onStart(() -> {
            AppExecutors.scheduleAtFixedRate(() -> {
                SimpleClient.get("http://localhost:9000/test-endpoint").asString();
            }, 1,1, TimeUnit.SECONDS);
        });

        cors();
        start(); //http://localhost:9100/

        //After start
        Metrics.supplyNumber("counter", 10000, () -> count++);
        Metrics.supplyBoolean("toggle", 10000, () -> toggle = !toggle);



    }

}
