package io.joshworks.snappy.metric;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by josh on 3/11/17.
 */
public class Metrics {

    private static final Map<String, Supplier<Object>> lazyProperty = new ConcurrentHashMap<>();
    private static final Map<String, Object> properties = new ConcurrentHashMap<>();



    private Metrics() {
    }


    public static void addMetric(String key, Object value) {
        properties.put(key, value);
    }

    public static void addMetric(String key, Supplier<Object> value) {
        lazyProperty.put(key, value);
    }

    static Map<String, Object> getData() {
        Map<String, Object> data = new HashMap<>();
        data.putAll(properties);
        Map<String, Object> collect = lazyProperty.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, val -> val.getValue().get()));
        data.putAll(collect);
        return data;
    }

}
