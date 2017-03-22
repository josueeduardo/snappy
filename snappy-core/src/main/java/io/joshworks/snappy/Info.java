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

package io.joshworks.snappy;

import io.joshworks.snappy.executor.ExecutorConfig;
import io.joshworks.snappy.executor.SchedulerConfig;
import io.joshworks.snappy.handler.HandlerUtil;
import io.joshworks.snappy.handler.MappedEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.OptionMap;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ThreadPoolExecutor;

import static io.joshworks.snappy.SnappyServer.*;

/**
 * Created by josh on 3/11/17.
 */
public class Info {

    private static final String LOGO = "logo.txt";
    private static final String VERSION = "version.properties";
    private static final String VERSION_KEY = "version";

    private static final Logger logger = LoggerFactory.getLogger(LOGGER_NAME);

    public static void logo() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(LOGO)))) {
            br.lines().forEach(System.err::println);
        } catch (Exception e) {
            //do nothing
        }
    }

    public static void version() {
        try {
            Properties properties = new Properties();
            properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(VERSION));
            System.err.println(String.format("Version: %s%n%n", properties.get(VERSION_KEY)));
        } catch (Exception ex) {
            //do nothing
        }
    }


    public static void deploymentInfo(boolean bindAddress,
                                      boolean httpTracer,
                                      int port,
                                      boolean httpMetrics,
                                      List<ExecutorConfig> executorConfig,
                                      List<SchedulerConfig> schedulerConfig,
                                      OptionMap.Builder options,
                                      List<MappedEndpoint> endpoints, String basePath) {

        System.err.println("---------------- HTTP CONFIG ----------------");
        System.err.println(String.format("Bind address: %s", bindAddress));
        System.err.println(String.format("Port: %d", port));
        System.err.println(String.format("Http tracer : %b", httpTracer));
        System.err.println(String.format("Http metrics: %b", httpMetrics));

        System.err.println();

        System.err.println("--------------- SERVER CONFIG ---------------");
        options.getMap().forEach(option -> {
            System.err.println(String.format("%s: %s", option.getName().replaceAll("_", " ").toLowerCase(), options.getMap().get(option)));
        });

        System.err.println();

        System.err.println("------------- APP THREAD CONFIG -------------");
        if (executorConfig.isEmpty() && schedulerConfig.isEmpty()) {
            System.err.println("No executors configured (default will be used)");
        }
        executorConfig.forEach(exec -> logExecutors(exec.getName(), exec.getExecutor()));
        schedulerConfig.forEach(entry -> logExecutors(entry.getName(), entry.getScheduler()));

        System.err.println();
        System.err.println("----------------- ENDPOINTS -----------------");
        logEndpoints(endpoints, basePath);
        System.err.println();
    }

    private static void logEndpoints(List<MappedEndpoint> endpoints, String basePath) {
        endpoints.sort(Comparator.comparing(me -> me.url));
        for (MappedEndpoint endpoint : endpoints) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < 10 - endpoint.method.length(); i++) {
                sb.append(" ");
            }
            String url = HandlerUtil.BASE_PATH.equals(basePath) ? endpoint.url : basePath + endpoint.url;
            System.err.println(String.format("%s%s", endpoint.method, sb.toString() + url));

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
