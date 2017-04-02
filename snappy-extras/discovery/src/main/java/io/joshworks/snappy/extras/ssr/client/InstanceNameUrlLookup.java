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

package io.joshworks.snappy.extras.ssr.client;

import io.joshworks.snappy.client.UrlLookup;
import io.joshworks.snappy.extras.ssr.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

import static io.joshworks.snappy.extras.ssr.SSRKeys.SSR_LOGGER;

/**
 * Created by Josh Gontijo on 4/2/17.
 */
public class InstanceNameUrlLookup extends UrlLookup {

    private final ServiceStore serviceStore;
    private static final String SCHEMA_SEPARATOR = "://";

    private static final Logger logger = LoggerFactory.getLogger(SSR_LOGGER);

    public InstanceNameUrlLookup(ServiceStore serviceStore) {
        this.serviceStore = serviceStore;
    }

    @Override
    public String getUrl(String original) {
        URI uri = URI.create(original);
        String serviceName = uri.getHost();
        Instance instance = serviceStore.get(serviceName);
        if (instance == null) {
            logger.debug("Service not found for name '{}', using URL as literal", original);
            return super.getUrl(original);
        }
        return uri.getScheme() + SCHEMA_SEPARATOR + instance.getAddress();
    }

}
