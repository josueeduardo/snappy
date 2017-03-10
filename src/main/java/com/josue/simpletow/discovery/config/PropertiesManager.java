package com.josue.simpletow.discovery.config;


import com.josue.simpletow.discovery.locator.Discovery;
import com.josue.simpletow.discovery.locator.EC2Discovery;
import com.josue.simpletow.discovery.locator.LocalDiscovery;
import com.josue.simpletow.property.PropertyLoader;

import static com.josue.simpletow.property.MicroserverProperties.SSR_AWS;
import static com.josue.simpletow.property.MicroserverProperties.SSR_REGISTRY_HOST;
import static com.josue.simpletow.property.MicroserverProperties.SSR_REGISTRY_PORT;
import static com.josue.simpletow.property.MicroserverProperties.SSR_SERVICE_HOST;
import static com.josue.simpletow.property.MicroserverProperties.SSR_SERVICE_PORT;
import static com.josue.simpletow.property.MicroserverProperties.SSR_USE_HOST;

/**
 * Created by Josue on 26/08/2016.
 */
public class PropertiesManager {

    private static final String DEFAULT_REGISTRY_PORT = "9090";
    private static final String DEFAULT_SERVICE_PORT = "8080";
    private static final String DEFAULT_USE_HOST = "true";

    private final Discovery discovery;

    public PropertiesManager() {

        discovery = isAws() ? new EC2Discovery() : new LocalDiscovery();
    }

    public int getRegistryPort() {
        String port =  PropertyLoader.getProperty(SSR_REGISTRY_PORT);
        port = isEmpty(port) ? DEFAULT_REGISTRY_PORT : port;
        return Integer.parseInt(port);
    }

    public int getServicePort() {
        String port =  PropertyLoader.getProperty(SSR_SERVICE_PORT);
        port = isEmpty(port) ? DEFAULT_SERVICE_PORT : port;
        return Integer.parseInt(port);
    }

    public boolean useHostname() {
        String useHost =  PropertyLoader.getProperty(SSR_USE_HOST);
        useHost = isEmpty(useHost) ? DEFAULT_USE_HOST : useHost;
        return Boolean.parseBoolean(useHost);
    }

    public String getRegistryHost() {
        return getHost(SSR_REGISTRY_HOST);
    }

    public String getServiceHost() {
        return getHost(SSR_SERVICE_HOST);
    }


    private String getHost(String key) {
        String host = PropertyLoader.getProperty(key);
        if (isEmpty(host)) {
            boolean useHost = useHostname();
            String defaultHost = discovery.resolveHost(useHost);
            PropertyLoader.getProperties().put(key, defaultHost);
        }
        return  PropertyLoader.getProperty(key);
    }

    public boolean isAws() {
        return Boolean.parseBoolean(PropertyLoader.getProperty(SSR_AWS));
    }

//    private String getProperty(String key) {
//        String fromFile = PropertyLoader.getProperty(key);
//        String fromEnv = fromSystemProperties(key);
//        return (fromEnv == null || fromEnv.isEmpty()) ? fromFile : fromEnv;
//    }
//
//    private String fromSystemProperties(String key) {
//        String value = System.getProperty(key);
//
//        if (value == null || value.isEmpty()) {
//            //replaces dot by underscore and to uppercase
//            //ex: service.port -> SSR_SERVICE_PORT
//            //so it can be used as System env
//            String convertedKeyFormat = key.replace(".", "_").toUpperCase();
//            value = System.getenv(convertedKeyFormat);
//        }
//        return value;
//    }

    private boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }


}
