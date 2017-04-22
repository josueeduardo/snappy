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

package io.joshworks.snappy.extensions.cblite;

import com.couchbase.lite.DatabaseOptions;
import com.couchbase.lite.Manager;
import io.joshworks.snappy.ext.ExtensionMeta;
import io.joshworks.snappy.ext.ServerData;
import io.joshworks.snappy.ext.SnappyExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Created by Josue on 07/02/2017.
 */
public class CouchbaseLiteExtension implements SnappyExtension {

    private static final Logger logger = LoggerFactory.getLogger(CouchbaseLiteExtension.class);

    private static final String EXTENSION_NAME = "COUCHBASE_LITE";
    private static final String PREFIX = "cblite.";

    private static final String PASSWORD = PREFIX + "password";
    private static final String LOCATION = PREFIX + "location";

    private static final String DEFAULT_LOCATION = System.getProperty("user.home") + "/snappy/cblite";
    private static final String DEFAULT_KEY = "snappy";

    private Manager manager;
    private DatabaseOptions options;

    public CouchbaseLiteExtension() {
    }

    public CouchbaseLiteExtension(Manager manager, DatabaseOptions options) {
        Objects.requireNonNull(manager, "Manager cannot be null");
        Objects.requireNonNull(options, "DatabaseOptions cannot be null");
        this.manager = manager;
        this.options = options;
    }

    @Override
    public void onStart(ServerData config) {
        try {
            if (manager != null && options != null) {
                CouchbaseStore.init(manager, options);
                return;
            }
            String location = String.valueOf(config.properties.getOrDefault(LOCATION, DEFAULT_LOCATION));
            String key = String.valueOf(config.properties.getOrDefault(PASSWORD, DEFAULT_KEY));

            manager = new Manager(new SnappyStoreContext(location), Manager.DEFAULT_OPTIONS);

            options = new DatabaseOptions();
            options.setCreate(true);
            options.setEncryptionKey(key);

            CouchbaseStore.init(manager, options);


        } catch (Exception ex) {
            logger.error("Error loading extension " + details().name, ex);
        }

    }

    @Override
    public void onShutdown() {
        manager.close();
    }

    @Override
    public ExtensionMeta details() {
        return new ExtensionMeta().name(EXTENSION_NAME).propertyPrefix(PREFIX);
    }

}
