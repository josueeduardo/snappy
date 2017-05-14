/*
 * Copyright 2017 Josue Gontijo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.joshworks.snappy.metric;

import io.joshworks.snappy.executor.AppExecutors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by josh on 3/11/17.
 */
public class Metrics {

    //suppliers
    private static final Map<String, Supplier<Object>> suppliers = new ConcurrentHashMap<>();
    //timedSuppliers hold its data
    private static final Map<String, TimeMetricSupplier> timedSuppliers = new ConcurrentHashMap<>();

    //instant values
    private static final Map<String, Object> metrics = new ConcurrentHashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(Metrics.class);

    private static final int SCHEDULE_TIME = 1; //frequency to run timed metrics, in seconds
    private static final int HISTORY_SIZE = 100;

    private Metrics() {
    }

    public static void set(String key, String num) {
        metrics.put(key, num);
    }

    public static void set(String key, int value) {
        metrics.put(key, value);
    }

    public static void set(String key, long value) {
        metrics.put(key, value);
    }

    public static void set(String key, boolean value) {
        metrics.put(key, value);
    }

    public static void add(String key, long num) {
        metrics.putIfAbsent(key, 0L);
        metrics.computeIfPresent(key, (k, val) -> {
            try {
                if (val instanceof Long || Long.TYPE.equals(val)) {
                    return ((Long) val) + num;
                }
                return val;

            } catch (Exception e) {
                logger.warn("Failed to update (value must be a long) " + key, e);
                return val;
            }
        });
    }

    public static void supply(String key, Supplier<Object> supplier) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        if (suppliers.containsKey(key)) {
            logger.warn("Supply method called multiple times with the same key '" + key + "'");
        }
        suppliers.put(key, supplier);
    }

    public static synchronized void supply(String key, Supplier<Object> supplier, int frequency) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        if (frequency < 0) {
            throw new IllegalArgumentException("frequency must be greater than zero");
        }
        if (timedSuppliers.containsKey(key)) {
            logger.warn("Supply method called multiple times with the same key '" + key + "'");
        }
        if (timedSuppliers.isEmpty()) {
            AppExecutors.scheduleAtFixedRate(() -> {
                timedSuppliers.values().iterator().forEachRemaining(TimeMetricSupplier::update);
            }, SCHEDULE_TIME, SCHEDULE_TIME, TimeUnit.SECONDS);
        }
        timedSuppliers.put(key, new TimeMetricSupplier(frequency, supplier));
    }

    static Map<String, Object> getData() {
        Map<String, Object> data = new HashMap<>();
        data.putAll(metrics);

        Map<String, Object> fromSuppliers = suppliers.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, val -> val.getValue().get()));
        data.putAll(fromSuppliers);

        Map<String, Object> fromTimedSuppliers = timedSuppliers.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, val -> val.getValue().data));

        data.putAll(fromTimedSuppliers);

        return data;
    }


    static class TimeMetricSupplier {

        final int frequency;
        long lastRun = 0;
        final Supplier<Object> supplier;
        final RingBuffer<Serie> data = RingBuffer.ofSize(HISTORY_SIZE);

        TimeMetricSupplier(int frequency, Supplier<Object> supplier) {
            this.supplier = supplier;
            this.frequency = frequency;
        }

        void update() {
            long now = System.currentTimeMillis();
            if (now - lastRun >= frequency) {
                this.data.add(new Serie(now, supplier.get()));
            }
        }

    }

    static class Serie {
        public final long timestamp;
        public final Object data;

        public Serie(long timestamp, Object data) {
            this.timestamp = timestamp;
            this.data = data;
        }
    }


}
