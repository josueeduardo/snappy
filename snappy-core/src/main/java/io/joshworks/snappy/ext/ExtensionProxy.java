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

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import static io.joshworks.snappy.SnappyServer.*;
import static io.joshworks.snappy.property.PropertyKeys.RESERVED_PREFIXES;

/**
 * Created by Josh Gontijo on 3/25/17.
 */
public class ExtensionProxy implements SnappyExtension {

    private static final Logger logger = LoggerFactory.getLogger(LOGGER_NAME);

    private final List<SnappyExtension> extensions = new ArrayList<>();


    public void load() {
        new FastClasspathScanner().matchClassesImplementing(SnappyExtension.class, aClass -> {
            try {
                if (!aClass.isAssignableFrom(this.getClass())) {
                    SnappyExtension extension = aClass.newInstance();
                    String name = extension.details() == null ? "NOt_PROVIDED" : extension.details().name;
                    logger.info("Extension found: " + name);
                    extensions.add(extension);
                }

            } catch (InstantiationException | IllegalAccessException e) {
                logger.error("Error while loading extension", e);
            }

        }).scan();
    }

    @Override
    public void onStart(ServerData config) {
        extensions.forEach(ext -> {
            config.properties = filter(ext.details(), config.properties);
            ext.onStart(config);
        });
    }

    @Override
    public void onShutdown() {
        extensions.forEach(SnappyExtension::onShutdown);
    }

    @Override
    public ExtensionMeta details() {
        return null;
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
