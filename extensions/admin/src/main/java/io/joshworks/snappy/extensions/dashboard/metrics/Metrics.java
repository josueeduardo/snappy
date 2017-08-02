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

package io.joshworks.snappy.extensions.dashboard.metrics;

import io.joshworks.snappy.extensions.dashboard.AdminExtension;
import io.joshworks.snappy.extensions.dashboard.TimeMetric;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by josh on 3/11/17.
 */
public class Metrics {

    //suppliers
    private static final Map<String, TimeMetric<Number>> numberSuppliers = new ConcurrentHashMap<>();
    private static final Map<String, TimeMetric<Boolean>> booleanSuppliers = new ConcurrentHashMap<>();

    private static final long UPDATE_FREQUENCY = Duration.ofMinutes(1).toMillis();

    private static boolean started = false;
    private static boolean enabled = true;

    private Metrics() {
    }

    public static void supplyNumber(String key, long frequency, Supplier<Number> supplier) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        numberSuppliers.put(key, new TimeMetric<>(key, frequency, supplier));

        tryStartMetricsUpdater();
    }

    public static void supplyBoolean(String key, long frequency, Supplier<Boolean> supplier) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        booleanSuppliers.put(key, new TimeMetric<>(key, frequency, supplier));

        tryStartMetricsUpdater();
    }


    public synchronized static void setEnabled(boolean enabled) {
        Metrics.enabled = enabled;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    private static synchronized void tryStartMetricsUpdater() {
        if (!started) {
            AdminExtension.scheduler.scheduleAtFixedRate(() -> {
                if (enabled) {
                    List<TimeMetric<?>> suppliers = new ArrayList<>();
                    suppliers.addAll(numberSuppliers.values());
                    suppliers.addAll(booleanSuppliers.values());

                    suppliers.forEach(TimeMetric::update);
                }

            }, UPDATE_FREQUENCY, UPDATE_FREQUENCY, TimeUnit.MILLISECONDS);
            started = true;
        }
    }

    static Map<String, Object> getData() {
        Map<String, TimeMetric<?>> data = new HashMap<>();
        data.putAll(numberSuppliers);
        data.putAll(booleanSuppliers);

        return data.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getData()));
    }

    static Queue<TimeMetric.Serie> getData(String key) {
        TimeMetric<Number> numberMetric = numberSuppliers.get(key);
        if (numberMetric != null) {
            return numberMetric.getData();
        }
        TimeMetric<Boolean> booleanSupplier = booleanSuppliers.get(key);
        return booleanSupplier == null ? new LinkedList<>() : booleanSupplier.getData();
    }

}
