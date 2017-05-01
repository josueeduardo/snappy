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

import io.joshworks.snappy.ext.ExtensionMeta;
import io.joshworks.snappy.ext.ServerData;
import io.joshworks.snappy.ext.SnappyExtension;
import io.joshworks.snappy.handler.HandlerUtil;
import org.apache.commons.io.input.Tailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Josue on 07/02/2017.
 */
public class DashboardExtension implements SnappyExtension {

    private static final Logger logger = LoggerFactory.getLogger(DashboardExtension.class);

    private static final String EXTENSION_NAME = "DASHBOARD";
    private static final String PREFIX = "dashboard.";

    private static final String PASSWORD = PREFIX + "password";
    static final String LOG_LOCATION = PREFIX + "logFile";

    private static final String ADMIN_ROOT_FOLDER = "admin";
    private static final String DEFAULT_PATH = "/";
    private final String path;

    private static final String LOG_SSE = "/logs";


    public DashboardExtension() {
        this(DEFAULT_PATH);
    }

    public DashboardExtension(String path) {
        this.path = path;
    }

    private static final ExecutorService executor = Executors.newFixedThreadPool(3);
    private final Set<Tailer> tailers = new HashSet<>();

    @Override
    public void onStart(ServerData config) {

        config.adminManager.setAdminPage(HandlerUtil.staticFiles(path, ADMIN_ROOT_FOLDER, new ArrayList<>()));

        config.adminManager.getAdminEndpoints().add(HandlerUtil.sse(LOG_SSE, config.adminManager.getAdminInterceptors(), (connection, lastEventId) -> {

            Deque<String> params = connection.getQueryParameters().get("tailf");
            String tailf = params.isEmpty() ? Boolean.FALSE.toString() : params.getFirst();

            String logLocation = config.properties.getProperty(LOG_LOCATION);
            LogTailer listener = new LogTailer(false, logLocation);
            Tailer tailer = Tailer.create(listener.file, listener, 1000, Boolean.parseBoolean(tailf));
            connection.addCloseTask(channel -> {
                tailer.stop();
                tailers.remove(tailer);
            });
            executor.execute(tailer);
        }));
    }

    @Override
    public void onShutdown() {
        tailers.forEach(Tailer::stop);
        executor.shutdownNow();
    }

    @Override
    public ExtensionMeta details() {
        return new ExtensionMeta().name(EXTENSION_NAME).propertyPrefix(PREFIX);
    }

}
