package io.joshworks.simpletow.discovery.config;


import io.joshworks.simpletow.discovery.common.Instance;

import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by Josue on 21/06/2016.
 */
public class Configurator {

    private static final Logger logger = Logger.getLogger(Configurator.class.getName());

    private static PropertiesManager propertyManager;
    private static Instance instance;

    private Configurator() {
    }

    public static synchronized void initService(String name, boolean clientEnabled, boolean enableDiscovery) {
        propertyManager = new PropertiesManager(); //lazy load, so clients can do System.setProperty
        if (name == null || name.isEmpty()) {
            name = "UNKNOWN";
            logger.warning(":: Service name not specified, please verify @EnableClient or @EnableDiscovery ::");
        }
        name = name.replaceAll(" ", "-");

        String serviceAddress = getServiceUrl();

        instance = new Instance();
        instance.setClient(clientEnabled);
        instance.setDiscoverable(enableDiscovery);
        instance.setSince(new Date());
        instance.setAddress(serviceAddress);
        instance.setName(name);
        instance.setState(Instance.State.UP);
    }


    public static synchronized Instance getCurrentInstance() {
        if (instance == null) {
            throw new IllegalStateException("Configuration not initialised yet");
        }
        return instance;
    }

    public static synchronized boolean isInitialised() {
        return instance != null;
    }

    public static String getRegistryUrl() {
        String host = propertyManager.getRegistryHost();
        int port = propertyManager.getRegistryPort();

        host = host.substring(host.length() - 1).equals("/") ?
                host.substring(0, host.length() - 1)
                : host;
        host = host.replaceFirst("http://", "");
        host = host.replaceFirst("https://", "");


        return host + ":" + port;
    }


    private static String getServiceUrl() {
        String serviceHost = propertyManager.getServiceHost();
        int servicePort = propertyManager.getServicePort();

        if (serviceHost == null || serviceHost.isEmpty()) {
            throw new IllegalArgumentException("Service address cannot be null or empty");
        }

        String serviceUrl = serviceHost + ":" + servicePort;

        serviceUrl = serviceUrl.endsWith("/") ?
                serviceUrl.substring(0, serviceUrl.length() - 1) :
                serviceUrl;

        return verifyProtocol(serviceUrl);
    }

    private static String verifyProtocol(String address) {
        if (!address.startsWith("http://")
                && !address.startsWith("https://")
                && !address.startsWith("ws://")) {

            return "http://" + address;
        }
        return address;
    }
}
