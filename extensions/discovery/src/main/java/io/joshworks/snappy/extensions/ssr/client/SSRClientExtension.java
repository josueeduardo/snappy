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

package io.joshworks.snappy.extensions.ssr.client;

import io.joshworks.snappy.ext.ServerData;
import io.joshworks.snappy.ext.SnappyExtension;
import io.joshworks.snappy.extensions.ssr.Configuration;
import io.joshworks.snappy.extensions.ssr.Instance;
import io.joshworks.snappy.extensions.ssr.client.connector.SSEServiceRegister;
import io.joshworks.snappy.property.AppProperties;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by Josh Gontijo on 3/29/17.
 */
public class SSRClientExtension implements SnappyExtension {

    private static final String EXTENSION_NAME = "SSR_CLIENT";

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private ServiceRegister register;
    private final ServiceStore serviceStore = new ServiceStore();


    @Override
    public void onStart(ServerData config) {
        Configuration configuration = new Configuration(AppProperties.getProperties());
        Instance instance = configuration.configureInstance();

        Services.init(serviceStore);

        register = new SSEServiceRegister(serviceStore, instance, configuration.getRegistryUrl(), scheduler);
        register.bootstrap();
    }

    @Override
    public void onShutdown() {
        scheduler.shutdownNow();
    }

    @Override
    public String name() {
        return EXTENSION_NAME;
    }
}
