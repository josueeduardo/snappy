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

package io.joshworks.snappy.extras.ssr;


import io.joshworks.snappy.extras.ssr.client.locator.Discovery;
import io.joshworks.snappy.extras.ssr.client.locator.EC2Discovery;
import io.joshworks.snappy.extras.ssr.client.locator.LocalDiscovery;
import io.joshworks.snappy.property.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Properties;

import static io.joshworks.snappy.extras.ssr.SSRKeys.LOGGER_NAME;

/**
 * Created by Josue on 26/08/2016.
 */
public class Configuration {

    private static final Logger logger = LoggerFactory.getLogger(LOGGER_NAME);

    private static final String DEFAULT_REGISTRY_PORT = "9999";
    private static final String DEFAULT_REGISTRY_HOST = "localhost";
    private static final String DEFAULT_USE_HOST = "true";

    private final Discovery discovery;
    private final Properties properties;

    public Configuration(Properties properties) {
        this.properties = new Properties();
        this.properties.putAll(properties);
        discovery = isAws() ? new EC2Discovery() : new LocalDiscovery();
    }

    public Instance configureInstance() {
        String name = getAppName();
        boolean clientEnabled = isClientEnabled();
        boolean enableDiscovery = isDiscoverable();

        if (name == null || name.isEmpty()) {
            name = "UNKNOWN";
            logger.warn("Service name not specified, please use {}", SSRKeys.SSR_CLIENT_APP_NAME);
        }
        name = name.replaceAll(" ", "-");

        String serviceAddress = getClientUrl();

        Instance instance = new Instance();
        instance.setHost(getClientHost());
        instance.setPort(getClientPort());
        instance.setFetchServices(clientEnabled);
        instance.setDiscoverable(enableDiscovery);
        instance.setSince(new Date());
        instance.setName(name);
        instance.setState(Instance.State.UP);

        return instance;
    }

    public String getRegistryUrl() {
        String host = getRegistryHost();
        int port = getRegistryPort();

        host = host.substring(host.length() - 1).equals("/") ?
                host.substring(0, host.length() - 1)
                : host;
        host = host.replaceFirst("http://", "");
        host = host.replaceFirst("https://", "");


        return host + ":" + port;
    }

    //String name, boolean clientEnabled, boolean enableDiscovery
    private String getAppName() {
        return String.valueOf(AppProperties.resolveProperties(properties, SSRKeys.SSR_CLIENT_APP_NAME));
    }

    private boolean isClientEnabled() {
        String isClientEnabled = AppProperties.resolveProperties(properties, SSRKeys.SSR_CLIENT_ENABLED);
        return Boolean.parseBoolean(isClientEnabled);
    }

    private boolean isDiscoverable() {
        String isDiscoverable = AppProperties.resolveProperties(properties, SSRKeys.SSR_CLIENT_DISCOVERABLE);
        return Boolean.parseBoolean(isDiscoverable);
    }

    private int getRegistryPort() {
        String port = AppProperties.resolveProperties(properties, SSRKeys.SSR_REGISTRY_PORT);
        port = isEmpty(port) ? DEFAULT_REGISTRY_PORT : port;
        return Integer.parseInt(port);
    }

    /**
     * @return Snappy port if no ssr.service.port is found, in Docker container, this property must be used
     */
    private int getClientPort() {
        String port = AppProperties.resolveProperties(properties, SSRKeys.SSR_CLIENT_PORT);
        String serverPort = AppProperties.resolveProperties(properties, SSRKeys.SNAPPY_PORT);
        port = isEmpty(port) ? serverPort : port;
        return Integer.parseInt(port);
    }

    private boolean useHostname() {
        String useHost = AppProperties.resolveProperties(properties, SSRKeys.SSR_CLIENT_USE_HOSTNAME);
        useHost = isEmpty(useHost) ? DEFAULT_USE_HOST : useHost;
        return Boolean.parseBoolean(useHost);
    }

    private String getRegistryHost() {
        String registryUrl = AppProperties.resolveProperties(properties, SSRKeys.SSR_CLIENT_HOST);
        return registryUrl == null ? DEFAULT_REGISTRY_HOST : registryUrl;
    }

    private String getClientHost() {
        String key = SSRKeys.SSR_CLIENT_HOST;
        String host = AppProperties.resolveProperties(properties, key);
        if (isEmpty(host)) {
            boolean useHost = useHostname();
            String defaultHost = discovery.resolveHost(useHost);
            properties.put(key, defaultHost);
        }
        return AppProperties.resolveProperties(properties, key);

    }

    private boolean isAws() {
        return Boolean.parseBoolean(AppProperties.resolveProperties(properties, SSRKeys.SSR_CLIENT_ON_AWS));
    }

    private String getClientUrl() {
        String serviceHost = getClientHost();
        int servicePort = getClientPort();

        if (serviceHost == null || serviceHost.isEmpty()) {
            throw new IllegalArgumentException("Service address cannot be null or empty");
        }

        String serviceUrl = serviceHost + ":" + servicePort;

        serviceUrl = serviceUrl.endsWith("/") ?
                serviceUrl.substring(0, serviceUrl.length() - 1) :
                serviceUrl;

        return getProtocol(serviceUrl);
    }


    private String getProtocol(String address) {
        if (!address.startsWith("http://")
                && !address.startsWith("https://")
                && !address.startsWith("ws://")) {

            return "http://" + address; //tODO only http is supported at the moment
        }
        return address;
    }

    private boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

//    private String getProperty(String key) {
//        String fromFile = AppProperties.getProperty(key);
//        String fromEnv = fromSystemProperties(key);
//        return (fromEnv == null || fromEnv.isEmpty()) ? fromFile : fromEnv;
//    }
//
//    private String fromSystemProperties(String key) {
//        String value = System.getProperty(key);
//
//        if (value == null || value.isEmpty()) {
//            //replaces dot by underscore and to uppercase
//            //ex: service.port -> SSR_CLIENT_PORT
//            //so it can be used as System env
//            String convertedKeyFormat = key.replace(".", "_").toUpperCase();
//            value = System.getenv(convertedKeyFormat);
//        }
//        return value;
//    }

}
