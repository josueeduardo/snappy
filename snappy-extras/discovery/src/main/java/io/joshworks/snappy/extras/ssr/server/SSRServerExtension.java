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

package io.joshworks.snappy.extras.ssr.server;

import io.joshworks.snappy.ext.ExtensionMeta;
import io.joshworks.snappy.ext.ServerData;
import io.joshworks.snappy.ext.SnappyExtension;
import io.joshworks.snappy.extras.ssr.client.ServiceRegister;
import io.joshworks.snappy.extras.ssr.client.ServiceStore;
import io.joshworks.snappy.extras.ssr.server.service.InstancesResource;
import io.joshworks.snappy.extras.ssr.server.service.ServiceResource;
import io.joshworks.snappy.handler.HandlerUtil;
import io.joshworks.snappy.handler.MappedEndpoint;
import io.undertow.util.Methods;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static io.joshworks.snappy.extras.ssr.SSRKeys.PROPERTY_PREFIX;
import static io.joshworks.snappy.parser.MediaTypes.consumes;
import static io.joshworks.snappy.parser.MediaTypes.produces;

/**
 * Created by Josh Gontijo on 3/29/17.
 */
public class SSRServerExtension implements SnappyExtension {

    private static final String EXTENSION_NAME = "SSR_SERVER";

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private ServiceRegister register;
    private final ServiceStore serviceStore = new ServiceStore();

    private static final String INSTANCES_URL = "/instances/{instanceId}";
    private static final String SERVICES_URL = "/services";
    private static final String SERVICES_NAME_URL = SERVICES_URL + "/{name}";

    @Override
    public void onStart(ServerData config) {



    }

    @Override
    public void onShutdown() {
        scheduler.shutdownNow();
    }

    @Override
    public ExtensionMeta details() {
        return new ExtensionMeta().name(EXTENSION_NAME).propertyPrefix(PROPERTY_PREFIX);
    }

    private MappedEndpoint createInstancesResource(ServerData config) {
        InstancesResource instancesResource = new InstancesResource(null, null);
        return HandlerUtil.rest(Methods.PUT,
                INSTANCES_URL,
                instancesResource::updateServiceState,
                config.exceptionMapper,
                config.interceptors,
                consumes("json"),
                produces("json"));
    }

    private List<MappedEndpoint> createServicesEndpoint(ServerData config) {
        ServiceResource serviceResource = new ServiceResource(null);


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
