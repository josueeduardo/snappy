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

package io.joshworks.snappy.extensions.dashboard;

import io.joshworks.snappy.ext.ServerData;
import io.joshworks.snappy.ext.SnappyExtension;
import io.joshworks.snappy.extensions.dashboard.log.LogStreamer;
import io.joshworks.snappy.extensions.dashboard.metrics.AppMetricsResource;
import io.joshworks.snappy.extensions.dashboard.resource.ResourceMetricHolder;
import io.joshworks.snappy.extensions.dashboard.resource.ResourcesMetricResource;
import io.joshworks.snappy.extensions.dashboard.resource.RestMetricsHandler;
import io.joshworks.snappy.extensions.dashboard.stats.ServerStats;
import io.joshworks.snappy.handler.HandlerUtil;
import io.joshworks.snappy.handler.MappedEndpoint;
import io.joshworks.snappy.property.AppProperties;
import io.joshworks.snappy.rest.ExceptionMapper;
import io.joshworks.snappy.rest.MediaType;
import io.undertow.util.Methods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

/**
 * Created by Josue on 07/02/2017.
 */
public class AdminExtension implements SnappyExtension {

    private static final Logger logger = LoggerFactory.getLogger(AdminExtension.class);

    private static final String EXTENSION_NAME = "DASHBOARD";
    private static final String PREFIX = "dashboard.";

    private static final String PASSWORD = PREFIX + "password";
    private static final String LOG_LOCATION = PREFIX + "logFile";

    private static final String ADMIN_ROOT_FOLDER = "admin";
    private static final String DEFAULT_PATH = "/";
    private final String path;

    private static final String LOG_SSE = "/logs";

    private static final String METRICS_ENDPOINT = "/metrics";
    private static final String METRICS_STATUS_ENDPOINT = METRICS_ENDPOINT + "/status";
    private static final String METRIC_ENDPOINT = METRICS_ENDPOINT + "/{id}";

    private static final String RESOURCES_METRIC_ENDPOINT = "/resources";
    private static final String RESOURCES_METRIC_STATUS_ENDPOINT = RESOURCES_METRIC_ENDPOINT + "/status";
    private static final String RESOURCE_METRIC_ENDPOINT = RESOURCES_METRIC_ENDPOINT + "/{id}";
    private static final String STATS_ENDPOINT = "/stats";

    public AdminExtension() {
        this(DEFAULT_PATH);
    }

    public AdminExtension(String path) {
        this.path = path;
    }

    private static final ExecutorService executor = Executors.newFixedThreadPool(3);
    public static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private LogStreamer streamer;

    @Override
    public void onStart(ServerData config) {
        config.adminManager.setAdminPage(path, ADMIN_ROOT_FOLDER, new ArrayList<>());

        registerStatsEndpoint(config);
        registerResourceMetricEndpoints(config);
        registerAppMetricsEndpoint(config);

        String logLocation = AppProperties.get(LOG_LOCATION).orElse(null);
        if (logLocation == null || logLocation.isEmpty()) {
            logger.warn(LOG_LOCATION + " not specified, log won't be available");
        } else {
            streamer = new LogStreamer(executor, logLocation);
        }
        config.adminManager.addEndpoint(HandlerUtil.sse(LOG_SSE, new ArrayList<>(), streamer));
    }

    @Override
    public void onShutdown() {
        if (streamer != null) {
            streamer.stopStreaming();
        }
        executor.shutdownNow();
        scheduler.shutdownNow();
    }

    @Override
    public String name() {
        return EXTENSION_NAME;
    }

