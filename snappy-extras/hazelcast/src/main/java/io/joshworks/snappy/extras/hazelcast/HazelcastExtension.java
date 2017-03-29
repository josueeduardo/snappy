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

package io.joshworks.snappy.extras.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import io.joshworks.snappy.ext.ExtensionMeta;
import io.joshworks.snappy.ext.ServerData;
import io.joshworks.snappy.ext.SnappyExtension;

/**
 * Created by Josh Gontijo on 3/29/17.
 */
public class HazelcastExtension implements SnappyExtension {

    private static final String EXTENSION_NAME = "HAZELCAST";
    private static final String PROPERTY_PREFIX = "hazelcast";

    private final Config hazelcastConfig;
    private HazelcastInstance hazelcastInstance;

    public HazelcastExtension() {
        hazelcastConfig = new Config();
    }

    public HazelcastExtension(Config hazelcastConfig) {
        this.hazelcastConfig = hazelcastConfig;
    }

    @Override
    public void onStart(ServerData config) {
        hazelcastInstance = Hazelcast.newHazelcastInstance(hazelcastConfig);
    }

    @Override
    public void onShutdown() {
        hazelcastInstance.shutdown();
    }

    @Override
    public ExtensionMeta details() {
        return new ExtensionMeta().name(EXTENSION_NAME).propertyPrefix(PROPERTY_PREFIX);
    }
}
