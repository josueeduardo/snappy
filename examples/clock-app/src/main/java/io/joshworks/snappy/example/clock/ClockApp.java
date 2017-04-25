package io.joshworks.snappy.example.clock;

import io.joshworks.snappy.executor.AppExecutors;
import io.joshworks.snappy.sse.SseBroadcaster;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static io.joshworks.snappy.SnappyServer.*;

/**
 * Created by Josh Gontijo on 4/25/17.
 */
public class ClockApp {

    public static void main(final String[] args) {
        staticFiles("/");
        sse("/real-time");


        onStart(() -> AppExecutors.scheduleAtFixedRate(() ->
                SseBroadcaster.broadcast(new Date().toString()), 1, 1, TimeUnit.SECONDS));


        start();
    }
}
