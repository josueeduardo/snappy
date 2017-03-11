package io.joshworks.microserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by josh on 3/11/17.
 */
public class Info {

    private static final Logger logger = LoggerFactory.getLogger(Info.class);

    public static void deploymentInfo(Config config) {

        logger.info("-------------------- HTTP CONFIG --------------------");
        logger.info("Bind address: {}", config.bindAddress);
        logger.info("Port: {}", config.port);
        logger.info("Http tracer : {}", config.httpTracer);
        logger.info("Http metrics: {}", config.httpMetrics);

        logger.info("---------------- SERVER CONFIG ---------------");
        config.optionBuilder.getMap().forEach(option -> {
            logger.info("{}: {}", option.getName(), config.optionBuilder.getMap().get(option));
        });

        logger.info("----------------- APP THREAD CONFIG -----------------");
        if (config.executors.isEmpty() && config.schedulers.isEmpty()) {
            logger.info("No executors configured");
        }
        config.executors.entrySet().forEach(entry -> logExecutors(entry.getKey(), entry.getValue()));
        config.schedulers.entrySet().forEach(entry -> logExecutors(entry.getKey(), entry.getValue()));

        logger.info("-------------------- ENDPOINTS --------------------");

        logEndpoints(new ArrayList<>(Endpoint.mappedEndpoints));

    }

    private static void logEndpoints(List<MappedEndpoint> endpoints) {
        endpoints.sort(Comparator.comparing(me -> me.url));
        for (MappedEndpoint endpoint : endpoints) {
            String ws = "";
            for (int i = 0; i < 10 - endpoint.method.length(); i++) {
                ws += " ";
            }
            logger.info("{}{}", endpoint.method, ws + endpoint.url);

        }
    }

    private static void logExecutors(String name, ThreadPoolExecutor executor) {
        logger.info("Pool name: {}", name);
        logger.info("   Core pool size: {}", executor.getCorePoolSize());
        logger.info("   Maximum pool size: {}", executor.getMaximumPoolSize());
        logger.info("   Queue size: {}", executor.getQueue().remainingCapacity());
        logger.info("   Rejection policy: {}", executor.getRejectedExecutionHandler().getClass().getSimpleName());
    }

}
