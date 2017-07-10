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

package io.joshworks.snappy.extensions.mvstore;

import io.joshworks.snappy.ext.ServerData;
import io.joshworks.snappy.ext.SnappyExtension;
import io.joshworks.snappy.property.AppProperties;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.OffHeapStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by Josue on 07/02/2017.
 */
public class MvStoreExtension implements SnappyExtension {

    private static final Logger logger = LoggerFactory.getLogger(MvStoreExtension.class);

    private static final String EXTENSION_NAME = "H2_MV_STORE";
    private static final String PREFIX = "mvstore.";

    private static final String PASSWORD = PREFIX + "password";
    private static final String LOCATION = PREFIX + "location";
    private static final String AUTO_COMMIT = PREFIX + "autoCommit";
    private static final String CACHE_SIZE = PREFIX + "cacheSize";
    private static final String OFF_HEAP_MODE = PREFIX + "offHeapMode";

    private static final String DEFAULT_LOCATION =
            System.getProperty("user.home") + File.separator + "snappy" + File.separator + "mvstore";
    private static final String DEFAULT_KEY = "snappy";
    private static final String DEFAULT_AUTO_COMMIT = "true";
    private static final String DEFAULT_OFF_HEAP_MODE = "false"; //not set
    private static final String DEFAULT_CACHE_SIZE = "-1"; //not set
    private static final String DATABASE_NAME = File.separator + "db.dat"; //not set

    private MVStore store;

    public MvStoreExtension() {
    }

    public MvStoreExtension(MVStore store) {
        this.store = store;
    }

    @Override
    public void onStart(ServerData config) {
        try {
            if (store != null) {
                return;
            }
            String location = String.valueOf(AppProperties.get(LOCATION).orElse(DEFAULT_LOCATION));
            String key = String.valueOf(AppProperties.get(PASSWORD).orElse(DEFAULT_KEY));
            boolean autoCommit = Boolean.parseBoolean(String.valueOf(AppProperties.get(AUTO_COMMIT).orElse(DEFAULT_AUTO_COMMIT)));
            boolean offHeap = Boolean.parseBoolean(String.valueOf(AppProperties.get(OFF_HEAP_MODE).orElse(DEFAULT_OFF_HEAP_MODE)));
            int cacheSize = Integer.parseInt(String.valueOf(AppProperties.get(CACHE_SIZE).orElse(DEFAULT_CACHE_SIZE)));

            location = location.endsWith(File.separator) ? location.substring(0, location.length() - 1) : location;
            createFolder(location);

            MVStore.Builder builder = new MVStore.Builder()
                    .fileName(location + DATABASE_NAME)
                    .encryptionKey(key.toCharArray())
                    .compress();


            if (offHeap) {
                builder.fileStore(new OffHeapStore());
            }
            if (!autoCommit) {
                builder.autoCommitDisabled();
            }
            if (cacheSize >= 0) {
                builder.cacheSize(cacheSize);
            }
            this.store = builder.open();

            H2MvStore.init(store);


        } catch (Exception ex) {
            logger.error("Error loading extension " + EXTENSION_NAME, ex);
        }
    }

    private void createFolder(String dirPath) throws Exception {
        File dataDir = new File(dirPath);
        if (!dataDir.exists()) {
            if (!dataDir.mkdirs()) {
                throw new IllegalStateException("Could not create data directory '" + dirPath + "'");
            }
        }
    }

    @Override
    public void onShutdown() {
        store.close();
    }

    @Override
    public String name() {
        return EXTENSION_NAME;
    }

}
