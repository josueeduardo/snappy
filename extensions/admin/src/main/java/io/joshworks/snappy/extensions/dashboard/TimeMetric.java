package io.joshworks.snappy.extensions.dashboard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * Created by Josh Gontijo on 7/9/17.
 */
public class TimeMetric<T> {

    private static final Logger logger = LoggerFactory.getLogger(TimeMetric.class);

    private static final int HISTORY_SIZE = 100;

    private final long frequency;
    private long lastRun = 0;
    final private String name;
    private final Supplier<T> supplier;
    private final RingBuffer<Serie> data = RingBuffer.ofSize(HISTORY_SIZE);

    public TimeMetric(String name, long frequency, Supplier<T> supplier) {
        this.name = name;
        this.supplier = supplier;
        this.frequency = frequency;
    }

    public void update() {
        long now = System.currentTimeMillis();
        if (now - lastRun >= frequency) {
            try {
                T value = supplier.get();
                this.data.add(new Serie(now, value));
            } catch (Exception e) {
                logger.warn("Could not get metrics for {}: {}", name, e.getMessage());
            }

        }
    }

    public RingBuffer<Serie> getData() {
        return data;
    }

    public static class Serie {
        public final long timestamp;
        public final Object data;

        public Serie(long timestamp, Object data) {
            this.timestamp = timestamp;
            this.data = data;
        }
    }

}
