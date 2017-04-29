package io.joshworks.snappy.example.dashboard;

import io.joshworks.snappy.extensions.dashboard.DashboardExtension;

import static io.joshworks.snappy.SnappyServer.*;

/**
 * Created by Josh Gontijo on 4/27/17.
 */
public class Dashboard {

    public static void main(String[] args) {
        register(new DashboardExtension());
        start();
    }
}
