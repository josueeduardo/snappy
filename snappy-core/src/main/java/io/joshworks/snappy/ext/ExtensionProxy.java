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

package io.joshworks.snappy.ext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Enumeration;
import java.util.Properties;
import java.util.ServiceLoader;

import static io.joshworks.snappy.property.PropertyKeys.RESERVED_PREFIXES;

/**
 * Created by Josh Gontijo on 3/25/17.
 */
public class ExtensionProxy implements SnappyExtension {

    private static final Logger logger = LoggerFactory.getLogger(ExtensionProxy.class);

    private ServiceLoader<SnappyExtension> extensions;


    public void load() {
        try {
            extensions = ServiceLoader.load(SnappyExtension.class);
            for (SnappyExtension extension : extensions) {
                String name = extension.details() == null ? "NOt_PROVIDED" : extension.details().name;
                logger.info("Extension found: " + name);
            }
        } catch (Throwable e) {
            logger.error("Error loading extension", e);
        }
    }

    @Override
    public void onStart(ServerData config) {
        if (extensions != null) {
            extensions.forEach(ext -> {
                config.properties = filter(ext.details(), config.properties);
                ext.onStart(config);
            });
        }
    }

    @Override
    public void onShutdown() {
        if (extensions != null) {
            extensions.forEach(SnappyExtension::onShutdown);
        }
    }

    @Override
    public ExtensionMeta details() {
        return new ExtensionMeta("EXTENSION-PROXY", null);
    }

    private Properties filter(ExtensionMeta meta, Properties original) {
        Properties properties = new Properties();
        if (meta == null) {
            return properties;
        }
        String prefix = meta.propertyPrefix;
        if (prefix == null || prefix.trim().isEmpty() || RESERVED_PREFIXES.contains(prefix)) {
            return properties;
        }
        Enumeration e = original.propertyNames();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            if (key.startsWith(prefix)) {
                properties.put(key, original.getProperty(key));
            }
        }
        return properties;
    }
}
