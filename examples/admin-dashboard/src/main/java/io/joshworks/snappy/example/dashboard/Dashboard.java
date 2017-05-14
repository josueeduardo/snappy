package io.joshworks.snappy.example.dashboard;

import io.joshworks.snappy.extensions.dashboard.DashboardExtension;
import io.joshworks.snappy.metric.Metrics;

import java.io.File;
import java.util.Date;

import static io.joshworks.snappy.SnappyServer.*;

/**
 * Created by Josh Gontijo on 4/27/17.
 */
public class Dashboard {

    private static final String LOG_PATH = System.getProperty("user.home") + File.separator + "sample.log";

    public static void main(String[] args) throws Exception {
        register(new DashboardExtension());

        cors();
        start(); //http://localhost:9100/

        //After start
        Metrics.add("metrics-a", 1);
        Metrics.supply("with-supplier", () -> "Lazy metric");
        Metrics.supply("current-time", () -> new Date().toString());

        Metrics.supply("time-based", () -> new Date().toString(), 1);

    }

}
