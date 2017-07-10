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

package io.joshworks.snappy.extensions.ssr.server;

import io.joshworks.snappy.executor.AppExecutors;
import io.joshworks.snappy.ext.ServerData;
import io.joshworks.snappy.ext.SnappyExtension;
import io.joshworks.snappy.extensions.ssr.server.service.InstancesResource;
import io.joshworks.snappy.extensions.ssr.server.service.ServiceControl;
import io.joshworks.snappy.extensions.ssr.server.service.ServiceResource;
import io.joshworks.snappy.extensions.ssr.server.sse.Hearbeat;
import io.joshworks.snappy.extensions.ssr.server.sse.ServiceMonitor;
import io.joshworks.snappy.handler.HandlerUtil;
import io.joshworks.snappy.handler.MappedEndpoint;
import io.undertow.util.Methods;

import java.util.Arrays;
import java.util.List;

import static io.joshworks.snappy.parser.MediaTypes.consumes;
import static io.joshworks.snappy.parser.MediaTypes.produces;

/**
 * Created by Josh Gontijo on 3/29/17.
 */
public class SSRServerExtension implements SnappyExtension {

    private static final String EXTENSION_NAME = "SSR_SERVER";

    private Hearbeat hearbeat;
    private int ackPeriod = 5;//TODO move to properties

    private final ServiceControl serviceControl = new ServiceControl();


    //TODO move to common
    public static final String INSTANCES_URL = "/instances";
    public static final String MONITOR_URL = "/monitor";
    public static final String SERVICES_URL = "/services";
    public static final String SERVICES_NAME_URL = SERVICES_URL + "/{name}";

    @Override
    public void onStart(ServerData config) {
        config.mappedEndpoints.addAll(instanceResource(config));
        config.mappedEndpoints.addAll(serviceEndpoint(config));

        config.mappedEndpoints.add(serviceMonitor(config));

        hearbeat = new Hearbeat(AppExecutors.scheduler(), ackPeriod);
        hearbeat.start();

    }

    @Override
    public void onShutdown() {
        hearbeat.stop();
    }

    @Override
    public String name() {
        return EXTENSION_NAME;
    }

    private MappedEndpoint serviceMonitor(ServerData config) {
        final ServiceMonitor monitor = new ServiceMonitor(serviceControl);
        return HandlerUtil.sse(MONITOR_URL + "/{instanceId}", config.interceptors, monitor);
    }

    private List<MappedEndpoint> instanceResource(ServerData config) {
        final InstancesResource instanceResource = new InstancesResource(serviceControl);

        MappedEndpoint updateInstance = HandlerUtil.rest(Methods.PUT,
                INSTANCES_URL + "/{instanceId}",
                instanceResource::updateServiceState,
                config.exceptionMapper,
                config.interceptors,
                consumes("json"),
                produces("json"));


        MappedEndpoint register = HandlerUtil.rest(Methods.POST,
                INSTANCES_URL,
                instanceResource::register,
                config.exceptionMapper,
                config.interceptors,
                consumes("json"),
                produces("json"));

        return Arrays.asList(register, updateInstance);

    }

    private List<MappedEndpoint> serviceEndpoint(ServerData config) {
        final ServiceResource serviceResource = new ServiceResource(serviceControl);


        MappedEndpoint getServices = HandlerUtil.rest(Methods.GET, SERVICES_URL, serviceResource::getServices,
                config.exceptionMapper,
                config.interceptors,
                consumes("json"),
                produces("json"));


        MappedEndpoint getService = HandlerUtil.rest(Methods.GET, SERVICES_NAME_URL, serviceResource::getService,
                config.exceptionMapper,
                config.interceptors,
                consumes("json"),
                produces("json"));

        MappedEndpoint addLink = HandlerUtil.rest(Methods.PUT, SERVICES_NAME_URL, serviceResource::addLink,
                config.exceptionMapper,
                config.interceptors,
                consumes("json"),
                produces("json"));

        return Arrays.asList(getService, getServices, addLink);
    }
}
