package io.joshworks.snappy;

import io.joshworks.snappy.handler.HandlerUtil;
import io.joshworks.snappy.handler.MappedEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by josh on 3/11/17.
 */
public class Info {

    private static final String LOGO = "logo.txt";
    private static final String VERSION = "version.properties";
    private static final String VERSION_KEY = "version";

    private static final Logger logger = LoggerFactory.getLogger(Info.class);

    public static void logo() {
        BufferedReader br = new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(LOGO)));
        br.lines().forEach(System.err::println);
    }

    public static void version() {
        try {
            Properties properties = new Properties();
            properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(VERSION));
            System.err.println(String.format("Version: %s\n\n", properties.get(VERSION_KEY)));
        } catch (Exception ex) {

        }
    }


    public static void deploymentInfo(Config config, List<MappedEndpoint> endpoints, String basePath) {

        System.err.println("---------------- HTTP CONFIG ----------------");
        System.err.println(String.format("Bind address: %s", config.bindAddress));
        System.err.println(String.format("Port: %d", config.port));
        System.err.println(String.format("Http tracer : %b", config.httpTracer));
        System.err.println(String.format("Http metrics: %b", config.httpMetrics));

        System.err.println();

        System.err.println("--------------- SERVER CONFIG ---------------");
        config.optionBuilder.getMap().forEach(option -> {
            System.err.println(String.format("%s: %s", option.getName().replaceAll("_", " ").toLowerCase(), config.optionBuilder.getMap().get(option)));
        });

        System.err.println();

        System.err.println("------------- APP THREAD CONFIG -------------");
        if (config.executors.isEmpty() && config.schedulers.isEmpty()) {
            System.err.println("No executors configured (default will be used)");
        }
        config.executors.entrySet().forEach(entry -> logExecutors(entry.getKey(), entry.getValue()));
        config.schedulers.entrySet().forEach(entry -> logExecutors(entry.getKey(), entry.getValue()));

        System.err.println();
        System.err.println("----------------- ENDPOINTS -----------------");
        logEndpoints(endpoints, basePath);
        System.err.println();
    }

    private static void logEndpoints(List<MappedEndpoint> endpoints, String basePath) {
        endpoints.sort(Comparator.comparing(me -> me.url));
        for (MappedEndpoint endpoint : endpoints) {
            String ws = "";
            for (int i = 0; i < 10 - endpoint.method.length(); i++) {
                ws += " ";
            }
            String url = HandlerUtil.BASE_PATH.equals(basePath) ? endpoint.url : basePath + endpoint.url;
            System.err.println(String.format("%s%s", endpoint.method, ws + url));

        }
    }

    private static void logExecutors(String name, ThreadPoolExecutor executor) {
        System.err.println(String.format("Pool name: %s", name));
        System.err.println(String.format("   Core pool size: %d", executor.getCorePoolSize()));
        System.err.println(String.format("   Maximum pool size: %d", executor.getMaximumPoolSize()));
        System.err.println(String.format("   Queue size: %d", executor.getQueue().remainingCapacity()));
        System.err.println(String.format("   Rejection policy: %s", executor.getRejectedExecutionHandler().getClass().getSimpleName()));
    }

}
