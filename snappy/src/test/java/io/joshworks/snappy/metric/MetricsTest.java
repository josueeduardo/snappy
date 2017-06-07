package io.joshworks.snappy.metric;

import io.joshworks.snappy.SnappyServer;
import org.junit.Test;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Created by Josh Gontijo on 5/14/17.
 */
public class MetricsTest {

    @Test
    public void set() throws Exception {
        String metricName = rand();
        int value = 1;

        Metrics.set(metricName, value);
        Map<String, Object> data = Metrics.getData();
        assertEquals(value, data.get(metricName));
    }

    @Test
    public void add() throws Exception {
        String metricName = rand();
        long value = 1;

        Metrics.add(metricName, value);
        Map<String, Object> data = Metrics.getData();
        assertEquals(value, data.get(metricName));

        Metrics.add(metricName, value);
        data = Metrics.getData();
        assertEquals(value + 1, data.get(metricName));
    }

    @Test(expected = NullPointerException.class)
    public void supply_null() throws Exception {
        String metricName = rand();
        Metrics.supply(metricName, null);
    }

    @Test
    public void supply() throws Exception {
        String metricName = rand();
        String value = "A";

        Metrics.supply(metricName, () -> value);

        assertEquals(value, Metrics.getData().get(metricName));
    }

    @Test
    public void timedSupply() throws Exception {
        try {
            int samples = 5;

            String value = "A";

            final CountDownLatch latch = new CountDownLatch(samples);
            SnappyServer.start();
            String metricName = rand();

            Metrics.supply(metricName, () -> {
                latch.countDown();
                return value;
            }, 1);

            //metrics sampler runs every second, may require tweaking if the frequency changes
            if(!latch.await(samples * 2, TimeUnit.SECONDS)) {
                fail("Failed to wait for metrics data");
            }

            Object timed = Metrics.getData().get(metricName);
            assertNotNull(timed);

            Collection<Metrics.Serie> serie = (Collection<Metrics.Serie>)timed;
            //countdown is called before
            assertEquals(samples, serie.size());
            serie.forEach(v -> assertEquals(value, v.data));


        }finally {
            SnappyServer.stop();
        }



    }


    private String rand() {
        return UUID.randomUUID().toString();
    }

}