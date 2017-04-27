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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Created by Josue on 07/02/2017.
 */
public class DashboardExtension implements SnappyExtension {

    private static final Logger logger = LoggerFactory.getLogger(DashboardExtension.class);

    private static final String EXTENSION_NAME = "DASHBOARD";
    private static final String PREFIX = "dashboard.";

    private static final String PASSWORD = PREFIX + "password";


    public DashboardExtension() {
    }


    @Override
    public void onStart(ServerData config) {
        config.adminEndpoints.add(HandlerUtil.staticFiles("/admin", "admin", new ArrayList<>()));
    }

    @Override
    public void onShutdown() {
    }

    @Override
    public ExtensionMeta details() {
        return new ExtensionMeta().name(EXTENSION_NAME).propertyPrefix(PREFIX);
    }

}