    private void registerResourceMetricEndpoints(ServerData config) {
        ResourceMetricHolder resourceMetricHolder = wrapHandlerWithMetricsHandler(config);
        final ResourcesMetricResource endpoint = new ResourcesMetricResource(resourceMetricHolder);

        MappedEndpoint getMetrics = HandlerUtil.rest(
                Methods.GET,
                RESOURCES_METRIC_ENDPOINT,
                endpoint::getMetrics,
                new ExceptionMapper(),
                new ArrayList<>());

        MappedEndpoint getMetric = HandlerUtil.rest(
                Methods.GET,
                RESOURCE_METRIC_ENDPOINT,
                endpoint::getMetric,
                new ExceptionMapper(),
               new ArrayList<>());


        MappedEndpoint updateMetric = HandlerUtil.rest(
                Methods.PUT,
                RESOURCES_METRIC_STATUS_ENDPOINT,
                endpoint::updateMetric,
                new ExceptionMapper(),
               new ArrayList<>());


        MappedEndpoint metricStatus = HandlerUtil.rest(
                Methods.GET,
                RESOURCES_METRIC_STATUS_ENDPOINT,
                endpoint::metricStatus,
                new ExceptionMapper(),
               new ArrayList<>());


        config.adminManager.addEndpoint(getMetrics);
        config.adminManager.addEndpoint(getMetric);
        config.adminManager.addEndpoint(updateMetric);
        config.adminManager.addEndpoint(metricStatus);
    }

    private ResourceMetricHolder wrapHandlerWithMetricsHandler(ServerData config) {
        //Enable metrics only for REST
        Map<Boolean, List<MappedEndpoint>> splitResources = config.mappedEndpoints.stream()
                .collect(Collectors.partitioningBy(me -> MappedEndpoint.Type.REST.equals(me.type)));

        final ResourceMetricHolder resourceMetricHolder = new ResourceMetricHolder();
        //Replaces the handler reference to a wrapped ResMetricsHandler
        List<MappedEndpoint> restResources =
                splitResources.getOrDefault(true, new ArrayList<>())
                        .stream()
                        .map(me -> {
                            RestMetricsHandler restMetricsHandler = new RestMetricsHandler(me.handler);
                            resourceMetricHolder.add(me.method, me.url, restMetricsHandler);
                            return new MappedEndpoint(me.method, me.url, me.type, restMetricsHandler);
                        })
                        .collect(Collectors.toList());

        List<MappedEndpoint> replacedResources = new ArrayList<>();
        //don't replace non rest endpoint
        replacedResources.addAll(splitResources.getOrDefault(false, new ArrayList<>())); //non rest resources
        replacedResources.addAll(restResources); //wrapped RestMetricsHandler

        config.mappedEndpoints.clear();
        config.mappedEndpoints.addAll(replacedResources);

        return resourceMetricHolder;
    }

    private void registerStatsEndpoint(ServerData config) {

        MappedEndpoint getMetrics = HandlerUtil.rest(Methods.GET, STATS_ENDPOINT, exchange -> {
            exchange.send(new ServerStats(), MediaType.APPLICATION_JSON_TYPE);
        }, new ExceptionMapper(),new ArrayList<>());


        config.adminManager.addEndpoint(getMetrics);
    }

    private void registerAppMetricsEndpoint(ServerData config) {

        AppMetricsResource appMetricsResource = new AppMetricsResource();

        MappedEndpoint getMetrics = HandlerUtil.rest(
                Methods.GET,
                METRICS_ENDPOINT,
                appMetricsResource::getMetrics,
                new ExceptionMapper(),
               new ArrayList<>());


        MappedEndpoint getMetric = HandlerUtil.rest(
                Methods.GET,
                METRIC_ENDPOINT,
                appMetricsResource::getMetric,
                new ExceptionMapper(),
               new ArrayList<>());


        MappedEndpoint updateMetrics = HandlerUtil.rest(
                Methods.PUT,
                METRICS_STATUS_ENDPOINT,
                appMetricsResource::updateMetricState,
                new ExceptionMapper(),
               new ArrayList<>());


        MappedEndpoint metricsStatus = HandlerUtil.rest(
                Methods.GET,
                METRICS_STATUS_ENDPOINT,
                appMetricsResource::updateMetricState,
                new ExceptionMapper(),
               new ArrayList<>());


        config.adminManager.addEndpoint(getMetrics);
        config.adminManager.addEndpoint(getMetric);
        config.adminManager.addEndpoint(updateMetrics);
        config.adminManager.addEndpoint(metricsStatus);

    }

}
